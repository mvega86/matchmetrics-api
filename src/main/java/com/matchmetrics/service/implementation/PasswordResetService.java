package com.matchmetrics.service.implementation;

import com.matchmetrics.mapper.dto.auth.ForgotPasswordMethodsResponse;
import com.matchmetrics.mapper.dto.auth.ResetPasswordRequest;
import com.matchmetrics.mapper.dto.auth.SendResetCodeRequest;
import com.matchmetrics.persistence.entity.AppUser;
import com.matchmetrics.persistence.entity.PasswordResetToken;
import com.matchmetrics.persistence.repository.AppUserRepository;
import com.matchmetrics.persistence.repository.PasswordResetTokenRepository;
import com.matchmetrics.service.IPasswordResetService;
import com.matchmetrics.service.notification.EmailNotificationService;
import com.matchmetrics.service.notification.SmsNotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetService implements IPasswordResetService {

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 15;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${app.password-reset.max-attempts:3}")
    private int maxAttemptsPerWindow;

    private final AppUserRepository appUserRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailNotificationService emailService;
    private final SmsNotificationService smsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ForgotPasswordMethodsResponse getAvailableMethods(String email) {
        Optional<AppUser> userOpt = appUserRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            // P6: no revelar si el email existe
            return new ForgotPasswordMethodsResponse(List.of("EMAIL"), null, null);
        }

        AppUser user = userOpt.get();
        List<String> methods = new ArrayList<>();
        methods.add("EMAIL");

        // P3+P4: SMS solo si usuario tiene teléfono Y proveedor está habilitado
        if (smsService.isEnabled()
                && user.getPhone() != null
                && !user.getPhone().isBlank()) {
            methods.add("SMS");
        }

        return new ForgotPasswordMethodsResponse(
                methods,
                maskEmail(user.getEmail()),
                user.getPhone() != null ? maskPhone(user.getPhone()) : null
        );
    }

    @Override
    @Transactional
    public void sendResetCode(SendResetCodeRequest request) {
        Optional<AppUser> userOpt = appUserRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            // P6: responder como si todo fuera bien — no revelar existencia de cuenta
            return;
        }

        AppUser user = userOpt.get();
        String method = request.getMethod().toUpperCase();

        if ("SMS".equals(method)) {
            if (!smsService.isEnabled()) {
                throw new IllegalArgumentException("SMS not available. Please use email.");
            }
            if (user.getPhone() == null || user.getPhone().isBlank()) {
                throw new IllegalArgumentException("SMS not available: no phone number on file.");
            }
        }

        // P7: rate limit — máximo maxAttemptsPerWindow códigos en la ventana de 15 min
        long recent = tokenRepository.countRecentByEmail(
                request.getEmail(), LocalDateTime.now().minusMinutes(EXPIRY_MINUTES));
        if (recent >= maxAttemptsPerWindow) {
            throw new IllegalArgumentException(
                    "Too many reset attempts. Please wait before requesting a new code.");
        }

        tokenRepository.invalidateAllForUser(user.getId());

        String code = generateCode();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setCode(code);
        token.setMethod(method);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
        tokenRepository.save(token);

        if ("SMS".equals(method)) {
            smsService.send(user.getPhone(), user.getFullName(), code);
        } else {
            emailService.send(user.getEmail(), user.getFullName(), code);
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // P5: validar por email + código para evitar colisiones entre usuarios
        PasswordResetToken token = tokenRepository
                .findByUserEmailAndCodeAndUsedFalse(request.getEmail(), request.getCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or already used code."));

        if (token.isExpired()) {
            throw new IllegalArgumentException("Code has expired. Please request a new one.");
        }

        AppUser user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        appUserRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);
    }

    private String generateCode() {
        int num = RANDOM.nextInt(1_000_000);
        return String.format("%06d", num);
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 2) return email;
        return email.substring(0, 2) + "***" + email.substring(at);
    }

    private String maskPhone(String phone) {
        if (phone.length() <= 4) return "***";
        return "***" + phone.substring(phone.length() - 4);
    }
}

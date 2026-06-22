package com.matchmetrics.service.implementation;

import com.matchmetrics.mapper.dto.auth.ForgotPasswordMethodsResponse;
import com.matchmetrics.mapper.dto.auth.ResetPasswordRequest;
import com.matchmetrics.mapper.dto.auth.SendResetCodeRequest;
import com.matchmetrics.persistence.entity.AppUser;
import com.matchmetrics.persistence.entity.PasswordResetToken;
import com.matchmetrics.persistence.repository.AppUserRepository;
import com.matchmetrics.persistence.repository.PasswordResetTokenRepository;
import com.matchmetrics.service.IPasswordResetService;
import com.matchmetrics.service.notification.INotificationService;
import com.matchmetrics.exception.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PasswordResetService implements IPasswordResetService {

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 15;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AppUserRepository appUserRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final INotificationService notificationService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ForgotPasswordMethodsResponse getAvailableMethods(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("No account found for: " + email));

        List<String> methods = new ArrayList<>();
        methods.add("EMAIL");
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
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
        AppUser user = appUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("No account found for: " + request.getEmail()));

        String method = request.getMethod().toUpperCase();
        if ("SMS".equals(method) && (user.getPhone() == null || user.getPhone().isBlank())) {
            throw new IllegalArgumentException("SMS not available: no phone number on file.");
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
            notificationService.sendBySms(user.getPhone(), user.getFullName(), code);
        } else {
            notificationService.sendByEmail(user.getEmail(), user.getFullName(), code);
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = tokenRepository.findByCodeAndUsedFalse(request.getCode())
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

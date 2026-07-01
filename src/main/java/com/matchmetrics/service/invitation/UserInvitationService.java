package com.matchmetrics.service.invitation;

import com.matchmetrics.mapper.dto.auth.InvitationValidateResponse;
import com.matchmetrics.persistence.entity.UserInvitation;
import com.matchmetrics.persistence.repository.UserInvitationRepository;
import com.matchmetrics.service.notification.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserInvitationService {

    private static final Logger log = LoggerFactory.getLogger(UserInvitationService.class);
    private static final int EXPIRY_HOURS = 72;

    private final UserInvitationRepository invitationRepository;
    private final EmailNotificationService emailService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public void sendInvitation(String email) {
        String normalizedEmail = email.toLowerCase().trim();

        invitationRepository.findActiveByEmail(normalizedEmail, LocalDateTime.now())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Ya existe una invitación vigente para ese correo. Expira en " + EXPIRY_HOURS + " horas.");
                });

        String token = UUID.randomUUID().toString();
        UserInvitation invitation = new UserInvitation();
        invitation.setEmail(normalizedEmail);
        invitation.setToken(token);
        invitation.setExpiresAt(LocalDateTime.now().plusHours(EXPIRY_HOURS));
        invitationRepository.save(invitation);

        String inviteUrl = frontendUrl + "/register?token=" + token;
        emailService.sendInvitation(normalizedEmail, inviteUrl);

        log.info("[INVITATION] Invitación enviada a {}", normalizedEmail);
    }

    public InvitationValidateResponse validateToken(String token) {
        UserInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitación no encontrada"));

        if (!invitation.isValid()) {
            throw new ResponseStatusException(HttpStatus.GONE, "La invitación ha expirado o ya fue utilizada");
        }

        return new InvitationValidateResponse(invitation.getEmail());
    }

    public UserInvitation consumeToken(String token) {
        UserInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token de invitación inválido"));

        if (!invitation.isValid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La invitación ha expirado o ya fue utilizada");
        }

        invitation.setUsed(true);
        return invitationRepository.save(invitation);
    }
}

package com.matchmetrics.service.implementation;

import com.matchmetrics.persistence.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenCleanupService {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupService.class);

    private final PasswordResetTokenRepository tokenRepository;

    // Cada hora — elimina tokens expirados o usados con más de 24 horas
    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void deleteExpiredTokens() {
        long deleted = tokenRepository.deleteOldTokens();
        if (deleted > 0) {
            log.info("[TOKEN-CLEANUP] {} tokens expirados/usados eliminados", deleted);
        }
    }
}

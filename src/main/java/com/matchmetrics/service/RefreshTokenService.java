package com.matchmetrics.service;

import com.matchmetrics.persistence.entity.AppUser;
import com.matchmetrics.persistence.entity.RefreshToken;
import com.matchmetrics.persistence.repository.AppUserRepository;
import com.matchmetrics.persistence.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppUserRepository appUserRepository;

    @Value("${app.security.jwt.refresh-expiration}")
    private long refreshExpirationMs;

    @Transactional
    public String createForUser(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        refreshTokenRepository.revokeAllByUser(user);

        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
        token.setRevoked(false);
        return refreshTokenRepository.save(token).getToken();
    }

    public record RefreshResult(AppUser user, String newRefreshToken) {}

    @Transactional
    public RefreshResult consume(String tokenStr) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido"));

        if (token.isRevoked() || token.isExpired()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expirado o revocado");
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);

        AppUser user = token.getUser();
        String newTokenStr = UUID.randomUUID().toString();
        RefreshToken newToken = new RefreshToken();
        newToken.setToken(newTokenStr);
        newToken.setUser(user);
        newToken.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
        newToken.setRevoked(false);
        refreshTokenRepository.save(newToken);

        return new RefreshResult(user, newTokenStr);
    }

    @Transactional
    public void revokeByTokenString(String tokenStr) {
        refreshTokenRepository.findByToken(tokenStr).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }
}

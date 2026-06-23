package com.matchmetrics.persistence.repository;

import com.matchmetrics.persistence.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByUserEmailAndCodeAndUsedFalse(String email, String code);

    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.used = true WHERE t.user.id = :userId AND t.used = false")
    void invalidateAllForUser(Long userId);

    @Query("SELECT COUNT(t) FROM PasswordResetToken t WHERE t.user.email = :email AND t.createdAt >= :since")
    long countRecentByEmail(@Param("email") String email, @Param("since") LocalDateTime since);

    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :cutoff OR (t.used = true AND t.createdAt < :cutoff)")
    long deleteOldTokens(@Param("cutoff") LocalDateTime cutoff);

    default long deleteOldTokens() {
        return deleteOldTokens(LocalDateTime.now().minusHours(24));
    }
}

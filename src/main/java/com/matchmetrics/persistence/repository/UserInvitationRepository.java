package com.matchmetrics.persistence.repository;

import com.matchmetrics.persistence.entity.UserInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserInvitationRepository extends JpaRepository<UserInvitation, Long> {

    Optional<UserInvitation> findByToken(String token);

    @Query("SELECT i FROM UserInvitation i WHERE i.email = :email AND i.used = false AND i.expiresAt > :now")
    Optional<UserInvitation> findActiveByEmail(@Param("email") String email, @Param("now") LocalDateTime now);
}

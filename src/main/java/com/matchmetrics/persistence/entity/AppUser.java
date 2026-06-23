package com.matchmetrics.persistence.entity;

import com.matchmetrics.domain.enums.AuthProvider;
import com.matchmetrics.domain.enums.SportType;
import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.domain.enums.UserStatus;
import com.matchmetrics.persistence.audit.AuditModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "app_users")
@Getter
@Setter
public class AppUser extends AuditModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column
    private String password;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuthProvider provider = AuthProvider.LOCAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status = UserStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "requested_team_name", length = 150)
    private String requestedTeamName;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_sport_type", length = 20)
    private SportType requestedSportType;

    @Column(name = "last_login")
    private java.time.LocalDateTime lastLogin;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(length = 50)
    private String phone;

    @Column(length = 500)
    private String bio;
}
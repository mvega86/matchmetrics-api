package com.matchmetrics.service.implementation;

import com.matchmetrics.domain.enums.AuthProvider;
import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.domain.enums.UserStatus;
import com.matchmetrics.mapper.dto.auth.AuthMeResponse;
import com.matchmetrics.mapper.dto.auth.AuthResponse;
import com.matchmetrics.mapper.dto.auth.ChangePasswordRequest;
import com.matchmetrics.mapper.dto.auth.LoginRequest;
import com.matchmetrics.mapper.dto.auth.RegisterRequest;
import com.matchmetrics.mapper.dto.auth.UpdateProfileRequest;
import com.matchmetrics.persistence.entity.AppUser;
import com.matchmetrics.persistence.entity.Team;
import com.matchmetrics.persistence.repository.AppUserRepository;
import com.matchmetrics.persistence.repository.TeamRepository;
import com.matchmetrics.security.JwtService;
import com.matchmetrics.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final AppUserRepository appUserRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }

        AppUser user = new AppUser();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setProvider(AuthProvider.LOCAL);
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.PENDING);

        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new IllegalArgumentException("Equipo no encontrado"));
            user.setTeam(team);
        } else {
            user.setRequestedTeamName(request.getRequestedTeamName());
            user.setRequestedSportType(request.getRequestedSportType());
        }

        AppUser savedUser = appUserRepository.save(user);
        String token = jwtService.generateToken(savedUser);

        return buildResponse(savedUser, token);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        if (user.getStatus() != UserStatus.APPROVED) {
            throw new BadCredentialsException("Usuario pendiente de aprobación o no habilitado");
        }

        String token = jwtService.generateToken(user);
        return buildResponse(user, token);
    }

    @Override
    public AuthMeResponse getProfile(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));
        return toMeResponse(user);
    }

    @Override
    public AuthMeResponse updateProfile(Long userId, UpdateProfileRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setBio(request.getBio());
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        appUserRepository.save(user);
        return toMeResponse(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("La contraseña actual es incorrecta");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        appUserRepository.save(user);
    }

    private AuthMeResponse toMeResponse(AppUser user) {
        return new AuthMeResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getStatus(),
                user.getTeam() != null ? user.getTeam().getId() : null,
                user.getTeam() != null ? user.getTeam().getName() : null,
                user.getRequestedTeamName(),
                user.getAvatarUrl(),
                user.getPhone(),
                user.getBio()
        );
    }

    private AuthResponse buildResponse(AppUser user, String token) {
        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getStatus()
        );
    }
}

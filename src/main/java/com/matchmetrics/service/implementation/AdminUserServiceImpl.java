package com.matchmetrics.service.implementation;

import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.domain.enums.UserStatus;
import com.matchmetrics.mapper.dto.admin.PendingUserResponse;
import com.matchmetrics.persistence.entity.AppUser;
import com.matchmetrics.persistence.entity.Team;
import com.matchmetrics.persistence.repository.AppUserRepository;
import com.matchmetrics.persistence.repository.TeamRepository;
import com.matchmetrics.service.IAdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements IAdminUserService {

    private final AppUserRepository appUserRepository;
    private final TeamRepository teamRepository;

    @Override
    public List<PendingUserResponse> getPendingUsers() {
        return appUserRepository.findByStatus(UserStatus.PENDING)
                .stream()
                .map(this::mapToPendingUserResponse)
                .toList();
    }

    @Override
    public PendingUserResponse approveUser(Long userId) {
        AppUser user = getUserOrThrow(userId);

        if (user.getTeam() == null) {
            Team team = resolveRequestedTeam(user);
            user.setTeam(team);
        }

        user.setStatus(UserStatus.APPROVED);

        AppUser savedUser = appUserRepository.save(user);
        return mapToPendingUserResponse(savedUser);
    }

    @Override
    public PendingUserResponse rejectUser(Long userId) {
        AppUser user = getUserOrThrow(userId);
        user.setStatus(UserStatus.REJECTED);

        AppUser savedUser = appUserRepository.save(user);
        return mapToPendingUserResponse(savedUser);
    }

    @Override
    public PendingUserResponse disableUser(Long userId, Long authenticatedUserId) {
        if (userId.equals(authenticatedUserId)) {
            throw new IllegalArgumentException("Un administrador no puede deshabilitarse a sí mismo");
        }

        AppUser user = getUserOrThrow(userId);
        user.setStatus(UserStatus.DISABLED);

        AppUser savedUser = appUserRepository.save(user);
        return mapToPendingUserResponse(savedUser);
    }

    @Override
    public PendingUserResponse changeRole(Long userId, UserRole role) {
        AppUser user = getUserOrThrow(userId);

        user.setRole(role);

        AppUser savedUser = appUserRepository.save(user);
        return mapToPendingUserResponse(savedUser);
    }

    private Team resolveRequestedTeam(AppUser user) {
        String requestedTeamName = user.getRequestedTeamName();

        if (requestedTeamName == null || requestedTeamName.isBlank()) {
            throw new IllegalArgumentException("El usuario no tiene equipo asignado ni equipo solicitado");
        }

        String cleanTeamName = requestedTeamName.trim();

        return teamRepository.findByNameIgnoreCase(cleanTeamName)
                .orElseGet(() -> createTeamFromRequestedName(cleanTeamName));
    }

    private Team createTeamFromRequestedName(String teamName) {
        Team team = new Team();
        team.setName(teamName);
        team.setAcronym(buildAcronym(teamName));
        team.setStadium("Pendiente");

        return teamRepository.save(team);
    }

    private String buildAcronym(String teamName) {
        String clean = teamName
                .replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase();

        if (clean.length() >= 3) {
            return clean.substring(0, 3);
        }

        return clean;
    }

    private AppUser getUserOrThrow(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private PendingUserResponse mapToPendingUserResponse(AppUser user) {
        Long teamId = user.getTeam() != null ? user.getTeam().getId() : null;
        String teamName = user.getTeam() != null ? user.getTeam().getName() : null;

        return new PendingUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getProvider(),
                user.getRole(),
                user.getStatus(),
                teamId,
                teamName,
                user.getRequestedTeamName()
        );
    }
}
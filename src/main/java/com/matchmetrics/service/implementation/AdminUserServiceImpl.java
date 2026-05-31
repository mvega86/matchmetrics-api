package com.matchmetrics.service.implementation;

import com.matchmetrics.domain.enums.UserStatus;
import com.matchmetrics.mapper.dto.admin.PendingUserResponse;
import com.matchmetrics.persistence.entity.AppUser;
import com.matchmetrics.persistence.repository.AppUserRepository;
import com.matchmetrics.service.IAdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements IAdminUserService {

    private final AppUserRepository appUserRepository;

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
    public PendingUserResponse disableUser(Long userId) {
        AppUser user = getUserOrThrow(userId);
        user.setStatus(UserStatus.DISABLED);
        AppUser savedUser = appUserRepository.save(user);
        return mapToPendingUserResponse(savedUser);
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
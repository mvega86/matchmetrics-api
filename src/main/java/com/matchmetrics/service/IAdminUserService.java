package com.matchmetrics.service;

import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.domain.enums.UserStatus;
import com.matchmetrics.mapper.dto.admin.PendingUserResponse;

import java.util.List;

public interface IAdminUserService {

    List<PendingUserResponse> getPendingUsers();

    List<PendingUserResponse> getAllUsers(UserStatus status);

    PendingUserResponse approveUser(Long userId);

    PendingUserResponse rejectUser(Long userId);

    PendingUserResponse disableUser(Long userId, Long authenticatedUserId);

    PendingUserResponse changeRole(Long userId, UserRole role);
}
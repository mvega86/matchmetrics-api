package com.matchmetrics.service;

import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.mapper.dto.admin.PendingUserResponse;

import java.util.List;

public interface IAdminUserService {

    List<PendingUserResponse> getPendingUsers();

    PendingUserResponse approveUser(Long userId);

    PendingUserResponse rejectUser(Long userId);

    PendingUserResponse disableUser(Long userId);

    PendingUserResponse changeRole(Long userId, UserRole role);
}
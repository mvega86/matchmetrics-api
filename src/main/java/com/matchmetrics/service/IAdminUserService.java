package com.matchmetrics.service;

import com.matchmetrics.mapper.dto.admin.PendingUserResponse;

import java.util.List;

public interface IAdminUserService {

    List<PendingUserResponse> getPendingUsers();

    PendingUserResponse approveUser(Long userId);

    PendingUserResponse rejectUser(Long userId);

    PendingUserResponse disableUser(Long userId);
}
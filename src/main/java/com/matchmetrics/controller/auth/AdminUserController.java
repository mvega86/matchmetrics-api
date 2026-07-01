package com.matchmetrics.controller.auth;

import com.matchmetrics.domain.enums.UserStatus;
import com.matchmetrics.mapper.dto.admin.ChangeUserRoleRequest;
import com.matchmetrics.mapper.dto.admin.PendingUserResponse;
import com.matchmetrics.security.UserPrincipal;
import com.matchmetrics.service.IAdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final IAdminUserService adminUserService;

    @GetMapping
    public List<PendingUserResponse> getAllUsers(
            @RequestParam(required = false) UserStatus status
    ) {
        return adminUserService.getAllUsers(status);
    }

    @GetMapping("/pending")
    public List<PendingUserResponse> getPendingUsers() {
        return adminUserService.getPendingUsers();
    }

    @PutMapping("/{userId}/approve")
    public PendingUserResponse approveUser(@PathVariable Long userId) {
        return adminUserService.approveUser(userId);
    }

    @PutMapping("/{userId}/reject")
    public PendingUserResponse rejectUser(@PathVariable Long userId) {
        return adminUserService.rejectUser(userId);
    }

    @PutMapping("/{userId}/disable")
    public PendingUserResponse disableUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal authenticatedUser
    ) {
        return adminUserService.disableUser(userId, authenticatedUser.getId());
    }

    @PutMapping("/{userId}/role")
    public PendingUserResponse changeRole(
            @PathVariable Long userId,
            @Valid @RequestBody ChangeUserRoleRequest request
    ) {
        return adminUserService.changeRole(userId, request.getRole());
    }
}
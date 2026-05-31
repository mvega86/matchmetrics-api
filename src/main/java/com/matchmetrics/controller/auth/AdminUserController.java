package com.matchmetrics.controller.auth;

import com.matchmetrics.mapper.dto.admin.PendingUserResponse;
import com.matchmetrics.service.IAdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final IAdminUserService adminUserService;

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
    public PendingUserResponse disableUser(@PathVariable Long userId) {
        return adminUserService.disableUser(userId);
    }
}
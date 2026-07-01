package com.matchmetrics.controller.auth;

import com.matchmetrics.mapper.dto.admin.SystemHealthDTO;
import com.matchmetrics.service.implementation.AdminHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/health")
@RequiredArgsConstructor
public class AdminHealthController {

    private final AdminHealthService adminHealthService;

    @GetMapping
    public SystemHealthDTO getSystemHealth() {
        return adminHealthService.getSystemHealth();
    }
}

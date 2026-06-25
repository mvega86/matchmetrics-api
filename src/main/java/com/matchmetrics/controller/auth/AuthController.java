package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.auth.*;
import com.matchmetrics.security.LoginRateLimiter;
import com.matchmetrics.security.UserPrincipal;
import com.matchmetrics.service.IAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;
    private final LoginRateLimiter loginRateLimiter;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String clientIp = resolveClientIp(httpRequest);
        loginRateLimiter.checkAndRecord(clientIp);
        try {
            AuthResponse response = authService.login(request);
            loginRateLimiter.reset(clientIp);
            return response;
        } catch (BadCredentialsException e) {
            throw e;
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @GetMapping("/me")
    public AuthMeResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new BadCredentialsException("Usuario no autenticado");
        }
        return authService.getProfile(principal.getId());
    }

    @PutMapping("/profile")
    public AuthMeResponse updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new BadCredentialsException("Usuario no autenticado");
        }
        return authService.updateProfile(principal.getId(), request);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new BadCredentialsException("Usuario no autenticado");
        }
        authService.changePassword(principal.getId(), request);
        return ResponseEntity.noContent().build();
    }
}
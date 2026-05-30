package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.auth.AuthResponse;
import com.matchmetrics.mapper.dto.auth.AuthenticatedUserResponse;
import com.matchmetrics.mapper.dto.auth.LoginRequest;
import com.matchmetrics.mapper.dto.auth.RegisterRequest;
import com.matchmetrics.security.UserPrincipal;
import com.matchmetrics.service.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthenticatedUserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new BadCredentialsException("Usuario no autenticado");
        }

        return new AuthenticatedUserResponse(
                principal.getId(),
                principal.getEmail(),
                principal.getFullName(),
                principal.getRole(),
                principal.getStatus()
        );
    }
}
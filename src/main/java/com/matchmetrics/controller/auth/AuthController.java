package com.matchmetrics.controller.auth;

import com.matchmetrics.mapper.dto.auth.AuthResponse;
import com.matchmetrics.mapper.dto.auth.LoginRequest;
import com.matchmetrics.mapper.dto.auth.RegisterRequest;
import com.matchmetrics.service.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}
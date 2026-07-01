package com.matchmetrics.controller;

import com.matchmetrics.mapper.dto.auth.*;
import com.matchmetrics.security.JwtService;
import com.matchmetrics.security.LoginRateLimiter;
import com.matchmetrics.security.UserPrincipal;
import com.matchmetrics.service.IAuthService;
import com.matchmetrics.service.RefreshTokenService;
import com.matchmetrics.service.RefreshTokenService.RefreshResult;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;
    private final LoginRateLimiter loginRateLimiter;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @Value("${app.security.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.security.cookie.same-site:Lax}")
    private String cookieSameSite;

    @Value("${app.security.jwt.expiration}")
    private long accessTokenExpirationMs;

    @Value("${app.security.jwt.refresh-expiration}")
    private long refreshTokenExpirationMs;

    // ── Auth endpoints ────────────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.register(request);
        setAuthCookies(response, authResponse.getToken(), authResponse.getUserId());
        return ResponseEntity.ok(authResponse.withoutToken());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        String clientIp = resolveClientIp(httpRequest);
        loginRateLimiter.checkAndRecord(clientIp);
        try {
            AuthResponse authResponse = authService.login(request);
            loginRateLimiter.reset(clientIp);
            setAuthCookies(response, authResponse.getToken(), authResponse.getUserId());
            return ResponseEntity.ok(authResponse.withoutToken());
        } catch (BadCredentialsException e) {
            throw e;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookieValue(request, "refresh_token");
        if (refreshToken == null) {
            throw new BadCredentialsException("Refresh token no encontrado");
        }
        RefreshResult result = refreshTokenService.consume(refreshToken);
        String newAccessToken = jwtService.generateToken(result.user());
        setAuthCookies(response, newAccessToken, result.user().getId(), result.newRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookieValue(request, "refresh_token");
        if (refreshToken != null) {
            refreshTokenService.revokeByTokenString(refreshToken);
        }
        clearAuthCookies(response);
        return ResponseEntity.noContent().build();
    }

    // ── Profile endpoints ─────────────────────────────────────────────────────

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

    // ── Cookie helpers ────────────────────────────────────────────────────────

    private void setAuthCookies(HttpServletResponse response, String accessToken, Long userId) {
        String refreshToken = refreshTokenService.createForUser(userId);
        setAuthCookies(response, accessToken, userId, refreshToken);
    }

    private void setAuthCookies(HttpServletResponse response, String accessToken, Long userId, String refreshToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(
                "access_token", accessToken, accessTokenExpirationMs / 1000, "/").toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(
                "refresh_token", refreshToken, refreshTokenExpirationMs / 1000, "/api/v1/auth").toString());
    }

    private void clearAuthCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie("access_token", "/").toString());
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie("refresh_token", "/api/v1/auth").toString());
    }

    private ResponseCookie buildCookie(String name, String value, long maxAgeSeconds, String path) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .path(path)
                .maxAge(maxAgeSeconds)
                .sameSite(cookieSameSite)
                .build();
    }

    private ResponseCookie clearCookie(String name, String path) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path(path)
                .maxAge(0)
                .sameSite(cookieSameSite)
                .build();
    }

    private String extractCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

package com.matchmetrics.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;
    private final ApiRateLimiter liveLimiter;
    private final ApiRateLimiter statsLimiter;

    public RateLimitInterceptor(
            JwtService jwtService,
            @Value("${app.rate-limit.live.max-requests-per-minute:60}") int liveMax,
            @Value("${app.rate-limit.stats.max-requests-per-minute:120}") int statsMax
    ) {
        this.jwtService = jwtService;
        this.liveLimiter = new ApiRateLimiter(liveMax, 60_000);
        this.statsLimiter = new ApiRateLimiter(statsMax, 60_000);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        ApiRateLimiter limiter;
        String key;

        if (isLiveEndpoint(uri)) {
            limiter = liveLimiter;
            key = resolveIp(request);
        } else if (isStatsEndpoint(uri)) {
            limiter = statsLimiter;
            key = resolveUserKey(request);
        } else {
            return true;
        }

        if (!limiter.allow(key)) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                "{\"status\":429,\"error\":\"Too Many Requests\"," +
                "\"message\":\"Límite de peticiones alcanzado. Inténtalo de nuevo en un minuto.\"}"
            );
            return false;
        }

        return true;
    }

    private boolean isLiveEndpoint(String uri) {
        return uri.startsWith("/api/v1/softball/game-state")
            || uri.startsWith("/api/v1/softball/play-events")
            || uri.startsWith("/api/v1/baseball/game-state")
            || uri.startsWith("/api/v1/baseball/play-events");
    }

    private boolean isStatsEndpoint(String uri) {
        return uri.startsWith("/api/v1/player-stats")
            || uri.startsWith("/api/v1/team-stats")
            || uri.startsWith("/api/v1/softball/stats")
            || uri.startsWith("/api/v1/player-statistics");
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveUserKey(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    try {
                        String email = jwtService.extractEmail(cookie.getValue());
                        if (email != null) {
                            return "user:" + email;
                        }
                    } catch (Exception ignored) {
                        // Token expired or invalid — fall through to IP
                    }
                    break;
                }
            }
        }
        return "ip:" + resolveIp(request);
    }
}

package com.matchmetrics.config;

import com.matchmetrics.security.RateLimitInterceptor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // En producción setear CORS_ALLOWED_ORIGINS=https://tudominio.com
    // http://localhost:[*] acepta cualquier puerto local (5173, 5174, 5176, etc.)
    @Value("${app.cors.allowed-origins:http://localhost:[*]}")
    private String[] allowedOrigins;

    @PostConstruct
    public void validateCorsOrigins() {
        if (allowedOrigins == null || allowedOrigins.length == 0) {
            throw new IllegalStateException(
                "app.cors.allowed-origins must be set — CORS will block all frontend requests");
        }
    }

    /**
     * Fuente de verdad CORS compartida por Spring MVC y Spring Security.
     * SecurityConfig la inyecta para garantizar que withCredentials=true
     * funcione en el preflight OPTIONS sin depender de HandlerMappingIntrospector.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // setAllowedOriginPatterns soporta [*] como wildcard de puerto y es compatible con allowCredentials=true
        config.setAllowedOriginPatterns(Arrays.asList(allowedOrigins));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization", "Accept", "X-Requested-With"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns(
                    "/api/v1/softball/game-state/**",
                    "/api/v1/softball/play-events/**",
                    "/api/v1/baseball/game-state/**",
                    "/api/v1/baseball/play-events/**",
                    "/api/v1/player-stats/**",
                    "/api/v1/team-stats/**",
                    "/api/v1/softball/stats/**",
                    "/api/v1/player-statistics/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("Content-Type", "Authorization", "Accept", "X-Requested-With")
                .allowCredentials(true);
    }
}

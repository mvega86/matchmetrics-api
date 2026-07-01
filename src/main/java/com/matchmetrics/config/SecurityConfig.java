package com.matchmetrics.config;

import com.matchmetrics.security.JwtAuthenticationEntryPoint;
import com.matchmetrics.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.PermissionsPolicyHeaderWriter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .headers(headers -> headers
                        .contentTypeOptions(Customizer.withDefaults())
                        .frameOptions(frame -> frame.deny())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                        )
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                        )
                        .addHeaderWriter(new PermissionsPolicyHeaderWriter(
                                "camera=(), microphone=(), geolocation=(), payment=()"
                        ))
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/v1/auth/register").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/auth/logout").permitAll()
                        .requestMatchers("/api/v1/auth/forgot-password/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/invitation/**").permitAll()

                        // Imágenes subidas — lectura pública, escritura autenticada
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/uploads").hasAnyRole("ADMIN", "MANAGER")

                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // Escritura de Teams solo ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/v1/teams").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/teams/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/teams").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/teams/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/teams").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/teams/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/teams").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/teams/**").hasRole("ADMIN")

                        // Lectura pública — no requiere autenticación
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/teams/**",
                                "/api/v1/matches/**",
                                "/api/v1/players/**",
                                "/api/v1/player-statistics/**",
                                "/api/v1/player-stats/**",
                                "/api/v1/team-stats/**",
                                "/api/v1/players-match/**",
                                "/api/v1/field-zones/**",
                                "/api/v1/tournaments/**",
                                "/api/v1/baseball/**",
                                "/api/v1/softball/**"
                        ).permitAll()

                        // Estadísticas (definición) — lectura pública
                        .requestMatchers(HttpMethod.GET, "/api/v1/statistics/**").permitAll()

                        // Gestión de definición de estadísticas — solo ADMIN
                        .requestMatchers(HttpMethod.POST,   "/api/v1/statistics/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/statistics/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/api/v1/statistics/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/statistics/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/players/**",
                                "/api/v1/matches/**",
                                "/api/v1/player-statistics/**",
                                "/api/v1/players-match/**",
                                "/api/v1/field-zones/**",
                                "/api/v1/baseball/**",
                                "/api/v1/softball/**"
                        ).hasAnyRole("ADMIN", "MANAGER")

                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/players/**",
                                "/api/v1/matches/**",
                                "/api/v1/player-statistics/**",
                                "/api/v1/players-match/**",
                                "/api/v1/field-zones/**",
                                "/api/v1/baseball/**",
                                "/api/v1/softball/**"
                        ).hasAnyRole("ADMIN", "MANAGER")

                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/players/**",
                                "/api/v1/matches/**",
                                "/api/v1/player-statistics/**",
                                "/api/v1/players-match/**",
                                "/api/v1/field-zones/**",
                                "/api/v1/baseball/**",
                                "/api/v1/softball/**"
                        ).hasAnyRole("ADMIN", "MANAGER")

                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/players/**",
                                "/api/v1/matches/**",
                                "/api/v1/player-statistics/**",
                                "/api/v1/players-match/**",
                                "/api/v1/field-zones/**",
                                "/api/v1/baseball/**",
                                "/api/v1/softball/**"
                        ).hasAnyRole("ADMIN", "MANAGER")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
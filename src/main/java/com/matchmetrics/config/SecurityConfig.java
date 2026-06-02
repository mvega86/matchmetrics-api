package com.matchmetrics.config;

import com.matchmetrics.security.JwtAuthenticationEntryPoint;
import com.matchmetrics.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

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
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/register").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()

                        // Necesario para el registro
                        .requestMatchers(HttpMethod.GET, "/api/v1/teams").permitAll()

                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // Escritura de Teams solo ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/v1/teams/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/teams/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/teams/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/teams/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/players/**",
                                "/api/v1/teams/**",
                                "/api/v1/matches/**",
                                "/api/v1/statistics/**",
                                "/api/v1/player-statistics/**",
                                "/api/v1/players-match/**",
                                "/api/v1/field-zones/**"
                        ).hasAnyRole("ADMIN", "MANAGER", "USER")

                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/players/**",
                                "/api/v1/teams/**",
                                "/api/v1/matches/**",
                                "/api/v1/statistics/**",
                                "/api/v1/player-statistics/**",
                                "/api/v1/players-match/**",
                                "/api/v1/field-zones/**"
                        ).hasAnyRole("ADMIN", "MANAGER")

                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/players/**",
                                "/api/v1/teams/**",
                                "/api/v1/matches/**",
                                "/api/v1/statistics/**",
                                "/api/v1/player-statistics/**",
                                "/api/v1/players-match/**",
                                "/api/v1/field-zones/**"
                        ).hasAnyRole("ADMIN", "MANAGER")

                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/players/**",
                                "/api/v1/teams/**",
                                "/api/v1/matches/**",
                                "/api/v1/statistics/**",
                                "/api/v1/player-statistics/**",
                                "/api/v1/players-match/**",
                                "/api/v1/field-zones/**"
                        ).hasAnyRole("ADMIN", "MANAGER")

                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/players/**",
                                "/api/v1/teams/**",
                                "/api/v1/matches/**",
                                "/api/v1/statistics/**",
                                "/api/v1/player-statistics/**",
                                "/api/v1/players-match/**",
                                "/api/v1/field-zones/**"
                        ).hasAnyRole("ADMIN", "MANAGER")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
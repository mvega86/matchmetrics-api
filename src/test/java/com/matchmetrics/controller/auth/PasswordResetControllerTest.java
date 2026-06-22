package com.matchmetrics.controller.auth;

import com.matchmetrics.domain.enums.AuthProvider;
import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.domain.enums.UserStatus;
import com.matchmetrics.persistence.entity.AppUser;
import com.matchmetrics.persistence.entity.PasswordResetToken;
import com.matchmetrics.persistence.repository.AppUserRepository;
import com.matchmetrics.persistence.repository.PasswordResetTokenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        tokenRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    private AppUser createUser(String email, String phone) {
        AppUser user = new AppUser();
        user.setFullName("Usuario Prueba");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setProvider(AuthProvider.LOCAL);
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.APPROVED);
        user.setPhone(phone);
        return appUserRepository.save(user);
    }

    // ── getMethods ──────────────────────────────────────────────────────────

    @Test
    void getMethods_ShouldReturnEmailMethod_WhenUserHasNoPhone() throws Exception {
        createUser("reset@test.com", null);

        mockMvc.perform(post("/api/v1/auth/forgot-password/methods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "reset@test.com" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableMethods", hasItem("EMAIL")))
                .andExpect(jsonPath("$.availableMethods", not(hasItem("SMS"))))
                .andExpect(jsonPath("$.maskedEmail", notNullValue()));
    }

    @Test
    void getMethods_ShouldReturnOnlyEmail_WhenUserHasPhoneButSmsDisabled() throws Exception {
        // SMS is disabled by default (app.sms.enabled=false)
        createUser("reset@test.com", "612345678");

        mockMvc.perform(post("/api/v1/auth/forgot-password/methods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "reset@test.com" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableMethods", hasItem("EMAIL")))
                .andExpect(jsonPath("$.availableMethods", not(hasItem("SMS"))));
    }

    @Test
    void getMethods_ShouldReturn200WithEmail_WhenEmailNotFound() throws Exception {
        // P6: no revelar si el email existe — siempre 200 con EMAIL disponible
        mockMvc.perform(post("/api/v1/auth/forgot-password/methods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "noexiste@test.com" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableMethods", hasItem("EMAIL")));
    }

    // ── sendCode ────────────────────────────────────────────────────────────

    @Test
    void sendCode_ShouldReturn204_WhenEmailMethodSelected() throws Exception {
        createUser("reset@test.com", null);

        mockMvc.perform(post("/api/v1/auth/forgot-password/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "reset@test.com", "method": "EMAIL" }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void sendCode_ShouldReturn400_WhenSmsRequestedButNoPhone() throws Exception {
        createUser("reset@test.com", null);

        mockMvc.perform(post("/api/v1/auth/forgot-password/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "reset@test.com", "method": "SMS" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendCode_ShouldReturn204_WhenEmailNotFound() throws Exception {
        // P6: no revelar si el email existe — responder siempre genérico
        mockMvc.perform(post("/api/v1/auth/forgot-password/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "noexiste@test.com", "method": "EMAIL" }
                                """))
                .andExpect(status().isNoContent());
    }

    // ── resetPassword ───────────────────────────────────────────────────────

    @Test
    void resetPassword_ShouldSucceed_WhenEmailAndCodeAreValid() throws Exception {
        AppUser user = createUser("reset@test.com", null);

        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setCode("123456");
        token.setMethod("EMAIL");
        token.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        tokenRepository.save(token);

        mockMvc.perform(post("/api/v1/auth/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "reset@test.com", "code": "123456", "newPassword": "nuevaPass123" }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void resetPassword_ShouldReturn400_WhenCodeDoesNotMatchEmail() throws Exception {
        AppUser user = createUser("reset@test.com", null);

        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setCode("123456");
        token.setMethod("EMAIL");
        token.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        tokenRepository.save(token);

        mockMvc.perform(post("/api/v1/auth/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "otro@test.com", "code": "123456", "newPassword": "nuevaPass123" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_ShouldReturn400_WhenCodeIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "reset@test.com", "code": "000000", "newPassword": "nuevaPass123" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_ShouldReturn400_WhenCodeIsExpired() throws Exception {
        AppUser user = createUser("reset@test.com", null);

        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setCode("999999");
        token.setMethod("EMAIL");
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        tokenRepository.save(token);

        mockMvc.perform(post("/api/v1/auth/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "reset@test.com", "code": "999999", "newPassword": "nuevaPass123" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendCode_ShouldReturn400_WhenRateLimitExceeded() throws Exception {
        AppUser user = createUser("reset@test.com", null);

        // Create 3 recent tokens to hit the rate limit
        for (int i = 0; i < 3; i++) {
            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setCode("10000" + i);
            token.setMethod("EMAIL");
            token.setExpiresAt(LocalDateTime.now().plusMinutes(15));
            tokenRepository.save(token);
        }

        mockMvc.perform(post("/api/v1/auth/forgot-password/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "reset@test.com", "method": "EMAIL" }
                                """))
                .andExpect(status().isBadRequest());
    }
}

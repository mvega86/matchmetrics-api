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
    void getMethods_ShouldReturnBothMethods_WhenUserHasPhone() throws Exception {
        createUser("reset@test.com", "612345678");

        mockMvc.perform(post("/api/v1/auth/forgot-password/methods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "reset@test.com" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableMethods", hasItem("EMAIL")))
                .andExpect(jsonPath("$.availableMethods", hasItem("SMS")))
                .andExpect(jsonPath("$.maskedPhone", notNullValue()));
    }

    @Test
    void getMethods_ShouldReturn404_WhenEmailNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password/methods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "noexiste@test.com" }
                                """))
                .andExpect(status().isNotFound());
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
    void sendCode_ShouldReturn404_WhenEmailNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "noexiste@test.com", "method": "EMAIL" }
                                """))
                .andExpect(status().isNotFound());
    }

    // ── resetPassword ───────────────────────────────────────────────────────

    @Test
    void resetPassword_ShouldSucceed_WhenCodeIsValid() throws Exception {
        AppUser user = createUser("reset@test.com", null);

        // Generate token manually to get the known code
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setCode("123456");
        token.setMethod("EMAIL");
        token.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        tokenRepository.save(token);

        mockMvc.perform(post("/api/v1/auth/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "code": "123456", "newPassword": "nuevaPass123" }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void resetPassword_ShouldReturn400_WhenCodeIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "code": "000000", "newPassword": "nuevaPass123" }
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
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1)); // already expired
        tokenRepository.save(token);

        mockMvc.perform(post("/api/v1/auth/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "code": "999999", "newPassword": "nuevaPass123" }
                                """))
                .andExpect(status().isBadRequest());
    }
}

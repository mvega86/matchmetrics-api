package com.matchmetrics.controller.auth;

import com.matchmetrics.domain.enums.AuthProvider;
import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.domain.enums.UserStatus;
import com.matchmetrics.persistence.entity.AppUser;
import com.matchmetrics.persistence.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        appUserRepository.deleteAll();
    }

    @Test
    void register_ShouldCreatePendingUserAndReturnToken() throws Exception {
        String body = """
                {
                  "fullName": "Usuario Prueba",
                  "email": "usuario@test.com",
                  "password": "123456",
                  "requestedTeamName": "Equipo Prueba"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", is("usuario@test.com")))
                .andExpect(jsonPath("$.fullName", is("Usuario Prueba")))
                .andExpect(jsonPath("$.role", is("USER")))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() throws Exception {
        AppUser user = new AppUser();
        user.setFullName("Usuario Prueba");
        user.setEmail("usuario@test.com");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setProvider(AuthProvider.LOCAL);
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.APPROVED);

        appUserRepository.save(user);

        String loginBody = """
            {
              "email": "usuario@test.com",
              "password": "123456"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", is("usuario@test.com")))
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    void register_ShouldFail_WhenEmailAlreadyExists() throws Exception {
        String body = """
                {
                  "fullName": "Usuario Prueba",
                  "email": "usuario@test.com",
                  "password": "123456",
                  "requestedTeamName": "Equipo Prueba"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ShouldFail_WhenPasswordIsWrong() throws Exception {
        String registerBody = """
                {
                  "fullName": "Usuario Prueba",
                  "email": "usuario@test.com",
                  "password": "123456",
                  "requestedTeamName": "Equipo Prueba"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk());

        String loginBody = """
                {
                  "email": "usuario@test.com",
                  "password": "wrong-password"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_ShouldReturnAuthenticatedUser_WhenTokenIsValid() throws Exception {
        String registerBody = """
            {
              "fullName": "Usuario Prueba",
              "email": "usuario@test.com",
              "password": "123456",
              "requestedTeamName": "Equipo Prueba"
            }
            """;

        String registerResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = registerResponse
                .split("\"token\":\"")[1]
                .split("\"")[0];

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("usuario@test.com")))
                .andExpect(jsonPath("$.fullName", is("Usuario Prueba")))
                .andExpect(jsonPath("$.role", is("USER")))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void me_ShouldFail_WhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    // ── change-password ──────────────────────────────────────────────────────

    @Test
    void changePassword_ShouldSucceed_WhenCurrentPasswordIsCorrect() throws Exception {
        String token = createApprovedUserAndGetToken();
        String body = """
                {
                  "currentPassword": "password123",
                  "newPassword": "newPassword456"
                }
                """;
        mockMvc.perform(put("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
    }

    @Test
    void changePassword_ShouldFail_WhenCurrentPasswordIsWrong() throws Exception {
        String token = createApprovedUserAndGetToken();
        String body = """
                {
                  "currentPassword": "wrong-password",
                  "newPassword": "newPassword456"
                }
                """;
        mockMvc.perform(put("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        String body = """
                {
                  "currentPassword": "password123",
                  "newPassword": "newPassword456"
                }
                """;
        mockMvc.perform(put("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    // ── update profile ───────────────────────────────────────────────────────

    @Test
    void updateProfile_ShouldUpdateAndReturnUpdatedData() throws Exception {
        String token = createApprovedUserAndGetToken();
        String body = """
                {
                  "fullName": "Nombre Actualizado",
                  "phone": "612345678",
                  "bio": "Bio de prueba"
                }
                """;
        mockMvc.perform(put("/api/v1/auth/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("Nombre Actualizado")))
                .andExpect(jsonPath("$.phone", is("612345678")))
                .andExpect(jsonPath("$.bio", is("Bio de prueba")));
    }

    @Test
    void updateProfile_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        String body = """
                {
                  "fullName": "Nombre Actualizado"
                }
                """;
        mockMvc.perform(put("/api/v1/auth/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String createApprovedUserAndGetToken() throws Exception {
        AppUser user = new AppUser();
        user.setFullName("Usuario Test");
        user.setEmail("test@approved.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setProvider(AuthProvider.LOCAL);
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.APPROVED);
        appUserRepository.save(user);

        String loginBody = """
                {
                  "email": "test@approved.com",
                  "password": "password123"
                }
                """;
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return response.split("\"token\":\"")[1].split("\"")[0];
    }
}
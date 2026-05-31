package com.matchmetrics.controller.auth;

import com.matchmetrics.domain.enums.AuthProvider;
import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.domain.enums.UserStatus;
import com.matchmetrics.persistence.entity.AppUser;
import com.matchmetrics.persistence.repository.AppUserRepository;
import com.matchmetrics.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String adminToken;
    private String userToken;
    private AppUser pendingUser;

    @BeforeEach
    void setUp() {
        appUserRepository.deleteAll();

        AppUser admin = new AppUser();
        admin.setFullName("Administrador");
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setProvider(AuthProvider.LOCAL);
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.APPROVED);
        AppUser savedAdmin = appUserRepository.save(admin);

        AppUser regularUser = new AppUser();
        regularUser.setFullName("Usuario Normal");
        regularUser.setEmail("user@test.com");
        regularUser.setPassword(passwordEncoder.encode("123456"));
        regularUser.setProvider(AuthProvider.LOCAL);
        regularUser.setRole(UserRole.USER);
        regularUser.setStatus(UserStatus.APPROVED);
        AppUser savedUser = appUserRepository.save(regularUser);

        AppUser pending = new AppUser();
        pending.setFullName("Usuario Pendiente");
        pending.setEmail("pending@test.com");
        pending.setPassword(passwordEncoder.encode("123456"));
        pending.setProvider(AuthProvider.LOCAL);
        pending.setRole(UserRole.USER);
        pending.setStatus(UserStatus.PENDING);
        pending.setRequestedTeamName("Equipo Pendiente");
        pendingUser = appUserRepository.save(pending);

        adminToken = jwtService.generateToken(savedAdmin);
        userToken = jwtService.generateToken(savedUser);
    }

    @Test
    void getPendingUsers_ShouldReturnPendingUsers_WhenUserIsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users/pending")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is(pendingUser.getId().intValue())))
                .andExpect(jsonPath("$[0].email", is("pending@test.com")))
                .andExpect(jsonPath("$[0].fullName", is("Usuario Pendiente")))
                .andExpect(jsonPath("$[0].role", is("USER")))
                .andExpect(jsonPath("$[0].status", is("PENDING")))
                .andExpect(jsonPath("$[0].requestedTeamName", is("Equipo Pendiente")));
    }

    @Test
    void approveUser_ShouldApprovePendingUser_WhenUserIsAdmin() throws Exception {
        mockMvc.perform(put("/api/v1/admin/users/" + pendingUser.getId() + "/approve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(pendingUser.getId().intValue())))
                .andExpect(jsonPath("$.email", is("pending@test.com")))
                .andExpect(jsonPath("$.status", is("APPROVED")));

        AppUser updatedUser = appUserRepository.findById(pendingUser.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(UserStatus.APPROVED, updatedUser.getStatus());
    }

    @Test
    void rejectUser_ShouldRejectPendingUser_WhenUserIsAdmin() throws Exception {
        mockMvc.perform(put("/api/v1/admin/users/" + pendingUser.getId() + "/reject")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(pendingUser.getId().intValue())))
                .andExpect(jsonPath("$.email", is("pending@test.com")))
                .andExpect(jsonPath("$.status", is("REJECTED")));

        AppUser updatedUser = appUserRepository.findById(pendingUser.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(UserStatus.REJECTED, updatedUser.getStatus());
    }

    @Test
    void disableUser_ShouldDisableUser_WhenUserIsAdmin() throws Exception {
        mockMvc.perform(put("/api/v1/admin/users/" + pendingUser.getId() + "/disable")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(pendingUser.getId().intValue())))
                .andExpect(jsonPath("$.email", is("pending@test.com")))
                .andExpect(jsonPath("$.status", is("DISABLED")));

        AppUser updatedUser = appUserRepository.findById(pendingUser.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(UserStatus.DISABLED, updatedUser.getStatus());
    }

    @Test
    void adminEndpoint_ShouldReturn403_WhenUserIsNotAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users/pending")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_ShouldReturn401_WhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users/pending"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void approveUser_ShouldReturn400_WhenUserDoesNotExist() throws Exception {
        mockMvc.perform(put("/api/v1/admin/users/999999/approve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Usuario no encontrado")));
    }
}
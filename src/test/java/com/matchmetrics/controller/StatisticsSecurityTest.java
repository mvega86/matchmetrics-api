package com.matchmetrics.controller;

import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.domain.enums.UserStatus;
import com.matchmetrics.persistence.entity.AppUser;
import com.matchmetrics.security.UserPrincipal;
import com.matchmetrics.service.IPlayerStatisticsQueryService;
import com.matchmetrics.service.IStatisticService;
import com.matchmetrics.service.ITeamStatisticsQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StatisticsSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IStatisticService statisticService;

    @MockitoBean
    private IPlayerStatisticsQueryService playerStatsService;

    @MockitoBean
    private ITeamStatisticsQueryService teamStatsService;

    // ── Definición de estadísticas — GET público ───────────────────────────────

    @Test
    void statisticsGet_ShouldBePublic() throws Exception {
        when(statisticService.search(anyString())).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/statistics"))
                .andExpect(status().isOk());
    }

    // ── Definición de estadísticas — escritura solo ADMIN ─────────────────────

    @Test
    void statisticsPost_ShouldReturn403_WhenManager() throws Exception {
        String body = """
                { "name": "TestStat", "sportType": "SOFTBALL" }
                """;
        mockMvc.perform(post("/api/v1/statistics")
                        .with(user(userPrincipal(UserRole.MANAGER, 1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void statisticsPost_ShouldReturn403_WhenUser() throws Exception {
        String body = """
                { "name": "TestStat", "sportType": "SOFTBALL" }
                """;
        mockMvc.perform(post("/api/v1/statistics")
                        .with(user(userPrincipal(UserRole.USER, null)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void statisticsDelete_ShouldReturn403_WhenManager() throws Exception {
        mockMvc.perform(delete("/api/v1/statistics/1")
                        .with(user(userPrincipal(UserRole.MANAGER, 1L))))
                .andExpect(status().isForbidden());
    }

    // ── Consulta de estadísticas acumuladas — requiere autenticación ──────────

    @Test
    void playerStats_ShouldReturn401_WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/player-stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void playerStats_ShouldReturn200_WhenUser() throws Exception {
        when(playerStatsService.getPlayerStatsList(any(), any(), any())).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/player-stats")
                        .with(user(userPrincipal(UserRole.USER, null))))
                .andExpect(status().isOk());
    }

    @Test
    void playerStats_ShouldReturn200_WhenManager() throws Exception {
        when(playerStatsService.getPlayerStatsList(any(), any(), any())).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/player-stats")
                        .with(user(userPrincipal(UserRole.MANAGER, 1L))))
                .andExpect(status().isOk());
    }

    @Test
    void playerStats_ShouldReturn200_WhenAdmin() throws Exception {
        when(playerStatsService.getPlayerStatsList(any(), any(), any())).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/player-stats")
                        .with(user(userPrincipal(UserRole.ADMIN, null))))
                .andExpect(status().isOk());
    }

    @Test
    void teamStats_ShouldReturn401_WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/team-stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void teamStats_ShouldReturn200_WhenUser() throws Exception {
        when(teamStatsService.getTeamStatsList(any(), any())).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/team-stats")
                        .with(user(userPrincipal(UserRole.USER, null))))
                .andExpect(status().isOk());
    }

    private UserPrincipal userPrincipal(UserRole role, Long teamId) {
        AppUser user = new AppUser();
        user.setId(99L);
        user.setEmail(role.name().toLowerCase() + "@test.com");
        user.setPassword("password");
        user.setFullName(role.name() + " Test");
        user.setRole(role);
        user.setStatus(UserStatus.APPROVED);

        if (teamId != null) {
            com.matchmetrics.persistence.entity.Team team = new com.matchmetrics.persistence.entity.Team();
            team.setId(teamId);
            team.setName("Equipo Test");
            team.setAcronym("TST");
            team.setStadium("Estadio Test");
            user.setTeam(team);
        }

        return new UserPrincipal(user);
    }
}

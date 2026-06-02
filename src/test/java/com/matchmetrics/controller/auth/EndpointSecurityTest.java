package com.matchmetrics.controller.auth;

import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.domain.enums.UserStatus;
import com.matchmetrics.mapper.dto.PlayerDTO;
import com.matchmetrics.mapper.dto.TeamDTO;
import com.matchmetrics.persistence.entity.AppUser;
import com.matchmetrics.persistence.entity.Team;
import com.matchmetrics.security.UserPrincipal;
import com.matchmetrics.service.*;
import com.matchmetrics.service.implementation.FieldZoneService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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
class EndpointSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IPlayerService playerService;

    @MockitoBean
    private ITeamService teamService;

    @MockitoBean
    private IMatchService matchService;

    @MockitoBean
    private IStatisticService statisticService;

    @MockitoBean
    private IPlayerMatchService playerMatchService;

    @MockitoBean
    private IPlayerStatisticService playerStatisticService;

    @MockitoBean
    private FieldZoneService fieldZoneService;

    @Test
    void playersGet_ShouldReturn401_WhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/players"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void playersGet_ShouldAllowUserRole() throws Exception {
        when(playerService.searchPlayers(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/players")
                        .with(user(userPrincipal(UserRole.USER, 1L))))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void playersPost_ShouldReturn403_WhenUserRole() throws Exception {
        String body = """
                {
                  "fullName": "Jugador Prueba",
                  "jerseyName": "Prueba",
                  "jerseyNumber": 10
                }
                """;

        mockMvc.perform(post("/api/v1/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void playersPost_ShouldAllowManagerRole() throws Exception {
        when(playerService.save(any(PlayerDTO.class))).thenReturn(new PlayerDTO());

        String body = """
                {
                  "fullName": "Jugador Prueba",
                  "jerseyName": "Prueba",
                  "jerseyNumber": 10
                }
                """;

        mockMvc.perform(post("/api/v1/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void playersPost_ShouldAllowAdminRole() throws Exception {
        when(playerService.save(any(PlayerDTO.class))).thenReturn(new PlayerDTO());

        String body = """
                {
                  "fullName": "Jugador Prueba",
                  "jerseyName": "Prueba",
                  "jerseyNumber": 10
                }
                """;

        mockMvc.perform(post("/api/v1/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void teamsGet_ShouldBePublicForRegistrationSearch() throws Exception {
        when(teamService.search(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/teams")
                        .param("search", "name:Equipo Prueba"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void teamsPost_ShouldReturn403_WhenUserRole() throws Exception {
        String body = """
                {
                  "name": "Equipo Prueba",
                  "acronym": "EQU",
                  "stadium": "Pendiente"
                }
                """;

        mockMvc.perform(post("/api/v1/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void teamsPost_ShouldAllowManagerRole() throws Exception {
        when(teamService.save(any(TeamDTO.class))).thenReturn(new TeamDTO());

        String body = """
                {
                  "name": "Equipo Prueba",
                  "acronym": "EQU",
                  "stadium": "Pendiente"
                }
                """;

        mockMvc.perform(post("/api/v1/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void matchesGet_ShouldReturn401_WhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/matches"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void matchesGet_ShouldAllowUserRole() throws Exception {
        when(matchService.search(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/matches")
                        .with(user(userPrincipal(UserRole.USER, 1L))))
                .andExpect(status().isOk());
    }

    private UserPrincipal userPrincipal(UserRole role, Long teamId) {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setEmail(role.name().toLowerCase() + "@test.com");
        user.setPassword("password");
        user.setFullName(role.name() + " Test");
        user.setRole(role);
        user.setStatus(UserStatus.APPROVED);

        if (teamId != null) {
            Team team = new Team();
            team.setId(teamId);
            team.setName("Equipo Test");
            team.setAcronym("TST");
            team.setStadium("Estadio Test");
            user.setTeam(team);
        }

        return new UserPrincipal(user);
    }
}
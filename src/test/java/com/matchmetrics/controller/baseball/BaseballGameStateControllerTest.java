package com.matchmetrics.controller.baseball;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchmetrics.domain.enums.BaseballGameStatus;
import com.matchmetrics.domain.enums.InningHalf;
import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.domain.enums.UserStatus;
import com.matchmetrics.mapper.dto.BaseballGameStateDTO;
import com.matchmetrics.mapper.dto.MatchDTO;
import com.matchmetrics.mapper.dto.TeamDTO;
import com.matchmetrics.persistence.entity.AppUser;
import com.matchmetrics.persistence.entity.Team;
import com.matchmetrics.security.UserPrincipal;
import com.matchmetrics.service.IBaseballGameStateService;
import com.matchmetrics.service.IMatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BaseballGameStateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IBaseballGameStateService gameStateService;

    @MockitoBean
    private IMatchService matchService;

    // Match con homeTeam=1, awayTeam=2
    @BeforeEach
    void setupMatchMock() {
        TeamDTO homeTeam = new TeamDTO();
        homeTeam.setId(1L);
        homeTeam.setName("Home Team");
        homeTeam.setAcronym("HME");
        homeTeam.setStadium("Home Stadium");

        TeamDTO awayTeam = new TeamDTO();
        awayTeam.setId(2L);
        awayTeam.setName("Away Team");
        awayTeam.setAcronym("AWY");
        awayTeam.setStadium("Away Stadium");

        MatchDTO match = new MatchDTO();
        match.setHomeTeam(homeTeam);
        match.setAwayTeam(awayTeam);

        when(matchService.getMatchById(anyLong())).thenReturn(match);
    }

    // -------------------------
    // POST /api/v1/baseball/game-state
    // -------------------------

    @Test
    void create_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/baseball/game-state")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleGameStateDTO())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_ShouldReturn403_WhenUserRole() throws Exception {
        mockMvc.perform(post("/api/v1/baseball/game-state")
                        .with(user(principal(UserRole.USER, 1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleGameStateDTO())))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_ShouldReturn403_WhenManagerFromDifferentTeam() throws Exception {
        // MANAGER del equipo 3, pero el partido es entre equipos 1 y 2
        mockMvc.perform(post("/api/v1/baseball/game-state")
                        .with(user(principal(UserRole.MANAGER, 3L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleGameStateDTO())))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_ShouldAllowManagerOfParticipatingTeam() throws Exception {
        BaseballGameStateDTO saved = sampleGameStateDTO();
        saved.setId(1L);
        when(gameStateService.createGameState(any())).thenReturn(saved);

        // MANAGER del equipo 1 (homeTeam del partido)
        mockMvc.perform(post("/api/v1/baseball/game-state")
                        .with(user(principal(UserRole.MANAGER, 1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleGameStateDTO())))
                .andExpect(status().isOk());
    }

    @Test
    void create_ShouldAllowAdminRole() throws Exception {
        BaseballGameStateDTO saved = sampleGameStateDTO();
        saved.setId(1L);
        when(gameStateService.createGameState(any())).thenReturn(saved);

        mockMvc.perform(post("/api/v1/baseball/game-state")
                        .with(user(principal(UserRole.ADMIN, null)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleGameStateDTO())))
                .andExpect(status().isOk());
    }

    // -------------------------
    // GET /api/v1/baseball/game-state/{matchId}
    // -------------------------

    @Test
    void get_ShouldBePublic_WhenNotAuthenticated() throws Exception {
        when(gameStateService.getGameStateByMatchId(anyLong())).thenReturn(sampleGameStateDTO());
        mockMvc.perform(get("/api/v1/baseball/game-state/1"))
                .andExpect(status().isOk());
    }

    @Test
    void get_ShouldReturn403_WhenUserFromDifferentTeam() throws Exception {
        mockMvc.perform(get("/api/v1/baseball/game-state/1")
                        .with(user(principal(UserRole.USER, 3L))))
                .andExpect(status().isForbidden());
    }

    @Test
    void get_ShouldAllowUserOfParticipatingTeam() throws Exception {
        when(gameStateService.getGameStateByMatchId(anyLong())).thenReturn(sampleGameStateDTO());

        mockMvc.perform(get("/api/v1/baseball/game-state/1")
                        .with(user(principal(UserRole.USER, 1L))))
                .andExpect(status().isOk());
    }

    @Test
    void get_ShouldAllowManagerOfParticipatingTeam() throws Exception {
        when(gameStateService.getGameStateByMatchId(anyLong())).thenReturn(sampleGameStateDTO());

        mockMvc.perform(get("/api/v1/baseball/game-state/1")
                        .with(user(principal(UserRole.MANAGER, 2L))))
                .andExpect(status().isOk());
    }

    @Test
    void get_ShouldAllowAdminRole() throws Exception {
        when(gameStateService.getGameStateByMatchId(anyLong())).thenReturn(sampleGameStateDTO());

        mockMvc.perform(get("/api/v1/baseball/game-state/1")
                        .with(user(principal(UserRole.ADMIN, null))))
                .andExpect(status().isOk());
    }

    // -------------------------
    // PUT /api/v1/baseball/game-state/{matchId}
    // -------------------------

    @Test
    void update_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/api/v1/baseball/game-state/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void update_ShouldReturn403_WhenUserRole() throws Exception {
        mockMvc.perform(put("/api/v1/baseball/game-state/1")
                        .with(user(principal(UserRole.USER, 1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_ShouldReturn403_WhenManagerFromDifferentTeam() throws Exception {
        mockMvc.perform(put("/api/v1/baseball/game-state/1")
                        .with(user(principal(UserRole.MANAGER, 3L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_ShouldAllowManagerOfParticipatingTeam() throws Exception {
        BaseballGameStateDTO updated = sampleGameStateDTO();
        when(gameStateService.updateGameState(anyLong(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/baseball/game-state/1")
                        .with(user(principal(UserRole.MANAGER, 1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void update_ShouldAllowAdminRole() throws Exception {
        BaseballGameStateDTO updated = sampleGameStateDTO();
        when(gameStateService.updateGameState(anyLong(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/baseball/game-state/1")
                        .with(user(principal(UserRole.ADMIN, null)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    // -------------------------
    // DELETE /api/v1/baseball/game-state/{matchId}
    // -------------------------

    @Test
    void delete_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/v1/baseball/game-state/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_ShouldReturn403_WhenUserRole() throws Exception {
        mockMvc.perform(delete("/api/v1/baseball/game-state/1")
                        .with(user(principal(UserRole.USER, 1L))))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_ShouldReturn403_WhenManagerRole() throws Exception {
        mockMvc.perform(delete("/api/v1/baseball/game-state/1")
                        .with(user(principal(UserRole.MANAGER, 1L))))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_ShouldAllowAdminRole() throws Exception {
        mockMvc.perform(delete("/api/v1/baseball/game-state/1")
                        .with(user(principal(UserRole.ADMIN, null))))
                .andExpect(status().isNoContent());
    }

    // -------------------------
    // PUT — player-match integrity validation
    // -------------------------

    @Test
    void update_ShouldReturn400_WhenBasePlayerDoesNotBelongToMatch() throws Exception {
        when(gameStateService.updateGameState(anyLong(), any()))
                .thenThrow(new IllegalArgumentException("PlayerMatch 99 does not belong to match 1"));

        BaseballGameStateDTO dto = sampleGameStateDTO();
        dto.setFirstBasePlayerMatchId(99L);

        mockMvc.perform(put("/api/v1/baseball/game-state/1")
                        .with(user(principal(UserRole.ADMIN, null)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------
    // Helpers
    // -------------------------

    private BaseballGameStateDTO sampleGameStateDTO() {
        BaseballGameStateDTO dto = new BaseballGameStateDTO();
        dto.setMatchId(1L);
        dto.setCurrentInning(1);
        dto.setInningHalf(InningHalf.TOP);
        dto.setOuts(0);
        dto.setBalls(0);
        dto.setStrikes(0);
        dto.setHomeScore(0);
        dto.setAwayScore(0);
        dto.setStatus(BaseballGameStatus.NOT_STARTED);
        return dto;
    }

    private UserPrincipal principal(UserRole role, Long teamId) {
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

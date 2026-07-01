package com.matchmetrics.controller.softball;

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
class SoftballGameStateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IBaseballGameStateService gameStateService;

    @MockitoBean
    private IMatchService matchService;

    @BeforeEach
    void setupMatchMock() {
        TeamDTO homeTeam = new TeamDTO();
        homeTeam.setId(1L);
        homeTeam.setName("Home Softball");
        homeTeam.setAcronym("HMS");
        homeTeam.setStadium("Estadio Local");

        TeamDTO awayTeam = new TeamDTO();
        awayTeam.setId(2L);
        awayTeam.setName("Away Softball");
        awayTeam.setAcronym("AWS");
        awayTeam.setStadium("Estadio Visitante");

        MatchDTO match = new MatchDTO();
        match.setHomeTeam(homeTeam);
        match.setAwayTeam(awayTeam);

        when(matchService.getMatchById(anyLong())).thenReturn(match);
    }

    // ── POST /api/v1/softball/game-state ────────────────────

    @Test
    void create_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/softball/game-state")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_ShouldReturn403_WhenUserRole() throws Exception {
        mockMvc.perform(post("/api/v1/softball/game-state")
                        .with(user(principal(UserRole.USER, 1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO())))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_ShouldReturn403_WhenManagerFromDifferentTeam() throws Exception {
        mockMvc.perform(post("/api/v1/softball/game-state")
                        .with(user(principal(UserRole.MANAGER, 3L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO())))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_ShouldReturn200_WhenManagerOfParticipatingTeam() throws Exception {
        BaseballGameStateDTO saved = sampleDTO();
        saved.setId(10L);
        when(gameStateService.createGameState(any())).thenReturn(saved);

        mockMvc.perform(post("/api/v1/softball/game-state")
                        .with(user(principal(UserRole.MANAGER, 1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO())))
                .andExpect(status().isOk());
    }

    @Test
    void create_ShouldReturn200_WhenAdmin() throws Exception {
        BaseballGameStateDTO saved = sampleDTO();
        saved.setId(10L);
        when(gameStateService.createGameState(any())).thenReturn(saved);

        mockMvc.perform(post("/api/v1/softball/game-state")
                        .with(user(principal(UserRole.ADMIN, null)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO())))
                .andExpect(status().isOk());
    }

    // ── GET /api/v1/softball/game-state/{matchId} ───────────

    @Test
    void get_ShouldBeAccessible_WhenParticipatingUser() throws Exception {
        when(gameStateService.getGameStateByMatchId(anyLong())).thenReturn(sampleDTO());

        mockMvc.perform(get("/api/v1/softball/game-state/1")
                        .with(user(principal(UserRole.USER, 1L))))
                .andExpect(status().isOk());
    }

    @Test
    void get_ShouldReturn403_WhenUserFromDifferentTeam() throws Exception {
        mockMvc.perform(get("/api/v1/softball/game-state/1")
                        .with(user(principal(UserRole.USER, 3L))))
                .andExpect(status().isForbidden());
    }

    // ── PUT /api/v1/softball/game-state/{matchId} ───────────

    @Test
    void update_ShouldReturn200_WhenManagerOfParticipatingTeam() throws Exception {
        when(gameStateService.updateGameState(anyLong(), any())).thenReturn(sampleDTO());

        mockMvc.perform(put("/api/v1/softball/game-state/1")
                        .with(user(principal(UserRole.MANAGER, 2L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO())))
                .andExpect(status().isOk());
    }

    @Test
    void update_ShouldReturn400_WhenOutsIsThree() throws Exception {
        when(gameStateService.updateGameState(anyLong(), any()))
                .thenThrow(new IllegalArgumentException("Outs must be between 0 and 2"));

        BaseballGameStateDTO dto = sampleDTO();
        dto.setOuts(3);

        mockMvc.perform(put("/api/v1/softball/game-state/1")
                        .with(user(principal(UserRole.ADMIN, null)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ── POST /api/v1/softball/game-state/{matchId}/rebuild ──

    @Test
    void rebuild_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/softball/game-state/1/rebuild"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rebuild_ShouldReturn403_WhenUserRole() throws Exception {
        mockMvc.perform(post("/api/v1/softball/game-state/1/rebuild")
                        .with(user(principal(UserRole.USER, 1L))))
                .andExpect(status().isForbidden());
    }

    @Test
    void rebuild_ShouldReturn200_WhenAdmin() throws Exception {
        when(gameStateService.rebuildGameStateFromEvents(anyLong())).thenReturn(sampleDTO());

        mockMvc.perform(post("/api/v1/softball/game-state/1/rebuild")
                        .with(user(principal(UserRole.ADMIN, null))))
                .andExpect(status().isOk());
    }

    @Test
    void rebuild_ShouldReturn200_WhenManagerOfParticipatingTeam() throws Exception {
        when(gameStateService.rebuildGameStateFromEvents(anyLong())).thenReturn(sampleDTO());

        mockMvc.perform(post("/api/v1/softball/game-state/1/rebuild")
                        .with(user(principal(UserRole.MANAGER, 1L))))
                .andExpect(status().isOk());
    }

    // ── DELETE /api/v1/softball/game-state/{matchId} ────────

    @Test
    void delete_ShouldReturn403_WhenManagerRole() throws Exception {
        mockMvc.perform(delete("/api/v1/softball/game-state/1")
                        .with(user(principal(UserRole.MANAGER, 1L))))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_ShouldReturn204_WhenAdmin() throws Exception {
        mockMvc.perform(delete("/api/v1/softball/game-state/1")
                        .with(user(principal(UserRole.ADMIN, null))))
                .andExpect(status().isNoContent());
    }

    // ── Helpers ──────────────────────────────────────────────

    private BaseballGameStateDTO sampleDTO() {
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
        user.setId(99L);
        user.setEmail(role.name().toLowerCase() + "@softball.test");
        user.setPassword("password");
        user.setFullName(role.name() + " Softball Tester");
        user.setRole(role);
        user.setStatus(UserStatus.APPROVED);

        if (teamId != null) {
            Team team = new Team();
            team.setId(teamId);
            team.setName("Equipo Softball");
            team.setAcronym("SFB");
            team.setStadium("Estadio Softball");
            user.setTeam(team);
        }

        return new UserPrincipal(user);
    }
}

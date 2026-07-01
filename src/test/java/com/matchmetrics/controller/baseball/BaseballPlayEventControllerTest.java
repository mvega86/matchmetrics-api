package com.matchmetrics.controller.baseball;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchmetrics.domain.enums.BaseballEventType;
import com.matchmetrics.domain.enums.InningHalf;
import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.domain.enums.UserStatus;
import com.matchmetrics.mapper.dto.BaseballPlayEventDTO;
import com.matchmetrics.mapper.dto.MatchDTO;
import com.matchmetrics.mapper.dto.TeamDTO;
import com.matchmetrics.persistence.entity.AppUser;
import com.matchmetrics.persistence.entity.Team;
import com.matchmetrics.security.UserPrincipal;
import com.matchmetrics.service.IBaseballPlayEventService;
import com.matchmetrics.service.IMatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BaseballPlayEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IBaseballPlayEventService playEventService;

    @MockitoBean
    private IMatchService matchService;

    // Partido con homeTeam=1, awayTeam=2
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

        BaseballPlayEventDTO existingEvent = sampleEventDTO();
        existingEvent.setId(1L);
        when(playEventService.getPlayEventById(anyLong())).thenReturn(existingEvent);
    }

    // -------------------------
    // POST /api/v1/baseball/play-events
    // -------------------------

    @Test
    void create_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/baseball/play-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleEventDTO())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_ShouldReturn403_WhenUserRole() throws Exception {
        mockMvc.perform(post("/api/v1/baseball/play-events")
                        .with(user(principal(UserRole.USER, 1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleEventDTO())))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_ShouldReturn403_WhenManagerFromDifferentTeam() throws Exception {
        // MANAGER del equipo 3, pero el partido es entre equipos 1 y 2
        mockMvc.perform(post("/api/v1/baseball/play-events")
                        .with(user(principal(UserRole.MANAGER, 3L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleEventDTO())))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_ShouldAllowManagerOfParticipatingTeam() throws Exception {
        BaseballPlayEventDTO saved = sampleEventDTO();
        saved.setId(1L);
        when(playEventService.createPlayEvent(any())).thenReturn(saved);

        mockMvc.perform(post("/api/v1/baseball/play-events")
                        .with(user(principal(UserRole.MANAGER, 1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleEventDTO())))
                .andExpect(status().isOk());
    }

    @Test
    void create_ShouldAllowAdminRole() throws Exception {
        BaseballPlayEventDTO saved = sampleEventDTO();
        saved.setId(1L);
        when(playEventService.createPlayEvent(any())).thenReturn(saved);

        mockMvc.perform(post("/api/v1/baseball/play-events")
                        .with(user(principal(UserRole.ADMIN, null)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleEventDTO())))
                .andExpect(status().isOk());
    }

    // -------------------------
    // GET /api/v1/baseball/play-events/{id}
    // -------------------------

    @Test
    void getById_ShouldBePublic_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/baseball/play-events/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_ShouldReturn403_WhenUserFromDifferentTeam() throws Exception {
        mockMvc.perform(get("/api/v1/baseball/play-events/1")
                        .with(user(principal(UserRole.USER, 3L))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_ShouldAllowUserOfParticipatingTeam() throws Exception {
        mockMvc.perform(get("/api/v1/baseball/play-events/1")
                        .with(user(principal(UserRole.USER, 1L))))
                .andExpect(status().isOk());
    }

    @Test
    void getById_ShouldAllowAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/baseball/play-events/1")
                        .with(user(principal(UserRole.ADMIN, null))))
                .andExpect(status().isOk());
    }

    // -------------------------
    // GET /api/v1/baseball/play-events?search=match:X
    // -------------------------

    @Test
    void getAll_ShouldBePublic_WhenNotAuthenticatedAndMatchFilterProvided() throws Exception {
        when(playEventService.search(any())).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/baseball/play-events").param("search", "match:1"))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_ShouldReturn400_WhenNotAuthenticatedAndNoMatchFilter() throws Exception {
        mockMvc.perform(get("/api/v1/baseball/play-events"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_ShouldReturn200_WhenManagerSearchesAnyMatch() throws Exception {
        when(playEventService.search(anyString())).thenReturn(Collections.emptyList());
        // Reads are now public — managers can query any match's events
        mockMvc.perform(get("/api/v1/baseball/play-events")
                        .param("search", "match:1")
                        .with(user(principal(UserRole.MANAGER, 3L))))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_ShouldAllowManagerSearchingOwnMatch() throws Exception {
        when(playEventService.search(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/baseball/play-events")
                        .param("search", "match:1")
                        .with(user(principal(UserRole.MANAGER, 1L))))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_AdminShouldGetAllWithoutFilter() throws Exception {
        when(playEventService.search(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/baseball/play-events")
                        .with(user(principal(UserRole.ADMIN, null))))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_ManagerWithoutSearchShouldGetOwnTeamEvents() throws Exception {
        when(playEventService.searchByTeam(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/baseball/play-events")
                        .with(user(principal(UserRole.MANAGER, 1L))))
                .andExpect(status().isOk());
    }

    // -------------------------
    // PUT /api/v1/baseball/play-events/{id}
    // -------------------------

    @Test
    void update_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/api/v1/baseball/play-events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void update_ShouldReturn403_WhenUserRole() throws Exception {
        mockMvc.perform(put("/api/v1/baseball/play-events/1")
                        .with(user(principal(UserRole.USER, 1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleEventDTO())))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_ShouldReturn403_WhenManagerFromDifferentTeam() throws Exception {
        mockMvc.perform(put("/api/v1/baseball/play-events/1")
                        .with(user(principal(UserRole.MANAGER, 3L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleEventDTO())))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_ShouldAllowManagerOfParticipatingTeam() throws Exception {
        BaseballPlayEventDTO updated = sampleEventDTO();
        updated.setId(1L);
        when(playEventService.updatePlayEvent(anyLong(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/baseball/play-events/1")
                        .with(user(principal(UserRole.MANAGER, 1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleEventDTO())))
                .andExpect(status().isOk());
    }

    @Test
    void update_ShouldAllowAdminRole() throws Exception {
        BaseballPlayEventDTO updated = sampleEventDTO();
        updated.setId(1L);
        when(playEventService.updatePlayEvent(anyLong(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/baseball/play-events/1")
                        .with(user(principal(UserRole.ADMIN, null)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleEventDTO())))
                .andExpect(status().isOk());
    }

    // -------------------------
    // DELETE /api/v1/baseball/play-events/{id}
    // -------------------------

    @Test
    void delete_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/v1/baseball/play-events/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_ShouldReturn403_WhenUserRole() throws Exception {
        mockMvc.perform(delete("/api/v1/baseball/play-events/1")
                        .with(user(principal(UserRole.USER, 1L))))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_ShouldReturn403_WhenManagerFromDifferentTeam() throws Exception {
        mockMvc.perform(delete("/api/v1/baseball/play-events/1")
                        .with(user(principal(UserRole.MANAGER, 3L))))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_ShouldAllowManagerOfParticipatingTeam() throws Exception {
        mockMvc.perform(delete("/api/v1/baseball/play-events/1")
                        .with(user(principal(UserRole.MANAGER, 1L))))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_ShouldAllowAdminRole() throws Exception {
        mockMvc.perform(delete("/api/v1/baseball/play-events/1")
                        .with(user(principal(UserRole.ADMIN, null))))
                .andExpect(status().isNoContent());
    }

    // -------------------------
    // POST — player-match integrity validation
    // -------------------------

    @Test
    void create_ShouldReturn400_WhenBatterDoesNotBelongToMatch() throws Exception {
        when(playEventService.createPlayEvent(any()))
                .thenThrow(new IllegalArgumentException("Batter PlayerMatch 99 does not belong to match 1"));

        BaseballPlayEventDTO dto = sampleEventDTO();
        dto.setBatterPlayerMatchId(99L);

        mockMvc.perform(post("/api/v1/baseball/play-events")
                        .with(user(principal(UserRole.ADMIN, null)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_ShouldReturn400_WhenPitcherDoesNotBelongToMatch() throws Exception {
        when(playEventService.createPlayEvent(any()))
                .thenThrow(new IllegalArgumentException("Pitcher PlayerMatch 99 does not belong to match 1"));

        BaseballPlayEventDTO dto = sampleEventDTO();
        dto.setPitcherPlayerMatchId(99L);

        mockMvc.perform(post("/api/v1/baseball/play-events")
                        .with(user(principal(UserRole.ADMIN, null)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------
    // Helpers
    // -------------------------

    private BaseballPlayEventDTO sampleEventDTO() {
        BaseballPlayEventDTO dto = new BaseballPlayEventDTO();
        dto.setMatchId(1L);
        dto.setInning(1);
        dto.setInningHalf(InningHalf.TOP);
        dto.setEventType(BaseballEventType.SINGLE);
        dto.setBattingTeamId(1L);
        dto.setFieldingTeamId(2L);
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

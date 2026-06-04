package com.matchmetrics.controller.baseball;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchmetrics.domain.enums.BaseballEventType;
import com.matchmetrics.domain.enums.InningHalf;
import com.matchmetrics.domain.enums.UserRole;
import com.matchmetrics.domain.enums.UserStatus;
import com.matchmetrics.mapper.dto.BaseballPlayEventDTO;
import com.matchmetrics.persistence.entity.AppUser;
import com.matchmetrics.persistence.entity.Team;
import com.matchmetrics.security.UserPrincipal;
import com.matchmetrics.service.IBaseballPlayEventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

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

    // -------------------------
    // GET /api/v1/baseball/play-events
    // -------------------------

    @Test
    void getAll_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/baseball/play-events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAll_ShouldAllowUserRole() throws Exception {
        when(playEventService.search(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/baseball/play-events")
                        .param("search", "match:1")
                        .with(user(principal(UserRole.USER, 1L))))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_ShouldAllowManagerRole() throws Exception {
        when(playEventService.search(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/baseball/play-events")
                        .param("search", "match:1")
                        .with(user(principal(UserRole.MANAGER, 1L))))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_ShouldAllowAdminRole() throws Exception {
        when(playEventService.search(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/baseball/play-events")
                        .with(user(principal(UserRole.ADMIN, null))))
                .andExpect(status().isOk());
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
    void create_ShouldAllowManagerRole() throws Exception {
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
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_ShouldAllowManagerRole() throws Exception {
        BaseballPlayEventDTO updated = sampleEventDTO();
        updated.setId(1L);
        when(playEventService.updatePlayEvent(anyLong(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/baseball/play-events/1")
                        .with(user(principal(UserRole.MANAGER, 1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
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
                        .content("{}"))
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
    void delete_ShouldAllowManagerRole() throws Exception {
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

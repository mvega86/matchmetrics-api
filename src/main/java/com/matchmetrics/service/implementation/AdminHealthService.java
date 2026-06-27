package com.matchmetrics.service.implementation;

import com.matchmetrics.domain.enums.MatchState;
import com.matchmetrics.mapper.dto.admin.*;
import com.matchmetrics.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminHealthService {

    private final AppUserRepository userRepository;
    private final MatchRepository matchRepository;
    private final BaseballPlayEventRepository playEventRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final TournamentRepository tournamentRepository;

    public SystemHealthDTO getSystemHealth() {
        long totalUsers       = userRepository.count();
        long totalMatches     = matchRepository.count();
        long activeLive       = matchRepository.countByState(MatchState.STARTED);
        long totalEvents      = playEventRepository.count();
        Double tableSizeMBRaw = playEventRepository.getTableSizeMB();
        double tableSizeMB    = tableSizeMBRaw != null ? tableSizeMBRaw : 0.0;
        long totalPlayers     = playerRepository.count();
        long totalTeams       = teamRepository.count();
        long totalTournaments = tournamentRepository.count();

        int phase = calculatePhase(totalUsers);

        SystemMetricsDTO metrics = SystemMetricsDTO.builder()
                .totalUsers(totalUsers)
                .totalMatches(totalMatches)
                .activeLiveMatches(activeLive)
                .totalPlayEvents(totalEvents)
                .playEventTableSizeMB(tableSizeMB)
                .totalPlayers(totalPlayers)
                .totalTeams(totalTeams)
                .totalTournaments(totalTournaments)
                .currentPhase(phase)
                .phaseLabel(phaseLabel(phase))
                .phaseDescription(phaseDescription(phase))
                .nextPhaseThreshold(nextPhaseThreshold(phase))
                .build();

        return SystemHealthDTO.builder()
                .metrics(metrics)
                .alerts(generateAlerts(totalUsers, totalEvents, activeLive))
                .generatedAt(LocalDateTime.now())
                .build();
    }

    // ── Phase calculation ──────────────────────────────────────────────────────

    private int calculatePhase(long totalUsers) {
        if (totalUsers < 50)     return 1;
        if (totalUsers < 500)    return 2;
        if (totalUsers < 5_000)  return 3;
        if (totalUsers < 50_000) return 4;
        return 5;
    }

    private String phaseLabel(int phase) {
        return switch (phase) {
            case 1 -> "PHASE_1";
            case 2 -> "PHASE_2";
            case 3 -> "PHASE_3";
            case 4 -> "PHASE_4";
            default -> "PHASE_5";
        };
    }

    private String phaseDescription(int phase) {
        return switch (phase) {
            case 1 -> "Sistema en fase inicial. Toda la arquitectura actual soporta la carga sin cambios.";
            case 2 -> "Crecimiento moderado. El pool de conexiones puede sentir presión con partidos en vivo simultáneos.";
            case 3 -> "Escala media. Se requieren caché, paginación y SSE para sostener la carga.";
            case 4 -> "Escala alta. Necesario Redis, vistas materializadas y archivado de temporadas.";
            default -> "Gran escala. Requiere WebSocket, read replicas y separación lectura/escritura.";
        };
    }

    private String nextPhaseThreshold(int phase) {
        return switch (phase) {
            case 1 -> "50 usuarios";
            case 2 -> "500 usuarios";
            case 3 -> "5.000 usuarios";
            case 4 -> "50.000 usuarios";
            default -> "Ya en fase máxima";
        };
    }

    // ── Alerts ────────────────────────────────────────────────────────────────

    private List<SystemAlertDTO> generateAlerts(long totalUsers, long totalEvents, long activeLive) {
        List<SystemAlertDTO> alerts = new ArrayList<>();

        // play_event table size
        if (totalEvents >= 1_000_000) {
            alerts.add(SystemAlertDTO.builder()
                    .id("ALERT_EVENT_CRITICAL")
                    .level("CRITICAL")
                    .title("Tabla de eventos requiere archivado urgente")
                    .description("La tabla baseball_play_event supera 1 millón de filas. Las queries de estadísticas serán cada vez más lentas.")
                    .metric("total_play_events")
                    .currentValue(totalEvents)
                    .threshold(1_000_000)
                    .recommendation("Implementar archivado de temporadas antiguas (R-LP-04). Añadir vista materializada de estadísticas (R-LP-03).")
                    .build());
        } else if (totalEvents >= 100_000) {
            alerts.add(SystemAlertDTO.builder()
                    .id("ALERT_EVENT_WARNING")
                    .level("WARNING")
                    .title("Tabla de eventos en crecimiento")
                    .description("La tabla baseball_play_event supera 100.000 filas. Planificar estrategia de archivado.")
                    .metric("total_play_events")
                    .currentValue(totalEvents)
                    .threshold(100_000)
                    .recommendation("Planificar archivado de torneos cerrados (R-LP-04) antes de alcanzar 1 millón de filas.")
                    .build());
        }

        // Active live matches vs connection pool
        // Thresholds account for Caffeine cache (2s TTL on getGameStateByMatchId),
        // which absorbs ~90% of polling load — only writes hit the DB.
        if (activeLive >= 10) {
            alerts.add(SystemAlertDTO.builder()
                    .id("ALERT_LIVE_CRITICAL")
                    .level("CRITICAL")
                    .title("Pool de conexiones bajo presión alta")
                    .description("Hay " + activeLive + " partidos en vivo. Aunque el caché absorbe las lecturas, las escrituras frecuentes pueden saturar HikariCP.")
                    .metric("active_live_matches")
                    .currentValue(activeLive)
                    .threshold(10)
                    .recommendation("Implementar SSE (R-MP-03) para eliminar el polling y escalar a 100+ usuarios por partido.")
                    .build());
        } else if (activeLive >= 6) {
            alerts.add(SystemAlertDTO.builder()
                    .id("ALERT_LIVE_WARNING")
                    .level("WARNING")
                    .title("Múltiples partidos en vivo activos")
                    .description("Hay " + activeLive + " partidos en vivo. El caché Caffeine (2s TTL) reduce la carga, pero vigilar la contención en escrituras.")
                    .metric("active_live_matches")
                    .currentValue(activeLive)
                    .threshold(6)
                    .recommendation("Considerar SSE (R-MP-03) para eliminar el polling por completo y liberar el pool de conexiones.")
                    .build());
        }

        // Phase transition proximity
        if (totalUsers >= 40 && totalUsers < 50) {
            alerts.add(SystemAlertDTO.builder()
                    .id("ALERT_PHASE2_APPROACHING")
                    .level("INFO")
                    .title("Aproximándose a Fase 2 (100 usuarios)")
                    .description("Con " + totalUsers + " usuarios, el sistema está cerca del umbral de Fase 2.")
                    .metric("total_users")
                    .currentValue(totalUsers)
                    .threshold(50)
                    .recommendation("Revisar R-CP-01 (caché) antes de llegar a Fase 2 para mantener rendimiento en live scoring.")
                    .build());
        } else if (totalUsers >= 400 && totalUsers < 500) {
            alerts.add(SystemAlertDTO.builder()
                    .id("ALERT_PHASE3_APPROACHING")
                    .level("INFO")
                    .title("Aproximándose a Fase 3 (1.000 usuarios)")
                    .description("Con " + totalUsers + " usuarios, el sistema se acerca al umbral donde paginación y SSE son necesarios.")
                    .metric("total_users")
                    .currentValue(totalUsers)
                    .threshold(500)
                    .recommendation("Implementar R-MP-01 (paginación) y R-MP-03 (SSE) antes de superar los 500 usuarios.")
                    .build());
        } else if (totalUsers >= 4_000 && totalUsers < 5_000) {
            alerts.add(SystemAlertDTO.builder()
                    .id("ALERT_PHASE4_APPROACHING")
                    .level("WARNING")
                    .title("Aproximándose a Fase 4 (10.000 usuarios)")
                    .description("Con " + totalUsers + " usuarios, se acerca la necesidad de Redis, vistas materializadas y archivado.")
                    .metric("total_users")
                    .currentValue(totalUsers)
                    .threshold(5_000)
                    .recommendation("Planificar R-LP-02 (Redis), R-LP-03 (vistas materializadas) y R-LP-04 (archivado) para esta temporada.")
                    .build());
        }

        return alerts;
    }

}

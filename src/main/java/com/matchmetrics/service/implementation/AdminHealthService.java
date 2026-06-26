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
                .roadmap(buildRoadmap())
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
        if (activeLive >= 4) {
            alerts.add(SystemAlertDTO.builder()
                    .id("ALERT_LIVE_CRITICAL")
                    .level("CRITICAL")
                    .title("Pool de conexiones saturado")
                    .description("Hay " + activeLive + " partidos en vivo activos. Con HikariCP máx=5, las peticiones de polling están en cola.")
                    .metric("active_live_matches")
                    .currentValue(activeLive)
                    .threshold(4)
                    .recommendation("Implementar caché in-memory del estado de juego (R-CP-01) para reducir la carga de BD por poll.")
                    .build());
        } else if (activeLive >= 2) {
            alerts.add(SystemAlertDTO.builder()
                    .id("ALERT_LIVE_WARNING")
                    .level("WARNING")
                    .title("Múltiples partidos en vivo activos")
                    .description("Hay " + activeLive + " partidos en vivo activos. El pool de 5 conexiones puede sufrir contención con overlays abiertos.")
                    .metric("active_live_matches")
                    .currentValue(activeLive)
                    .threshold(2)
                    .recommendation("Considerar caché in-memory del estado de juego (R-CP-01) para liberar conexiones de BD.")
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

    // ── Roadmap (hardcoded from AUDITORIA_ESCALABILIDAD_ROADMAP.md) ───────────

    private List<RoadmapItemDTO> buildRoadmap() {
        return List.of(
            // ── Corto plazo ──────────────────────────────────────────────────
            RoadmapItemDTO.builder()
                    .id("R-CP-01").horizon("SHORT").priority("HIGH").complexity("LOW").status("PENDING").breakingChange(false)
                    .title("Caché in-memory para estado de juego")
                    .description("Spring Cache con Caffeine. TTL 1s. Elimina el 90% de queries de BD en polling.")
                    .benefit("Soporta 10× más usuarios en live sin cambiar infraestructura.")
                    .build(),
            RoadmapItemDTO.builder()
                    .id("R-CP-02").horizon("SHORT").priority("HIGH").complexity("LOW").status("PENDING").breakingChange(false)
                    .title("Documentación de API con SpringDoc / OpenAPI")
                    .description("Añadir springdoc-openapi. Swagger UI automático en /swagger-ui.html.")
                    .benefit("Facilita onboarding y habilita integraciones externas.")
                    .build(),
            RoadmapItemDTO.builder()
                    .id("R-CP-03").horizon("SHORT").priority("HIGH").complexity("LOW").status("PENDING").breakingChange(false)
                    .title("Error Boundary global en el frontend")
                    .description("Componente React ErrorBoundary que envuelve toda la app.")
                    .benefit("Evita que un error en un overlay colapse toda la aplicación en directo.")
                    .build(),
            RoadmapItemDTO.builder()
                    .id("R-CP-04").horizon("SHORT").priority("HIGH").complexity("LOW").status("PENDING").breakingChange(false)
                    .title("Pipeline CI/CD básico (GitHub Actions)")
                    .description("Build + tests + deploy automático en merge a main para backend y frontend.")
                    .benefit("Detecta errores de build antes de llegar a producción.")
                    .build(),
            RoadmapItemDTO.builder()
                    .id("R-CP-05").horizon("SHORT").priority("MEDIUM").complexity("LOW").status("PENDING").breakingChange(false)
                    .title("Rate limiting general en endpoints de stats y live")
                    .description("Extender LoginRateLimiter o usar Bucket4j. Límite de 60 req/min por IP.")
                    .benefit("Protege contra scraping masivo y ataques DoS inadvertidos.")
                    .build(),
            RoadmapItemDTO.builder()
                    .id("R-CP-06").horizon("SHORT").priority("MEDIUM").complexity("LOW").status("PENDING").breakingChange(false)
                    .title("Renombrar baseball_play_event → play_event")
                    .description("Migración Flyway V54 de renombrado de tabla. Find+replace en repositorios.")
                    .benefit("Elimina naming incorrecto antes de añadir más deportes.")
                    .build(),
            RoadmapItemDTO.builder()
                    .id("R-CP-07").horizon("SHORT").priority("HIGH").complexity("LOW").status("PENDING").breakingChange(false)
                    .title("Tests unitarios de reglas de negocio críticas")
                    .description("JUnit 5 para fórmulas ERA, OBP, AVG, reglas de innings y reconstrucción de estado.")
                    .benefit("Protege las fórmulas estadísticas contra regresiones en futuros cambios.")
                    .build(),

            // ── Medio plazo ──────────────────────────────────────────────────
            RoadmapItemDTO.builder()
                    .id("R-MP-01").horizon("MEDIUM").priority("HIGH").complexity("MEDIUM").status("PENDING").breakingChange(false)
                    .title("Paginación en todos los endpoints de lista")
                    .description("Page<T> y Pageable en Spring. Parámetros opcionales page/size (default: todos).")
                    .benefit("Desbloquea Fase 3. Sin paginación el sistema no escala a 1.000 usuarios.")
                    .build(),
            RoadmapItemDTO.builder()
                    .id("R-MP-02").horizon("MEDIUM").priority("MEDIUM").complexity("MEDIUM").status("PENDING").breakingChange(false)
                    .title("StatsFormulas: clase de cálculo centralizada")
                    .description("Extraer obp(), formatAvg(), formatEra(), outsFromEvent() a clase compartida.")
                    .benefit("Elimina duplicación entre SoftballStatsService y PlayerStatisticsQueryService.")
                    .build(),
            RoadmapItemDTO.builder()
                    .id("R-MP-03").horizon("MEDIUM").priority("HIGH").complexity("MEDIUM").status("PENDING").breakingChange(false)
                    .title("Server-Sent Events (SSE) para live scoring")
                    .description("SseEmitter en Spring Boot. EventSource en React. El servidor empuja cuando el estado cambia.")
                    .benefit("Elimina el polling. Permite 100+ usuarios en live. Reduce carga de BD en 90%.")
                    .build(),
            RoadmapItemDTO.builder()
                    .id("R-MP-04").horizon("MEDIUM").priority("MEDIUM").complexity("MEDIUM").status("PENDING").breakingChange(false)
                    .title("Refresh tokens")
                    .description("Tabla refresh_token + endpoint /auth/refresh. Access token de 15 min.")
                    .benefit("Permite invalidar sesiones comprometidas. Reduce ventana de exposición de tokens.")
                    .build(),
            RoadmapItemDTO.builder()
                    .id("R-MP-05").horizon("MEDIUM").priority("MEDIUM").complexity("MEDIUM").status("PENDING").breakingChange(false)
                    .title("División de SoftballGamePanel y BaseballLiveView")
                    .description("Extraer ScoreBoard, BatterLineup, PitcherTracker, CountDisplay. Hook useSoftballGameState.")
                    .benefit("Componentes mantenibles, testables individualmente y reutilizables para nuevos deportes.")
                    .build(),
            RoadmapItemDTO.builder()
                    .id("R-MP-06").horizon("MEDIUM").priority("MEDIUM").complexity("LOW").status("PENDING").breakingChange(false)
                    .title("Separar migraciones de seed del esquema Flyway")
                    .description("Mover datos de ejemplo a scripts separados no ejecutados en producción.")
                    .benefit("Flyway queda limpio. Los entornos de producción no cargan datos de test.")
                    .build(),
            RoadmapItemDTO.builder()
                    .id("R-MP-07").horizon("MEDIUM").priority("LOW").complexity("MEDIUM").status("PENDING").breakingChange(false)
                    .title("MapStruct para conversión entidad → DTO")
                    .description("Sustituir mappers manuales por generación automática con MapStruct.")
                    .benefit("Elimina código repetitivo. Nuevos campos se mapean automáticamente.")
                    .build(),
            RoadmapItemDTO.builder()
                    .id("R-MP-08").horizon("MEDIUM").priority("MEDIUM").complexity("LOW").status("PENDING").breakingChange(false)
                    .title("Exportación CSV / Excel de estadísticas")
                    .description("Nuevo endpoint GET /stats/export. Apache POI o librería CSV.")
                    .benefit("Funcionalidad esperada por federaciones y usuarios avanzados.")
                    .build(),

            // ── Largo plazo ──────────────────────────────────────────────────
            RoadmapItemDTO.builder()
                    .id("R-LP-01").horizon("LONG").priority("HIGH").complexity("HIGH").status("PENDING").breakingChange(false)
                    .title("Abstracción de evento deportivo genérico")
                    .description("Tabla play_event con campo data JSONB para detalles específicos por deporte.")
                    .benefit("Permite añadir nuevos deportes sin duplicar infraestructura de eventos.")
                    .build(),
            RoadmapItemDTO.builder()
                    .id("R-LP-02").horizon("LONG").priority("HIGH").complexity("MEDIUM").status("PENDING").breakingChange(false)
                    .title("Caché Redis compartida")
                    .description("Spring Cache + Redis. Caché distribuida que sobrevive a reinicios del servidor.")
                    .benefit("Habilita horizontal scaling. Necesaria en Fase 4 (10.000 usuarios).")
                    .build(),
            RoadmapItemDTO.builder()
                    .id("R-LP-03").horizon("LONG").priority("HIGH").complexity("MEDIUM").status("PENDING").breakingChange(false)
                    .title("Vistas materializadas de estadísticas")
                    .description("CREATE MATERIALIZED VIEW player_stats_snapshot en PostgreSQL. Refresco al cerrar partido.")
                    .benefit("Estadísticas pre-calculadas. Queries de stats pasan de O(N eventos) a O(1).")
                    .build(),
            RoadmapItemDTO.builder()
                    .id("R-LP-04").horizon("LONG").priority("HIGH").complexity("MEDIUM").status("PENDING").breakingChange(false)
                    .title("Archivado de temporadas antiguas")
                    .description("Tabla play_event_archive + job batch al cierre de torneo.")
                    .benefit("Mantiene tablas de producción pequeñas. Previene degradación de queries de stats.")
                    .build(),
            RoadmapItemDTO.builder()
                    .id("R-LP-05").horizon("LONG").priority("MEDIUM").complexity("LOW").status("PENDING").breakingChange(false)
                    .title("Perfiles Spring (dev / staging / prod)")
                    .description("application-dev.properties, application-prod.properties. Beans distintos por entorno.")
                    .benefit("Caché desactivada en local, activada en prod. Email real solo en prod.")
                    .build(),
            RoadmapItemDTO.builder()
                    .id("R-LP-06").horizon("LONG").priority("LOW").complexity("HIGH").status("PENDING").breakingChange(true)
                    .title("Sistema RBAC granular")
                    .description("Roles STATISTICIAN, COMMENTATOR, etc. Permisos por acción sin modificar código.")
                    .benefit("Nuevos tipos de usuario sin tocar SecurityConfig ni @PreAuthorize.")
                    .build(),
            RoadmapItemDTO.builder()
                    .id("R-LP-07").horizon("LONG").priority("HIGH").complexity("MEDIUM").status("PENDING").breakingChange(false)
                    .title("Monitoreo APM")
                    .description("Grafana + Prometheus (gratuito) o Datadog. Alertas automáticas en producción.")
                    .benefit("Visibilidad de tiempos de respuesta, queries lentas y errores en tiempo real.")
                    .build(),

            // ── Muy largo plazo ──────────────────────────────────────────────
            RoadmapItemDTO.builder()
                    .id("R-VLP-01").horizon("VERY_LONG").priority("HIGH").complexity("HIGH").status("PENDING").breakingChange(false)
                    .title("WebSocket con broadcasting (reemplaza SSE)")
                    .description("Spring WebSocket + STOMP. Broadcast a todos los subscribers de un partido.")
                    .benefit("Latencia sub-100ms. Integración nativa con OBS. Soporta 1.000+ usuarios/partido.")
                    .build(),
            RoadmapItemDTO.builder()
                    .id("R-VLP-02").horizon("VERY_LONG").priority("MEDIUM").complexity("HIGH").status("PENDING").breakingChange(false)
                    .title("Separación lectura / escritura (CQRS parcial)")
                    .description("PostgreSQL read replicas + routing en Spring.")
                    .benefit("Queries pesadas de stats no compiten con escrituras de eventos live.")
                    .build(),
            RoadmapItemDTO.builder()
                    .id("R-VLP-03").horizon("VERY_LONG").priority("MEDIUM").complexity("MEDIUM").status("PENDING").breakingChange(false)
                    .title("API pública para integraciones externas")
                    .description("Versión v2 + API keys + rate limiting por key.")
                    .benefit("Permite a clubs, medios y apps de terceros consumir datos de MatchMetrics.")
                    .build(),
            RoadmapItemDTO.builder()
                    .id("R-VLP-04").horizon("VERY_LONG").priority("LOW").complexity("HIGH").status("PENDING").breakingChange(false)
                    .title("App móvil")
                    .description("React Native compartiendo lógica con el frontend web.")
                    .benefit("Acceso a live scoring y estadísticas desde el campo con tablets y móviles.")
                    .build()
        );
    }
}

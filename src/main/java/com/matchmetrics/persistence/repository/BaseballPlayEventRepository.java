package com.matchmetrics.persistence.repository;

import com.matchmetrics.domain.enums.SportType;
import com.matchmetrics.persistence.entity.BaseballPlayEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaseballPlayEventRepository extends JpaRepository<BaseballPlayEvent, Long> {
    List<BaseballPlayEvent> findAllByMatchIdOrderByCreatedAtAsc(Long matchId);
    List<BaseballPlayEvent> findByBattingTeamIdOrFieldingTeamIdOrderByCreatedAtAsc(Long battingTeamId, Long fieldingTeamId);

    @Modifying
    @Query("DELETE FROM BaseballPlayEvent e WHERE e.match.id = :matchId")
    void deleteAllByMatchId(@Param("matchId") Long matchId);

    @Query("SELECT e FROM BaseballPlayEvent e WHERE e.match.tournament IS NOT NULL AND e.match.tournament.id = :tournamentId AND e.batterPlayerMatch IS NOT NULL AND e.batterPlayerMatch.player.id = :playerId")
    List<BaseballPlayEvent> findBattingEventsByTournamentAndPlayer(@Param("tournamentId") Long tournamentId, @Param("playerId") Long playerId);

    @Query("SELECT e FROM BaseballPlayEvent e WHERE e.match.tournament IS NOT NULL AND e.match.tournament.id = :tournamentId AND e.pitcherPlayerMatch IS NOT NULL AND e.pitcherPlayerMatch.player.id = :playerId")
    List<BaseballPlayEvent> findPitchingEventsByTournamentAndPlayer(@Param("tournamentId") Long tournamentId, @Param("playerId") Long playerId);

    // JOIN FETCH match + tournament so getPlayerStatsDetail can access e.getMatch().getTournament()
    // without triggering lazy-load N+1 (Match.tournament is FetchType.LAZY).
    @Query("SELECT e FROM BaseballPlayEvent e " +
           "JOIN FETCH e.match m " +
           "LEFT JOIN FETCH m.tournament " +
           "WHERE e.batterPlayerMatch IS NOT NULL AND e.batterPlayerMatch.player.id = :playerId")
    List<BaseballPlayEvent> findBattingEventsByPlayer(@Param("playerId") Long playerId);

    @Query("SELECT e FROM BaseballPlayEvent e " +
           "JOIN FETCH e.match m " +
           "LEFT JOIN FETCH m.tournament " +
           "WHERE e.pitcherPlayerMatch IS NOT NULL AND e.pitcherPlayerMatch.player.id = :playerId")
    List<BaseballPlayEvent> findPitchingEventsByPlayer(@Param("playerId") Long playerId);

    // ── Stats aggregation queries ─────────────────────────────────────────────

    // JOIN FETCH e.match so getPlayerStatsList can call e.getMatch().getId() without N+1.
    @Query("SELECT e FROM BaseballPlayEvent e " +
           "JOIN FETCH e.match m " +
           "JOIN FETCH e.batterPlayerMatch pm " +
           "JOIN FETCH pm.player p " +
           "LEFT JOIN FETCH p.team " +
           "WHERE m.sportType = :sportType AND e.batterPlayerMatch IS NOT NULL")
    List<BaseballPlayEvent> findAllBattingEventsBySportType(@Param("sportType") SportType sportType);

    @Query("SELECT e FROM BaseballPlayEvent e " +
           "JOIN FETCH e.match m " +
           "JOIN FETCH e.batterPlayerMatch pm " +
           "JOIN FETCH pm.player p " +
           "LEFT JOIN FETCH p.team " +
           "WHERE m.sportType = :sportType AND m.tournament.id = :tournamentId AND e.batterPlayerMatch IS NOT NULL")
    List<BaseballPlayEvent> findAllBattingEventsBySportTypeAndTournament(@Param("sportType") SportType sportType,
                                                                          @Param("tournamentId") Long tournamentId);

    // Team-scoped batting queries: filter at SQL level instead of post-load in Java.
    @Query("SELECT e FROM BaseballPlayEvent e " +
           "JOIN FETCH e.match m " +
           "JOIN FETCH e.batterPlayerMatch pm " +
           "JOIN FETCH pm.player p " +
           "LEFT JOIN FETCH p.team t " +
           "WHERE m.sportType = :sportType AND e.batterPlayerMatch IS NOT NULL AND t.id = :teamId")
    List<BaseballPlayEvent> findAllBattingEventsBySportTypeAndTeam(@Param("sportType") SportType sportType,
                                                                    @Param("teamId") Long teamId);

    @Query("SELECT e FROM BaseballPlayEvent e " +
           "JOIN FETCH e.match m " +
           "JOIN FETCH e.batterPlayerMatch pm " +
           "JOIN FETCH pm.player p " +
           "LEFT JOIN FETCH p.team t " +
           "WHERE m.sportType = :sportType AND m.tournament.id = :tournamentId " +
           "AND e.batterPlayerMatch IS NOT NULL AND t.id = :teamId")
    List<BaseballPlayEvent> findAllBattingEventsBySportTypeAndTournamentAndTeam(@Param("sportType") SportType sportType,
                                                                                 @Param("tournamentId") Long tournamentId,
                                                                                 @Param("teamId") Long teamId);

    // JOIN FETCH pitcherPlayerMatch, player, team so loading the pitching side of
    // getPlayerStatsList doesn't degrade into N+1 SELECT per event.
    @Query("SELECT e FROM BaseballPlayEvent e " +
           "JOIN FETCH e.pitcherPlayerMatch pm " +
           "JOIN FETCH pm.player p " +
           "LEFT JOIN FETCH p.team " +
           "WHERE e.match.sportType = :sportType AND e.pitcherPlayerMatch IS NOT NULL " +
           "AND p.id IN :playerIds")
    List<BaseballPlayEvent> findAllPitchingEventsBySportTypeAndPlayerIds(@Param("sportType") SportType sportType,
                                                                          @Param("playerIds") List<Long> playerIds);

    @Query("SELECT e FROM BaseballPlayEvent e " +
           "JOIN FETCH e.pitcherPlayerMatch pm " +
           "JOIN FETCH pm.player p " +
           "LEFT JOIN FETCH p.team " +
           "WHERE e.match.sportType = :sportType AND e.match.tournament.id = :tournamentId " +
           "AND e.pitcherPlayerMatch IS NOT NULL AND p.id IN :playerIds")
    List<BaseballPlayEvent> findAllPitchingEventsBySportTypeAndTournamentAndPlayerIds(@Param("sportType") SportType sportType,
                                                                                       @Param("tournamentId") Long tournamentId,
                                                                                       @Param("playerIds") List<Long> playerIds);

    @Query(value = "SELECT ROUND(pg_total_relation_size('baseball_play_event') / 1024.0 / 1024.0, 2)", nativeQuery = true)
    Double getTableSizeMB();
}

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

    @Query("SELECT e FROM BaseballPlayEvent e WHERE e.batterPlayerMatch IS NOT NULL AND e.batterPlayerMatch.player.id = :playerId")
    List<BaseballPlayEvent> findBattingEventsByPlayer(@Param("playerId") Long playerId);

    @Query("SELECT e FROM BaseballPlayEvent e WHERE e.pitcherPlayerMatch IS NOT NULL AND e.pitcherPlayerMatch.player.id = :playerId")
    List<BaseballPlayEvent> findPitchingEventsByPlayer(@Param("playerId") Long playerId);

    // ── Stats aggregation queries ─────────────────────────────────────────────

    @Query("SELECT e FROM BaseballPlayEvent e " +
           "JOIN FETCH e.batterPlayerMatch pm " +
           "JOIN FETCH pm.player p " +
           "LEFT JOIN FETCH p.team " +
           "WHERE e.match.sportType = :sportType AND e.batterPlayerMatch IS NOT NULL")
    List<BaseballPlayEvent> findAllBattingEventsBySportType(@Param("sportType") SportType sportType);

    @Query("SELECT e FROM BaseballPlayEvent e " +
           "JOIN FETCH e.batterPlayerMatch pm " +
           "JOIN FETCH pm.player p " +
           "LEFT JOIN FETCH p.team " +
           "WHERE e.match.sportType = :sportType AND e.match.tournament.id = :tournamentId AND e.batterPlayerMatch IS NOT NULL")
    List<BaseballPlayEvent> findAllBattingEventsBySportTypeAndTournament(@Param("sportType") SportType sportType,
                                                                          @Param("tournamentId") Long tournamentId);

    @Query("SELECT e FROM BaseballPlayEvent e " +
           "WHERE e.match.sportType = :sportType AND e.pitcherPlayerMatch IS NOT NULL " +
           "AND e.pitcherPlayerMatch.player.id IN :playerIds")
    List<BaseballPlayEvent> findAllPitchingEventsBySportTypeAndPlayerIds(@Param("sportType") SportType sportType,
                                                                          @Param("playerIds") List<Long> playerIds);

    @Query("SELECT e FROM BaseballPlayEvent e " +
           "WHERE e.match.sportType = :sportType AND e.match.tournament.id = :tournamentId " +
           "AND e.pitcherPlayerMatch IS NOT NULL AND e.pitcherPlayerMatch.player.id IN :playerIds")
    List<BaseballPlayEvent> findAllPitchingEventsBySportTypeAndTournamentAndPlayerIds(@Param("sportType") SportType sportType,
                                                                                       @Param("tournamentId") Long tournamentId,
                                                                                       @Param("playerIds") List<Long> playerIds);
}

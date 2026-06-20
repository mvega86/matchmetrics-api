package com.matchmetrics.persistence.repository;

import com.matchmetrics.domain.enums.MatchState;
import com.matchmetrics.domain.enums.SportType;
import com.matchmetrics.persistence.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findAllByOrderByStartFirstTimeAsc();

    List<Match> findByHomeTeamIdOrAwayTeamIdOrderByStartFirstTimeAsc(Long homeTeamId, Long awayTeamId);

    List<Match> findByTournamentIdOrderByStartFirstTimeAsc(Long tournamentId);

    List<Match> findByTournamentIsNullOrderByStartFirstTimeAsc();

    List<Match> findBySportTypeOrderByStartFirstTimeAsc(SportType sportType);

    // ── Stats aggregation queries ─────────────────────────────────────────────

    @Query("SELECT m FROM Match m JOIN FETCH m.homeTeam JOIN FETCH m.awayTeam " +
           "WHERE m.sportType = :sportType AND m.state = :state")
    List<Match> findBySportTypeAndState(@Param("sportType") SportType sportType,
                                         @Param("state") MatchState state);

    @Query("SELECT m FROM Match m JOIN FETCH m.homeTeam JOIN FETCH m.awayTeam " +
           "WHERE m.sportType = :sportType AND m.state = :state AND m.tournament.id = :tournamentId")
    List<Match> findBySportTypeAndStateAndTournamentId(@Param("sportType") SportType sportType,
                                                        @Param("state") MatchState state,
                                                        @Param("tournamentId") Long tournamentId);
}
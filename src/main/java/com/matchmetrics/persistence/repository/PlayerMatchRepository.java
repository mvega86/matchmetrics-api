package com.matchmetrics.persistence.repository;

import com.matchmetrics.persistence.entity.PlayerMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerMatchRepository extends JpaRepository<PlayerMatch, Long> {

    List<PlayerMatch> findByMatchId(Long matchId);

    List<PlayerMatch> findByPlayerId(Long playerId);

    Optional<PlayerMatch> findByMatchIdAndPlayerId(Long matchId, Long playerId);

    List<PlayerMatch> findByPlayerTeamId(Long teamId);

    List<PlayerMatch> findByMatchIdAndPlayerTeamId(Long matchId, Long teamId);
}


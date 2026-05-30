package com.matchmetrics.persistence.repository;

import com.matchmetrics.persistence.entity.PlayerMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerMatchRepository extends JpaRepository<PlayerMatch, Long> {
    List<PlayerMatch> findByMatchId(Long matchId); // Getting players from a match
    List<PlayerMatch> findByPlayerId(Long playerId); // Get matches from a player
    Optional<PlayerMatch> findByMatchIdAndPlayerId(Long matchId, Long playerId);
}


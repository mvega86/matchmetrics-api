package com.matchmetrics.persistence.repository;

import com.matchmetrics.persistence.entity.BaseballGameState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BaseballGameStateRepository extends JpaRepository<BaseballGameState, Long> {
    Optional<BaseballGameState> findByMatchId(Long matchId);
    boolean existsByMatchId(Long matchId);
}

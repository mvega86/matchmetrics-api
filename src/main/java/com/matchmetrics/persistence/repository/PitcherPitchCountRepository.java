package com.matchmetrics.persistence.repository;

import com.matchmetrics.persistence.entity.PitcherPitchCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PitcherPitchCountRepository extends JpaRepository<PitcherPitchCount, Long> {

    List<PitcherPitchCount> findByGameStateId(Long gameStateId);

    @Query("SELECT p FROM PitcherPitchCount p WHERE p.gameState.id = :gsId AND p.pitcherPlayerMatch.id = :pmId")
    Optional<PitcherPitchCount> findByGameStateIdAndPitcherPMId(
            @Param("gsId") Long gameStateId,
            @Param("pmId") Long pitcherPlayerMatchId
    );
}

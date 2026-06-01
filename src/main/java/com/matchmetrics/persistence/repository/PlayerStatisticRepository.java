package com.matchmetrics.persistence.repository;

import com.matchmetrics.persistence.entity.PlayerStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerStatisticRepository extends JpaRepository<PlayerStatistic, Long> {

    List<PlayerStatistic> findByPlayerMatchId(Long playerMatchId);

    List<PlayerStatistic> findByPlayerMatch_Match_IdOrderByCreatedAtDesc(Long matchId);

    List<PlayerStatistic> findByPlayerMatchPlayerTeamIdOrderByCreatedAtDesc(Long teamId);

    List<PlayerStatistic> findByPlayerMatchMatchIdAndPlayerMatchPlayerTeamIdOrderByCreatedAtDesc(
            Long matchId,
            Long teamId
    );
}


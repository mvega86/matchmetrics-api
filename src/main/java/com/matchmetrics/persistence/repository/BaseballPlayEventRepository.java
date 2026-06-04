package com.matchmetrics.persistence.repository;

import com.matchmetrics.persistence.entity.BaseballPlayEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaseballPlayEventRepository extends JpaRepository<BaseballPlayEvent, Long> {
    List<BaseballPlayEvent> findAllByMatchIdOrderByCreatedAtAsc(Long matchId);
    List<BaseballPlayEvent> findByBattingTeamIdOrFieldingTeamIdOrderByCreatedAtAsc(Long battingTeamId, Long fieldingTeamId);
}

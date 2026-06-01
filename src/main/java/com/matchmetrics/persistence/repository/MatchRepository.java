package com.matchmetrics.persistence.repository;

import com.matchmetrics.persistence.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findAllByOrderByStartFirstTimeAsc();

    List<Match> findByHomeTeamIdOrAwayTeamIdOrderByStartFirstTimeAsc(Long homeTeamId, Long awayTeamId);
}
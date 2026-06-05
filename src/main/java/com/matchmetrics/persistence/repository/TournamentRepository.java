package com.matchmetrics.persistence.repository;

import com.matchmetrics.domain.enums.SportType;
import com.matchmetrics.domain.enums.TournamentStatus;
import com.matchmetrics.persistence.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    List<Tournament> findAllByOrderByStartDateDesc();
    List<Tournament> findBySportTypeOrderByStartDateDesc(SportType sportType);
    List<Tournament> findByStatusOrderByStartDateDesc(TournamentStatus status);
    List<Tournament> findByNameContainingIgnoreCaseOrderByStartDateDesc(String name);
}

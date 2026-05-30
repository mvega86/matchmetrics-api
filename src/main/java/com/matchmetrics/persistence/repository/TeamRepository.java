package com.matchmetrics.persistence.repository;

import com.matchmetrics.persistence.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// =========================
// REPOSITORIO TeamRepository
// =========================
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
}

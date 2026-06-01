package com.matchmetrics.persistence.repository;

import com.matchmetrics.persistence.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// =========================
// REPOSITORIO TeamRepository
// =========================
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByNameIgnoreCase(String name);

    List<Team> findByNameContainingIgnoreCase(String name);
}

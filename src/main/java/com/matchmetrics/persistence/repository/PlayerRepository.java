package com.matchmetrics.persistence.repository;

import com.matchmetrics.domain.enums.SportType;
import com.matchmetrics.persistence.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findAllByOrderByUpdatedAtDesc();
    List<Player> findByTeamIdOrderByFullNameAsc(Long teamId);

    @Query("SELECT DISTINCT p FROM Player p JOIN p.teams t WHERE t.sportType = :sportType ORDER BY p.fullName ASC")
    List<Player> findByTeamsSportTypeOrderByFullNameAsc(@Param("sportType") SportType sportType);
}

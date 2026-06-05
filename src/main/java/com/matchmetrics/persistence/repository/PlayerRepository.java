package com.matchmetrics.persistence.repository;

import com.matchmetrics.persistence.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findAllByOrderByUpdatedAtDesc();
    List<Player> findByTeamIdOrderByFullNameAsc(Long teamId);
    List<Player> findByTeam_SportTypeOrderByFullNameAsc(com.matchmetrics.domain.enums.SportType sportType);
}

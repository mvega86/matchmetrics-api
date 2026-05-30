package com.matchmetrics.persistence.repository;

import com.matchmetrics.persistence.entity.Statistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatisticRepository extends JpaRepository<Statistic, Long> {
    boolean existsByName(String name);
}


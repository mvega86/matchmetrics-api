// StatisticLocationRepository.java
package com.matchmetrics.persistence.repository;

import com.matchmetrics.persistence.entity.StatisticLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatisticLocationRepository extends JpaRepository<StatisticLocation, Long> {
}

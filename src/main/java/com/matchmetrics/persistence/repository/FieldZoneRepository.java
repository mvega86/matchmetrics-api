package com.matchmetrics.persistence.repository;

import com.matchmetrics.persistence.entity.FieldZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FieldZoneRepository extends JpaRepository<FieldZone, Long> {

    @Query("SELECT fz FROM FieldZone fz WHERE " +
            ":x BETWEEN fz.minX AND fz.maxX AND " +
            ":y BETWEEN fz.minY AND fz.maxY")
    FieldZone findByPosition(@Param("x") Double x, @Param("y") Double y);
}

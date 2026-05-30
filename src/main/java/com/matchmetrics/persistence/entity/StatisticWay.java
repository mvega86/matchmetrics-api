package com.matchmetrics.persistence.entity;

import com.matchmetrics.persistence.audit.AuditModel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "statistic_way")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatisticWay extends AuditModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}

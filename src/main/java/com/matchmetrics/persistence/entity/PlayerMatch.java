package com.matchmetrics.persistence.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.matchmetrics.persistence.audit.AuditModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerMatch extends AuditModel {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "match_id", nullable = false)
        @JsonBackReference
        private Match match;

        @ManyToOne
        @JoinColumn(name = "player_id", nullable = false)
        @JsonIgnore
        private Player player;

        @Column(name = "in_")
        private LocalDateTime inTime; // Exact date and time of entry
        @Column(name = "out")
        private LocalDateTime outTime; // Exact date and time of departure

}


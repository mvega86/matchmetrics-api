package com.matchmetrics.persistence.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.matchmetrics.persistence.audit.AuditModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
public class Player extends AuditModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "jersey_name")
    private String jerseyName;

    @Column(name = "jersey_number")
    private Integer jerseyNumber;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Formula("(EXTRACT(YEAR FROM AGE(CURRENT_DATE, birth_date)))")
    private Integer age;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = true)
    @JsonBackReference
    private Team team;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "player_teams",
        joinColumns = @JoinColumn(name = "player_id"),
        inverseJoinColumns = @JoinColumn(name = "team_id")
    )
    private Set<Team> teams = new HashSet<>();

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "field_position", length = 5)
    private String fieldPosition;

}

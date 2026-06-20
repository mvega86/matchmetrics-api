package com.matchmetrics.mapper.dto;

import com.matchmetrics.domain.enums.SportType;
import com.matchmetrics.domain.enums.TournamentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TournamentDTO {

    private Long id;

    @NotBlank(message = "Tournament name is required.")
    private String name;

    private String description;

    private String logoUrl;

    @NotNull(message = "Sport type is required.")
    private SportType sportType;

    private TournamentStatus status;

    private LocalDate startDate;

    private LocalDate endDate;

    private String organizer;

    private String location;

    private String country;

    private String category;

    private Integer matchCount;

    private List<TeamDTO> teams;
}

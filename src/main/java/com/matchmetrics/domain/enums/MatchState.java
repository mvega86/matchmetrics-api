package com.matchmetrics.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MatchState {
    PENDING("Pending"),
    SUSPENDED("Suspended"),
    STARTED("Started"),
    FINISHED("Finished");

    private final String description;
}

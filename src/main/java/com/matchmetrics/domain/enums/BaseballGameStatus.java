package com.matchmetrics.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BaseballGameStatus {
    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    SUSPENDED("Suspended"),
    FINISHED("Finished");

    private final String description;
}

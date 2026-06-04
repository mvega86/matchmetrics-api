package com.matchmetrics.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SportType {
    FOOTBALL("Football"),
    BASEBALL("Baseball"),
    SOFTBALL("Softball");

    private final String description;
}

package com.matchmetrics.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InningHalf {
    TOP("Top"),
    BOTTOM("Bottom");

    private final String description;
}

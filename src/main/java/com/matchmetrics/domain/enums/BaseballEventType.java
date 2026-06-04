package com.matchmetrics.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BaseballEventType {
    PLATE_APPEARANCE("Plate Appearance"),
    SINGLE("Single"),
    DOUBLE("Double"),
    TRIPLE("Triple"),
    HOME_RUN("Home Run"),
    WALK("Walk"),
    STRIKEOUT("Strikeout"),
    OUT("Out"),
    ERROR("Error"),
    HIT_BY_PITCH("Hit By Pitch"),
    SACRIFICE_FLY("Sacrifice Fly"),
    SACRIFICE_BUNT("Sacrifice Bunt"),
    STOLEN_BASE("Stolen Base"),
    CAUGHT_STEALING("Caught Stealing"),
    DOUBLE_PLAY("Double Play"),
    TRIPLE_PLAY("Triple Play"),
    RUN_SCORED("Run Scored"),
    PITCHING_CHANGE("Pitching Change"),
    DEFENSIVE_CHANGE("Defensive Change"),
    COMMENT("Comment");

    private final String description;
}

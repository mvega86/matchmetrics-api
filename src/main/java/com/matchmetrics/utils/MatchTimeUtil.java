package com.matchmetrics.utils;

import com.matchmetrics.persistence.entity.Match;

import java.time.Duration;
import java.time.LocalDateTime;

public class MatchTimeUtil {

    public static String calculateRelativeMinuteFormatted(Match match, LocalDateTime eventTime) {
        if (match == null || eventTime == null) {
            return null;
        }

        // **PRIMER TIEMPO**
        if (match.getStartFirstTime() != null && eventTime.isAfter(match.getStartFirstTime()) && match.getEndFirstTime()==null) {
            int minute = (int) Duration.between(match.getStartFirstTime(), eventTime).toMinutes();
            return (minute >= 45) ? "45+" + (minute - 45) : String.valueOf(minute);
        }

        // **DESCANSO PRIMER TIEMPO**
        if (match.getEndFirstTime() != null && eventTime.isAfter(match.getEndFirstTime()) && match.getStartSecondTime()==null) {
            return null; // Evento en el descanso → Inválido
        }

        // **SEGUNDO TIEMPO**
        if (match.getStartSecondTime() != null && eventTime.isAfter(match.getStartSecondTime()) && match.getEndSecondTime()==null) {
            int minute = (int) Duration.between(match.getStartSecondTime(), eventTime).toMinutes() + 45;
            return (minute >= 90) ? "90+" + (minute - 90) : String.valueOf(minute);
        }

        // **DESCANSO SEGUNDO TIEMPO**
        if (match.getEndSecondTime() != null && eventTime.isAfter(match.getEndSecondTime()) && match.getStartFirstExtraTime()==null) {
            return null; // Evento en el descanso → Inválido
        }

        // **PRIMER TIEMPO EXTRA**
        if (match.getStartFirstExtraTime() != null && eventTime.isAfter(match.getStartFirstExtraTime()) && match.getEndFirstExtraTime()==null) {
            int minute = (int) Duration.between(match.getStartFirstExtraTime(), eventTime).toMinutes() + 90;
            return (minute >= 105) ? "105+" + (minute - 105) : String.valueOf(minute);
        }

        // **DESCANSO PRIMER TIEMPO EXTRA**
        if (match.getStartSecondExtraTime() != null && eventTime.isAfter(match.getEndFirstExtraTime()) && match.getStartSecondExtraTime()==null) {
            return null; // Evento en el descanso → Inválido
        }

        // **SEGUNDO TIEMPO EXTRA**
        if (match.getStartSecondExtraTime() != null && eventTime.isAfter(match.getStartSecondExtraTime()) && match.getEndSecondExtraTime()==null) {
            int minute = (int) Duration.between(match.getStartSecondExtraTime(), eventTime).toMinutes() + 105;
            return (minute >= 120) ? "120+" + (minute - 120) : String.valueOf(minute);
        }

        return null; // Evento fuera del tiempo válido
    }
}



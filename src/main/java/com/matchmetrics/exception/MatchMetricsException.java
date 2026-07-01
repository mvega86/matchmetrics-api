package com.matchmetrics.exception;

public abstract class MatchMetricsException extends RuntimeException {
    protected MatchMetricsException(String message) {
        super(message);
    }
}

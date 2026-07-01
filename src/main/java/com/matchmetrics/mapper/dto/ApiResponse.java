package com.matchmetrics.mapper.dto;

/**
 * Typed wrapper for all controller success responses.
 * Serializes to {"message":"...","data":{...}} matching the existing frontend contract.
 */
public record ApiResponse<T>(String message, T data) {

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(message, data);
    }
}

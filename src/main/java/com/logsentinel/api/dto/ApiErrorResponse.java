package com.logsentinel.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Standard envelope for all API error responses.
 * Provides consistent structure regardless of exception type.
 */
@Getter
@Builder
public class ApiErrorResponse {

    private int status;
    private String error;
    private String message;
    private Instant timestamp;

    /** Field-level validation errors, populated only for constraint violations. */
    private Map<String, String> fieldErrors;
}

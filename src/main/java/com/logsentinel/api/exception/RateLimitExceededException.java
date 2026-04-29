package com.logsentinel.api.exception;

/**
 * Thrown when a client exceeds the configured rate limit for log ingestion.
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String sourceSystem) {
        super(String.format("Rate limit exceeded for source system: %s. Please slow down.", sourceSystem));
    }
}

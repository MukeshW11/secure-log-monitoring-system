package com.logsentinel.api.dto;

import com.logsentinel.api.entity.LogLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Outbound representation of a stored log entry.
 * Decoupled from the JPA entity to avoid over-exposing persistence internals.
 */
@Getter
@Builder
public class LogEntryResponse {

    private Long id;
    private LogLevel level;
    private String message;
    private String sourceSystem;
    private Instant timestamp;
    private Instant receivedAt;
    private String traceId;
}

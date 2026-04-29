package com.logsentinel.api.dto;

import com.logsentinel.api.entity.LogLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Inbound payload for submitting a new log entry.
 * Validated before being processed by the service layer.
 */
@Getter
@Setter
public class LogEntryRequest {

    @NotNull(message = "Log level is required")
    private LogLevel level;

    @NotBlank(message = "Message must not be blank")
    @Size(min = 3, max = 2048, message = "Message must be between 3 and 2048 characters")
    private String message;

    @NotBlank(message = "Source system identifier is required")
    @Size(max = 128, message = "Source system name must not exceed 128 characters")
    private String sourceSystem;

    /**
     * Optional client-provided timestamp. If absent, the server assigns the ingestion time.
     * Expected format: ISO-8601 (e.g. 2024-06-15T10:30:00Z)
     */
    private Instant timestamp;

    /** Optional trace/correlation ID for distributed tracing support. */
    @Size(max = 64, message = "Trace ID must not exceed 64 characters")
    private String traceId;
}

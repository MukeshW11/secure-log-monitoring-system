package com.logsentinel.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents a single log entry submitted by a source system.
 * Stored persistently and used by the alert engine for anomaly detection.
 */
@Entity
@Table(name = "log_entries", indexes = {
        @Index(name = "idx_log_level", columnList = "level"),
        @Index(name = "idx_log_source", columnList = "sourceSystem"),
        @Index(name = "idx_log_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Severity of the log event. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LogLevel level;

    /** Human-readable description of the event. */
    @Column(nullable = false, length = 2048)
    private String message;

    /**
     * Identifier of the system that produced this log.
     * Examples: "auth-service", "payment-gateway", "api-gateway"
     */
    @Column(nullable = false, length = 128)
    private String sourceSystem;

    /** When the event actually occurred (set by the client or defaulted to now). */
    @Column(nullable = false)
    private Instant timestamp;

    /** When LogSentinel received this entry. Used for ingestion rate tracking. */
    @Column(nullable = false, updatable = false)
    private Instant receivedAt;

    /** Optional: trace/correlation ID forwarded from the originating system. */
    @Column(length = 64)
    private String traceId;

    @PrePersist
    private void setReceivedAt() {
        this.receivedAt = Instant.now();
        if (this.timestamp == null) {
            this.timestamp = this.receivedAt;
        }
    }
}

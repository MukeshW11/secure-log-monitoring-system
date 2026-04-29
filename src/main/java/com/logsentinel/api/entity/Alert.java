package com.logsentinel.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * An alert is raised by the AlertEngine when a source system crosses
 * the configured error threshold within the observation window.
 */
@Entity
@Table(name = "alerts", indexes = {
        @Index(name = "idx_alert_status", columnList = "status"),
        @Index(name = "idx_alert_source", columnList = "sourceSystem")
})
@Getter
@Setter
@NoArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Source system that triggered this alert. */
    @Column(nullable = false, length = 128)
    private String sourceSystem;

    /** Short description of why the alert was raised. */
    @Column(nullable = false, length = 512)
    private String reason;

    /** Number of high-severity events that triggered this alert. */
    @Column(nullable = false)
    private int triggerCount;

    /** Timestamp of the oldest event that contributed to this alert. */
    @Column(nullable = false)
    private Instant windowStart;

    /** Timestamp of the most recent event that contributed to this alert. */
    @Column(nullable = false)
    private Instant windowEnd;

    /** When the alert was raised. */
    @Column(nullable = false, updatable = false)
    private Instant raisedAt;

    /** Current lifecycle state of the alert. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status;

    /** Optional notes added during acknowledgement or resolution. */
    @Column(length = 1024)
    private String operatorNotes;

    @PrePersist
    private void initDefaults() {
        this.raisedAt = Instant.now();
        if (this.status == null) {
            this.status = AlertStatus.ACTIVE;
        }
    }
}

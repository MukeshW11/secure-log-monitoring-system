package com.logsentinel.api.dto;

import com.logsentinel.api.entity.AlertStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Outbound representation of an alert.
 */
@Getter
@Builder
public class AlertResponse {

    private Long id;
    private String sourceSystem;
    private String reason;
    private int triggerCount;
    private Instant windowStart;
    private Instant windowEnd;
    private Instant raisedAt;
    private AlertStatus status;
    private String operatorNotes;
}

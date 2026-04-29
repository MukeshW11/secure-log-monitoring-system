package com.logsentinel.api.dto;

import com.logsentinel.api.entity.AlertStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Payload for updating the status of an existing alert (e.g., acknowledging or resolving it).
 */
@Getter
@Setter
public class AlertStatusUpdateRequest {

    @NotNull(message = "Target status is required")
    private AlertStatus status;

    @Size(max = 1024, message = "Operator notes must not exceed 1024 characters")
    private String operatorNotes;
}

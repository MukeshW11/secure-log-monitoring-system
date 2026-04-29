package com.logsentinel.api.service;

import com.logsentinel.api.dto.AlertResponse;
import com.logsentinel.api.dto.AlertStatusUpdateRequest;
import com.logsentinel.api.entity.Alert;
import com.logsentinel.api.entity.AlertStatus;
import com.logsentinel.api.exception.ResourceNotFoundException;
import com.logsentinel.api.repository.AlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Manages alert retrieval and lifecycle transitions (acknowledge, resolve).
 */
@Service
@Transactional(readOnly = true)
public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    public List<AlertResponse> findAll() {
        return alertRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AlertResponse> findActiveAlerts() {
        return alertRepository.findByStatus(AlertStatus.ACTIVE).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AlertResponse> findBySourceSystem(String sourceSystem) {
        return alertRepository.findBySourceSystem(sourceSystem).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AlertResponse findById(Long id) {
        return alertRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", id));
    }

    /**
     * Updates the lifecycle status of an alert (e.g., ACTIVE → ACKNOWLEDGED → RESOLVED).
     * Operator notes are persisted alongside the status change for audit purposes.
     */
    @Transactional
    public AlertResponse updateStatus(Long id, AlertStatusUpdateRequest request) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", id));

        alert.setStatus(request.getStatus());
        if (request.getOperatorNotes() != null && !request.getOperatorNotes().isBlank()) {
            alert.setOperatorNotes(request.getOperatorNotes());
        }

        return mapToResponse(alertRepository.save(alert));
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private AlertResponse mapToResponse(Alert entity) {
        return AlertResponse.builder()
                .id(entity.getId())
                .sourceSystem(entity.getSourceSystem())
                .reason(entity.getReason())
                .triggerCount(entity.getTriggerCount())
                .windowStart(entity.getWindowStart())
                .windowEnd(entity.getWindowEnd())
                .raisedAt(entity.getRaisedAt())
                .status(entity.getStatus())
                .operatorNotes(entity.getOperatorNotes())
                .build();
    }
}

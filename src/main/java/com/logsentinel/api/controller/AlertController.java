package com.logsentinel.api.controller;

import com.logsentinel.api.dto.AlertResponse;
import com.logsentinel.api.dto.AlertStatusUpdateRequest;
import com.logsentinel.api.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for alert management.
 *
 * Base path: /api/v1/alerts
 */
@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * GET /api/v1/alerts
     * Returns all alerts regardless of status.
     */
    @GetMapping
    public ResponseEntity<List<AlertResponse>> getAllAlerts() {
        return ResponseEntity.ok(alertService.findAll());
    }

    /**
     * GET /api/v1/alerts/active
     * Returns only currently active (unacknowledged) alerts.
     * Useful for a real-time monitoring dashboard.
     */
    @GetMapping("/active")
    public ResponseEntity<List<AlertResponse>> getActiveAlerts() {
        return ResponseEntity.ok(alertService.findActiveAlerts());
    }

    /**
     * GET /api/v1/alerts/{id}
     * Returns a single alert's full details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AlertResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.findById(id));
    }

    /**
     * GET /api/v1/alerts/by-source/{sourceSystem}
     * Returns all alerts raised for a specific source system.
     * Useful for investigating a noisy or compromised service.
     */
    @GetMapping("/by-source/{sourceSystem}")
    public ResponseEntity<List<AlertResponse>> getBySourceSystem(@PathVariable String sourceSystem) {
        return ResponseEntity.ok(alertService.findBySourceSystem(sourceSystem));
    }

    /**
     * PATCH /api/v1/alerts/{id}/status
     * Acknowledge or resolve an alert.
     * Operators can attach notes explaining the resolution.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<AlertResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AlertStatusUpdateRequest request) {
        return ResponseEntity.ok(alertService.updateStatus(id, request));
    }
}

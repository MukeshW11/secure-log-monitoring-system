package com.logsentinel.api.engine;

import com.logsentinel.api.config.AlertEngineProperties;
import com.logsentinel.api.entity.Alert;
import com.logsentinel.api.entity.AlertStatus;
import com.logsentinel.api.repository.AlertRepository;
import com.logsentinel.api.repository.LogEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Periodically scans active source systems and raises alerts when error
 * rates exceed the configured threshold within the observation window.
 *
 * Design decisions:
 * - Runs on a fixed-delay schedule (not fixed-rate) to avoid overlapping executions.
 * - Cooldown check prevents alert storms from noisy systems.
 * - Each alert stores enough context to reconstruct what triggered it.
 */
@Component
public class AlertEngine {

    private static final Logger log = LoggerFactory.getLogger(AlertEngine.class);

    private final LogEntryRepository logEntryRepository;
    private final AlertRepository alertRepository;
    private final AlertEngineProperties config;

    public AlertEngine(LogEntryRepository logEntryRepository,
                       AlertRepository alertRepository,
                       AlertEngineProperties config) {
        this.logEntryRepository = logEntryRepository;
        this.alertRepository = alertRepository;
        this.config = config;
    }

    /**
     * Main scan loop — runs every 30 seconds.
     * Evaluates all source systems that have been active in the last observation window.
     */
    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void scanForAlerts() {
        Instant windowStart = Instant.now().minus(config.getWindowSeconds(), ChronoUnit.SECONDS);

        List<String> activeSources = logEntryRepository.findActiveSourceSystemsSince(windowStart);
        if (activeSources.isEmpty()) {
            return;
        }

        log.debug("Alert scan running for {} active source(s)", activeSources.size());

        for (String source : activeSources) {
            evaluateSource(source, windowStart);
        }
    }

    private void evaluateSource(String sourceSystem, Instant windowStart) {
        long errorCount = logEntryRepository.countRecentHighSeverityEvents(sourceSystem, windowStart);

        if (errorCount < config.getErrorThreshold()) {
            return; // Within acceptable bounds
        }

        // Check if an alert was already raised for this source within the cooldown window
        Instant cooldownBoundary = Instant.now().minus(config.getCooldownSeconds(), ChronoUnit.SECONDS);
        boolean alreadyAlerted = alertRepository.existsActiveAlertWithinCooldown(sourceSystem, cooldownBoundary);

        if (alreadyAlerted) {
            log.debug("Suppressing duplicate alert for '{}' (within cooldown)", sourceSystem);
            return;
        }

        raiseAlert(sourceSystem, errorCount, windowStart);
    }

    private void raiseAlert(String sourceSystem, long errorCount, Instant windowStart) {
        Alert alert = new Alert();
        alert.setSourceSystem(sourceSystem);
        alert.setTriggerCount((int) errorCount);
        alert.setWindowStart(windowStart);
        alert.setWindowEnd(Instant.now());
        alert.setStatus(AlertStatus.ACTIVE);
        alert.setReason(String.format(
                "Detected %d ERROR/CRITICAL events from '%s' within a %d-second window (threshold: %d)",
                errorCount, sourceSystem, config.getWindowSeconds(), config.getErrorThreshold()
        ));

        alertRepository.save(alert);
        log.warn("ALERT raised for source '{}': {} high-severity events in {}s window",
                sourceSystem, errorCount, config.getWindowSeconds());
    }
}

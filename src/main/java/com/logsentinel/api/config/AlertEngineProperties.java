package com.logsentinel.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds alert engine tuning parameters from application.properties.
 * All thresholds are configurable without code changes.
 */
@Configuration
@ConfigurationProperties(prefix = "alert.engine")
public class AlertEngineProperties {

    /**
     * Number of ERROR or CRITICAL events within the time window
     * that triggers an alert for a source system.
     */
    private int errorThreshold = 5;

    /**
     * Duration of the sliding observation window (in seconds).
     * Events older than this are excluded from the count.
     */
    private int windowSeconds = 60;

    /**
     * After an alert is raised, this cooldown (in seconds) prevents
     * the same source from generating a new alert immediately.
     */
    private int cooldownSeconds = 300;

    public int getErrorThreshold() { return errorThreshold; }
    public void setErrorThreshold(int errorThreshold) { this.errorThreshold = errorThreshold; }

    public int getWindowSeconds() { return windowSeconds; }
    public void setWindowSeconds(int windowSeconds) { this.windowSeconds = windowSeconds; }

    public int getCooldownSeconds() { return cooldownSeconds; }
    public void setCooldownSeconds(int cooldownSeconds) { this.cooldownSeconds = cooldownSeconds; }
}

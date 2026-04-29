package com.logsentinel.api.entity;

/**
 * Severity levels for log entries.
 * Ordered by increasing criticality — used in alert threshold checks.
 */
public enum LogLevel {
    INFO,
    WARN,
    ERROR,
    CRITICAL
}

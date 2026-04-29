package com.logsentinel.api.entity;

/**
 * Represents the current lifecycle state of an alert.
 */
public enum AlertStatus {
    /** Alert has been raised and is not yet acknowledged. */
    ACTIVE,

    /** Alert has been acknowledged by an operator. */
    ACKNOWLEDGED,

    /** Alert conditions are no longer met; system auto-resolved. */
    RESOLVED
}

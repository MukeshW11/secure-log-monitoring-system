package com.logsentinel.api.exception;

/**
 * Thrown when a submitted log entry fails semantic validation
 * beyond what Bean Validation constraints already cover.
 *
 * Example: a future timestamp supplied for a log that claims to be historical.
 */
public class InvalidLogException extends RuntimeException {

    public InvalidLogException(String message) {
        super(message);
    }

    public InvalidLogException(String message, Throwable cause) {
        super(message, cause);
    }
}

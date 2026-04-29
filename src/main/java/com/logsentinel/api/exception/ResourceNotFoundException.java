package com.logsentinel.api.exception;

/**
 * Thrown when a requested resource (log entry, alert, etc.) does not exist in the system.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceType, Long id) {
        super(String.format("%s with id %d was not found", resourceType, id));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

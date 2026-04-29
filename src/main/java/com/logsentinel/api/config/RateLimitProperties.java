package com.logsentinel.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds rate limiting configuration from application.properties.
 */
@Configuration
@ConfigurationProperties(prefix = "rate.limit")
public class RateLimitProperties {

    /** Maximum number of log ingestion requests allowed per source system per minute. */
    private int requestsPerMinute = 100;

    public int getRequestsPerMinute() { return requestsPerMinute; }
    public void setRequestsPerMinute(int requestsPerMinute) { this.requestsPerMinute = requestsPerMinute; }
}

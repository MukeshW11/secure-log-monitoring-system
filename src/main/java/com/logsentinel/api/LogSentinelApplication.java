package com.logsentinel.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for LogSentinel — a lightweight SIEM-style backend
 * that ingests application logs, detects anomalies, and raises alerts.
 */
@SpringBootApplication
@EnableScheduling
public class LogSentinelApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogSentinelApplication.class, args);
    }
}

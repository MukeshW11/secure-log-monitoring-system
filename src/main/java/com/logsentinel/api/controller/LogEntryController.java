package com.logsentinel.api.controller;

import com.logsentinel.api.dto.LogEntryRequest;
import com.logsentinel.api.dto.LogEntryResponse;
import com.logsentinel.api.entity.LogLevel;
import com.logsentinel.api.service.LogEntryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * REST controller for log ingestion and retrieval.
 *
 * Base path: /api/v1/logs
 */
@RestController
@RequestMapping("/api/v1/logs")
public class LogEntryController {

    private final LogEntryService logEntryService;

    public LogEntryController(LogEntryService logEntryService) {
        this.logEntryService = logEntryService;
    }

    /**
     * POST /api/v1/logs
     * Submit a new log entry from a source system.
     */
    @PostMapping
    public ResponseEntity<LogEntryResponse> ingest(@Valid @RequestBody LogEntryRequest request) {
        LogEntryResponse created = logEntryService.ingest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/v1/logs/{id}
     * Retrieve a specific log entry by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LogEntryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(logEntryService.findById(id));
    }

    /**
     * GET /api/v1/logs
     * List all logs with pagination. Default: 20 per page, sorted by timestamp descending.
     */
    @GetMapping
    public ResponseEntity<Page<LogEntryResponse>> listAll(
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(logEntryService.findAll(pageable));
    }

    /**
     * GET /api/v1/logs/search?level=ERROR&source=auth-service&from=...&to=...
     * Multi-criteria search endpoint. All query parameters are optional.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<LogEntryResponse>> search(
            @RequestParam(required = false) LogLevel level,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(logEntryService.search(level, source, from, to, pageable));
    }

    /**
     * GET /api/v1/logs/recent
     * Returns the last N log entries served from the in-memory cache.
     * Faster than hitting the DB for real-time dashboards.
     */
    @GetMapping("/recent")
    public ResponseEntity<List<LogEntryResponse>> getRecentLogs() {
        return ResponseEntity.ok(logEntryService.getRecentLogs());
    }

    /**
     * GET /api/v1/logs/by-level/{level}
     * Retrieve logs filtered by severity level.
     */
    @GetMapping("/by-level/{level}")
    public ResponseEntity<Page<LogEntryResponse>> getByLevel(
            @PathVariable LogLevel level,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(logEntryService.findByLevel(level, pageable));
    }

    /**
     * GET /api/v1/logs/by-source/{sourceSystem}
     * Retrieve logs filtered by the originating system name.
     */
    @GetMapping("/by-source/{sourceSystem}")
    public ResponseEntity<Page<LogEntryResponse>> getBySourceSystem(
            @PathVariable String sourceSystem,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(logEntryService.findBySourceSystem(sourceSystem, pageable));
    }

    /**
     * GET /api/v1/logs/by-time-range?from=...&to=...
     * Retrieve logs within a specific time window.
     */
    @GetMapping("/by-time-range")
    public ResponseEntity<Page<LogEntryResponse>> getByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(logEntryService.findByTimeRange(from, to, pageable));
    }
}

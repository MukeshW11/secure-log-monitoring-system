package com.logsentinel.api.service;

import com.logsentinel.api.dto.LogEntryRequest;
import com.logsentinel.api.dto.LogEntryResponse;
import com.logsentinel.api.engine.InMemoryRateLimiter;
import com.logsentinel.api.engine.RecentLogCache;
import com.logsentinel.api.entity.LogEntry;
import com.logsentinel.api.entity.LogLevel;
import com.logsentinel.api.exception.InvalidLogException;
import com.logsentinel.api.exception.ResourceNotFoundException;
import com.logsentinel.api.repository.LogEntryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Core service for log ingestion, retrieval, and filtering.
 *
 * Responsibilities:
 * - Validates business rules that go beyond annotations (e.g., no future timestamps)
 * - Enforces rate limits per source system
 * - Populates the in-memory recent log cache on every successful ingestion
 * - Maps between DTOs and entities
 */
@Service
@Transactional(readOnly = true)
public class LogEntryService {

    // Reject logs timestamped more than 24 hours in the future
    private static final long MAX_FUTURE_DRIFT_HOURS = 24;

    private final LogEntryRepository logEntryRepository;
    private final InMemoryRateLimiter rateLimiter;
    private final RecentLogCache recentLogCache;

    public LogEntryService(LogEntryRepository logEntryRepository,
                           InMemoryRateLimiter rateLimiter,
                           RecentLogCache recentLogCache) {
        this.logEntryRepository = logEntryRepository;
        this.rateLimiter = rateLimiter;
        this.recentLogCache = recentLogCache;
    }

    /**
     * Ingests a new log entry after rate-limit and timestamp validation.
     *
     * @return the persisted log entry as a response DTO
     */
    @Transactional
    public LogEntryResponse ingest(LogEntryRequest request) {
        rateLimiter.checkAndIncrement(request.getSourceSystem());
        validateTimestamp(request.getTimestamp());

        LogEntry entity = mapToEntity(request);
        LogEntry saved = logEntryRepository.save(entity);

        LogEntryResponse response = mapToResponse(saved);
        recentLogCache.put(response); // warm the cache

        return response;
    }

    /** Retrieves a single log entry by ID. */
    public LogEntryResponse findById(Long id) {
        return logEntryRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("LogEntry", id));
    }

    /** Paginated listing of all log entries. */
    public Page<LogEntryResponse> findAll(Pageable pageable) {
        return logEntryRepository.findAll(pageable).map(this::mapToResponse);
    }

    /** Paginated filtering by log level. */
    public Page<LogEntryResponse> findByLevel(LogLevel level, Pageable pageable) {
        return logEntryRepository.findByLevel(level, pageable).map(this::mapToResponse);
    }

    /** Paginated filtering by source system. */
    public Page<LogEntryResponse> findBySourceSystem(String sourceSystem, Pageable pageable) {
        return logEntryRepository.findBySourceSystem(sourceSystem, pageable).map(this::mapToResponse);
    }

    /** Paginated filtering by time range. */
    public Page<LogEntryResponse> findByTimeRange(Instant from, Instant to, Pageable pageable) {
        if (from.isAfter(to)) {
            throw new InvalidLogException("'from' timestamp must be before 'to' timestamp");
        }
        return logEntryRepository.findByTimestampBetween(from, to, pageable).map(this::mapToResponse);
    }

    /**
     * Multi-criteria search — any combination of filters may be null (treated as "no filter").
     */
    public Page<LogEntryResponse> search(LogLevel level, String sourceSystem,
                                         Instant from, Instant to, Pageable pageable) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidLogException("'from' timestamp must be before 'to' timestamp");
        }
        return logEntryRepository.findWithFilters(level, sourceSystem, from, to, pageable)
                .map(this::mapToResponse);
    }

    /** Returns the last N entries served directly from the in-memory cache. */
    public java.util.List<LogEntryResponse> getRecentLogs() {
        return recentLogCache.getAll();
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void validateTimestamp(Instant timestamp) {
        if (timestamp == null) return;

        Instant maxAllowed = Instant.now().plus(MAX_FUTURE_DRIFT_HOURS, ChronoUnit.HOURS);
        if (timestamp.isAfter(maxAllowed)) {
            throw new InvalidLogException(
                    "Log timestamp cannot be more than 24 hours in the future"
            );
        }
    }

    private LogEntry mapToEntity(LogEntryRequest request) {
        LogEntry entry = new LogEntry();
        entry.setLevel(request.getLevel());
        entry.setMessage(request.getMessage());
        entry.setSourceSystem(request.getSourceSystem());
        entry.setTimestamp(request.getTimestamp()); // null handled by @PrePersist
        entry.setTraceId(request.getTraceId());
        return entry;
    }

    LogEntryResponse mapToResponse(LogEntry entity) {
        return LogEntryResponse.builder()
                .id(entity.getId())
                .level(entity.getLevel())
                .message(entity.getMessage())
                .sourceSystem(entity.getSourceSystem())
                .timestamp(entity.getTimestamp())
                .receivedAt(entity.getReceivedAt())
                .traceId(entity.getTraceId())
                .build();
    }
}

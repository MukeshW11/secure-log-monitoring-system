package com.logsentinel.api.repository;

import com.logsentinel.api.entity.LogEntry;
import com.logsentinel.api.entity.LogLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {

    Page<LogEntry> findByLevel(LogLevel level, Pageable pageable);

    Page<LogEntry> findBySourceSystem(String sourceSystem, Pageable pageable);

    Page<LogEntry> findByTimestampBetween(Instant from, Instant to, Pageable pageable);

    /**
     * Counts high-severity events from a given source within a time window.
     * Used by the AlertEngine to decide whether to raise an alert.
     */
    @Query("""
            SELECT COUNT(l) FROM LogEntry l
            WHERE l.sourceSystem = :source
              AND l.level IN ('ERROR', 'CRITICAL')
              AND l.timestamp >= :windowStart
            """)
    long countRecentHighSeverityEvents(
            @Param("source") String sourceSystem,
            @Param("windowStart") Instant windowStart
    );

    /**
     * Fetches distinct source systems that have logged anything since the given time.
     * Drives the periodic alert scan across active sources.
     */
    @Query("SELECT DISTINCT l.sourceSystem FROM LogEntry l WHERE l.timestamp >= :since")
    List<String> findActiveSourceSystemsSince(@Param("since") Instant since);

    /** Composite filter: level + source + time range — all parameters are optional via JPQL. */
    @Query("""
            SELECT l FROM LogEntry l
            WHERE (:level IS NULL OR l.level = :level)
              AND (:source IS NULL OR l.sourceSystem = :source)
              AND (:from IS NULL OR l.timestamp >= :from)
              AND (:to IS NULL OR l.timestamp <= :to)
            ORDER BY l.timestamp DESC
            """)
    Page<LogEntry> findWithFilters(
            @Param("level") LogLevel level,
            @Param("source") String sourceSystem,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );
}

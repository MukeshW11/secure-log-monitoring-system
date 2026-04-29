package com.logsentinel.api.repository;

import com.logsentinel.api.entity.Alert;
import com.logsentinel.api.entity.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByStatus(AlertStatus status);

    List<Alert> findBySourceSystem(String sourceSystem);

    /**
     * Finds the most recent active alert for a given source system.
     * Used by the AlertEngine to check whether the cooldown window has elapsed
     * before raising a duplicate alert.
     */
    @Query("""
            SELECT a FROM Alert a
            WHERE a.sourceSystem = :source
              AND a.status = 'ACTIVE'
            ORDER BY a.raisedAt DESC
            LIMIT 1
            """)
    Optional<Alert> findLatestActiveAlertForSource(@Param("source") String sourceSystem);

    /**
     * Checks if an active alert was already raised for this source within the cooldown period.
     * Returns true if a recent alert exists — suppressing duplicate noise.
     */
    @Query("""
            SELECT COUNT(a) > 0 FROM Alert a
            WHERE a.sourceSystem = :source
              AND a.status = 'ACTIVE'
              AND a.raisedAt >= :cooldownBoundary
            """)
    boolean existsActiveAlertWithinCooldown(
            @Param("source") String sourceSystem,
            @Param("cooldownBoundary") Instant cooldownBoundary
    );
}

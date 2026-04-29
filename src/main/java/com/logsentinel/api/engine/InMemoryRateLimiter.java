package com.logsentinel.api.engine;

import com.logsentinel.api.config.RateLimitProperties;
import com.logsentinel.api.exception.RateLimitExceededException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory token-bucket-style rate limiter.
 *
 * Tracks request counts per source system within a rolling 1-minute window.
 * Counters are reset every minute via a scheduled task.
 *
 * NOTE: This is single-node only. In a distributed system, replace with
 * Redis-backed counters (e.g., via Spring Data Redis + Lua scripts).
 */
@Component
public class InMemoryRateLimiter {

    private final int maxRequestsPerMinute;

    // Concurrent map to safely handle simultaneous requests from different sources
    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();

    public InMemoryRateLimiter(RateLimitProperties props) {
        this.maxRequestsPerMinute = props.getRequestsPerMinute();
    }

    /**
     * Increments the request count for the given source and throws if the limit is exceeded.
     *
     * @param sourceSystem identifier of the calling system
     * @throws RateLimitExceededException if the source has exceeded its quota
     */
    public void checkAndIncrement(String sourceSystem) {
        AtomicInteger count = requestCounts.computeIfAbsent(sourceSystem, k -> new AtomicInteger(0));
        int current = count.incrementAndGet();

        if (current > maxRequestsPerMinute) {
            // Decrement to avoid permanently blocking after transient bursts
            count.decrementAndGet();
            throw new RateLimitExceededException(sourceSystem);
        }
    }

    /** Resets all counters every 60 seconds. Runs automatically on the Spring scheduler. */
    @Scheduled(fixedDelay = 60_000)
    public void resetCounters() {
        requestCounts.clear();
    }

    /** Returns current request count for a source — useful for metrics/debugging. */
    public int getCurrentCount(String sourceSystem) {
        AtomicInteger counter = requestCounts.get(sourceSystem);
        return (counter != null) ? counter.get() : 0;
    }
}

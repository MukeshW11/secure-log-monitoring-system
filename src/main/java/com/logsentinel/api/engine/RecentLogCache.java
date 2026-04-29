package com.logsentinel.api.engine;

import com.logsentinel.api.dto.LogEntryResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A lightweight LRU cache that holds the most recently ingested log entries in memory.
 *
 * This avoids hitting the database for simple "latest activity" queries.
 * Backed by a bounded LinkedHashMap (access-ordered) so the oldest entries
 * are evicted automatically when capacity is reached.
 *
 * Thread-safety: wrapped via Collections.synchronizedMap — sufficient for
 * moderate concurrency. For high-throughput scenarios, consider Caffeine or Guava Cache.
 */
@Component
public class RecentLogCache {

    private static final int MAX_ENTRIES = 200;

    private final Map<Long, LogEntryResponse> cache;

    public RecentLogCache() {
        // Access-ordered = true makes this behave as an LRU cache
        LinkedHashMap<Long, LogEntryResponse> lruMap = new LinkedHashMap<>(MAX_ENTRIES, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, LogEntryResponse> eldest) {
                return size() > MAX_ENTRIES;
            }
        };
        this.cache = Collections.synchronizedMap(lruMap);
    }

    public void put(LogEntryResponse entry) {
        cache.put(entry.getId(), entry);
    }

    /** Returns recent log entries, newest last. Size is bounded by MAX_ENTRIES. */
    public List<LogEntryResponse> getAll() {
        synchronized (cache) {
            return List.copyOf(cache.values());
        }
    }

    public int size() {
        return cache.size();
    }

    public void evict(Long id) {
        cache.remove(id);
    }
}

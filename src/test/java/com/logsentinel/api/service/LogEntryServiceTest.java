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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogEntryService Unit Tests")
class LogEntryServiceTest {

    @Mock private LogEntryRepository logEntryRepository;
    @Mock private InMemoryRateLimiter rateLimiter;
    @Mock private RecentLogCache recentLogCache;

    @InjectMocks
    private LogEntryService logEntryService;

    private LogEntryRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new LogEntryRequest();
        validRequest.setLevel(LogLevel.ERROR);
        validRequest.setMessage("NullPointerException in PaymentService");
        validRequest.setSourceSystem("payment-gateway");
        validRequest.setTimestamp(Instant.now().minusSeconds(30));
    }

    @Test
    @DisplayName("Should successfully ingest a valid log entry")
    void ingest_validRequest_returnsPersistedEntry() {
        LogEntry saved = new LogEntry();
        saved.setId(1L);
        saved.setLevel(validRequest.getLevel());
        saved.setMessage(validRequest.getMessage());
        saved.setSourceSystem(validRequest.getSourceSystem());
        saved.setTimestamp(validRequest.getTimestamp());

        when(logEntryRepository.save(any(LogEntry.class))).thenReturn(saved);

        LogEntryResponse response = logEntryService.ingest(validRequest);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getLevel()).isEqualTo(LogLevel.ERROR);
        assertThat(response.getSourceSystem()).isEqualTo("payment-gateway");

        verify(rateLimiter).checkAndIncrement("payment-gateway");
        verify(recentLogCache).put(any(LogEntryResponse.class));
    }

    @Test
    @DisplayName("Should reject log entries with a far-future timestamp")
    void ingest_futureDriftExceeded_throwsInvalidLogException() {
        validRequest.setTimestamp(Instant.now().plus(48, ChronoUnit.HOURS));

        assertThatThrownBy(() -> logEntryService.ingest(validRequest))
                .isInstanceOf(InvalidLogException.class)
                .hasMessageContaining("24 hours in the future");

        verify(logEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("findById should throw ResourceNotFoundException for missing ID")
    void findById_nonExistentId_throwsResourceNotFoundException() {
        when(logEntryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> logEntryService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("findByTimeRange should throw InvalidLogException when from > to")
    void findByTimeRange_invalidRange_throwsInvalidLogException() {
        Instant from = Instant.now();
        Instant to = from.minusSeconds(3600);

        assertThatThrownBy(() -> logEntryService.findByTimeRange(from, to, null))
                .isInstanceOf(InvalidLogException.class)
                .hasMessageContaining("'from' timestamp must be before 'to'");
    }
}

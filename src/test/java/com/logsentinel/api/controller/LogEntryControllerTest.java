package com.logsentinel.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logsentinel.api.dto.LogEntryRequest;
import com.logsentinel.api.dto.LogEntryResponse;
import com.logsentinel.api.entity.LogLevel;
import com.logsentinel.api.service.LogEntryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LogEntryController.class)
@DisplayName("LogEntryController Integration Tests")
class LogEntryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private LogEntryService logEntryService;

    @Test
    @DisplayName("POST /api/v1/logs - valid request returns 201 CREATED")
    void ingest_validBody_returns201() throws Exception {
        LogEntryRequest request = new LogEntryRequest();
        request.setLevel(LogLevel.WARN);
        request.setMessage("Disk usage above 85%");
        request.setSourceSystem("infra-monitor");

        LogEntryResponse response = LogEntryResponse.builder()
                .id(1L).level(LogLevel.WARN)
                .message("Disk usage above 85%")
                .sourceSystem("infra-monitor")
                .timestamp(Instant.now())
                .receivedAt(Instant.now())
                .build();

        when(logEntryService.ingest(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.sourceSystem").value("infra-monitor"));
    }

    @Test
    @DisplayName("POST /api/v1/logs - missing message returns 400")
    void ingest_missingMessage_returns400() throws Exception {
        LogEntryRequest request = new LogEntryRequest();
        request.setLevel(LogLevel.INFO);
        // message is intentionally omitted
        request.setSourceSystem("auth-service");

        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.message").exists());
    }

    @Test
    @DisplayName("GET /api/v1/logs/by-level/ERROR returns paginated results")
    void getByLevel_validLevel_returnsPage() throws Exception {
        LogEntryResponse entry = LogEntryResponse.builder()
                .id(2L).level(LogLevel.ERROR)
                .message("Connection timeout")
                .sourceSystem("db-service")
                .timestamp(Instant.now())
                .receivedAt(Instant.now())
                .build();

        when(logEntryService.findByLevel(eq(LogLevel.ERROR), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entry)));

        mockMvc.perform(get("/api/v1/logs/by-level/ERROR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].level").value("ERROR"))
                .andExpect(jsonPath("$.content[0].sourceSystem").value("db-service"));
    }
}

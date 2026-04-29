# LogSentinel 🛡️

> A lightweight SIEM-style backend API that ingests application logs, detects anomalous error patterns, and raises alerts — built with Java 17 and Spring Boot.

---

## What It Does

LogSentinel acts as a centralized log aggregation and alerting layer. Source systems (microservices, servers, etc.) push log events to the API. An internal alert engine continuously scans for high-severity bursts and raises alerts when a configured threshold is exceeded within a time window.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Persistence | Spring Data JPA + H2 |
| Validation | Jakarta Bean Validation |
| Build | Maven |

---

## Project Structure

```
log-sentinel/
├── src/main/java/com/logsentinel/api/
│   ├── LogSentinelApplication.java        ← Entry point
│   ├── config/
│   │   ├── AlertEngineProperties.java     ← Alert threshold config
│   │   ├── RateLimitProperties.java       ← Rate limit config
│   │   └── WebConfig.java                 ← CORS config
│   ├── controller/
│   │   ├── LogEntryController.java        ← Log ingestion & search endpoints
│   │   └── AlertController.java           ← Alert management endpoints
│   ├── service/
│   │   ├── LogEntryService.java           ← Log business logic
│   │   └── AlertService.java              ← Alert business logic
│   ├── repository/
│   │   ├── LogEntryRepository.java        ← Log JPA queries
│   │   └── AlertRepository.java           ← Alert JPA queries
│   ├── entity/
│   │   ├── LogEntry.java                  ← Log entry entity
│   │   ├── Alert.java                     ← Alert entity
│   │   ├── LogLevel.java                  ← Enum: INFO/WARN/ERROR/CRITICAL
│   │   └── AlertStatus.java               ← Enum: ACTIVE/ACKNOWLEDGED/RESOLVED
│   ├── dto/
│   │   ├── LogEntryRequest.java           ← Inbound log payload
│   │   ├── LogEntryResponse.java          ← Outbound log representation
│   │   ├── AlertResponse.java             ← Outbound alert representation
│   │   ├── AlertStatusUpdateRequest.java  ← Status update payload
│   │   └── ApiErrorResponse.java          ← Unified error envelope
│   ├── engine/
│   │   ├── AlertEngine.java               ← Scheduled alert scanner
│   │   ├── InMemoryRateLimiter.java       ← Per-source request throttling
│   │   └── RecentLogCache.java            ← LRU cache for recent entries
│   └── exception/
│       ├── GlobalExceptionHandler.java    ← @ControllerAdvice handler
│       ├── InvalidLogException.java
│       ├── ResourceNotFoundException.java
│       └── RateLimitExceededException.java
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

---

## Running Locally

### Prerequisites
- Java 17+
- Maven 3.8+

### Steps

```bash
# Clone the repository
git clone https://github.com/your-username/log-sentinel.git
cd log-sentinel

# Build and run
mvn spring-boot:run

# Or build the JAR first
mvn clean package -DskipTests
java -jar target/log-sentinel-1.0.0.jar
```

The server starts on **http://localhost:8080**

### H2 Console (dev only)
Access the in-memory database browser at:
**http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:mem:logsentinel`
- Username: `root` | Password: *()*

---

## API Reference

### Log Ingestion

#### Submit a log entry
```bash
curl -X POST http://localhost:8080/api/v1/logs \
  -H "Content-Type: application/json" \
  -d '{
    "level": "ERROR",
    "message": "Database connection pool exhausted after 30s",
    "sourceSystem": "payment-gateway",
    "traceId": "trace-abc-123"
  }'
```

#### Submit a CRITICAL log
```bash
curl -X POST http://localhost:8080/api/v1/logs \
  -H "Content-Type: application/json" \
  -d '{
    "level": "CRITICAL",
    "message": "Authentication service is unreachable — all login attempts failing",
    "sourceSystem": "auth-service",
    "timestamp": "2024-06-15T10:30:00Z"
  }'
```

### Log Retrieval

#### List all logs (paginated)
```bash
curl "http://localhost:8080/api/v1/logs?page=0&size=10&sort=timestamp,desc"
```

#### Get a specific log by ID
```bash
curl http://localhost:8080/api/v1/logs/1
```

#### Filter by severity level
```bash
curl http://localhost:8080/api/v1/logs/by-level/CRITICAL
```

#### Filter by source system
```bash
curl http://localhost:8080/api/v1/logs/by-source/payment-gateway
```

#### Filter by time range
```bash
curl "http://localhost:8080/api/v1/logs/by-time-range?from=2024-06-15T00:00:00Z&to=2024-06-15T23:59:59Z"
```

#### Multi-criteria search
```bash
curl "http://localhost:8080/api/v1/logs/search?level=ERROR&source=auth-service&from=2024-06-15T00:00:00Z"
```

#### Get recent logs (from in-memory cache)
```bash
curl http://localhost:8080/api/v1/logs/recent
```

### Alert Management

#### Get all alerts
```bash
curl http://localhost:8080/api/v1/alerts
```

#### Get only active (unresolved) alerts
```bash
curl http://localhost:8080/api/v1/alerts/active
```

#### Get alert by ID
```bash
curl http://localhost:8080/api/v1/alerts/1
```

#### Get alerts for a specific source system
```bash
curl http://localhost:8080/api/v1/alerts/by-source/auth-service
```

#### Acknowledge an alert
```bash
curl -X PATCH http://localhost:8080/api/v1/alerts/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "ACKNOWLEDGED",
    "operatorNotes": "Investigated — DB replica lag caused connection pool saturation. Scaling up."
  }'
```

#### Resolve an alert
```bash
curl -X PATCH http://localhost:8080/api/v1/alerts/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "RESOLVED",
    "operatorNotes": "Replica caught up. Error rate normalized."
  }'
```

### Health & Info (Actuator)

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/info
```

---

## Alert Engine Configuration

Tunable via `application.properties` without touching code:

| Property | Default | Description |
|---|---|---|
| `alert.engine.error-threshold` | `5` | Errors within window before alert fires |
| `alert.engine.window-seconds` | `60` | Observation window duration |
| `alert.engine.cooldown-seconds` | `300` | Minimum gap between alerts for same source |
| `rate.limit.requests-per-minute` | `100` | Max ingest requests per source per minute |

---

## Running Tests

```bash
mvn test
```

---

## Design Highlights

- **No circular dependencies** — engine components are injected into services, not controllers.
- **Cooldown suppression** — prevents alert storms from repeatedly noisy services.
- **LRU cache** — O(1) access for recent log reads without DB round-trips.
- **Composite JPQL filter** — single query handles any combination of search parameters.
- **Unified error responses** — all errors return the same JSON shape for easy client-side handling.

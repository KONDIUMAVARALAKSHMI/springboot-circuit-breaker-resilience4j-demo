# Circuit Breaker Pattern with Resilience4j & Spring Boot

> **Author:** Uma VaraLakshmi Kondi  
> **Domain:** Backend Development / Distributed Systems  
> **Stack:** Java 17, Spring Boot 3.2, Resilience4j 2.2, Maven

---

## Overview

This project demonstrates the **Circuit Breaker** fault-tolerance pattern using [Resilience4j](https://resilience4j.readme.io/) integrated with a Spring Boot 3 application.

### What it does
- Exposes a `/api/users/{id}` endpoint that calls an (unreliable) external service
- Wraps the external call with a `@CircuitBreaker` that opens after 50% failures over 10 calls
- Returns a **fallback response** when the circuit is OPEN
- Exposes endpoints to inspect circuit breaker **state** and **metrics**
- Includes a built-in **Mock External Service** you can control to simulate failures

### Circuit Breaker States
```
[CLOSED] ---(too many failures)---> [OPEN] ---(wait 10s)---> [HALF_OPEN] ---(success)---> [CLOSED]
                                                                           ---(failure)---> [OPEN]
```

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 17 or higher |
| Maven | 3.8+ |
| curl | Any (for test script) |

Verify your Java version:
```bash
java -version
```

---

## Build Instructions

```bash
# Navigate to the project directory
cd circuit-breaker-demo

# Clean and compile the project
mvn clean install

# Skip tests during build (optional)
mvn clean install -DskipTests
```

A successful build produces:
```
target/circuit-breaker-demo-1.0.0.jar
```

---

## Run Instructions

```bash
# Run the application
java -jar target/circuit-breaker-demo-1.0.0.jar
```

The application starts on **port 8080**.

You should see:
```
Started CircuitBreakerDemoApplication in X.XXX seconds
```

### Or run with Maven directly:
```bash
mvn spring-boot:run
```

---

## API Endpoints

### User API
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/{id}` | Fetch user (circuit-breaker protected) |

**Success Response (200):**
```json
{ "id": "1", "name": "User 1", "email": "user1@example.com" }
```

**Fallback Response (200, when circuit is OPEN):**
```json
{ "id": "default-id", "name": "Default User", "email": "default@example.com" }
```

### Circuit Breaker Monitoring
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/circuit-breaker/state` | Returns `CLOSED`, `OPEN`, or `HALF_OPEN` |
| GET | `/api/circuit-breaker/metrics` | Returns simplified metrics JSON |
| GET | `/actuator/circuitbreakers` | Full Actuator metrics |

### Mock External Service Controls
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/mock/users/{id}` | The mock external service |
| POST | `/mock/failure?enabled=true` | Force always-fail mode |
| POST | `/mock/success?enabled=true` | Force always-succeed mode |
| POST | `/mock/reset` | Reset to alternating (50% fail) mode |
| GET | `/mock/status` | Current mock service status |

---

## Testing Instructions

### Option 1: Automated Test Script (Recommended)

```bash
# Make sure the application is running first!
# Then in a new terminal:
./test-circuit-breaker.sh
```

**What the script demonstrates:**
1. Resets the mock service to a clean state
2. Enables **ALWAYS FAIL** mode and makes 10 calls → trips the circuit breaker
3. Shows the circuit is now **OPEN** (fallback returned immediately)
4. Waits 12 seconds for the `wait-duration-in-open-state` (10s) to pass
5. Enables **ALWAYS SUCCEED** mode
6. Makes successful calls → circuit transitions **HALF_OPEN → CLOSED**
7. Displays final metrics and Actuator data

**Expected output:**
```
[Step 1] Initial state: CLOSED
[Step 2] 10 failing calls...
[Step 3] State: OPEN
[Step 4] Fallback responses while OPEN
[Step 5] Waiting 12s...
[Step 6] Successful calls...
[Step 7] Final state: CLOSED
```

### Option 2: Manual Testing with curl

```bash
# 1. Check initial state (should be CLOSED)
curl http://localhost:8080/api/circuit-breaker/state

# 2. Enable failure mode
curl -X POST "http://localhost:8080/mock/failure?enabled=true"

# 3. Make 10 failing calls to trip the circuit
for i in {1..10}; do curl -s http://localhost:8080/api/users/1; echo; done

# 4. Check state (should be OPEN)
curl http://localhost:8080/api/circuit-breaker/state

# 5. See fallback response
curl http://localhost:8080/api/users/1

# 6. Wait 10 seconds
sleep 10

# 7. Enable success mode
curl -X POST "http://localhost:8080/mock/success?enabled=true"

# 8. Make successful calls to close circuit
for i in {1..3}; do curl -s http://localhost:8080/api/users/1; echo; done

# 9. Verify CLOSED
curl http://localhost:8080/api/circuit-breaker/state
```

---

## Circuit Breaker Configuration

Located in `src/main/resources/application.yml`:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      userService:
        failure-rate-threshold: 50        # Open when 50% of calls fail
        minimum-number-of-calls: 5        # At least 5 calls before evaluating
        sliding-window-type: COUNT_BASED  # Count last N calls
        sliding-window-size: 10           # Window of 10 calls
        wait-duration-in-open-state: 10s  # Stay open for 10 seconds
        permitted-number-of-calls-in-half-open-state: 2  # 2 test calls in HALF_OPEN
```

---

## Project Structure

```
circuit-breaker-demo/
├── pom.xml
├── README.md
├── test-circuit-breaker.sh
└── src/
    └── main/
        ├── java/com/uma/circuitbreaker/
        │   ├── CircuitBreakerDemoApplication.java   # Entry point
        │   ├── config/
        │   │   ├── AppConfig.java                  # RestTemplate bean
        │   │   └── CircuitBreakerEventConfig.java  # State transition logging
        │   ├── controller/
        │   │   ├── UserController.java             # GET /api/users/{id}
        │   │   ├── CircuitBreakerController.java   # State & metrics endpoints
        │   │   └── MockExternalServiceController.java  # Simulated external API
        │   ├── dto/
        │   │   ├── UserDTO.java
        │   │   └── CircuitBreakerMetricsDTO.java
        │   └── service/
        │       └── UserDataService.java            # @CircuitBreaker + fallback
        └── resources/
            └── application.yml
```

---

## Troubleshooting

**@CircuitBreaker annotation not triggering?**  
Ensure the `UserController` calls `userDataService.fetchUser()` as a Spring bean (not `this.fetchUser()`). Spring AOP proxies only intercept calls between beans.

**Circuit won't open?**  
The `minimum-number-of-calls` is 5. You need at least 5 calls with ≥50% failure before the circuit opens. Make at least 10 calls with the failure mode enabled.

**Port already in use?**  
Change the port: `java -jar target/*.jar --server.port=9090`

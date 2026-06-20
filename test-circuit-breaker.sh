#!/bin/bash

# ============================================================
# test-circuit-breaker.sh
# Demonstrates the Circuit Breaker pattern with Resilience4j
# Author: Uma VaraLakshmi Kondi
# ============================================================

BASE_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================================${NC}"
echo -e "${BLUE}   Circuit Breaker Pattern Demo - Resilience4j          ${NC}"
echo -e "${BLUE}========================================================${NC}"
echo ""

# ---- Helper functions ----
check_state() {
    STATE=$(curl -s "${BASE_URL}/api/circuit-breaker/state")
    echo -e "  ${CYAN}[CB STATE]${NC} ${STATE}"
}

call_user_api() {
    local ID=$1
    RESPONSE=$(curl -s -w "\n%{http_code}" "${BASE_URL}/api/users/${ID}")
    HTTP_BODY=$(echo "$RESPONSE" | head -n -1)
    HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
    echo -e "  ${CYAN}[HTTP ${HTTP_CODE}]${NC} ${HTTP_BODY}"
}

print_metrics() {
    echo -e "  ${CYAN}[METRICS]${NC}"
    curl -s "${BASE_URL}/api/circuit-breaker/metrics" | python3 -m json.tool 2>/dev/null || \
    curl -s "${BASE_URL}/api/circuit-breaker/metrics"
    echo ""
}

separator() {
    echo -e "${YELLOW}--------------------------------------------------------${NC}"
}

# ---- Step 0: Verify app is running ----
echo -e "${YELLOW}[Step 0] Checking if the application is running...${NC}"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/actuator/health")
if [ "$HTTP" != "200" ]; then
    echo -e "${RED}ERROR: Application is not running on ${BASE_URL}${NC}"
    echo -e "${RED}Please start it first: java -jar target/circuit-breaker-demo-1.0.0.jar${NC}"
    exit 1
fi
echo -e "${GREEN}Application is UP!${NC}"
separator

# ---- Step 1: Reset mock service and check initial state ----
echo -e "${YELLOW}[Step 1] Resetting mock service and checking initial CB state...${NC}"
curl -s -X POST "${BASE_URL}/mock/reset" > /dev/null
check_state
echo ""
separator

# ---- Step 2: Enable always-fail mode and make 10 calls to trip the breaker ----
echo -e "${YELLOW}[Step 2] Enabling ALWAYS FAIL mode on mock service...${NC}"
curl -s -X POST "${BASE_URL}/mock/failure?enabled=true" > /dev/null
echo -e "${GREEN}Mock service now always returns 500.${NC}"
echo ""

echo -e "${YELLOW}Making 10 calls to trigger the circuit breaker...${NC}"
echo -e "(Need >= 5 calls with >= 50% failure rate to open)"
echo ""

for i in {1..10}; do
    echo -n "  Call #${i}: "
    call_user_api "1"
    sleep 0.3
done

separator
echo -e "${YELLOW}[Step 3] Checking circuit breaker state after failures...${NC}"
check_state
print_metrics

separator

# ---- Step 4: Show fallback is being returned with circuit OPEN ----
echo -e "${YELLOW}[Step 4] Making calls while circuit is OPEN (should get fallback immediately)...${NC}"
for i in {1..3}; do
    echo -n "  Call #${i} (circuit open): "
    call_user_api "1"
    sleep 0.2
done

echo ""
check_state
separator

# ---- Step 5: Wait for circuit to transition to HALF_OPEN ----
WAIT_SECS=12
echo -e "${YELLOW}[Step 5] Waiting ${WAIT_SECS}s for circuit to transition to HALF_OPEN...${NC}"
echo -e "(wait-duration-in-open-state = 10s)"
echo -n "  Waiting"
for i in $(seq 1 $WAIT_SECS); do
    sleep 1
    echo -n "."
done
echo ""
echo ""
check_state
separator

# ---- Step 6: Enable success mode and make test calls in HALF_OPEN ----
echo -e "${YELLOW}[Step 6] Enabling ALWAYS SUCCEED mode and testing HALF_OPEN state...${NC}"
curl -s -X POST "${BASE_URL}/mock/success?enabled=true" > /dev/null
echo -e "${GREEN}Mock service now always returns 200.${NC}"
echo ""

echo -e "${YELLOW}Making 3 successful calls to close the circuit...${NC}"
for i in {1..3}; do
    echo -n "  Call #${i}: "
    call_user_api "1"
    sleep 0.5
done

echo ""
separator

# ---- Step 7: Verify circuit is CLOSED ----
echo -e "${YELLOW}[Step 7] Final circuit breaker state:${NC}"
check_state
print_metrics

# ---- Step 8: Verify actuator endpoint ----
separator
echo -e "${YELLOW}[Step 8] Checking Spring Boot Actuator circuit breaker endpoint...${NC}"
echo -e "  ${CYAN}GET /actuator/circuitbreakers${NC}"
curl -s "${BASE_URL}/actuator/circuitbreakers" | python3 -m json.tool 2>/dev/null || \
curl -s "${BASE_URL}/actuator/circuitbreakers"
echo ""

separator
echo -e "${GREEN}========================================================${NC}"
echo -e "${GREEN}  Demo Complete! Circuit Breaker transitions observed:  ${NC}"
echo -e "${GREEN}  CLOSED -> OPEN -> HALF_OPEN -> CLOSED                 ${NC}"
echo -e "${GREEN}========================================================${NC}"

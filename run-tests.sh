#!/bin/bash

# KTX Reservation Backend - Test Runner Script
# This script runs tests and generates JaCoCo coverage reports for all microservices

set -e

SERVICES=("user-service" "train-service" "seat-service" "reservation-service")
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "======================================"
echo "KTX Reservation Backend Test Runner"
echo "======================================"
echo ""

# Function to run tests for a service
run_service_tests() {
    local service=$1
    echo -e "${YELLOW}[TEST]${NC} Running tests for ${service}..."

    docker run --rm \
        -v "$(pwd)/services/${service}:/app" \
        -w /app \
        gradle:8.5-jdk17 \
        gradle test jacocoTestReport jacocoTestCoverageVerification --no-daemon --console=plain

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓${NC} ${service} tests passed"
    else
        echo -e "${RED}✗${NC} ${service} tests failed"
        return 1
    fi
}

# Function to display coverage report
show_coverage() {
    local service=$1
    local report_file="services/${service}/build/reports/jacoco/test/html/index.html"

    if [ -f "$report_file" ]; then
        echo -e "${GREEN}Coverage report:${NC} $report_file"
    fi
}

# Main execution
FAILED_SERVICES=()

for service in "${SERVICES[@]}"; do
    echo ""
    echo "======================================"
    echo "Testing: $service"
    echo "======================================"

    if run_service_tests "$service"; then
        show_coverage "$service"
    else
        FAILED_SERVICES+=("$service")
    fi
done

echo ""
echo "======================================"
echo "Test Summary"
echo "======================================"

if [ ${#FAILED_SERVICES[@]} -eq 0 ]; then
    echo -e "${GREEN}✓ All tests passed!${NC}"
    echo ""
    echo "Coverage Reports:"
    for service in "${SERVICES[@]}"; do
        echo "  - $service: services/${service}/build/reports/jacoco/test/html/index.html"
    done
    exit 0
else
    echo -e "${RED}✗ Some tests failed:${NC}"
    for service in "${FAILED_SERVICES[@]}"; do
        echo "  - $service"
    done
    exit 1
fi

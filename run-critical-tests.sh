#!/bin/bash

# Critical Test Runner for OpenFields2
# This script runs all mandatory tests before DevCycle closure or system completion

echo "=== OpenFields2 Critical Test Suite ==="
echo "Running all mandatory tests..."
echo

# Array of critical tests
CRITICAL_TESTS=(
    "GunfightTestAutomated"
    "BasicMissTestAutomated" 
    "BasicMissTestSimple"
    "HeadlessGunfightTest"
)

# Track results
PASSED=0
FAILED=0
FAILED_TESTS=()

# Run each test
for test in "${CRITICAL_TESTS[@]}"; do
    echo "=== Running $test ==="
    if mvn test -Dtest="$test" -q; then
        echo "✅ $test PASSED"
        ((PASSED++))
    else
        echo "❌ $test FAILED"
        ((FAILED++))
        FAILED_TESTS+=("$test")
    fi
    echo
done

# Summary
echo "=== Critical Test Results ==="
echo "Passed: $PASSED"
echo "Failed: $FAILED"

if [ $FAILED -eq 0 ]; then
    echo "🎉 ALL CRITICAL TESTS PASSED - DevCycle closure/system completion approved"
    exit 0
else
    echo "🚫 CRITICAL TESTS FAILED - DevCycle closure/system completion BLOCKED"
    echo "Failed tests:"
    for failed_test in "${FAILED_TESTS[@]}"; do
        echo "  - $failed_test"
    done
    echo
    echo "All critical tests must pass before proceeding with DevCycle closure or system completion."
    exit 1
fi
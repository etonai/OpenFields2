#!/bin/bash

# OpenFields2 Test Runner
# Provides different test execution options

show_help() {
    echo "OpenFields2 Test Runner"
    echo "Usage: $0 [option]"
    echo ""
    echo "Options:"
    echo "  -f, --fast              Run fast tests only (headless)"
    echo "  -a, --all               Run all critical tests (default)"
    echo "  -c, --completion-check  Run critical tests + system completion validation"
    echo "  -s, --single TEST       Run single test by name"
    echo "  -h, --help              Show this help"
    echo ""
    echo "Fast tests:"
    echo "  - HeadlessGunfightTest"
    echo ""
    echo "All critical tests:"
    echo "  - GunfightTestAutomated"
    echo "  - BasicMissTestAutomated" 
    echo "  - BasicMissTestSimple"
    echo "  - HeadlessGunfightTest"
    echo ""
    echo "Completion check:"
    echo "  - Runs all critical tests"
    echo "  - Validates results for system completion"
    echo "  - Provides completion status summary"
}

run_fast_tests() {
    echo "=== Running Fast Tests (Headless Only) ==="
    mvn test -Dtest=HeadlessGunfightTest
    return $?
}

run_all_tests() {
    echo "=== Running All Critical Tests ==="
    
    TESTS=(
        "HeadlessGunfightTest"
        "BasicMissTestSimple"
        "BasicMissTestAutomated"
        "GunfightTestAutomated"
    )
    
    PASSED=0
    FAILED=0
    FAILED_TESTS=()
    
    for test in "${TESTS[@]}"; do
        echo "Running $test..."
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
    
    echo "=== Results ==="
    echo "Passed: $PASSED"
    echo "Failed: $FAILED"
    
    if [ $FAILED -eq 0 ]; then
        echo "🎉 ALL TESTS PASSED"
        return 0
    else
        echo "🚫 SOME TESTS FAILED:"
        for failed_test in "${FAILED_TESTS[@]}"; do
            echo "  - $failed_test"
        done
        return 1
    fi
}

run_single_test() {
    local test_name="$1"
    echo "=== Running Single Test: $test_name ==="
    mvn test -Dtest="$test_name"
    return $?
}

run_completion_check() {
    echo "🚨 SYSTEM COMPLETION VALIDATION 🚨"
    echo "Running all critical tests for system completion verification..."
    echo ""
    
    # Run all critical tests first
    if ! run_all_tests; then
        echo ""
        echo "❌ COMPLETION CHECK FAILED"
        echo "🚫 Critical tests failed - system CANNOT be marked as complete"
        echo ""
        echo "Required actions:"
        echo "1. Fix failing tests"
        echo "2. Re-run completion check"
        echo "3. Only mark system complete after ALL tests pass"
        return 1
    fi
    
    echo ""
    echo "✅ CRITICAL TESTS VALIDATION COMPLETE"
    echo "🎉 All critical tests passed - ready for system completion"
    echo ""
    echo "📋 NEXT STEPS FOR SYSTEM COMPLETION:"
    echo "1. ✅ Critical tests verified (COMPLETE)"
    echo "2. ⏳ Request user testing and confirmation"
    echo "3. ⏳ Wait for explicit user approval"
    echo "4. ⏳ Only then mark system as ✅ COMPLETE"
    echo ""
    echo "⚠️  REMINDER: Do NOT mark system complete without user confirmation!"
    return 0
}

# Main script
case "${1:-}" in
    -f|--fast)
        run_fast_tests
        ;;
    -a|--all|"")
        run_all_tests
        ;;
    -c|--completion-check)
        run_completion_check
        ;;
    -s|--single)
        if [ -z "$2" ]; then
            echo "Error: Test name required for --single option"
            show_help
            exit 1
        fi
        run_single_test "$2"
        ;;
    -h|--help)
        show_help
        exit 0
        ;;
    *)
        echo "Error: Unknown option '$1'"
        show_help
        exit 1
        ;;
esac
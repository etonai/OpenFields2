@echo off
REM Critical Test Runner for OpenFields2
REM This script runs all mandatory tests before DevCycle closure or system completion

echo === OpenFields2 Critical Test Suite ===
echo Running all mandatory tests...
echo.

set PASSED=0
set FAILED=0
set FAILED_TESTS=

REM Run each critical test
echo === Running GunfightTestAutomated ===
mvn test -Dtest=GunfightTestAutomated -q
if %ERRORLEVEL% == 0 (
    echo ✅ GunfightTestAutomated PASSED
    set /a PASSED+=1
) else (
    echo ❌ GunfightTestAutomated FAILED
    set /a FAILED+=1
    set FAILED_TESTS=%FAILED_TESTS% GunfightTestAutomated
)
echo.

echo === Running BasicMissTestAutomated ===
mvn test -Dtest=BasicMissTestAutomated -q
if %ERRORLEVEL% == 0 (
    echo ✅ BasicMissTestAutomated PASSED
    set /a PASSED+=1
) else (
    echo ❌ BasicMissTestAutomated FAILED
    set /a FAILED+=1
    set FAILED_TESTS=%FAILED_TESTS% BasicMissTestAutomated
)
echo.

echo === Running BasicMissTestSimple ===
mvn test -Dtest=BasicMissTestSimple -q
if %ERRORLEVEL% == 0 (
    echo ✅ BasicMissTestSimple PASSED
    set /a PASSED+=1
) else (
    echo ❌ BasicMissTestSimple FAILED
    set /a FAILED+=1
    set FAILED_TESTS=%FAILED_TESTS% BasicMissTestSimple
)
echo.

echo === Running HeadlessGunfightTest ===
mvn test -Dtest=HeadlessGunfightTest -q
if %ERRORLEVEL% == 0 (
    echo ✅ HeadlessGunfightTest PASSED
    set /a PASSED+=1
) else (
    echo ❌ HeadlessGunfightTest FAILED
    set /a FAILED+=1
    set FAILED_TESTS=%FAILED_TESTS% HeadlessGunfightTest
)
echo.

REM Summary
echo === Critical Test Results ===
echo Passed: %PASSED%
echo Failed: %FAILED%

if %FAILED% == 0 (
    echo 🎉 ALL CRITICAL TESTS PASSED - DevCycle closure/system completion approved
    exit /b 0
) else (
    echo 🚫 CRITICAL TESTS FAILED - DevCycle closure/system completion BLOCKED
    echo Failed tests:%FAILED_TESTS%
    echo.
    echo All critical tests must pass before proceeding with DevCycle closure or system completion.
    exit /b 1
)
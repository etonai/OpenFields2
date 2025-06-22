/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Centralized diagnostic and debugging service for the OpenFields2 input system.
 * 
 * This service provides comprehensive diagnostic capabilities including performance monitoring,
 * system health validation, input event tracing, component status tracking, and debug
 * information collection. It serves as the central hub for all diagnostic operations
 * while integrating with DisplayCoordinator for output formatting and presentation.
 * 
 * DIAGNOSTIC CATEGORIES:
 * - Performance Monitoring: Input event timing, memory usage, system performance metrics
 * - System Health: Component validation, integrity checks, system status reporting
 * - Event Tracing: Input event logging, workflow state tracking, operation monitoring
 * - Debug Control: Debug mode management, configuration, diagnostic data control
 * - Error Tracking: Exception logging, error condition monitoring, failure analysis
 * 
 * INTEGRATION PATTERN:
 * InputDiagnosticService collects and processes diagnostic data, while DisplayCoordinator
 * handles formatting and presentation. This separation allows for flexible diagnostic
 * data usage while maintaining clean display coordination.
 * 
 * @author DevCycle 15i - Phase 3: Input Diagnostic Service Extraction
 */
public class InputDiagnosticService {
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Diagnostic Configuration and State
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /** Whether diagnostic collection is enabled */
    private boolean diagnosticsEnabled = false;
    
    /** Whether detailed input event tracing is enabled */
    private boolean inputTracingEnabled = false;
    
    /** Whether performance monitoring is enabled */
    private boolean performanceMonitoringEnabled = false;
    
    /** Maximum number of diagnostic events to retain in memory */
    private static final int MAX_DIAGNOSTIC_EVENTS = 1000;
    
    /** Maximum number of performance measurements to retain */
    private static final int MAX_PERFORMANCE_MEASUREMENTS = 500;
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Performance Monitoring Data
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /** Active performance timers mapped by operation name */
    private final Map<String, Long> activeTimers = new HashMap<>();
    
    /** Completed performance measurements */
    private final List<PerformanceMeasurement> performanceMeasurements = new ArrayList<>();
    
    /** Memory usage snapshots for analysis */
    private final List<MemorySnapshot> memorySnapshots = new ArrayList<>();
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // System Health and Component Status
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /** Current system health status */
    private SystemHealthStatus systemHealth = SystemHealthStatus.UNKNOWN;
    
    /** Component health statuses mapped by component name */
    private final Map<String, ComponentStatus> componentStatuses = new HashMap<>();
    
    /** Last system integrity check result */
    private SystemIntegrityResult lastIntegrityCheck = null;
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Event Tracing and Debug Logging
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /** Input event trace log for debugging */
    private final List<InputTraceEvent> inputTraceEvents = new ArrayList<>();
    
    /** State transition events for workflow debugging */
    private final List<StateTransitionEvent> stateTransitionEvents = new ArrayList<>();
    
    /** General debug log entries */
    private final List<DebugLogEntry> debugLogEntries = new ArrayList<>();
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Performance Monitoring Methods
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Start performance timing for an operation.
     * 
     * @param operationName Name of the operation being timed
     */
    public void startPerformanceTimer(String operationName) {
        if (!performanceMonitoringEnabled) return;
        
        activeTimers.put(operationName, System.nanoTime());
    }
    
    /**
     * End performance timing for an operation and record the measurement.
     * 
     * @param operationName Name of the operation being timed
     * @return Duration in nanoseconds, or -1 if timer was not found
     */
    public long endPerformanceTimer(String operationName) {
        if (!performanceMonitoringEnabled) return -1;
        
        Long startTime = activeTimers.remove(operationName);
        if (startTime == null) {
            recordDebugLog("PERFORMANCE", "ERROR", "Timer not found for operation: " + operationName);
            return -1;
        }
        
        long duration = System.nanoTime() - startTime;
        recordPerformanceMeasurement(operationName, duration);
        return duration;
    }
    
    /**
     * Record a performance measurement.
     * 
     * @param operationName Name of the operation
     * @param durationNanos Duration in nanoseconds
     */
    private void recordPerformanceMeasurement(String operationName, long durationNanos) {
        PerformanceMeasurement measurement = new PerformanceMeasurement(
            operationName, durationNanos, System.currentTimeMillis()
        );
        
        performanceMeasurements.add(measurement);
        
        // Keep only recent measurements
        if (performanceMeasurements.size() > MAX_PERFORMANCE_MEASUREMENTS) {
            performanceMeasurements.remove(0);
        }
    }
    
    /**
     * Take a memory usage snapshot for monitoring.
     * 
     * @param context Context or operation when snapshot was taken
     */
    public void logMemoryUsage(String context) {
        if (!performanceMonitoringEnabled) return;
        
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        MemorySnapshot snapshot = new MemorySnapshot(
            context, usedMemory, totalMemory, maxMemory, System.currentTimeMillis()
        );
        
        memorySnapshots.add(snapshot);
        
        // Keep only recent snapshots
        if (memorySnapshots.size() > MAX_PERFORMANCE_MEASUREMENTS) {
            memorySnapshots.remove(0);
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Input Event Tracing Methods
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Record an input event for debugging and analysis.
     * 
     * @param eventType Type of input event (MOUSE_PRESS, KEY_PRESS, etc.)
     * @param details Detailed information about the event
     */
    public void recordInputEvent(String eventType, String details) {
        if (!inputTracingEnabled) return;
        
        InputTraceEvent event = new InputTraceEvent(
            eventType, details, System.currentTimeMillis()
        );
        
        inputTraceEvents.add(event);
        
        // Keep only recent events
        if (inputTraceEvents.size() > MAX_DIAGNOSTIC_EVENTS) {
            inputTraceEvents.remove(0);
        }
    }
    
    /**
     * Record a state transition for workflow debugging.
     * 
     * @param systemName Name of the system changing state
     * @param fromState Previous state
     * @param toState New state
     */
    public void recordStateTransition(String systemName, String fromState, String toState) {
        if (!diagnosticsEnabled) return;
        
        StateTransitionEvent event = new StateTransitionEvent(
            systemName, fromState, toState, System.currentTimeMillis()
        );
        
        stateTransitionEvents.add(event);
        
        // Keep only recent transitions
        if (stateTransitionEvents.size() > MAX_DIAGNOSTIC_EVENTS) {
            stateTransitionEvents.remove(0);
        }
    }
    
    /**
     * Record a debug log entry.
     * 
     * @param category Category or system generating the log
     * @param level Log level (INFO, WARN, ERROR, DEBUG)
     * @param message Log message
     */
    public void recordDebugLog(String category, String level, String message) {
        if (!diagnosticsEnabled) return;
        
        DebugLogEntry entry = new DebugLogEntry(
            category, level, message, System.currentTimeMillis()
        );
        
        debugLogEntries.add(entry);
        
        // Keep only recent entries
        if (debugLogEntries.size() > MAX_DIAGNOSTIC_EVENTS) {
            debugLogEntries.remove(0);
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // System Health and Component Monitoring
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Update the health status of a specific component.
     * 
     * @param componentName Name of the component
     * @param isHealthy Whether the component is healthy
     * @param statusMessage Optional status message
     */
    public void updateComponentStatus(String componentName, boolean isHealthy, String statusMessage) {
        ComponentStatus status = new ComponentStatus(
            componentName, isHealthy, statusMessage, System.currentTimeMillis()
        );
        
        componentStatuses.put(componentName, status);
        updateOverallSystemHealth();
    }
    
    /**
     * Update overall system health based on component statuses.
     */
    private void updateOverallSystemHealth() {
        if (componentStatuses.isEmpty()) {
            systemHealth = SystemHealthStatus.UNKNOWN;
            return;
        }
        
        boolean allHealthy = componentStatuses.values().stream()
            .allMatch(status -> status.isHealthy);
        
        boolean anyUnhealthy = componentStatuses.values().stream()
            .anyMatch(status -> !status.isHealthy);
        
        if (allHealthy) {
            systemHealth = SystemHealthStatus.HEALTHY;
        } else if (anyUnhealthy) {
            systemHealth = SystemHealthStatus.UNHEALTHY;
        } else {
            systemHealth = SystemHealthStatus.DEGRADED;
        }
    }
    
    /**
     * Perform comprehensive system integrity validation.
     * 
     * @param components List of components to validate
     * @return Integrity check result
     */
    public SystemIntegrityResult performSystemIntegrityCheck(List<Object> components) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Validate component availability
        for (Object component : components) {
            if (component == null) {
                errors.add("Null component found in system");
            }
        }
        
        // Check memory status
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        if (memoryUsagePercent > 90) {
            errors.add("Critical memory usage: " + String.format("%.1f%%", memoryUsagePercent));
        } else if (memoryUsagePercent > 75) {
            warnings.add("High memory usage: " + String.format("%.1f%%", memoryUsagePercent));
        }
        
        // Check component health statuses
        for (ComponentStatus status : componentStatuses.values()) {
            if (!status.isHealthy) {
                errors.add("Component " + status.componentName + " is unhealthy: " + status.statusMessage);
            }
        }
        
        lastIntegrityCheck = new SystemIntegrityResult(
            errors.isEmpty(), errors, warnings, System.currentTimeMillis()
        );
        
        return lastIntegrityCheck;
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Diagnostic Configuration Methods
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Enable or disable diagnostic collection.
     * 
     * @param enabled Whether to enable diagnostics
     */
    public void setDiagnosticsEnabled(boolean enabled) {
        this.diagnosticsEnabled = enabled;
        recordDebugLog("DIAGNOSTIC_SERVICE", "INFO", 
                      "Diagnostics " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Enable or disable input event tracing.
     * 
     * @param enabled Whether to enable input tracing
     */
    public void setInputTracingEnabled(boolean enabled) {
        this.inputTracingEnabled = enabled;
        recordDebugLog("DIAGNOSTIC_SERVICE", "INFO", 
                      "Input tracing " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Enable or disable performance monitoring.
     * 
     * @param enabled Whether to enable performance monitoring
     */
    public void setPerformanceMonitoringEnabled(boolean enabled) {
        this.performanceMonitoringEnabled = enabled;
        recordDebugLog("DIAGNOSTIC_SERVICE", "INFO", 
                      "Performance monitoring " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Configure all diagnostic features at once.
     * 
     * @param diagnostics Enable general diagnostics
     * @param inputTracing Enable input event tracing  
     * @param performance Enable performance monitoring
     */
    public void configureAllFeatures(boolean diagnostics, boolean inputTracing, boolean performance) {
        setDiagnosticsEnabled(diagnostics);
        setInputTracingEnabled(inputTracing);
        setPerformanceMonitoringEnabled(performance);
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Data Access and Reporting Methods
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Get current system health status.
     * 
     * @return Current system health
     */
    public SystemHealthStatus getSystemHealth() {
        return systemHealth;
    }
    
    /**
     * Get comprehensive diagnostic report.
     * 
     * @return Diagnostic report containing all collected data
     */
    public DiagnosticReport generateDiagnosticReport() {
        return new DiagnosticReport(
            systemHealth,
            new ArrayList<>(componentStatuses.values()),
            new ArrayList<>(performanceMeasurements),
            new ArrayList<>(memorySnapshots),
            new ArrayList<>(inputTraceEvents),
            new ArrayList<>(stateTransitionEvents),
            new ArrayList<>(debugLogEntries),
            lastIntegrityCheck
        );
    }
    
    /**
     * Clear all performance statistics.
     */
    public void clearPerformanceStatistics() {
        performanceMeasurements.clear();
        memorySnapshots.clear();
        activeTimers.clear();
        recordDebugLog("DIAGNOSTIC_SERVICE", "INFO", "Performance statistics cleared");
    }
    
    /**
     * Clear all input event traces.
     */
    public void clearInputEventTrace() {
        inputTraceEvents.clear();
        recordDebugLog("DIAGNOSTIC_SERVICE", "INFO", "Input event trace cleared");
    }
    
    /**
     * Clear all diagnostic data.
     */
    public void clearAllDiagnosticData() {
        clearPerformanceStatistics();
        clearInputEventTrace();
        stateTransitionEvents.clear();
        debugLogEntries.clear();
        componentStatuses.clear();
        systemHealth = SystemHealthStatus.UNKNOWN;
        lastIntegrityCheck = null;
        recordDebugLog("DIAGNOSTIC_SERVICE", "INFO", "All diagnostic data cleared");
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Query Methods
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Check if diagnostics are enabled.
     * 
     * @return true if diagnostics are enabled
     */
    public boolean isDiagnosticsEnabled() {
        return diagnosticsEnabled;
    }
    
    /**
     * Check if input tracing is enabled.
     * 
     * @return true if input tracing is enabled
     */
    public boolean isInputTracingEnabled() {
        return inputTracingEnabled;
    }
    
    /**
     * Check if performance monitoring is enabled.
     * 
     * @return true if performance monitoring is enabled
     */
    public boolean isPerformanceMonitoringEnabled() {
        return performanceMonitoringEnabled;
    }
    
    /**
     * Get number of recorded input events.
     * 
     * @return Number of input trace events
     */
    public int getInputEventCount() {
        return inputTraceEvents.size();
    }
    
    /**
     * Get number of performance measurements.
     * 
     * @return Number of performance measurements
     */
    public int getPerformanceMeasurementCount() {
        return performanceMeasurements.size();
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Data Transfer Objects
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * System health status enumeration.
     */
    public enum SystemHealthStatus {
        HEALTHY,    // All components are functioning normally
        DEGRADED,   // Some issues but system is functional
        UNHEALTHY,  // Significant issues affecting functionality
        UNKNOWN     // Health status has not been determined
    }
    
    /**
     * Performance measurement data.
     */
    public static class PerformanceMeasurement {
        public final String operationName;
        public final long durationNanos;
        public final long timestamp;
        
        public PerformanceMeasurement(String operationName, long durationNanos, long timestamp) {
            this.operationName = operationName;
            this.durationNanos = durationNanos;
            this.timestamp = timestamp;
        }
        
        public double getDurationMillis() {
            return durationNanos / 1_000_000.0;
        }
    }
    
    /**
     * Memory usage snapshot data.
     */
    public static class MemorySnapshot {
        public final String context;
        public final long usedMemoryBytes;
        public final long totalMemoryBytes;
        public final long maxMemoryBytes;
        public final long timestamp;
        
        public MemorySnapshot(String context, long usedMemoryBytes, long totalMemoryBytes, 
                            long maxMemoryBytes, long timestamp) {
            this.context = context;
            this.usedMemoryBytes = usedMemoryBytes;
            this.totalMemoryBytes = totalMemoryBytes;
            this.maxMemoryBytes = maxMemoryBytes;
            this.timestamp = timestamp;
        }
        
        public double getUsagePercentage() {
            return (double) usedMemoryBytes / maxMemoryBytes * 100.0;
        }
    }
    
    /**
     * Input trace event data.
     */
    public static class InputTraceEvent {
        public final String eventType;
        public final String details;
        public final long timestamp;
        
        public InputTraceEvent(String eventType, String details, long timestamp) {
            this.eventType = eventType;
            this.details = details;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * State transition event data.
     */
    public static class StateTransitionEvent {
        public final String systemName;
        public final String fromState;
        public final String toState;
        public final long timestamp;
        
        public StateTransitionEvent(String systemName, String fromState, String toState, long timestamp) {
            this.systemName = systemName;
            this.fromState = fromState;
            this.toState = toState;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Debug log entry data.
     */
    public static class DebugLogEntry {
        public final String category;
        public final String level;
        public final String message;
        public final long timestamp;
        
        public DebugLogEntry(String category, String level, String message, long timestamp) {
            this.category = category;
            this.level = level;
            this.message = message;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Component status information.
     */
    public static class ComponentStatus {
        public final String componentName;
        public final boolean isHealthy;
        public final String statusMessage;
        public final long timestamp;
        
        public ComponentStatus(String componentName, boolean isHealthy, String statusMessage, long timestamp) {
            this.componentName = componentName;
            this.isHealthy = isHealthy;
            this.statusMessage = statusMessage;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * System integrity check result.
     */
    public static class SystemIntegrityResult {
        public final boolean isHealthy;
        public final List<String> errors;
        public final List<String> warnings;
        public final long timestamp;
        
        public SystemIntegrityResult(boolean isHealthy, List<String> errors, List<String> warnings, long timestamp) {
            this.isHealthy = isHealthy;
            this.errors = new ArrayList<>(errors);
            this.warnings = new ArrayList<>(warnings);
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Comprehensive diagnostic report.
     */
    public static class DiagnosticReport {
        public final SystemHealthStatus systemHealth;
        public final List<ComponentStatus> componentStatuses;
        public final List<PerformanceMeasurement> performanceMeasurements;
        public final List<MemorySnapshot> memorySnapshots;
        public final List<InputTraceEvent> inputTraceEvents;
        public final List<StateTransitionEvent> stateTransitionEvents;
        public final List<DebugLogEntry> debugLogEntries;
        public final SystemIntegrityResult lastIntegrityCheck;
        
        public DiagnosticReport(SystemHealthStatus systemHealth, List<ComponentStatus> componentStatuses,
                              List<PerformanceMeasurement> performanceMeasurements, List<MemorySnapshot> memorySnapshots,
                              List<InputTraceEvent> inputTraceEvents, List<StateTransitionEvent> stateTransitionEvents,
                              List<DebugLogEntry> debugLogEntries, SystemIntegrityResult lastIntegrityCheck) {
            this.systemHealth = systemHealth;
            this.componentStatuses = new ArrayList<>(componentStatuses);
            this.performanceMeasurements = new ArrayList<>(performanceMeasurements);
            this.memorySnapshots = new ArrayList<>(memorySnapshots);
            this.inputTraceEvents = new ArrayList<>(inputTraceEvents);
            this.stateTransitionEvents = new ArrayList<>(stateTransitionEvents);
            this.debugLogEntries = new ArrayList<>(debugLogEntries);
            this.lastIntegrityCheck = lastIntegrityCheck;
        }
    }
}
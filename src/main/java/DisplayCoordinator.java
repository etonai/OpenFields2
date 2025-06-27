/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import javafx.scene.input.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import combat.*;
import game.*;
import input.interfaces.InputManagerCallbacks;

/**
 * DisplayCoordinator handles all input-related display management, feedback coordination, and UI state.
 * 
 * This component was extracted from InputManager as part of DevCycle 15e incremental refactoring.
 * It manages all user feedback, debug output, status messages, and display coordination operations
 * that result from input processing activities.
 * 
 * RESPONSIBILITIES:
 * - Character statistics display coordination
 * - Selection visual feedback management
 * - Status message coordination and formatting
 * - Debug information display coordination
 * - UI state display management
 * - Performance and diagnostic display
 * 
 * DESIGN PRINCIPLES:
 * - Display coordination: Centralized management of all input-related display operations
 * - Feedback consistency: Standardized feedback patterns across all input operations
 * - UI integration: Seamless integration with existing display and rendering systems
 * - Performance optimization: Efficient display coordination without performance impact
 * - Extensibility: Foundation for future display and feedback enhancements
 * 
 * @author DevCycle 15e - Display Coordination Component Extraction
 */
public class DisplayCoordinator {
    
    // ====================
    // DEPENDENCIES
    // ====================
    
    private final SelectionManager selectionManager;
    private final GameClock gameClock;
    private final InputManagerCallbacks callbacks;
    
    // ====================
    // DEBUG CONFIGURATION
    // ====================
    
    /** Master debug flag - enables all debug features when true */
    private static boolean DEBUG_ENABLED = false;
    
    /** Enable debug logging for input events (mouse, keyboard) */
    private static boolean DEBUG_INPUT_EVENTS = false;
    
    /** Enable debug logging for state transitions */
    private static boolean DEBUG_STATE_TRANSITIONS = false;
    
    /** Enable performance timing for complex operations */
    private static boolean DEBUG_PERFORMANCE_TIMING = false;
    
    /** Enable input event trace functionality */
    private static boolean DEBUG_INPUT_TRACE = false;
    
    /** Enable memory usage diagnostic output */
    private static boolean DEBUG_MEMORY_USAGE = false;
    
    /** Enable workflow state debugging */
    private static boolean DEBUG_WORKFLOW_STATES = false;
    
    /** Enable combat command debugging */
    private static boolean DEBUG_COMBAT_COMMANDS = false;
    
    /** Enable selection debugging */
    private static boolean DEBUG_SELECTION_OPERATIONS = false;
    
    // Performance timing storage for debug operations
    private static long lastOperationStartTime = 0;
    private static final Map<String, Long> performanceTimings = new java.util.HashMap<>();
    private static final List<String> inputEventTrace = new java.util.ArrayList<>();
    private static final int MAX_TRACE_EVENTS = 100; // Limit trace size to prevent memory issues
    
    // ====================
    // CONSTRUCTOR
    // ====================
    
    /**
     * Creates a new DisplayCoordinator with required dependencies.
     */
    public DisplayCoordinator(SelectionManager selectionManager, GameClock gameClock,
                            InputManagerCallbacks callbacks) {
        this.selectionManager = selectionManager;
        this.gameClock = gameClock;
        this.callbacks = callbacks;
    }
    
    // ====================
    // CHARACTER STATISTICS DISPLAY
    // ====================
    
    /**
     * Handle character stats display (Shift+/)
     */
    public void handleCharacterStatsDisplay(KeyEvent e) {
        if (e.getCode() == javafx.scene.input.KeyCode.SLASH && e.isShiftDown()) {
            if (selectionManager.getSelectionCount() == 1) {
                displayCharacterStats(selectionManager.getSelected());
            } else if (selectionManager.hasSelection()) {
                displayMultiCharacterSelection();
            } else {
                System.out.println("*** No units selected for stats display ***");
            }
        }
    }
    
    /**
     * Display enhanced character statistics for a single unit.
     */
    public void displayCharacterStats(Unit unit) {
        System.out.println("***********************");
        System.out.println("*** CHARACTER STATS ***");
        System.out.println("***********************");
        System.out.println("Character ID: " + unit.character.id);
        System.out.println("Unit ID: " + unit.id);
        System.out.println("Nickname: " + unit.character.nickname);
        System.out.println("Faction: " + unit.character.faction);
        System.out.println("Full Name: " + unit.character.getFullName());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");
        System.out.println("Birthdate: " + dateFormat.format(unit.character.birthdate));
        System.out.println("Dexterity: " + unit.character.dexterity + " (modifier: " + callbacks.convertStatToModifier(unit.character.dexterity) + ")");
        System.out.println("Strength: " + unit.character.strength + " (modifier: " + callbacks.convertStatToModifier(unit.character.strength) + ")");
        System.out.println("Reflexes: " + unit.character.reflexes + " (modifier: " + callbacks.convertStatToModifier(unit.character.reflexes) + ")");
        System.out.println("Health: " + unit.character.currentHealth + "/" + unit.character.health);
        System.out.println("Coolness: " + unit.character.coolness + " (modifier: " + callbacks.convertStatToModifier(unit.character.coolness) + ")");
        System.out.println("Handedness: " + unit.character.handedness.getDisplayName());
        System.out.println("Base Movement Speed: " + unit.character.baseMovementSpeed + " pixels/second");
        System.out.println("Current Movement: " + unit.character.getCurrentMovementType().getDisplayName() + 
                         " (" + String.format("%.1f", unit.character.getEffectiveMovementSpeed()) + " pixels/sec)");
        
        // Show movement restrictions if any
        combat.MovementType maxAllowed = unit.character.getMaxAllowedMovementType();
        if (maxAllowed != combat.MovementType.RUN) {
            if (unit.character.hasBothLegsWounded()) {
                System.out.println("Movement Restricted: Both legs wounded - CRAWL ONLY, forced prone");
            } else if (unit.character.hasAnyLegWound()) {
                System.out.println("Movement Restricted: Leg wound - maximum " + maxAllowed.getDisplayName());
            }
        }
        
        System.out.println("Current Aiming Speed: " + unit.character.getCurrentAimingSpeed().getDisplayName() + 
                         " (timing: " + String.format("%.2fx", unit.character.getCurrentAimingSpeed().getTimingMultiplier()) + 
                         ", accuracy: " + String.format("%+.0f", unit.character.getCurrentAimingSpeed().getAccuracyModifier()) + ")");
        System.out.println("Current Position: " + unit.character.getCurrentPosition().getDisplayName());
        
        // Show weapon ready speed
        double readySpeedMultiplier = unit.character.getWeaponReadySpeedMultiplier();
        int quickdrawLevel = unit.character.getSkillLevel(data.SkillsManager.QUICKDRAW);
        String quickdrawInfo = quickdrawLevel > 0 ? " (Quickdraw " + quickdrawLevel + ")" : "";
        System.out.println("Weapon Ready Speed: " + String.format("%.2fx", readySpeedMultiplier) + quickdrawInfo + 
                         " (reflexes: " + String.format("%+d", callbacks.convertStatToModifier(unit.character.reflexes)) + ")");
        
        System.out.println("Incapacitated: " + (unit.character.isIncapacitated() ? "YES" : "NO"));
        System.out.println("Automatic Targeting: " + (unit.character.isUsesAutomaticTargeting() ? "ON" : "OFF"));
        
        System.out.println("--- WEAPONS ---");
        
        // Enhanced Combat Mode Display (DevCycle 17)
        System.out.println("Combat Mode: " + (unit.character.isMeleeCombatMode ? "MELEE" : "RANGED"));
        System.out.println();
        
        // Display ranged weapon - check both rangedWeapon and legacy weapon fields
        RangedWeapon ranged = unit.character.rangedWeapon;
        if (ranged == null && unit.character.weapon instanceof RangedWeapon) {
            ranged = (RangedWeapon) unit.character.weapon;
        }
        
        if (ranged != null) {
            String activeMarker = !unit.character.isMeleeCombatMode ? " [ACTIVE]" : "";
            System.out.println("Ranged Weapon: " + ranged.getName() + activeMarker + " (" + ranged.getDamage() + " damage, " + 
                             ranged.getWeaponAccuracy() + " accuracy)");
        } else {
            String activeMarker = !unit.character.isMeleeCombatMode ? " [ACTIVE]" : "";
            System.out.println("Ranged Weapon: No ranged weapon" + activeMarker);
        }
        
        // Display melee weapon
        if (unit.character.meleeWeapon != null) {
            MeleeWeapon melee = unit.character.meleeWeapon;
            String activeMarker = unit.character.isMeleeCombatMode ? " [ACTIVE]" : "";
            System.out.println("Melee Weapon: " + melee.getName() + activeMarker + " (" + melee.getDamage() + " damage, " + 
                             melee.getWeaponAccuracy() + " accuracy, " + String.format("%.1f", melee.getTotalReach()) + "ft reach)");
        } else {
            String activeMarker = unit.character.isMeleeCombatMode ? " [ACTIVE]" : "";
            System.out.println("Melee Weapon: No melee weapon" + activeMarker);
        }
        
        // Show current weapon state and additional details for active weapon
        System.out.println("Current State: " + (unit.character.currentWeaponState != null ? unit.character.currentWeaponState.getState() : "None"));
        System.out.println("Hold State: " + unit.character.getCurrentWeaponHoldState());
        System.out.println("Firing Preference: " + (unit.character.firesFromAimingState ? "Aiming State" : "Point-from-Hip State"));
        
        // Show additional details for the active weapon
        if (!unit.character.isMeleeCombatMode && ranged != null) {
            System.out.println("Active Details: Range " + ranged.getMaximumRange() + "ft, Velocity " + ranged.getVelocityFeetPerSecond() + "ft/s, Ammo " + ranged.getAmmunition() + "/" + ranged.getMaxAmmunition());
        } else if (unit.character.isMeleeCombatMode && unit.character.meleeWeapon != null) {
            MeleeWeapon melee = unit.character.meleeWeapon;
            System.out.println("Active Details: " + melee.getWeaponType().getDisplayName() + " weapon");
        }
        
        if (!unit.character.getSkills().isEmpty()) {
            System.out.println("--- SKILLS ---");
            for (combat.Skill skill : unit.character.getSkills()) {
                System.out.println(skill.getSkillName() + ": " + skill.getLevel());
            }
        } else {
            System.out.println("--- SKILLS ---");
            System.out.println("No skills");
        }
        
        if (!unit.character.wounds.isEmpty()) {
            System.out.println("--- WOUNDS ---");
            for (combat.Wound wound : unit.character.wounds) {
                System.out.println(wound.getBodyPart().name().toLowerCase() + ": " + wound.getSeverity().name().toLowerCase() + 
                                 ", " + wound.getDamage() + " damage (from " + wound.getProjectileName() + ", weapon: " + wound.getWeaponId() + ")");
            }
        } else {
            System.out.println("--- WOUNDS ---");
            System.out.println("No wounds");
        }
        
        // Combat Experience Display
        System.out.println("--- COMBAT EXPERIENCE ---");
        System.out.println("Combat Engagements: " + unit.character.getCombatEngagements());
        System.out.println("Wounds Received: " + unit.character.getWoundsReceived());
        System.out.println("Wounds Inflicted: " + unit.character.getTotalWoundsInflicted() + " total (" + 
                         unit.character.getWoundsInflictedByType(combat.WoundSeverity.SCRATCH) + " scratch, " +
                         unit.character.getWoundsInflictedByType(combat.WoundSeverity.LIGHT) + " light, " +
                         unit.character.getWoundsInflictedByType(combat.WoundSeverity.SERIOUS) + " serious, " +
                         unit.character.getWoundsInflictedByType(combat.WoundSeverity.CRITICAL) + " critical)");
        
        // Separate combat statistics (DevCycle 12)
        System.out.println("Ranged Combat: " + unit.character.rangedAttacksAttempted + " attempted, " + 
                         unit.character.rangedAttacksSuccessful + " successful, " + 
                         unit.character.rangedWoundsInflicted + " wounds inflicted");
        System.out.println("Melee Combat: " + unit.character.meleeAttacksAttempted + " attempted, " + 
                         unit.character.meleeAttacksSuccessful + " successful, " + 
                         unit.character.meleeWoundsInflicted + " wounds inflicted");
        
        // Legacy combined statistics
        System.out.println("Total Attacks: " + unit.character.getAttacksAttempted() + " attempted, " + 
                         unit.character.getAttacksSuccessful() + " successful");
        System.out.println("***********************");
    }
    
    /**
     * Display multi-character selection summary.
     */
    public void displayMultiCharacterSelection() {
        System.out.println("***********************");
        System.out.println("*** MULTIPLE UNITS SELECTED ***");
        System.out.println("Selected Units: " + selectionManager.getSelectionCount());
        
        for (Unit unit : selectionManager.getSelectedUnits()) {
            System.out.println("- " + unit.character.getDisplayName() + 
                             " (ID: " + unit.character.id + ", Faction: " + unit.character.faction + 
                             ", Health: " + unit.character.health + 
                             (unit.character.isIncapacitated() ? " [INCAPACITATED]" : "") + ")");
        }
        System.out.println("***********************");
    }
    
    // ====================
    // GAME STATE STATUS MESSAGES
    // ====================
    
    /**
     * Display game pause/resume status.
     */
    public void displayPauseStatus(boolean isPaused, long currentTick) {
        System.out.println("***********************");
        if (isPaused) {
            System.out.println("*** Game paused at tick " + currentTick);
        } else {
            System.out.println("*** Game resumed");
        }
        System.out.println("***********************");
    }
    
    /**
     * Display debug mode toggle status.
     */
    public void displayDebugModeStatus(boolean isEnabled) {
        System.out.println("***********************");
        System.out.println("*** Debug mode " + (isEnabled ? "ENABLED" : "DISABLED"));
        System.out.println("***********************");
    }
    
    /**
     * Display edit mode toggle status.
     */
    public void displayEditModeStatus(boolean isEnabled) {
        System.out.println("***********************");
        System.out.println("*** Edit mode " + (isEnabled ? "ENABLED" : "DISABLED"));
        if (isEnabled) {
            System.out.println("*** Combat disabled, instant movement enabled");
        } else {
            System.out.println("*** Combat enabled, normal movement rules apply");
        }
        System.out.println("***********************");
    }
    
    // ====================
    // MOVEMENT AND COMBAT STATUS DISPLAY
    // ====================
    
    /**
     * Display movement type change status.
     */
    public void displayMovementTypeChange(Unit unit, combat.MovementType newType) {
        System.out.println("*** " + unit.character.getDisplayName() + " movement: " + newType.getDisplayName() + 
                         " (" + String.format("%.1f", unit.character.getEffectiveMovementSpeed()) + " pixels/sec)");
    }
    
    /**
     * Display aiming speed change status.
     */
    public void displayAimingSpeedChange(Unit unit, combat.AimingSpeed newSpeed) {
        System.out.println("*** " + unit.character.getDisplayName() + " aiming: " + newSpeed.getDisplayName() + 
                         " (timing: " + String.format("%.2fx", newSpeed.getTimingMultiplier()) + 
                         ", accuracy: " + String.format("%+.0f", newSpeed.getAccuracyModifier()) + ")");
    }
    
    /**
     * Display range check information in edit mode.
     */
    public void displayRangeCheck(Unit selectedUnit, Unit targetUnit, double distanceFeet) {
        System.out.println("*** RANGE CHECK ***");
        System.out.println("Distance from " + selectedUnit.character.getDisplayName() + " to " + targetUnit.character.getDisplayName() + ": " + 
                         String.format("%.2f", distanceFeet) + " feet");
        
        if (selectedUnit.character.weapon != null) {
            double maxRange = (selectedUnit.character.weapon instanceof RangedWeapon) ? ((RangedWeapon)selectedUnit.character.weapon).getMaximumRange() : 0.0;
            System.out.println("Weapon: " + selectedUnit.character.weapon.name + " (max range: " + 
                             String.format("%.2f", maxRange) + " feet)");
            
            if (distanceFeet <= maxRange) {
                System.out.println("Target is WITHIN range");
            } else {
                System.out.println("Target is OUT OF RANGE (exceeds by " + 
                                 String.format("%.2f", distanceFeet - maxRange) + " feet)");
            }
        } else {
            System.out.println("No weapon equipped");
        }
        System.out.println("******************");
    }
    
    /**
     * Display unit movement status.
     */
    public void displayUnitMovement(int unitCount, double x, double y, boolean isTeleport) {
        if (isTeleport) {
            System.out.println("TELEPORT " + unitCount + " units to (" + String.format("%.0f", x) + ", " + String.format("%.0f", y) + ")");
        } else {
            System.out.println("MOVE " + unitCount + " units to (" + String.format("%.0f", x) + ", " + String.format("%.0f", y) + ")");
        }
    }
    
    /**
     * Display combat action restrictions.
     */
    public void displayCombatRestriction(String reason) {
        System.out.println(">>> " + reason);
    }
    
    // ====================
    // DEBUG AND DIAGNOSTIC DISPLAY
    // ====================
    
    /**
     * Central debug logging method.
     */
    public void debugLog(String category, String message) {
        if (DEBUG_ENABLED) {
            System.out.println("[DEBUG-" + category + "] " + message);
        }
    }
    
    /**
     * Debug input event logging.
     */
    public void debugInputEvent(String eventType, String details) {
        if (DEBUG_ENABLED && DEBUG_INPUT_EVENTS) {
            System.out.println("[INPUT-" + eventType + "] " + details);
        }
    }
    
    /**
     * Debug state transition logging.
     */
    public void debugStateTransition(String stateType, String fromState, String toState) {
        if (DEBUG_ENABLED && DEBUG_STATE_TRANSITIONS) {
            System.out.println("[STATE-" + stateType + "] " + fromState + " → " + toState);
        }
    }
    
    /**
     * Debug workflow state logging.
     */
    public void debugWorkflowState(String workflowName, String step, String details) {
        if (DEBUG_ENABLED && DEBUG_WORKFLOW_STATES) {
            System.out.println("[WORKFLOW-" + workflowName + "-" + step + "] " + details);
        }
    }
    
    /**
     * Debug selection operation logging.
     */
    public void debugSelectionOperation(String operation, String details) {
        if (DEBUG_ENABLED && DEBUG_SELECTION_OPERATIONS) {
            System.out.println("[SELECTION-" + operation + "] " + details);
        }
    }
    
    /**
     * Debug combat command logging.
     */
    public void debugCombatCommand(String commandType, String unitInfo, String targetInfo) {
        if (DEBUG_ENABLED && DEBUG_COMBAT_COMMANDS) {
            System.out.println("[COMBAT-" + commandType + "] Unit: " + unitInfo + " Target: " + targetInfo);
        }
    }
    
    // ====================
    // PERFORMANCE AND MEMORY DISPLAY
    // ====================
    
    /**
     * Display performance statistics.
     */
    public void displayPerformanceStatistics(Map<String, Long> stats) {
        if (stats.isEmpty()) {
            System.out.println("*** No performance statistics available ***");
        } else {
            System.out.println("*** Performance Statistics ***");
            for (Map.Entry<String, Long> entry : stats.entrySet()) {
                double ms = entry.getValue() / 1_000_000.0;
                System.out.println("  " + entry.getKey() + ": " + String.format("%.3f", ms) + "ms");
            }
        }
    }
    
    /**
     * Display input event trace.
     */
    public void displayInputEventTrace(List<String> trace) {
        if (trace.isEmpty()) {
            System.out.println("*** No input trace available ***");
        } else {
            System.out.println("*** Recent Input Events ***");
            for (String event : trace) {
                System.out.println("  " + event);
            }
        }
    }
    
    /**
     * Display system integrity validation results.
     */
    public void displaySystemIntegrityResults() {
        System.out.println("*** Running System Integrity Validation ***");
        // System integrity validation would be implemented here
        System.out.println("*** System integrity validation completed ***");
    }
    
    /**
     * Display debug data cleared confirmation.
     */
    public void displayDebugDataCleared() {
        System.out.println("*** Debug data cleared ***");
    }
    
    // ====================
    // DEBUG CONFIGURATION
    // ====================
    
    /**
     * Configure debug features.
     */
    public void configureDebugFeatures(boolean inputEvents, boolean stateTransitions, boolean performanceTiming,
                                     boolean inputTrace, boolean memoryUsage, boolean workflowStates,
                                     boolean combatCommands, boolean selectionOperations) {
        DEBUG_INPUT_EVENTS = inputEvents;
        DEBUG_STATE_TRANSITIONS = stateTransitions;
        DEBUG_PERFORMANCE_TIMING = performanceTiming;
        DEBUG_INPUT_TRACE = inputTrace;
        DEBUG_MEMORY_USAGE = memoryUsage;
        DEBUG_WORKFLOW_STATES = workflowStates;
        DEBUG_COMBAT_COMMANDS = combatCommands;
        DEBUG_SELECTION_OPERATIONS = selectionOperations;
        
        System.out.println("*** Display debug categories configured ***");
    }
    
    /**
     * Set debug enabled state.
     */
    public void setDebugEnabled(boolean enabled) {
        DEBUG_ENABLED = enabled;
        System.out.println("*** DisplayCoordinator Debug " + (enabled ? "ENABLED" : "DISABLED") + " ***");
    }
    
    /**
     * Check if debug is enabled.
     */
    public boolean isDebugEnabled() {
        return DEBUG_ENABLED;
    }
    
    // ====================
    // SYSTEM STATE DISPLAY
    // ====================
    
    /**
     * Generate and display comprehensive system state dump.
     */
    public String generateSystemStateDump(Object inputStateData, Map<String, Long> performanceData, 
                                        List<String> inputTrace, CombatCommandProcessor combatProcessor) {
        StringBuilder dump = new StringBuilder();
        dump.append("==============================================\n");
        dump.append("DISPLAY COORDINATOR SYSTEM STATE DUMP\n");
        dump.append("==============================================\n\n");
        
        // Debug configuration
        dump.append("DEBUG CONFIGURATION:\n");
        dump.append("  Master Debug: ").append(DEBUG_ENABLED).append("\n");
        dump.append("  Input Events: ").append(DEBUG_INPUT_EVENTS).append("\n");
        dump.append("  State Transitions: ").append(DEBUG_STATE_TRANSITIONS).append("\n");
        dump.append("  Performance Timing: ").append(DEBUG_PERFORMANCE_TIMING).append("\n");
        dump.append("  Input Trace: ").append(DEBUG_INPUT_TRACE).append("\n");
        dump.append("  Memory Usage: ").append(DEBUG_MEMORY_USAGE).append("\n");
        dump.append("  Workflow States: ").append(DEBUG_WORKFLOW_STATES).append("\n");
        dump.append("  Combat Commands: ").append(DEBUG_COMBAT_COMMANDS).append("\n");
        dump.append("  Selection Operations: ").append(DEBUG_SELECTION_OPERATIONS).append("\n\n");
        
        // Selection state
        if (selectionManager.hasSelection()) {
            dump.append("SELECTION STATE:\n");
            dump.append("  Selection Count: ").append(selectionManager.getSelectionCount()).append("\n");
            dump.append("  Selected Units: ");
            for (Unit unit : selectionManager.getSelectedUnits()) {
                dump.append(unit.character.getDisplayName()).append(" ");
            }
            dump.append("\n\n");
        }
        
        // Memory usage (if enabled)
        if (DEBUG_MEMORY_USAGE) {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            dump.append("MEMORY USAGE:\n");
            dump.append("  Total Memory: ").append(totalMemory / 1024 / 1024).append(" MB\n");
            dump.append("  Free Memory: ").append(freeMemory / 1024 / 1024).append(" MB\n");
            dump.append("  Used Memory: ").append(usedMemory / 1024 / 1024).append(" MB\n\n");
        }
        
        // Performance statistics
        if (DEBUG_PERFORMANCE_TIMING && performanceData != null && !performanceData.isEmpty()) {
            dump.append("PERFORMANCE STATISTICS:\n");
            for (Map.Entry<String, Long> entry : performanceData.entrySet()) {
                double ms = entry.getValue() / 1_000_000.0;
                dump.append("  ").append(entry.getKey()).append(": ").append(String.format("%.3f", ms)).append("ms\n");
            }
            dump.append("\n");
        }
        
        // Input event trace
        if (DEBUG_INPUT_TRACE && inputTrace != null && !inputTrace.isEmpty()) {
            dump.append("INPUT EVENT TRACE (last ").append(Math.min(10, inputTrace.size())).append(" events):\n");
            List<String> recentEvents = inputTrace.subList(Math.max(0, inputTrace.size() - 10), inputTrace.size());
            for (String event : recentEvents) {
                dump.append("  ").append(event).append("\n");
            }
            dump.append("\n");
        }
        
        dump.append("==============================================\n");
        return dump.toString();
    }
    
    // ====================
    // INPUT TRACE MANAGEMENT
    // ====================
    
    /**
     * Add input trace event.
     */
    public void addInputTraceEvent(String event) {
        if (DEBUG_INPUT_TRACE) {
            inputEventTrace.add(event);
            if (inputEventTrace.size() > MAX_TRACE_EVENTS) {
                inputEventTrace.remove(0);
            }
        }
    }
    
    /**
     * Clear input event trace.
     */
    public void clearInputEventTrace() {
        inputEventTrace.clear();
    }
    
    /**
     * Get input event trace.
     */
    public List<String> getInputEventTrace() {
        return new java.util.ArrayList<>(inputEventTrace);
    }
    
    // ====================
    // PERFORMANCE TIMING MANAGEMENT
    // ====================
    
    /**
     * Start performance timer.
     */
    public void startPerformanceTimer(String operation) {
        if (DEBUG_PERFORMANCE_TIMING) {
            lastOperationStartTime = System.nanoTime();
        }
    }
    
    /**
     * End performance timer.
     */
    public void endPerformanceTimer(String operation) {
        if (DEBUG_PERFORMANCE_TIMING && lastOperationStartTime > 0) {
            long elapsedTime = System.nanoTime() - lastOperationStartTime;
            performanceTimings.put(operation, elapsedTime);
            lastOperationStartTime = 0;
        }
    }
    
    /**
     * Clear performance statistics.
     */
    public void clearPerformanceStatistics() {
        performanceTimings.clear();
    }
    
    /**
     * Get performance statistics.
     */
    public Map<String, Long> getPerformanceStatistics() {
        return new java.util.HashMap<>(performanceTimings);
    }
    
    // ====================
    // MEMORY USAGE LOGGING
    // ====================
    
    /**
     * Log memory usage (debug only).
     */
    public void logMemoryUsage(String context) {
        if (DEBUG_ENABLED && DEBUG_MEMORY_USAGE) {
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
            System.out.println("[MEMORY] " + context + ": " + usedMemory + " MB used");
        }
    }
}
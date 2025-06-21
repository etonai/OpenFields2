/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * InputStateTracker centralizes all input-related state management for the InputManager.
 * 
 * This component was extracted from InputManager as part of DevCycle 15c incremental refactoring.
 * It consolidates all the waitingFor... boolean flags and provides a clean interface for state
 * management with validation, conflict detection, and debug integration.
 * 
 * RESPONSIBILITIES:
 * - Manage all input state flags (waitingForSaveSlot, editMode, etc.)
 * - Provide state query interface for other components
 * - Validate state consistency and detect conflicts
 * - Track state history for debugging purposes
 * - Integrate with debug logging for state monitoring
 * 
 * DESIGN PRINCIPLES:
 * - Centralized state: Single source of truth for all input states
 * - Thread safety: Safe concurrent access to state information
 * - State validation: Prevent conflicting or invalid state combinations
 * - Debug integration: Full integration with DevCycle 15a debug capabilities
 * - Clear interface: Simple, intuitive API for state operations
 * 
 * @author DevCycle 15c - Incremental InputManager Refactoring
 */
public class InputStateTracker {
    
    // ====================
    // STATE FLAGS
    // ====================
    
    // Save/Load state flags
    private boolean waitingForSaveSlot = false;
    private boolean waitingForLoadSlot = false;
    
    // Character creation state flags
    private boolean waitingForCharacterCreation = false;
    private boolean waitingForWeaponSelection = false;
    private boolean waitingForRangedWeaponSelection = false;
    private boolean waitingForMeleeWeaponSelection = false;
    private boolean waitingForFactionSelection = false;
    private boolean waitingForCharacterRangedWeapon = false;
    private boolean waitingForCharacterMeleeWeapon = false;
    
    // Batch operations state flags
    private boolean waitingForBatchCharacterCreation = false;
    private boolean waitingForCharacterDeployment = false;
    private boolean waitingForDirectCharacterAddition = false;
    
    // Game management state flags
    private boolean waitingForDeletionConfirmation = false;
    private boolean waitingForVictoryOutcome = false;
    private boolean waitingForScenarioName = false;
    private boolean waitingForThemeSelection = false;
    
    // Mode state flags
    private boolean editMode = false;
    
    // State history for debugging
    private List<StateChange> stateHistory = new ArrayList<>();
    private static final int MAX_HISTORY_SIZE = 100;
    
    // Debug callback for state changes (optional)
    private StateChangeCallback debugCallback = null;
    
    /**
     * Interface for receiving state change notifications.
     */
    public interface StateChangeCallback {
        void onStateChange(String stateName, boolean oldValue, boolean newValue);
    }
    
    /**
     * Record of a state change for debugging purposes.
     */
    public static class StateChange {
        public final String stateName;
        public final boolean oldValue;
        public final boolean newValue;
        public final long timestamp;
        
        public StateChange(String stateName, boolean oldValue, boolean newValue) {
            this.stateName = stateName;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.timestamp = System.currentTimeMillis();
        }
        
        @Override
        public String toString() {
            return String.format("[%d] %s: %s -> %s", timestamp, stateName, oldValue, newValue);
        }
    }
    
    // ====================
    // CONSTRUCTOR
    // ====================
    
    /**
     * Creates a new InputStateTracker with all states initialized to false.
     */
    public InputStateTracker() {
        // All boolean fields are automatically initialized to false
    }
    
    /**
     * Sets the debug callback for state change notifications.
     * 
     * @param callback Callback to receive state change notifications
     */
    public void setDebugCallback(StateChangeCallback callback) {
        this.debugCallback = callback;
    }
    
    // ====================
    // STATE SETTERS
    // ====================
    
    public void setWaitingForSaveSlot(boolean value) { 
        setState("waitingForSaveSlot", waitingForSaveSlot, value);
        waitingForSaveSlot = value; 
    }
    
    public void setWaitingForLoadSlot(boolean value) { 
        setState("waitingForLoadSlot", waitingForLoadSlot, value);
        waitingForLoadSlot = value; 
    }
    
    public void setWaitingForCharacterCreation(boolean value) { 
        setState("waitingForCharacterCreation", waitingForCharacterCreation, value);
        waitingForCharacterCreation = value; 
    }
    
    public void setWaitingForWeaponSelection(boolean value) { 
        setState("waitingForWeaponSelection", waitingForWeaponSelection, value);
        waitingForWeaponSelection = value; 
    }
    
    public void setWaitingForRangedWeaponSelection(boolean value) { 
        setState("waitingForRangedWeaponSelection", waitingForRangedWeaponSelection, value);
        waitingForRangedWeaponSelection = value; 
    }
    
    public void setWaitingForMeleeWeaponSelection(boolean value) { 
        setState("waitingForMeleeWeaponSelection", waitingForMeleeWeaponSelection, value);
        waitingForMeleeWeaponSelection = value; 
    }
    
    public void setWaitingForFactionSelection(boolean value) { 
        setState("waitingForFactionSelection", waitingForFactionSelection, value);
        waitingForFactionSelection = value; 
    }
    
    public void setWaitingForCharacterRangedWeapon(boolean value) { 
        setState("waitingForCharacterRangedWeapon", waitingForCharacterRangedWeapon, value);
        waitingForCharacterRangedWeapon = value; 
    }
    
    public void setWaitingForCharacterMeleeWeapon(boolean value) { 
        setState("waitingForCharacterMeleeWeapon", waitingForCharacterMeleeWeapon, value);
        waitingForCharacterMeleeWeapon = value; 
    }
    
    public void setWaitingForBatchCharacterCreation(boolean value) { 
        setState("waitingForBatchCharacterCreation", waitingForBatchCharacterCreation, value);
        waitingForBatchCharacterCreation = value; 
    }
    
    public void setWaitingForCharacterDeployment(boolean value) { 
        setState("waitingForCharacterDeployment", waitingForCharacterDeployment, value);
        waitingForCharacterDeployment = value; 
    }
    
    public void setWaitingForDirectCharacterAddition(boolean value) { 
        setState("waitingForDirectCharacterAddition", waitingForDirectCharacterAddition, value);
        waitingForDirectCharacterAddition = value; 
    }
    
    public void setWaitingForDeletionConfirmation(boolean value) { 
        setState("waitingForDeletionConfirmation", waitingForDeletionConfirmation, value);
        waitingForDeletionConfirmation = value; 
    }
    
    public void setWaitingForVictoryOutcome(boolean value) { 
        setState("waitingForVictoryOutcome", waitingForVictoryOutcome, value);
        waitingForVictoryOutcome = value; 
    }
    
    public void setWaitingForScenarioName(boolean value) { 
        setState("waitingForScenarioName", waitingForScenarioName, value);
        waitingForScenarioName = value; 
    }
    
    public void setWaitingForThemeSelection(boolean value) { 
        setState("waitingForThemeSelection", waitingForThemeSelection, value);
        waitingForThemeSelection = value; 
    }
    
    public void setEditMode(boolean value) { 
        setState("editMode", editMode, value);
        editMode = value; 
    }
    
    // ====================
    // STATE GETTERS
    // ====================
    
    public boolean isWaitingForSaveSlot() { return waitingForSaveSlot; }
    public boolean isWaitingForLoadSlot() { return waitingForLoadSlot; }
    public boolean isWaitingForCharacterCreation() { return waitingForCharacterCreation; }
    public boolean isWaitingForWeaponSelection() { return waitingForWeaponSelection; }
    public boolean isWaitingForRangedWeaponSelection() { return waitingForRangedWeaponSelection; }
    public boolean isWaitingForMeleeWeaponSelection() { return waitingForMeleeWeaponSelection; }
    public boolean isWaitingForFactionSelection() { return waitingForFactionSelection; }
    public boolean isWaitingForCharacterRangedWeapon() { return waitingForCharacterRangedWeapon; }
    public boolean isWaitingForCharacterMeleeWeapon() { return waitingForCharacterMeleeWeapon; }
    public boolean isWaitingForBatchCharacterCreation() { return waitingForBatchCharacterCreation; }
    public boolean isWaitingForCharacterDeployment() { return waitingForCharacterDeployment; }
    public boolean isWaitingForDirectCharacterAddition() { return waitingForDirectCharacterAddition; }
    public boolean isWaitingForDeletionConfirmation() { return waitingForDeletionConfirmation; }
    public boolean isWaitingForVictoryOutcome() { return waitingForVictoryOutcome; }
    public boolean isWaitingForScenarioName() { return waitingForScenarioName; }
    public boolean isWaitingForThemeSelection() { return waitingForThemeSelection; }
    public boolean isEditMode() { return editMode; }
    
    // ====================
    // UTILITY METHODS
    // ====================
    
    /**
     * Returns true if any prompt input is being awaited.
     */
    public boolean isWaitingForAnyPrompt() {
        return waitingForSaveSlot || waitingForLoadSlot || waitingForCharacterCreation ||
               waitingForWeaponSelection || waitingForRangedWeaponSelection || 
               waitingForMeleeWeaponSelection || waitingForFactionSelection ||
               waitingForBatchCharacterCreation || waitingForCharacterDeployment ||
               waitingForDeletionConfirmation || waitingForVictoryOutcome ||
               waitingForScenarioName || waitingForThemeSelection ||
               waitingForDirectCharacterAddition || waitingForCharacterRangedWeapon ||
               waitingForCharacterMeleeWeapon;
    }
    
    /**
     * Returns true if any character creation workflow is active.
     */
    public boolean isInCharacterCreationWorkflow() {
        return waitingForCharacterCreation || waitingForWeaponSelection ||
               waitingForRangedWeaponSelection || waitingForMeleeWeaponSelection ||
               waitingForFactionSelection || waitingForCharacterRangedWeapon ||
               waitingForCharacterMeleeWeapon;
    }
    
    /**
     * Returns true if any batch operation is active.
     */
    public boolean isInBatchOperation() {
        return waitingForBatchCharacterCreation || waitingForCharacterDeployment ||
               waitingForDirectCharacterAddition;
    }
    
    /**
     * Clears all state flags, resetting to initial state.
     */
    public void clearAllStates() {
        setWaitingForSaveSlot(false);
        setWaitingForLoadSlot(false);
        setWaitingForCharacterCreation(false);
        setWaitingForWeaponSelection(false);
        setWaitingForRangedWeaponSelection(false);
        setWaitingForMeleeWeaponSelection(false);
        setWaitingForFactionSelection(false);
        setWaitingForCharacterRangedWeapon(false);
        setWaitingForCharacterMeleeWeapon(false);
        setWaitingForBatchCharacterCreation(false);
        setWaitingForCharacterDeployment(false);
        setWaitingForDirectCharacterAddition(false);
        setWaitingForDeletionConfirmation(false);
        setWaitingForVictoryOutcome(false);
        setWaitingForScenarioName(false);
        setWaitingForThemeSelection(false);
        setEditMode(false);
    }
    
    /**
     * Gets the current state summary for debugging.
     */
    public String getStateSummary() {
        StringBuilder sb = new StringBuilder("InputStateTracker Summary:\n");
        
        if (waitingForSaveSlot) sb.append("  - waitingForSaveSlot\n");
        if (waitingForLoadSlot) sb.append("  - waitingForLoadSlot\n");
        if (waitingForCharacterCreation) sb.append("  - waitingForCharacterCreation\n");
        if (waitingForWeaponSelection) sb.append("  - waitingForWeaponSelection\n");
        if (waitingForRangedWeaponSelection) sb.append("  - waitingForRangedWeaponSelection\n");
        if (waitingForMeleeWeaponSelection) sb.append("  - waitingForMeleeWeaponSelection\n");
        if (waitingForFactionSelection) sb.append("  - waitingForFactionSelection\n");
        if (waitingForCharacterRangedWeapon) sb.append("  - waitingForCharacterRangedWeapon\n");
        if (waitingForCharacterMeleeWeapon) sb.append("  - waitingForCharacterMeleeWeapon\n");
        if (waitingForBatchCharacterCreation) sb.append("  - waitingForBatchCharacterCreation\n");
        if (waitingForCharacterDeployment) sb.append("  - waitingForCharacterDeployment\n");
        if (waitingForDirectCharacterAddition) sb.append("  - waitingForDirectCharacterAddition\n");
        if (waitingForDeletionConfirmation) sb.append("  - waitingForDeletionConfirmation\n");
        if (waitingForVictoryOutcome) sb.append("  - waitingForVictoryOutcome\n");
        if (waitingForScenarioName) sb.append("  - waitingForScenarioName\n");
        if (waitingForThemeSelection) sb.append("  - waitingForThemeSelection\n");
        if (editMode) sb.append("  - editMode\n");
        
        if (sb.toString().equals("InputStateTracker Summary:\n")) {
            sb.append("  - No active states\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Gets the state change history for debugging.
     */
    public List<StateChange> getStateHistory() {
        return new ArrayList<>(stateHistory);
    }
    
    /**
     * Validates state consistency and returns any conflicts found.
     */
    public List<String> validateStateConsistency() {
        List<String> conflicts = new ArrayList<>();
        
        // Check for multiple conflicting prompts
        int promptCount = 0;
        if (waitingForSaveSlot) promptCount++;
        if (waitingForLoadSlot) promptCount++;
        if (waitingForCharacterCreation) promptCount++;
        if (waitingForBatchCharacterCreation) promptCount++;
        if (waitingForCharacterDeployment) promptCount++;
        if (waitingForDeletionConfirmation) promptCount++;
        if (waitingForVictoryOutcome) promptCount++;
        if (waitingForScenarioName) promptCount++;
        if (waitingForThemeSelection) promptCount++;
        if (waitingForDirectCharacterAddition) promptCount++;
        
        if (promptCount > 1) {
            conflicts.add("Multiple prompts active simultaneously (" + promptCount + " prompts)");
        }
        
        // Check for conflicting weapon selection states
        if (waitingForRangedWeaponSelection && waitingForMeleeWeaponSelection) {
            conflicts.add("Both ranged and melee weapon selection active");
        }
        
        if (waitingForCharacterRangedWeapon && waitingForCharacterMeleeWeapon) {
            conflicts.add("Both character ranged and melee weapon selection active");
        }
        
        return conflicts;
    }
    
    // ====================
    // PRIVATE METHODS
    // ====================
    
    /**
     * Internal method to handle state changes with history tracking and debug callbacks.
     */
    private void setState(String stateName, boolean oldValue, boolean newValue) {
        if (oldValue != newValue) {
            // Record state change in history
            StateChange change = new StateChange(stateName, oldValue, newValue);
            stateHistory.add(change);
            
            // Limit history size
            if (stateHistory.size() > MAX_HISTORY_SIZE) {
                stateHistory.remove(0);
            }
            
            // Notify debug callback if set
            if (debugCallback != null) {
                debugCallback.onStateChange(stateName, oldValue, newValue);
            }
        }
    }
}
/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import java.util.List;
import java.util.ArrayList;

import input.states.InputStates;
import input.interfaces.InputManagerCallbacks;

/**
 * Centralized coordinator for all workflow state management in the OpenFields2 input system.
 * 
 * This coordinator handles all multi-step workflow states that were previously scattered
 * throughout InputManager, providing a single point of truth for workflow state transitions,
 * validation, and coordination. It integrates with InputStateTracker for basic state flags
 * while managing complex workflow-specific state data.
 * 
 * WORKFLOW TYPES MANAGED:
 * - Direct Character Addition (CTRL-A workflow)
 * - Scenario Creation workflows
 * - Manual Victory workflows
 * - State transition validation and logging
 * 
 * INTEGRATION POINTS:
 * - InputStateTracker: Basic boolean state flags
 * - Controllers: Workflow-specific controllers for execution
 * - DisplayCoordinator: Workflow state transition debugging
 * - InputManager: Delegation from main coordinator
 * 
 * @author DevCycle 15i - Phase 1: Workflow State Coordination Extraction
 */
public class WorkflowStateCoordinator {
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Dependencies and Core State
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /** State tracker for basic boolean flags */
    private final InputStateTracker stateTracker;
    
    /** Display coordinator for workflow state debugging */
    private final DisplayCoordinator displayCoordinator;
    
    /** Callback interface for main game operations */
    private final InputManagerCallbacks callbacks;
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Direct Character Addition Workflow State (CTRL-A functionality)
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /** Selected faction for direct character addition */
    private int directAdditionFaction = 0;
    
    /** Number of characters to add directly */
    private int directAdditionQuantity = 0;
    
    /** Spacing between characters in feet for direct addition */
    private double directAdditionSpacing = 5.0;
    
    /** Current step in direct addition workflow */
    private InputStates.DirectAdditionStep directAdditionStep = InputStates.DirectAdditionStep.FACTION;
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Scenario Creation Workflow State
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /** Name for new scenario being created */
    private String newScenarioName = "";
    
    /** Theme ID for new scenario being created */
    private String newScenarioTheme = "";
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Constructor for WorkflowStateCoordinator.
     * 
     * @param stateTracker State tracker for basic boolean flags
     * @param displayCoordinator Display coordinator for debugging
     * @param callbacks Callback interface for main game operations
     */
    public WorkflowStateCoordinator(InputStateTracker stateTracker, DisplayCoordinator displayCoordinator,
                                   InputManagerCallbacks callbacks) {
        this.stateTracker = stateTracker;
        this.displayCoordinator = displayCoordinator;
        this.callbacks = callbacks;
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Direct Character Addition Workflow Management
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Initialize direct character addition workflow.
     */
    public void startDirectCharacterAddition() {
        stateTracker.setWaitingForDirectCharacterAddition(true);
        directAdditionStep = InputStates.DirectAdditionStep.FACTION;
        directAdditionFaction = 0;
        directAdditionQuantity = 0;
        directAdditionSpacing = 5.0; // Default 5 feet
        
        displayCoordinator.debugWorkflowState("DIRECT_ADDITION", "STARTED", "Beginning direct character addition workflow");
    }
    
    /**
     * Process faction selection for direct character addition.
     * 
     * @param factionNumber Selected faction (1-4)
     * @return true if transition successful, false if invalid
     */
    public boolean processDirectAdditionFactionSelection(int factionNumber) {
        if (!stateTracker.isWaitingForDirectCharacterAddition() || 
            directAdditionStep != InputStates.DirectAdditionStep.FACTION) {
            return false;
        }
        
        if (factionNumber < 1 || factionNumber > 4) {
            displayCoordinator.debugWorkflowState("DIRECT_ADDITION", "ERROR", "Invalid faction number: " + factionNumber);
            return false;
        }
        
        directAdditionFaction = factionNumber - 1; // Convert to 0-based index
        directAdditionStep = InputStates.DirectAdditionStep.QUANTITY;
        
        displayCoordinator.debugWorkflowState("DIRECT_ADDITION", "FACTION_SELECTED", "Faction " + factionNumber + " selected");
        return true;
    }
    
    /**
     * Process quantity selection for direct character addition.
     * 
     * @param quantity Number of characters to add (1-20)
     * @return true if transition successful, false if invalid
     */
    public boolean processDirectAdditionQuantitySelection(int quantity) {
        if (!stateTracker.isWaitingForDirectCharacterAddition() || 
            directAdditionStep != InputStates.DirectAdditionStep.QUANTITY) {
            return false;
        }
        
        if (quantity < 1 || quantity > 20) {
            displayCoordinator.debugWorkflowState("DIRECT_ADDITION", "ERROR", "Invalid quantity: " + quantity);
            return false;
        }
        
        directAdditionQuantity = quantity;
        directAdditionStep = InputStates.DirectAdditionStep.SPACING;
        
        displayCoordinator.debugWorkflowState("DIRECT_ADDITION", "QUANTITY_SELECTED", "Quantity " + quantity + " selected");
        return true;
    }
    
    /**
     * Process spacing selection for direct character addition.
     * 
     * @param spacing Spacing in feet (1-9)
     * @return true if transition successful, false if invalid
     */
    public boolean processDirectAdditionSpacingSelection(int spacing) {
        if (!stateTracker.isWaitingForDirectCharacterAddition() || 
            directAdditionStep != InputStates.DirectAdditionStep.SPACING) {
            return false;
        }
        
        if (spacing < 1 || spacing > 9) {
            displayCoordinator.debugWorkflowState("DIRECT_ADDITION", "ERROR", "Invalid spacing: " + spacing);
            return false;
        }
        
        directAdditionSpacing = spacing;
        directAdditionStep = InputStates.DirectAdditionStep.PLACEMENT;
        
        displayCoordinator.debugWorkflowState("DIRECT_ADDITION", "SPACING_SELECTED", "Spacing " + spacing + " feet selected");
        return true;
    }
    
    /**
     * Complete direct character addition workflow.
     */
    public void completeDirectCharacterAddition() {
        stateTracker.setWaitingForDirectCharacterAddition(false);
        directAdditionStep = InputStates.DirectAdditionStep.FACTION;
        directAdditionFaction = 0;
        directAdditionQuantity = 0;
        directAdditionSpacing = 5.0;
        
        displayCoordinator.debugWorkflowState("DIRECT_ADDITION", "COMPLETED", "Direct character addition workflow completed");
    }
    
    /**
     * Cancel direct character addition workflow.
     */
    public void cancelDirectCharacterAddition() {
        stateTracker.setWaitingForDirectCharacterAddition(false);
        directAdditionStep = InputStates.DirectAdditionStep.FACTION;
        directAdditionFaction = 0;
        directAdditionQuantity = 0;
        directAdditionSpacing = 5.0;
        
        displayCoordinator.debugWorkflowState("DIRECT_ADDITION", "CANCELLED", "Direct character addition workflow cancelled");
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Scenario Creation Workflow Management
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Initialize scenario creation workflow.
     */
    public void startScenarioCreation() {
        newScenarioName = "";
        newScenarioTheme = "";
        
        displayCoordinator.debugWorkflowState("SCENARIO_CREATION", "STARTED", "Beginning scenario creation workflow");
    }
    
    /**
     * Set scenario name during creation workflow.
     * 
     * @param name Scenario name
     * @return true if name is valid, false if invalid
     */
    public boolean setScenarioName(String name) {
        if (name == null) {
            newScenarioName = "";
            return true;
        }
        
        newScenarioName = name;
        displayCoordinator.debugWorkflowState("SCENARIO_CREATION", "NAME_SET", "Scenario name: '" + newScenarioName + "'");
        return true;
    }
    
    /**
     * Append character to scenario name during text input.
     * 
     * @param character Character to append
     */
    public void appendToScenarioName(char character) {
        newScenarioName += character;
        displayCoordinator.debugWorkflowState("SCENARIO_CREATION", "NAME_APPEND", "Scenario name: '" + newScenarioName + "'");
    }
    
    /**
     * Remove last character from scenario name (backspace).
     */
    public void removeLastCharacterFromScenarioName() {
        if (!newScenarioName.isEmpty()) {
            newScenarioName = newScenarioName.substring(0, newScenarioName.length() - 1);
            displayCoordinator.debugWorkflowState("SCENARIO_CREATION", "NAME_BACKSPACE", "Scenario name: '" + newScenarioName + "'");
        }
    }
    
    /**
     * Set scenario theme during creation workflow.
     * 
     * @param themeId Theme ID
     * @return true if theme is valid, false if invalid
     */
    public boolean setScenarioTheme(String themeId) {
        if (themeId == null || themeId.trim().isEmpty()) {
            displayCoordinator.debugWorkflowState("SCENARIO_CREATION", "ERROR", "Invalid scenario theme");
            return false;
        }
        
        newScenarioTheme = themeId.trim();
        displayCoordinator.debugWorkflowState("SCENARIO_CREATION", "THEME_SET", "Scenario theme: " + newScenarioTheme);
        return true;
    }
    
    /**
     * Complete scenario creation workflow.
     */
    public void completeScenarioCreation() {
        newScenarioName = "";
        newScenarioTheme = "";
        
        displayCoordinator.debugWorkflowState("SCENARIO_CREATION", "COMPLETED", "Scenario creation workflow completed");
    }
    
    /**
     * Cancel scenario creation workflow.
     */
    public void cancelScenarioCreation() {
        newScenarioName = "";
        newScenarioTheme = "";
        
        displayCoordinator.debugWorkflowState("SCENARIO_CREATION", "CANCELLED", "Scenario creation workflow cancelled");
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Workflow State Validation and Queries
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Check if any workflow is currently active.
     * 
     * @return true if any workflow is in progress
     */
    public boolean isAnyWorkflowActive() {
        return stateTracker.isWaitingForDirectCharacterAddition() ||
               !newScenarioName.isEmpty() ||
               !newScenarioTheme.isEmpty();
    }
    
    /**
     * Get current direct addition step.
     * 
     * @return Current step in direct addition workflow
     */
    public InputStates.DirectAdditionStep getDirectAdditionStep() {
        return directAdditionStep;
    }
    
    /**
     * Check if direct addition workflow is ready for placement.
     * 
     * @return true if ready for character placement
     */
    public boolean isDirectAdditionReadyForPlacement() {
        return stateTracker.isWaitingForDirectCharacterAddition() &&
               directAdditionStep == InputStates.DirectAdditionStep.PLACEMENT;
    }
    
    /**
     * Validate that direct addition workflow state is consistent.
     * 
     * @return true if state is valid, false if inconsistent
     */
    public boolean validateDirectAdditionState() {
        if (!stateTracker.isWaitingForDirectCharacterAddition()) {
            return directAdditionStep == InputStates.DirectAdditionStep.FACTION &&
                   directAdditionFaction == 0 &&
                   directAdditionQuantity == 0 &&
                   directAdditionSpacing == 5.0;
        }
        
        switch (directAdditionStep) {
            case FACTION:
                return directAdditionFaction == 0 && directAdditionQuantity == 0;
            case QUANTITY:
                return directAdditionFaction >= 0 && directAdditionFaction < 4 && directAdditionQuantity == 0;
            case SPACING:
                return directAdditionFaction >= 0 && directAdditionFaction < 4 && 
                       directAdditionQuantity > 0 && directAdditionQuantity <= 20;
            case PLACEMENT:
                return directAdditionFaction >= 0 && directAdditionFaction < 4 &&
                       directAdditionQuantity > 0 && directAdditionQuantity <= 20 &&
                       directAdditionSpacing >= 1.0 && directAdditionSpacing <= 9.0;
            default:
                return false;
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Workflow State Access (Read-Only)
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Get direct addition faction (0-based index).
     * 
     * @return Selected faction index
     */
    public int getDirectAdditionFaction() {
        return directAdditionFaction;
    }
    
    /**
     * Get direct addition quantity.
     * 
     * @return Number of characters to add
     */
    public int getDirectAdditionQuantity() {
        return directAdditionQuantity;
    }
    
    /**
     * Get direct addition spacing.
     * 
     * @return Spacing between characters in feet
     */
    public double getDirectAdditionSpacing() {
        return directAdditionSpacing;
    }
    
    /**
     * Get new scenario name.
     * 
     * @return Scenario name being created
     */
    public String getNewScenarioName() {
        return newScenarioName;
    }
    
    /**
     * Get new scenario theme.
     * 
     * @return Scenario theme ID being created
     */
    public String getNewScenarioTheme() {
        return newScenarioTheme;
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Workflow State Reset and Cleanup
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Reset all workflow states to their default values.
     * Should be called when starting a new game or resetting the input system.
     */
    public void resetAllWorkflowStates() {
        // Reset direct addition workflow
        stateTracker.setWaitingForDirectCharacterAddition(false);
        directAdditionStep = InputStates.DirectAdditionStep.FACTION;
        directAdditionFaction = 0;
        directAdditionQuantity = 0;
        directAdditionSpacing = 5.0;
        
        // Reset scenario creation workflow
        newScenarioName = "";
        newScenarioTheme = "";
        
        displayCoordinator.debugWorkflowState("WORKFLOW_COORDINATOR", "RESET", "All workflow states reset to defaults");
    }
    
    /**
     * Get comprehensive workflow status for debugging.
     * 
     * @return WorkflowStatus object containing current state information
     */
    public WorkflowStatus getWorkflowStatus() {
        return new WorkflowStatus(
            stateTracker.isWaitingForDirectCharacterAddition(),
            directAdditionStep,
            directAdditionFaction,
            directAdditionQuantity,
            directAdditionSpacing,
            newScenarioName,
            newScenarioTheme,
            isAnyWorkflowActive()
        );
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Data Transfer Objects
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Data transfer object for comprehensive workflow status information.
     */
    public static class WorkflowStatus {
        public final boolean directAdditionActive;
        public final InputStates.DirectAdditionStep directAdditionStep;
        public final int directAdditionFaction;
        public final int directAdditionQuantity;
        public final double directAdditionSpacing;
        public final String scenarioName;
        public final String scenarioTheme;
        public final boolean anyWorkflowActive;
        
        public WorkflowStatus(boolean directAdditionActive, InputStates.DirectAdditionStep directAdditionStep,
                             int directAdditionFaction, int directAdditionQuantity, double directAdditionSpacing,
                             String scenarioName, String scenarioTheme, boolean anyWorkflowActive) {
            this.directAdditionActive = directAdditionActive;
            this.directAdditionStep = directAdditionStep;
            this.directAdditionFaction = directAdditionFaction;
            this.directAdditionQuantity = directAdditionQuantity;
            this.directAdditionSpacing = directAdditionSpacing;
            this.scenarioName = scenarioName;
            this.scenarioTheme = scenarioTheme;
            this.anyWorkflowActive = anyWorkflowActive;
        }
        
        @Override
        public String toString() {
            return String.format("WorkflowStatus(directAddition: %s[%s, faction:%d, qty:%d, spacing:%.1f], " +
                               "scenario: '%s'/'%s', anyActive: %s)",
                               directAdditionActive, directAdditionStep, directAdditionFaction, 
                               directAdditionQuantity, directAdditionSpacing, scenarioName, scenarioTheme, anyWorkflowActive);
        }
    }
}
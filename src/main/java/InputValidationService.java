/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import java.util.List;
import game.Unit;

/**
 * Centralized input validation service for the OpenFields2 game.
 * 
 * This service provides comprehensive validation for all types of user input including:
 * - Coordinate validation and boundary checking
 * - Numeric range validation for quantities, indices, and selections
 * - Unit selection and operation validation
 * - Workflow state and transition validation
 * - Command validity checking based on game state
 * 
 * The service provides consistent validation logic across all input handlers and
 * controllers, ensuring uniform error handling and user feedback throughout the
 * input system.
 * 
 * VALIDATION CATEGORIES:
 * - Range Validation: Numeric inputs within specified bounds
 * - Coordinate Validation: Screen and world coordinate validation
 * - Selection Validation: Unit selection and multi-unit operation validation
 * - State Validation: Game state and workflow state consistency
 * - Command Validation: Command applicability and prerequisites
 * 
 * @author DevCycle 15i - Phase 2: Input Validation Service Extraction
 */
public class InputValidationService {
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Constants and Configuration
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /** Maximum number of characters that can be added at once */
    public static final int MAX_CHARACTER_QUANTITY = 20;
    
    /** Maximum spacing between characters in feet */
    public static final int MAX_CHARACTER_SPACING = 9;
    
    /** Minimum spacing between characters in feet */
    public static final int MIN_CHARACTER_SPACING = 1;
    
    /** Maximum number of factions supported */
    public static final int MAX_FACTION_COUNT = 4;
    
    /** Minimum faction index (1-based) */
    public static final int MIN_FACTION_INDEX = 1;
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Range and Numeric Validation
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Validate that a number is within specified range (inclusive).
     * 
     * @param value Value to validate
     * @param min Minimum allowed value (inclusive)
     * @param max Maximum allowed value (inclusive)
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateRange(int value, int min, int max) {
        if (value < min || value > max) {
            return ValidationResult.failure("Value " + value + " is out of range [" + min + "-" + max + "]");
        }
        return ValidationResult.success();
    }
    
    /**
     * Validate faction selection input.
     * 
     * @param factionNumber Faction number (1-based)
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateFactionSelection(int factionNumber) {
        if (factionNumber == 0) {
            return ValidationResult.success("Cancel operation");
        }
        
        return validateRange(factionNumber, MIN_FACTION_INDEX, MAX_FACTION_COUNT);
    }
    
    /**
     * Validate character quantity input for batch operations.
     * 
     * @param quantity Number of characters requested
     * @param maxAvailable Maximum available characters (optional constraint)
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateCharacterQuantity(int quantity, int maxAvailable) {
        if (quantity == 0) {
            return ValidationResult.success("Cancel operation");
        }
        
        ValidationResult rangeResult = validateRange(quantity, 1, MAX_CHARACTER_QUANTITY);
        if (!rangeResult.isValid) {
            return rangeResult;
        }
        
        if (quantity > maxAvailable) {
            return ValidationResult.failure("Requested " + quantity + " characters but only " + maxAvailable + " available");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validate character spacing input.
     * 
     * @param spacing Spacing in feet
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateCharacterSpacing(int spacing) {
        if (spacing == 0) {
            return ValidationResult.success("Cancel operation");
        }
        
        return validateRange(spacing, MIN_CHARACTER_SPACING, MAX_CHARACTER_SPACING);
    }
    
    /**
     * Validate theme selection input.
     * 
     * @param themeNumber Theme number (1-based)
     * @param maxThemes Maximum number of available themes
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateThemeSelection(int themeNumber, int maxThemes) {
        if (themeNumber == 0) {
            return ValidationResult.success("Cancel operation");
        }
        
        return validateRange(themeNumber, 1, maxThemes);
    }
    
    /**
     * Validate archetype selection input.
     * 
     * @param archetypeNumber Archetype number (1-based)
     * @param maxArchetypes Maximum number of available archetypes
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateArchetypeSelection(int archetypeNumber, int maxArchetypes) {
        if (archetypeNumber == 0) {
            return ValidationResult.success("Cancel operation");
        }
        
        return validateRange(archetypeNumber, 1, maxArchetypes);
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Coordinate and Position Validation
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Validate screen coordinates are within canvas bounds.
     * 
     * @param x Screen X coordinate
     * @param y Screen Y coordinate
     * @param canvasWidth Canvas width
     * @param canvasHeight Canvas height
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateScreenCoordinates(double x, double y, double canvasWidth, double canvasHeight) {
        if (x < 0 || x > canvasWidth || y < 0 || y > canvasHeight) {
            return ValidationResult.failure("Coordinates (" + String.format("%.1f", x) + ", " + String.format("%.1f", y) + 
                                          ") are outside canvas bounds [0-" + String.format("%.0f", canvasWidth) + 
                                          ", 0-" + String.format("%.0f", canvasHeight) + "]");
        }
        return ValidationResult.success();
    }
    
    /**
     * Validate that coordinates are suitable for character placement.
     * 
     * @param x World X coordinate
     * @param y World Y coordinate
     * @param units List of existing units to check for collisions
     * @param minSpacing Minimum spacing required between units in pixels
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateCharacterPlacement(double x, double y, List<Unit> units, double minSpacing) {
        for (Unit existingUnit : units) {
            double distance = Math.hypot(x - existingUnit.x, y - existingUnit.y);
            if (distance < minSpacing) {
                return ValidationResult.failure("Placement too close to existing unit " + existingUnit.character.getDisplayName() + 
                                              " (distance: " + String.format("%.1f", distance) + " pixels, minimum: " + 
                                              String.format("%.1f", minSpacing) + " pixels)");
            }
        }
        return ValidationResult.success();
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Unit Selection and Operation Validation
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Validate that units are selected for operations that require selection.
     * 
     * @param selectedUnits List of selected units
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateUnitsSelected(List<Unit> selectedUnits) {
        if (selectedUnits == null || selectedUnits.isEmpty()) {
            return ValidationResult.failure("No units selected for operation");
        }
        return ValidationResult.success();
    }
    
    /**
     * Validate that selected units can perform movement operations.
     * 
     * @param selectedUnits List of selected units
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateUnitsCanMove(List<Unit> selectedUnits) {
        ValidationResult selectionResult = validateUnitsSelected(selectedUnits);
        if (!selectionResult.isValid) {
            return selectionResult;
        }
        
        int movableCount = 0;
        for (Unit unit : selectedUnits) {
            if (!unit.character.isIncapacitated() && !unit.isStopped) {
                movableCount++;
            }
        }
        
        if (movableCount == 0) {
            return ValidationResult.failure("No selected units can move (all incapacitated or stopped)");
        }
        
        return ValidationResult.success("" + movableCount + " of " + selectedUnits.size() + " units can move");
    }
    
    /**
     * Validate that selected units can perform combat operations.
     * 
     * @param selectedUnits List of selected units
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateUnitsCanAttack(List<Unit> selectedUnits) {
        ValidationResult selectionResult = validateUnitsSelected(selectedUnits);
        if (!selectionResult.isValid) {
            return selectionResult;
        }
        
        int combatCapableCount = 0;
        for (Unit unit : selectedUnits) {
            if (!unit.character.isIncapacitated() && unit.character.weapon != null) {
                combatCapableCount++;
            }
        }
        
        if (combatCapableCount == 0) {
            return ValidationResult.failure("No selected units can attack (all incapacitated or without weapons)");
        }
        
        return ValidationResult.success("" + combatCapableCount + " of " + selectedUnits.size() + " units can attack");
    }
    
    /**
     * Validate that a target unit is valid for combat operations.
     * 
     * @param targetUnit Target unit
     * @param attackerUnits Attacking units
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateCombatTarget(Unit targetUnit, List<Unit> attackerUnits) {
        if (targetUnit == null) {
            return ValidationResult.failure("No target unit specified");
        }
        
        if (targetUnit.character.isIncapacitated()) {
            return ValidationResult.failure("Target " + targetUnit.character.getDisplayName() + " is already incapacitated");
        }
        
        // Check if target is friendly (same faction as any attacker)
        for (Unit attacker : attackerUnits) {
            if (attacker.character.getFaction() == targetUnit.character.getFaction()) {
                return ValidationResult.failure("Cannot attack friendly unit " + targetUnit.character.getDisplayName());
            }
        }
        
        return ValidationResult.success();
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Text and String Validation
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Validate scenario name input.
     * 
     * @param name Scenario name
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateScenarioName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return ValidationResult.failure("Scenario name cannot be empty");
        }
        
        if (name.trim().length() > 50) {
            return ValidationResult.failure("Scenario name too long (maximum 50 characters)");
        }
        
        // Check for invalid characters
        String trimmedName = name.trim();
        if (trimmedName.matches(".*[<>:\"/\\\\|?*].*")) {
            return ValidationResult.failure("Scenario name contains invalid characters");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validate theme ID format.
     * 
     * @param themeId Theme identifier
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateThemeId(String themeId) {
        if (themeId == null || themeId.trim().isEmpty()) {
            return ValidationResult.failure("Theme ID cannot be empty");
        }
        
        // Theme IDs should follow a specific pattern
        if (!themeId.matches("^[a-z][a-z0-9_]*$")) {
            return ValidationResult.failure("Invalid theme ID format (must start with letter, contain only lowercase letters, numbers, and underscores)");
        }
        
        return ValidationResult.success();
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Workflow State Validation
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Validate that the game is in the correct state for character creation.
     * 
     * @param editMode Whether edit mode is active
     * @param workflowActive Whether any workflow is currently active
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateCharacterCreationState(boolean editMode, boolean workflowActive) {
        if (!editMode) {
            return ValidationResult.failure("Character creation requires edit mode to be active");
        }
        
        if (workflowActive) {
            return ValidationResult.failure("Cannot start character creation while another workflow is active");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validate that the game is in the correct state for scenario operations.
     * 
     * @param editMode Whether edit mode is active
     * @param hasUnits Whether units are present on the battlefield
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateScenarioCreationState(boolean editMode, boolean hasUnits) {
        if (!editMode) {
            return ValidationResult.failure("Scenario creation requires edit mode to be active");
        }
        
        // Warning but not failure if units are present
        if (hasUnits) {
            return ValidationResult.success("Warning: Creating new scenario will clear all existing units");
        }
        
        return ValidationResult.success();
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Validation Result Utility Methods
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Create a formatted error message for display to the user.
     * 
     * @param validationResult Result from validation
     * @param operation Operation being attempted
     * @return Formatted error message
     */
    public static String formatValidationError(ValidationResult validationResult, String operation) {
        if (validationResult.isValid) {
            return null; // No error
        }
        
        return "*** " + operation + " failed: " + validationResult.message + " ***";
    }
    
    /**
     * Create a formatted range error message.
     * 
     * @param value Invalid value
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @param operation Operation name
     * @return Formatted error message
     */
    public static String formatRangeError(int value, int min, int max, String operation) {
        return "*** Invalid " + operation + ". Use " + min + "-" + max + " or 0 to cancel ***";
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Data Transfer Objects
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Result of input validation operation.
     */
    public static class ValidationResult {
        public final boolean isValid;
        public final String message;
        public final boolean isCancel;
        
        private ValidationResult(boolean isValid, String message, boolean isCancel) {
            this.isValid = isValid;
            this.message = message;
            this.isCancel = isCancel;
        }
        
        /**
         * Create a successful validation result.
         * 
         * @return Success validation result
         */
        public static ValidationResult success() {
            return new ValidationResult(true, null, false);
        }
        
        /**
         * Create a successful validation result with a message.
         * 
         * @param message Success message
         * @return Success validation result with message
         */
        public static ValidationResult success(String message) {
            return new ValidationResult(true, message, false);
        }
        
        /**
         * Create a failed validation result.
         * 
         * @param message Error message
         * @return Failed validation result
         */
        public static ValidationResult failure(String message) {
            return new ValidationResult(false, message, false);
        }
        
        /**
         * Create a cancel validation result (user chose to cancel).
         * 
         * @param message Cancel message
         * @return Cancel validation result
         */
        public static ValidationResult cancel(String message) {
            return new ValidationResult(true, message, true);
        }
        
        @Override
        public String toString() {
            String status = isValid ? (isCancel ? "CANCEL" : "SUCCESS") : "FAILURE";
            return "ValidationResult(" + status + (message != null ? ": " + message : "") + ")";
        }
    }
}
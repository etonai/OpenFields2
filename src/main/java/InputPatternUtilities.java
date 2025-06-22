/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import java.util.function.Consumer;
import java.util.function.Supplier;
import game.Unit;

/**
 * Consolidated utility methods for common input processing patterns in InputManager.
 * 
 * This utility class contains common patterns that appear repeatedly throughout 
 * InputManager, consolidated into reusable methods to reduce code duplication and
 * improve maintainability. The patterns included are:
 * 
 * - Input validation → action → error handling
 * - Cancellation handling with slot 0 pattern
 * - Selected character loop processing
 * - Status message display formatting
 * - Workflow step processing
 * 
 * These utilities maintain the exact behavior of the original code while eliminating
 * duplicate implementations and providing consistent error handling and user feedback.
 * 
 * @author DevCycle 15i - Phase 4: Method Pattern Consolidation
 */
public class InputPatternUtilities {
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Input Validation and Action Pattern
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Handle input validation and action execution pattern used throughout InputManager.
     * 
     * This pattern validates input, executes an action if valid, or displays an error message.
     * Used by theme selection, archetype selection, weapon selection, and other input handlers.
     * 
     * @param input Input value to validate
     * @param validator Validation function that returns ValidationResult
     * @param action Action to execute if validation passes
     * @param errorMessageSupplier Supplier for error message if validation fails
     * @return true if validation passed and action was executed, false otherwise
     */
    public static boolean validateAndExecute(int input, 
                                           Supplier<InputValidationService.ValidationResult> validator,
                                           Runnable action, 
                                           Supplier<String> errorMessageSupplier) {
        InputValidationService.ValidationResult result = validator.get();
        if (result.isValid) {
            action.run();
            return true;
        } else {
            System.out.println("*** " + errorMessageSupplier.get() + " ***");
            return false;
        }
    }
    
    /**
     * Handle range validation with custom error message pattern.
     * 
     * @param input Input value to validate
     * @param min Minimum valid value
     * @param max Maximum valid value  
     * @param action Action to execute if validation passes
     * @param operationName Name of operation for error message
     * @return true if validation passed and action was executed, false otherwise
     */
    public static boolean validateRangeAndExecute(int input, int min, int max, 
                                                 Runnable action, String operationName) {
        InputValidationService.ValidationResult result = InputValidationService.validateRange(input, min, max);
        if (result.isValid) {
            action.run();
            return true;
        } else {
            System.out.println("*** Invalid " + operationName + ". Use " + min + "-" + max + " or 0 to cancel ***");
            return false;
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Cancellation and State Management Pattern
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Handle cancellation with slot 0 pattern used throughout workflow handlers.
     * 
     * This pattern checks if input is 0 (cancel), performs cancellation cleanup,
     * or validates input range and executes action.
     * 
     * @param input Input value to check
     * @param operationName Name of operation being cancelled
     * @param stateTracker State tracker to update
     * @param stateSetter Method to set state to false on cancellation
     * @param validator Validation function for non-zero input
     * @param action Action to execute if validation passes
     * @return true if operation should continue, false if cancelled or invalid
     */
    public static boolean handleCancellationOrValidateAndExecute(int input, 
                                                               String operationName,
                                                               Runnable stateSetter,
                                                               Supplier<InputValidationService.ValidationResult> validator,
                                                               Runnable action) {
        if (input == 0) {
            System.out.println("*** " + operationName + " cancelled ***");
            stateSetter.run();
            return false;
        }
        
        return validateAndExecute(input, validator, action, 
                                () -> "Invalid selection. Use valid range or 0 to cancel");
    }
    
    /**
     * Handle cancellation with range validation pattern.
     * 
     * @param input Input value to check
     * @param operationName Name of operation being cancelled
     * @param stateSetter Method to set state to false on cancellation
     * @param min Minimum valid value
     * @param max Maximum valid value
     * @param action Action to execute if validation passes
     * @return true if operation should continue, false if cancelled or invalid
     */
    public static boolean handleCancellationOrRangeValidation(int input, 
                                                             String operationName,
                                                             Runnable stateSetter,
                                                             int min, int max,
                                                             Runnable action) {
        if (input == 0) {
            System.out.println("*** " + operationName + " cancelled ***");
            stateSetter.run();
            return false;
        }
        
        return validateRangeAndExecute(input, min, max, action, operationName);
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Selected Character Processing Pattern
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Process action on all selected characters that are not incapacitated.
     * 
     * This pattern is used by movement controls, aiming controls, combat mode changes,
     * and other operations that affect selected units.
     * 
     * @param selectionManager Selection manager to get selected units
     * @param action Action to perform on each valid unit
     * @param singleResultFormat Format string for single unit result (with %s for unit name)
     * @param multiResultFormat Format string for multiple units result (with %d for count)
     */
    public static void processSelectedCharacters(SelectionManager selectionManager,
                                               Consumer<Unit> action,
                                               String singleResultFormat,
                                               String multiResultFormat) {
        int processedCount = 0;
        
        for (Unit unit : selectionManager.getSelectedUnits()) {
            if (!unit.character.isIncapacitated()) {
                action.accept(unit);
                processedCount++;
            }
        }
        
        if (processedCount > 0) {
            displayActionResult(selectionManager, singleResultFormat, multiResultFormat);
        }
    }
    
    /**
     * Process action on selected characters with custom condition.
     * 
     * @param selectionManager Selection manager to get selected units
     * @param condition Additional condition for processing unit (beyond incapacitation check)
     * @param action Action to perform on each valid unit
     * @param singleResultFormat Format string for single unit result
     * @param multiResultFormat Format string for multiple units result
     */
    public static void processSelectedCharactersWithCondition(SelectionManager selectionManager,
                                                            java.util.function.Predicate<Unit> condition,
                                                            Consumer<Unit> action,
                                                            String singleResultFormat,
                                                            String multiResultFormat) {
        int processedCount = 0;
        
        for (Unit unit : selectionManager.getSelectedUnits()) {
            if (!unit.character.isIncapacitated() && condition.test(unit)) {
                action.accept(unit);
                processedCount++;
            }
        }
        
        if (processedCount > 0) {
            displayActionResult(selectionManager, singleResultFormat, multiResultFormat);
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Status Message Display Pattern
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Display action result message using single/multi-unit pattern.
     * 
     * This pattern is used throughout InputManager to display consistent feedback
     * for actions affecting selected units.
     * 
     * @param selectionManager Selection manager to get selected units
     * @param singleResultFormat Format string for single unit (with %s for unit name)
     * @param multiResultFormat Format string for multiple units (with %d for count)
     */
    public static void displayActionResult(SelectionManager selectionManager,
                                         String singleResultFormat,
                                         String multiResultFormat) {
        if (selectionManager.getSelectionCount() == 1) {
            Unit unit = selectionManager.getSelected();
            System.out.println("*** " + String.format(singleResultFormat, unit.character.getDisplayName()) + " ***");
        } else {
            System.out.println("*** " + String.format(multiResultFormat, selectionManager.getSelectionCount()) + " ***");
        }
    }
    
    /**
     * Display simple action result without formatting.
     * 
     * @param selectionManager Selection manager to get selected units
     * @param singleResult Message for single unit
     * @param multiResult Message for multiple units
     */
    public static void displaySimpleActionResult(SelectionManager selectionManager,
                                                String singleResult,
                                                String multiResult) {
        if (selectionManager.getSelectionCount() == 1) {
            System.out.println("*** " + singleResult + " ***");
        } else {
            System.out.println("*** " + multiResult + " ***");
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Unit State Checking Utilities
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Check if any selected units can perform an action (not incapacitated).
     * 
     * @param selectionManager Selection manager to check
     * @return true if at least one selected unit is not incapacitated
     */
    public static boolean hasActiveSelectedUnits(SelectionManager selectionManager) {
        return selectionManager.getSelectedUnits().stream()
               .anyMatch(unit -> !unit.character.isIncapacitated());
    }
    
    /**
     * Count active (non-incapacitated) units in selection.
     * 
     * @param selectionManager Selection manager to check
     * @return Number of active units in selection
     */
    public static int countActiveSelectedUnits(SelectionManager selectionManager) {
        return (int) selectionManager.getSelectedUnits().stream()
                    .filter(unit -> !unit.character.isIncapacitated())
                    .count();
    }
    
    /**
     * Check if selection contains units but none are active.
     * 
     * @param selectionManager Selection manager to check
     * @return true if units are selected but all are incapacitated
     */
    public static boolean hasOnlyIncapacitatedUnits(SelectionManager selectionManager) {
        return selectionManager.getSelectionCount() > 0 && !hasActiveSelectedUnits(selectionManager);
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Input Parsing Utilities
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Parse integer input with error handling.
     * 
     * @param input Input string to parse
     * @param defaultValue Default value if parsing fails
     * @return Parsed integer or default value
     */
    public static int parseIntWithDefault(String input, int defaultValue) {
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Parse integer input and validate it's within a specific range.
     * 
     * @param input Input string to parse
     * @param min Minimum valid value
     * @param max Maximum valid value
     * @return Parsed and validated integer, or -1 if invalid
     */
    public static int parseAndValidateInt(String input, int min, int max) {
        try {
            int value = Integer.parseInt(input.trim());
            if (value >= min && value <= max) {
                return value;
            }
        } catch (NumberFormatException e) {
            // Fall through to return -1
        }
        return -1;
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Error Message Formatting Utilities
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Format standard range error message.
     * 
     * @param operationName Name of the operation
     * @param min Minimum valid value
     * @param max Maximum valid value
     * @return Formatted error message
     */
    public static String formatRangeErrorMessage(String operationName, int min, int max) {
        return "Invalid " + operationName + ". Use " + min + "-" + max + " or 0 to cancel";
    }
    
    /**
     * Format standard cancellation message.
     * 
     * @param operationName Name of the operation being cancelled
     * @return Formatted cancellation message
     */
    public static String formatCancellationMessage(String operationName) {
        return operationName + " cancelled";
    }
    
    /**
     * Format validation error with custom message.
     * 
     * @param operationName Name of the operation
     * @param customMessage Custom error description
     * @return Formatted error message
     */
    public static String formatCustomErrorMessage(String operationName, String customMessage) {
        return "Invalid " + operationName + ": " + customMessage;
    }
}
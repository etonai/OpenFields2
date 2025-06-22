/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import java.util.List;

import game.Unit;
import input.interfaces.InputManagerCallbacks;

/**
 * Controller responsible for managing unit movement operations in the OpenFields2 game.
 * 
 * This controller handles all aspects of unit movement including:
 * - Movement type controls (W/S keys for speed adjustment)
 * - Movement command processing (right-click movement)
 * - Selection-based movement coordination
 * - Edit mode movement (instant teleportation)
 * - Movement state management and validation
 * 
 * The controller integrates with SelectionManager to provide coordinated movement
 * operations for single units and multi-unit selections while maintaining
 * consistent movement behavior across different game modes.
 * 
 * @author DevCycle 15h - Phase 4.2: Movement Controller Extraction
 */
public class MovementController {
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Dependencies and State
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /** List of game units for movement operations */
    private final List<Unit> units;
    
    /** Selection manager for unit selection and coordination */
    private final SelectionManager selectionManager;
    
    /** Display coordinator for movement feedback and debug operations */
    private final DisplayCoordinator displayCoordinator;
    
    /** Callback interface for game state queries and operations */
    private final InputManagerCallbacks callbacks;
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Constructor for MovementController.
     * 
     * @param units List of game units for movement operations
     * @param selectionManager Selection manager for unit coordination
     * @param displayCoordinator Display coordinator for feedback
     * @param callbacks Callback interface for game state operations
     */
    public MovementController(List<Unit> units, SelectionManager selectionManager,
                             DisplayCoordinator displayCoordinator, InputManagerCallbacks callbacks) {
        this.units = units;
        this.selectionManager = selectionManager;
        this.displayCoordinator = displayCoordinator;
        this.callbacks = callbacks;
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Movement Type Controls
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Handle movement control key events.
     * 
     * Processes W/S key presses for movement speed control:
     * - W key: Increases movement type (Crawl → Walk → Jog → Run)
     * - S key: Decreases movement type or stops if at minimum speed
     * 
     * Operations apply to all selected units and provide appropriate feedback.
     * 
     * @param e KeyEvent containing the key press information
     * @return true if the key event was handled by movement controls, false otherwise
     */
    public boolean handleMovementControls(KeyEvent e) {
        if (!selectionManager.hasSelection()) {
            return false; // No units selected for movement control
        }
        
        KeyCode keyCode = e.getCode();
        
        switch (keyCode) {
            case W:
                handleIncreaseMovementSpeed();
                return true;
                
            case S:
                handleDecreaseMovementSpeed();
                return true;
                
            default:
                return false; // Key not handled by movement controls
        }
    }
    
    /**
     * Handle W key press to increase movement speed for selected units.
     */
    private void handleIncreaseMovementSpeed() {
        for (Unit unit : selectionManager.getSelectedUnits()) {
            if (unit.character.isIncapacitated()) {
                continue; // Skip incapacitated units
            }
            
            // Resume movement if unit was stopped and speed is being increased
            if (unit.isStopped) {
                unit.resumeMovement();
                displayCoordinator.debugInputEvent("MOVEMENT_CONTROL", 
                    unit.character.getDisplayName() + " resumed movement");
            }
            
            // Increase movement type
            String oldType = unit.character.getCurrentMovementType().toString();
            unit.character.increaseMovementType();
            String newType = unit.character.getCurrentMovementType().toString();
            
            // Display feedback if movement type actually changed
            if (!oldType.equals(newType)) {
                displayCoordinator.displayMovementTypeChange(unit, unit.character.getCurrentMovementType());
                displayCoordinator.debugInputEvent("MOVEMENT_CONTROL", 
                    unit.character.getDisplayName() + " movement: " + oldType + " → " + newType);
            }
        }
    }
    
    /**
     * Handle S key press to decrease movement speed for selected units.
     */
    private void handleDecreaseMovementSpeed() {
        for (Unit unit : selectionManager.getSelectedUnits()) {
            if (unit.character.isIncapacitated()) {
                continue; // Skip incapacitated units
            }
            
            String oldType = unit.character.getCurrentMovementType().toString();
            
            // If at minimum movement type (Crawl), stop the unit instead
            if (unit.character.getCurrentMovementType() == combat.MovementType.CRAWL) {
                unit.stopMovement();
                displayCoordinator.debugInputEvent("MOVEMENT_CONTROL", 
                    unit.character.getDisplayName() + " stopped movement");
                continue;
            }
            
            // Decrease movement type
            unit.character.decreaseMovementType();
            String newType = unit.character.getCurrentMovementType().toString();
            
            // Display feedback
            displayCoordinator.displayMovementTypeChange(unit, unit.character.getCurrentMovementType());
            displayCoordinator.debugInputEvent("MOVEMENT_CONTROL", 
                unit.character.getDisplayName() + " movement: " + oldType + " → " + newType);
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Movement Command Processing
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Handle right-click movement commands for selected units.
     * 
     * Processes movement commands based on game mode:
     * - Edit mode: Instant teleportation to target location
     * - Normal mode: Coordinated movement relative to selection center
     * 
     * @param targetX World X coordinate of movement target
     * @param targetY World Y coordinate of movement target
     */
    public void handleMovementCommand(double targetX, double targetY) {
        if (!selectionManager.hasSelection()) {
            return; // No units selected for movement
        }
        
        if (callbacks.isEditMode()) {
            handleEditModeMovement(targetX, targetY);
        } else {
            handleNormalMovement(targetX, targetY);
        }
    }
    
    /**
     * Handle movement in edit mode (instant teleportation).
     * 
     * @param targetX World X coordinate of destination
     * @param targetY World Y coordinate of destination
     */
    private void handleEditModeMovement(double targetX, double targetY) {
        // Calculate selection center to maintain relative positioning
        selectionManager.calculateSelectionCenter();
        double selectionCenterX = selectionManager.getSelectionCenterX();
        double selectionCenterY = selectionManager.getSelectionCenterY();
        
        for (Unit unit : selectionManager.getSelectedUnits()) {
            // Calculate offset from selection center
            double deltaX = targetX - selectionCenterX;
            double deltaY = targetY - selectionCenterY;
            
            // Apply instant teleportation
            unit.x = unit.x + deltaX;
            unit.y = unit.y + deltaY;
            unit.targetX = unit.x;
            unit.targetY = unit.y;
            unit.hasTarget = false;
            unit.isStopped = false;
            
            displayCoordinator.debugInputEvent("MOVEMENT_COMMAND", 
                unit.character.getDisplayName() + " teleported to (" + 
                String.format("%.1f", unit.x) + ", " + String.format("%.1f", unit.y) + ")");
        }
        
        // Display movement feedback
        displayCoordinator.displayUnitMovement(selectionManager.getSelectionCount(), 
                                             targetX, targetY, true);
    }
    
    /**
     * Handle movement in normal mode (with movement rules).
     * 
     * @param targetX World X coordinate of destination
     * @param targetY World Y coordinate of destination
     */
    private void handleNormalMovement(double targetX, double targetY) {
        // Calculate selection center for relative movement
        selectionManager.calculateSelectionCenter();
        double selectionCenterX = selectionManager.getSelectionCenterX();
        double selectionCenterY = selectionManager.getSelectionCenterY();
        
        double deltaX = targetX - selectionCenterX;
        double deltaY = targetY - selectionCenterY;
        
        for (Unit unit : selectionManager.getSelectedUnits()) {
            if (unit.character.isIncapacitated()) {
                continue; // Skip incapacitated units
            }
            
            // Cancel any ongoing melee movement when new movement command is given
            if (unit.character.isMovingToMelee) {
                unit.character.isMovingToMelee = false;
                unit.character.meleeTarget = null;
                displayCoordinator.debugInputEvent("MOVEMENT_COMMAND", 
                    unit.character.getDisplayName() + " cancelled melee movement");
            }
            
            // Set new target position relative to unit's current position
            double newTargetX = unit.x + deltaX;
            double newTargetY = unit.y + deltaY;
            unit.setTarget(newTargetX, newTargetY);
            
            displayCoordinator.debugInputEvent("MOVEMENT_COMMAND", 
                unit.character.getDisplayName() + " moving to (" + 
                String.format("%.1f", newTargetX) + ", " + String.format("%.1f", newTargetY) + ")");
        }
        
        // Display movement feedback
        displayCoordinator.displayUnitMovement(selectionManager.getSelectionCount(), 
                                             targetX, targetY, false);
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Movement State Management
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Stop movement for all selected units.
     */
    public void stopSelectedUnits() {
        if (!selectionManager.hasSelection()) {
            return;
        }
        
        for (Unit unit : selectionManager.getSelectedUnits()) {
            if (!unit.character.isIncapacitated()) {
                unit.stopMovement();
                displayCoordinator.debugInputEvent("MOVEMENT_CONTROL", 
                    unit.character.getDisplayName() + " movement stopped");
            }
        }
        
        displayCoordinator.displayMovementTypeChange(selectionManager.getSelected(), combat.MovementType.CRAWL); // Use CRAWL as stopped indicator
    }
    
    /**
     * Resume movement for all selected units.
     */
    public void resumeSelectedUnits() {
        if (!selectionManager.hasSelection()) {
            return;
        }
        
        for (Unit unit : selectionManager.getSelectedUnits()) {
            if (!unit.character.isIncapacitated() && unit.isStopped) {
                unit.resumeMovement();
                displayCoordinator.debugInputEvent("MOVEMENT_CONTROL", 
                    unit.character.getDisplayName() + " movement resumed");
            }
        }
        
        if (selectionManager.hasSelection()) {
            displayCoordinator.displayMovementTypeChange(selectionManager.getSelected(), 
                selectionManager.getSelected().character.getCurrentMovementType());
        }
    }
    
    /**
     * Cancel melee movement for a specific unit.
     * 
     * @param unit The unit to cancel melee movement for
     */
    public void cancelMeleeMovement(Unit unit) {
        if (unit.character.isMovingToMelee) {
            unit.character.isMovingToMelee = false;
            unit.character.meleeTarget = null;
            
            // Stop the unit at current position
            unit.setTarget(unit.x, unit.y);
            
            displayCoordinator.debugInputEvent("MOVEMENT_COMMAND", 
                unit.character.getDisplayName() + " cancelled melee movement");
        }
    }
    
    /**
     * Cancel melee movement for all units moving to a specific target.
     * 
     * @param targetUnit The target unit that became unavailable
     */
    public void cancelMeleeMovementToTarget(Unit targetUnit) {
        for (Unit unit : units) {
            if (unit.character.isMovingToMelee && unit.character.meleeTarget == targetUnit) {
                cancelMeleeMovement(unit);
            }
        }
        
        displayCoordinator.debugInputEvent("MOVEMENT_COMMAND", 
            "Cancelled melee movement to " + targetUnit.character.getDisplayName() + " for all units");
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Movement Validation and Utility
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Check if a unit can move (not incapacitated, not stopped).
     * 
     * @param unit The unit to check
     * @return true if the unit can move, false otherwise
     */
    public boolean canUnitMove(Unit unit) {
        return !unit.character.isIncapacitated() && !unit.isStopped;
    }
    
    /**
     * Get the number of units that can move from the current selection.
     * 
     * @return Number of movable units in selection
     */
    public int getMovableUnitsInSelection() {
        if (!selectionManager.hasSelection()) {
            return 0;
        }
        
        int movableCount = 0;
        for (Unit unit : selectionManager.getSelectedUnits()) {
            if (canUnitMove(unit)) {
                movableCount++;
            }
        }
        
        return movableCount;
    }
    
    /**
     * Get movement status summary for selected units.
     * 
     * @return MovementStatus object containing movement information
     */
    public MovementStatus getSelectionMovementStatus() {
        if (!selectionManager.hasSelection()) {
            return new MovementStatus(0, 0, 0, "No units selected");
        }
        
        int totalUnits = selectionManager.getSelectionCount();
        int movableUnits = getMovableUnitsInSelection();
        int stoppedUnits = 0;
        
        for (Unit unit : selectionManager.getSelectedUnits()) {
            if (unit.isStopped && !unit.character.isIncapacitated()) {
                stoppedUnits++;
            }
        }
        
        String primaryMovementType = selectionManager.getSelected().character.getCurrentMovementType().toString();
        
        return new MovementStatus(totalUnits, movableUnits, stoppedUnits, primaryMovementType);
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Data Transfer Objects
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Data transfer object for movement status information.
     */
    public static class MovementStatus {
        public final int totalUnits;
        public final int movableUnits;
        public final int stoppedUnits;
        public final String primaryMovementType;
        
        public MovementStatus(int totalUnits, int movableUnits, int stoppedUnits, String primaryMovementType) {
            this.totalUnits = totalUnits;
            this.movableUnits = movableUnits;
            this.stoppedUnits = stoppedUnits;
            this.primaryMovementType = primaryMovementType;
        }
        
        @Override
        public String toString() {
            return String.format("MovementStatus(total: %d, movable: %d, stopped: %d, type: %s)", 
                               totalUnits, movableUnits, stoppedUnits, primaryMovementType);
        }
    }
}
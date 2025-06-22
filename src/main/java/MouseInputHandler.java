import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import java.util.List;

import game.Unit;
import game.GameClock;
import game.GameCallbacks;
import input.interfaces.InputManagerCallbacks;

/**
 * Handler for mouse input events in the OpenFields2 game.
 * 
 * This handler processes all mouse interactions including:
 * - Unit selection (single click and rectangle selection)
 * - Movement commands (right-click on empty space)
 * - Combat targeting (right-click on units)
 * - Special combat modes (Shift+right-click for target zones and persistent attack)
 * - Edit mode operations (character placement, instant teleport)
 * 
 * The handler coordinates with multiple systems:
 * - SelectionManager for unit selection operations
 * - CombatCommandProcessor for combat actions
 * - EditModeManager for edit mode workflows
 * - DisplayCoordinator for debug information and feedback
 * 
 * @author DevCycle 15h - Phase 3.1: Mouse Input Handler Extraction
 */
public class MouseInputHandler {
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Dependencies and State
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /** List of game units for interaction and collision detection */
    private final List<Unit> units;
    
    /** Selection manager for unit selection operations */
    private final SelectionManager selectionManager;
    
    /** Game renderer for coordinate conversion */
    private final GameRenderer gameRenderer;
    
    /** Display coordinator for debug information and feedback */
    private final DisplayCoordinator displayCoordinator;
    
    /** Event router for determining mouse event handling */
    private final InputEventRouter eventRouter;
    
    /** Edit mode manager for edit mode operations */
    private final EditModeManager editModeManager;
    
    /** Combat command processor for combat operations */
    private final CombatCommandProcessor combatCommandProcessor;
    
    /** Game clock for timing information */
    private final GameClock gameClock;
    
    /** Event queue for scheduling events */
    private final java.util.PriorityQueue<game.ScheduledEvent> eventQueue;
    
    /** Callback interface for main game operations */
    private final InputManagerCallbacks callbacks;
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Constructor for MouseInputHandler.
     * 
     * @param units List of game units
     * @param selectionManager Selection manager for unit operations
     * @param gameRenderer Game renderer for coordinate conversion
     * @param displayCoordinator Display coordinator for debug operations
     * @param eventRouter Event router for mouse event routing
     * @param editModeManager Edit mode manager for edit operations
     * @param combatCommandProcessor Combat command processor for combat operations
     * @param gameClock Game clock for timing
     * @param eventQueue Event queue for scheduling
     * @param callbacks Callback interface for main game operations
     */
    public MouseInputHandler(List<Unit> units, SelectionManager selectionManager,
                           GameRenderer gameRenderer, DisplayCoordinator displayCoordinator,
                           InputEventRouter eventRouter, EditModeManager editModeManager,
                           CombatCommandProcessor combatCommandProcessor, GameClock gameClock,
                           java.util.PriorityQueue<game.ScheduledEvent> eventQueue,
                           InputManagerCallbacks callbacks) {
        this.units = units;
        this.selectionManager = selectionManager;
        this.gameRenderer = gameRenderer;
        this.displayCoordinator = displayCoordinator;
        this.eventRouter = eventRouter;
        this.editModeManager = editModeManager;
        this.combatCommandProcessor = combatCommandProcessor;
        this.gameClock = gameClock;
        this.eventQueue = eventQueue;
        this.callbacks = callbacks;
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Mouse Event Processing
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Process mouse button press events.
     * 
     * This is the primary entry point for mouse input processing. It handles:
     * - Left-click: Unit selection, rectangle selection start, character placement
     * - Right-click: Target zone selection (Shift+right), combat targeting, movement
     * - Context-sensitive routing based on edit mode and current operations
     * 
     * The method uses the InputEventRouter to determine appropriate handling
     * based on current application state and delegates to specialized handlers.
     * 
     * @param e MouseEvent containing button type, position, and modifier states
     */
    public void handleMousePressed(MouseEvent e) {
        displayCoordinator.startPerformanceTimer("MousePressed");
        double x = gameRenderer.screenToWorldX(e.getX());
        double y = gameRenderer.screenToWorldY(e.getY());
        
        displayCoordinator.debugInputEvent("MOUSE_PRESS", e.getButton() + " at screen(" + e.getX() + "," + e.getY() + 
                       ") world(" + String.format("%.1f", x) + "," + String.format("%.1f", y) + ")");
        displayCoordinator.addInputTraceEvent("Mouse pressed: " + e.getButton() + " at (" + String.format("%.1f", x) + "," + String.format("%.1f", y) + ")");
        
        if (e.getButton() == MouseButton.PRIMARY) {
            handleLeftClick(e, x, y);
        } else if (e.getButton() == MouseButton.SECONDARY) {
            handleRightClickPress(e, x, y);
        }
        
        displayCoordinator.endPerformanceTimer("MousePressed");
        displayCoordinator.logMemoryUsage("After MousePressed");
    }
    
    /**
     * Process mouse drag events for ongoing operations.
     * 
     * Handles continuous mouse movement while a button is held down:
     * - Rectangle selection: Updates selection rectangle size and position
     * - Future expansion: Could handle camera panning or drag operations
     * 
     * Only processes drag events when a relevant operation is active to
     * minimize unnecessary processing during normal mouse movement.
     * 
     * @param e MouseEvent containing current mouse position during drag
     */
    public void handleMouseDragged(MouseEvent e) {
        if (selectionManager.isSelecting()) {
            double x = gameRenderer.screenToWorldX(e.getX());
            double y = gameRenderer.screenToWorldY(e.getY());
            displayCoordinator.debugSelectionOperation("UPDATE_RECTANGLE", "Dragging to (" + 
                                   String.format("%.1f", x) + "," + String.format("%.1f", y) + ")");
            selectionManager.updateRectangleSelection(x, y);
        }
    }
    
    /**
     * Process mouse button release events to complete operations.
     * 
     * Finalizes operations that were started on mouse press:
     * - Rectangle selection: Completes multi-unit selection and displays stats
     * - Target zone selection: Completes target zone with combat command processor
     * - Clears temporary UI state and prepares for next input
     * 
     * This handler ensures that drag-based operations have a clear completion
     * point and that the UI returns to a clean state after user interactions.
     * 
     * @param e MouseEvent containing button type and final release position
     */
    public void handleMouseReleased(MouseEvent e) {
        displayCoordinator.debugInputEvent("MOUSE_RELEASE", e.getButton() + " at screen(" + e.getX() + "," + e.getY() + ")");
        displayCoordinator.addInputTraceEvent("Mouse released: " + e.getButton());
        
        if (selectionManager.isSelecting() && e.getButton() == MouseButton.PRIMARY) {
            // Complete rectangle selection
            displayCoordinator.debugSelectionOperation("COMPLETE_RECTANGLE", "Finishing rectangle selection");
            selectionManager.completeRectangleSelection(units);
            
            if (selectionManager.hasSelection()) {
                displayCoordinator.debugSelectionOperation("MULTI_SELECT_COMPLETE", selectionManager.getSelectionCount() + " units selected");
                displayMultiCharacterSelection();
            }
        } else if (combatCommandProcessor.isSelectingTargetZone() && e.getButton() == MouseButton.SECONDARY && e.isShiftDown()) {
            // Complete target zone selection via CombatCommandProcessor
            double x = gameRenderer.screenToWorldX(e.getX());
            double y = gameRenderer.screenToWorldY(e.getY());
            
            displayCoordinator.debugSelectionOperation("COMPLETE_TARGET_ZONE", "Target zone at (" + 
                                   String.format("%.1f", x) + "," + String.format("%.1f", y) + ")");
            combatCommandProcessor.completeTargetZoneSelection(x, y);
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Left Click Handling
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Handle left-click mouse events with context-sensitive routing.
     * 
     * @param e MouseEvent for modifier and position information
     * @param x World x coordinate of click
     * @param y World y coordinate of click
     */
    private void handleLeftClick(MouseEvent e, double x, double y) {
        // Use InputEventRouter to determine handling
        InputEventRouter.MouseEventRoute route = eventRouter.routeMouseEvent(e, 
            editModeManager.isInDeploymentPlacementMode(), 
            editModeManager.isInDirectAdditionPlacementMode(),
            callbacks.isEditMode());
        
        switch (route) {
            case DEPLOYMENT_PLACEMENT:
                // Delegate to EditModeManager
                editModeManager.completeCharacterDeployment(x, y);
                return;
            case CHARACTER_PLACEMENT:
                displayCoordinator.debugWorkflowState("DIRECT_ADDITION", "PLACEMENT", "Placing character at (" + 
                                 String.format("%.1f", x) + "," + String.format("%.1f", y) + ")");
                // Delegate to EditModeManager
                editModeManager.handleCharacterPlacement(x, y);
                return;
            case UNIT_SELECTION:
                // Continue with normal selection logic below
                break;
        }
        
        // Left click - single unit selection or start rectangle selection
        Unit clickedUnit = findUnitAt(x, y);
        
        if (clickedUnit != null) {
            // Single unit selection
            displayCoordinator.debugSelectionOperation("SELECT_UNIT", clickedUnit.character.getDisplayName() + " at (" + 
                                  String.format("%.1f", x) + "," + String.format("%.1f", y) + ")");
            selectionManager.selectUnit(clickedUnit);
            displayEnhancedCharacterStats(clickedUnit);
        } else {
            // Start rectangle selection
            displayCoordinator.debugSelectionOperation("START_RECTANGLE", "Starting at (" + 
                                   String.format("%.1f", x) + "," + String.format("%.1f", y) + ")");
            selectionManager.startRectangleSelection(x, y);
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Right Click Handling
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Handle right-click mouse press events.
     * 
     * @param e MouseEvent for modifier and position information
     * @param x World x coordinate of click
     * @param y World y coordinate of click
     */
    private void handleRightClickPress(MouseEvent e, double x, double y) {
        Unit clickedUnit = findUnitAt(x, y);
        
        // Check for Shift+right click target zone selection
        if (e.isShiftDown() && clickedUnit == null && selectionManager.getSelectionCount() == 1) {
            // Delegate target zone selection to CombatCommandProcessor
            combatCommandProcessor.startTargetZoneSelection(x, y);
        } else {
            handleRightClick(clickedUnit, x, y, e.isShiftDown());
        }
    }
    
    /**
     * Handle right-click actions based on context.
     * 
     * @param clickedUnit Unit that was clicked, or null if empty space
     * @param x World x coordinate of click
     * @param y World y coordinate of click
     * @param isShiftDown Whether Shift key was held
     */
    private void handleRightClick(Unit clickedUnit, double x, double y, boolean isShiftDown) {
        if (clickedUnit != null) {
            handleRightClickOnUnit(clickedUnit, x, y, isShiftDown);
        } else {
            handleRightClickOnEmptySpace(x, y);
        }
    }
    
    /**
     * Handle right-click on a unit (combat or self-targeting).
     * 
     * @param clickedUnit The unit that was clicked
     * @param x World x coordinate of click
     * @param y World y coordinate of click
     * @param isShiftDown Whether Shift key was held
     */
    private void handleRightClickOnUnit(Unit clickedUnit, double x, double y, boolean isShiftDown) {
        if (selectionManager.isUnitSelected(clickedUnit) && selectionManager.getSelectionCount() == 1) {
            // Right-click on self (single selection) - cease fire or ready weapon
            if (callbacks.isEditMode()) {
                System.out.println(">>> Combat actions disabled in edit mode");
                return;
            }
            if (clickedUnit.character.isIncapacitated()) {
                System.out.println(">>> " + clickedUnit.character.getDisplayName() + " is incapacitated and cannot ready weapon.");
                return;
            }
            
            // Delegate combat operations to CombatCommandProcessor
            // Check if character is currently attacking - if so, cease fire; otherwise ready weapon
            combatCommandProcessor.handleSelfTargetCombat(clickedUnit, gameClock.getCurrentTick(), eventQueue);
        } else if (isShiftDown && selectionManager.hasSelection() && !selectionManager.isUnitSelected(clickedUnit)) {
            // Shift+right-click on different unit - toggle persistent attack for all selected
            handlePersistentAttackToggle(clickedUnit);
        } else if (selectionManager.hasSelection() && !selectionManager.isUnitSelected(clickedUnit)) {
            // Right-click on enemy unit - attack with all selected units
            handleCombatRightClick(clickedUnit, x, y);
        }
    }
    
    /**
     * Handle Shift+right-click for persistent attack toggling.
     * 
     * @param targetUnit The unit to toggle persistent attack on
     */
    private void handlePersistentAttackToggle(Unit targetUnit) {
        if (callbacks.isEditMode()) {
            System.out.println(">>> Combat actions disabled in edit mode");
            return;
        }
        
        for (Unit unit : selectionManager.getSelectedUnits()) {
            if (!unit.character.isIncapacitated()) {
                unit.character.setPersistentAttack(!unit.character.isPersistentAttack());
                unit.character.currentTarget = targetUnit;
                
                // Make unit face the target
                unit.setTargetFacing(targetUnit.x, targetUnit.y);
                
                if (unit.character.isPersistentAttack()) {
                    // The callbacks object implements both interfaces, so we can cast safely
                    unit.character.startAttackSequence(unit, targetUnit, gameClock.getCurrentTick(), eventQueue, unit.getId(), (GameCallbacks) callbacks);
                } else {
                    unit.character.currentTarget = null;
                }
            }
        }
        
        boolean newState = selectionManager.hasSelection() && selectionManager.getSelected().character.isPersistentAttack();
        System.out.println(selectionManager.getSelectionCount() + " units " + (newState ? "enable" : "disable") + " persistent attack on " + targetUnit.character.getDisplayName());
    }
    
    /**
     * Handle right-click combat targeting.
     * 
     * @param targetUnit The unit to attack
     * @param x World x coordinate of click
     * @param y World y coordinate of click
     */
    private void handleCombatRightClick(Unit targetUnit, double x, double y) {
        if (callbacks.isEditMode()) {
            // Show range information in edit mode
            if (selectionManager.hasSelection()) {
                Unit selected = selectionManager.getSelected();
                double dx = targetUnit.x - selected.x;
                double dy = targetUnit.y - selected.y;
                double distancePixels = Math.hypot(dx, dy);
                double distanceFeet = callbacks.convertPixelsToFeet(distancePixels);
                
                // Delegate range check display to DisplayCoordinator
                displayCoordinator.displayRangeCheck(selected, targetUnit, distanceFeet);
            }
            return;
        }
        
        // Delegate combat operations to CombatCommandProcessor
        combatCommandProcessor.handleCombatRightClick(x, y, targetUnit, units);
    }
    
    /**
     * Handle right-click on empty space (movement commands).
     * 
     * @param x World x coordinate of click
     * @param y World y coordinate of click
     */
    private void handleRightClickOnEmptySpace(double x, double y) {
        if (!selectionManager.hasSelection()) return;
        
        if (callbacks.isEditMode()) {
            handleEditModeMovement(x, y);
        } else {
            handleNormalMovement(x, y);
        }
    }
    
    /**
     * Handle movement in edit mode (instant teleport).
     * 
     * @param x World x coordinate of destination
     * @param y World y coordinate of destination
     */
    private void handleEditModeMovement(double x, double y) {
        // Instant teleport in edit mode
        // Recalculate selection center to account for unit movement
        selectionManager.calculateSelectionCenter();
        for (Unit unit : selectionManager.getSelectedUnits()) {
            double deltaX = x - selectionManager.getSelectionCenterX();
            double deltaY = y - selectionManager.getSelectionCenterY();
            unit.x = unit.x + deltaX;
            unit.y = unit.y + deltaY;
            unit.targetX = unit.x;
            unit.targetY = unit.y;
            unit.hasTarget = false;
            unit.isStopped = false;
        }
        // Delegate unit movement display to DisplayCoordinator
        displayCoordinator.displayUnitMovement(selectionManager.getSelectionCount(), x, y, true);
    }
    
    /**
     * Handle movement in normal mode (with movement rules).
     * 
     * @param x World x coordinate of destination
     * @param y World y coordinate of destination
     */
    private void handleNormalMovement(double x, double y) {
        // Normal movement with movement rules - relative to selection center
        // Recalculate selection center to account for unit movement
        selectionManager.calculateSelectionCenter();
        double deltaX = x - selectionManager.getSelectionCenterX();
        double deltaY = y - selectionManager.getSelectionCenterY();
        
        for (Unit unit : selectionManager.getSelectedUnits()) {
            if (!unit.character.isIncapacitated()) {
                // Cancel any ongoing melee movement when new movement command is given
                if (unit.character.isMovingToMelee) {
                    unit.character.isMovingToMelee = false;
                    unit.character.meleeTarget = null;
                }
                
                double newTargetX = unit.x + deltaX;
                double newTargetY = unit.y + deltaY;
                unit.setTarget(newTargetX, newTargetY);
            }
        }
        // Delegate unit movement display to DisplayCoordinator
        displayCoordinator.displayUnitMovement(selectionManager.getSelectionCount(), x, y, false);
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Utility Methods
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Find unit at the specified coordinates.
     * 
     * @param x World x coordinate
     * @param y World y coordinate
     * @return Unit at coordinates, or null if none found
     */
    private Unit findUnitAt(double x, double y) {
        for (Unit u : units) {
            if (u.contains(x, y)) {
                return u;
            }
        }
        return null;
    }
    
    /**
     * Display enhanced character stats for selected unit.
     * This method delegates to the DisplayCoordinator for consistent display formatting.
     * 
     * @param unit The unit to display stats for
     */
    private void displayEnhancedCharacterStats(Unit unit) {
        // For now, use a simple implementation until DisplayCoordinator has this method
        System.out.println("Selected: " + unit.id + ":" + unit.character.nickname);
    }
    
    /**
     * Display multi-character selection information.
     * This method delegates to the DisplayCoordinator for consistent display formatting.
     */
    private void displayMultiCharacterSelection() {
        // Delegate to DisplayCoordinator for proper formatting
        displayCoordinator.displayMultiCharacterSelection();
    }
}
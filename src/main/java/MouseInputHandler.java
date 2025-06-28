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
    
    /** Movement controller for unit movement operations */
    private final MovementController movementController;
    
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
     * @param movementController Movement controller for unit movement operations
     */
    public MouseInputHandler(List<Unit> units, SelectionManager selectionManager,
                           GameRenderer gameRenderer, DisplayCoordinator displayCoordinator,
                           InputEventRouter eventRouter, EditModeManager editModeManager,
                           CombatCommandProcessor combatCommandProcessor, GameClock gameClock,
                           java.util.PriorityQueue<game.ScheduledEvent> eventQueue,
                           InputManagerCallbacks callbacks, MovementController movementController) {
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
        this.movementController = movementController;
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
            false, // Deployment placement mode no longer exists
            editModeManager.isInDirectAdditionPlacementMode(),
            callbacks.isEditMode());
        
        switch (route) {
            case DEPLOYMENT_PLACEMENT:
                // Deployment feature removed - should not reach here
                System.out.println("*** Error: Deployment placement is no longer supported ***");
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
            handleRightClick(clickedUnit, x, y, e.isShiftDown(), e.isControlDown());
        }
    }
    
    /**
     * Handle right-click actions based on context.
     * 
     * @param clickedUnit Unit that was clicked, or null if empty space
     * @param x World x coordinate of click
     * @param y World y coordinate of click
     * @param isShiftDown Whether Shift key was held
     * @param isControlDown Whether Ctrl key was held
     */
    private void handleRightClick(Unit clickedUnit, double x, double y, boolean isShiftDown, boolean isControlDown) {
        if (clickedUnit != null) {
            handleRightClickOnUnit(clickedUnit, x, y, isShiftDown, isControlDown);
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
     * @param isControlDown Whether Ctrl key was held
     */
    private void handleRightClickOnUnit(Unit clickedUnit, double x, double y, boolean isShiftDown, boolean isControlDown) {
        if (selectionManager.isUnitSelected(clickedUnit) && selectionManager.getSelectionCount() == 1) {
            // Right-click on self (single selection) - cease fire, cancel reaction, or ready weapon
            if (callbacks.isEditMode()) {
                System.out.println(">>> Combat actions disabled in edit mode");
                return;
            }
            if (clickedUnit.character.isIncapacitated()) {
                System.out.println(">>> " + clickedUnit.character.getDisplayName() + " is incapacitated and cannot ready weapon.");
                return;
            }
            
            // DevCycle 28: Check if character has an active reaction to cancel
            if (clickedUnit.character.reactionTarget != null) {
                String targetName = clickedUnit.character.reactionTarget.getCharacter().getDisplayName();
                clickedUnit.character.reactionTarget = null;
                clickedUnit.character.reactionBaselineState = null;
                clickedUnit.character.reactionTriggerTick = -1;
                System.out.println("*** " + clickedUnit.character.getDisplayName() + 
                                 " cancelled reaction to " + targetName + " ***");
                return;
            }
            
            // Delegate combat operations to CombatCommandProcessor
            // Check if character is currently attacking - if so, cease fire; otherwise ready weapon
            combatCommandProcessor.handleSelfTargetCombat(clickedUnit, gameClock.getCurrentTick(), eventQueue);
        } else if (isControlDown && isShiftDown && selectionManager.hasSelection() && !selectionManager.isUnitSelected(clickedUnit)) {
            // DevCycle 28: Ctrl+Shift+right-click on unit - set up reaction action
            handleReactionSetup(clickedUnit);
        } else if (isShiftDown && selectionManager.hasSelection() && !selectionManager.isUnitSelected(clickedUnit)) {
            // Shift+right-click on different unit - toggle persistent attack for all selected
            handlePersistentAttackToggle(clickedUnit);
        } else if (isControlDown && selectionManager.hasSelection() && !selectionManager.isUnitSelected(clickedUnit)) {
            // Ctrl+right-click on enemy unit - target with hold state for all selected units
            handleHoldStateCombat(clickedUnit, x, y);
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
     * Handle Ctrl+right-click for weapon hold state targeting.
     * 
     * @param targetUnit The unit to target with hold state
     * @param x World x coordinate of click
     * @param y World y coordinate of click
     */
    private void handleHoldStateCombat(Unit targetUnit, double x, double y) {
        if (callbacks.isEditMode()) {
            System.out.println(">>> Combat actions disabled in edit mode");
            return;
        }
        
        for (Unit attackingUnit : selectionManager.getSelectedUnits()) {
            if (!attackingUnit.character.isIncapacitated() && attackingUnit != targetUnit) {
                // Set combat target for attacking unit
                attackingUnit.setCombatTarget(targetUnit);
                
                // Get the character's current hold state
                String holdState = attackingUnit.character.getCurrentWeaponHoldState();
                
                // Start weapon progression to hold state instead of full attack
                startWeaponProgressionToHoldState(attackingUnit, targetUnit, holdState);
                
                System.out.println("*** " + attackingUnit.character.getDisplayName() + 
                                 " targeting " + targetUnit.character.getDisplayName() + 
                                 " with hold state: " + holdState + " ***");
            }
        }
    }
    
    /**
     * Start weapon progression to a specific hold state.
     * 
     * @param attackingUnit The unit to progress weapon state
     * @param targetUnit The target unit
     * @param holdState The state to hold at
     */
    private void startWeaponProgressionToHoldState(Unit attackingUnit, Unit targetUnit, String holdState) {
        // Set target
        attackingUnit.character.currentTarget = targetUnit;
        
        // Make unit face the target
        attackingUnit.faceToward(targetUnit.x, targetUnit.y);
        
        // Start weapon progression to hold state
        scheduleWeaponProgressionToState(attackingUnit, holdState, gameClock.getCurrentTick());
    }
    
    /**
     * Schedule weapon state progression to a specific state.
     * 
     * @param unit The unit to progress
     * @param targetState The state to progress to
     * @param currentTick Current game tick
     */
    private void scheduleWeaponProgressionToState(Unit unit, String targetState, long currentTick) {
        // Use the character's existing weapon ready system but stop at target state
        combat.Character character = unit.character;
        combat.Weapon activeWeapon = character.isMeleeCombatMode() ? character.meleeWeapon : character.weapon;
        
        if (activeWeapon == null) {
            return;
        }
        
        // Initialize weapon state if needed
        if (character.currentWeaponState == null) {
            character.currentWeaponState = activeWeapon.getInitialState();
        }
        
        // Start progression using existing system but with hold state target
        character.targetHoldState = targetState;
        character.scheduleReadyFromCurrentState(unit, currentTick, eventQueue, unit.getId());
    }
    
    /**
     * DevCycle 28: Handle Ctrl+Shift+right-click for reaction action setup.
     * Sets selected units to monitor the target unit for weapon state changes.
     * 
     * @param targetUnit The unit to monitor for weapon state changes
     */
    private void handleReactionSetup(Unit targetUnit) {
        if (callbacks.isEditMode()) {
            System.out.println(">>> Combat actions disabled in edit mode");
            return;
        }
        
        int reactionsSet = 0;
        for (Unit unit : selectionManager.getSelectedUnits()) {
            if (!unit.character.isIncapacitated() && unit != targetUnit) {
                combat.Character character = unit.character;
                
                // Set up reaction monitoring
                character.reactionTarget = targetUnit;
                
                // Record baseline weapon state of target
                if (targetUnit.character.currentWeaponState != null) {
                    character.reactionBaselineState = targetUnit.character.currentWeaponState;
                } else if (targetUnit.character.weapon != null) {
                    // Initialize weapon state if needed
                    character.reactionBaselineState = targetUnit.character.weapon.getInitialState();
                } else {
                    // No weapon state to monitor
                    character.reactionBaselineState = null;
                }
                
                // Clear any pending reaction trigger
                character.reactionTriggerTick = -1;
                
                // Move character to preferred hold state
                if (character.weapon != null) {
                    String preferredHoldState = character.firesFromAimingState ? "aiming" : "pointedfromhip";
                    scheduleWeaponProgressionToState(unit, preferredHoldState, gameClock.getCurrentTick());
                }
                
                // Make unit face the target
                unit.setTargetFacing(targetUnit.x, targetUnit.y);
                
                reactionsSet++;
            }
        }
        
        if (reactionsSet > 0) {
            if (selectionManager.getSelectionCount() == 1) {
                Unit unit = selectionManager.getSelected();
                System.out.println("*** " + unit.character.getDisplayName() + 
                                 " set to react to " + targetUnit.character.getDisplayName() + " ***");
            } else {
                System.out.println("*** " + reactionsSet + " units set to react to " + 
                                 targetUnit.character.getDisplayName() + " ***");
            }
        }
    }
    
    /**
     * Handle right-click on empty space (movement commands).
     * Delegates to MovementController for all movement operations.
     * 
     * @param x World x coordinate of click
     * @param y World y coordinate of click
     */
    private void handleRightClickOnEmptySpace(double x, double y) {
        // Delegate all movement commands to MovementController
        movementController.handleMovementCommand(x, y);
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
        combat.Character character = unit.character;
        
        // Task #12: Get active weapon name based on combat mode
        String weaponName = "None";
        String weaponState = "None";
        if (character.isMeleeCombatMode && character.meleeWeapon != null) {
            weaponName = character.meleeWeapon.getName();
        } else if (!character.isMeleeCombatMode && character.rangedWeapon != null) {
            weaponName = character.rangedWeapon.getName();
        } else if (character.weapon != null) {
            // Fallback to legacy weapon field
            weaponName = character.weapon.getName();
        }
        if (character.currentWeaponState != null) {
            weaponState = character.currentWeaponState.getState();
        }
        
        // Get current position
        String position = String.format("(%.1f, %.1f)", unit.x, unit.y);
        
        // Display enhanced format: Character ID, Health, Weapon, Weapon State, Position
        System.out.println("Selected: " + character.id + ":" + character.nickname + 
                         " | Health: " + character.currentHealth + "/" + character.health + 
                         " | Weapon: " + weaponName + 
                         " | State: " + weaponState + 
                         " | Pos: " + position);
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
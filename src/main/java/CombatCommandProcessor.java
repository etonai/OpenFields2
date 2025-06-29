/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import java.util.List;
import java.util.PriorityQueue;
import combat.*;
import combat.managers.BurstFireManager;
import game.*;
import game.GameCallbacks;

/**
 * CombatCommandProcessor handles all combat-specific input processing and command coordination.
 * 
 * This component was extracted from InputManager as part of DevCycle 15e incremental refactoring.
 * It manages combat-related input operations including firing mode controls, target zone selection,
 * automatic targeting management, weapon ready commands, and combat coordination.
 * 
 * RESPONSIBILITIES:
 * - Firing mode control and cycling (F key)
 * - Target zone selection and management (Shift+right click, Z key)
 * - Automatic targeting toggle (Shift+T)
 * - Weapon ready command processing (R key)
 * - Combat state coordination with weapon systems
 * - High-performance combat input processing
 * 
 * DESIGN PRINCIPLES:
 * - Combat focus: Specialized handling of all combat-related input operations
 * - High performance: Maintain responsive combat input processing for 60 FPS gameplay
 * - System integration: Seamless integration with weapon systems, targeting, and combat logic
 * - Command coordination: Efficient processing of complex multi-unit combat commands
 * - State management: Proper integration with combat state tracking and validation
 * 
 * @author DevCycle 15e - Combat Command Component Extraction
 */
public class CombatCommandProcessor {
    
    // ====================
    // DEPENDENCIES
    // ====================
    
    private final SelectionManager selectionManager;
    private final GameClock gameClock;
    private final PriorityQueue<ScheduledEvent> eventQueue;
    private final GameCallbacks gameCallbacks;
    
    // ====================
    // TARGET ZONE SELECTION STATE
    // ====================
    
    /** True when user is selecting a target zone by dragging */
    private boolean isSelectingTargetZone = false;
    
    /** X coordinate where target zone selection started */
    private double targetZoneStartX = 0;
    
    /** Y coordinate where target zone selection started */
    private double targetZoneStartY = 0;
    
    /** Unit for which target zone is being selected */
    private Unit targetZoneUnit = null;
    
    // ====================
    // CONSTRUCTOR
    // ====================
    
    /**
     * Creates a new CombatCommandProcessor with required dependencies.
     */
    public CombatCommandProcessor(SelectionManager selectionManager, GameClock gameClock,
                                PriorityQueue<ScheduledEvent> eventQueue, GameCallbacks gameCallbacks) {
        this.selectionManager = selectionManager;
        this.gameClock = gameClock;
        this.eventQueue = eventQueue;
        this.gameCallbacks = gameCallbacks;
    }
    
    // ====================
    // COMBAT KEY COMMANDS
    // ====================
    
    /**
     * Handles combat-specific keyboard commands.
     */
    public void handleCombatKeys(KeyEvent e) {
        // Firing mode controls (F key)
        handleFiringModeControls(e);
        
        // Weapon ready command (R key)
        handleWeaponReadyCommand(e);
        
        // Automatic targeting control (Shift+T)
        handleAutomaticTargetingToggle(e);
        
        // Target zone controls (Z key)
        handleTargetZoneControls(e);
        
        // Multiple shot control (CTRL+1)
        handleMultipleShotControl(e);
    }
    
    /**
     * Handle firing mode controls (F key).
     */
    private void handleFiringModeControls(KeyEvent e) {
        // F key - cycle firing mode for selected units (only when not in edit mode to avoid conflict)
        if (e.getCode() == KeyCode.F && !e.isControlDown() && !e.isShiftDown() && selectionManager.hasSelection()) {
            for (Unit unit : selectionManager.getSelectedUnits()) {
                if (!unit.character.isIncapacitated() && unit.character.hasMultipleFiringModes()) {
                    unit.character.cycleFiringMode();
                }
            }
            
            if (selectionManager.getSelectionCount() == 1) {
                Unit unit = selectionManager.getSelected();
                if (unit.character.hasMultipleFiringModes()) {
                    System.out.println("*** " + unit.character.getDisplayName() + " firing mode: " + 
                                     unit.character.getCurrentFiringMode());
                } else {
                    System.out.println("*** " + unit.character.getDisplayName() + " has no selectable firing modes");
                }
            } else {
                int unitsWithModes = 0;
                for (Unit unit : selectionManager.getSelectedUnits()) {
                    if (unit.character.hasMultipleFiringModes()) {
                        unitsWithModes++;
                    }
                }
                if (unitsWithModes > 0) {
                    System.out.println("*** " + unitsWithModes + " units cycled firing modes");
                } else {
                    System.out.println("*** No selected units have selectable firing modes");
                }
            }
        }
    }
    
    /**
     * Handle weapon ready command (R key).
     */
    private void handleWeaponReadyCommand(KeyEvent e) {
        if (e.getCode() == KeyCode.R && selectionManager.hasSelection()) {
            for (Unit unit : selectionManager.getSelectedUnits()) {
                if (!unit.character.isIncapacitated()) {
                    unit.character.startReadyWeaponSequence(unit, gameClock.getCurrentTick(), eventQueue, unit.getId());
                }
            }
            
            if (selectionManager.getSelectionCount() == 1) {
                Unit unit = selectionManager.getSelected();
                System.out.println("READY WEAPON " + unit.character.getDisplayName() + " (Unit ID: " + unit.id + ") - current state: " + 
                                 (unit.character.currentWeaponState != null ? unit.character.currentWeaponState.getState() : "None"));
            } else {
                System.out.println("READY WEAPONS " + selectionManager.getSelectionCount() + " units");
            }
        }
    }
    
    /**
     * Handle automatic targeting toggle (Shift+T).
     */
    private void handleAutomaticTargetingToggle(KeyEvent e) {
        if (e.getCode() == KeyCode.T && e.isShiftDown() && selectionManager.hasSelection()) {
            int enabledCount = 0;
            int disabledCount = 0;
            
            for (Unit unit : selectionManager.getSelectedUnits()) {
                if (!unit.character.isIncapacitated()) {
                    // Toggle automatic targeting state
                    boolean currentState = unit.character.isUsesAutomaticTargeting();
                    boolean newState = !currentState;
                    unit.character.setUsesAutomaticTargeting(newState);
                    
                    if (newState) {
                        enabledCount++;
                    } else {
                        disabledCount++;
                    }
                }
            }
            
            if (selectionManager.getSelectionCount() == 1) {
                Unit unit = selectionManager.getSelected();
                if (!unit.character.isIncapacitated()) {
                    boolean newState = unit.character.isUsesAutomaticTargeting();
                    System.out.println("*** " + unit.character.getDisplayName() + " automatic targeting " + 
                                     (newState ? "ENABLED" : "DISABLED") + " ***");
                }
            } else {
                if (enabledCount > 0 && disabledCount > 0) {
                    System.out.println("*** " + enabledCount + " units automatic targeting ENABLED, " + 
                                     disabledCount + " units automatic targeting DISABLED");
                } else if (enabledCount > 0) {
                    System.out.println("*** " + enabledCount + " units automatic targeting ENABLED");
                } else if (disabledCount > 0) {
                    System.out.println("*** " + disabledCount + " units automatic targeting DISABLED");
                }
            }
        }
    }
    
    /**
     * Handle target zone controls (Z key).
     */
    private void handleTargetZoneControls(KeyEvent e) {
        // Z key - clear target zone for selected character
        if (e.getCode() == KeyCode.Z && selectionManager.getSelectionCount() == 1) {
            Unit selected = selectionManager.getSelected();
            if (selected.character.targetZone != null) {
                selected.character.targetZone = null;
                System.out.println("*** Target zone cleared for " + selected.character.getDisplayName());
            } else {
                System.out.println("*** " + selected.character.getDisplayName() + " has no target zone to clear");
            }
        }
    }
    
    // ====================
    // TARGET ZONE SELECTION
    // ====================
    
    /**
     * Handles target zone selection start (Shift+right click on empty space).
     */
    public void startTargetZoneSelection(double x, double y) {
        if (selectionManager.getSelectionCount() == 1) {
            isSelectingTargetZone = true;
            targetZoneStartX = x;
            targetZoneStartY = y;
            targetZoneUnit = selectionManager.getSelected();
        }
    }
    
    /**
     * Completes target zone selection (Shift+right click end).
     */
    public void completeTargetZoneSelection(double endX, double endY) {
        if (isSelectingTargetZone && targetZoneUnit != null) {
            // Calculate rectangle bounds
            double minX = Math.min(targetZoneStartX, endX);
            double maxX = Math.max(targetZoneStartX, endX);
            double minY = Math.min(targetZoneStartY, endY);
            double maxY = Math.max(targetZoneStartY, endY);
            
            // Only create zone if there's meaningful size (at least 10 pixels)
            if (Math.abs(maxX - minX) >= 10 && Math.abs(maxY - minY) >= 10) {
                // Create target zone as AWT Rectangle for compatibility
                java.awt.Rectangle targetZone = new java.awt.Rectangle(
                    (int) minX, (int) minY,
                    (int) (maxX - minX), (int) (maxY - minY)
                );
                
                targetZoneUnit.character.targetZone = targetZone;
                System.out.println("*** Target zone set for " + targetZoneUnit.character.getDisplayName() + 
                                 " (" + (int)(maxX - minX) + "x" + (int)(maxY - minY) + " pixels)");
            } else {
                System.out.println("*** Target zone too small - not created");
            }
        }
        
        // Reset target zone selection state
        isSelectingTargetZone = false;
        targetZoneUnit = null;
    }
    
    /**
     * Cancels target zone selection in progress.
     */
    public void cancelTargetZoneSelection() {
        isSelectingTargetZone = false;
        targetZoneUnit = null;
    }
    
    /**
     * Returns true if currently selecting a target zone.
     */
    public boolean isSelectingTargetZone() {
        return isSelectingTargetZone;
    }
    
    /**
     * Returns the unit for which target zone is being selected.
     */
    public Unit getTargetZoneUnit() {
        return targetZoneUnit;
    }
    
    /**
     * Returns the start X coordinate of target zone selection.
     */
    public double getTargetZoneStartX() {
        return targetZoneStartX;
    }
    
    /**
     * Returns the start Y coordinate of target zone selection.
     */
    public double getTargetZoneStartY() {
        return targetZoneStartY;
    }
    
    // ====================
    // COMBAT COMMAND COORDINATION
    // ====================
    
    /**
     * Handles right-click combat commands (targeting and attack initiation).
     */
    public boolean handleCombatRightClick(double x, double y, Unit clickedUnit, List<Unit> units) {
        if (!selectionManager.hasSelection()) {
            return false; // No units selected for combat
        }
        
        if (clickedUnit != null) {
            // Right click on unit - initiate combat
            for (Unit attackingUnit : selectionManager.getSelectedUnits()) {
                if (!attackingUnit.character.isIncapacitated() && attackingUnit != clickedUnit) {
                    // Check combat mode first, then distance for melee attacks
                    if (attackingUnit.character.isMeleeCombatMode) {
                        // Character is in melee mode - always initiate melee combat (will move if needed)
                        initiateMeleeCombat(attackingUnit, clickedUnit);
                    } else {
                        // Character is in ranged mode - initiate ranged combat
                        initiateRangedCombat(attackingUnit, clickedUnit);
                    }
                }
            }
            return true; // Combat initiated
        }
        
        return false; // No combat target
    }
    
    /**
     * Initiates ranged combat between attacking unit and target.
     */
    private void initiateRangedCombat(Unit attackingUnit, Unit targetUnit) {
        // Set combat target for attacking unit (do NOT set movement target for ranged attacks)
        attackingUnit.setCombatTarget(targetUnit);
        
        // Schedule ranged attack using CombatCoordinator
        combat.CombatCoordinator.getInstance().startAttackSequence(attackingUnit, targetUnit, gameClock.getCurrentTick(), gameCallbacks);
        
        System.out.println("*** " + attackingUnit.character.getDisplayName() + 
                          " targeting " + targetUnit.character.getDisplayName() + " for ranged attack ***");
    }
    
    /**
     * Initiates melee combat between attacking unit and target.
     */
    private void initiateMeleeCombat(Unit attackingUnit, Unit targetUnit) {
        // Move to target for melee combat
        attackingUnit.setTarget(targetUnit.x, targetUnit.y);
        
        // Set up melee combat state
        attackingUnit.character.meleeTarget = targetUnit;
        attackingUnit.character.isMovingToMelee = true;
        
        // Ready melee weapon if possible
        if (attackingUnit.character.meleeWeapon != null) {
            attackingUnit.character.startReadyWeaponSequence(attackingUnit, gameClock.getCurrentTick(), eventQueue, attackingUnit.getId());
        }
        
        System.out.println("*** " + attackingUnit.character.getDisplayName() + 
                          " moving to engage " + targetUnit.character.getDisplayName() + " in melee combat ***");
    }
    
    // ====================
    // ADVANCED COMBAT FEATURES
    // ====================
    
    /**
     * Handles formation and tactical command processing.
     */
    public void processFormationCommand(String formationType, List<Unit> selectedUnits) {
        // Formation commands would be implemented here
        // This is a placeholder for future tactical command extensions
        System.out.println("*** Formation command: " + formationType + " for " + selectedUnits.size() + " units ***");
    }
    
    /**
     * Handles multi-unit combat coordination.
     */
    public void coordinateMultiUnitCombat(List<Unit> attackingUnits, Unit targetUnit) {
        for (Unit unit : attackingUnits) {
            if (!unit.character.isIncapacitated() && unit != targetUnit) {
                double distance = Math.sqrt(Math.pow(unit.x - targetUnit.x, 2) + 
                                          Math.pow(unit.y - targetUnit.y, 2));
                
                // Convert distance to feet for melee range check
                double distanceFeet = distance / 7.0;
                if (unit.character.meleeWeapon != null && 
                    distanceFeet <= unit.character.meleeWeapon.getTotalReach()) {
                    initiateMeleeCombat(unit, targetUnit);
                } else {
                    initiateRangedCombat(unit, targetUnit);
                }
            }
        }
    }
    
    /**
     * Handles combat feedback and status management.
     */
    public void manageCombatFeedback(Unit unit, String actionType, String result) {
        System.out.println("*** Combat Feedback: " + unit.character.getDisplayName() + 
                          " " + actionType + " - " + result + " ***");
    }
    
    // ====================
    // SELF-TARGET COMBAT OPERATIONS
    // ====================
    
    /**
     * Handles self-targeted combat operations (cease fire or weapon ready).
     */
    public void handleSelfTargetCombat(Unit unit, long currentTick, PriorityQueue<ScheduledEvent> eventQueue) {
        // Check if character is currently attacking - if so, cease fire
        if (unit.character.isAttacking || unit.character.isPersistentAttack()) {
            performCeaseFire(unit, eventQueue);
        } else {
            // Not attacking - ready weapon
            unit.character.startReadyWeaponSequence(unit, currentTick, eventQueue, unit.getId());
            System.out.println("READY WEAPON " + unit.character.getDisplayName() + " (Unit ID: " + unit.id + ") - current state: " + unit.character.currentWeaponState.getState());
        }
    }
    
    /**
     * Perform cease fire command for the specified unit.
     */
    private void performCeaseFire(Unit unit, PriorityQueue<ScheduledEvent> eventQueue) {
        combat.Character character = unit.character;
        
        // Cancel all scheduled events for this unit (attacks, bursts, auto fire)
        java.util.Iterator<ScheduledEvent> iterator = eventQueue.iterator();
        java.util.List<ScheduledEvent> toRemove = new java.util.ArrayList<>();
        
        while (iterator.hasNext()) {
            ScheduledEvent event = iterator.next();
            if (event.getOwnerId() == unit.getId()) {
                toRemove.add(event);
            }
        }
        
        eventQueue.removeAll(toRemove);
        
        // Reset attack state but maintain target and weapon state
        character.isAttacking = false;
        character.persistentAttack = false;
        
        // Reset automatic firing state
        BurstFireManager.getInstance().setAutomaticFiring(character.id, false);
        BurstFireManager.getInstance().setBurstShotsFired(character.id, 0);
        character.savedAimingSpeed = null;
        
        // Maintain weapon in ready state if possible
        if (character.weapon != null && character.currentWeaponState != null) {
            String currentState = character.currentWeaponState.getState();
            if ("aiming".equals(currentState) || "firing".equals(currentState) || "recovering".equals(currentState)) {
                character.currentWeaponState = character.weapon.getStateByName("aiming");
                System.out.println("*** CEASE FIRE: " + character.getDisplayName() + " ceases fire, maintains aiming at " + 
                                 (character.currentTarget != null ? character.currentTarget.getCharacter().getDisplayName() : "last target"));
            } else {
                System.out.println("*** CEASE FIRE: " + character.getDisplayName() + " ceases fire");
            }
        } else {
            System.out.println("*** CEASE FIRE: " + character.getDisplayName() + " ceases fire");
        }
        
        // Log number of cancelled events
        if (!toRemove.isEmpty()) {
            System.out.println("*** Cancelled " + toRemove.size() + " scheduled combat events");
        }
    }
    
    /**
     * Handle multiple shot control (CTRL+1).
     * Cycles through multiple shot count (1-5) for selected characters.
     * Ignored when character is in melee mode.
     */
    private void handleMultipleShotControl(KeyEvent e) {
        // CTRL+1 - cycle multiple shot count for selected units
        if (e.getCode() == KeyCode.DIGIT1 && e.isControlDown() && !e.isShiftDown() && selectionManager.hasSelection()) {
            int targetShotCount = 1; // Default to start
            boolean firstUnit = true;
            
            // Get the target shot count - all selected units will be set to same value
            for (Unit unit : selectionManager.getSelectedUnits()) {
                if (!unit.character.isIncapacitated() && !unit.character.isMeleeCombatMode) {
                    if (firstUnit) {
                        // Get current count from first valid unit and cycle it
                        int currentCount = unit.character.multipleShootCount;
                        targetShotCount = (currentCount % 5) + 1; // Cycle 1->2->3->4->5->1
                        firstUnit = false;
                    }
                    break;
                }
            }
            
            // Apply the target shot count to all selected units
            int unitsChanged = 0;
            for (Unit unit : selectionManager.getSelectedUnits()) {
                if (!unit.character.isIncapacitated() && !unit.character.isMeleeCombatMode) {
                    unit.character.multipleShootCount = targetShotCount;
                    unitsChanged++;
                }
            }
            
            // Provide feedback
            if (unitsChanged > 0) {
                if (selectionManager.getSelectionCount() == 1) {
                    Unit unit = selectionManager.getSelected();
                    if (!unit.character.isMeleeCombatMode) {
                        System.out.println("*** " + unit.character.getDisplayName() + 
                                         " multiple shot count: " + targetShotCount + " ***");
                    }
                } else {
                    System.out.println("*** " + unitsChanged + " units set to " + 
                                     targetShotCount + " shot" + (targetShotCount > 1 ? "s" : "") + " ***");
                }
            } else if (selectionManager.getSelectionCount() == 1 && 
                      selectionManager.getSelected().character.isMeleeCombatMode) {
                // Single unit in melee mode - no feedback (CTRL-1 ignored)
            } else {
                System.out.println("*** No ranged units selected to change shot count ***");
            }
        }
    }
    
    // ====================
    // STATE VALIDATION AND DEBUGGING
    // ====================
    
    /**
     * Validates combat state consistency.
     */
    public boolean validateCombatState() {
        // Check for inconsistent target zone selection state
        if (isSelectingTargetZone && targetZoneUnit == null) {
            System.err.println("Warning: Target zone selection active but no unit assigned");
            cancelTargetZoneSelection();
            return false;
        }
        
        // Check for target zone unit that no longer exists
        if (targetZoneUnit != null && targetZoneUnit.character.isIncapacitated()) {
            System.out.println("Target zone unit incapacitated - cancelling selection");
            cancelTargetZoneSelection();
            return false;
        }
        
        return true;
    }
    
    /**
     * Generates debug information for combat state.
     */
    public String generateCombatStateDebug() {
        StringBuilder debug = new StringBuilder();
        debug.append("COMBAT COMMAND PROCESSOR STATE:\n");
        debug.append("  Target Zone Selection Active: ").append(isSelectingTargetZone).append("\n");
        debug.append("  Target Zone Unit: ").append(targetZoneUnit != null ? targetZoneUnit.character.getDisplayName() : "None").append("\n");
        debug.append("  Target Zone Start: (").append(targetZoneStartX).append(", ").append(targetZoneStartY).append(")\n");
        
        // Add selected units combat status
        if (selectionManager.hasSelection()) {
            debug.append("  Selected Units Combat Status:\n");
            for (Unit unit : selectionManager.getSelectedUnits()) {
                debug.append("    ").append(unit.character.getDisplayName()).append(":\n");
                debug.append("      Firing Mode: ").append(unit.character.getCurrentFiringMode()).append("\n");
                debug.append("      Automatic Targeting: ").append(unit.character.isUsesAutomaticTargeting()).append("\n");
                debug.append("      Has Target Zone: ").append(unit.character.targetZone != null).append("\n");
                debug.append("      Weapon State: ").append(unit.character.currentWeaponState != null ? 
                    unit.character.currentWeaponState.getState() : "None").append("\n");
            }
        }
        
        return debug.toString();
    }
}
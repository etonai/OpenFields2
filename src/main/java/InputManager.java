/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyEvent;

import java.util.List;
import java.util.PriorityQueue;
import java.text.SimpleDateFormat;

import combat.*;
import game.*;
import data.SkillsManager;
import data.SaveGameManager;
import data.UniversalCharacterRegistry;

/**
 * InputManager handles all user input for the OpenFields2 game, including:
 * - Mouse input (selection, movement, combat commands)
 * - Keyboard input (camera controls, game controls, unit commands)
 * - Input state management (save/load prompts, edit mode operations)
 * 
 * This class coordinates with various subsystems to process input appropriately:
 * - SelectionManager for unit selection operations
 * - GameRenderer for camera controls
 * - Game state for combat and movement commands
 * - Save/load systems for game persistence
 * - Edit mode operations for character/weapon management
 */
public class InputManager {
    // Core game dependencies
    private final List<Unit> units;
    private final SelectionManager selectionManager;
    private final GameRenderer gameRenderer;
    private final GameClock gameClock;
    private final PriorityQueue<ScheduledEvent> eventQueue;
    private final Canvas canvas;
    
    // Game state references
    private boolean paused;
    private boolean editMode;
    private int nextUnitId;
    
    // Input state management
    private boolean waitingForSaveSlot = false;
    private boolean waitingForLoadSlot = false;
    private boolean waitingForCharacterCreation = false;
    private boolean waitingForWeaponSelection = false;
    private boolean waitingForFactionSelection = false;
    
    // Target zone selection state
    private boolean isSelectingTargetZone = false;
    private double targetZoneStartX = 0;
    private double targetZoneStartY = 0;
    private Unit targetZoneUnit = null;
    
    // Game management dependencies
    private final SaveGameManager saveGameManager;
    private final UniversalCharacterRegistry characterRegistry;
    
    // Callback interface for operations that require main game access
    private final InputManagerCallbacks callbacks;
    
    /**
     * Interface for callbacks to the main game class for operations that
     * require access to methods or state not directly available to InputManager
     */
    public interface InputManagerCallbacks {
        // Save/Load operations
        void saveGameToSlot(int slot);
        void loadGameFromSlot(int slot);
        void promptForSaveSlot();
        void promptForLoadSlot();
        
        // Character/Weapon/Faction management
        void promptForCharacterCreation();
        void promptForWeaponSelection();
        void promptForFactionSelection();
        void createCharacterFromArchetype(int archetypeIndex);
        void assignWeaponToSelectedUnits(int weaponIndex);
        void assignFactionToSelectedUnits(int factionNumber);
        
        // State accessors/mutators
        boolean isPaused();
        void setPaused(boolean paused);
        boolean isEditMode();
        void setEditMode(boolean editMode);
        int getNextUnitId();
        void setNextUnitId(int nextUnitId);
        
        // Utility methods
        double convertPixelsToFeet(double pixels);
        int convertStatToModifier(int stat);
    }
    
    /**
     * Constructor for InputManager
     * 
     * @param units List of game units
     * @param selectionManager Unit selection manager
     * @param gameRenderer Game renderer for camera controls
     * @param gameClock Game clock for timing
     * @param eventQueue Event queue for scheduled actions
     * @param canvas Game canvas for input event handling
     * @param callbacks Callback interface to main game operations
     */
    public InputManager(List<Unit> units, SelectionManager selectionManager, 
                       GameRenderer gameRenderer, GameClock gameClock,
                       PriorityQueue<ScheduledEvent> eventQueue, Canvas canvas,
                       InputManagerCallbacks callbacks) {
        this.units = units;
        this.selectionManager = selectionManager;
        this.gameRenderer = gameRenderer;
        this.gameClock = gameClock;
        this.eventQueue = eventQueue;
        this.canvas = canvas;
        this.callbacks = callbacks;
        
        // Get manager instances
        this.saveGameManager = SaveGameManager.getInstance();
        this.characterRegistry = UniversalCharacterRegistry.getInstance();
    }
    
    /**
     * Initialize input handlers for the given scene
     * 
     * @param scene JavaFX Scene to attach input handlers to
     */
    public void initializeInputHandlers(Scene scene) {
        setupMouseHandlers();
        setupKeyboardHandlers(scene);
    }
    
    /**
     * Setup mouse event handlers for the canvas
     */
    private void setupMouseHandlers() {
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);
    }
    
    /**
     * Setup keyboard event handlers for the scene
     * 
     * @param scene JavaFX Scene to attach keyboard handlers to
     */
    private void setupKeyboardHandlers(Scene scene) {
        scene.setOnKeyPressed(this::handleKeyPressed);
    }
    
    /**
     * Handle mouse pressed events
     * 
     * @param e MouseEvent
     */
    private void handleMousePressed(MouseEvent e) {
        double x = gameRenderer.screenToWorldX(e.getX());
        double y = gameRenderer.screenToWorldY(e.getY());
        
        if (e.getButton() == MouseButton.PRIMARY) {
            // Left click - single unit selection or start rectangle selection
            Unit clickedUnit = null;
            for (Unit u : units) {
                if (u.contains(x, y)) {
                    clickedUnit = u;
                    break;
                }
            }
            
            if (clickedUnit != null) {
                // Single unit selection
                selectionManager.selectUnit(clickedUnit);
                displayEnhancedCharacterStats(clickedUnit);
            } else {
                // Start rectangle selection
                selectionManager.startRectangleSelection(x, y);
            }
        } else if (e.getButton() == MouseButton.SECONDARY) {
            // Right click - check if this is target zone selection or normal right click
            Unit clickedUnit = null;
            for (Unit u : units) {
                if (u.contains(x, y)) {
                    clickedUnit = u;
                    break;
                }
            }
            
            // Check for Shift+right click target zone selection
            if (e.isShiftDown() && clickedUnit == null && selectionManager.getSelectionCount() == 1) {
                // Shift+right click on empty space with single character selected - start target zone selection
                isSelectingTargetZone = true;
                targetZoneStartX = x;
                targetZoneStartY = y;
                targetZoneUnit = selectionManager.getSelected();
            } else {
                handleRightClick(clickedUnit, x, y, e.isShiftDown());
            }
        }
    }
    
    /**
     * Handle mouse dragged events
     * 
     * @param e MouseEvent
     */
    private void handleMouseDragged(MouseEvent e) {
        if (selectionManager.isSelecting()) {
            double x = gameRenderer.screenToWorldX(e.getX());
            double y = gameRenderer.screenToWorldY(e.getY());
            selectionManager.updateRectangleSelection(x, y);
        }
    }
    
    /**
     * Handle mouse released events
     * 
     * @param e MouseEvent
     */
    private void handleMouseReleased(MouseEvent e) {
        if (selectionManager.isSelecting() && e.getButton() == MouseButton.PRIMARY) {
            // Complete rectangle selection
            selectionManager.completeRectangleSelection(units);
            
            if (selectionManager.hasSelection()) {
                displayMultiCharacterSelection();
            }
        } else if (isSelectingTargetZone && e.getButton() == MouseButton.SECONDARY && e.isShiftDown()) {
            // Complete target zone selection (Shift+right click)
            double x = gameRenderer.screenToWorldX(e.getX());
            double y = gameRenderer.screenToWorldY(e.getY());
            
            completeTargetZoneSelection(x, y);
        }
    }
    
    /**
     * Handle right-click actions based on context
     * 
     * @param clickedUnit Unit that was clicked, or null if empty space
     * @param x World x coordinate of click
     * @param y World y coordinate of click
     * @param isShiftDown Whether Shift key was held
     */
    private void handleRightClick(Unit clickedUnit, double x, double y, boolean isShiftDown) {
        if (clickedUnit != null) {
            // Right-click on a unit
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
                
                // Check if character is currently attacking - if so, cease fire
                if (clickedUnit.character.isAttacking || clickedUnit.character.isPersistentAttack()) {
                    performCeaseFire(clickedUnit);
                } else {
                    // Not attacking - ready weapon
                    clickedUnit.character.startReadyWeaponSequence(clickedUnit, gameClock.getCurrentTick(), eventQueue, clickedUnit.getId());
                    System.out.println("READY WEAPON " + clickedUnit.character.getDisplayName() + " (Unit ID: " + clickedUnit.id + ") - current state: " + clickedUnit.character.currentWeaponState.getState());
                }
            } else if (isShiftDown && selectionManager.hasSelection() && !selectionManager.isUnitSelected(clickedUnit)) {
                // Shift+right-click on different unit - toggle persistent attack for all selected
                if (callbacks.isEditMode()) {
                    System.out.println(">>> Combat actions disabled in edit mode");
                    return;
                }
                
                for (Unit unit : selectionManager.getSelectedUnits()) {
                    if (!unit.character.isIncapacitated()) {
                        unit.character.setPersistentAttack(!unit.character.isPersistentAttack());
                        unit.character.currentTarget = clickedUnit;
                        
                        // Make unit face the target
                        unit.setTargetFacing(clickedUnit.x, clickedUnit.y);
                        
                        if (unit.character.isPersistentAttack()) {
                            // The callbacks object implements both interfaces, so we can cast safely
                            unit.character.startAttackSequence(unit, clickedUnit, gameClock.getCurrentTick(), eventQueue, unit.getId(), (GameCallbacks) callbacks);
                        } else {
                            unit.character.currentTarget = null;
                        }
                    }
                }
                
                boolean newState = selectionManager.hasSelection() && selectionManager.getSelected().character.isPersistentAttack();
                System.out.println(selectionManager.getSelectionCount() + " units " + (newState ? "enable" : "disable") + " persistent attack on " + clickedUnit.character.getDisplayName());
            } else if (selectionManager.hasSelection() && !selectionManager.isUnitSelected(clickedUnit)) {
                // Right-click on enemy unit - attack with all selected units
                if (callbacks.isEditMode()) {
                    // Show range information in edit mode
                    if (selectionManager.hasSelection()) {
                        Unit selected = selectionManager.getSelected();
                        double dx = clickedUnit.x - selected.x;
                        double dy = clickedUnit.y - selected.y;
                        double distancePixels = Math.hypot(dx, dy);
                        double distanceFeet = callbacks.convertPixelsToFeet(distancePixels);
                        
                        System.out.println("*** RANGE CHECK ***");
                        System.out.println("Distance from " + selected.character.getDisplayName() + " to " + clickedUnit.character.getDisplayName() + ": " + 
                                         String.format("%.2f", distanceFeet) + " feet");
                        
                        if (selected.character.weapon != null) {
                            double maxRange = selected.character.weapon.maximumRange;
                            System.out.println("Weapon: " + selected.character.weapon.name + " (max range: " + 
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
                    return;
                }
                
                // Attack with all selected units
                for (Unit unit : selectionManager.getSelectedUnits()) {
                    if (!unit.character.isIncapacitated() && unit != clickedUnit) {
                        // The callbacks object implements both interfaces, so we can cast safely
                        unit.character.startAttackSequence(unit, clickedUnit, gameClock.getCurrentTick(), eventQueue, unit.getId(), (GameCallbacks) callbacks);
                    }
                }
                System.out.println("ATTACK " + selectionManager.getSelectionCount() + " units target " + clickedUnit.character.getDisplayName() + " (Unit ID: " + clickedUnit.id + ")");
            }
        } else {
            // Right-click on empty space - movement command
            if (!selectionManager.hasSelection()) return;
            
            if (callbacks.isEditMode()) {
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
                System.out.println("TELEPORT " + selectionManager.getSelectionCount() + " units to (" + String.format("%.0f", x) + ", " + String.format("%.0f", y) + ")");
            } else {
                // Normal movement with movement rules - relative to selection center
                // Recalculate selection center to account for unit movement
                selectionManager.calculateSelectionCenter();
                double deltaX = x - selectionManager.getSelectionCenterX();
                double deltaY = y - selectionManager.getSelectionCenterY();
                
                for (Unit unit : selectionManager.getSelectedUnits()) {
                    if (!unit.character.isIncapacitated()) {
                        double newTargetX = unit.x + deltaX;
                        double newTargetY = unit.y + deltaY;
                        unit.setTarget(newTargetX, newTargetY);
                    }
                }
                System.out.println("MOVE " + selectionManager.getSelectionCount() + " units to (" + String.format("%.0f", x) + ", " + String.format("%.0f", y) + ")");
            }
        }
    }
    
    /**
     * Handle keyboard input events
     * 
     * @param e KeyEvent
     */
    private void handleKeyPressed(KeyEvent e) {
        // Camera controls
        if (e.getCode() == KeyCode.UP) gameRenderer.adjustOffset(0, 20);
        if (e.getCode() == KeyCode.DOWN) gameRenderer.adjustOffset(0, -20);
        if (e.getCode() == KeyCode.LEFT) gameRenderer.adjustOffset(20, 0);
        if (e.getCode() == KeyCode.RIGHT) gameRenderer.adjustOffset(-20, 0);
        if (e.getCode() == KeyCode.EQUALS || e.getCode() == KeyCode.PLUS) gameRenderer.adjustZoom(1.1);
        if (e.getCode() == KeyCode.MINUS) gameRenderer.adjustZoom(1.0 / 1.1);
        
        // Game controls
        if (e.getCode() == KeyCode.SPACE) {
            boolean newPauseState = !callbacks.isPaused();
            callbacks.setPaused(newPauseState);
            if (newPauseState) {
                System.out.println("***********************");
                System.out.println("*** Game paused at tick " + gameClock.getCurrentTick());
                System.out.println("***********************");
            } else {
                System.out.println("***********************");
                System.out.println("*** Game resumed");
                System.out.println("***********************");
            }
        }
        
        // Debug mode toggle
        if (e.getCode() == KeyCode.D && e.isControlDown()) {
            GameRenderer.setDebugMode(!GameRenderer.isDebugMode());
            System.out.println("***********************");
            System.out.println("*** Debug mode " + (GameRenderer.isDebugMode() ? "ENABLED" : "DISABLED"));
            System.out.println("***********************");
        }
        
        // Edit mode toggle
        if (e.getCode() == KeyCode.E && e.isControlDown()) {
            boolean newEditMode = !callbacks.isEditMode();
            callbacks.setEditMode(newEditMode);
            System.out.println("***********************");
            System.out.println("*** Edit mode " + (newEditMode ? "ENABLED" : "DISABLED"));
            if (newEditMode) {
                System.out.println("*** Combat disabled, instant movement enabled");
            } else {
                System.out.println("*** Combat enabled, normal movement rules apply");
            }
            System.out.println("***********************");
        }
        
        // Edit mode operations
        handleEditModeKeys(e);
        
        // Character stats display
        handleCharacterStatsDisplay(e);
        
        // Movement and aiming controls
        handleMovementControls(e);
        handleAimingControls(e);
        
        // Target zone controls
        handleTargetZoneControls(e);
        
        // Firing mode controls
        handleFiringModeControls(e);
        
        // Weapon ready command
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
        
        // Automatic targeting control
        handleAutomaticTargetingToggle(e);
        
        // Save/Load controls
        handleSaveLoadControls(e);
        
        // Handle prompt responses
        handlePromptInputs(e);
    }
    
    /**
     * Handle edit mode specific key commands
     * 
     * @param e KeyEvent
     */
    private void handleEditModeKeys(KeyEvent e) {
        if (e.getCode() == KeyCode.C && e.isControlDown()) {
            if (callbacks.isEditMode() && !waitingForCharacterCreation && !waitingForSaveSlot && !waitingForLoadSlot && !waitingForWeaponSelection && !waitingForFactionSelection) {
                callbacks.promptForCharacterCreation();
            } else if (!callbacks.isEditMode()) {
                System.out.println("*** Character creation only available in edit mode (Ctrl+E) ***");
            }
        }
        if (e.getCode() == KeyCode.W && e.isControlDown()) {
            if (callbacks.isEditMode() && !waitingForWeaponSelection && !waitingForSaveSlot && !waitingForLoadSlot && !waitingForCharacterCreation && !waitingForFactionSelection) {
                if (selectionManager.hasSelection()) {
                    callbacks.promptForWeaponSelection();
                } else {
                    System.out.println("*** No units selected - select a unit first ***");
                }
            } else if (!callbacks.isEditMode()) {
                System.out.println("*** Weapon selection only available in edit mode (Ctrl+E) ***");
            }
        }
        if (e.getCode() == KeyCode.F && e.isControlDown()) {
            if (callbacks.isEditMode() && !waitingForFactionSelection && !waitingForSaveSlot && !waitingForLoadSlot && !waitingForCharacterCreation && !waitingForWeaponSelection) {
                if (selectionManager.hasSelection()) {
                    callbacks.promptForFactionSelection();
                } else {
                    System.out.println("*** No units selected - select a unit first ***");
                }
            } else if (!callbacks.isEditMode()) {
                System.out.println("*** Faction selection only available in edit mode (Ctrl+E) ***");
            }
        }
    }
    
    /**
     * Handle character stats display (Shift+/)
     * 
     * @param e KeyEvent
     */
    private void handleCharacterStatsDisplay(KeyEvent e) {
        if (e.getCode() == KeyCode.SLASH && e.isShiftDown()) {
            if (selectionManager.getSelectionCount() == 1) {
                Unit selected = selectionManager.getSelected();
                System.out.println("***********************");
                System.out.println("*** CHARACTER STATS ***");
                System.out.println("***********************");
                System.out.println("Character ID: " + selected.character.id);
                System.out.println("Unit ID: " + selected.id);
                System.out.println("Nickname: " + selected.character.nickname);
                System.out.println("Faction: " + selected.character.faction);
                System.out.println("Full Name: " + selected.character.getFullName());
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");
                System.out.println("Birthdate: " + dateFormat.format(selected.character.birthdate));
                System.out.println("Dexterity: " + selected.character.dexterity + " (modifier: " + callbacks.convertStatToModifier(selected.character.dexterity) + ")");
                System.out.println("Strength: " + selected.character.strength + " (modifier: " + callbacks.convertStatToModifier(selected.character.strength) + ")");
                System.out.println("Reflexes: " + selected.character.reflexes + " (modifier: " + callbacks.convertStatToModifier(selected.character.reflexes) + ")");
                System.out.println("Health: " + selected.character.health);
                System.out.println("Coolness: " + selected.character.coolness + " (modifier: " + callbacks.convertStatToModifier(selected.character.coolness) + ")");
                System.out.println("Handedness: " + selected.character.handedness.getDisplayName());
                System.out.println("Base Movement Speed: " + selected.character.baseMovementSpeed + " pixels/second");
                System.out.println("Current Movement: " + selected.character.getCurrentMovementType().getDisplayName() + 
                                 " (" + String.format("%.1f", selected.character.getEffectiveMovementSpeed()) + " pixels/sec)");
                
                // Show movement restrictions if any
                combat.MovementType maxAllowed = selected.character.getMaxAllowedMovementType();
                if (maxAllowed != combat.MovementType.RUN) {
                    if (selected.character.hasBothLegsWounded()) {
                        System.out.println("Movement Restricted: Both legs wounded - CRAWL ONLY, forced prone");
                    } else if (selected.character.hasAnyLegWound()) {
                        System.out.println("Movement Restricted: Leg wound - maximum " + maxAllowed.getDisplayName());
                    }
                }
                
                System.out.println("Current Aiming Speed: " + selected.character.getCurrentAimingSpeed().getDisplayName() + 
                                 " (timing: " + String.format("%.2fx", selected.character.getCurrentAimingSpeed().getTimingMultiplier()) + 
                                 ", accuracy: " + String.format("%+.0f", selected.character.getCurrentAimingSpeed().getAccuracyModifier()) + ")");
                System.out.println("Current Position: " + selected.character.getCurrentPosition().getDisplayName());
                
                // Show weapon ready speed
                double readySpeedMultiplier = selected.character.getWeaponReadySpeedMultiplier();
                int quickdrawLevel = selected.character.getSkillLevel(SkillsManager.QUICKDRAW);
                String quickdrawInfo = quickdrawLevel > 0 ? " (Quickdraw " + quickdrawLevel + ")" : "";
                System.out.println("Weapon Ready Speed: " + String.format("%.2fx", readySpeedMultiplier) + quickdrawInfo + 
                                 " (reflexes: " + String.format("%+d", callbacks.convertStatToModifier(selected.character.reflexes)) + ")");
                
                System.out.println("Incapacitated: " + (selected.character.isIncapacitated() ? "YES" : "NO"));
                System.out.println("Automatic Targeting: " + (selected.character.isUsesAutomaticTargeting() ? "ON" : "OFF"));
                
                if (selected.character.weapon != null) {
                    System.out.println("--- WEAPON ---");
                    System.out.println("Name: " + selected.character.weapon.name);
                    System.out.println("Type: " + selected.character.weapon.weaponType.getDisplayName());
                    System.out.println("Damage: " + selected.character.weapon.damage);
                    System.out.println("Accuracy: " + selected.character.weapon.weaponAccuracy);
                    System.out.println("Max Range: " + selected.character.weapon.maximumRange + " feet");
                    System.out.println("Velocity: " + selected.character.weapon.velocityFeetPerSecond + " feet/second");
                    System.out.println("Ammunition: " + selected.character.weapon.ammunition);
                    System.out.println("Current State: " + (selected.character.currentWeaponState != null ? selected.character.currentWeaponState.getState() : "None"));
                } else {
                    System.out.println("--- WEAPON ---");
                    System.out.println("No weapon equipped");
                }
                
                if (!selected.character.getSkills().isEmpty()) {
                    System.out.println("--- SKILLS ---");
                    for (combat.Skill skill : selected.character.getSkills()) {
                        System.out.println(skill.getSkillName() + ": " + skill.getLevel());
                    }
                } else {
                    System.out.println("--- SKILLS ---");
                    System.out.println("No skills");
                }
                
                if (!selected.character.wounds.isEmpty()) {
                    System.out.println("--- WOUNDS ---");
                    for (combat.Wound wound : selected.character.wounds) {
                        System.out.println(wound.getBodyPart().name().toLowerCase() + ": " + wound.getSeverity().name().toLowerCase() + 
                                         ", " + wound.getDamage() + " damage (from " + wound.getProjectileName() + ", weapon: " + wound.getWeaponId() + ")");
                    }
                } else {
                    System.out.println("--- WOUNDS ---");
                    System.out.println("No wounds");
                }
                
                // Combat Experience Display
                System.out.println("--- COMBAT EXPERIENCE ---");
                System.out.println("Combat Engagements: " + selected.character.getCombatEngagements());
                System.out.println("Wounds Received: " + selected.character.getWoundsReceived());
                System.out.println("Wounds Inflicted: " + selected.character.getTotalWoundsInflicted() + " total (" + 
                                 selected.character.getWoundsInflictedByType(combat.WoundSeverity.SCRATCH) + " scratch, " +
                                 selected.character.getWoundsInflictedByType(combat.WoundSeverity.LIGHT) + " light, " +
                                 selected.character.getWoundsInflictedByType(combat.WoundSeverity.SERIOUS) + " serious, " +
                                 selected.character.getWoundsInflictedByType(combat.WoundSeverity.CRITICAL) + " critical)");
                System.out.println("Attacks: " + selected.character.getAttacksAttempted() + " attempted, " + 
                                 selected.character.getAttacksSuccessful() + " successful (" + 
                                 String.format("%.1f", selected.character.getAccuracyPercentage()) + "% accuracy)");
                System.out.println("Targets Incapacitated: " + selected.character.getTargetsIncapacitated());
                System.out.println("***********************");
            } else if (!selectionManager.hasSelection()) {
                System.out.println("*** No character selected - select a character first ***");
            } else {
                System.out.println("*** Character stats unavailable for multiple unit selection ***");
            }
        }
    }
    
    /**
     * Handle movement type controls (W/S keys)
     * 
     * @param e KeyEvent
     */
    private void handleMovementControls(KeyEvent e) {
        // Movement type controls - W to increase, S to decrease
        if (e.getCode() == KeyCode.W && selectionManager.hasSelection()) {
            for (Unit unit : selectionManager.getSelectedUnits()) {
                if (!unit.character.isIncapacitated()) {
                    combat.MovementType previousType = unit.character.getCurrentMovementType();
                    unit.character.increaseMovementType();
                    combat.MovementType newType = unit.character.getCurrentMovementType();
                    
                    // Resume movement if stopped and speed was increased
                    if (unit.isStopped) {
                        unit.resumeMovement();
                    }
                }
            }
            
            if (selectionManager.getSelectionCount() == 1) {
                Unit unit = selectionManager.getSelected();
                combat.MovementType newType = unit.character.getCurrentMovementType();
                System.out.println("*** " + unit.character.getDisplayName() + " movement increased to " + newType.getDisplayName() + 
                                 " (speed: " + String.format("%.1f", unit.character.getEffectiveMovementSpeed()) + " pixels/sec)");
            } else {
                System.out.println("*** " + selectionManager.getSelectionCount() + " units movement speed increased");
            }
        }
        if (e.getCode() == KeyCode.S && selectionManager.hasSelection()) {
            for (Unit unit : selectionManager.getSelectedUnits()) {
                if (!unit.character.isIncapacitated()) {
                    combat.MovementType previousType = unit.character.getCurrentMovementType();
                    
                    // If already at crawling speed and currently moving, stop movement
                    if (previousType == combat.MovementType.CRAWL && unit.isMoving()) {
                        unit.stopMovement();
                    } else {
                        // Otherwise, decrease movement type normally
                        unit.character.decreaseMovementType();
                    }
                }
            }
            
            if (selectionManager.getSelectionCount() == 1) {
                Unit unit = selectionManager.getSelected();
                combat.MovementType newType = unit.character.getCurrentMovementType();
                System.out.println("*** " + unit.character.getDisplayName() + " movement decreased to " + newType.getDisplayName() + 
                                 " (speed: " + String.format("%.1f", unit.character.getEffectiveMovementSpeed()) + " pixels/sec)");
            } else {
                System.out.println("*** " + selectionManager.getSelectionCount() + " units movement speed decreased");
            }
        }
    }
    
    /**
     * Handle aiming speed controls (Q/E keys)
     * 
     * @param e KeyEvent
     */
    private void handleAimingControls(KeyEvent e) {
        // Aiming speed controls - Q to increase, E to decrease
        if (e.getCode() == KeyCode.Q && selectionManager.hasSelection()) {
            for (Unit unit : selectionManager.getSelectedUnits()) {
                if (!unit.character.isIncapacitated()) {
                    unit.character.increaseAimingSpeed();
                }
            }
            
            if (selectionManager.getSelectionCount() == 1) {
                Unit unit = selectionManager.getSelected();
                combat.AimingSpeed newSpeed = unit.character.getCurrentAimingSpeed();
                System.out.println("*** " + unit.character.getDisplayName() + " aiming speed increased to " + newSpeed.getDisplayName() + 
                                 " (timing: " + String.format("%.2fx", newSpeed.getTimingMultiplier()) + ", accuracy: " + String.format("%+.0f", newSpeed.getAccuracyModifier()) + ")");
            } else {
                System.out.println("*** " + selectionManager.getSelectionCount() + " units aiming speed increased");
            }
        }
        if (e.getCode() == KeyCode.E && !e.isControlDown() && selectionManager.hasSelection()) {
            for (Unit unit : selectionManager.getSelectedUnits()) {
                if (!unit.character.isIncapacitated()) {
                    combat.AimingSpeed oldSpeed = unit.character.getCurrentAimingSpeed();
                    unit.character.decreaseAimingSpeed();
                    combat.AimingSpeed newSpeed = unit.character.getCurrentAimingSpeed();
                    
                    // Check if very careful aiming was attempted but not allowed
                    if (oldSpeed == combat.AimingSpeed.CAREFUL && newSpeed == combat.AimingSpeed.CAREFUL && 
                        !unit.character.canUseVeryCarefulAiming()) {
                        if (selectionManager.getSelectionCount() == 1) {
                            if (unit.character.weapon == null) {
                                System.out.println("*** " + unit.character.getDisplayName() + " cannot use very careful aiming (no weapon)");
                            } else if (unit.character.weapon.getWeaponType() == combat.WeaponType.OTHER) {
                                System.out.println("*** " + unit.character.getDisplayName() + " cannot use very careful aiming (weapon type not supported)");
                            } else {
                                String skillName = unit.character.weapon.getWeaponType() == combat.WeaponType.PISTOL ? "Pistol" : "Rifle";
                                int skillLevel = unit.character.getSkillLevel(skillName.toLowerCase());
                                System.out.println("*** " + unit.character.getDisplayName() + " cannot use very careful aiming (" + skillName + " skill level " + skillLevel + ", requires level 1+)");
                            }
                        }
                    }
                }
            }
            
            if (selectionManager.getSelectionCount() == 1) {
                Unit unit = selectionManager.getSelected();
                combat.AimingSpeed newSpeed = unit.character.getCurrentAimingSpeed();
                System.out.println("*** " + unit.character.getDisplayName() + " aiming speed decreased to " + newSpeed.getDisplayName() + 
                                 " (timing: " + String.format("%.2fx", newSpeed.getTimingMultiplier()) + ", accuracy: " + String.format("%+.0f", newSpeed.getAccuracyModifier()) + ")");
            } else {
                System.out.println("*** " + selectionManager.getSelectionCount() + " units aiming speed decreased");
            }
        }
        
        // Position controls: C (crouch down), V (stand up)
        if (e.getCode() == KeyCode.C && !e.isControlDown() && selectionManager.hasSelection()) {
            for (Unit unit : selectionManager.getSelectedUnits()) {
                if (!unit.character.isIncapacitated()) {
                    unit.character.decreasePosition();
                }
            }
            
            if (selectionManager.getSelectionCount() == 1) {
                Unit unit = selectionManager.getSelected();
                combat.PositionState newPosition = unit.character.getCurrentPosition();
                System.out.println("*** " + unit.character.getDisplayName() + " position changed to " + newPosition.getDisplayName());
            } else {
                System.out.println("*** " + selectionManager.getSelectionCount() + " units crouched down");
            }
        }
        
        if (e.getCode() == KeyCode.V && selectionManager.hasSelection()) {
            for (Unit unit : selectionManager.getSelectedUnits()) {
                if (!unit.character.isIncapacitated()) {
                    unit.character.increasePosition();
                }
            }
            
            if (selectionManager.getSelectionCount() == 1) {
                Unit unit = selectionManager.getSelected();
                combat.PositionState newPosition = unit.character.getCurrentPosition();
                System.out.println("*** " + unit.character.getDisplayName() + " position changed to " + newPosition.getDisplayName());
            } else {
                System.out.println("*** " + selectionManager.getSelectionCount() + " units stood up");
            }
        }
    }
    
    /**
     * Handle automatic targeting toggle (Shift+T)
     * 
     * @param e KeyEvent
     */
    private void handleAutomaticTargetingToggle(KeyEvent e) {
        if (e.getCode() == KeyCode.T && e.isShiftDown()) {
            if (selectionManager.hasSelection()) {
                // Toggle each unit individually (units may have different current states)
                int enabledCount = 0;
                int disabledCount = 0;
                
                for (Unit unit : selectionManager.getSelectedUnits()) {
                    if (!unit.character.isIncapacitated()) {
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
                    boolean newState = unit.character.isUsesAutomaticTargeting();
                    System.out.println("*** " + unit.character.getDisplayName() + " automatic targeting " + 
                                     (newState ? "ENABLED" : "DISABLED"));
                } else {
                    if (enabledCount > 0 && disabledCount > 0) {
                        System.out.println("*** " + enabledCount + " units automatic targeting ENABLED, " + 
                                         disabledCount + " units automatic targeting DISABLED");
                    } else if (enabledCount > 0) {
                        System.out.println("*** " + enabledCount + " units automatic targeting ENABLED");
                    } else {
                        System.out.println("*** " + disabledCount + " units automatic targeting DISABLED");
                    }
                }
            } else {
                System.out.println("*** No units selected - select units first ***");
            }
        }
    }
    
    /**
     * Handle save/load controls (Ctrl+S/Ctrl+L)
     * 
     * @param e KeyEvent
     */
    private void handleSaveLoadControls(KeyEvent e) {
        if (e.getCode() == KeyCode.S && e.isControlDown()) {
            if (!waitingForSaveSlot && !waitingForLoadSlot) {
                callbacks.promptForSaveSlot();
            }
        }
        if (e.getCode() == KeyCode.L && e.isControlDown()) {
            if (!waitingForSaveSlot && !waitingForLoadSlot) {
                callbacks.promptForLoadSlot();
            }
        }
    }
    
    /**
     * Handle input responses to prompts (number keys, escape)
     * 
     * @param e KeyEvent
     */
    private void handlePromptInputs(KeyEvent e) {
        // Handle number key input for save/load slot selection, character creation, weapon selection, and faction selection
        if (waitingForSaveSlot || waitingForLoadSlot || waitingForCharacterCreation || waitingForWeaponSelection || waitingForFactionSelection) {
            int slotNumber = -1;
            if (e.getCode() == KeyCode.DIGIT1) slotNumber = 1;
            else if (e.getCode() == KeyCode.DIGIT2) slotNumber = 2;
            else if (e.getCode() == KeyCode.DIGIT3) slotNumber = 3;
            else if (e.getCode() == KeyCode.DIGIT4) slotNumber = 4;
            else if (e.getCode() == KeyCode.DIGIT5) slotNumber = 5;
            else if (e.getCode() == KeyCode.DIGIT6) slotNumber = 6;
            else if (e.getCode() == KeyCode.DIGIT7) slotNumber = 7;
            else if (e.getCode() == KeyCode.DIGIT8) slotNumber = 8;
            else if (e.getCode() == KeyCode.DIGIT9) slotNumber = 9;
            else if (e.getCode() == KeyCode.DIGIT0) slotNumber = 0;
            else if (e.getCode() == KeyCode.ESCAPE) {
                if (waitingForCharacterCreation) {
                    System.out.println("*** Character creation cancelled ***");
                    waitingForCharacterCreation = false;
                } else if (waitingForWeaponSelection) {
                    System.out.println("*** Weapon selection cancelled ***");
                    waitingForWeaponSelection = false;
                } else if (waitingForFactionSelection) {
                    System.out.println("*** Faction selection cancelled ***");
                    waitingForFactionSelection = false;
                } else {
                    System.out.println("*** Save/Load cancelled ***");
                    waitingForSaveSlot = false;
                    waitingForLoadSlot = false;
                }
            }
            
            if (slotNumber >= 0 && slotNumber <= 9) {
                if (waitingForSaveSlot) {
                    if (slotNumber >= 1 && slotNumber <= 9) {
                        callbacks.saveGameToSlot(slotNumber);
                    } else {
                        System.out.println("*** Invalid save slot. Use 1-9 ***");
                    }
                } else if (waitingForLoadSlot) {
                    if (slotNumber == 0) {
                        System.out.println("*** Load cancelled ***");
                        waitingForLoadSlot = false;
                    } else if (slotNumber >= 1 && slotNumber <= 9) {
                        callbacks.loadGameFromSlot(slotNumber);
                    } else {
                        System.out.println("*** Invalid load slot. Use 1-9 or 0 to cancel ***");
                    }
                } else if (waitingForCharacterCreation) {
                    if (slotNumber == 0) {
                        System.out.println("*** Character creation cancelled ***");
                        waitingForCharacterCreation = false;
                    } else if (slotNumber >= 1 && slotNumber <= 9) {
                        callbacks.createCharacterFromArchetype(slotNumber);
                    } else {
                        System.out.println("*** Invalid archetype selection. Use 1-9 or 0 to cancel ***");
                    }
                } else if (waitingForWeaponSelection) {
                    if (slotNumber == 0) {
                        System.out.println("*** Weapon selection cancelled ***");
                        waitingForWeaponSelection = false;
                    } else {
                        callbacks.assignWeaponToSelectedUnits(slotNumber);
                    }
                } else if (waitingForFactionSelection) {
                    if (slotNumber == 0) {
                        System.out.println("*** Faction selection cancelled ***");
                        waitingForFactionSelection = false;
                    } else {
                        callbacks.assignFactionToSelectedUnits(slotNumber);
                    }
                }
            }
        }
    }
    
    // State management methods for coordination with main class
    public void setWaitingForSaveSlot(boolean waiting) {
        this.waitingForSaveSlot = waiting;
    }
    
    public void setWaitingForLoadSlot(boolean waiting) {
        this.waitingForLoadSlot = waiting;
    }
    
    public void setWaitingForCharacterCreation(boolean waiting) {
        this.waitingForCharacterCreation = waiting;
    }
    
    public void setWaitingForWeaponSelection(boolean waiting) {
        this.waitingForWeaponSelection = waiting;
    }
    
    public void setWaitingForFactionSelection(boolean waiting) {
        this.waitingForFactionSelection = waiting;
    }
    
    public boolean isWaitingForInput() {
        return waitingForSaveSlot || waitingForLoadSlot || waitingForCharacterCreation || 
               waitingForWeaponSelection || waitingForFactionSelection;
    }
    
    /**
     * Display enhanced character stats for single character selection
     */
    private void displayEnhancedCharacterStats(Unit unit) {
        combat.Character character = unit.character;
        
        // Single character selection format: "Selected: ID:Nickname"
        System.out.println("Selected: " + unit.id + ":" + character.nickname);
        
        // Status Line 1: Health, Faction, Weapon Name, Position State
        String weaponName = (character.weapon != null) ? character.weapon.getName() : "None";
        String factionName = getFactionDisplayName(character.faction);
        String positionName = character.getCurrentPosition().getDisplayName();
        
        System.out.println("Health: " + character.currentHealth + "/" + character.health + 
                         ", Faction: " + factionName + 
                         ", Weapon: " + weaponName + 
                         ", Position: " + positionName);
        
        // Status Line 2: Current Movement, Aiming Speed, Hesitation Time
        String movementName = character.getCurrentMovementType().getDisplayName();
        String aimingName = character.getCurrentAimingSpeed().getDisplayName();
        
        // Calculate remaining hesitation time
        long currentTick = gameClock.getCurrentTick();
        long remainingHesitation = Math.max(0, character.hesitationEndTick - currentTick);
        long remainingBravery = Math.max(0, character.braveryPenaltyEndTick - currentTick);
        double hesitationSeconds = remainingHesitation / 60.0; // Convert ticks to seconds
        double braverySeconds = remainingBravery / 60.0;
        
        System.out.println("Movement: " + movementName + 
                         ", Aiming: " + aimingName + 
                         ", Hesitation: " + String.format("%.1fs", hesitationSeconds) +
                         " (Wound: " + String.format("%.1fs", hesitationSeconds) +
                         ", Bravery: " + String.format("%.1fs", braverySeconds) + ")");
    }
    
    /**
     * Display character IDs and names for multi-character selection
     */
    private void displayMultiCharacterSelection() {
        System.out.print("Selected: ");
        boolean first = true;
        for (Unit unit : selectionManager.getSelectedUnits()) {
            if (!first) {
                System.out.print(", ");
            }
            System.out.print(unit.id + ":" + unit.character.nickname);
            first = false;
        }
        System.out.println();
    }
    
    /**
     * Get display name for faction
     */
    private String getFactionDisplayName(int faction) {
        switch (faction) {
            case 1: return "Red";
            case 2: return "Blue";
            case 3: return "Green";
            case 4: return "Yellow";
            case 5: return "Purple";
            default: return "Faction " + faction;
        }
    }
    
    /**
     * Handle target zone controls
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
    
    /**
     * Handle firing mode controls (F key)
     */
    private void handleFiringModeControls(KeyEvent e) {
        // F key - cycle firing mode for selected units (only when not in edit mode to avoid conflict)
        if (e.getCode() == KeyCode.F && !e.isControlDown() && selectionManager.hasSelection()) {
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
     * Complete target zone selection
     */
    private void completeTargetZoneSelection(double endX, double endY) {
        if (isSelectingTargetZone && targetZoneUnit != null) {
            // Calculate rectangle bounds
            double minX = Math.min(targetZoneStartX, endX);
            double maxX = Math.max(targetZoneStartX, endX);
            double minY = Math.min(targetZoneStartY, endY);
            double maxY = Math.max(targetZoneStartY, endY);
            
            // Only create zone if there's meaningful size (at least 10 pixels)
            if (Math.abs(maxX - minX) > 10 && Math.abs(maxY - minY) > 10) {
                java.awt.Rectangle targetZone = new java.awt.Rectangle(
                    (int)minX, (int)minY, 
                    (int)(maxX - minX), (int)(maxY - minY)
                );
                
                targetZoneUnit.character.targetZone = targetZone;
                System.out.println("*** Target zone set for " + targetZoneUnit.character.getDisplayName() + 
                                 " at (" + (int)minX + "," + (int)minY + ") size " + 
                                 (int)(maxX - minX) + "x" + (int)(maxY - minY));
            } else {
                System.out.println("*** Target zone too small - not created");
            }
        }
        
        // Reset target zone selection state
        isSelectingTargetZone = false;
        targetZoneUnit = null;
    }
    
    /**
     * Perform cease fire command for the specified unit
     */
    private void performCeaseFire(Unit unit) {
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
        character.isAutomaticFiring = false;
        character.burstShotsFired = 0;
        character.savedAimingSpeed = null;
        
        // Maintain weapon in ready state if possible
        if (character.weapon != null && character.currentWeaponState != null) {
            String currentState = character.currentWeaponState.getState();
            if ("aiming".equals(currentState) || "firing".equals(currentState) || "recovering".equals(currentState)) {
                character.currentWeaponState = character.weapon.getStateByName("aiming");
                System.out.println("*** CEASE FIRE: " + character.getDisplayName() + " ceases fire, maintains aiming at " + 
                                 (character.currentTarget != null ? character.currentTarget.character.getDisplayName() : "last target"));
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
}
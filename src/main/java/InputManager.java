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
    private boolean waitingForRangedWeaponSelection = false;
    private boolean waitingForMeleeWeaponSelection = false;
    private boolean waitingForFactionSelection = false;
    private boolean waitingForBatchCharacterCreation = false;
    private boolean waitingForCharacterDeployment = false;
    private boolean waitingForDeletionConfirmation = false;
    private java.util.List<Unit> unitsToDelete = new java.util.ArrayList<>();
    private boolean waitingForVictoryOutcome = false;
    private boolean waitingForScenarioName = false;
    private boolean waitingForThemeSelection = false;
    
    // Manual victory state
    private java.util.List<Integer> scenarioFactions = new java.util.ArrayList<>();
    private java.util.Map<Integer, VictoryOutcome> factionOutcomes = new java.util.HashMap<>();
    private int currentVictoryFactionIndex = 0;
    
    // New scenario state
    private String newScenarioName = "";
    private String newScenarioTheme = "";
    
    // Batch character creation state
    private int batchQuantity = 0;
    private int batchArchetype = 0;
    private int batchFaction = 0;
    private BatchCreationStep batchCreationStep = BatchCreationStep.QUANTITY;
    
    // Individual character creation state
    private String selectedArchetype = "";
    private String selectedRangedWeapon = "";
    private String selectedMeleeWeapon = "";
    private boolean waitingForCharacterRangedWeapon = false;
    private boolean waitingForCharacterMeleeWeapon = false;
    
    // Character deployment state
    private int deploymentFaction = 0;
    private int deploymentQuantity = 0;
    private String deploymentWeapon = "";
    private String deploymentFormation = "";
    private int deploymentSpacing = 35; // Default 5 feet = 35 pixels
    private DeploymentStep deploymentStep = DeploymentStep.FACTION;
    private java.util.List<combat.Character> deploymentCharacters = new java.util.ArrayList<>();
    
    // Batch creation workflow steps
    private enum BatchCreationStep {
        QUANTITY,    // Prompting for quantity
        ARCHETYPE,   // Prompting for archetype selection
        FACTION      // Prompting for faction selection
    }
    
    // Character deployment workflow steps
    private enum DeploymentStep {
        FACTION,     // Prompting for faction selection
        QUANTITY,    // Prompting for quantity to deploy
        WEAPON,      // Prompting for weapon selection
        FORMATION,   // Prompting for formation type
        SPACING,     // Prompting for character spacing
        PLACEMENT    // Click-to-place mode active
    }
    
    // Victory outcome options for factions
    private enum VictoryOutcome {
        VICTORY,     // Faction achieved victory
        DEFEAT,      // Faction was defeated
        PARTICIPANT  // Faction participated but neither won nor lost
    }
    
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
        
        // Scenario management
        void setWindowTitle(String title);
        String[] getAvailableThemes();
        void setCurrentTheme(String themeId);
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
            // Check if we're in deployment placement mode first
            if (isInDeploymentPlacementMode()) {
                handleDeploymentPlacement(x, y);
                return;
            }
            
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
                        // Check if unit is in melee combat mode
                        if (unit.character.isMeleeCombatMode() && unit.character.meleeWeapon != null) {
                            // Handle melee attack
                            startMeleeAttackSequence(unit, clickedUnit);
                        } else {
                            // Handle ranged attack (existing logic)
                            unit.character.startAttackSequence(unit, clickedUnit, gameClock.getCurrentTick(), eventQueue, unit.getId(), (GameCallbacks) callbacks);
                        }
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
        
        // Unit deletion
        handleUnitDeletion(e);
        
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
            if (callbacks.isEditMode() && !isWaitingForInput()) {
                promptForBatchCharacterCreation();
            } else if (!callbacks.isEditMode()) {
                System.out.println("*** Character creation only available in edit mode (Ctrl+E) ***");
            } else if (isWaitingForInput()) {
                System.out.println("*** Please complete current operation before creating characters ***");
            }
        }
        if (e.getCode() == KeyCode.W && e.isControlDown()) {
            if (callbacks.isEditMode() && !isWaitingForInput()) {
                if (selectionManager.hasSelection()) {
                    callbacks.promptForWeaponSelection();
                } else {
                    System.out.println("*** No units selected - select a unit first ***");
                }
            } else if (!callbacks.isEditMode()) {
                System.out.println("*** Weapon selection only available in edit mode (Ctrl+E) ***");
            } else if (isWaitingForInput()) {
                System.out.println("*** Please complete current operation before changing weapons ***");
            }
        }
        if (e.getCode() == KeyCode.F && e.isControlDown()) {
            if (callbacks.isEditMode() && !isWaitingForInput()) {
                if (selectionManager.hasSelection()) {
                    callbacks.promptForFactionSelection();
                } else {
                    System.out.println("*** No units selected - select a unit first ***");
                }
            } else if (!callbacks.isEditMode()) {
                System.out.println("*** Faction selection only available in edit mode (Ctrl+E) ***");
            } else if (isWaitingForInput()) {
                System.out.println("*** Please complete current operation before changing factions ***");
            }
        }
        if (e.getCode() == KeyCode.A && e.isControlDown()) {
            if (callbacks.isEditMode() && !isWaitingForInput()) {
                callbacks.promptForCharacterCreation();
                waitingForCharacterCreation = true;
            } else if (!callbacks.isEditMode()) {
                System.out.println("*** Character creation only available in edit mode (Ctrl+E) ***");
            } else if (isWaitingForInput()) {
                System.out.println("*** Please complete current operation before creating characters ***");
            }
        }
        if (e.getCode() == KeyCode.V && e.isControlDown() && e.isShiftDown()) {
            if (!isWaitingForInput()) {
                promptForManualVictory();
            } else {
                System.out.println("*** Please complete current operation before processing victory ***");
            }
        }
        if (e.getCode() == KeyCode.N && e.isControlDown() && e.isShiftDown()) {
            if (!isWaitingForInput()) {
                promptForNewScenario();
            } else {
                System.out.println("*** Please complete current operation before creating new scenario ***");
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
                    System.out.println("Ammunition: " + (selected.character.weapon instanceof RangedWeapon ? ((RangedWeapon)selected.character.weapon).getAmmunition() + "/" + ((RangedWeapon)selected.character.weapon).getMaxAmmunition() : "N/A"));
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
        
        // Combat mode toggle: M (melee/ranged toggle)
        if (e.getCode() == KeyCode.M && selectionManager.hasSelection()) {
            for (Unit unit : selectionManager.getSelectedUnits()) {
                if (!unit.character.isIncapacitated()) {
                    unit.character.toggleCombatMode();
                }
            }
            
            if (selectionManager.getSelectionCount() == 1) {
                Unit unit = selectionManager.getSelected();
                String modeText = unit.character.isMeleeCombatMode() ? "Melee Combat" : "Ranged Combat";
                System.out.println("*** " + unit.character.getDisplayName() + " switched to " + modeText + " mode");
            } else {
                System.out.println("*** " + selectionManager.getSelectionCount() + " units toggled combat mode");
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
        // Handle deletion confirmation with Y/N keys
        if (waitingForDeletionConfirmation) {
            if (e.getCode() == KeyCode.Y) {
                confirmUnitDeletion();
            } else if (e.getCode() == KeyCode.N || e.getCode() == KeyCode.ESCAPE) {
                cancelUnitDeletion();
            }
            return; // Don't process other input while waiting for deletion confirmation
        }
        
        // Handle victory outcome selection
        if (waitingForVictoryOutcome) {
            int outcomeNumber = -1;
            if (e.getCode() == KeyCode.DIGIT1) outcomeNumber = 1;
            else if (e.getCode() == KeyCode.DIGIT2) outcomeNumber = 2;
            else if (e.getCode() == KeyCode.DIGIT3) outcomeNumber = 3;
            else if (e.getCode() == KeyCode.DIGIT0) outcomeNumber = 0;
            else if (e.getCode() == KeyCode.ESCAPE) outcomeNumber = 0;
            
            if (outcomeNumber >= 0) {
                handleVictoryOutcomeInput(outcomeNumber);
            }
            return; // Don't process other input while waiting for victory outcome
        }
        
        // Handle scenario name input
        if (waitingForScenarioName) {
            if (e.getCode() == KeyCode.ENTER) {
                handleScenarioNameInput();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                cancelNewScenario();
            } else if (e.getCode() == KeyCode.BACK_SPACE && newScenarioName.length() > 0) {
                newScenarioName = newScenarioName.substring(0, newScenarioName.length() - 1);
                System.out.print("\b \b"); // Backspace effect
            } else if (e.getText() != null && !e.getText().isEmpty() && e.getText().matches("[a-zA-Z0-9 \\-_]")) {
                if (newScenarioName.length() < 50) { // Limit name length
                    newScenarioName += e.getText();
                    System.out.print(e.getText());
                }
            }
            return; // Don't process other input while waiting for scenario name
        }
        
        // Handle theme selection
        if (waitingForThemeSelection) {
            int themeNumber = -1;
            if (e.getCode() == KeyCode.DIGIT1) themeNumber = 1;
            else if (e.getCode() == KeyCode.DIGIT2) themeNumber = 2;
            else if (e.getCode() == KeyCode.DIGIT3) themeNumber = 3;
            else if (e.getCode() == KeyCode.DIGIT4) themeNumber = 4;
            else if (e.getCode() == KeyCode.DIGIT5) themeNumber = 5;
            else if (e.getCode() == KeyCode.DIGIT0) themeNumber = 0;
            else if (e.getCode() == KeyCode.ESCAPE) themeNumber = 0;
            
            if (themeNumber >= 0) {
                handleThemeSelectionInput(themeNumber);
            }
            return; // Don't process other input while waiting for theme selection
        }
        
        // Handle number key input for save/load slot selection, character creation, weapon selection, faction selection, batch character creation, and character deployment
        if (waitingForSaveSlot || waitingForLoadSlot || waitingForCharacterCreation || waitingForWeaponSelection || waitingForRangedWeaponSelection || waitingForMeleeWeaponSelection || waitingForFactionSelection || waitingForBatchCharacterCreation || waitingForCharacterDeployment || waitingForCharacterRangedWeapon || waitingForCharacterMeleeWeapon) {
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
                    resetCharacterCreationState();
                } else if (waitingForCharacterRangedWeapon) {
                    System.out.println("*** Character creation cancelled ***");
                    waitingForCharacterRangedWeapon = false;
                    resetCharacterCreationState();
                } else if (waitingForCharacterMeleeWeapon) {
                    System.out.println("*** Character creation cancelled ***");
                    waitingForCharacterMeleeWeapon = false;
                    resetCharacterCreationState();
                } else if (waitingForWeaponSelection) {
                    System.out.println("*** Weapon selection cancelled ***");
                    waitingForWeaponSelection = false;
                } else if (waitingForRangedWeaponSelection) {
                    System.out.println("*** Ranged weapon selection cancelled ***");
                    waitingForRangedWeaponSelection = false;
                } else if (waitingForMeleeWeaponSelection) {
                    System.out.println("*** Melee weapon selection cancelled ***");
                    waitingForMeleeWeaponSelection = false;
                } else if (waitingForFactionSelection) {
                    System.out.println("*** Faction selection cancelled ***");
                    waitingForFactionSelection = false;
                } else if (waitingForBatchCharacterCreation) {
                    System.out.println("*** Batch character creation cancelled ***");
                    waitingForBatchCharacterCreation = false;
                    batchQuantity = 0;
                    batchArchetype = 0;
                    batchFaction = 0;
                    batchCreationStep = BatchCreationStep.QUANTITY;
                } else if (waitingForCharacterDeployment) {
                    System.out.println("*** Character deployment cancelled ***");
                    cancelCharacterDeployment();
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
                        resetCharacterCreationState();
                    } else if (slotNumber >= 1 && slotNumber <= 9) {
                        handleCharacterArchetypeSelection(slotNumber);
                    } else {
                        System.out.println("*** Invalid archetype selection. Use 1-9 or 0 to cancel ***");
                    }
                } else if (waitingForCharacterRangedWeapon) {
                    if (slotNumber == 0) {
                        System.out.println("*** Character creation cancelled ***");
                        waitingForCharacterRangedWeapon = false;
                        resetCharacterCreationState();
                    } else {
                        handleCharacterRangedWeaponSelection(slotNumber);
                    }
                } else if (waitingForCharacterMeleeWeapon) {
                    if (slotNumber == 0) {
                        System.out.println("*** Character creation cancelled ***");
                        waitingForCharacterMeleeWeapon = false;
                        resetCharacterCreationState();
                    } else {
                        handleCharacterMeleeWeaponSelection(slotNumber);
                    }
                } else if (waitingForWeaponSelection) {
                    // Handle weapon type selection (1=Ranged, 2=Melee)
                    if (slotNumber == 0) {
                        System.out.println("*** Weapon selection cancelled ***");
                        waitingForWeaponSelection = false;
                    } else if (slotNumber == 1) {
                        // User chose ranged weapons
                        waitingForWeaponSelection = false;
                        waitingForRangedWeaponSelection = true;
                        ((EditModeController)callbacks).promptForRangedWeaponSelection();
                    } else if (slotNumber == 2) {
                        // User chose melee weapons
                        waitingForWeaponSelection = false;
                        waitingForMeleeWeaponSelection = true;
                        ((EditModeController)callbacks).promptForMeleeWeaponSelection();
                    } else {
                        System.out.println("*** Invalid weapon type selection. Use 1 for Ranged, 2 for Melee, or 0 to cancel ***");
                    }
                } else if (waitingForRangedWeaponSelection) {
                    if (slotNumber == 0) {
                        System.out.println("*** Ranged weapon selection cancelled ***");
                        waitingForRangedWeaponSelection = false;
                    } else {
                        ((EditModeController)callbacks).assignRangedWeaponToSelectedUnits(slotNumber);
                        waitingForRangedWeaponSelection = false;
                    }
                } else if (waitingForMeleeWeaponSelection) {
                    if (slotNumber == 0) {
                        System.out.println("*** Melee weapon selection cancelled ***");
                        waitingForMeleeWeaponSelection = false;
                    } else {
                        ((EditModeController)callbacks).assignMeleeWeaponToSelectedUnits(slotNumber);
                        waitingForMeleeWeaponSelection = false;
                    }
                } else if (waitingForFactionSelection) {
                    if (slotNumber == 0) {
                        System.out.println("*** Faction selection cancelled ***");
                        waitingForFactionSelection = false;
                    } else {
                        callbacks.assignFactionToSelectedUnits(slotNumber);
                    }
                } else if (waitingForBatchCharacterCreation) {
                    handleBatchCharacterCreationInput(slotNumber);
                } else if (waitingForCharacterDeployment) {
                    handleCharacterDeploymentInput(slotNumber);
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
    
    public void setWaitingForBatchCharacterCreation(boolean waiting) {
        this.waitingForBatchCharacterCreation = waiting;
    }
    
    public void setWaitingForCharacterDeployment(boolean waiting) {
        this.waitingForCharacterDeployment = waiting;
    }
    
    public void setWaitingForDeletionConfirmation(boolean waiting) {
        this.waitingForDeletionConfirmation = waiting;
    }
    
    public void setWaitingForVictoryOutcome(boolean waiting) {
        this.waitingForVictoryOutcome = waiting;
    }
    
    public void setWaitingForScenarioName(boolean waiting) {
        this.waitingForScenarioName = waiting;
    }
    
    public void setWaitingForThemeSelection(boolean waiting) {
        this.waitingForThemeSelection = waiting;
    }
    
    public boolean isWaitingForInput() {
        return waitingForSaveSlot || waitingForLoadSlot || waitingForCharacterCreation || 
               waitingForWeaponSelection || waitingForRangedWeaponSelection || waitingForMeleeWeaponSelection ||
               waitingForFactionSelection || waitingForBatchCharacterCreation || 
               waitingForCharacterDeployment || waitingForDeletionConfirmation || waitingForVictoryOutcome ||
               waitingForScenarioName || waitingForThemeSelection || waitingForCharacterRangedWeapon || waitingForCharacterMeleeWeapon;
    }
    
    /**
     * Start the batch character creation workflow
     */
    private void promptForBatchCharacterCreation() {
        waitingForBatchCharacterCreation = true;
        batchCreationStep = BatchCreationStep.QUANTITY;
        batchQuantity = 0;
        batchArchetype = 0;
        batchFaction = 0;
        
        System.out.println("***********************");
        System.out.println("*** BATCH CHARACTER CREATION ***");
        System.out.println("How many characters do you want to create?");
        System.out.println("Enter quantity (1-20, 0 to cancel): ");
    }
    
    /**
     * Handle input during batch character creation workflow
     * 
     * @param inputNumber The number entered by the user
     */
    private void handleBatchCharacterCreationInput(int inputNumber) {
        switch (batchCreationStep) {
            case QUANTITY:
                if (inputNumber == 0) {
                    System.out.println("*** Batch character creation cancelled ***");
                    waitingForBatchCharacterCreation = false;
                    batchQuantity = 0;
                    batchArchetype = 0;
                    batchFaction = 0;
                    batchCreationStep = BatchCreationStep.QUANTITY;
                } else if (inputNumber >= 1 && inputNumber <= 20) {
                    batchQuantity = inputNumber;
                    batchCreationStep = BatchCreationStep.ARCHETYPE;
                    showArchetypeSelection();
                } else {
                    System.out.println("*** Invalid quantity. Use 1-20 or 0 to cancel ***");
                }
                break;
                
            case ARCHETYPE:
                if (inputNumber == 0) {
                    System.out.println("*** Batch character creation cancelled ***");
                    waitingForBatchCharacterCreation = false;
                    batchQuantity = 0;
                    batchArchetype = 0;
                    batchFaction = 0;
                    batchCreationStep = BatchCreationStep.QUANTITY;
                } else if (inputNumber >= 1 && inputNumber <= 9) {
                    batchArchetype = inputNumber;
                    batchCreationStep = BatchCreationStep.FACTION;
                    showFactionSelection();
                } else {
                    System.out.println("*** Invalid archetype selection. Use 1-9 or 0 to cancel ***");
                }
                break;
                
            case FACTION:
                if (inputNumber == 0) {
                    System.out.println("*** Batch character creation cancelled ***");
                    waitingForBatchCharacterCreation = false;
                    batchQuantity = 0;
                    batchArchetype = 0;
                    batchFaction = 0;
                    batchCreationStep = BatchCreationStep.QUANTITY;
                } else if (inputNumber >= 1 && inputNumber <= 9) {
                    batchFaction = inputNumber;
                    createBatchCharacters();
                    // Reset state after creation
                    waitingForBatchCharacterCreation = false;
                    batchQuantity = 0;
                    batchArchetype = 0;
                    batchFaction = 0;
                    batchCreationStep = BatchCreationStep.QUANTITY;
                } else {
                    System.out.println("*** Invalid faction selection. Use 1-9 or 0 to cancel ***");
                }
                break;
        }
    }
    
    /**
     * Show archetype selection menu for batch creation
     */
    private void showArchetypeSelection() {
        System.out.println("***********************");
        System.out.println("*** ARCHETYPE SELECTION ***");
        System.out.println("Creating " + batchQuantity + " characters");
        System.out.println("Select archetype:");
        System.out.println("1. Gunslinger - High dexterity, quick reflexes, pistol specialist");
        System.out.println("2. Soldier - Balanced combat stats, rifle proficiency");
        System.out.println("3. Weighted Random - Randomly generated stats (averaged), no skills");
        System.out.println("4. Scout - High reflexes, stealth and observation skills");
        System.out.println("5. Marksman - Excellent dexterity, rifle specialist, long-range expert");
        System.out.println("6. Brawler - High strength, close combat specialist");
        System.out.println("7. Confederate Soldier - Civil War Confederate with Brown Bess musket");
        System.out.println("8. Union Soldier - Civil War Union with Brown Bess musket");
        System.out.println("9. Balanced - Well-rounded stats for versatile gameplay");
        System.out.println("0. Cancel batch creation");
        System.out.println();
        System.out.println("Enter selection (1-9, 0 to cancel): ");
    }
    
    /**
     * Show faction selection menu for batch creation
     */
    private void showFactionSelection() {
        System.out.println("***********************");
        System.out.println("*** FACTION SELECTION ***");
        System.out.println("Creating " + batchQuantity + " characters");
        System.out.println("Archetype: " + getArchetypeName(batchArchetype));
        System.out.println("Select faction:");
        System.out.println("1. NONE - No faction");
        System.out.println("2. Union - Federal forces");
        System.out.println("3. Confederacy - Confederate forces");
        System.out.println("4. Southern Unionists - Pro-Union Southerners");
        System.out.println("0. Cancel batch creation");
        System.out.println();
        System.out.println("Enter selection (1-4, 0 to cancel): ");
    }
    
    /**
     * Get display name for archetype number
     * 
     * @param archetypeNumber The archetype number (1-9)
     * @return The display name
     */
    private String getArchetypeName(int archetypeNumber) {
        String[] names = {"Gunslinger", "Soldier", "Weighted Random", "Scout", "Marksman", 
                         "Brawler", "Confederate Soldier", "Union Soldier", "Balanced"};
        if (archetypeNumber >= 1 && archetypeNumber <= names.length) {
            return names[archetypeNumber - 1];
        }
        return "Unknown";
    }
    
    /**
     * Create the batch of characters with the selected settings
     */
    private void createBatchCharacters() {
        System.out.println("***********************");
        System.out.println("*** CREATING CHARACTERS ***");
        System.out.println("Quantity: " + batchQuantity);
        System.out.println("Archetype: " + getArchetypeName(batchArchetype));
        System.out.println("Faction: " + getFactionName(batchFaction));
        System.out.println();
        
        // Convert faction number to faction ID (1-based to 0-based for NONE, Union, Confederacy, Southern Unionists)
        int factionId = batchFaction - 1;
        
        int successCount = 0;
        for (int i = 0; i < batchQuantity; i++) {
            try {
                // Create character using the same method but with faction assignment
                createSingleBatchCharacter(batchArchetype, factionId);
                successCount++;
            } catch (Exception e) {
                System.err.println("Failed to create character " + (i + 1) + ": " + e.getMessage());
            }
        }
        
        System.out.println("*** BATCH CREATION COMPLETE ***");
        System.out.println("Successfully created " + successCount + " out of " + batchQuantity + " characters");
        if (successCount < batchQuantity) {
            System.out.println("Failed to create " + (batchQuantity - successCount) + " characters");
        }
        System.out.println("***********************");
    }
    
    /**
     * Create a single character as part of batch creation
     * 
     * @param archetypeIndex The archetype index (1-9)
     * @param factionId The faction ID (0-3)
     */
    private void createSingleBatchCharacter(int archetypeIndex, int factionId) {
        String[] archetypes = {"gunslinger", "soldier", "weighted_random", "scout", "marksman", 
                              "brawler", "confederate_soldier", "union_soldier", "balanced"};
        
        if (archetypeIndex < 1 || archetypeIndex > archetypes.length) {
            throw new IllegalArgumentException("Invalid archetype index: " + archetypeIndex);
        }
        
        String selectedArchetype = archetypes[archetypeIndex - 1];
        
        // Create character using CharacterFactory
        int characterId = data.CharacterFactory.createCharacter(selectedArchetype);
        combat.Character character = characterRegistry.getCharacter(characterId);
        
        if (character != null) {
            // Assign appropriate weapon based on archetype
            String weaponId = getWeaponForArchetype(selectedArchetype);
            character.weapon = data.WeaponFactory.createWeapon(weaponId);
            character.currentWeaponState = character.weapon.getInitialState();
            character.setFaction(factionId);
            
            // Save character to faction file
            data.CharacterPersistenceManager.getInstance().saveCharacter(character);
            
            // Spawn character at camera center with offset
            spawnBatchCharacterUnit(character, selectedArchetype);
            
            System.out.println("Created: " + character.getDisplayName() + " (ID: " + character.id + 
                             ", Faction: " + getFactionName(factionId + 1) + ")");
        } else {
            throw new RuntimeException("Failed to create character from archetype: " + selectedArchetype);
        }
    }
    
    /**
     * Get faction display name by number (1-4)
     */
    private String getFactionName(int factionNumber) {
        switch (factionNumber) {
            case 1: return "NONE";
            case 2: return "Union";
            case 3: return "Confederacy"; 
            case 4: return "Southern Unionists";
            default: return "Unknown";
        }
    }
    
    /**
     * Get weapon ID for archetype (reused from EditModeController pattern)
     */
    private String getWeaponForArchetype(String archetype) {
        switch (archetype.toLowerCase()) {
            case "gunslinger":
            case "brawler":
            case "balanced":
            case "weighted_random":
                return "wpn_colt_peacemaker"; // Pistol
            case "soldier":
            case "scout": 
            case "marksman":
                return "wpn_hunting_rifle"; // Rifle
            case "confederate_soldier":
            case "union_soldier":
                return "wpn_brown_bess"; // Brown Bess musket
            case "medic":
                return "wpn_derringer"; // Backup weapon
            default:
                return "wpn_colt_peacemaker"; // Default fallback
        }
    }
    
    /**
     * Spawn a batch character unit in the game world with offset to avoid collisions
     */
    private void spawnBatchCharacterUnit(combat.Character character, String archetype) {
        // Calculate spawn location at camera center using canvas dimensions
        double baseX = gameRenderer.screenToWorldX(canvas.getWidth() / 2.0);
        double baseY = gameRenderer.screenToWorldY(canvas.getHeight() / 2.0);
        
        // Add offset based on character ID to spread characters out
        double offsetX = (character.id % 5) * 35; // 5 feet spacing horizontally 
        double offsetY = (character.id / 5) * 35; // 5 feet spacing vertically after 5 characters
        
        double spawnX = baseX + offsetX;
        double spawnY = baseY + offsetY;
        
        // Check for collision with existing units and offset if necessary
        boolean collision = true;
        int attempts = 0;
        double finalX = spawnX;
        double finalY = spawnY;
        
        while (collision && attempts < 10) {
            collision = false;
            for (Unit existingUnit : units) {
                double distance = Math.hypot(finalX - existingUnit.x, finalY - existingUnit.y);
                if (distance < 28) { // 4 feet = 28 pixels minimum distance
                    collision = true;
                    finalX += 28; // Offset by 4 feet (28 pixels) in X direction only
                    break;
                }
            }
            attempts++;
        }
        
        // Get color based on archetype
        javafx.scene.paint.Color characterColor = getColorForArchetype(archetype);
        
        // Create and add unit
        int unitId = callbacks.getNextUnitId();
        Unit newUnit = new Unit(character, finalX, finalY, characterColor, unitId);
        callbacks.setNextUnitId(unitId + 1);
        units.add(newUnit);
    }
    
    /**
     * Get color for character archetype (reused from EditModeController pattern)
     */
    private javafx.scene.paint.Color getColorForArchetype(String archetype) {
        switch (archetype.toLowerCase()) {
            case "confederate_soldier":
                return javafx.scene.paint.Color.DARKGRAY; // Confederate dark gray
            case "union_soldier":
                return javafx.scene.paint.Color.BLUE; // Union blue
            default:
                return javafx.scene.paint.Color.CYAN; // Default color for other archetypes
        }
    }
    
    /**
     * Start the character deployment workflow
     */
    private void promptForCharacterDeployment() {
        waitingForCharacterDeployment = true;
        deploymentStep = DeploymentStep.FACTION;
        deploymentFaction = 0;
        deploymentQuantity = 0;
        deploymentWeapon = "";
        deploymentFormation = "";
        deploymentSpacing = 35; // Default 5 feet
        deploymentCharacters.clear();
        
        System.out.println("***********************");
        System.out.println("*** CHARACTER DEPLOYMENT ***");
        System.out.println("Select faction to deploy from:");
        System.out.println("1. NONE - No faction");
        System.out.println("2. Union - Federal forces");
        System.out.println("3. Confederacy - Confederate forces");
        System.out.println("4. Southern Unionists - Pro-Union Southerners");
        System.out.println("0. Cancel deployment");
        System.out.println();
        System.out.println("Enter selection (1-4, 0 to cancel): ");
    }
    
    /**
     * Handle input during character deployment workflow
     * 
     * @param inputNumber The number entered by the user
     */
    private void handleCharacterDeploymentInput(int inputNumber) {
        switch (deploymentStep) {
            case FACTION:
                if (inputNumber == 0) {
                    System.out.println("*** Character deployment cancelled ***");
                    cancelCharacterDeployment();
                } else if (inputNumber >= 1 && inputNumber <= 4) {
                    deploymentFaction = inputNumber - 1; // Convert to 0-based faction ID
                    loadDeploymentCharacters();
                } else {
                    System.out.println("*** Invalid faction selection. Use 1-4 or 0 to cancel ***");
                }
                break;
                
            case QUANTITY:
                if (inputNumber == 0) {
                    System.out.println("*** Character deployment cancelled ***");
                    cancelCharacterDeployment();
                } else if (inputNumber >= 1 && inputNumber <= 20) {
                    if (inputNumber <= deploymentCharacters.size()) {
                        deploymentQuantity = inputNumber;
                        deploymentStep = DeploymentStep.WEAPON;
                        showWeaponSelectionForDeployment();
                    } else {
                        System.out.println("*** Not enough characters available. Maximum: " + deploymentCharacters.size() + " ***");
                    }
                } else {
                    System.out.println("*** Invalid quantity. Use 1-" + Math.min(20, deploymentCharacters.size()) + " or 0 to cancel ***");
                }
                break;
                
            case WEAPON:
                if (inputNumber == 0) {
                    System.out.println("*** Character deployment cancelled ***");
                    cancelCharacterDeployment();
                } else if (inputNumber >= 1 && inputNumber <= getWeaponOptionsCount()) {
                    deploymentWeapon = getWeaponIdByIndex(inputNumber);
                    deploymentStep = DeploymentStep.FORMATION;
                    showFormationSelection();
                } else {
                    System.out.println("*** Invalid weapon selection. Use 1-" + getWeaponOptionsCount() + " or 0 to cancel ***");
                }
                break;
                
            case FORMATION:
                if (inputNumber == 0) {
                    System.out.println("*** Character deployment cancelled ***");
                    cancelCharacterDeployment();
                } else if (inputNumber >= 1 && inputNumber <= 2) {
                    deploymentFormation = (inputNumber == 1) ? "line_right" : "line_down";
                    deploymentStep = DeploymentStep.SPACING;
                    showSpacingSelection();
                } else {
                    System.out.println("*** Invalid formation selection. Use 1-2 or 0 to cancel ***");
                }
                break;
                
            case SPACING:
                if (inputNumber == 0) {
                    System.out.println("*** Character deployment cancelled ***");
                    cancelCharacterDeployment();
                } else if (inputNumber >= 1 && inputNumber <= 9) {
                    deploymentSpacing = inputNumber * 7; // Convert feet to pixels (7 pixels = 1 foot)
                    deploymentStep = DeploymentStep.PLACEMENT;
                    showPlacementInstructions();
                } else {
                    System.out.println("*** Invalid spacing. Use 1-9 feet or 0 to cancel ***");
                }
                break;
        }
    }
    
    /**
     * Load available characters from the selected faction
     */
    private void loadDeploymentCharacters() {
        try {
            data.CharacterPersistenceManager persistenceManager = data.CharacterPersistenceManager.getInstance();
            java.util.List<combat.Character> allCharacters = persistenceManager.loadCharactersFromFaction(deploymentFaction);

            System.out.println("ETONAI Debug: Reached this line");
            // Filter for non-incapacitated characters
            deploymentCharacters.clear();
            for (combat.Character character : allCharacters) {
                if (!character.isIncapacitated()) {
                    deploymentCharacters.add(character);
                }
            }
            
            if (deploymentCharacters.isEmpty()) {
                System.out.println("*** No available characters in " + getFactionName(deploymentFaction + 1) + " faction ***");
                System.out.println("*** Character deployment cancelled ***");
                cancelCharacterDeployment();
            } else {
                deploymentStep = DeploymentStep.QUANTITY;
                showCharacterQuantitySelection();
            }
        } catch (Exception e) {
            System.err.println("*** Error loading characters: " + e.getMessage() + " ***");
            System.out.println("*** Character deployment cancelled ***");
            cancelCharacterDeployment();
        }
    }
    
    /**
     * Show character quantity selection prompt
     */
    private void showCharacterQuantitySelection() {
        System.out.println("***********************");
        System.out.println("*** CHARACTER QUANTITY ***");
        System.out.println("Faction: " + getFactionName(deploymentFaction + 1));
        System.out.println("Available characters: " + deploymentCharacters.size());
        System.out.println();
        
        // Show first few characters for reference
        int showCount = Math.min(5, deploymentCharacters.size());
        System.out.println("Available characters (showing first " + showCount + "):");
        for (int i = 0; i < showCount; i++) {
            combat.Character character = deploymentCharacters.get(i);
            System.out.println("  " + character.getDisplayName() + " (ID: " + character.id + 
                             ", Health: " + character.currentHealth + "/" + character.health + ")");
        }
        if (deploymentCharacters.size() > showCount) {
            System.out.println("  ... and " + (deploymentCharacters.size() - showCount) + " more");
        }
        
        System.out.println();
        System.out.println("How many characters do you want to deploy?");
        System.out.println("Enter quantity (1-" + Math.min(20, deploymentCharacters.size()) + ", 0 to cancel): ");
    }
    
    /**
     * Show weapon selection for deployment
     */
    private void showWeaponSelectionForDeployment() {
        System.out.println("***********************");
        System.out.println("*** WEAPON SELECTION ***");
        System.out.println("Deploying " + deploymentQuantity + " characters from " + getFactionName(deploymentFaction + 1));
        System.out.println("Select weapon for all deployed characters:");
        System.out.println("1. Colt Peacemaker (Pistol) - 6 damage, 150 feet range");
        System.out.println("2. Hunting Rifle (Rifle) - 12 damage, 400 feet range");
        System.out.println("3. Brown Bess Musket (Rifle) - 15 damage, 300 feet range");
        System.out.println("4. Derringer (Pistol) - 4 damage, 50 feet range");
        System.out.println("0. Cancel deployment");
        System.out.println();
        System.out.println("Enter selection (1-4, 0 to cancel): ");
    }
    
    /**
     * Show formation selection
     */
    private void showFormationSelection() {
        System.out.println("***********************");
        System.out.println("*** FORMATION SELECTION ***");
        System.out.println("Deploying " + deploymentQuantity + " characters");
        System.out.println("Weapon: " + getWeaponDisplayName(deploymentWeapon));
        System.out.println("Select formation:");
        System.out.println("1. Line Right - Characters arranged horizontally");
        System.out.println("2. Line Down - Characters arranged vertically");
        System.out.println("0. Cancel deployment");
        System.out.println();
        System.out.println("Enter selection (1-2, 0 to cancel): ");
    }
    
    /**
     * Show spacing selection
     */
    private void showSpacingSelection() {
        System.out.println("***********************");
        System.out.println("*** SPACING SELECTION ***");
        System.out.println("Deploying " + deploymentQuantity + " characters");
        System.out.println("Formation: " + (deploymentFormation.equals("line_right") ? "Line Right" : "Line Down"));
        System.out.println("Select spacing between characters (edge-to-edge):");
        System.out.println("1. 1 foot - Very tight formation (touching)");
        System.out.println("2. 2 feet - Tight formation");
        System.out.println("3. 3 feet - Normal formation");
        System.out.println("4. 4 feet - Loose formation");
        System.out.println("5. 5 feet - Very loose formation (recommended)");
        System.out.println("6. 6 feet - Extended formation");
        System.out.println("7. 7 feet - Wide formation");
        System.out.println("8. 8 feet - Very wide formation");
        System.out.println("9. 9 feet - Maximum spacing");
        System.out.println("0. Cancel deployment");
        System.out.println();
        System.out.println("Enter selection (1-9, 0 to cancel): ");
    }
    
    /**
     * Show placement instructions
     */
    private void showPlacementInstructions() {
        System.out.println("***********************");
        System.out.println("*** PLACEMENT MODE ***");
        System.out.println("Deploying " + deploymentQuantity + " characters");
        System.out.println("Formation: " + (deploymentFormation.equals("line_right") ? "Line Right" : "Line Down"));
        System.out.println("Spacing: " + (deploymentSpacing / 7) + " feet edge-to-edge (" + deploymentSpacing + " pixels)");
        System.out.println("Weapon: " + getWeaponDisplayName(deploymentWeapon));
        System.out.println();
        System.out.println("Click on the battlefield to place the formation.");
        System.out.println("The first character will be placed at the click location.");
        System.out.println("Press ESC to cancel deployment.");
        System.out.println("***********************");
    }
    
    /**
     * Cancel character deployment and reset state
     */
    private void cancelCharacterDeployment() {
        waitingForCharacterDeployment = false;
        deploymentStep = DeploymentStep.FACTION;
        deploymentFaction = 0;
        deploymentQuantity = 0;
        deploymentWeapon = "";
        deploymentFormation = "";
        deploymentSpacing = 35;
        deploymentCharacters.clear();
    }
    
    /**
     * Get weapon options count for deployment
     */
    private int getWeaponOptionsCount() {
        return 4; // Colt Peacemaker, Hunting Rifle, Brown Bess, Derringer
    }
    
    /**
     * Get weapon ID by selection index
     */
    private String getWeaponIdByIndex(int index) {
        switch (index) {
            case 1: return "wpn_colt_peacemaker";
            case 2: return "wpn_hunting_rifle";
            case 3: return "wpn_brown_bess";
            case 4: return "wpn_derringer";
            default: return "wpn_colt_peacemaker";
        }
    }
    
    /**
     * Get weapon display name for UI
     */
    private String getWeaponDisplayName(String weaponId) {
        switch (weaponId) {
            case "wpn_colt_peacemaker": return "Colt Peacemaker (Pistol)";
            case "wpn_hunting_rifle": return "Hunting Rifle (Rifle)";
            case "wpn_brown_bess": return "Brown Bess Musket (Rifle)";
            case "wpn_derringer": return "Derringer (Pistol)";
            default: return "Unknown Weapon";
        }
    }
    
    /**
     * Check if we're in deployment placement mode
     */
    public boolean isInDeploymentPlacementMode() {
        return waitingForCharacterDeployment && deploymentStep == DeploymentStep.PLACEMENT;
    }
    
    /**
     * Handle deployment click placement
     */
    public void handleDeploymentPlacement(double worldX, double worldY) {
        if (!isInDeploymentPlacementMode()) {
            return;
        }
        
        try {
            System.out.println("***********************");
            System.out.println("*** DEPLOYING CHARACTERS ***");
            
            // Deploy characters in formation
            for (int i = 0; i < deploymentQuantity; i++) {
                if (i >= deploymentCharacters.size()) {
                    break; // Safety check
                }
                
                combat.Character character = deploymentCharacters.get(i);
                
                // Calculate position based on formation
                double charX = worldX;
                double charY = worldY;
                
                if (deploymentFormation.equals("line_right")) {
                    // Add character diameter (21 pixels = 3 feet) to spacing for edge-to-edge spacing
                    charX += i * (deploymentSpacing + 21);
                } else { // line_down
                    // Add character diameter (21 pixels = 3 feet) to spacing for edge-to-edge spacing
                    charY += i * (deploymentSpacing + 21);
                }
                
                // Assign weapon
                character.weapon = data.WeaponFactory.createWeapon(deploymentWeapon);
                character.currentWeaponState = character.weapon.getInitialState();
                
                // Get color based on faction
                javafx.scene.paint.Color characterColor = getFactionColor(deploymentFaction);
                
                // Create and add unit
                int unitId = callbacks.getNextUnitId();
                Unit newUnit = new Unit(character, charX, charY, characterColor, unitId);
                callbacks.setNextUnitId(unitId + 1);
                units.add(newUnit);
                
                System.out.println("Deployed: " + character.getDisplayName() + " at (" + 
                                 String.format("%.0f", charX) + ", " + String.format("%.0f", charY) + ")");
            }
            
            System.out.println("*** DEPLOYMENT COMPLETE ***");
            System.out.println("Successfully deployed " + deploymentQuantity + " characters from " + 
                             getFactionName(deploymentFaction + 1) + " faction");
            System.out.println("Formation: " + (deploymentFormation.equals("line_right") ? "Line Right" : "Line Down"));
            System.out.println("Spacing: " + (deploymentSpacing / 7) + " feet edge-to-edge");
            System.out.println("Weapon: " + getWeaponDisplayName(deploymentWeapon));
            System.out.println("***********************");
            
            // Reset deployment state
            cancelCharacterDeployment();
            
        } catch (Exception e) {
            System.err.println("*** Error during deployment: " + e.getMessage() + " ***");
            cancelCharacterDeployment();
        }
    }
    
    /**
     * Get faction color for character display
     */
    private javafx.scene.paint.Color getFactionColor(int factionId) {
        switch (factionId) {
            case 0: return javafx.scene.paint.Color.GRAY;     // NONE
            case 1: return javafx.scene.paint.Color.BLUE;     // Union
            case 2: return javafx.scene.paint.Color.DARKGRAY; // Confederacy
            case 3: return javafx.scene.paint.Color.LIGHTBLUE; // Southern Unionists
            default: return javafx.scene.paint.Color.CYAN;
        }
    }
    
    /**
     * Handle unit deletion (DEL key)
     * 
     * @param e KeyEvent
     */
    private void handleUnitDeletion(KeyEvent e) {
        if (e.getCode() == KeyCode.DELETE) {
            if (!callbacks.isEditMode()) {
                System.out.println("*** Unit deletion is only available in edit mode ***");
                System.out.println("*** Press CTRL+E to enter edit mode ***");
                return;
            }
            
            if (selectionManager.hasSelection() && !isWaitingForInput()) {
                promptForUnitDeletion();
            } else if (!selectionManager.hasSelection()) {
                System.out.println("*** No units selected - select units to delete first ***");
            }
        }
    }
    
    /**
     * Prompt for unit deletion confirmation
     */
    private void promptForUnitDeletion() {
        unitsToDelete.clear();
        unitsToDelete.addAll(selectionManager.getSelectedUnits());
        
        waitingForDeletionConfirmation = true;
        
        System.out.println("***********************");
        System.out.println("*** UNIT DELETION CONFIRMATION ***");
        System.out.println("You are about to delete " + unitsToDelete.size() + " unit(s):");
        
        for (Unit unit : unitsToDelete) {
            System.out.println("  " + unit.character.getDisplayName() + " (ID: " + unit.character.id + 
                             ", Faction: " + getFactionName(unit.character.getFaction() + 1) + ")");
        }
        
        System.out.println();
        System.out.println("WARNING: This will remove units from the current scenario.");
        System.out.println("Character data will be preserved in faction files.");
        System.out.println();
        System.out.println("Are you sure you want to delete these units? (Y/N): ");
    }
    
    /**
     * Confirm unit deletion and perform the deletion
     */
    private void confirmUnitDeletion() {
        System.out.println("***********************");
        System.out.println("*** DELETING UNITS ***");
        
        int deletedCount = 0;
        for (Unit unit : unitsToDelete) {
            try {
                // Cancel any scheduled events for this unit
                cancelScheduledEventsForUnit(unit);
                
                // Remove unit from game world
                units.remove(unit);
                
                System.out.println("Deleted: " + unit.character.getDisplayName() + " (Unit ID: " + unit.id + ")");
                deletedCount++;
            } catch (Exception e) {
                System.err.println("Failed to delete unit " + unit.character.getDisplayName() + ": " + e.getMessage());
            }
        }
        
        // Clear selection since deleted units are no longer valid
        selectionManager.clearSelection();
        
        System.out.println("*** DELETION COMPLETE ***");
        System.out.println("Successfully deleted " + deletedCount + " out of " + unitsToDelete.size() + " units");
        System.out.println("Character data preserved in faction files");
        System.out.println("***********************");
        
        // Reset deletion state
        cancelUnitDeletion();
    }
    
    /**
     * Cancel unit deletion
     */
    private void cancelUnitDeletion() {
        System.out.println("*** Unit deletion cancelled ***");
        waitingForDeletionConfirmation = false;
        unitsToDelete.clear();
    }
    
    /**
     * Cancel any scheduled events for a unit being deleted
     * 
     * @param unit The unit being deleted
     */
    private void cancelScheduledEventsForUnit(Unit unit) {
        // Remove any scheduled events that reference this unit
        eventQueue.removeIf(event -> {
            // Check if event involves this unit (this is a simplified check)
            // In a more complete implementation, we'd need to check event details
            return event.toString().contains("Unit:" + unit.id) || 
                   event.toString().contains(unit.character.getDisplayName());
        });
        
        // Clear any combat state for this unit
        if (unit.character.isAttacking) {
            unit.character.isAttacking = false;
            unit.character.currentTarget = null;
        }
        
        // Clear this unit as a target for other units
        for (Unit otherUnit : units) {
            if (otherUnit.character.currentTarget == unit) {
                otherUnit.character.currentTarget = null;
                otherUnit.character.isAttacking = false;
            }
        }
    }
    
    /**
     * Start the manual victory workflow
     */
    private void promptForManualVictory() {
        // Identify factions in the current scenario
        scenarioFactions.clear();
        factionOutcomes.clear();
        currentVictoryFactionIndex = 0;
        
        java.util.Set<Integer> factionsInScenario = new java.util.HashSet<>();
        for (Unit unit : units) {
            factionsInScenario.add(unit.character.getFaction());
        }
        
        if (factionsInScenario.isEmpty()) {
            System.out.println("*** No factions present in current scenario ***");
            System.out.println("*** Manual victory not applicable ***");
            return;
        }
        
        scenarioFactions.addAll(factionsInScenario);
        
        System.out.println("***********************");
        System.out.println("*** MANUAL VICTORY SYSTEM ***");
        System.out.println("Factions in current scenario: " + scenarioFactions.size());
        
        for (Integer factionId : scenarioFactions) {
            int characterCount = 0;
            for (Unit unit : units) {
                if (unit.character.getFaction() == factionId) {
                    characterCount++;
                }
            }
            System.out.println("  " + getFactionName(factionId + 1) + ": " + characterCount + " characters");
        }
        
        System.out.println();
        System.out.println("You will now assign victory outcomes to each faction.");
        System.out.println("***********************");
        
        // Start the outcome selection workflow
        promptForNextFactionOutcome();
    }
    
    /**
     * Prompt for the next faction's victory outcome
     */
    private void promptForNextFactionOutcome() {
        if (currentVictoryFactionIndex >= scenarioFactions.size()) {
            // All factions processed, execute victory
            executeManualVictory();
            return;
        }
        
        waitingForVictoryOutcome = true;
        int currentFactionId = scenarioFactions.get(currentVictoryFactionIndex);
        
        System.out.println("***********************");
        System.out.println("*** FACTION OUTCOME: " + getFactionName(currentFactionId + 1) + " ***");
        
        // Show characters in this faction
        java.util.List<Unit> factionUnits = new java.util.ArrayList<>();
        for (Unit unit : units) {
            if (unit.character.getFaction() == currentFactionId) {
                factionUnits.add(unit);
            }
        }
        
        System.out.println("Characters in faction (" + factionUnits.size() + " total):");
        for (Unit unit : factionUnits) {
            String status = unit.character.isIncapacitated() ? "INCAPACITATED" : "Active";
            System.out.println("  " + unit.character.getDisplayName() + " (" + status + 
                             ", Health: " + unit.character.currentHealth + "/" + unit.character.health + ")");
        }
        
        System.out.println();
        System.out.println("Select outcome for " + getFactionName(currentFactionId + 1) + ":");
        System.out.println("1. Victory - Faction achieved victory");
        System.out.println("2. Defeat - Faction was defeated");
        System.out.println("3. Participant - Faction participated but neither won nor lost");
        System.out.println("0. Cancel manual victory");
        System.out.println();
        System.out.println("Enter selection (1-3, 0 to cancel): ");
    }
    
    /**
     * Handle input for victory outcome selection
     * 
     * @param outcomeNumber The number entered by the user
     */
    private void handleVictoryOutcomeInput(int outcomeNumber) {
        if (outcomeNumber == 0) {
            System.out.println("*** Manual victory cancelled ***");
            cancelManualVictory();
            return;
        }
        
        if (outcomeNumber < 1 || outcomeNumber > 3) {
            System.out.println("*** Invalid selection. Use 1-3 or 0 to cancel ***");
            return;
        }
        
        // Store the outcome for the current faction
        int currentFactionId = scenarioFactions.get(currentVictoryFactionIndex);
        VictoryOutcome outcome;
        
        switch (outcomeNumber) {
            case 1: outcome = VictoryOutcome.VICTORY; break;
            case 2: outcome = VictoryOutcome.DEFEAT; break;
            case 3: outcome = VictoryOutcome.PARTICIPANT; break;
            default: return; // Should never happen
        }
        
        factionOutcomes.put(currentFactionId, outcome);
        
        String outcomeName = getOutcomeName(outcome);
        System.out.println("*** " + getFactionName(currentFactionId + 1) + " outcome set to: " + outcomeName + " ***");
        
        // Move to next faction
        currentVictoryFactionIndex++;
        waitingForVictoryOutcome = false;
        
        // Continue with next faction or execute victory
        promptForNextFactionOutcome();
    }
    
    /**
     * Execute the manual victory and update all faction and character data
     */
    private void executeManualVictory() {
        System.out.println("***********************");
        System.out.println("*** EXECUTING MANUAL VICTORY ***");
        System.out.println("Processing outcomes for " + scenarioFactions.size() + " factions...");
        
        try {
            data.CharacterPersistenceManager persistenceManager = data.CharacterPersistenceManager.getInstance();
            data.FactionRegistry factionRegistry = data.FactionRegistry.getInstance();
            
            // Update faction statistics and character outcomes
            for (Integer factionId : scenarioFactions) {
                VictoryOutcome outcome = factionOutcomes.get(factionId);
                String outcomeName = getOutcomeName(outcome);
                
                System.out.println("Processing " + getFactionName(factionId + 1) + " (" + outcomeName + ")...");
                
                // Update faction statistics
                try {
                    data.Faction faction = factionRegistry.getFaction(factionId);
                    if (faction != null) {
                        switch (outcome) {
                            case VICTORY:
                                faction.incrementVictories();
                                break;
                            case DEFEAT:
                                faction.incrementDefeats();
                                break;
                            case PARTICIPANT:
                                faction.incrementParticipations();
                                break;
                        }
                        factionRegistry.saveFactionFile(faction);
                    }
                } catch (Exception e) {
                    System.err.println("  Failed to update faction statistics: " + e.getMessage());
                }
                
                // Update character statistics for all characters in this faction in the scenario
                java.util.List<Unit> factionUnits = new java.util.ArrayList<>();
                for (Unit unit : units) {
                    if (unit.character.getFaction() == factionId) {
                        factionUnits.add(unit);
                    }
                }
                
                for (Unit unit : factionUnits) {
                    try {
                        combat.Character character = unit.character;
                        
                        // Update battle participation
                        character.battlesParticipated++;
                        
                        // Update victory/defeat counts based on faction outcome
                        switch (outcome) {
                            case VICTORY:
                                character.victories++;
                                break;
                            case DEFEAT:
                                character.defeats++;
                                break;
                            case PARTICIPANT:
                                // No additional stat changes for participants
                                break;
                        }
                        
                        // Save character data back to faction file
                        persistenceManager.saveCharacter(character);
                        
                        System.out.println("  Updated: " + character.getDisplayName() + 
                                         " (Battles: " + character.battlesParticipated + 
                                         ", Victories: " + character.victories + 
                                         ", Defeats: " + character.defeats + ")");
                        
                    } catch (Exception e) {
                        System.err.println("  Failed to update character " + unit.character.getDisplayName() + ": " + e.getMessage());
                    }
                }
            }
            
            System.out.println("*** VICTORY PROCESSING COMPLETE ***");
            
            // Display summary
            displayVictorySummary();
            
            // End scenario
            endScenario();
            
        } catch (Exception e) {
            System.err.println("*** Error during victory processing: " + e.getMessage() + " ***");
            cancelManualVictory();
        }
    }
    
    /**
     * Display victory summary
     */
    private void displayVictorySummary() {
        System.out.println("***********************");
        System.out.println("*** BATTLE SUMMARY ***");
        
        for (Integer factionId : scenarioFactions) {
            VictoryOutcome outcome = factionOutcomes.get(factionId);
            String outcomeName = getOutcomeName(outcome);
            
            int characterCount = 0;
            int incapacitatedCount = 0;
            for (Unit unit : units) {
                if (unit.character.getFaction() == factionId) {
                    characterCount++;
                    if (unit.character.isIncapacitated()) {
                        incapacitatedCount++;
                    }
                }
            }
            
            System.out.println(getFactionName(factionId + 1) + ": " + outcomeName);
            System.out.println("  Characters: " + characterCount + " total, " + 
                             incapacitatedCount + " incapacitated, " + 
                             (characterCount - incapacitatedCount) + " active");
        }
        
        System.out.println("***********************");
    }
    
    /**
     * End the current scenario after victory processing
     */
    private void endScenario() {
        System.out.println("***********************");
        System.out.println("*** SCENARIO ENDED ***");
        System.out.println("All units cleared from battlefield.");
        System.out.println("Character and faction data saved to files.");
        System.out.println("Ready for new scenario or character operations.");
        System.out.println("***********************");
        
        // Clear all units from the scenario
        units.clear();
        
        // Clear any selections
        selectionManager.clearSelection();
        
        // Clear event queue
        eventQueue.clear();
        
        // Reset victory state
        cancelManualVictory();
    }
    
    /**
     * Cancel manual victory and reset state
     */
    private void cancelManualVictory() {
        waitingForVictoryOutcome = false;
        scenarioFactions.clear();
        factionOutcomes.clear();
        currentVictoryFactionIndex = 0;
    }
    
    /**
     * Get display name for victory outcome
     * 
     * @param outcome The victory outcome
     * @return The display name
     */
    private String getOutcomeName(VictoryOutcome outcome) {
        switch (outcome) {
            case VICTORY: return "VICTORY";
            case DEFEAT: return "DEFEAT";
            case PARTICIPANT: return "PARTICIPANT";
            default: return "UNKNOWN";
        }
    }
    
    /**
     * Start the new scenario workflow
     */
    private void promptForNewScenario() {
        newScenarioName = "";
        newScenarioTheme = "";
        
        System.out.println("***********************");
        System.out.println("*** CREATE NEW SCENARIO ***");
        System.out.println("This will clear all units from the current battlefield.");
        System.out.println("Character data will be preserved in faction files.");
        System.out.println();
        System.out.println("Enter scenario name (or press ESC to cancel): ");
        System.out.print("> ");
        
        waitingForScenarioName = true;
    }
    
    /**
     * Handle scenario name input when ENTER is pressed
     */
    private void handleScenarioNameInput() {
        if (newScenarioName.trim().isEmpty()) {
            System.out.println();
            System.out.println("*** Scenario name cannot be empty. Try again or press ESC to cancel ***");
            System.out.print("> ");
            return;
        }
        
        System.out.println();
        System.out.println("Scenario name: \"" + newScenarioName.trim() + "\"");
        
        waitingForScenarioName = false;
        promptForThemeSelection();
    }
    
    /**
     * Prompt for theme selection
     */
    private void promptForThemeSelection() {
        waitingForThemeSelection = true;
        
        System.out.println("***********************");
        System.out.println("*** THEME SELECTION ***");
        System.out.println("Select a theme for the new scenario:");
        
        String[] themes = callbacks.getAvailableThemes();
        for (int i = 0; i < themes.length; i++) {
            System.out.println((i + 1) + ". " + getThemeDisplayName(themes[i]));
        }
        System.out.println("0. Cancel scenario creation");
        System.out.println();
        System.out.println("Enter selection (1-" + themes.length + ", 0 to cancel): ");
    }
    
    /**
     * Handle theme selection input
     * 
     * @param themeNumber The number entered by the user
     */
    private void handleThemeSelectionInput(int themeNumber) {
        String[] themes = callbacks.getAvailableThemes();
        
        if (themeNumber == 0) {
            cancelNewScenario();
            return;
        }
        
        if (themeNumber < 1 || themeNumber > themes.length) {
            System.out.println("*** Invalid theme selection. Use 1-" + themes.length + " or 0 to cancel ***");
            return;
        }
        
        newScenarioTheme = themes[themeNumber - 1];
        waitingForThemeSelection = false;
        
        System.out.println("Selected theme: " + getThemeDisplayName(newScenarioTheme));
        
        executeNewScenario();
    }
    
    /**
     * Execute the new scenario creation
     */
    private void executeNewScenario() {
        System.out.println("***********************");
        System.out.println("*** CREATING NEW SCENARIO ***");
        System.out.println("Scenario: \"" + newScenarioName.trim() + "\"");
        System.out.println("Theme: " + getThemeDisplayName(newScenarioTheme));
        System.out.println();
        
        try {
            // Clear all units from the battlefield
            int clearedUnits = units.size();
            units.clear();
            
            // Clear any selections
            selectionManager.clearSelection();
            
            // Clear event queue
            eventQueue.clear();
            
            // Set the new theme
            callbacks.setCurrentTheme(newScenarioTheme);
            
            // Update window title with scenario name
            callbacks.setWindowTitle("OpenFields2 - " + newScenarioName.trim());
            
            System.out.println("*** NEW SCENARIO CREATED ***");
            System.out.println("Cleared " + clearedUnits + " units from battlefield");
            System.out.println("Applied theme: " + getThemeDisplayName(newScenarioTheme));
            System.out.println("Updated window title");
            System.out.println("Event queue cleared");
            System.out.println();
            System.out.println("Ready for character creation (CTRL-C) and deployment (CTRL-A)");
            System.out.println("***********************");
            
        } catch (Exception e) {
            System.err.println("*** Error creating new scenario: " + e.getMessage() + " ***");
        } finally {
            // Reset new scenario state
            cancelNewScenario();
        }
    }
    
    /**
     * Cancel new scenario creation and reset state
     */
    private void cancelNewScenario() {
        System.out.println();
        System.out.println("*** Scenario creation cancelled ***");
        waitingForScenarioName = false;
        waitingForThemeSelection = false;
        newScenarioName = "";
        newScenarioTheme = "";
    }
    
    /**
     * Get display name for theme ID
     * 
     * @param themeId The theme ID
     * @return The display name
     */
    private String getThemeDisplayName(String themeId) {
        // Convert theme ID to display name
        switch (themeId.toLowerCase()) {
            case "test_theme":
                return "Test Theme - Basic testing environment";
            case "civil_war":
                return "Civil War - American Civil War setting";
            case "western":
                return "Western - Wild West frontier";
            case "modern":
                return "Modern - Contemporary setting";
            default:
                return themeId + " - Theme";
        }
    }
    
    /**
     * Validate system integrity and display warnings for any issues
     */
    public void validateSystemIntegrity() {
        try {
            System.out.println("***********************");
            System.out.println("*** SYSTEM VALIDATION ***");
            
            // Test faction registry
            data.FactionRegistry factionRegistry = data.FactionRegistry.getInstance();
            if (factionRegistry.getAllFactions().isEmpty()) {
                System.out.println("WARNING: No factions loaded in registry");
            } else {
                System.out.println("✓ Faction registry operational (" + factionRegistry.getAllFactions().size() + " factions)");
            }
            
            // Test character persistence
            data.CharacterPersistenceManager persistenceManager = data.CharacterPersistenceManager.getInstance();
            System.out.println("✓ Character persistence manager operational");
            
            // Test theme manager
            String[] themes = callbacks.getAvailableThemes();
            if (themes.length == 0) {
                System.out.println("WARNING: No themes available");
            } else {
                System.out.println("✓ Theme manager operational (" + themes.length + " themes available)");
            }
            
            // Test weapon factory
            String[] weaponIds = data.WeaponFactory.getAllWeaponIds();
            if (weaponIds.length == 0) {
                System.out.println("WARNING: No weapons available");
            } else {
                System.out.println("✓ Weapon factory operational (" + weaponIds.length + " weapons available)");
            }
            
            System.out.println("*** VALIDATION COMPLETE ***");
            System.out.println("***********************");
            
        } catch (Exception e) {
            System.err.println("*** SYSTEM VALIDATION FAILED: " + e.getMessage() + " ***");
        }
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
    
    /**
     * Start melee attack sequence for a unit attacking a target
     */
    private void startMeleeAttackSequence(Unit attacker, Unit target) {
        MeleeWeapon meleeWeapon = attacker.character.meleeWeapon;
        if (meleeWeapon == null) {
            System.out.println("*** " + attacker.character.getDisplayName() + " has no melee weapon equipped");
            return;
        }
        
        // Check if target is within melee range
        CombatResolver combatResolver = new CombatResolver(units, eventQueue, false);
        if (!combatResolver.isInMeleeRange(attacker, target, meleeWeapon)) {
            double distance = Math.hypot(target.x - attacker.x, target.y - attacker.y);
            double distanceFeet = distance / 7.0; // Convert pixels to feet
            double maxReach = meleeWeapon.getTotalReach();
            
            System.out.println("*** " + attacker.character.getDisplayName() + " cannot reach " + target.character.getDisplayName());
            System.out.println("*** Target distance: " + String.format("%.2f", distanceFeet) + " feet, weapon reach: " + String.format("%.2f", maxReach) + " feet");
            
            // TODO: Add automatic movement toward target when out of range
            return;
        }
        
        // Schedule melee attack based on weapon state
        attacker.character.startMeleeAttackSequence(attacker, target, gameClock.getCurrentTick(), eventQueue, attacker.getId(), (GameCallbacks) callbacks);
        
        System.out.println("*** " + attacker.character.getDisplayName() + " begins melee attack on " + target.character.getDisplayName() + " with " + meleeWeapon.getName());
    }
    
    /**
     * Reset character creation state variables
     */
    private void resetCharacterCreationState() {
        selectedArchetype = "";
        selectedRangedWeapon = "";
        selectedMeleeWeapon = "";
        waitingForCharacterCreation = false;
        waitingForCharacterRangedWeapon = false;
        waitingForCharacterMeleeWeapon = false;
    }
    
    /**
     * Handle archetype selection for character creation
     */
    private void handleCharacterArchetypeSelection(int archetypeIndex) {
        String[] archetypes = {"gunslinger", "soldier", "weighted_random", "scout", "marksman", "brawler", "confederate_soldier", "union_soldier", "balanced"};
        
        if (archetypeIndex >= 1 && archetypeIndex <= archetypes.length) {
            selectedArchetype = archetypes[archetypeIndex - 1];
            
            // Move to ranged weapon selection
            waitingForCharacterCreation = false;
            waitingForCharacterRangedWeapon = true;
            promptForCharacterRangedWeaponSelection();
        } else {
            System.out.println("*** Invalid archetype selection ***");
        }
    }
    
    /**
     * Prompt for ranged weapon selection during character creation
     */
    private void promptForCharacterRangedWeaponSelection() {
        String[] weaponIds = data.WeaponFactory.getAllWeaponIds();
        if (weaponIds.length == 0) {
            System.out.println("*** No ranged weapons available ***");
            return;
        }
        
        System.out.println("***********************");
        System.out.println("*** RANGED WEAPON SELECTION ***");
        System.out.println("Select ranged weapon for " + selectedArchetype + ":");
        
        for (int i = 0; i < weaponIds.length; i++) {
            data.WeaponData weaponData = data.WeaponFactory.getWeaponData(weaponIds[i]);
            if (weaponData != null) {
                System.out.println((i + 1) + ". " + weaponData.name + 
                                 " (Range: " + String.format("%.0f", weaponData.maximumRange) + " feet, " +
                                 "Damage: " + weaponData.damage + ")");
            }
        }
        System.out.println("0. Cancel character creation");
        System.out.println();
        System.out.println("Enter selection (1-" + weaponIds.length + ", 0 to cancel): ");
    }
    
    /**
     * Handle ranged weapon selection for character creation
     */
    private void handleCharacterRangedWeaponSelection(int weaponIndex) {
        String[] weaponIds = data.WeaponFactory.getAllWeaponIds();
        
        if (weaponIndex >= 1 && weaponIndex <= weaponIds.length) {
            selectedRangedWeapon = weaponIds[weaponIndex - 1];
            
            // Move to melee weapon selection
            waitingForCharacterRangedWeapon = false;
            waitingForCharacterMeleeWeapon = true;
            promptForCharacterMeleeWeaponSelection();
        } else {
            System.out.println("*** Invalid weapon selection ***");
        }
    }
    
    /**
     * Prompt for melee weapon selection during character creation
     */
    private void promptForCharacterMeleeWeaponSelection() {
        data.DataManager dataManager = data.DataManager.getInstance();
        java.util.Map<String, data.MeleeWeaponData> meleeWeapons = dataManager.getAllMeleeWeapons();
        String[] meleeWeaponIds = meleeWeapons.keySet().toArray(new String[0]);
        
        System.out.println("***********************");
        System.out.println("*** MELEE WEAPON SELECTION ***");
        System.out.println("Select melee weapon for " + selectedArchetype + ":");
        
        // Add "Unarmed" option first
        System.out.println("1. Unarmed (No melee weapon)");
        
        for (int i = 0; i < meleeWeaponIds.length; i++) {
            data.MeleeWeaponData meleeWeaponData = meleeWeapons.get(meleeWeaponIds[i]);
            if (meleeWeaponData != null) {
                System.out.println((i + 2) + ". " + meleeWeaponData.name + 
                                 " (Length: " + String.format("%.1f", meleeWeaponData.weaponLength) + " feet, " +
                                 "Damage: " + meleeWeaponData.damage + ")");
            }
        }
        System.out.println("0. Cancel character creation");
        System.out.println();
        System.out.println("Enter selection (1-" + (meleeWeaponIds.length + 1) + ", 0 to cancel): ");
    }
    
    /**
     * Handle melee weapon selection for character creation
     */
    private void handleCharacterMeleeWeaponSelection(int weaponIndex) {
        data.DataManager dataManager = data.DataManager.getInstance();
        java.util.Map<String, data.MeleeWeaponData> meleeWeapons = dataManager.getAllMeleeWeapons();
        String[] meleeWeaponIds = meleeWeapons.keySet().toArray(new String[0]);
        
        if (weaponIndex == 1) {
            // User selected "Unarmed"
            selectedMeleeWeapon = "unarmed";
            completeCharacterCreation();
        } else if (weaponIndex >= 2 && weaponIndex <= (meleeWeaponIds.length + 1)) {
            // User selected a melee weapon
            selectedMeleeWeapon = meleeWeaponIds[weaponIndex - 2];
            completeCharacterCreation();
        } else {
            System.out.println("*** Invalid weapon selection ***");
        }
    }
    
    /**
     * Complete character creation with selected archetype and weapons
     */
    private void completeCharacterCreation() {
        try {
            // Create character using CharacterFactory
            int characterId = data.CharacterFactory.createCharacter(selectedArchetype);
            combat.Character character = data.UniversalCharacterRegistry.getInstance().getCharacter(characterId);
            
            if (character != null) {
                // Assign selected ranged weapon
                character.weapon = data.WeaponFactory.createWeapon(selectedRangedWeapon);
                character.currentWeaponState = character.weapon.getInitialState();
                
                // Assign selected melee weapon
                if (!"unarmed".equals(selectedMeleeWeapon)) {
                    character.meleeWeapon = combat.MeleeWeaponFactory.createWeapon(selectedMeleeWeapon);
                }
                
                character.setFaction(1); // Default faction
                
                // Spawn character at camera center
                spawnCharacterUnit(character, selectedArchetype);
                
                // Display character creation confirmation
                System.out.println("*** Character created successfully! ***");
                System.out.println("Name: " + character.getDisplayName());
                System.out.println("Archetype: " + selectedArchetype);
                System.out.println("Ranged Weapon: " + character.weapon.name);
                System.out.println("Melee Weapon: " + (character.meleeWeapon != null ? character.meleeWeapon.getName() : "Unarmed"));
                System.out.println("Stats: DEX=" + character.dexterity + " HEALTH=" + character.health + 
                                 " COOL=" + character.coolness + " STR=" + character.strength + " REF=" + character.reflexes);
                System.out.println("***********************");
            } else {
                System.out.println("*** Failed to create character ***");
            }
        } catch (Exception e) {
            System.out.println("*** Error creating character: " + e.getMessage() + " ***");
        } finally {
            // Reset creation state
            resetCharacterCreationState();
        }
    }
    
    /**
     * Spawn a character unit in the game world (character creation version)
     */
    private void spawnCharacterUnit(combat.Character character, String archetype) {
        // Calculate spawn location at camera center
        double spawnX = gameRenderer.screenToWorldX(canvas.getWidth() / 2.0);
        double spawnY = gameRenderer.screenToWorldY(canvas.getHeight() / 2.0);
        
        // Check for collision with existing units and offset if necessary
        boolean collision = true;
        int attempts = 0;
        double finalX = spawnX;
        double finalY = spawnY;
        
        while (collision && attempts < 10) {
            collision = false;
            for (Unit existingUnit : units) {
                double distance = Math.hypot(finalX - existingUnit.x, finalY - existingUnit.y);
                if (distance < 28) { // 4 feet = 28 pixels minimum distance
                    collision = true;
                    finalX += 28; // Offset by 4 feet (28 pixels) in X direction only
                    break;
                }
            }
            attempts++;
        }
        
        // Get color based on archetype (reuse EditModeController logic)
        javafx.scene.paint.Color characterColor = getArchetypeColor(archetype);
        
        // Create and add unit
        int unitId = callbacks.getNextUnitId();
        Unit newUnit = new Unit(character, finalX, finalY, characterColor, unitId);
        callbacks.setNextUnitId(unitId + 1);
        units.add(newUnit);
        
        // Auto-select the newly created character
        selectionManager.selectUnit(newUnit);
        
        System.out.println("Character spawned at (" + String.format("%.0f", finalX) + ", " + String.format("%.0f", finalY) + ")");
    }
    
    /**
     * Get the appropriate color for a character archetype
     */
    private javafx.scene.paint.Color getArchetypeColor(String archetype) {
        switch (archetype.toLowerCase()) {
            case "confederate_soldier":
                return javafx.scene.paint.Color.DARKGRAY; // Confederate dark gray
            case "union_soldier":
                return javafx.scene.paint.Color.BLUE; // Union blue
            default:
                return javafx.scene.paint.Color.CYAN; // Default color for other archetypes
        }
    }
}
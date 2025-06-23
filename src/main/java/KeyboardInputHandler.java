import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import java.util.List;
import java.util.ArrayList;

import game.Unit;
import game.GameClock;
import game.GameCallbacks;
import input.interfaces.InputManagerCallbacks;
// InputStateTracker is in default package

/**
 * Handler for keyboard input events in the OpenFields2 game.
 * 
 * This handler processes all keyboard interactions including:
 * - Camera controls (arrow keys, zoom)
 * - Game state controls (pause/resume, debug modes, edit mode)
 * - Unit controls (movement types, aiming speeds, position, combat mode)
 * - Debug operations (system dumps, performance stats, integrity checks)
 * - Workflow navigation (prompts, confirmations, menu selections)
 * - Save/load operations
 * - Unit deletion with confirmation
 * 
 * The handler coordinates with multiple systems:
 * - GameRenderer for camera controls
 * - SelectionManager for unit selection operations
 * - EditModeManager for edit mode workflows
 * - CombatCommandProcessor for combat operations
 * - GameStateManager for save/load and scenario operations
 * - CharacterCreationController for character creation workflows
 * - DisplayCoordinator for debug operations and feedback
 * 
 * @author DevCycle 15h - Phase 3.2: Keyboard Input Handler Extraction
 */
public class KeyboardInputHandler {
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Dependencies and State
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /** List of game units for unit operations */
    private final List<Unit> units;
    
    /** Selection manager for unit selection operations */
    private final SelectionManager selectionManager;
    
    /** Game renderer for camera controls */
    private final GameRenderer gameRenderer;
    
    /** Display coordinator for debug operations and feedback */
    private final DisplayCoordinator displayCoordinator;
    
    /** Edit mode manager for edit mode operations */
    private final EditModeManager editModeManager;
    
    /** Combat command processor for combat operations */
    private final CombatCommandProcessor combatCommandProcessor;
    
    /** Game clock for timing information */
    private final GameClock gameClock;
    
    /** Game state manager for save/load and scenario operations */
    private final GameStateManager gameStateManager;
    
    /** Character creation controller for character creation workflows */
    private final CharacterCreationController characterCreationController;
    
    /** State tracker for workflow state management */
    private final InputStateTracker stateTracker;
    
    /** Camera controller for camera operations */
    private final CameraController cameraController;
    
    /** Movement controller for unit movement operations */
    private final MovementController movementController;
    
    /** Callback interface for main game operations */
    private final InputManagerCallbacks callbacks;
    
    /** Units pending deletion confirmation */
    private final List<Unit> unitsToDelete = new ArrayList<>();
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Constructor for KeyboardInputHandler.
     * 
     * @param units List of game units
     * @param selectionManager Selection manager for unit operations
     * @param gameRenderer Game renderer for camera controls
     * @param displayCoordinator Display coordinator for debug operations
     * @param editModeManager Edit mode manager for edit operations
     * @param combatCommandProcessor Combat command processor for combat operations
     * @param gameClock Game clock for timing
     * @param gameStateManager Game state manager for save/load operations
     * @param characterCreationController Character creation controller
     * @param stateTracker State tracker for workflow management
     * @param cameraController Camera controller for camera operations
     * @param movementController Movement controller for unit movement operations
     * @param callbacks Callback interface for main game operations
     */
    public KeyboardInputHandler(List<Unit> units, SelectionManager selectionManager,
                              GameRenderer gameRenderer, DisplayCoordinator displayCoordinator,
                              EditModeManager editModeManager, CombatCommandProcessor combatCommandProcessor,
                              GameClock gameClock, GameStateManager gameStateManager,
                              CharacterCreationController characterCreationController,
                              InputStateTracker stateTracker, CameraController cameraController,
                              MovementController movementController, InputManagerCallbacks callbacks) {
        this.units = units;
        this.selectionManager = selectionManager;
        this.gameRenderer = gameRenderer;
        this.displayCoordinator = displayCoordinator;
        this.editModeManager = editModeManager;
        this.combatCommandProcessor = combatCommandProcessor;
        this.gameClock = gameClock;
        this.gameStateManager = gameStateManager;
        this.characterCreationController = characterCreationController;
        this.stateTracker = stateTracker;
        this.cameraController = cameraController;
        this.movementController = movementController;
        this.callbacks = callbacks;
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Main Keyboard Event Processing
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Process keyboard input events and route to appropriate handlers.
     * 
     * This is the central keyboard input dispatcher that processes all keyboard events
     * and routes them to the appropriate specialized handlers based on current game state.
     * The method handles immediate response controls (camera, pause) first, then delegates
     * to specialized handlers for complex workflows.
     * 
     * The processing order is optimized for responsiveness:
     * 1. Camera controls (immediate visual feedback)
     * 2. Game state controls (pause, debug, edit mode)
     * 3. Edit mode operations (delegated to EditModeManager)
     * 4. Unit controls (movement, aiming, position)
     * 5. Combat operations (delegated to CombatCommandProcessor)
     * 6. Workflow navigation (prompts, confirmations)
     * 7. Save/load operations (delegated to GameStateManager)
     * 
     * @param e KeyEvent containing key code and modifier states
     */
    public void handleKeyPressed(KeyEvent e) {
        // Delegate debug operations to DisplayCoordinator
        displayCoordinator.startPerformanceTimer("KeyPressed");
        String modifiers = (e.isShiftDown() ? "Shift+" : "") + (e.isControlDown() ? "Ctrl+" : "") + (e.isAltDown() ? "Alt+" : "");
        displayCoordinator.debugInputEvent("KEY_PRESS", modifiers + e.getCode());
        displayCoordinator.addInputTraceEvent("Key pressed: " + modifiers + e.getCode());
        
        // Camera controls
        handleCameraControls(e);
        
        // Game controls
        handleGameStateControls(e);
        
        // Debug controls
        handleDebugControls(e);
        
        // Edit mode operations
        handleEditModeKeys(e);
        
        // Unit deletion
        handleUnitDeletion(e);
        
        // Character stats display
        displayCoordinator.handleCharacterStatsDisplay(e);
        
        // Movement and aiming controls
        handleMovementControls(e);
        handleAimingControls(e);
        
        // Delegate combat commands to CombatCommandProcessor
        combatCommandProcessor.handleCombatKeys(e);
        
        // Save/Load controls
        handleSaveLoadControls(e);
        
        // Handle prompt responses
        handlePromptInputs(e);
        
        displayCoordinator.endPerformanceTimer("KeyPressed");
        displayCoordinator.logMemoryUsage("After KeyPressed");
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Camera Controls
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Handle camera control keys (arrow keys, zoom).
     * Delegates to CameraController for all camera operations.
     * 
     * @param e KeyEvent
     */
    private void handleCameraControls(KeyEvent e) {
        // Delegate all camera controls to CameraController
        cameraController.handleCameraControls(e);
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Game State Controls
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Handle game state controls (pause, edit mode toggle).
     * 
     * @param e KeyEvent
     */
    private void handleGameStateControls(KeyEvent e) {
        // Pause/resume toggle
        if (e.getCode() == KeyCode.SPACE) {
            boolean newPauseState = !callbacks.isPaused();
            // Delegate state transition debugging to DisplayCoordinator
            displayCoordinator.debugStateTransition("GAME_STATE", callbacks.isPaused() ? "PAUSED" : "RUNNING", 
                                newPauseState ? "PAUSED" : "RUNNING");
            callbacks.setPaused(newPauseState);
            // Delegate pause status display to DisplayCoordinator
            displayCoordinator.displayPauseStatus(newPauseState, gameClock.getCurrentTick());
        }
        
        // Debug mode toggle
        if (e.getCode() == KeyCode.D && e.isControlDown()) {
            GameRenderer.setDebugMode(!GameRenderer.isDebugMode());
            // Delegate debug mode status display to DisplayCoordinator
            displayCoordinator.displayDebugModeStatus(GameRenderer.isDebugMode());
        }
        
        // Edit mode toggle
        if (e.getCode() == KeyCode.E && e.isControlDown()) {
            boolean newEditMode = !callbacks.isEditMode();
            callbacks.setEditMode(newEditMode);
            // Delegate edit mode status display to DisplayCoordinator
            displayCoordinator.displayEditModeStatus(newEditMode);
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Debug Controls
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Handle debug function keys (F1-F7 with Ctrl).
     * 
     * @param e KeyEvent
     */
    private void handleDebugControls(KeyEvent e) {
        // Debug mode toggle (traditional CTRL-D)
        if (e.getCode() == KeyCode.D && e.isControlDown()) {
            GameRenderer.setDebugMode(!GameRenderer.isDebugMode());
            System.out.println("*** Debug mode " + (GameRenderer.isDebugMode() ? "ENABLED" : "DISABLED") + " ***");
        }
        
        // All other debug operations delegated to DisplayCoordinator  
        if (e.getCode() == KeyCode.F1 && e.isControlDown()) {
            // Ctrl+F1: Toggle debug logging
            displayCoordinator.setDebugEnabled(!displayCoordinator.isDebugEnabled());
        }
        
        if (e.getCode() == KeyCode.F2 && e.isControlDown()) {
            // Ctrl+F2: Configure debug categories
            displayCoordinator.configureDebugFeatures(true, true, true, false, false, true, true, true);
        }
        
        if (e.getCode() == KeyCode.F3 && e.isControlDown()) {
            // Ctrl+F3: System state dump
            if (displayCoordinator.isDebugEnabled()) {
                String stateDump = displayCoordinator.generateSystemStateDump(
                    stateTracker, displayCoordinator.getPerformanceStatistics(), 
                    displayCoordinator.getInputEventTrace(), combatCommandProcessor);
                System.out.println(stateDump);
            } else {
                System.out.println("*** Debug mode must be enabled for system state dump ***");
            }
        }
        
        if (e.getCode() == KeyCode.F4 && e.isControlDown()) {
            // Ctrl+F4: Performance statistics
            if (displayCoordinator.isDebugEnabled()) {
                displayCoordinator.displayPerformanceStatistics(displayCoordinator.getPerformanceStatistics());
            }
        }
        
        if (e.getCode() == KeyCode.F5 && e.isControlDown()) {
            // Ctrl+F5: Input trace
            if (displayCoordinator.isDebugEnabled()) {
                displayCoordinator.displayInputEventTrace(displayCoordinator.getInputEventTrace());
            }
        }
        
        if (e.getCode() == KeyCode.F6 && e.isControlDown()) {
            // Ctrl+F6: System integrity validation
            displayCoordinator.displaySystemIntegrityResults();
            validateSystemIntegrity();
        }
        
        if (e.getCode() == KeyCode.F7 && e.isControlDown()) {
            // Ctrl+F7: Clear debug data
            if (displayCoordinator.isDebugEnabled()) {
                displayCoordinator.clearPerformanceStatistics();
                displayCoordinator.clearInputEventTrace();
                displayCoordinator.displayDebugDataCleared();
            }
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Edit Mode and System Integrity
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Handle edit mode specific key commands.
     * 
     * @param e KeyEvent
     */
    private void handleEditModeKeys(KeyEvent e) {
        // Delegate edit mode operations to EditModeManager
        editModeManager.handleEditModeKeys(e);
        
        // Delegate manual victory to GameStateManager
        if (e.getCode() == KeyCode.V && e.isControlDown() && e.isShiftDown()) {
            if (!isWaitingForInput()) {
                gameStateManager.promptForManualVictory();
            }
        }
    }
    
    /**
     * Validate system integrity (placeholder for future implementation).
     */
    private void validateSystemIntegrity() {
        // Placeholder for system integrity validation
        // Future implementation would check unit/character consistency,
        // state synchronization, memory leaks, etc.
    }
    
    /**
     * Check if the system is waiting for user input.
     * 
     * @return true if waiting for input, false otherwise
     */
    private boolean isWaitingForInput() {
        return stateTracker.isWaitingForSaveSlot() || 
               stateTracker.isWaitingForLoadSlot() ||
               stateTracker.isWaitingForCharacterCreation() ||
               stateTracker.isWaitingForWeaponSelection() ||
               stateTracker.isWaitingForRangedWeaponSelection() ||
               stateTracker.isWaitingForMeleeWeaponSelection() ||
               stateTracker.isWaitingForFactionSelection() ||
               stateTracker.isWaitingForBatchCharacterCreation() ||
               stateTracker.isWaitingForCharacterDeployment() ||
               stateTracker.isWaitingForCharacterRangedWeapon() ||
               stateTracker.isWaitingForCharacterMeleeWeapon() ||
               stateTracker.isWaitingForDirectCharacterAddition() ||
               stateTracker.isWaitingForDeletionConfirmation() ||
               stateTracker.isWaitingForVictoryOutcome() ||
               stateTracker.isWaitingForScenarioName() ||
               stateTracker.isWaitingForThemeSelection();
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Unit Controls
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Handle movement controls (W/S keys).
     * Delegates to MovementController for all movement speed operations.
     * 
     * @param e KeyEvent
     */
    private void handleMovementControls(KeyEvent e) {
        // Delegate all movement controls to MovementController
        movementController.handleMovementControls(e);
    }
    
    /**
     * Handle aiming speed controls (Q/E keys) and position/combat mode controls (C/V/M keys).
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
                // Delegate aiming speed status display to DisplayCoordinator
                displayCoordinator.displayAimingSpeedChange(unit, newSpeed);
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
                // Delegate aiming speed status display to DisplayCoordinator
                displayCoordinator.displayAimingSpeedChange(unit, newSpeed);
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
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Save/Load Controls
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Handle save/load controls (Ctrl+S/Ctrl+L).
     * 
     * @param e KeyEvent
     */
    private void handleSaveLoadControls(KeyEvent e) {
        // Delegate save/load operations to GameStateManager
        gameStateManager.handleSaveLoadControls(e);
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Prompt Input Handling
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Handle input responses to prompts (number keys, escape).
     * 
     * @param e KeyEvent
     */
    private void handlePromptInputs(KeyEvent e) {
        // Handle deletion confirmation with Y/N keys
        if (stateTracker.isWaitingForDeletionConfirmation()) {
            if (e.getCode() == KeyCode.Y) {
                confirmUnitDeletion();
            } else if (e.getCode() == KeyCode.N || e.getCode() == KeyCode.ESCAPE) {
                cancelUnitDeletion();
            }
            return; // Don't process other input while waiting for deletion confirmation
        }
        
        // Handle victory outcome selection
        if (stateTracker.isWaitingForVictoryOutcome()) {
            int outcomeNumber = -1;
            if (e.getCode() == KeyCode.DIGIT1) outcomeNumber = 1;
            else if (e.getCode() == KeyCode.DIGIT2) outcomeNumber = 2;
            else if (e.getCode() == KeyCode.DIGIT3) outcomeNumber = 3;
            else if (e.getCode() == KeyCode.DIGIT0) outcomeNumber = 0;
            else if (e.getCode() == KeyCode.ESCAPE) outcomeNumber = 0;
            
            if (outcomeNumber >= 0) {
                // Delegate to GameStateManager
                gameStateManager.handleVictoryOutcomeInput(outcomeNumber);
            }
            return; // Don't process other input while waiting for victory outcome
        }
        
        // Handle scenario name input
        if (stateTracker.isWaitingForScenarioName()) {
            // Delegate to GameStateManager
            gameStateManager.handleScenarioNameTextInput(e);
            return; // Don't process other input while waiting for scenario name
        }
        
        // Handle theme selection
        if (stateTracker.isWaitingForThemeSelection()) {
            int themeNumber = -1;
            if (e.getCode() == KeyCode.DIGIT1) themeNumber = 1;
            else if (e.getCode() == KeyCode.DIGIT2) themeNumber = 2;
            else if (e.getCode() == KeyCode.DIGIT3) themeNumber = 3;
            else if (e.getCode() == KeyCode.DIGIT4) themeNumber = 4;
            else if (e.getCode() == KeyCode.DIGIT5) themeNumber = 5;
            else if (e.getCode() == KeyCode.DIGIT0) themeNumber = 0;
            else if (e.getCode() == KeyCode.ESCAPE) themeNumber = 0;
            
            if (themeNumber >= 0) {
                // Delegate to GameStateManager
                gameStateManager.handleThemeSelectionInput(themeNumber);
            }
            return; // Don't process other input while waiting for theme selection
        }
        
        // Handle numeric input for various workflows
        handleNumericWorkflowInputs(e);
    }
    
    /**
     * Handle numeric workflow inputs for character creation, weapon selection, etc.
     * 
     * @param e KeyEvent
     */
    private void handleNumericWorkflowInputs(KeyEvent e) {
        // Handle number key input for save/load slot selection, character creation, weapon selection, faction selection, batch character creation, character deployment, and direct character addition
        if (stateTracker.isWaitingForSaveSlot() || stateTracker.isWaitingForLoadSlot() || stateTracker.isWaitingForCharacterCreation() || stateTracker.isWaitingForWeaponSelection() || stateTracker.isWaitingForRangedWeaponSelection() || stateTracker.isWaitingForMeleeWeaponSelection() || stateTracker.isWaitingForFactionSelection() || stateTracker.isWaitingForBatchCharacterCreation() || stateTracker.isWaitingForCharacterDeployment() || stateTracker.isWaitingForCharacterRangedWeapon() || stateTracker.isWaitingForCharacterMeleeWeapon() || stateTracker.isWaitingForDirectCharacterAddition()) {
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
            // Handle 'A' key for weapon selection workflows (option 10)
            else if (e.getCode() == KeyCode.A && isWeaponSelectionActive()) {
                slotNumber = 10;
            }
            else if (e.getCode() == KeyCode.ESCAPE) {
                handleWorkflowCancellation();
            }
            
            if (slotNumber >= 0 && slotNumber <= 10) {
                handleWorkflowNumericInput(slotNumber);
            }
        }
    }
    
    /**
     * Check if weapon selection workflow is currently active.
     * 
     * @return true if weapon selection is active
     */
    private boolean isWeaponSelectionActive() {
        return stateTracker.isWaitingForRangedWeaponSelection() || 
               stateTracker.isWaitingForMeleeWeaponSelection() ||
               (stateTracker.isWaitingForDirectCharacterAddition() && 
                (editModeManager.getDirectAdditionStep() == EditModeManager.DirectAdditionStep.RANGED_WEAPON_SELECTION ||
                 editModeManager.getDirectAdditionStep() == EditModeManager.DirectAdditionStep.MELEE_WEAPON_SELECTION));
    }
    
    /**
     * Handle workflow cancellation (Escape key).
     */
    private void handleWorkflowCancellation() {
        if (stateTracker.isWaitingForCharacterCreation()) {
            System.out.println("*** Character creation cancelled ***");
            stateTracker.setWaitingForCharacterCreation(false);
        } else if (stateTracker.isWaitingForCharacterRangedWeapon()) {
            System.out.println("*** Character creation cancelled ***");
            stateTracker.setWaitingForCharacterRangedWeapon(false);
        } else if (stateTracker.isWaitingForCharacterMeleeWeapon()) {
            System.out.println("*** Character creation cancelled ***");
            stateTracker.setWaitingForCharacterMeleeWeapon(false);
        } else if (stateTracker.isWaitingForWeaponSelection()) {
            System.out.println("*** Weapon selection cancelled ***");
            stateTracker.setWaitingForWeaponSelection(false);
        } else if (stateTracker.isWaitingForRangedWeaponSelection()) {
            System.out.println("*** Ranged weapon selection cancelled ***");
            stateTracker.setWaitingForRangedWeaponSelection(false);
        } else if (stateTracker.isWaitingForMeleeWeaponSelection()) {
            System.out.println("*** Melee weapon selection cancelled ***");
            stateTracker.setWaitingForMeleeWeaponSelection(false);
        } else if (stateTracker.isWaitingForFactionSelection()) {
            System.out.println("*** Faction selection cancelled ***");
            stateTracker.setWaitingForFactionSelection(false);
        } else if (stateTracker.isWaitingForBatchCharacterCreation()) {
            System.out.println("*** Batch character creation cancelled ***");
            stateTracker.setWaitingForBatchCharacterCreation(false);
        } else if (stateTracker.isWaitingForCharacterDeployment()) {
            System.out.println("*** Character deployment cancelled ***");
            cancelCharacterDeployment();
        } else if (stateTracker.isWaitingForDirectCharacterAddition()) {
            System.out.println("*** Character addition cancelled ***");
            cancelDirectCharacterAddition();
        } else {
            // Delegate save/load cancellation to GameStateManager
            gameStateManager.cancelSaveLoad();
        }
    }
    
    /**
     * Handle numeric input for workflows.
     * 
     * @param slotNumber The number pressed (0-9)
     */
    private void handleWorkflowNumericInput(int slotNumber) {
        if (stateTracker.isWaitingForSaveSlot() || stateTracker.isWaitingForLoadSlot()) {
            // Delegate save/load slot handling to GameStateManager
            gameStateManager.handleSaveLoadInput(slotNumber);
        } else if (stateTracker.isWaitingForCharacterCreation()) {
            handleCharacterCreationInput(slotNumber);
        } else if (stateTracker.isWaitingForCharacterRangedWeapon()) {
            handleCharacterRangedWeaponInput(slotNumber);
        } else if (stateTracker.isWaitingForCharacterMeleeWeapon()) {
            handleCharacterMeleeWeaponInput(slotNumber);
        } else if (stateTracker.isWaitingForWeaponSelection()) {
            handleWeaponTypeSelectionInput(slotNumber);
        } else if (stateTracker.isWaitingForRangedWeaponSelection()) {
            handleRangedWeaponSelectionInput(slotNumber);
        } else if (stateTracker.isWaitingForMeleeWeaponSelection()) {
            handleMeleeWeaponSelectionInput(slotNumber);
        } else if (stateTracker.isWaitingForFactionSelection()) {
            handleFactionSelectionInput(slotNumber);
        } else if (stateTracker.isWaitingForBatchCharacterCreation()) {
            handleBatchCharacterCreationInput(slotNumber);
        } else if (stateTracker.isWaitingForCharacterDeployment()) {
            // Character deployment feature removed
            System.out.println("*** Error: Character deployment is no longer supported ***");
        } else if (stateTracker.isWaitingForDirectCharacterAddition()) {
            // Delegate to EditModeManager
            editModeManager.handleDirectCharacterAdditionInput(slotNumber);
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Specific Workflow Input Handlers
    // ─────────────────────────────────────────────────────────────────────────────────
    
    private void handleCharacterCreationInput(int slotNumber) {
        if (slotNumber == 0) {
            System.out.println("*** Character creation cancelled ***");
            stateTracker.setWaitingForCharacterCreation(false);
        } else if (slotNumber >= 1 && slotNumber <= 9) {
            // Delegate to EditModeManager
            editModeManager.handleCharacterArchetypeSelection(slotNumber);
        } else {
            System.out.println("*** Invalid archetype selection. Use 1-9 or 0 to cancel ***");
        }
    }
    
    private void handleCharacterRangedWeaponInput(int slotNumber) {
        if (slotNumber == 0) {
            System.out.println("*** Character creation cancelled ***");
            stateTracker.setWaitingForCharacterRangedWeapon(false);
        } else {
            // Delegate to EditModeManager
            editModeManager.handleCharacterRangedWeaponSelection(slotNumber);
        }
    }
    
    private void handleCharacterMeleeWeaponInput(int slotNumber) {
        if (slotNumber == 0) {
            System.out.println("*** Character creation cancelled ***");
            stateTracker.setWaitingForCharacterMeleeWeapon(false);
        } else {
            // Delegate to EditModeManager
            editModeManager.handleCharacterMeleeWeaponSelection(slotNumber);
        }
    }
    
    private void handleWeaponTypeSelectionInput(int slotNumber) {
        if (slotNumber == 0) {
            System.out.println("*** Weapon selection cancelled ***");
            stateTracker.setWaitingForWeaponSelection(false);
        } else if (slotNumber == 1) {
            // User chose ranged weapons
            stateTracker.setWaitingForWeaponSelection(false);
            stateTracker.setWaitingForRangedWeaponSelection(true);
            ((EditModeController)callbacks).promptForRangedWeaponSelection();
        } else if (slotNumber == 2) {
            // User chose melee weapons
            stateTracker.setWaitingForWeaponSelection(false);
            stateTracker.setWaitingForMeleeWeaponSelection(true);
            ((EditModeController)callbacks).promptForMeleeWeaponSelection();
        } else {
            System.out.println("*** Invalid weapon type selection. Use 1 for Ranged, 2 for Melee, or 0 to cancel ***");
        }
    }
    
    private void handleRangedWeaponSelectionInput(int slotNumber) {
        if (slotNumber == 0) {
            System.out.println("*** Ranged weapon selection cancelled ***");
            stateTracker.setWaitingForRangedWeaponSelection(false);
        } else {
            ((EditModeController)callbacks).assignRangedWeaponToSelectedUnits(slotNumber);
            stateTracker.setWaitingForRangedWeaponSelection(false);
        }
    }
    
    private void handleMeleeWeaponSelectionInput(int slotNumber) {
        if (slotNumber == 0) {
            System.out.println("*** Melee weapon selection cancelled ***");
            stateTracker.setWaitingForMeleeWeaponSelection(false);
        } else {
            ((EditModeController)callbacks).assignMeleeWeaponToSelectedUnits(slotNumber);
            stateTracker.setWaitingForMeleeWeaponSelection(false);
        }
    }
    
    private void handleFactionSelectionInput(int slotNumber) {
        if (slotNumber == 0) {
            System.out.println("*** Faction selection cancelled ***");
            stateTracker.setWaitingForFactionSelection(false);
        } else {
            callbacks.assignFactionToSelectedUnits(slotNumber);
        }
    }
    
    private void handleBatchCharacterCreationInput(int slotNumber) {
        // Delegate to CharacterCreationController
        boolean continueWorkflow = characterCreationController.handleBatchCharacterCreationInput(slotNumber);
        if (!continueWorkflow) {
            stateTracker.setWaitingForBatchCharacterCreation(false);
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Unit Deletion
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Handle unit deletion key (Delete).
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
     * Prompt for unit deletion confirmation.
     */
    private void promptForUnitDeletion() {
        unitsToDelete.clear();
        unitsToDelete.addAll(selectionManager.getSelectedUnits());
        
        stateTracker.setWaitingForDeletionConfirmation(true);
        
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
     * Confirm unit deletion and perform the deletion.
     */
    private void confirmUnitDeletion() {
        System.out.println("***********************");
        System.out.println("*** DELETING UNITS ***");
        
        int deletedCount = 0;
        for (Unit unit : unitsToDelete) {
            units.remove(unit);
            deletedCount++;
            System.out.println("Deleted: " + unit.character.getDisplayName() + " (ID: " + unit.character.id + ")");
        }
        
        selectionManager.clearSelection();
        unitsToDelete.clear();
        stateTracker.setWaitingForDeletionConfirmation(false);
        
        System.out.println("*** " + deletedCount + " units deleted from scenario ***");
        System.out.println("*** Character data preserved in faction files ***");
        System.out.println("***********************");
    }
    
    /**
     * Cancel unit deletion.
     */
    private void cancelUnitDeletion() {
        unitsToDelete.clear();
        stateTracker.setWaitingForDeletionConfirmation(false);
        System.out.println("*** Unit deletion cancelled ***");
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Utility Methods
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Get faction name by faction number (1-based).
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
     * Cancel character deployment (placeholder for future implementation).
     */
    private void cancelCharacterDeployment() {
        // This should delegate to the appropriate system
        // For now, just reset the state
        stateTracker.setWaitingForCharacterDeployment(false);
    }
    
    /**
     * Cancel direct character addition (placeholder for future implementation).
     */
    private void cancelDirectCharacterAddition() {
        // This should delegate to the appropriate system
        // For now, just reset the state
        stateTracker.setWaitingForDirectCharacterAddition(false);
    }
}
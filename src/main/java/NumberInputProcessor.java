import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

import input.interfaces.InputManagerCallbacks;

/**
 * Processor for numeric input workflows in the OpenFields2 game.
 * 
 * This processor handles all number-based input interactions including:
 * - Save/load slot selection (digits 0-9)
 * - Character creation workflows (archetype selection, weapon assignment)
 * - Victory outcome selection and theme selection
 * - Batch character creation and deployment operations
 * - Direct character addition workflows
 * - Yes/No confirmation prompts (Y/N keys)
 * - Text input workflows (scenario names)
 * 
 * The processor coordinates with multiple systems:
 * - InputStateTracker for workflow state management
 * - WorkflowStateCoordinator for complex multi-step workflows
 * - EditModeManager for character creation and deployment
 * - GameStateManager for save/load and victory outcomes
 * - CharacterCreationController for batch character operations
 * 
 * @author DevCycle 15k - Phase 4: Number Input Processor Extraction
 */
public class NumberInputProcessor {
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Dependencies and State
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /** State tracker for workflow state management */
    private final InputStateTracker stateTracker;
    
    /** Workflow coordinator for complex multi-step workflows */
    private final WorkflowStateCoordinator workflowCoordinator;
    
    /** Edit mode manager for character creation and deployment operations */
    private final EditModeManager editModeManager;
    
    /** Game state manager for save/load and scenario operations */
    private final GameStateManager gameStateManager;
    
    /** Character creation controller for batch character operations */
    private final CharacterCreationController characterCreationController;
    
    /** Deployment controller for character deployment operations */
    private final input.controllers.DeploymentController deploymentController;
    
    /** Callback interface for main game operations */
    private final InputManagerCallbacks callbacks;
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Constructor for NumberInputProcessor.
     * 
     * @param stateTracker State tracker for workflow management
     * @param workflowCoordinator Workflow coordinator for complex workflows
     * @param editModeManager Edit mode manager for edit operations
     * @param gameStateManager Game state manager for save/load operations
     * @param characterCreationController Character creation controller
     * @param deploymentController Deployment controller for character deployment
     * @param callbacks Callback interface for main game operations
     */
    public NumberInputProcessor(InputStateTracker stateTracker,
                               WorkflowStateCoordinator workflowCoordinator,
                               EditModeManager editModeManager,
                               GameStateManager gameStateManager,
                               CharacterCreationController characterCreationController,
                               input.controllers.DeploymentController deploymentController,
                               InputManagerCallbacks callbacks) {
        this.stateTracker = stateTracker;
        this.workflowCoordinator = workflowCoordinator;
        this.editModeManager = editModeManager;
        this.gameStateManager = gameStateManager;
        this.characterCreationController = characterCreationController;
        this.deploymentController = deploymentController;
        this.callbacks = callbacks;
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Public Interface
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Process keyboard input for numeric workflows and prompts.
     * 
     * This is the main entry point for all numeric input processing. It routes
     * input to appropriate handlers based on current workflow state.
     * 
     * @param e KeyEvent containing the key pressed
     */
    public void handlePromptInputs(KeyEvent e) {
        // Note: Deletion confirmation is handled by InputManager directly
        // due to dependencies on private state. Skip it here.
        if (stateTracker.isWaitingForDeletionConfirmation()) {
            return; // Let InputManager handle this directly
        }
        
        // DevCycle 15e: Handle victory outcome selection
        if (stateTracker.isWaitingForVictoryOutcome()) {
            handleVictoryOutcomeSelection(e);
            return; // Don't process other input while waiting for victory outcome
        }
        
        // DevCycle 15j: Handle scenario name input  
        if (stateTracker.isWaitingForScenarioName()) {
            gameStateManager.handleScenarioNameTextInput(e);
            return;
        }
        
        // DevCycle 15j: Handle theme selection
        if (stateTracker.isWaitingForThemeSelection()) {
            handleThemeSelection(e);
            return;
        }
        
        // Handle number key input for save/load slot selection, character creation, weapon selection, faction selection, batch character creation, character deployment, and direct character addition
        if (isWaitingForNumericInput()) {
            handleNumericInputWorkflows(e);
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Workflow State Checking
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Check if any numeric input workflow is currently active.
     * 
     * @return true if waiting for numeric input, false otherwise
     */
    private boolean isWaitingForNumericInput() {
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
               stateTracker.isWaitingForDirectCharacterAddition();
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Specific Input Handlers
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Handle deletion confirmation (Y/N keys).
     * Note: Unit deletion handling requires InputManager's private methods,
     * so this should be handled by InputManager directly, not delegated here.
     * This method exists for interface completeness but delegates back to InputManager.
     * 
     * @param e KeyEvent containing the key pressed
     */
    private void handleDeletionConfirmation(KeyEvent e) {
        // TODO: Unit deletion requires InputManager's private state and methods
        // This needs to be handled by InputManager directly rather than delegated
        throw new UnsupportedOperationException("Unit deletion should be handled by InputManager directly");
    }
    
    /**
     * Handle victory outcome selection (digits 0-3).
     * 
     * @param e KeyEvent containing the key pressed
     */
    private void handleVictoryOutcomeSelection(KeyEvent e) {
        int outcomeNumber = -1;
        if (e.getCode() == KeyCode.DIGIT1) outcomeNumber = 1;
        else if (e.getCode() == KeyCode.DIGIT2) outcomeNumber = 2;
        else if (e.getCode() == KeyCode.DIGIT3) outcomeNumber = 3;
        else if (e.getCode() == KeyCode.DIGIT0) outcomeNumber = 0;
        else if (e.getCode() == KeyCode.ESCAPE) outcomeNumber = 0;
        
        if (outcomeNumber >= 0) {
            // DevCycle 15e: Delegate to GameStateManager
            gameStateManager.handleVictoryOutcomeInput(outcomeNumber);
        }
    }
    
    /**
     * Handle theme selection (digits 0-5).
     * 
     * @param e KeyEvent containing the key pressed
     */
    private void handleThemeSelection(KeyEvent e) {
        int themeNumber = -1;
        if (e.getCode() == KeyCode.DIGIT1) themeNumber = 1;
        else if (e.getCode() == KeyCode.DIGIT2) themeNumber = 2;
        else if (e.getCode() == KeyCode.DIGIT3) themeNumber = 3;
        else if (e.getCode() == KeyCode.DIGIT4) themeNumber = 4;
        else if (e.getCode() == KeyCode.DIGIT5) themeNumber = 5;
        else if (e.getCode() == KeyCode.DIGIT0) themeNumber = 0;
        else if (e.getCode() == KeyCode.ESCAPE) themeNumber = 0;
        
        if (themeNumber >= 0) {
            gameStateManager.handleThemeSelectionInput(themeNumber);
        }
    }
    
    /**
     * Handle numeric input workflows (digits 0-9 and letters for weapon selection).
     * 
     * @param e KeyEvent containing the key pressed
     */
    private void handleNumericInputWorkflows(KeyEvent e) {
        // Handle escape key for cancellation
        if (e.getCode() == KeyCode.ESCAPE) {
            handleWorkflowCancellation();
            return;
        }
        
        // For weapon selection workflows, support alphanumeric input (1-9, A)
        if (isWeaponSelectionWorkflow()) {
            int selectionNumber = extractAlphanumericSelection(e);
            if (selectionNumber >= 1 && selectionNumber <= 10) {
                processNumericInput(selectionNumber);
            }
        } else if (stateTracker.isWaitingForLoadSlot() && isDebugModeActive()) {
            // For load slot in debug mode, support both numbers (1-9) and letters (a-z)
            int slotNumber = extractDigitFromKeyEvent(e);
            if (slotNumber >= 0 && slotNumber <= 9) {
                processNumericInput(slotNumber);
            } else {
                char testSlot = extractTestSlotFromKeyEvent(e);
                if (testSlot >= 'a' && testSlot <= 'z') {
                    processTestSlotInput(testSlot);
                }
            }
        } else {
            // For non-weapon workflows, use standard digit input
            int slotNumber = extractDigitFromKeyEvent(e);
            if (slotNumber >= 0 && slotNumber <= 9) {
                processNumericInput(slotNumber);
            }
        }
    }
    
    /**
     * Extract digit value from KeyEvent.
     * 
     * @param e KeyEvent to extract digit from
     * @return digit value (0-9) or -1 if not a digit key
     */
    private int extractDigitFromKeyEvent(KeyEvent e) {
        if (e.getCode() == KeyCode.DIGIT1) return 1;
        else if (e.getCode() == KeyCode.DIGIT2) return 2;
        else if (e.getCode() == KeyCode.DIGIT3) return 3;
        else if (e.getCode() == KeyCode.DIGIT4) return 4;
        else if (e.getCode() == KeyCode.DIGIT5) return 5;
        else if (e.getCode() == KeyCode.DIGIT6) return 6;
        else if (e.getCode() == KeyCode.DIGIT7) return 7;
        else if (e.getCode() == KeyCode.DIGIT8) return 8;
        else if (e.getCode() == KeyCode.DIGIT9) return 9;
        else if (e.getCode() == KeyCode.DIGIT0) return 0;
        else return -1;
    }
    
    /**
     * Check if current workflow is weapon selection (ranged, melee, or direct addition weapon steps).
     * 
     * @return true if weapon selection workflow is active
     */
    private boolean isWeaponSelectionWorkflow() {
        return stateTracker.isWaitingForRangedWeaponSelection() || 
               stateTracker.isWaitingForMeleeWeaponSelection() ||
               (stateTracker.isWaitingForDirectCharacterAddition() && 
                (editModeManager.getDirectAdditionStep() == EditModeManager.DirectAdditionStep.RANGED_WEAPON_SELECTION ||
                 editModeManager.getDirectAdditionStep() == EditModeManager.DirectAdditionStep.MELEE_WEAPON_SELECTION));
    }
    
    /**
     * Extract alphanumeric selection value from KeyEvent for weapon selection.
     * Supports 1-9 (digits) and A (for option 10).
     * 
     * @param e KeyEvent to extract selection from
     * @return selection value (1-10) or -1 if not a valid selection key
     */
    private int extractAlphanumericSelection(KeyEvent e) {
        // Handle digits 1-9
        if (e.getCode() == KeyCode.DIGIT1) return 1;
        else if (e.getCode() == KeyCode.DIGIT2) return 2;
        else if (e.getCode() == KeyCode.DIGIT3) return 3;
        else if (e.getCode() == KeyCode.DIGIT4) return 4;
        else if (e.getCode() == KeyCode.DIGIT5) return 5;
        else if (e.getCode() == KeyCode.DIGIT6) return 6;
        else if (e.getCode() == KeyCode.DIGIT7) return 7;
        else if (e.getCode() == KeyCode.DIGIT8) return 8;
        else if (e.getCode() == KeyCode.DIGIT9) return 9;
        // Handle letter A for option 10
        else if (e.getCode() == KeyCode.A) return 10;
        else return -1;
    }
    
    /**
     * Handle workflow cancellation (ESC key).
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
            deploymentController.cancelCharacterDeployment();
        } else if (stateTracker.isWaitingForDirectCharacterAddition()) {
            System.out.println("*** Character addition cancelled ***");
            workflowCoordinator.cancelDirectCharacterAddition();
        } else {
            // DevCycle 15e: Delegate save/load cancellation to GameStateManager
            gameStateManager.cancelSaveLoad();
        }
    }
    
    /**
     * Process valid numeric input based on current workflow state.
     * 
     * @param slotNumber The numeric input (0-9)
     */
    private void processNumericInput(int slotNumber) {
        if (stateTracker.isWaitingForSaveSlot() || stateTracker.isWaitingForLoadSlot()) {
            // DevCycle 15e: Delegate save/load slot handling to GameStateManager
            gameStateManager.handleSaveLoadInput(slotNumber);
        } else if (stateTracker.isWaitingForCharacterCreation()) {
            handleCharacterCreationInput(slotNumber);
        } else if (stateTracker.isWaitingForCharacterRangedWeapon()) {
            handleCharacterRangedWeaponInput(slotNumber);
        } else if (stateTracker.isWaitingForCharacterMeleeWeapon()) {
            handleCharacterMeleeWeaponInput(slotNumber);
        } else if (stateTracker.isWaitingForWeaponSelection()) {
            handleWeaponTypeSelection(slotNumber);
        } else if (stateTracker.isWaitingForRangedWeaponSelection()) {
            handleRangedWeaponSelection(slotNumber);
        } else if (stateTracker.isWaitingForMeleeWeaponSelection()) {
            handleMeleeWeaponSelection(slotNumber);
        } else if (stateTracker.isWaitingForFactionSelection()) {
            handleFactionSelection(slotNumber);
        } else if (stateTracker.isWaitingForBatchCharacterCreation()) {
            handleBatchCharacterCreation(slotNumber);
        } else if (stateTracker.isWaitingForCharacterDeployment()) {
            handleCharacterDeployment(slotNumber);
        } else if (stateTracker.isWaitingForDirectCharacterAddition()) {
            handleDirectCharacterAddition(slotNumber);
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Specific Workflow Handlers
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Handle character creation archetype selection.
     * 
     * @param slotNumber The selected archetype number
     */
    private void handleCharacterCreationInput(int slotNumber) {
        if (slotNumber == 0) {
            System.out.println("*** Character creation cancelled ***");
            stateTracker.setWaitingForCharacterCreation(false);
        } else {
            // DevCycle 15j: Delegate to EditModeManager with validation
            editModeManager.handleCharacterArchetypeSelection(slotNumber);
        }
    }
    
    /**
     * Handle character ranged weapon selection during creation.
     * 
     * @param slotNumber The selected weapon number
     */
    private void handleCharacterRangedWeaponInput(int slotNumber) {
        if (slotNumber == 0) {
            System.out.println("*** Character creation cancelled ***");
            stateTracker.setWaitingForCharacterRangedWeapon(false);
        } else {
            // DevCycle 15d: Delegate to EditModeManager
            editModeManager.handleCharacterRangedWeaponSelection(slotNumber);
        }
    }
    
    /**
     * Handle character melee weapon selection during creation.
     * 
     * @param slotNumber The selected weapon number
     */
    private void handleCharacterMeleeWeaponInput(int slotNumber) {
        if (slotNumber == 0) {
            System.out.println("*** Character creation cancelled ***");
            stateTracker.setWaitingForCharacterMeleeWeapon(false);
        } else {
            // DevCycle 15d: Delegate to EditModeManager
            editModeManager.handleCharacterMeleeWeaponSelection(slotNumber);
        }
    }
    
    /**
     * Handle weapon type selection (ranged vs melee).
     * 
     * @param slotNumber The selected weapon type (1=Ranged, 2=Melee)
     */
    private void handleWeaponTypeSelection(int slotNumber) {
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
        }
    }
    
    /**
     * Handle ranged weapon selection for existing units.
     * 
     * @param slotNumber The selected weapon number
     */
    private void handleRangedWeaponSelection(int slotNumber) {
        if (slotNumber == 0) {
            System.out.println("*** Ranged weapon selection cancelled ***");
            stateTracker.setWaitingForRangedWeaponSelection(false);
        } else {
            ((EditModeController)callbacks).assignRangedWeaponToSelectedUnits(slotNumber);
            stateTracker.setWaitingForRangedWeaponSelection(false);
        }
    }
    
    /**
     * Handle melee weapon selection for existing units.
     * 
     * @param slotNumber The selected weapon number
     */
    private void handleMeleeWeaponSelection(int slotNumber) {
        if (slotNumber == 0) {
            System.out.println("*** Melee weapon selection cancelled ***");
            stateTracker.setWaitingForMeleeWeaponSelection(false);
        } else {
            ((EditModeController)callbacks).assignMeleeWeaponToSelectedUnits(slotNumber);
            stateTracker.setWaitingForMeleeWeaponSelection(false);
        }
    }
    
    /**
     * Handle faction selection for units.
     * 
     * @param slotNumber The selected faction number
     */
    private void handleFactionSelection(int slotNumber) {
        if (slotNumber == 0) {
            System.out.println("*** Faction selection cancelled ***");
            stateTracker.setWaitingForFactionSelection(false);
        } else {
            callbacks.assignFactionToSelectedUnits(slotNumber);
        }
    }
    
    /**
     * Handle batch character creation workflow.
     * 
     * @param slotNumber The input number for batch creation
     */
    private void handleBatchCharacterCreation(int slotNumber) {
        // DevCycle 15h: Delegate to CharacterCreationController
        boolean continueWorkflow = characterCreationController.handleBatchCharacterCreationInput(slotNumber);
        if (!continueWorkflow) {
            stateTracker.setWaitingForBatchCharacterCreation(false);
        }
    }
    
    /**
     * Handle character deployment workflow.
     * 
     * @param slotNumber The input number for deployment
     */
    private void handleCharacterDeployment(int slotNumber) {
        // Character deployment feature removed
        System.out.println("*** Error: Character deployment is no longer supported ***");
    }
    
    /**
     * Handle direct character addition workflow.
     * 
     * @param slotNumber The input number for character addition
     */
    private void handleDirectCharacterAddition(int slotNumber) {
        // DevCycle 15d: Delegate to EditModeManager
        editModeManager.handleDirectCharacterAdditionInput(slotNumber);
    }
    
    /**
     * Extract test slot character from KeyEvent.
     * 
     * @param e KeyEvent to extract test slot from
     * @return test slot character (a-z) or '?' if not a valid test slot key
     */
    private char extractTestSlotFromKeyEvent(KeyEvent e) {
        if (e.getCode() == KeyCode.A) return 'a';
        else if (e.getCode() == KeyCode.B) return 'b';
        else if (e.getCode() == KeyCode.C) return 'c';
        else if (e.getCode() == KeyCode.D) return 'd';
        else if (e.getCode() == KeyCode.E) return 'e';
        else if (e.getCode() == KeyCode.F) return 'f';
        else if (e.getCode() == KeyCode.G) return 'g';
        else if (e.getCode() == KeyCode.H) return 'h';
        else if (e.getCode() == KeyCode.I) return 'i';
        else if (e.getCode() == KeyCode.J) return 'j';
        else if (e.getCode() == KeyCode.K) return 'k';
        else if (e.getCode() == KeyCode.L) return 'l';
        else if (e.getCode() == KeyCode.M) return 'm';
        else if (e.getCode() == KeyCode.N) return 'n';
        else if (e.getCode() == KeyCode.O) return 'o';
        else if (e.getCode() == KeyCode.P) return 'p';
        else if (e.getCode() == KeyCode.Q) return 'q';
        else if (e.getCode() == KeyCode.R) return 'r';
        else if (e.getCode() == KeyCode.S) return 's';
        else if (e.getCode() == KeyCode.T) return 't';
        else if (e.getCode() == KeyCode.U) return 'u';
        else if (e.getCode() == KeyCode.V) return 'v';
        else if (e.getCode() == KeyCode.W) return 'w';
        else if (e.getCode() == KeyCode.X) return 'x';
        else if (e.getCode() == KeyCode.Y) return 'y';
        else if (e.getCode() == KeyCode.Z) return 'z';
        else return '?';
    }
    
    /**
     * Process test slot input for load game functionality.
     * 
     * @param testSlot The test slot character (a-z)
     */
    private void processTestSlotInput(char testSlot) {
        gameStateManager.handleTestSlotLoadInput(testSlot);
    }
    
    /**
     * Check if debug mode is currently active.
     * 
     * @return true if debug mode is enabled, false otherwise
     */
    private boolean isDebugModeActive() {
        try {
            // Use reflection to access GameRenderer's debug mode since it's in default package
            Class<?> gameRendererClass = Class.forName("GameRenderer");
            java.lang.reflect.Method isDebugMode = gameRendererClass.getMethod("isDebugMode");
            return (Boolean) isDebugMode.invoke(null);
        } catch (Exception e) {
            // If we can't access debug mode, default to false
            return false;
        }
    }
}
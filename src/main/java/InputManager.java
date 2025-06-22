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
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import combat.*;
import game.*;
import data.SkillsManager;
import data.SaveGameManager;
import data.UniversalCharacterRegistry;
import input.interfaces.InputManagerCallbacks;
import input.states.InputStates;

// DevCycle 15e Phase 4: Utility imports removed - functionality delegated to components

/**
 * InputManager handles all user input for the OpenFields2 game.
 * 
 * This is the central hub for all input processing in the game, managing both immediate
 * responses to user actions and complex multi-step workflows. The class coordinates
 * with multiple subsystems to provide a comprehensive input handling experience.
 * 
 * PRIMARY RESPONSIBILITIES:
 * - Mouse input processing (selection, movement, combat commands, edit mode operations)
 * - Keyboard input handling (camera controls, unit commands, game state management)
 * - Input state management (save/load prompts, character creation workflows)
 * - Edit mode operations (character creation, weapon assignment, faction management)
 * - Combat command processing (targeting, firing modes, melee combat)
 * - Game state controls (pause/resume, save/load, scenario management)
 * 
 * SYSTEM INTEGRATION:
 * - SelectionManager: Unit selection and multi-selection operations
 * - GameRenderer: Camera controls (pan, zoom, offset management)
 * - GameClock: Timing and event scheduling
 * - SaveGameManager: Game persistence and slot management  
 * - UniversalCharacterRegistry: Character data access and management
 * - Combat System: Attack commands, weapon state management, targeting
 * - Edit Mode: Character creation, deployment, faction assignment
 * 
 * INPUT PROCESSING ARCHITECTURE:
 * The InputManager uses a state-based approach to handle complex multi-step workflows.
 * Boolean flags track current input state (e.g., stateTracker.isWaitingForSaveSlot(), editMode) and
 * enum-based state machines manage complex workflows like character creation and deployment.
 * 
 * TABLE OF CONTENTS:
 * ==================
 * 1. Class Declaration and Field Definitions
 *    1.1 Core Game Dependencies
 *    1.2 Game State References  
 *    1.3 Input State Management Flags
 *    1.4 Workflow State Management
 *    1.5 Static Configuration and Utilities
 * 
 * 2. Inner Classes and Enums
 *    2.1 Callback Interfaces
 *    2.2 Workflow State Enums
 *    2.3 Data Transfer Objects
 *    2.4 JSON Deserialization Support
 * 
 * 3. Constructor and Initialization
 *    3.1 Primary Constructor
 *    3.2 Input Handler Setup
 *    3.3 Manager Instance Initialization
 * 
 * 4. Core Input Event Processing  
 *    4.1 Mouse Event Handlers
 *    4.2 Keyboard Event Handlers
 *    4.3 Event Routing and Delegation
 * 
 * 5. Game Control Operations
 *    5.1 Unit Movement and Positioning
 *    5.2 Combat Commands and Targeting
 *    5.3 Camera Controls and Navigation
 *    5.4 Game State Management (Pause/Resume)
 * 
 * 6. Edit Mode Operations
 *    6.1 Character Creation Workflows
 *    6.2 Weapon Assignment Systems
 *    6.3 Faction Management
 *    6.4 Batch Operations and Deployment
 * 
 * 7. Save/Load System Integration
 *    7.1 Save Slot Management
 *    7.2 Load Operations
 *    7.3 State Persistence
 * 
 * 8. Advanced Features
 *    8.1 Target Zone Selection
 *    8.2 Automatic Targeting Systems
 *    8.3 Multi-Character Operations
 *    8.4 Formation and Deployment Systems
 * 
 * 9. UI and Display Support
 *    9.1 Character Statistics Display
 *    9.2 Status Messages and Feedback
 *    9.3 Selection Visual Feedback
 *    9.4 Debug Information Display
 * 
 * 10. Utility Methods and Helpers
 *     10.1 Coordinate Conversion
 *     10.2 String Formatting and Display
 *     10.3 Validation and Error Handling
 *     10.4 State Management Helpers
 * 
 * 11. Workflow State Management
 *     11.1 Character Creation State Machine
 *     11.2 Deployment Workflow Management
 *     11.3 Victory/Scenario State Handling
 *     11.4 Multi-Step Input Processing
 * 
 * 12. Integration and Callback Methods
 *     12.1 Callback Interface Implementation
 *     12.2 Cross-System Communication
 *     12.3 Event Coordination
 * 
 * DESIGN PATTERNS USED:
 * - State Machine: Complex workflows use enum-based state tracking
 * - Command Pattern: Input commands delegate to appropriate handlers
 * - Observer Pattern: Callbacks notify main game of state changes
 * - Strategy Pattern: Different input modes use different processing strategies
 * 
 * PERFORMANCE CONSIDERATIONS:
 * - Input event processing is optimized for 60 FPS game loop
 * - State checks are arranged by frequency for optimal branching
 * - Heavy operations (file I/O, character creation) are batched appropriately
 * - Memory allocations are minimized in frequent event handlers
 */
public class InputManager {
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // SECTION 1: CLASS DECLARATION AND FIELD DEFINITIONS
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 1.1 Core Game Dependencies
    // ─────────────────────────────────────────────────────────────────────────────────
    // These final references connect InputManager to the core game systems
    
    /** List of all active units in the game - used for selection, movement, and combat operations */
    private final List<Unit> units;
    
    /** Manages unit selection state including single and multi-unit selection */
    private final SelectionManager selectionManager;
    
    /** Handles camera positioning, zoom, and rendering operations */
    private final GameRenderer gameRenderer;
    
    /** Provides game timing and tick management for event scheduling */
    private final GameClock gameClock;
    
    /** Priority queue for managing scheduled game events (attacks, effects, etc.) */
    private final PriorityQueue<ScheduledEvent> eventQueue;
    
    /** JavaFX Canvas for capturing mouse events and coordinate conversion */
    private final Canvas canvas;
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 1.1.1 Component Architecture (DevCycle 15c)
    // ─────────────────────────────────────────────────────────────────────────────────
    // Extracted components for incremental refactoring
    
    /** Routes input events to appropriate handlers based on application state */
    private final InputEventRouter eventRouter;
    
    /** Manages all input-related state flags and provides centralized state tracking */
    private final InputStateTracker stateTracker;
    
    /** Handles character creation, deployment, and edit mode workflows */
    private final EditModeManager editModeManager;
    
    /** Handles save/load operations, scenario management, and victory processing */
    private final GameStateManager gameStateManager;
    
    /** Handles combat-specific input processing and command coordination */
    private final CombatCommandProcessor combatCommandProcessor;
    
    /** Handles input-related display management, feedback coordination, and UI state */
    private final DisplayCoordinator displayCoordinator;
    
    /** Handles character creation workflows including batch creation and archetype selection */
    private final CharacterCreationController characterCreationController;
    
    /** Handles character deployment workflows including faction selection and formation placement */
    private final input.controllers.DeploymentController deploymentController;
    
    /** Handles victory outcome processing workflows including faction outcomes and scenario completion */
    private final VictoryOutcomeController victoryOutcomeController;
    
    /** Handles mouse input events including selection, movement, and combat targeting */
    private final MouseInputHandler mouseInputHandler;
    
    /** Handles keyboard input events including controls, shortcuts, and workflow navigation */
    private final KeyboardInputHandler keyboardInputHandler;
    
    /** Handles camera controls including navigation, zoom, and coordinate conversion */
    private final CameraController cameraController;
    
    /** Handles unit movement controls including speed adjustment and movement commands */
    private final MovementController movementController;
    
    /** Handles component lifecycle and system integration coordination */
    private final InputSystemIntegrator systemIntegrator;
    
    /** Handles workflow state coordination and management */
    private final WorkflowStateCoordinator workflowCoordinator;
    
    /** Handles input validation for all input types with consistent error reporting */
    private final InputValidationService inputValidationService;
    
    /** Handles diagnostic and debugging utilities for input system monitoring */
    private final InputDiagnosticService inputDiagnosticService;
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 1.2 Game State References
    // ─────────────────────────────────────────────────────────────────────────────────
    // Basic game state that InputManager needs to track for input processing
    
    /** Whether the game is currently paused - affects input processing priorities */
    private boolean paused;
    
    /** Next available unit ID for creating new units */
    private int nextUnitId;
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 1.3 Input State Management (DevCycle 15c)
    // ─────────────────────────────────────────────────────────────────────────────────
    // State management is now handled by InputStateTracker component.
    // All waitingFor... boolean flags have been moved to stateTracker.
    // Edit mode remains here as it's a core game state flag.
    
    /** Whether edit mode is active - changes input handling behavior significantly */
    private boolean editMode;
    
    /** List of units marked for deletion pending confirmation */
    private java.util.List<Unit> unitsToDelete = new java.util.ArrayList<>();
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 1.4 Workflow State Management
    // ─────────────────────────────────────────────────────────────────────────────────
    // Complex state management for multi-step workflows
    
    // Manual Victory Workflow State
    
    // Workflow State Management (DevCycle 15i: Moved to WorkflowStateCoordinator)
    // All workflow state variables and management are now handled by the WorkflowStateCoordinator component
    // This includes direct character addition, scenario creation, and other multi-step workflows
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // SECTION 2: INNER CLASSES AND ENUMS
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 2.1 Workflow State Management (DevCycle 15h: Moved to InputStates)
    // ─────────────────────────────────────────────────────────────────────────────────
    // State machine enums and data objects moved to input.states.InputStates
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 1.5 Additional State Variables
    // ─────────────────────────────────────────────────────────────────────────────────
    // Remaining state variables that support advanced features
    
    // Target Zone Selection State - DevCycle 15e: Moved to CombatCommandProcessor
    // Target zone selection is now handled by the CombatCommandProcessor component
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 1.6 Manager Dependencies  
    // ─────────────────────────────────────────────────────────────────────────────────
    // References to singleton managers for data access and persistence
    
    /** Singleton manager for save/load operations */
    private final SaveGameManager saveGameManager;
    
    /** Singleton registry for character data access */
    private final UniversalCharacterRegistry characterRegistry;
    
    /** Callback interface for operations requiring main game access */
    private final InputManagerCallbacks callbacks;
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 1.7 Debug and Diagnostic Configuration
    // ─────────────────────────────────────────────────────────────────────────────────
    // Configurable debug features with zero performance impact when disabled
    
    // DevCycle 15e Phase 4: Debug functionality moved to DisplayCoordinator
    
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // SECTION 3: CONSTRUCTOR AND INITIALIZATION
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 3.1 Primary Constructor
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Constructs a new InputManager with all necessary dependencies.
     * 
     * This constructor initializes the InputManager with references to all core game
     * systems needed for input processing. The InputManager acts as a central coordinator
     * for user input, delegating to appropriate subsystems based on current game state.
     * 
     * @param units List of game units for selection and command operations
     * @param selectionManager Unit selection state manager for tracking selected units
     * @param gameRenderer Game renderer for camera controls and coordinate conversion
     * @param gameClock Game clock for timing-based operations and event scheduling
     * @param eventQueue Priority queue for scheduling delayed game events
     * @param canvas JavaFX Canvas for capturing mouse input events
     * @param callbacks Callback interface for operations requiring main game access
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
        
        // Initialize singleton manager references
        this.saveGameManager = SaveGameManager.getInstance();
        this.characterRegistry = UniversalCharacterRegistry.getInstance();
        
        // DevCycle 15c: Initialize extracted components
        this.eventRouter = new InputEventRouter();
        this.stateTracker = new InputStateTracker();
        
        // DevCycle 15d: Initialize workflow components
        this.editModeManager = new EditModeManager(stateTracker, selectionManager, units, callbacks);
        
        // DevCycle 15e: Initialize game state components
        this.gameStateManager = new GameStateManager(stateTracker, units, callbacks);
        
        // DevCycle 15e: Initialize combat command components
        this.combatCommandProcessor = new CombatCommandProcessor(selectionManager, gameClock, eventQueue, (game.GameCallbacks) callbacks);
        
        // DevCycle 15e: Initialize display coordination components
        this.displayCoordinator = new DisplayCoordinator(selectionManager, gameClock, callbacks);
        
        // DevCycle 15h: Initialize character creation controller
        this.characterCreationController = new CharacterCreationController(units, canvas, gameRenderer, callbacks);
        
        // DevCycle 15h: Initialize deployment controller
        this.deploymentController = new input.controllers.DeploymentController(callbacks, units);
        
        // DevCycle 15h: Initialize victory outcome controller
        this.victoryOutcomeController = new VictoryOutcomeController(callbacks, units, selectionManager, eventQueue);
        
        // DevCycle 15h Phase 4: Initialize navigation and movement controllers
        this.cameraController = new CameraController(gameRenderer, displayCoordinator, canvas);
        this.movementController = new MovementController(units, selectionManager, displayCoordinator, callbacks);
        
        // DevCycle 15h: Initialize input handlers
        this.mouseInputHandler = new MouseInputHandler(units, selectionManager, gameRenderer, 
                                     displayCoordinator, eventRouter, editModeManager, combatCommandProcessor, 
                                     gameClock, eventQueue, callbacks, movementController);
        
        this.keyboardInputHandler = new KeyboardInputHandler(units, selectionManager, gameRenderer,
                                        displayCoordinator, editModeManager, combatCommandProcessor, gameClock,
                                        gameStateManager, characterCreationController, stateTracker, 
                                        cameraController, movementController, callbacks);
        
        // DevCycle 15h Phase 5: Initialize system integrator
        this.systemIntegrator = new InputSystemIntegrator(eventRouter, stateTracker, editModeManager,
                                   gameStateManager, combatCommandProcessor, displayCoordinator,
                                   characterCreationController, deploymentController, victoryOutcomeController,
                                   mouseInputHandler, keyboardInputHandler, cameraController, movementController,
                                   callbacks, units);
        
        // DevCycle 15i Phase 1: Initialize workflow state coordinator
        this.workflowCoordinator = new WorkflowStateCoordinator(stateTracker, displayCoordinator, callbacks);
        
        // DevCycle 15i Phase 2: Initialize input validation service
        this.inputValidationService = new InputValidationService();
        
        // DevCycle 15i Phase 3: Initialize input diagnostic service
        this.inputDiagnosticService = new InputDiagnosticService();
        
        // DevCycle 15e Phase 4: Set up debug callback for state tracking integration
        this.stateTracker.setDebugCallback((stateName, oldValue, newValue) -> {
            displayCoordinator.debugStateTransition("INPUT_STATE", oldValue ? stateName : "NONE", 
                                newValue ? stateName : "NONE");
        });
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 3.1.1 Component Lifecycle Management (DevCycle 15e Phase 4)
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Initialize all components and establish coordination patterns.
     * Called automatically by constructor but can be called for reinitialization.
     */
    public void initializeComponents() {
        // DevCycle 15h Phase 5: Delegate to system integrator
        systemIntegrator.initializeComponents();
    }
    
    /**
     * Validate component health and integration integrity.
     * Should be called periodically to ensure all components are functioning correctly.
     */
    public boolean validateComponentIntegrity() {
        // DevCycle 15h Phase 5: Delegate to system integrator
        return systemIntegrator.validateComponentIntegrity();
    }
    
    /**
     * Get system status information.
     */
    public InputSystemIntegrator.SystemStatusSummary getSystemStatus() {
        return systemIntegrator.getSystemStatus();
    }
    
    /**
     * Perform comprehensive system validation.
     */
    public InputSystemIntegrator.SystemValidationResult performSystemValidation() {
        return systemIntegrator.performSystemValidation();
    }
    
    /**
     * Check if input system is healthy.
     */
    public boolean isSystemHealthy() {
        return systemIntegrator.isSystemHealthy();
    }
    
    /**
     * Shutdown all components gracefully.
     */
    public void shutdownComponents() {
        systemIntegrator.shutdownComponents();
    }
    
    // Legacy validation method (for backward compatibility)
    private boolean legacyValidateComponentIntegrity() {
        inputDiagnosticService.recordDebugLog("LIFECYCLE", "INFO", "Validating component integrity");
        
        boolean allHealthy = true;
        
        // Validate each component
        if (eventRouter == null) {
            inputDiagnosticService.recordDebugLog("LIFECYCLE", "ERROR", "InputEventRouter not initialized");
            allHealthy = false;
        }
        
        if (stateTracker == null) {
            inputDiagnosticService.recordDebugLog("LIFECYCLE", "ERROR", "InputStateTracker not initialized");
            allHealthy = false;
        }
        
        // Additional legacy validation can be added here if needed
        
        if (allHealthy) {
            inputDiagnosticService.recordDebugLog("LIFECYCLE", "INFO", "All components healthy");
        } else {
            inputDiagnosticService.recordDebugLog("LIFECYCLE", "ERROR", "Component integrity validation failed");
        }
        
        return allHealthy;
    }
    
    /**
     * Get the InputStateTracker for external access to input state management.
     * Used by callbacks to delegate state management operations.
     * 
     * @return InputStateTracker instance for state management
     */
    public InputStateTracker getStateTracker() {
        return stateTracker;
    }
    
    /**
     * Get comprehensive component status for debugging and monitoring.
     */
    public String getComponentStatus() {
        return inputDiagnosticService.generateDiagnosticReport().toString();
    }
    
    
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 3.2 Input Handler Setup
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Initialize input event handlers for the JavaFX scene.
     * 
     * This method must be called after the JavaFX scene is created to attach
     * mouse and keyboard event handlers. The handlers are set up to capture
     * all relevant input events and route them through the InputManager's
     * processing pipeline.
     * 
     * @param scene JavaFX Scene to attach input handlers to
     */
    public void initializeInputHandlers(Scene scene) {
        setupMouseHandlers();
        setupKeyboardHandlers(scene);
    }
    
    /**
     * Configure mouse event handlers on the game canvas.
     * 
     * Sets up the three primary mouse event handlers:
     * - onMousePressed: Initiates selection, movement, and combat commands
     * - onMouseDragged: Handles selection rectangles and camera panning
     * - onMouseReleased: Completes selection operations and finalizes commands
     */
    private void setupMouseHandlers() {
        canvas.setOnMousePressed(mouseInputHandler::handleMousePressed);
        canvas.setOnMouseDragged(mouseInputHandler::handleMouseDragged);
        canvas.setOnMouseReleased(mouseInputHandler::handleMouseReleased);
    }
    
    /**
     * Configure keyboard event handlers on the JavaFX scene.
     * 
     * Attaches the primary keyboard event handler which processes all keyboard
     * input including game controls, unit commands, camera controls, and
     * workflow navigation commands.
     * 
     * @param scene JavaFX Scene to attach keyboard handlers to
     */
    private void setupKeyboardHandlers(Scene scene) {
        scene.setOnKeyPressed(keyboardInputHandler::handleKeyPressed);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // SECTION 4: CORE INPUT EVENT PROCESSING
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 4.1 Mouse Event Handlers
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Process mouse button press events.
     * 
     * This is the primary entry point for mouse input processing. It handles:
     * - Left click: Unit selection, rectangle selection start, character placement
     * - Right click: Combat commands, movement orders, context actions
     * - Coordinate conversion from screen to world space
     * - State-based routing to appropriate specialized handlers
     * 
     * @param e MouseEvent containing button type and screen coordinates
     */
    private void handleMousePressed(MouseEvent e) {
        inputDiagnosticService.startPerformanceTimer("MousePressed");
        double x = gameRenderer.screenToWorldX(e.getX());
        double y = gameRenderer.screenToWorldY(e.getY());
        
        inputDiagnosticService.recordInputEvent("MOUSE_PRESS", e.getButton() + " at screen(" + e.getX() + "," + e.getY() + 
                       ") world(" + String.format("%.1f", x) + "," + String.format("%.1f", y) + ")");
        inputDiagnosticService.recordInputEvent("MOUSE_TRACE", "Mouse pressed: " + e.getButton() + " at (" + String.format("%.1f", x) + "," + String.format("%.1f", y) + ")");
        
        if (e.getButton() == MouseButton.PRIMARY) {
            // DevCycle 15c: Use InputEventRouter to determine handling
            InputEventRouter.MouseEventRoute route = eventRouter.routeMouseEvent(e, 
                editModeManager.isInDeploymentPlacementMode(), 
                editModeManager.isInDirectAdditionPlacementMode(),
                editMode);
            
            switch (route) {
                case DEPLOYMENT_PLACEMENT:
                    // DevCycle 15d: Delegate to EditModeManager
                    editModeManager.completeCharacterDeployment(x, y);
                    return;
                case CHARACTER_PLACEMENT:
                    displayCoordinator.debugWorkflowState("DIRECT_ADDITION", "PLACEMENT", "Placing character at (" + 
                                     String.format("%.1f", x) + "," + String.format("%.1f", y) + ")");
                    // DevCycle 15d: Delegate to EditModeManager
                    editModeManager.handleCharacterPlacement(x, y);
                    inputDiagnosticService.endPerformanceTimer("MousePressed");
                    return;
                case UNIT_SELECTION:
                    // Continue with normal selection logic below
                    break;
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
                // DevCycle 15e: Delegate target zone selection to CombatCommandProcessor
                combatCommandProcessor.startTargetZoneSelection(x, y);
            } else {
                handleRightClick(clickedUnit, x, y, e.isShiftDown());
            }
        }
        
        inputDiagnosticService.endPerformanceTimer("MousePressed");
        inputDiagnosticService.logMemoryUsage("After MousePressed");
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
    private void handleMouseDragged(MouseEvent e) {
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
     * - Clears temporary UI state and prepares for next input
     * 
     * This handler ensures that drag-based operations have a clear completion
     * point and that the UI returns to a clean state after user interactions.
     * 
     * @param e MouseEvent containing button type and final release position
     */
    private void handleMouseReleased(MouseEvent e) {
        inputDiagnosticService.recordInputEvent("MOUSE_RELEASE", e.getButton() + " at screen(" + e.getX() + "," + e.getY() + ")");
        inputDiagnosticService.recordInputEvent("MOUSE_TRACE", "Mouse released: " + e.getButton());
        
        if (selectionManager.isSelecting() && e.getButton() == MouseButton.PRIMARY) {
            // Complete rectangle selection
            displayCoordinator.debugSelectionOperation("COMPLETE_RECTANGLE", "Finishing rectangle selection");
            selectionManager.completeRectangleSelection(units);
            
            if (selectionManager.hasSelection()) {
                displayCoordinator.debugSelectionOperation("MULTI_SELECT_COMPLETE", selectionManager.getSelectionCount() + " units selected");
                displayMultiCharacterSelection();
            }
        } else if (combatCommandProcessor.isSelectingTargetZone() && e.getButton() == MouseButton.SECONDARY && e.isShiftDown()) {
            // DevCycle 15e: Complete target zone selection via CombatCommandProcessor
            double x = gameRenderer.screenToWorldX(e.getX());
            double y = gameRenderer.screenToWorldY(e.getY());
            
            displayCoordinator.debugSelectionOperation("COMPLETE_TARGET_ZONE", "Target zone at (" + 
                                   String.format("%.1f", x) + "," + String.format("%.1f", y) + ")");
            combatCommandProcessor.completeTargetZoneSelection(x, y);
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
                
                // DevCycle 15e: Delegate combat operations to CombatCommandProcessor
                // Check if character is currently attacking - if so, cease fire; otherwise ready weapon
                combatCommandProcessor.handleSelfTargetCombat(clickedUnit, gameClock.getCurrentTick(), eventQueue);
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
                        
                        // DevCycle 15e: Delegate range check display to DisplayCoordinator
                        displayCoordinator.displayRangeCheck(selected, clickedUnit, distanceFeet);
                    }
                    return;
                }
                
                // DevCycle 15e: Delegate combat operations to CombatCommandProcessor
                combatCommandProcessor.handleCombatRightClick(x, y, clickedUnit, units);
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
                // DevCycle 15e: Delegate unit movement display to DisplayCoordinator
                displayCoordinator.displayUnitMovement(selectionManager.getSelectionCount(), x, y, true);
            } else {
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
                // DevCycle 15e: Delegate unit movement display to DisplayCoordinator
                displayCoordinator.displayUnitMovement(selectionManager.getSelectionCount(), x, y, false);
            }
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 4.2 Keyboard Event Handlers
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Process keyboard input events and route to appropriate handlers.
     * 
     * This is the central keyboard input dispatcher that processes all keyboard events
     * and routes them to specialized handlers based on current game state and input mode.
     * 
     * KEYBOARD INPUT CATEGORIES:
     * - Camera controls: Arrow keys (pan), +/- keys (zoom)
     * - Game controls: Space (pause/resume), Escape (cancel operations)
     * - Unit commands: W/S (movement speed), Q/E (aiming speed), R/M (combat modes)
     * - Edit mode: Character creation, weapon assignment, faction management
     * - Debug/Display: Shift+/ (character stats), various debug combinations
     * - Save/Load: Ctrl+S (save), Ctrl+L (load), numbered keys for slot selection
     * - Workflow navigation: Number keys, letter keys for multi-step processes
     * 
     * INPUT STATE ROUTING:
     * The method uses a hierarchical approach to input processing:
     * 1. Universal camera and game controls (always available)
     * 2. State-specific handlers (save/load prompts, character creation, etc.)
     * 3. Context-sensitive commands (unit controls when units selected)
     * 4. Edit mode operations (when edit mode is active)
     * 
     * @param e KeyEvent containing key code and modifier states
     */
    private void handleKeyPressed(KeyEvent e) {
        // DevCycle 15e Phase 4: Delegate debug operations to DisplayCoordinator
        inputDiagnosticService.startPerformanceTimer("KeyPressed");
        String modifiers = (e.isShiftDown() ? "Shift+" : "") + (e.isControlDown() ? "Ctrl+" : "") + (e.isAltDown() ? "Alt+" : "");
        inputDiagnosticService.recordInputEvent("KEY_PRESS", modifiers + e.getCode());
        inputDiagnosticService.recordInputEvent("KEY_TRACE", "Key pressed: " + modifiers + e.getCode());
        
        // Camera controls
        if (e.getCode() == KeyCode.UP) {
            inputDiagnosticService.recordInputEvent("CAMERA_CONTROL", "Pan up");
            gameRenderer.adjustOffset(0, 20);
        }
        if (e.getCode() == KeyCode.DOWN) {
            inputDiagnosticService.recordInputEvent("CAMERA_CONTROL", "Pan down");
            gameRenderer.adjustOffset(0, -20);
        }
        if (e.getCode() == KeyCode.LEFT) {
            inputDiagnosticService.recordInputEvent("CAMERA_CONTROL", "Pan left");
            gameRenderer.adjustOffset(20, 0);
        }
        if (e.getCode() == KeyCode.RIGHT) {
            inputDiagnosticService.recordInputEvent("CAMERA_CONTROL", "Pan right");
            gameRenderer.adjustOffset(-20, 0);
        }
        if (e.getCode() == KeyCode.EQUALS || e.getCode() == KeyCode.PLUS) {
            inputDiagnosticService.recordInputEvent("CAMERA_CONTROL", "Zoom in");
            gameRenderer.adjustZoom(1.1);
        }
        if (e.getCode() == KeyCode.MINUS) {
            inputDiagnosticService.recordInputEvent("CAMERA_CONTROL", "Zoom out");
            gameRenderer.adjustZoom(1.0 / 1.1);
        }
        
        // Game controls
        if (e.getCode() == KeyCode.SPACE) {
            boolean newPauseState = !callbacks.isPaused();
            // DevCycle 15e Phase 4: Delegate state transition debugging to DisplayCoordinator
            displayCoordinator.debugStateTransition("GAME_STATE", callbacks.isPaused() ? "PAUSED" : "RUNNING", 
                                newPauseState ? "PAUSED" : "RUNNING");
            callbacks.setPaused(newPauseState);
            // DevCycle 15e: Delegate pause status display to DisplayCoordinator
            displayCoordinator.displayPauseStatus(newPauseState, gameClock.getCurrentTick());
        }
        
        // Debug mode toggle
        if (e.getCode() == KeyCode.D && e.isControlDown()) {
            GameRenderer.setDebugMode(!GameRenderer.isDebugMode());
            // DevCycle 15e: Delegate debug mode status display to DisplayCoordinator
            displayCoordinator.displayDebugModeStatus(GameRenderer.isDebugMode());
        }
        
        // DevCycle 15e Phase 4: All debug operations delegated to DisplayCoordinator
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
        
        // Edit mode toggle
        if (e.getCode() == KeyCode.E && e.isControlDown()) {
            boolean newEditMode = !callbacks.isEditMode();
            callbacks.setEditMode(newEditMode);
            // DevCycle 15e: Delegate edit mode status display to DisplayCoordinator
            displayCoordinator.displayEditModeStatus(newEditMode);
        }
        
        // Edit mode operations
        handleEditModeKeys(e);
        
        // Unit deletion
        handleUnitDeletion(e);
        
        // DevCycle 15e: Delegate character stats display to DisplayCoordinator
        displayCoordinator.handleCharacterStatsDisplay(e);
        
        // Movement and aiming controls
        handleMovementControls(e);
        handleAimingControls(e);
        
        // DevCycle 15e: Delegate combat commands to CombatCommandProcessor
        combatCommandProcessor.handleCombatKeys(e);
        
        // Save/Load controls
        handleSaveLoadControls(e);
        
        // Handle prompt responses
        handlePromptInputs(e);
        
        inputDiagnosticService.endPerformanceTimer("KeyPressed");
        inputDiagnosticService.logMemoryUsage("After KeyPressed");
    }
    
    /**
     * Handle edit mode specific key commands
     * 
     * @param e KeyEvent
     */
    private void handleEditModeKeys(KeyEvent e) {
        // DevCycle 15d: Delegate edit mode operations to EditModeManager
        editModeManager.handleEditModeKeys(e);
        
        // DevCycle 15e: Delegate game state operations to GameStateManager
        if (e.getCode() == KeyCode.V && e.isControlDown() && e.isShiftDown()) {
            if (!isWaitingForInput()) {
                gameStateManager.promptForManualVictory();
            } else {
                System.out.println("*** Please complete current operation before processing victory ***");
            }
        }
        if (e.getCode() == KeyCode.N && e.isControlDown() && e.isShiftDown()) {
            if (!isWaitingForInput()) {
                gameStateManager.promptForNewScenario();
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
    // DevCycle 15e: handleCharacterStatsDisplay method removed - delegated to DisplayCoordinator
    
    /**
     * Handle movement type controls (W/S keys)
     * 
     * @param e KeyEvent
     */
    private void handleMovementControls(KeyEvent e) {
        // Movement type controls - W to increase, S to decrease
        if (e.getCode() == KeyCode.W && selectionManager.hasSelection()) {
            InputPatternUtilities.processSelectedCharacters(
                selectionManager,
                unit -> {
                    combat.MovementType previousType = unit.character.getCurrentMovementType();
                    unit.character.increaseMovementType();
                    combat.MovementType newType = unit.character.getCurrentMovementType();
                    
                    // Resume movement if stopped and speed was increased
                    if (unit.isStopped) {
                        unit.resumeMovement();
                    }
                },
                "", // Will handle display separately for special DisplayCoordinator integration
                ""
            );
            
            // Custom display handling to integrate with DisplayCoordinator
            if (selectionManager.getSelectionCount() == 1) {
                Unit unit = selectionManager.getSelected();
                combat.MovementType newType = unit.character.getCurrentMovementType();
                // DevCycle 15e: Delegate movement status display to DisplayCoordinator
                displayCoordinator.displayMovementTypeChange(unit, newType);
            } else {
                System.out.println("*** " + selectionManager.getSelectionCount() + " units movement speed increased");
            }
        }
        if (e.getCode() == KeyCode.S && selectionManager.hasSelection()) {
            InputPatternUtilities.processSelectedCharacters(
                selectionManager,
                unit -> {
                    combat.MovementType previousType = unit.character.getCurrentMovementType();
                    
                    // If already at crawling speed and currently moving, stop movement
                    if (previousType == combat.MovementType.CRAWL && unit.isMoving()) {
                        unit.stopMovement();
                    } else {
                        // Otherwise, decrease movement type normally
                        unit.character.decreaseMovementType();
                    }
                },
                "", // Will handle display separately for special DisplayCoordinator integration
                ""
            );
            
            // Custom display handling to integrate with DisplayCoordinator
            if (selectionManager.getSelectionCount() == 1) {
                Unit unit = selectionManager.getSelected();
                combat.MovementType newType = unit.character.getCurrentMovementType();
                // DevCycle 15e: Delegate movement status display to DisplayCoordinator
                displayCoordinator.displayMovementTypeChange(unit, newType);
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
            InputPatternUtilities.processSelectedCharacters(
                selectionManager,
                unit -> unit.character.increaseAimingSpeed(),
                "", // Will handle display separately for special DisplayCoordinator integration
                ""
            );
            
            // Custom display handling to integrate with DisplayCoordinator
            if (selectionManager.getSelectionCount() == 1) {
                Unit unit = selectionManager.getSelected();
                combat.AimingSpeed newSpeed = unit.character.getCurrentAimingSpeed();
                // DevCycle 15e: Delegate aiming speed status display to DisplayCoordinator
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
                // DevCycle 15e: Delegate aiming speed status display to DisplayCoordinator
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
            InputPatternUtilities.processSelectedCharacters(
                selectionManager,
                unit -> unit.character.toggleCombatMode(),
                "", // Will handle display separately to determine mode after toggle
                ""
            );
            
            // Custom display handling to show correct combat mode after toggle
            if (selectionManager.getSelectionCount() == 1) {
                Unit unit = selectionManager.getSelected();
                String modeText = unit.character.isMeleeCombatMode() ? "Melee Combat" : "Ranged Combat";
                System.out.println("*** " + unit.character.getDisplayName() + " switched to " + modeText + " mode");
            } else {
                System.out.println("*** " + selectionManager.getSelectionCount() + " units toggled combat mode");
            }
        }
    }
    
    // DevCycle 15e: handleAutomaticTargetingToggle moved to CombatCommandProcessor
    
    /**
     * Handle save/load controls (Ctrl+S/Ctrl+L)
     * 
     * @param e KeyEvent
     */
    private void handleSaveLoadControls(KeyEvent e) {
        // DevCycle 15e: Delegate save/load operations to GameStateManager
        gameStateManager.handleSaveLoadControls(e);
    }
    
    /**
     * Handle input responses to prompts (number keys, escape)
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
        
        // DevCycle 15e: Handle victory outcome selection
        if (stateTracker.isWaitingForVictoryOutcome()) {
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
            return; // Don't process other input while waiting for victory outcome
        }
        
        // DevCycle 15j: Handle scenario name input  
        if (stateTracker.isWaitingForScenarioName()) {
            gameStateManager.handleScenarioNameTextInput(e);
            return;
        }
        
        // DevCycle 15j: Handle theme selection
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
                handleThemeSelectionInput(themeNumber);
            }
            return;
        }
        
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
            else if (e.getCode() == KeyCode.ESCAPE) {
                if (stateTracker.isWaitingForCharacterCreation()) {
                    System.out.println("*** Character creation cancelled ***");
                    stateTracker.setWaitingForCharacterCreation(false);
                    // DevCycle 15d: Reset handled by EditModeManager
                } else if (stateTracker.isWaitingForCharacterRangedWeapon()) {
                    System.out.println("*** Character creation cancelled ***");
                    stateTracker.setWaitingForCharacterRangedWeapon(false);
                    // DevCycle 15d: Reset handled by EditModeManager
                } else if (stateTracker.isWaitingForCharacterMeleeWeapon()) {
                    System.out.println("*** Character creation cancelled ***");
                    stateTracker.setWaitingForCharacterMeleeWeapon(false);
                    // DevCycle 15d: Reset handled by EditModeManager
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
                    // DevCycle 15h: State reset now handled by CharacterCreationController
                } else if (stateTracker.isWaitingForCharacterDeployment()) {
                    System.out.println("*** Character deployment cancelled ***");
                    cancelCharacterDeployment();
                } else if (stateTracker.isWaitingForDirectCharacterAddition()) {
                    System.out.println("*** Character addition cancelled ***");
                    cancelDirectCharacterAddition();
                } else {
                    // DevCycle 15e: Delegate save/load cancellation to GameStateManager
                    gameStateManager.cancelSaveLoad();
                }
            }
            
            if (slotNumber >= 0 && slotNumber <= 9) {
                if (stateTracker.isWaitingForSaveSlot() || stateTracker.isWaitingForLoadSlot()) {
                    // DevCycle 15e: Delegate save/load slot handling to GameStateManager
                    gameStateManager.handleSaveLoadInput(slotNumber);
                } else if (stateTracker.isWaitingForCharacterCreation()) {
                    if (slotNumber == 0) {
                        System.out.println("*** Character creation cancelled ***");
                        stateTracker.setWaitingForCharacterCreation(false);
                        // DevCycle 15d: Reset handled by EditModeManager
                    } else {
                        // DevCycle 15j: Delegate to EditModeManager with validation
                        editModeManager.handleCharacterArchetypeSelection(slotNumber);
                    }
                } else if (stateTracker.isWaitingForCharacterRangedWeapon()) {
                    if (slotNumber == 0) {
                        System.out.println("*** Character creation cancelled ***");
                        stateTracker.setWaitingForCharacterRangedWeapon(false);
                        // DevCycle 15d: Reset handled by EditModeManager
                    } else {
                        // DevCycle 15d: Delegate to EditModeManager
                        editModeManager.handleCharacterRangedWeaponSelection(slotNumber);
                    }
                } else if (stateTracker.isWaitingForCharacterMeleeWeapon()) {
                    if (slotNumber == 0) {
                        System.out.println("*** Character creation cancelled ***");
                        stateTracker.setWaitingForCharacterMeleeWeapon(false);
                        // DevCycle 15d: Reset handled by EditModeManager
                    } else {
                        // DevCycle 15d: Delegate to EditModeManager
                        editModeManager.handleCharacterMeleeWeaponSelection(slotNumber);
                    }
                } else if (stateTracker.isWaitingForWeaponSelection()) {
                    // Handle weapon type selection (1=Ranged, 2=Melee)
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
                } else if (stateTracker.isWaitingForRangedWeaponSelection()) {
                    if (slotNumber == 0) {
                        System.out.println("*** Ranged weapon selection cancelled ***");
                        stateTracker.setWaitingForRangedWeaponSelection(false);
                    } else {
                        ((EditModeController)callbacks).assignRangedWeaponToSelectedUnits(slotNumber);
                        stateTracker.setWaitingForRangedWeaponSelection(false);
                    }
                } else if (stateTracker.isWaitingForMeleeWeaponSelection()) {
                    if (slotNumber == 0) {
                        System.out.println("*** Melee weapon selection cancelled ***");
                        stateTracker.setWaitingForMeleeWeaponSelection(false);
                    } else {
                        ((EditModeController)callbacks).assignMeleeWeaponToSelectedUnits(slotNumber);
                        stateTracker.setWaitingForMeleeWeaponSelection(false);
                    }
                } else if (stateTracker.isWaitingForFactionSelection()) {
                    if (slotNumber == 0) {
                        System.out.println("*** Faction selection cancelled ***");
                        stateTracker.setWaitingForFactionSelection(false);
                    } else {
                        callbacks.assignFactionToSelectedUnits(slotNumber);
                    }
                } else if (stateTracker.isWaitingForBatchCharacterCreation()) {
                    // DevCycle 15h: Delegate to CharacterCreationController
                    boolean continueWorkflow = characterCreationController.handleBatchCharacterCreationInput(slotNumber);
                    if (!continueWorkflow) {
                        stateTracker.setWaitingForBatchCharacterCreation(false);
                    }
                } else if (stateTracker.isWaitingForCharacterDeployment()) {
                    // DevCycle 15d: Delegate to EditModeManager
                    editModeManager.handleCharacterDeploymentInput(slotNumber);
                } else if (stateTracker.isWaitingForDirectCharacterAddition()) {
                    // DevCycle 15d: Delegate to EditModeManager
                    editModeManager.handleDirectCharacterAdditionInput(slotNumber);
                }
            }
        }
    }
    
    // State management methods for coordination with main class
    public void setWaitingForSaveSlot(boolean waiting) {
        this.stateTracker.setWaitingForSaveSlot(waiting);
    }
    
    public void setWaitingForLoadSlot(boolean waiting) {
        this.stateTracker.setWaitingForLoadSlot(waiting);
    }
    
    public void setWaitingForCharacterCreation(boolean waiting) {
        this.stateTracker.setWaitingForCharacterCreation(waiting);
    }
    
    public void setWaitingForWeaponSelection(boolean waiting) {
        this.stateTracker.setWaitingForWeaponSelection(waiting);
    }
    
    public void setWaitingForFactionSelection(boolean waiting) {
        this.stateTracker.setWaitingForFactionSelection(waiting);
    }
    
    public void setWaitingForBatchCharacterCreation(boolean waiting) {
        this.stateTracker.setWaitingForBatchCharacterCreation(waiting);
    }
    
    public void setWaitingForCharacterDeployment(boolean waiting) {
        this.stateTracker.setWaitingForCharacterDeployment(waiting);
    }
    
    public void setWaitingForDeletionConfirmation(boolean waiting) {
        this.stateTracker.setWaitingForDeletionConfirmation(waiting);
    }
    
    public void setWaitingForVictoryOutcome(boolean waiting) {
        // DevCycle 15e: Delegate to GameStateManager
        gameStateManager.setWaitingForVictoryOutcome(waiting);
    }
    
    
    
    public boolean isWaitingForInput() {
        return stateTracker.isWaitingForSaveSlot() || stateTracker.isWaitingForLoadSlot() || stateTracker.isWaitingForCharacterCreation() || 
               stateTracker.isWaitingForWeaponSelection() || stateTracker.isWaitingForRangedWeaponSelection() || stateTracker.isWaitingForMeleeWeaponSelection() ||
               stateTracker.isWaitingForFactionSelection() || stateTracker.isWaitingForBatchCharacterCreation() || 
               stateTracker.isWaitingForCharacterDeployment() || stateTracker.isWaitingForDeletionConfirmation() || stateTracker.isWaitingForVictoryOutcome() ||
               stateTracker.isWaitingForScenarioName() || stateTracker.isWaitingForThemeSelection() || stateTracker.isWaitingForCharacterRangedWeapon() || 
               stateTracker.isWaitingForCharacterMeleeWeapon() || stateTracker.isWaitingForDirectCharacterAddition();
    }
    
    /**
     * Start the batch character creation workflow (DevCycle 15h: Delegated to CharacterCreationController)
     */
    public void promptForBatchCharacterCreation() {
        stateTracker.setWaitingForBatchCharacterCreation(true);
        characterCreationController.promptForBatchCharacterCreation();
    }
    
    // DevCycle 15h: Character creation methods moved to CharacterCreationController
    // All batch character creation functionality is now handled by the dedicated controller
    
    
    /**
     * Start the character deployment workflow (DevCycle 15h: Delegated to DeploymentController)
     */
    private void promptForCharacterDeployment() {
        deploymentController.promptForCharacterDeployment();
    }
    
    /**
     * Handle input during character deployment workflow (DevCycle 15h: Delegated to DeploymentController)
     * 
     * @param inputNumber The number entered by the user
     */
    private void handleCharacterDeploymentInput(int inputNumber) {
        deploymentController.handleCharacterDeploymentInput(inputNumber);
    }
    
    
    
    
    
    
    
    /**
     * Cancel character deployment and reset state (DevCycle 15h: Delegated to DeploymentController)
     */
    private void cancelCharacterDeployment() {
        deploymentController.cancelCharacterDeployment();
    }
    
    
    
    
    /**
     * Check if we're in deployment placement mode (DevCycle 15h: Delegated to DeploymentController)
     */
    public boolean isInDeploymentPlacementMode() {
        return deploymentController.isInDeploymentPlacementMode();
    }
    
    /**
     * Handle deployment click placement (DevCycle 15h: Delegated to DeploymentController)
     */
    public void handleDeploymentPlacement(double worldX, double worldY) {
        deploymentController.handleDeploymentPlacement(worldX, worldY);
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
        
        stateTracker.setWaitingForDeletionConfirmation(true);
        
        System.out.println("***********************");
        System.out.println("*** UNIT DELETION CONFIRMATION ***");
        System.out.println("You are about to delete " + unitsToDelete.size() + " unit(s):");
        
        for (Unit unit : unitsToDelete) {
            System.out.println("  " + unit.character.getDisplayName() + " (ID: " + unit.character.id + 
                             ", Faction: " + InputUtils.getFactionName(unit.character.getFaction() + 1) + ")");
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
        stateTracker.setWaitingForDeletionConfirmation(false);
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
        java.util.Set<Integer> factionsInScenario = new java.util.HashSet<>();
        for (Unit unit : units) {
            factionsInScenario.add(unit.character.getFaction());
        }
        
        if (factionsInScenario.isEmpty()) {
            System.out.println("*** No factions present in current scenario ***");
            System.out.println("*** Manual victory not applicable ***");
            return;
        }
        
        java.util.List<Integer> factionList = new java.util.ArrayList<>(factionsInScenario);
        
        System.out.println("***********************");
        System.out.println("*** MANUAL VICTORY SYSTEM ***");
        System.out.println("Factions in current scenario: " + factionList.size());
        
        for (Integer factionId : factionList) {
            int characterCount = 0;
            for (Unit unit : units) {
                if (unit.character.getFaction() == factionId) {
                    characterCount++;
                }
            }
            System.out.println("  " + InputUtils.getFactionName(factionId + 1) + ": " + characterCount + " characters");
        }
        
        System.out.println();
        System.out.println("You will now assign victory outcomes to each faction.");
        System.out.println("***********************");
        
        // DevCycle 15h: Delegate to VictoryOutcomeController
        victoryOutcomeController.setScenarioFactions(factionList);
        victoryOutcomeController.promptForNextFactionOutcome();
    }
    
    /**
     * Prompt for the next faction's victory outcome
     */
    private void promptForNextFactionOutcome() {
        // DevCycle 15h: Delegate to VictoryOutcomeController
        victoryOutcomeController.promptForNextFactionOutcome();
    }
    
    /**
     * Handle input for victory outcome selection (DevCycle 15h: Delegated to VictoryOutcomeController)
     * 
     * @param outcomeNumber The number entered by the user
     */
    private void handleVictoryOutcomeInput(int outcomeNumber) {
        victoryOutcomeController.handleVictoryOutcomeInput(outcomeNumber);
    }
    
    
    
    
    /**
     * Cancel manual victory and reset state
     */
    private void cancelManualVictory() {
        // DevCycle 15h: Delegate to VictoryOutcomeController
        victoryOutcomeController.cancelManualVictory();
    }
    
    
    /**
     * Start the new scenario workflow (DevCycle 15i: Delegated to WorkflowStateCoordinator)
     */
    private void promptForNewScenario() {
        workflowCoordinator.startScenarioCreation();
        
        System.out.println("***********************");
        System.out.println("*** CREATE NEW SCENARIO ***");
        System.out.println("This will clear all units from the current battlefield.");
        System.out.println("Character data will be preserved in faction files.");
        System.out.println();
        System.out.println("Enter scenario name (or press ESC to cancel): ");
        System.out.print("> ");
        
    }
    
    /**
     * Handle scenario name input when ENTER is pressed (DevCycle 15j: Simplified to delegation)
     */
    private void handleScenarioNameInput() {
        gameStateManager.handleScenarioNameInput();
    }
    
    /**
     * Prompt for theme selection (DevCycle 15j: Simplified to delegation)
     */
    private void promptForThemeSelection() {
        gameStateManager.handleScenarioNameInput();
    }
    
    /**
     * Handle theme selection input (DevCycle 15j: Simplified to delegation)
     * 
     * @param themeNumber The number entered by the user
     */
    private void handleThemeSelectionInput(int themeNumber) {
        gameStateManager.handleThemeSelectionInput(themeNumber);
    }
    
    /**
     * Execute the new scenario creation
     */
    private void executeNewScenario() {
        System.out.println("***********************");
        System.out.println("*** CREATING NEW SCENARIO ***");
        System.out.println("Scenario: \"" + workflowCoordinator.getNewScenarioName().trim() + "\"");
        System.out.println("Theme: " + getThemeDisplayName(workflowCoordinator.getNewScenarioTheme()));
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
            callbacks.setCurrentTheme(workflowCoordinator.getNewScenarioTheme());
            
            // Update window title with scenario name
            callbacks.setWindowTitle("OpenFields2 - " + workflowCoordinator.getNewScenarioName().trim());
            
            System.out.println("*** NEW SCENARIO CREATED ***");
            System.out.println("Cleared " + clearedUnits + " units from battlefield");
            System.out.println("Applied theme: " + getThemeDisplayName(workflowCoordinator.getNewScenarioTheme()));
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
     * Cancel new scenario creation and reset state (DevCycle 15j: Simplified to delegation)
     */
    private void cancelNewScenario() {
        gameStateManager.cancelNewScenario();
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
    // DevCycle 15e: handleTargetZoneControls moved to CombatCommandProcessor
    
    // DevCycle 15e: handleFiringModeControls moved to CombatCommandProcessor
    
    // DevCycle 15e: completeTargetZoneSelection moved to CombatCommandProcessor
    
    // DevCycle 15e: performCeaseFire moved to CombatCommandProcessor
    
    // DevCycle 15e: startMeleeAttackSequence moved to CombatCommandProcessor
    
    /**
     * Debug print helper that only outputs when in debug mode
     */
    
    // DevCycle 15e: initiateMovementToMeleeTarget moved to CombatCommandProcessor
    
    /**
     * Reset character creation state variables (DevCycle 15h: Delegated to CharacterCreationController)
     */
    private void resetCharacterCreationState() {
        characterCreationController.setSelectedArchetype("");
        characterCreationController.setSelectedRangedWeapon("");
        characterCreationController.setSelectedMeleeWeapon("");
        stateTracker.setWaitingForCharacterCreation(false);
        stateTracker.setWaitingForCharacterRangedWeapon(false);
        stateTracker.setWaitingForCharacterMeleeWeapon(false);
    }
    
    /**
     * Handle archetype selection for character creation
     */
    private void handleCharacterArchetypeSelection(int archetypeIndex) {
        String[] archetypes = {"gunslinger", "soldier", "weighted_random", "scout", "marksman", "brawler", "confederate_soldier", "union_soldier", "balanced"};
        
        InputValidationService.ValidationResult result = InputValidationService.validateArchetypeSelection(archetypeIndex, archetypes.length);
        if (result.isValid) {
            characterCreationController.setSelectedArchetype(archetypes[archetypeIndex - 1]);
            
            // Move to ranged weapon selection
            stateTracker.setWaitingForCharacterCreation(false);
            stateTracker.setWaitingForCharacterRangedWeapon(true);
            promptForCharacterRangedWeaponSelection();
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
        System.out.println("Select ranged weapon for " + characterCreationController.getSelectedArchetype() + ":");
        
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
        
        InputPatternUtilities.validateRangeAndExecute(weaponIndex, 1, weaponIds.length, () -> {
            characterCreationController.setSelectedRangedWeapon(weaponIds[weaponIndex - 1]);
            
            // Move to melee weapon selection
            stateTracker.setWaitingForCharacterRangedWeapon(false);
            stateTracker.setWaitingForCharacterMeleeWeapon(true);
            promptForCharacterMeleeWeaponSelection();
        }, "weapon selection");
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
        System.out.println("Select melee weapon for " + characterCreationController.getSelectedArchetype() + ":");
        
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
        
        InputPatternUtilities.validateRangeAndExecute(weaponIndex, 1, meleeWeaponIds.length + 1, () -> {
            if (weaponIndex == 1) {
                // User selected "Unarmed"
                characterCreationController.setSelectedMeleeWeapon("unarmed");
                completeCharacterCreation();
            } else {
                // User selected a melee weapon
                characterCreationController.setSelectedMeleeWeapon(meleeWeaponIds[weaponIndex - 2]);
                completeCharacterCreation();
            }
        }, "weapon selection");
    }
    
    /**
     * Complete character creation with selected archetype and weapons
     */
    private void completeCharacterCreation() {
        try {
            // Create character using CharacterFactory
            int characterId = data.CharacterFactory.createCharacter(characterCreationController.getSelectedArchetype());
            combat.Character character = data.UniversalCharacterRegistry.getInstance().getCharacter(characterId);
            
            if (character != null) {
                // Assign selected ranged weapon
                character.weapon = data.WeaponFactory.createWeapon(characterCreationController.getSelectedRangedWeapon());
                character.currentWeaponState = character.weapon.getInitialState();
                
                // Assign selected melee weapon
                if (!"unarmed".equals(characterCreationController.getSelectedMeleeWeapon())) {
                    character.meleeWeapon = combat.MeleeWeaponFactory.createWeapon(characterCreationController.getSelectedMeleeWeapon());
                }
                
                character.setFaction(1); // Default faction
                
                // Spawn character at camera center
                spawnCharacterUnit(character, characterCreationController.getSelectedArchetype());
                
                // Display character creation confirmation
                System.out.println("*** Character created successfully! ***");
                System.out.println("Name: " + character.getDisplayName());
                System.out.println("Archetype: " + characterCreationController.getSelectedArchetype());
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
        javafx.scene.paint.Color characterColor = InputUtils.getArchetypeColor(archetype);
        
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
    
    /**
     * Start the direct character addition workflow
     */
    private void promptForDirectCharacterAddition() {
        workflowCoordinator.startDirectCharacterAddition();
        
        System.out.println("***********************");
        System.out.println("*** DIRECT CHARACTER ADDITION ***");
        System.out.println("Select faction:");
        
        // Display factions with available character counts
        for (int i = 1; i <= 3; i++) {
            InputStates.FactionCharacterInfo info = getFactionCharacterInfo(i);
            System.out.println(i + ". " + info.factionName + " (" + info.availableCount + " available characters)");
        }
        
        System.out.println("0. Cancel addition");
        System.out.println();
        System.out.println("Enter faction (1-3, 0 to cancel): ");
    }
    
    /**
     * Handle user input for direct character addition workflow (DevCycle 15i: Delegated to WorkflowStateCoordinator)
     */
    private void handleDirectCharacterAdditionInput(int inputNumber) {
        switch (workflowCoordinator.getDirectAdditionStep()) {
            case FACTION:
                InputPatternUtilities.handleCancellationOrRangeValidation(
                    inputNumber,
                    "Character addition",
                    () -> workflowCoordinator.cancelDirectCharacterAddition(),
                    1, 3,
                    () -> {
                        InputStates.FactionCharacterInfo info = getFactionCharacterInfo(inputNumber);
                        if (info.availableCount == 0) {
                            System.out.println("*** No available characters in " + info.factionName + " ***");
                            System.out.println("*** Please select a different faction or press 0 to cancel ***");
                        } else if (workflowCoordinator.processDirectAdditionFactionSelection(inputNumber)) {
                            System.out.println("***********************");
                            System.out.println("*** CHARACTER QUANTITY ***");
                            System.out.println("Selected faction: " + info.factionName);
                            System.out.println("Available characters: " + info.availableCount);
                            System.out.println("How many characters to add?");
                            System.out.println("Enter quantity (1-" + Math.min(20, info.availableCount) + ", 0 to cancel): ");
                        }
                    }
                );
                break;
                
            case QUANTITY:
                InputPatternUtilities.handleCancellationOrValidateAndExecute(
                    inputNumber,
                    "Character addition",
                    () -> workflowCoordinator.cancelDirectCharacterAddition(),
                    () -> {
                        InputStates.FactionCharacterInfo info = getFactionCharacterInfo(workflowCoordinator.getDirectAdditionFaction() + 1);
                        return InputValidationService.validateCharacterQuantity(inputNumber, info.availableCount);
                    },
                    () -> {
                        if (workflowCoordinator.processDirectAdditionQuantitySelection(inputNumber)) {
                            System.out.println("***********************");
                            System.out.println("*** CHARACTER SPACING ***");
                            System.out.println("Set spacing between characters:");
                            System.out.println("1. 1 foot");
                            System.out.println("2. 2 feet");
                            System.out.println("3. 3 feet");
                            System.out.println("4. 4 feet");
                            System.out.println("5. 5 feet (default)");
                            System.out.println("6. 6 feet");
                            System.out.println("7. 7 feet");
                            System.out.println("8. 8 feet");
                            System.out.println("9. 9 feet");
                            System.out.println("0. Cancel addition");
                            System.out.println();
                            System.out.println("Enter spacing (1-9, 0 to cancel): ");
                        }
                    }
                );
                break;
                
            case SPACING:
                InputPatternUtilities.handleCancellationOrRangeValidation(inputNumber, 
                    "Character addition",
                    () -> workflowCoordinator.cancelDirectCharacterAddition(),
                    1, 9,
                    () -> {
                        if (workflowCoordinator.processDirectAdditionSpacingSelection(inputNumber)) {
                            System.out.println("***********************");
                            System.out.println("*** CHARACTER PLACEMENT ***");
                            System.out.println("Click on the map to place " + workflowCoordinator.getDirectAdditionQuantity() + " characters");
                            System.out.println("Faction: " + (workflowCoordinator.getDirectAdditionFaction() + 1) + " | Spacing: " + workflowCoordinator.getDirectAdditionSpacing() + " feet");
                            System.out.println("Characters will be placed in a line going right from your click point");
                            System.out.println("Press ESC to cancel");
                        }
                    });
                break;
                
            case PLACEMENT:
                // Placement is handled by mouse clicks, not number input
                break;
        }
    }
    
    /**
     * Cancel the direct character addition workflow and reset state (DevCycle 15i: Delegated to WorkflowStateCoordinator)
     */
    private void cancelDirectCharacterAddition() {
        workflowCoordinator.cancelDirectCharacterAddition();
    }
    
    /**
     * Add workflow delegation methods for text input (DevCycle 15i: Workflow State Coordination)
     */
    
    /**
     * Append character to scenario name during text input.
     * 
     * @param character Character to append
     */
    public void appendToScenarioName(char character) {
        workflowCoordinator.appendToScenarioName(character);
    }
    
    /**
     * Remove last character from scenario name (backspace).
     */
    public void removeLastCharacterFromScenarioName() {
        workflowCoordinator.removeLastCharacterFromScenarioName();
    }
    
    /**
     * Get current workflow state coordinator.
     * 
     * @return WorkflowStateCoordinator instance
     */
    public WorkflowStateCoordinator getWorkflowCoordinator() {
        return workflowCoordinator;
    }
    
    /**
     * Handle character placement at the clicked location
     */
    private void handleCharacterPlacement(double x, double y) {
        System.out.println("***********************");
        System.out.println("*** PLACING CHARACTERS ***");
        System.out.println("Deploying " + workflowCoordinator.getDirectAdditionQuantity() + " characters from faction " + (workflowCoordinator.getDirectAdditionFaction() + 1));
        
        // Get available characters from faction
        InputStates.FactionCharacterInfo info = getFactionCharacterInfo(workflowCoordinator.getDirectAdditionFaction() + 1);
        if (info.availableCharacters.size() < workflowCoordinator.getDirectAdditionQuantity()) {
            System.out.println("*** Error: Not enough available characters ***");
            System.out.println("*** Available: " + info.availableCharacters.size() + ", Requested: " + workflowCoordinator.getDirectAdditionQuantity() + " ***");
            cancelDirectCharacterAddition();
            return;
        }
        
        // Convert spacing from feet to pixels (7 pixels = 1 foot)
        double spacingPixels = workflowCoordinator.getDirectAdditionSpacing() * 7.0;
        
        // Deploy characters in a horizontal line going right
        for (int i = 0; i < workflowCoordinator.getDirectAdditionQuantity(); i++) {
            // Calculate position for this character
            double charX = x + (i * (spacingPixels + 21)); // spacing + character diameter (21 pixels)
            double charY = y;
            
            // Get the next available character from faction
            combat.Character character = info.availableCharacters.get(i);
            
            // Set up weapons for the character if not already set
            if (character.weapon == null) {
                character.weapon = data.WeaponFactory.createWeapon("wpn_colt_peacemaker");
                character.currentWeaponState = character.weapon.getInitialState();
            }
            if (character.meleeWeapon == null) {
                character.meleeWeapon = combat.MeleeWeaponFactory.createWeapon("mel_sword");
            }
            
            // Get faction color
            javafx.scene.paint.Color factionColor = getFactionColor(workflowCoordinator.getDirectAdditionFaction());
            
            // Create and place the unit with all required parameters
            Unit newUnit = new Unit(character, charX, charY, factionColor, nextUnitId++);
            
            // Add to units list
            units.add(newUnit);
            
            System.out.println("Character " + (i + 1) + ": " + character.getDisplayName() + " placed at (" + String.format("%.0f", charX) + ", " + String.format("%.0f", charY) + ")");
        }
        
        System.out.println("*** Character deployment complete ***");
        System.out.println("***********************");
        
        // Reset state
        cancelDirectCharacterAddition();
    }
    
    /**
     * Generate a random character for the specified faction
     */
    private combat.Character generateRandomCharacterForFaction(int faction) {
        // Generate random stats for the character
        java.util.Random random = new java.util.Random();
        
        // Generate random names
        String[] firstNames = {"John", "Mary", "James", "Sarah", "Robert", "Jane", "William", "Emma"};
        String[] lastNames = {"Smith", "Johnson", "Brown", "Wilson", "Davis", "Miller", "Moore", "Taylor"};
        String firstName = firstNames[random.nextInt(firstNames.length)];
        String lastName = lastNames[random.nextInt(lastNames.length)];
        String nickname = firstName + " " + lastName.charAt(0) + ".";
        
        // Generate random stats (40-90 range for variety)
        int dexterity = 40 + random.nextInt(51); // 40-90
        int strength = 40 + random.nextInt(51);  // 40-90
        int reflexes = 40 + random.nextInt(51);  // 40-90
        int coolness = 40 + random.nextInt(51);  // 40-90
        int health = 60 + random.nextInt(41);    // 60-100
        
        // Random handedness
        combat.Handedness handedness = random.nextBoolean() ? combat.Handedness.RIGHT_HANDED : combat.Handedness.LEFT_HANDED;
        
        // Create character using simple constructor
        combat.Character character = new combat.Character(nickname, dexterity, health, coolness, strength, reflexes, handedness);
        
        // Set faction
        character.faction = faction;
        
        // Add basic weapons
        character.weapon = data.WeaponFactory.createWeapon("wpn_colt_peacemaker");
        character.meleeWeapon = combat.MeleeWeaponFactory.createWeapon("mel_sword");
        character.currentWeaponState = character.weapon.getInitialState();
        
        return character;
    }
    
    /**
     * Get faction character information including available character count
     */
    private InputStates.FactionCharacterInfo getFactionCharacterInfo(int factionId) {
        try {
            // Load faction file
            File factionFile = new File("factions/" + factionId + ".json");
            if (!factionFile.exists()) {
                return new InputStates.FactionCharacterInfo("Unknown Faction", 0, 0, new ArrayList<>());
            }
            
            JsonNode rootNode = InputStates.objectMapper.readTree(factionFile);
            JsonNode factionNode = rootNode.get("faction");
            String factionName = factionNode.get("name").asText();
            
            JsonNode charactersNode = rootNode.get("characters");
            List<combat.Character> allCharacters = new ArrayList<>();
            List<combat.Character> availableCharacters = new ArrayList<>();
            
            // Load all characters from faction file using the proper approach
            if (charactersNode != null && charactersNode.isArray()) {
                for (JsonNode charNode : charactersNode) {
                    try {
                        // Add debug logging to see the actual charNode content
                        inputDiagnosticService.recordDebugLog("CHARACTER_LOAD", "DEBUG", "Attempting to deserialize character node");
                        inputDiagnosticService.recordDebugLog("CHARACTER_LOAD", "DEBUG", "charNode content: " + charNode.toString());
                        
                        // Use CharacterData for proper JSON deserialization (same approach as CTRL-C)
                        data.CharacterData characterData = InputStates.objectMapper.treeToValue(charNode, data.CharacterData.class);
                        inputDiagnosticService.recordDebugLog("CHARACTER_LOAD", "DEBUG", "Successfully deserialized to CharacterData: " + characterData.nickname);
                        
                        // Convert CharacterData to Character using CharacterPersistenceManager approach
                        combat.Character character = InputUtils.convertFromCharacterData(characterData);
                        allCharacters.add(character);
                        
                        inputDiagnosticService.recordDebugLog("CHARACTER_LOAD", "DEBUG", "Successfully converted to Character: " + character.getDisplayName());
                        
                        // Check if character is available (not already deployed and not incapacitated)
                        if (isCharacterAvailable(character)) {
                            availableCharacters.add(character);
                            inputDiagnosticService.recordDebugLog("CHARACTER_LOAD", "DEBUG", "Character " + character.getDisplayName() + " is available for deployment");
                        } else {
                            inputDiagnosticService.recordDebugLog("CHARACTER_LOAD", "DEBUG", "Character " + character.getDisplayName() + " is not available (already deployed or incapacitated)");
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading character from faction " + factionId + ": " + e.getMessage());
                        inputDiagnosticService.recordDebugLog("CHARACTER_LOAD", "ERROR", "Full exception details: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                        if (e.getCause() != null) {
                            inputDiagnosticService.recordDebugLog("CHARACTER_LOAD", "ERROR", "Caused by: " + e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage());
                        }
                    }
                }
            }
            
            return new InputStates.FactionCharacterInfo(factionName, allCharacters.size(), availableCharacters.size(), availableCharacters);
            
        } catch (IOException e) {
            System.err.println("Error loading faction file " + factionId + ".json: " + e.getMessage());
            return new InputStates.FactionCharacterInfo("Error Loading Faction", 0, 0, new ArrayList<>());
        }
    }
    
    /**
     * Check if a character is available for deployment
     */
    private boolean isCharacterAvailable(combat.Character character) {
        // Check if character is already deployed on the map
        for (Unit unit : units) {
            if (unit.character != null && unit.character.id == character.id) {
                return false; // Character is already deployed
            }
        }
        
        // Check if character is incapacitated using Character class logic
        return !character.isIncapacitated();
    }
    
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // DevCycle 15e Phase 4: Debug and diagnostic methods moved to DisplayCoordinator
    // ═══════════════════════════════════════════════════════════════════════════════════
    
}

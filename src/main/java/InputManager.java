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

// Utility imports for Phase 3 refactoring (TODO: Re-enable after utility compilation)
// import static InputUtilities.*;
// import static DisplayHelpers.*;
// import static InputConstants.*;

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
    /** List of faction IDs participating in manual victory determination */
    private java.util.List<Integer> scenarioFactions = new java.util.ArrayList<>();
    
    /** Map of faction ID to victory outcome for manual victory processing */
    private java.util.Map<Integer, VictoryOutcome> factionOutcomes = new java.util.HashMap<>();
    
    /** Current faction index being processed in manual victory workflow */
    private int currentVictoryFactionIndex = 0;
    
    // New Scenario Creation State
    /** Name for new scenario being created */
    private String newScenarioName = "";
    
    /** Theme ID for new scenario being created */
    private String newScenarioTheme = "";
    
    // Batch Character Creation Workflow State
    /** Number of characters to create in batch operation */
    private int batchQuantity = 0;
    
    /** Selected archetype index for batch creation */
    private int batchArchetype = 0;
    
    /** Selected faction number for batch creation */
    private int batchFaction = 0;
    
    /** Current step in batch creation workflow */
    private BatchCreationStep batchCreationStep = BatchCreationStep.QUANTITY;
    
    // Individual Character Creation Workflow State
    /** Selected archetype name for individual character creation */
    private String selectedArchetype = "";
    
    /** Selected ranged weapon for individual character creation */
    private String selectedRangedWeapon = "";
    
    /** Selected melee weapon for individual character creation */
    private String selectedMeleeWeapon = "";
    
    // Character weapon selection state moved to InputStateTracker
    
    // Character Deployment Workflow State
    /** Selected faction for character deployment */
    private int deploymentFaction = 0;
    
    /** Number of characters to deploy */
    private int deploymentQuantity = 0;
    
    /** Selected weapon for deployment */
    private String deploymentWeapon = "";
    
    /** Selected formation type for deployment */
    private String deploymentFormation = "";
    
    /** Spacing between deployed characters in pixels (default 5 feet = 35 pixels) */
    private int deploymentSpacing = 35;
    
    /** Current step in deployment workflow */
    private DeploymentStep deploymentStep = DeploymentStep.FACTION;
    
    /** List of characters prepared for deployment */
    private java.util.List<combat.Character> deploymentCharacters = new java.util.ArrayList<>();
    
    // Direct Character Addition Workflow State (CTRL-A functionality)
    /** Selected faction for direct character addition */
    private int directAdditionFaction = 0;
    
    /** Number of characters to add directly */
    private int directAdditionQuantity = 0;
    
    /** Spacing between characters in feet for direct addition */
    private double directAdditionSpacing = 5.0;
    
    /** Current step in direct addition workflow */
    private DirectAdditionStep directAdditionStep = DirectAdditionStep.FACTION;
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // SECTION 2: INNER CLASSES AND ENUMS
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 2.1 Workflow State Enums
    // ─────────────────────────────────────────────────────────────────────────────────
    // State machine enums for managing complex multi-step workflows
    
    /**
     * Steps in the batch character creation workflow.
     * This workflow allows creating multiple characters of the same archetype at once.
     */
    private enum BatchCreationStep {
        /** Prompting user to enter number of characters to create (1-20) */
        QUANTITY,
        
        /** Prompting user to select character archetype from available options */
        ARCHETYPE,
        
        /** Prompting user to select faction assignment for created characters */
        FACTION
    }
    
    /**
     * Steps in the character deployment workflow.
     * This workflow deploys pre-created characters from faction files to the battlefield.
     */
    private enum DeploymentStep {
        /** Prompting user to select faction for character deployment */
        FACTION,
        
        /** Prompting user to specify number of characters to deploy */
        QUANTITY,
        
        /** Prompting user to select weapon configuration for deployed characters */
        WEAPON,
        
        /** Prompting user to select formation type (line, column, etc.) */
        FORMATION,
        
        /** Prompting user to specify spacing between characters */
        SPACING,
        
        /** Waiting for mouse click to place characters on battlefield */
        PLACEMENT
    }
    
    /**
     * Steps in the direct character addition workflow (CTRL-A functionality).
     * This workflow adds existing characters directly from faction files.
     */
    private enum DirectAdditionStep {
        /** Prompting user to select faction for character addition */
        FACTION,
        
        /** Prompting user to specify number of characters to add (1-20) */
        QUANTITY,
        
        /** Prompting user to specify spacing between characters (1-9 feet) */
        SPACING,
        
        /** Waiting for mouse click to place characters in line formation */
        PLACEMENT
    }
    
    /**
     * Victory outcome options for factions in manual victory determination.
     * Used when manually ending scenarios to record faction performance.
     */
    private enum VictoryOutcome {
        /** Faction achieved complete victory in the scenario */
        VICTORY,
        
        /** Faction was defeated or destroyed */
        DEFEAT,
        
        /** Faction participated but neither won nor lost definitively */
        PARTICIPANT
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 2.2 Data Transfer Objects
    // ─────────────────────────────────────────────────────────────────────────────────
    // Helper classes for organizing and transferring complex data
    
    /**
     * Data transfer object holding faction character information for deployment operations.
     * Provides organized access to faction character data including availability counts.
     */
    private static class FactionCharacterInfo {
        /** Display name of the faction */
        String factionName;
        
        /** Total number of characters defined for this faction */
        int totalCharacters;
        
        /** Number of characters available for deployment (not already deployed/incapacitated) */
        int availableCount;
        
        /** List of available characters ready for deployment */
        List<combat.Character> availableCharacters;
        
        /**
         * Constructor for faction character information.
         * 
         * @param factionName Display name of the faction
         * @param totalCharacters Total characters defined for faction
         * @param availableCount Characters available for deployment
         * @param availableCharacters List of deployable characters
         */
        FactionCharacterInfo(String factionName, int totalCharacters, int availableCount, List<combat.Character> availableCharacters) {
            this.factionName = factionName;
            this.totalCharacters = totalCharacters;
            this.availableCount = availableCount;
            this.availableCharacters = availableCharacters;
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 2.3 JSON Deserialization Support
    // ─────────────────────────────────────────────────────────────────────────────────
    // Static configuration for handling faction character file loading
    
    /** Jackson ObjectMapper configured for safe faction character deserialization */
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        // Configure ObjectMapper to handle deserialization issues gracefully
        // These settings prevent crashes when loading faction files with missing or extra fields
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
        
        // Add mixin to ignore problematic runtime fields during deserialization
        objectMapper.addMixIn(combat.Character.class, CharacterMixin.class);
    }
    
    /**
     * Jackson mixin class to control Character deserialization from faction JSON files.
     * This prevents deserialization failures on runtime-only fields that shouldn't be persisted.
     */
    public static abstract class CharacterMixin {
        /** Ignore target zone (runtime-only AWT Rectangle) */
        @com.fasterxml.jackson.annotation.JsonIgnore
        public java.awt.Rectangle targetZone;
        
        /** Ignore current target reference (runtime-only Unit reference) */
        @com.fasterxml.jackson.annotation.JsonIgnore
        public Unit currentTarget;
        
        /** Ignore melee target reference (runtime-only Unit reference) */
        @com.fasterxml.jackson.annotation.JsonIgnore
        public Unit meleeTarget;
        
        /** Ignore weapon state (runtime-only state machine reference) */
        @com.fasterxml.jackson.annotation.JsonIgnore
        public WeaponState currentWeaponState;
        
        /** Ignore paused events (runtime-only event queue) */
        @com.fasterxml.jackson.annotation.JsonIgnore
        public List<ScheduledEvent> pausedEvents;
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 1.5 Additional State Variables
    // ─────────────────────────────────────────────────────────────────────────────────
    // Remaining state variables that support advanced features
    
    // Target Zone Selection State
    /** True when user is selecting a target zone by dragging */
    private boolean isSelectingTargetZone = false;
    
    /** X coordinate where target zone selection started */
    private double targetZoneStartX = 0;
    
    /** Y coordinate where target zone selection started */
    private double targetZoneStartY = 0;
    
    /** Unit for which target zone is being selected */
    private Unit targetZoneUnit = null;
    
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
    
    /** Master debug flag - enables all debug features when true */
    private static boolean DEBUG_ENABLED = false;
    
    /** Enable debug logging for input events (mouse, keyboard) */
    private static boolean DEBUG_INPUT_EVENTS = false;
    
    /** Enable debug logging for state transitions */
    private static boolean DEBUG_STATE_TRANSITIONS = false;
    
    /** Enable performance timing for complex operations */
    private static boolean DEBUG_PERFORMANCE_TIMING = false;
    
    /** Enable input event trace functionality */
    private static boolean DEBUG_INPUT_TRACE = false;
    
    /** Enable memory usage diagnostic output */
    private static boolean DEBUG_MEMORY_USAGE = false;
    
    /** Enable workflow state debugging */
    private static boolean DEBUG_WORKFLOW_STATES = false;
    
    /** Enable combat command debugging */
    private static boolean DEBUG_COMBAT_COMMANDS = false;
    
    /** Enable selection debugging */
    private static boolean DEBUG_SELECTION_OPERATIONS = false;
    
    // Performance timing storage for debug operations
    private static long lastOperationStartTime = 0;
    private static final java.util.Map<String, Long> performanceTimings = new java.util.HashMap<>();
    private static final java.util.List<String> inputEventTrace = new java.util.ArrayList<>();
    private static final int MAX_TRACE_EVENTS = 100; // Limit trace size to prevent memory issues
    
    /**
     * Callback interface for operations that require access to main game functionality.
     * 
     * InputManager uses this interface to delegate operations that require access to
     * methods or state not directly available within the InputManager scope. This design
     * maintains separation of concerns while enabling InputManager to coordinate complex
     * operations that span multiple game subsystems.
     * 
     * The interface is organized into functional groups for different types of operations.
     */
    public interface InputManagerCallbacks {
        
        // ─────────────────────────────────────────────────────────────────────────────────
        // Save/Load Operations
        // ─────────────────────────────────────────────────────────────────────────────────
        
        /** Initiate save operation to specified slot number (1-9) */
        void saveGameToSlot(int slot);
        
        /** Initiate load operation from specified slot number (1-9) */
        void loadGameFromSlot(int slot);
        
        /** Display save slot selection prompt to user */
        void promptForSaveSlot();
        
        /** Display load slot selection prompt to user */
        void promptForLoadSlot();
        
        // ─────────────────────────────────────────────────────────────────────────────────
        // Character/Weapon/Faction Management
        // ─────────────────────────────────────────────────────────────────────────────────
        
        /** Display character creation archetype selection prompt */
        void promptForCharacterCreation();
        
        /** Display weapon assignment selection prompt */
        void promptForWeaponSelection();
        
        /** Display faction assignment selection prompt */
        void promptForFactionSelection();
        
        /** Create new character from selected archetype index */
        void createCharacterFromArchetype(int archetypeIndex);
        
        /** Assign weapon by index to currently selected units */
        void assignWeaponToSelectedUnits(int weaponIndex);
        
        /** Assign faction number to currently selected units */
        void assignFactionToSelectedUnits(int factionNumber);
        
        // ─────────────────────────────────────────────────────────────────────────────────
        // Game State Access and Mutation
        // ─────────────────────────────────────────────────────────────────────────────────
        
        /** Get current game pause state */
        boolean isPaused();
        
        /** Set game pause state */
        void setPaused(boolean paused);
        
        /** Get current edit mode state */
        boolean isEditMode();
        
        /** Set edit mode state */
        void setEditMode(boolean editMode);
        
        /** Get next available unit ID for unit creation */
        int getNextUnitId();
        
        /** Set next unit ID (used during save/load operations) */
        void setNextUnitId(int nextUnitId);
        
        // ─────────────────────────────────────────────────────────────────────────────────
        // Utility and Conversion Methods
        // ─────────────────────────────────────────────────────────────────────────────────
        
        /** Convert pixel coordinates to feet using game's conversion factor */
        double convertPixelsToFeet(double pixels);
        
        /** Convert character stat value (1-100) to game modifier (-20 to +20) */
        int convertStatToModifier(int stat);
        
        // ─────────────────────────────────────────────────────────────────────────────────
        // UI and Scenario Management
        // ─────────────────────────────────────────────────────────────────────────────────
        
        /** Set main window title to reflect current scenario or game state */
        void setWindowTitle(String title);
        
        /** Get array of available theme IDs for scenario creation */
        String[] getAvailableThemes();
        
        /** Set current active theme for new scenarios */
        void setCurrentTheme(String themeId);
    }
    
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
        
        // Set up debug callback for state tracking integration
        this.stateTracker.setDebugCallback((stateName, oldValue, newValue) -> {
            debugStateTransition("INPUT_STATE", oldValue ? stateName : "NONE", 
                                newValue ? stateName : "NONE");
        });
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
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);
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
        scene.setOnKeyPressed(this::handleKeyPressed);
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
        startPerformanceTimer("MousePressed");
        double x = gameRenderer.screenToWorldX(e.getX());
        double y = gameRenderer.screenToWorldY(e.getY());
        
        debugInputEvent("MOUSE_PRESS", e.getButton() + " at screen(" + e.getX() + "," + e.getY() + 
                       ") world(" + String.format("%.1f", x) + "," + String.format("%.1f", y) + ")");
        addInputTraceEvent("Mouse pressed: " + e.getButton() + " at (" + String.format("%.1f", x) + "," + String.format("%.1f", y) + ")");
        
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
                    debugWorkflowState("DIRECT_ADDITION", "PLACEMENT", "Placing character at (" + 
                                     String.format("%.1f", x) + "," + String.format("%.1f", y) + ")");
                    // DevCycle 15d: Delegate to EditModeManager
                    editModeManager.handleCharacterPlacement(x, y);
                    endPerformanceTimer("MousePressed");
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
                debugSelectionOperation("SELECT_UNIT", clickedUnit.character.getDisplayName() + " at (" + 
                                      String.format("%.1f", x) + "," + String.format("%.1f", y) + ")");
                selectionManager.selectUnit(clickedUnit);
                displayEnhancedCharacterStats(clickedUnit);
            } else {
                // Start rectangle selection
                debugSelectionOperation("START_RECTANGLE", "Starting at (" + 
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
                // Shift+right click on empty space with single character selected - start target zone selection
                isSelectingTargetZone = true;
                targetZoneStartX = x;
                targetZoneStartY = y;
                targetZoneUnit = selectionManager.getSelected();
            } else {
                handleRightClick(clickedUnit, x, y, e.isShiftDown());
            }
        }
        
        endPerformanceTimer("MousePressed");
        logMemoryUsage("After MousePressed");
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
            debugSelectionOperation("UPDATE_RECTANGLE", "Dragging to (" + 
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
        debugInputEvent("MOUSE_RELEASE", e.getButton() + " at screen(" + e.getX() + "," + e.getY() + ")");
        addInputTraceEvent("Mouse released: " + e.getButton());
        
        if (selectionManager.isSelecting() && e.getButton() == MouseButton.PRIMARY) {
            // Complete rectangle selection
            debugSelectionOperation("COMPLETE_RECTANGLE", "Finishing rectangle selection");
            selectionManager.completeRectangleSelection(units);
            
            if (selectionManager.hasSelection()) {
                debugSelectionOperation("MULTI_SELECT_COMPLETE", selectionManager.getSelectionCount() + " units selected");
                displayMultiCharacterSelection();
            }
        } else if (isSelectingTargetZone && e.getButton() == MouseButton.SECONDARY && e.isShiftDown()) {
            // Complete target zone selection (Shift+right click)
            double x = gameRenderer.screenToWorldX(e.getX());
            double y = gameRenderer.screenToWorldY(e.getY());
            
            debugSelectionOperation("COMPLETE_TARGET_ZONE", "Target zone at (" + 
                                   String.format("%.1f", x) + "," + String.format("%.1f", y) + ")");
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
                            double maxRange = (selected.character.weapon instanceof RangedWeapon) ? ((RangedWeapon)selected.character.weapon).getMaximumRange() : 0.0;
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
                        // Debug the attack decision logic - ALWAYS PRINT for diagnosis
                        System.out.println("[ATTACK-DECISION] " + unit.character.getDisplayName() + " attack decision:");
                        System.out.println("[ATTACK-DECISION] isMeleeCombatMode: " + unit.character.isMeleeCombatMode());
                        System.out.println("[ATTACK-DECISION] meleeWeapon: " + (unit.character.meleeWeapon != null ? unit.character.meleeWeapon.getName() : "null"));
                        System.out.println("[ATTACK-DECISION] rangedWeapon: " + (unit.character.rangedWeapon != null ? unit.character.rangedWeapon.getName() : "null"));
                        
                        if (unit.character.meleeWeapon != null) {
                            System.out.println("[ATTACK-DECISION] meleeWeapon reach: " + String.format("%.2f", unit.character.meleeWeapon.getTotalReach()) + " feet");
                        }
                        
                        // Check if unit is in melee combat mode
                        if (unit.character.isMeleeCombatMode() && unit.character.meleeWeapon != null) {
                            // Handle melee attack
                            debugPrint("[ATTACK-DECISION] Routing to MELEE attack");
                            startMeleeAttackSequence(unit, clickedUnit);
                        } else {
                            // Handle ranged attack (existing logic)
                            debugPrint("[ATTACK-DECISION] Routing to RANGED attack (melee mode: " + unit.character.isMeleeCombatMode() + ", melee weapon: " + (unit.character.meleeWeapon != null) + ")");
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
                System.out.println("MOVE " + selectionManager.getSelectionCount() + " units to (" + String.format("%.0f", x) + ", " + String.format("%.0f", y) + ")");
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
        startPerformanceTimer("KeyPressed");
        String modifiers = (e.isShiftDown() ? "Shift+" : "") + (e.isControlDown() ? "Ctrl+" : "") + (e.isAltDown() ? "Alt+" : "");
        debugInputEvent("KEY_PRESS", modifiers + e.getCode());
        addInputTraceEvent("Key pressed: " + modifiers + e.getCode());
        
        // Camera controls
        if (e.getCode() == KeyCode.UP) {
            debugInputEvent("CAMERA_CONTROL", "Pan up");
            gameRenderer.adjustOffset(0, 20);
        }
        if (e.getCode() == KeyCode.DOWN) {
            debugInputEvent("CAMERA_CONTROL", "Pan down");
            gameRenderer.adjustOffset(0, -20);
        }
        if (e.getCode() == KeyCode.LEFT) {
            debugInputEvent("CAMERA_CONTROL", "Pan left");
            gameRenderer.adjustOffset(20, 0);
        }
        if (e.getCode() == KeyCode.RIGHT) {
            debugInputEvent("CAMERA_CONTROL", "Pan right");
            gameRenderer.adjustOffset(-20, 0);
        }
        if (e.getCode() == KeyCode.EQUALS || e.getCode() == KeyCode.PLUS) {
            debugInputEvent("CAMERA_CONTROL", "Zoom in");
            gameRenderer.adjustZoom(1.1);
        }
        if (e.getCode() == KeyCode.MINUS) {
            debugInputEvent("CAMERA_CONTROL", "Zoom out");
            gameRenderer.adjustZoom(1.0 / 1.1);
        }
        
        // Game controls
        if (e.getCode() == KeyCode.SPACE) {
            boolean newPauseState = !callbacks.isPaused();
            debugStateTransition("GAME_STATE", callbacks.isPaused() ? "PAUSED" : "RUNNING", 
                                newPauseState ? "PAUSED" : "RUNNING");
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
        
        // InputManager debug hotkeys
        if (e.getCode() == KeyCode.F1 && e.isControlDown()) {
            // Ctrl+F1: Toggle InputManager debug logging
            setDebugEnabled(!isDebugEnabled());
            System.out.println("*** InputManager Debug " + (isDebugEnabled() ? "ENABLED" : "DISABLED") + " ***");
        }
        
        if (e.getCode() == KeyCode.F2 && e.isControlDown()) {
            // Ctrl+F2: Configure debug categories
            configureDebugFeatures(true, true, true, false, false, true, true, true);
            System.out.println("*** InputManager Debug Categories Configured ***");
        }
        
        if (e.getCode() == KeyCode.F3 && e.isControlDown()) {
            // Ctrl+F3: System state dump
            if (isDebugEnabled()) {
                System.out.println(generateSystemStateDump());
            } else {
                System.out.println("*** Debug mode must be enabled for system state dump ***");
            }
        }
        
        if (e.getCode() == KeyCode.F4 && e.isControlDown()) {
            // Ctrl+F4: Performance statistics
            if (isDebugEnabled()) {
                java.util.Map<String, Long> stats = getPerformanceStatistics();
                if (stats.isEmpty()) {
                    System.out.println("*** No performance statistics available ***");
                } else {
                    System.out.println("*** Performance Statistics ***");
                    for (java.util.Map.Entry<String, Long> entry : stats.entrySet()) {
                        double ms = entry.getValue() / 1_000_000.0;
                        System.out.println("  " + entry.getKey() + ": " + String.format("%.3f", ms) + "ms");
                    }
                }
            }
        }
        
        if (e.getCode() == KeyCode.F5 && e.isControlDown()) {
            // Ctrl+F5: Input trace
            if (isDebugEnabled()) {
                java.util.List<String> trace = getInputEventTrace();
                if (trace.isEmpty()) {
                    System.out.println("*** No input trace available ***");
                } else {
                    System.out.println("*** Recent Input Events ***");
                    for (String event : trace) {
                        System.out.println("  " + event);
                    }
                }
            }
        }
        
        if (e.getCode() == KeyCode.F6 && e.isControlDown()) {
            // Ctrl+F6: System integrity validation
            System.out.println("*** Running System Integrity Validation ***");
            validateSystemIntegrity();
        }
        
        if (e.getCode() == KeyCode.F7 && e.isControlDown()) {
            // Ctrl+F7: Clear debug data
            if (isDebugEnabled()) {
                clearPerformanceStatistics();
                clearInputEventTrace();
                System.out.println("*** Debug data cleared ***");
            }
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
        
        endPerformanceTimer("KeyPressed");
        logMemoryUsage("After KeyPressed");
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
                
                System.out.println("--- WEAPONS ---");
                
                // Display ranged weapon
                if (selected.character.rangedWeapon != null) {
                    RangedWeapon ranged = selected.character.rangedWeapon;
                    String activeMarker = !selected.character.isMeleeCombatMode ? " [ACTIVE]" : "";
                    System.out.println("Ranged: " + ranged.getName() + " (" + ranged.getDamage() + " damage, " + 
                                     ranged.getWeaponAccuracy() + " accuracy)" + activeMarker);
                } else {
                    String activeMarker = !selected.character.isMeleeCombatMode ? " [ACTIVE]" : "";
                    System.out.println("Ranged: No ranged weapon" + activeMarker);
                }
                
                // Display melee weapon
                if (selected.character.meleeWeapon != null) {
                    MeleeWeapon melee = selected.character.meleeWeapon;
                    String activeMarker = selected.character.isMeleeCombatMode ? " [ACTIVE]" : "";
                    System.out.println("Melee: " + melee.getName() + " (" + melee.getDamage() + " damage, " + 
                                     melee.getWeaponAccuracy() + " accuracy, " + String.format("%.1f", melee.getTotalReach()) + "ft reach)" + activeMarker);
                } else {
                    String activeMarker = selected.character.isMeleeCombatMode ? " [ACTIVE]" : "";
                    System.out.println("Melee: No melee weapon" + activeMarker);
                }
                
                // Show current weapon state and additional details for active weapon
                System.out.println("Current State: " + (selected.character.currentWeaponState != null ? selected.character.currentWeaponState.getState() : "None"));
                
                // Show additional details for the active weapon
                if (!selected.character.isMeleeCombatMode && selected.character.rangedWeapon != null) {
                    RangedWeapon ranged = selected.character.rangedWeapon;
                    System.out.println("Active Details: Range " + ranged.getMaximumRange() + "ft, Velocity " + ranged.getVelocityFeetPerSecond() + "ft/s, Ammo " + ranged.getAmmunition() + "/" + ranged.getMaxAmmunition());
                } else if (selected.character.isMeleeCombatMode && selected.character.meleeWeapon != null) {
                    MeleeWeapon melee = selected.character.meleeWeapon;
                    System.out.println("Active Details: " + melee.getWeaponType().getDisplayName() + " weapon");
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
                
                // Separate combat statistics (DevCycle 12)
                System.out.println("Ranged Combat: " + selected.character.rangedAttacksAttempted + " attempted, " + 
                                 selected.character.rangedAttacksSuccessful + " successful, " + 
                                 selected.character.rangedWoundsInflicted + " wounds inflicted");
                System.out.println("Melee Combat: " + selected.character.meleeAttacksAttempted + " attempted, " + 
                                 selected.character.meleeAttacksSuccessful + " successful, " + 
                                 selected.character.meleeWoundsInflicted + " wounds inflicted");
                
                // Legacy combined statistics
                System.out.println("Total Attacks: " + selected.character.getAttacksAttempted() + " attempted, " + 
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
        
        // DevCycle 15e: Handle scenario name input
        if (stateTracker.isWaitingForScenarioName()) {
            // DevCycle 15e: Delegate to GameStateManager
            gameStateManager.handleScenarioNameTextInput(e);
            return; // Don't process other input while waiting for scenario name
        }
        
        // DevCycle 15e: Handle theme selection
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
                // DevCycle 15e: Delegate to GameStateManager
                gameStateManager.handleThemeSelectionInput(themeNumber);
            }
            return; // Don't process other input while waiting for theme selection
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
                    batchQuantity = 0;
                    batchArchetype = 0;
                    batchFaction = 0;
                    batchCreationStep = BatchCreationStep.QUANTITY;
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
                    } else if (slotNumber >= 1 && slotNumber <= 9) {
                        // DevCycle 15d: Delegate to EditModeManager
                        editModeManager.handleCharacterArchetypeSelection(slotNumber);
                    } else {
                        System.out.println("*** Invalid archetype selection. Use 1-9 or 0 to cancel ***");
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
                    } else {
                        System.out.println("*** Invalid weapon type selection. Use 1 for Ranged, 2 for Melee, or 0 to cancel ***");
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
                    // DevCycle 15d: Delegate to EditModeManager
                    editModeManager.handleBatchCharacterCreationInput(slotNumber);
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
    
    public void setWaitingForScenarioName(boolean waiting) {
        // DevCycle 15e: Delegate to GameStateManager
        gameStateManager.setWaitingForScenarioName(waiting);
    }
    
    public void setWaitingForThemeSelection(boolean waiting) {
        // DevCycle 15e: Delegate to GameStateManager
        gameStateManager.setWaitingForThemeSelection(waiting);
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
     * Start the batch character creation workflow
     */
    private void promptForBatchCharacterCreation() {
        stateTracker.setWaitingForBatchCharacterCreation(true);
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
                    stateTracker.setWaitingForBatchCharacterCreation(false);
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
                    stateTracker.setWaitingForBatchCharacterCreation(false);
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
                    stateTracker.setWaitingForBatchCharacterCreation(false);
                    batchQuantity = 0;
                    batchArchetype = 0;
                    batchFaction = 0;
                    batchCreationStep = BatchCreationStep.QUANTITY;
                } else if (inputNumber >= 1 && inputNumber <= 9) {
                    batchFaction = inputNumber;
                    createBatchCharacters();
                    // Reset state after creation
                    stateTracker.setWaitingForBatchCharacterCreation(false);
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
        stateTracker.setWaitingForCharacterDeployment(true);
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
        stateTracker.setWaitingForCharacterDeployment(false);
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
        return stateTracker.isWaitingForCharacterDeployment() && deploymentStep == DeploymentStep.PLACEMENT;
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
        
        stateTracker.setWaitingForVictoryOutcome(true);
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
        stateTracker.setWaitingForVictoryOutcome(false);
        
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
        stateTracker.setWaitingForVictoryOutcome(false);
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
        
        stateTracker.setWaitingForScenarioName(true);
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
        
        stateTracker.setWaitingForScenarioName(false);
        promptForThemeSelection();
    }
    
    /**
     * Prompt for theme selection
     */
    private void promptForThemeSelection() {
        stateTracker.setWaitingForThemeSelection(true);
        
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
        stateTracker.setWaitingForThemeSelection(false);
        
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
        stateTracker.setWaitingForScenarioName(false);
        stateTracker.setWaitingForThemeSelection(false);
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
        System.out.println("[MELEE-TRIGGER] " + attacker.character.getDisplayName() + " attempting to attack " + target.character.getDisplayName());
        
        MeleeWeapon meleeWeapon = attacker.character.meleeWeapon;
        if (meleeWeapon == null) {
            System.out.println("[MELEE-TRIGGER] Attack FAILED - no melee weapon equipped");
            System.out.println("*** " + attacker.character.getDisplayName() + " has no melee weapon equipped");
            return;
        }
        
        System.out.println("[MELEE-TRIGGER] Weapon: " + meleeWeapon.getName() + " (reach: " + String.format("%.2f", meleeWeapon.getTotalReach()) + " feet)");
        System.out.println("[MELEE-TRIGGER] Weapon state: " + attacker.character.currentWeaponState);
        
        // Check if target is within melee range
        CombatResolver combatResolver = new CombatResolver(units, eventQueue, true); // Force debug mode
        double distance = Math.hypot(target.x - attacker.x, target.y - attacker.y);
        double distanceFeet = distance / 7.0; // Convert pixels to feet
        double maxReach = meleeWeapon.getTotalReach();
        boolean inRange = combatResolver.isInMeleeRange(attacker, target, meleeWeapon);
        
        System.out.println("[MELEE-TRIGGER] Range check: " + String.format("%.2f", distanceFeet) + " feet (need " + String.format("%.2f", maxReach) + " feet)");
        System.out.println("[MELEE-TRIGGER] In range result: " + inRange);
        
        if (!inRange) {
            System.out.println("[MELEE-TRIGGER] Attack FAILED - target out of range, initiating movement");
            System.out.println("*** " + attacker.character.getDisplayName() + " cannot reach " + target.character.getDisplayName());
            System.out.println("*** Target distance: " + String.format("%.2f", distanceFeet) + " feet, weapon reach: " + String.format("%.2f", maxReach) + " feet");
            
            // Initiate automatic movement toward target
            initiateMovementToMeleeTarget(attacker, target, meleeWeapon);
            return;
        }
        
        // Target is in range - proceed with attack
        System.out.println("[MELEE-TRIGGER] Target in range - proceeding with attack sequence");
        System.out.println("[MELEE-TRIGGER] Current tick: " + gameClock.getCurrentTick());
        
        // Schedule melee attack based on weapon state
        System.out.println("[MELEE-TRIGGER] Calling startMeleeAttackSequence on character");
        attacker.character.startMeleeAttackSequence(attacker, target, gameClock.getCurrentTick(), eventQueue, attacker.getId(), (GameCallbacks) callbacks);
        
        System.out.println("[MELEE-TRIGGER] Attack sequence call completed");
        System.out.println("*** " + attacker.character.getDisplayName() + " begins melee attack on " + target.character.getDisplayName() + " with " + meleeWeapon.getName());
    }
    
    /**
     * Debug print helper that only outputs when in debug mode
     */
    private void debugPrint(String message) {
        if (GameRenderer.isDebugMode()) {
            System.out.println(message);
        }
    }
    
    /**
     * Initiate automatic movement toward a melee target that is out of range
     */
    private void initiateMovementToMeleeTarget(Unit attacker, Unit target, MeleeWeapon meleeWeapon) {
        // Calculate optimal approach position within melee range
        double weaponReach = meleeWeapon.getTotalReach();
        double approachDistance = weaponReach - 0.5; // Leave 0.5 feet buffer to ensure we're in range
        
        // Calculate direction from target to attacker (we want to approach from current position)
        double dx = attacker.x - target.x;
        double dy = attacker.y - target.y;
        double currentDistance = Math.hypot(dx, dy);
        
        // Normalize direction vector
        if (currentDistance > 0) {
            dx = dx / currentDistance;
            dy = dy / currentDistance;
        }
        
        // Calculate approach position (move toward target, stopping at weapon range)
        double approachPixelDistance = approachDistance * 7.0; // Convert feet to pixels
        double approachX = target.x + (dx * approachPixelDistance);
        double approachY = target.y + (dy * approachPixelDistance);
        
        // Set melee movement state
        attacker.character.isMovingToMelee = true;
        attacker.character.meleeTarget = target;
        
        // Set melee weapon to ready state for combat (using unified weapon system)
        if (attacker.character.meleeWeapon != null) {
            attacker.character.startReadyWeaponSequence(attacker, gameClock.getCurrentTick(), eventQueue, attacker.getId());
            System.out.println("*** " + attacker.character.getDisplayName() + " readying melee weapon " + attacker.character.meleeWeapon.getName() + " for combat");
        }
        
        // Initiate movement using existing movement system
        attacker.setTarget(approachX, approachY);
        
        // Debug output
        double targetDistanceFeet = currentDistance / 7.0;
        System.out.println("*** " + attacker.character.getDisplayName() + " moving to melee range of " + target.character.getDisplayName());
        System.out.println("*** Approach distance: " + String.format("%.2f", approachDistance) + " feet, current distance: " + String.format("%.2f", targetDistanceFeet) + " feet");
        System.out.println("*** Target position: (" + String.format("%.0f", approachX) + ", " + String.format("%.0f", approachY) + ")");
    }
    
    /**
     * Reset character creation state variables
     */
    private void resetCharacterCreationState() {
        selectedArchetype = "";
        selectedRangedWeapon = "";
        selectedMeleeWeapon = "";
        stateTracker.setWaitingForCharacterCreation(false);
        stateTracker.setWaitingForCharacterRangedWeapon(false);
        stateTracker.setWaitingForCharacterMeleeWeapon(false);
    }
    
    /**
     * Handle archetype selection for character creation
     */
    private void handleCharacterArchetypeSelection(int archetypeIndex) {
        String[] archetypes = {"gunslinger", "soldier", "weighted_random", "scout", "marksman", "brawler", "confederate_soldier", "union_soldier", "balanced"};
        
        if (archetypeIndex >= 1 && archetypeIndex <= archetypes.length) {
            selectedArchetype = archetypes[archetypeIndex - 1];
            
            // Move to ranged weapon selection
            stateTracker.setWaitingForCharacterCreation(false);
            stateTracker.setWaitingForCharacterRangedWeapon(true);
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
            stateTracker.setWaitingForCharacterRangedWeapon(false);
            stateTracker.setWaitingForCharacterMeleeWeapon(true);
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
    
    /**
     * Start the direct character addition workflow
     */
    private void promptForDirectCharacterAddition() {
        stateTracker.setWaitingForDirectCharacterAddition(true);
        directAdditionStep = DirectAdditionStep.FACTION;
        directAdditionFaction = 0;
        directAdditionQuantity = 0;
        directAdditionSpacing = 5.0; // Default 5 feet
        
        System.out.println("***********************");
        System.out.println("*** DIRECT CHARACTER ADDITION ***");
        System.out.println("Select faction:");
        
        // Display factions with available character counts
        for (int i = 1; i <= 3; i++) {
            FactionCharacterInfo info = getFactionCharacterInfo(i);
            System.out.println(i + ". " + info.factionName + " (" + info.availableCount + " available characters)");
        }
        
        System.out.println("0. Cancel addition");
        System.out.println();
        System.out.println("Enter faction (1-3, 0 to cancel): ");
    }
    
    /**
     * Handle user input for direct character addition workflow
     */
    private void handleDirectCharacterAdditionInput(int inputNumber) {
        switch (directAdditionStep) {
            case FACTION:
                if (inputNumber == 0) {
                    System.out.println("*** Character addition cancelled ***");
                    cancelDirectCharacterAddition();
                } else if (inputNumber >= 1 && inputNumber <= 3) {
                    FactionCharacterInfo info = getFactionCharacterInfo(inputNumber);
                    if (info.availableCount == 0) {
                        System.out.println("*** No available characters in " + info.factionName + " ***");
                        System.out.println("*** Please select a different faction or press 0 to cancel ***");
                    } else {
                        directAdditionFaction = inputNumber;
                        directAdditionStep = DirectAdditionStep.QUANTITY;
                        System.out.println("***********************");
                        System.out.println("*** CHARACTER QUANTITY ***");
                        System.out.println("Selected faction: " + info.factionName);
                        System.out.println("Available characters: " + info.availableCount);
                        System.out.println("How many characters to add?");
                        System.out.println("Enter quantity (1-" + Math.min(20, info.availableCount) + ", 0 to cancel): ");
                    }
                } else {
                    System.out.println("*** Invalid faction. Use 1-3 or 0 to cancel ***");
                }
                break;
                
            case QUANTITY:
                if (inputNumber == 0) {
                    System.out.println("*** Character addition cancelled ***");
                    cancelDirectCharacterAddition();
                } else {
                    FactionCharacterInfo info = getFactionCharacterInfo(directAdditionFaction);
                    int maxAllowed = Math.min(20, info.availableCount);
                    
                    if (inputNumber >= 1 && inputNumber <= maxAllowed) {
                        directAdditionQuantity = inputNumber;
                        directAdditionStep = DirectAdditionStep.SPACING;
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
                    } else {
                        System.out.println("*** Invalid quantity. Use 1-" + maxAllowed + " or 0 to cancel ***");
                        System.out.println("*** Available characters: " + info.availableCount + " ***");
                    }
                }
                break;
                
            case SPACING:
                if (inputNumber == 0) {
                    System.out.println("*** Character addition cancelled ***");
                    cancelDirectCharacterAddition();
                } else if (inputNumber >= 1 && inputNumber <= 9) {
                    directAdditionSpacing = inputNumber; // feet
                    directAdditionStep = DirectAdditionStep.PLACEMENT;
                    System.out.println("***********************");
                    System.out.println("*** CHARACTER PLACEMENT ***");
                    System.out.println("Click on the map to place " + directAdditionQuantity + " characters");
                    System.out.println("Faction: " + directAdditionFaction + " | Spacing: " + directAdditionSpacing + " feet");
                    System.out.println("Characters will be placed in a line going right from your click point");
                    System.out.println("Press ESC to cancel");
                } else {
                    System.out.println("*** Invalid spacing. Use 1-9 feet or 0 to cancel ***");
                }
                break;
                
            case PLACEMENT:
                // Placement is handled by mouse clicks, not number input
                break;
        }
    }
    
    /**
     * Cancel the direct character addition workflow and reset state
     */
    private void cancelDirectCharacterAddition() {
        stateTracker.setWaitingForDirectCharacterAddition(false);
        directAdditionStep = DirectAdditionStep.FACTION;
        directAdditionFaction = 0;
        directAdditionQuantity = 0;
        directAdditionSpacing = 5.0;
    }
    
    /**
     * Handle character placement at the clicked location
     */
    private void handleCharacterPlacement(double x, double y) {
        System.out.println("***********************");
        System.out.println("*** PLACING CHARACTERS ***");
        System.out.println("Deploying " + directAdditionQuantity + " characters from faction " + directAdditionFaction);
        
        // Get available characters from faction
        FactionCharacterInfo info = getFactionCharacterInfo(directAdditionFaction);
        if (info.availableCharacters.size() < directAdditionQuantity) {
            System.out.println("*** Error: Not enough available characters ***");
            System.out.println("*** Available: " + info.availableCharacters.size() + ", Requested: " + directAdditionQuantity + " ***");
            cancelDirectCharacterAddition();
            return;
        }
        
        // Convert spacing from feet to pixels (7 pixels = 1 foot)
        double spacingPixels = directAdditionSpacing * 7.0;
        
        // Deploy characters in a horizontal line going right
        for (int i = 0; i < directAdditionQuantity; i++) {
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
            javafx.scene.paint.Color factionColor = getFactionColor(directAdditionFaction);
            
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
    private FactionCharacterInfo getFactionCharacterInfo(int factionId) {
        try {
            // Load faction file
            File factionFile = new File("factions/" + factionId + ".json");
            if (!factionFile.exists()) {
                return new FactionCharacterInfo("Unknown Faction", 0, 0, new ArrayList<>());
            }
            
            JsonNode rootNode = objectMapper.readTree(factionFile);
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
                        debugPrint("[DEBUG] Attempting to deserialize character node:");
                        debugPrint("[DEBUG] charNode content: " + charNode.toString());
                        
                        // Use CharacterData for proper JSON deserialization (same approach as CTRL-C)
                        data.CharacterData characterData = objectMapper.treeToValue(charNode, data.CharacterData.class);
                        debugPrint("[DEBUG] Successfully deserialized to CharacterData: " + characterData.nickname);
                        
                        // Convert CharacterData to Character using CharacterPersistenceManager approach
                        combat.Character character = convertFromCharacterData(characterData);
                        allCharacters.add(character);
                        
                        debugPrint("[DEBUG] Successfully converted to Character: " + character.getDisplayName());
                        
                        // Check if character is available (not already deployed and not incapacitated)
                        if (isCharacterAvailable(character)) {
                            availableCharacters.add(character);
                            debugPrint("[DEBUG] Character " + character.getDisplayName() + " is available for deployment");
                        } else {
                            debugPrint("[DEBUG] Character " + character.getDisplayName() + " is not available (already deployed or incapacitated)");
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading character from faction " + factionId + ": " + e.getMessage());
                        debugPrint("[DEBUG] Full exception details: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                        if (e.getCause() != null) {
                            debugPrint("[DEBUG] Caused by: " + e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage());
                        }
                    }
                }
            }
            
            return new FactionCharacterInfo(factionName, allCharacters.size(), availableCharacters.size(), availableCharacters);
            
        } catch (IOException e) {
            System.err.println("Error loading faction file " + factionId + ".json: " + e.getMessage());
            return new FactionCharacterInfo("Error Loading Faction", 0, 0, new ArrayList<>());
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
    
    /**
     * Convert CharacterData to Character object (same approach as CharacterPersistenceManager)
     */
    private combat.Character convertFromCharacterData(data.CharacterData data) {
        // Create character with basic info - use the full constructor
        combat.Character character = new combat.Character(data.id, data.nickname, data.firstName, data.lastName, 
                                          data.birthdate, data.themeId, data.dexterity, data.health, 
                                          data.coolness, data.strength, data.reflexes, data.handedness);
        
        // Set remaining stats not covered by constructor
        character.currentDexterity = data.currentDexterity;
        character.currentHealth = data.currentHealth;
        character.baseMovementSpeed = data.baseMovementSpeed;
        
        // Set current state
        character.currentMovementType = data.currentMovementType;
        character.currentAimingSpeed = data.currentAimingSpeed;
        character.usesAutomaticTargeting = data.usesAutomaticTargeting;
        character.preferredFiringMode = data.preferredFiringMode;
        
        // Set faction
        character.setFaction(data.faction);
        
        // Set battle statistics
        character.combatEngagements = data.combatEngagements;
        character.woundsReceived = data.woundsReceived;
        character.woundsInflictedScratch = data.woundsInflictedScratch;
        character.woundsInflictedLight = data.woundsInflictedLight;
        character.woundsInflictedSerious = data.woundsInflictedSerious;
        character.woundsInflictedCritical = data.woundsInflictedCritical;
        character.attacksAttempted = data.attacksAttempted;
        character.attacksSuccessful = data.attacksSuccessful;
        character.targetsIncapacitated = data.targetsIncapacitated;
        character.headshotsAttempted = data.headshotsAttempted;
        character.headshotsSuccessful = data.headshotsSuccessful;
        character.headshotsKills = data.headshotsKills;
        character.battlesParticipated = data.battlesParticipated;
        character.victories = data.victories;
        character.defeats = data.defeats;
        
        // Set skills
        if (data.skills != null) {
            for (data.CharacterData.SkillData skillData : data.skills) {
                character.setSkillLevel(skillData.skillName, skillData.level);
            }
        }
        
        // Set wounds
        if (data.wounds != null) {
            for (data.CharacterData.WoundData woundData : data.wounds) {
                try {
                    combat.BodyPart bodyPart = combat.BodyPart.valueOf(woundData.bodyPart);
                    combat.WoundSeverity severity = combat.WoundSeverity.valueOf(woundData.severity);
                    character.addWound(new combat.Wound(bodyPart, severity, "Persistent wound", "", woundData.damage));
                } catch (IllegalArgumentException e) {
                    System.err.println("Warning: Invalid wound data - " + e.getMessage());
                }
            }
        }
        
        return character;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // SECTION 12: DEBUG AND DIAGNOSTIC METHODS
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 12.1 Debug Configuration and Control
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Enable or disable all debug features.
     * 
     * @param enabled true to enable debug features, false to disable
     */
    public static void setDebugEnabled(boolean enabled) {
        DEBUG_ENABLED = enabled;
        if (enabled) {
            debugLog("DEBUG", "Debug mode enabled - all debug features activated");
        }
    }
    
    /**
     * Configure specific debug categories.
     * 
     * @param inputEvents Enable input event debugging
     * @param stateTransitions Enable state transition debugging  
     * @param performance Enable performance timing
     * @param inputTrace Enable input event tracing
     * @param memoryUsage Enable memory usage monitoring
     * @param workflowStates Enable workflow state debugging
     * @param combatCommands Enable combat command debugging
     * @param selectionOps Enable selection operation debugging
     */
    public static void configureDebugFeatures(boolean inputEvents, boolean stateTransitions,
                                            boolean performance, boolean inputTrace,
                                            boolean memoryUsage, boolean workflowStates,
                                            boolean combatCommands, boolean selectionOps) {
        DEBUG_INPUT_EVENTS = inputEvents;
        DEBUG_STATE_TRANSITIONS = stateTransitions;
        DEBUG_PERFORMANCE_TIMING = performance;
        DEBUG_INPUT_TRACE = inputTrace;
        DEBUG_MEMORY_USAGE = memoryUsage;
        DEBUG_WORKFLOW_STATES = workflowStates;
        DEBUG_COMBAT_COMMANDS = combatCommands;
        DEBUG_SELECTION_OPERATIONS = selectionOps;
        
        if (DEBUG_ENABLED) {
            debugLog("DEBUG", "Debug features configured - InputEvents:" + inputEvents + 
                    " StateTransitions:" + stateTransitions + " Performance:" + performance +
                    " InputTrace:" + inputTrace + " Memory:" + memoryUsage +
                    " Workflows:" + workflowStates + " Combat:" + combatCommands +
                    " Selection:" + selectionOps);
        }
    }
    
    /**
     * Check if any debug features are enabled.
     * 
     * @return true if debugging is active
     */
    public static boolean isDebugEnabled() {
        return DEBUG_ENABLED;
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 12.2 Debug Logging Methods
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Core debug logging method with zero performance impact when disabled.
     * 
     * @param category Debug category (INPUT, STATE, PERF, TRACE, MEMORY, WORKFLOW, COMBAT, SELECTION)
     * @param message Debug message to log
     */
    private static void debugLog(String category, String message) {
        if (DEBUG_ENABLED) {
            long timestamp = System.currentTimeMillis();
            System.out.println("[DEBUG-" + category + "] " + timestamp + ": " + message);
        }
    }
    
    /**
     * Log input events when debug is enabled.
     * 
     * @param eventType Type of input event (MOUSE_PRESS, MOUSE_RELEASE, KEY_PRESS, etc.)
     * @param details Additional event details
     */
    private static void debugInputEvent(String eventType, String details) {
        if (DEBUG_ENABLED && DEBUG_INPUT_EVENTS) {
            debugLog("INPUT", eventType + " - " + details);
        }
    }
    
    /**
     * Log state transitions when debug is enabled.
     * 
     * @param stateType Type of state changing (INPUT_STATE, WORKFLOW_STATE, GAME_STATE)
     * @param fromState Previous state
     * @param toState New state
     */
    private static void debugStateTransition(String stateType, String fromState, String toState) {
        if (DEBUG_ENABLED && DEBUG_STATE_TRANSITIONS) {
            debugLog("STATE", stateType + ": " + fromState + " → " + toState);
        }
    }
    
    /**
     * Log workflow state changes when debug is enabled.
     * 
     * @param workflowName Name of the workflow (BATCH_CREATION, DEPLOYMENT, etc.)
     * @param step Current workflow step
     * @param details Additional workflow details
     */
    private static void debugWorkflowState(String workflowName, String step, String details) {
        if (DEBUG_ENABLED && DEBUG_WORKFLOW_STATES) {
            debugLog("WORKFLOW", workflowName + " - Step: " + step + " - " + details);
        }
    }
    
    /**
     * Log combat commands when debug is enabled.
     * 
     * @param commandType Type of combat command (ATTACK, MOVE_TO_MELEE, TARGET_SELECTION, etc.)
     * @param unitInfo Information about the unit executing the command
     * @param targetInfo Information about the target (if applicable)
     */
    private static void debugCombatCommand(String commandType, String unitInfo, String targetInfo) {
        if (DEBUG_ENABLED && DEBUG_COMBAT_COMMANDS) {
            String message = commandType + " - Unit: " + unitInfo;
            if (targetInfo != null && !targetInfo.isEmpty()) {
                message += " - Target: " + targetInfo;
            }
            debugLog("COMBAT", message);
        }
    }
    
    /**
     * Log selection operations when debug is enabled.
     * 
     * @param operation Type of selection operation (SELECT_UNIT, START_RECTANGLE, etc.)
     * @param details Operation details
     */
    private static void debugSelectionOperation(String operation, String details) {
        if (DEBUG_ENABLED && DEBUG_SELECTION_OPERATIONS) {
            debugLog("SELECTION", operation + " - " + details);
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 12.3 Performance Monitoring
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Start timing an operation for performance monitoring.
     * 
     * @param operationName Name of the operation being timed
     */
    private static void startPerformanceTimer(String operationName) {
        if (DEBUG_ENABLED && DEBUG_PERFORMANCE_TIMING) {
            lastOperationStartTime = System.nanoTime();
            debugLog("PERF", "START: " + operationName);
        }
    }
    
    /**
     * End timing an operation and log the duration.
     * 
     * @param operationName Name of the operation that completed
     */
    private static void endPerformanceTimer(String operationName) {
        if (DEBUG_ENABLED && DEBUG_PERFORMANCE_TIMING && lastOperationStartTime > 0) {
            long duration = System.nanoTime() - lastOperationStartTime;
            double durationMs = duration / 1_000_000.0;
            performanceTimings.put(operationName, duration);
            debugLog("PERF", "END: " + operationName + " - Duration: " + String.format("%.3f", durationMs) + "ms");
            lastOperationStartTime = 0;
        }
    }
    
    /**
     * Get performance statistics for all timed operations.
     * 
     * @return Map of operation names to durations in nanoseconds
     */
    public static java.util.Map<String, Long> getPerformanceStatistics() {
        return new java.util.HashMap<>(performanceTimings);
    }
    
    /**
     * Clear all performance timing data.
     */
    public static void clearPerformanceStatistics() {
        performanceTimings.clear();
        debugLog("PERF", "Performance statistics cleared");
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 12.4 Input Event Tracing
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Add an event to the input trace when tracing is enabled.
     * 
     * @param event Description of the input event
     */
    private static void addInputTraceEvent(String event) {
        if (DEBUG_ENABLED && DEBUG_INPUT_TRACE) {
            synchronized (inputEventTrace) {
                // Maintain maximum trace size to prevent memory issues
                if (inputEventTrace.size() >= MAX_TRACE_EVENTS) {
                    inputEventTrace.remove(0);
                }
                inputEventTrace.add(System.currentTimeMillis() + ": " + event);
            }
            debugLog("TRACE", event);
        }
    }
    
    /**
     * Get the current input event trace.
     * 
     * @return List of recent input events
     */
    public static java.util.List<String> getInputEventTrace() {
        synchronized (inputEventTrace) {
            return new java.util.ArrayList<>(inputEventTrace);
        }
    }
    
    /**
     * Clear the input event trace.
     */
    public static void clearInputEventTrace() {
        synchronized (inputEventTrace) {
            inputEventTrace.clear();
        }
        debugLog("TRACE", "Input event trace cleared");
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // 12.5 System State Diagnostics
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Generate a comprehensive system state dump for debugging.
     * 
     * @return String containing current system state information
     */
    public String generateSystemStateDump() {
        StringBuilder dump = new StringBuilder();
        dump.append("=== InputManager System State Dump ===\n");
        dump.append("Timestamp: ").append(new java.util.Date()).append("\n\n");
        
        // Debug configuration
        dump.append("DEBUG CONFIGURATION:\n");
        dump.append("  Master Debug: ").append(DEBUG_ENABLED).append("\n");
        dump.append("  Input Events: ").append(DEBUG_INPUT_EVENTS).append("\n");
        dump.append("  State Transitions: ").append(DEBUG_STATE_TRANSITIONS).append("\n");
        dump.append("  Performance Timing: ").append(DEBUG_PERFORMANCE_TIMING).append("\n");
        dump.append("  Input Trace: ").append(DEBUG_INPUT_TRACE).append("\n");
        dump.append("  Memory Usage: ").append(DEBUG_MEMORY_USAGE).append("\n");
        dump.append("  Workflow States: ").append(DEBUG_WORKFLOW_STATES).append("\n");
        dump.append("  Combat Commands: ").append(DEBUG_COMBAT_COMMANDS).append("\n");
        dump.append("  Selection Operations: ").append(DEBUG_SELECTION_OPERATIONS).append("\n\n");
        
        // Game state
        dump.append("GAME STATE:\n");
        dump.append("  Paused: ").append(callbacks.isPaused()).append("\n");
        dump.append("  Edit Mode: ").append(callbacks.isEditMode()).append("\n");
        dump.append("  Units Count: ").append(units.size()).append("\n");
        dump.append("  Selected Units: ").append(selectionManager.getSelectionCount()).append("\n");
        dump.append("  Current Tick: ").append(gameClock.getCurrentTick()).append("\n");
        dump.append("  Event Queue Size: ").append(eventQueue.size()).append("\n\n");
        
        // Input state flags
        dump.append("INPUT STATE FLAGS:\n");
        dump.append("  Waiting for Save Slot: ").append(stateTracker.isWaitingForSaveSlot()).append("\n");
        dump.append("  Waiting for Load Slot: ").append(stateTracker.isWaitingForLoadSlot()).append("\n");
        dump.append("  Waiting for Character Creation: ").append(stateTracker.isWaitingForCharacterCreation()).append("\n");
        dump.append("  Waiting for Weapon Selection: ").append(stateTracker.isWaitingForWeaponSelection()).append("\n");
        dump.append("  Waiting for Faction Selection: ").append(stateTracker.isWaitingForFactionSelection()).append("\n");
        dump.append("  Waiting for Batch Character Creation: ").append(stateTracker.isWaitingForBatchCharacterCreation()).append("\n");
        dump.append("  Waiting for Character Deployment: ").append(stateTracker.isWaitingForCharacterDeployment()).append("\n");
        dump.append("  Waiting for Deletion Confirmation: ").append(stateTracker.isWaitingForDeletionConfirmation()).append("\n");
        dump.append("  Waiting for Victory Outcome: ").append(stateTracker.isWaitingForVictoryOutcome()).append("\n");
        dump.append("  Waiting for Scenario Name: ").append(stateTracker.isWaitingForScenarioName()).append("\n");
        dump.append("  Waiting for Theme Selection: ").append(stateTracker.isWaitingForThemeSelection()).append("\n");
        dump.append("  Waiting for Direct Character Addition: ").append(stateTracker.isWaitingForDirectCharacterAddition()).append("\n\n");
        
        // Workflow states
        dump.append("WORKFLOW STATES:\n");
        dump.append("  Batch Creation Step: ").append(batchCreationStep).append("\n");
        dump.append("  Deployment Step: ").append(deploymentStep).append("\n");
        dump.append("  Direct Addition Step: ").append(directAdditionStep).append("\n");
        dump.append("  Victory Faction Index: ").append(currentVictoryFactionIndex).append("\n\n");
        
        // Target zone selection
        dump.append("TARGET ZONE SELECTION:\n");
        dump.append("  Is Selecting: ").append(isSelectingTargetZone).append("\n");
        dump.append("  Start X: ").append(targetZoneStartX).append("\n");
        dump.append("  Start Y: ").append(targetZoneStartY).append("\n");
        dump.append("  Target Unit: ").append(targetZoneUnit != null ? targetZoneUnit.character.getDisplayName() : "None").append("\n\n");
        
        // Memory usage (if enabled)
        if (DEBUG_MEMORY_USAGE) {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            dump.append("MEMORY USAGE:\n");
            dump.append("  Total Memory: ").append(totalMemory / (1024 * 1024)).append(" MB\n");
            dump.append("  Used Memory: ").append(usedMemory / (1024 * 1024)).append(" MB\n");
            dump.append("  Free Memory: ").append(freeMemory / (1024 * 1024)).append(" MB\n");
            dump.append("  Max Memory: ").append(runtime.maxMemory() / (1024 * 1024)).append(" MB\n\n");
        }
        
        // Performance statistics
        if (!performanceTimings.isEmpty()) {
            dump.append("PERFORMANCE STATISTICS:\n");
            for (java.util.Map.Entry<String, Long> entry : performanceTimings.entrySet()) {
                double durationMs = entry.getValue() / 1_000_000.0;
                dump.append("  ").append(entry.getKey()).append(": ").append(String.format("%.3f", durationMs)).append("ms\n");
            }
            dump.append("\n");
        }
        
        // Recent input trace
        if (!inputEventTrace.isEmpty()) {
            dump.append("RECENT INPUT EVENTS:\n");
            synchronized (inputEventTrace) {
                int start = Math.max(0, inputEventTrace.size() - 10);
                for (int i = start; i < inputEventTrace.size(); i++) {
                    dump.append("  ").append(inputEventTrace.get(i)).append("\n");
                }
            }
            dump.append("\n");
        }
        
        dump.append("=== End System State Dump ===");
        return dump.toString();
    }
    
    /**
     * Log memory usage statistics when memory debugging is enabled.
     */
    private static void logMemoryUsage(String context) {
        if (DEBUG_ENABLED && DEBUG_MEMORY_USAGE) {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            debugLog("MEMORY", context + " - Used: " + (usedMemory / (1024 * 1024)) + "MB, " +
                    "Free: " + (freeMemory / (1024 * 1024)) + "MB, " +
                    "Total: " + (totalMemory / (1024 * 1024)) + "MB");
        }
    }
    
}
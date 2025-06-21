/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import javafx.scene.input.KeyCode;

/**
 * Constants used throughout the input processing system.
 * 
 * This class centralizes all magic numbers, configuration values, and
 * commonly used constants to improve maintainability and consistency.
 * All constants are public static final for compile-time optimization.
 * 
 * CONSTANT CATEGORIES:
 * - Game mechanics constants
 * - Input validation limits
 * - Display formatting settings
 * - Keyboard shortcuts and hotkeys
 * - Default values for various operations
 * - File paths and naming patterns
 * 
 * DESIGN PRINCIPLES:
 * - All values are compile-time constants
 * - Clear, descriptive naming
 * - Grouped by functional area
 * - Documented with usage context
 * - No magic numbers in the codebase
 */
public final class InputConstants {
    
    // Private constructor to prevent instantiation
    private InputConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // GAME MECHANICS CONSTANTS
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    /** Conversion factor from pixels to feet (7 pixels = 1 foot) */
    public static final double PIXELS_PER_FOOT = 7.0;
    
    /** Conversion factor from feet to pixels (1 foot = 7 pixels) */
    public static final double FEET_PER_PIXEL = 1.0 / PIXELS_PER_FOOT;
    
    /** Default movement speed in pixels per second */
    public static final double DEFAULT_MOVEMENT_SPEED = 42.0;
    
    /** Camera pan speed in pixels per key press */
    public static final double CAMERA_PAN_SPEED = 20.0;
    
    /** Camera zoom factor for zoom in/out operations */
    public static final double CAMERA_ZOOM_FACTOR = 1.1;
    
    /** Default spacing between deployed characters in pixels (5 feet) */
    public static final int DEFAULT_CHARACTER_SPACING_PIXELS = 35;
    
    /** Default spacing between deployed characters in feet */
    public static final double DEFAULT_CHARACTER_SPACING_FEET = 5.0;
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // INPUT VALIDATION LIMITS
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    /** Maximum valid slot number for save/load operations */
    public static final int MAX_SLOT_NUMBER = 9;
    
    /** Minimum valid slot number for save/load operations */
    public static final int MIN_SLOT_NUMBER = 1;
    
    /** Maximum valid quantity for batch character creation */
    public static final int MAX_BATCH_QUANTITY = 20;
    
    /** Minimum valid quantity for batch character creation */
    public static final int MIN_BATCH_QUANTITY = 1;
    
    /** Maximum valid spacing in feet for character placement */
    public static final double MAX_SPACING_FEET = 9.0;
    
    /** Minimum valid spacing in feet for character placement */
    public static final double MIN_SPACING_FEET = 1.0;
    
    /** Maximum number of characters that can be selected at once */
    public static final int MAX_SELECTION_COUNT = 50;
    
    /** Maximum length for character names in display */
    public static final int MAX_CHARACTER_NAME_LENGTH = 25;
    
    /** Maximum length for scenario names */
    public static final int MAX_SCENARIO_NAME_LENGTH = 50;
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // DISPLAY FORMATTING CONSTANTS
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    /** Number of decimal places for coordinate display */
    public static final int COORDINATE_DECIMAL_PLACES = 1;
    
    /** Number of decimal places for percentage display */
    public static final int PERCENTAGE_DECIMAL_PLACES = 1;
    
    /** Number of decimal places for distance display */
    public static final int DISTANCE_DECIMAL_PLACES = 1;
    
    /** Width of ASCII health bars in characters */
    public static final int HEALTH_BAR_WIDTH = 10;
    
    /** Default width for formatted display headers */
    public static final int DEFAULT_HEADER_WIDTH = 50;
    
    /** Maximum number of recent input events to store in trace */
    public static final int MAX_INPUT_TRACE_EVENTS = 100;
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // KEYBOARD SHORTCUTS AND HOTKEYS
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    // Game Control Hotkeys
    /** Key code for pause/resume toggle */
    public static final KeyCode PAUSE_RESUME_KEY = KeyCode.SPACE;
    
    /** Key code for debug mode toggle (with Ctrl) */
    public static final KeyCode DEBUG_MODE_KEY = KeyCode.D;
    
    /** Key code for edit mode toggle (with Ctrl) */
    public static final KeyCode EDIT_MODE_KEY = KeyCode.E;
    
    /** Key code for save game (with Ctrl) */
    public static final KeyCode SAVE_GAME_KEY = KeyCode.S;
    
    /** Key code for load game (with Ctrl) */
    public static final KeyCode LOAD_GAME_KEY = KeyCode.L;
    
    // Camera Control Keys
    /** Key code for camera pan up */
    public static final KeyCode CAMERA_UP_KEY = KeyCode.UP;
    
    /** Key code for camera pan down */
    public static final KeyCode CAMERA_DOWN_KEY = KeyCode.DOWN;
    
    /** Key code for camera pan left */
    public static final KeyCode CAMERA_LEFT_KEY = KeyCode.LEFT;
    
    /** Key code for camera pan right */
    public static final KeyCode CAMERA_RIGHT_KEY = KeyCode.RIGHT;
    
    /** Key code for zoom in */
    public static final KeyCode ZOOM_IN_KEY_1 = KeyCode.EQUALS;
    
    /** Alternative key code for zoom in */
    public static final KeyCode ZOOM_IN_KEY_2 = KeyCode.PLUS;
    
    /** Key code for zoom out */
    public static final KeyCode ZOOM_OUT_KEY = KeyCode.MINUS;
    
    // Unit Control Keys
    /** Key code for increase movement speed */
    public static final KeyCode MOVEMENT_SPEED_UP_KEY = KeyCode.W;
    
    /** Key code for decrease movement speed */
    public static final KeyCode MOVEMENT_SPEED_DOWN_KEY = KeyCode.S;
    
    /** Key code for increase aiming speed */
    public static final KeyCode AIMING_SPEED_UP_KEY = KeyCode.Q;
    
    /** Key code for decrease aiming speed */
    public static final KeyCode AIMING_SPEED_DOWN_KEY = KeyCode.E;
    
    /** Key code for toggle ranged mode */
    public static final KeyCode RANGED_MODE_KEY = KeyCode.R;
    
    /** Key code for toggle melee mode */
    public static final KeyCode MELEE_MODE_KEY = KeyCode.M;
    
    /** Key code for position control */
    public static final KeyCode POSITION_CONTROL_KEY = KeyCode.C;
    
    /** Key code for position adjustment */
    public static final KeyCode POSITION_ADJUST_KEY = KeyCode.V;
    
    /** Key code for automatic targeting toggle */
    public static final KeyCode AUTO_TARGETING_KEY = KeyCode.T;
    
    // Debug Hotkeys (with Ctrl modifier)
    /** Key code for toggle InputManager debug */
    public static final KeyCode DEBUG_TOGGLE_KEY = KeyCode.F1;
    
    /** Key code for configure debug categories */
    public static final KeyCode DEBUG_CONFIGURE_KEY = KeyCode.F2;
    
    /** Key code for system state dump */
    public static final KeyCode DEBUG_STATE_DUMP_KEY = KeyCode.F3;
    
    /** Key code for performance statistics */
    public static final KeyCode DEBUG_PERFORMANCE_KEY = KeyCode.F4;
    
    /** Key code for input trace display */
    public static final KeyCode DEBUG_INPUT_TRACE_KEY = KeyCode.F5;
    
    /** Key code for system integrity validation */
    public static final KeyCode DEBUG_VALIDATION_KEY = KeyCode.F6;
    
    /** Key code for clear debug data */
    public static final KeyCode DEBUG_CLEAR_KEY = KeyCode.F7;
    
    // Character Stats Display
    /** Key code for character stats display (with Shift) */
    public static final KeyCode CHARACTER_STATS_KEY = KeyCode.SLASH;
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // WORKFLOW AND OPERATION DEFAULTS
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    /** Default archetype for character creation */
    public static final int DEFAULT_ARCHETYPE = 1; // Gunslinger
    
    /** Default faction for character assignment */
    public static final int DEFAULT_FACTION = 1; // Cowboys
    
    /** Default batch quantity for character creation */
    public static final int DEFAULT_BATCH_QUANTITY = 5;
    
    /** Default weapon index for weapon assignment */
    public static final int DEFAULT_WEAPON_INDEX = 0; // First weapon in list
    
    /** Default formation for character deployment */
    public static final String DEFAULT_FORMATION = "line";
    
    /** Default theme for new scenarios */
    public static final String DEFAULT_THEME = "western";
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // FILE PATHS AND NAMING PATTERNS
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    /** Base directory for faction character files */
    public static final String FACTION_FILES_DIRECTORY = "factions/";
    
    /** File extension for faction character files */
    public static final String FACTION_FILE_EXTENSION = ".json";
    
    /** Pattern for faction file names */
    public static final String FACTION_FILE_PATTERN = "factions/%d.json";
    
    /** Base directory for save game files */
    public static final String SAVE_FILES_DIRECTORY = "saves/";
    
    /** File extension for save game files */
    public static final String SAVE_FILE_EXTENSION = ".json";
    
    /** Pattern for save file names */
    public static final String SAVE_FILE_PATTERN = "saves/slot_%d.json";
    
    /** Audio file path for gunshot sound */
    public static final String GUNSHOT_SOUND_PATH = "/Slap0003.wav";
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // DEBUG AND DIAGNOSTIC CONSTANTS
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    /** Performance timing precision in nanoseconds */
    public static final long NANOSECONDS_PER_MILLISECOND = 1_000_000L;
    
    /** Memory usage reporting threshold in megabytes */
    public static final long MEMORY_REPORT_THRESHOLD_MB = 10L;
    
    /** Maximum number of performance timing entries to store */
    public static final int MAX_PERFORMANCE_ENTRIES = 50;
    
    /** Debug log timestamp format */
    public static final String DEBUG_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    
    /** Debug category separator for log messages */
    public static final String DEBUG_CATEGORY_SEPARATOR = " - ";
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // MESSAGE TEMPLATES AND FORMATS
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    /** Template for movement command messages */
    public static final String MOVEMENT_MESSAGE_TEMPLATE = "MOVE %d %s to (%.1f, %.1f)";
    
    /** Template for attack command messages */
    public static final String ATTACK_MESSAGE_TEMPLATE = "%s attacks %s with %s";
    
    /** Template for selection messages */
    public static final String SELECTION_MESSAGE_TEMPLATE = "%d units selected";
    
    /** Template for health status display */
    public static final String HEALTH_STATUS_TEMPLATE = "Health: %d/%d (%.1f%%)";
    
    /** Template for coordinate display */
    public static final String COORDINATE_TEMPLATE = "(%.1f, %.1f)";
    
    /** Template for percentage display */
    public static final String PERCENTAGE_TEMPLATE = "%.1f%%";
    
    /** Template for distance display in feet */
    public static final String DISTANCE_FEET_TEMPLATE = "%.1f ft";
    
    /** Template for distance display in pixels */
    public static final String DISTANCE_PIXELS_TEMPLATE = "%.1f px";
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // ARCHETYPE AND FACTION DEFINITIONS
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    /** Number of available character archetypes */
    public static final int ARCHETYPE_COUNT = 6;
    
    /** Array of archetype names for display */
    public static final String[] ARCHETYPE_NAMES = {
        "Gunslinger", "Soldier", "Medic", "Scout", "Marksman", "Brawler"
    };
    
    /** Number of available factions */
    public static final int FACTION_COUNT = 9;
    
    /** Array of faction names for display */
    public static final String[] FACTION_NAMES = {
        "Cowboys", "Outlaws", "Lawmen", "Natives", "Soldiers", 
        "Civilians", "Bandits", "Rangers", "Mercenaries"
    };
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // TIMING AND PERFORMANCE CONSTANTS
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    /** Game update frequency in frames per second */
    public static final int GAME_FPS = 60;
    
    /** Target frame time in milliseconds */
    public static final double TARGET_FRAME_TIME_MS = 1000.0 / GAME_FPS;
    
    /** Performance warning threshold in milliseconds */
    public static final double PERFORMANCE_WARNING_THRESHOLD_MS = 16.67; // ~60 FPS
    
    /** Memory garbage collection threshold in megabytes */
    public static final long GC_THRESHOLD_MB = 100L;
    
    /** Input event processing timeout in milliseconds */
    public static final long INPUT_PROCESSING_TIMEOUT_MS = 1000L;
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // UI AND DISPLAY CONSTANTS
    // ═══════════════════════════════════════════════════────────────────────────────────
    
    /** Default window width in pixels */
    public static final int DEFAULT_WINDOW_WIDTH = 800;
    
    /** Default window height in pixels */
    public static final int DEFAULT_WINDOW_HEIGHT = 600;
    
    /** Minimum unit selection radius in pixels */
    public static final double MIN_SELECTION_RADIUS = 5.0;
    
    /** Maximum unit selection radius in pixels */
    public static final double MAX_SELECTION_RADIUS = 50.0;
    
    /** Default unit selection radius in pixels */
    public static final double DEFAULT_SELECTION_RADIUS = 15.0;
    
    /** Selection rectangle minimum size in pixels */
    public static final double MIN_SELECTION_RECTANGLE_SIZE = 10.0;
    
    /** Character stats display timeout in milliseconds */
    public static final long STATS_DISPLAY_TIMEOUT_MS = 5000L;
    
    /** Status message display duration in milliseconds */
    public static final long STATUS_MESSAGE_DURATION_MS = 3000L;
}
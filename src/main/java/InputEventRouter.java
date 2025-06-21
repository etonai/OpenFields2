/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyEvent;

/**
 * InputEventRouter handles the routing of input events to appropriate handlers based on current application state.
 * 
 * This component was extracted from InputManager as part of DevCycle 15c incremental refactoring.
 * It provides pure event routing logic without dependencies on game state or complex systems.
 * 
 * RESPONSIBILITIES:
 * - Analyze mouse and keyboard events to determine appropriate handling route
 * - Make routing decisions based on current application state flags
 * - Provide consistent event routing logic across the application
 * - Integrate with debug logging for event tracking
 * 
 * DESIGN PRINCIPLES:
 * - Stateless operation: All routing decisions based on passed parameters
 * - No side effects: Pure routing logic without state modifications
 * - Single responsibility: Focus solely on event routing decisions
 * - Debug integration: Full integration with DevCycle 15a debug capabilities
 * 
 * @author DevCycle 15c - Incremental InputManager Refactoring
 */
public class InputEventRouter {
    
    /**
     * Determines the appropriate handler for a mouse event based on current state.
     * 
     * @param event The mouse event to route
     * @param isInDeploymentPlacement Whether currently in deployment placement mode
     * @param isWaitingForDirectAddition Whether waiting for direct character addition
     * @param isInEditMode Whether currently in edit mode
     * @return MouseEventRoute indicating how the event should be handled
     */
    public MouseEventRoute routeMouseEvent(MouseEvent event, boolean isInDeploymentPlacement, 
                                         boolean isWaitingForDirectAddition, boolean isInEditMode) {
        
        if (event.getButton() == MouseButton.PRIMARY) {
            // Left click routing
            if (isInDeploymentPlacement) {
                return MouseEventRoute.DEPLOYMENT_PLACEMENT;
            }
            
            if (isWaitingForDirectAddition) {
                return MouseEventRoute.CHARACTER_PLACEMENT;
            }
            
            // Default left click - selection or rectangle selection
            return MouseEventRoute.UNIT_SELECTION;
            
        } else if (event.getButton() == MouseButton.SECONDARY) {
            // Right click routing
            if (isInEditMode) {
                return MouseEventRoute.EDIT_MODE_CONTEXT;
            }
            
            // Default right click - combat/movement commands
            return MouseEventRoute.COMBAT_MOVEMENT;
        }
        
        // Unknown or unhandled mouse button
        return MouseEventRoute.UNHANDLED;
    }
    
    /**
     * Determines the appropriate handler for a keyboard event based on current state.
     * 
     * @param event The keyboard event to route
     * @param isInEditMode Whether currently in edit mode
     * @param isWaitingForPrompt Whether waiting for user prompt input
     * @param hasSelectedUnits Whether units are currently selected
     * @param isInBatchCreation Whether in batch character creation mode
     * @param isInCharacterDeployment Whether in character deployment mode
     * @return KeyboardEventRoute indicating how the event should be handled
     */
    public KeyboardEventRoute routeKeyboardEvent(KeyEvent event, boolean isInEditMode, 
                                                boolean isWaitingForPrompt, boolean hasSelectedUnits,
                                                boolean isInBatchCreation, boolean isInCharacterDeployment) {
        
        KeyCode code = event.getCode();
        
        // Universal camera controls (always available)
        if (isCameraControl(code)) {
            return KeyboardEventRoute.CAMERA_CONTROL;
        }
        
        // Universal game controls
        if (isGameControl(code)) {
            return KeyboardEventRoute.GAME_CONTROL;
        }
        
        // Debug controls (Ctrl+F keys)
        if (isDebugControl(event)) {
            return KeyboardEventRoute.DEBUG_CONTROL;
        }
        
        // State-specific routing (highest priority)
        if (isWaitingForPrompt) {
            return KeyboardEventRoute.PROMPT_INPUT;
        }
        
        if (isInBatchCreation) {
            return KeyboardEventRoute.BATCH_CREATION;
        }
        
        if (isInCharacterDeployment) {
            return KeyboardEventRoute.CHARACTER_DEPLOYMENT;
        }
        
        // Edit mode controls
        if (isInEditMode && isEditModeControl(code)) {
            return KeyboardEventRoute.EDIT_MODE;
        }
        
        // Unit controls (when units are selected)
        if (hasSelectedUnits && isUnitControl(code)) {
            return KeyboardEventRoute.UNIT_CONTROL;
        }
        
        // Character stats display
        if (isStatsDisplay(event)) {
            return KeyboardEventRoute.STATS_DISPLAY;
        }
        
        // Save/load controls
        if (isSaveLoadControl(event)) {
            return KeyboardEventRoute.SAVE_LOAD;
        }
        
        // Combat controls
        if (isCombatControl(code)) {
            return KeyboardEventRoute.COMBAT_CONTROL;
        }
        
        // Unhandled key
        return KeyboardEventRoute.UNHANDLED;
    }
    
    // Camera control detection
    private boolean isCameraControl(KeyCode code) {
        return code == KeyCode.UP || code == KeyCode.DOWN || 
               code == KeyCode.LEFT || code == KeyCode.RIGHT ||
               code == KeyCode.EQUALS || code == KeyCode.PLUS || 
               code == KeyCode.MINUS;
    }
    
    // Game control detection
    private boolean isGameControl(KeyCode code) {
        return code == KeyCode.SPACE;  // Pause/resume
    }
    
    // Debug control detection
    private boolean isDebugControl(KeyEvent event) {
        return event.isControlDown() && 
               (event.getCode() == KeyCode.F1 || event.getCode() == KeyCode.F2 ||
                event.getCode() == KeyCode.F3 || event.getCode() == KeyCode.F4 ||
                event.getCode() == KeyCode.F5 || event.getCode() == KeyCode.F6 ||
                event.getCode() == KeyCode.F7);
    }
    
    // Edit mode control detection
    private boolean isEditModeControl(KeyCode code) {
        return code == KeyCode.DIGIT1 || code == KeyCode.DIGIT2 || 
               code == KeyCode.DIGIT3 || code == KeyCode.DIGIT4 ||
               code == KeyCode.DIGIT5 || code == KeyCode.DIGIT6 ||
               code == KeyCode.DIGIT7 || code == KeyCode.DIGIT8 ||
               code == KeyCode.DIGIT9 || code == KeyCode.A ||
               code == KeyCode.D || code == KeyCode.S || code == KeyCode.L ||
               code == KeyCode.V || code == KeyCode.T;
    }
    
    // Unit control detection
    private boolean isUnitControl(KeyCode code) {
        return code == KeyCode.W || code == KeyCode.S ||  // Movement speed
               code == KeyCode.Q || code == KeyCode.E ||  // Aiming speed
               code == KeyCode.C || code == KeyCode.V ||  // Position controls
               code == KeyCode.R || code == KeyCode.F ||  // Combat controls
               code == KeyCode.X;                         // Auto-targeting
    }
    
    // Stats display detection
    private boolean isStatsDisplay(KeyEvent event) {
        return event.isShiftDown() && event.getCode() == KeyCode.SLASH;
    }
    
    // Save/load control detection
    private boolean isSaveLoadControl(KeyEvent event) {
        return event.isControlDown() && 
               (event.getCode() == KeyCode.S || event.getCode() == KeyCode.L);
    }
    
    // Combat control detection
    private boolean isCombatControl(KeyCode code) {
        return code == KeyCode.Z || code == KeyCode.I || code == KeyCode.J ||
               code == KeyCode.K || code == KeyCode.O || code == KeyCode.P ||
               code == KeyCode.SEMICOLON;
    }
    
    /**
     * Enumeration of possible mouse event routing destinations.
     */
    public enum MouseEventRoute {
        DEPLOYMENT_PLACEMENT,   // Handle deployment character placement
        CHARACTER_PLACEMENT,    // Handle direct character addition placement
        UNIT_SELECTION,        // Handle unit selection or rectangle selection
        EDIT_MODE_CONTEXT,     // Handle edit mode right-click operations
        COMBAT_MOVEMENT,       // Handle combat commands and movement orders
        UNHANDLED             // Event not recognized or supported
    }
    
    /**
     * Enumeration of possible keyboard event routing destinations.
     */
    public enum KeyboardEventRoute {
        CAMERA_CONTROL,        // Camera pan and zoom operations
        GAME_CONTROL,         // Game state controls (pause/resume)
        DEBUG_CONTROL,        // Debug system controls (Ctrl+F keys)
        PROMPT_INPUT,         // User prompt input handling
        BATCH_CREATION,       // Batch character creation workflow
        CHARACTER_DEPLOYMENT, // Character deployment workflow
        EDIT_MODE,           // Edit mode operations
        UNIT_CONTROL,        // Unit movement and behavior controls
        STATS_DISPLAY,       // Character statistics display
        SAVE_LOAD,          // Save and load operations
        COMBAT_CONTROL,      // Combat-specific controls
        UNHANDLED           // Event not recognized or supported
    }
}
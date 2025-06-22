/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.canvas.Canvas;

/**
 * Controller responsible for managing camera operations in the OpenFields2 game.
 * 
 * This controller handles all aspects of camera control including:
 * - Camera navigation (arrow key panning)
 * - Zoom controls (plus/minus keys)
 * - Coordinate conversion between screen and world coordinates
 * - Camera state management for consistent positioning
 * 
 * The controller integrates with the GameRenderer to provide seamless camera
 * operations while maintaining performance and user experience standards.
 * 
 * @author DevCycle 15h - Phase 4.1: Camera Controller Extraction
 */
public class CameraController {
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Dependencies and State
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /** Game renderer for camera positioning, zoom, and coordinate conversion */
    private final GameRenderer gameRenderer;
    
    /** Display coordinator for debug operations and feedback */
    private final DisplayCoordinator displayCoordinator;
    
    /** Canvas for screen dimensions and coordinate calculations */
    private final Canvas canvas;
    
    // Camera Control Constants
    /** Pan distance in pixels for arrow key navigation */
    private static final double PAN_DISTANCE = 20.0;
    
    /** Zoom factor for zoom in/out operations */
    private static final double ZOOM_FACTOR = 1.1;
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Constructor for CameraController.
     * 
     * @param gameRenderer Game renderer for camera operations
     * @param displayCoordinator Display coordinator for debug operations
     * @param canvas Canvas for coordinate calculations
     */
    public CameraController(GameRenderer gameRenderer, DisplayCoordinator displayCoordinator, 
                           Canvas canvas) {
        this.gameRenderer = gameRenderer;
        this.displayCoordinator = displayCoordinator;
        this.canvas = canvas;
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Camera Navigation Controls
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Handle camera control key events.
     * 
     * Processes keyboard input for camera navigation and zoom operations:
     * - Arrow keys: Pan camera in four directions
     * - Plus/Equals keys: Zoom in
     * - Minus key: Zoom out
     * 
     * @param e KeyEvent containing the key press information
     * @return true if the key event was handled by camera controls, false otherwise
     */
    public boolean handleCameraControls(KeyEvent e) {
        KeyCode keyCode = e.getCode();
        
        switch (keyCode) {
            case UP:
                displayCoordinator.debugInputEvent("CAMERA_CONTROL", "Pan up");
                gameRenderer.adjustOffset(0, PAN_DISTANCE);
                return true;
                
            case DOWN:
                displayCoordinator.debugInputEvent("CAMERA_CONTROL", "Pan down");
                gameRenderer.adjustOffset(0, -PAN_DISTANCE);
                return true;
                
            case LEFT:
                displayCoordinator.debugInputEvent("CAMERA_CONTROL", "Pan left");
                gameRenderer.adjustOffset(PAN_DISTANCE, 0);
                return true;
                
            case RIGHT:
                displayCoordinator.debugInputEvent("CAMERA_CONTROL", "Pan right");
                gameRenderer.adjustOffset(-PAN_DISTANCE, 0);
                return true;
                
            case EQUALS:
            case PLUS:
                displayCoordinator.debugInputEvent("CAMERA_CONTROL", "Zoom in");
                gameRenderer.adjustZoom(ZOOM_FACTOR);
                return true;
                
            case MINUS:
                displayCoordinator.debugInputEvent("CAMERA_CONTROL", "Zoom out");
                gameRenderer.adjustZoom(1.0 / ZOOM_FACTOR);
                return true;
                
            default:
                return false; // Key not handled by camera controls
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Coordinate Conversion
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Convert screen coordinates to world coordinates.
     * 
     * This method provides a centralized way to convert mouse/screen coordinates
     * to world coordinates, taking into account camera position and zoom level.
     * 
     * @param screenX Screen X coordinate (typically from mouse event)
     * @param screenY Screen Y coordinate (typically from mouse event)
     * @return WorldCoordinate object containing the converted coordinates
     */
    public WorldCoordinate convertScreenToWorld(double screenX, double screenY) {
        double worldX = gameRenderer.screenToWorldX(screenX);
        double worldY = gameRenderer.screenToWorldY(screenY);
        return new WorldCoordinate(worldX, worldY);
    }
    
    /**
     * Get the world coordinates of the camera center.
     * 
     * This is useful for operations that need to know the center of the visible area,
     * such as character spawning or centering operations.
     * 
     * @return WorldCoordinate object containing the camera center in world coordinates
     */
    public WorldCoordinate getCameraCenterWorldCoordinates() {
        double centerScreenX = canvas.getWidth() / 2.0;
        double centerScreenY = canvas.getHeight() / 2.0;
        return convertScreenToWorld(centerScreenX, centerScreenY);
    }
    
    /**
     * Get current camera offset values.
     * 
     * @return CameraOffset object containing current X and Y offset values
     */
    public CameraOffset getCurrentOffset() {
        return new CameraOffset(gameRenderer.getOffsetX(), gameRenderer.getOffsetY());
    }
    
    /**
     * Get current camera zoom level.
     * 
     * @return Current zoom factor as a double
     */
    public double getCurrentZoom() {
        return gameRenderer.getZoom();
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Camera Positioning Operations
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Set camera position to center on specific world coordinates.
     * 
     * @param worldX World X coordinate to center on
     * @param worldY World Y coordinate to center on
     */
    public void centerOnWorldCoordinates(double worldX, double worldY) {
        // Calculate screen center
        double centerScreenX = canvas.getWidth() / 2.0;
        double centerScreenY = canvas.getHeight() / 2.0;
        
        // Calculate required offset to center on the world coordinates
        double currentCenterWorldX = gameRenderer.screenToWorldX(centerScreenX);
        double currentCenterWorldY = gameRenderer.screenToWorldY(centerScreenY);
        
        double deltaX = worldX - currentCenterWorldX;
        double deltaY = worldY - currentCenterWorldY;
        
        // Apply offset adjustment to center on target coordinates
        gameRenderer.adjustOffset(-deltaX * gameRenderer.getZoom(), -deltaY * gameRenderer.getZoom());
        
        displayCoordinator.debugInputEvent("CAMERA_CONTROL", 
            "Centered on world coordinates (" + String.format("%.1f", worldX) + 
            ", " + String.format("%.1f", worldY) + ")");
    }
    
    /**
     * Reset camera to default position and zoom.
     */
    public void resetCameraToDefault() {
        // Reset to default zoom and offset
        gameRenderer.setZoom(1.0);
        gameRenderer.setOffset(0.0, 0.0);
        displayCoordinator.debugInputEvent("CAMERA_CONTROL", "Camera reset to default position");
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Data Transfer Objects
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Data transfer object for world coordinates.
     */
    public static class WorldCoordinate {
        public final double x;
        public final double y;
        
        public WorldCoordinate(double x, double y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String toString() {
            return String.format("WorldCoordinate(%.1f, %.1f)", x, y);
        }
    }
    
    /**
     * Data transfer object for camera offset values.
     */
    public static class CameraOffset {
        public final double offsetX;
        public final double offsetY;
        
        public CameraOffset(double offsetX, double offsetY) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
        
        @Override
        public String toString() {
            return String.format("CameraOffset(%.1f, %.1f)", offsetX, offsetY);
        }
    }
}
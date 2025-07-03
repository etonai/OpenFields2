/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import game.Unit;

/**
 * Minimal GameRenderer implementation for headless mode that provides
 * just enough functionality for SaveGameController to work.
 */
public class HeadlessGameRendererWrapper extends GameRenderer {
    
    // Basic camera state for save/load operations
    private double offsetX = 0.0;
    private double offsetY = 0.0;
    private double zoom = 1.0;
    
    // Muzzle flash tracking - maintain for compatibility
    private Map<Integer, Long> muzzleFlashes = new HashMap<>();
    private long currentTick = 0;
    
    /**
     * Constructor for headless mode
     */
    public HeadlessGameRendererWrapper() {
        super(null); // Pass null canvas for headless mode
    }
    
    /**
     * Sets the game state references needed for rendering.
     */
    public void setGameState(List<Unit> units, SelectionManager selectionManager) {
        // No-op for headless mode
    }
    
    /**
     * Main rendering method - no-op in headless mode.
     */
    public void render() {
        // No-op for headless mode
    }
    
    /**
     * Sets the current tick for muzzle flash timing.
     */
    public void setCurrentTick(long tick) {
        this.currentTick = tick;
    }
    
    /**
     * Adds a muzzle flash effect.
     */
    public void addMuzzleFlash(int unitId, long endTick) {
        muzzleFlashes.put(unitId, endTick);
    }
    
    /**
     * Gets camera offset X.
     */
    public double getOffsetX() {
        return offsetX;
    }
    
    /**
     * Gets camera offset Y.
     */
    public double getOffsetY() {
        return offsetY;
    }
    
    /**
     * Gets zoom level.
     */
    public double getZoom() {
        return zoom;
    }
    
    /**
     * Sets camera offset.
     */
    public void setOffset(double offsetX, double offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }
    
    /**
     * Sets zoom level.
     */
    public void setZoom(double zoom) {
        this.zoom = zoom;
    }
    
    /**
     * Adjusts zoom level.
     */
    public void adjustZoom(double delta) {
        this.zoom += delta;
    }
}
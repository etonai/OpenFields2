/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import game.Unit;

/**
 * Headless version of GameRenderer that provides no-op implementations
 * for all rendering operations. Used for testing without JavaFX.
 */
public class HeadlessGameRenderer implements BaseGameRenderer {
    // Game state references
    private List<Unit> units;
    private SelectionManager selectionManager;
    
    // Muzzle flash tracking - maintain for compatibility
    private Map<Integer, Long> muzzleFlashes = new HashMap<>();
    private long currentTick = 0;
    
    /**
     * Sets the game state references needed for rendering.
     */
    public void setGameState(List<Unit> units, SelectionManager selectionManager) {
        this.units = units;
        this.selectionManager = selectionManager;
    }
    
    /**
     * Main rendering method - no-op in headless mode.
     */
    public void render() {
        // No-op for headless mode
    }
    
    /**
     * Sets the current tick for muzzle flash timing.
     * @param tick the current game tick
     */
    public void setCurrentTick(long tick) {
        this.currentTick = tick;
    }
    
    /**
     * Sets debug mode flag.
     * @param debugMode true to enable debug mode
     */
    public static void setDebugMode(boolean debugMode) {
        // No-op for headless mode
    }
    
    /**
     * Adds a muzzle flash effect.
     * @param unitId the unit ID to add flash for
     * @param endTick the tick when the flash should end
     */
    public void addMuzzleFlash(int unitId, long endTick) {
        muzzleFlashes.put(unitId, endTick);
    }
    
    /**
     * Gets camera offset X.
     * @return camera offset X
     */
    public double getOffsetX() {
        return 0.0;
    }
    
    /**
     * Gets camera offset Y.
     * @return camera offset Y
     */
    public double getOffsetY() {
        return 0.0;
    }
    
    /**
     * Gets zoom level.
     * @return zoom level
     */
    public double getZoom() {
        return 1.0;
    }
    
    /**
     * Sets camera offset.
     * @param offsetX new offset X
     * @param offsetY new offset Y
     */
    public void setOffset(double offsetX, double offsetY) {
        // No-op for headless mode
    }
    
    /**
     * Sets zoom level.
     * @param zoom new zoom level
     */
    public void setZoom(double zoom) {
        // No-op for headless mode
    }
    
    /**
     * Adjusts zoom level.
     * @param delta zoom adjustment
     */
    public void adjustZoom(double delta) {
        // No-op for headless mode
    }
}
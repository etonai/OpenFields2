/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import java.util.List;
import game.Unit;

/**
 * Base interface for game renderers.
 * Allows for both JavaFX and headless implementations.
 */
public interface BaseGameRenderer {
    /**
     * Sets the game state references needed for rendering.
     */
    void setGameState(List<Unit> units, SelectionManager selectionManager);
    
    /**
     * Main rendering method.
     */
    void render();
    
    /**
     * Sets the current tick for timing-based effects.
     * @param tick the current game tick
     */
    void setCurrentTick(long tick);
    
    /**
     * Adds a muzzle flash effect.
     * @param unitId the unit ID to add flash for
     * @param endTick the tick when the flash should end
     */
    void addMuzzleFlash(int unitId, long endTick);
}
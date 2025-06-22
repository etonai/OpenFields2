/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

package input.interfaces;

/**
 * Callback interface for operations that require access to main game functionality.
 * 
 * InputManager uses this interface to delegate operations that require access to
 * methods or state not directly available within the InputManager scope. This design
 * maintains separation of concerns while enabling InputManager to coordinate complex
 * operations that span multiple game subsystems.
 * 
 * The interface is organized into functional groups for different types of operations.
 * 
 * @author DevCycle 15h - Interface Extraction
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
    
    /** Get current character deployment state */
    boolean isWaitingForCharacterDeployment();
    
    /** Set character deployment state */
    void setWaitingForCharacterDeployment(boolean waiting);
    
    /** Get current victory outcome state */
    boolean isWaitingForVictoryOutcome();
    
    /** Set victory outcome state */
    void setWaitingForVictoryOutcome(boolean waiting);
    
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
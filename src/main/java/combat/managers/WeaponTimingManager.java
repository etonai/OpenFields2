package combat.managers;

import combat.Character;
import data.SkillsManager;
import utils.GameConstants;

/**
 * Manages weapon speed and timing calculations for characters.
 * Extracted from Character.java following DevCycle 32 refactoring.
 * Provides utility methods for weapon state transition timing.
 */
public class WeaponTimingManager {
    
    // Singleton instance
    private static WeaponTimingManager instance;
    
    /**
     * Private constructor for singleton pattern.
     */
    private WeaponTimingManager() {
    }
    
    /**
     * Get the singleton instance of WeaponTimingManager.
     * 
     * @return The manager instance
     */
    public static WeaponTimingManager getInstance() {
        if (instance == null) {
            instance = new WeaponTimingManager();
        }
        return instance;
    }
    
    /**
     * Calculate weapon ready speed multiplier based on reflexes and quickdraw skill.
     * Extracted from Character.calculateWeaponReadySpeedMultiplier() (~8 lines).
     * 
     * @param character The character to calculate speed for
     * @return Speed multiplier (lower values = faster)
     */
    public double calculateWeaponReadySpeedMultiplier(Character character) {
        int reflexesModifier = GameConstants.statToModifier(character.reflexes);
        double reflexesSpeedMultiplier = 1.0 - (reflexesModifier * 0.015);
        
        int quickdrawLevel = character.getSkillLevel(SkillsManager.QUICKDRAW);
        double quickdrawSpeedMultiplier = 1.0 - (quickdrawLevel * 0.08);
        
        return reflexesSpeedMultiplier * quickdrawSpeedMultiplier;
    }
    
    /**
     * Get weapon ready speed multiplier (simple wrapper).
     * Extracted from Character.getWeaponReadySpeedMultiplier() (~2 lines).
     * 
     * @param character The character to calculate speed for
     * @return Speed multiplier (lower values = faster)
     */
    public double getWeaponReadySpeedMultiplier(Character character) {
        return calculateWeaponReadySpeedMultiplier(character);
    }
    
    /**
     * Calculate attack speed multiplier for consistency with weapon ready speed.
     * Extracted from Character.calculateAttackSpeedMultiplier() (~4 lines).
     * 
     * @param character The character to calculate speed for
     * @return Speed multiplier (lower values = faster)
     */
    public double calculateAttackSpeedMultiplier(Character character) {
        // Use same speed calculation as weapon ready speed for consistency
        return calculateWeaponReadySpeedMultiplier(character);
    }
}
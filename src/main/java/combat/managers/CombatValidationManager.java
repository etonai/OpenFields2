package combat.managers;

import combat.Character;
import combat.RangedWeapon;
import combat.MeleeWeapon;
import combat.WeaponState;
import game.interfaces.IUnit;

/**
 * Manages combat validation and range checking for characters.
 * Extracted from Character.java following DevCycle 32 refactoring.
 * Provides validation utilities for combat operations.
 */
public class CombatValidationManager {
    
    // Singleton instance
    private static CombatValidationManager instance;
    
    /**
     * Private constructor for singleton pattern.
     */
    private CombatValidationManager() {
    }
    
    /**
     * Get the singleton instance of CombatValidationManager.
     * 
     * @return The manager instance
     */
    public static CombatValidationManager getInstance() {
        if (instance == null) {
            instance = new CombatValidationManager();
        }
        return instance;
    }
    
    /**
     * Check if character can reload their ranged weapon.
     * Extracted from Character.canReload() (~7 lines).
     * 
     * @param character The character to check
     * @return true if can reload, false otherwise
     */
    public boolean canReload(Character character) {
        if (character.weapon == null || !(character.weapon instanceof RangedWeapon)) return false;
        RangedWeapon rangedWeapon = (RangedWeapon)character.weapon;
        if (rangedWeapon.getAmmunition() >= rangedWeapon.getMaxAmmunition()) return false;
        String state = character.currentWeaponState.getState();
        return "ready".equals(state) || "aiming".equals(state) || "recovering".equals(state);
    }
    
    /**
     * Check if attacker is in melee range of target.
     * Extracted from Character.isInMeleeRange() (~8 lines).
     * 
     * @param attacker The attacking unit
     * @param target The target unit
     * @param weapon The melee weapon being used
     * @return true if in range, false otherwise
     */
    public boolean isInMeleeRange(IUnit attacker, IUnit target, MeleeWeapon weapon) {
        double centerToCenter = Math.hypot(target.getX() - attacker.getX(), target.getY() - attacker.getY());
        // Convert to edge-to-edge by subtracting target radius (1.5 feet = 10.5 pixels)
        double edgeToEdge = centerToCenter - (1.5 * 7.0);
        double pixelRange = weapon.getTotalReach() * 7.0; // Convert feet to pixels (7 pixels = 1 foot)
        
        return edgeToEdge <= pixelRange;
    }
    
    /**
     * Check if weapon state is a preparation state.
     * Extracted from Character.isWeaponPreparationState() (~4 lines).
     * 
     * @param character The character to check
     * @param stateName The state name to check
     * @return true if preparation state, false otherwise
     */
    public boolean isWeaponPreparationState(Character character, String stateName) {
        // Create a temporary WeaponState to delegate to WeaponStateManager
        WeaponState tempState = new WeaponState(stateName, "", 0);
        return WeaponStateManager.getInstance().isWeaponPreparationState(tempState);
    }
}
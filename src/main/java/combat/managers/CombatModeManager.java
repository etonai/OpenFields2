package combat.managers;

import combat.Character;
import combat.Weapon;
import combat.WeaponState;
import combat.RangedWeapon;
import combat.MeleeWeapon;
import combat.MeleeWeaponFactory;

/**
 * Manages combat mode transitions and weapon management for characters.
 * Extracted from Character.java following DevCycle 32 refactoring.
 * Handles switching between ranged and melee combat modes.
 */
public class CombatModeManager {
    
    // Singleton instance
    private static CombatModeManager instance;
    
    /**
     * Private constructor for singleton pattern.
     */
    private CombatModeManager() {
    }
    
    /**
     * Get the singleton instance of CombatModeManager.
     * 
     * @return The manager instance
     */
    public static CombatModeManager getInstance() {
        if (instance == null) {
            instance = new CombatModeManager();
        }
        return instance;
    }
    
    /**
     * Toggle between ranged and melee combat modes for a character.
     * Extracted from Character.toggleCombatMode() (~25 lines).
     * 
     * @param character The character to toggle combat mode for
     */
    public void toggleCombatMode(Character character) {
        // Cancel any ongoing melee movement when switching modes
        if (character.isMovingToMelee) {
            character.isMovingToMelee = false;
            character.meleeTarget = null;
        }
        
        // Cancel any ongoing attacks when switching modes
        if (character.isAttacking) {
            character.isAttacking = false;
        }
        
        boolean oldMode = character.isMeleeCombatMode;
        character.isMeleeCombatMode = !character.isMeleeCombatMode;
        
        // Ensure character has a melee weapon when switching to melee mode
        if (character.isMeleeCombatMode && character.meleeWeapon == null) {
            character.meleeWeapon = MeleeWeaponFactory.createUnarmed();
        }
        
        // Initialize weapon state to melee weapon's initial state when switching to melee mode
        if (character.isMeleeCombatMode && character.meleeWeapon != null) {
            WeaponState meleeInitialState = character.meleeWeapon.getInitialState();
            if (meleeInitialState != null) {
                character.currentWeaponState = meleeInitialState;
            }
        }
        
        // Reset weapon hold state when switching combat modes
        character.resetWeaponHoldStateToDefault();
    }
    
    /**
     * Get the currently active weapon for a character based on combat mode.
     * Extracted from Character.getActiveWeapon() (~8 lines).
     * 
     * @param character The character to get active weapon for
     * @return The active weapon (ranged or melee based on mode)
     */
    public Weapon getActiveWeapon(Character character) {
        if (character.isMeleeCombatMode) {
            return character.meleeWeapon != null ? character.meleeWeapon : character.weapon;
        } else {
            return character.rangedWeapon != null ? character.rangedWeapon : character.weapon;
        }
    }
    
    /**
     * Initialize default weapons for a character if not already set.
     * Extracted from Character.initializeDefaultWeapons() (~18 lines).
     * 
     * @param character The character to initialize weapons for
     */
    public void initializeDefaultWeapons(Character character) {
        // Check if we should skip weapon initialization (for platform independence)
        if (isWeaponInitializationDisabled()) {
            return;
        }
        
        // Initialize melee weapon to unarmed if not set
        if (character.meleeWeapon == null) {
            character.meleeWeapon = MeleeWeaponFactory.createUnarmed();
        }
        
        // If we have a legacy weapon, convert it to ranged weapon
        if (character.weapon != null && character.rangedWeapon == null) {
            if (character.weapon instanceof RangedWeapon) {
                character.rangedWeapon = (RangedWeapon) character.weapon;
            }
            // Note: weapon will remain for backward compatibility
        }
    }
    
    /**
     * Check if weapon initialization should be disabled (for platform independence)
     */
    private boolean isWeaponInitializationDisabled() {
        // Check system property for disabling weapon initialization
        String skipWeapons = System.getProperty("openfields2.skipDefaultWeapons");
        return "true".equals(skipWeapons);
    }
}
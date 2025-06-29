package combat.managers;

import combat.WeaponState;
import combat.Weapon;
import combat.Character;

/**
 * Interface for managing weapon state transitions and hold states.
 * Handles weapon state progression, hold states, and firing preferences.
 */
public interface IWeaponStateManager {
    
    /**
     * Schedule a weapon state transition.
     * 
     * @param character The character whose weapon is transitioning
     * @param fromState The current weapon state
     * @param toState The target weapon state
     * @param currentTick The current game tick
     * @return The tick when the transition will complete
     */
    long scheduleStateTransition(Character character, WeaponState fromState, WeaponState toState, long currentTick);
    
    /**
     * Check if a weapon state is a preparation state.
     * Preparation states include drawing, unslinging, unsheathing, etc.
     * 
     * @param state The weapon state to check
     * @return true if this is a preparation state
     */
    boolean isWeaponPreparationState(WeaponState state);
    
    /**
     * Get the current weapon hold state for a character.
     * 
     * @param characterId The ID of the character
     * @return The current hold state name, or null if not set
     */
    String getWeaponHoldState(int characterId);
    
    /**
     * Set the weapon hold state for a character.
     * 
     * @param characterId The ID of the character
     * @param holdState The hold state to set
     */
    void setWeaponHoldState(int characterId, String holdState);
    
    /**
     * Cycle to the next available hold state for a weapon.
     * Implements the H key functionality from DevCycle 25.
     * 
     * @param character The character cycling hold states
     * @param weapon The weapon to cycle states for
     * @return The new hold state name
     */
    String cycleWeaponHoldState(Character character, Weapon weapon);
    
    /**
     * Get the firing preference for a character.
     * True for aiming state, false for point-from-hip state.
     * 
     * @param characterId The ID of the character
     * @return The firing preference
     */
    boolean getFiresFromAimingState(int characterId);
    
    /**
     * Set the firing preference for a character.
     * 
     * @param characterId The ID of the character
     * @param firesFromAiming True for aiming, false for point-from-hip
     */
    void setFiresFromAimingState(int characterId, boolean firesFromAiming);
    
    /**
     * Toggle the firing preference for a character.
     * Implements the SHIFT-F functionality from DevCycle 26.
     * 
     * @param characterId The ID of the character
     * @return The new firing preference
     */
    boolean toggleFiringPreference(int characterId);
    
    /**
     * Get the target hold state for a character's weapon progression.
     * 
     * @param characterId The ID of the character
     * @return The target hold state, or null if not progressing
     */
    String getTargetHoldState(int characterId);
    
    /**
     * Set the target hold state for weapon progression.
     * 
     * @param characterId The ID of the character
     * @param targetState The target state to progress to
     */
    void setTargetHoldState(int characterId, String targetState);
    
    /**
     * Determine if a weapon should stop at a specific state.
     * Takes into account hold states and firing preferences.
     * 
     * @param character The character with the weapon
     * @param currentState The current weapon state
     * @param targetState The intended target state
     * @return true if progression should stop at current state
     */
    boolean shouldStopAtState(Character character, WeaponState currentState, String targetState);
    
    /**
     * Calculate the duration for a state transition.
     * Takes into account reflexes, quickdraw skill, and weapon properties.
     * 
     * @param character The character performing the transition
     * @param fromState The starting state
     * @param toState The ending state
     * @return The duration in ticks
     */
    long calculateTransitionDuration(Character character, WeaponState fromState, WeaponState toState);
    
    /**
     * Clean up all state for a character that is being removed.
     * 
     * @param characterId The ID of the character to clean up
     */
    void cleanupCharacter(int characterId);
}
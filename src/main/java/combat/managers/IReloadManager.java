package combat.managers;

import combat.Character;
import combat.RangedWeapon;
import combat.ReloadType;

/**
 * Interface for managing weapon reload mechanics.
 * Handles reload sequences, timing, and ammunition management.
 */
public interface IReloadManager {
    
    /**
     * Start a reload sequence for a character's weapon.
     * 
     * @param character The character reloading
     * @param weapon The weapon being reloaded
     * @param currentTick The current game tick
     * @return true if reload started successfully
     */
    boolean startReloadSequence(Character character, RangedWeapon weapon, long currentTick);
    
    /**
     * Continue an ongoing reload sequence.
     * 
     * @param character The character continuing reload
     * @param currentTick The current game tick
     * @return true if reload is still in progress
     */
    boolean continueReloading(Character character, long currentTick);
    
    /**
     * Perform the actual reload operation.
     * Updates ammunition counts based on reload type.
     * 
     * @param character The character performing reload
     * @param weapon The weapon being reloaded
     * @param reloadType The type of reload to perform
     */
    void performReload(Character character, RangedWeapon weapon, ReloadType reloadType);
    
    /**
     * Calculate the reload speed for a character.
     * Takes into account dexterity and wound penalties.
     * 
     * @param character The character reloading
     * @return The reload speed multiplier (1.0 = normal speed)
     */
    double calculateReloadSpeed(Character character);
    
    /**
     * Check if a character is currently reloading.
     * 
     * @param characterId The ID of the character
     * @return true if the character is reloading
     */
    boolean isReloading(int characterId);
    
    /**
     * Get the progress of a reload sequence.
     * 
     * @param characterId The ID of the character
     * @param currentTick The current game tick
     * @return The reload progress as a percentage (0.0 to 1.0), or -1 if not reloading
     */
    double getReloadProgress(int characterId, long currentTick);
    
    /**
     * Get the tick when a reload sequence started.
     * 
     * @param characterId The ID of the character
     * @return The start tick, or -1 if not reloading
     */
    long getReloadStartTick(int characterId);
    
    /**
     * Get the tick when a reload sequence will complete.
     * 
     * @param characterId The ID of the character
     * @return The completion tick, or -1 if not reloading
     */
    long getReloadCompletionTick(int characterId);
    
    /**
     * Cancel an ongoing reload sequence.
     * 
     * @param characterId The ID of the character
     * @return true if a reload was cancelled
     */
    boolean cancelReload(int characterId);
    
    /**
     * Set reload state for a character.
     * 
     * @param characterId The ID of the character
     * @param startTick The tick when reload started
     * @param completionTick The tick when reload will complete
     */
    void setReloadState(int characterId, long startTick, long completionTick);
    
    /**
     * Calculate the total reload duration for a weapon.
     * 
     * @param weapon The weapon to calculate reload time for
     * @param character The character performing the reload
     * @return The reload duration in ticks
     */
    long calculateReloadDuration(RangedWeapon weapon, Character character);
    
    /**
     * Clean up all state for a character that is being removed.
     * 
     * @param characterId The ID of the character to clean up
     */
    void cleanupCharacter(int characterId);
}
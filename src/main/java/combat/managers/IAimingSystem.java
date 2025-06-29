package combat.managers;

import combat.AimingSpeed;
import combat.Character;

/**
 * Interface for managing aiming mechanics and time-based accuracy bonuses.
 * Tracks aiming duration and calculates accuracy bonuses based on time spent aiming.
 */
public interface IAimingSystem {
    
    /**
     * Start timing for aiming state.
     * Records when a character begins aiming at a target.
     * 
     * @param characterId The ID of the character starting to aim
     * @param currentTick The current game tick
     */
    void startAimingTiming(int characterId, long currentTick);
    
    /**
     * Start timing for pointing-from-hip state.
     * Records when a character begins pointing from hip.
     * 
     * @param characterId The ID of the character starting to point from hip
     * @param currentTick The current game tick
     */
    void startPointingFromHipTiming(int characterId, long currentTick);
    
    /**
     * Get the duration a character has been aiming.
     * 
     * @param characterId The ID of the character
     * @param currentTick The current game tick
     * @return The duration in ticks, or -1 if not aiming
     */
    long getAimingDuration(int characterId, long currentTick);
    
    /**
     * Get the duration a character has been pointing from hip.
     * 
     * @param characterId The ID of the character
     * @param currentTick The current game tick
     * @return The duration in ticks, or -1 if not pointing
     */
    long getPointingFromHipDuration(int characterId, long currentTick);
    
    /**
     * Calculate the earned aiming bonus modifier based on time spent aiming.
     * Implements the accumulated aiming bonus system from DevCycle 27.
     * 
     * @param character The character who is aiming
     * @param aimingDuration The duration in ticks spent aiming
     * @return The accuracy bonus modifier earned from aiming
     */
    int calculateEarnedAimingBonusModifier(Character character, long aimingDuration);
    
    /**
     * Determine the aiming speed to use for a shot.
     * Takes into account multiple shot sequences from DevCycle 28.
     * 
     * @param character The character taking the shot
     * @param shotInSequence The shot number in a multiple shot sequence (0 for single shot)
     * @return The aiming speed to use for this shot
     */
    AimingSpeed determineAimingSpeedForShot(Character character, int shotInSequence);
    
    /**
     * Get the aiming speed for a specific shot in a multiple shot sequence.
     * Implements DevCycle 28's pattern: first shot uses character's aiming speed, rest use Quick.
     * 
     * @param character The character taking multiple shots
     * @return The aiming speed for the current shot in sequence
     */
    AimingSpeed getAimingSpeedForMultipleShot(Character character);
    
    /**
     * Reset aiming timing for a character.
     * Called when aiming is interrupted or completed.
     * 
     * @param characterId The ID of the character
     */
    void resetAimingTiming(int characterId);
    
    /**
     * Reset pointing-from-hip timing for a character.
     * Called when pointing is interrupted or completed.
     * 
     * @param characterId The ID of the character
     */
    void resetPointingFromHipTiming(int characterId);
    
    /**
     * Check if a character is currently timing their aim.
     * 
     * @param characterId The ID of the character
     * @return true if aiming timing is active
     */
    boolean isAimingTimingActive(int characterId);
    
    /**
     * Check if a character is currently timing their point-from-hip.
     * 
     * @param characterId The ID of the character
     * @return true if pointing timing is active
     */
    boolean isPointingFromHipTimingActive(int characterId);
    
    /**
     * Get current aiming duration based on character's firing preference.
     * Returns aiming duration if firing from aiming state, pointing duration otherwise.
     * 
     * @param character The character to check
     * @param currentTick The current game tick
     * @return The relevant duration in ticks
     */
    long getCurrentAimingDuration(Character character, long currentTick);
    
    /**
     * Calculate the aiming speed multiplier for a character.
     * Applies 25% of weapon ready speed bonus to aiming.
     * 
     * @param character The character to calculate for
     * @return The aiming speed multiplier
     */
    double calculateAimingSpeedMultiplier(Character character);
    
    /**
     * Toggle firing preference between aiming and point-from-hip.
     * Implements SHIFT-F functionality from DevCycle 26.
     * 
     * @param character The character toggling preference
     * @param currentTick The current game tick
     */
    void toggleFiringPreference(Character character, long currentTick);
    
    /**
     * Handle smart state adjustments when firing preference changes.
     * 
     * @param character The character whose preference changed
     * @param oldPreference The previous firing preference
     * @param currentTick The current game tick
     */
    void handleFiringPreferenceStateAdjustment(Character character, boolean oldPreference, long currentTick);
    
    /**
     * Check if a character can use very careful aiming based on weapon skills.
     * 
     * @param character The character to check
     * @return true if very careful aiming is available
     */
    boolean canUseVeryCarefulAiming(Character character);
    
    /**
     * Increase aiming speed (Q key functionality).
     * 
     * @param character The character increasing aiming speed
     */
    void increaseAimingSpeed(Character character);
    
    /**
     * Decrease aiming speed (E key functionality).
     * 
     * @param character The character decreasing aiming speed
     */
    void decreaseAimingSpeed(Character character);
    
    /**
     * Calculate accumulated aiming bonus enum value.
     * 
     * @param character The character aiming
     * @param currentTick The current game tick
     * @return The accumulated aiming bonus enum
     */
    combat.AccumulatedAimingBonus calculateEarnedAimingBonus(Character character, long currentTick);
    
    /**
     * Clean up all state for a character that is being removed.
     * 
     * @param characterId The ID of the character to clean up
     */
    void cleanupCharacter(int characterId);
}
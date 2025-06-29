package combat.managers;

import game.interfaces.IUnit;
import combat.Character;

/**
 * Interface for managing burst and automatic fire mechanics.
 * Handles burst sequences, full-auto fire, and related state tracking.
 */
public interface IBurstFireManager {
    
    /**
     * Handle continuous firing for automatic weapons.
     * Determines whether to continue burst or full-auto sequences.
     * 
     * @param character The character performing the firing
     * @param target The current target unit
     * @param currentTick The current game tick
     * @return true if firing should continue, false otherwise
     */
    boolean handleContinuousFiring(Character character, IUnit target, long currentTick);
    
    /**
     * Handle burst firing mode.
     * Manages the state and timing of burst fire sequences.
     * 
     * @param character The character performing burst fire
     * @param target The current target unit
     * @param currentTick The current game tick
     */
    void handleBurstFiring(Character character, IUnit target, long currentTick);
    
    /**
     * Handle full-auto firing mode.
     * Manages continuous automatic fire sequences.
     * 
     * @param character The character performing full-auto fire
     * @param target The current target unit
     * @param currentTick The current game tick
     */
    void handleFullAutoFiring(Character character, IUnit target, long currentTick);
    
    /**
     * Continue a standard (non-burst) attack sequence.
     * 
     * @param character The character continuing the attack
     * @param target The current target unit
     * @param currentTick The current game tick
     */
    void continueStandardAttack(Character character, IUnit target, long currentTick);
    
    /**
     * Continue automatic shooting sequence.
     * Handles the next shot in an automatic firing sequence.
     * 
     * @param character The character continuing automatic fire
     * @param target The current target unit
     * @param currentTick The current game tick
     */
    void continueAutomaticShooting(Character character, IUnit target, long currentTick);
    
    /**
     * Check if a character is currently in automatic firing mode.
     * 
     * @param characterId The ID of the character to check
     * @return true if the character is firing automatically
     */
    boolean isAutomaticFiring(int characterId);
    
    /**
     * Set automatic firing state for a character.
     * 
     * @param characterId The ID of the character
     * @param isAutomaticFiring Whether the character is firing automatically
     */
    void setAutomaticFiring(int characterId, boolean isAutomaticFiring);
    
    /**
     * Get the number of burst shots fired by a character.
     * 
     * @param characterId The ID of the character
     * @return The number of burst shots fired in the current sequence
     */
    int getBurstShotsFired(int characterId);
    
    /**
     * Set the number of burst shots fired by a character.
     * 
     * @param characterId The ID of the character
     * @param shotsFired The number of shots fired
     */
    void setBurstShotsFired(int characterId, int shotsFired);
    
    /**
     * Get the tick of the last automatic shot.
     * 
     * @param characterId The ID of the character
     * @return The tick when the last automatic shot was fired
     */
    long getLastAutomaticShot(int characterId);
    
    /**
     * Set the tick of the last automatic shot.
     * 
     * @param characterId The ID of the character
     * @param tick The tick of the last shot
     */
    void setLastAutomaticShot(int characterId, long tick);
    
    /**
     * Schedule burst shots after the first shot in a burst sequence.
     * Called from the firing phase to schedule remaining burst shots.
     * 
     * @param character The character performing burst fire
     * @param shooter The unit performing the burst fire
     * @param fireTick The tick when the first shot was fired
     * @param gameCallbacks Game callbacks for effects and impact scheduling
     */
    void scheduleBurstShots(Character character, IUnit shooter, long fireTick, game.GameCallbacks gameCallbacks);
    
    /**
     * Check if burst/auto accuracy penalty should apply.
     * 
     * @param characterId The ID of the character
     * @return true if accuracy penalty should apply (shots 2+ in burst/auto)
     */
    boolean shouldApplyBurstAutoPenalty(int characterId);
    
    /**
     * Clean up all state for a character that is being removed.
     * 
     * @param characterId The ID of the character to clean up
     */
    void cleanupCharacter(int characterId);
}
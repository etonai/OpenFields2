package combat.managers;

import combat.Character;
import game.interfaces.IUnit;

/**
 * Interface for character targeting management
 * DevCycle 30 - Character class size reduction
 */
public interface ITargetManager {
    /**
     * Get character's current target
     * @param characterId Character ID
     * @return Current target unit
     */
    IUnit getCurrentTarget(int characterId);
    
    /**
     * Set character's current target
     * @param characterId Character ID
     * @param target Target unit
     */
    void setCurrentTarget(int characterId, IUnit target);
    
    /**
     * Get character's previous target
     * @param characterId Character ID
     * @return Previous target unit
     */
    IUnit getPreviousTarget(int characterId);
    
    /**
     * Set character's previous target
     * @param characterId Character ID
     * @param target Previous target unit
     */
    void setPreviousTarget(int characterId, IUnit target);
    
    /**
     * Check if character has a valid target
     * @param characterId Character ID
     * @return true if has valid target
     */
    boolean hasValidTarget(int characterId);
    
    /**
     * Get character's melee target
     * @param characterId Character ID
     * @return Melee target unit
     */
    IUnit getMeleeTarget(int characterId);
    
    /**
     * Set character's melee target
     * @param characterId Character ID
     * @param target Melee target unit
     */
    void setMeleeTarget(int characterId, IUnit target);
    
    /**
     * Check if target changed since last attack
     * @param characterId Character ID
     * @param newTarget New target being checked
     * @return true if target changed
     */
    boolean hasTargetChanged(int characterId, IUnit newTarget);
    
    /**
     * Get reaction target for character
     * @param characterId Character ID
     * @return Reaction target unit
     */
    IUnit getReactionTarget(int characterId);
    
    /**
     * Set reaction target for character
     * @param characterId Character ID
     * @param target Reaction target unit
     */
    void setReactionTarget(int characterId, IUnit target);
    
    /**
     * Clean up targeting data for a character when they are removed
     * @param characterId Character ID to clean up
     */
    void cleanupCharacter(int characterId);
}
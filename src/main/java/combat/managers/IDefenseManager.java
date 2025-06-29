package combat.managers;

import combat.DefenseState;
import combat.Character;
import game.interfaces.IUnit;

/**
 * Interface for managing defensive mechanics including blocking and counter-attacks.
 * Handles defense states, cooldowns, and counter-attack opportunities.
 */
public interface IDefenseManager {
    
    /**
     * Get the current defense state for a character.
     * 
     * @param characterId The ID of the character
     * @return The current defense state
     */
    DefenseState getDefenseState(int characterId);
    
    /**
     * Set the defense state for a character.
     * 
     * @param characterId The ID of the character
     * @param state The new defense state
     */
    void setDefenseState(int characterId, DefenseState state);
    
    /**
     * Check if a character can currently defend.
     * Takes into account cooldowns and current state.
     * 
     * @param character The character attempting to defend
     * @param currentTick The current game tick
     * @return true if the character can defend
     */
    boolean canDefend(Character character, long currentTick);
    
    /**
     * Attempt to block an incoming attack.
     * 
     * @param defender The defending character
     * @param attacker The attacking unit
     * @param currentTick The current game tick
     * @return true if the block was successful
     */
    boolean attemptBlock(Character defender, IUnit attacker, long currentTick);
    
    /**
     * Set defense cooldown for a character.
     * 
     * @param characterId The ID of the character
     * @param cooldownEndTick The tick when the cooldown ends
     */
    void setDefenseCooldown(int characterId, long cooldownEndTick);
    
    /**
     * Get the defense cooldown end tick for a character.
     * 
     * @param characterId The ID of the character
     * @return The tick when cooldown ends, or -1 if no cooldown
     */
    long getDefenseCooldownEndTick(int characterId);
    
    /**
     * Check if a character is in defense cooldown.
     * 
     * @param characterId The ID of the character
     * @param currentTick The current game tick
     * @return true if the character is in cooldown
     */
    boolean isInDefenseCooldown(int characterId, long currentTick);
    
    /**
     * Set counter-attack opportunity window for a character.
     * 
     * @param characterId The ID of the character
     * @param windowEndTick The tick when the counter window closes
     */
    void setCounterAttackWindow(int characterId, long windowEndTick);
    
    /**
     * Get the counter-attack window end tick for a character.
     * 
     * @param characterId The ID of the character
     * @return The tick when counter window ends, or -1 if no window
     */
    long getCounterAttackWindowEndTick(int characterId);
    
    /**
     * Check if a character has a counter-attack opportunity.
     * 
     * @param characterId The ID of the character
     * @param currentTick The current game tick
     * @return true if the character can counter-attack
     */
    boolean hasCounterAttackOpportunity(int characterId, long currentTick);
    
    /**
     * Set whether a character has a counter-attack opportunity available.
     * 
     * @param characterId The ID of the character
     * @param hasOpportunity Whether counter-attack is available
     */
    void setHasCounterAttackOpportunity(int characterId, boolean hasOpportunity);
    
    /**
     * Execute a counter-attack if available.
     * 
     * @param defender The defending character executing the counter
     * @param attacker The original attacker to counter
     * @param currentTick The current game tick
     * @return true if counter-attack was executed
     */
    boolean executeCounterAttack(Character defender, IUnit attacker, long currentTick);
    
    /**
     * Update defense statistics for a character.
     * 
     * @param characterId The ID of the character
     * @param isSuccessful Whether the defense was successful
     */
    void updateDefenseStatistics(int characterId, boolean isSuccessful);
    
    /**
     * Update counter-attack statistics for a character.
     * 
     * @param characterId The ID of the character
     * @param isSuccessful Whether the counter-attack was successful
     */
    void updateCounterAttackStatistics(int characterId, boolean isSuccessful);
    
    /**
     * Clean up all state for a character that is being removed.
     * 
     * @param characterId The ID of the character to clean up
     */
    void cleanupCharacter(int characterId);
}
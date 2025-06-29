package combat.managers;

import combat.Character;
import combat.Wound;
import java.util.List;

/**
 * Interface for character statistics and health management
 * DevCycle 30 - Character class size reduction
 */
public interface ICharacterStatsManager {
    /**
     * Get character's current health
     * @param characterId Character ID
     * @return Current health
     */
    int getCurrentHealth(int characterId);
    
    /**
     * Set character's current health
     * @param characterId Character ID
     * @param health New health value
     */
    void setCurrentHealth(int characterId, int health);
    
    /**
     * Get character's maximum health
     * @param characterId Character ID
     * @return Maximum health
     */
    int getMaxHealth(int characterId);
    
    /**
     * Set character's maximum health
     * @param characterId Character ID
     * @param health Maximum health value
     */
    void setMaxHealth(int characterId, int health);
    
    /**
     * Check if character is incapacitated (health <= 0)
     * @param characterId Character ID
     * @return true if incapacitated
     */
    boolean isIncapacitated(int characterId);
    
    /**
     * Get dexterity modifier for character
     * @param characterId Character ID
     * @return Dexterity modifier (-20 to +20)
     */
    int getDexterityModifier(int characterId);
    
    /**
     * Get coolness modifier for character
     * @param characterId Character ID
     * @return Coolness modifier (-20 to +20)
     */
    int getCoolnessModifier(int characterId);
    
    /**
     * Get reflexes modifier for character
     * @param characterId Character ID
     * @return Reflexes modifier (-20 to +20)
     */
    int getReflexesModifier(int characterId);
    
    /**
     * Get strength modifier for character
     * @param characterId Character ID
     * @return Strength modifier (-20 to +20)
     */
    int getStrengthModifier(int characterId);
    
    /**
     * Calculate wound penalty for character
     * @param characterId Character ID
     * @return Wound penalty value
     */
    int getWoundPenalty(int characterId);
    
    /**
     * Get character's wounds
     * @param characterId Character ID
     * @return List of wounds
     */
    List<Wound> getWounds(int characterId);
    
    /**
     * Set character's wounds
     * @param characterId Character ID
     * @param wounds List of wounds
     */
    void setWounds(int characterId, List<Wound> wounds);
    
    /**
     * Add a wound to character
     * @param characterId Character ID
     * @param wound Wound to add
     */
    void addWound(int characterId, Wound wound);
    
    /**
     * Remove a wound from character
     * @param characterId Character ID
     * @param wound Wound to remove
     * @return true if wound was removed
     */
    boolean removeWound(int characterId, Wound wound);
    
    /**
     * Get character's dexterity stat
     * @param characterId Character ID
     * @return Dexterity value
     */
    int getDexterity(int characterId);
    
    /**
     * Set character's dexterity stat
     * @param characterId Character ID
     * @param dexterity Dexterity value
     */
    void setDexterity(int characterId, int dexterity);
    
    /**
     * Get character's coolness stat
     * @param characterId Character ID
     * @return Coolness value
     */
    int getCoolness(int characterId);
    
    /**
     * Set character's coolness stat
     * @param characterId Character ID
     * @param coolness Coolness value
     */
    void setCoolness(int characterId, int coolness);
    
    /**
     * Get character's reflexes stat
     * @param characterId Character ID
     * @return Reflexes value
     */
    int getReflexes(int characterId);
    
    /**
     * Set character's reflexes stat
     * @param characterId Character ID
     * @param reflexes Reflexes value
     */
    void setReflexes(int characterId, int reflexes);
    
    /**
     * Get character's strength stat
     * @param characterId Character ID
     * @return Strength value
     */
    int getStrength(int characterId);
    
    /**
     * Set character's strength stat
     * @param characterId Character ID
     * @param strength Strength value
     */
    void setStrength(int characterId, int strength);
    
    /**
     * Clean up stats data for a character when they are removed
     * @param characterId Character ID to clean up
     */
    void cleanupCharacter(int characterId);
}
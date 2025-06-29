package combat.managers;

import combat.Character;
import combat.Wound;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton manager for character statistics and health
 * DevCycle 30 - Character class size reduction
 * Follows DevCycle 29 singleton pattern with per-character state tracking
 */
public class CharacterStatsManager implements ICharacterStatsManager {
    private static CharacterStatsManager instance;
    
    // Per-character statistics storage
    private final Map<Integer, Integer> currentHealth = new HashMap<>();
    private final Map<Integer, Integer> maxHealth = new HashMap<>();
    private final Map<Integer, Integer> dexterity = new HashMap<>();
    private final Map<Integer, Integer> coolness = new HashMap<>();
    private final Map<Integer, Integer> reflexes = new HashMap<>();
    private final Map<Integer, Integer> strength = new HashMap<>();
    private final Map<Integer, List<Wound>> wounds = new HashMap<>();
    
    private CharacterStatsManager() {
        // Singleton pattern - private constructor
    }
    
    public static CharacterStatsManager getInstance() {
        if (instance == null) {
            instance = new CharacterStatsManager();
        }
        return instance;
    }
    
    @Override
    public int getCurrentHealth(int characterId) {
        return currentHealth.getOrDefault(characterId, 0);
    }
    
    @Override
    public void setCurrentHealth(int characterId, int health) {
        currentHealth.put(characterId, health);
    }
    
    @Override
    public int getMaxHealth(int characterId) {
        return maxHealth.getOrDefault(characterId, 0);
    }
    
    @Override
    public void setMaxHealth(int characterId, int health) {
        maxHealth.put(characterId, health);
    }
    
    @Override
    public boolean isIncapacitated(int characterId) {
        return getCurrentHealth(characterId) <= 0;
    }
    
    @Override
    public int getDexterityModifier(int characterId) {
        return calculateStatModifier(dexterity.getOrDefault(characterId, 50));
    }
    
    @Override
    public int getCoolnessModifier(int characterId) {
        return calculateStatModifier(coolness.getOrDefault(characterId, 50));
    }
    
    @Override
    public int getReflexesModifier(int characterId) {
        return calculateStatModifier(reflexes.getOrDefault(characterId, 50));
    }
    
    @Override
    public int getStrengthModifier(int characterId) {
        return calculateStatModifier(strength.getOrDefault(characterId, 50));
    }
    
    /**
     * Calculate stat modifier from stat value (1-100) to modifier (-20 to +20)
     * Uses the same balanced curve as Character class
     */
    private int calculateStatModifier(int statValue) {
        // Clamp to valid range
        statValue = Math.max(1, Math.min(100, statValue));
        
        // Use balanced curve from Character class
        if (statValue <= 50) {
            // Below average: -20 to 0
            return (int) (-20.0 + (40.0 * (statValue - 1) / 49.0));
        } else {
            // Above average: 0 to +20
            return (int) (20.0 * (statValue - 50) / 50.0);
        }
    }
    
    @Override
    public int getWoundPenalty(int characterId) {
        List<Wound> characterWounds = getWounds(characterId);
        int penalty = 0;
        
        for (Wound wound : characterWounds) {
            penalty += wound.getSeverity().ordinal(); // Basic penalty calculation
        }
        
        return penalty;
    }
    
    @Override
    public List<Wound> getWounds(int characterId) {
        return wounds.getOrDefault(characterId, new ArrayList<>());
    }
    
    @Override
    public void setWounds(int characterId, List<Wound> woundsList) {
        wounds.put(characterId, new ArrayList<>(woundsList));
    }
    
    @Override
    public void addWound(int characterId, Wound wound) {
        List<Wound> characterWounds = wounds.computeIfAbsent(characterId, k -> new ArrayList<>());
        characterWounds.add(wound);
    }
    
    @Override
    public boolean removeWound(int characterId, Wound wound) {
        List<Wound> characterWounds = wounds.get(characterId);
        if (characterWounds == null) return false;
        return characterWounds.remove(wound);
    }
    
    @Override
    public int getDexterity(int characterId) {
        return dexterity.getOrDefault(characterId, 0);
    }
    
    @Override
    public void setDexterity(int characterId, int dexterityValue) {
        dexterity.put(characterId, dexterityValue);
    }
    
    @Override
    public int getCoolness(int characterId) {
        return coolness.getOrDefault(characterId, 0);
    }
    
    @Override
    public void setCoolness(int characterId, int coolnessValue) {
        coolness.put(characterId, coolnessValue);
    }
    
    @Override
    public int getReflexes(int characterId) {
        return reflexes.getOrDefault(characterId, 0);
    }
    
    @Override
    public void setReflexes(int characterId, int reflexesValue) {
        reflexes.put(characterId, reflexesValue);
    }
    
    @Override
    public int getStrength(int characterId) {
        return strength.getOrDefault(characterId, 0);
    }
    
    @Override
    public void setStrength(int characterId, int strengthValue) {
        strength.put(characterId, strengthValue);
    }
    
    /**
     * Initialize stats for a character from Character object
     * @param character Character to initialize from
     */
    public void initializeStats(Character character) {
        int id = character.id;
        setCurrentHealth(id, character.currentHealth);
        setMaxHealth(id, character.health);
        dexterity.put(id, character.dexterity);
        coolness.put(id, character.coolness);
        reflexes.put(id, character.reflexes);
        strength.put(id, character.strength);
        setWounds(id, character.wounds);
    }
    
    @Override
    public void cleanupCharacter(int characterId) {
        currentHealth.remove(characterId);
        maxHealth.remove(characterId);
        dexterity.remove(characterId);
        coolness.remove(characterId);
        reflexes.remove(characterId);
        strength.remove(characterId);
        wounds.remove(characterId);
    }
}
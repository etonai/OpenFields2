package combat;

/**
 * Legacy adapter for Character backward compatibility methods.
 * Extracted from Character class as part of DevCycle 24 refactoring.
 * 
 * Provides combined statistics methods that sum ranged and melee combat data.
 * These methods exist for backward compatibility with DevCycle 12 enhanced tracking.
 */
public class CharacterLegacyAdapter {
    
    /**
     * Returns total attacks attempted across both ranged and melee combat
     * @param character Character to get statistics from
     * @return Sum of ranged and melee attacks attempted
     */
    public static int getCombinedAttacksAttempted(Character character) {
        return character.rangedAttacksAttempted + character.meleeAttacksAttempted;
    }
    
    /**
     * Returns total successful attacks across both ranged and melee combat
     * @param character Character to get statistics from
     * @return Sum of ranged and melee successful attacks
     */
    public static int getCombinedAttacksSuccessful(Character character) {
        return character.rangedAttacksSuccessful + character.meleeAttacksSuccessful;
    }
    
    /**
     * Returns total wounds inflicted across both ranged and melee combat (simple count)
     * @param character Character to get statistics from
     * @return Sum of ranged and melee wounds inflicted
     */
    public static int getCombinedWoundsInflicted(Character character) {
        return character.rangedWoundsInflicted + character.meleeWoundsInflicted;
    }
}
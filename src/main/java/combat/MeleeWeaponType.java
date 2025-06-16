package combat;

/**
 * Enumeration of melee weapon types categorized by size and reach.
 * Used to determine weapon reach, handedness, and tactical usage.
 */
public enum MeleeWeaponType {
    /**
     * Unarmed combat - no weapon, basic punching and grappling
     * Reach: 1.5 feet (character radius only)
     */
    UNARMED,
    
    /**
     * Dual-wielding weapons - two weapons used together
     * Reach: Varies by weapons being dual-wielded
     */
    TWO_WEAPON,
    
    /**
     * Long weapons - spears, rifles with bayonets, two-handed swords
     * Reach: 3+ feet, typically two-handed
     */
    LONG,
    
    /**
     * Medium weapons - tomahawks, sabres, one-handed swords
     * Reach: 2-3 feet, typically one-handed but can be two-handed
     */
    MEDIUM,
    
    /**
     * Short weapons - knives, daggers, pistol-whipping
     * Reach: 1.5-2 feet, one-handed
     */
    SHORT;
    
    /**
     * Get the default reach for this weapon type in feet.
     * This is the base reach that can be modified by specific weapons.
     */
    public double getDefaultReach() {
        switch (this) {
            case UNARMED: return 1.5;
            case SHORT: return 2.0;
            case MEDIUM: return 2.5;
            case LONG: return 3.0;
            case TWO_WEAPON: return 2.0; // Default, varies by weapons
            default: return 1.5;
        }
    }
    
    /**
     * Check if this weapon type is typically one-handed.
     */
    public boolean isTypicallyOneHanded() {
        switch (this) {
            case UNARMED: return true; // Technically both hands, but treated as one-handed for combat
            case SHORT: return true;
            case MEDIUM: return true; // Can be two-handed but typically one
            case LONG: return false;
            case TWO_WEAPON: return true; // Each weapon is one-handed
            default: return true;
        }
    }
}
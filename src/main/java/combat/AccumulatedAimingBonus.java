package combat;

/**
 * Enum representing accuracy bonuses earned through accumulated aiming time.
 * Characters who maintain aim on targets for extended periods earn these bonuses
 * based on time thresholds, independent of their selected aiming speed.
 * 
 * DevCycle 27: System 3 - Accumulated Aiming Time Bonus System
 */
public enum AccumulatedAimingBonus {
    NONE("None", 0.0),
    NORMAL("Normal", 0.0),
    CAREFUL("Careful", 15.0),
    VERY_CAREFUL("Very Careful", 15.0);
    
    private final String displayName;
    private final double accuracyModifier;
    
    AccumulatedAimingBonus(String displayName, double accuracyModifier) {
        this.displayName = displayName;
        this.accuracyModifier = accuracyModifier;
    }
    
    /**
     * Get the display name for this bonus level
     * @return Display name string
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get the accuracy modifier for this bonus level
     * @return Accuracy modifier value
     */
    public double getAccuracyModifier() {
        return accuracyModifier;
    }
    
    /**
     * Check if this is the Very Careful bonus level
     * @return True if Very Careful, false otherwise
     */
    public boolean isVeryCareful() {
        return this == VERY_CAREFUL;
    }
}
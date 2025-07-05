package combat;

public enum AimingSpeed {
    VERY_CAREFUL("Very Careful", 3.0, 15.0), // Base timing, skill bonus calculated separately
    CAREFUL("Careful", 2.0, 15.0),
    NORMAL("Normal", 1.0, 0.0),
    QUICK("Quick", 0.2, -20.0);
    
    private final String displayName;
    private final double timingMultiplier;
    private final double accuracyModifier;
    
    AimingSpeed(String displayName, double timingMultiplier, double accuracyModifier) {
        this.displayName = displayName;
        this.timingMultiplier = timingMultiplier;
        this.accuracyModifier = accuracyModifier;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public double getTimingMultiplier() {
        return timingMultiplier;
    }
    
    public double getAccuracyModifier() {
        return accuracyModifier;
    }
    
    public AimingSpeed increase() {
        switch (this) {
            case VERY_CAREFUL: return CAREFUL;
            case CAREFUL: return NORMAL;
            case NORMAL: return QUICK;
            case QUICK: return QUICK; // Already at maximum
            default: return this;
        }
    }
    
    public AimingSpeed decrease() {
        switch (this) {
            case QUICK: return NORMAL;
            case NORMAL: return CAREFUL;
            case CAREFUL: return VERY_CAREFUL;
            case VERY_CAREFUL: return VERY_CAREFUL; // Already at minimum
            default: return this;
        }
    }
    
    public boolean isVeryCareful() {
        return this == VERY_CAREFUL;
    }
    
    // Get random additional aiming time for very careful aiming (2-5 seconds)
    public long getVeryCarefulAdditionalTime() {
        if (this == VERY_CAREFUL) {
            return (long) (120 + utils.RandomProvider.nextDouble() * 180); // 2-5 seconds in ticks (60 ticks/second)
        }
        return 0;
    }
}
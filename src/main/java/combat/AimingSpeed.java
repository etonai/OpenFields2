package combat;

public enum AimingSpeed {
    CAREFUL("Careful", 2.0, 15.0),
    NORMAL("Normal", 1.0, 0.0),
    QUICK("Quick", 0.5, -20.0);
    
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
            case CAREFUL: return CAREFUL; // Already at minimum
            default: return this;
        }
    }
}
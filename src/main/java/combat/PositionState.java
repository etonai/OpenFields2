package combat;

public enum PositionState {
    STANDING("Standing", 1.0),
    KNEELING("Kneeling", 0.8),
    PRONE("Prone", 0.6);
    
    private final String displayName;
    private final double speedMultiplier;
    
    PositionState(String displayName, double speedMultiplier) {
        this.displayName = displayName;
        this.speedMultiplier = speedMultiplier;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public double getSpeedMultiplier() {
        return speedMultiplier;
    }
    
    public PositionState increase() {
        switch (this) {
            case PRONE: return KNEELING;
            case KNEELING: return STANDING;
            case STANDING: return STANDING; // Already at max
            default: return this;
        }
    }
    
    public PositionState decrease() {
        switch (this) {
            case STANDING: return KNEELING;
            case KNEELING: return PRONE;
            case PRONE: return PRONE; // Already at min
            default: return this;
        }
    }
    
    // Get stray shot probability contribution
    public double getStrayProbabilityContribution() {
        switch (this) {
            case STANDING: return 0.5;
            case KNEELING: return 0.25;
            case PRONE: return 0.125;
            default: return 0.5;
        }
    }
    
    // Get hit selection weight for stray shots
    public double getHitSelectionWeight() {
        switch (this) {
            case STANDING: return 100.0;
            case KNEELING: return 50.0;
            case PRONE: return 25.0;
            default: return 100.0;
        }
    }
    
    // Get accuracy penalty when targeting this position
    public int getTargetingPenalty() {
        switch (this) {
            case STANDING: return 0;
            case KNEELING: return 0; // Future enhancement
            case PRONE: return -15;
            default: return 0;
        }
    }
}
package combat;

public enum MovementType {
    CRAWL("Crawl", 0.25),
    WALK("Walk", 1.0),
    JOG("Jog", 1.5),
    RUN("Run", 2.0);
    
    private final String displayName;
    private final double speedMultiplier;
    
    MovementType(String displayName, double speedMultiplier) {
        this.displayName = displayName;
        this.speedMultiplier = speedMultiplier;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public double getSpeedMultiplier() {
        return speedMultiplier;
    }
    
    public MovementType increase() {
        switch (this) {
            case CRAWL: return WALK;
            case WALK: return JOG;
            case JOG: return RUN;
            case RUN: return RUN; // Already at maximum
            default: return this;
        }
    }
    
    public MovementType decrease() {
        switch (this) {
            case RUN: return JOG;
            case JOG: return WALK;
            case WALK: return CRAWL;
            case CRAWL: return CRAWL; // Already at minimum
            default: return this;
        }
    }
}
package combat;

public enum Handedness {
    LEFT_HANDED("Left-handed"),
    RIGHT_HANDED("Right-handed"),
    AMBIDEXTROUS("Ambidextrous");
    
    private final String displayName;
    
    Handedness(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
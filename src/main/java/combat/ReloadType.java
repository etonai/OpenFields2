package combat;

public enum ReloadType {
    SINGLE_ROUND("Single Round"),  // Revolver-style: reload one round at a time
    FULL_MAGAZINE("Full Magazine"); // Magazine-style: reload all ammunition at once
    
    private final String displayName;
    
    ReloadType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
package combat;

public enum WeaponType {
    PISTOL("Pistol"),
    RIFLE("Rifle"),
    OTHER("Other");
    
    private final String displayName;
    
    WeaponType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
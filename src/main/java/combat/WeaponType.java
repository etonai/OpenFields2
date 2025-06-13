package combat;

public enum WeaponType {
    PISTOL("Pistol"),
    RIFLE("Rifle"),
    SUBMACHINE_GUN("Submachine Gun"),
    OTHER("Other");
    
    private final String displayName;
    
    WeaponType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
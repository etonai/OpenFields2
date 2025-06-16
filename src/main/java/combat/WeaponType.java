package combat;

public enum WeaponType {
    PISTOL("Pistol"),
    RIFLE("Rifle"),
    SUBMACHINE_GUN("Submachine Gun"),
    OTHER("Other"),
    MELEE_UNARMED("Unarmed"),
    MELEE_SHORT("Short Melee"),
    MELEE_MEDIUM("Medium Melee"),
    MELEE_LONG("Long Melee");
    
    private final String displayName;
    
    WeaponType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
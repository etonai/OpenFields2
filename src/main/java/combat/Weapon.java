package combat;

import java.util.List;

/**
 * Abstract base class for all weapons in the game.
 * Contains common properties shared between ranged and melee weapons.
 */
public abstract class Weapon {
    // Common weapon properties - temporarily public for backward compatibility
    public String name;
    public int damage;
    public String soundFile;
    public double weaponLength; // Weapon length in feet
    public int weaponAccuracy;
    public WeaponType weaponType;
    public List<WeaponState> states;
    public String initialStateName;
    protected String woundDescription; // Wound description for combat messages (renamed from projectileName - DevCycle 17)
    protected String weaponId; // Unique weapon identifier from JSON data (DevCycle 17)
    protected int defenseBonus = 0; // Defense bonus when using this weapon (DevCycle 40)
    
    // Note: Ranged-weapon-specific fields have been moved to RangedWeapon class to eliminate duplication

    /**
     * Base constructor for all weapons
     */
    public Weapon(String weaponId, String name, int damage, String soundFile, double weaponLength, int weaponAccuracy, WeaponType weaponType) {
        this.weaponId = weaponId;
        this.name = name;
        this.damage = damage;
        this.soundFile = soundFile;
        this.weaponLength = weaponLength;
        this.weaponAccuracy = weaponAccuracy;
        this.weaponType = weaponType;
        this.woundDescription = "projectile"; // Default value
        this.defenseBonus = 0; // Default value
    }
    
    /**
     * Base constructor for all weapons with defense bonus (DevCycle 40)
     */
    public Weapon(String weaponId, String name, int damage, String soundFile, double weaponLength, int weaponAccuracy, WeaponType weaponType, int defenseBonus) {
        this(weaponId, name, damage, soundFile, weaponLength, weaponAccuracy, weaponType);
        this.defenseBonus = defenseBonus;
    }

    // Common getter methods
    public String getName() {
        return name;
    }

    public int getDamage() {
        return damage;
    }
    
    public String getSoundFile() {
        return soundFile;
    }
    
    public double getWeaponLength() {
        return weaponLength;
    }
    
    public int getWeaponAccuracy() {
        return weaponAccuracy;
    }
    
    public WeaponType getWeaponType() {
        return weaponType;
    }
    
    public List<WeaponState> getStates() {
        return states;
    }
    
    public void setStates(List<WeaponState> states) {
        this.states = states;
    }
    
    public String getInitialStateName() {
        return initialStateName;
    }
    
    public void setInitialStateName(String initialStateName) {
        this.initialStateName = initialStateName;
    }

    // Common weapon state methods
    public WeaponState getStateByName(String name) {
        if (states == null) return null;
        for (WeaponState s : states) {
            if (s.getState().equals(name)) return s;
        }
        return null;
    }

    public WeaponState getNextState(WeaponState current) {
        return getStateByName(current.getAction());
    }

    public WeaponState getInitialState() {
        return getStateByName(initialStateName);
    }
    
    public void setWeaponLength(double length) {
        if (length > 0) {
            this.weaponLength = length;
        }
    }
    
    
    public String getWoundDescription() {
        return woundDescription != null ? woundDescription : "projectile";
    }
    
    public void setWoundDescription(String woundDescription) {
        this.woundDescription = woundDescription;
    }
    
    // Legacy methods for backward compatibility (DevCycle 17)
    @Deprecated
    public String getProjectileName() {
        return getWoundDescription();
    }
    
    @Deprecated
    public void setProjectileName(String projectileName) {
        setWoundDescription(projectileName);
    }
    
    /**
     * Get the unique weapon identifier (DevCycle 17)
     */
    public String getWeaponId() {
        return weaponId;
    }
    
    public void setWeaponId(String weaponId) {
        this.weaponId = weaponId;
    }
    
    /**
     * Get the defense bonus this weapon provides (DevCycle 40)
     * @return Defense bonus value
     */
    public int getDefenseBonus() {
        return defenseBonus;
    }
    
    /**
     * Set the defense bonus this weapon provides (DevCycle 40)
     * @param defenseBonus Defense bonus value
     */
    public void setDefenseBonus(int defenseBonus) {
        this.defenseBonus = defenseBonus;
    }
    
}
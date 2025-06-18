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
    protected String projectileName; // Projectile name for combat messages
    
    // Note: Ranged-weapon-specific fields have been moved to RangedWeapon class to eliminate duplication

    /**
     * Base constructor for all weapons
     */
    public Weapon(String name, int damage, String soundFile, double weaponLength, int weaponAccuracy, WeaponType weaponType) {
        this.name = name;
        this.damage = damage;
        this.soundFile = soundFile;
        this.weaponLength = weaponLength;
        this.weaponAccuracy = weaponAccuracy;
        this.weaponType = weaponType;
        this.projectileName = "projectile"; // Default value
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
    
    
    public String getProjectileName() {
        return projectileName != null ? projectileName : "projectile";
    }
    
    public void setProjectileName(String projectileName) {
        this.projectileName = projectileName;
    }
    
    
}
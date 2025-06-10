package combat;

import java.util.List;

public class Weapon {
    public String name;
    public double velocityFeetPerSecond;
    public int damage;
    public List<WeaponState> states;
    public String initialStateName;
    public int ammunition;
    public int maxAmmunition;
    public int reloadTicks;
    public ReloadType reloadType;
    public String soundFile;
    public double maximumRange;
    public int weaponAccuracy;
    public WeaponType weaponType;
    public String projectileName;

    public Weapon(String name, double velocityFeetPerSecond, int damage, int ammunition, String soundFile, double maximumRange, int weaponAccuracy, String projectileName) {
        this.name = name;
        this.velocityFeetPerSecond = velocityFeetPerSecond;
        this.damage = damage;
        this.ammunition = ammunition;
        this.maxAmmunition = ammunition; // Default max to current
        this.reloadTicks = 60; // Default reload time
        this.reloadType = ReloadType.FULL_MAGAZINE; // Default reload type
        this.soundFile = soundFile;
        this.maximumRange = maximumRange;
        this.weaponAccuracy = weaponAccuracy;
        this.weaponType = WeaponType.OTHER; // Default to OTHER
        this.projectileName = projectileName;
    }
    
    public Weapon(String name, double velocityFeetPerSecond, int damage, int ammunition, String soundFile, double maximumRange, int weaponAccuracy, String projectileName, WeaponType weaponType) {
        this.name = name;
        this.velocityFeetPerSecond = velocityFeetPerSecond;
        this.damage = damage;
        this.ammunition = ammunition;
        this.maxAmmunition = ammunition; // Default max to current
        this.reloadTicks = 60; // Default reload time
        this.reloadType = ReloadType.FULL_MAGAZINE; // Default reload type
        this.soundFile = soundFile;
        this.maximumRange = maximumRange;
        this.weaponAccuracy = weaponAccuracy;
        this.weaponType = weaponType;
        this.projectileName = projectileName;

    }

    public String getName() {
        return name;
    }

    public double getVelocityFeetPerSecond() {
        return velocityFeetPerSecond;
    }

    public int getDamage() {
        return damage;
    }
    
    public WeaponType getWeaponType() {
        return weaponType;
    }

    public WeaponState getStateByName(String name) {
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

    public String getProjectileName() { return projectileName; }
}
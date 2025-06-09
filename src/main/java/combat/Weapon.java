package combat;

import java.util.List;

public class Weapon {
    public String name;
    public double velocityFeetPerSecond;
    public int damage;
    public List<WeaponState> states;
    public String initialStateName;
    public int ammunition;
    public String soundFile;
    public double maximumRange;
    public int weaponAccuracy;
    public WeaponType weaponType;

    public Weapon(String name, double velocityFeetPerSecond, int damage, int ammunition, String soundFile, double maximumRange, int weaponAccuracy) {
        this.name = name;
        this.velocityFeetPerSecond = velocityFeetPerSecond;
        this.damage = damage;
        this.ammunition = ammunition;
        this.soundFile = soundFile;
        this.maximumRange = maximumRange;
        this.weaponAccuracy = weaponAccuracy;
        this.weaponType = WeaponType.OTHER; // Default to OTHER
    }
    
    public Weapon(String name, double velocityFeetPerSecond, int damage, int ammunition, String soundFile, double maximumRange, int weaponAccuracy, WeaponType weaponType) {
        this.name = name;
        this.velocityFeetPerSecond = velocityFeetPerSecond;
        this.damage = damage;
        this.ammunition = ammunition;
        this.soundFile = soundFile;
        this.maximumRange = maximumRange;
        this.weaponAccuracy = weaponAccuracy;
        this.weaponType = weaponType;
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
}
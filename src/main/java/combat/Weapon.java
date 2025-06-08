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

    public Weapon(String name, double velocityFeetPerSecond, int damage, int ammunition, String soundFile, double maximumRange, int weaponAccuracy) {
        this.name = name;
        this.velocityFeetPerSecond = velocityFeetPerSecond;
        this.damage = damage;
        this.ammunition = ammunition;
        this.soundFile = soundFile;
        this.maximumRange = maximumRange;
        this.weaponAccuracy = weaponAccuracy;
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
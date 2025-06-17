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
    
    // Legacy fields for backward compatibility - will be moved to subclasses later
    public double velocityFeetPerSecond;
    public double maximumRange;
    public int firingDelay;
    public FiringMode currentFiringMode;
    public List<FiringMode> availableFiringModes;
    public int cyclicRate;

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
    
    // Legacy methods for backward compatibility
    public double getVelocityFeetPerSecond() {
        return velocityFeetPerSecond;
    }
    
    public String getProjectileName() {
        return "projectile"; // Default for base class, overridden in subclasses
    }
    
    
    public void cycleFiringMode() {
        if (availableFiringModes != null && availableFiringModes.size() > 1) {
            int currentIndex = availableFiringModes.indexOf(currentFiringMode);
            int nextIndex = (currentIndex + 1) % availableFiringModes.size();
            currentFiringMode = availableFiringModes.get(nextIndex);
        }
    }
    
    public boolean hasMultipleFiringModes() {
        return availableFiringModes != null && availableFiringModes.size() > 1;
    }
    
    public String getFiringModeDisplayName() {
        if (currentFiringMode == null) return "Unknown";
        switch (currentFiringMode) {
            case SINGLE_SHOT: return "Single";
            case BURST: return "Burst";
            case FULL_AUTO: return "Auto";
            default: return "Unknown";
        }
    }
}
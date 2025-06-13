package combat;

import java.util.ArrayList;
import java.util.Arrays;
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
    public int firingDelay; // Delay in ticks between successive shots
    public double weaponLength; // Weapon length in feet
    
    // Automatic firing properties
    public FiringMode currentFiringMode;
    public List<FiringMode> availableFiringModes;
    public int cyclicRate; // Ticks between shots in full auto mode
    public int burstSize; // Number of rounds per burst (typically 3)

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
        this.firingDelay = 0; // Default no firing delay
        this.weaponLength = 1.0; // Default weapon length 1 foot
        
        // Initialize automatic firing properties (default to single shot only)
        this.availableFiringModes = new ArrayList<>(Arrays.asList(FiringMode.SINGLE_SHOT));
        this.currentFiringMode = FiringMode.SINGLE_SHOT;
        this.cyclicRate = 60; // Default 1 second between shots
        this.burstSize = 3; // Default 3-round bursts
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
        this.firingDelay = 0; // Default no firing delay
        
        // Initialize automatic firing properties (default to single shot only)
        this.availableFiringModes = new ArrayList<>(Arrays.asList(FiringMode.SINGLE_SHOT));
        this.currentFiringMode = FiringMode.SINGLE_SHOT;
        this.cyclicRate = 60; // Default 1 second between shots
        this.burstSize = 3; // Default 3-round bursts
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
    
    // Firing mode management
    public void cycleFiringMode() {
        if (availableFiringModes.size() <= 1) return; // No modes to cycle
        
        int currentIndex = availableFiringModes.indexOf(currentFiringMode);
        int nextIndex = (currentIndex + 1) % availableFiringModes.size();
        currentFiringMode = availableFiringModes.get(nextIndex);
    }
    
    public boolean hasMultipleFiringModes() {
        return availableFiringModes.size() > 1;
    }
    
    public String getFiringModeDisplayName() {
        switch (currentFiringMode) {
            case SINGLE_SHOT: return "Single";
            case BURST: return "Burst";
            case FULL_AUTO: return "Auto";
            default: return "Unknown";
        }
    }
    
    // Weapon length property accessors
    public double getWeaponLength() {
        return weaponLength;
    }
    
    public void setWeaponLength(double length) {
        if (length > 0) {
            this.weaponLength = length;
        }
    }
}
package combat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Ranged weapon implementation with ammunition, velocity, and firing modes.
 * Extends the abstract Weapon base class with ranged-specific properties.
 */
public class RangedWeapon extends Weapon {
    // Ranged-specific properties - public for backward compatibility
    public double velocityFeetPerSecond;
    public int ammunition;
    public int maxAmmunition;
    public int reloadTicks;
    public ReloadType reloadType;
    public double maximumRange;
    public String projectileName;
    public int firingDelay; // Delay in ticks between successive shots
    
    // Automatic firing properties - public for backward compatibility
    public FiringMode currentFiringMode;
    public List<FiringMode> availableFiringModes;
    public int cyclicRate; // Ticks between shots in full auto mode
    public int burstSize; // Number of rounds per burst (typically 3)

    public RangedWeapon(String name, double velocityFeetPerSecond, int damage, int ammunition, String soundFile, double maximumRange, int weaponAccuracy, String projectileName) {
        super(name, damage, soundFile, 1.0, weaponAccuracy, WeaponType.OTHER); // Default weapon length 1 foot
        this.velocityFeetPerSecond = velocityFeetPerSecond;
        this.ammunition = ammunition;
        this.maxAmmunition = ammunition; // Default max to current
        this.reloadTicks = 60; // Default reload time
        this.reloadType = ReloadType.FULL_MAGAZINE; // Default reload type
        this.maximumRange = maximumRange;
        this.projectileName = projectileName;
        this.firingDelay = 0; // Default no firing delay
        
        // Initialize automatic firing properties (default to single shot only)
        this.availableFiringModes = new ArrayList<>(Arrays.asList(FiringMode.SINGLE_SHOT));
        this.currentFiringMode = FiringMode.SINGLE_SHOT;
        this.cyclicRate = 60; // Default 1 second between shots
        this.burstSize = 3; // Default 3-round bursts
    }
    
    public RangedWeapon(String name, double velocityFeetPerSecond, int damage, int ammunition, String soundFile, double maximumRange, int weaponAccuracy, String projectileName, WeaponType weaponType) {
        super(name, damage, soundFile, 1.0, weaponAccuracy, weaponType);
        this.velocityFeetPerSecond = velocityFeetPerSecond;
        this.ammunition = ammunition;
        this.maxAmmunition = ammunition; // Default max to current
        this.reloadTicks = 60; // Default reload time
        this.reloadType = ReloadType.FULL_MAGAZINE; // Default reload type
        this.maximumRange = maximumRange;
        this.projectileName = projectileName;
        this.firingDelay = 0; // Default no firing delay
        
        // Initialize automatic firing properties (default to single shot only)
        this.availableFiringModes = new ArrayList<>(Arrays.asList(FiringMode.SINGLE_SHOT));
        this.currentFiringMode = FiringMode.SINGLE_SHOT;
        this.cyclicRate = 60; // Default 1 second between shots
        this.burstSize = 3; // Default 3-round bursts
    }

    // Ranged-specific getter methods
    public double getVelocityFeetPerSecond() {
        return velocityFeetPerSecond;
    }
    
    public int getAmmunition() {
        return ammunition;
    }
    
    public void setAmmunition(int ammunition) {
        this.ammunition = ammunition;
    }
    
    public int getMaxAmmunition() {
        return maxAmmunition;
    }
    
    public void setMaxAmmunition(int maxAmmunition) {
        this.maxAmmunition = maxAmmunition;
    }
    
    public int getReloadTicks() {
        return reloadTicks;
    }
    
    public void setReloadTicks(int reloadTicks) {
        this.reloadTicks = reloadTicks;
    }
    
    public ReloadType getReloadType() {
        return reloadType;
    }
    
    public void setReloadType(ReloadType reloadType) {
        this.reloadType = reloadType;
    }
    
    public double getMaximumRange() {
        return maximumRange;
    }
    
    public String getProjectileName() {
        return projectileName;
    }
    
    public int getFiringDelay() {
        return firingDelay;
    }
    
    public void setFiringDelay(int firingDelay) {
        this.firingDelay = firingDelay;
    }
    
    public FiringMode getCurrentFiringMode() {
        return currentFiringMode;
    }
    
    public List<FiringMode> getAvailableFiringModes() {
        return availableFiringModes;
    }
    
    public void setAvailableFiringModes(List<FiringMode> availableFiringModes) {
        this.availableFiringModes = availableFiringModes;
    }
    
    public int getCyclicRate() {
        return cyclicRate;
    }
    
    public void setCyclicRate(int cyclicRate) {
        this.cyclicRate = cyclicRate;
    }
    
    public int getBurstSize() {
        return burstSize;
    }
    
    public void setBurstSize(int burstSize) {
        this.burstSize = burstSize;
    }
    
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
}
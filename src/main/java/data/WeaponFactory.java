package data;

import combat.Weapon;
import combat.WeaponState;
import combat.WeaponType;

import java.util.ArrayList;

public class WeaponFactory {
    private static final DataManager dataManager = DataManager.getInstance();
    
    public static Weapon createWeapon(String weaponId) {
        WeaponData weaponData = dataManager.getWeapon(weaponId);
        if (weaponData == null) {
            throw new IllegalArgumentException("Unknown weapon ID: " + weaponId);
        }
        
        WeaponTypeData weaponTypeData = dataManager.getWeaponType(weaponData.type);
        if (weaponTypeData == null) {
            throw new IllegalArgumentException("Unknown weapon type: " + weaponData.type);
        }
        
        // Create the weapon with basic properties
        Weapon weapon = new Weapon(
            weaponData.name,
            weaponData.velocity,
            weaponData.damage,
            weaponData.ammunition,
            weaponData.soundFile,
            weaponData.maximumRange,
            weaponData.weaponAccuracy,
            weaponData.projectileName,
            weaponData.type
        );
        
        // Set reload properties from data
        weapon.maxAmmunition = weaponData.maxAmmunition;
        weapon.reloadTicks = weaponData.reloadTicks;
        weapon.reloadType = weaponData.reloadType;
        weapon.firingDelay = weaponData.firingDelay;
        
        // Set automatic firing properties from data
        weapon.cyclicRate = weaponData.cyclicRate;
        weapon.burstSize = weaponData.burstSize;
        weapon.availableFiringModes = new ArrayList<>(weaponData.availableFiringModes);
        weapon.currentFiringMode = weaponData.availableFiringModes.get(0); // Default to first available mode
        
        // Set up the weapon states from the weapon type definition
        weapon.states = new ArrayList<>();
        for (WeaponStateData stateData : weaponTypeData.states) {
            weapon.states.add(new WeaponState(stateData.state, stateData.action, stateData.ticks));
        }
        
        weapon.initialStateName = weaponTypeData.initialState;
        
        return weapon;
    }
    
    // Utility methods for weapon management
    public static boolean isValidWeaponId(String weaponId) {
        return dataManager.hasWeapon(weaponId);
    }
    
    public static String[] getAllWeaponIds() {
        return dataManager.getAllWeapons().keySet().toArray(new String[0]);
    }
    
    public static WeaponData getWeaponData(String weaponId) {
        return dataManager.getWeapon(weaponId);
    }
}
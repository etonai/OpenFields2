package combat;

import data.DataManager;
import data.MeleeWeaponData;

/**
 * Factory for creating standard melee weapons with balanced stats.
 * Provides common melee weapons for testing and basic gameplay.
 */
public class MeleeWeaponFactory {
    
    /**
     * Create an unarmed combat "weapon" - represents fighting without weapons
     */
    public static MeleeWeapon createUnarmed() {
        return new MeleeWeapon(
            "Unarmed",
            8, // Lower damage than weapons
            "punch.wav", // Default sound
            MeleeWeaponType.UNARMED,
            25 // Lower accuracy than weapons
        );
    }
    
    /**
     * Create a basic knife - short, fast, one-handed
     */
    public static MeleeWeapon createKnife() {
        return new MeleeWeapon(
            "Knife",
            15,
            "blade_swing.wav",
            MeleeWeaponType.SHORT,
            35
        );
    }
    
    /**
     * Create a tomahawk - medium reach, balanced weapon
     */
    public static MeleeWeapon createTomahawk() {
        return new MeleeWeapon(
            "Tomahawk",
            22,
            "axe_swing.wav",
            MeleeWeaponType.MEDIUM,
            40
        );
    }
    
    /**
     * Create a rifle with bayonet for melee combat
     */
    public static MeleeWeapon createRifleBayonet() {
        MeleeWeapon bayonet = new MeleeWeapon(
            "Rifle (Bayonet)",
            20,
            "bayonet_thrust.wav",
            MeleeWeaponType.LONG,
            45
        );
        bayonet.setMeleeVersionOfRanged(true); // This is the melee version of a ranged weapon
        return bayonet;
    }
    
    /**
     * Create a sabre - medium weapon, good balance of speed and damage
     */
    public static MeleeWeapon createSabre() {
        return new MeleeWeapon(
            "Sabre",
            25,
            "sword_swing.wav",
            MeleeWeaponType.MEDIUM,
            50
        );
    }
    
    /**
     * Create a pistol for pistol-whipping (crude melee use)
     */
    public static MeleeWeapon createPistolWhip() {
        MeleeWeapon pistolWhip = new MeleeWeapon(
            "Pistol (Melee)",
            12, // Low damage - not designed for melee
            "pistol_whip.wav",
            MeleeWeaponType.SHORT,
            25 // Low accuracy - poor melee weapon
        );
        pistolWhip.setMeleeVersionOfRanged(true);
        return pistolWhip;
    }
    
    /**
     * Create a dual-weapon setup (knife and tomahawk example)
     */
    public static MeleeWeapon createDualWeapons() {
        MeleeWeapon dualWeapons = new MeleeWeapon(
            "Knife & Tomahawk",
            28, // Higher damage due to two weapons
            "dual_weapon_swing.wav",
            MeleeWeaponType.TWO_WEAPON,
            42
        );
        // Dual weapons have moderate speed but good damage
        dualWeapons.setAttackSpeed(105); // 1.75 seconds
        dualWeapons.setAttackCooldown(105);
        return dualWeapons;
    }
    
    /**
     * Create a melee weapon from JSON data by ID
     */
    public static MeleeWeapon createWeapon(String meleeWeaponId) {
        DataManager dataManager = DataManager.getInstance();
        MeleeWeaponData data = dataManager.getMeleeWeapon(meleeWeaponId);
        
        if (data == null) {
            System.err.println("Melee weapon not found: " + meleeWeaponId + ". Falling back to unarmed.");
            return createUnarmed();
        }
        
        return new MeleeWeapon(
            data.name,
            data.damage,
            data.soundFile,
            data.meleeType,
            data.defendScore,
            data.attackSpeed,
            data.attackCooldown,
            data.weaponLength,
            data.readyingTime,
            data.isOneHanded,
            data.isMeleeVersionOfRanged,
            data.weaponAccuracy
        );
    }

    /**
     * Get a default melee weapon for a character (unarmed combat)
     */
    public static MeleeWeapon getDefaultMeleeWeapon() {
        return createUnarmed();
    }
    
    /**
     * Create a melee weapon by name for testing and configuration
     */
    public static MeleeWeapon createByName(String weaponName) {
        switch (weaponName.toLowerCase()) {
            case "unarmed":
                return createUnarmed();
            case "knife":
                return createKnife();
            case "tomahawk":
                return createTomahawk();
            case "bayonet":
            case "rifle_bayonet":
                return createRifleBayonet();
            case "sabre":
                return createSabre();
            case "pistol_whip":
                return createPistolWhip();
            case "dual_weapons":
                return createDualWeapons();
            default:
                return createUnarmed(); // Default fallback
        }
    }
}
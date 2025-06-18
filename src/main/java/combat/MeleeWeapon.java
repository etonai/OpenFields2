package combat;

/**
 * Melee weapon implementation with reach, defense, and attack timing.
 * Extends the abstract Weapon base class with melee-specific properties.
 */
public class MeleeWeapon extends Weapon {
    // Melee-specific properties
    private MeleeWeaponType meleeType;
    private int defendScore; // 1-100 defensive capability
    private int attackSpeed; // ticks to perform attack
    private int attackCooldown; // ticks before next attack
    private double weaponRange; // weapon range in feet (based on weaponLength)
    private int readyingTime; // ticks to ready weapon
    private boolean isOneHanded; // true for one-handed weapons
    private boolean isMeleeVersionOfRanged; // true if melee version of ranged weapon

    public MeleeWeapon(String name, int damage, String soundFile, MeleeWeaponType meleeType, 
                       int defendScore, int attackSpeed, int attackCooldown, double weaponLength, 
                       int readyingTime, boolean isOneHanded, boolean isMeleeVersionOfRanged, int weaponAccuracy) {
        super(name, damage, soundFile, weaponLength, weaponAccuracy, getWeaponTypeForMeleeType(meleeType));
        this.meleeType = meleeType;
        this.defendScore = defendScore;
        this.attackSpeed = attackSpeed;
        this.attackCooldown = attackCooldown;
        this.weaponRange = weaponLength; // Range equals weapon length for melee weapons
        this.readyingTime = readyingTime;
        this.isOneHanded = isOneHanded;
        this.isMeleeVersionOfRanged = isMeleeVersionOfRanged;
        
        // Debug output for range validation
        debugPrint("[MELEE-RANGE] " + name + " (" + meleeType + ") total reach: " + String.format("%.1f", getTotalReach()) + " feet (4.0 base + " + String.format("%.1f", weaponLength) + " weapon)");
    }
    
    /**
     * Constructor using default values based on melee weapon type
     */
    public MeleeWeapon(String name, int damage, String soundFile, MeleeWeaponType meleeType, int weaponAccuracy) {
        super(name, damage, soundFile, meleeType.getDefaultReach(), weaponAccuracy, getWeaponTypeForMeleeType(meleeType));
        this.meleeType = meleeType;
        this.weaponRange = meleeType.getDefaultReach();
        this.isOneHanded = meleeType.isTypicallyOneHanded();
        this.isMeleeVersionOfRanged = false;
        
        // Set default values based on weapon type
        setDefaultValuesForType(meleeType);
        
        // Debug output for range validation
        debugPrint("[MELEE-RANGE] " + name + " (" + meleeType + ") total reach: " + String.format("%.1f", getTotalReach()) + " feet (4.0 base + " + String.format("%.1f", weaponRange) + " weapon)");
    }
    
    /**
     * Set default values based on melee weapon type
     */
    private void setDefaultValuesForType(MeleeWeaponType type) {
        switch (type) {
            case UNARMED:
                this.defendScore = 30; // Lower defense without weapon
                this.attackSpeed = 120; // 2 seconds
                this.attackCooldown = 120; // 2 seconds
                this.readyingTime = 0; // Always ready
                break;
            case SHORT:
                this.defendScore = 40;
                this.attackSpeed = 90; // 1.5 seconds - faster than larger weapons
                this.attackCooldown = 90;
                this.readyingTime = 60; // 1 second
                break;
            case MEDIUM:
                this.defendScore = 60;
                this.attackSpeed = 120; // 2 seconds
                this.attackCooldown = 120;
                this.readyingTime = 90; // 1.5 seconds
                break;
            case LONG:
                this.defendScore = 80; // Better defense with longer reach
                this.attackSpeed = 150; // 2.5 seconds - slower due to size
                this.attackCooldown = 150;
                this.readyingTime = 120; // 2 seconds
                break;
            case TWO_WEAPON:
                this.defendScore = 70; // Good defense with multiple weapons
                this.attackSpeed = 105; // 1.75 seconds - moderate speed
                this.attackCooldown = 105;
                this.readyingTime = 90; // 1.5 seconds
                break;
            default:
                this.defendScore = 50;
                this.attackSpeed = 120;
                this.attackCooldown = 120;
                this.readyingTime = 90;
                break;
        }
    }

    // Melee-specific getter methods
    public MeleeWeaponType getMeleeType() {
        return meleeType;
    }
    
    public int getDefendScore() {
        return defendScore;
    }
    
    public void setDefendScore(int defendScore) {
        this.defendScore = Math.max(1, Math.min(100, defendScore)); // Clamp to 1-100
    }
    
    public int getAttackSpeed() {
        return attackSpeed;
    }
    
    public void setAttackSpeed(int attackSpeed) {
        this.attackSpeed = Math.max(1, attackSpeed);
    }
    
    public int getAttackCooldown() {
        return attackCooldown;
    }
    
    public void setAttackCooldown(int attackCooldown) {
        this.attackCooldown = Math.max(0, attackCooldown);
    }
    
    public double getWeaponRange() {
        return weaponRange;
    }
    
    public void setWeaponRange(double weaponRange) {
        this.weaponRange = Math.max(0, weaponRange);
    }
    
    public int getReadyingTime() {
        return readyingTime;
    }
    
    public void setReadyingTime(int readyingTime) {
        this.readyingTime = Math.max(0, readyingTime);
    }
    
    public boolean isOneHanded() {
        return isOneHanded;
    }
    
    public void setOneHanded(boolean isOneHanded) {
        this.isOneHanded = isOneHanded;
    }
    
    public boolean isMeleeVersionOfRanged() {
        return isMeleeVersionOfRanged;
    }
    
    public void setMeleeVersionOfRanged(boolean isMeleeVersionOfRanged) {
        this.isMeleeVersionOfRanged = isMeleeVersionOfRanged;
    }
    
    /**
     * Calculate total reach including minimum engagement range + weapon length.
     * Uses 4-foot minimum to account for combat stance, character reach, and tactical positioning.
     * This provides realistic melee combat distances and ensures weapon viability.
     */
    public double getTotalReach() {
        return 4.0 + weaponRange; // Minimum engagement range + weapon length
    }
    
    /**
     * Get total attack cycle time (attack + cooldown)
     */
    public int getTotalAttackCycle() {
        return attackSpeed + attackCooldown;
    }
    
    /**
     * Check if target is within melee range
     */
    public boolean isInRange(double distanceToTarget) {
        return distanceToTarget <= getTotalReach();
    }
    
    /**
     * Debug print helper that only outputs when in debug mode
     */
    private void debugPrint(String message) {
        try {
            Class<?> gameRendererClass = Class.forName("GameRenderer");
            java.lang.reflect.Method isDebugModeMethod = gameRendererClass.getMethod("isDebugMode");
            boolean isDebugMode = (Boolean) isDebugModeMethod.invoke(null);
            if (isDebugMode) {
                System.out.println(message);
            }
        } catch (Exception e) {
            // Silent fail for safety
        }
    }
    
    /**
     * Map MeleeWeaponType to WeaponType for weapon state management
     */
    private static WeaponType getWeaponTypeForMeleeType(MeleeWeaponType meleeType) {
        switch (meleeType) {
            case UNARMED:
                return WeaponType.MELEE_UNARMED;
            case SHORT:
                return WeaponType.MELEE_SHORT;  
            case MEDIUM:
                return WeaponType.MELEE_MEDIUM;
            case LONG:
                return WeaponType.MELEE_LONG;
            case TWO_WEAPON:
                return WeaponType.MELEE_MEDIUM; // Treat dual weapons as medium
            default:
                return WeaponType.MELEE_UNARMED;
        }
    }
}
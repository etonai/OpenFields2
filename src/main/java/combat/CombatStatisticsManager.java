package combat;

import combat.managers.BurstFireManager;

/**
 * Combat statistics manager for Character combat performance tracking.
 * Extracted from Character class as part of DevCycle 24 refactoring.
 * 
 * Handles all combat statistics calculations including accuracy, wounds inflicted,
 * headshot statistics, and firing mode management.
 */
public class CombatStatisticsManager {
    
    // ========================================
    // WOUND STATISTICS CALCULATIONS
    // ========================================
    
    /**
     * Returns total wounds inflicted across all severity levels
     * @param character Character to get statistics from
     * @return Sum of all wounds inflicted by type
     */
    public static int getTotalWoundsInflicted(Character character) {
        return character.woundsInflictedScratch + character.woundsInflictedLight + 
               character.woundsInflictedSerious + character.woundsInflictedCritical;
    }
    
    /**
     * Returns wounds inflicted by specific severity type
     * @param character Character to get statistics from
     * @param severity Wound severity level to query
     * @return Number of wounds inflicted of specified severity
     */
    public static int getWoundsInflictedByType(Character character, WoundSeverity severity) {
        switch (severity) {
            case SCRATCH: return character.woundsInflictedScratch;
            case LIGHT: return character.woundsInflictedLight;
            case SERIOUS: return character.woundsInflictedSerious;
            case CRITICAL: return character.woundsInflictedCritical;
            default: return 0;
        }
    }
    
    // ========================================
    // ACCURACY CALCULATIONS
    // ========================================
    
    /**
     * Calculates overall accuracy percentage
     * @param character Character to get statistics from
     * @return Accuracy percentage (0.0 to 100.0), or 0.0 if no attacks attempted
     */
    public static double getAccuracyPercentage(Character character) {
        return character.attacksAttempted > 0 ? 
            (character.attacksSuccessful * 100.0 / character.attacksAttempted) : 0.0;
    }
    
    // ========================================
    // HEADSHOT STATISTICS
    // ========================================
    
    /**
     * Returns number of headshots attempted
     * @param character Character to get statistics from
     * @return Total headshots attempted
     */
    public static int getHeadshotsAttempted(Character character) {
        return character.headshotsAttempted;
    }
    
    /**
     * Returns number of successful headshots
     * @param character Character to get statistics from
     * @return Total successful headshots
     */
    public static int getHeadshotsSuccessful(Character character) {
        return character.headshotsSuccessful;
    }
    
    /**
     * Calculates headshot accuracy percentage
     * @param character Character to get statistics from
     * @return Headshot accuracy percentage (0.0 to 100.0), or 0.0 if no headshots attempted
     */
    public static double getHeadshotAccuracyPercentage(Character character) {
        return character.headshotsAttempted > 0 ? 
            (character.headshotsSuccessful * 100.0 / character.headshotsAttempted) : 0.0;
    }
    
    /**
     * Returns number of incapacitations from headshots
     * @param character Character to get statistics from
     * @return Total incapacitations from headshots
     */
    public static int getHeadshotIncapacitations(Character character) {
        return character.headshotIncapacitations;
    }
    
    // ========================================
    // FIRING MODE MANAGEMENT
    // ========================================
    
    /**
     * Cycles through available firing modes for character's weapon
     * @param character Character whose weapon firing mode to cycle
     */
    public static void cycleFiringMode(Character character) {
        if (character.weapon != null && character.weapon instanceof RangedWeapon) {
            // Interrupt burst/auto firing when switching modes
            if (BurstFireManager.getInstance().isAutomaticFiring(character.id)) {
                BurstFireManager.getInstance().setAutomaticFiring(character.id, false);
                BurstFireManager.getInstance().setBurstShotsFired(character.id, 0);
                System.out.println(character.getDisplayName() + " burst/auto firing interrupted by mode switch");
            }
            ((RangedWeapon)character.weapon).cycleFiringMode();
        }
    }
    
    /**
     * Gets the current firing mode display name
     * @param character Character to get firing mode from
     * @return Current firing mode display name, or "N/A" if no ranged weapon
     */
    public static String getCurrentFiringMode(Character character) {
        if (character.weapon != null && character.weapon instanceof RangedWeapon) {
            return ((RangedWeapon)character.weapon).getFiringModeDisplayName();
        }
        return "N/A";
    }
}
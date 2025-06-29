package combat.managers;

import combat.AimingSpeed;
import combat.Character;
import combat.AccumulatedAimingBonus;
import combat.WeaponState;
import combat.WeaponType;
import utils.GameConstants;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton manager for aiming mechanics and time-based accuracy bonuses.
 * Tracks aiming duration and calculates accuracy bonuses based on time spent aiming.
 */
public class AimingSystem implements IAimingSystem {
    
    private static AimingSystem instance;
    
    // Per-character state tracking
    private final Map<Integer, Long> aimingStartTicks = new HashMap<>();
    private final Map<Integer, Long> pointingFromHipStartTicks = new HashMap<>();
    
    /**
     * Private constructor for singleton pattern.
     */
    private AimingSystem() {
        // Private constructor for singleton
    }
    
    /**
     * Get the singleton instance of AimingSystem.
     * 
     * @return The manager instance
     */
    public static AimingSystem getInstance() {
        if (instance == null) {
            instance = new AimingSystem();
        }
        return instance;
    }
    
    @Override
    public void startAimingTiming(int characterId, long currentTick) {
        aimingStartTicks.put(characterId, currentTick);
        pointingFromHipStartTicks.remove(characterId); // Clear pointing timing
    }
    
    @Override
    public void startPointingFromHipTiming(int characterId, long currentTick) {
        pointingFromHipStartTicks.put(characterId, currentTick);
        aimingStartTicks.remove(characterId); // Clear aiming timing
    }
    
    @Override
    public long getAimingDuration(int characterId, long currentTick) {
        Long startTick = aimingStartTicks.get(characterId);
        if (startTick == null || startTick < 0) {
            return 0;
        }
        return currentTick - startTick;
    }
    
    @Override
    public long getPointingFromHipDuration(int characterId, long currentTick) {
        Long startTick = pointingFromHipStartTicks.get(characterId);
        if (startTick == null || startTick < 0) {
            return 0;
        }
        return currentTick - startTick;
    }
    
    @Override
    public int calculateEarnedAimingBonusModifier(Character character, long aimingDuration) {
        AccumulatedAimingBonus bonus = calculateAccumulatedAimingBonus(character, aimingDuration);
        return (int) bonus.getAccuracyModifier();
    }
    
    @Override
    public AimingSpeed determineAimingSpeedForShot(Character character, int shotInSequence) {
        // For multiple shot sequences
        if (character.multipleShootCount > 1 && shotInSequence > 0) {
            return getAimingSpeedForMultipleShot(character);
        }
        
        // For single shots, use character's current aiming speed
        return character.getCurrentAimingSpeed();
    }
    
    @Override
    public AimingSpeed getAimingSpeedForMultipleShot(Character character) {
        if (character.currentShotInSequence <= 0 || character.currentShotInSequence == 1) {
            // 1st shot always uses character's current aiming speed
            return character.getCurrentAimingSpeed();
        } else {
            // All subsequent shots (2nd, 3rd, 4th, 5th) use Quick
            return AimingSpeed.QUICK;
        }
    }
    
    @Override
    public void resetAimingTiming(int characterId) {
        aimingStartTicks.remove(characterId);
    }
    
    @Override
    public void resetPointingFromHipTiming(int characterId) {
        pointingFromHipStartTicks.remove(characterId);
    }
    
    @Override
    public boolean isAimingTimingActive(int characterId) {
        Long startTick = aimingStartTicks.get(characterId);
        return startTick != null && startTick >= 0;
    }
    
    @Override
    public boolean isPointingFromHipTimingActive(int characterId) {
        Long startTick = pointingFromHipStartTicks.get(characterId);
        return startTick != null && startTick >= 0;
    }
    
    @Override
    public long getCurrentAimingDuration(Character character, long currentTick) {
        if (character.getFiresFromAimingState()) {
            return getAimingDuration(character.id, currentTick);
        } else {
            return getPointingFromHipDuration(character.id, currentTick);
        }
    }
    
    @Override
    public double calculateAimingSpeedMultiplier(Character character) {
        // Apply 25% of the weapon ready speed bonus to aiming
        double weaponReadyMultiplier = calculateWeaponReadySpeedMultiplier(character);
        double speedBonus = 1.0 - weaponReadyMultiplier;
        double aimingSpeedBonus = speedBonus * 0.25;
        return 1.0 - aimingSpeedBonus;
    }
    
    @Override
    public void toggleFiringPreference(Character character, long currentTick) {
        boolean oldPreference = character.getFiresFromAimingState();
        character.setFiresFromAimingState(!character.getFiresFromAimingState());
        handleFiringPreferenceStateAdjustment(character, oldPreference, currentTick);
    }
    
    @Override
    public void handleFiringPreferenceStateAdjustment(Character character, boolean oldPreference, long currentTick) {
        String currentState = getCurrentWeaponStateName(character);
        
        // Handle immediate state adjustments based on current weapon state
        if ("pointedfromhip".equals(currentState) && character.getFiresFromAimingState()) {
            // Was at pointedfromhip, now prefers aiming
            character.currentWeaponState = findWeaponState(character, "aiming");
            resetPointingFromHipTiming(character.id);
            startAimingTiming(character.id, currentTick);
        } else if ("aiming".equals(currentState) && !character.getFiresFromAimingState()) {
            // Was at aiming, now prefers pointedfromhip
            character.currentWeaponState = findWeaponState(character, "pointedfromhip");
            resetAimingTiming(character.id);
            startPointingFromHipTiming(character.id, currentTick);
        }
        
        // For other states, preference will affect next progression
    }
    
    @Override
    public boolean canUseVeryCarefulAiming(Character character) {
        if (character.weapon == null) {
            return false;
        }
        
        WeaponType weaponType = character.weapon.getWeaponType();
        
        // Check skill requirements based on weapon type
        switch (weaponType) {
            case PISTOL:
                return character.getSkillLevel("Pistol") >= 1;
            case RIFLE:
                return character.getSkillLevel("Rifle") >= 1;
            case SUBMACHINE_GUN:
                return character.getSkillLevel("Submachine Gun") >= 1;
            default:
                return false; // OTHER and melee weapons don't support very careful aiming
        }
    }
    
    @Override
    public void increaseAimingSpeed(Character character) {
        AimingSpeed current = character.getCurrentAimingSpeed();
        switch (current) {
            case QUICK:
                character.setCurrentAimingSpeed(AimingSpeed.NORMAL);
                break;
            case NORMAL:
                character.setCurrentAimingSpeed(AimingSpeed.CAREFUL);
                break;
            case CAREFUL:
                if (canUseVeryCarefulAiming(character)) {
                    character.setCurrentAimingSpeed(AimingSpeed.VERY_CAREFUL);
                }
                break;
            case VERY_CAREFUL:
                // Already at maximum
                break;
        }
    }
    
    @Override
    public void decreaseAimingSpeed(Character character) {
        AimingSpeed current = character.getCurrentAimingSpeed();
        switch (current) {
            case VERY_CAREFUL:
                character.setCurrentAimingSpeed(AimingSpeed.CAREFUL);
                break;
            case CAREFUL:
                character.setCurrentAimingSpeed(AimingSpeed.NORMAL);
                break;
            case NORMAL:
                character.setCurrentAimingSpeed(AimingSpeed.QUICK);
                break;
            case QUICK:
                // Already at minimum
                break;
        }
    }
    
    @Override
    public AccumulatedAimingBonus calculateEarnedAimingBonus(Character character, long currentTick) {
        // Get current aiming duration based on firing preference
        long accumulatedTime = getCurrentAimingDuration(character, currentTick);
        
        // Determine bonus based on accumulated time and weapon state
        return calculateAccumulatedAimingBonus(character, accumulatedTime);
    }
    
    @Override
    public void cleanupCharacter(int characterId) {
        aimingStartTicks.remove(characterId);
        pointingFromHipStartTicks.remove(characterId);
    }
    
    // Private helper methods
    
    /**
     * Calculate accumulated aiming bonus based on time spent aiming at target.
     * DevCycle 27: System 3 - Accumulated Aiming Time Bonus System
     */
    private AccumulatedAimingBonus calculateAccumulatedAimingBonus(Character character, long accumulatedTime) {
        if (!isInAimingOrPointingState(character)) {
            return AccumulatedAimingBonus.NONE;
        }
        
        long baseTime = getCurrentWeaponAimingStateTicks(character);
        
        // Apply weapon ready speed multiplier to thresholds (faster chars earn bonuses quicker)
        double weaponReadySpeedMultiplier = calculateAimingSpeedMultiplier(character);
        long adjustedBaseTime = Math.round(baseTime * weaponReadySpeedMultiplier);
        
        // For pointing-from-hip, cap at NORMAL
        if (isPointingFromHip(character)) {
            return accumulatedTime >= adjustedBaseTime ? AccumulatedAimingBonus.NORMAL : AccumulatedAimingBonus.NONE;
        }
        
        // For aiming state, full progression available
        if (accumulatedTime >= adjustedBaseTime * 3) return AccumulatedAimingBonus.VERY_CAREFUL;
        if (accumulatedTime >= adjustedBaseTime * 2) return AccumulatedAimingBonus.CAREFUL;
        if (accumulatedTime >= adjustedBaseTime) return AccumulatedAimingBonus.NORMAL;
        return AccumulatedAimingBonus.NONE;
    }
    
    /**
     * Get the base aiming time for the current weapon from its state data.
     */
    private long getCurrentWeaponAimingStateTicks(Character character) {
        // Get base aiming time from current weapon's state data
        WeaponState aimingState = findWeaponState(character, "aiming");
        return aimingState != null ? aimingState.ticks : 30; // Default 30 if not found
    }
    
    /**
     * Check if character is currently in aiming or pointing-from-hip state.
     */
    private boolean isInAimingOrPointingState(Character character) {
        String currentState = getCurrentWeaponStateName(character);
        return "aiming".equals(currentState) || "pointedfromhip".equals(currentState);
    }
    
    /**
     * Check if character is currently in pointing-from-hip state.
     */
    private boolean isPointingFromHip(Character character) {
        return "pointedfromhip".equals(getCurrentWeaponStateName(character));
    }
    
    /**
     * Find a weapon state by name from the character's current weapon.
     */
    private WeaponState findWeaponState(Character character, String stateName) {
        if (character.weapon != null && character.weapon.states != null) {
            for (WeaponState state : character.weapon.states) {
                if (stateName.equals(state.state)) {
                    return state;
                }
            }
        }
        return null;
    }
    
    /**
     * Get the current weapon state name.
     */
    private String getCurrentWeaponStateName(Character character) {
        return character.currentWeaponState != null ? character.currentWeaponState.state : "";
    }
    
    
    /**
     * Calculate weapon ready speed multiplier based on reflexes and quickdraw skill.
     */
    private double calculateWeaponReadySpeedMultiplier(Character character) {
        // Get reflex modifier (-20 to +20)
        int reflexMod = GameConstants.statToModifier(character.reflexes);
        
        // Convert modifier to speed multiplier (1.2x slower to 0.8x faster)
        // -20 mod = 1.2x slower, +20 mod = 0.8x faster
        double reflexMultiplier = 1.0 - (reflexMod * 0.01);
        
        // Get quickdraw skill level (default 0 if no skill)
        int quickdrawLevel = character.getSkillLevel("Quickdraw");
        
        // Each level provides 5% speed improvement (level 4 = 20% faster = 0.8x multiplier)
        double quickdrawMultiplier = 1.0 - (quickdrawLevel * 0.05);
        
        // Combine multipliers
        return reflexMultiplier * quickdrawMultiplier;
    }
}
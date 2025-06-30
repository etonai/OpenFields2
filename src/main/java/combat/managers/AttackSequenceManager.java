package combat.managers;

import combat.Character;
import combat.AimingSpeed;
import combat.AccumulatedAimingBonus;
import combat.WeaponState;
import game.interfaces.IUnit;
import game.ScheduledEvent;
import game.GameCallbacks;

/**
 * AttackSequenceManager handles attack sequence orchestration for characters.
 * Extracted from Character.java following DevCycle 31 Option 4 refactoring.
 * Manages complex combat flow with weapon state management and timing calculations.
 */
public class AttackSequenceManager {
    
    // Singleton instance
    private static AttackSequenceManager instance;
    
    /**
     * Private constructor for singleton pattern.
     */
    private AttackSequenceManager() {
    }
    
    /**
     * Get the singleton instance of AttackSequenceManager.
     * 
     * @return The manager instance
     */
    public static AttackSequenceManager getInstance() {
        if (instance == null) {
            instance = new AttackSequenceManager();
        }
        return instance;
    }
    
    /**
     * Schedule attack from character's current state.
     * Extracted from Character.scheduleAttackFromCurrentState() (~91 lines).
     * Handles complex combat flow orchestration with weapon state management and timing.
     * 
     * @param character The attacking character
     * @param shooter The attacking unit
     * @param target The target unit
     * @param currentTick The current game tick
     * @param eventQueue Event queue for scheduling future actions
     * @param ownerId Owner ID for event scheduling
     * @param gameCallbacks Game callback interface
     */
    public void scheduleAttackFromCurrentState(Character character, IUnit shooter, IUnit target, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Debug: Check weapon and weapon state
        System.out.println("*** " + character.getDisplayName() + " scheduleAttackFromCurrentState: weapon=" + 
                          (character.weapon != null ? character.weapon.getName() : "null") + 
                          ", currentWeaponState=" + (character.currentWeaponState != null ? character.currentWeaponState.getState() : "null") + " ***");
        
        if (character.weapon == null || character.currentWeaponState == null) {
            // Initialize weapon state if missing
            if (character.weapon != null && character.currentWeaponState == null) {
                character.currentWeaponState = character.weapon.getInitialState();
                System.out.println("*** " + character.getDisplayName() + " initialized weapon state to: " + 
                                  (character.currentWeaponState != null ? character.currentWeaponState.getState() : "null") + " ***");
            }
            if (character.currentWeaponState == null) return;
        }
        
        String currentState = character.currentWeaponState.getState();
        
        // Prevent scheduling attacks if weapon is still firing or recovering
        if ("firing".equals(currentState) || "recovering".equals(currentState)) {
            return;
        }

        // EDTODO: Verify this is no longer needed
        // long totalTimeToFire = calculateTimeToFire();
        
        // Use JSON-driven state progression for all states except aiming and firing
        if (!"aiming".equals(currentState) && !"firing".equals(currentState)) {
            // Check if we should stop at pointedfromhip based on firing preference
            if ("pointedfromhip".equals(currentState) && !character.getFiresFromAimingState()) {
                // Point-from-hip firing preference - fire from this state
                // Continue to aiming logic below to handle firing
            } else {
                // Find the next state in the weapon's progression using the action field
                String nextState = character.currentWeaponState.getAction();
                if (nextState != null && !nextState.isEmpty()) {
                    // Check if the next state is available in the weapon
                    WeaponState nextWeaponState = character.weapon.getStateByName(nextState);
                    if (nextWeaponState != null) {
                        // Schedule transition to the next state using JSON-driven progression
                        character.scheduleStateTransition(nextState, currentTick, character.currentWeaponState.ticks, shooter, target, eventQueue, ownerId, gameCallbacks);
                        return;
                    }
                }
            }
        }
        
        if ("aiming".equals(currentState) || ("pointedfromhip".equals(currentState) && !character.getFiresFromAimingState())) {
            // DevCycle 27: System 5 - Check for immediate firing when character is already in correct hold state
            boolean shouldFireImmediately = character.isAlreadyInCorrectFiringState(currentState, currentTick);
            
            long fireDelay;
            if (shouldFireImmediately) {
                // Fire immediately (1 tick delay for scheduling) when already in correct state
                fireDelay = 1;
                System.out.println("*** " + character.getDisplayName() + " firing immediately - already in correct state: " + currentState + " ***");
            } else {
                // Handle normal firing progression with delays
                fireDelay = character.currentWeaponState.ticks;
                
                // Only apply aiming speed modifiers if firing from aiming state
                if ("aiming".equals(currentState)) {
                    // Determine which aiming speed to use based on firing mode and shot number
                    AimingSpeed aimingSpeedToUse = AimingSystem.getInstance().determineAimingSpeedForShot(character, character.currentShotInSequence);
                    
                    fireDelay = Math.round(character.currentWeaponState.ticks * aimingSpeedToUse.getTimingMultiplier() * AimingSystem.getInstance().calculateAimingSpeedMultiplier(character));
                    
                    // Add random additional time for very careful aiming
                    if (aimingSpeedToUse.isVeryCareful()) {
                        long additionalTime = aimingSpeedToUse.getVeryCarefulAdditionalTime();
                        fireDelay += additionalTime;
                    }
                }
                
                // DevCycle 27: System 3 - Add Very Careful timing for earned bonus
                AccumulatedAimingBonus earnedBonus = character.calculateEarnedAimingBonus(currentTick);
                if (earnedBonus == AccumulatedAimingBonus.VERY_CAREFUL && "aiming".equals(currentState)) {
                    // Add 2-5 seconds random time, same as selected Very Careful
                    long additionalTime = 120 + (long)(Math.random() * 181); // 120-300 ticks
                    fireDelay += additionalTime;
                    
                    // Log aiming speed usage for burst/auto modes
                    if (BurstFireManager.getInstance().isAutomaticFiring(character.id) && BurstFireManager.getInstance().getBurstShotsFired(character.id) > 1) {
                    }
                }
                // For pointedfromhip firing, use base timing without aiming speed modifiers
            }
            
            character.scheduleFiring(shooter, target, currentTick + fireDelay, eventQueue, ownerId, gameCallbacks);
        }
    }
    
    /**
     * Start melee attack sequence for character.
     * Extracted from Character.startMeleeAttackSequence() (~30 lines).
     * Handles melee attack initiation including range checking and movement coordination.
     * 
     * @param character The attacking character
     * @param attacker The attacking unit
     * @param target The target unit
     * @param currentTick The current game tick
     * @param eventQueue Event queue for scheduling future actions
     * @param ownerId Owner ID for event scheduling
     * @param gameCallbacks Game callback interface
     */
    public void startMeleeAttackSequence(Character character, IUnit attacker, IUnit target, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        if (character.meleeWeapon == null) {
            return;
        }
        
        // Check if target is within melee range using edge-to-edge calculation
        double distance = Math.hypot(target.getX() - attacker.getX(), target.getY() - attacker.getY());
        double distanceFeet = distance / 7.0;
        
        if (!character.isInMeleeRange(attacker, target, character.meleeWeapon)) {
            // Target is out of range - move towards target
            attacker.setTarget(target.getX(), target.getY());
            
            // Schedule a follow-up check to attempt attack once in range
            character.scheduleRangeCheckForMeleeAttack(attacker, target, currentTick + 10, eventQueue, ownerId, gameCallbacks);
            return;
        }
        
        // Calculate facing direction to target
        double dx = target.getX() - attacker.getX();
        double dy = target.getY() - attacker.getY();
        double angleRadians = Math.atan2(dx, -dy);
        double angleDegrees = Math.toDegrees(angleRadians);
        if (angleDegrees < 0) angleDegrees += 360;
        character.lastTargetFacing = angleDegrees;
        
        // Schedule melee attack from current state
        character.scheduleMeleeAttackFromCurrentState(attacker, target, currentTick, eventQueue, ownerId, gameCallbacks);
    }
}
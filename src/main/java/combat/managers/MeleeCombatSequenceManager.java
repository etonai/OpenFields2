package combat.managers;

import combat.Character;
import combat.Weapon;
import combat.WeaponState;
import game.interfaces.IUnit;
import game.Unit;
import game.ScheduledEvent;
import game.GameCallbacks;

/**
 * MeleeCombatSequenceManager handles melee combat sequence orchestration for characters.
 * Extracted from Character.java following DevCycle 31 Option 4 refactoring.
 * Manages melee combat orchestration with range checking and state transitions.
 */
public class MeleeCombatSequenceManager {
    
    // Singleton instance
    private static MeleeCombatSequenceManager instance;
    
    /**
     * Private constructor for singleton pattern.
     */
    private MeleeCombatSequenceManager() {
    }
    
    /**
     * Get the singleton instance of MeleeCombatSequenceManager.
     * 
     * @return The manager instance
     */
    public static MeleeCombatSequenceManager getInstance() {
        if (instance == null) {
            instance = new MeleeCombatSequenceManager();
        }
        return instance;
    }
    
    /**
     * Schedule melee state transition.
     * Extracted from Character.scheduleMeleeStateTransition() (~34 lines).
     * Handles melee weapon state progression with speed calculations.
     * 
     * @param character The character transitioning states
     * @param newStateName The target state name
     * @param currentTick The current game tick
     * @param transitionTickLength Base transition duration
     * @param attacker The attacking unit
     * @param target The target unit
     * @param eventQueue Event queue for scheduling future actions
     * @param ownerId Owner ID for event scheduling
     * @param gameCallbacks Game callback interface
     */
    public void scheduleMeleeStateTransition(Character character, String newStateName, long currentTick, long transitionTickLength, IUnit attacker, IUnit target, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        
        // Apply speed multiplier to weapon preparation states
        if (character.isWeaponPreparationState(newStateName)) {
            double speedMultiplier = character.calculateWeaponReadySpeedMultiplier();
            transitionTickLength = Math.round(transitionTickLength * speedMultiplier);
        }
        
        Weapon activeWeapon = character.getActiveWeapon();
        
        WeaponState newState = activeWeapon != null ? activeWeapon.getStateByName(newStateName) : null;
        
        if (newState != null) {
            // Create final copies for lambda
            final String finalStateName = newStateName;
            final long finalTick = currentTick + transitionTickLength;
            
            eventQueue.add(new ScheduledEvent(finalTick, () -> {
                character.currentWeaponState = newState;
                
                // Continue the attack sequence
                character.scheduleMeleeAttackFromCurrentState(attacker, target, finalTick, eventQueue, ownerId, gameCallbacks);
            }, ownerId));
            
        } else {
            // Fallback: skip to melee_ready state immediately
            WeaponState readyState = activeWeapon != null ? activeWeapon.getStateByName("melee_ready") : null;
            if (readyState != null) {
                character.currentWeaponState = readyState;
                character.scheduleMeleeAttackFromCurrentState(attacker, target, currentTick, eventQueue, ownerId, gameCallbacks);
            } else {
            }
        }
    }
    
    /**
     * Schedule range check for melee attack - continues tracking target until in range.
     * Extracted from Character.scheduleRangeCheckForMeleeAttack() (~30 lines).
     * Handles continuous range monitoring and target tracking.
     * 
     * @param character The attacking character
     * @param attacker The attacking unit
     * @param target The target unit
     * @param checkTick The tick to perform the range check
     * @param eventQueue Event queue for scheduling future actions
     * @param ownerId Owner ID for event scheduling
     * @param gameCallbacks Game callback interface
     */
    public void scheduleRangeCheckForMeleeAttack(Character character, IUnit attacker, IUnit target, long checkTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        eventQueue.add(new ScheduledEvent(checkTick, () -> {
            // Update movement target to track target's current position
            attacker.setTarget(target.getX(), target.getY());
            
            // Calculate current distance for debug
            double distance = Math.hypot(target.getX() - attacker.getX(), target.getY() - attacker.getY());
            double distanceFeet = distance / 7.0;
            double weaponReach = character.meleeWeapon.getTotalReach();
            
            // Check if now in range
            if (character.isInMeleeRange(attacker, target, character.meleeWeapon)) {
                // Now in range - proceed with attack
                
                // Calculate facing direction to target
                double dx = target.getX() - attacker.getX();
                double dy = target.getY() - attacker.getY();
                double angleRadians = Math.atan2(dx, -dy);
                double angleDegrees = Math.toDegrees(angleRadians);
                if (angleDegrees < 0) angleDegrees += 360;
                character.lastTargetFacing = angleDegrees;
                
                // Schedule melee attack from current state
                character.scheduleMeleeAttackFromCurrentState(attacker, target, checkTick, eventQueue, ownerId, gameCallbacks);
            } else {
                // Still not in range - schedule another check
                scheduleRangeCheckForMeleeAttack(character, attacker, target, checkTick + 10, eventQueue, ownerId, gameCallbacks);
            }
        }, ownerId));
    }
    
    /**
     * Schedule actual melee attack execution.
     * Extracted from Character.scheduleMeleeAttack() (~46 lines).
     * Handles melee attack execution with impact scheduling and recovery.
     * 
     * @param character The attacking character
     * @param attacker The attacking unit
     * @param target The target unit
     * @param attackTick The tick to execute the attack
     * @param eventQueue Event queue for scheduling future actions
     * @param ownerId Owner ID for event scheduling
     * @param gameCallbacks Game callback interface
     */
    public void scheduleMeleeAttack(Character character, IUnit attacker, IUnit target, long attackTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        
        eventQueue.add(new ScheduledEvent(attackTick, () -> {
            
            // Validate target is still valid
            if (target.getCharacter().isIncapacitated()) {
                return;
            }
            
            if (character.isIncapacitated()) {
                return;
            }
            
            // Update weapon state to attacking
            WeaponState attackingState = character.getActiveWeapon().getStateByName("melee_attacking");
            if (attackingState != null) {
                character.currentWeaponState = attackingState;
            } else {
            }
            
            
            // DevCycle 33: System 1 - Audio moved to resolution phase to prevent spam
            // Audio now plays in CombatResolver.resolveMeleeAttack() after recovery validation
            
            // Schedule immediate impact (no travel time for melee)
            gameCallbacks.scheduleMeleeImpact((Unit)attacker, (Unit)target, character.meleeWeapon, attackTick);
            
            // Schedule recovery back to ready state
            long recoveryTime = Math.round(character.meleeWeapon.getStateBasedAttackCooldown() * character.calculateAttackSpeedMultiplier());
            
            WeaponState readyState = character.getActiveWeapon().getStateByName("melee_ready");
            if (readyState != null) {
                eventQueue.add(new ScheduledEvent(attackTick + recoveryTime, () -> {
                    character.currentWeaponState = readyState;
                    character.isAttacking = false; // Clear attacking flag to allow auto-targeting to continue
                    
                    // Additional debug: check if auto-targeting should continue
                    if (character.usesAutomaticTargeting) {
                    }
                    
                    // Call checkContinuousAttack to trigger auto-targeting re-evaluation (similar to ranged weapon recovery)
                    character.checkContinuousAttack(attacker, attackTick + recoveryTime, eventQueue, ownerId, gameCallbacks);
                }, ownerId));
            }
        }, ownerId));
    }
}
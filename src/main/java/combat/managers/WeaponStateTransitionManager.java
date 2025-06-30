package combat.managers;

import combat.Character;
import combat.Weapon;
import combat.WeaponState;
import game.interfaces.IUnit;
import game.ScheduledEvent;
import game.GameCallbacks;

/**
 * WeaponStateTransitionManager handles weapon state transition orchestration for characters.
 * Extracted from Character.java following DevCycle 31 Option 4 refactoring.
 * Manages complex state transition timing and speed calculations.
 */
public class WeaponStateTransitionManager {
    
    // Singleton instance
    private static WeaponStateTransitionManager instance;
    
    /**
     * Private constructor for singleton pattern.
     */
    private WeaponStateTransitionManager() {
    }
    
    /**
     * Get the singleton instance of WeaponStateTransitionManager.
     * 
     * @return The manager instance
     */
    public static WeaponStateTransitionManager getInstance() {
        if (instance == null) {
            instance = new WeaponStateTransitionManager();
        }
        return instance;
    }
    
    /**
     * Schedule weapon state transition during attack sequences.
     * Extracted from Character.scheduleStateTransition() (~32 lines).
     * Handles state progression with speed calculations and timing management.
     * 
     * @param character The character transitioning states
     * @param newStateName The target state name
     * @param currentTick The current game tick
     * @param transitionTickLength Base transition duration
     * @param shooter The attacking unit
     * @param target The target unit
     * @param eventQueue Event queue for scheduling future actions
     * @param ownerId Owner ID for event scheduling
     * @param gameCallbacks Game callback interface
     */
    public void scheduleStateTransition(Character character, String newStateName, long currentTick, long transitionTickLength, IUnit shooter, IUnit target, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Create WeaponState objects for transition calculation
        WeaponState fromState = character.currentWeaponState;
        WeaponState toState = character.weapon.getStateByName(newStateName);
        
        // Delegate transition duration calculation to WeaponStateManager
        long calculatedDuration = WeaponStateManager.getInstance().calculateTransitionDuration(character, fromState, toState);
        
        // If WeaponStateManager provided a duration, use it; otherwise fall back to original logic
        if (calculatedDuration > 0) {
            transitionTickLength = calculatedDuration;
        } else {
            // Apply speed multiplier only to weapon preparation states (fallback)
            if (character.isWeaponPreparationState(newStateName)) {
                double speedMultiplier = character.calculateWeaponReadySpeedMultiplier();
                transitionTickLength = Math.round(transitionTickLength * speedMultiplier);
            }
        }
        
        long transitionTick = currentTick + transitionTickLength;
        eventQueue.add(new ScheduledEvent(transitionTick, () -> {
            character.currentWeaponState = character.weapon.getStateByName(newStateName);
            
            // DevCycle 27: Start timing when entering aiming or pointing states
            if ("aiming".equals(newStateName)) {
                character.startAimingTiming(transitionTick);
            } else if ("pointedfromhip".equals(newStateName)) {
                character.startPointingFromHipTiming(transitionTick);
            }
            
            character.scheduleAttackFromCurrentState(shooter, target, transitionTick, eventQueue, ownerId, gameCallbacks);
        }, ownerId));
    }
    
    /**
     * Schedule weapon state transition during ready sequences.
     * Extracted from Character.scheduleReadyStateTransition() (~28 lines).
     * Handles weapon preparation state progression with speed calculations.
     * 
     * @param character The character transitioning states
     * @param newStateName The target state name
     * @param currentTick The current game tick
     * @param transitionTickLength Base transition duration
     * @param unit The unit preparing weapon
     * @param eventQueue Event queue for scheduling future actions
     * @param ownerId Owner ID for event scheduling
     */
    public void scheduleReadyStateTransition(Character character, String newStateName, long currentTick, long transitionTickLength, IUnit unit, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        // Apply speed multiplier only to weapon preparation states
        if (character.isWeaponPreparationState(newStateName)) {
            double speedMultiplier = character.calculateWeaponReadySpeedMultiplier();
            transitionTickLength = Math.round(transitionTickLength * speedMultiplier);
        }
        
        long transitionTick = currentTick + transitionTickLength;
        
        eventQueue.add(new ScheduledEvent(transitionTick, () -> {
            // Get the appropriate weapon for state lookup
            Weapon activeWeapon = character.isMeleeCombatMode ? character.meleeWeapon : character.weapon;
            String previousState = character.currentWeaponState != null ? character.currentWeaponState.getState() : "None";
            character.currentWeaponState = activeWeapon.getStateByName(newStateName);
            
            // DevCycle 27: Start timing when entering aiming or pointing states during ready sequence
            if ("aiming".equals(newStateName)) {
                character.startAimingTiming(transitionTick);
            } else if ("pointedfromhip".equals(newStateName)) {
                character.startPointingFromHipTiming(transitionTick);
            }
            
            // Output weapon state change (like the old system)
            System.out.println("*** " + character.getDisplayName() + " weapon state: " + previousState + " -> " + newStateName + " ***");
            
            // Continue the ready sequence recursively
            character.scheduleReadyFromCurrentState(unit, transitionTick, eventQueue, ownerId);
        }, ownerId));
    }
}
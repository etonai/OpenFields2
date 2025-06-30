package combat.managers;

import combat.Character;
import combat.Weapon;
import combat.WeaponState;
import game.interfaces.IUnit;
import game.ScheduledEvent;
import utils.GameConstants;
import data.SkillsManager;

/**
 * WeaponReadinessManager handles weapon preparation and readiness orchestration for characters.
 * Extracted from Character.java following DevCycle 31 Option 4 refactoring.
 * Manages complex weapon progression logic with hold state management.
 */
public class WeaponReadinessManager {
    
    // Singleton instance
    private static WeaponReadinessManager instance;
    
    /**
     * Private constructor for singleton pattern.
     */
    private WeaponReadinessManager() {
    }
    
    /**
     * Get the singleton instance of WeaponReadinessManager.
     * 
     * @return The manager instance
     */
    public static WeaponReadinessManager getInstance() {
        if (instance == null) {
            instance = new WeaponReadinessManager();
        }
        return instance;
    }
    
    /**
     * Schedule weapon ready progression from current state.
     * Extracted from Character.scheduleReadyFromCurrentState() (~55 lines).
     * Handles weapon progression logic with hold state management and target state calculation.
     * 
     * @param character The character readying weapon
     * @param unit The unit readying weapon
     * @param currentTick The current game tick
     * @param eventQueue Event queue for scheduling future actions
     * @param ownerId Owner ID for event scheduling
     */
    public void scheduleReadyFromCurrentState(Character character, IUnit unit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        // Check weapon and state availability for both ranged and melee
        if (character.currentWeaponState == null) return;
        if (!character.isMeleeCombatMode && character.weapon == null) return;
        if (character.isMeleeCombatMode && character.meleeWeapon == null) return;
        
        String currentState = character.currentWeaponState.getState();
        
        // Determine target state: either hold state or default ready state
        String targetState;
        String targetHoldState = WeaponStateManager.getInstance().getTargetHoldState(character.id);
        if (targetHoldState != null) {
            targetState = targetHoldState;
        } else {
            targetState = character.isMeleeCombatMode ? "melee_ready" : "ready";
        }
        
        // Debug output for weapon state progression
        System.out.println("*** " + character.getDisplayName() + " weapon progression: current=" + currentState + 
                          ", target=" + targetState + ", tick=" + currentTick + " ***");
        
        // If we're already at the target state, stop progression
        if (targetState.equals(currentState)) {
            if (targetHoldState != null) {
                WeaponStateManager.getInstance().setTargetHoldState(character.id, null); // Clear target hold state after reaching it
                System.out.println("*** " + character.getDisplayName() + " reached hold state: " + currentState + " ***");
            }
            return;
        }
        
        // Get the appropriate weapon for state transitions
        Weapon activeWeapon = character.isMeleeCombatMode ? character.meleeWeapon : character.weapon;
        
        // Find the next state in the weapon's progression using the action field
        String nextState = character.currentWeaponState.getAction();
        if (nextState == null || nextState.isEmpty()) {
            // No next state defined, we're at the end of progression
            return;
        }
        
        // Check if the next state is available in the weapon
        WeaponState nextWeaponState = activeWeapon.getStateByName(nextState);
        if (nextWeaponState == null) {
            // Next state not found in weapon, can't progress
            return;
        }
        
        // Debug: Show what state we're transitioning to and when
        long transitionTime = currentTick + character.currentWeaponState.ticks;
        System.out.println("*** " + character.getDisplayName() + " scheduling transition from " + currentState + 
                          " to " + nextState + " in " + character.currentWeaponState.ticks + " ticks (at tick " + transitionTime + ") ***");
        
        // Schedule transition to the next state
        character.scheduleReadyStateTransition(nextState, currentTick, character.currentWeaponState.ticks, unit, eventQueue, ownerId);
    }
    
    /**
     * Start weapon ready sequence from beginning.
     * Extracted from Character.startReadyWeaponSequence() (~25 lines).
     * Handles weapon initialization and progression startup.
     * 
     * @param character The character starting ready sequence
     * @param unit The unit starting ready sequence
     * @param currentTick The current game tick
     * @param eventQueue Event queue for scheduling future actions
     * @param ownerId Owner ID for event scheduling
     */
    public void startReadyWeaponSequence(Character character, IUnit unit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        // Handle both ranged and melee weapons in unified system
        if (character.isMeleeCombatMode) {
            // Melee mode - ready the melee weapon
            if (character.meleeWeapon == null) {
                return;
            }
            
            // Initialize weapon state if needed for melee weapon
            if (character.currentWeaponState == null) {
                character.currentWeaponState = character.meleeWeapon.getInitialState();
            }
        } else {
            // Ranged mode - ready the ranged weapon
            if (character.weapon == null) {
                return;
            }
            
            // Initialize weapon state if needed for ranged weapon
            if (character.currentWeaponState == null) {
                character.currentWeaponState = character.weapon.getInitialState();
            }
        }
        
        scheduleReadyFromCurrentState(character, unit, currentTick, eventQueue, ownerId);
    }
    
    /**
     * Calculate weapon ready speed multiplier based on character stats and skills.
     * Extracted from Character.calculateWeaponReadySpeedMultiplier() (~8 lines).
     * Handles reflexes and quickdraw skill calculations.
     * 
     * @param character The character to calculate speed for
     * @return Speed multiplier for weapon readiness
     */
    public double calculateWeaponReadySpeedMultiplier(Character character) {
        int reflexesModifier = GameConstants.statToModifier(character.reflexes);
        double reflexesSpeedMultiplier = 1.0 - (reflexesModifier * 0.015);
        
        int quickdrawLevel = character.getSkillLevel(SkillsManager.QUICKDRAW);
        double quickdrawSpeedMultiplier = 1.0 - (quickdrawLevel * 0.08);
        
        return reflexesSpeedMultiplier * quickdrawSpeedMultiplier;
    }
}
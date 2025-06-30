package combat.managers;

import combat.Character;
import combat.WeaponState;
import game.interfaces.IUnit;
import game.ScheduledEvent;
import game.GameCallbacks;
import utils.GameConstants;

/**
 * ReactionManager handles reaction monitoring and execution for characters.
 * Extracted from Character.java following DevCycle 31 Option 4 refactoring.
 * Manages complex reaction monitoring with timing and event scheduling.
 */
public class ReactionManager {
    
    // Singleton instance
    private static ReactionManager instance;
    
    /**
     * Private constructor for singleton pattern.
     */
    private ReactionManager() {
    }
    
    /**
     * Get the singleton instance of ReactionManager.
     * 
     * @return The manager instance
     */
    public static ReactionManager getInstance() {
        if (instance == null) {
            instance = new ReactionManager();
        }
        return instance;
    }
    
    /**
     * Update reaction monitoring each tick.
     * Extracted from Character.updateReactionMonitoring() (~56 lines).
     * Checks if monitored target's weapon state has changed and schedules reaction.
     * 
     * @param character The character performing the monitoring
     * @param selfUnit The unit performing the monitoring
     * @param currentTick Current game tick
     * @param eventQueue Event queue for scheduling reactions
     * @param gameCallbacks Game callbacks for attack scheduling
     */
    public void updateReactionMonitoring(Character character, IUnit selfUnit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, GameCallbacks gameCallbacks) {
        // Skip if no reaction target set
        if (character.reactionTarget == null || character.reactionBaselineState == null) {
            return;
        }
        
        // Skip if already triggered
        if (character.reactionTriggerTick > 0) {
            return;
        }
        
        // Skip if character is incapacitated or reloading
        if (character.isIncapacitated() || character.isReloading) {
            return;
        }
        
        // Check if target's weapon state has changed
        WeaponState currentTargetState = character.reactionTarget.getCharacter().currentWeaponState;
        if (currentTargetState != null && currentTargetState != character.reactionBaselineState) {
            // Weapon state changed - trigger reaction with delay
            int reflexModifier = GameConstants.statToModifier(character.reflexes);
            long reactionDelay = Math.max(1, 30 - reflexModifier); // 30 base minus reflex modifier, minimum 1 tick
            
            character.reactionTriggerTick = currentTick + reactionDelay;
            
            // Schedule the reaction attack
            eventQueue.add(new ScheduledEvent(character.reactionTriggerTick, () -> {
                // Check if still valid to react (not incapacitated, target still exists, etc)
                if (!character.isIncapacitated() && character.reactionTarget != null && !character.isAttacking) {
                    System.out.println("*** " + character.getDisplayName() + " reacting to " + 
                                     character.reactionTarget.getCharacter().getDisplayName() + 
                                     " weapon state change (delay: " + reactionDelay + " ticks) ***");
                    
                    // Start attack sequence - this will handle queueing if already attacking
                    character.startAttackSequence(selfUnit, character.reactionTarget, character.reactionTriggerTick, eventQueue, selfUnit.getId(), gameCallbacks);
                    
                    // Clear reaction after triggering
                    character.reactionTarget = null;
                    character.reactionBaselineState = null;
                    character.reactionTriggerTick = -1;
                } else if (character.isAttacking) {
                    // Queue the reaction for after current attack
                    System.out.println("*** " + character.getDisplayName() + " queuing reaction - already attacking ***");
                    // Re-schedule for later
                    eventQueue.add(new ScheduledEvent(character.reactionTriggerTick + 30, () -> {
                        if (!character.isIncapacitated() && character.reactionTarget != null && !character.isAttacking) {
                            character.startAttackSequence(selfUnit, character.reactionTarget, character.reactionTriggerTick + 30, eventQueue, selfUnit.getId(), gameCallbacks);
                            // Clear reaction after triggering
                            character.reactionTarget = null;
                            character.reactionBaselineState = null;
                            character.reactionTriggerTick = -1;
                        }
                    }, selfUnit.getId()));
                }
            }, selfUnit.getId()));
        }
    }
}
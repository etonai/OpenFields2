package combat.managers;

import combat.Character;
import combat.AutoTargetingSystem;
import game.interfaces.IUnit;
import game.ScheduledEvent;
import game.GameCallbacks;

/**
 * AttackContinuationManager handles attack continuation and target management for characters.
 * Extracted from Character.java following DevCycle 31 Option 4 refactoring.
 * Manages complex target management and persistent attack logic.
 */
public class AttackContinuationManager {
    
    // Singleton instance
    private static AttackContinuationManager instance;
    
    /**
     * Private constructor for singleton pattern.
     */
    private AttackContinuationManager() {
    }
    
    /**
     * Get the singleton instance of AttackContinuationManager.
     * 
     * @return The manager instance
     */
    public static AttackContinuationManager getInstance() {
        if (instance == null) {
            instance = new AttackContinuationManager();
        }
        return instance;
    }
    
    /**
     * Perform automatic target change for persistent attack mode.
     * Extracted from Character.performAutomaticTargetChange() (~42 lines).
     * Handles complex target management and persistent attack logic.
     * 
     * @param character The character changing targets
     * @param shooter The attacking unit
     * @param currentTick The current game tick
     * @param eventQueue Event queue for scheduling future actions
     * @param ownerId Owner ID for event scheduling
     * @param gameCallbacks Game callback interface
     */
    public void performAutomaticTargetChange(Character character, IUnit shooter, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Only proceed if still in persistent attack mode and not incapacitated
        if (!character.persistentAttack || character.isIncapacitated() || character.weapon == null) {
            character.persistentAttack = false;
            character.currentTarget = null;
            return;
        }
        
        // Try to find a new target if using automatic targeting
        if (character.usesAutomaticTargeting) {
            IUnit newTarget = AutoTargetingSystem.findNearestHostileTargetWithZonePriority(character, shooter, gameCallbacks);
            if (newTarget != null) {
                character.currentTarget = newTarget;
                
                // Start new attack sequence against new target
                character.startAttackSequence(shooter, newTarget, currentTick, eventQueue, ownerId, gameCallbacks);
                return;
            }
        }
        
        // No new target found - clear persistent attack
        character.persistentAttack = false;
        character.currentTarget = null;
        character.isAttacking = false;
        
        // Preserve the current facing direction so weapon continues to aim at last target location
        if (character.lastTargetFacing != null && shooter != null) {
            shooter.setTargetFacing(character.lastTargetFacing);
        }
    }
}
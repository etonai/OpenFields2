package combat;

import game.interfaces.IUnit;
import game.ScheduledEvent;
import game.GameCallbacks;
import java.util.PriorityQueue;

/**
 * Handles character movement logic, restrictions, and state management.
 * Extracted from Character.java to improve code organization and reduce file size.
 */
public class MovementController {
    
    /**
     * Calculates the character's effective movement speed considering wounds and other factors
     */
    public static double getEffectiveMovementSpeed(Character character) {
        double baseSpeed = character.baseMovementSpeed;
        double typeMultiplier = character.currentMovementType.getSpeedMultiplier();
        return baseSpeed * typeMultiplier;
    }
    
    /**
     * Increases the character's movement type (Walk -> Jog -> Run) with wound restrictions
     */
    public static void increaseMovementType(Character character) {
        MovementType currentType = character.getCurrentMovementType();
        MovementType newType = getNextMovementType(currentType);
        
        if (newType != null) {
            // Check if the new movement type is allowed given wound restrictions
            MovementType maxAllowed = getMaxAllowedMovementType(character);
            
            if (newType.ordinal() <= maxAllowed.ordinal()) {
                character.setCurrentMovementType(newType);
            } else {
                System.out.println(">>> " + character.getDisplayName() + " cannot increase movement speed to " + newType.getDisplayName() + " due to leg wounds (max: " + maxAllowed.getDisplayName() + ")");
            }
        }
    }
    
    /**
     * Decreases the character's movement type (Run -> Jog -> Walk -> Crawl)
     */
    public static void decreaseMovementType(Character character) {
        MovementType currentType = character.getCurrentMovementType();
        MovementType newType = getPreviousMovementType(currentType);
        
        if (newType != null) {
            character.setCurrentMovementType(newType);
        }
    }
    
    /**
     * Gets the next higher movement type in the sequence
     */
    private static MovementType getNextMovementType(MovementType current) {
        MovementType[] types = MovementType.values();
        int currentIndex = current.ordinal();
        
        if (currentIndex < types.length - 1) {
            return types[currentIndex + 1];
        }
        return null; // Already at maximum
    }
    
    /**
     * Gets the next lower movement type in the sequence
     */
    private static MovementType getPreviousMovementType(MovementType current) {
        MovementType[] types = MovementType.values();
        int currentIndex = current.ordinal();
        
        if (currentIndex > 0) {
            return types[currentIndex - 1];
        }
        return null; // Already at minimum
    }
    
    /**
     * Updates melee movement progress and triggers attack when target is reached
     */
    public static void updateMeleeMovement(Character character, IUnit selfUnit, long currentTick, 
                                         PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Delegate to MeleeCombatManager for melee-specific movement logic
        MeleeCombatManager.updateMeleeMovement(character, selfUnit, currentTick, eventQueue, ownerId, gameCallbacks);
    }
    
    /**
     * Determines the maximum allowed movement type based on character's wounds
     */
    public static MovementType getMaxAllowedMovementType(Character character) {
        // Both legs wounded: can only crawl
        if (character.hasBothLegsWounded()) {
            return MovementType.CRAWL;
        }
        
        // Single leg wound: cannot run
        if (character.hasAnyLegWound()) {
            return MovementType.JOG; // Can walk, jog, crawl but not run
        }
        
        // No leg wounds: no movement restrictions
        return MovementType.RUN;
    }
    
    /**
     * Enforces movement restrictions based on current wounds
     */
    public static void enforceMovementRestrictions(Character character) {
        MovementType maxAllowed = getMaxAllowedMovementType(character);
        
        // If current movement type exceeds what's allowed, force it down
        if (character.getCurrentMovementType().ordinal() > maxAllowed.ordinal()) {
            character.setCurrentMovementType(maxAllowed);
            System.out.println(">>> " + character.getDisplayName() + " movement restricted to " + maxAllowed.getDisplayName() + " due to leg wounds");
        }
    }
    
    /**
     * Handles position changes that affect movement (e.g., going prone forces crawling)
     */
    public static void handlePositionChangeMovement(Character character, PositionState newPosition) {
        if (newPosition == PositionState.PRONE) {
            // Force crawl movement when going prone
            character.setCurrentMovementType(MovementType.CRAWL);
        }
    }
}
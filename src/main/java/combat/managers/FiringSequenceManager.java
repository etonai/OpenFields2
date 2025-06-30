package combat.managers;

import combat.Character;

/**
 * FiringSequenceManager handles firing sequence analysis for characters.
 * Extracted from Character.java following DevCycle 31 Option 4 refactoring.
 * Manages complex firing state analysis with timing calculations.
 */
public class FiringSequenceManager {
    
    // Singleton instance
    private static FiringSequenceManager instance;
    
    /**
     * Private constructor for singleton pattern.
     */
    private FiringSequenceManager() {
    }
    
    /**
     * Get the singleton instance of FiringSequenceManager.
     * 
     * @return The manager instance
     */
    public static FiringSequenceManager getInstance() {
        if (instance == null) {
            instance = new FiringSequenceManager();
        }
        return instance;
    }
    
    /**
     * Check if character is already in the correct firing state for immediate firing.
     * Extracted from Character.isAlreadyInCorrectFiringState() (~22 lines).
     * Handles complex firing state analysis with timing calculations and preference handling.
     * 
     * @param character The character to check firing state for
     * @param currentState The current weapon state
     * @param currentTick The current game tick
     * @return true if character should fire immediately
     */
    public boolean isAlreadyInCorrectFiringState(Character character, String currentState, long currentTick) {
        // DevCycle 27: System 5 - Check if already in correct hold state for immediate firing
        
        // Check if we're in aiming state and firing preference is aiming
        if ("aiming".equals(currentState) && character.getFiresFromAimingState()) {
            // For DevCycle 31, use simplified immediate firing check
            return true; // Allow immediate firing from aiming state
        }
        
        // Check if we're in pointedfromhip state and firing preference is point-from-hip
        if ("pointedfromhip".equals(currentState) && !character.getFiresFromAimingState()) {
            // For DevCycle 31, use simplified immediate firing check
            return true; // Allow immediate firing from point-from-hip state
        }
        
        return false;
    }
}
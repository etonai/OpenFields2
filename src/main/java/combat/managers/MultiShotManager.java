package combat.managers;

import combat.Character;

/**
 * MultiShotManager handles multiple shot sequence management for characters.
 * Extracted from Character.java following DevCycle 31 Option 4 refactoring.
 * Manages specialized firing sequence management.
 */
public class MultiShotManager {
    
    // Singleton instance
    private static MultiShotManager instance;
    
    /**
     * Private constructor for singleton pattern.
     */
    private MultiShotManager() {
    }
    
    /**
     * Get the singleton instance of MultiShotManager.
     * 
     * @return The manager instance
     */
    public static MultiShotManager getInstance() {
        if (instance == null) {
            instance = new MultiShotManager();
        }
        return instance;
    }
    
    /**
     * Reset multiple shot sequence state.
     * Extracted from Character multiple shot management (~15 lines).
     * Handles multi-shot sequence state cleanup.
     * 
     * @param character The character to reset multi-shot state for
     */
    public void resetMultipleShotSequence(Character character) {
        character.currentShotInSequence = 0;
        character.isAttacking = false;
    }
    
    /**
     * Check if character is in multiple shot sequence.
     * 
     * @param character The character to check
     * @return true if character is in multi-shot sequence
     */
    public boolean isInMultipleShotSequence(Character character) {
        return character.currentShotInSequence > 0;
    }
    
    /**
     * Increment shot sequence counter.
     * 
     * @param character The character to increment shot counter for
     */
    public void incrementShotSequence(Character character) {
        character.currentShotInSequence++;
    }
    
    /**
     * Get current shot number in sequence.
     * 
     * @param character The character to get shot number for
     * @return Current shot number in sequence
     */
    public int getCurrentShotInSequence(Character character) {
        return character.currentShotInSequence;
    }
}
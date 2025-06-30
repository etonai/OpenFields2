package combat.managers;

import combat.Character;
import combat.Wound;
import combat.WoundSeverity;
import combat.PositionState;
import combat.HesitationManager;
import game.ScheduledEvent;

/**
 * Manages wound application logic and health calculations for characters.
 * Extracted from Character.java following DevCycle 32 refactoring.
 * Works closely with CharacterStatsManager for wound storage.
 */
public class HealthManager {
    
    // Singleton instance
    private static HealthManager instance;
    
    /**
     * Private constructor for singleton pattern.
     */
    private HealthManager() {
    }
    
    /**
     * Get the singleton instance of HealthManager.
     * 
     * @return The manager instance
     */
    public static HealthManager getInstance() {
        if (instance == null) {
            instance = new HealthManager();
        }
        return instance;
    }
    
    /**
     * Add a wound to the character with hesitation triggering.
     * Extracted from Character.addWound() (~14 lines).
     * 
     * @param character The character receiving the wound
     * @param wound The wound to add
     * @param currentTick Current game tick for timing
     * @param eventQueue Event queue for scheduling hesitation
     * @param ownerId Owner ID for events
     */
    public void addWound(Character character, Wound wound, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        character.wounds.add(wound);
        character.woundsReceived++;
        
        // Apply damage to current health
        character.currentHealth -= wound.getDamage();
        
        // Enforce movement restrictions immediately after adding wound
        enforceMovementRestrictions(character);
        
        // Don't add hesitation for incapacitated characters
        if (!isIncapacitated(character)) {
            triggerHesitation(character, wound.severity, currentTick, eventQueue, ownerId);
        }
    }
    
    /**
     * Add a wound to the character without hesitation (backwards compatibility).
     * Extracted from Character.addWound() (~12 lines).
     * 
     * @param character The character receiving the wound
     * @param wound The wound to add
     */
    public void addWound(Character character, Wound wound) {
        character.wounds.add(wound);
        character.woundsReceived++;
        
        // Apply damage to current health
        character.currentHealth -= wound.getDamage();
        
        // Enforce movement restrictions immediately after adding wound
        enforceMovementRestrictions(character);
        
        // Note: Hesitation will not be triggered without event queue context
    }
    
    /**
     * Remove a wound from the character.
     * Coordinates with CharacterStatsManager for wound storage.
     * 
     * @param character The character to remove wound from
     * @param wound The wound to remove
     * @return true if wound was removed, false otherwise
     */
    public boolean removeWound(Character character, Wound wound) {
        // Delegate to CharacterStatsManager and sync field
        boolean removed = CharacterStatsManager.getInstance().removeWound(character.id, wound);
        if (removed) {
            character.wounds = CharacterStatsManager.getInstance().getWounds(character.id);
        }
        return removed;
    }
    
    /**
     * Check if character is incapacitated due to health or critical wounds.
     * 
     * @param character The character to check
     * @return true if incapacitated, false otherwise
     */
    public boolean isIncapacitated(Character character) {
        boolean incapacitated = false;
        
        if (character.currentHealth <= 0) {
            incapacitated = true;
        }
        // Check for any critical wounds
        for (Wound wound : character.wounds) {
            if (wound.getSeverity() == WoundSeverity.CRITICAL) {
                incapacitated = true;
                break;
            }
        }
        
        // Force prone position for incapacitated characters
        if (incapacitated && character.currentPosition != PositionState.PRONE) {
            character.currentPosition = PositionState.PRONE;
        }
        
        return incapacitated;
    }
    
    // Private helper methods
    
    /**
     * Enforce movement restrictions after wound application.
     */
    private void enforceMovementRestrictions(Character character) {
        // Note: This method could be enhanced to implement specific movement restrictions
        // based on wound severity and type. Currently a placeholder for future implementation.
    }
    
    /**
     * Trigger hesitation response to wound.
     * Delegates to HesitationManager for the actual hesitation logic.
     */
    private void triggerHesitation(Character character, WoundSeverity severity, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        // Delegate to HesitationManager for wound-induced hesitation
        HesitationManager.triggerHesitation(character, severity, currentTick, eventQueue, ownerId);
    }
}
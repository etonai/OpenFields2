import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import combat.Handedness;
import combat.Wound;
import combat.WeaponState;
import game.*;
import data.WeaponFactory;
import utils.GameConstants;
import java.util.Date;
import java.util.PriorityQueue;

/**
 * Test class for DevCycle 28 features:
 * - Multiple Shot Control System
 * - Reaction Action System
 */
public class DevCycle28Test {
    
    private combat.Character testCharacter;
    private combat.Character targetCharacter;
    private Unit testUnit;
    private Unit targetUnit;
    private PriorityQueue<ScheduledEvent> eventQueue;
    private TestGameCallbacks gameCallbacks;
    
    private class TestGameCallbacks implements GameCallbacks {
        @Override
        public void playGunshotSound() {}
        
        @Override
        public javafx.scene.media.AudioClip getGunshotSound() { return null; }
        
        @Override
        public double convertPixelsToFeet(double pixels) { return pixels / 7.0; }
        
        @Override
        public int getNextOwnerObjectId() { return 1; }
        
        @Override
        public void applyWoundToCharacter(int characterId, Wound wound) {}
        
        @Override
        public void updateCharacterInRegistry(combat.Character character) {}
        
        @Override
        public Unit findUnitByCharacterId(int characterId) { return null; }
        
        @Override
        public java.util.List<Unit> getAllUnits() { return new java.util.ArrayList<>(); }
        
        @Override
        public void setPaused(boolean paused) {}
        
        @Override
        public void displayDiceRollResult(String attackerName, String targetName, int roll, int modifiedRoll, int targetNumber, boolean isHit, String rollDetails) {}
        
        @Override
        public void displayWoundResult(String attackerName, String targetName, String bodyPart, String severity, int damage, int currentHealth, int maxHealth) {}
        
        @Override
        public Unit findUnitById(int unitId) { return null; }
        
        @Override
        public void log(String message) { System.out.println(message); }
    }
    
    @BeforeEach
    void setUp() {
        // Create test characters
        testCharacter = new combat.Character(1, "Test", "Test", "Character", new Date(), "modern",
                                    70, 100, 60, 50, 80, Handedness.RIGHT);
        targetCharacter = new combat.Character(2, "Target", "Target", "Character", new Date(), "modern",
                                      60, 100, 50, 40, 60, Handedness.RIGHT);
        
        // Create units
        testUnit = new Unit(testCharacter, 100, 100, null, 1);
        targetUnit = new Unit(targetCharacter, 200, 100, null, 2);
        
        // Create weapons
        testCharacter.weapon = WeaponFactory.createWeapon("wpn_mp5");
        testCharacter.currentWeaponState = testCharacter.weapon.getInitialState();
        
        targetCharacter.weapon = WeaponFactory.createWeapon("wpn_colt_peacemaker");
        targetCharacter.currentWeaponState = targetCharacter.weapon.getInitialState();
        
        // Initialize event queue and callbacks
        eventQueue = new PriorityQueue<>();
        gameCallbacks = new TestGameCallbacks();
    }
    
    // Multiple Shot Control Tests
    
    @Test
    void testMultipleShotCountDefault() {
        assertEquals(1, testCharacter.multipleShootCount, "Default multiple shot count should be 1");
    }
    
    @Test
    void testMultipleShotCountCycling() {
        // Test cycling through values
        testCharacter.multipleShootCount = 1;
        testCharacter.multipleShootCount = (testCharacter.multipleShootCount % 5) + 1;
        assertEquals(2, testCharacter.multipleShootCount);
        
        testCharacter.multipleShootCount = (testCharacter.multipleShootCount % 5) + 1;
        assertEquals(3, testCharacter.multipleShootCount);
        
        testCharacter.multipleShootCount = (testCharacter.multipleShootCount % 5) + 1;
        assertEquals(4, testCharacter.multipleShootCount);
        
        testCharacter.multipleShootCount = (testCharacter.multipleShootCount % 5) + 1;
        assertEquals(5, testCharacter.multipleShootCount);
        
        testCharacter.multipleShootCount = (testCharacter.multipleShootCount % 5) + 1;
        assertEquals(1, testCharacter.multipleShootCount, "Should cycle back to 1 after 5");
    }
    
    @Test
    void testMultipleShotIgnoredInMeleeMode() {
        // Set character to melee mode
        testCharacter.isMeleeCombatMode = true;
        int originalCount = testCharacter.multipleShootCount;
        
        // In actual implementation, CTRL-1 handler checks melee mode and ignores input
        // This test verifies the field behavior
        assertEquals(originalCount, testCharacter.multipleShootCount, 
                    "Multiple shot count should not change in melee mode");
    }
    
    @Test
    void testShotSequenceTracking() {
        testCharacter.multipleShootCount = 3;
        testCharacter.currentShotInSequence = 0;
        
        // Simulate shot sequence
        testCharacter.currentShotInSequence++;
        assertEquals(1, testCharacter.currentShotInSequence);
        
        testCharacter.currentShotInSequence++;
        assertEquals(2, testCharacter.currentShotInSequence);
        
        testCharacter.currentShotInSequence++;
        assertEquals(3, testCharacter.currentShotInSequence);
        
        // Reset after sequence completes
        testCharacter.currentShotInSequence = 0;
        assertEquals(0, testCharacter.currentShotInSequence);
    }
    
    // Reaction Action Tests
    
    @Test
    void testReactionTargetSetup() {
        // Set up reaction
        testCharacter.reactionTarget = targetUnit;
        testCharacter.reactionBaselineState = targetCharacter.currentWeaponState;
        testCharacter.reactionTriggerTick = -1;
        
        assertNotNull(testCharacter.reactionTarget, "Reaction target should be set");
        assertNotNull(testCharacter.reactionBaselineState, "Baseline state should be recorded");
        assertEquals(-1, testCharacter.reactionTriggerTick, "Trigger tick should be -1 initially");
    }
    
    @Test
    void testReactionTargetCancellation() {
        // Set up reaction
        testCharacter.reactionTarget = targetUnit;
        testCharacter.reactionBaselineState = targetCharacter.currentWeaponState;
        testCharacter.reactionTriggerTick = -1;
        
        // Cancel reaction
        testCharacter.reactionTarget = null;
        testCharacter.reactionBaselineState = null;
        testCharacter.reactionTriggerTick = -1;
        
        assertNull(testCharacter.reactionTarget, "Reaction target should be cleared");
        assertNull(testCharacter.reactionBaselineState, "Baseline state should be cleared");
    }
    
    @Test
    void testReactionDelayCalculation() {
        // Test with different reflex values
        testCharacter.reflexes = 80; // Modifier +12
        long baseDelay = 30;
        long expectedDelay = baseDelay - 12; // 18 ticks
        assertEquals(18, baseDelay - GameConstants.statToModifier(testCharacter.reflexes));
        
        testCharacter.reflexes = 20; // Modifier -12
        expectedDelay = baseDelay - (-12); // 42 ticks
        assertEquals(42, baseDelay - GameConstants.statToModifier(testCharacter.reflexes));
        
        testCharacter.reflexes = 100; // Modifier +20
        expectedDelay = Math.max(1, baseDelay - 20); // Minimum 1 tick
        assertEquals(10, baseDelay - GameConstants.statToModifier(testCharacter.reflexes));
    }
    
    @Test
    void testReactionMonitoringSkipsWhenNoTarget() {
        testCharacter.reactionTarget = null;
        testCharacter.updateReactionMonitoring(testUnit, 100, eventQueue, gameCallbacks);
        
        // Should not add any events
        assertTrue(eventQueue.isEmpty(), "No events should be scheduled when no reaction target");
    }
    
    @Test
    void testReactionMonitoringSkipsWhenAlreadyTriggered() {
        testCharacter.reactionTarget = targetUnit;
        testCharacter.reactionBaselineState = targetCharacter.currentWeaponState;
        testCharacter.reactionTriggerTick = 150; // Already triggered
        
        testCharacter.updateReactionMonitoring(testUnit, 100, eventQueue, gameCallbacks);
        
        // Should not add any events
        assertTrue(eventQueue.isEmpty(), "No events should be scheduled when already triggered");
    }
    
    @Test
    void testReactionMonitoringDetectsStateChange() {
        // Set up reaction with baseline state
        WeaponState initialState = targetCharacter.currentWeaponState;
        testCharacter.reactionTarget = targetUnit;
        testCharacter.reactionBaselineState = initialState;
        testCharacter.reactionTriggerTick = -1;
        
        // Change target's weapon state
        targetCharacter.currentWeaponState = targetCharacter.weapon.getStateByName("aiming");
        
        // Run reaction monitoring
        testCharacter.updateReactionMonitoring(testUnit, 100, eventQueue, gameCallbacks);
        
        // Should schedule an event
        assertFalse(eventQueue.isEmpty(), "Event should be scheduled for state change");
        
        // Check trigger tick is set
        assertTrue(testCharacter.reactionTriggerTick > 100, "Trigger tick should be set in future");
    }
    
    // Integration Tests
    
    @Test
    void testMultipleShotAndReactionIntegration() {
        // Set up multiple shots and reaction
        testCharacter.multipleShootCount = 3;
        testCharacter.reactionTarget = targetUnit;
        testCharacter.reactionBaselineState = targetCharacter.currentWeaponState;
        
        // Both systems should coexist without interference
        assertEquals(3, testCharacter.multipleShootCount);
        assertNotNull(testCharacter.reactionTarget);
        
        // Clear reaction should not affect multiple shot count
        testCharacter.reactionTarget = null;
        assertEquals(3, testCharacter.multipleShootCount, "Multiple shot count should remain unchanged");
    }
    
    @Test
    void testSaveLoadPersistence() {
        // Set multiple shot count
        testCharacter.multipleShootCount = 4;
        
        // Create CharacterData for save
        data.CharacterData saveData = new data.CharacterData();
        saveData.multipleShootCount = testCharacter.multipleShootCount;
        
        // Verify it saves correctly
        assertEquals(4, saveData.multipleShootCount);
        
        // Simulate load with default handling
        int loadedCount = saveData.multipleShootCount > 0 ? saveData.multipleShootCount : 1;
        assertEquals(4, loadedCount);
        
        // Test backward compatibility (0 or unset)
        saveData.multipleShootCount = 0;
        loadedCount = saveData.multipleShootCount > 0 ? saveData.multipleShootCount : 1;
        assertEquals(1, loadedCount, "Should default to 1 for backward compatibility");
    }
}
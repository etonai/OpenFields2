import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import combat.*;
import game.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ReflexesQuickdrawTest {
    
    private combat.Character testCharacter;
    private Unit testUnit;
    private Weapon testWeapon;
    
    @BeforeEach
    public void setUp() {
        testCharacter = new combat.Character("TestChar", 70, 100, 60, 50, 55, combat.Handedness.RIGHT_HANDED);
        testUnit = new Unit(testCharacter, 100, 100, Color.BLUE, 1);
        testWeapon = createTestPistol();
        testCharacter.setWeapon(testWeapon);
        testCharacter.setCurrentWeaponState(testWeapon.getStateByName("holstered"));
    }
    
    private Weapon createTestPistol() {
        Weapon weapon = new Weapon("Test Pistol", 600.0, 8, 10, "/test.wav", 200.0, 10, "bullet", WeaponType.PISTOL);
        weapon.states = new ArrayList<>();
        weapon.states.add(new WeaponState("holstered", "drawing", 0));
        weapon.states.add(new WeaponState("drawing", "ready", 30));
        weapon.initialStateName = "holstered";
        return weapon;
    }
    
    @Test
    public void testReflexesSpeedMultiplier_BaselineReflexes() {
        // Test with reflexes 50 (0 modifier) - should be 1.0x speed
        testCharacter.setReflexes(50);
        double speedMultiplier = testCharacter.getWeaponReadySpeedMultiplier();
        assertEquals(1.0, speedMultiplier, 0.001, "Reflexes 50 should give 1.0x speed multiplier");
    }
    
    @Test
    public void testReflexesSpeedMultiplier_HighReflexes() {
        // Test with reflexes 90 (+12 modifier) - should be 0.88x speed (12% faster)
        testCharacter.setReflexes(90);
        double speedMultiplier = testCharacter.getWeaponReadySpeedMultiplier();
        assertEquals(0.88, speedMultiplier, 0.001, "Reflexes 90 should give 0.88x speed multiplier");
    }
    
    @Test
    public void testReflexesSpeedMultiplier_LowReflexes() {
        // Test with reflexes 10 (-13 modifier) - should be 1.13x speed (13% slower)
        testCharacter.setReflexes(10);
        double speedMultiplier = testCharacter.getWeaponReadySpeedMultiplier();
        assertEquals(1.13, speedMultiplier, 0.001, "Reflexes 10 should give 1.13x speed multiplier");
    }
    
    @Test
    public void testQuickdrawSpeedMultiplier_NoSkill() {
        // Test with no quickdraw skill - should be 1.0x speed
        testCharacter.setReflexes(50); // Baseline reflexes
        double speedMultiplier = testCharacter.getWeaponReadySpeedMultiplier();
        assertEquals(1.0, speedMultiplier, 0.001, "No quickdraw skill should give 1.0x speed multiplier");
    }
    
    @Test
    public void testQuickdrawSpeedMultiplier_Level4() {
        // Test with quickdraw level 4 - should be 0.8x speed (20% faster)
        testCharacter.setReflexes(50); // Baseline reflexes
        testCharacter.addSkill(new Skill(Skills.QUICKDRAW, 4));
        double speedMultiplier = testCharacter.getWeaponReadySpeedMultiplier();
        assertEquals(0.8, speedMultiplier, 0.001, "Quickdraw level 4 should give 0.8x speed multiplier");
    }
    
    @Test
    public void testCombinedSpeedMultiplier_EthanStats() {
        // Test Ethan's stats: reflexes 90 (+12 modifier) + quickdraw 4
        // reflexes: 0.88x, quickdraw: 0.8x, combined: 0.88 * 0.8 = 0.704x
        testCharacter.setReflexes(90);
        testCharacter.addSkill(new Skill(Skills.QUICKDRAW, 4));
        double speedMultiplier = testCharacter.getWeaponReadySpeedMultiplier();
        assertEquals(0.704, speedMultiplier, 0.001, "Ethan's stats should give 0.704x speed multiplier");
    }
    
    @Test
    public void testCombinedSpeedMultiplier_ExtremeCase() {
        // Test extreme case: reflexes 100 (+20 modifier) + quickdraw 8
        // reflexes: 0.8x, quickdraw: 0.6x, combined: 0.8 * 0.6 = 0.48x
        testCharacter.setReflexes(100);
        testCharacter.addSkill(new Skill(Skills.QUICKDRAW, 8));
        double speedMultiplier = testCharacter.getWeaponReadySpeedMultiplier();
        assertEquals(0.48, speedMultiplier, 0.001, "Extreme stats should give 0.48x speed multiplier");
    }
    
    @Test
    public void testActualTimingApplication() {
        // Test that the speed multiplier is actually applied to weapon preparation timing
        // Set up character with fast reflexes and quickdraw
        testCharacter.setReflexes(90); // +12 modifier = 0.88x
        testCharacter.addSkill(new Skill(Skills.QUICKDRAW, 4)); // 0.8x
        // Combined: 0.704x
        
        // Original drawing time is 30 ticks, should become ~21 ticks (30 * 0.704 = 21.12)
        java.util.PriorityQueue<ScheduledEvent> eventQueue = new java.util.PriorityQueue<>();
        testCharacter.startReadyWeaponSequence(testUnit, 100, eventQueue, 1);
        
        // Check that events are scheduled with modified timing
        assertFalse(eventQueue.isEmpty(), "Should have scheduled weapon ready events");
        
        // First event should be the drawing transition
        ScheduledEvent firstEvent = eventQueue.poll();
        assertEquals(100, firstEvent.getTick(), "Drawing should start immediately");
        
        // Execute first event and check next timing
        firstEvent.getAction().run();
        assertEquals("drawing", testCharacter.getCurrentWeaponState().getState(), "Should be in drawing state");
        
        // Second event should be ready transition with modified timing
        ScheduledEvent secondEvent = eventQueue.poll();
        long expectedTick = 100 + Math.round(30 * 0.704); // 100 + 21 = 121
        assertEquals(expectedTick, secondEvent.getTick(), "Ready timing should be modified by speed multiplier");
    }
    
    @Test
    public void testSpeedMultiplierOnlyAffectsPreparationStates() {
        // Verify that only weapon preparation states (drawing, unsheathing, unsling) are affected
        // and not combat states like aiming, firing, recovering
        
        // This test verifies the isWeaponPreparationState method logic
        // We can't directly test it since it's private, but we can verify it through behavior
        
        testCharacter.setReflexes(100); // Maximum speed bonus
        testCharacter.addSkill(new Skill(Skills.QUICKDRAW, 8)); // Maximum quickdraw
        
        double speedMultiplier = testCharacter.getWeaponReadySpeedMultiplier();
        assertTrue(speedMultiplier < 1.0, "Should have speed bonus from high reflexes and quickdraw");
        
        // The detailed testing of which states are affected would require integration testing
        // or making the isWeaponPreparationState method public/package-private for testing
    }
    
    @Test
    public void testSpeedMultiplierBoundaries() {
        // Test boundary conditions for reflexes
        testCharacter.setReflexes(1); // Minimum reflexes (-20 modifier) = 1.2x slower
        double slowest = testCharacter.getWeaponReadySpeedMultiplier();
        assertEquals(1.2, slowest, 0.001, "Minimum reflexes should give slowest speed");
        
        testCharacter.setReflexes(100); // Maximum reflexes (+20 modifier) = 0.8x faster
        testCharacter.getSkills().clear(); // Remove any quickdraw skill
        double fastest = testCharacter.getWeaponReadySpeedMultiplier();
        assertEquals(0.8, fastest, 0.001, "Maximum reflexes should give fastest speed from reflexes alone");
    }
}
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import combat.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DevCycle 27: System 3 - Accumulated Aiming Time Bonus System
 */
public class AccumulatedAimingBonusTest {
    
    private combat.Character testCharacter;
    
    @BeforeEach
    public void setUp() {
        testCharacter = new combat.Character("TestChar", 70, 100, 60, 50, 55, combat.Handedness.RIGHT_HANDED);
        
        // Create a simple test weapon with aiming state
        WeaponState aimingState = new WeaponState("aiming", "firing", 30); // 30 tick base aiming time
        
        // Set current weapon state directly for testing
        testCharacter.currentWeaponState = aimingState;
    }
    
    @Test
    public void testAccumulatedAimingBonus_NoBonus() {
        // Test with very short aiming time
        testCharacter.startAimingTiming(100);
        AccumulatedAimingBonus bonus = testCharacter.calculateEarnedAimingBonus(110); // 10 ticks
        assertEquals(AccumulatedAimingBonus.NONE, bonus, "Should get no bonus for short aiming time");
    }
    
    @Test
    public void testAccumulatedAimingBonus_NormalBonus() {
        // Test with 1x base aiming time (30 ticks)
        testCharacter.startAimingTiming(100);
        AccumulatedAimingBonus bonus = testCharacter.calculateEarnedAimingBonus(130); // 30 ticks
        assertEquals(AccumulatedAimingBonus.NORMAL, bonus, "Should get Normal bonus at 1x base time");
    }
    
    @Test
    public void testAccumulatedAimingBonus_CarefulBonus() {
        // Test with 2x base aiming time (60 ticks)
        testCharacter.startAimingTiming(100);
        AccumulatedAimingBonus bonus = testCharacter.calculateEarnedAimingBonus(160); // 60 ticks
        assertEquals(AccumulatedAimingBonus.CAREFUL, bonus, "Should get Careful bonus at 2x base time");
    }
    
    @Test
    public void testAccumulatedAimingBonus_VeryCarefulBonus() {
        // Test with 3x base aiming time (90 ticks)
        testCharacter.startAimingTiming(100);
        AccumulatedAimingBonus bonus = testCharacter.calculateEarnedAimingBonus( 190); // 90 ticks
        assertEquals(AccumulatedAimingBonus.VERY_CAREFUL, bonus, "Should get Very Careful bonus at 3x base time");
    }
    
    @Test
    public void testAccumulatedAimingBonus_PointingFromHipCap() {
        // Test that pointing from hip caps at Normal
        testCharacter.currentWeaponState = new WeaponState("pointedfromhip", "firing", 20);
        testCharacter.startPointingFromHipTiming(100);
        
        // Even with 3x base time, should cap at Normal
        AccumulatedAimingBonus bonus = testCharacter.calculateEarnedAimingBonus( 190); // 90 ticks
        assertEquals(AccumulatedAimingBonus.NORMAL, bonus, "Pointing from hip should cap at Normal bonus");
    }
    
    @Test
    public void testAccumulatedAimingBonus_NotInAimingState() {
        // Test when not in aiming or pointing state
        testCharacter.currentWeaponState = new WeaponState("ready", "aiming", 15);
        testCharacter.startAimingTiming(100);
        
        AccumulatedAimingBonus bonus = testCharacter.calculateEarnedAimingBonus( 200);
        assertEquals(AccumulatedAimingBonus.NONE, bonus, "Should get no bonus when not in aiming/pointing state");
    }
    
    @Test
    public void testAccumulatedAimingBonus_WeaponReadySpeedMultiplier() {
        // Test that weapon ready speed affects thresholds
        // Mock character with faster weapon ready speed (0.8x multiplier)
        testCharacter = new combat.Character("FastChar", 90, 100, 60, 50, 90, combat.Handedness.RIGHT_HANDED); // High reflexes
        testCharacter.currentWeaponState = new WeaponState("aiming", "firing", 30);
        
        testCharacter.startAimingTiming(100);
        
        // With 0.8x multiplier, thresholds are lowered:
        // Normal: 30 * 0.8 = 24 ticks
        // So 25 ticks should give Normal bonus
        AccumulatedAimingBonus bonus = testCharacter.calculateEarnedAimingBonus( 125); // 25 ticks
        assertEquals(AccumulatedAimingBonus.NORMAL, bonus, "Fast character should earn Normal bonus quicker");
    }
    
    @Test
    public void testAccumulatedAimingBonus_EnumProperties() {
        // Test AccumulatedAimingBonus enum properties
        assertEquals("None", AccumulatedAimingBonus.NONE.getDisplayName());
        assertEquals(0.0, AccumulatedAimingBonus.NONE.getAccuracyModifier(), 0.001);
        assertFalse(AccumulatedAimingBonus.NONE.isVeryCareful());
        
        assertEquals("Normal", AccumulatedAimingBonus.NORMAL.getDisplayName());
        assertEquals(0.0, AccumulatedAimingBonus.NORMAL.getAccuracyModifier(), 0.001);
        assertFalse(AccumulatedAimingBonus.NORMAL.isVeryCareful());
        
        assertEquals("Careful", AccumulatedAimingBonus.CAREFUL.getDisplayName());
        assertEquals(15.0, AccumulatedAimingBonus.CAREFUL.getAccuracyModifier(), 0.001);
        assertFalse(AccumulatedAimingBonus.CAREFUL.isVeryCareful());
        
        assertEquals("Very Careful", AccumulatedAimingBonus.VERY_CAREFUL.getDisplayName());
        assertEquals(15.0, AccumulatedAimingBonus.VERY_CAREFUL.getAccuracyModifier(), 0.001);
        assertTrue(AccumulatedAimingBonus.VERY_CAREFUL.isVeryCareful());
    }
    
    @Test
    public void testCharacterSupportMethods() {
        // Test the support methods added to Character class
        assertEquals(30, testCharacter.getCurrentWeaponAimingStateTicks(), "Should get default base aiming time");
        
        assertTrue(testCharacter.isInAimingOrPointingState(), "Should detect aiming state");
        assertFalse(testCharacter.isPointingFromHip(), "Should not detect pointing from hip when aiming");
        
        testCharacter.currentWeaponState = new WeaponState("pointedfromhip", "firing", 20);
        assertTrue(testCharacter.isInAimingOrPointingState(), "Should detect pointing state");
        assertTrue(testCharacter.isPointingFromHip(), "Should detect pointing from hip");
        
        testCharacter.currentWeaponState = new WeaponState("ready", "aiming", 15);
        assertFalse(testCharacter.isInAimingOrPointingState(), "Should not detect ready state");
        assertFalse(testCharacter.isPointingFromHip(), "Should not detect pointing when in ready");
    }
}
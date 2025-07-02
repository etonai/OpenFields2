import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import combat.Character;
import combat.Handedness;

/**
 * DevCycle 27: Test aiming duration tracking functionality
 */
public class AimingDurationTest {
    
    @Test
    public void testAimingDurationTracking() {
        // Create a test character
        Character character = new Character("TestChar", 75, 100, 75, 75, 75, Handedness.RIGHT_HANDED);
        
        // Initially no timing should be recorded
        assertEquals(-1, character.aimingStartTick);
        assertEquals(-1, character.pointingFromHipStartTick);
        assertEquals(0, character.getAimingDuration(100));
        assertEquals(0, character.getPointingFromHipDuration(100));
        
        // Start aiming timing at tick 50
        character.startAimingTiming(50);
        assertEquals(50, character.aimingStartTick);
        assertEquals(-1, character.pointingFromHipStartTick);
        assertEquals(25, character.getAimingDuration(75)); // 75 - 50 = 25 ticks
        assertEquals(0, character.getPointingFromHipDuration(75));
        
        // Start pointing from hip timing at tick 100 (should clear aiming)
        character.startPointingFromHipTiming(100);
        assertEquals(-1, character.aimingStartTick);
        assertEquals(100, character.pointingFromHipStartTick);
        assertEquals(0, character.getAimingDuration(120));
        assertEquals(20, character.getPointingFromHipDuration(120)); // 120 - 100 = 20 ticks
        
        // Test getCurrentAimingDuration with different firing preferences
        character.firesFromAimingState = false; // Point from hip mode
        assertEquals(20, character.getCurrentAimingDuration(120));
        
        character.startAimingTiming(130);
        character.firesFromAimingState = true; // Aiming mode
        assertEquals(10, character.getCurrentAimingDuration(140)); // 140 - 130 = 10 ticks
        
        // Reset timing
        character.resetAimingTiming();
        assertEquals(-1, character.aimingStartTick);
        assertEquals(-1, character.pointingFromHipStartTick);
        assertEquals(0, character.getCurrentAimingDuration(150));
    }
    
    @Test
    public void testTimingReset() {
        Character character = new Character("TestChar", 75, 100, 75, 75, 75, Handedness.RIGHT_HANDED);
        
        // Set up some timing
        character.startAimingTiming(10);
        character.startPointingFromHipTiming(20);
        
        // Verify timing is set
        assertNotEquals(-1, character.pointingFromHipStartTick);
        
        // Reset should clear everything
        character.resetAimingTiming();
        assertEquals(-1, character.aimingStartTick);
        assertEquals(-1, character.pointingFromHipStartTick);
        assertEquals(0, character.getAimingDuration(100));
        assertEquals(0, character.getPointingFromHipDuration(100));
    }
}
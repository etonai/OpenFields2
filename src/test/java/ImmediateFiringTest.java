import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import combat.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DevCycle 27: System 5 - Immediate Hold State Firing
 */
public class ImmediateFiringTest {
    
    private combat.Character testCharacter;
    
    @BeforeEach
    public void setUp() {
        testCharacter = new combat.Character("TestChar", 70, 100, 60, 50, 55, combat.Handedness.RIGHT_HANDED);
        
        // Create a simple test weapon with aiming state
        WeaponState aimingState = new WeaponState("aiming", "firing", 30); // 30 tick base aiming time
        WeaponState pointingState = new WeaponState("pointedfromhip", "firing", 20); // 20 tick base pointing time
        
        // Set current weapon state directly for testing
        testCharacter.currentWeaponState = aimingState;
    }
    
    @Test
    public void testIsAlreadyInCorrectFiringState_AimingPreference() {
        // Character prefers aiming and is currently aiming
        testCharacter.firesFromAimingState = true;
        testCharacter.currentWeaponState = new WeaponState("aiming", "firing", 30);
        
        // Start aiming timing
        testCharacter.startAimingTiming(100);
        
        // Should fire immediately after minimum duration (5+ ticks)
        boolean shouldFireImmediately = testCharacter.isAlreadyInCorrectFiringState("aiming", 106); // 6 ticks
        assertTrue(shouldFireImmediately, "Should fire immediately when in aiming state with aiming preference and sufficient duration");
        
        // Should NOT fire immediately if duration is too short
        boolean shouldNotFireImmediately = testCharacter.isAlreadyInCorrectFiringState("aiming", 104); // 4 ticks
        assertFalse(shouldNotFireImmediately, "Should not fire immediately if duration is less than 5 ticks");
    }
    
    @Test
    public void testIsAlreadyInCorrectFiringState_PointingPreference() {
        // Character prefers pointing from hip and is currently pointing
        testCharacter.firesFromAimingState = false;
        testCharacter.currentWeaponState = new WeaponState("pointedfromhip", "firing", 20);
        
        // Start pointing timing
        testCharacter.startPointingFromHipTiming(100);
        
        // Should fire immediately after minimum duration (5+ ticks)
        boolean shouldFireImmediately = testCharacter.isAlreadyInCorrectFiringState("pointedfromhip", 106); // 6 ticks
        assertTrue(shouldFireImmediately, "Should fire immediately when in pointedfromhip state with hip preference and sufficient duration");
    }
    
    @Test
    public void testIsAlreadyInCorrectFiringState_WrongStateForPreference() {
        // Character prefers aiming but is currently pointing from hip
        testCharacter.firesFromAimingState = true;
        testCharacter.currentWeaponState = new WeaponState("pointedfromhip", "firing", 20);
        
        testCharacter.startPointingFromHipTiming(100);
        
        // Should NOT fire immediately because state doesn't match preference
        boolean shouldNotFireImmediately = testCharacter.isAlreadyInCorrectFiringState("pointedfromhip", 110);
        assertFalse(shouldNotFireImmediately, "Should not fire immediately when state doesn't match firing preference");
    }
    
    @Test
    public void testIsAlreadyInCorrectFiringState_NonFiringStates() {
        // Test with states that should never fire immediately
        testCharacter.firesFromAimingState = true;
        
        boolean shouldNotFireFromReady = testCharacter.isAlreadyInCorrectFiringState("ready", 110);
        assertFalse(shouldNotFireFromReady, "Should not fire immediately from ready state");
        
        boolean shouldNotFireFromDrawing = testCharacter.isAlreadyInCorrectFiringState("drawing", 110);
        assertFalse(shouldNotFireFromDrawing, "Should not fire immediately from drawing state");
        
        boolean shouldNotFireFromSlung = testCharacter.isAlreadyInCorrectFiringState("slung", 110);
        assertFalse(shouldNotFireFromSlung, "Should not fire immediately from slung state");
    }
    
    @Test
    public void testGetCurrentPointingFromHipDuration() {
        // Test the new getCurrentPointingFromHipDuration method
        testCharacter.startPointingFromHipTiming(100);
        
        long duration = testCharacter.getCurrentPointingFromHipDuration(125);
        assertEquals(25, duration, "Should return correct pointing from hip duration");
        
        // Test when not pointing
        testCharacter.resetAimingTiming();
        long noDuration = testCharacter.getCurrentPointingFromHipDuration(130);
        assertEquals(0, noDuration, "Should return 0 when not pointing from hip");
    }
}
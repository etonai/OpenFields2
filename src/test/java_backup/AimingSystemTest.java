package combat.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import combat.Character;
import combat.AimingSpeed;
import combat.Handedness;
import java.util.Date;

/**
 * Test class for AimingSystem functionality.
 */
public class AimingSystemTest {
    
    private AimingSystem aimingSystem;
    private Character testCharacter;
    private long currentTick;
    
    @BeforeEach
    void setUp() {
        aimingSystem = AimingSystem.getInstance();
        
        // Create test character
        testCharacter = new Character(1, "Test", "Test", "Character", new Date(), "modern",
                                     70, 100, 60, 50, 80, Handedness.RIGHT_HANDED);
        
        currentTick = 100;
    }
    
    @Test
    void testAimingTimingLifecycle() {
        int characterId = testCharacter.id;
        
        // Initially no timing active
        assertFalse(aimingSystem.isAimingTimingActive(characterId));
        assertEquals(0, aimingSystem.getAimingDuration(characterId, currentTick));
        
        // Start aiming timing
        aimingSystem.startAimingTiming(characterId, currentTick);
        assertTrue(aimingSystem.isAimingTimingActive(characterId));
        
        // Check duration after 30 ticks
        long laterTick = currentTick + 30;
        assertEquals(30, aimingSystem.getAimingDuration(characterId, laterTick));
        
        // Reset timing
        aimingSystem.resetAimingTiming(characterId);
        assertFalse(aimingSystem.isAimingTimingActive(characterId));
        assertEquals(0, aimingSystem.getAimingDuration(characterId, laterTick));
    }
    
    @Test
    void testPointingFromHipTimingLifecycle() {
        int characterId = testCharacter.id;
        
        // Initially no timing active
        assertFalse(aimingSystem.isPointingFromHipTimingActive(characterId));
        assertEquals(0, aimingSystem.getPointingFromHipDuration(characterId, currentTick));
        
        // Start pointing timing
        aimingSystem.startPointingFromHipTiming(characterId, currentTick);
        assertTrue(aimingSystem.isPointingFromHipTimingActive(characterId));
        
        // Check duration after 20 ticks
        long laterTick = currentTick + 20;
        assertEquals(20, aimingSystem.getPointingFromHipDuration(characterId, laterTick));
        
        // Reset timing
        aimingSystem.resetPointingFromHipTiming(characterId);
        assertFalse(aimingSystem.isPointingFromHipTimingActive(characterId));
        assertEquals(0, aimingSystem.getPointingFromHipDuration(characterId, laterTick));
    }
    
    @Test
    void testAimingAndPointingAreMutuallyExclusive() {
        int characterId = testCharacter.id;
        
        // Start aiming
        aimingSystem.startAimingTiming(characterId, currentTick);
        assertTrue(aimingSystem.isAimingTimingActive(characterId));
        assertFalse(aimingSystem.isPointingFromHipTimingActive(characterId));
        
        // Start pointing - should clear aiming
        aimingSystem.startPointingFromHipTiming(characterId, currentTick);
        assertFalse(aimingSystem.isAimingTimingActive(characterId));
        assertTrue(aimingSystem.isPointingFromHipTimingActive(characterId));
        
        // Start aiming again - should clear pointing
        aimingSystem.startAimingTiming(characterId, currentTick);
        assertTrue(aimingSystem.isAimingTimingActive(characterId));
        assertFalse(aimingSystem.isPointingFromHipTimingActive(characterId));
    }
    
    @Test
    void testAimingSpeedForMultipleShot() {
        // Set character's current aiming speed
        testCharacter.currentAimingSpeed = AimingSpeed.CAREFUL;
        
        // First shot should use character's current aiming speed
        testCharacter.currentShotInSequence = 1;
        assertEquals(AimingSpeed.CAREFUL, aimingSystem.getAimingSpeedForMultipleShot(testCharacter));
        
        // Subsequent shots should use QUICK
        testCharacter.currentShotInSequence = 2;
        assertEquals(AimingSpeed.QUICK, aimingSystem.getAimingSpeedForMultipleShot(testCharacter));
        
        testCharacter.currentShotInSequence = 3;
        assertEquals(AimingSpeed.QUICK, aimingSystem.getAimingSpeedForMultipleShot(testCharacter));
        
        testCharacter.currentShotInSequence = 4;
        assertEquals(AimingSpeed.QUICK, aimingSystem.getAimingSpeedForMultipleShot(testCharacter));
        
        testCharacter.currentShotInSequence = 5;
        assertEquals(AimingSpeed.QUICK, aimingSystem.getAimingSpeedForMultipleShot(testCharacter));
    }
    
    @Test
    void testDetermineAimingSpeedForShot() {
        testCharacter.currentAimingSpeed = AimingSpeed.NORMAL;
        
        // Single shot (shotInSequence = 0) should use character's current speed
        assertEquals(AimingSpeed.NORMAL, aimingSystem.determineAimingSpeedForShot(testCharacter, 0));
        
        // Multiple shot with sequence > 0 should delegate to getAimingSpeedForMultipleShot
        testCharacter.multipleShootCount = 3;
        testCharacter.currentShotInSequence = 2;
        assertEquals(AimingSpeed.QUICK, aimingSystem.determineAimingSpeedForShot(testCharacter, 2));
    }
    
    @Test
    void testCleanupCharacter() {
        int characterId = testCharacter.id;
        
        // Set up some timing
        aimingSystem.startAimingTiming(characterId, currentTick);
        assertTrue(aimingSystem.isAimingTimingActive(characterId));
        
        // Clean up
        aimingSystem.cleanupCharacter(characterId);
        
        // Verify all state is cleared
        assertFalse(aimingSystem.isAimingTimingActive(characterId));
        assertFalse(aimingSystem.isPointingFromHipTimingActive(characterId));
        assertEquals(0, aimingSystem.getAimingDuration(characterId, currentTick));
        assertEquals(0, aimingSystem.getPointingFromHipDuration(characterId, currentTick));
    }
}
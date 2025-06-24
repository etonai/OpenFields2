package combat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class BurstFiringTest {
    
    private Character shooter;
    private RangedWeapon testWeapon;
    
    @BeforeEach
    public void setUp() {
        // Create test character
        shooter = new Character("Test Shooter");
        shooter.setDexterity(75);
        shooter.setStrength(75);
        shooter.setReflexes(75);
        shooter.setCoolness(75);
        shooter.setCurrentHealth(100);
        
        // Create test weapon with burst capability
        testWeapon = new RangedWeapon(
            "Test SMG",           // name
            "9mm round",          // projectileName
            1300.0,               // velocity
            30,                   // damage
            32,                   // ammunition
            "/test_sound.wav",    // soundFile
            200.0,                // maximumRange
            0,                    // weaponAccuracy
            "SUBMACHINE_GUN"      // type
        );
        
        // Configure burst mode
        testWeapon.setFiringDelay(6);
        testWeapon.setCyclicRate(10); // Different from firingDelay to test correct usage
        testWeapon.setBurstSize(3);
        testWeapon.addAvailableFiringMode(FiringMode.SINGLE_SHOT);
        testWeapon.addAvailableFiringMode(FiringMode.BURST);
        testWeapon.addAvailableFiringMode(FiringMode.FULL_AUTO);
        
        shooter.setWeapon(testWeapon);
    }
    
    @Test
    public void testBurstModeSelection() {
        // Start in single shot mode
        assertEquals(FiringMode.SINGLE_SHOT, testWeapon.getCurrentFiringMode());
        
        // Cycle to burst mode
        shooter.cycleFiringMode();
        assertEquals(FiringMode.BURST, testWeapon.getCurrentFiringMode());
    }
    
    @Test
    public void testBurstPenaltyCalculation() {
        // Set to burst mode
        testWeapon.setCurrentFiringMode(FiringMode.BURST);
        
        // First shot - no penalty
        shooter.isAutomaticFiring = false;
        shooter.burstShotsFired = 0;
        assertFalse(shooter.shouldApplyBurstAutoPenalty());
        
        // Simulate burst start
        shooter.isAutomaticFiring = true;
        shooter.burstShotsFired = 1;
        assertFalse(shooter.shouldApplyBurstAutoPenalty()); // First shot still no penalty
        
        // Second shot - should have penalty
        shooter.burstShotsFired = 2;
        assertTrue(shooter.shouldApplyBurstAutoPenalty());
        
        // Third shot - should have penalty
        shooter.burstShotsFired = 3;
        assertTrue(shooter.shouldApplyBurstAutoPenalty());
    }
    
    @Test
    public void testFullAutoPenaltyCalculation() {
        // Set to full auto mode
        testWeapon.setCurrentFiringMode(FiringMode.FULL_AUTO);
        
        // First shot - no penalty
        shooter.isAutomaticFiring = true;
        shooter.burstShotsFired = 1;
        assertFalse(shooter.shouldApplyBurstAutoPenalty());
        
        // Subsequent shots - should have penalty
        for (int shot = 2; shot <= 10; shot++) {
            shooter.burstShotsFired = shot;
            assertTrue(shooter.shouldApplyBurstAutoPenalty(), 
                      "Shot " + shot + " should have burst/auto penalty");
        }
    }
    
    @Test
    public void testBurstInterruptionByModeSwitch() {
        // Set up burst in progress
        testWeapon.setCurrentFiringMode(FiringMode.BURST);
        shooter.isAutomaticFiring = true;
        shooter.burstShotsFired = 2;
        
        // Switch mode
        shooter.cycleFiringMode();
        
        // Verify burst was interrupted
        assertFalse(shooter.isAutomaticFiring);
        assertEquals(0, shooter.burstShotsFired);
        assertEquals(FiringMode.FULL_AUTO, testWeapon.getCurrentFiringMode());
    }
    
    @Test
    public void testBurstSizeConfiguration() {
        assertEquals(3, testWeapon.getBurstSize());
        
        // Test different burst size
        testWeapon.setBurstSize(5);
        assertEquals(5, testWeapon.getBurstSize());
    }
    
    @Test
    public void testFiringDelayVsCyclicRate() {
        // Ensure they can be different values
        assertEquals(6, testWeapon.getFiringDelay());
        assertEquals(10, testWeapon.getCyclicRate());
        
        // Burst timing should use firingDelay (tested via timing calculations)
        // This would be tested in integration tests with actual event scheduling
    }
    
    @Test
    public void testSingleShotNoPenalty() {
        // Single shot mode should never apply burst penalty
        testWeapon.setCurrentFiringMode(FiringMode.SINGLE_SHOT);
        
        shooter.isAutomaticFiring = false;
        shooter.burstShotsFired = 0;
        assertFalse(shooter.shouldApplyBurstAutoPenalty());
        
        // Even if somehow automatic firing is set
        shooter.isAutomaticFiring = true;
        shooter.burstShotsFired = 5;
        assertFalse(shooter.shouldApplyBurstAutoPenalty());
    }
}
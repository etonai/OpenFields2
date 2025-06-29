package combat;

import combat.managers.BurstFireManager;
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
            "test_smg",           // weaponId
            "Test SMG",           // name
            1300.0,               // velocityFeetPerSecond
            30,                   // damage
            32,                   // ammunition
            "/test_sound.wav",    // soundFile
            200.0,                // maximumRange
            0,                    // weaponAccuracy
            "9mm round"           // projectileName
        );
        
        // Configure burst mode
        testWeapon.setFiringDelay(6);
        testWeapon.setCyclicRate(10); // Different from firingDelay to test correct usage
        testWeapon.setBurstSize(3);
        testWeapon.getAvailableFiringModes().clear();
        testWeapon.getAvailableFiringModes().add(FiringMode.SINGLE_SHOT);
        testWeapon.getAvailableFiringModes().add(FiringMode.BURST);
        testWeapon.getAvailableFiringModes().add(FiringMode.FULL_AUTO);
        
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
        BurstFireManager.getInstance().setAutomaticFiring(shooter.id, false);
        BurstFireManager.getInstance().setBurstShotsFired(shooter.id, 0);
        assertFalse(BurstFireManager.getInstance().shouldApplyBurstAutoPenalty(shooter));
        
        // Simulate burst start
        BurstFireManager.getInstance().setAutomaticFiring(shooter.id, true);
        BurstFireManager.getInstance().setBurstShotsFired(shooter.id, 1);
        assertFalse(BurstFireManager.getInstance().shouldApplyBurstAutoPenalty(shooter)); // First shot still no penalty
        
        // Second shot - should have penalty
        BurstFireManager.getInstance().setBurstShotsFired(shooter.id, 2);
        assertTrue(BurstFireManager.getInstance().shouldApplyBurstAutoPenalty(shooter));
        
        // Third shot - should have penalty
        BurstFireManager.getInstance().setBurstShotsFired(shooter.id, 3);
        assertTrue(BurstFireManager.getInstance().shouldApplyBurstAutoPenalty(shooter));
    }
    
    @Test
    public void testFullAutoPenaltyCalculation() {
        // Set to full auto mode
        testWeapon.setCurrentFiringMode(FiringMode.FULL_AUTO);
        
        // First shot - no penalty
        BurstFireManager.getInstance().setAutomaticFiring(shooter.id, true);
        BurstFireManager.getInstance().setBurstShotsFired(shooter.id, 1);
        assertFalse(shooter.shouldApplyBurstAutoPenalty());
        
        // Subsequent shots - should have penalty
        for (int shot = 2; shot <= 10; shot++) {
            BurstFireManager.getInstance().setBurstShotsFired(shooter.id, shot);
            assertTrue(shooter.shouldApplyBurstAutoPenalty(), 
                      "Shot " + shot + " should have burst/auto penalty");
        }
    }
    
    @Test
    public void testBurstInterruptionByModeSwitch() {
        // Set up burst in progress
        testWeapon.setCurrentFiringMode(FiringMode.BURST);
        BurstFireManager.getInstance().setAutomaticFiring(shooter.id, true);
        BurstFireManager.getInstance().setBurstShotsFired(shooter.id, 2);
        
        // Switch mode
        shooter.cycleFiringMode();
        
        // Verify burst was interrupted
        assertFalse(shooter.isAutomaticFiring());
        assertEquals(0, shooter.getBurstShotsFired());
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
        
        BurstFireManager.getInstance().setAutomaticFiring(shooter.id, false);
        BurstFireManager.getInstance().setBurstShotsFired(shooter.id, 0);
        assertFalse(shooter.shouldApplyBurstAutoPenalty());
        
        // Even if somehow automatic firing is set
        BurstFireManager.getInstance().setAutomaticFiring(shooter.id, true);
        BurstFireManager.getInstance().setBurstShotsFired(shooter.id, 5);
        assertFalse(shooter.shouldApplyBurstAutoPenalty());
    }
}
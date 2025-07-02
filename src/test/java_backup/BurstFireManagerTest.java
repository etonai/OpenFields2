package combat.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import combat.Character;
import combat.RangedWeapon;
import combat.FiringMode;
import combat.Handedness;
import game.Unit;
import game.EventSchedulingService;
import game.GameClock;
import java.util.Date;
import java.util.PriorityQueue;
import game.ScheduledEvent;

/**
 * Test class for BurstFireManager functionality.
 */
public class BurstFireManagerTest {
    
    private BurstFireManager manager;
    private Character testCharacter;
    private RangedWeapon testWeapon;
    private Unit testUnit;
    private Unit targetUnit;
    private PriorityQueue<ScheduledEvent> eventQueue;
    private GameClock gameClock;
    
    @BeforeEach
    void setUp() {
        // Initialize event scheduling service
        eventQueue = new PriorityQueue<>();
        gameClock = new GameClock();
        EventSchedulingService.getInstance().initialize(eventQueue, gameClock);
        
        // Get manager instance
        manager = BurstFireManager.getInstance();
        
        // Create test character
        testCharacter = new Character(1, "Test", "Test", "Character", new Date(), "modern",
                                     70, 100, 60, 50, 80, Handedness.RIGHT_HANDED);
        
        // Create test weapon with burst capability
        testWeapon = new RangedWeapon("test_weapon", "Test Weapon", 1000.0, 20, 30, 
                                      "gun_shot.wav", 500.0, 75, "bullet");
        testWeapon.setFiringDelay(6); // 6 ticks between shots
        testWeapon.setBurstSize(3);
        testWeapon.setCyclicRate(6);
        testWeapon.getAvailableFiringModes().add(FiringMode.BURST);
        testWeapon.setCurrentFiringMode(FiringMode.BURST);
        
        testCharacter.weapon = testWeapon;
        
        // Create units
        testUnit = new Unit(testCharacter, 100, 100, null, 1);
        Character targetCharacter = new Character(2, "Target", "Target", "Character", new Date(), "modern",
                                                 60, 100, 50, 40, 60, Handedness.RIGHT_HANDED);
        targetUnit = new Unit(targetCharacter, 200, 100, null, 2);
    }
    
    @Test
    void testAutomaticFiringState() {
        // Initially not firing
        assertFalse(manager.isAutomaticFiring(testCharacter.id));
        
        // Set automatic firing
        manager.setAutomaticFiring(testCharacter.id, true);
        assertTrue(manager.isAutomaticFiring(testCharacter.id));
        
        // Clear automatic firing
        manager.setAutomaticFiring(testCharacter.id, false);
        assertFalse(manager.isAutomaticFiring(testCharacter.id));
    }
    
    @Test
    void testBurstShotsFired() {
        // Initially zero
        assertEquals(0, manager.getBurstShotsFired(testCharacter.id));
        
        // Set burst shots
        manager.setBurstShotsFired(testCharacter.id, 2);
        assertEquals(2, manager.getBurstShotsFired(testCharacter.id));
        
        // Clear burst shots
        manager.setBurstShotsFired(testCharacter.id, 0);
        assertEquals(0, manager.getBurstShotsFired(testCharacter.id));
    }
    
    @Test
    void testLastAutomaticShot() {
        // Initially zero
        assertEquals(0, manager.getLastAutomaticShot(testCharacter.id));
        
        // Set last shot tick
        manager.setLastAutomaticShot(testCharacter.id, 100);
        assertEquals(100, manager.getLastAutomaticShot(testCharacter.id));
    }
    
    @Test
    void testHandleContinuousFiring() {
        // Single shot mode should return false
        testWeapon.setCurrentFiringMode(FiringMode.SINGLE_SHOT);
        assertFalse(manager.handleContinuousFiring(testCharacter, targetUnit, 0));
        
        // Burst mode should return true
        testWeapon.setCurrentFiringMode(FiringMode.BURST);
        assertTrue(manager.handleContinuousFiring(testCharacter, targetUnit, 0));
        
        // Full auto mode should return true
        testWeapon.setCurrentFiringMode(FiringMode.FULL_AUTO);
        assertTrue(manager.handleContinuousFiring(testCharacter, targetUnit, 0));
    }
    
    @Test
    void testCleanupCharacter() {
        // Set some state
        manager.setAutomaticFiring(testCharacter.id, true);
        manager.setBurstShotsFired(testCharacter.id, 2);
        manager.setLastAutomaticShot(testCharacter.id, 100);
        
        // Verify state is set
        assertTrue(manager.isAutomaticFiring(testCharacter.id));
        assertEquals(2, manager.getBurstShotsFired(testCharacter.id));
        assertEquals(100, manager.getLastAutomaticShot(testCharacter.id));
        
        // Clean up
        manager.cleanupCharacter(testCharacter.id);
        
        // Verify state is cleared
        assertFalse(manager.isAutomaticFiring(testCharacter.id));
        assertEquals(0, manager.getBurstShotsFired(testCharacter.id));
        assertEquals(0, manager.getLastAutomaticShot(testCharacter.id));
    }
}
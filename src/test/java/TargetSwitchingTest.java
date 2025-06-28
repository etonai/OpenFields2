import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import combat.*;
import game.Unit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DevCycle 27: System 6 - Target Switch Aiming State Preservation
 */
public class TargetSwitchingTest {
    
    private combat.Character testCharacter;
    private Unit shooter;
    private Unit target1;
    private Unit target2;
    
    @BeforeEach
    public void setUp() {
        testCharacter = new combat.Character("TestChar", 70, 100, 60, 50, 55, combat.Handedness.RIGHT_HANDED);
        
        // Create test weapon with aiming states
        Weapon testWeapon = new Weapon();
        testWeapon.name = "Test Rifle";
        
        // Add weapon states
        testWeapon.states = new java.util.ArrayList<>();
        testWeapon.states.add(new WeaponState("ready", "pointedfromhip", 15));
        testWeapon.states.add(new WeaponState("pointedfromhip", "aiming", 20));
        testWeapon.states.add(new WeaponState("aiming", "firing", 30));
        
        testCharacter.weapon = testWeapon;
        testCharacter.currentWeaponState = testWeapon.getStateByName("ready");
        
        // Create test units
        shooter = new Unit(1, 100, 100, null, null);
        shooter.character = testCharacter;
        
        target1 = new Unit(2, 200, 100, null, null);
        target1.character = new combat.Character("Target1", 70, 100, 60, 50, 55, combat.Handedness.RIGHT_HANDED);
        
        target2 = new Unit(3, 150, 150, null, null);
        target2.character = new combat.Character("Target2", 70, 100, 60, 50, 55, combat.Handedness.RIGHT_HANDED);
    }
    
    @Test
    public void testGetOptimalStateForTargetSwitch_AimingPreference() {
        // Character prefers aiming state
        testCharacter.firesFromAimingState = true;
        
        // Test optimal state selection
        WeaponState optimalState = testCharacter.getOptimalStateForTargetSwitch();
        
        assertNotNull(optimalState, "Should return a valid weapon state");
        assertEquals("aiming", optimalState.getState(), "Should return aiming state for aiming preference");
    }
    
    @Test
    public void testGetOptimalStateForTargetSwitch_PointingPreference() {
        // Character prefers pointing from hip state
        testCharacter.firesFromAimingState = false;
        
        // Test optimal state selection
        WeaponState optimalState = testCharacter.getOptimalStateForTargetSwitch();
        
        assertNotNull(optimalState, "Should return a valid weapon state");
        assertEquals("pointedfromhip", optimalState.getState(), "Should return pointedfromhip state for pointing preference");
    }
    
    @Test
    public void testTargetSwitchPreservesAimingState() {
        // Set character to prefer aiming
        testCharacter.firesFromAimingState = true;
        
        // Set initial state to aiming
        testCharacter.currentWeaponState = testCharacter.weapon.getStateByName("aiming");
        testCharacter.startAimingTiming(100);
        testCharacter.currentTarget = target1;
        
        // Verify initial state
        assertEquals("aiming", testCharacter.currentWeaponState.getState());
        long initialDuration = testCharacter.getCurrentAimingDuration(150); // 50 ticks
        assertEquals(50, initialDuration);
        
        // Simulate target switch by calling the private method indirectly through state reset
        testCharacter.resetAimingTiming();
        WeaponState newState = testCharacter.getOptimalStateForTargetSwitch();
        testCharacter.currentWeaponState = newState;
        testCharacter.startTimingForTargetSwitchState(200); // New timing starts
        
        // Verify new state is aiming (not ready or pointedfromhip)
        assertEquals("aiming", testCharacter.currentWeaponState.getState(), 
                    "Should maintain aiming state after target switch for aiming preference");
        
        // Verify timing restarted
        long newDuration = testCharacter.getCurrentAimingDuration(220); // 20 ticks from restart
        assertEquals(20, newDuration, "Should have new timing counter for new target");
    }
    
    @Test
    public void testTargetSwitchPreservesPointingState() {
        // Set character to prefer pointing from hip
        testCharacter.firesFromAimingState = false;
        
        // Set initial state to pointedfromhip
        testCharacter.currentWeaponState = testCharacter.weapon.getStateByName("pointedfromhip");
        testCharacter.startPointingFromHipTiming(100);
        testCharacter.currentTarget = target1;
        
        // Verify initial state
        assertEquals("pointedfromhip", testCharacter.currentWeaponState.getState());
        
        // Simulate target switch
        testCharacter.resetAimingTiming();
        WeaponState newState = testCharacter.getOptimalStateForTargetSwitch();
        testCharacter.currentWeaponState = newState;
        testCharacter.startTimingForTargetSwitchState(200);
        
        // Verify new state is pointedfromhip (not ready or aiming)
        assertEquals("pointedfromhip", testCharacter.currentWeaponState.getState(), 
                    "Should maintain pointedfromhip state after target switch for pointing preference");
        
        // Verify timing restarted
        long newDuration = testCharacter.getCurrentPointingFromHipDuration(220);
        assertEquals(20, newDuration, "Should have new timing counter for new target");
    }
    
    @Test
    public void testStartTimingForTargetSwitchState() {
        // Test timing start for aiming state
        testCharacter.currentWeaponState = testCharacter.weapon.getStateByName("aiming");
        testCharacter.startTimingForTargetSwitchState(500);
        
        long aimingDuration = testCharacter.getAimingDuration(520);
        assertEquals(20, aimingDuration, "Should start aiming timing correctly");
        
        // Test timing start for pointedfromhip state
        testCharacter.resetAimingTiming();
        testCharacter.currentWeaponState = testCharacter.weapon.getStateByName("pointedfromhip");
        testCharacter.startTimingForTargetSwitchState(600);
        
        long pointingDuration = testCharacter.getPointingFromHipDuration(630);
        assertEquals(30, pointingDuration, "Should start pointing timing correctly");
    }
}
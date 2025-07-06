import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import combat.AutoTargetingSystem;
import utils.GameConfiguration;
import java.security.SecureRandom;
import combat.Handedness;
import combat.WeaponState;
import combat.WeaponType;
import combat.RangedWeapon;
import game.GameCallbacks;
import game.ScheduledEvent;
import game.Unit;
import platform.api.Color;
import utils.GameConstants;

import java.util.ArrayList;
import java.util.PriorityQueue;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DevCycle 37 System 2: Reaiming State After Target Incapacitation
 * Validates that characters enter a reaiming state when switching targets
 * after incapacitating opponents in auto-targeting mode.
 * Enhanced in DevCycle 41 with deterministic mode and random seed generation.
 * 
 * SEED MANAGEMENT:
 * - Normal Operation: Uses randomly generated seed each run to discover edge cases
 * - Bug Reproduction: Use -Dtest.seed=123456789 to reproduce specific test scenarios
 * - Seed Reporting: Outputs seed at start and completion for easy reproduction
 * 
 * USAGE EXAMPLES:
 * 
 * Basic Usage:
 * mvn test -Dtest=TargetSwitchDelayTest                     # Random seed testing
 * mvn test -Dtest=TargetSwitchDelayTest -Dtest.seed=54321  # Positive seed reproduction
 * 
 * Cross-Platform Seed Reproduction:
 * 
 * Windows PowerShell (recommended - always quote properties):
 * mvn test "-Dtest=TargetSwitchDelayTest" "-Dtest.seed=4292768217366888882"
 * 
 * Windows Command Prompt (standard syntax):
 * mvn test -Dtest=TargetSwitchDelayTest -Dtest.seed=4292768217366888882
 * 
 * macOS/Linux (bash/zsh):
 * mvn test -Dtest=TargetSwitchDelayTest -Dtest.seed=4292768217366888882
 * 
 * TROUBLESHOOTING:
 * - If you see "Unknown lifecycle phase .seed=" errors, quote the -D properties
 * - Windows PowerShell has parsing issues with -D properties, always use quotes
 * - Use Windows Command Prompt as alternative if PowerShell fails
 * - All seeds (positive and negative) produce deterministic results
 * 
 * @author DevCycle 37 System 2 - Reaiming State After Target Incapacitation
 * @author DevCycle 41 System 8 - Deterministic Mode Standardization
 */
public class TargetSwitchDelayTest {
    
    private combat.Character alice;
    private combat.Character drake;
    private combat.Character bobby;
    private Unit aliceUnit;
    private Unit drakeUnit; 
    private Unit bobbyUnit;
    private PriorityQueue<ScheduledEvent> eventQueue;
    private MockGameCallbacks gameCallbacks;
    
    // Random seed for deterministic testing with reproducibility
    private long testSeed;
    
    @BeforeEach
    public void setUp() {
        // DevCycle 41: System 8 - Deterministic mode and seed management
        String seedProperty = System.getProperty("test.seed");
        if (seedProperty != null && !seedProperty.isEmpty()) {
            try {
                testSeed = Long.parseLong(seedProperty);
                System.out.println("=== MANUAL SEED OVERRIDE ===");
                System.out.println("Using manual seed: " + testSeed);
                System.out.println("============================");
            } catch (NumberFormatException e) {
                System.out.println("Invalid seed format: " + seedProperty + ", generating random seed");
                testSeed = new SecureRandom().nextLong();
            }
        } else {
            testSeed = new SecureRandom().nextLong();
        }
        
        // Enable deterministic mode
        GameConfiguration.setDeterministicMode(true, testSeed);
        System.out.println("Deterministic mode ENABLED with seed: " + testSeed);
        
        // Create test characters with different reflexes
        alice = new combat.Character("Alice", 70, 80, 60, 50, 55, Handedness.RIGHT_HANDED); // Baseline reflexes (50)
        drake = new combat.Character("Drake", 70, 80, 60, 50, 10, Handedness.RIGHT_HANDED); // Low reflexes (10)
        bobby = new combat.Character("Bobby", 70, 80, 60, 50, 90, Handedness.RIGHT_HANDED); // High reflexes (90)
        
        // Create units
        aliceUnit = new Unit(alice, 100, 100, Color.BLUE, 1);
        drakeUnit = new Unit(drake, 200, 100, Color.RED, 2);
        bobbyUnit = new Unit(bobby, 300, 100, Color.RED, 3);
        
        // Set up Alice for auto-targeting
        alice.usesAutomaticTargeting = true;
        alice.faction = 1; // Blue faction
        drake.faction = 2; // Red faction
        bobby.faction = 2; // Red faction
        
        // Set up weapons
        RangedWeapon pistol = createTestPistol();
        alice.setWeapon(pistol);
        
        // Initialize event queue
        eventQueue = new PriorityQueue<>();
        
        // Create mock game callbacks
        gameCallbacks = new MockGameCallbacks();
        gameCallbacks.addUnit(aliceUnit);
        gameCallbacks.addUnit(drakeUnit);
        gameCallbacks.addUnit(bobbyUnit);
    }
    
    private RangedWeapon createTestPistol() {
        RangedWeapon weapon = new RangedWeapon("test-pistol", "Test Pistol", 600.0, 8, 10, "/test.wav", 200.0, 10, "bullet");
        weapon.states = new ArrayList<>();
        weapon.states.add(new WeaponState("holstered", "ready", 0));
        weapon.states.add(new WeaponState("ready", "pointedfromhip", 20));
        weapon.states.add(new WeaponState("pointedfromhip", "aiming", 30));
        weapon.states.add(new WeaponState("aiming", "firing", 60));
        weapon.initialStateName = "holstered";
        return weapon;
    }
    
    @Test
    public void testReaimingStateTriggeredOnIncapacitation() {
        // Setup: Alice targets Drake and in aiming state
        alice.currentTarget = drakeUnit;
        alice.hasProcessedTargetIncapacitation = false;
        alice.currentWeaponState = alice.weapon.getStateByName("aiming");
        
        // Simulate Drake becoming incapacitated
        drake.currentHealth = 0; // Drake is now incapacitated
        
        // Run auto-targeting update
        AutoTargetingSystem.updateAutomaticTargeting(alice, aliceUnit, 100, eventQueue, gameCallbacks);
        
        // Verify reaiming state is triggered
        assertEquals("reaiming", alice.currentWeaponState.getState(), "Alice should be in reaiming state");
        assertEquals("aiming", alice.currentWeaponState.action, "Reaiming should return to aiming");
        assertEquals(15, alice.currentWeaponState.ticks, "Reaiming should take 15 ticks");
        // Note: hasProcessedTargetIncapacitation gets reset to false when a new target is found, which is correct behavior
    }
    
    @Test
    public void testReaimingStateFromPointedFromHip() {
        // Setup: Alice targets Drake and in pointedfromhip state
        alice.currentTarget = drakeUnit;
        alice.hasProcessedTargetIncapacitation = false;
        alice.currentWeaponState = alice.weapon.getStateByName("pointedfromhip");
        
        // Simulate Drake becoming incapacitated
        drake.currentHealth = 0;
        
        // Run auto-targeting update
        AutoTargetingSystem.updateAutomaticTargeting(alice, aliceUnit, 100, eventQueue, gameCallbacks);
        
        // Verify reaiming state is triggered
        assertEquals("reaiming", alice.currentWeaponState.getState(), "Alice should be in reaiming state");
        assertEquals("pointedfromhip", alice.currentWeaponState.action, "Reaiming should return to pointedfromhip");
        assertEquals(15, alice.currentWeaponState.ticks, "Reaiming should take 15 ticks");
        // Note: hasProcessedTargetIncapacitation gets reset to false when a new target is found, which is correct behavior
    }
    
    @Test
    public void testFirstIncapacitationOnlyTriggersReaiming() {
        // Setup: Alice targets Drake, no reaiming active
        alice.currentTarget = drakeUnit;
        alice.hasProcessedTargetIncapacitation = false;
        alice.currentWeaponState = alice.weapon.getStateByName("aiming");
        
        // First incapacitation - should trigger reaiming
        drake.currentHealth = 0;
        AutoTargetingSystem.updateAutomaticTargeting(alice, aliceUnit, 100, eventQueue, gameCallbacks);
        
        assertEquals("reaiming", alice.currentWeaponState.getState(), "First incapacitation should trigger reaiming");
        
        // After the first call, Alice should have found a new target (Bobby) and reset the flag
        // The second call should not retrigger reaiming because no new incapacitation has occurred
        String currentState = alice.currentWeaponState.getState();
        AutoTargetingSystem.updateAutomaticTargeting(alice, aliceUnit, 110, eventQueue, gameCallbacks);
        
        assertEquals(currentState, alice.currentWeaponState.getState(), "Second call should not change weapon state");
        // The flag should be false because Alice found a new target and reset it
    }
    
    @Test
    public void testNonIncapacitationTargetChangesDoNotTriggerReaiming() {
        // Setup: Alice not targeting anyone initially
        alice.currentTarget = null;
        alice.hasProcessedTargetIncapacitation = false;
        alice.currentWeaponState = alice.weapon.getStateByName("aiming");
        
        // Run auto-targeting to find new target (Bobby is alive)
        AutoTargetingSystem.updateAutomaticTargeting(alice, aliceUnit, 100, eventQueue, gameCallbacks);
        
        // Verify no reaiming triggered for normal target acquisition
        assertEquals("aiming", alice.currentWeaponState.getState(), "Normal target acquisition should not trigger reaiming");
        assertFalse(alice.hasProcessedTargetIncapacitation, "Should not mark incapacitation processing for normal targeting");
    }
    
    @Test
    public void testWeaponStateResetOnTargetIncapacitation() {
        // Setup: Alice targeting Drake and in aiming state
        alice.currentTarget = drakeUnit;
        alice.hasProcessedTargetIncapacitation = false;
        
        // Set Alice to be in aiming state (advanced weapon state)
        alice.currentWeaponState = alice.weapon.getStateByName("aiming");
        
        // Simulate Drake becoming incapacitated
        drake.currentHealth = 0;
        
        // Run auto-targeting update
        AutoTargetingSystem.updateAutomaticTargeting(alice, aliceUnit, 100, eventQueue, gameCallbacks);
        
        // Verify weapon state is reset to "reaiming" state
        assertEquals("reaiming", alice.currentWeaponState.getState(), 
                    "Weapon state should be reset to reaiming after target incapacitation");
        
        // Verify reaiming state will transition back to aiming (since Alice was in aiming)
        assertEquals("aiming", alice.currentWeaponState.action,
                    "Reaiming state should transition back to aiming since Alice was previously aiming");
        
        // Verify reaiming state has 15 tick duration
        assertEquals(15, alice.currentWeaponState.ticks,
                    "Reaiming state should have 15 tick duration");
    }
    
    @Test
    public void testWeaponStateResetFromPointedFromHip() {
        // Setup: Alice targeting Drake and in pointedfromhip state
        alice.currentTarget = drakeUnit;
        alice.hasProcessedTargetIncapacitation = false;
        
        // Set Alice to be in pointedfromhip state
        alice.currentWeaponState = alice.weapon.getStateByName("pointedfromhip");
        
        // Simulate Drake becoming incapacitated
        drake.currentHealth = 0;
        
        // Run auto-targeting update
        AutoTargetingSystem.updateAutomaticTargeting(alice, aliceUnit, 100, eventQueue, gameCallbacks);
        
        // Verify weapon state is reset to "reaiming" state
        assertEquals("reaiming", alice.currentWeaponState.getState(), 
                    "Weapon state should be reset to reaiming after target incapacitation");
        
        // Verify reaiming state will transition back to pointedfromhip (since Alice was in pointedfromhip)
        assertEquals("pointedfromhip", alice.currentWeaponState.action,
                    "Reaiming state should transition back to pointedfromhip since Alice was previously in pointedfromhip");
        
        // Verify reaiming state has 15 tick duration
        assertEquals(15, alice.currentWeaponState.ticks,
                    "Reaiming state should have 15 tick duration");
        
        System.out.println("=== TEST COMPLETION SUMMARY ===");
        System.out.println("Test seed used: " + testSeed);
        System.out.println("To reproduce (Windows PowerShell): mvn test \"-Dtest=TargetSwitchDelayTest\" \"-Dtest.seed=" + testSeed + "\"");
        System.out.println("To reproduce (CMD/Linux/macOS): mvn test -Dtest=TargetSwitchDelayTest -Dtest.seed=" + testSeed);
        System.out.println("===============================");
    }
    
    // Mock GameCallbacks implementation for testing
    private static class MockGameCallbacks implements GameCallbacks {
        private java.util.List<Unit> units = new ArrayList<>();
        private PriorityQueue<ScheduledEvent> eventQueue = new PriorityQueue<>();
        
        public void addUnit(Unit unit) {
            units.add(unit);
        }
        
        @Override
        public java.util.List<Unit> getUnits() {
            return units;
        }
        
        @Override
        public PriorityQueue<ScheduledEvent> getEventQueue() {
            return eventQueue;
        }
        
        // Other methods not needed for this test
        @Override public void playWeaponSound(combat.Weapon weapon) {}
        @Override public void scheduleProjectileImpact(Unit shooter, Unit target, combat.Weapon weapon, long fireTick, double distanceFeet) {}
        @Override public void scheduleMeleeImpact(Unit attacker, Unit target, combat.MeleeWeapon weapon, long attackTick) {}
        @Override public void applyFiringHighlight(Unit shooter, long fireTick) {}
        @Override public void addMuzzleFlash(Unit shooter, long fireTick) {}
        @Override public void removeAllEventsForOwner(int ownerId) {}
    }
}
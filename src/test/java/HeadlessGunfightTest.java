import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import platform.TestPlatform;
import platform.api.Platform;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import combat.Character;
import combat.CombatCoordinator;
import combat.AimingSpeed;
import combat.MovementType;
import combat.Weapon;
import combat.RangedWeapon;
import combat.Handedness;
import game.Unit;
import game.GameCallbacks;
import game.ScheduledEvent;
import game.GameClock;
import java.util.Random;
import utils.GameConfiguration;

/**
 * Enhanced headless gunfight test using real OpenFields2 game instance - DevCycle 39.
 * 
 * This test provides a complete JavaFX-independent version of the gunfight test scenario,
 * using the real OpenFields2 game instance in headless mode to ensure actual combat
 * mechanics are tested rather than mock implementations.
 * 
 * Key features:
 * - Real OpenFields2 game instance running in headless mode
 * - Real CombatCoordinator and weapon systems
 * - Actual combat calculations and mechanics
 * - Deterministic execution with controlled randomness
 * - Performance advantages of headless operation
 * 
 * This test mirrors the exact sequence of GunfightTestAutomated but uses the actual
 * game systems for true validation of combat mechanics.
 * 
 * @author DevCycle 39 - Headless Combat Testing Enhancement
 */
public class HeadlessGunfightTest {
    
    private TestPlatform testPlatform;
    private OpenFields2 gameInstance;
    
    // Test configuration
    private static final String TEST_SAVE_FILE = "test_b.json";
    private static final int MAX_COMBAT_DURATION_SECONDS = 60;
    private static final int TICKS_PER_SECOND = 60;
    private static final int MAX_COMBAT_TICKS = MAX_COMBAT_DURATION_SECONDS * TICKS_PER_SECOND;
    
    // Test validation thresholds
    private static final int MIN_EXPECTED_SHOTS = 3; // Adjusted for manual combat events
    private static final int MIN_EXPECTED_DURATION_TICKS = 30;
    private static final double MIN_EXPECTED_HIT_RATE = 0.1; // 10% minimum hit rate
    
    @BeforeEach
    public void setUp() {
        System.out.println("=== HeadlessGunfightTest Setup ===");
        
        // Initialize headless platform
        testPlatform = new TestPlatform();
        assertTrue(testPlatform.initialize(800, 600, "Headless Gunfight Test"),
                  "TestPlatform should initialize successfully");
        
        // Create OpenFields2 instance in headless mode
        gameInstance = new OpenFields2(true);
        assertTrue(gameInstance.initializeHeadless(), "Game should initialize in headless mode");
        
        // Set up deterministic random seed for consistent test results
        GameConfiguration.setDeterministicMode(true, 12345L);
        System.out.println("✓ Deterministic mode enabled with seed 12345");
        
        System.out.println("✓ Headless gunfight test environment initialized");
    }
    
    @AfterEach
    public void tearDown() {
        System.out.println("=== HeadlessGunfightTest Teardown ===");
        
        if (gameInstance != null) {
            gameInstance.setPaused(true);
            // Game instance will be garbage collected
        }
        
        if (testPlatform != null) {
            testPlatform.shutdown();
        }
        
        // Reset deterministic mode to avoid interfering with other tests
        GameConfiguration.reset();
        System.out.println("✓ Deterministic mode reset");
        
        System.out.println("✓ Headless test environment cleaned up");
    }
    
    @Test
    public void testHeadlessGunfightSimulation() {
        System.out.println("Starting complete headless gunfight simulation...");
        
        // Step 1: Set up test characters (similar to GunfightTestAutomated)
        setupTestCharacters();
        
        // Step 2: Validate initial setup
        validateInitialSetup();
        
        // Step 3: Initialize combat scenario
        initializeCombatScenario();
        
        // Step 4: Run combat simulation
        runCombatSimulation();
        
        // Step 5: Validate combat results
        validateCombatResults();
        
        System.out.println("✓ Headless gunfight simulation completed successfully");
    }
    
    private void setupTestCharacters() {
        System.out.println("Setting up test characters...");
        
        try {
            // Load test_b.json save file like GunfightTestAutomated does
            gameInstance.getSaveGameController().loadGameFromTestSlot('b');
            
            System.out.println("✓ Test save file loaded (test_b.json)");
            
        } catch (Exception e) {
            fail("Failed to load test save file: " + e.getMessage());
        }
    }
    
    private void validateInitialSetup() {
        System.out.println("Validating initial test setup...");
        
        List<Unit> units = gameInstance.getUnits();
        
        // Find GunfighterAlpha and GunfighterBeta by their character IDs (like GunfightTestAutomated)
        Unit gunfighterAlphaUnit = null;
        Unit gunfighterBetaUnit = null;
        Character gunfighterAlpha = null;
        Character gunfighterBeta = null;
        
        for (Unit unit : units) {
            Character character = unit.character;
            if (character.id == -1002) {
                gunfighterAlpha = character;
                gunfighterAlphaUnit = unit;
            } else if (character.id == -2002) {
                gunfighterBeta = character;
                gunfighterBetaUnit = unit;
            }
        }
        
        // Validate we found both gunfighters
        assertNotNull(gunfighterAlpha, "Should find GunfighterAlpha (ID: -1002)");
        assertNotNull(gunfighterBeta, "Should find GunfighterBeta (ID: -2002)");
        assertNotNull(gunfighterAlphaUnit, "Should find GunfighterAlpha unit");
        assertNotNull(gunfighterBetaUnit, "Should find GunfighterBeta unit");
        
        // Validate character configuration like GunfightTestAutomated
        assertEquals(-1002, gunfighterAlpha.id, "GunfighterAlpha should have correct ID");
        assertEquals(50, gunfighterAlpha.dexterity, "GunfighterAlpha should have dexterity 50");
        assertEquals(50, gunfighterAlpha.currentHealth, "GunfighterAlpha should have 50 health");
        assertEquals(AimingSpeed.CAREFUL, gunfighterAlpha.getCurrentAimingSpeed(), "GunfighterAlpha should use careful aiming");
        
        assertEquals(-2002, gunfighterBeta.id, "GunfighterBeta should have correct ID");
        assertEquals(50, gunfighterBeta.dexterity, "GunfighterBeta should have dexterity 50");
        assertEquals(50, gunfighterBeta.currentHealth, "GunfighterBeta should have 50 health");
        assertEquals(AimingSpeed.NORMAL, gunfighterBeta.getCurrentAimingSpeed(), "GunfighterBeta should use normal aiming");
        
        // Validate positioning (30 feet apart = 210 pixels)
        double distance = Math.abs(gunfighterBetaUnit.x - gunfighterAlphaUnit.x);
        assertEquals(210.0, distance, 1.0, "Characters should be 210 pixels (30 feet) apart");
        
        // Validate weapons
        assertNotNull(gunfighterAlpha.weapon, "GunfighterAlpha should have a weapon");
        assertNotNull(gunfighterBeta.weapon, "GunfighterBeta should have a weapon");
        
        System.out.println("✓ Initial setup validation passed");
        System.out.println("  GunfighterAlpha: " + gunfighterAlpha.getName() + " at (" + gunfighterAlphaUnit.x + ", " + gunfighterAlphaUnit.y + ") health=" + gunfighterAlpha.currentHealth);
        System.out.println("  GunfighterBeta: " + gunfighterBeta.getName() + " at (" + gunfighterBetaUnit.x + ", " + gunfighterBetaUnit.y + ") health=" + gunfighterBeta.currentHealth);
    }
    
    private void initializeCombatScenario() {
        System.out.println("Initializing combat scenario...");
        
        List<Unit> units = gameInstance.getUnits();
        
        // Find GunfighterAlpha and GunfighterBeta by their character IDs
        Unit gunfighterAlphaUnit = null;
        Unit gunfighterBetaUnit = null;
        
        for (Unit unit : units) {
            Character character = unit.character;
            if (character.id == -1002) {
                gunfighterAlphaUnit = unit;
            } else if (character.id == -2002) {
                gunfighterBetaUnit = unit;
            }
        }
        
        if (gunfighterAlphaUnit != null && gunfighterBetaUnit != null) {
            // Set up mutual targeting using real game system (like GunfightTestAutomated)
            gunfighterAlphaUnit.combatTarget = gunfighterBetaUnit;
            gunfighterBetaUnit.combatTarget = gunfighterAlphaUnit;
            
            // Configure multiple shoot count for GunfighterBeta like GunfightTestAutomated
            gunfighterBetaUnit.character.multipleShootCount = 3;
            
            System.out.println("  " + gunfighterAlphaUnit.character.getName() + " targets " + gunfighterBetaUnit.character.getName());
            System.out.println("  " + gunfighterBetaUnit.character.getName() + " targets " + gunfighterAlphaUnit.character.getName());
            System.out.println("  GunfighterBeta multiple shoot count: " + gunfighterBetaUnit.character.multipleShootCount);
        }
        
        System.out.println("✓ Combat scenario initialized - characters ready to fight");
    }
    
    private void runCombatSimulation() {
        System.out.println("Running combat simulation...");
        
        long startTime = System.currentTimeMillis();
        
        // Unpause the game to start combat
        gameInstance.setPaused(false);
        
        // Run simulation for maximum duration or until combat ends
        boolean combatComplete = false;
        int ticksRun = 0;
        int maxTicks = MAX_COMBAT_DURATION_SECONDS * TICKS_PER_SECOND;
        
        while (!combatComplete && ticksRun < maxTicks) {
            // Run one tick of the game
            gameInstance.runSingleTick();
            ticksRun++;
            
            // Check if combat is complete (one character incapacitated)
            List<Unit> units = gameInstance.getUnits();
            Character gunfighterAlpha = null;
            Character gunfighterBeta = null;
            
            for (Unit unit : units) {
                Character character = unit.character;
                if (character.id == -1002) {
                    gunfighterAlpha = character;
                } else if (character.id == -2002) {
                    gunfighterBeta = character;
                }
            }
            
            if (gunfighterAlpha != null && gunfighterBeta != null) {
                if (gunfighterAlpha.isIncapacitated() || gunfighterBeta.isIncapacitated()) {
                    combatComplete = true;
                    System.out.println("Combat completed - one character incapacitated");
                }
            }
            
            // Occasional status update
            if (ticksRun % 600 == 0) { // Every 10 seconds
                System.out.println("  Combat running... " + (ticksRun / 60) + " seconds elapsed");
            }
        }
        
        // Pause the game
        gameInstance.setPaused(true);
        
        long endTime = System.currentTimeMillis();
        long realTimeMs = endTime - startTime;
        
        System.out.println("Combat simulation completed:");
        System.out.println("  Real time: " + realTimeMs + "ms");
        System.out.println("  Game ticks: " + ticksRun);
        System.out.println("  Game time: " + (ticksRun / 60.0) + " seconds");
        System.out.println("  Combat completed: " + combatComplete);
        
        assertTrue(combatComplete, "Combat should complete within time limit");
        assertTrue(ticksRun >= MIN_EXPECTED_DURATION_TICKS, "Combat should run for minimum duration");
    }
    
    
    private void validateCombatResults() {
        System.out.println("Validating combat results...");
        
        List<Unit> units = gameInstance.getUnits();
        
        // Find GunfighterAlpha and GunfighterBeta by their character IDs
        Character gunfighterAlpha = null;
        Character gunfighterBeta = null;
        
        for (Unit unit : units) {
            Character character = unit.character;
            if (character.id == -1002) {
                gunfighterAlpha = character;
            } else if (character.id == -2002) {
                gunfighterBeta = character;
            }
        }
        
        // Validate we found both gunfighters
        assertNotNull(gunfighterAlpha, "Should find GunfighterAlpha in results");
        assertNotNull(gunfighterBeta, "Should find GunfighterBeta in results");
        
        // Use the found characters for validation
        Character char1 = gunfighterAlpha;
        Character char2 = gunfighterBeta;
        
        // Validate that one character is incapacitated
        boolean char1Incapacitated = char1.isIncapacitated();
        boolean char2Incapacitated = char2.isIncapacitated();
        assertTrue(char1Incapacitated || char2Incapacitated, "One character should be incapacitated");
        
        // Validate combat statistics using real character stats
        int totalAttacks = char1.getAttacksAttempted() + char2.getAttacksAttempted();
        int totalHits = char1.getAttacksSuccessful() + char2.getAttacksSuccessful();
        
        assertTrue(totalAttacks >= MIN_EXPECTED_SHOTS, "Should have attempted minimum shots: " + totalAttacks);
        
        // Validate hit rate if shots were fired
        if (totalAttacks > 0) {
            double hitRate = (double) totalHits / totalAttacks;
            assertTrue(hitRate >= MIN_EXPECTED_HIT_RATE, "Hit rate should be reasonable: " + hitRate);
        }
        
        // Determine winner
        Character winner = null;
        if (char1Incapacitated && !char2Incapacitated) {
            winner = char2;
        } else if (char2Incapacitated && !char1Incapacitated) {
            winner = char1;
        }
        
        System.out.println("✓ Combat results validation passed");
        
        // Display detailed character stats like GunfightTestAutomated
        displayCharacterStats();
        
        // Print detailed validation results
        System.out.println("\nDetailed Combat Validation:");
        System.out.println("  " + char1.getName() + ": health=" + char1.currentHealth + ", attacks=" + char1.getAttacksAttempted() + ", hits=" + char1.getAttacksSuccessful());
        System.out.println("  " + char2.getName() + ": health=" + char2.currentHealth + ", attacks=" + char2.getAttacksAttempted() + ", hits=" + char2.getAttacksSuccessful());
        System.out.println("  Total Attacks: " + totalAttacks);
        System.out.println("  Total Hits: " + totalHits);
        System.out.println("  Hit Rate: " + (totalAttacks > 0 ? String.format("%.1f%%", (double) totalHits / totalAttacks * 100) : "N/A"));
        System.out.println("  Winner: " + (winner != null ? winner.getName() : "Draw"));
    }
    
    /**
     * Display character stats using DisplayCoordinator, exactly like GunfightTestAutomated
     */
    private void displayCharacterStats() {
        List<Unit> units = gameInstance.getUnits();
        
        // Find GunfighterAlpha and GunfighterBeta by their character IDs
        Unit gunfighterAlphaUnit = null;
        Unit gunfighterBetaUnit = null;
        
        for (Unit unit : units) {
            Character character = unit.character;
            if (character.id == -1002) {
                gunfighterAlphaUnit = unit;
            } else if (character.id == -2002) {
                gunfighterBetaUnit = unit;
            }
        }
        
        if (gunfighterAlphaUnit == null || gunfighterBetaUnit == null) {
            System.out.println("Could not find GunfighterAlpha and GunfighterBeta for stats display");
            return;
        }
        
        Unit gunfighter1 = gunfighterAlphaUnit;
        Unit gunfighter2 = gunfighterBetaUnit;
        
        // Create DisplayCoordinator instance directly since OpenFields2 implements InputManagerCallbacks
        try {
            // Cast gameInstance to InputManagerCallbacks since OpenFields2 implements it
            input.interfaces.InputManagerCallbacks callbacks = (input.interfaces.InputManagerCallbacks) gameInstance;
            
            // Create DisplayCoordinator with the required dependencies
            Object displayCoordinatorObj = createDisplayCoordinator(gameInstance.getSelectionManager(), gameInstance.getGameClock(), callbacks);
            
            if (displayCoordinatorObj != null) {
                // Display stats for Gunfighter1 using actual DisplayCoordinator
                System.out.println("=== " + gunfighter1.character.getName() + " Stats (via DisplayCoordinator) ===");
                callDisplayCharacterStats(displayCoordinatorObj, gunfighter1);
                
                System.out.println();  // Blank line between characters
                
                // Display stats for Gunfighter2 using actual DisplayCoordinator
                System.out.println("=== " + gunfighter2.character.getName() + " Stats (via DisplayCoordinator) ===");
                callDisplayCharacterStats(displayCoordinatorObj, gunfighter2);
                return; // Success - exit method
            }
            
            // Fallback if DisplayCoordinator creation failed
            System.out.println("DisplayCoordinator creation failed - using fallback stats display");
            displayBasicStats(gunfighter1, gunfighter1.character.getName());
            System.out.println();
            displayBasicStats(gunfighter2, gunfighter2.character.getName());
            
        } catch (Exception e) {
            System.err.println("Error creating DisplayCoordinator: " + e.getMessage());
            // Fallback to basic stats display
            displayBasicStats(gunfighter1, gunfighter1.character.getName());
            System.out.println();
            displayBasicStats(gunfighter2, gunfighter2.character.getName());
        }
    }
    
    /**
     * Simple fallback stats display if DisplayCoordinator is not available
     */
    private void displayBasicStats(Unit unit, String characterName) {
        Character character = unit.character;
        
        System.out.println("=== " + characterName + " Basic Stats ===");
        System.out.println("Health: " + character.currentHealth + "/" + character.health);
        System.out.println("Incapacitated: " + (character.isIncapacitated() ? "YES" : "NO"));
        
        // Calculate wounds inflicted manually since getTotalWoundsInflicted() might not exist
        int totalWoundsInflicted = character.woundsInflictedScratch + character.woundsInflictedLight + 
                                 character.woundsInflictedSerious + character.woundsInflictedCritical;
        
        System.out.println("Combat Stats:");
        System.out.println("  Attacks: " + character.getAttacksAttempted() + " attempted, " + character.getAttacksSuccessful() + " successful");
        System.out.println("  Wounds Inflicted: " + totalWoundsInflicted);
        System.out.println("=== End " + characterName + " Stats ===");
    }
    
    /**
     * Helper method to create DisplayCoordinator instance using reflection
     */
    private Object createDisplayCoordinator(SelectionManager selectionManager, GameClock gameClock, input.interfaces.InputManagerCallbacks callbacks) {
        try {
            Class<?> displayCoordinatorClass = Class.forName("DisplayCoordinator");
            java.lang.reflect.Constructor<?> constructor = displayCoordinatorClass.getConstructor(
                SelectionManager.class, GameClock.class, input.interfaces.InputManagerCallbacks.class);
            return constructor.newInstance(selectionManager, gameClock, callbacks);
        } catch (Exception e) {
            System.err.println("Failed to create DisplayCoordinator: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Helper method to call displayCharacterStats using reflection
     */
    private void callDisplayCharacterStats(Object displayCoordinator, Unit unit) {
        try {
            Class<?> displayCoordinatorClass = displayCoordinator.getClass();
            java.lang.reflect.Method method = displayCoordinatorClass.getMethod("displayCharacterStats", Unit.class);
            method.invoke(displayCoordinator, unit);
        } catch (Exception e) {
            System.err.println("Failed to call displayCharacterStats: " + e.getMessage());
            displayBasicStats(unit, unit.character.getName());
        }
    }
    
    /**
     * Helper method to access private fields using reflection
     */
    private Object getPrivateField(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            return null;
        }
    }
}
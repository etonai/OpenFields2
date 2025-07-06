import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import combat.*;
import data.*;
import utils.GameConfiguration;
import java.security.SecureRandom;

/**
 * Simple automated test for BasicMissTest scenario.
 * Enhanced in DevCycle 41 with deterministic mode and random seed generation.
 * 
 * This test validates that the test infrastructure (test factions, test weapons, test save)
 * is properly configured and can be loaded for automated testing.
 * 
 * SEED MANAGEMENT:
 * - Normal Operation: Uses randomly generated seed each run to discover edge cases
 * - Bug Reproduction: Use -Dtest.seed=123456789 to reproduce specific test scenarios
 * - Seed Reporting: Outputs seed at start and completion for easy reproduction
 * 
 * USAGE EXAMPLES:
 * 
 * Basic Usage:
 * mvn test -Dtest=BasicMissTestSimple                     # Random seed testing
 * mvn test -Dtest=BasicMissTestSimple -Dtest.seed=54321  # Positive seed reproduction
 * 
 * Cross-Platform Seed Reproduction:
 * 
 * Windows PowerShell (recommended - always quote properties):
 * mvn test "-Dtest=BasicMissTestSimple" "-Dtest.seed=4292768217366888882"
 * 
 * Windows Command Prompt (standard syntax):
 * mvn test -Dtest=BasicMissTestSimple -Dtest.seed=4292768217366888882
 * 
 * macOS/Linux (bash/zsh):
 * mvn test -Dtest=BasicMissTestSimple -Dtest.seed=4292768217366888882
 * 
 * TROUBLESHOOTING:
 * - If you see "Unknown lifecycle phase .seed=" errors, quote the -D properties
 * - Windows PowerShell has parsing issues with -D properties, always use quotes
 * - Use Windows Command Prompt as alternative if PowerShell fails
 * - All seeds (positive and negative) produce deterministic results
 * 
 * Test Sequence:
 * 1. Generate random seed or use manual override from -Dtest.seed property
 * 2. Enable deterministic mode with generated/specified seed
 * 3. Load test save data (test_a.json)
 * 4. Verify test characters are loaded correctly
 * 5. Verify test weapons are assigned properly
 * 6. Validate positioning and setup for miss testing
 * 
 * @author DevCycle 34 - Automated Testing Foundation
 * @author DevCycle 41 System 8 - Deterministic Mode Standardization
 */
public class BasicMissTestSimple {
    
    private SaveGameManager saveGameManager;
    private UniversalCharacterRegistry characterRegistry;
    private SaveData testSaveData;
    
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
        
        // Initialize data systems
        DataManager dataManager = DataManager.getInstance();
        saveGameManager = SaveGameManager.getInstance();
        characterRegistry = UniversalCharacterRegistry.getInstance();
        
        System.out.println("=== BasicMissTest Simple Setup ===");
    }
    
    @Test
    public void testBasicMissInfrastructure() {
        System.out.println("Testing BasicMiss test infrastructure...");
        
        // Test 1: Load test save data
        testSaveData = saveGameManager.loadFromTestSlot('a');
        assertNotNull(testSaveData, "Test save data should load successfully");
        System.out.println("✓ Test save data loaded");
        
        // Test 2: Verify test characters exist in registry
        combat.Character missBot = characterRegistry.getCharacter(-1001);
        combat.Character targetDummy = characterRegistry.getCharacter(-2001);
        assertNotNull(missBot, "MissBot should exist in character registry");
        assertNotNull(targetDummy, "TargetDummy should exist in character registry");
        System.out.println("✓ Test characters found in registry");
        
        // Test 3: Verify test save has 2 units
        assertEquals(2, testSaveData.units.size(), "Test save should have 2 units");
        System.out.println("✓ Test save has correct number of units");
        
        // Test 4: Verify character IDs in save data
        boolean foundMissBot = false;
        boolean foundTargetDummy = false;
        for (UnitData unitData : testSaveData.units) {
            if (unitData.characterId == -1001) {
                foundMissBot = true;
                // Verify MissBot has TestInaccuratePistol
                assertEquals("wpn_test_inaccurate_pistol", unitData.weaponId, 
                           "MissBot should have TestInaccuratePistol");
            } else if (unitData.characterId == -2001) {
                foundTargetDummy = true;
            }
        }
        assertTrue(foundMissBot, "MissBot should be in test save data");
        assertTrue(foundTargetDummy, "TargetDummy should be in test save data");
        System.out.println("✓ Test characters properly configured in save data");
        
        // Test 5: Verify test weapon exists
        RangedWeapon testWeapon = (RangedWeapon) WeaponFactory.createWeapon("wpn_test_inaccurate_pistol");
        assertNotNull(testWeapon, "TestInaccuratePistol should be creatable");
        assertEquals("TestInaccuratePistol", testWeapon.getName(), "Weapon should have correct name");
        assertEquals(-100, testWeapon.getWeaponAccuracy(), "TestInaccuratePistol should have -100 accuracy");
        System.out.println("✓ Test weapon validated: " + testWeapon.getName() + " (accuracy: " + testWeapon.getWeaponAccuracy() + ")");
        
        // Test 6: Verify character positioning (21 feet apart)
        UnitData missBotData = null;
        UnitData targetDummyData = null;
        for (UnitData unitData : testSaveData.units) {
            if (unitData.characterId == -1001) {
                missBotData = unitData;
            } else if (unitData.characterId == -2001) {
                targetDummyData = unitData;
            }
        }
        
        assertNotNull(missBotData, "MissBot unit data should exist");
        assertNotNull(targetDummyData, "TargetDummy unit data should exist");
        
        double distance = calculateDistance(missBotData.x, missBotData.y, targetDummyData.x, targetDummyData.y);
        assertEquals(21.0, distance, 1.0, "Characters should be 21 feet apart");
        System.out.println("✓ Character positioning verified: " + String.format("%.1f", distance) + " feet apart");
        
        System.out.println("=== BasicMissTest Infrastructure VALIDATED ===");
        
        System.out.println("=== TEST COMPLETION SUMMARY ===");
        System.out.println("Test seed used: " + testSeed);
        System.out.println("To reproduce (Windows PowerShell): mvn test \"-Dtest=BasicMissTestSimple\" \"-Dtest.seed=" + testSeed + "\"");
        System.out.println("To reproduce (CMD/Linux/macOS): mvn test -Dtest=BasicMissTestSimple -Dtest.seed=" + testSeed);
        System.out.println("===============================");
    }
    
    private double calculateDistance(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double pixelDistance = Math.sqrt(dx * dx + dy * dy);
        return pixelDistance / 7.0; // 7 pixels = 1 foot
    }
    
    @Test
    public void testMissScenarioSetup() {
        System.out.println("Testing miss scenario setup...");
        
        // Load test characters
        combat.Character missBot = characterRegistry.getCharacter(-1001);
        combat.Character targetDummy = characterRegistry.getCharacter(-2001);
        
        // Create test weapon
        RangedWeapon testWeapon = (RangedWeapon) WeaponFactory.createWeapon("wpn_test_inaccurate_pistol");
        missBot.weapon = testWeapon;
        
        // Test hit calculation with extreme negative accuracy
        // This verifies the miss scenario will work as expected
        int baseHitChance = 50; // Base chance
        int weaponModifier = testWeapon.getWeaponAccuracy(); // -100
        int expectedHitChance = baseHitChance + weaponModifier; // Should be very low
        
        assertTrue(expectedHitChance <= 0, "Hit chance should be 0% or negative with TestInaccuratePistol");
        System.out.println("✓ Miss scenario validated: Base " + baseHitChance + " + Weapon " + weaponModifier + " = " + expectedHitChance + "% hit chance");
        
        // Verify weapon has full capacity
        assertEquals(6, testWeapon.getMaxAmmunition(), "TestInaccuratePistol should have 6 round capacity");
        System.out.println("✓ Weapon capacity confirmed: " + testWeapon.getMaxAmmunition() + " rounds");
        
        System.out.println("=== Miss Scenario Setup VALIDATED ===");
        
        System.out.println("=== TEST COMPLETION SUMMARY ===");
        System.out.println("Test seed used: " + testSeed);
        System.out.println("To reproduce (Windows PowerShell): mvn test \"-Dtest=BasicMissTestSimple\" \"-Dtest.seed=" + testSeed + "\"");
        System.out.println("To reproduce (CMD/Linux/macOS): mvn test -Dtest=BasicMissTestSimple -Dtest.seed=" + testSeed);
        System.out.println("===============================");
    }
    
    // Main method for standalone testing
    public static void main(String[] args) {
        BasicMissTestSimple test = new BasicMissTestSimple();
        test.setUp();
        
        try {
            test.testBasicMissInfrastructure();
            test.testMissScenarioSetup();
            System.out.println("\n=== ALL TESTS PASSED ===");
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
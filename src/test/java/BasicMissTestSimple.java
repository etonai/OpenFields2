import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import combat.*;
import data.*;

/**
 * Simple automated test for BasicMissTest scenario.
 * 
 * This test validates that the test infrastructure (test factions, test weapons, test save)
 * is properly configured and can be loaded for automated testing.
 * 
 * Test Sequence:
 * 1. Load test save data (test_a.json)
 * 2. Verify test characters are loaded correctly
 * 3. Verify test weapons are assigned properly
 * 4. Validate positioning and setup for miss testing
 * 
 * @author DevCycle 34 - Automated Testing Foundation
 */
public class BasicMissTestSimple {
    
    private SaveGameManager saveGameManager;
    private UniversalCharacterRegistry characterRegistry;
    private SaveData testSaveData;
    
    @BeforeEach
    public void setUp() {
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
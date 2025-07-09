import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import combat.*;
import data.*;
import com.openfields.testutils.TestGameSetup;

/**
 * REFACTORED VERSION - Simple automated test for BasicMissTest scenario using new test utilities.
 * Enhanced in DevCycle 41 with deterministic mode and random seed generation.
 * Refactored in DevCycle 42 to use TestGameSetup utilities.
 * 
 * This test validates that the test infrastructure (test factions, test weapons, test save)
 * is properly configured and can be loaded for automated testing.
 * 
 * This version demonstrates the simplified setup using TestGameSetup utilities:
 * - Automatic seed generation and extraction
 * - Standardized deterministic mode setup
 * - Simplified setup and teardown patterns
 * 
 * @author DevCycle 34 - Automated Testing Foundation
 * @author DevCycle 41 System 8 - Deterministic Mode Standardization
 * @author DevCycle 42 - Test Utility Classes Implementation
 */
public class BasicMissTestSimpleRefactored {
    
    private SaveGameManager saveGameManager;
    private UniversalCharacterRegistry characterRegistry;
    private SaveData testSaveData;
    
    // Test seed managed by utilities
    private long testSeed;
    
    @BeforeEach
    public void setUp() {
        System.out.println("=== BasicMissTest Simple Setup (Refactored) ===");
        
        // Use TestGameSetup utilities for seed management and deterministic mode
        testSeed = TestGameSetup.generateOrExtractSeed();
        TestGameSetup.enableDeterministicMode(testSeed);
        
        // Initialize data systems
        DataManager dataManager = DataManager.getInstance();
        saveGameManager = SaveGameManager.getInstance();
        characterRegistry = UniversalCharacterRegistry.getInstance();
        
        System.out.println("✓ Setup completed using TestGameSetup utilities");
    }
    
    @AfterEach
    public void tearDown() {
        // Use TestGameSetup for consistent cleanup
        TestGameSetup.resetGlobalState();
        
        // Print seed information for reproduction
        TestGameSetup.printSeedInformation(testSeed);
        
        System.out.println("✓ Teardown completed using TestGameSetup utilities");
    }
    
    @Test
    public void testBasicMissInfrastructure() {
        System.out.println("Testing BasicMiss test infrastructure (refactored version)...");
        
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
                System.out.println("✓ Found MissBot in save data");
            } else if (unitData.characterId == -2001) {
                foundTargetDummy = true;
                System.out.println("✓ Found TargetDummy in save data");
            }
        }
        assertTrue(foundMissBot, "MissBot should be found in save data");
        assertTrue(foundTargetDummy, "TargetDummy should be found in save data");
        
        // Test 5: Verify test weapon exists and has expected properties
        RangedWeapon testWeapon = (RangedWeapon) data.WeaponFactory.createWeapon("wpn_test_inaccurate_pistol");
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
        assertNotNull(missBotData, "MissBot unit data should be found");
        assertNotNull(targetDummyData, "TargetDummy unit data should be found");
        
        double deltaX = targetDummyData.x - missBotData.x;
        double deltaY = targetDummyData.y - missBotData.y;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        double expectedDistance = 21.0 * 7; // 21 feet * 7 pixels per foot
        assertEquals(expectedDistance, distance, 1.0, "Characters should be 21 feet apart");
        System.out.println("✓ Character positioning verified: " + (distance / 7.0) + " feet apart");
        
        System.out.println("✅ BasicMiss infrastructure validation completed successfully (refactored)");
    }
}
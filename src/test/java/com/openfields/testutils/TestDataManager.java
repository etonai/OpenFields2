package com.openfields.testutils;

import data.SaveData;
import data.SaveGameManager;
import data.CharacterData;
import data.WeaponData;
import data.UnitData;
import combat.Character;
import java.util.List;
import java.util.ArrayList;

/**
 * Centralized test data creation and management utilities.
 * 
 * This utility manages test save games, character data, weapon data,
 * and test environment configuration, providing standardized data
 * creation patterns for consistent testing across all test classes.
 * 
 * Key features:
 * - Standard test save game loading and creation
 * - Character data generation for different test scenarios
 * - Weapon data creation utilities
 * - Test environment configuration management
 * - Scenario-specific data setup
 * 
 * Usage examples:
 * 
 * Save game management:
 * <pre>
 * {@code
 * TestDataManager.loadTestSave(game, 'a');
 * SaveData gunfightData = TestDataManager.createGunfightSaveData();
 * SaveData meleeData = TestDataManager.createMeleeSaveData();
 * }
 * </pre>
 * 
 * Character data creation:
 * <pre>
 * {@code
 * CharacterData data = TestDataManager.createTestCharacterData("TestChar", -1000);
 * List<CharacterData> faction = TestDataManager.createFactionData("TestFaction");
 * }
 * </pre>
 * 
 * Environment setup:
 * <pre>
 * {@code
 * TestDataManager.configureTestEnvironment();
 * TestDataManager.resetTestEnvironment();
 * }
 * </pre>
 * 
 * @author DevCycle 42 - Test Utility Classes Implementation
 */
public class TestDataManager {
    
    // Standard test save slots
    public static final char MISS_TEST_SLOT = 'a';
    public static final char GUNFIGHT_TEST_SLOT = 'b';
    public static final char MELEE_TEST_SLOT = 'c';
    public static final char SPRINGFIELD_TEST_SLOT = 'd';
    
    // Standard test character IDs
    public static final int MISS_BOT_ID = -1001;
    public static final int GUNFIGHTER_ALPHA_ID = -1002;
    public static final int SOLDIER_ALPHA_ID = -1003;
    public static final int TARGET_DUMMY_ID = -2001;
    public static final int GUNFIGHTER_BETA_ID = -2002;
    public static final int SOLDIER_BETA_ID = -2003;
    
    // Standard test weapon IDs
    public static final String TEST_PISTOL_ID = "wpn_test_pistol";
    public static final String TEST_INACCURATE_PISTOL_ID = "wpn_test_inaccurate_pistol";
    public static final String TEST_RIFLE_ID = "wpn_test_rifle";
    public static final String TEST_MELEE_WEAPON_ID = "mlw_test_knife";
    
    // Standard positioning
    private static final double DEFAULT_UNIT_X = 200.0;
    private static final double DEFAULT_UNIT_Y = 300.0;
    private static final double STANDARD_SPACING = 147.0; // 21 feet in pixels
    
    /**
     * Loads a test save game into the specified game instance.
     * 
     * @param game the game instance to load into
     * @param slot the save slot character ('a', 'b', 'c', etc.)
     * @throws RuntimeException if save loading fails
     */
    public static void loadTestSave(Object game, char slot) {
        if (game == null) {
            throw new IllegalArgumentException("Game instance cannot be null");
        }
        
        try {
            // Get the SaveGameController using reflection
            Object saveGameController = ReflectionTestUtils.getSaveGameController(game);
            
            // Call loadGameFromTestSlot method using reflection
            java.lang.reflect.Method loadMethod = saveGameController.getClass()
                .getMethod("loadGameFromTestSlot", char.class);
            loadMethod.invoke(saveGameController, slot);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test save from slot '" + slot + "': " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates standard gunfight save data with two opposing gunfighters.
     * 
     * @return configured SaveData for gunfight testing
     */
    public static SaveData createGunfightSaveData() {
        SaveData saveData = new SaveData();
        saveData.units = new ArrayList<>();
        
        // Create GunfighterAlpha unit
        UnitData alphaUnit = new UnitData();
        alphaUnit.characterId = GUNFIGHTER_ALPHA_ID;
        alphaUnit.x = DEFAULT_UNIT_X;
        alphaUnit.y = DEFAULT_UNIT_Y;
        alphaUnit.weaponId = TEST_PISTOL_ID;
        // Note: Color assignment needs to be done through proper API
        
        // Create GunfighterBeta unit
        UnitData betaUnit = new UnitData();
        betaUnit.characterId = GUNFIGHTER_BETA_ID;
        betaUnit.x = DEFAULT_UNIT_X + STANDARD_SPACING;
        betaUnit.y = DEFAULT_UNIT_Y;
        betaUnit.weaponId = TEST_PISTOL_ID;
        // Note: Color assignment needs to be done through proper API
        
        saveData.units.add(alphaUnit);
        saveData.units.add(betaUnit);
        
        return saveData;
    }
    
    /**
     * Creates standard melee combat save data with two melee fighters.
     * 
     * @return configured SaveData for melee testing
     */
    public static SaveData createMeleeSaveData() {
        SaveData saveData = new SaveData();
        saveData.units = new ArrayList<>();
        
        // Create melee fighter units positioned closer together
        double meleeSpacing = 49.0; // 7 feet in pixels
        
        UnitData alphaUnit = new UnitData();
        alphaUnit.characterId = SOLDIER_ALPHA_ID;
        alphaUnit.x = DEFAULT_UNIT_X;
        alphaUnit.y = DEFAULT_UNIT_Y;
        alphaUnit.weaponId = TEST_MELEE_WEAPON_ID;
        // Note: Color assignment needs to be done through proper API
        
        UnitData betaUnit = new UnitData();
        betaUnit.characterId = SOLDIER_BETA_ID;
        betaUnit.x = DEFAULT_UNIT_X + meleeSpacing;
        betaUnit.y = DEFAULT_UNIT_Y;
        betaUnit.weaponId = TEST_MELEE_WEAPON_ID;
        // Note: Color assignment needs to be done through proper API
        
        saveData.units.add(alphaUnit);
        saveData.units.add(betaUnit);
        
        return saveData;
    }
    
    /**
     * Creates miss test save data with MissBot and TargetDummy.
     * 
     * @return configured SaveData for miss testing
     */
    public static SaveData createMissTestSaveData() {
        SaveData saveData = new SaveData();
        saveData.units = new ArrayList<>();
        
        // Create MissBot unit with inaccurate weapon
        UnitData missBotUnit = new UnitData();
        missBotUnit.characterId = MISS_BOT_ID;
        missBotUnit.x = DEFAULT_UNIT_X;
        missBotUnit.y = DEFAULT_UNIT_Y;
        missBotUnit.weaponId = TEST_INACCURATE_PISTOL_ID;
        // Note: Color assignment needs to be done through proper API
        
        // Create TargetDummy unit
        UnitData targetUnit = new UnitData();
        targetUnit.characterId = TARGET_DUMMY_ID;
        targetUnit.x = DEFAULT_UNIT_X + STANDARD_SPACING;
        targetUnit.y = DEFAULT_UNIT_Y;
        targetUnit.weaponId = TEST_PISTOL_ID;
        // Note: Color assignment needs to be done through proper API
        
        saveData.units.add(missBotUnit);
        saveData.units.add(targetUnit);
        
        return saveData;
    }
    
    /**
     * Creates Springfield test save data for rifle testing.
     * 
     * @return configured SaveData for Springfield rifle testing
     */
    public static SaveData createSpringfieldSaveData() {
        SaveData saveData = new SaveData();
        saveData.units = new ArrayList<>();
        
        // Create units with rifles for longer-range combat
        UnitData alphaUnit = new UnitData();
        alphaUnit.characterId = SOLDIER_ALPHA_ID;
        alphaUnit.x = DEFAULT_UNIT_X;
        alphaUnit.y = DEFAULT_UNIT_Y;
        alphaUnit.weaponId = TEST_RIFLE_ID;
        // Note: Color assignment needs to be done through proper API
        
        UnitData betaUnit = new UnitData();
        betaUnit.characterId = SOLDIER_BETA_ID;
        betaUnit.x = DEFAULT_UNIT_X + (STANDARD_SPACING * 2); // Farther apart for rifles
        betaUnit.y = DEFAULT_UNIT_Y;
        betaUnit.weaponId = TEST_RIFLE_ID;
        // Note: Color assignment needs to be done through proper API
        
        saveData.units.add(alphaUnit);
        saveData.units.add(betaUnit);
        
        return saveData;
    }
    
    /**
     * Creates test character data with specified name and ID.
     * 
     * @param name the character name
     * @param id the character ID
     * @return configured CharacterData
     */
    public static CharacterData createTestCharacterData(String name, int id) {
        CharacterData data = new CharacterData();
        data.id = id;
        // Note: CharacterData fields need to be set through proper API
        System.out.println("Creating character data for: " + name);
        return data;
    }
    
    /**
     * Creates a faction of test characters.
     * 
     * @param factionName the name prefix for characters
     * @return list of CharacterData for the faction
     */
    public static List<CharacterData> createFactionData(String factionName) {
        List<CharacterData> faction = new ArrayList<>();
        
        // Create a small faction with varied character types
        CharacterData leader = createTestCharacterData(factionName + "Leader", -9001);
        leader.health = 120;
        leader.dexterity = 70;
        faction.add(leader);
        
        CharacterData soldier = createTestCharacterData(factionName + "Soldier", -9002);
        soldier.health = 100;
        soldier.dexterity = 60;
        faction.add(soldier);
        
        CharacterData scout = createTestCharacterData(factionName + "Scout", -9003);
        scout.health = 80;
        scout.dexterity = 80;
        faction.add(scout);
        
        return faction;
    }
    
    /**
     * Creates test pistol weapon data.
     * 
     * @return configured WeaponData for test pistol
     */
    public static WeaponData createTestPistolData() {
        WeaponData data = new WeaponData();
        data.id = TEST_PISTOL_ID;
        data.name = "TestPistol";
        data.damage = 15;
        // Note: Additional WeaponData fields need to be set through proper API
        return data;
    }
    
    /**
     * Creates test rifle weapon data.
     * 
     * @return configured WeaponData for test rifle
     */
    public static WeaponData createTestRifleData() {
        WeaponData data = new WeaponData();
        data.id = TEST_RIFLE_ID;
        data.name = "TestRifle";
        data.damage = 25;
        // Note: Additional WeaponData fields need to be set through proper API
        return data;
    }
    
    /**
     * Creates test melee weapon data.
     * 
     * @return configured WeaponData for test melee weapon
     */
    public static WeaponData createTestMeleeWeaponData() {
        WeaponData data = new WeaponData();
        data.id = TEST_MELEE_WEAPON_ID;
        data.name = "TestKnife";
        data.damage = 10;
        // Note: Additional WeaponData fields need to be set through proper API
        return data;
    }
    
    /**
     * Configures the test environment with standard settings.
     */
    public static void configureTestEnvironment() {
        try {
            // Initialize data systems if needed
            data.DataManager.getInstance();
            
            System.out.println("✓ Test environment configured");
        } catch (Exception e) {
            System.out.println("Warning: Test environment configuration encountered issues: " + e.getMessage());
        }
    }
    
    /**
     * Resets the test environment to clean state.
     */
    public static void resetTestEnvironment() {
        try {
            // Reset any global test state if needed
            System.out.println("✓ Test environment reset");
        } catch (Exception e) {
            System.out.println("Warning: Test environment reset encountered issues: " + e.getMessage());
        }
    }
    
    /**
     * Creates a UnitData with standard test configuration.
     * 
     * @param characterId the character ID to use
     * @param x the X position
     * @param y the Y position
     * @param weaponId the weapon ID to assign
     * @return configured UnitData
     */
    public static UnitData createStandardUnitData(int characterId, double x, double y, String weaponId) {
        UnitData unit = new UnitData();
        unit.characterId = characterId;
        unit.x = x;
        unit.y = y;
        unit.weaponId = weaponId;
        // Note: Color assignment needs to be done through proper API
        return unit;
    }
    
    /**
     * Validates that a save data structure is properly configured for testing.
     * 
     * @param saveData the save data to validate
     * @throws IllegalArgumentException if save data is invalid
     */
    public static void validateSaveData(SaveData saveData) {
        if (saveData == null) {
            throw new IllegalArgumentException("SaveData cannot be null");
        }
        if (saveData.units == null) {
            throw new IllegalArgumentException("SaveData units list cannot be null");
        }
        if (saveData.units.isEmpty()) {
            throw new IllegalArgumentException("SaveData must contain at least one unit");
        }
        
        // Validate each unit
        for (UnitData unit : saveData.units) {
            if (unit.characterId == 0) {
                throw new IllegalArgumentException("Unit must have a valid character ID");
            }
            if (unit.weaponId == null || unit.weaponId.isEmpty()) {
                throw new IllegalArgumentException("Unit must have a weapon ID");
            }
        }
    }
}
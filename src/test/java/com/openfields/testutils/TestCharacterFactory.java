package com.openfields.testutils;

import combat.Character;
import java.util.List;
import java.util.ArrayList;
import data.UniversalCharacterRegistry;

/**
 * Simplified character utilities for test scenarios.
 * 
 * This utility provides access to test characters from the registry.
 * 
 * Key features:
 * - Registry access helpers for standard test characters
 * - Character pair utilities for common test scenarios
 * - Character validation utilities
 * 
 * Usage examples:
 * 
 * Registry access:
 * <pre>
 * {@code
 * Character missBot = TestCharacterFactory.getMissBot();
 * Character[] gunfighters = TestCharacterFactory.getGunfighterPair();
 * TestCharacterFactory.validateTestCharactersExist();
 * }
 * </pre>
 * 
 * @author DevCycle 42 - Test Utility Classes Implementation
 */
public class TestCharacterFactory {
    
    // Standard test character IDs - matches registry
    public static final int MISS_BOT_ID = -1001;
    public static final int GUNFIGHTER_ALPHA_ID = -1002;
    public static final int SOLDIER_ALPHA_ID = -1003;
    public static final int TARGET_DUMMY_ID = -2001;
    public static final int GUNFIGHTER_BETA_ID = -2002;
    public static final int SOLDIER_BETA_ID = -2003;
    
    
    /**
     * Gets a test character from the registry by ID.
     * 
     * @param characterId the character ID (e.g., -1001 for MissBot)
     * @return the character from registry, or null if not found
     */
    public static Character getTestCharacter(int characterId) {
        UniversalCharacterRegistry registry = UniversalCharacterRegistry.getInstance();
        return registry.getCharacter(characterId);
    }
    
    /**
     * Gets MissBot from the registry.
     * 
     * @return MissBot character or null if not found
     */
    public static Character getMissBot() {
        return getTestCharacter(MISS_BOT_ID);
    }
    
    /**
     * Gets TargetDummy from the registry.
     * 
     * @return TargetDummy character or null if not found
     */
    public static Character getTargetDummy() {
        return getTestCharacter(TARGET_DUMMY_ID);
    }
    
    /**
     * Gets GunfighterAlpha from the registry.
     * 
     * @return GunfighterAlpha character or null if not found
     */
    public static Character getGunfighterAlpha() {
        return getTestCharacter(GUNFIGHTER_ALPHA_ID);
    }
    
    /**
     * Gets GunfighterBeta from the registry.
     * 
     * @return GunfighterBeta character or null if not found
     */
    public static Character getGunfighterBeta() {
        return getTestCharacter(GUNFIGHTER_BETA_ID);
    }
    
    /**
     * Gets GunfighterAlpha and GunfighterBeta as a pair for testing.
     * 
     * @return array with [GunfighterAlpha, GunfighterBeta]
     */
    public static Character[] getGunfighterPair() {
        return new Character[] {
            getTestCharacter(GUNFIGHTER_ALPHA_ID),
            getTestCharacter(GUNFIGHTER_BETA_ID)
        };
    }
    
    /**
     * Gets MissBot and TargetDummy as a pair for testing.
     * 
     * @return array with [MissBot, TargetDummy]
     */
    public static Character[] getMissTestPair() {
        return new Character[] {
            getTestCharacter(MISS_BOT_ID),
            getTestCharacter(TARGET_DUMMY_ID)
        };
    }
    
    /**
     * Gets all available test characters from the registry.
     * 
     * @return list of all test characters that exist in registry
     */
    public static List<Character> getAllTestCharacters() {
        List<Character> characters = new ArrayList<>();
        
        // Try to get all known test character IDs
        int[] testIds = {MISS_BOT_ID, GUNFIGHTER_ALPHA_ID, SOLDIER_ALPHA_ID, 
                        TARGET_DUMMY_ID, GUNFIGHTER_BETA_ID, SOLDIER_BETA_ID};
        
        for (int id : testIds) {
            Character character = getTestCharacter(id);
            if (character != null) {
                characters.add(character);
            }
        }
        
        return characters;
    }
    
    /**
     * Checks if a test character exists in the registry.
     * 
     * @param characterId the character ID to check
     * @return true if character exists, false otherwise
     */
    public static boolean testCharacterExists(int characterId) {
        return getTestCharacter(characterId) != null;
    }
    
    /**
     * Validates that all expected test characters exist in the registry.
     * 
     * @throws IllegalStateException if any required test characters are missing
     */
    public static void validateTestCharactersExist() {
        int[] requiredIds = {MISS_BOT_ID, TARGET_DUMMY_ID, GUNFIGHTER_ALPHA_ID, GUNFIGHTER_BETA_ID};
        List<Integer> missingIds = new ArrayList<>();
        
        for (int id : requiredIds) {
            if (!testCharacterExists(id)) {
                missingIds.add(id);
            }
        }
        
        if (!missingIds.isEmpty()) {
            throw new IllegalStateException("Missing required test characters: " + missingIds);
        }
    }
}
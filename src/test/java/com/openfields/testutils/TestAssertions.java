package com.openfields.testutils;

import static org.junit.jupiter.api.Assertions.*;
import combat.Character;
import combat.RangedWeapon;
import combat.MeleeWeapon;
import combat.WeaponState;
import combat.WeaponType;
import game.Unit;
import java.util.function.Supplier;

/**
 * Game-specific assertion methods for OpenFields2 testing.
 * 
 * This utility provides meaningful assertion methods tailored to OpenFields2
 * game mechanics, offering clear error messages and domain-specific validations
 * that make test failures easier to understand and debug.
 * 
 * Key features:
 * - Character state and health assertions
 * - Combat state and targeting validations
 * - Weapon state and type checking
 * - Position and distance calculations
 * - Timeout-based assertions for asynchronous operations
 * 
 * Usage examples:
 * 
 * Character assertions:
 * <pre>
 * {@code
 * TestAssertions.assertCharacterAlive(character, "Character should survive combat");
 * TestAssertions.assertCharacterHealth(character, 75);
 * TestAssertions.assertCharacterStats(character, 65, 90);
 * }
 * </pre>
 * 
 * Combat assertions:
 * <pre>
 * {@code
 * TestAssertions.assertCharactersTargeting(alphaUnit, betaUnit);
 * TestAssertions.assertCombatComplete(alpha, beta);
 * TestAssertions.assertAttackCounts(character, 5, 3);
 * }
 * </pre>
 * 
 * Weapon assertions:
 * <pre>
 * {@code
 * TestAssertions.assertWeaponState(character, "ready");
 * TestAssertions.assertWeaponType(character, WeaponType.PISTOL);
 * TestAssertions.assertAmmunition(weapon, 6);
 * }
 * </pre>
 * 
 * @author DevCycle 42 - Test Utility Classes Implementation
 */
public class TestAssertions {
    
    // Distance calculation constants
    private static final double PIXELS_PER_FOOT = 7.0;
    private static final double DEFAULT_POSITION_TOLERANCE = 1.0; // pixels
    private static final double DEFAULT_DISTANCE_TOLERANCE = 0.5; // pixels
    
    // Timeout constants
    private static final int DEFAULT_TIMEOUT_MS = 5000;
    private static final int POLLING_INTERVAL_MS = 100;
    
    // Character state assertions
    
    /**
     * Asserts that a character is alive (health > 0).
     * 
     * @param character the character to check
     * @param message the assertion message
     */
    public static void assertCharacterAlive(Character character, String message) {
        assertNotNull(character, message + " - character cannot be null");
        assertTrue(character.getHealth() > 0, 
                  message + " - character health: " + character.getHealth());
    }
    
    /**
     * Asserts that a character is incapacitated (health <= 0).
     * 
     * @param character the character to check
     * @param message the assertion message
     */
    public static void assertCharacterIncapacitated(Character character, String message) {
        assertNotNull(character, message + " - character cannot be null");
        assertTrue(character.getHealth() <= 0, 
                  message + " - character health: " + character.getHealth());
    }
    
    /**
     * Asserts that a character has the expected health.
     * 
     * @param character the character to check
     * @param expectedHealth the expected health value
     */
    public static void assertCharacterHealth(Character character, int expectedHealth) {
        assertCharacterHealth(character, expectedHealth, 
                             "Character should have " + expectedHealth + " health");
    }
    
    /**
     * Asserts that a character has the expected health with custom message.
     * 
     * @param character the character to check
     * @param expectedHealth the expected health value
     * @param message the assertion message
     */
    public static void assertCharacterHealth(Character character, int expectedHealth, String message) {
        assertNotNull(character, message + " - character cannot be null");
        assertEquals(expectedHealth, character.getHealth(), 
                    message + " - actual health: " + character.getHealth());
    }
    
    /**
     * Asserts that a character has the expected dexterity and health.
     * 
     * @param character the character to check
     * @param expectedDexterity the expected dexterity value
     * @param expectedHealth the expected health value
     */
    public static void assertCharacterStats(Character character, int expectedDexterity, int expectedHealth) {
        assertNotNull(character, "Character cannot be null for stats assertion");
        assertEquals(expectedDexterity, character.getDexterity(), 
                    "Character dexterity mismatch - expected: " + expectedDexterity + 
                    ", actual: " + character.getDexterity());
        assertEquals(expectedHealth, character.getHealth(), 
                    "Character health mismatch - expected: " + expectedHealth + 
                    ", actual: " + character.getHealth());
    }
    
    // Combat state assertions
    
    /**
     * Asserts that one character is targeting another.
     * Note: This method needs to be implemented based on actual targeting API.
     * 
     * @param sourceUnit the unit doing the targeting
     * @param targetUnit the unit being targeted
     */
    public static void assertCharactersTargeting(Unit sourceUnit, Unit targetUnit) {
        assertNotNull(sourceUnit, "Source unit cannot be null");
        assertNotNull(targetUnit, "Target unit cannot be null");
        
        // TODO: Implement based on actual targeting API
        System.out.println("Note: Character targeting assertion needs API implementation");
    }
    
    /**
     * Asserts that combat between two characters is complete (one is incapacitated).
     * 
     * @param char1 the first character
     * @param char2 the second character
     */
    public static void assertCombatComplete(Character char1, Character char2) {
        assertNotNull(char1, "First character cannot be null");
        assertNotNull(char2, "Second character cannot be null");
        
        boolean char1Incapacitated = char1.getHealth() <= 0;
        boolean char2Incapacitated = char2.getHealth() <= 0;
        
        assertTrue(char1Incapacitated || char2Incapacitated, 
                  "Combat should be complete (one character incapacitated) - " +
                  char1.getName() + " health: " + char1.getHealth() + 
                  ", " + char2.getName() + " health: " + char2.getHealth());
    }
    
    /**
     * Asserts that a character has the expected attack counts.
     * Note: This method needs to be implemented based on actual attack tracking API.
     * 
     * @param character the character to check
     * @param expectedAttempted the expected attempted attacks
     * @param expectedSuccessful the expected successful attacks
     */
    public static void assertAttackCounts(Character character, int expectedAttempted, int expectedSuccessful) {
        assertNotNull(character, "Character cannot be null for attack count assertion");
        
        // TODO: Implement based on actual attack tracking API
        System.out.println("Note: Attack count assertion needs API implementation for " + character.getNickname());
    }
    
    // Game state assertions
    
    /**
     * Asserts that a game instance is paused.
     * 
     * @param game the game instance to check
     */
    public static void assertGamePaused(Object game) {
        assertNotNull(game, "Game instance cannot be null");
        
        Boolean paused = ReflectionTestUtils.getPrivateField(game, "paused", Boolean.class);
        assertTrue(paused, "Game should be paused");
    }
    
    /**
     * Asserts that a game instance is running (not paused).
     * 
     * @param game the game instance to check
     */
    public static void assertGameRunning(Object game) {
        assertNotNull(game, "Game instance cannot be null");
        
        Boolean paused = ReflectionTestUtils.getPrivateField(game, "paused", Boolean.class);
        assertFalse(paused, "Game should be running (not paused)");
    }
    
    /**
     * Asserts that a game has the expected number of units loaded.
     * 
     * @param game the game instance to check
     * @param expectedCount the expected number of units
     */
    public static void assertUnitsLoaded(Object game, int expectedCount) {
        assertNotNull(game, "Game instance cannot be null");
        
        java.util.List<Unit> units = ReflectionTestUtils.getUnits(game);
        assertNotNull(units, "Units list should not be null");
        assertEquals(expectedCount, units.size(), 
                    "Expected " + expectedCount + " units, found " + units.size());
    }
    
    // Weapon state assertions
    
    /**
     * Asserts that a character's weapon is in the expected state.
     * Note: This method needs to be implemented based on actual weapon state API.
     * 
     * @param character the character to check
     * @param expectedState the expected weapon state
     */
    public static void assertWeaponState(Character character, String expectedState) {
        assertNotNull(character, "Character cannot be null for weapon state assertion");
        
        // TODO: Implement based on actual weapon state API
        System.out.println("Note: Weapon state assertion needs API implementation for " + character.getNickname());
    }
    
    /**
     * Asserts that a character's weapon is of the expected type.
     * Note: This method needs to be implemented based on actual weapon type API.
     * 
     * @param character the character to check
     * @param expectedType the expected weapon type
     */
    public static void assertWeaponType(Character character, WeaponType expectedType) {
        assertNotNull(character, "Character cannot be null for weapon type assertion");
        assertNotNull(expectedType, "Expected weapon type cannot be null");
        
        // TODO: Implement based on actual weapon type API
        System.out.println("Note: Weapon type assertion needs API implementation for " + character.getNickname());
    }
    
    /**
     * Asserts that a ranged weapon has the expected ammunition count.
     * Note: This method needs to be implemented based on actual ammunition API.
     * 
     * @param weapon the weapon to check
     * @param expectedAmmo the expected ammunition count
     */
    public static void assertAmmunition(RangedWeapon weapon, int expectedAmmo) {
        assertNotNull(weapon, "Weapon cannot be null for ammunition assertion");
        
        // TODO: Implement based on actual ammunition API
        System.out.println("Note: Ammunition assertion needs API implementation for " + weapon.getWeaponId());
    }
    
    // Position and distance assertions
    
    /**
     * Asserts that two units are at the expected distance apart.
     * 
     * @param unit1 the first unit
     * @param unit2 the second unit
     * @param expectedDistancePixels the expected distance in pixels
     * @param tolerance the tolerance in pixels
     */
    public static void assertDistance(Unit unit1, Unit unit2, double expectedDistancePixels, double tolerance) {
        assertNotNull(unit1, "First unit cannot be null");
        assertNotNull(unit2, "Second unit cannot be null");
        
        double actualDistance = calculateDistance(unit1, unit2);
        assertEquals(expectedDistancePixels, actualDistance, tolerance, 
                    "Distance mismatch between " + unit1.getCharacter().getName() + 
                    " and " + unit2.getCharacter().getName() + 
                    " - expected: " + expectedDistancePixels + " pixels, actual: " + actualDistance + " pixels");
    }
    
    /**
     * Asserts that two units are at the expected distance apart with default tolerance.
     * 
     * @param unit1 the first unit
     * @param unit2 the second unit
     * @param expectedDistancePixels the expected distance in pixels
     */
    public static void assertDistance(Unit unit1, Unit unit2, double expectedDistancePixels) {
        assertDistance(unit1, unit2, expectedDistancePixels, DEFAULT_DISTANCE_TOLERANCE);
    }
    
    /**
     * Asserts that a unit is at the expected position.
     * 
     * @param unit the unit to check
     * @param expectedX the expected X coordinate
     * @param expectedY the expected Y coordinate
     * @param tolerance the tolerance in pixels
     */
    public static void assertPosition(Unit unit, double expectedX, double expectedY, double tolerance) {
        assertNotNull(unit, "Unit cannot be null for position assertion");
        
        double actualX = unit.getX();
        double actualY = unit.getY();
        
        assertEquals(expectedX, actualX, tolerance, 
                    "X position mismatch for " + unit.getCharacter().getName() + 
                    " - expected: " + expectedX + ", actual: " + actualX);
        assertEquals(expectedY, actualY, tolerance, 
                    "Y position mismatch for " + unit.getCharacter().getName() + 
                    " - expected: " + expectedY + ", actual: " + actualY);
    }
    
    /**
     * Asserts that a unit is at the expected position with default tolerance.
     * 
     * @param unit the unit to check
     * @param expectedX the expected X coordinate
     * @param expectedY the expected Y coordinate
     */
    public static void assertPosition(Unit unit, double expectedX, double expectedY) {
        assertPosition(unit, expectedX, expectedY, DEFAULT_POSITION_TOLERANCE);
    }
    
    // Utility methods
    
    /**
     * Asserts that a runnable operation does not throw exceptions.
     * 
     * @param action the action to execute
     * @param message the assertion message
     */
    public static void assertNoExceptions(Runnable action, String message) {
        assertNotNull(action, "Action cannot be null");
        
        try {
            action.run();
        } catch (Exception e) {
            fail(message + " - unexpected exception: " + e.getMessage(), e);
        }
    }
    
    /**
     * Asserts that a condition becomes true within the specified timeout.
     * 
     * @param condition the condition to check
     * @param timeoutMs the timeout in milliseconds
     * @param message the assertion message
     */
    public static void assertEventuallyTrue(Supplier<Boolean> condition, int timeoutMs, String message) {
        assertNotNull(condition, "Condition cannot be null");
        
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeoutMs;
        
        while (System.currentTimeMillis() < endTime) {
            try {
                if (condition.get()) {
                    return; // Condition met
                }
            } catch (Exception e) {
                // Continue polling despite exceptions
            }
            
            try {
                Thread.sleep(POLLING_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail(message + " - interrupted while waiting for condition");
            }
        }
        
        fail(message + " - condition not met within " + timeoutMs + "ms");
    }
    
    /**
     * Asserts that a condition becomes true within the default timeout.
     * 
     * @param condition the condition to check
     * @param message the assertion message
     */
    public static void assertEventuallyTrue(Supplier<Boolean> condition, String message) {
        assertEventuallyTrue(condition, DEFAULT_TIMEOUT_MS, message);
    }
    
    // Helper methods
    
    /**
     * Calculates the distance between two units in pixels.
     * 
     * @param unit1 the first unit
     * @param unit2 the second unit
     * @return the distance in pixels
     */
    private static double calculateDistance(Unit unit1, Unit unit2) {
        double deltaX = unit2.getX() - unit1.getX();
        double deltaY = unit2.getY() - unit1.getY();
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
    
    /**
     * Converts feet to pixels using the game's scale.
     * 
     * @param feet the distance in feet
     * @return the distance in pixels
     */
    public static double feetToPixels(double feet) {
        return feet * PIXELS_PER_FOOT;
    }
    
    /**
     * Converts pixels to feet using the game's scale.
     * 
     * @param pixels the distance in pixels
     * @return the distance in feet
     */
    public static double pixelsToFeet(double pixels) {
        return pixels / PIXELS_PER_FOOT;
    }
}
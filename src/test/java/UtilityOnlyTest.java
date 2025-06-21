/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.paint.Color;

/**
 * Focused test suite for the utility classes extracted in Phase 3.
 * 
 * This test suite validates all the utility functionality without relying on
 * complex game object creation or external dependencies. It serves as regression
 * prevention for the Phase 3 utility extractions and documents expected behavior.
 * 
 * TESTING FOCUS:
 * - InputUtilities: Pure mathematical and validation functions
 * - DisplayHelpers: String formatting and display utilities
 * - InputConstants: Constant values and configurations
 * 
 * This approach provides comprehensive coverage of the extracted utility
 * functionality while avoiding complex dependencies and setup requirements.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UtilityOnlyTest {
    
    @Test
    @Order(1)
    @DisplayName("InputUtilities: Coordinate conversion is accurate")
    void testCoordinateConversion() {
        // Test pixels to feet conversion
        assertEquals(1.0, InputUtilities.pixelsToFeet(7.0), 0.001, 
                    "7 pixels should equal 1 foot");
        assertEquals(0.0, InputUtilities.pixelsToFeet(0.0), 0.001, 
                    "0 pixels should equal 0 feet");
        assertEquals(10.0, InputUtilities.pixelsToFeet(70.0), 0.001, 
                    "70 pixels should equal 10 feet");
        
        // Test feet to pixels conversion
        assertEquals(7.0, InputUtilities.feetToPixels(1.0), 0.001, 
                    "1 foot should equal 7 pixels");
        assertEquals(0.0, InputUtilities.feetToPixels(0.0), 0.001, 
                    "0 feet should equal 0 pixels");
        assertEquals(35.0, InputUtilities.feetToPixels(5.0), 0.001, 
                    "5 feet should equal 35 pixels");
        
        // Test round-trip conversion
        double originalPixels = 42.0;
        double convertedFeet = InputUtilities.pixelsToFeet(originalPixels);
        double backToPixels = InputUtilities.feetToPixels(convertedFeet);
        assertEquals(originalPixels, backToPixels, 0.001, 
                    "Round-trip conversion should preserve value");
    }
    
    @Test
    @Order(2)
    @DisplayName("InputUtilities: Distance calculations work correctly")
    void testDistanceCalculations() {
        // Test basic distance calculation
        assertEquals(0.0, InputUtilities.calculateDistance(0, 0, 0, 0), 0.001, 
                    "Distance from point to itself should be 0");
        assertEquals(5.0, InputUtilities.calculateDistance(0, 0, 3, 4), 0.001, 
                    "3-4-5 triangle should have distance 5");
        assertEquals(10.0, InputUtilities.calculateDistance(0, 0, 6, 8), 0.001, 
                    "6-8-10 triangle should have distance 10");
        
        // Test distance in feet
        assertEquals(1.0, InputUtilities.calculateDistanceInFeet(0, 0, 7, 0), 0.001, 
                    "7 pixels horizontal should be 1 foot");
        assertEquals(1.0, InputUtilities.calculateDistanceInFeet(0, 0, 0, 7), 0.001, 
                    "7 pixels vertical should be 1 foot");
        
        // Test negative coordinates
        assertEquals(5.0, InputUtilities.calculateDistance(-3, 0, 0, 4), 0.001, 
                    "Distance should work with negative coordinates");
    }
    
    @Test
    @Order(3)
    @DisplayName("InputUtilities: Validation methods work correctly")
    void testValidationMethods() {
        // Test slot number validation
        assertTrue(InputUtilities.isValidSlotNumber(1), "Slot 1 should be valid");
        assertTrue(InputUtilities.isValidSlotNumber(9), "Slot 9 should be valid");
        assertFalse(InputUtilities.isValidSlotNumber(0), "Slot 0 should be invalid");
        assertFalse(InputUtilities.isValidSlotNumber(10), "Slot 10 should be invalid");
        
        // Test batch quantity validation
        assertTrue(InputUtilities.isValidBatchQuantity(1), "Quantity 1 should be valid");
        assertTrue(InputUtilities.isValidBatchQuantity(20), "Quantity 20 should be valid");
        assertFalse(InputUtilities.isValidBatchQuantity(0), "Quantity 0 should be invalid");
        assertFalse(InputUtilities.isValidBatchQuantity(21), "Quantity 21 should be invalid");
        
        // Test spacing validation
        assertTrue(InputUtilities.isValidSpacing(1.0), "Spacing 1.0 should be valid");
        assertTrue(InputUtilities.isValidSpacing(9.0), "Spacing 9.0 should be valid");
        assertFalse(InputUtilities.isValidSpacing(0.5), "Spacing 0.5 should be invalid");
        assertFalse(InputUtilities.isValidSpacing(10.0), "Spacing 10.0 should be invalid");
        
        // Test numeric input validation
        assertTrue(InputUtilities.isValidNumericInput("123"), "Integer string should be valid");
        assertTrue(InputUtilities.isValidNumericInput("123.45"), "Decimal string should be valid");
        assertTrue(InputUtilities.isValidNumericInput("-123"), "Negative string should be valid");
        assertFalse(InputUtilities.isValidNumericInput("abc"), "Non-numeric string should be invalid");
        assertFalse(InputUtilities.isValidNumericInput(""), "Empty string should be invalid");
        assertFalse(InputUtilities.isValidNumericInput(null), "Null string should be invalid");
    }
    
    @Test
    @Order(4)
    @DisplayName("InputUtilities: Mathematical utilities are accurate")
    void testMathematicalUtilities() {
        // Test clamping
        assertEquals(5.0, InputUtilities.clamp(3.0, 5.0, 10.0), 0.001, 
                    "Value below range should be clamped to minimum");
        assertEquals(10.0, InputUtilities.clamp(15.0, 5.0, 10.0), 0.001, 
                    "Value above range should be clamped to maximum");
        assertEquals(7.0, InputUtilities.clamp(7.0, 5.0, 10.0), 0.001, 
                    "Value within range should be unchanged");
        
        // Test range checking
        assertTrue(InputUtilities.isInRange(7.0, 5.0, 10.0), 
                  "Value within range should return true");
        assertFalse(InputUtilities.isInRange(3.0, 5.0, 10.0), 
                   "Value below range should return false");
        assertFalse(InputUtilities.isInRange(15.0, 5.0, 10.0), 
                   "Value above range should return false");
        
        // Test percentage calculation
        assertEquals(50.0, InputUtilities.calculatePercentage(1.0, 2.0), 0.001, 
                    "50% calculation should be correct");
        assertEquals(0.0, InputUtilities.calculatePercentage(0.0, 5.0), 0.001, 
                    "0% calculation should be correct");
        assertEquals(0.0, InputUtilities.calculatePercentage(5.0, 0.0), 0.001, 
                    "Division by zero should return 0");
    }
    
    @Test
    @Order(5)
    @DisplayName("DisplayHelpers: Coordinate formatting works correctly")
    void testCoordinateFormatting() {
        // Test coordinate formatting
        assertEquals("(10.5, 20.5)", DisplayHelpers.formatCoordinates(10.5, 20.5), 
                    "Coordinates should be formatted correctly");
        assertEquals("(0.0, 0.0)", DisplayHelpers.formatCoordinates(0, 0), 
                    "Zero coordinates should be formatted correctly");
        
        // Test coordinate formatting with custom precision
        assertEquals("(10.50, 20.50)", DisplayHelpers.formatCoordinates(10.5, 20.5, 2), 
                    "Coordinates should use custom precision");
        
        // Test large number formatting
        assertEquals("1.0K", DisplayHelpers.formatLargeNumber(1000), 
                    "Thousands should be formatted with K");
        assertEquals("1.5M", DisplayHelpers.formatLargeNumber(1500000), 
                    "Millions should be formatted with M");
        assertEquals("500", DisplayHelpers.formatLargeNumber(500), 
                    "Small numbers should remain unchanged");
    }
    
    @Test
    @Order(6)
    @DisplayName("DisplayHelpers: Faction and color utilities work correctly")
    void testFactionAndColorUtilities() {
        // Test faction display names
        assertEquals("Cowboys", DisplayHelpers.getFactionDisplayName(1), 
                    "Faction 1 should be Cowboys");
        assertEquals("Outlaws", DisplayHelpers.getFactionDisplayName(2), 
                    "Faction 2 should be Outlaws");
        assertEquals("Lawmen", DisplayHelpers.getFactionDisplayName(3), 
                    "Faction 3 should be Lawmen");
        assertTrue(DisplayHelpers.getFactionDisplayName(99).contains("Unknown"), 
                  "Unknown faction should contain 'Unknown'");
        
        // Test faction colors
        assertEquals(Color.BLUE, DisplayHelpers.getFactionColor(1), 
                    "Cowboys should be blue");
        assertEquals(Color.RED, DisplayHelpers.getFactionColor(2), 
                    "Outlaws should be red");
        assertEquals(Color.GREEN, DisplayHelpers.getFactionColor(3), 
                    "Lawmen should be green");
        assertEquals(Color.BLACK, DisplayHelpers.getFactionColor(99), 
                    "Unknown faction should be black");
        
        // Test color display names
        assertEquals("Red", DisplayHelpers.getColorDisplayName(Color.RED), 
                    "Red color should be identified");
        assertEquals("Blue", DisplayHelpers.getColorDisplayName(Color.BLUE), 
                    "Blue color should be identified");
        assertEquals("Unknown", DisplayHelpers.getColorDisplayName(null), 
                    "Null color should return Unknown");
        
        // Test archetype display names
        assertEquals("Gunslinger", DisplayHelpers.getArchetypeDisplayName(1), 
                    "Archetype 1 should be Gunslinger");
        assertEquals("Soldier", DisplayHelpers.getArchetypeDisplayName(2), 
                    "Archetype 2 should be Soldier");
        assertTrue(DisplayHelpers.getArchetypeDisplayName(99).contains("Unknown"), 
                  "Unknown archetype should contain 'Unknown'");
    }
    
    @Test
    @Order(7)
    @DisplayName("DisplayHelpers: Status message formatting works correctly")
    void testStatusMessageFormatting() {
        // Test movement message formatting
        String moveMessage = DisplayHelpers.formatMovementMessage(3, 100.5, 200.5);
        assertTrue(moveMessage.contains("MOVE"), "Should contain MOVE command");
        assertTrue(moveMessage.contains("3"), "Should contain unit count");
        assertTrue(moveMessage.contains("units"), "Should use plural 'units'");
        assertTrue(moveMessage.contains("(100.5, 200.5)"), "Should contain coordinates");
        
        String singleMoveMessage = DisplayHelpers.formatMovementMessage(1, 50.0, 75.0);
        assertTrue(singleMoveMessage.contains("unit") && !singleMoveMessage.contains("units"), 
                  "Should use singular 'unit' for count of 1");
        
        // Test attack message formatting
        String attackMessage = DisplayHelpers.formatAttackMessage("Attacker", "Target", "Rifle");
        assertEquals("Attacker attacks Target with Rifle", attackMessage, 
                    "Attack message should be formatted correctly");
        
        // Test selection message formatting
        assertEquals("No units selected", DisplayHelpers.formatSelectionMessage(0), 
                    "Zero selection should be handled correctly");
        assertEquals("1 unit selected", DisplayHelpers.formatSelectionMessage(1), 
                    "Single selection should be handled correctly");
        assertEquals("5 units selected", DisplayHelpers.formatSelectionMessage(5), 
                    "Multiple selection should be handled correctly");
    }
    
    @Test
    @Order(8)
    @DisplayName("DisplayHelpers: Health and percentage formatting works correctly")
    void testHealthAndPercentageFormatting() {
        // Test health status formatting
        String healthStatus = DisplayHelpers.formatHealthStatus(75, 100);
        assertTrue(healthStatus.contains("75/100"), "Health status should contain current/max");
        assertTrue(healthStatus.contains("75.0%"), "Health status should contain percentage");
        assertTrue(healthStatus.contains("["), "Health status should contain health bar");
        
        // Test health bar generation
        String fullHealthBar = DisplayHelpers.generateHealthBar(100.0);
        assertTrue(fullHealthBar.contains("█"), "Full health bar should contain filled segments");
        
        String halfHealthBar = DisplayHelpers.generateHealthBar(50.0);
        assertTrue(halfHealthBar.contains("█") && halfHealthBar.contains("░"), 
                  "Half health bar should contain both filled and empty segments");
        
        String emptyHealthBar = DisplayHelpers.generateHealthBar(0.0);
        assertTrue(emptyHealthBar.contains("░"), "Empty health bar should contain empty segments");
        
        // Test percentage formatting
        assertEquals("75.0%", DisplayHelpers.formatPercentage(75.0), 
                    "Percentage should be formatted correctly");
    }
    
    @Test
    @Order(9)
    @DisplayName("InputConstants: All constants have expected values")
    void testInputConstantsValues() {
        // Test conversion constants
        assertEquals(7.0, InputConstants.PIXELS_PER_FOOT, 0.001, 
                    "Pixels per foot should be 7.0");
        assertEquals(1.0/7.0, InputConstants.FEET_PER_PIXEL, 0.001, 
                    "Feet per pixel should be 1/7");
        
        // Test validation limits
        assertEquals(9, InputConstants.MAX_SLOT_NUMBER, "Max slot should be 9");
        assertEquals(1, InputConstants.MIN_SLOT_NUMBER, "Min slot should be 1");
        assertEquals(20, InputConstants.MAX_BATCH_QUANTITY, "Max batch quantity should be 20");
        assertEquals(1, InputConstants.MIN_BATCH_QUANTITY, "Min batch quantity should be 1");
        assertEquals(9.0, InputConstants.MAX_SPACING_FEET, 0.001, "Max spacing should be 9.0 feet");
        assertEquals(1.0, InputConstants.MIN_SPACING_FEET, 0.001, "Min spacing should be 1.0 feet");
        
        // Test array lengths match counts
        assertEquals(InputConstants.ARCHETYPE_COUNT, InputConstants.ARCHETYPE_NAMES.length, 
                    "Archetype count should match array length");
        assertEquals(InputConstants.FACTION_COUNT, InputConstants.FACTION_NAMES.length, 
                    "Faction count should match array length");
        assertEquals(6, InputConstants.ARCHETYPE_COUNT, "Should have 6 archetypes");
        assertEquals(9, InputConstants.FACTION_COUNT, "Should have 9 factions");
        
        // Test file patterns
        assertEquals("factions/%d.json", InputConstants.FACTION_FILE_PATTERN, 
                    "Faction file pattern should be correct");
        assertEquals("saves/slot_%d.json", InputConstants.SAVE_FILE_PATTERN, 
                    "Save file pattern should be correct");
        
        // Test performance constants are reasonable
        assertEquals(60, InputConstants.GAME_FPS, "Game FPS should be 60");
        assertTrue(InputConstants.TARGET_FRAME_TIME_MS > 0, "Target frame time should be positive");
        assertTrue(InputConstants.PERFORMANCE_WARNING_THRESHOLD_MS > 0, "Performance threshold should be positive");
    }
    
    @Test
    @Order(10)
    @DisplayName("Integration: Utility classes work together correctly")
    void testUtilityIntegration() {
        // Test that utilities work together in realistic scenarios
        
        // Scenario 1: Character positioning and distance calculation
        double unit1X = 50.0;
        double unit1Y = 50.0;
        double unit2X = 100.0;
        double unit2Y = 100.0;
        
        double distance = InputUtilities.calculateDistance(unit1X, unit1Y, unit2X, unit2Y);
        double distanceInFeet = InputUtilities.pixelsToFeet(distance);
        String formattedDistance = DisplayHelpers.formatDistance(distance, true);
        
        assertTrue(distance > 0, "Distance should be positive");
        assertTrue(distanceInFeet > 0, "Distance in feet should be positive");
        assertTrue(formattedDistance.contains("ft"), "Formatted distance should include feet unit");
        
        // Scenario 2: Coordinate display with utility formatting
        String coordinates1 = DisplayHelpers.formatCoordinates(unit1X, unit1Y);
        String coordinates2 = DisplayHelpers.formatCoordinates(unit2X, unit2Y);
        
        assertTrue(coordinates1.contains("50.0"), "Should contain X coordinate");
        assertTrue(coordinates2.contains("100.0"), "Should contain coordinates");
        
        // Scenario 3: Movement message with multiple units
        int selectedCount = 3;
        String moveMessage = DisplayHelpers.formatMovementMessage(selectedCount, unit2X, unit2Y);
        
        assertTrue(moveMessage.contains("MOVE"), "Should contain move command");
        assertTrue(moveMessage.contains("3"), "Should contain unit count");
        assertTrue(moveMessage.contains("units"), "Should use plural form");
        assertTrue(moveMessage.contains(coordinates2), "Should contain target coordinates");
        
        // Scenario 4: Validation with constants
        int testSlot = 5;
        boolean isValidSlot = InputUtilities.isValidSlotNumber(testSlot);
        assertTrue(isValidSlot, "Slot 5 should be valid according to constants");
        
        assertEquals(InputConstants.MAX_SLOT_NUMBER >= testSlot, isValidSlot, 
                    "Validation should match constant limits");
    }
    
    @Test
    @Order(11)
    @DisplayName("Edge Cases: All utilities handle edge cases gracefully")
    void testEdgeCases() {
        // Test InputUtilities edge cases
        assertEquals(0.0, InputUtilities.pixelsToFeet(0.0), 0.001, "Zero conversion should work");
        assertEquals(0.0, InputUtilities.calculateDistance(5, 5, 5, 5), 0.001, "Same point distance should be 0");
        assertEquals(0.0, InputUtilities.calculatePercentage(0, 100), 0.001, "Zero percentage should work");
        assertEquals(0.0, InputUtilities.calculatePercentage(50, 0), 0.001, "Division by zero should return 0");
        
        // Test DisplayHelpers edge cases
        assertEquals("Unknown", DisplayHelpers.getColorDisplayName(null), "Null color should return Unknown");
        assertTrue(DisplayHelpers.getFactionDisplayName(999).contains("Unknown"), "Invalid faction should return Unknown");
        assertTrue(DisplayHelpers.getArchetypeDisplayName(999).contains("Unknown"), "Invalid archetype should return Unknown");
        
        // Test large values
        assertTrue(InputUtilities.pixelsToFeet(1000000.0) > 0, "Large pixel values should convert");
        assertTrue(DisplayHelpers.formatLargeNumber(999999999L).contains("M"), "Large numbers should format");
        
        // Test negative values
        assertEquals(-1.0, InputUtilities.pixelsToFeet(-7.0), 0.001, "Negative conversion should work");
        assertTrue(InputUtilities.calculateDistance(-10, -10, 10, 10) > 0, "Distance should be positive regardless of coordinates");
        
        // Test boundary values for validation
        assertTrue(InputUtilities.isValidSlotNumber(InputConstants.MIN_SLOT_NUMBER), "Min slot should be valid");
        assertTrue(InputUtilities.isValidSlotNumber(InputConstants.MAX_SLOT_NUMBER), "Max slot should be valid");
        assertFalse(InputUtilities.isValidSlotNumber(InputConstants.MIN_SLOT_NUMBER - 1), "Below min should be invalid");
        assertFalse(InputUtilities.isValidSlotNumber(InputConstants.MAX_SLOT_NUMBER + 1), "Above max should be invalid");
    }
}
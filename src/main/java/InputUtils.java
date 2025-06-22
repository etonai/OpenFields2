/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import javafx.scene.paint.Color;
import java.util.List;

import game.Unit;
import combat.Character;
import data.CharacterData;
import data.WeaponFactory;

/**
 * Utility class providing common input processing functions for the OpenFields2 game.
 * 
 * This class contains static utility methods for:
 * - Coordinate conversion and distance calculations
 * - Data validation and collision detection
 * - Character data conversion and mapping
 * - Display name and color mappings
 * - Input processing constants and configuration
 * 
 * All methods are static and stateless, making them suitable for use across
 * the entire input system and other game components.
 * 
 * @author DevCycle 15h - Phase 5.1: Input Utilities Extraction
 */
public class InputUtils {
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Constants and Configuration
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /** Conversion factor: 7 pixels = 1 foot */
    public static final double PIXELS_PER_FOOT = 7.0;
    
    /** Character diameter in pixels for collision detection */
    public static final double CHARACTER_DIAMETER_PIXELS = 21.0;
    
    /** Minimum unit spacing in pixels (4 feet) */
    public static final double MINIMUM_UNIT_SPACING_PIXELS = 28.0;
    
    /** Base movement speed in pixels per second (6 feet/second = 42 pixels/second) */
    public static final double BASE_MOVEMENT_SPEED_PIXELS_PER_SECOND = 42.0;
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Coordinate Conversion and Distance Utilities
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Convert feet to pixels using the game's conversion factor.
     * 
     * @param feet Distance in feet
     * @return Distance in pixels
     */
    public static double feetToPixels(double feet) {
        return feet * PIXELS_PER_FOOT;
    }
    
    /**
     * Convert pixels to feet using the game's conversion factor.
     * 
     * @param pixels Distance in pixels
     * @return Distance in feet
     */
    public static double pixelsToFeet(double pixels) {
        return pixels / PIXELS_PER_FOOT;
    }
    
    /**
     * Calculate distance between two points in pixels.
     * 
     * @param x1 First point X coordinate
     * @param y1 First point Y coordinate
     * @param x2 Second point X coordinate
     * @param y2 Second point Y coordinate
     * @return Distance in pixels
     */
    public static double calculateDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.hypot(dx, dy);
    }
    
    /**
     * Calculate distance between two units in pixels.
     * 
     * @param unit1 First unit
     * @param unit2 Second unit
     * @return Distance in pixels
     */
    public static double calculateUnitDistance(Unit unit1, Unit unit2) {
        return calculateDistance(unit1.x, unit1.y, unit2.x, unit2.y);
    }
    
    /**
     * Calculate distance between two units in feet.
     * 
     * @param unit1 First unit
     * @param unit2 Second unit
     * @return Distance in feet
     */
    public static double calculateUnitDistanceFeet(Unit unit1, Unit unit2) {
        return pixelsToFeet(calculateUnitDistance(unit1, unit2));
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Collision Detection and Validation
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Check if two units are colliding based on minimum spacing requirements.
     * 
     * @param unit1 First unit
     * @param unit2 Second unit
     * @return true if units are too close together
     */
    public static boolean areUnitsColliding(Unit unit1, Unit unit2) {
        double distance = calculateUnitDistance(unit1, unit2);
        return distance < MINIMUM_UNIT_SPACING_PIXELS;
    }
    
    /**
     * Check if a position would cause collision with existing units.
     * 
     * @param x X coordinate to check
     * @param y Y coordinate to check
     * @param units List of existing units
     * @return true if position would cause collision
     */
    public static boolean wouldCauseCollision(double x, double y, List<Unit> units) {
        for (Unit existingUnit : units) {
            double distance = calculateDistance(x, y, existingUnit.x, existingUnit.y);
            if (distance < MINIMUM_UNIT_SPACING_PIXELS) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Find safe position near target coordinates to avoid collisions.
     * 
     * @param targetX Desired X coordinate
     * @param targetY Desired Y coordinate
     * @param units List of existing units
     * @param maxAttempts Maximum number of placement attempts
     * @return SafePosition object with final coordinates and success status
     */
    public static SafePosition findSafePosition(double targetX, double targetY, List<Unit> units, int maxAttempts) {
        double x = targetX;
        double y = targetY;
        
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            if (!wouldCauseCollision(x, y, units)) {
                return new SafePosition(x, y, true);
            }
            
            // Offset by minimum spacing in X direction
            x += MINIMUM_UNIT_SPACING_PIXELS;
        }
        
        // Return last attempted position even if not safe
        return new SafePosition(x, y, false);
    }
    
    /**
     * Check if a character is available for deployment (not already deployed or incapacitated).
     * 
     * @param character Character to check
     * @param units List of deployed units
     * @return true if character can be deployed
     */
    public static boolean isCharacterAvailable(Character character, List<Unit> units) {
        if (character.isIncapacitated()) {
            return false;
        }
        
        // Check if character is already deployed
        for (Unit unit : units) {
            if (unit.character == character) {
                return false;
            }
        }
        
        return true;
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Data Conversion Utilities
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Convert CharacterData to runtime Character object.
     * Note: This is a simplified version that handles basic character creation.
     * Full conversion logic may need to be customized based on CharacterData structure.
     * 
     * @param data CharacterData from persistence
     * @return Runtime Character object
     */
    public static Character convertFromCharacterData(CharacterData data) {
        // Create character using data constructor
        Character character = new Character(
            data.nickname,
            data.dexterity,
            data.health,
            data.coolness,
            data.strength,
            data.reflexes,
            data.handedness
        );
        
        // Set basic properties
        character.id = data.id;
        character.setFaction(data.faction);
        
        // Set weapons using WeaponFactory if weapon ID is available
        if (data.weaponId != null && !data.weaponId.isEmpty()) {
            character.weapon = WeaponFactory.createWeapon(data.weaponId);
            if (character.weapon != null) {
                character.currentWeaponState = character.weapon.getInitialState();
            }
        }
        
        // Note: Skills, movement types, and other properties may need custom handling
        // based on the specific CharacterData structure used in your application
        
        return character;
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Display Name and Color Mappings
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Get display name for faction number.
     * 
     * @param factionNumber Faction number (1-4)
     * @return Faction display name
     */
    public static String getFactionName(int factionNumber) {
        switch (factionNumber) {
            case 1: return "NONE";
            case 2: return "Union";
            case 3: return "Confederacy";
            case 4: return "Southern Unionists";
            default: return "Unknown";
        }
    }
    
    /**
     * Get display name for faction ID.
     * 
     * @param faction Faction ID (0-3)
     * @return Faction display name
     */
    public static String getFactionDisplayName(int faction) {
        switch (faction) {
            case 0: return "NONE";
            case 1: return "Union";
            case 2: return "Confederacy";
            case 3: return "Southern Unionists";
            default: return "Unknown";
        }
    }
    
    /**
     * Get color for character archetype.
     * 
     * @param archetype Archetype name
     * @return Color for the archetype
     */
    public static Color getArchetypeColor(String archetype) {
        switch (archetype.toLowerCase()) {
            case "confederate_soldier":
                return Color.DARKGRAY; // Confederate dark gray
            case "union_soldier":
                return Color.BLUE; // Union blue
            default:
                return Color.CYAN; // Default color for other archetypes
        }
    }
    
    /**
     * Get theme display name for theme ID.
     * 
     * @param themeId Theme identifier
     * @return Human-readable theme name
     */
    public static String getThemeDisplayName(String themeId) {
        switch (themeId) {
            case "test_theme":
                return "Test Theme";
            case "civil_war":
                return "American Civil War";
            case "western":
                return "Wild West";
            case "modern":
                return "Modern Combat";
            case "sci_fi":
                return "Science Fiction";
            default:
                return "Unknown Theme";
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Debug and Logging Utilities
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Print debug message if debug mode is enabled.
     * 
     * @param message Debug message to print
     */
    public static void debugPrint(String message) {
        if (GameRenderer.isDebugMode()) {
            System.out.println("DEBUG: " + message);
        }
    }
    
    /**
     * Format coordinates for display.
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @return Formatted coordinate string
     */
    public static String formatCoordinates(double x, double y) {
        return String.format("(%.1f, %.1f)", x, y);
    }
    
    /**
     * Format coordinates with feet conversion for display.
     * 
     * @param x X coordinate in pixels
     * @param y Y coordinate in pixels
     * @return Formatted coordinate string with feet conversion
     */
    public static String formatCoordinatesWithFeet(double x, double y) {
        double xFeet = pixelsToFeet(x);
        double yFeet = pixelsToFeet(y);
        return String.format("(%.1f, %.1f) pixels / (%.1f, %.1f) feet", x, y, xFeet, yFeet);
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Data Transfer Objects
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Data transfer object for safe position results.
     */
    public static class SafePosition {
        public final double x;
        public final double y;
        public final boolean isValid;
        
        public SafePosition(double x, double y, boolean isValid) {
            this.x = x;
            this.y = y;
            this.isValid = isValid;
        }
        
        @Override
        public String toString() {
            return String.format("SafePosition(%.1f, %.1f, valid: %s)", x, y, isValid);
        }
    }
}
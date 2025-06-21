/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

/**
 * Static utility methods for input processing and coordinate conversion.
 * 
 * This class provides pure functions with no side effects that can be safely
 * extracted from InputManager to improve code organization and testability.
 * All methods are stateless and have no dependencies on game state.
 * 
 * UTILITY CATEGORIES:
 * - Coordinate conversion between screen and world space
 * - Mathematical calculations for distance and positioning
 * - Input validation helpers
 * - Common formatting operations
 * 
 * DESIGN PRINCIPLES:
 * - Pure functions only (no side effects)
 * - No dependencies on game state or external systems
 * - Easily unit testable
 * - Thread-safe (stateless)
 * - Performance optimized for frequent calls
 */
public final class InputUtilities {
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    /** Conversion factor from pixels to feet (7 pixels = 1 foot) */
    public static final double PIXELS_PER_FOOT = 7.0;
    
    /** Conversion factor from feet to pixels (1 foot = 7 pixels) */
    public static final double FEET_PER_PIXEL = 1.0 / PIXELS_PER_FOOT;
    
    /** Maximum valid slot number for save/load operations */
    public static final int MAX_SLOT_NUMBER = 9;
    
    /** Minimum valid slot number for save/load operations */
    public static final int MIN_SLOT_NUMBER = 1;
    
    /** Maximum valid quantity for batch operations */
    public static final int MAX_BATCH_QUANTITY = 20;
    
    /** Minimum valid quantity for batch operations */
    public static final int MIN_BATCH_QUANTITY = 1;
    
    /** Maximum valid spacing in feet for character placement */
    public static final double MAX_SPACING_FEET = 9.0;
    
    /** Minimum valid spacing in feet for character placement */
    public static final double MIN_SPACING_FEET = 1.0;
    
    // Private constructor to prevent instantiation
    private InputUtilities() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // COORDINATE CONVERSION UTILITIES
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Convert pixel coordinates to feet using the game's conversion factor.
     * 
     * @param pixels Distance in pixels
     * @return Distance in feet
     */
    public static double pixelsToFeet(double pixels) {
        return pixels * FEET_PER_PIXEL;
    }
    
    /**
     * Convert feet to pixel coordinates using the game's conversion factor.
     * 
     * @param feet Distance in feet
     * @return Distance in pixels
     */
    public static double feetToPixels(double feet) {
        return feet * PIXELS_PER_FOOT;
    }
    
    /**
     * Calculate the distance between two points in pixels.
     * 
     * @param x1 X coordinate of first point
     * @param y1 Y coordinate of first point
     * @param x2 X coordinate of second point
     * @param y2 Y coordinate of second point
     * @return Distance between points in pixels
     */
    public static double calculateDistance(double x1, double y1, double x2, double y2) {
        double deltaX = x2 - x1;
        double deltaY = y2 - y1;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
    
    /**
     * Calculate the distance between two points in feet.
     * 
     * @param x1 X coordinate of first point in pixels
     * @param y1 Y coordinate of first point in pixels
     * @param x2 X coordinate of second point in pixels
     * @param y2 Y coordinate of second point in pixels
     * @return Distance between points in feet
     */
    public static double calculateDistanceInFeet(double x1, double y1, double x2, double y2) {
        return pixelsToFeet(calculateDistance(x1, y1, x2, y2));
    }
    
    /**
     * Calculate the angle between two points in radians.
     * 
     * @param fromX X coordinate of origin point
     * @param fromY Y coordinate of origin point
     * @param toX X coordinate of target point
     * @param toY Y coordinate of target point
     * @return Angle in radians from origin to target
     */
    public static double calculateAngle(double fromX, double fromY, double toX, double toY) {
        return Math.atan2(toY - fromY, toX - fromX);
    }
    
    /**
     * Calculate a point at a specific distance and angle from an origin point.
     * 
     * @param originX X coordinate of origin point
     * @param originY Y coordinate of origin point
     * @param distance Distance from origin in pixels
     * @param angle Angle in radians
     * @return Array containing [x, y] coordinates of calculated point
     */
    public static double[] calculatePointAtDistanceAndAngle(double originX, double originY, 
                                                          double distance, double angle) {
        double x = originX + distance * Math.cos(angle);
        double y = originY + distance * Math.sin(angle);
        return new double[]{x, y};
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // MATHEMATICAL UTILITIES
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Clamp a value between minimum and maximum bounds.
     * 
     * @param value Value to clamp
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @return Clamped value
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Clamp an integer value between minimum and maximum bounds.
     * 
     * @param value Value to clamp
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @return Clamped value
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Check if a value is within a specified range (inclusive).
     * 
     * @param value Value to check
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @return true if value is within range
     */
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }
    
    /**
     * Check if an integer value is within a specified range (inclusive).
     * 
     * @param value Value to check
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @return true if value is within range
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }
    
    /**
     * Round a double value to a specified number of decimal places.
     * 
     * @param value Value to round
     * @param decimalPlaces Number of decimal places
     * @return Rounded value
     */
    public static double roundToDecimalPlaces(double value, int decimalPlaces) {
        double multiplier = Math.pow(10, decimalPlaces);
        return Math.round(value * multiplier) / multiplier;
    }
    
    /**
     * Calculate the percentage of one value relative to another.
     * 
     * @param part Partial value
     * @param total Total value
     * @return Percentage (0-100)
     */
    public static double calculatePercentage(double part, double total) {
        if (total == 0) return 0;
        return (part / total) * 100.0;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // INPUT VALIDATION UTILITIES
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Validate that a slot number is within valid range.
     * 
     * @param slot Slot number to validate
     * @return true if slot is valid (1-9)
     */
    public static boolean isValidSlotNumber(int slot) {
        return isInRange(slot, MIN_SLOT_NUMBER, MAX_SLOT_NUMBER);
    }
    
    /**
     * Validate that a batch quantity is within valid range.
     * 
     * @param quantity Quantity to validate
     * @return true if quantity is valid (1-20)
     */
    public static boolean isValidBatchQuantity(int quantity) {
        return isInRange(quantity, MIN_BATCH_QUANTITY, MAX_BATCH_QUANTITY);
    }
    
    /**
     * Validate that spacing is within valid range.
     * 
     * @param spacing Spacing in feet to validate
     * @return true if spacing is valid (1-9 feet)
     */
    public static boolean isValidSpacing(double spacing) {
        return isInRange(spacing, MIN_SPACING_FEET, MAX_SPACING_FEET);
    }
    
    /**
     * Parse a string to an integer with validation.
     * 
     * @param str String to parse
     * @param defaultValue Default value if parsing fails
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @return Parsed and validated integer
     */
    public static int parseAndValidateInt(String str, int defaultValue, int min, int max) {
        try {
            int value = Integer.parseInt(str.trim());
            return clamp(value, min, max);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Parse a string to a double with validation.
     * 
     * @param str String to parse
     * @param defaultValue Default value if parsing fails
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @return Parsed and validated double
     */
    public static double parseAndValidateDouble(String str, double defaultValue, double min, double max) {
        try {
            double value = Double.parseDouble(str.trim());
            return clamp(value, min, max);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Check if a string represents a valid numeric input.
     * 
     * @param str String to check
     * @return true if string can be parsed as a number
     */
    public static boolean isValidNumericInput(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // COORDINATE POSITIONING UTILITIES
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Calculate positions for placing multiple units in a line formation.
     * 
     * @param startX Starting X coordinate
     * @param startY Starting Y coordinate
     * @param count Number of units to place
     * @param spacingPixels Spacing between units in pixels
     * @param horizontal true for horizontal line, false for vertical line
     * @return Array of coordinate pairs [x, y] for each unit
     */
    public static double[][] calculateLineFormation(double startX, double startY, int count, 
                                                   double spacingPixels, boolean horizontal) {
        double[][] positions = new double[count][2];
        
        for (int i = 0; i < count; i++) {
            if (horizontal) {
                positions[i][0] = startX + (i * spacingPixels);
                positions[i][1] = startY;
            } else {
                positions[i][0] = startX;
                positions[i][1] = startY + (i * spacingPixels);
            }
        }
        
        return positions;
    }
    
    /**
     * Calculate positions for placing multiple units in a rectangular grid formation.
     * 
     * @param startX Starting X coordinate
     * @param startY Starting Y coordinate
     * @param count Number of units to place
     * @param spacingPixels Spacing between units in pixels
     * @param maxColumns Maximum number of columns before wrapping to next row
     * @return Array of coordinate pairs [x, y] for each unit
     */
    public static double[][] calculateGridFormation(double startX, double startY, int count, 
                                                   double spacingPixels, int maxColumns) {
        double[][] positions = new double[count][2];
        
        for (int i = 0; i < count; i++) {
            int row = i / maxColumns;
            int col = i % maxColumns;
            
            positions[i][0] = startX + (col * spacingPixels);
            positions[i][1] = startY + (row * spacingPixels);
        }
        
        return positions;
    }
    
    /**
     * Check if a point is within a rectangular area.
     * 
     * @param pointX X coordinate of point to check
     * @param pointY Y coordinate of point to check
     * @param rectX X coordinate of rectangle top-left corner
     * @param rectY Y coordinate of rectangle top-left corner
     * @param rectWidth Width of rectangle
     * @param rectHeight Height of rectangle
     * @return true if point is within rectangle
     */
    public static boolean isPointInRectangle(double pointX, double pointY, double rectX, double rectY, 
                                           double rectWidth, double rectHeight) {
        return pointX >= rectX && pointX <= rectX + rectWidth && 
               pointY >= rectY && pointY <= rectY + rectHeight;
    }
    
    /**
     * Check if a point is within a circular area.
     * 
     * @param pointX X coordinate of point to check
     * @param pointY Y coordinate of point to check
     * @param centerX X coordinate of circle center
     * @param centerY Y coordinate of circle center
     * @param radius Radius of circle
     * @return true if point is within circle
     */
    public static boolean isPointInCircle(double pointX, double pointY, double centerX, double centerY, 
                                        double radius) {
        double distance = calculateDistance(pointX, pointY, centerX, centerY);
        return distance <= radius;
    }
}
/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import javafx.scene.paint.Color;
import combat.*;
import game.Unit;

/**
 * Static utility methods for display formatting and string generation.
 * 
 * This class provides pure functions for formatting display text, generating
 * status messages, and creating user-friendly representations of game data.
 * All methods are stateless and have no dependencies on game state.
 * 
 * UTILITY CATEGORIES:
 * - Character information formatting
 * - Weapon display utilities
 * - Faction name and color mapping
 * - Status message formatting
 * - Debug output formatting
 * - Coordinate and numeric formatting
 * 
 * DESIGN PRINCIPLES:
 * - Pure functions only (no side effects)
 * - No dependencies on game state or external systems
 * - Consistent formatting across the application
 * - Easily unit testable
 * - Performance optimized for frequent calls
 */
public final class DisplayHelpers {
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    /** Default precision for coordinate display */
    private static final int COORDINATE_PRECISION = 1;
    
    /** Default precision for percentage display */
    private static final int PERCENTAGE_PRECISION = 1;
    
    /** Default precision for distance display */
    private static final int DISTANCE_PRECISION = 1;
    
    /** Maximum length for truncated display names */
    private static final int MAX_DISPLAY_NAME_LENGTH = 25;
    
    // Private constructor to prevent instantiation
    private DisplayHelpers() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // CHARACTER INFORMATION FORMATTING
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Format a character's display name with optional truncation.
     * 
     * @param character Character to format name for
     * @param maxLength Maximum length before truncation (0 for no limit)
     * @return Formatted display name
     */
    public static String formatCharacterDisplayName(combat.Character character, int maxLength) {
        if (character == null) return "Unknown Character";
        
        String displayName = character.getDisplayName();
        if (maxLength > 0 && displayName.length() > maxLength) {
            return displayName.substring(0, maxLength - 3) + "...";
        }
        return displayName;
    }
    
    /**
     * Format a character's health status with percentage and bar.
     * 
     * @param currentHealth Current health value
     * @param maxHealth Maximum health value
     * @return Formatted health status string
     */
    public static String formatHealthStatus(int currentHealth, int maxHealth) {
        if (maxHealth <= 0) return "Health: Unknown";
        
        double percentage = (double) currentHealth / maxHealth * 100.0;
        String healthBar = generateHealthBar(percentage);
        
        return String.format("Health: %d/%d (%.1f%%) %s", 
                           currentHealth, maxHealth, percentage, healthBar);
    }
    
    /**
     * Generate a visual health bar using ASCII characters.
     * 
     * @param percentage Health percentage (0-100)
     * @return ASCII health bar string
     */
    public static String generateHealthBar(double percentage) {
        int barLength = 10;
        int filledBars = (int) Math.round(percentage / 10.0);
        
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            if (i < filledBars) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        bar.append("]");
        
        return bar.toString();
    }
    
    /**
     * Format character statistics for display.
     * 
     * @param character Character to format stats for
     * @return Formatted statistics string
     */
    public static String formatCharacterStats(combat.Character character) {
        if (character == null) return "No character data";
        
        StringBuilder stats = new StringBuilder();
        stats.append(String.format("Stats: DEX=%d STR=%d REF=%d COOL=%d", 
                    character.dexterity, character.strength, 
                    character.reflexes, character.coolness));
        
        return stats.toString();
    }
    
    /**
     * Format character combat statistics for display.
     * 
     * @param character Character to format combat stats for
     * @return Formatted combat statistics string
     */
    public static String formatCombatStats(combat.Character character) {
        if (character == null) return "No combat data";
        
        // Calculate accuracy percentages
        double rangedAccuracy = character.rangedAttacksAttempted > 0 ? 
            (double) character.rangedAttacksSuccessful / character.rangedAttacksAttempted * 100.0 : 0.0;
        double meleeAccuracy = character.meleeAttacksAttempted > 0 ? 
            (double) character.meleeAttacksSuccessful / character.meleeAttacksAttempted * 100.0 : 0.0;
        
        return String.format("Combat: Ranged %d/%d (%.1f%%) Melee %d/%d (%.1f%%) Wounds: %d",
                           character.rangedAttacksSuccessful, character.rangedAttacksAttempted, rangedAccuracy,
                           character.meleeAttacksSuccessful, character.meleeAttacksAttempted, meleeAccuracy,
                           character.woundsReceived);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // WEAPON DISPLAY UTILITIES
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Format weapon information for display.
     * 
     * @param weapon Weapon to format
     * @return Formatted weapon information string
     */
    public static String formatWeaponInfo(Weapon weapon) {
        if (weapon == null) return "No weapon";
        
        StringBuilder info = new StringBuilder();
        info.append(weapon.name);
        
        if (weapon instanceof RangedWeapon) {
            RangedWeapon ranged = (RangedWeapon) weapon;
            info.append(String.format(" (Damage: %d, Accuracy: %d, Range: %.0f)", 
                       ranged.damage, ranged.getWeaponAccuracy(), ranged.getMaximumRange()));
        } else if (weapon instanceof MeleeWeapon) {
            MeleeWeapon melee = (MeleeWeapon) weapon;
            info.append(String.format(" (Damage: %d, Accuracy: %d, Range: %.1f)", 
                       melee.damage, melee.getWeaponAccuracy(), melee.getWeaponRange()));
        } else {
            info.append(String.format(" (Damage: %d)", weapon.damage));
        }
        
        return info.toString();
    }
    
    /**
     * Get weapon type display name.
     * 
     * @param weapon Weapon to get type for
     * @return Human-readable weapon type
     */
    public static String getWeaponTypeDisplayName(Weapon weapon) {
        if (weapon == null) return "None";
        
        if (weapon instanceof RangedWeapon) {
            return "Ranged";
        } else if (weapon instanceof MeleeWeapon) {
            return "Melee";
        } else {
            return "Other";
        }
    }
    
    /**
     * Format weapon state information for display.
     * 
     * @param weaponState Current weapon state
     * @return Formatted weapon state string
     */
    public static String formatWeaponState(WeaponState weaponState) {
        if (weaponState == null) return "No state";
        
        return String.format("State: %s", weaponState.getState());
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // FACTION AND COLOR UTILITIES
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Get display name for faction number.
     * 
     * @param factionNumber Faction number (1-based)
     * @return Human-readable faction name
     */
    public static String getFactionDisplayName(int factionNumber) {
        switch (factionNumber) {
            case 1: return "Cowboys";
            case 2: return "Outlaws";
            case 3: return "Lawmen";
            case 4: return "Natives";
            case 5: return "Soldiers";
            case 6: return "Civilians";
            case 7: return "Bandits";
            case 8: return "Rangers";
            case 9: return "Mercenaries";
            default: return "Unknown Faction (" + factionNumber + ")";
        }
    }
    
    /**
     * Get color associated with faction number.
     * 
     * @param factionNumber Faction number (1-based)
     * @return JavaFX Color for faction
     */
    public static Color getFactionColor(int factionNumber) {
        switch (factionNumber) {
            case 1: return Color.BLUE;      // Cowboys
            case 2: return Color.RED;       // Outlaws
            case 3: return Color.GREEN;     // Lawmen
            case 4: return Color.ORANGE;    // Natives
            case 5: return Color.PURPLE;    // Soldiers
            case 6: return Color.YELLOW;    // Civilians
            case 7: return Color.DARKGRAY;  // Bandits
            case 8: return Color.CYAN;      // Rangers
            case 9: return Color.GRAY;      // Mercenaries
            default: return Color.BLACK;
        }
    }
    
    /**
     * Convert JavaFX Color to display name.
     * 
     * @param color JavaFX Color object
     * @return Human-readable color name
     */
    public static String getColorDisplayName(Color color) {
        if (color == null) return "Unknown";
        
        if (color.equals(Color.RED)) return "Red";
        if (color.equals(Color.BLUE)) return "Blue";
        if (color.equals(Color.GREEN)) return "Green";
        if (color.equals(Color.PURPLE)) return "Purple";
        if (color.equals(Color.ORANGE)) return "Orange";
        if (color.equals(Color.YELLOW)) return "Yellow";
        if (color.equals(Color.DARKGRAY)) return "Dark Gray";
        if (color.equals(Color.GRAY)) return "Gray";
        if (color.equals(Color.CYAN)) return "Cyan";
        if (color.equals(Color.BLACK)) return "Black";
        
        return "Custom Color";
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // COORDINATE AND NUMERIC FORMATTING
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Format coordinates for display with consistent precision.
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @return Formatted coordinate string (x, y)
     */
    public static String formatCoordinates(double x, double y) {
        return String.format("(%.1f, %.1f)", x, y);
    }
    
    /**
     * Format coordinates with custom precision.
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @param precision Number of decimal places
     * @return Formatted coordinate string
     */
    public static String formatCoordinates(double x, double y, int precision) {
        String format = "(%." + precision + "f, %." + precision + "f)";
        return String.format(format, x, y);
    }
    
    /**
     * Format distance with appropriate units (pixels or feet).
     * 
     * @param distance Distance value
     * @param inFeet true to display in feet, false for pixels
     * @return Formatted distance string with units
     */
    public static String formatDistance(double distance, boolean inFeet) {
        if (inFeet) {
            double distanceInFeet = InputUtilities.pixelsToFeet(distance);
            return String.format("%.1f ft", distanceInFeet);
        } else {
            return String.format("%.1f px", distance);
        }
    }
    
    /**
     * Format percentage with consistent precision.
     * 
     * @param percentage Percentage value (0-100)
     * @return Formatted percentage string with % symbol
     */
    public static String formatPercentage(double percentage) {
        return String.format("%.1f%%", percentage);
    }
    
    /**
     * Format large numbers with appropriate scaling (K, M, etc.).
     * 
     * @param number Number to format
     * @return Formatted number string with scale suffix
     */
    public static String formatLargeNumber(long number) {
        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        } else {
            return String.valueOf(number);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // STATUS MESSAGE FORMATTING
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Format a movement command message.
     * 
     * @param unitCount Number of units moving
     * @param targetX Target X coordinate
     * @param targetY Target Y coordinate
     * @return Formatted movement message
     */
    public static String formatMovementMessage(int unitCount, double targetX, double targetY) {
        String unitText = unitCount == 1 ? "unit" : "units";
        return String.format("MOVE %d %s to %s", unitCount, unitText, formatCoordinates(targetX, targetY));
    }
    
    /**
     * Format an attack command message.
     * 
     * @param attackerName Name of attacking unit
     * @param targetName Name of target unit
     * @param weaponName Name of weapon used
     * @return Formatted attack message
     */
    public static String formatAttackMessage(String attackerName, String targetName, String weaponName) {
        return String.format("%s attacks %s with %s", attackerName, targetName, weaponName);
    }
    
    /**
     * Format a selection message.
     * 
     * @param selectedCount Number of units selected
     * @return Formatted selection message
     */
    public static String formatSelectionMessage(int selectedCount) {
        if (selectedCount == 0) {
            return "No units selected";
        } else if (selectedCount == 1) {
            return "1 unit selected";
        } else {
            return String.format("%d units selected", selectedCount);
        }
    }
    
    /**
     * Format a time duration for display.
     * 
     * @param milliseconds Duration in milliseconds
     * @return Formatted duration string
     */
    public static String formatDuration(long milliseconds) {
        if (milliseconds < 1000) {
            return String.format("%d ms", milliseconds);
        } else if (milliseconds < 60000) {
            return String.format("%.1f s", milliseconds / 1000.0);
        } else {
            long seconds = milliseconds / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format("%d:%02d", minutes, seconds);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // DEBUG OUTPUT FORMATTING
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Format debug information for input events.
     * 
     * @param eventType Type of input event
     * @param coordinates Event coordinates
     * @param modifiers Keyboard modifiers active
     * @return Formatted debug string
     */
    public static String formatDebugInputEvent(String eventType, String coordinates, String modifiers) {
        if (modifiers == null || modifiers.isEmpty()) {
            return String.format("%s at %s", eventType, coordinates);
        } else {
            return String.format("%s at %s (modifiers: %s)", eventType, coordinates, modifiers);
        }
    }
    
    /**
     * Format debug information for state transitions.
     * 
     * @param stateType Type of state being changed
     * @param fromState Previous state
     * @param toState New state
     * @return Formatted debug string
     */
    public static String formatDebugStateTransition(String stateType, String fromState, String toState) {
        return String.format("%s: %s → %s", stateType, fromState, toState);
    }
    
    /**
     * Format debug information for performance timing.
     * 
     * @param operationName Name of the operation
     * @param durationMs Duration in milliseconds
     * @return Formatted debug string
     */
    public static String formatDebugPerformance(String operationName, double durationMs) {
        return String.format("%s completed in %.3f ms", operationName, durationMs);
    }
    
    /**
     * Format debug information for memory usage.
     * 
     * @param context Context where memory was measured
     * @param usedMB Used memory in megabytes
     * @param totalMB Total memory in megabytes
     * @return Formatted debug string
     */
    public static String formatDebugMemory(String context, long usedMB, long totalMB) {
        double percentage = (double) usedMB / totalMB * 100.0;
        return String.format("%s - Memory: %d/%d MB (%.1f%%)", context, usedMB, totalMB, percentage);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════
    // ARCHETYPE AND GAME ELEMENT FORMATTING
    // ═══════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Get display name for character archetype number.
     * 
     * @param archetypeNumber Archetype number (1-based)
     * @return Human-readable archetype name
     */
    public static String getArchetypeDisplayName(int archetypeNumber) {
        switch (archetypeNumber) {
            case 1: return "Gunslinger";
            case 2: return "Soldier";
            case 3: return "Medic";
            case 4: return "Scout";
            case 5: return "Marksman";
            case 6: return "Brawler";
            default: return "Unknown Archetype (" + archetypeNumber + ")";
        }
    }
    
    /**
     * Get color associated with character archetype.
     * 
     * @param archetypeName Name of the archetype
     * @return JavaFX Color for archetype
     */
    public static Color getArchetypeColor(String archetypeName) {
        if (archetypeName == null) return Color.GRAY;
        
        switch (archetypeName.toLowerCase()) {
            case "gunslinger": return Color.BLUE;
            case "soldier": return Color.GREEN;
            case "medic": return Color.RED;
            case "scout": return Color.ORANGE;
            case "marksman": return Color.PURPLE;
            case "brawler": return Color.DARKGRAY;
            default: return Color.GRAY;
        }
    }
    
    /**
     * Format unit information for display.
     * 
     * @param unit Unit to format information for
     * @return Formatted unit information string
     */
    public static String formatUnitInfo(Unit unit) {
        if (unit == null || unit.character == null) {
            return "Invalid unit";
        }
        
        combat.Character character = unit.character;
        String name = formatCharacterDisplayName(character, MAX_DISPLAY_NAME_LENGTH);
        String health = formatHealthStatus(character.currentHealth, character.health);
        String weapon = formatWeaponInfo(character.weapon);
        
        return String.format("%s - %s - %s", name, health, weapon);
    }
    
    /**
     * Create a formatted header for display sections.
     * 
     * @param title Header title
     * @param width Total width of header
     * @return Formatted header string with decorative borders
     */
    public static String createDisplayHeader(String title, int width) {
        if (title == null) title = "";
        
        int titleLength = title.length();
        int paddingTotal = width - titleLength - 2; // -2 for spaces around title
        int paddingLeft = paddingTotal / 2;
        int paddingRight = paddingTotal - paddingLeft;
        
        StringBuilder header = new StringBuilder();
        
        // Top border
        for (int i = 0; i < width; i++) {
            header.append("═");
        }
        header.append("\n");
        
        // Title line
        for (int i = 0; i < paddingLeft; i++) {
            header.append(" ");
        }
        header.append(" ").append(title).append(" ");
        for (int i = 0; i < paddingRight; i++) {
            header.append(" ");
        }
        header.append("\n");
        
        // Bottom border
        for (int i = 0; i < width; i++) {
            header.append("═");
        }
        
        return header.toString();
    }
}
package combat;

/**
 * Utility class for Character debug functionality.
 * Extracted from Character class as part of DevCycle 24 refactoring.
 * 
 * Handles all debug output, auto-targeting debug controls, and debug message formatting.
 */
public class CharacterDebugUtils {
    
    // ========================================
    // DEBUG CONFIGURATION
    // ========================================
    
    /** Master flag to show/hide ALL auto-target debug messages */
    private static boolean autoTargetDebugVisible = false;
    
    /** Set to true to enable verbose auto-target debugging */
    private static boolean autoTargetDebugEnabled = false;
    
    // ========================================
    // DEBUG OUTPUT METHODS
    // ========================================
    
    /**
     * Helper method to print debug messages only when debug mode is enabled.
     * Uses reflection to check for debug mode in OpenFields2 class.
     */
    public static void debugPrint(String message) {
        try {
            // Use reflection to access OpenFields2.debugMode
            Class<?> openFields2Class = Class.forName("OpenFields2");
            java.lang.reflect.Field debugModeField = openFields2Class.getDeclaredField("debugMode");
            debugModeField.setAccessible(true);
            boolean debugMode = debugModeField.getBoolean(null);
            
            if (debugMode) {
                System.out.println(message);
            }
        } catch (Exception e) {
            // If reflection fails, silently skip debug output
        }
    }
    
    /**
     * Print debug message with character context
     */
    public static void debugPrint(Character character, String message) {
        if (character != null) {
            debugPrint(message);
        }
    }
    
    /**
     * Print auto-targeting debug message with character context and throttling
     */
    public static void autoTargetDebugPrint(Character character, String message, long currentTick) {
        if (!autoTargetDebugVisible || !autoTargetDebugEnabled || character == null) return;
        
        // Throttle repetitive messages - only print every 60 ticks (1 second) for same character
        if (currentTick - character.lastAutoTargetDebugTick >= 60) {
            debugPrint(message);
            character.lastAutoTargetDebugTick = currentTick;
        }
    }
    
    /**
     * Print auto-targeting debug message that always prints (for important events)
     */
    public static void autoTargetDebugPrintAlways(Character character, String message) {
        if (autoTargetDebugVisible && autoTargetDebugEnabled && character != null) {
            debugPrint(message);
        }
    }
    
    /**
     * Print auto-targeting info messages (important events like target acquisition)
     * These use System.out.println and respect the visibility flag
     */
    public static void autoTargetInfoPrint(Character character, String message) {
        if (autoTargetDebugVisible && character != null) {
            System.out.println(message);
        }
    }
    
    // ========================================
    // AUTO-TARGETING DEBUG CONTROLS
    // ========================================
    
    /**
     * Master control to show/hide ALL auto-targeting debug messages
     */
    public static void setAutoTargetDebugVisible(boolean visible) {
        autoTargetDebugVisible = visible;
        if (visible) {
            System.out.println("Auto-targeting debug messages are now VISIBLE");
        } else {
            System.out.println("Auto-targeting debug messages are now HIDDEN (completely suppressed)");
        }
    }
    
    /**
     * Enable/disable verbose auto-targeting debug messages (when visible)
     */
    public static void setAutoTargetDebugEnabled(boolean enabled) {
        autoTargetDebugEnabled = enabled;
        if (autoTargetDebugVisible) {
            if (enabled) {
                System.out.println("Auto-targeting debug messages set to VERBOSE (may be very chatty)");
            } else {
                System.out.println("Auto-targeting debug messages set to THROTTLED (reduced spam)");
            }
        } else {
            System.out.println("Auto-targeting debug messages are hidden - enable visibility first with setAutoTargetDebugVisible(true)");
        }
    }
    
    /**
     * Check if auto-target debug is visible
     */
    public static boolean isAutoTargetDebugVisible() {
        return autoTargetDebugVisible;
    }
    
    /**
     * Check if auto-target debug is enabled (verbose mode)
     */
    public static boolean isAutoTargetDebugEnabled() {
        return autoTargetDebugEnabled;
    }
    
    // ========================================
    // SPECIALIZED DEBUG FORMATTERS
    // ========================================
    
    /**
     * Format combat mode debug message
     */
    public static void debugCombatMode(Character character, String action) {
        debugPrint("[COMBAT-MODE] " + character.getDisplayName() + " " + action);
    }
    
    /**
     * Format weapon ready debug message
     */
    public static void debugWeaponReady(Character character, String action) {
        debugPrint("[WEAPON-READY] " + character.getDisplayName() + " " + action);
    }
    
    /**
     * Format melee debug message
     */
    public static void debugMelee(Character character, String action) {
        debugPrint("[MELEE-EVENT] " + character.getDisplayName() + " " + action);
    }
    
    /**
     * Format auto-targeting debug message
     */
    public static void debugAutoTarget(Character character, String action) {
        debugPrint("[AUTO-TARGET-DEBUG] " + character.getDisplayName() + " " + action);
    }
    
    /**
     * Format continuous attack debug message
     */
    public static void debugContinuousAttack(Character character, String action) {
        debugPrint("[CONTINUOUS-ATTACK] " + character.getDisplayName() + " " + action);
    }
}
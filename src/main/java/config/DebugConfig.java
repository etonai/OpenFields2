package config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.io.IOException;

/**
 * Debug configuration manager for OpenFields2
 * Loads debug settings from debug-config.json
 */
public class DebugConfig {
    private static DebugConfig instance;
    private JsonNode config;
    
    // Auto-targeting debug settings
    private boolean autoTargetVisible = false;
    private boolean autoTargetVerbose = false;
    
    // Other debug categories
    private boolean combatDebugEnabled = true;
    private boolean movementDebugEnabled = true;
    private boolean weaponsDebugEnabled = true;
    private boolean eventQueueDebugEnabled = false;
    
    private DebugConfig() {
        loadConfig();
    }
    
    public static DebugConfig getInstance() {
        if (instance == null) {
            instance = new DebugConfig();
        }
        return instance;
    }
    
    private void loadConfig() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("debug-config.json");
            if (inputStream == null) {
                System.err.println("debug-config.json not found, using default debug settings");
                return;
            }
            
            ObjectMapper mapper = new ObjectMapper();
            config = mapper.readTree(inputStream);
            
            JsonNode debugConfig = config.get("debugConfig");
            if (debugConfig != null) {
                // Load auto-targeting settings
                JsonNode autoTargeting = debugConfig.get("autoTargeting");
                if (autoTargeting != null) {
                    autoTargetVisible = autoTargeting.get("visible").asBoolean(false);
                    autoTargetVerbose = autoTargeting.get("verbose").asBoolean(false);
                }
                
                // Load other debug category settings
                JsonNode combat = debugConfig.get("combat");
                if (combat != null) {
                    combatDebugEnabled = combat.get("enabled").asBoolean(true);
                }
                
                JsonNode movement = debugConfig.get("movement");
                if (movement != null) {
                    movementDebugEnabled = movement.get("enabled").asBoolean(true);
                }
                
                JsonNode weapons = debugConfig.get("weapons");
                if (weapons != null) {
                    weaponsDebugEnabled = weapons.get("enabled").asBoolean(true);
                }
                
                JsonNode eventQueue = debugConfig.get("eventQueue");
                if (eventQueue != null) {
                    eventQueueDebugEnabled = eventQueue.get("enabled").asBoolean(false);
                }
            }
            
            System.out.println("Debug configuration loaded from debug-config.json");
            printCurrentSettings();
            
        } catch (IOException e) {
            System.err.println("Failed to load debug-config.json: " + e.getMessage());
            System.err.println("Using default debug settings");
        }
    }
    
    private void printCurrentSettings() {
        System.out.println("Debug Settings:");
        System.out.println("  Auto-targeting: visible=" + autoTargetVisible + ", verbose=" + autoTargetVerbose);
        System.out.println("  Combat debug: " + combatDebugEnabled);
        System.out.println("  Movement debug: " + movementDebugEnabled);
        System.out.println("  Weapons debug: " + weaponsDebugEnabled);
        System.out.println("  Event queue debug: " + eventQueueDebugEnabled);
    }
    
    // Auto-targeting getters
    public boolean isAutoTargetVisible() {
        return autoTargetVisible;
    }
    
    public boolean isAutoTargetVerbose() {
        return autoTargetVerbose;
    }
    
    // Other debug category getters
    public boolean isCombatDebugEnabled() {
        return combatDebugEnabled;
    }
    
    public boolean isMovementDebugEnabled() {
        return movementDebugEnabled;
    }
    
    public boolean isWeaponsDebugEnabled() {
        return weaponsDebugEnabled;
    }
    
    public boolean isEventQueueDebugEnabled() {
        return eventQueueDebugEnabled;
    }
    
    // Setters for runtime changes
    public void setAutoTargetVisible(boolean visible) {
        this.autoTargetVisible = visible;
    }
    
    public void setAutoTargetVerbose(boolean verbose) {
        this.autoTargetVerbose = verbose;
    }
    
    /**
     * Apply debug configuration to the game systems
     */
    public void applyConfiguration() {
        // Apply auto-targeting debug settings
        try {
            Class<?> characterClass = Class.forName("combat.Character");
            java.lang.reflect.Method setVisibleMethod = characterClass.getMethod("setAutoTargetDebugVisible", boolean.class);
            java.lang.reflect.Method setEnabledMethod = characterClass.getMethod("setAutoTargetDebugEnabled", boolean.class);
            
            setVisibleMethod.invoke(null, autoTargetVisible);
            setEnabledMethod.invoke(null, autoTargetVerbose);
            
        } catch (Exception e) {
            System.err.println("Failed to apply auto-targeting debug configuration: " + e.getMessage());
        }
    }
}
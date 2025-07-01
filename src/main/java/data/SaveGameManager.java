package data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SaveGameManager {
    private static SaveGameManager instance;
    private final ObjectMapper objectMapper;
    private final String saveDirectory = "saves/";
    private final String gameVersion = "1.0";
    
    private SaveGameManager() {
        this.objectMapper = new ObjectMapper();
        createSaveDirectory();
    }
    
    public static SaveGameManager getInstance() {
        if (instance == null) {
            instance = new SaveGameManager();
        }
        return instance;
    }
    
    private void createSaveDirectory() {
        File saveDir = new File(saveDirectory);
        if (!saveDir.exists()) {
            boolean created = saveDir.mkdirs();
            if (created) {
                System.out.println("*** Created saves directory: " + saveDirectory);
            }
        }
    }
    
    private boolean isDebugModeActive() {
        try {
            // Use reflection to access GameRenderer's debug mode since it's in default package
            Class<?> gameRendererClass = Class.forName("GameRenderer");
            java.lang.reflect.Method isDebugMode = gameRendererClass.getMethod("isDebugMode");
            return (Boolean) isDebugMode.invoke(null);
        } catch (Exception e) {
            // If we can't access debug mode, default to false
            return false;
        }
    }
    
    public boolean saveToSlot(int slot, SaveData saveData) {
        if (slot < 1 || slot > 9) {
            System.err.println("Error: Save slot must be between 1 and 9");
            return false;
        }
        
        try {
            String filename = saveDirectory + "slot_" + slot + ".json";
            
            // Update metadata with current timestamp and slot
            saveData.metadata.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            saveData.metadata.gameVersion = gameVersion;
            saveData.metadata.saveSlot = slot;
            
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), saveData);
            System.out.println("*** Game saved to slot " + slot + ": " + filename);
            return true;
            
        } catch (IOException e) {
            System.err.println("Error saving game to slot " + slot + ": " + e.getMessage());
            return false;
        }
    }
    
    public SaveData loadFromSlot(int slot) {
        if (slot < 1 || slot > 9) {
            System.err.println("Error: Save slot must be between 1 and 9");
            return null;
        }
        
        try {
            String filename = saveDirectory + "slot_" + slot + ".json";
            File saveFile = new File(filename);
            
            if (!saveFile.exists()) {
                System.err.println("Error: No save file found in slot " + slot);
                return null;
            }
            
            SaveData saveData = objectMapper.readValue(saveFile, SaveData.class);
            
            // Validate save file
            if (saveData.metadata == null || saveData.gameState == null) {
                System.err.println("Error: Corrupted save file in slot " + slot);
                return null;
            }
            
            System.out.println("*** Game loaded from slot " + slot + ": " + filename);
            return saveData;
            
        } catch (IOException e) {
            System.err.println("Error loading game from slot " + slot + ": " + e.getMessage());
            return null;
        }
    }
    
    public List<SaveSlotInfo> listAvailableSlots() {
        List<SaveSlotInfo> slots = new ArrayList<>();
        
        for (int i = 1; i <= 9; i++) {
            String filename = saveDirectory + "slot_" + i + ".json";
            File saveFile = new File(filename);
            
            if (saveFile.exists()) {
                try {
                    JsonNode rootNode = objectMapper.readTree(saveFile);
                    JsonNode metadataNode = rootNode.get("metadata");
                    JsonNode gameStateNode = rootNode.get("gameState");
                    
                    if (metadataNode != null && gameStateNode != null) {
                        String timestamp = metadataNode.get("timestamp").asText();
                        String themeId = metadataNode.get("themeId").asText();
                        long currentTick = gameStateNode.get("currentTick").asLong();
                        
                        slots.add(new SaveSlotInfo(i, timestamp, themeId, currentTick));
                    }
                } catch (IOException e) {
                    System.err.println("Warning: Could not read save file in slot " + i + ": " + e.getMessage());
                }
            }
        }
        
        return slots;
    }
    
    public boolean validateSaveFile(int slot) {
        if (slot < 1 || slot > 9) {
            return false;
        }
        
        String filename = saveDirectory + "slot_" + slot + ".json";
        File saveFile = new File(filename);
        
        if (!saveFile.exists()) {
            return false;
        }
        
        try {
            SaveData saveData = objectMapper.readValue(saveFile, SaveData.class);
            return saveData.metadata != null && saveData.gameState != null && 
                   saveData.characters != null && saveData.units != null;
        } catch (IOException e) {
            return false;
        }
    }
    
    // Test save methods (slots a-z, visible only in debug mode)
    
    public boolean saveToTestSlot(char slot, SaveData saveData) {
        if (slot < 'a' || slot > 'z') {
            System.err.println("Error: Test save slot must be between 'a' and 'z'");
            return false;
        }
        
        try {
            String filename = saveDirectory + "test_" + slot + ".json";
            
            // Update metadata with current timestamp and slot
            saveData.metadata.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            saveData.metadata.gameVersion = gameVersion;
            saveData.metadata.saveSlot = -(slot - 'a' + 1); // Negative slot for test saves
            
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), saveData);
            System.out.println("*** Game saved to test slot " + slot + ": " + filename);
            return true;
            
        } catch (IOException e) {
            System.err.println("Error saving game to test slot " + slot + ": " + e.getMessage());
            return false;
        }
    }
    
    public SaveData loadFromTestSlot(char slot) {
        if (slot < 'a' || slot > 'z') {
            System.err.println("Error: Test save slot must be between 'a' and 'z'");
            return null;
        }
        
        try {
            String filename = saveDirectory + "test_" + slot + ".json";
            File saveFile = new File(filename);
            
            if (!saveFile.exists()) {
                System.err.println("Error: No test save file found in slot " + slot);
                return null;
            }
            
            SaveData saveData = objectMapper.readValue(saveFile, SaveData.class);
            
            // Validate save file
            if (saveData.metadata == null || saveData.gameState == null) {
                System.err.println("Error: Corrupted test save file in slot " + slot);
                return null;
            }
            
            System.out.println("*** Game loaded from test slot " + slot + ": " + filename);
            return saveData;
            
        } catch (IOException e) {
            System.err.println("Error loading game from test slot " + slot + ": " + e.getMessage());
            return null;
        }
    }
    
    public List<SaveSlotInfo> listAvailableSlotsWithTests() {
        List<SaveSlotInfo> slots = new ArrayList<>();
        
        // Add regular slots (1-9)
        for (int i = 1; i <= 9; i++) {
            String filename = saveDirectory + "slot_" + i + ".json";
            File saveFile = new File(filename);
            
            if (saveFile.exists()) {
                try {
                    JsonNode rootNode = objectMapper.readTree(saveFile);
                    JsonNode metadataNode = rootNode.get("metadata");
                    JsonNode gameStateNode = rootNode.get("gameState");
                    
                    if (metadataNode != null && gameStateNode != null) {
                        String timestamp = metadataNode.get("timestamp").asText();
                        String themeId = metadataNode.get("themeId").asText();
                        long currentTick = gameStateNode.get("currentTick").asLong();
                        
                        slots.add(new SaveSlotInfo(i, timestamp, themeId, currentTick));
                    }
                } catch (IOException e) {
                    System.err.println("Warning: Could not read save file in slot " + i + ": " + e.getMessage());
                }
            }
        }
        
        // Add test slots (a-z) only if debug mode is active
        if (isDebugModeActive()) {
            for (char c = 'a'; c <= 'z'; c++) {
                String filename = saveDirectory + "test_" + c + ".json";
                File saveFile = new File(filename);
                
                if (saveFile.exists()) {
                    try {
                        JsonNode rootNode = objectMapper.readTree(saveFile);
                        JsonNode metadataNode = rootNode.get("metadata");
                        JsonNode gameStateNode = rootNode.get("gameState");
                        
                        if (metadataNode != null && gameStateNode != null) {
                            String timestamp = metadataNode.get("timestamp").asText();
                            String themeId = metadataNode.get("themeId").asText();
                            long currentTick = gameStateNode.get("currentTick").asLong();
                            
                            slots.add(new TestSaveSlotInfo(c, timestamp, themeId, currentTick));
                        }
                    } catch (IOException e) {
                        System.err.println("Warning: Could not read test save file in slot " + c + ": " + e.getMessage());
                    }
                }
            }
        }
        
        return slots;
    }
    
    public boolean validateTestSaveFile(char slot) {
        if (slot < 'a' || slot > 'z') {
            return false;
        }
        
        String filename = saveDirectory + "test_" + slot + ".json";
        File saveFile = new File(filename);
        
        if (!saveFile.exists()) {
            return false;
        }
        
        try {
            SaveData saveData = objectMapper.readValue(saveFile, SaveData.class);
            return saveData.metadata != null && saveData.gameState != null && 
                   saveData.characters != null && saveData.units != null;
        } catch (IOException e) {
            return false;
        }
    }
    
    public static class SaveSlotInfo {
        public final int slot;
        public final String timestamp;
        public final String themeId;
        public final long currentTick;
        
        public SaveSlotInfo(int slot, String timestamp, String themeId, long currentTick) {
            this.slot = slot;
            this.timestamp = timestamp;
            this.themeId = themeId;
            this.currentTick = currentTick;
        }
        
        public String getFormattedTimestamp() {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(timestamp);
                return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            } catch (Exception e) {
                return timestamp;
            }
        }
    }
    
    public static class TestSaveSlotInfo extends SaveSlotInfo {
        public final char testSlot;
        
        public TestSaveSlotInfo(char testSlot, String timestamp, String themeId, long currentTick) {
            super(-(testSlot - 'a' + 1), timestamp, themeId, currentTick); // Negative slot numbers for test saves
            this.testSlot = testSlot;
        }
        
        public String getSlotName() {
            return "test_" + testSlot;
        }
    }
}
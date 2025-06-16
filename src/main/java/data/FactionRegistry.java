package data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.paint.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class FactionRegistry {
    private static FactionRegistry instance;
    private final ObjectMapper objectMapper;
    private final String factionsDir = "factions";
    private final String registryFile = "factions/registry.json";
    
    private Map<Integer, Faction> factions;
    private int nextCharacterId;
    
    // Registry file structure
    public static class RegistryData {
        @JsonProperty("nextCharacterId")
        public int nextCharacterId;
        
        @JsonProperty("factions")
        public List<FactionInfo> factions;
        
        public RegistryData() {
            this.factions = new ArrayList<>();
        }
    }
    
    public static class FactionInfo {
        @JsonProperty("id")
        public int id;
        
        @JsonProperty("name")
        public String name;
        
        public FactionInfo() {}
        
        public FactionInfo(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
    
    // Faction file structure
    public static class FactionFileData {
        @JsonProperty("faction")
        public Faction faction;
        
        @JsonProperty("characters")
        public List<CharacterData> characters;
        
        public FactionFileData() {
            this.characters = new ArrayList<>();
        }
        
        public FactionFileData(Faction faction) {
            this.faction = faction;
            this.characters = new ArrayList<>();
        }
    }
    
    private FactionRegistry() {
        this.objectMapper = new ObjectMapper();
        this.factions = new HashMap<>();
        this.nextCharacterId = 1;
        loadRegistry();
    }
    
    public static FactionRegistry getInstance() {
        if (instance == null) {
            instance = new FactionRegistry();
        }
        return instance;
    }
    
    private void loadRegistry() {
        try {
            // Create factions directory if it doesn't exist
            Path factionsPath = Paths.get(factionsDir);
            if (!Files.exists(factionsPath)) {
                Files.createDirectories(factionsPath);
                System.out.println("Created factions/ directory");
            }
            
            // Load registry file if it exists
            File registryFileObj = new File(registryFile);
            if (registryFileObj.exists()) {
                RegistryData registryData = objectMapper.readValue(registryFileObj, RegistryData.class);
                nextCharacterId = registryData.nextCharacterId;
                
                // Load individual faction files
                for (FactionInfo info : registryData.factions) {
                    loadFactionFile(info.id);
                }
                
                System.out.println("Loaded faction registry with " + factions.size() + " factions, nextCharacterId: " + nextCharacterId);
            } else {
                // Create default factions if registry doesn't exist
                createDefaultFactions();
                saveRegistry();
                System.out.println("Created default faction registry");
            }
        } catch (IOException e) {
            System.err.println("Error loading faction registry: " + e.getMessage());
            createDefaultFactions();
        }
    }
    
    private void loadFactionFile(int factionId) {
        try {
            String filename = factionsDir + "/" + factionId + ".json";
            File factionFile = new File(filename);
            if (factionFile.exists()) {
                FactionFileData data = objectMapper.readValue(factionFile, FactionFileData.class);
                factions.put(factionId, data.faction);
            } else {
                System.err.println("Warning: Faction file not found: " + filename);
            }
        } catch (IOException e) {
            System.err.println("Error loading faction file " + factionId + ".json: " + e.getMessage());
        }
    }
    
    private void createDefaultFactions() {
        // NONE faction (ID: 0)
        Faction none = new Faction(0, "NONE", "No faction affiliation", Color.ORANGE);
        factions.put(0, none);
        
        // Union faction (ID: 1)
        Faction union = new Faction(1, "Union", "Union Army", Color.BLUE);
        union.addAlly(3); // Allied with Southern Unionists
        union.addEnemy(2); // Enemy with Confederacy
        factions.put(1, union);
        
        // Confederacy faction (ID: 2)
        Faction confederacy = new Faction(2, "Confederacy", "Confederate States", Color.DARKGRAY);
        confederacy.addEnemy(1); // Enemy with Union
        confederacy.addEnemy(3); // Enemy with Southern Unionists
        factions.put(2, confederacy);
        
        // Southern Unionists faction (ID: 3)
        Faction southernUnionists = new Faction(3, "Southern Unionists", "Pro-Union Southerners", Color.LIGHTBLUE);
        southernUnionists.addAlly(1); // Allied with Union
        southernUnionists.addEnemy(2); // Enemy with Confederacy
        factions.put(3, southernUnionists);
        
        // Save faction files
        for (Faction faction : factions.values()) {
            saveFactionFile(faction);
        }
    }
    
    public void saveRegistry() {
        try {
            RegistryData registryData = new RegistryData();
            registryData.nextCharacterId = nextCharacterId;
            
            for (Faction faction : factions.values()) {
                registryData.factions.add(new FactionInfo(faction.getId(), faction.getName()));
            }
            
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(registryFile), registryData);
        } catch (IOException e) {
            System.err.println("Error saving faction registry: " + e.getMessage());
        }
    }
    
    public void saveFactionFile(Faction faction) {
        try {
            String filename = factionsDir + "/" + faction.getId() + ".json";
            FactionFileData data = new FactionFileData(faction);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), data);
        } catch (IOException e) {
            System.err.println("Error saving faction file " + faction.getId() + ".json: " + e.getMessage());
        }
    }
    
    // Public API methods
    public Faction getFaction(int factionId) {
        return factions.get(factionId);
    }
    
    public Map<Integer, Faction> getAllFactions() {
        return new HashMap<>(factions);
    }
    
    public void addFaction(Faction faction) {
        factions.put(faction.getId(), faction);
        saveFactionFile(faction);
        saveRegistry();
    }
    
    public void updateFaction(Faction faction) {
        factions.put(faction.getId(), faction);
        saveFactionFile(faction);
    }
    
    public boolean hasFaction(int factionId) {
        return factions.containsKey(factionId);
    }
    
    public int getNextCharacterId() {
        return nextCharacterId;
    }
    
    public void updateNextCharacterId(int newNextCharacterId) {
        this.nextCharacterId = newNextCharacterId;
        saveRegistry();
    }
    
    public int allocateCharacterIds(int count) {
        int startId = nextCharacterId;
        nextCharacterId += count;
        saveRegistry();
        return startId;
    }
    
    public String[] getAllFactionNames() {
        return factions.values().stream()
                .map(Faction::getName)
                .toArray(String[]::new);
    }
    
    public Integer[] getAllFactionIds() {
        return factions.keySet().toArray(new Integer[0]);
    }
    
    public void printFactionInfo() {
        System.out.println("=== FACTION REGISTRY ===");
        System.out.println("Next Character ID: " + nextCharacterId);
        System.out.println("Factions:");
        for (Faction faction : factions.values()) {
            System.out.println("  " + faction);
        }
    }
    
    // Additional methods for CharacterPersistenceManager
    public FactionFileData loadFactionFileData(int factionId) throws IOException {
        String filename = factionsDir + "/" + factionId + ".json";
        File factionFile = new File(filename);
        if (factionFile.exists()) {
            return objectMapper.readValue(factionFile, FactionFileData.class);
        } else {
            throw new IOException("Faction file not found: " + filename);
        }
    }
    
    public void saveFactionFileData(int factionId, FactionFileData data) throws IOException {
        String filename = factionsDir + "/" + factionId + ".json";
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), data);
    }
}
package data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.ArrayList;

public class SaveData {
    @JsonProperty("metadata")
    public SaveMetadata metadata;
    
    @JsonProperty("gameState")
    public GameStateData gameState;
    
    @JsonProperty("units")
    public List<UnitData> units;
    
    // Legacy field for backward compatibility - will be migrated to universal registry
    @JsonProperty("characters")
    public List<CharacterData> characters;
    
    public SaveData() {
        // Default constructor for Jackson
        this.units = new ArrayList<>();
        this.characters = new ArrayList<>();
    }
    
    public SaveData(SaveMetadata metadata, GameStateData gameState, List<UnitData> units) {
        this.metadata = metadata;
        this.gameState = gameState;
        this.units = units != null ? units : new ArrayList<>();
        this.characters = new ArrayList<>(); // Empty - characters are in universal registry
    }
    
    // Legacy constructor for backward compatibility
    public SaveData(SaveMetadata metadata, GameStateData gameState, 
                   List<CharacterData> characters, List<UnitData> units) {
        this.metadata = metadata;
        this.gameState = gameState;
        this.characters = characters != null ? characters : new ArrayList<>();
        this.units = units != null ? units : new ArrayList<>();
    }
}
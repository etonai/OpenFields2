package data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.ArrayList;

public class SaveData {
    @JsonProperty("metadata")
    public SaveMetadata metadata;
    
    @JsonProperty("gameState")
    public GameStateData gameState;
    
    @JsonProperty("characters")
    public List<CharacterData> characters;
    
    @JsonProperty("units")
    public List<UnitData> units;
    
    public SaveData() {
        // Default constructor for Jackson
        this.characters = new ArrayList<>();
        this.units = new ArrayList<>();
    }
    
    public SaveData(SaveMetadata metadata, GameStateData gameState, 
                   List<CharacterData> characters, List<UnitData> units) {
        this.metadata = metadata;
        this.gameState = gameState;
        this.characters = characters != null ? characters : new ArrayList<>();
        this.units = units != null ? units : new ArrayList<>();
    }
}
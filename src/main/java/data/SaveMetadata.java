package data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SaveMetadata {
    @JsonProperty("timestamp")
    public String timestamp;
    
    @JsonProperty("gameVersion")
    public String gameVersion;
    
    @JsonProperty("themeId")
    public String themeId;
    
    @JsonProperty("saveSlot")
    public int saveSlot;
    
    public SaveMetadata() {
        // Default constructor for Jackson
    }
    
    public SaveMetadata(String timestamp, String gameVersion, String themeId, int saveSlot) {
        this.timestamp = timestamp;
        this.gameVersion = gameVersion;
        this.themeId = themeId;
        this.saveSlot = saveSlot;
    }
}
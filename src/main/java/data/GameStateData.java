package data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GameStateData {
    @JsonProperty("currentTick")
    public long currentTick;
    
    @JsonProperty("paused")
    public boolean paused;
    
    @JsonProperty("offsetX")
    public double offsetX;
    
    @JsonProperty("offsetY")
    public double offsetY;
    
    @JsonProperty("zoom")
    public double zoom;
    
    @JsonProperty("nextCharacterId")
    public int nextCharacterId;
    
    @JsonProperty("nextUnitId")
    public int nextUnitId;
    
    public GameStateData() {
        // Default constructor for Jackson
    }
    
    public GameStateData(long currentTick, boolean paused, double offsetX, double offsetY, 
                        double zoom, int nextCharacterId, int nextUnitId) {
        this.currentTick = currentTick;
        this.paused = paused;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.zoom = zoom;
        this.nextCharacterId = nextCharacterId;
        this.nextUnitId = nextUnitId;
    }
}
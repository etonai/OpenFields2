package data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WeaponStateData {
    @JsonProperty("state")
    public String state;
    
    @JsonProperty("action")
    public String action;
    
    @JsonProperty("ticks")
    public int ticks;
    
    public WeaponStateData() {
        // Default constructor for Jackson
    }
    
    public WeaponStateData(String state, String action, int ticks) {
        this.state = state;
        this.action = action;
        this.ticks = ticks;
    }
}
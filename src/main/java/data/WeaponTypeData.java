package data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class WeaponTypeData {
    @JsonProperty("initialState")
    public String initialState;
    
    @JsonProperty("states")
    public List<WeaponStateData> states;
    
    public WeaponTypeData() {
        // Default constructor for Jackson
    }
    
    public WeaponTypeData(String initialState, List<WeaponStateData> states) {
        this.initialState = initialState;
        this.states = states;
    }
}
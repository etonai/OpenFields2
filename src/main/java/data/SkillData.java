package data;

import com.fasterxml.jackson.annotation.JsonProperty;
import combat.WeaponType;
import java.util.List;

public class SkillData {
    @JsonProperty("id")
    public String id;
    
    @JsonProperty("name")
    public String name;
    
    @JsonProperty("description")
    public String description;
    
    @JsonProperty("maxLevel")
    public int maxLevel;
    
    @JsonProperty("effectType")
    public String effectType;
    
    @JsonProperty("effectValue")
    public double effectValue;
    
    @JsonProperty("appliesTo")
    public List<WeaponType> appliesTo;
    
    public SkillData() {
        // Default constructor for Jackson
    }
    
    public SkillData(String id, String name, String description, int maxLevel, String effectType, 
                    double effectValue, List<WeaponType> appliesTo) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.maxLevel = maxLevel;
        this.effectType = effectType;
        this.effectValue = effectValue;
        this.appliesTo = appliesTo;
    }
}
package data;

import com.fasterxml.jackson.annotation.JsonProperty;
import combat.WeaponType;

public class WeaponData {
    @JsonProperty("id")
    public String id;
    
    @JsonProperty("name")
    public String name;
    
    @JsonProperty("type")
    public WeaponType type;
    
    @JsonProperty("velocity")
    public double velocity;
    
    @JsonProperty("damage")
    public int damage;
    
    @JsonProperty("ammunition")
    public int ammunition;
    
    @JsonProperty("soundFile")
    public String soundFile;
    
    @JsonProperty("maximumRange")
    public double maximumRange;
    
    @JsonProperty("weaponAccuracy")
    public int weaponAccuracy;
    
    public WeaponData() {
        // Default constructor for Jackson
    }
    
    public WeaponData(String id, String name, WeaponType type, double velocity, int damage, int ammunition, 
                     String soundFile, double maximumRange, int weaponAccuracy) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.velocity = velocity;
        this.damage = damage;
        this.ammunition = ammunition;
        this.soundFile = soundFile;
        this.maximumRange = maximumRange;
        this.weaponAccuracy = weaponAccuracy;
    }
}
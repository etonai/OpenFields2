package data;

import com.fasterxml.jackson.annotation.JsonProperty;
import combat.WeaponType;
import combat.ReloadType;
import combat.FiringMode;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

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
    
    @JsonProperty("maxAmmunition")
    public int maxAmmunition;
    
    @JsonProperty("reloadTicks")
    public int reloadTicks;
    
    @JsonProperty("reloadType")
    public ReloadType reloadType;
    
    @JsonProperty("projectileName")
    public String projectileName;
    
    @JsonProperty("firingDelay")
    public int firingDelay;
    
    // Automatic firing properties
    @JsonProperty("cyclicRate")
    public int cyclicRate = 60; // Default 1 second between shots
    
    @JsonProperty("burstSize")
    public int burstSize = 3; // Default 3-round bursts
    
    @JsonProperty("availableFiringModes")
    public List<FiringMode> availableFiringModes = new ArrayList<>(Arrays.asList(FiringMode.SINGLE_SHOT));
    
    public WeaponData() {
        // Default constructor for Jackson
    }
    
    public WeaponData(String id, String name, WeaponType type, double velocity, int damage, int ammunition, 
                     String soundFile, double maximumRange, int weaponAccuracy, int maxAmmunition, 
                     int reloadTicks, ReloadType reloadType, String projectileName, int firingDelay) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.velocity = velocity;
        this.damage = damage;
        this.ammunition = ammunition;
        this.soundFile = soundFile;
        this.maximumRange = maximumRange;
        this.weaponAccuracy = weaponAccuracy;
        this.maxAmmunition = maxAmmunition;
        this.reloadTicks = reloadTicks;
        this.reloadType = reloadType;
        this.projectileName = projectileName;
        this.firingDelay = firingDelay;
    }
}
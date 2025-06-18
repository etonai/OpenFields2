package data;

import com.fasterxml.jackson.annotation.JsonProperty;
import combat.WeaponType;
import combat.MeleeWeaponType;

public class MeleeWeaponData {
    @JsonProperty("id")
    public String id;
    
    @JsonProperty("name")
    public String name;
    
    @JsonProperty("type")
    public WeaponType type; // MELEE_SHORT, MELEE_MEDIUM, MELEE_LONG, etc.
    
    @JsonProperty("damage")
    public int damage;
    
    @JsonProperty("soundFile")
    public String soundFile;
    
    @JsonProperty("weaponLength")
    public double weaponLength = 1.0; // Default weapon length 1 foot
    
    @JsonProperty("weaponAccuracy")
    public int weaponAccuracy;
    
    @JsonProperty("meleeType")
    public MeleeWeaponType meleeType;
    
    @JsonProperty("defendScore")
    public int defendScore; // 1-100 defensive capability
    
    @JsonProperty("attackSpeed")
    public int attackSpeed; // ticks to perform attack
    
    @JsonProperty("attackCooldown")
    public int attackCooldown; // ticks before next attack
    
    @JsonProperty("readyingTime")
    public int readyingTime; // ticks to ready weapon
    
    @JsonProperty("isOneHanded")
    public boolean isOneHanded;
    
    @JsonProperty("isMeleeVersionOfRanged")
    public boolean isMeleeVersionOfRanged;
    
    @JsonProperty("projectileName")
    public String projectileName;
    
    public MeleeWeaponData() {
        // Default constructor for Jackson
    }
    
    public MeleeWeaponData(String id, String name, WeaponType type, int damage, String soundFile, 
                          double weaponLength, int weaponAccuracy, MeleeWeaponType meleeType, 
                          int defendScore, int attackSpeed, int attackCooldown, int readyingTime, 
                          boolean isOneHanded, boolean isMeleeVersionOfRanged, String projectileName) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.damage = damage;
        this.soundFile = soundFile;
        this.weaponLength = weaponLength;
        this.weaponAccuracy = weaponAccuracy;
        this.meleeType = meleeType;
        this.defendScore = defendScore;
        this.attackSpeed = attackSpeed;
        this.attackCooldown = attackCooldown;
        this.readyingTime = readyingTime;
        this.isOneHanded = isOneHanded;
        this.isMeleeVersionOfRanged = isMeleeVersionOfRanged;
        this.projectileName = projectileName;
    }
}
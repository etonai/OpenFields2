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
    
    @JsonProperty("defenseCooldown")
    public int defenseCooldown = 60; // DevCycle 23: default 60 ticks
    
    @JsonProperty("defenseBonus")
    public int defenseBonus = 0; // DevCycle 40: Defense bonus value (0-20) for defense calculations
    
    @JsonProperty("woundDescription")
    public String woundDescription; // Renamed from projectileName (DevCycle 17)
    
    @JsonProperty("combatSkill")
    public String combatSkill; // Combat skill associated with this weapon (DevCycle 17)
    
    @JsonProperty("states")
    public java.util.List<WeaponStateData> states; // Weapon state transitions for state-based timing
    
    public MeleeWeaponData() {
        // Default constructor for Jackson
    }
    
    public MeleeWeaponData(String id, String name, WeaponType type, int damage, String soundFile, 
                          double weaponLength, int weaponAccuracy, MeleeWeaponType meleeType, 
                          int defendScore, int attackSpeed, int attackCooldown, int readyingTime, 
                          boolean isOneHanded, boolean isMeleeVersionOfRanged, int defenseBonus, String woundDescription, String combatSkill) {
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
        this.defenseBonus = defenseBonus;
        this.woundDescription = woundDescription;
        this.combatSkill = combatSkill;
    }
}
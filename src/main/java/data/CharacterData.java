package data;

import com.fasterxml.jackson.annotation.JsonProperty;
import combat.Handedness;
import combat.MovementType;
import combat.AimingSpeed;
import java.util.List;
import java.util.ArrayList;

public class CharacterData {
    @JsonProperty("id")
    public int id;
    
    @JsonProperty("name")
    public String name;
    
    @JsonProperty("themeId")
    public String themeId;
    
    @JsonProperty("dexterity")
    public int dexterity;
    
    @JsonProperty("currentDexterity")
    public int currentDexterity;
    
    @JsonProperty("health")
    public int health;
    
    @JsonProperty("currentHealth")
    public int currentHealth;
    
    @JsonProperty("coolness")
    public int coolness;
    
    @JsonProperty("strength")
    public int strength;
    
    @JsonProperty("reflexes")
    public int reflexes;
    
    @JsonProperty("handedness")
    public Handedness handedness;
    
    @JsonProperty("baseMovementSpeed")
    public double baseMovementSpeed;
    
    @JsonProperty("currentMovementType")
    public MovementType currentMovementType;
    
    @JsonProperty("currentAimingSpeed")
    public AimingSpeed currentAimingSpeed;
    
    @JsonProperty("weaponId")
    public String weaponId;
    
    @JsonProperty("currentWeaponState")
    public String currentWeaponState;
    
    @JsonProperty("queuedShots")
    public int queuedShots;
    
    @JsonProperty("skills")
    public List<SkillData> skills;
    
    @JsonProperty("wounds")
    public List<WoundData> wounds;
    
    public CharacterData() {
        // Default constructor for Jackson
        this.skills = new ArrayList<>();
        this.wounds = new ArrayList<>();
    }
    
    public static class SkillData {
        @JsonProperty("skillName")
        public String skillName;
        
        @JsonProperty("level")
        public int level;
        
        public SkillData() {
            // Default constructor for Jackson
        }
        
        public SkillData(String skillName, int level) {
            this.skillName = skillName;
            this.level = level;
        }
    }
    
    public static class WoundData {
        @JsonProperty("bodyPart")
        public String bodyPart;
        
        @JsonProperty("severity")
        public String severity;
        
        public WoundData() {
            // Default constructor for Jackson
        }
        
        public WoundData(String bodyPart, String severity) {
            this.bodyPart = bodyPart;
            this.severity = severity;
        }
    }
}
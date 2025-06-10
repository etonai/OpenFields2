package data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import combat.Handedness;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

/**
 * Universal character data for cross-theme character storage
 * Contains only theme-independent attributes
 */
public class UniversalCharacterData {
    @JsonProperty("id")
    public int id;
    
    @JsonProperty("nickname")
    public String nickname;
    
    @JsonProperty("firstName")
    public String firstName;
    
    @JsonProperty("lastName")
    public String lastName;
    
    @JsonProperty("birthdate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMMM d, yyyy")
    public Date birthdate;
    
    @JsonProperty("dexterity")
    public int dexterity;
    
    @JsonProperty("health")
    public int health;
    
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
    
    @JsonProperty("skills")
    public List<SkillData> skills;
    
    @JsonProperty("wounds")
    public List<WoundData> wounds;
    
    public UniversalCharacterData() {
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
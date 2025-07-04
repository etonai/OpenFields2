package data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import combat.Handedness;
import combat.MovementType;
import combat.AimingSpeed;
import combat.FiringMode;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

public class CharacterData {
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
    
    @JsonProperty("skills")
    public List<SkillData> skills;
    
    @JsonProperty("wounds")
    public List<WoundData> wounds;
    
    @JsonProperty("usesAutomaticTargeting")
    public boolean usesAutomaticTargeting = false;
    
    @JsonProperty("preferredFiringMode")
    public FiringMode preferredFiringMode = FiringMode.SINGLE_SHOT;
    
    // Faction data
    @JsonProperty("faction")
    public int faction = 0;
    
    // Battle statistics 
    @JsonProperty("combatEngagements")
    public int combatEngagements = 0;
    
    @JsonProperty("woundsReceived")
    public int woundsReceived = 0;
    
    @JsonProperty("woundsInflictedScratch")
    public int woundsInflictedScratch = 0;
    
    @JsonProperty("woundsInflictedLight")
    public int woundsInflictedLight = 0;
    
    @JsonProperty("woundsInflictedSerious")
    public int woundsInflictedSerious = 0;
    
    @JsonProperty("woundsInflictedCritical")
    public int woundsInflictedCritical = 0;
    
    @JsonProperty("attacksAttempted")
    public int attacksAttempted = 0;
    
    @JsonProperty("attacksSuccessful")
    public int attacksSuccessful = 0;
    
    @JsonProperty("targetsIncapacitated")
    public int targetsIncapacitated = 0;
    
    @JsonProperty("headshotsAttempted")
    public int headshotsAttempted = 0;
    
    @JsonProperty("headshotsSuccessful")
    public int headshotsSuccessful = 0;
    
    @JsonProperty("headshotIncapacitations")
    public int headshotIncapacitations = 0;
    
    @JsonProperty("battlesParticipated")
    public int battlesParticipated = 0;
    
    @JsonProperty("victories")
    public int victories = 0;
    
    @JsonProperty("defeats")
    public int defeats = 0;
    
    // Defensive Statistics (DevCycle 23)
    @JsonProperty("defensiveAttempts")
    public int defensiveAttempts = 0;
    
    @JsonProperty("defensiveSuccesses")
    public int defensiveSuccesses = 0;
    
    @JsonProperty("counterAttacksExecuted")
    public int counterAttacksExecuted = 0;
    
    @JsonProperty("counterAttacksSuccessful")
    public int counterAttacksSuccessful = 0;
    
    // Defense state fields (DevCycle 23)
    @JsonProperty("currentDefenseState")
    public String currentDefenseState = "READY";
    
    @JsonProperty("defenseCooldownEndTick")
    public long defenseCooldownEndTick = 0;
    
    @JsonProperty("counterAttackWindowEndTick")
    public long counterAttackWindowEndTick = 0;
    
    @JsonProperty("hasCounterAttackOpportunity")
    public boolean hasCounterAttackOpportunity = false;
    
    // DevCycle 28: Multiple shot control
    @JsonProperty("multipleShootCount")
    public int multipleShootCount = 1;
    
    // DevCycle 40: Melee combat mode
    @JsonProperty("isMeleeCombatMode")
    public boolean isMeleeCombatMode = false;
    
    // DevCycle 40: Defense system timing
    @JsonProperty("nextDefenseTick")
    public long nextDefenseTick = 0;
    
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
        
        @JsonProperty("damage")
        public int damage;
        
        public WoundData() {
            // Default constructor for Jackson
        }
        
        public WoundData(String bodyPart, String severity) {
            this.bodyPart = bodyPart;
            this.severity = severity;
            this.damage = 1; // Default for backwards compatibility
        }
        
        public WoundData(String bodyPart, String severity, int damage) {
            this.bodyPart = bodyPart;
            this.severity = severity;
            this.damage = damage;
        }
    }
}
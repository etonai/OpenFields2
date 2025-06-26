package combat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Builder pattern for creating Character instances with flexible configuration.
 * Extracted from Character.java to reduce constructor proliferation and improve code organization.
 */
public class CharacterBuilder {
    // Identity fields
    private int id = 0;
    private String nickname = "Character";
    private String firstName = "";
    private String lastName = "";
    private Date birthdate = new Date();
    private String themeId = "default_theme";
    
    // Stats fields
    private int dexterity = 50;
    private int health = 100;
    private int coolness = 50;
    private int strength = 50;
    private int reflexes = 50;
    private Handedness handedness = Handedness.RIGHT_HANDED;
    private double baseMovementSpeed = 42.0;
    
    // Other fields
    private Weapon weapon = null;
    private List<Skill> skills = new ArrayList<>();
    private int faction = 1;
    private boolean usesAutomaticTargeting = false;
    private FiringMode preferredFiringMode = FiringMode.SINGLE_SHOT;
    
    // Identity setters
    public CharacterBuilder withId(int id) {
        this.id = id;
        return this;
    }
    
    public CharacterBuilder withNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }
    
    public CharacterBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }
    
    public CharacterBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }
    
    public CharacterBuilder withBirthdate(Date birthdate) {
        this.birthdate = birthdate;
        return this;
    }
    
    public CharacterBuilder withThemeId(String themeId) {
        this.themeId = themeId;
        return this;
    }
    
    // Stats setters
    public CharacterBuilder withDexterity(int dexterity) {
        this.dexterity = dexterity;
        return this;
    }
    
    public CharacterBuilder withHealth(int health) {
        this.health = health;
        return this;
    }
    
    public CharacterBuilder withCoolness(int coolness) {
        this.coolness = coolness;
        return this;
    }
    
    public CharacterBuilder withStrength(int strength) {
        this.strength = strength;
        return this;
    }
    
    public CharacterBuilder withReflexes(int reflexes) {
        this.reflexes = reflexes;
        return this;
    }
    
    public CharacterBuilder withHandedness(Handedness handedness) {
        this.handedness = handedness;
        return this;
    }
    
    public CharacterBuilder withBaseMovementSpeed(double baseMovementSpeed) {
        this.baseMovementSpeed = baseMovementSpeed;
        return this;
    }
    
    // All stats at once
    public CharacterBuilder withStats(int dexterity, int health, int coolness, int strength, int reflexes) {
        this.dexterity = dexterity;
        this.health = health;
        this.coolness = coolness;
        this.strength = strength;
        this.reflexes = reflexes;
        return this;
    }
    
    // Equipment and skills
    public CharacterBuilder withWeapon(Weapon weapon) {
        this.weapon = weapon;
        return this;
    }
    
    public CharacterBuilder withSkills(List<Skill> skills) {
        this.skills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
        return this;
    }
    
    public CharacterBuilder addSkill(Skill skill) {
        if (skill != null) {
            this.skills.add(skill);
        }
        return this;
    }
    
    // Configuration
    public CharacterBuilder withFaction(int faction) {
        this.faction = faction;
        return this;
    }
    
    public CharacterBuilder withAutomaticTargeting(boolean usesAutomaticTargeting) {
        this.usesAutomaticTargeting = usesAutomaticTargeting;
        return this;
    }
    
    public CharacterBuilder withPreferredFiringMode(FiringMode preferredFiringMode) {
        this.preferredFiringMode = preferredFiringMode;
        return this;
    }
    
    // Convenience methods for common configurations
    public static CharacterBuilder testCharacter(String nickname) {
        return new CharacterBuilder()
            .withNickname(nickname)
            .withThemeId("test_theme");
    }
    
    public static CharacterBuilder civilWarSoldier(String nickname) {
        return new CharacterBuilder()
            .withNickname(nickname)
            .withStats(60, 90, 70, 65, 55) // Typical soldier stats
            .withThemeId("civil_war")
            .withFaction(1);
    }
    
    public static CharacterBuilder gunslinger(String nickname) {
        return new CharacterBuilder()
            .withNickname(nickname)
            .withStats(80, 85, 75, 60, 70) // High dex, good health
            .withThemeId("gunslinger")
            .withFaction(2);
    }
    
    /**
     * Creates the Character instance with all configured values
     */
    public Character build() {
        // Create identity and stats value objects
        CharacterIdentity identity = new CharacterIdentity(id, nickname, firstName, lastName, birthdate, themeId);
        CharacterStats stats = new CharacterStats(dexterity, health, coolness, strength, reflexes, handedness, baseMovementSpeed);
        
        // Create the character using the comprehensive constructor
        Character character = new Character(id, nickname, firstName, lastName, birthdate, themeId, 
                                          dexterity, health, coolness, strength, reflexes, handedness);
        
        // Set additional properties
        if (weapon != null) {
            character.weapon = weapon;
        }
        character.skills = new ArrayList<>(skills);
        character.faction = faction;
        character.usesAutomaticTargeting = usesAutomaticTargeting;
        character.preferredFiringMode = preferredFiringMode;
        
        // Set the value objects
        character.identity = identity;
        character.stats = stats;
        
        return character;
    }
}
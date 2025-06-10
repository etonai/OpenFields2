package data;

import combat.Character;
import combat.Handedness;
import combat.Skill;
import java.util.Date;
import java.util.Calendar;
import java.util.Random;

/**
 * Factory for creating characters with pre-defined archetypes and themes
 */
public class CharacterFactory {
    private static final Random random = new Random();
    private static final UniversalCharacterRegistry registry = UniversalCharacterRegistry.getInstance();
    
    /**
     * Creates a character and registers it in the universal registry
     * @param archetype The character archetype to create
     * @return The character ID in the registry
     */
    public static int createCharacter(String archetype) {
        Character character = createCharacterByArchetype(archetype);
        return registry.registerCharacter(character);
    }
    
    /**
     * Creates a character with specific attributes and registers it
     */
    public static int createCharacter(String nickname, String firstName, String lastName, 
                                    Date birthdate, int dexterity, int health, int coolness, 
                                    int strength, int reflexes, Handedness handedness) {
        Character character = new Character(
            0, // ID will be assigned by registry
            nickname, firstName, lastName, birthdate,
            null, // No theme in universal registry
            dexterity, health, coolness, strength, reflexes, handedness
        );
        
        return registry.registerCharacter(character);
    }
    
    /**
     * Creates a character based on archetype
     */
    private static Character createCharacterByArchetype(String archetype) {
        switch (archetype.toLowerCase()) {
            case "gunslinger":
                return createGunslinger();
            case "soldier":
                return createSoldier();
            case "medic":
                return createMedic();
            case "scout":
                return createScout();
            case "marksman":
                return createMarksman();
            case "brawler":
                return createBrawler();
            default:
                return createBalanced();
        }
    }
    
    private static Character createGunslinger() {
        Character character = new Character(
            0, // ID assigned by registry
            generateName("Gunslinger"),
            generateFirstName(),
            generateLastName(),
            generateBirthdate(),
            null, // No theme
            85, // High dexterity
            10, 
            80, // High coolness
            60,
            90, // High reflexes
            Handedness.RIGHT_HANDED
        );
        
        // Add relevant skills
        character.addSkill(new Skill(SkillsManager.PISTOL, 8));
        character.addSkill(new Skill(SkillsManager.QUICKDRAW, 7));
        character.addSkill(new Skill(SkillsManager.OBSERVATION, 6));
        
        return character;
    }
    
    private static Character createSoldier() {
        Character character = new Character(
            0,
            generateName("Soldier"),
            generateFirstName(),
            generateLastName(),
            generateBirthdate(),
            null,
            75,
            15, // High health
            70,
            80, // High strength
            70,
            Handedness.RIGHT_HANDED
        );
        
        character.addSkill(new Skill(SkillsManager.RIFLE, 7));
        character.addSkill(new Skill(SkillsManager.ATHLETICS, 6));
        character.addSkill(new Skill(SkillsManager.INTIMIDATION, 5));
        
        return character;
    }
    
    private static Character createMedic() {
        Character character = new Character(
            0,
            generateName("Medic"),
            generateFirstName(),
            generateLastName(),
            generateBirthdate(),
            null,
            80, // Good dexterity for medical work
            12,
            85, // High coolness under pressure
            65,
            75,
            Handedness.RIGHT_HANDED
        );
        
        character.addSkill(new Skill(SkillsManager.MEDICINE, 9));
        character.addSkill(new Skill(SkillsManager.OBSERVATION, 7));
        character.addSkill(new Skill(SkillsManager.PISTOL, 4));
        
        return character;
    }
    
    private static Character createScout() {
        Character character = new Character(
            0,
            generateName("Scout"),
            generateFirstName(),
            generateLastName(),
            generateBirthdate(),
            null,
            90, // Very high dexterity
            11,
            75,
            70,
            85, // High reflexes
            Handedness.RIGHT_HANDED
        );
        
        character.addSkill(new Skill(SkillsManager.STEALTH, 8));
        character.addSkill(new Skill(SkillsManager.OBSERVATION, 8));
        character.addSkill(new Skill(SkillsManager.ATHLETICS, 7));
        character.addSkill(new Skill(SkillsManager.PISTOL, 5));
        
        return character;
    }
    
    private static Character createMarksman() {
        Character character = new Character(
            0,
            generateName("Marksman"),
            generateFirstName(),
            generateLastName(),
            generateBirthdate(),
            null,
            95, // Excellent dexterity
            10,
            85, // High coolness
            65,
            80,
            Handedness.RIGHT_HANDED
        );
        
        character.addSkill(new Skill(SkillsManager.RIFLE, 9));
        character.addSkill(new Skill(SkillsManager.OBSERVATION, 8));
        character.addSkill(new Skill(SkillsManager.STEALTH, 6));
        
        return character;
    }
    
    private static Character createBrawler() {
        Character character = new Character(
            0,
            generateName("Brawler"),
            generateFirstName(),
            generateLastName(),
            generateBirthdate(),
            null,
            70,
            18, // Very high health
            60,
            90, // Very high strength
            75,
            Handedness.RIGHT_HANDED
        );
        
        character.addSkill(new Skill(SkillsManager.ATHLETICS, 8));
        character.addSkill(new Skill(SkillsManager.INTIMIDATION, 7));
        character.addSkill(new Skill(SkillsManager.PISTOL, 4));
        
        return character;
    }
    
    private static Character createBalanced() {
        Character character = new Character(
            0,
            generateName("Adventurer"),
            generateFirstName(),
            generateLastName(),
            generateBirthdate(),
            null,
            75, // Balanced stats
            12,
            75,
            75,
            75,
            Handedness.RIGHT_HANDED
        );
        
        character.addSkill(new Skill(SkillsManager.PISTOL, 6));
        character.addSkill(new Skill(SkillsManager.OBSERVATION, 5));
        character.addSkill(new Skill(SkillsManager.ATHLETICS, 5));
        
        return character;
    }
    
    // Helper methods for generating character attributes
    private static String generateName(String archetype) {
        String[] prefixes = {"", "Young ", "Old ", "Wild ", "Silent ", "Quick ", "Dead-Eye ", "Iron "};
        String[] suffixes = {"", " Jr.", " Sr.", " the Bold", " the Wise", " the Swift"};
        
        return prefixes[random.nextInt(prefixes.length)] + archetype + 
               suffixes[random.nextInt(suffixes.length)];
    }
    
    private static String generateFirstName() {
        String[] maleNames = {"John", "William", "James", "Charles", "George", "Frank", "Joseph", "Thomas", "Henry", "Robert", "Edward", "Samuel", "David", "Walter", "Arthur", "Albert"};
        String[] femaleNames = {"Mary", "Anna", "Emma", "Elizabeth", "Margaret", "Minnie", "Ida", "Bertha", "Clara", "Alice", "Annie", "Florence", "Bessie", "Grace", "Ethel", "Sarah"};
        
        if (random.nextBoolean()) {
            return maleNames[random.nextInt(maleNames.length)];
        } else {
            return femaleNames[random.nextInt(femaleNames.length)];
        }
    }
    
    private static String generateLastName() {
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Wilson", "Moore", "Taylor", "Anderson", "Thomas", "Jackson", "White", "Harris", "Martin", "Thompson", "Garcia", "Martinez", "Robinson"};
        return lastNames[random.nextInt(lastNames.length)];
    }
    
    private static Date generateBirthdate() {
        Calendar cal = Calendar.getInstance();
        // Generate birthdate between 1850-1870 for a western theme feel
        int year = 1850 + random.nextInt(20);
        int month = random.nextInt(12);
        int day = 1 + random.nextInt(28); // Avoid month-end issues
        cal.set(year, month, day);
        return cal.getTime();
    }
    
    /**
     * Lists all available character archetypes
     */
    public static String[] getAvailableArchetypes() {
        return new String[]{
            "gunslinger", "soldier", "medic", "scout", "marksman", "brawler", "balanced"
        };
    }
    
    /**
     * Gets a description of an archetype
     */
    public static String getArchetypeDescription(String archetype) {
        switch (archetype.toLowerCase()) {
            case "gunslinger":
                return "High dexterity and reflexes, excellent with pistols and quickdraw";
            case "soldier":
                return "Balanced fighter with good health, strength, and rifle skills";
            case "medic":
                return "High medical skills and coolness under pressure";
            case "scout":
                return "Excellent stealth, observation, and mobility";
            case "marksman":
                return "Elite rifleman with exceptional accuracy";
            case "brawler":
                return "High health and strength, prefers close combat";
            case "balanced":
                return "Well-rounded character good at many things";
            default:
                return "Unknown archetype";
        }
    }
}
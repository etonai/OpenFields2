package data;

import combat.Character;
import combat.Handedness;
import combat.Skill;
import java.util.Date;
import java.util.Calendar;
import java.util.Random;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Factory for creating characters with pre-defined archetypes and themes
 */
public class CharacterFactory {
    private static final Random random = new Random();
    
    // CSV-based name data cache
    private static List<String> weightedMaleNames = null;
    private static boolean csvNamesLoaded = false;
    private static final UniversalCharacterRegistry registry = UniversalCharacterRegistry.getInstance();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ThemeManager themeManager = ThemeManager.getInstance();
    
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
            case "confederate_soldier":
                return createConfederateSoldier();
            case "union_soldier":
                return createUnionSoldier();
            case "weighted_random":
                return createWeightedRandom();
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
            9, 
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
            8,
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
    
    private static Character createWeightedRandom() {
        // Generate two sets of random stats and average them for more balanced results
        int dex1 = random.nextInt(100) + 1, dex2 = random.nextInt(100) + 1;
        int health1 = random.nextInt(14) + 7, health2 = random.nextInt(14) + 7; // Health between 7-20
        int cool1 = random.nextInt(100) + 1, cool2 = random.nextInt(100) + 1;
        int str1 = random.nextInt(100) + 1, str2 = random.nextInt(100) + 1;
        int ref1 = random.nextInt(100) + 1, ref2 = random.nextInt(100) + 1;
        
        // Calculate averages
        int avgDexterity = (dex1 + dex2) / 2;
        int avgHealth = (health1 + health2) / 2;
        int avgCoolness = (cool1 + cool2) / 2;
        int avgStrength = (str1 + str2) / 2;
        int avgReflexes = (ref1 + ref2) / 2;
        
        // Random handedness
        Handedness randomHandedness = random.nextBoolean() ? 
            Handedness.LEFT_HANDED : Handedness.RIGHT_HANDED;
        
        Character character = new Character(
            0, // ID assigned by registry
            generateName("Wanderer"),
            generateFirstName(),
            generateLastName(),
            generateBirthdate(),
            null, // No theme
            avgDexterity,
            avgHealth,
            avgCoolness,
            avgStrength,
            avgReflexes,
            randomHandedness
        );
        
        // No skills assigned for weighted random - blank slate character
        return character;
    }
    
    private static Character createConfederateSoldier() {
        // Use the same random stat generation as weighted_random
        int dex1 = random.nextInt(100) + 1, dex2 = random.nextInt(100) + 1;
        int health1 = random.nextInt(14) + 7, health2 = random.nextInt(14) + 7; // Health between 7-20
        int cool1 = random.nextInt(100) + 1, cool2 = random.nextInt(100) + 1;
        int str1 = random.nextInt(100) + 1, str2 = random.nextInt(100) + 1;
        int ref1 = random.nextInt(100) + 1, ref2 = random.nextInt(100) + 1;
        
        // Calculate averages
        int avgDexterity = (dex1 + dex2) / 2;
        int avgHealth = (health1 + health2) / 2;
        int avgCoolness = (cool1 + cool2) / 2;
        int avgStrength = (str1 + str2) / 2;
        int avgReflexes = (ref1 + ref2) / 2;
        
        // Random handedness
        Handedness randomHandedness = random.nextBoolean() ? 
            Handedness.LEFT_HANDED : Handedness.RIGHT_HANDED;
        
        Character character = new Character(
            0, // ID assigned by registry
            generateName("Confederate"),
            generateFirstName(),
            generateLastName(),
            generateBirthdate(),
            null, // No theme
            avgDexterity,
            avgHealth,
            avgCoolness,
            avgStrength,
            avgReflexes,
            randomHandedness
        );
        
        // Add rifle skill for Brown Bess
        character.addSkill(new Skill(SkillsManager.RIFLE, 6));
        
        return character;
    }
    
    private static Character createUnionSoldier() {
        // Use the same random stat generation as weighted_random
        int dex1 = random.nextInt(100) + 1, dex2 = random.nextInt(100) + 1;
        int health1 = random.nextInt(14) + 7, health2 = random.nextInt(14) + 7; // Health between 7-20
        int cool1 = random.nextInt(100) + 1, cool2 = random.nextInt(100) + 1;
        int str1 = random.nextInt(100) + 1, str2 = random.nextInt(100) + 1;
        int ref1 = random.nextInt(100) + 1, ref2 = random.nextInt(100) + 1;
        
        // Calculate averages
        int avgDexterity = (dex1 + dex2) / 2;
        int avgHealth = (health1 + health2) / 2;
        int avgCoolness = (cool1 + cool2) / 2;
        int avgStrength = (str1 + str2) / 2;
        int avgReflexes = (ref1 + ref2) / 2;
        
        // Random handedness
        Handedness randomHandedness = random.nextBoolean() ? 
            Handedness.LEFT_HANDED : Handedness.RIGHT_HANDED;
        
        Character character = new Character(
            0, // ID assigned by registry
            generateName("Union"),
            generateFirstName(),
            generateLastName(),
            generateBirthdate(),
            null, // No theme
            avgDexterity,
            avgHealth,
            avgCoolness,
            avgStrength,
            avgReflexes,
            randomHandedness
        );
        
        // Add rifle skill for Brown Bess
        character.addSkill(new Skill(SkillsManager.RIFLE, 6));
        
        return character;
    }
    
    // Helper methods for generating character attributes
    private static String generateName(String archetype) {
        // Generate a first name from CSV data, nickname matches firstName
        String firstName = generateCSVBasedFirstName();
        return firstName; // Nickname matches firstName as requested
    }
    
    /**
     * Generate an appropriate nickname based on first name and theme
     */
    private static String generateNicknameFromFirstName(String firstName, String themeId) {
        try {
            InputStream is = CharacterFactory.class.getResourceAsStream("/data/themes/" + themeId + "/names.json");
            if (is == null) {
                return firstName; // Fallback to first name
            }
            
            JsonNode root = objectMapper.readTree(is);
            JsonNode nicknames = root.get("nicknames");
            
            if (nicknames == null || !nicknames.has(firstName)) {
                return firstName; // No nicknames available, use first name
            }
            
            JsonNode nameNicknames = nicknames.get(firstName);
            if (nameNicknames.isArray() && nameNicknames.size() > 0) {
                // 50% chance to use nickname vs first name
                if (random.nextBoolean()) {
                    int index = random.nextInt(nameNicknames.size());
                    return nameNicknames.get(index).asText();
                } else {
                    return firstName;
                }
            }
            
            return firstName;
            
        } catch (Exception e) {
            return firstName; // Fallback to first name
        }
    }
    
    private static String generateFirstName() {
        return generateCSVBasedFirstName();
    }
    
    /**
     * Generate a theme-based first name using frequency-weighted selection
     */
    private static String generateThemeBasedFirstName(String themeId) {
        try {
            InputStream is = CharacterFactory.class.getResourceAsStream("/data/themes/" + themeId + "/names.json");
            if (is == null) {
                return generateFallbackFirstName();
            }
            
            JsonNode root = objectMapper.readTree(is);
            JsonNode maleNames = root.get("maleNames");
            
            if (maleNames == null) {
                return generateFallbackFirstName();
            }
            
            // Create weighted list for frequency-based selection
            List<String> weightedNames = new ArrayList<>();
            maleNames.fields().forEachRemaining(entry -> {
                String name = entry.getKey();
                double frequency = entry.getValue().asDouble();
                int weight = Math.max(1, (int) Math.round(frequency * 10)); // Scale frequency to integer weight
                
                for (int i = 0; i < weight; i++) {
                    weightedNames.add(name);
                }
            });
            
            if (weightedNames.isEmpty()) {
                return generateFallbackFirstName();
            }
            
            return weightedNames.get(random.nextInt(weightedNames.size()));
            
        } catch (Exception e) {
            return generateFallbackFirstName();
        }
    }
    
    private static String generateFallbackFirstName() {
        String[] maleNames = {"John", "William", "James", "Charles", "George", "Frank", "Joseph", "Thomas", "Henry", "Robert"};
        return maleNames[random.nextInt(maleNames.length)];
    }
    
    private static String generateLastName() {
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Wilson", "Moore", "Taylor", "Anderson", "Thomas", "Jackson", "White", "Harris", "Martin", "Thompson", "Garcia", "Martinez", "Robinson"};
        return lastNames[random.nextInt(lastNames.length)];
    }
    
    /**
     * Load male names from 1880USNames.csv with frequency weighting
     */
    private static void loadCSVNames() {
        if (csvNamesLoaded) {
            return; // Already loaded
        }
        
        weightedMaleNames = new ArrayList<>();
        
        // Try multiple possible paths for the CSV file
        String[] possiblePaths = {
            "1880USNames.csv",
            "../1880USNames.csv",
            "/mnt/c/dev/TTCombat/OF2Prototype01/1880USNames.csv"
        };
        
        boolean fileFound = false;
        for (String path : possiblePaths) {
            try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
                System.out.println("Successfully opened CSV file at: " + path);
                
                String line;
                boolean isHeader = true;
                
                while ((line = reader.readLine()) != null) {
                    if (isHeader) {
                        isHeader = false;
                        continue; // Skip header row
                    }
                    
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        String maleName = parts[1].trim();
                        String maleCountStr = parts[2].trim().replaceAll("\"", "").replaceAll(",", "");
                        
                        try {
                            int count = Integer.parseInt(maleCountStr);
                            
                            // Add name to weighted list based on frequency
                            // Scale down the count to avoid massive lists (divide by 1000, minimum 1)
                            int weight = Math.max(1, count / 1000);
                            
                            for (int i = 0; i < weight; i++) {
                                weightedMaleNames.add(maleName);
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Failed to parse count for name " + maleName + ": " + maleCountStr);
                        }
                    }
                }
                
                fileFound = true;
                csvNamesLoaded = true;
                System.out.println("Loaded " + weightedMaleNames.size() + " weighted male names from 1880USNames.csv");
                break;
                
            } catch (IOException e) {
                // Try next path
                System.err.println("Could not open CSV at " + path + ": " + e.getMessage());
            }
        }
        
        if (!fileFound) {
            System.err.println("Failed to load 1880USNames.csv from any location, using fallback names");
            // Fall back to hardcoded names
            weightedMaleNames = new ArrayList<>();
            String[] fallbackNames = {"John", "William", "James", "George", "Charles", "Frank", "Joseph", "Henry", "Robert", "Thomas"};
            for (String name : fallbackNames) {
                for (int i = 0; i < 10; i++) { // Add each name 10 times for weighting
                    weightedMaleNames.add(name);
                }
            }
            csvNamesLoaded = true;
        }
    }
    
    /**
     * Generate a random male name from 1880 US Census data with frequency weighting
     */
    private static String generateCSVBasedFirstName() {
        loadCSVNames(); // Ensure names are loaded
        
        if (weightedMaleNames.isEmpty()) {
            return "John"; // Final fallback
        }
        
        return weightedMaleNames.get(random.nextInt(weightedMaleNames.size()));
    }
    
    private static Date generateBirthdate() {
        return generateBirthdateForTheme(themeManager.getCurrentThemeId());
    }
    
    /**
     * Generate a theme-based birthdate for ages 18-45 based on theme's currentDate
     */
    private static Date generateBirthdateForTheme(String themeId) {
        ThemeData theme = themeManager.getTheme(themeId);
        if (theme == null) {
            return generateFallbackBirthdate();
        }
        
        try {
            // Use theme's currentDate and calculate appropriate birthdate
            Date currentDate = theme.currentDate;
            Calendar current = Calendar.getInstance();
            
            if (currentDate != null) {
                current.setTime(currentDate);
            } else {
                // Fallback based on theme ID
                if (themeId.equals("test_theme")) {
                    current.set(1881, Calendar.JUNE, 9);
                } else if (themeId.equals("civil_war")) {
                    current.set(1861, Calendar.APRIL, 16);
                } else {
                    return generateFallbackBirthdate();
                }
            }
            
            // Generate age between 18-45
            int age = 18 + random.nextInt(28);
            Calendar birthdate = Calendar.getInstance();
            birthdate.setTime(current.getTime());
            birthdate.add(Calendar.YEAR, -age);
            
            return birthdate.getTime();
        } catch (Exception e) {
            return generateFallbackBirthdate();
        }
    }
    
    private static Date generateFallbackBirthdate() {
        Calendar cal = Calendar.getInstance();
        int year = 1850 + random.nextInt(20);
        int month = random.nextInt(12);
        int day = 1 + random.nextInt(28);
        cal.set(year, month, day);
        return cal.getTime();
    }
    
    /**
     * Lists all available character archetypes
     */
    public static String[] getAvailableArchetypes() {
        return new String[]{
            "gunslinger", "soldier", "medic", "scout", "marksman", "brawler", 
            "confederate_soldier", "union_soldier", "balanced", "weighted_random"
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
            case "confederate_soldier":
                return "Civil War Confederate soldier with random stats and rifle training";
            case "union_soldier":
                return "Civil War Union soldier with random stats and rifle training";
            case "balanced":
                return "Well-rounded character good at many things";
            case "weighted_random":
                return "Randomly generated stats (averaged), no predefined skills";
            default:
                return "Unknown archetype";
        }
    }
}
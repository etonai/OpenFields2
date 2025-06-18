package data;

import combat.Character;
import combat.Handedness;
import combat.Skill;
import java.util.Date;
import java.util.Calendar;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.io.IOException;

/**
 * Factory for creating characters with pre-defined archetypes and themes
 */
public class CharacterFactory {
    private static final Random random = new Random();
    
    // Theme-based name data cache
    private static Map<String, Map<String, Object>> themeNameCache = new HashMap<>();
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
        String firstName = generateFirstName();
        String nickname = (random.nextInt(100) < 80) ? firstName : generateCreativeNickname(themeManager.getCurrentThemeId());
        
        Character character = new Character(
            0, // ID assigned by registry
            nickname,
            firstName,
            generateLastName(),
            generateBirthdate(),
            null, // No theme
            85, // High dexterity
            100, 
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
        String firstName = generateFirstName();
        String nickname = (random.nextInt(100) < 80) ? firstName : generateCreativeNickname(themeManager.getCurrentThemeId());
        
        Character character = new Character(
            0,
            nickname,
            firstName,
            generateLastName(),
            generateBirthdate(),
            null,
            75,
            100, // Base health
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
        String firstName = generateFirstName();
        String nickname = (random.nextInt(100) < 80) ? firstName : generateCreativeNickname(themeManager.getCurrentThemeId());
        
        Character character = new Character(
            0,
            nickname,
            firstName,
            generateLastName(),
            generateBirthdate(),
            null,
            80, // Good dexterity for medical work
            100,
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
        String firstName = generateFirstName();
        String nickname = (random.nextInt(100) < 80) ? firstName : generateCreativeNickname(themeManager.getCurrentThemeId());
        
        Character character = new Character(
            0,
            nickname,
            firstName,
            generateLastName(),
            generateBirthdate(),
            null,
            90, // Very high dexterity
            100,
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
        String firstName = generateFirstName();
        String nickname = (random.nextInt(100) < 80) ? firstName : generateCreativeNickname(themeManager.getCurrentThemeId());
        
        Character character = new Character(
            0,
            nickname,
            firstName,
            generateLastName(),
            generateBirthdate(),
            null,
            95, // Excellent dexterity
            100,
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
        String firstName = generateFirstName();
        String nickname = (random.nextInt(100) < 80) ? firstName : generateCreativeNickname(themeManager.getCurrentThemeId());
        
        Character character = new Character(
            0,
            nickname,
            firstName,
            generateLastName(),
            generateBirthdate(),
            null,
            70,
            100, // Base health
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
        // 50/50 gender split for balanced characters
        String gender = random.nextBoolean() ? "male" : "female";
        String themeId = themeManager.getCurrentThemeId();
        String firstName = generateThemeBasedFirstName(gender, themeId);
        String nickname = (random.nextInt(100) < 80) ? firstName : generateCreativeNickname(themeId);
        
        Character character = new Character(
            0,
            nickname,
            firstName,
            generateLastName(),
            generateBirthdate(),
            null,
            75, // Balanced stats
            100,
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
        int health1 = 100, health2 = 100; // Health base 100 consistent with character stats
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
        
        // 50/50 gender split for weighted_random
        String gender = random.nextBoolean() ? "male" : "female";
        String themeId = themeManager.getCurrentThemeId();
        String firstName = generateThemeBasedFirstName(gender, themeId);
        String nickname = (random.nextInt(100) < 80) ? firstName : generateCreativeNickname(themeId);
        
        Character character = new Character(
            0, // ID assigned by registry
            nickname,
            firstName,
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
        int health1 = 100, health2 = 100; // Health base 100 consistent with character stats
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
        
        String firstName = generateFirstName();
        String nickname = (random.nextInt(100) < 80) ? firstName : generateCreativeNickname(themeManager.getCurrentThemeId());
        
        Character character = new Character(
            0, // ID assigned by registry
            nickname,
            firstName,
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
        int health1 = 100, health2 = 100; // Health base 100 consistent with character stats
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
        
        String firstName = generateFirstName();
        String nickname = (random.nextInt(100) < 80) ? firstName : generateCreativeNickname(themeManager.getCurrentThemeId());
        
        Character character = new Character(
            0, // ID assigned by registry
            nickname,
            firstName,
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
        String themeId = themeManager.getCurrentThemeId();
        String firstName = generateThemeBasedFirstName("male", themeId);
        
        // 80% of time use first name, 20% use creative nickname
        if (random.nextInt(100) < 80) {
            return firstName;
        } else {
            return generateCreativeNickname(themeId);
        }
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
        String themeId = themeManager.getCurrentThemeId();
        return generateThemeBasedFirstName("male", themeId);
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
        String themeId = themeManager.getCurrentThemeId();
        return generateThemeBasedLastName(themeId);
    }
    
    /**
     * Load theme names from JSON files
     */
    private static void loadThemeNames(String themeId) {
        if (themeNameCache.containsKey(themeId)) {
            return; // Already loaded
        }
        
        Map<String, Object> themeNames = new HashMap<>();
        
        try {
            // Load male names
            InputStream maleStream = CharacterFactory.class.getResourceAsStream("/data/themes/" + themeId + "/male_names.json");
            if (maleStream != null) {
                JsonNode maleRoot = objectMapper.readTree(maleStream);
                themeNames.put("maleNames", maleRoot.get("names"));
            }
            
            // Load female names
            InputStream femaleStream = CharacterFactory.class.getResourceAsStream("/data/themes/" + themeId + "/female_names.json");
            if (femaleStream != null) {
                JsonNode femaleRoot = objectMapper.readTree(femaleStream);
                themeNames.put("femaleNames", femaleRoot.get("names"));
            }
            
            // Load last names
            InputStream lastNameStream = CharacterFactory.class.getResourceAsStream("/data/themes/" + themeId + "/last_names.json");
            if (lastNameStream != null) {
                JsonNode lastNameRoot = objectMapper.readTree(lastNameStream);
                themeNames.put("lastNames", lastNameRoot.get("names"));
            }
            
            // Load nicknames
            InputStream nicknameStream = CharacterFactory.class.getResourceAsStream("/data/themes/" + themeId + "/nicknames.json");
            if (nicknameStream != null) {
                JsonNode nicknameRoot = objectMapper.readTree(nicknameStream);
                themeNames.put("nicknames", nicknameRoot.get("names"));
            }
            
            themeNameCache.put(themeId, themeNames);
            
        } catch (Exception e) {
            System.err.println("Failed to load theme names for " + themeId + ": " + e.getMessage());
            themeNameCache.put(themeId, new HashMap<>()); // Empty cache to prevent repeated attempts
        }
    }
    
    /**
     * Generate a theme-based first name using frequency-weighted selection
     */
    private static String generateThemeBasedFirstName(String gender, String themeId) {
        loadThemeNames(themeId);
        
        try {
            Map<String, Object> themeNames = themeNameCache.get(themeId);
            if (themeNames == null) {
                return generateFallbackFirstName();
            }
            
            String namesKey = gender.equals("female") ? "femaleNames" : "maleNames";
            JsonNode names = (JsonNode) themeNames.get(namesKey);
            
            if (names == null) {
                return generateFallbackFirstName();
            }
            
            // Create weighted list for frequency-based selection
            List<String> weightedNames = new ArrayList<>();
            names.fields().forEachRemaining(entry -> {
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
    
    /**
     * Generate a theme-based last name using frequency-weighted selection
     */
    private static String generateThemeBasedLastName(String themeId) {
        loadThemeNames(themeId);
        
        try {
            Map<String, Object> themeNames = themeNameCache.get(themeId);
            if (themeNames == null) {
                return generateFallbackLastName();
            }
            
            JsonNode lastNames = (JsonNode) themeNames.get("lastNames");
            if (lastNames == null) {
                return generateFallbackLastName();
            }
            
            // Create weighted list for frequency-based selection
            List<String> weightedLastNames = new ArrayList<>();
            lastNames.fields().forEachRemaining(entry -> {
                String name = entry.getKey();
                double frequency = entry.getValue().asDouble();
                int weight = Math.max(1, (int) Math.round(frequency * 10)); // Scale frequency to integer weight
                
                for (int i = 0; i < weight; i++) {
                    weightedLastNames.add(name);
                }
            });
            
            if (weightedLastNames.isEmpty()) {
                return generateFallbackLastName();
            }
            
            return weightedLastNames.get(random.nextInt(weightedLastNames.size()));
            
        } catch (Exception e) {
            return generateFallbackLastName();
        }
    }
    
    /**
     * Generate a creative nickname from theme-specific nickname list
     */
    private static String generateCreativeNickname(String themeId) {
        loadThemeNames(themeId);
        
        try {
            Map<String, Object> themeNames = themeNameCache.get(themeId);
            if (themeNames == null) {
                return generateFallbackFirstName();
            }
            
            JsonNode nicknames = (JsonNode) themeNames.get("nicknames");
            if (nicknames == null || !nicknames.isArray()) {
                return generateFallbackFirstName();
            }
            
            List<String> nicknameList = new ArrayList<>();
            nicknames.forEach(node -> nicknameList.add(node.asText()));
            
            if (nicknameList.isEmpty()) {
                return generateFallbackFirstName();
            }
            
            return nicknameList.get(random.nextInt(nicknameList.size()));
            
        } catch (Exception e) {
            return generateFallbackFirstName();
        }
    }
    
    private static String generateFallbackLastName() {
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Wilson", "Moore", "Taylor"};
        return lastNames[random.nextInt(lastNames.length)];
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
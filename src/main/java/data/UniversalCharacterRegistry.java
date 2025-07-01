package data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import combat.Character;
import combat.Skill;
import combat.BodyPart;
import combat.WoundSeverity;
import combat.Wound;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UniversalCharacterRegistry {
    private static UniversalCharacterRegistry instance;
    private final ObjectMapper objectMapper;
    private final Map<Integer, Character> characters;
    private final AtomicInteger nextCharacterId;
    private final String registryFilePath;
    
    private UniversalCharacterRegistry() {
        this.objectMapper = new ObjectMapper();
        this.characters = new ConcurrentHashMap<>();
        this.nextCharacterId = new AtomicInteger(1000); // Start from 1000 to avoid conflicts
        this.registryFilePath = "characters.json";
        loadRegistry();
        loadTestFactionCharacters();
    }
    
    public static UniversalCharacterRegistry getInstance() {
        if (instance == null) {
            synchronized (UniversalCharacterRegistry.class) {
                if (instance == null) {
                    instance = new UniversalCharacterRegistry();
                }
            }
        }
        return instance;
    }
    
    /**
     * Registers a character and assigns it a unique ID
     * @param character The character to register (ID will be assigned)
     * @return The assigned character ID
     */
    public int registerCharacter(Character character) {
        int id = nextCharacterId.getAndIncrement();
        character.id = id; // Set the ID on the character
        characters.put(id, character);
        saveRegistry();
        System.out.println("*** Registered character: " + character.getDisplayName() + " with ID: " + id);
        return id;
    }
    
    /**
     * Gets a character by ID (returns a copy to prevent modification of registry)
     * @param id The character ID
     * @return A copy of the character, or null if not found
     */
    public Character getCharacter(int id) {
        Character original = characters.get(id);
        if (original == null) {
            return null;
        }
        // Return a copy with no weapon/weapon state - those come from save game
        return createCharacterCopy(original);
    }
    
    /**
     * Gets all character IDs
     * @return Set of all character IDs
     */
    public Set<Integer> getAllCharacterIds() {
        return characters.keySet();
    }
    
    /**
     * Gets all characters (as copies)
     * @return Map of all characters by ID
     */
    public Map<Integer, Character> getAllCharacters() {
        Map<Integer, Character> result = new HashMap<>();
        for (Map.Entry<Integer, Character> entry : characters.entrySet()) {
            result.put(entry.getKey(), createCharacterCopy(entry.getValue()));
        }
        return result;
    }
    
    /**
     * Checks if a character exists
     * @param id The character ID
     * @return true if character exists
     */
    public boolean hasCharacter(int id) {
        return characters.containsKey(id);
    }
    
    /**
     * Gets the next character ID that would be assigned
     * @return The next character ID
     */
    public int getNextCharacterId() {
        return nextCharacterId.get();
    }
    
    /**
     * Creates a copy of a character with only universal attributes (no weapons)
     */
    private Character createCharacterCopy(Character original) {
        Character copy = new Character(
            original.id,
            original.nickname,
            original.firstName,
            original.lastName,
            original.birthdate,
            null, // No theme ID in universal registry
            original.dexterity,
            original.health,
            original.coolness,
            original.strength,
            original.reflexes,
            original.handedness
        );
        
        // Copy skills (universal)
        copy.skills.clear();
        for (combat.Skill skill : original.skills) {
            copy.addSkill(new combat.Skill(skill.getSkillName(), skill.getLevel()));
        }
        
        // Copy wounds (universal)
        copy.wounds.clear();
        for (combat.Wound wound : original.wounds) {
            copy.addWound(new combat.Wound(wound.getBodyPart(), wound.getSeverity()));
        }
        
        // Intentionally NOT copying:
        // - weapon (theme-specific)
        // - currentWeaponState (scenario-specific)
        // - themeId (scenario-specific)
        // - queuedShots (scenario-specific)
        // - currentMovementType (scenario-specific)
        // - currentAimingSpeed (scenario-specific)
        
        return copy;
    }
    
    /**
     * Loads the character registry from file
     */
    private void loadRegistry() {
        try {
            // Try to load from resources first (for embedded data)
            InputStream is = getClass().getResourceAsStream("/data/characters.json");
            if (is != null) {
                loadFromInputStream(is);
                System.out.println("*** Loaded character registry from resources");
                return;
            }
            
            // Try to load from file system (for user data)
            File file = new File(registryFilePath);
            if (file.exists()) {
                CharacterRegistryData data = objectMapper.readValue(file, CharacterRegistryData.class);
                loadFromRegistryData(data);
                System.out.println("*** Loaded character registry from file: " + characters.size() + " characters");
            } else {
                System.out.println("*** No character registry found, starting with empty registry");
            }
        } catch (Exception e) {
            System.err.println("Error loading character registry: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadFromInputStream(InputStream is) throws IOException {
        JsonNode rootNode = objectMapper.readTree(is);
        CharacterRegistryData data = objectMapper.treeToValue(rootNode, CharacterRegistryData.class);
        loadFromRegistryData(data);
    }
    
    private void loadFromRegistryData(CharacterRegistryData data) {
        if (data.nextCharacterId > 0) {
            nextCharacterId.set(data.nextCharacterId);
        }
        
        if (data.characters != null) {
            for (Map.Entry<String, UniversalCharacterData> entry : data.characters.entrySet()) {
                try {
                    int id = Integer.parseInt(entry.getKey());
                    UniversalCharacterData charData = entry.getValue();
                    Character character = deserializeCharacter(charData);
                    character.id = id; // Ensure ID is set correctly
                    characters.put(id, character);
                } catch (Exception e) {
                    System.err.println("Error loading character " + entry.getKey() + ": " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Saves the character registry to file
     */
    public void saveRegistry() {
        try {
            CharacterRegistryData data = new CharacterRegistryData();
            data.nextCharacterId = nextCharacterId.get();
            data.characters = new HashMap<>();
            
            for (Map.Entry<Integer, Character> entry : characters.entrySet()) {
                data.characters.put(entry.getKey().toString(), serializeCharacter(entry.getValue()));
            }
            
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(registryFilePath), data);
        } catch (Exception e) {
            System.err.println("Error saving character registry: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private UniversalCharacterData serializeCharacter(Character character) {
        UniversalCharacterData data = new UniversalCharacterData();
        data.id = character.id;
        data.nickname = character.nickname;
        data.firstName = character.firstName;
        data.lastName = character.lastName;
        data.birthdate = character.birthdate;
        data.dexterity = character.dexterity;
        data.health = character.health;
        data.coolness = character.coolness;
        data.strength = character.strength;
        data.reflexes = character.reflexes;
        data.handedness = character.handedness;
        data.baseMovementSpeed = character.baseMovementSpeed;
        
        // Serialize skills (universal)
        if (character.skills != null) {
            for (combat.Skill skill : character.skills) {
                if (data.skills == null) {
                    data.skills = new java.util.ArrayList<>();
                }
                data.skills.add(new UniversalCharacterData.SkillData(skill.getSkillName(), skill.getLevel()));
            }
        }
        
        // Serialize wounds (universal)
        if (character.wounds != null) {
            for (combat.Wound wound : character.wounds) {
                if (data.wounds == null) {
                    data.wounds = new java.util.ArrayList<>();
                }
                data.wounds.add(new UniversalCharacterData.WoundData(wound.getBodyPart().name(), wound.getSeverity().name()));
            }
        }
        
        // NOT serializing: weapon, currentWeaponState, themeId, queuedShots, currentMovementType, currentAimingSpeed
        
        return data;
    }
    
    private Character deserializeCharacter(UniversalCharacterData data) {
        Character character = new Character(
            data.id, 
            data.nickname != null ? data.nickname : "",
            data.firstName != null ? data.firstName : "",
            data.lastName != null ? data.lastName : "",
            data.birthdate != null ? data.birthdate : new java.util.Date(),
            null, // No theme ID in universal registry
            data.dexterity, 
            data.health,
            data.coolness, 
            data.strength, 
            data.reflexes, 
            data.handedness
        );
        
        character.baseMovementSpeed = data.baseMovementSpeed;
        
        // Restore skills
        character.skills.clear();
        if (data.skills != null) {
            for (UniversalCharacterData.SkillData skillData : data.skills) {
                character.addSkill(new combat.Skill(skillData.skillName, skillData.level));
            }
        }
        
        // Restore wounds
        character.wounds.clear();
        if (data.wounds != null) {
            for (UniversalCharacterData.WoundData woundData : data.wounds) {
                try {
                    combat.BodyPart bodyPart = combat.BodyPart.valueOf(woundData.bodyPart);
                    combat.WoundSeverity severity = combat.WoundSeverity.valueOf(woundData.severity);
                    character.addWound(new combat.Wound(bodyPart, severity));
                } catch (IllegalArgumentException e) {
                    System.err.println("Warning: Invalid wound data: " + woundData.bodyPart + "/" + woundData.severity);
                }
            }
        }
        
        return character;
    }
    
    /**
     * Convert CharacterData to Character object.
     * Similar to SaveGameController.deserializeCharacter but for faction character loading.
     * 
     * @param data CharacterData from faction file
     * @return Character object
     */
    private Character convertCharacterDataToCharacter(CharacterData data) {
        // Handle null values
        String nickname = data.nickname != null ? data.nickname : "";
        String firstName = data.firstName != null ? data.firstName : "";
        String lastName = data.lastName != null ? data.lastName : "";
        Date birthdate = data.birthdate != null ? data.birthdate : new Date(0);
        
        Character character = new Character(
            data.id, nickname, firstName, lastName, birthdate, data.themeId, data.dexterity, data.health,
            data.coolness, data.strength, data.reflexes, data.handedness
        );
        
        character.currentDexterity = data.currentDexterity;
        character.currentHealth = data.currentHealth;
        character.baseMovementSpeed = data.baseMovementSpeed;
        character.currentMovementType = data.currentMovementType;
        character.currentAimingSpeed = data.currentAimingSpeed;
        
        // Restore faction
        character.faction = data.faction;
        
        // Restore skills
        character.skills.clear();
        if (data.skills != null) {
            for (CharacterData.SkillData skillData : data.skills) {
                character.addSkill(new Skill(skillData.skillName, skillData.level));
            }
        }
        
        // Restore wounds
        character.wounds.clear();
        if (data.wounds != null) {
            for (CharacterData.WoundData woundData : data.wounds) {
                try {
                    BodyPart bodyPart = BodyPart.valueOf(woundData.bodyPart);
                    WoundSeverity severity = WoundSeverity.valueOf(woundData.severity);
                    int damage = (woundData.damage > 0) ? woundData.damage : 1;
                    character.addWound(new Wound(bodyPart, severity, "Saved wound", "", damage));
                } catch (IllegalArgumentException e) {
                    System.err.println("Warning: Invalid wound data: " + woundData.bodyPart + "/" + woundData.severity);
                }
            }
        }
        
        return character;
    }
    
    /**
     * Load test faction characters into the registry for automated testing.
     * This loads characters from TestFactionAlpha.json and TestFactionBeta.json
     * when they exist, allowing test saves to reference these characters.
     */
    private void loadTestFactionCharacters() {
        try {
            // Load TestFactionAlpha characters
            loadTestFactionFile("factions/TestFactionAlpha.json");
            // Load TestFactionBeta characters  
            loadTestFactionFile("factions/TestFactionBeta.json");
        } catch (Exception e) {
            // Silently fail - test factions are optional
            System.out.println("*** Test factions not found, skipping test character loading");
        }
    }
    
    /**
     * Load characters from a specific test faction file.
     * 
     * @param filePath Path to the faction JSON file
     */
    private void loadTestFactionFile(String filePath) {
        try {
            File factionFile = new File(filePath);
            if (!factionFile.exists()) {
                return; // Test faction file doesn't exist, skip silently
            }
            
            JsonNode rootNode = objectMapper.readTree(factionFile);
            JsonNode charactersNode = rootNode.get("characters");
            
            if (charactersNode != null && charactersNode.isArray()) {
                for (JsonNode characterNode : charactersNode) {
                    CharacterData charData = objectMapper.treeToValue(characterNode, CharacterData.class);
                    Character character = convertCharacterDataToCharacter(charData);
                    characters.put(character.id, character);
                    System.out.println("*** Loaded test character: " + character.getDisplayName() + " (ID: " + character.id + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load test faction file " + filePath + ": " + e.getMessage());
        }
    }
    
    /**
     * Data class for JSON serialization of the registry
     */
    public static class CharacterRegistryData {
        public int nextCharacterId;
        public Map<String, UniversalCharacterData> characters;
        
        public CharacterRegistryData() {
            this.characters = new HashMap<>();
        }
    }
}
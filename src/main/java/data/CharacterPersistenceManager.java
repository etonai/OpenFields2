package data;

import combat.Character;
import java.util.List;
import java.util.ArrayList;

public class CharacterPersistenceManager {
    private static CharacterPersistenceManager instance;
    private FactionRegistry factionRegistry;
    
    private CharacterPersistenceManager() {
        this.factionRegistry = FactionRegistry.getInstance();
    }
    
    public static CharacterPersistenceManager getInstance() {
        if (instance == null) {
            instance = new CharacterPersistenceManager();
        }
        return instance;
    }
    
    /**
     * Save a character to its faction file
     */
    public void saveCharacter(Character character) {
        int factionId = character.getFaction();
        CharacterData characterData = convertToCharacterData(character);
        
        // Get the faction file data
        FactionRegistry.FactionFileData factionFile = loadFactionFileData(factionId);
        if (factionFile == null) {
            System.err.println("Error: Cannot find faction file for faction " + factionId);
            return;
        }
        
        // Update or add character in the faction file
        boolean found = false;
        for (int i = 0; i < factionFile.characters.size(); i++) {
            CharacterData existing = factionFile.characters.get(i);
            if (existing.id == character.id) {
                factionFile.characters.set(i, characterData);
                found = true;
                break;
            }
        }
        
        if (!found) {
            factionFile.characters.add(characterData);
        }
        
        // Save the faction file
        saveFactionFileData(factionId, factionFile);
    }
    
    /**
     * Load all characters from a faction file
     */
    public List<Character> loadCharactersFromFaction(int factionId) {
        List<Character> characters = new ArrayList<>();
        FactionRegistry.FactionFileData factionFile = loadFactionFileData(factionId);
        
        if (factionFile != null) {
            for (CharacterData characterData : factionFile.characters) {
                Character character = convertFromCharacterData(characterData);
                characters.add(character);
            }
        }
        
        return characters;
    }
    
    /**
     * Load a specific character by ID from its faction file
     */
    public Character loadCharacter(int characterId, int factionId) {
        List<Character> characters = loadCharactersFromFaction(factionId);
        for (Character character : characters) {
            if (character.id == characterId) {
                return character;
            }
        }
        return null;
    }
    
    /**
     * Get non-incapacitated characters from a faction
     */
    public List<Character> getNonIncapacitatedCharacters(int factionId) {
        List<Character> allCharacters = loadCharactersFromFaction(factionId);
        List<Character> available = new ArrayList<>();
        
        for (Character character : allCharacters) {
            if (!character.isIncapacitated()) {
                available.add(character);
            }
        }
        
        return available;
    }
    
    /**
     * Remove a character from its faction file
     */
    public void removeCharacter(int characterId, int factionId) {
        FactionRegistry.FactionFileData factionFile = loadFactionFileData(factionId);
        if (factionFile == null) {
            return;
        }
        
        factionFile.characters.removeIf(characterData -> characterData.id == characterId);
        
        saveFactionFileData(factionId, factionFile);
    }
    
    /**
     * Update battle statistics for all characters in a faction
     */
    public void updateFactionBattleStatistics(int factionId, boolean victory) {
        List<Character> characters = loadCharactersFromFaction(factionId);
        
        for (Character character : characters) {
            character.battlesParticipated++;
            if (victory) {
                character.victories++;
            } else {
                character.defeats++;
            }
            saveCharacter(character);
        }
    }
    
    // Helper methods
    private FactionRegistry.FactionFileData loadFactionFileData(int factionId) {
        try {
            return factionRegistry.loadFactionFileData(factionId);
        } catch (Exception e) {
            System.err.println("Error loading faction file data for faction " + factionId + ": " + e.getMessage());
            return null;
        }
    }
    
    private void saveFactionFileData(int factionId, FactionRegistry.FactionFileData factionFile) {
        try {
            factionRegistry.saveFactionFileData(factionId, factionFile);
        } catch (Exception e) {
            System.err.println("Error saving faction file data for faction " + factionId + ": " + e.getMessage());
        }
    }
    
    private CharacterData convertToCharacterData(Character character) {
        CharacterData data = new CharacterData();
        
        // Basic info
        data.id = character.id;
        data.nickname = character.nickname;
        data.firstName = character.firstName;
        data.lastName = character.lastName;
        data.birthdate = character.birthdate;
        data.themeId = character.themeId;
        data.faction = character.getFaction();
        
        // Stats
        data.dexterity = character.dexterity;
        data.currentDexterity = character.currentDexterity;
        data.health = character.health;
        data.currentHealth = character.currentHealth;
        data.coolness = character.coolness;
        data.strength = character.strength;
        data.reflexes = character.reflexes;
        data.handedness = character.handedness;
        data.baseMovementSpeed = character.baseMovementSpeed;
        
        // Current state (persisted except position)
        data.currentMovementType = character.currentMovementType;
        data.currentAimingSpeed = character.currentAimingSpeed;
        data.usesAutomaticTargeting = character.usesAutomaticTargeting;
        data.preferredFiringMode = character.preferredFiringMode;
        
        // Battle statistics
        data.combatEngagements = character.combatEngagements;
        data.woundsReceived = character.woundsReceived;
        data.woundsInflictedScratch = character.woundsInflictedScratch;
        data.woundsInflictedLight = character.woundsInflictedLight;
        data.woundsInflictedSerious = character.woundsInflictedSerious;
        data.woundsInflictedCritical = character.woundsInflictedCritical;
        data.attacksAttempted = character.attacksAttempted;
        data.attacksSuccessful = character.attacksSuccessful;
        data.targetsIncapacitated = character.targetsIncapacitated;
        data.headshotsAttempted = character.headshotsAttempted;
        data.headshotsSuccessful = character.headshotsSuccessful;
        data.headshotsKills = character.headshotsKills;
        data.battlesParticipated = character.battlesParticipated;
        data.victories = character.victories;
        data.defeats = character.defeats;
        
        // Defensive statistics (DevCycle 23)
        data.defensiveAttempts = character.defensiveAttempts;
        data.defensiveSuccesses = character.defensiveSuccesses;
        data.counterAttacksExecuted = character.counterAttacksExecuted;
        data.counterAttacksSuccessful = character.counterAttacksSuccessful;
        
        // Skills
        if (character.skills != null) {
            for (combat.Skill skill : character.skills) {
                data.skills.add(new CharacterData.SkillData(skill.getSkillName(), skill.getLevel()));
            }
        }
        
        // Wounds
        if (character.wounds != null) {
            for (combat.Wound wound : character.wounds) {
                data.wounds.add(new CharacterData.WoundData(
                    wound.getBodyPart().name(),
                    wound.getSeverity().name(),
                    wound.getDamage()
                ));
            }
        }
        
        // Defense state (DevCycle 23)
        data.currentDefenseState = character.getDefenseState().name();
        // Note: We don't save defense cooldown times or counter-attack windows
        // as these are transient combat states that shouldn't persist
        
        return data;
    }
    
    private Character convertFromCharacterData(CharacterData data) {
        // Create character with basic info - use the full constructor
        Character character = new Character(data.id, data.nickname, data.firstName, data.lastName, 
                                          data.birthdate, data.themeId, data.dexterity, data.health, 
                                          data.coolness, data.strength, data.reflexes, data.handedness);
        
        // Set remaining stats not covered by constructor
        character.currentDexterity = data.currentDexterity;
        character.currentHealth = data.currentHealth;
        character.baseMovementSpeed = data.baseMovementSpeed;
        
        // Set current state
        character.currentMovementType = data.currentMovementType;
        character.currentAimingSpeed = data.currentAimingSpeed;
        character.usesAutomaticTargeting = data.usesAutomaticTargeting;
        character.preferredFiringMode = data.preferredFiringMode;
        
        // Set faction
        character.setFaction(data.faction);
        
        // Set battle statistics
        character.combatEngagements = data.combatEngagements;
        character.woundsReceived = data.woundsReceived;
        character.woundsInflictedScratch = data.woundsInflictedScratch;
        character.woundsInflictedLight = data.woundsInflictedLight;
        character.woundsInflictedSerious = data.woundsInflictedSerious;
        character.woundsInflictedCritical = data.woundsInflictedCritical;
        character.attacksAttempted = data.attacksAttempted;
        character.attacksSuccessful = data.attacksSuccessful;
        character.targetsIncapacitated = data.targetsIncapacitated;
        character.headshotsAttempted = data.headshotsAttempted;
        character.headshotsSuccessful = data.headshotsSuccessful;
        character.headshotsKills = data.headshotsKills;
        character.battlesParticipated = data.battlesParticipated;
        character.victories = data.victories;
        character.defeats = data.defeats;
        
        // Set defensive statistics (DevCycle 23)
        character.defensiveAttempts = data.defensiveAttempts;
        character.defensiveSuccesses = data.defensiveSuccesses;
        character.counterAttacksExecuted = data.counterAttacksExecuted;
        character.counterAttacksSuccessful = data.counterAttacksSuccessful;
        
        // Set skills
        if (data.skills != null) {
            for (CharacterData.SkillData skillData : data.skills) {
                character.setSkillLevel(skillData.skillName, skillData.level);
            }
        }
        
        // Set wounds
        if (data.wounds != null) {
            for (CharacterData.WoundData woundData : data.wounds) {
                try {
                    combat.BodyPart bodyPart = combat.BodyPart.valueOf(woundData.bodyPart);
                    combat.WoundSeverity severity = combat.WoundSeverity.valueOf(woundData.severity);
                    character.addWound(new combat.Wound(bodyPart, severity, "Persistent wound", "", woundData.damage));
                } catch (IllegalArgumentException e) {
                    System.err.println("Warning: Invalid wound data - " + e.getMessage());
                }
            }
        }
        
        // Set defense state (DevCycle 23)
        if (data.currentDefenseState != null) {
            try {
                combat.DefenseState defenseState = combat.DefenseState.valueOf(data.currentDefenseState);
                character.setDefenseState(defenseState);
            } catch (IllegalArgumentException e) {
                // Default to READY if invalid state
                character.setDefenseState(combat.DefenseState.READY);
            }
        }
        
        return character;
    }
}
/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import combat.*;
import game.*;
import data.*;

/**
 * SaveGameController handles all save/load operations for the OpenFields2 game.
 * This class is responsible for:
 * - Managing save slot prompts and user interaction
 * - Capturing current game state for saving
 * - Serializing characters and units to save format
 * - Loading game data and restoring game state
 * - Deserializing saved data back to game objects
 * 
 * The controller works with the SaveGameManager for file I/O operations
 * and integrates with the existing InputManager system for user prompts.
 */
public class SaveGameController {
    
    // Core dependencies
    private final SaveGameManager saveGameManager;
    private final UniversalCharacterRegistry characterRegistry;
    private final InputManager inputManager;
    
    // Game state references
    private final List<Unit> units;
    private final SelectionManager selectionManager;
    private final GameRenderer gameRenderer;
    private final GameClock gameClock;
    private final java.util.PriorityQueue<ScheduledEvent> eventQueue;
    
    // Game state accessors
    private final GameStateAccessor gameStateAccessor;
    
    /**
     * Interface for accessing game state that SaveGameController needs but doesn't directly manage
     */
    public interface GameStateAccessor {
        boolean isPaused();
        void setPaused(boolean paused);
        int getNextUnitId();
        void setNextUnitId(int nextUnitId);
    }
    
    /**
     * Constructor for SaveGameController
     * 
     * @param units List of game units
     * @param selectionManager Unit selection manager
     * @param gameRenderer Game renderer for camera state
     * @param gameClock Game clock for timing
     * @param eventQueue Event queue for scheduled actions
     * @param inputManager Input manager for prompt coordination
     * @param gameStateAccessor Interface to access additional game state
     */
    public SaveGameController(List<Unit> units, SelectionManager selectionManager,
                             GameRenderer gameRenderer, GameClock gameClock,
                             java.util.PriorityQueue<ScheduledEvent> eventQueue,
                             InputManager inputManager, GameStateAccessor gameStateAccessor) {
        this.units = units;
        this.selectionManager = selectionManager;
        this.gameRenderer = gameRenderer;
        this.gameClock = gameClock;
        this.eventQueue = eventQueue;
        this.inputManager = inputManager;
        this.gameStateAccessor = gameStateAccessor;
        
        // Get manager instances
        this.saveGameManager = SaveGameManager.getInstance();
        this.characterRegistry = UniversalCharacterRegistry.getInstance();
    }
    
    /**
     * Prompt user for save slot selection
     */
    public void promptForSaveSlot() {
        inputManager.setWaitingForSaveSlot(true);
        System.out.println("*** SAVE GAME ***");
        List<SaveGameManager.SaveSlotInfo> availableSlots = saveGameManager.listAvailableSlots();
        
        if (!availableSlots.isEmpty()) {
            System.out.println("Existing saves:");
            for (SaveGameManager.SaveSlotInfo slot : availableSlots) {
                System.out.println(slot.slot + ". slot_" + slot.slot + ".json (" + 
                                 slot.getFormattedTimestamp() + ") - " + 
                                 slot.themeId + ", tick " + slot.currentTick);
            }
        } else {
            System.out.println("No existing saves found.");
        }
        
        System.out.println("Enter save slot (1-9): ");
    }
    
    /**
     * Prompt user for load slot selection
     */
    public void promptForLoadSlot() {
        inputManager.setWaitingForLoadSlot(true);
        System.out.println("*** LOAD GAME ***");
        List<SaveGameManager.SaveSlotInfo> availableSlots = saveGameManager.listAvailableSlots();
        
        if (availableSlots.isEmpty()) {
            System.out.println("No save files found.");
            return;
        }
        
        System.out.println("Available saves:");
        for (SaveGameManager.SaveSlotInfo slot : availableSlots) {
            System.out.println(slot.slot + ". slot_" + slot.slot + ".json (" + 
                             slot.getFormattedTimestamp() + ") - " + 
                             slot.themeId + ", tick " + slot.currentTick);
        }
        System.out.println("Enter slot number (1-9) or 0 to cancel: ");
    }
    
    /**
     * Save game to the specified slot
     * 
     * @param slot Slot number (1-9)
     */
    public void saveGameToSlot(int slot) {
        try {
            // Pause game during save
            boolean wasPaused = gameStateAccessor.isPaused();
            gameStateAccessor.setPaused(true);
            
            SaveData saveData = captureSaveData(slot);
            boolean success = saveGameManager.saveToSlot(slot, saveData);
            
            if (success) {
                System.out.println("*** Game saved successfully to slot " + slot + " ***");
            } else {
                System.out.println("*** Failed to save game to slot " + slot + " ***");
            }
            
            // Restore pause state
            gameStateAccessor.setPaused(wasPaused);
            
        } catch (Exception e) {
            System.err.println("Error during save: " + e.getMessage());
            e.printStackTrace();
        } finally {
            inputManager.setWaitingForSaveSlot(false);
        }
    }
    
    /**
     * Load game from the specified slot
     * 
     * @param slot Slot number (1-9)
     */
    public void loadGameFromSlot(int slot) {
        try {
            SaveData saveData = saveGameManager.loadFromSlot(slot);
            if (saveData != null) {
                applySaveData(saveData);
                System.out.println("*** Game loaded successfully from slot " + slot + " ***");
                System.out.println("*** Loaded at tick " + gameClock.getCurrentTick() + " ***");
            } else {
                System.out.println("*** Failed to load game from slot " + slot + " ***");
            }
        } catch (Exception e) {
            System.err.println("Error during load: " + e.getMessage());
            e.printStackTrace();
        } finally {
            inputManager.setWaitingForLoadSlot(false);
        }
    }
    
    /**
     * Capture current game state into SaveData object
     * 
     * @param slot Save slot number
     * @return SaveData object containing current game state
     */
    private SaveData captureSaveData(int slot) {
        // Create metadata
        String currentThemeId = ThemeManager.getInstance().getCurrentThemeId();
        SaveMetadata metadata = new SaveMetadata("", "1.0", currentThemeId, slot);
        
        // Create game state
        GameStateData gameState = new GameStateData(
            gameClock.getCurrentTick(),
            gameStateAccessor.isPaused(),
            gameRenderer.getOffsetX(),
            gameRenderer.getOffsetY(),
            gameRenderer.getZoom(),
            0, // nextCharacterId is managed by universal registry
            gameStateAccessor.getNextUnitId()
        );
        
        // Serialize units with character ID references and scenario-specific data
        List<UnitData> unitDataList = new ArrayList<>();
        for (Unit unit : units) {
            UnitData unitData = serializeUnitWithCharacterRef(unit, currentThemeId);
            unitDataList.add(unitData);
            
            // Debug output showing what weapons are being saved
            String weaponName = (unit.character.weapon != null) ? unit.character.weapon.name : "None";
            String weaponId = unitData.weaponId != null ? unitData.weaponId : "None";
            System.out.println("  Saving " + unit.character.getDisplayName() + ": weapon=" + weaponName + " (ID: " + weaponId + ")");
        }
        
        return new SaveData(metadata, gameState, unitDataList);
    }
    
    /**
     * Serialize a character to CharacterData format
     * 
     * @param character Character to serialize
     * @return CharacterData object
     */
    private CharacterData serializeCharacter(combat.Character character) {
        CharacterData data = new CharacterData();
        data.id = character.id;
        data.name = character.nickname; // Legacy field for backward compatibility
        data.nickname = character.nickname;
        data.firstName = character.firstName;
        data.lastName = character.lastName;
        data.birthdate = character.birthdate;
        data.themeId = character.themeId;
        data.dexterity = character.dexterity;
        data.currentDexterity = character.currentDexterity;
        data.health = character.health;
        data.currentHealth = character.currentHealth;
        data.coolness = character.coolness;
        data.strength = character.strength;
        data.reflexes = character.reflexes;
        data.handedness = character.handedness;
        data.baseMovementSpeed = character.baseMovementSpeed;
        data.currentMovementType = character.currentMovementType;
        data.currentAimingSpeed = character.currentAimingSpeed;
        
        // Serialize weapon
        if (character.weapon != null) {
            // Find weapon ID from weapon name
            data.weaponId = findWeaponId(character.weapon);
        }
        
        // Serialize weapon state
        if (character.currentWeaponState != null) {
            data.currentWeaponState = character.currentWeaponState.getState();
        }
        
        // Serialize skills
        data.skills = new ArrayList<>();
        for (Skill skill : character.skills) {
            data.skills.add(new CharacterData.SkillData(skill.getSkillName(), skill.getLevel()));
        }
        
        // Serialize wounds
        data.wounds = new ArrayList<>();
        for (Wound wound : character.wounds) {
            data.wounds.add(new CharacterData.WoundData(wound.getBodyPart().name(), wound.getSeverity().name()));
        }
        
        // Serialize character preferences
        data.usesAutomaticTargeting = character.usesAutomaticTargeting;
        data.preferredFiringMode = character.preferredFiringMode;
        
        return data;
    }
    
    /**
     * Find weapon ID from weapon object
     * 
     * @param weapon Weapon object
     * @return Weapon ID string
     */
    public static String findWeaponId(Weapon weapon) {
        // This is a simple approach - in a more complex system, 
        // we might want to store the weapon ID in the weapon object
        if (weapon.name.equals("Colt Peacemaker")) return "wpn_colt_peacemaker";
        if (weapon.name.equals("Hunting Rifle")) return "wpn_hunting_rifle";
        if (weapon.name.equals("Derringer")) return "wpn_derringer";
        if (weapon.name.equals("Plasma Pistol")) return "wpn_plasma_pistol";
        if (weapon.name.equals("Wand of Magic Bolts")) return "wpn_wand_of_magic_bolts";
        if (weapon.name.equals("Enchanted Sword")) return "wpn_magic_sword";
        if (weapon.name.equals("Brown Bess Musket")) return "wpn_brown_bess";
        if (weapon.name.equals("Lee-Enfield SMLE")) return "wpn_lee_enfield";
        if (weapon.name.equals("M1 Garand")) return "wpn_m1_garand";
        if (weapon.name.equals("English Longbow")) return "wpn_longbow";
        if (weapon.name.equals("Heavy Crossbow")) return "wpn_crossbow";
        if (weapon.name.equals("Steel Dagger")) return "wpn_dagger";
        if (weapon.name.equals("Longsword")) return "wpn_sword";
        if (weapon.name.equals("Battle Axe")) return "wpn_battleaxe";
        if (weapon.name.equals("Uzi Submachine Gun")) return "wpn_uzi";
        return "wpn_colt_peacemaker"; // default fallback
    }
    
    /**
     * Serialize unit with character reference for new save format
     * 
     * @param unit Unit to serialize
     * @param themeId Current theme ID
     * @return UnitData object
     */
    private UnitData serializeUnitWithCharacterRef(Unit unit, String themeId) {
        // Find weapon ID from current weapon
        String weaponId = null;
        String currentWeaponState = null;
        FiringMode currentFiringMode = null;
        if (unit.character.weapon != null) {
            weaponId = findWeaponId(unit.character.weapon);
            if (unit.character.currentWeaponState != null) {
                currentWeaponState = unit.character.currentWeaponState.getState();
            }
            currentFiringMode = unit.character.weapon.currentFiringMode;
        }
        
        // Find current target ID if targeting someone
        Integer currentTargetId = null;
        if (unit.character.currentTarget != null) {
            currentTargetId = unit.character.currentTarget.getId();
        }
        
        return new UnitData(
            unit.id,
            unit.character.id,
            unit.x,
            unit.y,
            unit.targetX,
            unit.targetY,
            unit.hasTarget,
            unit.isStopped,
            colorToString(unit.color),
            colorToString(unit.baseColor),
            unit.isHitHighlighted,
            unit.isFiringHighlighted,
            weaponId,
            currentWeaponState,
            themeId,
            currentTargetId,
            currentFiringMode,
            unit.character.usesAutomaticTargeting
        );
    }
    
    /**
     * Serialize unit for legacy save format
     * 
     * @param unit Unit to serialize
     * @return UnitData object
     */
    private UnitData serializeUnit(Unit unit) {
        return new UnitData(
            unit.id,
            unit.character.id,
            unit.x,
            unit.y,
            unit.targetX,
            unit.targetY,
            unit.hasTarget,
            unit.isStopped,
            colorToString(unit.color),
            colorToString(unit.baseColor),
            unit.isHitHighlighted
        );
    }
    
    /**
     * Convert JavaFX Color to string representation
     * 
     * @param color JavaFX Color object
     * @return String representation of color
     */
    private String colorToString(Color color) {
        if (color.equals(Color.RED)) return "RED";
        if (color.equals(Color.BLUE)) return "BLUE";
        if (color.equals(Color.GREEN)) return "GREEN";
        if (color.equals(Color.PURPLE)) return "PURPLE";
        if (color.equals(Color.ORANGE)) return "ORANGE";
        if (color.equals(Color.YELLOW)) return "YELLOW";
        if (color.equals(Color.DARKGRAY)) return "DARKGRAY";
        if (color.equals(Color.GRAY)) return "GRAY";
        if (color.equals(Color.CYAN)) return "CYAN";
        return "RED"; // default fallback
    }
    
    /**
     * Apply loaded save data to restore game state
     * 
     * @param saveData SaveData object containing game state to restore
     */
    private void applySaveData(SaveData saveData) {
        // Clear current game state
        units.clear();
        eventQueue.clear();
        selectionManager.reset();
        
        // Restore game state
        gameClock.reset();
        for (long i = 0; i < saveData.gameState.currentTick; i++) {
            gameClock.advanceTick();
        }
        
        gameStateAccessor.setPaused(saveData.gameState.paused);
        gameRenderer.setOffset(saveData.gameState.offsetX, saveData.gameState.offsetY);
        gameRenderer.setZoom(saveData.gameState.zoom);
        gameStateAccessor.setNextUnitId(saveData.gameState.nextUnitId);
        
        // Handle both new and legacy save formats
        if (saveData.characters != null && !saveData.characters.isEmpty()) {
            // Legacy format - deserialize characters from save data
            for (int i = 0; i < saveData.characters.size() && i < saveData.units.size(); i++) {
                CharacterData charData = saveData.characters.get(i);
                UnitData unitData = saveData.units.get(i);
                
                combat.Character character = deserializeCharacter(charData);
                Unit unit = deserializeUnit(unitData, character);
                units.add(unit);
            }
        } else {
            // New format - load characters from universal registry and apply unit data
            for (UnitData unitData : saveData.units) {
                combat.Character character = characterRegistry.getCharacter(unitData.characterId);
                if (character != null) {
                    // Apply scenario-specific weapon and state
                    if (unitData.weaponId != null && !unitData.weaponId.isEmpty()) {
                        character.weapon = WeaponFactory.createWeapon(unitData.weaponId);
                        if (character.weapon != null && unitData.currentWeaponState != null) {
                            character.currentWeaponState = character.weapon.getStateByName(unitData.currentWeaponState);
                            if (character.currentWeaponState == null) {
                                character.currentWeaponState = character.weapon.getInitialState();
                            }
                        }
                        
                        // Debug output showing what weapons are being loaded
                        String weaponName = (character.weapon != null) ? character.weapon.name : "Failed to create";
                        System.out.println("  Loading " + character.getDisplayName() + ": weaponId=" + unitData.weaponId + " → " + weaponName);
                    } else {
                        System.out.println("  Loading " + character.getDisplayName() + ": no weapon data");
                    }
                    
                    Unit unit = deserializeUnitFromCharacterRef(unitData, character);
                    units.add(unit);
                } else {
                    System.err.println("Warning: Character " + unitData.characterId + " not found in universal registry");
                }
            }
        }
        
        // Second phase: Restore target relationships after all units are loaded
        restoreTargetRelationships(saveData.units);
        
        System.out.println("*** Restored " + units.size() + " units ***");
    }
    
    /**
     * Restore target relationships between units after all units have been loaded
     * 
     * @param unitDataList List of UnitData objects containing target relationships
     */
    private void restoreTargetRelationships(List<UnitData> unitDataList) {
        // Create a map of unit ID to Unit object for quick lookup
        Map<Integer, Unit> unitMap = new HashMap<>();
        for (Unit unit : units) {
            unitMap.put(unit.getId(), unit);
        }
        
        // Create a map of unit ID to UnitData for finding the corresponding data
        Map<Integer, UnitData> unitDataMap = new HashMap<>();
        for (UnitData unitData : unitDataList) {
            unitDataMap.put(unitData.id, unitData);
        }
        
        // Restore target relationships by matching unit IDs
        for (Unit unit : units) {
            UnitData unitData = unitDataMap.get(unit.getId());
            if (unitData != null && unitData.currentTargetId != null) {
                Unit targetUnit = unitMap.get(unitData.currentTargetId);
                if (targetUnit != null) {
                    unit.character.currentTarget = targetUnit;
                    System.out.println("  Restored target: " + unit.character.getDisplayName() + " → " + targetUnit.character.getDisplayName());
                } else {
                    System.out.println("  Warning: Target unit " + unitData.currentTargetId + " not found for " + unit.character.getDisplayName());
                }
            }
        }
    }
    
    /**
     * Deserialize character from CharacterData
     * 
     * @param data CharacterData object
     * @return Character object
     */
    private combat.Character deserializeCharacter(CharacterData data) {
        // Handle both old and new save formats
        String nickname = data.nickname != null ? data.nickname : data.name;
        String firstName = data.firstName != null ? data.firstName : "";
        String lastName = data.lastName != null ? data.lastName : "";
        Date birthdate = data.birthdate != null ? data.birthdate : new Date(0); // Default to epoch if null
        
        combat.Character character = new combat.Character(
            data.id, nickname, firstName, lastName, birthdate, data.themeId, data.dexterity, data.health,
            data.coolness, data.strength, data.reflexes, data.handedness
        );
        
        character.currentDexterity = data.currentDexterity;
        character.currentHealth = data.currentHealth;
        character.baseMovementSpeed = data.baseMovementSpeed;
        character.currentMovementType = data.currentMovementType;
        character.currentAimingSpeed = data.currentAimingSpeed;
        
        // Restore weapon
        if (data.weaponId != null && !data.weaponId.isEmpty()) {
            character.weapon = WeaponFactory.createWeapon(data.weaponId);
            if (character.weapon != null && data.currentWeaponState != null) {
                character.currentWeaponState = character.weapon.getStateByName(data.currentWeaponState);
                if (character.currentWeaponState == null) {
                    character.currentWeaponState = character.weapon.getInitialState();
                }
            }
        }
        
        // Restore skills
        character.skills.clear();
        for (CharacterData.SkillData skillData : data.skills) {
            character.addSkill(new Skill(skillData.skillName, skillData.level));
        }
        
        // Restore wounds
        character.wounds.clear();
        for (CharacterData.WoundData woundData : data.wounds) {
            try {
                BodyPart bodyPart = BodyPart.valueOf(woundData.bodyPart);
                WoundSeverity severity = WoundSeverity.valueOf(woundData.severity);
                character.addWound(new Wound(bodyPart, severity));
            } catch (IllegalArgumentException e) {
                System.err.println("Warning: Invalid wound data: " + woundData.bodyPart + "/" + woundData.severity);
            }
        }
        
        // Restore character preferences (with defaults for backward compatibility)
        character.usesAutomaticTargeting = data.usesAutomaticTargeting;
        character.preferredFiringMode = data.preferredFiringMode != null ? data.preferredFiringMode : FiringMode.SINGLE_SHOT;
        
        return character;
    }
    
    /**
     * Deserialize unit from character reference (new save format)
     * 
     * @param data UnitData object
     * @param character Character object from universal registry
     * @return Unit object
     */
    private Unit deserializeUnitFromCharacterRef(UnitData data, combat.Character character) {
        Color color = stringToColor(data.color);
        Color baseColor = stringToColor(data.baseColor);
        
        // Create unit with the base color initially, then set current color
        Unit unit = new Unit(character, data.x, data.y, baseColor, data.id);
        unit.color = color; // Set the current color (which might be different due to highlighting)
        unit.targetX = data.targetX;
        unit.targetY = data.targetY;
        unit.hasTarget = data.hasTarget;
        unit.isStopped = data.isStopped;
        unit.isHitHighlighted = data.isHitHighlighted;
        unit.isFiringHighlighted = data.isFiringHighlighted;
        
        // Restore weapon firing mode if available
        if (character.weapon != null && data.currentFiringMode != null) {
            character.weapon.currentFiringMode = data.currentFiringMode;
        }
        
        // Restore automatic targeting setting
        character.usesAutomaticTargeting = data.usesAutomaticTargeting;
        
        return unit;
    }
    
    /**
     * Deserialize unit (legacy save format)
     * 
     * @param data UnitData object
     * @param character Character object
     * @return Unit object
     */
    private Unit deserializeUnit(UnitData data, combat.Character character) {
        Color color = stringToColor(data.color);
        Color baseColor = stringToColor(data.baseColor);
        
        // Create unit with the base color initially, then set current color
        Unit unit = new Unit(character, data.x, data.y, baseColor, data.id);
        unit.color = color; // Set the current color (which might be different due to highlighting)
        unit.targetX = data.targetX;
        unit.targetY = data.targetY;
        unit.hasTarget = data.hasTarget;
        unit.isStopped = data.isStopped;
        unit.isHitHighlighted = data.isHitHighlighted;
        // Handle backward compatibility for isFiringHighlighted (defaults to false if not present)
        unit.isFiringHighlighted = data.isFiringHighlighted;
        
        // Restore weapon firing mode if available (new field, may be null in legacy saves)
        if (character.weapon != null && data.currentFiringMode != null) {
            character.weapon.currentFiringMode = data.currentFiringMode;
        }
        
        // Restore automatic targeting setting (defaults to false for legacy saves)
        character.usesAutomaticTargeting = data.usesAutomaticTargeting;
        
        return unit;
    }
    
    /**
     * Convert string representation to JavaFX Color
     * 
     * @param colorString String representation of color
     * @return JavaFX Color object
     */
    private Color stringToColor(String colorString) {
        switch (colorString) {
            case "RED": return Color.RED;
            case "BLUE": return Color.BLUE;
            case "GREEN": return Color.GREEN;
            case "PURPLE": return Color.PURPLE;
            case "ORANGE": return Color.ORANGE;
            case "YELLOW": return Color.YELLOW;
            case "DARKGRAY": return Color.DARKGRAY;
            case "GRAY": return Color.GRAY;
            case "CYAN": return Color.CYAN;
            default: return Color.RED;
        }
    }
}
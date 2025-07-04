/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

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
        if (inputManager != null) {
            inputManager.setWaitingForSaveSlot(true);
        }
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
        if (inputManager != null) {
            inputManager.setWaitingForLoadSlot(true);
        }
        System.out.println("*** LOAD GAME ***");
        List<SaveGameManager.SaveSlotInfo> availableSlots = saveGameManager.listAvailableSlotsWithTests();
        
        if (availableSlots.isEmpty()) {
            System.out.println("No save files found.");
            return;
        }
        
        System.out.println("Available saves:");
        for (SaveGameManager.SaveSlotInfo slot : availableSlots) {
            if (slot instanceof SaveGameManager.TestSaveSlotInfo) {
                SaveGameManager.TestSaveSlotInfo testSlot = (SaveGameManager.TestSaveSlotInfo) slot;
                System.out.println(testSlot.testSlot + ". " + testSlot.getSlotName() + ".json (" + 
                                 testSlot.getFormattedTimestamp() + ") - " + 
                                 testSlot.themeId + ", tick " + testSlot.currentTick);
            } else {
                System.out.println(slot.slot + ". slot_" + slot.slot + ".json (" + 
                                 slot.getFormattedTimestamp() + ") - " + 
                                 slot.themeId + ", tick " + slot.currentTick);
            }
        }
        
        String promptText = "Enter slot number (1-9)";
        if (isDebugModeActive()) {
            promptText += " or test slot (a-z)";
        }
        promptText += " or 0 to cancel: ";
        System.out.println(promptText);
    }
    
    private boolean isDebugModeActive() {
        try {
            // Use reflection to access GameRenderer's debug mode since it's in default package
            Class<?> gameRendererClass = Class.forName("GameRenderer");
            java.lang.reflect.Method isDebugMode = gameRendererClass.getMethod("isDebugMode");
            return (Boolean) isDebugMode.invoke(null);
        } catch (Exception e) {
            // If we can't access debug mode, default to false
            return false;
        }
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
            if (inputManager != null) {
                inputManager.setWaitingForSaveSlot(false);
            }
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
            if (inputManager != null) {
                inputManager.setWaitingForLoadSlot(false);
            }
        }
    }
    
    /**
     * Load game from the specified test slot
     * 
     * @param testSlot Test slot character (a-z)
     */
    public void loadGameFromTestSlot(char testSlot) {
        try {
            SaveData saveData = saveGameManager.loadFromTestSlot(testSlot);
            if (saveData != null) {
                applySaveData(saveData);
                System.out.println("*** Game loaded successfully from test slot " + testSlot + " ***");
                System.out.println("*** Loaded at tick " + gameClock.getCurrentTick() + " ***");
            } else {
                System.out.println("*** Failed to load game from test slot " + testSlot + " ***");
            }
        } catch (Exception e) {
            System.err.println("Error during test load: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (inputManager != null) {
                inputManager.setWaitingForLoadSlot(false);
            }
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
            // Get weapon ID directly from weapon object (DevCycle 17)
            data.weaponId = character.weapon.getWeaponId();
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
            data.wounds.add(new CharacterData.WoundData(wound.getBodyPart().name(), wound.getSeverity().name(), wound.getDamage()));
        }
        
        // Serialize character preferences
        data.usesAutomaticTargeting = character.usesAutomaticTargeting;
        data.preferredFiringMode = character.preferredFiringMode;
        
        // DevCycle 28: Multiple shot control
        data.multipleShootCount = character.multipleShootCount;
        
        return data;
    }
    
    // findWeaponId() method removed in DevCycle 17 - replaced with direct weapon.getWeaponId() access
    
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
            weaponId = unit.character.weapon.getWeaponId(); // Direct access to weapon ID (DevCycle 17)
            if (unit.character.currentWeaponState != null) {
                currentWeaponState = unit.character.currentWeaponState.getState();
            }
            currentFiringMode = (unit.character.weapon instanceof RangedWeapon) ? ((RangedWeapon)unit.character.weapon).getCurrentFiringMode() : null;
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
            unit.character.usesAutomaticTargeting,
            unit.character.faction
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
     * Convert platform.api.Color to string representation
     * 
     * @param color platform.api.Color object
     * @return String representation of color
     */
    private String colorToString(platform.api.Color color) {
        if (color.equals(platform.api.Color.RED)) return "RED";
        if (color.equals(platform.api.Color.BLUE)) return "BLUE";
        if (color.equals(platform.api.Color.GREEN)) return "GREEN";
        if (color.equals(platform.api.Color.PURPLE)) return "PURPLE";
        if (color.equals(platform.api.Color.ORANGE)) return "ORANGE";
        if (color.equals(platform.api.Color.YELLOW)) return "YELLOW";
        if (color.equals(platform.api.Color.DARK_GRAY)) return "DARKGRAY";
        if (color.equals(platform.api.Color.GRAY)) return "GRAY";
        if (color.equals(platform.api.Color.CYAN)) return "CYAN";
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
                    // Apply scenario-specific weapon and state - handle both ranged and melee weapons (DevCycle 40)
                    if (unitData.weaponId != null && !unitData.weaponId.isEmpty()) {
                        // First try to create as melee weapon
                        DataManager dataManager = DataManager.getInstance();
                        if (dataManager.getMeleeWeapon(unitData.weaponId) != null) {
                            character.meleeWeapon = MeleeWeaponFactory.createWeapon(unitData.weaponId);
                            character.weapon = character.meleeWeapon; // Set as primary weapon
                        } else {
                            // Fall back to ranged weapon
                            character.weapon = WeaponFactory.createWeapon(unitData.weaponId);
                        }
                        
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
        String nickname = data.nickname != null ? data.nickname : "";
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
        
        // Restore weapon - handle both ranged and melee weapons (DevCycle 40)
        if (data.weaponId != null && !data.weaponId.isEmpty()) {
            // First try to create as melee weapon
            DataManager dataManager = DataManager.getInstance();
            if (dataManager.getMeleeWeapon(data.weaponId) != null) {
                character.meleeWeapon = MeleeWeaponFactory.createWeapon(data.weaponId);
                character.weapon = character.meleeWeapon; // Set as primary weapon
            } else {
                // Fall back to ranged weapon
                character.weapon = WeaponFactory.createWeapon(data.weaponId);
            }
            
            if (character.weapon != null && data.currentWeaponState != null) {
                character.currentWeaponState = character.weapon.getStateByName(data.currentWeaponState);
                if (character.currentWeaponState == null) {
                    character.currentWeaponState = character.weapon.getInitialState();
                }
            }
        }
        
        // Restore melee combat mode (DevCycle 40)
        character.isMeleeCombatMode = data.isMeleeCombatMode;
        
        // Restore defense timing (DevCycle 40)
        character.nextDefenseTick = data.nextDefenseTick;
        
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
                // Use damage value from save data, default to 1 for backwards compatibility
                int damage = (woundData.damage > 0) ? woundData.damage : 1;
                character.addWound(new Wound(bodyPart, severity, "Saved wound", "", damage));
            } catch (IllegalArgumentException e) {
                System.err.println("Warning: Invalid wound data: " + woundData.bodyPart + "/" + woundData.severity);
            }
        }
        
        // Restore character preferences (with defaults for backward compatibility)
        character.usesAutomaticTargeting = data.usesAutomaticTargeting;
        character.preferredFiringMode = data.preferredFiringMode != null ? data.preferredFiringMode : FiringMode.SINGLE_SHOT;
        
        // DevCycle 28: Multiple shot control (default to 1 for backward compatibility)
        character.multipleShootCount = data.multipleShootCount > 0 ? data.multipleShootCount : 1;
        
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
        platform.api.Color color = stringToColor(data.color);
        platform.api.Color baseColor = stringToColor(data.baseColor);
        
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
        if (character.weapon != null && data.currentFiringMode != null && character.weapon instanceof RangedWeapon) {
            ((RangedWeapon)character.weapon).setCurrentFiringMode(data.currentFiringMode);
        }
        
        // Restore automatic targeting setting
        character.usesAutomaticTargeting = data.usesAutomaticTargeting;
        
        // Restore faction information
        character.faction = data.faction;
        
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
        platform.api.Color color = stringToColor(data.color);
        platform.api.Color baseColor = stringToColor(data.baseColor);
        
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
        if (character.weapon != null && data.currentFiringMode != null && character.weapon instanceof RangedWeapon) {
            ((RangedWeapon)character.weapon).setCurrentFiringMode(data.currentFiringMode);
        }
        
        // Restore automatic targeting setting (defaults to false for legacy saves)
        character.usesAutomaticTargeting = data.usesAutomaticTargeting;
        
        // Restore faction information (defaults to 1 for legacy saves if not present)
        character.faction = data.faction;
        
        return unit;
    }
    
    /**
     * Convert string representation to platform.api.Color
     * 
     * @param colorString String representation of color
     * @return platform.api.Color object
     */
    private platform.api.Color stringToColor(String colorString) {
        switch (colorString) {
            case "RED": return platform.api.Color.RED;
            case "BLUE": return platform.api.Color.BLUE;
            case "GREEN": return platform.api.Color.GREEN;
            case "PURPLE": return platform.api.Color.PURPLE;
            case "ORANGE": return platform.api.Color.ORANGE;
            case "YELLOW": return platform.api.Color.YELLOW;
            case "DARKGRAY": return platform.api.Color.DARK_GRAY;
            case "GRAY": return platform.api.Color.GRAY;
            case "CYAN": return platform.api.Color.CYAN;
            default: return platform.api.Color.RED;
        }
    }
}
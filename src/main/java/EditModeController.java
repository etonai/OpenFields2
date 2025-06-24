/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import java.util.List;
import javafx.scene.paint.Color;

import combat.*;
import game.*;
import data.WeaponFactory;
import data.WeaponData;
import data.UniversalCharacterRegistry;
import data.CharacterFactory;

/**
 * EditModeController handles all edit mode operations for the OpenFields2 game, including:
 * - Character creation and spawning
 * - Weapon assignment to selected units
 * - Faction assignment to selected units
 * - Color and weapon assignment based on character archetypes
 * 
 * This class is responsible for managing edit mode state and coordinating with
 * various subsystems to perform edit mode operations.
 */
public class EditModeController {
    // Dependencies
    private final List<Unit> units;
    private final SelectionManager selectionManager;
    private final GameRenderer gameRenderer;
    private final UniversalCharacterRegistry characterRegistry;
    
    // Edit mode state
    private boolean editMode = false;
    
    // Unit management
    private int nextUnitId;
    
    // Screen dimensions for spawn calculations
    private final int screenWidth;
    private final int screenHeight;
    
    /**
     * Interface for callbacks to operations that require access to the main game class
     */
    public interface EditModeCallbacks {
        // Unit ID management
        int getNextUnitId();
        void setNextUnitId(int nextUnitId);
        
        // Edit mode state
        boolean isEditMode();
        void setEditMode(boolean editMode);
    }
    
    private final EditModeCallbacks callbacks;
    
    /**
     * Constructor for EditModeController
     * 
     * @param units List of game units
     * @param selectionManager Unit selection manager
     * @param gameRenderer Game renderer for spawn location calculations
     * @param screenWidth Screen width for spawn calculations
     * @param screenHeight Screen height for spawn calculations
     * @param callbacks Callback interface to main game operations
     */
    public EditModeController(List<Unit> units, SelectionManager selectionManager, 
                             GameRenderer gameRenderer, int screenWidth, int screenHeight,
                             EditModeCallbacks callbacks) {
        this.units = units;
        this.selectionManager = selectionManager;
        this.gameRenderer = gameRenderer;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.callbacks = callbacks;
        
        // Get registry instance
        this.characterRegistry = UniversalCharacterRegistry.getInstance();
    }
    
    /**
     * Display character creation prompt with available archetypes
     */
    public void promptForCharacterCreation() {
        System.out.println("***********************");
        System.out.println("*** CHARACTER CREATION ***");
        System.out.println("Select archetype:");
        System.out.println("1. Gunslinger - High dexterity, quick reflexes, pistol specialist");
        System.out.println("2. Soldier - Balanced combat stats, rifle proficiency");
        System.out.println("3. Weighted Random - Randomly generated stats (averaged), no skills");
        System.out.println("4. Scout - High reflexes, stealth and observation skills");
        System.out.println("5. Marksman - Excellent dexterity, rifle specialist, long-range expert");
        System.out.println("6. Brawler - High strength, close combat specialist");
        System.out.println("7. Confederate Soldier - Civil War Confederate with Brown Bess musket");
        System.out.println("8. Union Soldier - Civil War Union with Brown Bess musket");
        System.out.println("9. Balanced - Well-rounded stats for versatile gameplay");
        System.out.println("0. Cancel character creation");
        System.out.println();
        System.out.println("Enter selection (1-9, 0 to cancel): ");
    }
    
    /**
     * Create a character from the specified archetype
     * 
     * @param archetypeIndex Index of the archetype (1-9)
     */
    public void createCharacterFromArchetype(int archetypeIndex) {
        String[] archetypes = {"gunslinger", "soldier", "weighted_random", "scout", "marksman", "brawler", "confederate_soldier", "union_soldier", "balanced"};
        
        if (archetypeIndex < 1 || archetypeIndex > archetypes.length) {
            System.out.println("*** Invalid archetype selection ***");
            return;
        }
        
        String selectedArchetype = archetypes[archetypeIndex - 1];
        
        try {
            // Create character using CharacterFactory
            int characterId = CharacterFactory.createCharacter(selectedArchetype);
            combat.Character character = characterRegistry.getCharacter(characterId);
            
            if (character != null) {
                // Assign appropriate weapon based on archetype
                String weaponId = getWeaponForArchetype(selectedArchetype);
                character.weapon = WeaponFactory.createWeapon(weaponId);
                character.currentWeaponState = character.weapon.getInitialState();
                character.setFaction(1); // Default faction
                
                // Spawn character at camera center
                spawnCharacterUnit(character, selectedArchetype);
                
                // Display character creation confirmation
                System.out.println("*** Character created successfully! ***");
                System.out.println("Name: " + character.getDisplayName());
                System.out.println("Archetype: " + selectedArchetype);
                System.out.println("Stats: DEX=" + character.dexterity + " HEALTH=" + character.health + 
                                 " COOL=" + character.coolness + " STR=" + character.strength + " REF=" + character.reflexes);
                System.out.println("Handedness: " + character.handedness.getDisplayName());
                System.out.println("Weapon: " + character.weapon.name);
                System.out.println("Skills: " + (character.skills.isEmpty() ? "None" : character.skills.size() + " skills"));
                System.out.println("***********************");
            } else {
                System.out.println("*** Failed to create character ***");
            }
        } catch (Exception e) {
            System.out.println("*** Error creating character: " + e.getMessage() + " ***");
        }
    }
    
    /**
     * Spawn a character unit in the game world
     * 
     * @param character The character to spawn
     * @param archetype The archetype string for color determination
     */
    private void spawnCharacterUnit(combat.Character character, String archetype) {
        // Calculate spawn location at camera center
        double spawnX = gameRenderer.screenToWorldX(screenWidth / 2.0);
        double spawnY = gameRenderer.screenToWorldY(screenHeight / 2.0);
        
        // Check for collision with existing units and offset if necessary
        boolean collision = true;
        int attempts = 0;
        double finalX = spawnX;
        double finalY = spawnY;
        
        while (collision && attempts < 10) {
            collision = false;
            for (Unit existingUnit : units) {
                double distance = Math.hypot(finalX - existingUnit.x, finalY - existingUnit.y);
                if (distance < 28) { // 4 feet = 28 pixels minimum distance
                    collision = true;
                    finalX += 28; // Offset by 4 feet (28 pixels) in X direction only
                    break;
                }
            }
            attempts++;
        }
        
        // Get color based on archetype
        Color javafxColor = getColorForArchetype(archetype);
        platform.api.Color characterColor = platform.api.Color.fromJavaFX(javafxColor);
        
        // Create and add unit
        int unitId = callbacks.getNextUnitId();
        Unit newUnit = new Unit(character, finalX, finalY, characterColor, unitId);
        callbacks.setNextUnitId(unitId + 1);
        units.add(newUnit);
        
        // Auto-select the newly created character
        selectionManager.selectUnit(newUnit);
        
        System.out.println("Character spawned at (" + String.format("%.0f", finalX) + ", " + String.format("%.0f", finalY) + ")");
    }
    
    /**
     * Get the appropriate weapon ID for a character archetype
     * 
     * @param archetype The character archetype
     * @return The weapon ID string
     */
    private String getWeaponForArchetype(String archetype) {
        switch (archetype.toLowerCase()) {
            case "gunslinger":
            case "brawler":
            case "balanced":
            case "weighted_random":
                return "wpn_colt_peacemaker"; // Pistol
            case "soldier":
            case "scout": 
            case "marksman":
                return "wpn_hunting_rifle"; // Rifle
            case "confederate_soldier":
            case "union_soldier":
                return "wpn_brown_bess"; // Brown Bess musket
            case "medic":
                return "wpn_derringer"; // Backup weapon
            default:
                return "wpn_colt_peacemaker"; // Default fallback
        }
    }
    
    /**
     * Get the appropriate color for a character archetype
     * 
     * @param archetype The character archetype
     * @return The color for the character
     */
    private Color getColorForArchetype(String archetype) {
        switch (archetype.toLowerCase()) {
            case "confederate_soldier":
                return Color.DARKGRAY; // Confederate dark gray
            case "union_soldier":
                return Color.BLUE; // Union blue
            default:
                return Color.CYAN; // Default color for other archetypes
        }
    }
    
    /**
     * Display weapon type selection prompt (Ranged or Melee)
     */
    public void promptForWeaponSelection() {
        System.out.println("***********************");
        System.out.println("*** WEAPON SELECTION ***");
        System.out.println("Selected units: " + selectionManager.getSelectionCount());
        System.out.println("Choose weapon type:");
        System.out.println("1. Ranged Weapons");
        System.out.println("2. Melee Weapons");
        System.out.println("0. Cancel weapon selection");
        System.out.println();
        System.out.println("Enter selection (1-2, 0 to cancel): ");
    }
    
    /**
     * Display ranged weapon selection prompt
     */
    public void promptForRangedWeaponSelection() {
        String[] weaponIds = WeaponFactory.getAllWeaponIds();
        if (weaponIds.length == 0) {
            System.out.println("*** No ranged weapons available ***");
            return;
        }
        
        System.out.println("***********************");
        System.out.println("*** RANGED WEAPON SELECTION ***");
        System.out.println("Selected units: " + selectionManager.getSelectionCount());
        System.out.println("Available ranged weapons:");
        
        for (int i = 0; i < weaponIds.length; i++) {
            WeaponData weaponData = WeaponFactory.getWeaponData(weaponIds[i]);
            if (weaponData != null) {
                if (i == 9) {
                    // Option 10 is selected with 'A' key
                    System.out.println("A. " + weaponData.name + 
                                     " (" + weaponData.type.getDisplayName() + 
                                     ") - Damage: " + weaponData.damage + 
                                     ", Range: " + String.format("%.0f", weaponData.maximumRange) + " feet");
                } else {
                    System.out.println((i + 1) + ". " + weaponData.name + 
                                     " (" + weaponData.type.getDisplayName() + 
                                     ") - Damage: " + weaponData.damage + 
                                     ", Range: " + String.format("%.0f", weaponData.maximumRange) + " feet");
                }
            }
        }
        System.out.println("0. Cancel ranged weapon selection");
        System.out.println();
        System.out.println("Enter selection (1-" + weaponIds.length + " or A for option 10, 0 to cancel): ");
    }
    
    /**
     * Display melee weapon selection prompt
     */
    public void promptForMeleeWeaponSelection() {
        data.DataManager dataManager = data.DataManager.getInstance();
        java.util.Map<String, data.MeleeWeaponData> meleeWeapons = dataManager.getAllMeleeWeapons();
        String[] meleeWeaponIds = meleeWeapons.keySet().toArray(new String[0]);
        
        if (meleeWeaponIds.length == 0) {
            System.out.println("*** No melee weapons available ***");
            return;
        }
        
        System.out.println("***********************");
        System.out.println("*** MELEE WEAPON SELECTION ***");
        System.out.println("Selected units: " + selectionManager.getSelectionCount());
        System.out.println("Available melee weapons:");
        
        for (int i = 0; i < meleeWeaponIds.length; i++) {
            data.MeleeWeaponData meleeWeaponData = meleeWeapons.get(meleeWeaponIds[i]);
            if (meleeWeaponData != null) {
                if (i == 9) {
                    // Option 10 is selected with 'A' key
                    System.out.println("A. " + meleeWeaponData.name + 
                                     " (" + meleeWeaponData.meleeType + 
                                     ") - Damage: " + meleeWeaponData.damage + 
                                     ", Length: " + String.format("%.1f", meleeWeaponData.weaponLength) + " feet");
                } else {
                    System.out.println((i + 1) + ". " + meleeWeaponData.name + 
                                     " (" + meleeWeaponData.meleeType + 
                                     ") - Damage: " + meleeWeaponData.damage + 
                                     ", Length: " + String.format("%.1f", meleeWeaponData.weaponLength) + " feet");
                }
            }
        }
        System.out.println("0. Cancel melee weapon selection");
        System.out.println();
        System.out.println("Enter selection (1-" + meleeWeaponIds.length + " or A for option 10, 0 to cancel): ");
    }
    
    /**
     * Assign the selected ranged weapon to all selected units
     * 
     * @param weaponIndex Index of the weapon to assign (1-based)
     */
    public void assignRangedWeaponToSelectedUnits(int weaponIndex) {
        String[] weaponIds = WeaponFactory.getAllWeaponIds();
        
        if (weaponIndex < 1 || weaponIndex > weaponIds.length) {
            System.out.println("*** Invalid weapon selection. Use 1-" + weaponIds.length + " or 0 to cancel ***");
            return;
        }
        
        String selectedWeaponId = weaponIds[weaponIndex - 1];
        WeaponData weaponData = WeaponFactory.getWeaponData(selectedWeaponId);
        
        if (weaponData == null) {
            System.out.println("*** Error: Weapon data not found for " + selectedWeaponId + " ***");
            return;
        }
        
        int successCount = 0;
        int failureCount = 0;
        
        for (Unit unit : selectionManager.getSelectedUnits()) {
            try {
                // Get old weapon name for comparison
                String oldWeaponName = (unit.character.weapon != null) ? unit.character.weapon.name : "None";
                
                // Create new weapon instance
                combat.Weapon newWeapon = WeaponFactory.createWeapon(selectedWeaponId);
                
                // Assign weapon to character
                unit.character.weapon = newWeapon;
                unit.character.currentWeaponState = newWeapon.getInitialState();
                
                // Debug output
                System.out.println("  " + unit.character.getDisplayName() + ": " + oldWeaponName + " → " + newWeapon.name);
                
                successCount++;
            } catch (Exception e) {
                System.err.println("Failed to assign ranged weapon to " + unit.character.getDisplayName() + ": " + e.getMessage());
                failureCount++;
            }
        }
        
        // Display results
        System.out.println("*** Ranged weapon assignment complete ***");
        System.out.println("Weapon: " + weaponData.name + " (" + weaponData.type.getDisplayName() + ")");
        System.out.println("Successfully assigned to " + successCount + " units");
        if (failureCount > 0) {
            System.out.println("Failed to assign to " + failureCount + " units");
        }
        System.out.println("***********************");
    }
    
    /**
     * Assign the selected melee weapon to all selected units
     * 
     * @param weaponIndex Index of the weapon to assign (1-based)
     */
    public void assignMeleeWeaponToSelectedUnits(int weaponIndex) {
        data.DataManager dataManager = data.DataManager.getInstance();
        java.util.Map<String, data.MeleeWeaponData> meleeWeapons = dataManager.getAllMeleeWeapons();
        String[] meleeWeaponIds = meleeWeapons.keySet().toArray(new String[0]);
        
        if (weaponIndex < 1 || weaponIndex > meleeWeaponIds.length) {
            System.out.println("*** Invalid weapon selection. Use 1-" + meleeWeaponIds.length + " or 0 to cancel ***");
            return;
        }
        
        String selectedMeleeWeaponId = meleeWeaponIds[weaponIndex - 1];
        data.MeleeWeaponData meleeWeaponData = meleeWeapons.get(selectedMeleeWeaponId);
        
        if (meleeWeaponData == null) {
            System.out.println("*** Error: Melee weapon data not found for " + selectedMeleeWeaponId + " ***");
            return;
        }
        
        int successCount = 0;
        int failureCount = 0;
        
        for (Unit unit : selectionManager.getSelectedUnits()) {
            try {
                // Get old melee weapon name for comparison
                String oldMeleeWeaponName = (unit.character.meleeWeapon != null) ? unit.character.meleeWeapon.name : "Unarmed";
                
                // Create new melee weapon instance
                combat.MeleeWeapon newMeleeWeapon = combat.MeleeWeaponFactory.createWeapon(selectedMeleeWeaponId);
                
                // Assign melee weapon to character
                unit.character.meleeWeapon = newMeleeWeapon;
                
                // Debug output
                System.out.println("  " + unit.character.getDisplayName() + ": " + oldMeleeWeaponName + " → " + newMeleeWeapon.name);
                
                successCount++;
            } catch (Exception e) {
                System.err.println("Failed to assign melee weapon to " + unit.character.getDisplayName() + ": " + e.getMessage());
                failureCount++;
            }
        }
        
        // Display results
        System.out.println("*** Melee weapon assignment complete ***");
        System.out.println("Weapon: " + meleeWeaponData.name + " (" + meleeWeaponData.meleeType + ")");
        System.out.println("Successfully assigned to " + successCount + " units");
        if (failureCount > 0) {
            System.out.println("Failed to assign to " + failureCount + " units");
        }
        System.out.println("***********************");
    }
    
    /**
     * Legacy method for backward compatibility - now routes to weapon type selection
     * 
     * @param weaponIndex Index of the weapon to assign (1-based)
     */
    public void assignWeaponToSelectedUnits(int weaponIndex) {
        // For backward compatibility, this now routes to ranged weapon assignment
        assignRangedWeaponToSelectedUnits(weaponIndex);
    }
    
    /**
     * Display faction selection prompt with available factions
     */
    public void promptForFactionSelection() {
        System.out.println("***********************");
        System.out.println("*** FACTION SELECTION ***");
        System.out.println("Selected units: " + selectionManager.getSelectionCount());
        System.out.println("Available factions:");
        System.out.println("1. Faction 1 (Red units)");
        System.out.println("2. Faction 2 (Blue units)");
        System.out.println("3. Faction 3 (Neutral)");
        System.out.println("4. Faction 4 (Custom)");
        System.out.println("5. Faction 5 (Custom)");
        System.out.println("0. Cancel faction selection");
        System.out.println();
        System.out.println("Enter selection (1-5, 0 to cancel): ");
    }
    
    /**
     * Assign the selected faction to all selected units
     * 
     * @param factionNumber The faction number to assign (1-5)
     */
    public void assignFactionToSelectedUnits(int factionNumber) {
        if (factionNumber < 1 || factionNumber > 5) {
            System.out.println("*** Invalid faction selection. Use 1-5 or 0 to cancel ***");
            return;
        }
        
        int successCount = 0;
        String factionName = getFactionName(factionNumber);
        
        for (Unit unit : selectionManager.getSelectedUnits()) {
            try {
                unit.character.setFaction(factionNumber);
                successCount++;
            } catch (Exception e) {
                System.err.println("Failed to assign faction to " + unit.character.getDisplayName() + ": " + e.getMessage());
            }
        }
        
        // Display results
        System.out.println("*** Faction assignment complete ***");
        System.out.println("Faction: " + factionName + " (ID: " + factionNumber + ")");
        System.out.println("Successfully assigned to " + successCount + " units");
        System.out.println("***********************");
    }
    
    /**
     * Get the display name for a faction number
     * 
     * @param factionNumber The faction number
     * @return The faction display name
     */
    private String getFactionName(int factionNumber) {
        switch (factionNumber) {
            case 1: return "Faction 1 (Red)";
            case 2: return "Faction 2 (Blue)";
            case 3: return "Faction 3 (Neutral)";
            case 4: return "Faction 4 (Custom)";
            case 5: return "Faction 5 (Custom)";
            default: return "Unknown Faction";
        }
    }
    
    /**
     * Check if edit mode is currently enabled
     * 
     * @return true if edit mode is enabled
     */
    public boolean isEditMode() {
        return callbacks.isEditMode();
    }
    
    /**
     * Set edit mode state
     * 
     * @param editMode true to enable edit mode, false to disable
     */
    public void setEditMode(boolean editMode) {
        callbacks.setEditMode(editMode);
    }
}
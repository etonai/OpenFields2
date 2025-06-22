/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import java.util.List;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import combat.Character;
import game.Unit;
import input.interfaces.InputManagerCallbacks;
import input.states.InputStates;
import data.UniversalCharacterRegistry;

/**
 * Controller responsible for managing character creation workflows in the game.
 * 
 * This controller handles all aspects of character creation including:
 * - Batch character creation with archetype and faction selection
 * - Individual character creation workflows
 * - Character spawning and placement logic
 * - Weapon assignment based on archetype
 * - Character deployment coordination
 * 
 * The controller uses state machine patterns to manage complex multi-step workflows,
 * ensuring consistent user experience and proper error handling throughout the
 * character creation process.
 * 
 * @author DevCycle 15h - Phase 2.1: Character Creation Controller Extraction
 */
public class CharacterCreationController {
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Dependencies and State
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /** Reference to game units list for spawning new characters */
    private final List<Unit> units;
    
    /** Canvas for coordinate calculations during character placement */
    private final Canvas canvas;
    
    /** Game renderer for world coordinate conversion (from default package) */
    private final GameRenderer gameRenderer;
    
    /** Callback interface for main game operations */
    private final InputManagerCallbacks callbacks;
    
    /** Character registry for accessing character data */
    private final UniversalCharacterRegistry characterRegistry;
    
    // Batch Character Creation Workflow State
    /** Number of characters to create in batch operation */
    private int batchQuantity = 0;
    
    /** Selected archetype index for batch creation */
    private int batchArchetype = 0;
    
    /** Selected faction number for batch creation */
    private int batchFaction = 0;
    
    /** Current step in batch creation workflow */
    private InputStates.BatchCreationStep batchCreationStep = InputStates.BatchCreationStep.QUANTITY;
    
    // Individual Character Creation Workflow State
    /** Selected archetype name for individual character creation */
    private String selectedArchetype = "";
    
    /** Selected ranged weapon for individual character creation */
    private String selectedRangedWeapon = "";
    
    /** Selected melee weapon for individual character creation */
    private String selectedMeleeWeapon = "";
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Constructor for CharacterCreationController.
     * 
     * @param units List of game units for spawning new characters
     * @param canvas Canvas for coordinate calculations
     * @param gameRenderer Game renderer for coordinate conversion
     * @param callbacks Callback interface for main game operations
     */
    public CharacterCreationController(List<Unit> units, Canvas canvas, 
                                     GameRenderer gameRenderer, 
                                     InputManagerCallbacks callbacks) {
        this.units = units;
        this.canvas = canvas;
        this.gameRenderer = gameRenderer;
        this.callbacks = callbacks;
        this.characterRegistry = UniversalCharacterRegistry.getInstance();
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Batch Character Creation Workflow
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Start the batch character creation workflow.
     * This initiates a multi-step process for creating multiple characters at once.
     */
    public void promptForBatchCharacterCreation() {
        batchCreationStep = InputStates.BatchCreationStep.QUANTITY;
        batchQuantity = 0;
        batchArchetype = 0;
        batchFaction = 0;
        
        System.out.println("***********************");
        System.out.println("*** BATCH CHARACTER CREATION ***");
        System.out.println("How many characters do you want to create?");
        System.out.println("Enter quantity (1-20, 0 to cancel): ");
    }
    
    /**
     * Handle input during batch character creation workflow.
     * 
     * @param inputNumber The number entered by the user
     * @return true if the workflow should continue, false if cancelled or completed
     */
    public boolean handleBatchCharacterCreationInput(int inputNumber) {
        switch (batchCreationStep) {
            case QUANTITY:
                if (inputNumber == 0) {
                    System.out.println("*** Batch character creation cancelled ***");
                    resetBatchCreationState();
                    return false;
                } else if (inputNumber >= 1 && inputNumber <= 20) {
                    batchQuantity = inputNumber;
                    batchCreationStep = InputStates.BatchCreationStep.ARCHETYPE;
                    showArchetypeSelection();
                    return true;
                } else {
                    System.out.println("*** Invalid quantity. Use 1-20 or 0 to cancel ***");
                    return true;
                }
                
            case ARCHETYPE:
                if (inputNumber == 0) {
                    System.out.println("*** Batch character creation cancelled ***");
                    resetBatchCreationState();
                    return false;
                } else if (inputNumber >= 1 && inputNumber <= 9) {
                    batchArchetype = inputNumber;
                    batchCreationStep = InputStates.BatchCreationStep.FACTION;
                    showFactionSelection();
                    return true;
                } else {
                    System.out.println("*** Invalid archetype selection. Use 1-9 or 0 to cancel ***");
                    return true;
                }
                
            case FACTION:
                if (inputNumber == 0) {
                    System.out.println("*** Batch character creation cancelled ***");
                    resetBatchCreationState();
                    return false;
                } else if (inputNumber >= 1 && inputNumber <= 4) {
                    batchFaction = inputNumber;
                    createBatchCharacters();
                    resetBatchCreationState();
                    return false; // Workflow completed
                } else {
                    System.out.println("*** Invalid faction selection. Use 1-4 or 0 to cancel ***");
                    return true;
                }
        }
        return false;
    }
    
    /**
     * Reset the batch creation state to initial values.
     */
    private void resetBatchCreationState() {
        batchQuantity = 0;
        batchArchetype = 0;
        batchFaction = 0;
        batchCreationStep = InputStates.BatchCreationStep.QUANTITY;
    }
    
    /**
     * Show archetype selection menu for batch creation.
     */
    private void showArchetypeSelection() {
        System.out.println("***********************");
        System.out.println("*** ARCHETYPE SELECTION ***");
        System.out.println("Creating " + batchQuantity + " characters");
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
        System.out.println("0. Cancel batch creation");
        System.out.println();
        System.out.println("Enter selection (1-9, 0 to cancel): ");
    }
    
    /**
     * Show faction selection menu for batch creation.
     */
    private void showFactionSelection() {
        System.out.println("***********************");
        System.out.println("*** FACTION SELECTION ***");
        System.out.println("Creating " + batchQuantity + " characters");
        System.out.println("Archetype: " + getArchetypeName(batchArchetype));
        System.out.println("Select faction:");
        System.out.println("1. NONE - No faction");
        System.out.println("2. Union - Federal forces");
        System.out.println("3. Confederacy - Confederate forces");
        System.out.println("4. Southern Unionists - Pro-Union Southerners");
        System.out.println("0. Cancel batch creation");
        System.out.println();
        System.out.println("Enter selection (1-4, 0 to cancel): ");
    }
    
    /**
     * Create the batch of characters with the selected settings.
     */
    private void createBatchCharacters() {
        System.out.println("***********************");
        System.out.println("*** CREATING CHARACTERS ***");
        System.out.println("Quantity: " + batchQuantity);
        System.out.println("Archetype: " + getArchetypeName(batchArchetype));
        System.out.println("Faction: " + getFactionName(batchFaction));
        System.out.println();
        
        // Convert faction number to faction ID (1-based to 0-based for NONE, Union, Confederacy, Southern Unionists)
        int factionId = batchFaction - 1;
        
        int successCount = 0;
        for (int i = 0; i < batchQuantity; i++) {
            try {
                // Create character using the same method but with faction assignment
                createSingleBatchCharacter(batchArchetype, factionId);
                successCount++;
            } catch (Exception e) {
                System.err.println("Failed to create character " + (i + 1) + ": " + e.getMessage());
            }
        }
        
        System.out.println("*** BATCH CREATION COMPLETE ***");
        System.out.println("Successfully created " + successCount + " out of " + batchQuantity + " characters");
        if (successCount < batchQuantity) {
            System.out.println("Failed to create " + (batchQuantity - successCount) + " characters");
        }
        System.out.println("***********************");
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Character Creation Implementation
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Create a single character as part of batch creation.
     * 
     * @param archetypeIndex The archetype index (1-9)
     * @param factionId The faction ID (0-3)
     */
    private void createSingleBatchCharacter(int archetypeIndex, int factionId) {
        String[] archetypes = {"gunslinger", "soldier", "weighted_random", "scout", "marksman", 
                              "brawler", "confederate_soldier", "union_soldier", "balanced"};
        
        if (archetypeIndex < 1 || archetypeIndex > archetypes.length) {
            throw new IllegalArgumentException("Invalid archetype index: " + archetypeIndex);
        }
        
        String selectedArchetype = archetypes[archetypeIndex - 1];
        
        // Create character using CharacterFactory
        int characterId = data.CharacterFactory.createCharacter(selectedArchetype);
        Character character = characterRegistry.getCharacter(characterId);
        
        if (character != null) {
            // Assign appropriate weapon based on archetype
            String weaponId = getWeaponForArchetype(selectedArchetype);
            character.weapon = data.WeaponFactory.createWeapon(weaponId);
            character.currentWeaponState = character.weapon.getInitialState();
            character.setFaction(factionId);
            
            // Save character to faction file
            data.CharacterPersistenceManager.getInstance().saveCharacter(character);
            
            // Spawn character at camera center with offset
            spawnBatchCharacterUnit(character, selectedArchetype);
            
            System.out.println("Created: " + character.getDisplayName() + " (ID: " + character.id + 
                             ", Faction: " + getFactionName(factionId + 1) + ")");
        } else {
            throw new RuntimeException("Failed to create character from archetype: " + selectedArchetype);
        }
    }
    
    /**
     * Spawn a batch character unit in the game world with offset to avoid collisions.
     */
    private void spawnBatchCharacterUnit(Character character, String archetype) {
        // Calculate spawn location at camera center using canvas dimensions
        double baseX = gameRenderer.screenToWorldX(canvas.getWidth() / 2.0);
        double baseY = gameRenderer.screenToWorldY(canvas.getHeight() / 2.0);
        
        // Add offset based on character ID to spread characters out
        double offsetX = (character.id % 5) * 35; // 5 feet spacing horizontally 
        double offsetY = (character.id / 5) * 35; // 5 feet spacing vertically after 5 characters
        
        double spawnX = baseX + offsetX;
        double spawnY = baseY + offsetY;
        
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
        Color characterColor = getColorForArchetype(archetype);
        
        // Create and add unit
        int unitId = callbacks.getNextUnitId();
        Unit newUnit = new Unit(character, finalX, finalY, characterColor, unitId);
        callbacks.setNextUnitId(unitId + 1);
        units.add(newUnit);
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Archetype and Weapon Management
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Get weapon ID for archetype (consistent with EditModeController pattern).
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
     * Get color for character archetype (consistent with EditModeController pattern).
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
     * Get display name for archetype number.
     * 
     * @param archetypeNumber The archetype number (1-9)
     * @return The display name
     */
    private String getArchetypeName(int archetypeNumber) {
        String[] names = {"Gunslinger", "Soldier", "Weighted Random", "Scout", "Marksman", 
                         "Brawler", "Confederate Soldier", "Union Soldier", "Balanced"};
        if (archetypeNumber >= 1 && archetypeNumber <= names.length) {
            return names[archetypeNumber - 1];
        }
        return "Unknown";
    }
    
    /**
     * Get faction display name by number (1-4).
     */
    private String getFactionName(int factionNumber) {
        switch (factionNumber) {
            case 1: return "NONE";
            case 2: return "Union";
            case 3: return "Confederacy"; 
            case 4: return "Southern Unionists";
            default: return "Unknown";
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Individual Character Creation (Future Extension Point)
    // ─────────────────────────────────────────────────────────────────────────────────
    
    /**
     * Set the selected archetype for individual character creation.
     * This is used for single character creation workflows.
     * 
     * @param archetype The archetype name
     */
    public void setSelectedArchetype(String archetype) {
        this.selectedArchetype = archetype;
    }
    
    /**
     * Set the selected ranged weapon for individual character creation.
     * 
     * @param weaponId The weapon ID
     */
    public void setSelectedRangedWeapon(String weaponId) {
        this.selectedRangedWeapon = weaponId;
    }
    
    /**
     * Set the selected melee weapon for individual character creation.
     * 
     * @param weaponId The weapon ID
     */
    public void setSelectedMeleeWeapon(String weaponId) {
        this.selectedMeleeWeapon = weaponId;
    }
    
    /**
     * Get the current selected archetype.
     * 
     * @return The selected archetype name
     */
    public String getSelectedArchetype() {
        return selectedArchetype;
    }
    
    /**
     * Get the current selected ranged weapon.
     * 
     * @return The selected ranged weapon ID
     */
    public String getSelectedRangedWeapon() {
        return selectedRangedWeapon;
    }
    
    /**
     * Get the current selected melee weapon.
     * 
     * @return The selected melee weapon ID
     */
    public String getSelectedMeleeWeapon() {
        return selectedMeleeWeapon;
    }
    
    /**
     * Get the current batch creation step.
     * 
     * @return The current batch creation step
     */
    public InputStates.BatchCreationStep getBatchCreationStep() {
        return batchCreationStep;
    }
    
    /**
     * Check if batch character creation is currently active.
     * 
     * @return true if batch creation is in progress
     */
    public boolean isBatchCreationActive() {
        return batchQuantity > 0 || batchCreationStep != InputStates.BatchCreationStep.QUANTITY;
    }
}
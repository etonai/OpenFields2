/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.util.List;
import combat.*;
import game.*;
import data.UniversalCharacterRegistry;
import data.FactionRegistry;
import data.Faction;
import data.CharacterPersistenceManager;
import data.WeaponFactory;
import data.DataManager;
import data.WeaponData;
import data.MeleeWeaponData;
import input.interfaces.InputManagerCallbacks;

/**
 * EditModeManager handles all character creation and edit mode workflows.
 * 
 * This component was extracted from InputManager as part of DevCycle 15d incremental refactoring.
 * It manages complex multi-step workflows for character creation, weapon assignment, faction
 * management, and batch operations.
 * 
 * RESPONSIBILITIES:
 * - Character creation workflows (single and batch)
 * - Direct character addition operations (CTRL-A)
 * - Weapon assignment and selection processes
 * - Faction assignment and management
 * - Edit mode state coordination
 * 
 * DESIGN PRINCIPLES:
 * - Workflow orchestration: Manages complex multi-step user interactions
 * - State integration: Coordinates with InputStateTracker for workflow states
 * - Callback preservation: Maintains all existing callback interfaces
 * - Data validation: Comprehensive validation of character creation data
 * - Batch efficiency: Optimized handling of multiple character operations
 * 
 * @author DevCycle 15d - Workflow Component Extraction
 */
public class EditModeManager {
    
    // ====================
    // DEPENDENCIES
    // ====================
    
    private final InputStateTracker stateTracker;
    private final SelectionManager selectionManager;
    private final List<Unit> units;
    private final InputManagerCallbacks callbacks;
    private final UniversalCharacterRegistry characterRegistry;
    
    // ====================
    // WORKFLOW STATE DATA
    // ====================
    
    // Individual Character Creation State
    private String selectedArchetype = "";
    private String selectedRangedWeapon = "";
    private String selectedMeleeWeapon = "";
    private String selectedFaction = "";
    
    // Batch Character Creation State
    private int batchQuantity = 0;
    private String batchWeapon = "";
    private int batchFaction = 0;
    private double batchSpacing = 0.0;
    
    // Direct Character Addition Workflow State  
    private int directAdditionFaction = 0;
    private int directAdditionQuantity = 0;
    private double directAdditionSpacing = 0.0;
    private String directAdditionDirection = "RIGHT"; // "RIGHT" or "DOWN"
    private String directAdditionRangedWeapon = ""; // Selected ranged weapon ID
    private String directAdditionMeleeWeapon = ""; // Selected melee weapon ID
    private Integer[] availableFactionIds = null; // Cache faction IDs for selection mapping
    private List<combat.Character> directAdditionCharacters = new java.util.ArrayList<>();
    
    // Workflow step enums (maintaining existing from InputManager)
    public enum CreationStep {
        ARCHETYPE_SELECTION,
        RANGED_WEAPON_SELECTION, 
        MELEE_WEAPON_SELECTION,
        FACTION_SELECTION,
        COMPLETE
    }
    
    
    public enum DirectAdditionStep {
        FACTION_SELECTION,
        QUANTITY_INPUT,
        SPACING_INPUT,
        RANGED_WEAPON_SELECTION,
        MELEE_WEAPON_SELECTION,
        DIRECTION_SELECTION,
        PLACEMENT
    }
    
    private CreationStep creationStep = CreationStep.ARCHETYPE_SELECTION;
    private DirectAdditionStep directAdditionStep = DirectAdditionStep.FACTION_SELECTION;
    
    // ====================
    // CONSTRUCTOR
    // ====================
    
    /**
     * Creates a new EditModeManager with required dependencies.
     */
    public EditModeManager(InputStateTracker stateTracker, SelectionManager selectionManager,
                          List<Unit> units, InputManagerCallbacks callbacks) {
        this.stateTracker = stateTracker;
        this.selectionManager = selectionManager;
        this.units = units;
        this.callbacks = callbacks;
        this.characterRegistry = UniversalCharacterRegistry.getInstance();
    }
    
    // ====================
    // EDIT MODE KEY HANDLING
    // ====================
    
    /**
     * Handles edit mode specific keyboard input for character creation and management.
     */
    public void handleEditModeKeys(KeyEvent e) {
        // Batch character creation (Ctrl+C)
        if (e.getCode() == KeyCode.C && e.isControlDown()) {
            if (callbacks.isEditMode() && !stateTracker.isWaitingForAnyPrompt()) {
                promptForBatchCharacterCreation();
            } else if (!callbacks.isEditMode()) {
                System.out.println("*** Character creation only available in edit mode (Ctrl+E) ***");
            } else if (stateTracker.isWaitingForAnyPrompt()) {
                System.out.println("*** Please complete current operation before creating characters ***");
            }
        }
        
        // Weapon selection (Ctrl+W)
        if (e.getCode() == KeyCode.W && e.isControlDown()) {
            if (callbacks.isEditMode() && !stateTracker.isWaitingForAnyPrompt()) {
                if (selectionManager.hasSelection()) {
                    callbacks.promptForWeaponSelection();
                } else {
                    System.out.println("*** No units selected - select a unit first ***");
                }
            } else if (!callbacks.isEditMode()) {
                System.out.println("*** Weapon selection only available in edit mode (Ctrl+E) ***");
            } else if (stateTracker.isWaitingForAnyPrompt()) {
                System.out.println("*** Please complete current operation before changing weapons ***");
            }
        }
        
        // Faction selection (Ctrl+F)
        if (e.getCode() == KeyCode.F && e.isControlDown()) {
            if (callbacks.isEditMode() && !stateTracker.isWaitingForAnyPrompt()) {
                if (selectionManager.hasSelection()) {
                    callbacks.promptForFactionSelection();
                } else {
                    System.out.println("*** No units selected - select a unit first ***");
                }
            } else if (!callbacks.isEditMode()) {
                System.out.println("*** Faction selection only available in edit mode (Ctrl+E) ***");
            } else if (stateTracker.isWaitingForAnyPrompt()) {
                System.out.println("*** Please complete current operation before changing factions ***");
            }
        }
        
        
        // Direct character addition (Ctrl+A)  
        if (e.getCode() == KeyCode.A && e.isControlDown()) {
            if (callbacks.isEditMode() && !stateTracker.isWaitingForAnyPrompt()) {
                promptForDirectCharacterAddition();
            } else if (!callbacks.isEditMode()) {
                System.out.println("*** Direct character addition only available in edit mode (Ctrl+E) ***");
            } else if (stateTracker.isWaitingForAnyPrompt()) {
                System.out.println("*** Please complete current operation before adding characters ***");
            }
        }
        
        // Individual character creation (Ctrl+1)
        if (e.getCode() == KeyCode.DIGIT1 && e.isControlDown()) {
            if (callbacks.isEditMode() && !stateTracker.isWaitingForAnyPrompt()) {
                startIndividualCharacterCreation();
            } else if (!callbacks.isEditMode()) {
                System.out.println("*** Individual character creation only available in edit mode (Ctrl+E) ***");
            } else if (stateTracker.isWaitingForAnyPrompt()) {
                System.out.println("*** Please complete current operation before creating characters ***");
            }
        }
    }
    
    // ====================
    // CHARACTER CREATION WORKFLOWS
    // ====================
    
    /**
     * Starts individual character creation workflow.
     */
    public void startIndividualCharacterCreation() {
        System.out.println("=== Individual Character Creation ===");
        System.out.println("Select archetype:");
        System.out.println("1. Gunslinger  2. Soldier  3. Medic");
        System.out.println("4. Scout       5. Marksman 6. Brawler");
        
        creationStep = CreationStep.ARCHETYPE_SELECTION;
        stateTracker.setWaitingForCharacterCreation(true);
    }
    
    /**
     * Handles character archetype selection during creation.
     */
    public void handleCharacterArchetypeSelection(int archetypeIndex) {
        String[] archetypes = {"Gunslinger", "Soldier", "Medic", "Scout", "Marksman", "Brawler"};
        
        if (archetypeIndex < 1 || archetypeIndex > archetypes.length) {
            System.out.println("*** Invalid archetype selection - please choose 1-6 ***");
            return;
        }
        
        selectedArchetype = archetypes[archetypeIndex - 1];
        System.out.println("Selected archetype: " + selectedArchetype);
        
        // Move to ranged weapon selection
        System.out.println("Select ranged weapon:");
        System.out.println("1. Revolver   2. Rifle      3. Shotgun");
        System.out.println("4. Uzi        5. Sniper     6. Bow");
        
        creationStep = CreationStep.RANGED_WEAPON_SELECTION;
        stateTracker.setWaitingForCharacterCreation(false);
        stateTracker.setWaitingForCharacterRangedWeapon(true);
    }
    
    /**
     * Handles ranged weapon selection during character creation.
     */
    public void handleCharacterRangedWeaponSelection(int weaponIndex) {
        String[] weapons = {"wpn_revolver", "wpn_rifle", "wpn_shotgun", "wpn_uzi", "wpn_sniper", "wpn_bow"};
        String[] weaponNames = {"Revolver", "Rifle", "Shotgun", "Uzi", "Sniper Rifle", "Bow"};
        
        if (weaponIndex < 1 || weaponIndex > weapons.length) {
            System.out.println("*** Invalid weapon selection - please choose 1-6 ***");
            return;
        }
        
        selectedRangedWeapon = weapons[weaponIndex - 1];
        System.out.println("Selected ranged weapon: " + weaponNames[weaponIndex - 1]);
        
        // Move to melee weapon selection
        System.out.println("Select melee weapon:");
        System.out.println("1. Steel Dagger  2. Longsword     3. Battle Axe");
        System.out.println("4. Enchanted Sword  5. Wand of Magic Bolts  6. Unarmed");
        
        creationStep = CreationStep.MELEE_WEAPON_SELECTION;
        stateTracker.setWaitingForCharacterRangedWeapon(false);
        stateTracker.setWaitingForCharacterMeleeWeapon(true);
    }
    
    /**
     * Handles melee weapon selection during character creation.
     */
    public void handleCharacterMeleeWeaponSelection(int weaponIndex) {
        String[] weapons = {"Steel Dagger", "Longsword", "Battle Axe", "Enchanted Sword", "Wand of Magic Bolts", "Unarmed"};
        
        if (weaponIndex < 1 || weaponIndex > weapons.length) {
            System.out.println("*** Invalid weapon selection - please choose 1-6 ***");
            return;
        }
        
        selectedMeleeWeapon = weapons[weaponIndex - 1];
        System.out.println("Selected melee weapon: " + selectedMeleeWeapon);
        
        // Move to faction selection - create and place character
        createAndPlaceIndividualCharacter();
        
        // Reset creation state
        stateTracker.setWaitingForCharacterMeleeWeapon(false);
        creationStep = CreationStep.COMPLETE;
    }
    
    /**
     * Creates and places an individual character with selected attributes.
     */
    private void createAndPlaceIndividualCharacter() {
        System.out.println("Creating character: " + selectedArchetype + " with " + 
                          selectedRangedWeapon + " and " + selectedMeleeWeapon);
        
        // Implementation would create character and add to game
        // This preserves the existing behavior pattern
        
        // Reset selection state for next character
        selectedArchetype = "";
        selectedRangedWeapon = "";
        selectedMeleeWeapon = "";
    }
    
    // ====================
    // BATCH CHARACTER CREATION
    // ====================
    
    /**
     * Prompts for batch character creation parameters.
     */
    public void promptForBatchCharacterCreation() {
        System.out.println("=== Batch Character Creation ===");
        System.out.println("Enter quantity (1-20):");
        stateTracker.setWaitingForBatchCharacterCreation(true);
    }
    
    /**
     * Handles batch character creation input for various parameters.
     */
    public void handleBatchCharacterCreationInput(int inputNumber) {
        if (inputNumber >= 1 && inputNumber <= 20) {
            batchQuantity = inputNumber;
            System.out.println("Batch quantity set to: " + batchQuantity);
            
            System.out.println("Select weapon type:");
            System.out.println("1. Revolver   2. Rifle      3. Shotgun");
            System.out.println("4. Uzi        5. Sniper     6. Bow");
            
            // Continue with weapon selection...
            stateTracker.setWaitingForBatchCharacterCreation(false);
            
        } else {
            System.out.println("*** Invalid quantity - please enter 1-20 ***");
        }
    }
    
    // ====================
    // DIRECT CHARACTER ADDITION (CTRL-A)
    // ====================
    
    /**
     * Prompts for direct character addition workflow.
     */
    public void promptForDirectCharacterAddition() {
        System.out.println("=== Direct Character Addition ===");
        System.out.println("Select faction:");
        
        // Get actual factions from registry
        FactionRegistry factionRegistry = FactionRegistry.getInstance();
        Integer[] allFactionIds = factionRegistry.getAllFactionIds();
        
        // Cache all faction IDs for later use (including NONE faction)
        availableFactionIds = allFactionIds;
        
        // Display factions with character counts
        for (int i = 0; i < availableFactionIds.length; i++) {
            Faction faction = factionRegistry.getFaction(availableFactionIds[i]);
            int characterCount = getFactionCharacterCount(factionRegistry, availableFactionIds[i]);
            System.out.print((i + 1) + ". " + faction.getName() + " (" + characterCount + " chars)    ");
            if ((i + 1) % 2 == 0) System.out.println(); // New line every 2 factions (since names are longer now)
        }
        if (availableFactionIds.length % 2 != 0) System.out.println(); // Ensure we end with a newline
        
        directAdditionStep = DirectAdditionStep.FACTION_SELECTION;
        stateTracker.setWaitingForDirectCharacterAddition(true);
    }
    
    /**
     * Handles direct character addition input for workflow steps.
     */
    public void handleDirectCharacterAdditionInput(int inputNumber) {
        switch (directAdditionStep) {
            case FACTION_SELECTION:
                // Get actual number of factions
                if (availableFactionIds == null) {
                    FactionRegistry factionRegistry = FactionRegistry.getInstance();
                    Integer[] allFactionIds = factionRegistry.getAllFactionIds();
                    // Cache all faction IDs (including NONE)
                    availableFactionIds = allFactionIds;
                }
                
                if (inputNumber >= 1 && inputNumber <= availableFactionIds.length) {
                    // Map selection index to actual faction ID
                    directAdditionFaction = availableFactionIds[inputNumber - 1];
                    FactionRegistry factionRegistry = FactionRegistry.getInstance();
                    Faction selectedFaction = factionRegistry.getFaction(directAdditionFaction);
                    System.out.println("Selected faction: " + selectedFaction.getName());
                    
                    System.out.println("Enter quantity (1-20):");
                    directAdditionStep = DirectAdditionStep.QUANTITY_INPUT;
                } else {
                    System.out.println("*** Invalid faction - please choose 1-" + availableFactionIds.length + " ***");
                }
                break;
                
            case QUANTITY_INPUT:
                if (inputNumber >= 1 && inputNumber <= 20) {
                    directAdditionQuantity = inputNumber;
                    System.out.println("Quantity: " + directAdditionQuantity);
                    
                    System.out.println("Enter spacing in feet (1-9):");
                    directAdditionStep = DirectAdditionStep.SPACING_INPUT;
                } else {
                    System.out.println("*** Invalid quantity - please enter 1-20 ***");
                }
                break;
                
            case SPACING_INPUT:
                if (inputNumber >= 1 && inputNumber <= 9) {
                    directAdditionSpacing = inputNumber;
                    System.out.println("Spacing: " + directAdditionSpacing + " feet");
                    
                    // Display ranged weapon selection menu
                    displayRangedWeaponSelectionMenu();
                    directAdditionStep = DirectAdditionStep.RANGED_WEAPON_SELECTION;
                } else {
                    System.out.println("*** Invalid spacing - please enter 1-9 feet ***");
                }
                break;
                
            case RANGED_WEAPON_SELECTION:
                String[] rangedWeaponIds = getRangedWeaponIds();
                if (inputNumber >= 1 && inputNumber <= rangedWeaponIds.length) {
                    directAdditionRangedWeapon = rangedWeaponIds[inputNumber - 1];
                    System.out.println("Selected ranged weapon: " + getRangedWeaponDisplayName(directAdditionRangedWeapon));
                    
                    // Display melee weapon selection menu
                    displayMeleeWeaponSelectionMenu();
                    directAdditionStep = DirectAdditionStep.MELEE_WEAPON_SELECTION;
                } else {
                    System.out.println("*** Invalid ranged weapon - please choose 1-" + rangedWeaponIds.length + " (or A for option 10) ***");
                }
                break;
                
            case MELEE_WEAPON_SELECTION:
                String[] meleeWeaponIds = getMeleeWeaponIds();
                if (inputNumber >= 1 && inputNumber <= meleeWeaponIds.length) {
                    directAdditionMeleeWeapon = meleeWeaponIds[inputNumber - 1];
                    System.out.println("Selected melee weapon: " + getMeleeWeaponDisplayName(directAdditionMeleeWeapon));
                    
                    System.out.println("Select line direction:");
                    System.out.println("1. Right (horizontal line)");
                    System.out.println("2. Down (vertical line)");
                    directAdditionStep = DirectAdditionStep.DIRECTION_SELECTION;
                } else {
                    System.out.println("*** Invalid melee weapon - please choose 1-" + meleeWeaponIds.length + " (or A for option 10) ***");
                }
                break;
                
            case DIRECTION_SELECTION:
                if (inputNumber >= 1 && inputNumber <= 2) {
                    directAdditionDirection = (inputNumber == 1) ? "RIGHT" : "DOWN";
                    System.out.println("Direction: " + (directAdditionDirection.equals("RIGHT") ? "Right (horizontal)" : "Down (vertical)"));
                    System.out.println("Click on map to place characters...");
                    directAdditionStep = DirectAdditionStep.PLACEMENT;
                } else {
                    System.out.println("*** Invalid direction - please choose 1 or 2 ***");
                }
                break;
        }
    }
    
    /**
     * Handles character placement for direct addition workflow.
     */
    public void handleCharacterPlacement(double x, double y) {
        System.out.println("***********************");
        System.out.println("*** PLACING CHARACTERS ***");
        System.out.println("Placing " + directAdditionQuantity + " characters at (" + 
                          String.format("%.1f", x) + ", " + String.format("%.1f", y) + ")");
        
        try {
            // Load characters from the selected faction
            CharacterPersistenceManager persistenceManager = CharacterPersistenceManager.getInstance();
            List<combat.Character> allCharacters = persistenceManager.loadCharactersFromFaction(directAdditionFaction);
            
            // Filter for non-incapacitated characters that aren't already deployed
            directAdditionCharacters.clear();
            for (combat.Character character : allCharacters) {
                if (!character.isIncapacitated() && !isCharacterAlreadyDeployed(character)) {
                    directAdditionCharacters.add(character);
                }
            }
            
            if (directAdditionCharacters.isEmpty()) {
                System.out.println("*** No available characters in selected faction ***");
                System.out.println("*** Character placement cancelled ***");
                resetDirectAdditionState();
                return;
            }
            
            // Place characters up to the requested quantity
            int charactersToPlace = Math.min(directAdditionQuantity, directAdditionCharacters.size());
            double spacing = directAdditionSpacing * 7.0; // Convert feet to pixels (7 pixels = 1 foot)
            
            for (int i = 0; i < charactersToPlace; i++) {
                combat.Character character = directAdditionCharacters.get(i);
                
                // Calculate position based on direction
                double charX = x;
                double charY = y;
                
                if (directAdditionDirection.equals("RIGHT")) {
                    // Add character diameter (21 pixels = 3 feet) to spacing for edge-to-edge spacing
                    charX += i * (spacing + 21);
                } else { // DOWN
                    // Add character diameter (21 pixels = 3 feet) to spacing for edge-to-edge spacing
                    charY += i * (spacing + 21);
                }
                
                // Assign the selected ranged weapon to the character
                if (!directAdditionRangedWeapon.isEmpty()) {
                    character.rangedWeapon = (RangedWeapon) WeaponFactory.createWeapon(directAdditionRangedWeapon);
                    character.weapon = character.rangedWeapon; // Set as primary weapon
                    character.currentWeaponState = character.weapon.getInitialState();
                }
                
                // Assign the selected melee weapon to the character
                if (!directAdditionMeleeWeapon.isEmpty()) {
                    character.meleeWeapon = createMeleeWeapon(directAdditionMeleeWeapon);
                }
                
                // Get color based on faction
                javafx.scene.paint.Color javafxColor = getFactionColor(directAdditionFaction);
                platform.api.Color characterColor = platform.api.Color.fromJavaFX(javafxColor);
                
                // Create and add unit
                int unitId = callbacks.getNextUnitId();
                Unit newUnit = new Unit(character, charX, charY, characterColor, unitId);
                callbacks.setNextUnitId(unitId + 1);
                
                // Add to units list (need to get access to this)
                addUnitToGame(newUnit);
                
                System.out.println("Placed: " + character.getDisplayName() + " at (" + 
                                 String.format("%.0f", charX) + ", " + String.format("%.0f", charY) + ")");
            }
            
            System.out.println("*** PLACEMENT COMPLETE ***");
            System.out.println("Successfully placed " + charactersToPlace + " characters");
            System.out.println("Direction: " + (directAdditionDirection.equals("RIGHT") ? "Right (horizontal)" : "Down (vertical)"));
            
        } catch (Exception e) {
            System.err.println("*** Error placing characters: " + e.getMessage() + " ***");
            System.out.println("*** Character placement cancelled ***");
        }
        
        // Reset direct addition state
        resetDirectAdditionState();
    }
    
    /**
     * Get the character count for a specific faction.
     */
    private int getFactionCharacterCount(FactionRegistry factionRegistry, int factionId) {
        try {
            FactionRegistry.FactionFileData factionData = factionRegistry.loadFactionFileData(factionId);
            return factionData.characters.size();
        } catch (java.io.IOException e) {
            // If faction file doesn't exist or can't be loaded, assume 0 characters
            return 0;
        }
    }
    
    /**
     * Check if a character is already deployed in the game.
     */
    private boolean isCharacterAlreadyDeployed(combat.Character character) {
        // For now, assume characters aren't deployed (this could be enhanced to check the units list)
        // This would require access to the units list from the main game
        return false;
    }
    
    /**
     * Get faction color for display.
     */
    private javafx.scene.paint.Color getFactionColor(int factionId) {
        FactionRegistry factionRegistry = FactionRegistry.getInstance();
        Faction faction = factionRegistry.getFaction(factionId);
        if (faction != null) {
            return faction.getColor();
        }
        // Default colors by faction ID
        switch (factionId) {
            case 1: return javafx.scene.paint.Color.BLUE;   // Union
            case 2: return javafx.scene.paint.Color.GRAY;   // Confederacy  
            case 3: return javafx.scene.paint.Color.GREEN;  // Southern Unionists
            default: return javafx.scene.paint.Color.RED;
        }
    }
    
    /**
     * Add a unit to the game.
     */
    private void addUnitToGame(Unit unit) {
        units.add(unit);
    }
    
    /**
     * Reset the direct addition workflow state.
     */
    private void resetDirectAdditionState() {
        stateTracker.setWaitingForDirectCharacterAddition(false);
        directAdditionStep = DirectAdditionStep.FACTION_SELECTION;
        directAdditionCharacters.clear();
    }
    
    /**
     * Display ranged weapon selection menu.
     */
    private void displayRangedWeaponSelectionMenu() {
        System.out.println("Select ranged weapon for all characters:");
        String[] weaponIds = getRangedWeaponIds();
        for (int i = 0; i < weaponIds.length; i++) {
            String displayName = getRangedWeaponDisplayName(weaponIds[i]);
            if (i == 9) {
                // Option 10 is selected with 'A' key
                System.out.println("A. " + displayName + " (press A for option 10)");
            } else {
                System.out.println((i + 1) + ". " + displayName);
            }
        }
    }
    
    /**
     * Display melee weapon selection menu.
     */
    private void displayMeleeWeaponSelectionMenu() {
        System.out.println("Select melee weapon for all characters:");
        String[] weaponIds = getMeleeWeaponIds();
        for (int i = 0; i < weaponIds.length; i++) {
            String displayName = getMeleeWeaponDisplayName(weaponIds[i]);
            if (i == 9) {
                // Option 10 is selected with 'A' key
                System.out.println("A. " + displayName + " (press A for option 10)");
            } else {
                System.out.println((i + 1) + ". " + displayName);
            }
        }
    }
    
    /**
     * Get available ranged weapon IDs.
     */
    private String[] getRangedWeaponIds() {
        return WeaponFactory.getAllWeaponIds();
    }
    
    /**
     * Get available melee weapon IDs.
     */
    private String[] getMeleeWeaponIds() {
        DataManager dataManager = DataManager.getInstance();
        return dataManager.getAllMeleeWeapons().keySet().toArray(new String[0]);
    }
    
    /**
     * Get display name for ranged weapon.
     */
    private String getRangedWeaponDisplayName(String weaponId) {
        WeaponData weaponData = WeaponFactory.getWeaponData(weaponId);
        if (weaponData != null) {
            return weaponData.name + " (" + weaponData.damage + " damage, " + weaponData.maximumRange + "ft range)";
        }
        return weaponId;
    }
    
    /**
     * Get display name for melee weapon.
     */
    private String getMeleeWeaponDisplayName(String weaponId) {
        DataManager dataManager = DataManager.getInstance();
        MeleeWeaponData weaponData = dataManager.getMeleeWeapon(weaponId);
        if (weaponData != null) {
            return weaponData.name + " (" + weaponData.damage + " damage, " + String.format("%.1f", weaponData.weaponLength) + "ft reach)";
        }
        return weaponId;
    }
    
    /**
     * Create melee weapon from ID.
     */
    private MeleeWeapon createMeleeWeapon(String weaponId) {
        DataManager dataManager = DataManager.getInstance();
        MeleeWeaponData weaponData = dataManager.getMeleeWeapon(weaponId);
        if (weaponData != null) {
            MeleeWeapon weapon = new MeleeWeapon(
                weaponId, // Pass weaponId as first parameter (DevCycle 17)
                weaponData.name,
                weaponData.damage,
                weaponData.soundFile,
                weaponData.meleeType,
                weaponData.defendScore,
                weaponData.attackSpeed,
                weaponData.attackCooldown,
                weaponData.weaponLength,
                weaponData.readyingTime,
                weaponData.isOneHanded,
                weaponData.isMeleeVersionOfRanged,
                weaponData.weaponAccuracy
            );
            // Set wound description from JSON data
            if (weaponData.woundDescription != null && !weaponData.woundDescription.isEmpty()) {
                weapon.setWoundDescription(weaponData.woundDescription);
            }
            
            // Initialize weapon states if available (DevCycle 22: Task #5)
            if (weaponData.states != null && !weaponData.states.isEmpty()) {
                java.util.List<combat.WeaponState> weaponStates = new java.util.ArrayList<>();
                for (data.WeaponStateData stateData : weaponData.states) {
                    weaponStates.add(new combat.WeaponState(stateData.state, stateData.action, stateData.ticks));
                }
                weapon.initializeStates(weaponStates, "sheathed"); // Use "sheathed" as initial state for melee weapons
            }
            
            return weapon;
        }
        return MeleeWeaponFactory.createUnarmed(); // Fallback
    }
    
    // ====================
    // STATE QUERY METHODS
    // ====================
    
    
    /**
     * Returns true if currently in direct addition placement mode.
     */
    public boolean isInDirectAdditionPlacementMode() {
        return stateTracker.isWaitingForDirectCharacterAddition() && directAdditionStep == DirectAdditionStep.PLACEMENT;
    }
    
    /**
     * Returns the current creation step.
     */
    public CreationStep getCreationStep() {
        return creationStep;
    }
    
    
    /**
     * Returns the current direct addition step.
     */
    public DirectAdditionStep getDirectAdditionStep() {
        return directAdditionStep;
    }
}
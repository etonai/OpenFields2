package input.controllers;

import input.interfaces.InputManagerCallbacks;
import input.states.InputStates;
import combat.Character;
import game.Unit;
import data.CharacterPersistenceManager;
import data.WeaponFactory;

import java.util.List;
import java.util.ArrayList;

/**
 * Controller for character deployment workflow operations.
 * Handles the complete deployment process from faction selection through character placement.
 */
public class DeploymentController {
    // Dependencies
    private final InputManagerCallbacks callbacks;
    private final List<Unit> units;
    
    // Deployment workflow state
    private int deploymentFaction = 0;
    private int deploymentQuantity = 0;
    private String deploymentWeapon = "";
    private String deploymentFormation = "";
    private int deploymentSpacing = 35; // Default 5 feet in pixels (7 pixels = 1 foot)
    private InputStates.DeploymentStep deploymentStep = InputStates.DeploymentStep.FACTION;
    private List<Character> deploymentCharacters = new ArrayList<>();
    
    /**
     * Constructor for DeploymentController
     * 
     * @param callbacks Interface for InputManager operations
     * @param units List of game units to add deployed characters to
     */
    public DeploymentController(InputManagerCallbacks callbacks, List<Unit> units) {
        this.callbacks = callbacks;
        this.units = units;
    }
    
    /**
     * Start the character deployment workflow
     */
    public void promptForCharacterDeployment() {
        callbacks.setWaitingForCharacterDeployment(true);
        deploymentStep = InputStates.DeploymentStep.FACTION;
        deploymentFaction = 0;
        deploymentQuantity = 0;
        deploymentWeapon = "";
        deploymentFormation = "";
        deploymentSpacing = 35; // Default 5 feet
        deploymentCharacters.clear();
        
        System.out.println("***********************");
        System.out.println("*** CHARACTER DEPLOYMENT ***");
        System.out.println("Select faction to deploy from:");
        System.out.println("1. NONE - No faction");
        System.out.println("2. Union - Federal forces");
        System.out.println("3. Confederacy - Confederate forces");
        System.out.println("4. Southern Unionists - Pro-Union Southerners");
        System.out.println("0. Cancel deployment");
        System.out.println();
        System.out.println("Enter selection (1-4, 0 to cancel): ");
    }
    
    /**
     * Handle input during character deployment workflow
     * 
     * @param inputNumber The number entered by the user
     */
    public void handleCharacterDeploymentInput(int inputNumber) {
        switch (deploymentStep) {
            case FACTION:
                if (inputNumber == 0) {
                    System.out.println("*** Character deployment cancelled ***");
                    cancelCharacterDeployment();
                } else if (inputNumber >= 1 && inputNumber <= 4) {
                    deploymentFaction = inputNumber - 1; // Convert to 0-based faction ID
                    loadDeploymentCharacters();
                } else {
                    System.out.println("*** Invalid faction selection. Use 1-4 or 0 to cancel ***");
                }
                break;
                
            case QUANTITY:
                if (inputNumber == 0) {
                    System.out.println("*** Character deployment cancelled ***");
                    cancelCharacterDeployment();
                } else if (inputNumber >= 1 && inputNumber <= 20) {
                    if (inputNumber <= deploymentCharacters.size()) {
                        deploymentQuantity = inputNumber;
                        deploymentStep = InputStates.DeploymentStep.WEAPON;
                        showWeaponSelectionForDeployment();
                    } else {
                        System.out.println("*** Not enough characters available. Maximum: " + deploymentCharacters.size() + " ***");
                    }
                } else {
                    System.out.println("*** Invalid quantity. Use 1-" + Math.min(20, deploymentCharacters.size()) + " or 0 to cancel ***");
                }
                break;
                
            case WEAPON:
                if (inputNumber == 0) {
                    System.out.println("*** Character deployment cancelled ***");
                    cancelCharacterDeployment();
                } else if (inputNumber >= 1 && inputNumber <= getWeaponOptionsCount()) {
                    deploymentWeapon = getWeaponIdByIndex(inputNumber);
                    deploymentStep = InputStates.DeploymentStep.FORMATION;
                    showFormationSelection();
                } else {
                    System.out.println("*** Invalid weapon selection. Use 1-" + getWeaponOptionsCount() + " or 0 to cancel ***");
                }
                break;
                
            case FORMATION:
                if (inputNumber == 0) {
                    System.out.println("*** Character deployment cancelled ***");
                    cancelCharacterDeployment();
                } else if (inputNumber >= 1 && inputNumber <= 2) {
                    deploymentFormation = (inputNumber == 1) ? "line_right" : "line_down";
                    deploymentStep = InputStates.DeploymentStep.SPACING;
                    showSpacingSelection();
                } else {
                    System.out.println("*** Invalid formation selection. Use 1-2 or 0 to cancel ***");
                }
                break;
                
            case SPACING:
                if (inputNumber == 0) {
                    System.out.println("*** Character deployment cancelled ***");
                    cancelCharacterDeployment();
                } else if (inputNumber >= 1 && inputNumber <= 9) {
                    deploymentSpacing = inputNumber * 7; // Convert feet to pixels (7 pixels = 1 foot)
                    deploymentStep = InputStates.DeploymentStep.PLACEMENT;
                    showPlacementInstructions();
                } else {
                    System.out.println("*** Invalid spacing. Use 1-9 feet or 0 to cancel ***");
                }
                break;
        }
    }
    
    /**
     * Load available characters from the selected faction
     */
    private void loadDeploymentCharacters() {
        try {
            CharacterPersistenceManager persistenceManager = CharacterPersistenceManager.getInstance();
            List<Character> allCharacters = persistenceManager.loadCharactersFromFaction(deploymentFaction);

            System.out.println("ETONAI Debug: Reached this line");
            // Filter for non-incapacitated characters
            deploymentCharacters.clear();
            for (Character character : allCharacters) {
                if (!character.isIncapacitated()) {
                    deploymentCharacters.add(character);
                }
            }
            
            if (deploymentCharacters.isEmpty()) {
                System.out.println("*** No available characters in " + getFactionName(deploymentFaction + 1) + " faction ***");
                System.out.println("*** Character deployment cancelled ***");
                cancelCharacterDeployment();
            } else {
                deploymentStep = InputStates.DeploymentStep.QUANTITY;
                showCharacterQuantitySelection();
            }
        } catch (Exception e) {
            System.err.println("*** Error loading characters: " + e.getMessage() + " ***");
            System.out.println("*** Character deployment cancelled ***");
            cancelCharacterDeployment();
        }
    }
    
    /**
     * Show character quantity selection prompt
     */
    private void showCharacterQuantitySelection() {
        System.out.println("***********************");
        System.out.println("*** CHARACTER QUANTITY ***");
        System.out.println("Faction: " + getFactionName(deploymentFaction + 1));
        System.out.println("Available characters: " + deploymentCharacters.size());
        System.out.println();
        
        // Show first few characters for reference
        int showCount = Math.min(5, deploymentCharacters.size());
        System.out.println("Available characters (showing first " + showCount + "):");
        for (int i = 0; i < showCount; i++) {
            Character character = deploymentCharacters.get(i);
            System.out.println("  " + character.getDisplayName() + " (ID: " + character.id + 
                             ", Health: " + character.currentHealth + "/" + character.health + ")");
        }
        if (deploymentCharacters.size() > showCount) {
            System.out.println("  ... and " + (deploymentCharacters.size() - showCount) + " more");
        }
        
        System.out.println();
        System.out.println("How many characters do you want to deploy?");
        System.out.println("Enter quantity (1-" + Math.min(20, deploymentCharacters.size()) + ", 0 to cancel): ");
    }
    
    /**
     * Show weapon selection for deployment
     */
    private void showWeaponSelectionForDeployment() {
        System.out.println("***********************");
        System.out.println("*** WEAPON SELECTION ***");
        System.out.println("Deploying " + deploymentQuantity + " characters from " + getFactionName(deploymentFaction + 1));
        System.out.println("Select weapon for all deployed characters:");
        System.out.println("1. Colt Peacemaker (Pistol) - 6 damage, 150 feet range");
        System.out.println("2. Hunting Rifle (Rifle) - 12 damage, 400 feet range");
        System.out.println("3. Brown Bess Musket (Rifle) - 15 damage, 300 feet range");
        System.out.println("4. Derringer (Pistol) - 4 damage, 50 feet range");
        System.out.println("0. Cancel deployment");
        System.out.println();
        System.out.println("Enter selection (1-4, 0 to cancel): ");
    }
    
    /**
     * Show formation selection
     */
    private void showFormationSelection() {
        System.out.println("***********************");
        System.out.println("*** FORMATION SELECTION ***");
        System.out.println("Deploying " + deploymentQuantity + " characters");
        System.out.println("Weapon: " + getWeaponDisplayName(deploymentWeapon));
        System.out.println("Select formation:");
        System.out.println("1. Line Right - Characters arranged horizontally");
        System.out.println("2. Line Down - Characters arranged vertically");
        System.out.println("0. Cancel deployment");
        System.out.println();
        System.out.println("Enter selection (1-2, 0 to cancel): ");
    }
    
    /**
     * Show spacing selection
     */
    private void showSpacingSelection() {
        System.out.println("***********************");
        System.out.println("*** SPACING SELECTION ***");
        System.out.println("Deploying " + deploymentQuantity + " characters");
        System.out.println("Formation: " + (deploymentFormation.equals("line_right") ? "Line Right" : "Line Down"));
        System.out.println("Select spacing between characters (edge-to-edge):");
        System.out.println("1. 1 foot - Very tight formation (touching)");
        System.out.println("2. 2 feet - Tight formation");
        System.out.println("3. 3 feet - Normal formation");
        System.out.println("4. 4 feet - Loose formation");
        System.out.println("5. 5 feet - Very loose formation (recommended)");
        System.out.println("6. 6 feet - Extended formation");
        System.out.println("7. 7 feet - Wide formation");
        System.out.println("8. 8 feet - Very wide formation");
        System.out.println("9. 9 feet - Maximum spacing");
        System.out.println("0. Cancel deployment");
        System.out.println();
        System.out.println("Enter selection (1-9, 0 to cancel): ");
    }
    
    /**
     * Show placement instructions
     */
    private void showPlacementInstructions() {
        System.out.println("***********************");
        System.out.println("*** PLACEMENT MODE ***");
        System.out.println("Deploying " + deploymentQuantity + " characters");
        System.out.println("Formation: " + (deploymentFormation.equals("line_right") ? "Line Right" : "Line Down"));
        System.out.println("Spacing: " + (deploymentSpacing / 7) + " feet edge-to-edge (" + deploymentSpacing + " pixels)");
        System.out.println("Weapon: " + getWeaponDisplayName(deploymentWeapon));
        System.out.println();
        System.out.println("Click on the battlefield to place the formation.");
        System.out.println("The first character will be placed at the click location.");
        System.out.println("Press ESC to cancel deployment.");
        System.out.println("***********************");
    }
    
    /**
     * Cancel character deployment and reset state
     */
    public void cancelCharacterDeployment() {
        callbacks.setWaitingForCharacterDeployment(false);
        deploymentStep = InputStates.DeploymentStep.FACTION;
        deploymentFaction = 0;
        deploymentQuantity = 0;
        deploymentWeapon = "";
        deploymentFormation = "";
        deploymentSpacing = 35;
        deploymentCharacters.clear();
    }
    
    /**
     * Check if we're in deployment placement mode
     */
    public boolean isInDeploymentPlacementMode() {
        return callbacks.isWaitingForCharacterDeployment() && deploymentStep == InputStates.DeploymentStep.PLACEMENT;
    }
    
    /**
     * Handle deployment click placement
     */
    public void handleDeploymentPlacement(double worldX, double worldY) {
        if (!isInDeploymentPlacementMode()) {
            return;
        }
        
        try {
            System.out.println("***********************");
            System.out.println("*** DEPLOYING CHARACTERS ***");
            
            // Deploy characters in formation
            for (int i = 0; i < deploymentQuantity; i++) {
                if (i >= deploymentCharacters.size()) {
                    break; // Safety check
                }
                
                Character character = deploymentCharacters.get(i);
                
                // Calculate position based on formation
                double charX = worldX;
                double charY = worldY;
                
                if (deploymentFormation.equals("line_right")) {
                    // Add character diameter (21 pixels = 3 feet) to spacing for edge-to-edge spacing
                    charX += i * (deploymentSpacing + 21);
                } else { // line_down
                    // Add character diameter (21 pixels = 3 feet) to spacing for edge-to-edge spacing
                    charY += i * (deploymentSpacing + 21);
                }
                
                // Assign weapon
                character.weapon = WeaponFactory.createWeapon(deploymentWeapon);
                character.currentWeaponState = character.weapon.getInitialState();
                
                // Get color based on faction
                javafx.scene.paint.Color javafxColor = getFactionColor(deploymentFaction);
                platform.api.Color characterColor = platform.api.Color.fromJavaFX(javafxColor);
                
                // Create and add unit
                int unitId = callbacks.getNextUnitId();
                Unit newUnit = new Unit(character, charX, charY, characterColor, unitId);
                callbacks.setNextUnitId(unitId + 1);
                units.add(newUnit);
                
                System.out.println("Deployed: " + character.getDisplayName() + " at (" + 
                                 String.format("%.0f", charX) + ", " + String.format("%.0f", charY) + ")");
            }
            
            System.out.println("*** DEPLOYMENT COMPLETE ***");
            System.out.println("Successfully deployed " + deploymentQuantity + " characters from " + 
                             getFactionName(deploymentFaction + 1) + " faction");
            System.out.println("Formation: " + (deploymentFormation.equals("line_right") ? "Line Right" : "Line Down"));
            System.out.println("Spacing: " + (deploymentSpacing / 7) + " feet edge-to-edge");
            System.out.println("Weapon: " + getWeaponDisplayName(deploymentWeapon));
            System.out.println("***********************");
            
            // Reset deployment state
            cancelCharacterDeployment();
            
        } catch (Exception e) {
            System.err.println("*** Error during deployment: " + e.getMessage() + " ***");
            cancelCharacterDeployment();
        }
    }
    
    // Helper methods
    
    /**
     * Get weapon options count for deployment
     */
    private int getWeaponOptionsCount() {
        return 4; // Colt Peacemaker, Hunting Rifle, Brown Bess, Derringer
    }
    
    /**
     * Get weapon ID by selection index
     */
    private String getWeaponIdByIndex(int index) {
        switch (index) {
            case 1: return "wpn_colt_peacemaker";
            case 2: return "wpn_hunting_rifle";
            case 3: return "wpn_brown_bess";
            case 4: return "wpn_derringer";
            default: return "wpn_colt_peacemaker";
        }
    }
    
    /**
     * Get display name for weapon ID
     */
    private String getWeaponDisplayName(String weaponId) {
        switch (weaponId) {
            case "wpn_colt_peacemaker": return "Colt Peacemaker (Pistol)";
            case "wpn_hunting_rifle": return "Hunting Rifle (Rifle)";
            case "wpn_brown_bess": return "Brown Bess Musket (Rifle)";
            case "wpn_derringer": return "Derringer (Pistol)";
            default: return "Unknown Weapon";
        }
    }
    
    /**
     * Get faction name by faction number (1-based)
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
    
    /**
     * Get faction color for character display
     */
    private javafx.scene.paint.Color getFactionColor(int factionId) {
        switch (factionId) {
            case 0: return javafx.scene.paint.Color.GRAY;     // NONE
            case 1: return javafx.scene.paint.Color.BLUE;     // Union
            case 2: return javafx.scene.paint.Color.DARKGRAY; // Confederacy
            case 3: return javafx.scene.paint.Color.LIGHTBLUE; // Southern Unionists
            default: return javafx.scene.paint.Color.CYAN;
        }
    }
}
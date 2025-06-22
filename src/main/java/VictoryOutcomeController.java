import input.interfaces.InputManagerCallbacks;
import input.states.InputStates;
import game.Unit;
import combat.Character;
import data.CharacterPersistenceManager;
import data.FactionRegistry;
import data.Faction;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * Controller for victory outcome processing workflows.
 * Handles the complete victory processing from faction outcome selection through scenario completion.
 */
public class VictoryOutcomeController {
    // Dependencies
    private final InputManagerCallbacks callbacks;
    private final List<Unit> units;
    private final SelectionManager selectionManager;
    private final PriorityQueue<game.ScheduledEvent> eventQueue;
    
    // Manual Victory Workflow State
    /** List of faction IDs participating in manual victory determination */
    private List<Integer> scenarioFactions = new ArrayList<>();
    
    /** Map of faction ID to victory outcome for manual victory processing */
    private Map<Integer, InputStates.VictoryOutcome> factionOutcomes = new HashMap<>();
    
    /** Current faction index being processed in manual victory workflow */
    private int currentVictoryFactionIndex = 0;
    
    /**
     * Constructor for VictoryOutcomeController
     * 
     * @param callbacks Interface for InputManager operations
     * @param units List of game units
     * @param selectionManager Selection manager for clearing selections
     * @param eventQueue Event queue for clearing events
     */
    public VictoryOutcomeController(InputManagerCallbacks callbacks, List<Unit> units, 
                                  SelectionManager selectionManager, PriorityQueue<game.ScheduledEvent> eventQueue) {
        this.callbacks = callbacks;
        this.units = units;
        this.selectionManager = selectionManager;
        this.eventQueue = eventQueue;
    }
    
    /**
     * Start the next faction outcome selection in the victory workflow
     */
    public void promptForNextFactionOutcome() {
        if (currentVictoryFactionIndex >= scenarioFactions.size()) {
            // All factions processed, execute victory
            executeManualVictory();
            return;
        }
        
        callbacks.setWaitingForVictoryOutcome(true);
        int currentFactionId = scenarioFactions.get(currentVictoryFactionIndex);
        
        System.out.println("***********************");
        System.out.println("*** FACTION OUTCOME: " + getFactionName(currentFactionId + 1) + " ***");
        
        // Show characters in this faction
        List<Unit> factionUnits = new ArrayList<>();
        for (Unit unit : units) {
            if (unit.character.getFaction() == currentFactionId) {
                factionUnits.add(unit);
            }
        }
        
        System.out.println("Characters in faction (" + factionUnits.size() + " total):");
        for (Unit unit : factionUnits) {
            String status = unit.character.isIncapacitated() ? "INCAPACITATED" : "Active";
            System.out.println("  " + unit.character.getDisplayName() + " (" + status + 
                             ", Health: " + unit.character.currentHealth + "/" + unit.character.health + ")");
        }
        
        System.out.println();
        System.out.println("Select outcome for " + getFactionName(currentFactionId + 1) + ":");
        System.out.println("1. Victory - Faction achieved victory");
        System.out.println("2. Defeat - Faction was defeated");
        System.out.println("3. Participant - Faction participated but neither won nor lost");
        System.out.println("0. Cancel manual victory");
        System.out.println();
        System.out.println("Enter selection (1-3, 0 to cancel): ");
    }
    
    /**
     * Handle input for victory outcome selection
     * 
     * @param outcomeNumber The number entered by the user
     */
    public void handleVictoryOutcomeInput(int outcomeNumber) {
        if (outcomeNumber == 0) {
            System.out.println("*** Manual victory cancelled ***");
            cancelManualVictory();
            return;
        }
        
        if (outcomeNumber < 1 || outcomeNumber > 3) {
            System.out.println("*** Invalid selection. Use 1-3 or 0 to cancel ***");
            return;
        }
        
        // Store the outcome for the current faction
        int currentFactionId = scenarioFactions.get(currentVictoryFactionIndex);
        InputStates.VictoryOutcome outcome;
        
        switch (outcomeNumber) {
            case 1: outcome = InputStates.VictoryOutcome.VICTORY; break;
            case 2: outcome = InputStates.VictoryOutcome.DEFEAT; break;
            case 3: outcome = InputStates.VictoryOutcome.PARTICIPANT; break;
            default: return; // Should never happen
        }
        
        factionOutcomes.put(currentFactionId, outcome);
        
        String outcomeName = getOutcomeName(outcome);
        System.out.println("*** " + getFactionName(currentFactionId + 1) + " outcome set to: " + outcomeName + " ***");
        
        // Move to next faction
        currentVictoryFactionIndex++;
        callbacks.setWaitingForVictoryOutcome(false);
        
        // Continue with next faction or execute victory
        promptForNextFactionOutcome();
    }
    
    /**
     * Execute the manual victory and update all faction and character data
     */
    private void executeManualVictory() {
        System.out.println("***********************");
        System.out.println("*** EXECUTING MANUAL VICTORY ***");
        System.out.println("Processing outcomes for " + scenarioFactions.size() + " factions...");
        
        try {
            CharacterPersistenceManager persistenceManager = CharacterPersistenceManager.getInstance();
            FactionRegistry factionRegistry = FactionRegistry.getInstance();
            
            // Update faction statistics and character outcomes
            for (Integer factionId : scenarioFactions) {
                InputStates.VictoryOutcome outcome = factionOutcomes.get(factionId);
                String outcomeName = getOutcomeName(outcome);
                
                System.out.println("Processing " + getFactionName(factionId + 1) + " (" + outcomeName + ")...");
                
                // Update faction statistics
                try {
                    Faction faction = factionRegistry.getFaction(factionId);
                    if (faction != null) {
                        switch (outcome) {
                            case VICTORY:
                                faction.incrementVictories();
                                break;
                            case DEFEAT:
                                faction.incrementDefeats();
                                break;
                            case PARTICIPANT:
                                faction.incrementParticipations();
                                break;
                        }
                        factionRegistry.saveFactionFile(faction);
                    }
                } catch (Exception e) {
                    System.err.println("  Failed to update faction statistics: " + e.getMessage());
                }
                
                // Update character statistics for all characters in this faction in the scenario
                List<Unit> factionUnits = new ArrayList<>();
                for (Unit unit : units) {
                    if (unit.character.getFaction() == factionId) {
                        factionUnits.add(unit);
                    }
                }
                
                for (Unit unit : factionUnits) {
                    try {
                        Character character = unit.character;
                        
                        // Update battle participation
                        character.battlesParticipated++;
                        
                        // Update victory/defeat counts based on faction outcome
                        switch (outcome) {
                            case VICTORY:
                                character.victories++;
                                break;
                            case DEFEAT:
                                character.defeats++;
                                break;
                            case PARTICIPANT:
                                // No additional stat changes for participants
                                break;
                        }
                        
                        // Save character data back to faction file
                        persistenceManager.saveCharacter(character);
                        
                        System.out.println("  Updated: " + character.getDisplayName() + 
                                         " (Battles: " + character.battlesParticipated + 
                                         ", Victories: " + character.victories + 
                                         ", Defeats: " + character.defeats + ")");
                        
                    } catch (Exception e) {
                        System.err.println("  Failed to update character " + unit.character.getDisplayName() + ": " + e.getMessage());
                    }
                }
            }
            
            System.out.println("*** VICTORY PROCESSING COMPLETE ***");
            
            // Display summary
            displayVictorySummary();
            
            // End scenario
            endScenario();
            
        } catch (Exception e) {
            System.err.println("*** Error during victory processing: " + e.getMessage() + " ***");
            cancelManualVictory();
        }
    }
    
    /**
     * Display victory summary
     */
    private void displayVictorySummary() {
        System.out.println("***********************");
        System.out.println("*** BATTLE SUMMARY ***");
        
        for (Integer factionId : scenarioFactions) {
            InputStates.VictoryOutcome outcome = factionOutcomes.get(factionId);
            String outcomeName = getOutcomeName(outcome);
            
            int characterCount = 0;
            int incapacitatedCount = 0;
            for (Unit unit : units) {
                if (unit.character.getFaction() == factionId) {
                    characterCount++;
                    if (unit.character.isIncapacitated()) {
                        incapacitatedCount++;
                    }
                }
            }
            
            System.out.println(getFactionName(factionId + 1) + ": " + outcomeName);
            System.out.println("  Characters: " + characterCount + " total, " + 
                             incapacitatedCount + " incapacitated, " + 
                             (characterCount - incapacitatedCount) + " active");
        }
        
        System.out.println("***********************");
    }
    
    /**
     * End the current scenario after victory processing
     */
    private void endScenario() {
        System.out.println("***********************");
        System.out.println("*** SCENARIO ENDED ***");
        System.out.println("All units cleared from battlefield.");
        System.out.println("Character and faction data saved to files.");
        System.out.println("Ready for new scenario or character operations.");
        System.out.println("***********************");
        
        // Clear all units from the scenario
        units.clear();
        
        // Clear any selections
        selectionManager.clearSelection();
        
        // Clear event queue
        eventQueue.clear();
        
        // Reset victory state
        cancelManualVictory();
    }
    
    /**
     * Cancel manual victory and reset state
     */
    public void cancelManualVictory() {
        callbacks.setWaitingForVictoryOutcome(false);
        scenarioFactions.clear();
        factionOutcomes.clear();
        currentVictoryFactionIndex = 0;
    }
    
    /**
     * Get display name for victory outcome
     * 
     * @param outcome The victory outcome
     * @return The display name
     */
    private String getOutcomeName(InputStates.VictoryOutcome outcome) {
        switch (outcome) {
            case VICTORY: return "VICTORY";
            case DEFEAT: return "DEFEAT";
            case PARTICIPANT: return "PARTICIPANT";
            default: return "UNKNOWN";
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
     * Set scenario factions for victory processing
     * Used by external systems to initialize the victory workflow
     * 
     * @param factions List of faction IDs participating in the scenario
     */
    public void setScenarioFactions(List<Integer> factions) {
        this.scenarioFactions = new ArrayList<>(factions);
        this.factionOutcomes.clear();
        this.currentVictoryFactionIndex = 0;
    }
    
    /**
     * Check if victory outcome processing is currently active
     * 
     * @return true if victory processing is in progress
     */
    public boolean isVictoryProcessingActive() {
        return !scenarioFactions.isEmpty() || !factionOutcomes.isEmpty();
    }
}
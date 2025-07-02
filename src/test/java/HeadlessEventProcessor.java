import java.util.List;
import combat.CombatCoordinator;
import combat.Character;
import game.Unit;
import game.ScheduledEvent;

/**
 * Event processing system for headless combat testing - System 5 of DevCycle 36.
 * 
 * This class handles tick-based event processing without UI dependencies, integrating
 * with the existing ScheduledEvent system and CombatCoordinator to provide authentic
 * combat simulation in headless mode.
 * 
 * Key features:
 * - Manual tick advancement for deterministic testing
 * - Integration with existing CombatCoordinator and combat managers
 * - Event execution without UI callbacks
 * - Combat statistics tracking and validation
 * 
 * @author DevCycle 36 - System 5: Complete Headless GunfightTestAutomated
 */
public class HeadlessEventProcessor {
    
    private final HeadlessGameState gameState;
    private final CombatCoordinator combatCoordinator;
    private boolean processingEvents;
    private int eventsProcessedThisTick;
    
    // Statistics tracking
    private long totalEventsProcessed;
    private long ticksWithEvents;
    private long lastEventTick;
    
    public HeadlessEventProcessor(HeadlessGameState gameState) {
        this.gameState = gameState;
        this.combatCoordinator = CombatCoordinator.getInstance();
        this.processingEvents = false;
        this.eventsProcessedThisTick = 0;
        this.totalEventsProcessed = 0;
        this.ticksWithEvents = 0;
        this.lastEventTick = 0;
        
        System.out.println("HeadlessEventProcessor initialized with CombatCoordinator integration");
    }
    
    /**
     * Process all events scheduled for the current tick.
     * This method advances the game state by one tick and executes all events
     * that are due for execution.
     * 
     * @return number of events processed this tick
     */
    public int processCurrentTick() {
        if (processingEvents) {
            System.err.println("Warning: Recursive event processing detected at tick " + gameState.getCurrentTick());
            return 0;
        }
        
        processingEvents = true;
        eventsProcessedThisTick = 0;
        
        try {
            // Advance the game clock
            gameState.advanceTick();
            long currentTick = gameState.getCurrentTick();
            
            // Get all events scheduled for this tick
            List<ScheduledEvent> currentEvents = gameState.getEventsForCurrentTick();
            
            if (!currentEvents.isEmpty()) {
                ticksWithEvents++;
                lastEventTick = currentTick;
                
                System.out.println("Processing " + currentEvents.size() + " events at tick " + currentTick);
                
                // Process each event
                for (ScheduledEvent event : currentEvents) {
                    processEvent(event);
                    eventsProcessedThisTick++;
                    totalEventsProcessed++;
                }
                
                // Update game state with any changes from event processing
                updateGameStateAfterEvents();
            }
            
            return eventsProcessedThisTick;
            
        } finally {
            processingEvents = false;
        }
    }
    
    /**
     * Process a single scheduled event.
     * This method handles the execution of individual events and updates
     * combat statistics based on the event type.
     * 
     * @param event the event to process
     */
    private void processEvent(ScheduledEvent event) {
        try {
            System.out.println("  Executing event at tick " + event.getTick());
            
            // Execute the event
            event.getAction().run();
            
            // Track combat statistics based on event type
            updateCombatStatisticsForEvent(event);
            
        } catch (Exception e) {
            System.err.println("Error processing event at tick " + event.getTick());
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Update combat statistics based on the type of event processed.
     * This helps track combat progress and validate test results.
     * 
     * @param event the event that was processed
     */
    private void updateCombatStatisticsForEvent(ScheduledEvent event) {
        // Note: ScheduledEvent doesn't have description, so we track events generically
        // Combat statistics will be tracked through character state changes
        // rather than event descriptions
    }
    
    /**
     * Update the game state after processing all events for the current tick.
     * This includes checking character health, updating unit states, and
     * validating combat continuation.
     */
    private void updateGameStateAfterEvents() {
        // Check for newly incapacitated characters
        for (Character character : gameState.getCharacters()) {
            if (character.getHealth() <= 0 && character.isIncapacitated()) {
                gameState.recordIncapacitation();
                System.out.println("Character incapacitated: " + character.getName());
            }
        }
        
        // Update unit positions and states if needed
        for (Unit unit : gameState.getUnits()) {
            // Update unit state based on character condition
            if (unit.getCharacter() != null && unit.getCharacter().getHealth() <= 0) {
                // Stop movement for incapacitated units
                if (unit.isMoving()) {
                    unit.setTarget(unit.getX(), unit.getY());
                }
            }
        }
    }
    
    /**
     * Run the event processing loop until combat is complete or timeout.
     * This method provides the main simulation loop for headless testing.
     * 
     * @return true if combat completed successfully, false if timeout or error
     */
    public boolean runCombatSimulation() {
        System.out.println("Starting headless combat simulation...");
        long startTick = gameState.getCurrentTick();
        
        while (gameState.isRunning() && gameState.isCombatActive() && !gameState.isCombatComplete()) {
            int eventsProcessed = processCurrentTick();
            
            // Log periodic status updates
            if (gameState.getCurrentTick() % 60 == 0) { // Every second
                System.out.println("Tick " + gameState.getCurrentTick() + 
                                 ": " + eventsProcessed + " events processed, " +
                                 gameState.getAliveCharacters().size() + " characters alive");
            }
            
            // Safety check to prevent infinite loops
            if (gameState.getCurrentTick() - startTick > 3600) { // 60 seconds max
                System.err.println("Combat simulation timeout - stopping at tick " + gameState.getCurrentTick());
                break;
            }
        }
        
        boolean success = gameState.isCombatComplete();
        System.out.println("Combat simulation " + (success ? "completed" : "terminated") + 
                         " at tick " + gameState.getCurrentTick());
        System.out.println("Reason: " + gameState.getCombatCompletionReason());
        
        return success;
    }
    
    /**
     * Run the simulation for a specific number of ticks.
     * Useful for controlled testing scenarios.
     * 
     * @param maxTicks maximum number of ticks to process
     * @return number of ticks actually processed
     */
    public int runForTicks(int maxTicks) {
        System.out.println("Running combat simulation for " + maxTicks + " ticks...");
        
        int ticksProcessed = 0;
        while (ticksProcessed < maxTicks && gameState.isRunning() && 
               gameState.isCombatActive() && !gameState.isCombatComplete()) {
            
            processCurrentTick();
            ticksProcessed++;
        }
        
        System.out.println("Processed " + ticksProcessed + " ticks");
        return ticksProcessed;
    }
    
    // Statistics and monitoring
    public long getTotalEventsProcessed() {
        return totalEventsProcessed;
    }
    
    public long getTicksWithEvents() {
        return ticksWithEvents;
    }
    
    public long getLastEventTick() {
        return lastEventTick;
    }
    
    public int getEventsProcessedThisTick() {
        return eventsProcessedThisTick;
    }
    
    public boolean isProcessingEvents() {
        return processingEvents;
    }
    
    /**
     * Get a summary of event processing statistics.
     * 
     * @return formatted string with processing statistics
     */
    public String getProcessingStatistics() {
        long currentTick = gameState.getCurrentTick();
        double eventsPerTick = currentTick > 0 ? (double) totalEventsProcessed / currentTick : 0;
        double activeTickPercentage = currentTick > 0 ? (double) ticksWithEvents / currentTick * 100 : 0;
        
        return String.format(
            "Event Processing Statistics:\n" +
            "  Total Events Processed: %d\n" +
            "  Ticks with Events: %d / %d (%.1f%%)\n" +
            "  Events per Tick: %.2f\n" +
            "  Last Event Tick: %d\n" +
            "  Currently Processing: %s",
            totalEventsProcessed,
            ticksWithEvents, currentTick, activeTickPercentage,
            eventsPerTick,
            lastEventTick,
            processingEvents ? "Yes" : "No"
        );
    }
    
    /**
     * Reset all processing statistics.
     * Useful for running multiple test scenarios.
     */
    public void resetStatistics() {
        totalEventsProcessed = 0;
        ticksWithEvents = 0;
        lastEventTick = 0;
        eventsProcessedThisTick = 0;
        processingEvents = false;
        
        System.out.println("Event processing statistics reset");
    }
    
    /**
     * Validate that the event processing system is working correctly.
     * This method performs basic sanity checks on the event processing state.
     * 
     * @return true if validation passes, false otherwise
     */
    public boolean validateEventProcessing() {
        boolean valid = true;
        
        // Check that we're not stuck in recursive processing
        if (processingEvents) {
            System.err.println("Validation failed: Still processing events");
            valid = false;
        }
        
        // Check that event statistics are reasonable
        if (totalEventsProcessed < 0 || ticksWithEvents < 0) {
            System.err.println("Validation failed: Negative statistics");
            valid = false;
        }
        
        // Check that the game state is consistent
        if (gameState.getQueuedEventCount() < 0) {
            System.err.println("Validation failed: Negative queued event count");
            valid = false;
        }
        
        return valid;
    }
}
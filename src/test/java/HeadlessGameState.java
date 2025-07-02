import java.util.*;
import combat.Character;
import game.Unit;
import game.ScheduledEvent;

/**
 * Minimal game state management for headless combat testing - System 5 of DevCycle 36.
 * 
 * This class provides the essential game state components needed for combat simulation
 * without UI dependencies. It manages units, characters, event scheduling, and game timing
 * in a simplified form optimized for testing.
 * 
 * Key features:
 * - Manual tick advancement for deterministic testing
 * - Event queue management using existing ScheduledEvent system
 * - Unit and character tracking for combat scenarios
 * - Game clock management for precise timing control
 * 
 * @author DevCycle 36 - System 5: Complete Headless GunfightTestAutomated
 */
public class HeadlessGameState {
    
    // Core game state
    private final List<Unit> units;
    private final List<Character> characters;
    private final PriorityQueue<ScheduledEvent> eventQueue;
    private long currentTick;
    private boolean running;
    
    // Combat tracking
    private final Map<String, Integer> combatStatistics;
    private long combatStartTick;
    private long lastActivityTick;
    
    // Configuration
    private final long maxIdleTicks;
    private final long timeoutTicks;
    
    public HeadlessGameState() {
        this.units = new ArrayList<>();
        this.characters = new ArrayList<>();
        this.eventQueue = new PriorityQueue<>(Comparator.comparingLong(ScheduledEvent::getTick));
        this.currentTick = 0;
        this.running = false;
        this.combatStatistics = new HashMap<>();
        this.combatStartTick = 0;
        this.lastActivityTick = 0;
        
        // Configuration: 60 seconds at 60 ticks per second = 3600 ticks
        this.timeoutTicks = 60 * 60; // 60 seconds maximum
        this.maxIdleTicks = 15 * 60;  // 15 seconds of inactivity before considering combat complete
        
        initializeCombatStatistics();
    }
    
    private void initializeCombatStatistics() {
        combatStatistics.put("totalShots", 0);
        combatStatistics.put("totalHits", 0);
        combatStatistics.put("totalWounds", 0);
        combatStatistics.put("incapacitatedCharacters", 0);
        combatStatistics.put("ticksElapsed", 0);
        combatStatistics.put("eventsProcessed", 0);
    }
    
    // Game state management
    public void initialize() {
        currentTick = 0;
        running = true;
        combatStartTick = 0;
        lastActivityTick = 0;
        clearCombatStatistics();
        System.out.println("HeadlessGameState initialized - ready for combat simulation");
    }
    
    public void shutdown() {
        running = false;
        System.out.println("HeadlessGameState shutdown - combat simulation ended");
    }
    
    public boolean isRunning() {
        return running;
    }
    
    // Tick management
    public void advanceTick() {
        currentTick++;
        updateCombatStatistics();
    }
    
    public long getCurrentTick() {
        return currentTick;
    }
    
    public void setCurrentTick(long tick) {
        this.currentTick = tick;
    }
    
    // Unit and character management
    public void addUnit(Unit unit) {
        if (unit != null && !units.contains(unit)) {
            units.add(unit);
            System.out.println("Added unit to headless game state: " + unit.toString());
        }
    }
    
    public void addCharacter(Character character) {
        if (character != null && !characters.contains(character)) {
            characters.add(character);
            System.out.println("Added character to headless game state: " + character.getName());
        }
    }
    
    public List<Unit> getUnits() {
        return new ArrayList<>(units);
    }
    
    public List<Character> getCharacters() {
        return new ArrayList<>(characters);
    }
    
    public void clearUnits() {
        units.clear();
        characters.clear();
        System.out.println("Cleared all units and characters from headless game state");
    }
    
    // Event queue management
    public void scheduleEvent(ScheduledEvent event) {
        if (event != null) {
            eventQueue.offer(event);
            lastActivityTick = currentTick;
        }
    }
    
    public List<ScheduledEvent> getEventsForCurrentTick() {
        List<ScheduledEvent> currentEvents = new ArrayList<>();
        
        while (!eventQueue.isEmpty() && eventQueue.peek().getTick() <= currentTick) {
            ScheduledEvent event = eventQueue.poll();
            currentEvents.add(event);
            combatStatistics.put("eventsProcessed", combatStatistics.get("eventsProcessed") + 1);
            lastActivityTick = currentTick;
        }
        
        return currentEvents;
    }
    
    public int getQueuedEventCount() {
        return eventQueue.size();
    }
    
    public void clearEventQueue() {
        eventQueue.clear();
        System.out.println("Cleared event queue");
    }
    
    public java.util.PriorityQueue<ScheduledEvent> getInternalEventQueue() {
        return eventQueue;
    }
    
    // Combat state assessment
    public boolean isCombatActive() {
        // Combat is active if there are living characters and recent activity
        boolean hasLivingCharacters = characters.stream().anyMatch(c -> c.getHealth() > 0);
        boolean hasRecentActivity = (currentTick - lastActivityTick) < maxIdleTicks;
        boolean hasQueuedEvents = !eventQueue.isEmpty();
        
        return hasLivingCharacters && (hasRecentActivity || hasQueuedEvents);
    }
    
    public boolean isCombatComplete() {
        // Combat is complete if only one character is alive or no activity for too long
        long aliveCount = characters.stream().mapToLong(c -> c.getHealth() > 0 ? 1 : 0).sum();
        boolean timeout = (currentTick - combatStartTick) > timeoutTicks;
        boolean inactive = (currentTick - lastActivityTick) > maxIdleTicks;
        
        return aliveCount <= 1 || timeout || inactive;
    }
    
    public String getCombatCompletionReason() {
        long aliveCount = characters.stream().mapToLong(c -> c.getHealth() > 0 ? 1 : 0).sum();
        boolean timeout = (currentTick - combatStartTick) > timeoutTicks;
        boolean inactive = (currentTick - lastActivityTick) > maxIdleTicks;
        
        if (aliveCount <= 1) {
            return "Combat complete: " + aliveCount + " character(s) remaining alive";
        } else if (timeout) {
            return "Combat complete: Timeout reached (" + timeoutTicks + " ticks)";
        } else if (inactive) {
            return "Combat complete: No activity for " + (currentTick - lastActivityTick) + " ticks";
        } else {
            return "Combat ongoing";
        }
    }
    
    // Combat statistics
    public void recordShot() {
        combatStatistics.put("totalShots", combatStatistics.get("totalShots") + 1);
        lastActivityTick = currentTick;
    }
    
    public void recordHit() {
        combatStatistics.put("totalHits", combatStatistics.get("totalHits") + 1);
        lastActivityTick = currentTick;
    }
    
    public void recordWound(int damage) {
        combatStatistics.put("totalWounds", combatStatistics.get("totalWounds") + damage);
        lastActivityTick = currentTick;
    }
    
    public void recordIncapacitation() {
        combatStatistics.put("incapacitatedCharacters", combatStatistics.get("incapacitatedCharacters") + 1);
        lastActivityTick = currentTick;
    }
    
    private void updateCombatStatistics() {
        combatStatistics.put("ticksElapsed", (int) currentTick);
        
        // Update combat start time on first activity
        if (combatStartTick == 0 && lastActivityTick > 0) {
            combatStartTick = currentTick;
        }
    }
    
    private void clearCombatStatistics() {
        combatStatistics.replaceAll((k, v) -> 0);
    }
    
    public Map<String, Integer> getCombatStatistics() {
        return new HashMap<>(combatStatistics);
    }
    
    // Character state queries
    public List<Character> getAliveCharacters() {
        return characters.stream()
                .filter(c -> c.getHealth() > 0)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    public List<Character> getIncapacitatedCharacters() {
        return characters.stream()
                .filter(c -> c.getHealth() <= 0)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    public Character getWinner() {
        List<Character> alive = getAliveCharacters();
        return alive.size() == 1 ? alive.get(0) : null;
    }
    
    // Debugging and diagnostics
    public void printGameState() {
        System.out.println("=== Headless Game State ===");
        System.out.println("Current Tick: " + currentTick);
        System.out.println("Running: " + running);
        System.out.println("Units: " + units.size());
        System.out.println("Characters: " + characters.size());
        System.out.println("Queued Events: " + eventQueue.size());
        System.out.println("Combat Active: " + isCombatActive());
        System.out.println("Combat Complete: " + isCombatComplete());
        System.out.println("Completion Reason: " + getCombatCompletionReason());
        
        System.out.println("\nCharacter Status:");
        for (Character character : characters) {
            System.out.println("  " + character.getName() + ": Health=" + character.getHealth() + 
                             (character.getHealth() > 0 ? " (Alive)" : " (Incapacitated)"));
        }
        
        System.out.println("\nCombat Statistics:");
        combatStatistics.forEach((key, value) -> 
            System.out.println("  " + key + ": " + value));
        
        System.out.println("========================");
    }
    
    public void printCombatSummary() {
        System.out.println("\n=== Combat Summary ===");
        System.out.println("Duration: " + combatStatistics.get("ticksElapsed") + " ticks");
        System.out.println("Total Shots: " + combatStatistics.get("totalShots"));
        System.out.println("Total Hits: " + combatStatistics.get("totalHits"));
        System.out.println("Total Wounds: " + combatStatistics.get("totalWounds"));
        System.out.println("Incapacitated: " + combatStatistics.get("incapacitatedCharacters"));
        System.out.println("Events Processed: " + combatStatistics.get("eventsProcessed"));
        
        if (combatStatistics.get("totalShots") > 0) {
            double hitRate = (double) combatStatistics.get("totalHits") / combatStatistics.get("totalShots") * 100;
            System.out.println("Hit Rate: " + String.format("%.1f%%", hitRate));
        }
        
        Character winner = getWinner();
        if (winner != null) {
            System.out.println("Winner: " + winner.getName());
        } else {
            System.out.println("No clear winner");
        }
        
        System.out.println("Final Status: " + getCombatCompletionReason());
        System.out.println("======================");
    }
}
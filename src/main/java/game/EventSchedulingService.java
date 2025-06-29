package game;

import java.util.PriorityQueue;

/**
 * Implementation of centralized event scheduling service.
 * Singleton service that provides thread-safe event scheduling for all combat managers.
 */
public class EventSchedulingService implements IEventSchedulingService {
    
    private static EventSchedulingService instance;
    private PriorityQueue<ScheduledEvent> eventQueue;
    private GameClock gameClock;
    private boolean initialized = false;
    
    /**
     * Private constructor for singleton pattern.
     */
    private EventSchedulingService() {
        // Constructor is private for singleton
    }
    
    /**
     * Get the singleton instance of the EventSchedulingService.
     * 
     * @return The service instance
     */
    public static EventSchedulingService getInstance() {
        if (instance == null) {
            instance = new EventSchedulingService();
        }
        return instance;
    }
    
    /**
     * Initialize the service with required dependencies.
     * Must be called before using the service.
     * 
     * @param eventQueue The game's event queue
     * @param gameClock The game's clock
     */
    public void initialize(PriorityQueue<ScheduledEvent> eventQueue, GameClock gameClock) {
        if (eventQueue == null || gameClock == null) {
            throw new IllegalArgumentException("EventQueue and GameClock cannot be null");
        }
        this.eventQueue = eventQueue;
        this.gameClock = gameClock;
        this.initialized = true;
    }
    
    @Override
    public void scheduleEvent(long tick, Runnable action, int ownerId) {
        if (!initialized) {
            throw new IllegalStateException("EventSchedulingService not initialized");
        }
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }
        if (tick < gameClock.getCurrentTick()) {
            throw new IllegalArgumentException("Cannot schedule events in the past");
        }
        
        ScheduledEvent event = new ScheduledEvent(tick, action, ownerId);
        eventQueue.add(event);
    }
    
    @Override
    public void scheduleEventWithDelay(long delayTicks, Runnable action, int ownerId) {
        if (!initialized) {
            throw new IllegalStateException("EventSchedulingService not initialized");
        }
        if (delayTicks < 0) {
            throw new IllegalArgumentException("Delay cannot be negative");
        }
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }
        
        long scheduledTick = gameClock.getCurrentTick() + delayTicks;
        scheduleEvent(scheduledTick, action, ownerId);
    }
    
    @Override
    public int cancelEventsForOwner(int ownerId) {
        if (!initialized) {
            throw new IllegalStateException("EventSchedulingService not initialized");
        }
        
        // Remove all events with matching owner ID
        int cancelledCount = 0;
        PriorityQueue<ScheduledEvent> newQueue = new PriorityQueue<>();
        
        while (!eventQueue.isEmpty()) {
            ScheduledEvent event = eventQueue.poll();
            if (event.getOwnerId() != ownerId) {
                newQueue.add(event);
            } else {
                cancelledCount++;
            }
        }
        
        eventQueue.addAll(newQueue);
        return cancelledCount;
    }
    
    @Override
    public long getCurrentTick() {
        if (!initialized) {
            throw new IllegalStateException("EventSchedulingService not initialized");
        }
        return gameClock.getCurrentTick();
    }
    
    @Override
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Reset the service for testing purposes.
     * This should only be used in test environments.
     */
    public void reset() {
        eventQueue = null;
        gameClock = null;
        initialized = false;
    }
}
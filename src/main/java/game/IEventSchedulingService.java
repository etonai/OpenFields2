package game;

/**
 * Interface for centralized event scheduling service.
 * Provides a unified way for all managers to schedule game events.
 */
public interface IEventSchedulingService {
    
    /**
     * Schedule an event to occur at a specific tick.
     * 
     * @param tick The game tick when the event should execute
     * @param action The action to perform
     * @param ownerId The ID of the entity owning this event
     * @throws IllegalArgumentException if tick is in the past or action is null
     */
    void scheduleEvent(long tick, Runnable action, int ownerId);
    
    /**
     * Schedule an event to occur after a delay.
     * 
     * @param delayTicks The number of ticks to wait before executing
     * @param action The action to perform
     * @param ownerId The ID of the entity owning this event
     * @throws IllegalArgumentException if delay is negative or action is null
     */
    void scheduleEventWithDelay(long delayTicks, Runnable action, int ownerId);
    
    /**
     * Cancel all events owned by a specific entity.
     * 
     * @param ownerId The ID of the entity whose events should be cancelled
     * @return The number of events cancelled
     */
    int cancelEventsForOwner(int ownerId);
    
    /**
     * Get the current game tick.
     * 
     * @return The current tick
     */
    long getCurrentTick();
    
    /**
     * Check if the service is initialized and ready.
     * 
     * @return true if the service is ready for use
     */
    boolean isInitialized();
}
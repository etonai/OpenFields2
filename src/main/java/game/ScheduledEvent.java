package game;

public class ScheduledEvent implements Comparable<ScheduledEvent> {
    public final long tick;
    public final Runnable action;

    private final int ownerId; // -1 for world-owned events

    public static final int WORLD_OWNER = -1;

    /*
    public ScheduledEvent(long tick, Runnable action) {
        this.tick = tick;
        this.action = action;
        this.ownerId = WORLD_OWNER;
    }

     */
    public ScheduledEvent(long tick, Runnable action, int ownerId) {
        this.tick = tick;
        this.action = action;
        this.ownerId = ownerId;
    }

    public long getTick() {
        return tick;
    }

    public Runnable getAction() {
        return action;
    }

    public int getOwnerId() {
        return ownerId;
    }

    @Override
    public int compareTo(ScheduledEvent other) {
        return Long.compare(this.tick, other.tick);
    }
}
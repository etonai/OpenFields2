public class ScheduledEvent implements Comparable<ScheduledEvent> {
    int ownerId = -1;
    long tick;
    Runnable action;

    public ScheduledEvent(long tick, Runnable action) {
        this.tick = tick;
        this.action = action;
        this.ownerId = -1;
    }

    public ScheduledEvent(long tick, Runnable action, int ownerId) {
        this.tick = tick;
        this.action = action;
    }

    public Runnable getAction() {
        return action;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public long getTick() {
        return tick;
    }

    @Override
    public int compareTo(ScheduledEvent other) {
        return Long.compare(this.tick, other.tick);
    }

    @Override
    public String toString() {
        return "ScheduledEvent{tick=" + tick + ", ownerId=" + ownerId + ", action=" + action + "}";
    }

}

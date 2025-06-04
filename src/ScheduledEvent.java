class ScheduledEvent implements Comparable<ScheduledEvent> {
    final long tick;
    final Runnable action;

    public ScheduledEvent(long tick, Runnable action) {
        this.tick = tick;
        this.action = action;
    }

    @Override
    public int compareTo(ScheduledEvent other) {
        return Long.compare(this.tick, other.tick);
    }
}


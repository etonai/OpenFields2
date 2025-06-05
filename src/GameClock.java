public class GameClock {
    private long currentTick = 0;
    private boolean paused = false;

    public void tick() {
        currentTick++;
    }

    public long getCurrentTick() {
        return currentTick;
    }

    public void togglePause() {
        paused = !paused;
    }

    public boolean isPaused() {
        return paused;
    }
}

class GameClock {
    private long currentTick = 0;

    public void advanceTick() {
        currentTick++;
    }



    public long getCurrentTick() {
        return currentTick;
    }

    public void reset() {
        currentTick = 0;
    }
}

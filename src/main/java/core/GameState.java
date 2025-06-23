package core;

import game.Unit;
import game.GameClock;
import game.ScheduledEvent;
import combat.Character;
import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Map;
import java.util.HashMap;

/**
 * Complete game state that can be serialized and tested.
 * This represents all mutable game data in a platform-independent way.
 */
public class GameState {
    // Core game objects
    private final List<Unit> units;
    private final PriorityQueue<ScheduledEvent> eventQueue;
    private final GameClock gameClock;
    
    // Game state
    private boolean paused;
    private int selectedUnitId;
    private double cameraX;
    private double cameraY;
    private double cameraZoom;
    
    // Statistics
    private final Map<String, Integer> gameStats;
    
    public GameState() {
        this.units = new ArrayList<>();
        this.eventQueue = new PriorityQueue<>();
        this.gameClock = new GameClock();
        this.paused = false;
        this.selectedUnitId = -1;
        this.cameraX = 0;
        this.cameraY = 0;
        this.cameraZoom = 1.0;
        this.gameStats = new HashMap<>();
    }
    
    // Unit management
    public void addUnit(Unit unit) {
        units.add(unit);
    }
    
    public void removeUnit(Unit unit) {
        units.remove(unit);
    }
    
    public List<Unit> getUnits() {
        return new ArrayList<>(units);
    }
    
    public Unit getUnitById(int id) {
        for (Unit unit : units) {
            if (unit.getId() == id) {
                return unit;
            }
        }
        return null;
    }
    
    // Event management
    public void scheduleEvent(ScheduledEvent event) {
        eventQueue.add(event);
    }
    
    public void processEvents() {
        long currentTick = gameClock.getCurrentTick();
        while (!eventQueue.isEmpty() && eventQueue.peek().getTick() <= currentTick) {
            ScheduledEvent event = eventQueue.poll();
            event.getAction().run();
        }
    }
    
    public PriorityQueue<ScheduledEvent> getEventQueue() {
        return new PriorityQueue<>(eventQueue);
    }
    
    // Game clock
    public GameClock getGameClock() {
        return gameClock;
    }
    
    public void advanceTick() {
        if (!paused) {
            gameClock.advanceTick();
        }
    }
    
    // Game state
    public boolean isPaused() {
        return paused;
    }
    
    public void setPaused(boolean paused) {
        this.paused = paused;
    }
    
    public void togglePause() {
        this.paused = !this.paused;
    }
    
    // Selection
    public int getSelectedUnitId() {
        return selectedUnitId;
    }
    
    public void setSelectedUnitId(int unitId) {
        this.selectedUnitId = unitId;
    }
    
    public Unit getSelectedUnit() {
        return getUnitById(selectedUnitId);
    }
    
    // Camera
    public double getCameraX() {
        return cameraX;
    }
    
    public void setCameraX(double x) {
        this.cameraX = x;
    }
    
    public double getCameraY() {
        return cameraY;
    }
    
    public void setCameraY(double y) {
        this.cameraY = y;
    }
    
    public double getCameraZoom() {
        return cameraZoom;
    }
    
    public void setCameraZoom(double zoom) {
        this.cameraZoom = Math.max(0.1, Math.min(10.0, zoom));
    }
    
    public void panCamera(double dx, double dy) {
        this.cameraX += dx;
        this.cameraY += dy;
    }
    
    public void zoomCamera(double factor) {
        setCameraZoom(cameraZoom * factor);
    }
    
    // Statistics
    public void incrementStat(String key) {
        gameStats.put(key, gameStats.getOrDefault(key, 0) + 1);
    }
    
    public int getStat(String key) {
        return gameStats.getOrDefault(key, 0);
    }
    
    public Map<String, Integer> getAllStats() {
        return new HashMap<>(gameStats);
    }
    
    // Utility methods
    public int getAliveUnitCount() {
        int count = 0;
        for (Unit unit : units) {
            if (!unit.character.isIncapacitated()) {
                count++;
            }
        }
        return count;
    }
    
    public List<Unit> getUnitsInRange(double x, double y, double range) {
        List<Unit> result = new ArrayList<>();
        double rangeSquared = range * range;
        
        for (Unit unit : units) {
            double dx = unit.getX() - x;
            double dy = unit.getY() - y;
            double distSquared = dx * dx + dy * dy;
            
            if (distSquared <= rangeSquared) {
                result.add(unit);
            }
        }
        
        return result;
    }
    
    /**
     * Creates a deep copy of the game state for testing or save/load.
     */
    public GameState copy() {
        // This would need proper implementation for deep copying
        // For now, returning this as a placeholder
        return this;
    }
}
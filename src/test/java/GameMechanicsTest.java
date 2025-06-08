import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import combat.*;
import game.*;
import javafx.scene.paint.Color;

import java.util.PriorityQueue;

import static org.junit.jupiter.api.Assertions.*;

public class GameMechanicsTest {
    
    private Unit testUnit;
    private combat.Character testCharacter;
    private GameClock gameClock;
    private PriorityQueue<ScheduledEvent> eventQueue;
    
    @BeforeEach
    public void setUp() {
        testCharacter = new combat.Character("TestChar", 70, 100, 60);
        testUnit = new Unit(testCharacter, 100, 100, Color.BLUE, 1);
        gameClock = new GameClock();
        eventQueue = new PriorityQueue<>();
    }
    
    @Test
    public void testGameClock_InitialState() {
        assertEquals(0, gameClock.getCurrentTick(), "Game clock should start at tick 0");
    }
    
    @Test
    public void testGameClock_AdvanceTick() {
        gameClock.advanceTick();
        assertEquals(1, gameClock.getCurrentTick(), "Game clock should advance to tick 1");
        
        gameClock.advanceTick();
        assertEquals(2, gameClock.getCurrentTick(), "Game clock should advance to tick 2");
    }
    
    @Test
    public void testGameClock_Reset() {
        gameClock.advanceTick();
        gameClock.advanceTick();
        assertEquals(2, gameClock.getCurrentTick(), "Game clock should be at tick 2");
        
        gameClock.reset();
        assertEquals(0, gameClock.getCurrentTick(), "Game clock should reset to tick 0");
    }
    
    @Test
    public void testUnit_InitialPosition() {
        assertEquals(100, testUnit.getX(), "Unit should start at X position 100");
        assertEquals(100, testUnit.getY(), "Unit should start at Y position 100");
        assertEquals(100, testUnit.targetX, "Target X should initially equal current X");
        assertEquals(100, testUnit.targetY, "Target Y should initially equal current Y");
        assertFalse(testUnit.hasTarget, "Unit should not have target initially");
    }
    
    @Test
    public void testUnit_SetTarget() {
        testUnit.setTarget(200, 150);
        
        assertEquals(200, testUnit.targetX, "Target X should be set to 200");
        assertEquals(150, testUnit.targetY, "Target Y should be set to 150");
        assertTrue(testUnit.hasTarget, "Unit should have target after setting");
    }
    
    @Test
    public void testUnit_MovementCalculation() {
        testUnit.setTarget(200, 100); // Move 100 pixels to the right
        
        long currentTick = 1;
        testUnit.update(currentTick);
        
        // Movement speed is 42 pixels/second, at 60 ticks/second = 0.7 pixels/tick
        double expectedX = 100 + (42.0 / 60.0); // 100.7
        assertEquals(expectedX, testUnit.getX(), 0.001, "Unit should move toward target");
        assertEquals(100, testUnit.getY(), 0.001, "Y position should remain unchanged");
    }
    
    @Test
    public void testUnit_MovementDiagonal() {
        testUnit.setTarget(200, 200); // Move diagonally
        
        long currentTick = 1;
        testUnit.update(currentTick);
        
        // Distance = sqrt((200-100)^2 + (200-100)^2) = sqrt(20000) ≈ 141.42
        // Move speed = 42/60 = 0.7 pixels/tick
        // Move X = 0.7 * (100/141.42) ≈ 0.495
        // Move Y = 0.7 * (100/141.42) ≈ 0.495
        
        assertTrue(testUnit.getX() > 100, "Unit should move toward target in X direction");
        assertTrue(testUnit.getY() > 100, "Unit should move toward target in Y direction");
        
        double moveDistance = Math.hypot(testUnit.getX() - 100, testUnit.getY() - 100);
        assertEquals(42.0 / 60.0, moveDistance, 0.001, "Unit should move at correct speed");
    }
    
    @Test
    public void testUnit_ReachTarget() {
        testUnit.setTarget(101, 100); // Very close target
        
        long currentTick = 1;
        testUnit.update(currentTick);
        
        // Since target is very close, unit should reach it and stop
        assertFalse(testUnit.hasTarget, "Unit should clear target when reached");
        assertEquals(101, testUnit.getX(), 0.001, "Unit should be at target X position");
        assertEquals(100, testUnit.getY(), 0.001, "Unit should be at target Y position");
    }
    
    @Test
    public void testUnit_NoMovementWithoutTarget() {
        double initialX = testUnit.getX();
        double initialY = testUnit.getY();
        
        long currentTick = 1;
        testUnit.update(currentTick);
        
        assertEquals(initialX, testUnit.getX(), "Unit should not move without target");
        assertEquals(initialY, testUnit.getY(), "Unit should not move without target");
    }
    
    @Test
    public void testUnit_IncapacitatedMovement() {
        testCharacter.health = 0; // Incapacitate character
        testUnit.setTarget(200, 200);
        assertTrue(testUnit.hasTarget, "Unit should have target before update");
        
        long currentTick = 1;
        testUnit.update(currentTick);
        
        assertFalse(testUnit.hasTarget, "Incapacitated unit should clear target");
        assertEquals(100, testUnit.getX(), "Incapacitated unit should not move");
        assertEquals(100, testUnit.getY(), "Incapacitated unit should not move");
    }
    
    @Test
    public void testUnit_Contains() {
        // Unit is at (100, 100) with radius 10.5
        assertTrue(testUnit.contains(100, 100), "Should contain center point");
        assertTrue(testUnit.contains(105, 105), "Should contain point within radius");
        assertTrue(testUnit.contains(110, 100), "Should contain point at edge");
        assertFalse(testUnit.contains(120, 100), "Should not contain point outside radius");
        assertFalse(testUnit.contains(100, 120), "Should not contain point outside radius");
    }
    
    @Test
    public void testUnit_SameTickUpdate() {
        testUnit.setTarget(200, 200);
        
        long currentTick = 5;
        testUnit.update(currentTick);
        double firstX = testUnit.getX();
        double firstY = testUnit.getY();
        
        // Update again with same tick - should not move
        testUnit.update(currentTick);
        assertEquals(firstX, testUnit.getX(), "Unit should not update position on same tick");
        assertEquals(firstY, testUnit.getY(), "Unit should not update position on same tick");
    }
    
    @Test
    public void testScheduledEvent_Construction() {
        Runnable action = () -> System.out.println("Test action");
        ScheduledEvent event = new ScheduledEvent(100, action, 1);
        
        assertEquals(100, event.getTick(), "Event should have correct tick");
        assertEquals(action, event.getAction(), "Event should have correct action");
        assertEquals(1, event.getOwnerId(), "Event should have correct owner ID");
    }
    
    @Test
    public void testScheduledEvent_WorldOwner() {
        Runnable action = () -> System.out.println("World action");
        ScheduledEvent event = new ScheduledEvent(50, action, ScheduledEvent.WORLD_OWNER);
        
        assertEquals(ScheduledEvent.WORLD_OWNER, event.getOwnerId(), "Event should be world-owned");
        assertEquals(-1, event.getOwnerId(), "World owner should be -1");
    }
    
    @Test
    public void testScheduledEvent_Ordering() {
        ScheduledEvent early = new ScheduledEvent(10, () -> {}, 1);
        ScheduledEvent middle = new ScheduledEvent(50, () -> {}, 2);
        ScheduledEvent late = new ScheduledEvent(100, () -> {}, 3);
        
        PriorityQueue<ScheduledEvent> queue = new PriorityQueue<>();
        queue.add(late);
        queue.add(early);
        queue.add(middle);
        
        assertEquals(early, queue.poll(), "Earliest event should come first");
        assertEquals(middle, queue.poll(), "Middle event should come second");
        assertEquals(late, queue.poll(), "Latest event should come last");
    }
    
    @Test
    public void testEventQueue_RemovalByOwner() {
        ScheduledEvent event1 = new ScheduledEvent(10, () -> {}, 1);
        ScheduledEvent event2 = new ScheduledEvent(20, () -> {}, 2);
        ScheduledEvent event3 = new ScheduledEvent(30, () -> {}, 1);
        ScheduledEvent event4 = new ScheduledEvent(40, () -> {}, 3);
        
        eventQueue.add(event1);
        eventQueue.add(event2);
        eventQueue.add(event3);
        eventQueue.add(event4);
        
        assertEquals(4, eventQueue.size(), "Queue should have 4 events");
        
        // Remove all events for owner 1
        eventQueue.removeIf(e -> e.getOwnerId() == 1);
        
        assertEquals(2, eventQueue.size(), "Queue should have 2 events after removal");
        
        // Verify remaining events
        ScheduledEvent remaining1 = eventQueue.poll();
        ScheduledEvent remaining2 = eventQueue.poll();
        
        assertTrue((remaining1.getOwnerId() == 2 || remaining1.getOwnerId() == 3), "Remaining event should be owner 2 or 3");
        assertTrue((remaining2.getOwnerId() == 2 || remaining2.getOwnerId() == 3), "Remaining event should be owner 2 or 3");
        assertNotEquals(remaining1.getOwnerId(), remaining2.getOwnerId(), "Remaining events should have different owners");
    }
    
    @Test
    public void testUnit_HitHighlight() {
        assertFalse(testUnit.isHitHighlighted, "Unit should not be highlighted initially");
        assertEquals(testUnit.baseColor, testUnit.color, "Unit color should equal base color initially");
        
        // Simulate hit highlighting
        testUnit.isHitHighlighted = true;
        testUnit.color = Color.YELLOW;
        
        assertTrue(testUnit.isHitHighlighted, "Unit should be highlighted after hit");
        assertEquals(Color.YELLOW, testUnit.color, "Unit color should be yellow when highlighted");
        
        // Simulate highlight removal
        testUnit.color = testUnit.baseColor;
        testUnit.isHitHighlighted = false;
        
        assertFalse(testUnit.isHitHighlighted, "Unit should not be highlighted after removal");
        assertEquals(testUnit.baseColor, testUnit.color, "Unit color should return to base color");
    }
    
    @Test
    public void testUnit_MovementSpeed() {
        // Test that unit moves at expected speed
        testUnit.setTarget(200, 100); // 100 pixels to the right
        
        double initialX = testUnit.getX();
        
        // Update for 60 ticks (1 second at 60 fps)
        for (int tick = 1; tick <= 60; tick++) {
            testUnit.update(tick);
        }
        
        double distanceMoved = testUnit.getX() - initialX;
        assertEquals(42.0, distanceMoved, 1.0, "Unit should move 42 pixels in 1 second (60 ticks)");
    }
    
    @Test
    public void testUnit_MovementPrecision() {
        // Test precise movement to avoid overshooting
        testUnit.setTarget(100.5, 100); // Very small movement
        
        long currentTick = 1;
        testUnit.update(currentTick);
        
        assertEquals(100.5, testUnit.getX(), 0.001, "Unit should reach precise target position");
        assertFalse(testUnit.hasTarget, "Unit should clear target when reached precisely");
    }
    
    @Test
    public void testGameClock_LongSequence() {
        for (int i = 0; i < 1000; i++) {
            gameClock.advanceTick();
        }
        assertEquals(1000, gameClock.getCurrentTick(), "Game clock should handle long sequences");
    }
    
    @Test
    public void testUnit_MultipleTargets() {
        // Test setting multiple targets in sequence
        testUnit.setTarget(200, 100);
        assertTrue(testUnit.hasTarget, "Unit should have first target");
        
        testUnit.setTarget(300, 200);
        assertEquals(300, testUnit.targetX, "Unit should update to new target X");
        assertEquals(200, testUnit.targetY, "Unit should update to new target Y");
        assertTrue(testUnit.hasTarget, "Unit should still have target");
    }
}
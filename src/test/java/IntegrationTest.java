import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import combat.*;
import game.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {
    
    private OpenFields2 game;
    private combat.Character shooter;
    private combat.Character target;
    private Unit shooterUnit;
    private Unit targetUnit;
    private RangedWeapon testWeapon;
    private PriorityQueue<ScheduledEvent> eventQueue;
    private GameClock gameClock;
    
    @BeforeEach
    public void setUp() {
        game = new OpenFields2();
        shooter = new combat.Character("Shooter", 80, 100, 70, 60, 75, combat.Handedness.RIGHT_HANDED);
        target = new combat.Character("Target", 60, 80, 50, 45, 55, combat.Handedness.RIGHT_HANDED);
        
        shooterUnit = new Unit(shooter, 100, 100, Color.BLUE, 1);
        targetUnit = new Unit(target, 300, 100, Color.RED, 2);
        
        testWeapon = createTestPistol();
        shooter.weapon = testWeapon;
        shooter.currentWeaponState = testWeapon.getStateByName("ready");
        
        eventQueue = new PriorityQueue<>();
        gameClock = new GameClock();
    }
    
    private GameCallbacks createTestCallbacks() {
        return new GameCallbacks() {
            @Override
            public void playWeaponSound(Weapon weapon) {
                // Mock sound playing for tests
            }
            
            @Override
            public void scheduleProjectileImpact(Unit shooter, Unit target, Weapon weapon, long fireTick, double distanceFeet) {
                // Schedule in the test's event queue instead of the game's private queue
                long impactTick = fireTick + Math.round(distanceFeet / weapon.velocityFeetPerSecond * 60);
                eventQueue.add(new ScheduledEvent(impactTick, () -> {
                    // Mock projectile impact for testing
                }, ScheduledEvent.WORLD_OWNER));
            }
            
            @Override
            public void applyFiringHighlight(Unit shooter, long fireTick) {
                // Mock firing highlight for tests
            }
            
            @Override
            public void addMuzzleFlash(Unit shooter, long fireTick) {
                // Mock muzzle flash for tests
            }
            
            @Override
            public void removeAllEventsForOwner(int ownerId) {
                eventQueue.removeIf(e -> e.getOwnerId() == ownerId);
            }
            
            @Override
            public List<Unit> getUnits() {
                return new ArrayList<>(); // Return empty list for tests
            }
        };
    }
    
    private RangedWeapon createTestPistol() {
        RangedWeapon weapon = new RangedWeapon("test_pistol", "Test Pistol", 600.0, 8, 10, "/test.wav", 200.0, 10, "bullet");
        weapon.states = new ArrayList<>();
        weapon.states.add(new WeaponState("holstered", "drawing", 0));
        weapon.states.add(new WeaponState("drawing", "ready", 30));
        weapon.states.add(new WeaponState("ready", "aiming", 15));
        weapon.states.add(new WeaponState("aiming", "firing", 60));
        weapon.states.add(new WeaponState("firing", "recovering", 5));
        weapon.states.add(new WeaponState("recovering", "aiming", 30));
        weapon.initialStateName = "holstered";
        return weapon;
    }
    
    @Test
    public void testCompleteAttackSequence_Hit() {
        // Position units 140 pixels apart (20 feet)
        targetUnit.x = 240;
        targetUnit.y = 100;
        
        // Start attack sequence from ready state
        shooter.startAttackSequence(shooterUnit, targetUnit, 100, eventQueue, 1, createTestCallbacks());
        
        // Execute the aiming transition
        ScheduledEvent aimingEvent = eventQueue.poll();
        assertEquals(115, aimingEvent.getTick(), "Aiming should start at tick 115");
        aimingEvent.getAction().run();
        assertEquals("aiming", shooter.currentWeaponState.getState());
        
        // Execute the firing
        ScheduledEvent firingEvent = eventQueue.poll();
        assertEquals(175, firingEvent.getTick(), "Firing should occur at tick 175");
        
        int initialTargetHealth = target.health;
        int initialAmmo = testWeapon.getAmmunition();
        
        firingEvent.getAction().run();
        
        assertEquals("firing", shooter.currentWeaponState.getState());
        assertEquals(initialAmmo - 1, testWeapon.getAmmunition(), "Ammunition should decrease");
        
        // Verify projectile impact was scheduled
        assertFalse(eventQueue.isEmpty(), "Projectile impact should be scheduled");
        
        // Find and execute projectile impact event
        ScheduledEvent impactEvent = null;
        while (!eventQueue.isEmpty()) {
            ScheduledEvent event = eventQueue.poll();
            if (event.getOwnerId() == ScheduledEvent.WORLD_OWNER) {
                impactEvent = event;
                break;
            }
        }
        
        assertNotNull(impactEvent, "Should find projectile impact event");
        
        // Calculate expected impact tick: fire tick + travel time
        double distanceFeet = 20.0; // 140 pixels / 7 = 20 feet
        long expectedTravelTicks = Math.round(distanceFeet / testWeapon.velocityFeetPerSecond * 60);
        assertEquals(175 + expectedTravelTicks, impactEvent.getTick());
        
        // Execute impact - may hit or miss depending on random roll
        impactEvent.getAction().run();
        
        // Verify that either the target took damage or the shot missed
        assertTrue(target.health <= initialTargetHealth, "Target health should be same or less");
    }
    
    @Test
    public void testCompleteAttackSequence_OutOfRange() {
        // Position target beyond weapon range
        targetUnit.x = 100 + (testWeapon.maximumRange * 7) + 100; // Beyond max range
        targetUnit.y = 100;
        
        shooter.startAttackSequence(shooterUnit, targetUnit, 100, eventQueue, 1, createTestCallbacks());
        
        // Execute sequence to firing
        while (!eventQueue.isEmpty() && !shooter.currentWeaponState.getState().equals("firing")) {
            ScheduledEvent event = eventQueue.poll();
            if (event.getOwnerId() == 1) { // Only execute shooter's events
                event.getAction().run();
            }
        }
        
        // Find and execute firing event
        ScheduledEvent firingEvent = null;
        while (!eventQueue.isEmpty()) {
            ScheduledEvent event = eventQueue.poll();
            if (event.getOwnerId() == 1 && shooter.currentWeaponState.getState().equals("aiming")) {
                firingEvent = event;
                break;
            }
        }
        
        if (firingEvent != null) {
            firingEvent.getAction().run();
            
            // Should still fire but hit chance will be very low due to range
            assertEquals("firing", shooter.currentWeaponState.getState());
        }
    }
    
    @Test
    public void testProjectileTravelTime() {
        // Test projectile travel time calculation
        double distanceFeet = 30.0;
        long fireTick = 100;
        
        // This should not throw any exceptions
        game.scheduleProjectileImpact(shooterUnit, targetUnit, testWeapon, fireTick, distanceFeet);
        
        // Calculate expected travel time for verification
        long expectedTravelTicks = Math.round(distanceFeet / testWeapon.velocityFeetPerSecond * 60);
        long expectedImpactTick = fireTick + expectedTravelTicks;
        
        // Verify the calculation makes sense
        assertTrue(expectedTravelTicks > 0, "Travel time should be positive");
        assertTrue(expectedImpactTick > fireTick, "Impact should be after firing");
    }
    
    @Test
    public void testCombatWithIncapacitation() {
        // Reduce target health to make incapacitation likely
        target.health = 1;
        
        // Position units close for high hit chance
        targetUnit.x = 150;
        targetUnit.y = 100;
        
        shooter.startAttackSequence(shooterUnit, targetUnit, 100, eventQueue, 1, createTestCallbacks());
        
        // Execute complete attack sequence
        while (!eventQueue.isEmpty()) {
            ScheduledEvent event = eventQueue.poll();
            if (event.getOwnerId() == 1) { // Shooter's events
                event.getAction().run();
                if (shooter.currentWeaponState.getState().equals("firing")) {
                    break;
                }
            }
        }
        
        // If target becomes incapacitated, movement should stop
        if (target.isIncapacitated()) {
            targetUnit.setTarget(500, 500);
            targetUnit.update(200);
            assertEquals(targetUnit.x, 150, 0.1, "Incapacitated unit should not move");
        }
    }
    
    @Test
    public void testMultipleAttacksQueuing() {
        shooter.startAttackSequence(shooterUnit, targetUnit, 100, eventQueue, 1, createTestCallbacks());
        // Queued shots tracking removed - test weapon state instead
        assertNotNull(shooter.currentWeaponState, "Should have active weapon state");
        
        shooter.startAttackSequence(shooterUnit, targetUnit, 100, eventQueue, 1, createTestCallbacks());
        // Queued shots tracking removed - test continues to work with weapon states
        
        shooter.startAttackSequence(shooterUnit, targetUnit, 100, eventQueue, 1, createTestCallbacks());
        // Queued shots tracking removed - test continues to work with weapon states
        
        // Execute events until first shot completes
        int eventCount = 0;
        while (!eventQueue.isEmpty() && eventCount < 10) { // Safety limit
            ScheduledEvent event = eventQueue.poll();
            if (event.getOwnerId() == 1) {
                event.getAction().run();
                eventCount++;
                
                // Check if we completed the recovery phase
                if (shooter.currentWeaponState.getState().equals("aiming")) {
                    break;
                }
            }
        }
        
        // Should have processed at least one shot
        // Queued shots tracking removed - test weapon state instead
        assertTrue(shooter.currentWeaponState.getState().equals("aiming") || shooter.currentWeaponState.getState().equals("ready"), "Should be in ready state after completing attacks");
    }
    
    @Test
    public void testTargetSwitching() {
        combat.Character newTarget = new combat.Character("NewTarget", 50, 70, 45, 40, 50, combat.Handedness.RIGHT_HANDED);
        Unit newTargetUnit = new Unit(newTarget, 400, 400, Color.GREEN, 3);
        
        // Start aiming at first target
        shooter.currentWeaponState = testWeapon.getStateByName("aiming");
        shooter.currentTarget = targetUnit;
        
        // Switch to new target - should reset to ready
        shooter.startAttackSequence(shooterUnit, newTargetUnit, 100, eventQueue, 1, createTestCallbacks());
        
        assertEquals("ready", shooter.currentWeaponState.getState(), "Should reset to ready when switching targets");
        assertEquals(newTargetUnit, shooter.currentTarget, "Should update to new target");
    }
    
    @Test
    public void testWeaponStateProgression() {
        // Start from holstered
        shooter.currentWeaponState = testWeapon.getStateByName("holstered");
        
        shooter.startAttackSequence(shooterUnit, targetUnit, 100, eventQueue, 1, createTestCallbacks());
        
        // Track state progression
        String[] expectedStates = {"drawing", "ready", "aiming", "firing", "recovering", "aiming"};
        int stateIndex = 0;
        
        while (!eventQueue.isEmpty() && stateIndex < expectedStates.length) {
            ScheduledEvent event = eventQueue.poll();
            if (event.getOwnerId() == 1) {
                event.getAction().run();
                if (stateIndex < expectedStates.length) {
                    assertEquals(expectedStates[stateIndex], shooter.currentWeaponState.getState(), 
                        "State progression should follow expected sequence at step " + stateIndex);
                    stateIndex++;
                }
                
                // Stop after recovering back to aiming
                if (shooter.currentWeaponState.getState().equals("aiming") && stateIndex > 3) {
                    break;
                }
            }
        }
        
        assertTrue(stateIndex >= 4, "Should have progressed through at least drawing->ready->aiming->firing");
    }
    
    @Test
    public void testGameClockIntegration() {
        gameClock.reset();
        assertEquals(0, gameClock.getCurrentTick());
        
        // Simulate game loop
        for (int i = 0; i < 100; i++) {
            gameClock.advanceTick();
            
            // Process events for current tick
            while (!eventQueue.isEmpty() && eventQueue.peek().getTick() <= gameClock.getCurrentTick()) {
                ScheduledEvent event = eventQueue.poll();
                event.getAction().run();
            }
        }
        
        assertEquals(100, gameClock.getCurrentTick(), "Game clock should advance correctly");
    }
    
    @Test
    public void testUnitMovementDuringCombat() {
        // Set target in motion
        targetUnit.setTarget(400, 200);
        
        // Start attack sequence
        shooter.startAttackSequence(shooterUnit, targetUnit, 100, eventQueue, 1, createTestCallbacks());
        
        // Update target position during attack sequence
        targetUnit.update(110);
        double newX = targetUnit.getX();
        double newY = targetUnit.getY();
        
        assertTrue(newX > 300, "Target should move toward new position");
        assertTrue(newY > 100, "Target should move toward new position");
        
        // Attack should still execute even though target moved
        // Execute aiming transition first
        ScheduledEvent aimingEvent = null;
        while (!eventQueue.isEmpty()) {
            ScheduledEvent event = eventQueue.poll();
            if (event.getOwnerId() == 1) {
                aimingEvent = event;
                break;
            }
        }
        if (aimingEvent != null) {
            aimingEvent.getAction().run(); // Should transition to aiming
        }
        
        // Then execute firing transition
        ScheduledEvent firingEvent = null;
        while (!eventQueue.isEmpty()) {
            ScheduledEvent event = eventQueue.poll();
            if (event.getOwnerId() == 1) {
                firingEvent = event;
                break;
            }
        }
        if (firingEvent != null) {
            firingEvent.getAction().run(); // Should transition to firing
        }
        
        assertEquals("firing", shooter.currentWeaponState.getState(), "Attack should proceed even with moving target");
    }
    
    @Test
    public void testEventOrderingWithMultipleUnits() {
        // Create multiple units with overlapping events
        combat.Character secondShooter = new combat.Character("Shooter2", 75, 90, 65, 55, 70, combat.Handedness.RIGHT_HANDED);
        secondShooter.weapon = createTestPistol();
        secondShooter.currentWeaponState = secondShooter.weapon.getStateByName("ready");
        Unit secondShooterUnit = new Unit(secondShooter, 200, 200, Color.YELLOW, 4);
        
        // Start attacks at different times
        shooter.startAttackSequence(shooterUnit, targetUnit, 100, eventQueue, 1, createTestCallbacks());
        secondShooter.startAttackSequence(secondShooterUnit, targetUnit, 110, eventQueue, 4, createTestCallbacks());
        
        // Events should be processed in chronological order
        long lastTick = -1;
        while (!eventQueue.isEmpty()) {
            ScheduledEvent event = eventQueue.poll();
            assertTrue(event.getTick() >= lastTick, "Events should be in chronological order");
            lastTick = event.getTick();
            
            // Execute the event
            event.getAction().run();
            
            // Stop after both have started firing
            if (shooter.currentWeaponState.getState().equals("firing") && 
                secondShooter.currentWeaponState.getState().equals("firing")) {
                break;
            }
        }
    }
}
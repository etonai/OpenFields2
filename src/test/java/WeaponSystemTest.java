import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import combat.*;
import game.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.PriorityQueue;

import static org.junit.jupiter.api.Assertions.*;

public class WeaponSystemTest {
    
    private Weapon pistol;
    private Weapon rifle;
    private Weapon sheathedWeapon;
    private combat.Character testCharacter;
    private Unit testUnit;
    private MockGameCallbacks mockCallbacks;
    
    @BeforeEach
    public void setUp() {
        pistol = createPistol();
        rifle = createRifle();
        sheathedWeapon = createSheathedWeapon();
        testCharacter = new combat.Character("TestChar", 70, 100, 60, 50, 55, combat.Handedness.RIGHT_HANDED);
        testUnit = new Unit(testCharacter, 100, 100, Color.BLUE, 1);
        mockCallbacks = new MockGameCallbacks();
    }
    
    private Weapon createPistol() {
        Weapon weapon = new Weapon("Test Pistol", 600.0, 7, 6, "/test.wav", 150.0, 0, WeaponType.PISTOL);
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
    
    private Weapon createRifle() {
        Weapon weapon = new Weapon("Test Rifle", 800.0, 12, 10, "/test.wav", 300.0, 5, WeaponType.RIFLE);
        weapon.states = new ArrayList<>();
        weapon.states.add(new WeaponState("slung", "unsling", 0));
        weapon.states.add(new WeaponState("unsling", "ready", 90));
        weapon.states.add(new WeaponState("ready", "aiming", 15));
        weapon.states.add(new WeaponState("aiming", "firing", 60));
        weapon.states.add(new WeaponState("firing", "recovering", 5));
        weapon.states.add(new WeaponState("recovering", "aiming", 20));
        weapon.initialStateName = "slung";
        return weapon;
    }
    
    private Weapon createSheathedWeapon() {
        Weapon weapon = new Weapon("Magic Wand", 30.0, 8, 20, "/magic.wav", 100.0, 20, WeaponType.OTHER);
        weapon.states = new ArrayList<>();
        weapon.states.add(new WeaponState("sheathed", "unsheathing", 0));
        weapon.states.add(new WeaponState("unsheathing", "ready", 25));
        weapon.states.add(new WeaponState("ready", "aiming", 10));
        weapon.states.add(new WeaponState("aiming", "firing", 45));
        weapon.states.add(new WeaponState("firing", "recovering", 8));
        weapon.states.add(new WeaponState("recovering", "aiming", 20));
        weapon.initialStateName = "sheathed";
        return weapon;
    }
    
    private static class MockGameCallbacks implements GameCallbacks {
        public boolean soundPlayed = false;
        public boolean projectileScheduled = false;
        public Weapon lastWeaponSound = null;
        public Unit lastShooter = null;
        public Unit lastTarget = null;
        
        @Override
        public void playWeaponSound(Weapon weapon) {
            soundPlayed = true;
            lastWeaponSound = weapon;
        }
        
        @Override
        public void scheduleProjectileImpact(Unit shooter, Unit target, Weapon weapon, long fireTick, double distanceFeet) {
            projectileScheduled = true;
            lastShooter = shooter;
            lastTarget = target;
        }
    }
    
    @Test
    public void testWeapon_Construction() {
        assertEquals("Test Pistol", pistol.name);
        assertEquals(600.0, pistol.velocityFeetPerSecond);
        assertEquals(7, pistol.damage);
        assertEquals(6, pistol.ammunition);
        assertEquals("/test.wav", pistol.soundFile);
        assertEquals(150.0, pistol.maximumRange);
        assertEquals(0, pistol.weaponAccuracy);
    }
    
    @Test
    public void testWeaponState_Construction() {
        WeaponState state = new WeaponState("ready", "aiming", 15);
        assertEquals("ready", state.getState());
        assertEquals("aiming", state.getAction());
        assertEquals(15, state.ticks);
    }
    
    @Test
    public void testWeapon_GetStateByName() {
        WeaponState readyState = pistol.getStateByName("ready");
        assertNotNull(readyState, "Should find ready state");
        assertEquals("ready", readyState.getState());
        assertEquals("aiming", readyState.getAction());
        assertEquals(15, readyState.ticks);
    }
    
    @Test
    public void testWeapon_GetStateByName_NotFound() {
        WeaponState invalidState = pistol.getStateByName("invalid");
        assertNull(invalidState, "Should return null for invalid state name");
    }
    
    @Test
    public void testWeapon_GetInitialState() {
        WeaponState initialState = pistol.getInitialState();
        assertNotNull(initialState, "Should have initial state");
        assertEquals("holstered", initialState.getState());
        
        WeaponState rifleInitial = rifle.getInitialState();
        assertEquals("slung", rifleInitial.getState());
        
        WeaponState sheathedInitial = sheathedWeapon.getInitialState();
        assertEquals("sheathed", sheathedInitial.getState());
    }
    
    @Test
    public void testWeapon_GetNextState() {
        WeaponState readyState = pistol.getStateByName("ready");
        WeaponState nextState = pistol.getNextState(readyState);
        
        assertNotNull(nextState, "Should find next state");
        assertEquals("aiming", nextState.getState());
    }
    
    @Test
    public void testCharacter_CanFire_WhenAiming() {
        testCharacter.weapon = pistol;
        testCharacter.currentWeaponState = pistol.getStateByName("aiming");
        
        assertTrue(testCharacter.canFire(), "Character should be able to fire when aiming");
    }
    
    @Test
    public void testCharacter_CanFire_WhenNotAiming() {
        testCharacter.weapon = pistol;
        testCharacter.currentWeaponState = pistol.getStateByName("ready");
        
        assertFalse(testCharacter.canFire(), "Character should not be able to fire when not aiming");
    }
    
    @Test
    public void testCharacter_CanFire_NoWeapon() {
        testCharacter.weapon = null;
        testCharacter.currentWeaponState = null;
        
        assertFalse(testCharacter.canFire(), "Character should not be able to fire without weapon");
    }
    
    @Test
    public void testCharacter_StartReadyWeaponSequence_AlreadyReady() {
        testCharacter.weapon = pistol;
        testCharacter.currentWeaponState = pistol.getStateByName("ready");
        
        PriorityQueue<ScheduledEvent> eventQueue = new PriorityQueue<>();
        testCharacter.startReadyWeaponSequence(testUnit, 100, eventQueue, 1);
        
        assertTrue(eventQueue.isEmpty(), "No events should be scheduled when weapon is already ready");
    }
    
    @Test
    public void testCharacter_StartReadyWeaponSequence_FromHolstered() {
        testCharacter.weapon = pistol;
        testCharacter.currentWeaponState = pistol.getStateByName("holstered");
        
        PriorityQueue<ScheduledEvent> eventQueue = new PriorityQueue<>();
        testCharacter.startReadyWeaponSequence(testUnit, 100, eventQueue, 1);
        
        assertFalse(eventQueue.isEmpty(), "Events should be scheduled to ready weapon from holstered");
        
        // Execute first event (drawing)
        ScheduledEvent firstEvent = eventQueue.poll();
        assertEquals(100, firstEvent.getTick(), "First event should be immediate (tick 100)");
        firstEvent.getAction().run();
        assertEquals("drawing", testCharacter.currentWeaponState.getState(), "Should transition to drawing");
        
        // Execute second event (ready)
        ScheduledEvent secondEvent = eventQueue.poll();
        assertEquals(130, secondEvent.getTick(), "Second event should be at tick 130 (100 + 30)");
        secondEvent.getAction().run();
        assertEquals("ready", testCharacter.currentWeaponState.getState(), "Should transition to ready");
    }
    
    @Test
    public void testCharacter_StartAttackSequence_FromReady() {
        testCharacter.weapon = pistol;
        testCharacter.currentWeaponState = pistol.getStateByName("ready");
        
        Unit target = new Unit(new combat.Character("Target", 50, 80, 50, 45, 55, combat.Handedness.RIGHT_HANDED), 200, 200, Color.RED, 2);
        PriorityQueue<ScheduledEvent> eventQueue = new PriorityQueue<>();
        
        testCharacter.startAttackSequence(testUnit, target, 100, eventQueue, 1, mockCallbacks);
        
        assertFalse(eventQueue.isEmpty(), "Events should be scheduled for attack sequence");
        assertEquals(1, testCharacter.queuedShots, "Should have 1 queued shot");
        
        // Execute aiming transition
        ScheduledEvent aimingEvent = eventQueue.poll();
        assertEquals(115, aimingEvent.getTick(), "Aiming should start at tick 115 (100 + 15)");
        aimingEvent.getAction().run();
        assertEquals("aiming", testCharacter.currentWeaponState.getState(), "Should transition to aiming");
        
        // Execute firing
        ScheduledEvent firingEvent = eventQueue.poll();
        assertEquals(175, firingEvent.getTick(), "Firing should occur at tick 175 (115 + 60)");
        firingEvent.getAction().run();
        assertEquals("firing", testCharacter.currentWeaponState.getState(), "Should transition to firing");
        assertTrue(mockCallbacks.soundPlayed, "Should play weapon sound");
        assertTrue(mockCallbacks.projectileScheduled, "Should schedule projectile impact");
        assertEquals(5, pistol.ammunition, "Ammunition should decrease from 6 to 5");
    }
    
    @Test
    public void testCharacter_StartAttackSequence_OutOfAmmo() {
        testCharacter.weapon = pistol;
        testCharacter.currentWeaponState = pistol.getStateByName("aiming");
        pistol.ammunition = 0; // No ammo
        
        Unit target = new Unit(new combat.Character("Target", 50, 80, 50, 45, 55, combat.Handedness.RIGHT_HANDED), 200, 200, Color.RED, 2);
        PriorityQueue<ScheduledEvent> eventQueue = new PriorityQueue<>();
        
        testCharacter.startAttackSequence(testUnit, target, 100, eventQueue, 1, mockCallbacks);
        
        // Execute firing event
        ScheduledEvent firingEvent = eventQueue.poll();
        firingEvent.getAction().run();
        
        assertFalse(mockCallbacks.soundPlayed, "Should not play sound when out of ammo");
        assertFalse(mockCallbacks.projectileScheduled, "Should not schedule projectile when out of ammo");
        assertEquals(0, pistol.ammunition, "Ammunition should remain 0");
    }
    
    @Test
    public void testCharacter_QueuedShots() {
        testCharacter.weapon = pistol;
        testCharacter.currentWeaponState = pistol.getStateByName("aiming");
        
        Unit target = new Unit(new combat.Character("Target", 50, 80, 50, 45, 55, combat.Handedness.RIGHT_HANDED), 200, 200, Color.RED, 2);
        PriorityQueue<ScheduledEvent> eventQueue = new PriorityQueue<>();
        
        // Start first attack
        testCharacter.startAttackSequence(testUnit, target, 100, eventQueue, 1, mockCallbacks);
        assertEquals(1, testCharacter.queuedShots, "Should have 1 queued shot");
        
        // Start second attack while first is processing
        testCharacter.startAttackSequence(testUnit, target, 100, eventQueue, 1, mockCallbacks);
        assertEquals(2, testCharacter.queuedShots, "Should have 2 queued shots");
        
        // Start third attack
        testCharacter.startAttackSequence(testUnit, target, 100, eventQueue, 1, mockCallbacks);
        assertEquals(3, testCharacter.queuedShots, "Should have 3 queued shots");
    }
    
    @Test
    public void testCharacter_TargetChange_ResetsToReady() {
        testCharacter.weapon = pistol;
        testCharacter.currentWeaponState = pistol.getStateByName("aiming");
        
        Unit firstTarget = new Unit(new combat.Character("Target1", 50, 80, 50, 45, 55, combat.Handedness.RIGHT_HANDED), 200, 200, Color.RED, 2);
        Unit secondTarget = new Unit(new combat.Character("Target2", 60, 90, 55, 50, 60, combat.Handedness.RIGHT_HANDED), 300, 300, Color.GREEN, 3);
        
        testCharacter.currentTarget = firstTarget;
        
        PriorityQueue<ScheduledEvent> eventQueue = new PriorityQueue<>();
        testCharacter.startAttackSequence(testUnit, secondTarget, 100, eventQueue, 1, mockCallbacks);
        
        assertEquals("ready", testCharacter.currentWeaponState.getState(), "Should reset to ready when target changes");
        assertEquals(secondTarget, testCharacter.currentTarget, "Should update current target");
    }
    
    @Test
    public void testRifle_StateFlow() {
        testCharacter.weapon = rifle;
        testCharacter.currentWeaponState = rifle.getStateByName("slung");
        
        PriorityQueue<ScheduledEvent> eventQueue = new PriorityQueue<>();
        testCharacter.startReadyWeaponSequence(testUnit, 100, eventQueue, 1);
        
        // Execute unsling
        ScheduledEvent unslingEvent = eventQueue.poll();
        unslingEvent.getAction().run();
        assertEquals("unsling", testCharacter.currentWeaponState.getState(), "Should transition to unsling");
        
        // Execute ready
        ScheduledEvent readyEvent = eventQueue.poll();
        assertEquals(190, readyEvent.getTick(), "Ready should occur at tick 190 (100 + 90)");
        readyEvent.getAction().run();
        assertEquals("ready", testCharacter.currentWeaponState.getState(), "Should transition to ready");
    }
    
    @Test
    public void testSheathedWeapon_StateFlow() {
        testCharacter.weapon = sheathedWeapon;
        testCharacter.currentWeaponState = sheathedWeapon.getStateByName("sheathed");
        
        PriorityQueue<ScheduledEvent> eventQueue = new PriorityQueue<>();
        testCharacter.startReadyWeaponSequence(testUnit, 100, eventQueue, 1);
        
        // Execute unsheathing
        ScheduledEvent unsheathingEvent = eventQueue.poll();
        unsheathingEvent.getAction().run();
        assertEquals("unsheathing", testCharacter.currentWeaponState.getState(), "Should transition to unsheathing");
        
        // Execute ready
        ScheduledEvent readyEvent = eventQueue.poll();
        assertEquals(125, readyEvent.getTick(), "Ready should occur at tick 125 (100 + 25)");
        readyEvent.getAction().run();
        assertEquals("ready", testCharacter.currentWeaponState.getState(), "Should transition to ready");
    }
    
    @Test
    public void testCharacter_StartAttackSequence_NoWeapon() {
        testCharacter.weapon = null;
        testCharacter.currentWeaponState = null;
        
        Unit target = new Unit(new combat.Character("Target", 50, 80, 50, 45, 55, combat.Handedness.RIGHT_HANDED), 200, 200, Color.RED, 2);
        PriorityQueue<ScheduledEvent> eventQueue = new PriorityQueue<>();
        
        testCharacter.startAttackSequence(testUnit, target, 100, eventQueue, 1, mockCallbacks);
        
        assertTrue(eventQueue.isEmpty(), "No events should be scheduled without weapon");
        assertEquals(0, testCharacter.queuedShots, "Should not queue shots without weapon");
    }
    
    @Test
    public void testCharacter_StartReadyWeaponSequence_NoWeapon() {
        testCharacter.weapon = null;
        testCharacter.currentWeaponState = null;
        
        PriorityQueue<ScheduledEvent> eventQueue = new PriorityQueue<>();
        testCharacter.startReadyWeaponSequence(testUnit, 100, eventQueue, 1);
        
        assertTrue(eventQueue.isEmpty(), "No events should be scheduled without weapon");
    }
    
    @Test
    public void testWeapon_TypesCorrect() {
        assertEquals(WeaponType.PISTOL, pistol.getWeaponType(), "Pistol should have PISTOL type");
        assertEquals(WeaponType.RIFLE, rifle.getWeaponType(), "Rifle should have RIFLE type");
        assertEquals(WeaponType.OTHER, sheathedWeapon.getWeaponType(), "Sheathed weapon should have OTHER type");
    }
    
    @Test
    public void testWeapon_DefaultConstructorType() {
        Weapon defaultWeapon = new Weapon("Default", 500.0, 5, 10, "/default.wav", 100.0, 0);
        assertEquals(WeaponType.OTHER, defaultWeapon.getWeaponType(), "Default constructor should set weapon type to OTHER");
    }
    
    @Test
    public void testWeapon_ExplicitTypeConstructor() {
        Weapon pistolWeapon = new Weapon("Custom Pistol", 700.0, 8, 12, "/custom.wav", 200.0, 5, WeaponType.PISTOL);
        Weapon rifleWeapon = new Weapon("Custom Rifle", 900.0, 15, 8, "/custom.wav", 400.0, 10, WeaponType.RIFLE);
        Weapon otherWeapon = new Weapon("Custom Other", 400.0, 6, 20, "/custom.wav", 150.0, 0, WeaponType.OTHER);
        
        assertEquals(WeaponType.PISTOL, pistolWeapon.getWeaponType(), "Should set PISTOL type correctly");
        assertEquals(WeaponType.RIFLE, rifleWeapon.getWeaponType(), "Should set RIFLE type correctly");
        assertEquals(WeaponType.OTHER, otherWeapon.getWeaponType(), "Should set OTHER type correctly");
    }
    
    @Test
    public void testWeaponType_EnumProperties() {
        assertEquals("Pistol", WeaponType.PISTOL.getDisplayName());
        assertEquals("Rifle", WeaponType.RIFLE.getDisplayName());
        assertEquals("Other", WeaponType.OTHER.getDisplayName());
    }
}
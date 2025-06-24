import combat.*;
import game.ScheduledEvent;
import game.GameCallbacks;
import game.PlatformUnit;
import platform.api.Color;
import java.util.PriorityQueue;

public class TestBurstFiring {
    public static void main(String[] args) {
        System.out.println("=== Testing Burst Firing ===");
        
        // Create test character with UZI
        Character shooter = new Character("Test Shooter", 25, Archetype.SOLDIER, 
            75, 75, 75, 100, 75);
        
        // Load UZI weapon
        RangedWeapon uzi = RangedWeapon.loadFromId("wpn_uzi");
        shooter.setWeapon(uzi);
        
        // Set to burst mode
        while (uzi.getCurrentFiringMode() != FiringMode.BURST) {
            uzi.cycleFiringMode();
        }
        
        System.out.println("Weapon: " + uzi.getName());
        System.out.println("Firing Mode: " + uzi.getCurrentFiringMode());
        System.out.println("Burst Size: " + uzi.getBurstSize());
        System.out.println("Firing Delay: " + uzi.getFiringDelay());
        System.out.println("Cyclic Rate: " + uzi.getCyclicRate());
        System.out.println("Current Ammo: " + uzi.getAmmunition());
        
        // Create target
        Character target = new Character("Target", 25, Archetype.SOLDIER,
            75, 75, 75, 100, 75);
        
        // Create units
        PlatformUnit shooterUnit = new PlatformUnit(shooter, 100, 100, Color.BLUE, 1);
        PlatformUnit targetUnit = new PlatformUnit(target, 200, 100, Color.RED, 2);
        
        // Create event queue
        PriorityQueue<ScheduledEvent> eventQueue = new PriorityQueue<>();
        
        // Create mock game callbacks
        GameCallbacks mockCallbacks = new GameCallbacks() {
            @Override
            public void playWeaponSound(Weapon weapon) {
                System.out.println("[SOUND] Playing weapon sound: " + weapon.name);
            }
            
            @Override
            public void applyFiringHighlight(game.Unit unit, long tick) {
                System.out.println("[VISUAL] Firing highlight at tick " + tick);
            }
            
            @Override
            public void addMuzzleFlash(game.Unit unit, long tick) {
                System.out.println("[VISUAL] Muzzle flash at tick " + tick);
            }
            
            @Override
            public void scheduleProjectileImpact(game.Unit shooter, game.Unit target, 
                                                  Weapon weapon, long fireTick, double distanceFeet) {
                System.out.println("[PROJECTILE] Scheduled impact at distance " + 
                                   String.format("%.1f", distanceFeet) + " feet");
            }
            
            @Override
            public void removeAllEventsForOwner(int ownerId) {
                System.out.println("[EVENT] Removing all events for owner " + ownerId);
            }
        };
        
        // Start attack sequence
        System.out.println("\n=== Starting Burst Attack ===");
        shooter.startAttackSequence(shooterUnit, targetUnit, 1000, eventQueue, 1, mockCallbacks);
        
        // Process events
        long currentTick = 1000;
        while (!eventQueue.isEmpty() && currentTick < 1500) {
            ScheduledEvent event = eventQueue.peek();
            if (event != null && event.getTick() <= currentTick) {
                eventQueue.poll();
                System.out.println("\n[TICK " + currentTick + "] Processing event");
                event.execute();
            }
            currentTick++;
        }
        
        System.out.println("\n=== Final State ===");
        System.out.println("Ammo remaining: " + uzi.getAmmunition());
        System.out.println("Shots fired: " + (32 - uzi.getAmmunition()));
    }
}
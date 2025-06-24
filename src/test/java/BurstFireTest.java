import combat.*;
import game.ScheduledEvent;
import java.util.PriorityQueue;
import java.util.Comparator;

public class BurstFireTest {
    public static void main(String[] args) {
        System.out.println("=== BURST FIRING COMPREHENSIVE TEST ===\n");
        
        // Test 1: Basic burst timing
        testBurstTiming();
        
        // Test 2: Burst with insufficient ammo
        testBurstWithLowAmmo();
        
        // Test 3: Mode switch interruption
        testModeSwitchInterruption();
        
        // Test 4: Full auto timing
        testFullAutoTiming();
        
        System.out.println("\n=== ALL TESTS COMPLETE ===");
    }
    
    private static void testBurstTiming() {
        System.out.println("TEST 1: Burst Timing (should use firingDelay)\n");
        
        Character shooter = new Character("Shooter", 25, Archetype.SOLDIER, 75, 75, 75, 100, 75);
        RangedWeapon uzi = createTestUzi();
        shooter.setWeapon(uzi);
        
        // Set to burst mode
        while (uzi.getCurrentFiringMode() != FiringMode.BURST) {
            uzi.cycleFiringMode();
        }
        
        System.out.println("Weapon: " + uzi.getName());
        System.out.println("Firing Mode: " + uzi.getCurrentFiringMode());
        System.out.println("Burst Size: " + uzi.getBurstSize());
        System.out.println("Firing Delay: " + uzi.getFiringDelay());
        System.out.println("Expected timing: Bullet 1 at tick 0, Bullet 2 at tick 6, Bullet 3 at tick 12");
        
        // Simulate firing
        shooter.isAutomaticFiring = true;
        shooter.burstShotsFired = 1;
        
        // Check shouldApplyBurstAutoPenalty
        System.out.println("\nBullet 1 - Burst penalty: " + shooter.shouldApplyBurstAutoPenalty());
        shooter.burstShotsFired = 2;
        System.out.println("Bullet 2 - Burst penalty: " + shooter.shouldApplyBurstAutoPenalty());
        shooter.burstShotsFired = 3;
        System.out.println("Bullet 3 - Burst penalty: " + shooter.shouldApplyBurstAutoPenalty());
        
        System.out.println("\n---\n");
    }
    
    private static void testBurstWithLowAmmo() {
        System.out.println("TEST 2: Burst with Insufficient Ammo\n");
        
        Character shooter = new Character("Shooter", 25, Archetype.SOLDIER, 75, 75, 75, 100, 75);
        RangedWeapon uzi = createTestUzi();
        uzi.setAmmunition(2); // Only 2 rounds for 3-round burst
        shooter.setWeapon(uzi);
        
        // Set to burst mode
        while (uzi.getCurrentFiringMode() != FiringMode.BURST) {
            uzi.cycleFiringMode();
        }
        
        System.out.println("Weapon: " + uzi.getName());
        System.out.println("Ammo: " + uzi.getAmmunition() + " (burst size: " + uzi.getBurstSize() + ")");
        System.out.println("Expected: Should fire 2 bullets then stop gracefully");
        
        System.out.println("\n---\n");
    }
    
    private static void testModeSwitchInterruption() {
        System.out.println("TEST 3: Mode Switch Interruption\n");
        
        Character shooter = new Character("Shooter", 25, Archetype.SOLDIER, 75, 75, 75, 100, 75);
        RangedWeapon uzi = createTestUzi();
        shooter.setWeapon(uzi);
        
        // Set to burst mode
        while (uzi.getCurrentFiringMode() != FiringMode.BURST) {
            uzi.cycleFiringMode();
        }
        
        // Simulate burst in progress
        shooter.isAutomaticFiring = true;
        shooter.burstShotsFired = 2;
        
        System.out.println("Before mode switch:");
        System.out.println("- Firing Mode: " + uzi.getCurrentFiringMode());
        System.out.println("- isAutomaticFiring: " + shooter.isAutomaticFiring);
        System.out.println("- burstShotsFired: " + shooter.burstShotsFired);
        
        // Switch mode
        shooter.cycleFiringMode();
        
        System.out.println("\nAfter mode switch:");
        System.out.println("- Firing Mode: " + uzi.getCurrentFiringMode());
        System.out.println("- isAutomaticFiring: " + shooter.isAutomaticFiring);
        System.out.println("- burstShotsFired: " + shooter.burstShotsFired);
        
        System.out.println("\n---\n");
    }
    
    private static void testFullAutoTiming() {
        System.out.println("TEST 4: Full Auto Timing (should use firingDelay)\n");
        
        Character shooter = new Character("Shooter", 25, Archetype.SOLDIER, 75, 75, 75, 100, 75);
        RangedWeapon uzi = createTestUzi();
        shooter.setWeapon(uzi);
        
        // Set to full auto mode
        while (uzi.getCurrentFiringMode() != FiringMode.FULL_AUTO) {
            uzi.cycleFiringMode();
        }
        
        System.out.println("Weapon: " + uzi.getName());
        System.out.println("Firing Mode: " + uzi.getCurrentFiringMode());
        System.out.println("Firing Delay: " + uzi.getFiringDelay());
        System.out.println("Expected: Continuous fire every 6 ticks");
        
        // Simulate full auto
        shooter.isAutomaticFiring = true;
        shooter.burstShotsFired = 1;
        
        System.out.println("\nFull auto penalties:");
        for (int i = 1; i <= 5; i++) {
            shooter.burstShotsFired = i;
            System.out.println("Bullet " + i + " - Burst/Auto penalty: " + shooter.shouldApplyBurstAutoPenalty());
        }
        
        System.out.println("\n---\n");
    }
    
    private static RangedWeapon createTestUzi() {
        RangedWeapon uzi = new RangedWeapon();
        uzi.name = "Uzi Submachine Gun";
        uzi.damage = 30;
        uzi.weaponAccuracy = 0;
        uzi.setAmmunition(32);
        uzi.setMaxAmmunition(32);
        uzi.setFiringDelay(6);
        uzi.setCyclicRate(6);
        uzi.setBurstSize(3);
        uzi.setVelocity(1300.0);
        uzi.setMaximumRange(200.0);
        
        // Add firing modes
        uzi.addAvailableFiringMode(FiringMode.SINGLE_SHOT);
        uzi.addAvailableFiringMode(FiringMode.BURST);
        uzi.addAvailableFiringMode(FiringMode.FULL_AUTO);
        
        return uzi;
    }
}
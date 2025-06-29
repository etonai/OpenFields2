import combat.Character;
import combat.Handedness;
import combat.RangedWeapon;
import combat.FiringMode;
import combat.WeaponState;
import combat.managers.BurstFireManager;
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
        
        Character shooter = new Character("Shooter", 75, 100, 75, 75, 75, Handedness.RIGHT_HANDED);
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
        BurstFireManager.getInstance().setAutomaticFiring(shooter.id, true);
        BurstFireManager.getInstance().setBurstShotsFired(shooter.id, 1);
        
        // Check shouldApplyBurstAutoPenalty
        System.out.println("\nBullet 1 - Burst penalty: " + BurstFireManager.getInstance().shouldApplyBurstAutoPenalty(shooter.id));
        BurstFireManager.getInstance().setBurstShotsFired(shooter.id, 2);
        System.out.println("Bullet 2 - Burst penalty: " + BurstFireManager.getInstance().shouldApplyBurstAutoPenalty(shooter.id));
        BurstFireManager.getInstance().setBurstShotsFired(shooter.id, 3);
        System.out.println("Bullet 3 - Burst penalty: " + BurstFireManager.getInstance().shouldApplyBurstAutoPenalty(shooter.id));
        
        System.out.println("\n---\n");
    }
    
    private static void testBurstWithLowAmmo() {
        System.out.println("TEST 2: Burst with Insufficient Ammo\n");
        
        Character shooter = new Character("Shooter", 75, 100, 75, 75, 75, Handedness.RIGHT_HANDED);
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
        
        Character shooter = new Character("Shooter", 75, 100, 75, 75, 75, Handedness.RIGHT_HANDED);
        RangedWeapon uzi = createTestUzi();
        shooter.setWeapon(uzi);
        
        // Set to burst mode
        while (uzi.getCurrentFiringMode() != FiringMode.BURST) {
            uzi.cycleFiringMode();
        }
        
        // Simulate burst in progress
        BurstFireManager.getInstance().setAutomaticFiring(shooter.id, true);
        BurstFireManager.getInstance().setBurstShotsFired(shooter.id, 2);
        
        System.out.println("Before mode switch:");
        System.out.println("- Firing Mode: " + uzi.getCurrentFiringMode());
        System.out.println("- isAutomaticFiring: " + BurstFireManager.getInstance().isAutomaticFiring(shooter.id));
        System.out.println("- burstShotsFired: " + BurstFireManager.getInstance().getBurstShotsFired(shooter.id));
        
        // Switch mode
        shooter.cycleFiringMode();
        
        System.out.println("\nAfter mode switch:");
        System.out.println("- Firing Mode: " + uzi.getCurrentFiringMode());
        System.out.println("- isAutomaticFiring: " + BurstFireManager.getInstance().isAutomaticFiring(shooter.id));
        System.out.println("- burstShotsFired: " + BurstFireManager.getInstance().getBurstShotsFired(shooter.id));
        
        System.out.println("\n---\n");
    }
    
    private static void testFullAutoTiming() {
        System.out.println("TEST 4: Full Auto Timing (should use firingDelay)\n");
        
        Character shooter = new Character("Shooter", 75, 100, 75, 75, 75, Handedness.RIGHT_HANDED);
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
        BurstFireManager.getInstance().setAutomaticFiring(shooter.id, true);
        BurstFireManager.getInstance().setBurstShotsFired(shooter.id, 1);
        
        System.out.println("\nFull auto penalties:");
        for (int i = 1; i <= 5; i++) {
            BurstFireManager.getInstance().setBurstShotsFired(shooter.id, i);
            System.out.println("Bullet " + i + " - Burst/Auto penalty: " + BurstFireManager.getInstance().shouldApplyBurstAutoPenalty(shooter.id));
        }
        
        System.out.println("\n---\n");
    }
    
    private static RangedWeapon createTestUzi() {
        RangedWeapon uzi = new RangedWeapon(
            "uzi_test",              // weaponId
            "Uzi Submachine Gun",    // name
            1300.0,                  // velocityFeetPerSecond
            30,                      // damage
            32,                      // ammunition
            "uzi_fire.wav",          // soundFile
            200.0,                   // maximumRange
            0,                       // weaponAccuracy
            "9mm bullet"             // projectileName
        );
        
        // Set additional properties
        uzi.setFiringDelay(6);
        uzi.setCyclicRate(6);
        uzi.setBurstSize(3);
        
        // Add firing modes
        uzi.getAvailableFiringModes().clear();
        uzi.getAvailableFiringModes().add(FiringMode.SINGLE_SHOT);
        uzi.getAvailableFiringModes().add(FiringMode.BURST);
        uzi.getAvailableFiringModes().add(FiringMode.FULL_AUTO);
        
        return uzi;
    }
}
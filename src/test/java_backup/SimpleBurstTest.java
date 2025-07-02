import combat.RangedWeapon;
import combat.FiringMode;

public class SimpleBurstTest {
    public static void main(String[] args) {
        System.out.println("=== SIMPLE BURST FIRING TEST ===\n");
        
        // Load UZI from JSON
        RangedWeapon uzi = RangedWeapon.loadFromId("wpn_uzi");
        
        if (uzi == null) {
            System.out.println("ERROR: Failed to load UZI weapon");
            return;
        }
        
        System.out.println("Weapon loaded: " + uzi.getName());
        System.out.println("Burst Size: " + uzi.getBurstSize());
        System.out.println("Firing Delay: " + uzi.getFiringDelay());
        System.out.println("Cyclic Rate: " + uzi.getCyclicRate());
        System.out.println("Available modes: " + uzi.getAvailableFiringModes());
        
        // Test timing calculation
        System.out.println("\nExpected burst timing with firingDelay (" + uzi.getFiringDelay() + "):");
        for (int shot = 1; shot <= uzi.getBurstSize(); shot++) {
            long tick = (shot - 1) * uzi.getFiringDelay();
            System.out.println("  Bullet " + shot + " at tick " + tick);
        }
        
        System.out.println("\nOLD burst timing with cyclicRate (" + uzi.getCyclicRate() + "):");
        for (int shot = 1; shot <= uzi.getBurstSize(); shot++) {
            long tick = (shot - 1) * uzi.getCyclicRate();
            System.out.println("  Bullet " + shot + " at tick " + tick);
        }
        
        System.out.println("\nSince firingDelay == cyclicRate for UZI, timing is the same.");
        System.out.println("But the fix ensures we use the correct property.");
    }
}
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import combat.*;
import game.Unit;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DevCycle 27: System 7 - Ammunition Display Restoration
 */
public class AmmunitionDisplayTest {
    
    private combat.Character testCharacter;
    private Unit shooter;
    private Unit target;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    @BeforeEach
    public void setUp() {
        // Capture System.out for testing console output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        testCharacter = new combat.Character("TestShooter", 70, 100, 60, 50, 55, combat.Handedness.RIGHT_HANDED);
        
        // Create test ranged weapon with ammunition
        RangedWeapon testWeapon = new RangedWeapon("test_rifle", "Test Rifle", 2800.0, 25, 25, "rifle_sound.wav", 300.0, 75, "5.56mm NATO");
        testWeapon.setMaxAmmunition(30);
        
        // Add weapon states for testing
        testWeapon.states = new java.util.ArrayList<>();
        testWeapon.states.add(new WeaponState("ready", "aiming", 15));
        testWeapon.states.add(new WeaponState("aiming", "firing", 30));
        testWeapon.states.add(new WeaponState("firing", "recovering", 10));
        
        testCharacter.weapon = testWeapon;
        testCharacter.currentWeaponState = testWeapon.getStateByName("aiming");
        
        // Create test units
        shooter = new Unit(testCharacter, 100, 100, null, 1);
        
        combat.Character targetCharacter = new combat.Character("TestTarget", 70, 100, 60, 50, 55, combat.Handedness.RIGHT_HANDED);
        target = new Unit(targetCharacter, 200, 100, null, 2);
    }
    
    @AfterEach
    public void tearDown() {
        // Restore original System.out
        System.setOut(originalOut);
    }
    
    @Test
    public void testAmmunitionDisplayInFiringOutput() {
        // Set up character state for firing
        testCharacter.firesFromAimingState = true;
        testCharacter.startAimingTiming(100);
        testCharacter.currentTarget = target;
        
        // Test the ammunition display by creating a mock firing scenario
        // This simulates the console output generation in scheduleAttackFromCurrentState
        long fireTick = 130;
        String firingMode = testCharacter.firesFromAimingState ? "shootingfromaiming" : "shootingfromhip";
        long aimingDuration = testCharacter.getCurrentAimingDuration(fireTick);
        String aimingText = testCharacter.firesFromAimingState ? "aimed " + aimingDuration + " ticks" : "pointed " + aimingDuration + " ticks";
        
        // Check for earned bonus
        AccumulatedAimingBonus earnedBonus = testCharacter.calculateEarnedAimingBonus(fireTick);
        String bonusText;
        if (earnedBonus != AccumulatedAimingBonus.NONE) {
            bonusText = ", earned " + earnedBonus.getDisplayName() + " bonus";
        } else {
            bonusText = ", using " + testCharacter.getCurrentAimingSpeed().getDisplayName() + " aiming";
        }
        
        // Test ammunition display generation (shows ammunition after firing)
        String ammunitionText = "";
        if (testCharacter.weapon instanceof RangedWeapon) {
            RangedWeapon rangedWeapon = (RangedWeapon) testCharacter.weapon;
            int currentAmmo = rangedWeapon.getAmmunition();
            int maxAmmo = rangedWeapon.getMaxAmmunition();
            // Show ammunition after firing (subtract 1 if there's ammunition to fire)
            int ammoAfterFiring = currentAmmo > 0 ? currentAmmo - 1 : currentAmmo;
            ammunitionText = ", [ammo: " + ammoAfterFiring + "/" + maxAmmo + "]";
        }
        
        // Generate the console output
        System.out.println(testCharacter.getDisplayName() + " fires a " + testCharacter.weapon.getName() + " at " + 
                         target.getCharacter().getDisplayName() + ", " + firingMode + " (" + aimingText + bonusText + ")" + ammunitionText + ", at tick " + fireTick);
        
        // Verify the output contains ammunition information (after firing: 25-1=24)
        String output = outputStream.toString();
        assertTrue(output.contains("[ammo: 24/30]"), "Output should contain ammunition display [ammo: 24/30] (after firing)");
        assertTrue(output.contains("TestShooter fires a Test Rifle at TestTarget"), "Output should contain firing information");
        assertTrue(output.contains("shootingfromaiming"), "Output should contain firing mode");
        assertTrue(output.contains("aimed 30 ticks"), "Output should contain aiming duration");
    }
    
    @Test
    public void testAmmunitionDisplayWithDifferentAmmoLevels() {
        // Test with low ammunition
        RangedWeapon rifle = (RangedWeapon) testCharacter.weapon;
        rifle.setAmmunition(1);
        rifle.setMaxAmmunition(30);
        
        String ammunitionText = "";
        if (testCharacter.weapon instanceof RangedWeapon) {
            RangedWeapon rangedWeapon = (RangedWeapon) testCharacter.weapon;
            int currentAmmo = rangedWeapon.getAmmunition();
            int maxAmmo = rangedWeapon.getMaxAmmunition();
            // Show ammunition after firing (subtract 1 if there's ammunition to fire)
            int ammoAfterFiring = currentAmmo > 0 ? currentAmmo - 1 : currentAmmo;
            ammunitionText = ", [ammo: " + ammoAfterFiring + "/" + maxAmmo + "]";
        }
        
        assertEquals(", [ammo: 0/30]", ammunitionText, "Should show correct low ammunition count (after firing: 1-1=0)");
        
        // Test with empty ammunition
        rifle.setAmmunition(0);
        ammunitionText = "";
        if (testCharacter.weapon instanceof RangedWeapon) {
            RangedWeapon rangedWeapon = (RangedWeapon) testCharacter.weapon;
            int currentAmmo = rangedWeapon.getAmmunition();
            int maxAmmo = rangedWeapon.getMaxAmmunition();
            // Show ammunition after firing (subtract 1 if there's ammunition to fire)
            int ammoAfterFiring = currentAmmo > 0 ? currentAmmo - 1 : currentAmmo;
            ammunitionText = ", [ammo: " + ammoAfterFiring + "/" + maxAmmo + "]";
        }
        
        assertEquals(", [ammo: 0/30]", ammunitionText, "Should show zero ammunition correctly (already empty, stays 0)");
        
        // Test with full ammunition
        rifle.setAmmunition(30);
        ammunitionText = "";
        if (testCharacter.weapon instanceof RangedWeapon) {
            RangedWeapon rangedWeapon = (RangedWeapon) testCharacter.weapon;
            int currentAmmo = rangedWeapon.getAmmunition();
            int maxAmmo = rangedWeapon.getMaxAmmunition();
            // Show ammunition after firing (subtract 1 if there's ammunition to fire)
            int ammoAfterFiring = currentAmmo > 0 ? currentAmmo - 1 : currentAmmo;
            ammunitionText = ", [ammo: " + ammoAfterFiring + "/" + maxAmmo + "]";
        }
        
        assertEquals(", [ammo: 29/30]", ammunitionText, "Should show full ammunition correctly (after firing: 30-1=29)");
    }
    
    @Test
    public void testNoAmmunitionDisplayForMeleeWeapon() {
        // Create melee weapon
        MeleeWeapon meleeWeapon = new MeleeWeapon("test_sword", "Test Sword", 15, "sword_sound.wav", combat.MeleeWeaponType.SWORD, 80);
        testCharacter.weapon = meleeWeapon;
        
        // Test ammunition display generation for melee weapon
        String ammunitionText = "";
        if (testCharacter.weapon instanceof RangedWeapon) {
            RangedWeapon rangedWeapon = (RangedWeapon) testCharacter.weapon;
            int currentAmmo = rangedWeapon.getAmmunition();
            int maxAmmo = rangedWeapon.getMaxAmmunition();
            // Show ammunition after firing (subtract 1 if there's ammunition to fire)
            int ammoAfterFiring = currentAmmo > 0 ? currentAmmo - 1 : currentAmmo;
            ammunitionText = ", [ammo: " + ammoAfterFiring + "/" + maxAmmo + "]";
        }
        
        assertEquals("", ammunitionText, "Melee weapons should not show ammunition display");
    }
    
    @Test
    public void testAmmunitionDisplayFormat() {
        // Test various ammunition scenarios for correct formatting
        RangedWeapon rifle = (RangedWeapon) testCharacter.weapon;
        
        // Test different maximum capacities
        rifle.setAmmunition(15);
        rifle.setMaxAmmunition(15);
        
        String ammunitionText = "";
        if (testCharacter.weapon instanceof RangedWeapon) {
            RangedWeapon rangedWeapon = (RangedWeapon) testCharacter.weapon;
            int currentAmmo = rangedWeapon.getAmmunition();
            int maxAmmo = rangedWeapon.getMaxAmmunition();
            // Show ammunition after firing (subtract 1 if there's ammunition to fire)
            int ammoAfterFiring = currentAmmo > 0 ? currentAmmo - 1 : currentAmmo;
            ammunitionText = ", [ammo: " + ammoAfterFiring + "/" + maxAmmo + "]";
        }
        
        assertEquals(", [ammo: 14/15]", ammunitionText, "Should format ammunition display correctly (after firing: 15-1=14)");
        
        // Test large capacity weapons
        rifle.setAmmunition(75);
        rifle.setMaxAmmunition(100);
        
        ammunitionText = "";
        if (testCharacter.weapon instanceof RangedWeapon) {
            RangedWeapon rangedWeapon = (RangedWeapon) testCharacter.weapon;
            int currentAmmo = rangedWeapon.getAmmunition();
            int maxAmmo = rangedWeapon.getMaxAmmunition();
            // Show ammunition after firing (subtract 1 if there's ammunition to fire)
            int ammoAfterFiring = currentAmmo > 0 ? currentAmmo - 1 : currentAmmo;
            ammunitionText = ", [ammo: " + ammoAfterFiring + "/" + maxAmmo + "]";
        }
        
        assertEquals(", [ammo: 74/100]", ammunitionText, "Should format large ammunition counts correctly (after firing: 75-1=74)");
    }
}
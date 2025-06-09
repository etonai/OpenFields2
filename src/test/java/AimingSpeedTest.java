import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import combat.*;
import game.*;
import javafx.scene.paint.Color;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class AimingSpeedTest {
    
    private combat.Character testCharacter;
    private Unit testUnit;
    
    @BeforeEach
    public void setUp() {
        testCharacter = new combat.Character("TestChar", 70, 100, 60);
        testUnit = new Unit(testCharacter, 100, 100, Color.BLUE, 1);
    }
    
    @Test
    public void testAimingSpeed_DefaultValue() {
        assertEquals(AimingSpeed.NORMAL, testCharacter.getCurrentAimingSpeed(), "Character should start with NORMAL aiming speed");
    }
    
    @Test
    public void testAimingSpeed_SettersAndGetters() {
        testCharacter.setCurrentAimingSpeed(AimingSpeed.CAREFUL);
        assertEquals(AimingSpeed.CAREFUL, testCharacter.getCurrentAimingSpeed(), "Should be able to set aiming speed to CAREFUL");
        
        testCharacter.setCurrentAimingSpeed(AimingSpeed.QUICK);
        assertEquals(AimingSpeed.QUICK, testCharacter.getCurrentAimingSpeed(), "Should be able to set aiming speed to QUICK");
        
        testCharacter.setCurrentAimingSpeed(AimingSpeed.NORMAL);
        assertEquals(AimingSpeed.NORMAL, testCharacter.getCurrentAimingSpeed(), "Should be able to set aiming speed to NORMAL");
    }
    
    @Test
    public void testAimingSpeed_IncreaseAndDecrease() {
        // Test increasing from NORMAL
        testCharacter.increaseAimingSpeed();
        assertEquals(AimingSpeed.QUICK, testCharacter.getCurrentAimingSpeed(), "Should increase from NORMAL to QUICK");
        
        // Test at maximum boundary
        testCharacter.increaseAimingSpeed();
        assertEquals(AimingSpeed.QUICK, testCharacter.getCurrentAimingSpeed(), "Should remain at QUICK when already at maximum");
        
        // Test decreasing from QUICK
        testCharacter.decreaseAimingSpeed();
        assertEquals(AimingSpeed.NORMAL, testCharacter.getCurrentAimingSpeed(), "Should decrease from QUICK to NORMAL");
        
        // Test decreasing from NORMAL
        testCharacter.decreaseAimingSpeed();
        assertEquals(AimingSpeed.CAREFUL, testCharacter.getCurrentAimingSpeed(), "Should decrease from NORMAL to CAREFUL");
        
        // Test at minimum boundary
        testCharacter.decreaseAimingSpeed();
        assertEquals(AimingSpeed.CAREFUL, testCharacter.getCurrentAimingSpeed(), "Should remain at CAREFUL when already at minimum");
    }
    
    @Test
    public void testAimingSpeed_IncapacitatedCharacter() {
        testCharacter.setHealth(0); // Incapacitate character
        AimingSpeed originalSpeed = testCharacter.getCurrentAimingSpeed();
        
        // Should not be able to change aiming speed when incapacitated
        testCharacter.increaseAimingSpeed();
        assertEquals(originalSpeed, testCharacter.getCurrentAimingSpeed(), "Incapacitated character should not change aiming speed");
        
        testCharacter.decreaseAimingSpeed();
        assertEquals(originalSpeed, testCharacter.getCurrentAimingSpeed(), "Incapacitated character should not change aiming speed");
    }
    
    @Test
    public void testAimingSpeedEnum_Properties() {
        // Test CAREFUL properties
        assertEquals("Careful", AimingSpeed.CAREFUL.getDisplayName());
        assertEquals(2.0, AimingSpeed.CAREFUL.getTimingMultiplier(), 0.001);
        assertEquals(15.0, AimingSpeed.CAREFUL.getAccuracyModifier(), 0.001);
        
        // Test NORMAL properties
        assertEquals("Normal", AimingSpeed.NORMAL.getDisplayName());
        assertEquals(1.0, AimingSpeed.NORMAL.getTimingMultiplier(), 0.001);
        assertEquals(0.0, AimingSpeed.NORMAL.getAccuracyModifier(), 0.001);
        
        // Test QUICK properties
        assertEquals("Quick", AimingSpeed.QUICK.getDisplayName());
        assertEquals(0.5, AimingSpeed.QUICK.getTimingMultiplier(), 0.001);
        assertEquals(-20.0, AimingSpeed.QUICK.getAccuracyModifier(), 0.001);
    }
    
    @Test
    public void testAimingSpeedEnum_IncreaseAndDecrease() {
        // Test increase method
        assertEquals(AimingSpeed.NORMAL, AimingSpeed.CAREFUL.increase());
        assertEquals(AimingSpeed.QUICK, AimingSpeed.NORMAL.increase());
        assertEquals(AimingSpeed.QUICK, AimingSpeed.QUICK.increase()); // Already at max
        
        // Test decrease method
        assertEquals(AimingSpeed.CAREFUL, AimingSpeed.CAREFUL.decrease()); // Already at min
        assertEquals(AimingSpeed.CAREFUL, AimingSpeed.NORMAL.decrease());
        assertEquals(AimingSpeed.NORMAL, AimingSpeed.QUICK.decrease());
    }
    
    @Test
    public void testAimingSpeedAccuracy_Integration() {
        // Test that aiming speed affects accuracy calculation in hit determination
        try {
            Method method = OpenFields2.class.getDeclaredMethod("determineHit", Unit.class, Unit.class, double.class, double.class, int.class, int.class);
            method.setAccessible(true);
            
            Unit shooter = new Unit(testCharacter, 100, 100, Color.RED, 1);
            Unit target = new Unit(new combat.Character("Target", 50, 80, 50), 200, 200, Color.BLUE, 2);
            
            // Set up weapon for the test
            Weapon testWeapon = new Weapon("Test Gun", 600.0, 8, 10, "/test.wav", 200.0, 10);
            testWeapon.states = new ArrayList<>();
            testWeapon.states.add(new WeaponState("ready", "aiming", 15));
            testWeapon.initialStateName = "ready";
            testCharacter.setWeapon(testWeapon);
            
            // Test multiple shots with different aiming speeds to verify modifier is applied
            // Note: Due to randomness, we can't test exact hit/miss, but we can verify the method runs without error
            testCharacter.setCurrentAimingSpeed(AimingSpeed.CAREFUL);
            HitResult carefulResult = (HitResult) method.invoke(null, shooter, target, 50.0, 200.0, 10, 8);
            assertNotNull(carefulResult, "Should get a result with CAREFUL aiming speed");
            
            testCharacter.setCurrentAimingSpeed(AimingSpeed.NORMAL);
            HitResult normalResult = (HitResult) method.invoke(null, shooter, target, 50.0, 200.0, 10, 8);
            assertNotNull(normalResult, "Should get a result with NORMAL aiming speed");
            
            testCharacter.setCurrentAimingSpeed(AimingSpeed.QUICK);
            HitResult quickResult = (HitResult) method.invoke(null, shooter, target, 50.0, 200.0, 10, 8);
            assertNotNull(quickResult, "Should get a result with QUICK aiming speed");
            
        } catch (Exception e) {
            fail("Failed to test aiming speed accuracy integration: " + e.getMessage());
        }
    }
    
    @Test
    public void testAimingSpeedTiming_EdgeCases() {
        // Test timing multiplier edge cases
        assertEquals(2.0, AimingSpeed.CAREFUL.getTimingMultiplier(), "CAREFUL should have 2.0x timing (slower)");
        assertEquals(1.0, AimingSpeed.NORMAL.getTimingMultiplier(), "NORMAL should have 1.0x timing (baseline)");
        assertEquals(0.5, AimingSpeed.QUICK.getTimingMultiplier(), "QUICK should have 0.5x timing (faster)");
        
        // Verify that CAREFUL is slower than NORMAL, and NORMAL is slower than QUICK
        assertTrue(AimingSpeed.CAREFUL.getTimingMultiplier() > AimingSpeed.NORMAL.getTimingMultiplier(), 
                  "CAREFUL should be slower than NORMAL");
        assertTrue(AimingSpeed.NORMAL.getTimingMultiplier() > AimingSpeed.QUICK.getTimingMultiplier(), 
                  "NORMAL should be slower than QUICK");
    }
    
    @Test
    public void testAimingSpeedAccuracy_EdgeCases() {
        // Test accuracy modifier edge cases
        assertEquals(15.0, AimingSpeed.CAREFUL.getAccuracyModifier(), "CAREFUL should have +15 accuracy");
        assertEquals(0.0, AimingSpeed.NORMAL.getAccuracyModifier(), "NORMAL should have 0 accuracy modifier");
        assertEquals(-20.0, AimingSpeed.QUICK.getAccuracyModifier(), "QUICK should have -20 accuracy");
        
        // Verify that CAREFUL is more accurate than NORMAL, and NORMAL is more accurate than QUICK
        assertTrue(AimingSpeed.CAREFUL.getAccuracyModifier() > AimingSpeed.NORMAL.getAccuracyModifier(), 
                  "CAREFUL should be more accurate than NORMAL");
        assertTrue(AimingSpeed.NORMAL.getAccuracyModifier() > AimingSpeed.QUICK.getAccuracyModifier(), 
                  "NORMAL should be more accurate than QUICK");
    }
    
    @Test
    public void testAllConstructors_AimingSpeedInitialization() {
        // Test basic constructor
        combat.Character char1 = new combat.Character("Test1", 70, 100, 60);
        assertEquals(AimingSpeed.NORMAL, char1.getCurrentAimingSpeed(), "Basic constructor should initialize NORMAL aiming speed");
        
        // Test constructor with weapon
        Weapon weapon = new Weapon("TestWeapon", 500.0, 8, 10, "/test.wav", 200.0, 5);
        combat.Character char2 = new combat.Character("Test2", 70, 100, 60, weapon);
        assertEquals(AimingSpeed.NORMAL, char2.getCurrentAimingSpeed(), "Weapon constructor should initialize NORMAL aiming speed");
        
        // Test constructor with skills
        ArrayList<Skill> skills = new ArrayList<>();
        skills.add(new Skill("TestSkill", 75));
        combat.Character char3 = new combat.Character("Test3", 70, 100, 60, skills);
        assertEquals(AimingSpeed.NORMAL, char3.getCurrentAimingSpeed(), "Skills constructor should initialize NORMAL aiming speed");
        
        // Test constructor with weapon and skills
        combat.Character char4 = new combat.Character("Test4", 70, 100, 60, weapon, skills);
        assertEquals(AimingSpeed.NORMAL, char4.getCurrentAimingSpeed(), "Full constructor should initialize NORMAL aiming speed");
    }
}
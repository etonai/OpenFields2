import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import combat.*;
import game.*;
import platform.api.Color;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class CombatSystemTest {
    
    private combat.Character testShooter;
    private Unit testTarget;
    private Weapon testWeapon;
    
    @BeforeEach
    public void setUp() {
        testShooter = new combat.Character("TestShooter", 75, 100, 60, 55, 70, combat.Handedness.RIGHT_HANDED);
        combat.Character targetCharacter = new combat.Character("TestTarget", 50, 80, 50, 45, 60, combat.Handedness.RIGHT_HANDED);
        testTarget = new Unit(targetCharacter, 200, 200, Color.RED, 1);
        testWeapon = createTestWeapon();
    }
    
    private Weapon createTestWeapon() {
        RangedWeapon weapon = new RangedWeapon("test_gun", "Test Gun", 600.0, 10, 8, "/test.wav", 200.0, 10, "bullet");
        weapon.states = new ArrayList<>();
        weapon.states.add(new WeaponState("ready", "aiming", 15));
        weapon.states.add(new WeaponState("aiming", "firing", 60));
        weapon.initialStateName = "ready";
        return weapon;
    }
    
    @Test
    public void testPixelsToFeet_StandardConversion() {
        double result = OpenFields2.pixelsToFeet(70.0);
        assertEquals(10.0, result, 0.001, "70 pixels should equal 10 feet");
    }
    
    @Test
    public void testPixelsToFeet_ZeroDistance() {
        double result = OpenFields2.pixelsToFeet(0.0);
        assertEquals(0.0, result, 0.001, "0 pixels should equal 0 feet");
    }
    
    @Test
    public void testCalculateRangeModifier_OptimalRange() throws Exception {
        Method method = OpenFields2.class.getDeclaredMethod("calculateRangeModifier", double.class, double.class);
        method.setAccessible(true);
        
        double maximumRange = 200.0;
        double optimalRange = maximumRange * 0.3; // 60 feet
        double distanceAtOptimal = optimalRange;
        
        double result = (Double) method.invoke(null, distanceAtOptimal, maximumRange);
        assertEquals(0.0, result, 0.001, "Distance at optimal range should give 0 modifier");
    }
    
    @Test
    public void testCalculateRangeModifier_ZeroDistance() throws Exception {
        Method method = OpenFields2.class.getDeclaredMethod("calculateRangeModifier", double.class, double.class);
        method.setAccessible(true);
        
        double result = (Double) method.invoke(null, 0.0, 200.0);
        assertEquals(10.0, result, 0.001, "Zero distance should give maximum positive modifier (+10)");
    }
    
    @Test
    public void testCalculateRangeModifier_MaximumRange() throws Exception {
        Method method = OpenFields2.class.getDeclaredMethod("calculateRangeModifier", double.class, double.class);
        method.setAccessible(true);
        
        double maximumRange = 200.0;
        double result = (Double) method.invoke(null, maximumRange, maximumRange);
        assertEquals(-20.0, result, 0.001, "Maximum range should give maximum negative modifier (-20)");
    }
    
    @Test
    public void testCalculateRangeModifier_BeyondMaximumRange() throws Exception {
        Method method = OpenFields2.class.getDeclaredMethod("calculateRangeModifier", double.class, double.class);
        method.setAccessible(true);
        
        double maximumRange = 200.0;
        double beyondRange = maximumRange + 50.0;
        double result = (Double) method.invoke(null, beyondRange, maximumRange);
        assertTrue(result < -20.0, "Beyond maximum range should give worse than -20 modifier");
    }
    
    @Test
    public void testIsVitalArea_VitalBodyParts() throws Exception {
        Method method = OpenFields2.class.getDeclaredMethod("isVitalArea", combat.BodyPart.class);
        method.setAccessible(true);
        
        assertTrue((Boolean) method.invoke(null, combat.BodyPart.HEAD), "Head should be vital area");
        assertTrue((Boolean) method.invoke(null, combat.BodyPart.CHEST), "Chest should be vital area");
        assertTrue((Boolean) method.invoke(null, combat.BodyPart.ABDOMEN), "Abdomen should be vital area");
    }
    
    @Test
    public void testIsVitalArea_NonVitalBodyParts() throws Exception {
        Method method = OpenFields2.class.getDeclaredMethod("isVitalArea", combat.BodyPart.class);
        method.setAccessible(true);
        
        assertFalse((Boolean) method.invoke(null, combat.BodyPart.LEFT_ARM), "Left arm should not be vital area");
        assertFalse((Boolean) method.invoke(null, combat.BodyPart.RIGHT_ARM), "Right arm should not be vital area");
        assertFalse((Boolean) method.invoke(null, combat.BodyPart.LEFT_LEG), "Left leg should not be vital area");
        assertFalse((Boolean) method.invoke(null, combat.BodyPart.RIGHT_LEG), "Right leg should not be vital area");
        assertFalse((Boolean) method.invoke(null, combat.BodyPart.LEFT_SHOULDER), "Left shoulder should not be vital area");
        assertFalse((Boolean) method.invoke(null, combat.BodyPart.RIGHT_SHOULDER), "Right shoulder should not be vital area");
    }
    
    @Test
    public void testCalculateActualDamage_CriticalWound() throws Exception {
        Method method = OpenFields2.class.getDeclaredMethod("calculateActualDamage", int.class, combat.WoundSeverity.class);
        method.setAccessible(true);
        
        int weaponDamage = 10;
        int result = (Integer) method.invoke(null, weaponDamage, combat.WoundSeverity.CRITICAL);
        assertEquals(weaponDamage, result, "Critical wounds should deal full weapon damage");
    }
    
    @Test
    public void testCalculateActualDamage_SeriousWound() throws Exception {
        Method method = OpenFields2.class.getDeclaredMethod("calculateActualDamage", int.class, combat.WoundSeverity.class);
        method.setAccessible(true);
        
        int weaponDamage = 10;
        int result = (Integer) method.invoke(null, weaponDamage, combat.WoundSeverity.SERIOUS);
        assertEquals(weaponDamage, result, "Serious wounds should deal full weapon damage");
    }
    
    @Test
    public void testCalculateActualDamage_LightWound() throws Exception {
        Method method = OpenFields2.class.getDeclaredMethod("calculateActualDamage", int.class, combat.WoundSeverity.class);
        method.setAccessible(true);
        
        int weaponDamage = 10;
        int result = (Integer) method.invoke(null, weaponDamage, combat.WoundSeverity.LIGHT);
        assertEquals(4, result, "Light wounds should deal 40% of weapon damage (10 * 0.4 = 4)");
    }
    
    @Test
    public void testCalculateActualDamage_LightWound_MinimumDamage() throws Exception {
        Method method = OpenFields2.class.getDeclaredMethod("calculateActualDamage", int.class, combat.WoundSeverity.class);
        method.setAccessible(true);
        
        int weaponDamage = 1;
        int result = (Integer) method.invoke(null, weaponDamage, combat.WoundSeverity.LIGHT);
        assertEquals(1, result, "Light wounds should deal minimum 1 damage even for low-damage weapons");
    }
    
    @Test
    public void testCalculateActualDamage_ScratchWound() throws Exception {
        Method method = OpenFields2.class.getDeclaredMethod("calculateActualDamage", int.class, combat.WoundSeverity.class);
        method.setAccessible(true);
        
        int weaponDamage = 50;
        int result = (Integer) method.invoke(null, weaponDamage, combat.WoundSeverity.SCRATCH);
        assertEquals(1, result, "Scratch wounds should always deal exactly 1 damage regardless of weapon");
    }
    
    @Test
    public void testGetRandomBodyPart_ReturnsValidBodyPart() throws Exception {
        Method method = OpenFields2.class.getDeclaredMethod("getRandomBodyPart");
        method.setAccessible(true);
        
        for (int i = 0; i < 100; i++) {
            combat.BodyPart result = (combat.BodyPart) method.invoke(null);
            assertNotNull(result, "getRandomBodyPart should never return null");
            assertTrue(isValidBodyPart(result), "Should return a valid body part: " + result);
        }
    }
    
    private boolean isValidBodyPart(combat.BodyPart bodyPart) {
        return bodyPart == combat.BodyPart.LEFT_ARM || 
               bodyPart == combat.BodyPart.RIGHT_ARM ||
               bodyPart == combat.BodyPart.LEFT_SHOULDER ||
               bodyPart == combat.BodyPart.RIGHT_SHOULDER ||
               bodyPart == combat.BodyPart.HEAD ||
               bodyPart == combat.BodyPart.LEFT_LEG ||
               bodyPart == combat.BodyPart.RIGHT_LEG;
    }
    
    @Test
    public void testDetermineHitLocation_ExcellentShot() throws Exception {
        Method method = OpenFields2.class.getDeclaredMethod("determineHitLocation", double.class, double.class);
        method.setAccessible(true);
        
        double chanceToHit = 80.0;
        double excellentThreshold = chanceToHit * 0.2; // 16.0
        double excellentRoll = excellentThreshold - 1; // 15.0, just under threshold
        
        combat.BodyPart result = (combat.BodyPart) method.invoke(null, excellentRoll, chanceToHit);
        assertEquals(combat.BodyPart.CHEST, result, "Excellent shots should hit chest");
    }
    
    @Test
    public void testDetermineWoundSeverity_ExcellentShot() throws Exception {
        Method method = OpenFields2.class.getDeclaredMethod("determineWoundSeverity", double.class, double.class, combat.BodyPart.class);
        method.setAccessible(true);
        
        double chanceToHit = 80.0;
        double excellentThreshold = chanceToHit * 0.2; // 16.0
        double excellentRoll = excellentThreshold - 1; // 15.0, just under threshold
        
        combat.WoundSeverity result = (combat.WoundSeverity) method.invoke(null, excellentRoll, chanceToHit, combat.BodyPart.CHEST);
        assertEquals(combat.WoundSeverity.CRITICAL, result, "Excellent shots should always be critical");
    }
    
    @Test
    public void testHitResult_Construction() {
        combat.BodyPart hitLocation = combat.BodyPart.CHEST;
        combat.WoundSeverity severity = combat.WoundSeverity.SERIOUS;
        int damage = 8;
        
        HitResult hitResult = new HitResult(true, hitLocation, severity, damage);
        
        assertTrue(hitResult.isHit(), "Hit result should indicate hit");
        assertEquals(hitLocation, hitResult.getHitLocation(), "Hit location should match");
        assertEquals(severity, hitResult.getWoundSeverity(), "Wound severity should match");
        assertEquals(damage, hitResult.getActualDamage(), "Damage should match");
    }
    
    @Test
    public void testHitResult_Miss() {
        HitResult missResult = new HitResult(false, null, null, 0);
        
        assertFalse(missResult.isHit(), "Miss result should indicate miss");
        assertNull(missResult.getHitLocation(), "Miss should have no hit location");
        assertNull(missResult.getWoundSeverity(), "Miss should have no wound severity");
        assertEquals(0, missResult.getActualDamage(), "Miss should deal no damage");
    }
    
    @Test
    public void testMovementModifier_StationaryUnit() {
        // Test that stationary units have no movement penalty
        Unit stationaryUnit = new Unit(testShooter, 100, 100, Color.RED, 1);
        
        // Use reflection to test the private calculateMovementModifier method
        try {
            Method method = OpenFields2.class.getDeclaredMethod("calculateMovementModifier", Unit.class);
            method.setAccessible(true);
            double modifier = (Double) method.invoke(null, stationaryUnit);
            assertEquals(0.0, modifier, 0.001, "Stationary unit should have no movement penalty");
        } catch (Exception e) {
            fail("Failed to test calculateMovementModifier: " + e.getMessage());
        }
    }
    
    @Test
    public void testMovementModifier_MovingUnits() {
        // Test movement penalties for different movement types
        Unit movingUnit = new Unit(testShooter, 100, 100, Color.RED, 1);
        movingUnit.setTarget(200, 200); // Make unit moving
        
        try {
            Method method = OpenFields2.class.getDeclaredMethod("calculateMovementModifier", Unit.class);
            method.setAccessible(true);
            
            // Test WALK movement
            testShooter.setCurrentMovementType(MovementType.WALK);
            double walkModifier = (Double) method.invoke(null, movingUnit);
            assertEquals(-5.0, walkModifier, 0.001, "Walking should have -5 penalty");
            
            // Test CRAWL movement
            testShooter.setCurrentMovementType(MovementType.CRAWL);
            double crawlModifier = (Double) method.invoke(null, movingUnit);
            assertEquals(-10.0, crawlModifier, 0.001, "Crawling should have -10 penalty");
            
            // Test JOG movement
            testShooter.setCurrentMovementType(MovementType.JOG);
            double jogModifier = (Double) method.invoke(null, movingUnit);
            assertEquals(-15.0, jogModifier, 0.001, "Jogging should have -15 penalty");
            
            // Test RUN movement
            testShooter.setCurrentMovementType(MovementType.RUN);
            double runModifier = (Double) method.invoke(null, movingUnit);
            assertEquals(-25.0, runModifier, 0.001, "Running should have -25 penalty");
            
        } catch (Exception e) {
            fail("Failed to test calculateMovementModifier: " + e.getMessage());
        }
    }
    
    @Test
    public void testMovementModifier_IncapacitatedUnit() {
        // Test that incapacitated units are considered stationary
        Unit incapacitatedUnit = new Unit(testShooter, 100, 100, Color.RED, 1);
        incapacitatedUnit.setTarget(200, 200); // Try to make unit moving
        testShooter.setHealth(0); // Incapacitate
        
        try {
            Method method = OpenFields2.class.getDeclaredMethod("calculateMovementModifier", Unit.class);
            method.setAccessible(true);
            double modifier = (Double) method.invoke(null, incapacitatedUnit);
            assertEquals(0.0, modifier, 0.001, "Incapacitated unit should have no movement penalty (not moving)");
        } catch (Exception e) {
            fail("Failed to test calculateMovementModifier: " + e.getMessage());
        }
    }
    
    @Test
    public void testSkillModifier_NoWeapon() {
        Unit shooterUnit = new Unit(testShooter, 100, 100, Color.BLUE, 1);
        testShooter.weapon = null; // No weapon
        
        try {
            Method method = OpenFields2.class.getDeclaredMethod("calculateSkillModifier", Unit.class);
            method.setAccessible(true);
            double modifier = (Double) method.invoke(null, shooterUnit);
            assertEquals(0.0, modifier, 0.001, "Should have no skill modifier without weapon");
        } catch (Exception e) {
            fail("Failed to test calculateSkillModifier: " + e.getMessage());
        }
    }
    
    @Test
    public void testSkillModifier_PistolWeaponNoSkill() {
        RangedWeapon pistol = new RangedWeapon("test_pistol", "Test Pistol", 600.0, 10, 8, "/test.wav", 200.0, 10, "bullet", WeaponType.PISTOL);
        testShooter.weapon = pistol;
        Unit shooterUnit = new Unit(testShooter, 100, 100, Color.BLUE, 1);
        
        // Character has no Pistol skill (level 0)
        assertEquals(0, testShooter.getSkillLevel(Skills.PISTOL), "Should have no Pistol skill initially");
        
        try {
            Method method = OpenFields2.class.getDeclaredMethod("calculateSkillModifier", Unit.class);
            method.setAccessible(true);
            double modifier = (Double) method.invoke(null, shooterUnit);
            assertEquals(0.0, modifier, 0.001, "Should have no skill modifier with no skill");
        } catch (Exception e) {
            fail("Failed to test calculateSkillModifier: " + e.getMessage());
        }
    }
    
    @Test
    public void testSkillModifier_PistolWeaponWithSkill() {
        RangedWeapon pistol = new RangedWeapon("test_pistol", "Test Pistol", 600.0, 10, 8, "/test.wav", 200.0, 10, "bullet", WeaponType.PISTOL);
        testShooter.weapon = pistol;
        testShooter.addSkill(new Skill(Skills.PISTOL, 75)); // 75 level pistol skill
        Unit shooterUnit = new Unit(testShooter, 100, 100, Color.BLUE, 1);
        
        try {
            Method method = OpenFields2.class.getDeclaredMethod("calculateSkillModifier", Unit.class);
            method.setAccessible(true);
            double modifier = (Double) method.invoke(null, shooterUnit);
            assertEquals(375.0, modifier, 0.001, "Should have 75 * 5 = 375 skill modifier for level 75 Pistol skill");
        } catch (Exception e) {
            fail("Failed to test calculateSkillModifier: " + e.getMessage());
        }
    }
    
    @Test
    public void testSkillModifier_RifleWeaponWithSkill() {
        RangedWeapon rifle = new RangedWeapon("test_rifle", "Test Rifle", 800.0, 10, 12, "/test.wav", 300.0, 5, "bullet", WeaponType.RIFLE);
        testShooter.weapon = rifle;
        testShooter.addSkill(new Skill(Skills.RIFLE, 60)); // 60 level rifle skill
        Unit shooterUnit = new Unit(testShooter, 100, 100, Color.BLUE, 1);
        
        try {
            Method method = OpenFields2.class.getDeclaredMethod("calculateSkillModifier", Unit.class);
            method.setAccessible(true);
            double modifier = (Double) method.invoke(null, shooterUnit);
            assertEquals(300.0, modifier, 0.001, "Should have 60 * 5 = 300 skill modifier for level 60 Rifle skill");
        } catch (Exception e) {
            fail("Failed to test calculateSkillModifier: " + e.getMessage());
        }
    }
    
    @Test
    public void testSkillModifier_OtherWeaponType() {
        RangedWeapon otherWeapon = new RangedWeapon("magic_wand", "Magic Wand", 30.0, 20, 8, "/magic.wav", 100.0, 20, "magic bolt", WeaponType.OTHER);
        testShooter.weapon = otherWeapon;
        testShooter.addSkill(new Skill(Skills.PISTOL, 80)); // Has pistol skill but weapon is OTHER type
        Unit shooterUnit = new Unit(testShooter, 100, 100, Color.BLUE, 1);
        
        try {
            Method method = OpenFields2.class.getDeclaredMethod("calculateSkillModifier", Unit.class);
            method.setAccessible(true);
            double modifier = (Double) method.invoke(null, shooterUnit);
            assertEquals(0.0, modifier, 0.001, "Should have no skill modifier for OTHER weapon type");
        } catch (Exception e) {
            fail("Failed to test calculateSkillModifier: " + e.getMessage());
        }
    }
    
    @Test
    public void testSkillModifier_WrongSkillType() {
        RangedWeapon pistol = new RangedWeapon("test_pistol", "Test Pistol", 600.0, 10, 8, "/test.wav", 200.0, 10, "bullet", WeaponType.PISTOL);
        testShooter.weapon = pistol;
        testShooter.addSkill(new Skill(Skills.RIFLE, 90)); // Has rifle skill but weapon is pistol
        Unit shooterUnit = new Unit(testShooter, 100, 100, Color.BLUE, 1);
        
        try {
            Method method = OpenFields2.class.getDeclaredMethod("calculateSkillModifier", Unit.class);
            method.setAccessible(true);
            double modifier = (Double) method.invoke(null, shooterUnit);
            assertEquals(0.0, modifier, 0.001, "Should have no skill modifier when weapon type doesn't match skill");
        } catch (Exception e) {
            fail("Failed to test calculateSkillModifier: " + e.getMessage());
        }
    }
    
    @Test
    public void testSkillModifier_EdgeCases() {
        RangedWeapon pistol = new RangedWeapon("test_pistol", "Test Pistol", 600.0, 10, 8, "/test.wav", 200.0, 10, "bullet", WeaponType.PISTOL);
        testShooter.weapon = pistol;
        Unit shooterUnit = new Unit(testShooter, 100, 100, Color.BLUE, 1);
        
        try {
            Method method = OpenFields2.class.getDeclaredMethod("calculateSkillModifier", Unit.class);
            method.setAccessible(true);
            
            // Test with skill level 1
            testShooter.addSkill(new Skill(Skills.PISTOL, 1));
            double modifier = (Double) method.invoke(null, shooterUnit);
            assertEquals(5.0, modifier, 0.001, "Should have 1 * 5 = 5 skill modifier for level 1");
            
            // Test with skill level 100
            testShooter.getSkills().clear();
            testShooter.addSkill(new Skill(Skills.PISTOL, 100));
            modifier = (Double) method.invoke(null, shooterUnit);
            assertEquals(500.0, modifier, 0.001, "Should have 100 * 5 = 500 skill modifier for level 100");
            
        } catch (Exception e) {
            fail("Failed to test calculateSkillModifier edge cases: " + e.getMessage());
        }
    }
}
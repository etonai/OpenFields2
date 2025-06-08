import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import combat.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CharacterSystemTest {
    
    private combat.Character testCharacter;
    private Weapon testWeapon;
    
    @BeforeEach
    public void setUp() {
        testCharacter = new combat.Character("TestChar", 70, 100, 60);
        testWeapon = createTestWeapon();
    }
    
    private Weapon createTestWeapon() {
        Weapon weapon = new Weapon("Test Weapon", 500.0, 8, 10, "/test.wav", 200.0, 5);
        weapon.states = new ArrayList<>();
        weapon.states.add(new WeaponState("ready", "aiming", 15));
        weapon.states.add(new WeaponState("aiming", "firing", 60));
        weapon.initialStateName = "ready";
        return weapon;
    }
    
    @Test
    public void testCharacter_BasicConstruction() {
        assertEquals("TestChar", testCharacter.getName());
        assertEquals(70, testCharacter.getDexterity());
        assertEquals(100, testCharacter.getHealth());
        assertEquals(60, testCharacter.getBravery());
        assertEquals(42.0, testCharacter.getBaseMovementSpeed());
        assertNull(testCharacter.getWeapon());
        assertNotNull(testCharacter.getSkills());
        assertNotNull(testCharacter.getWounds());
        assertTrue(testCharacter.getSkills().isEmpty());
        assertTrue(testCharacter.getWounds().isEmpty());
    }
    
    @Test
    public void testCharacter_ConstructionWithWeapon() {
        combat.Character charWithWeapon = new combat.Character("Armed", 80, 90, 70, testWeapon);
        
        assertEquals("Armed", charWithWeapon.getName());
        assertEquals(testWeapon, charWithWeapon.getWeapon());
        assertEquals(80, charWithWeapon.getDexterity());
        assertEquals(90, charWithWeapon.getHealth());
        assertEquals(70, charWithWeapon.getBravery());
    }
    
    @Test
    public void testCharacter_ConstructionWithSkills() {
        List<Skill> skills = new ArrayList<>();
        skills.add(new Skill("Marksmanship", 75));
        skills.add(new Skill("Athletics", 60));
        
        combat.Character charWithSkills = new combat.Character("Skilled", 65, 85, 55, skills);
        
        assertEquals(2, charWithSkills.getSkills().size());
        assertEquals("Marksmanship", charWithSkills.getSkills().get(0).getSkillName());
        assertEquals(75, charWithSkills.getSkills().get(0).getLevel());
    }
    
    @Test
    public void testCharacter_ConstructionWithWeaponAndSkills() {
        List<Skill> skills = new ArrayList<>();
        skills.add(new Skill("Combat", 80));
        
        combat.Character fullyArmed = new combat.Character("Elite", 85, 95, 75, testWeapon, skills);
        
        assertEquals("Elite", fullyArmed.getName());
        assertEquals(testWeapon, fullyArmed.getWeapon());
        assertEquals(1, fullyArmed.getSkills().size());
        assertEquals("Combat", fullyArmed.getSkills().get(0).getSkillName());
    }
    
    @Test
    public void testCharacter_ConstructionWithNullSkills() {
        combat.Character charWithNullSkills = new combat.Character("NullSkills", 70, 100, 60, (List<Skill>) null);
        
        assertNotNull(charWithNullSkills.getSkills());
        assertTrue(charWithNullSkills.getSkills().isEmpty());
    }
    
    @Test
    public void testCharacter_SettersAndGetters() {
        testCharacter.setName("NewName");
        assertEquals("NewName", testCharacter.getName());
        
        testCharacter.setDexterity(85);
        assertEquals(85, testCharacter.getDexterity());
        
        testCharacter.setHealth(75);
        assertEquals(75, testCharacter.getHealth());
        
        testCharacter.setBravery(80);
        assertEquals(80, testCharacter.getBravery());
        
        testCharacter.setBaseMovementSpeed(50.0);
        assertEquals(50.0, testCharacter.getBaseMovementSpeed());
        
        testCharacter.setWeapon(testWeapon);
        assertEquals(testWeapon, testCharacter.getWeapon());
    }
    
    @Test
    public void testCharacter_HealthIncapacitation() {
        assertFalse(testCharacter.isIncapacitated(), "Healthy character should not be incapacitated");
        
        testCharacter.setHealth(0);
        assertTrue(testCharacter.isIncapacitated(), "Character with 0 health should be incapacitated");
        
        testCharacter.setHealth(-10);
        assertTrue(testCharacter.isIncapacitated(), "Character with negative health should be incapacitated");
        
        testCharacter.setHealth(1);
        assertFalse(testCharacter.isIncapacitated(), "Character with positive health should not be incapacitated");
    }
    
    @Test
    public void testCharacter_CriticalWoundIncapacitation() {
        testCharacter.setHealth(50); // Healthy
        assertFalse(testCharacter.isIncapacitated(), "Healthy character should not be incapacitated");
        
        // Add non-critical wounds
        testCharacter.addWound(new combat.Wound(combat.BodyPart.LEFT_ARM, combat.WoundSeverity.LIGHT));
        testCharacter.addWound(new combat.Wound(combat.BodyPart.RIGHT_LEG, combat.WoundSeverity.SERIOUS));
        assertFalse(testCharacter.isIncapacitated(), "Character with non-critical wounds should not be incapacitated");
        
        // Add critical wound
        testCharacter.addWound(new combat.Wound(combat.BodyPart.CHEST, combat.WoundSeverity.CRITICAL));
        assertTrue(testCharacter.isIncapacitated(), "Character with critical wound should be incapacitated");
    }
    
    @Test
    public void testCharacter_WoundManagement() {
        assertTrue(testCharacter.getWounds().isEmpty(), "Character should start with no wounds");
        
        combat.Wound armWound = new combat.Wound(combat.BodyPart.LEFT_ARM, combat.WoundSeverity.LIGHT);
        combat.Wound legWound = new combat.Wound(combat.BodyPart.RIGHT_LEG, combat.WoundSeverity.SERIOUS);
        
        testCharacter.addWound(armWound);
        assertEquals(1, testCharacter.getWounds().size());
        assertTrue(testCharacter.getWounds().contains(armWound));
        
        testCharacter.addWound(legWound);
        assertEquals(2, testCharacter.getWounds().size());
        assertTrue(testCharacter.getWounds().contains(legWound));
        
        boolean removed = testCharacter.removeWound(armWound);
        assertTrue(removed, "Should successfully remove existing wound");
        assertEquals(1, testCharacter.getWounds().size());
        assertFalse(testCharacter.getWounds().contains(armWound));
        assertTrue(testCharacter.getWounds().contains(legWound));
        
        boolean removedAgain = testCharacter.removeWound(armWound);
        assertFalse(removedAgain, "Should not remove non-existing wound");
    }
    
    @Test
    public void testCharacter_SkillManagement() {
        assertTrue(testCharacter.getSkills().isEmpty(), "Character should start with no skills");
        
        Skill marksmanship = new Skill("Marksmanship", 75);
        Skill athletics = new Skill("Athletics", 60);
        
        testCharacter.addSkill(marksmanship);
        assertEquals(1, testCharacter.getSkills().size());
        
        testCharacter.addSkill(athletics);
        assertEquals(2, testCharacter.getSkills().size());
        
        Skill foundSkill = testCharacter.getSkill("Marksmanship");
        assertNotNull(foundSkill, "Should find existing skill");
        assertEquals(marksmanship, foundSkill);
        
        Skill notFoundSkill = testCharacter.getSkill("NonExistent");
        assertNull(notFoundSkill, "Should not find non-existing skill");
        
        int marksmanshipLevel = testCharacter.getSkillLevel("Marksmanship");
        assertEquals(75, marksmanshipLevel, "Should return correct skill level");
        
        int nonExistentLevel = testCharacter.getSkillLevel("NonExistent");
        assertEquals(0, nonExistentLevel, "Should return 0 for non-existing skill");
    }
    
    @Test
    public void testCharacter_SetSkillsWithNull() {
        testCharacter.addSkill(new Skill("Test", 50));
        assertEquals(1, testCharacter.getSkills().size());
        
        testCharacter.setSkills(null);
        assertNotNull(testCharacter.getSkills());
        assertTrue(testCharacter.getSkills().isEmpty());
    }
    
    @Test
    public void testCharacter_SetWoundsWithNull() {
        testCharacter.addWound(new combat.Wound(combat.BodyPart.HEAD, combat.WoundSeverity.LIGHT));
        assertEquals(1, testCharacter.getWounds().size());
        
        testCharacter.setWounds(null);
        assertNotNull(testCharacter.getWounds());
        assertTrue(testCharacter.getWounds().isEmpty());
    }
    
    @Test
    public void testWound_Construction() {
        combat.Wound wound = new combat.Wound(combat.BodyPart.CHEST, combat.WoundSeverity.SERIOUS);
        
        assertEquals(combat.BodyPart.CHEST, wound.getBodyPart());
        assertEquals(combat.WoundSeverity.SERIOUS, wound.getSeverity());
    }
    
    @Test
    public void testWound_SettersAndGetters() {
        combat.Wound wound = new combat.Wound(combat.BodyPart.LEFT_ARM, combat.WoundSeverity.LIGHT);
        
        wound.setBodyPart(combat.BodyPart.RIGHT_ARM);
        assertEquals(combat.BodyPart.RIGHT_ARM, wound.getBodyPart());
        
        wound.setSeverity(combat.WoundSeverity.CRITICAL);
        assertEquals(combat.WoundSeverity.CRITICAL, wound.getSeverity());
    }
    
    @Test
    public void testSkill_Construction() {
        Skill skill = new Skill("TestSkill", 85);
        
        assertEquals("TestSkill", skill.getSkillName());
        assertEquals(85, skill.getLevel());
    }
    
    @Test
    public void testSkill_SettersAndGetters() {
        Skill skill = new Skill("Original", 50);
        
        skill.setSkillName("Modified");
        assertEquals("Modified", skill.getSkillName());
        
        skill.setLevel(90);
        assertEquals(90, skill.getLevel());
    }
    
    @Test
    public void testCharacter_WeaponStateManagement() {
        testCharacter.setWeapon(testWeapon);
        
        WeaponState readyState = testWeapon.getStateByName("ready");
        testCharacter.setCurrentWeaponState(readyState);
        
        assertEquals(readyState, testCharacter.getCurrentWeaponState());
        assertFalse(testCharacter.canFire(), "Character should not be able to fire when ready");
        
        WeaponState aimingState = testWeapon.getStateByName("aiming");
        testCharacter.setCurrentWeaponState(aimingState);
        
        assertTrue(testCharacter.canFire(), "Character should be able to fire when aiming");
    }
    
    @Test
    public void testCharacter_MultipleIncapacitationSources() {
        // Test that either low health OR critical wound incapacitates
        testCharacter.setHealth(50);
        testCharacter.addWound(new combat.Wound(combat.BodyPart.HEAD, combat.WoundSeverity.CRITICAL));
        assertTrue(testCharacter.isIncapacitated(), "Character should be incapacitated by critical wound even with health");
        
        // Remove critical wound but set health to 0
        testCharacter.getWounds().clear();
        testCharacter.setHealth(0);
        assertTrue(testCharacter.isIncapacitated(), "Character should be incapacitated by 0 health even without critical wounds");
        
        // Both conditions at once
        testCharacter.addWound(new combat.Wound(combat.BodyPart.CHEST, combat.WoundSeverity.CRITICAL));
        assertTrue(testCharacter.isIncapacitated(), "Character should be incapacitated by both low health and critical wound");
        
        // Remove both conditions
        testCharacter.getWounds().clear();
        testCharacter.setHealth(50);
        assertFalse(testCharacter.isIncapacitated(), "Character should not be incapacitated when both conditions removed");
    }
    
    @Test
    public void testCharacter_EdgeCaseWounds() {
        // Test all wound severities for incapacitation
        testCharacter.addWound(new combat.Wound(combat.BodyPart.LEFT_ARM, combat.WoundSeverity.SCRATCH));
        assertFalse(testCharacter.isIncapacitated(), "Scratch wound should not incapacitate");
        
        testCharacter.addWound(new combat.Wound(combat.BodyPart.RIGHT_ARM, combat.WoundSeverity.LIGHT));
        assertFalse(testCharacter.isIncapacitated(), "Light wound should not incapacitate");
        
        testCharacter.addWound(new combat.Wound(combat.BodyPart.LEFT_LEG, combat.WoundSeverity.SERIOUS));
        assertFalse(testCharacter.isIncapacitated(), "Serious wound should not incapacitate");
        
        testCharacter.addWound(new combat.Wound(combat.BodyPart.ABDOMEN, combat.WoundSeverity.CRITICAL));
        assertTrue(testCharacter.isIncapacitated(), "Critical wound should incapacitate");
    }
    
    @Test
    public void testCharacter_SkillsEdgeCases() {
        // Test empty skill name
        Skill emptySkill = new Skill("", 50);
        testCharacter.addSkill(emptySkill);
        
        Skill found = testCharacter.getSkill("");
        assertEquals(emptySkill, found);
        
        // Test skill level 0
        Skill zeroSkill = new Skill("ZeroSkill", 0);
        testCharacter.addSkill(zeroSkill);
        
        assertEquals(0, testCharacter.getSkillLevel("ZeroSkill"));
        
        // Test negative skill level
        Skill negativeSkill = new Skill("NegativeSkill", -10);
        testCharacter.addSkill(negativeSkill);
        
        assertEquals(-10, testCharacter.getSkillLevel("NegativeSkill"));
    }
}
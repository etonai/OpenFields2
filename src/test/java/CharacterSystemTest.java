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
        testCharacter = new combat.Character("TestChar", 70, 100, 60, 50, 55);
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
        assertEquals(60, testCharacter.getCoolness());
        assertEquals(50, testCharacter.getStrength());
        assertEquals(55, testCharacter.getReflexes());
        assertEquals(42.0, testCharacter.getBaseMovementSpeed());
        assertNull(testCharacter.getWeapon());
        assertNotNull(testCharacter.getSkills());
        assertNotNull(testCharacter.getWounds());
        assertTrue(testCharacter.getSkills().isEmpty());
        assertTrue(testCharacter.getWounds().isEmpty());
    }
    
    @Test
    public void testCharacter_ConstructionWithWeapon() {
        combat.Character charWithWeapon = new combat.Character("Armed", 80, 90, 70, 60, 75, testWeapon);
        
        assertEquals("Armed", charWithWeapon.getName());
        assertEquals(testWeapon, charWithWeapon.getWeapon());
        assertEquals(80, charWithWeapon.getDexterity());
        assertEquals(90, charWithWeapon.getHealth());
        assertEquals(70, charWithWeapon.getCoolness());
    }
    
    @Test
    public void testCharacter_ConstructionWithSkills() {
        List<Skill> skills = new ArrayList<>();
        skills.add(new Skill("Marksmanship", 75));
        skills.add(new Skill("Athletics", 60));
        
        combat.Character charWithSkills = new combat.Character("Skilled", 65, 85, 55, 45, 65, skills);
        
        assertEquals(2, charWithSkills.getSkills().size());
        assertEquals("Marksmanship", charWithSkills.getSkills().get(0).getSkillName());
        assertEquals(75, charWithSkills.getSkills().get(0).getLevel());
    }
    
    @Test
    public void testCharacter_ConstructionWithWeaponAndSkills() {
        List<Skill> skills = new ArrayList<>();
        skills.add(new Skill("Combat", 80));
        
        combat.Character fullyArmed = new combat.Character("Elite", 85, 95, 75, 70, 85, testWeapon, skills);
        
        assertEquals("Elite", fullyArmed.getName());
        assertEquals(testWeapon, fullyArmed.getWeapon());
        assertEquals(1, fullyArmed.getSkills().size());
        assertEquals("Combat", fullyArmed.getSkills().get(0).getSkillName());
    }
    
    @Test
    public void testCharacter_ConstructionWithNullSkills() {
        combat.Character charWithNullSkills = new combat.Character("NullSkills", 70, 100, 60, 50, 55, (List<Skill>) null);
        
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
        
        testCharacter.setCoolness(80);
        assertEquals(80, testCharacter.getCoolness());
        
        testCharacter.setStrength(85);
        assertEquals(85, testCharacter.getStrength());
        
        testCharacter.setReflexes(75);
        assertEquals(75, testCharacter.getReflexes());
        
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
    
    @Test
    public void testCharacter_DefaultMovementType() {
        assertEquals(combat.MovementType.WALK, testCharacter.getCurrentMovementType(), "Character should start with WALK movement type");
    }
    
    @Test
    public void testCharacter_MovementTypeChangeAndSpeed() {
        // Test increasing movement types
        testCharacter.increaseMovementType();
        assertEquals(combat.MovementType.JOG, testCharacter.getCurrentMovementType(), "Should increase from WALK to JOG");
        assertEquals(63.0, testCharacter.getEffectiveMovementSpeed(), 0.001, "JOG should be 1.5x base speed");
        
        testCharacter.increaseMovementType();
        assertEquals(combat.MovementType.RUN, testCharacter.getCurrentMovementType(), "Should increase from JOG to RUN");
        assertEquals(84.0, testCharacter.getEffectiveMovementSpeed(), 0.001, "RUN should be 2.0x base speed");
        
        // Test maximum limit
        testCharacter.increaseMovementType();
        assertEquals(combat.MovementType.RUN, testCharacter.getCurrentMovementType(), "Should remain at RUN when already at maximum");
        
        // Test decreasing movement types
        testCharacter.decreaseMovementType();
        assertEquals(combat.MovementType.JOG, testCharacter.getCurrentMovementType(), "Should decrease from RUN to JOG");
        
        testCharacter.decreaseMovementType();
        assertEquals(combat.MovementType.WALK, testCharacter.getCurrentMovementType(), "Should decrease from JOG to WALK");
        assertEquals(42.0, testCharacter.getEffectiveMovementSpeed(), 0.001, "WALK should be 1.0x base speed");
        
        testCharacter.decreaseMovementType();
        assertEquals(combat.MovementType.CRAWL, testCharacter.getCurrentMovementType(), "Should decrease from WALK to CRAWL");
        assertEquals(10.5, testCharacter.getEffectiveMovementSpeed(), 0.001, "CRAWL should be 0.25x base speed");
        
        // Test minimum limit
        testCharacter.decreaseMovementType();
        assertEquals(combat.MovementType.CRAWL, testCharacter.getCurrentMovementType(), "Should remain at CRAWL when already at minimum");
    }
    
    @Test
    public void testCharacter_IncapacitatedMovement() {
        testCharacter.setHealth(0); // Incapacitate character
        
        // Should not be able to change movement type when incapacitated
        testCharacter.increaseMovementType();
        assertEquals(combat.MovementType.WALK, testCharacter.getCurrentMovementType(), "Incapacitated character should not change movement type");
        
        testCharacter.decreaseMovementType();
        assertEquals(combat.MovementType.WALK, testCharacter.getCurrentMovementType(), "Incapacitated character should not change movement type");
        
        // Effective movement speed should be 0 when incapacitated
        assertEquals(0.0, testCharacter.getEffectiveMovementSpeed(), 0.001, "Incapacitated character should have 0 movement speed");
    }
    
    @Test
    public void testCharacter_MovementTypeSettersAndGetters() {
        testCharacter.setCurrentMovementType(combat.MovementType.RUN);
        assertEquals(combat.MovementType.RUN, testCharacter.getCurrentMovementType(), "Should be able to set movement type directly");
        assertEquals(84.0, testCharacter.getEffectiveMovementSpeed(), 0.001, "Effective speed should update with movement type");
        
        testCharacter.setCurrentMovementType(combat.MovementType.CRAWL);
        assertEquals(combat.MovementType.CRAWL, testCharacter.getCurrentMovementType(), "Should be able to set movement type to CRAWL");
        assertEquals(10.5, testCharacter.getEffectiveMovementSpeed(), 0.001, "Effective speed should update with movement type");
    }
    
    @Test
    public void testMovementType_EnumProperties() {
        assertEquals("Crawl", combat.MovementType.CRAWL.getDisplayName());
        assertEquals(0.25, combat.MovementType.CRAWL.getSpeedMultiplier(), 0.001);
        
        assertEquals("Walk", combat.MovementType.WALK.getDisplayName());
        assertEquals(1.0, combat.MovementType.WALK.getSpeedMultiplier(), 0.001);
        
        assertEquals("Jog", combat.MovementType.JOG.getDisplayName());
        assertEquals(1.5, combat.MovementType.JOG.getSpeedMultiplier(), 0.001);
        
        assertEquals("Run", combat.MovementType.RUN.getDisplayName());
        assertEquals(2.0, combat.MovementType.RUN.getSpeedMultiplier(), 0.001);
    }
    
    @Test
    public void testMovementType_IncreaseAndDecrease() {
        assertEquals(combat.MovementType.WALK, combat.MovementType.CRAWL.increase());
        assertEquals(combat.MovementType.JOG, combat.MovementType.WALK.increase());
        assertEquals(combat.MovementType.RUN, combat.MovementType.JOG.increase());
        assertEquals(combat.MovementType.RUN, combat.MovementType.RUN.increase()); // Already at max
        
        assertEquals(combat.MovementType.CRAWL, combat.MovementType.CRAWL.decrease()); // Already at min
        assertEquals(combat.MovementType.CRAWL, combat.MovementType.WALK.decrease());
        assertEquals(combat.MovementType.WALK, combat.MovementType.JOG.decrease());
        assertEquals(combat.MovementType.JOG, combat.MovementType.RUN.decrease());
    }
    
    @Test
    public void testCharacter_DefaultSkillsCreation() {
        List<Skill> defaultSkills = combat.Character.createDefaultSkills();
        
        assertEquals(4, defaultSkills.size(), "Should create 4 default skills");
        
        // Check all default skills are present
        boolean hasPistol = false, hasRifle = false, hasQuickdraw = false, hasMedicine = false;
        for (Skill skill : defaultSkills) {
            assertEquals(50, skill.getLevel(), "All default skills should start at level 50");
            
            if (Skills.PISTOL.equals(skill.getSkillName())) hasPistol = true;
            else if (Skills.RIFLE.equals(skill.getSkillName())) hasRifle = true;
            else if (Skills.QUICKDRAW.equals(skill.getSkillName())) hasQuickdraw = true;
            else if (Skills.MEDICINE.equals(skill.getSkillName())) hasMedicine = true;
        }
        
        assertTrue(hasPistol, "Should include Pistol skill");
        assertTrue(hasRifle, "Should include Rifle skill");
        assertTrue(hasQuickdraw, "Should include Quickdraw skill");
        assertTrue(hasMedicine, "Should include Medicine skill");
    }
    
    @Test
    public void testCharacter_HasSkillMethod() {
        assertFalse(testCharacter.hasSkill(Skills.PISTOL), "Should not have Pistol skill initially");
        
        testCharacter.addSkill(new Skill(Skills.PISTOL, 75));
        assertTrue(testCharacter.hasSkill(Skills.PISTOL), "Should have Pistol skill after adding");
        assertFalse(testCharacter.hasSkill(Skills.RIFLE), "Should not have Rifle skill");
        
        // Test case sensitivity
        assertFalse(testCharacter.hasSkill("pistol"), "Should be case sensitive");
        assertFalse(testCharacter.hasSkill("PISTOL"), "Should be case sensitive");
    }
    
    @Test
    public void testCharacter_AddDefaultSkills() {
        // Character should start with no skills
        assertEquals(0, testCharacter.getSkills().size(), "Should start with no skills");
        
        // Add default skills
        testCharacter.addDefaultSkills();
        assertEquals(4, testCharacter.getSkills().size(), "Should have 4 skills after adding defaults");
        
        // Check all default skills are present
        assertTrue(testCharacter.hasSkill(Skills.PISTOL), "Should have Pistol skill");
        assertTrue(testCharacter.hasSkill(Skills.RIFLE), "Should have Rifle skill");
        assertTrue(testCharacter.hasSkill(Skills.QUICKDRAW), "Should have Quickdraw skill");
        assertTrue(testCharacter.hasSkill(Skills.MEDICINE), "Should have Medicine skill");
        
        // Check skill levels
        assertEquals(50, testCharacter.getSkillLevel(Skills.PISTOL), "Pistol should be level 50");
        assertEquals(50, testCharacter.getSkillLevel(Skills.RIFLE), "Rifle should be level 50");
        assertEquals(50, testCharacter.getSkillLevel(Skills.QUICKDRAW), "Quickdraw should be level 50");
        assertEquals(50, testCharacter.getSkillLevel(Skills.MEDICINE), "Medicine should be level 50");
    }
    
    @Test
    public void testCharacter_AddDefaultSkillsNoDuplicates() {
        // Add a custom Pistol skill first
        testCharacter.addSkill(new Skill(Skills.PISTOL, 85));
        assertEquals(1, testCharacter.getSkills().size(), "Should have 1 skill");
        assertEquals(85, testCharacter.getSkillLevel(Skills.PISTOL), "Custom Pistol skill should be level 85");
        
        // Add default skills - should not duplicate Pistol
        testCharacter.addDefaultSkills();
        assertEquals(4, testCharacter.getSkills().size(), "Should have 4 total skills");
        assertEquals(85, testCharacter.getSkillLevel(Skills.PISTOL), "Pistol skill should remain at level 85");
        
        // Other skills should be added at default level
        assertEquals(50, testCharacter.getSkillLevel(Skills.RIFLE), "Rifle should be level 50");
        assertEquals(50, testCharacter.getSkillLevel(Skills.QUICKDRAW), "Quickdraw should be level 50");
        assertEquals(50, testCharacter.getSkillLevel(Skills.MEDICINE), "Medicine should be level 50");
    }
    
    @Test
    public void testCharacter_NewCharactersNoDefaultSkills() {
        // Verify that new characters don't automatically get default skills
        combat.Character newChar = new combat.Character("NewChar", 50, 80, 55, 45, 60);
        assertEquals(0, newChar.getSkills().size(), "New characters should not have default skills automatically");
        
        assertFalse(newChar.hasSkill(Skills.PISTOL), "Should not have Pistol skill");
        assertFalse(newChar.hasSkill(Skills.RIFLE), "Should not have Rifle skill");
        assertFalse(newChar.hasSkill(Skills.QUICKDRAW), "Should not have Quickdraw skill");
        assertFalse(newChar.hasSkill(Skills.MEDICINE), "Should not have Medicine skill");
    }
    
    @Test
    public void testSkills_Constants() {
        assertEquals("Pistol", Skills.PISTOL);
        assertEquals("Rifle", Skills.RIFLE);
        assertEquals("Quickdraw", Skills.QUICKDRAW);
        assertEquals("Medicine", Skills.MEDICINE);
        
        String[] allSkills = Skills.getAllSkillNames();
        assertEquals(4, allSkills.length, "Should have 4 skill names");
        
        boolean hasPistol = false, hasRifle = false, hasQuickdraw = false, hasMedicine = false;
        for (String skillName : allSkills) {
            if (Skills.PISTOL.equals(skillName)) hasPistol = true;
            else if (Skills.RIFLE.equals(skillName)) hasRifle = true;
            else if (Skills.QUICKDRAW.equals(skillName)) hasQuickdraw = true;
            else if (Skills.MEDICINE.equals(skillName)) hasMedicine = true;
        }
        
        assertTrue(hasPistol, "All skills array should include Pistol");
        assertTrue(hasRifle, "All skills array should include Rifle");
        assertTrue(hasQuickdraw, "All skills array should include Quickdraw");
        assertTrue(hasMedicine, "All skills array should include Medicine");
    }
}
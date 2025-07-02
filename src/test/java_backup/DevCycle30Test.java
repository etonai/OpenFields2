import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import combat.Character;
import combat.Handedness;

public class DevCycle30Test {
    
    @Test
    public void testCharacterCreation() {
        // Test basic character creation still works
        Character character = new Character("TestCharacter", 50, 100, 50, 50, 50, Handedness.RIGHT);
        assertNotNull(character);
        assertEquals("TestCharacter", character.nickname);
        assertEquals(100, character.health);
        assertEquals(50, character.dexterity);
    }
    
    @Test 
    public void testSkillsDelegation() {
        Character character = new Character("TestCharacter", 50, 100, 50, 50, 50, Handedness.RIGHT);
        
        // Test skills delegation works
        assertTrue(character.getSkills().isEmpty()); // Should start empty
        assertEquals(0, character.getSkillLevel("Pistol")); // Should return 0 for non-existent skill
        
        character.setSkillLevel("Pistol", 75);
        assertEquals(75, character.getSkillLevel("Pistol"));
        assertTrue(character.hasSkill("Pistol"));
    }
    
    @Test
    public void testTargetingDelegation() {
        Character character = new Character("TestCharacter", 50, 100, 50, 50, 50, Handedness.RIGHT);
        
        // Test targeting delegation works
        assertNull(character.getCurrentTarget());
        assertFalse(character.hasValidTarget());
    }
}
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import combat.*;
import combat.Character; // Explicit import to resolve ambiguity

/**
 * Simple test to verify the refactoring changes work correctly
 */
public class RefactoringTest {

    @Test
    public void testCharacterBuilder() {
        // Test CharacterBuilder pattern
        Character character = CharacterBuilder.testCharacter("TestChar")
            .withDexterity(80)
            .withHealth(90)
            .build();
        
        assertEquals("TestChar", character.nickname);
        assertEquals(80, character.dexterity);
        assertEquals(90, character.health);
        assertNotNull(character.identity);
        assertNotNull(character.stats);
    }

    @Test 
    public void testCharacterStats() {
        // Test CharacterStats value object
        CharacterStats stats = new CharacterStats(70, 100, 60, 50, 75, Handedness.RIGHT_HANDED, 42.0);
        
        assertEquals(70, stats.getDexterity());
        assertEquals(100, stats.getHealth());
        assertEquals(8, stats.getDexterityModifier());
        assertEquals(4, stats.getCoolnessModifier()); // 60 gives +4 modifier
        
        // Test stat modifier calculation
        assertEquals(-20, CharacterStats.getStatModifier(1));
        assertEquals(0, CharacterStats.getStatModifier(50));
        assertEquals(20, CharacterStats.getStatModifier(100));
    }

    @Test
    public void testCharacterIdentity() {
        // Test CharacterIdentity value object
        CharacterIdentity identity = new CharacterIdentity(1, "TestChar", "John", "Doe", 
            new java.util.Date(), "test_theme");
        
        assertEquals(1, identity.getId());
        assertEquals("TestChar", identity.getNickname());
        assertEquals("TestChar", identity.getDisplayName());
        assertEquals("test_theme", identity.getThemeId());
    }

    @Test
    public void testMovementController() {
        // Test MovementController
        Character character = new Character("TestChar", 70, 100, 60, 50, 75, Handedness.RIGHT_HANDED);
        
        // Test movement type changes via Character methods
        character.currentMovementType = MovementType.WALK;
        character.increaseMovementType();
        assertEquals(MovementType.JOG, character.currentMovementType);
        
        character.decreaseMovementType();
        assertEquals(MovementType.WALK, character.currentMovementType);
        
        // Test effective movement speed
        double speed = character.getEffectiveMovementSpeed();
        assertTrue(speed > 0);
    }

    @Test
    public void testAutoTargetingSystemExists() {
        // Test that AutoTargetingSystem class exists and has expected methods
        assertNotNull(AutoTargetingSystem.class);
        
        // Verify key static methods exist (by reflection)
        try {
            AutoTargetingSystem.class.getDeclaredMethod("updateAutomaticTargeting", 
                Character.class, game.interfaces.IUnit.class, long.class, 
                java.util.PriorityQueue.class, game.GameCallbacks.class);
        } catch (NoSuchMethodException e) {
            fail("AutoTargetingSystem.updateAutomaticTargeting method should exist");
        }
    }

    @Test
    public void testFactoryMethods() {
        // Test builder-based factory methods
        Character testChar = CharacterBuilder.testCharacter("TestFactory").build();
        assertEquals("TestFactory", testChar.nickname);
        
        Character soldier = CharacterBuilder.civilWarSoldier("Soldier").build();
        assertEquals("Soldier", soldier.nickname);
        
        Character gunslinger = CharacterBuilder.gunslinger("Gunslinger").build();
        assertEquals("Gunslinger", gunslinger.nickname);
    }

    @Test
    public void testValueObjectSynchronization() {
        // Test that value objects can be created and synced
        Character character = new Character("SyncTest", 80, 90, 70, 60, 85, Handedness.LEFT_HANDED);
        
        // Manually create value objects (constructors don't auto-initialize them)
        character.identity = new CharacterIdentity(character.id, character.nickname, 
            character.firstName, character.lastName, character.birthdate, character.themeId);
        character.stats = new CharacterStats(character.dexterity, character.health, 
            character.coolness, character.strength, character.reflexes, character.handedness, 
            character.baseMovementSpeed);
        
        // Value objects should now exist
        assertNotNull(character.identity);
        assertNotNull(character.stats);
        
        // Values should match
        assertEquals(character.dexterity, character.stats.getDexterity());
        assertEquals(character.health, character.stats.getHealth());
        assertEquals(character.nickname, character.identity.getNickname());
    }
}
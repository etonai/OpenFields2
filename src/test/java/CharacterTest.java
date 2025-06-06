import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CharacterTest {

    @Test
    public void testCharacterInitialization() {
        Character alice = new Character("Alice", 10, 100, null);
        assertEquals("Alice", alice.getName());
        assertEquals(10, alice.getDexterity());
        assertEquals(100, alice.getHealth());
        assertNull(alice.getWeapon());
    }

}

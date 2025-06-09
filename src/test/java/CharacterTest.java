import org.junit.jupiter.api.Test;

import java.util.List;
import combat.Character;
import combat.Weapon;

import static org.junit.jupiter.api.Assertions.*;

public class CharacterTest {

    @Test
    public void testCharacterInitialization() {
        Character alan = new Character("Alan", 10, 100, 15, 20, 25, combat.Handedness.RIGHT_HANDED);
        assertEquals("Alan", alan.getName());
        assertEquals(10, alan.getDexterity());
        assertEquals(100, alan.getHealth());
        assertEquals(42.0, alan.getBaseMovementSpeed());
        assertNull(alan.getWeapon());
    }

    @Test
    public void testCharacterWeapon() {
        Character bart = new Character("Bart", 70, 40, 25, 65, 50, combat.Handedness.LEFT_HANDED);
        assertEquals("Bart", bart.getName());
        assertEquals(70, bart.getDexterity());
        assertEquals(40, bart.getHealth());
        assertEquals(42.0, bart.getBaseMovementSpeed());
        assertNull(bart.getWeapon());

        bart.weapon = new Weapon("Airsoft Pistol", 350.0, 1, 20, "test.wav", 100.0, 0);
        assertEquals("Airsoft Pistol", bart.getWeapon().getName());
        assertEquals(350.0, bart.getWeapon().getVelocityFeetPerSecond());
        assertEquals(1, bart.getWeapon().getDamage());
    }

}

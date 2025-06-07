import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CharacterTest {

    @Test
    public void testCharacterInitialization() {
        Character alan = new Character("Alan", 10, 100, null);
        assertEquals("Alan", alan.getName());
        assertEquals(10, alan.getDexterity());
        assertEquals(100, alan.getHealth());
        assertEquals(42.0, alan.getMovementSpeed());
        assertNull(alan.getWeapon());
    }

    @Test
    public void testCharacterWeapon() {
        Character bart = new Character("Bart", 70, 40, null);
        assertEquals("Bart", bart.getName());
        assertEquals(70, bart.getDexterity());
        assertEquals(40, bart.getHealth());
        assertEquals(42.0, bart.getMovementSpeed());
        assertNull(bart.getWeapon());

        bart.weapon = new Weapon("Airsoft Pistol", 350.0, 1, 20);
        assertEquals("Airsoft Pistol", bart.getWeapon().getName());
        assertEquals(350.0, bart.getWeapon().getVelocityFeetPerSecond());
        assertEquals(1, bart.getWeapon().getDamage());
    }

}

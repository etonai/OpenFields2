import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OpenFields2Tests {

    @Test
    void testCreateUnits() {
        OpenFields2 game = new OpenFields2();
        game.createUnits();

        List<Unit> units = game.getUnits();
        assertEquals(3, units.size(), "Expected 3 units");

        // Alice
        Unit aliceUnit = units.get(0);
        Character alice = aliceUnit.getCharacter();
        assertEquals("Alice", alice.getName());
        assertEquals(100, aliceUnit.getX());
        assertEquals(100, aliceUnit.getY());
        assertEquals("Colt Peacemaker", alice.getWeapon().getName());

        // Bobby
        Unit bobbyUnit = units.get(1);
        Character bobby = bobbyUnit.getCharacter();
        assertEquals("Bobby", bobby.getName());
        assertEquals(400, bobbyUnit.getX());
        assertEquals(400, bobbyUnit.getY());
        assertEquals("Paintball Gun", bobby.getWeapon().getName());

        // Chris
        Unit chrisUnit = units.get(2);
        Character chris = chrisUnit.getCharacter();
        assertEquals("Chris", chris.getName());
        assertEquals(400, chrisUnit.getX());
        assertEquals(100, chrisUnit.getY());
        assertEquals("Nerf Gun", chris.getWeapon().getName());
    }

    @Test
    void testPixelsToFeet() {
        assertEquals(1.0, OpenFields2.pixelsToFeet(7.0), "Expected 1.0 Feet");
    }
}

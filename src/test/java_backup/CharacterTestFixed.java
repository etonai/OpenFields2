import org.junit.jupiter.api.Test;

import java.util.List;
import combat.Character;
import combat.RangedWeapon;

import static org.junit.jupiter.api.Assertions.*;

public class CharacterTestFixed {

    @Test
    public void testCharacterInitialization() {
        Character alan = new Character("Alan", 10, 100, 15, 20, 25, combat.Handedness.RIGHT_HANDED);
        assertEquals("Alan", alan.nickname);              // ✅ Direct field access
        assertEquals(10, alan.dexterity);                 // ✅ Direct field access
        assertEquals(100, alan.health);                   // ✅ Direct field access
        assertEquals(42.0, alan.baseMovementSpeed);       // ✅ Direct field access
        assertNull(alan.weapon);                          // ✅ Direct field access
    }

    @Test
    public void testCharacterWeapon() {
        Character bart = new Character("Bart", 70, 40, 25, 65, 50, combat.Handedness.LEFT_HANDED);
        assertEquals("Bart", bart.nickname);              // ✅ Direct field access
        assertEquals(70, bart.dexterity);                 // ✅ Direct field access
        assertEquals(40, bart.health);                    // ✅ Direct field access
        assertEquals(42.0, bart.baseMovementSpeed);       // ✅ Direct field access
        assertNull(bart.weapon);                          // ✅ Direct field access

        // ✅ Use RangedWeapon instead of abstract Weapon
        bart.weapon = new RangedWeapon("airsoft_pistol", "Airsoft Pistol", 350.0, 1, 20, "test.wav", 100.0, 0, "pellet");
        assertEquals("Airsoft Pistol", bart.weapon.name); // ✅ Direct field access
        assertEquals(350.0, bart.weapon.velocityFeetPerSecond); // ✅ Correct property name
        assertEquals(1, bart.weapon.damage);              // ✅ Direct field access
    }
}
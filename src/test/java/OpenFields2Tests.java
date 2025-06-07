import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OpenFields2Tests {

    @Test
    void testPixelsToFeet() {
        assertEquals(1.0, OpenFields2.pixelsToFeet(7.0), "Expected 1.0 Feet");
    }
}

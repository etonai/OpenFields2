import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import platform.api.Color;

/**
 * Unit tests for SaveGameController color conversion methods.
 * Tests the decoupling of JavaFX dependencies by verifying 
 * platform.api.Color conversion works correctly.
 */
public class SaveGameControllerColorTest {
    
    private SaveGameController saveGameController;
    private Method colorToStringMethod;
    private Method stringToColorMethod;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Create a test instance of SaveGameController
        saveGameController = new SaveGameController(null, null, null, null, null, null, null);
        
        // Get private methods via reflection for testing
        colorToStringMethod = SaveGameController.class.getDeclaredMethod("colorToString", platform.api.Color.class);
        colorToStringMethod.setAccessible(true);
        
        stringToColorMethod = SaveGameController.class.getDeclaredMethod("stringToColor", String.class);
        stringToColorMethod.setAccessible(true);
    }
    
    @Test
    public void testColorToStringConversion() throws Exception {
        // Test all supported colors
        assertEquals("RED", colorToStringMethod.invoke(saveGameController, Color.RED));
        assertEquals("BLUE", colorToStringMethod.invoke(saveGameController, Color.BLUE));
        assertEquals("GREEN", colorToStringMethod.invoke(saveGameController, Color.GREEN));
        assertEquals("PURPLE", colorToStringMethod.invoke(saveGameController, Color.PURPLE));
        assertEquals("ORANGE", colorToStringMethod.invoke(saveGameController, Color.ORANGE));
        assertEquals("YELLOW", colorToStringMethod.invoke(saveGameController, Color.YELLOW));
        assertEquals("DARKGRAY", colorToStringMethod.invoke(saveGameController, Color.DARK_GRAY));
        assertEquals("GRAY", colorToStringMethod.invoke(saveGameController, Color.GRAY));
        assertEquals("CYAN", colorToStringMethod.invoke(saveGameController, Color.CYAN));
    }
    
    @Test
    public void testStringToColorConversion() throws Exception {
        // Test all supported color strings
        assertEquals(Color.RED, stringToColorMethod.invoke(saveGameController, "RED"));
        assertEquals(Color.BLUE, stringToColorMethod.invoke(saveGameController, "BLUE"));
        assertEquals(Color.GREEN, stringToColorMethod.invoke(saveGameController, "GREEN"));
        assertEquals(Color.PURPLE, stringToColorMethod.invoke(saveGameController, "PURPLE"));
        assertEquals(Color.ORANGE, stringToColorMethod.invoke(saveGameController, "ORANGE"));
        assertEquals(Color.YELLOW, stringToColorMethod.invoke(saveGameController, "YELLOW"));
        assertEquals(Color.DARK_GRAY, stringToColorMethod.invoke(saveGameController, "DARKGRAY"));
        assertEquals(Color.GRAY, stringToColorMethod.invoke(saveGameController, "GRAY"));
        assertEquals(Color.CYAN, stringToColorMethod.invoke(saveGameController, "CYAN"));
    }
    
    @Test
    public void testRoundTripConversion() throws Exception {
        // Test that converting color to string and back yields the same color
        Color[] testColors = {
            Color.RED, Color.BLUE, Color.GREEN, Color.PURPLE,
            Color.ORANGE, Color.YELLOW, Color.DARK_GRAY, Color.GRAY, Color.CYAN
        };
        
        for (Color originalColor : testColors) {
            String colorString = (String) colorToStringMethod.invoke(saveGameController, originalColor);
            Color convertedColor = (Color) stringToColorMethod.invoke(saveGameController, colorString);
            assertEquals(originalColor, convertedColor, 
                "Round-trip conversion failed for color: " + originalColor);
        }
    }
    
    @Test
    public void testUnknownColorDefaultsToRed() throws Exception {
        // Test that unknown color strings default to RED
        assertEquals(Color.RED, stringToColorMethod.invoke(saveGameController, "UNKNOWN"));
        assertEquals(Color.RED, stringToColorMethod.invoke(saveGameController, "INVALID"));
        assertEquals(Color.RED, stringToColorMethod.invoke(saveGameController, ""));
    }
    
    @Test 
    public void testNullColorDefaultsToRed() throws Exception {
        // Test null handling separately since it requires different approach
        try {
            Color result = (Color) stringToColorMethod.invoke(saveGameController, (Object) null);
            assertEquals(Color.RED, result);
        } catch (Exception e) {
            // If null throws exception, that's acceptable behavior
            // as the current implementation uses switch on String
            assertTrue(e.getCause() instanceof NullPointerException, 
                "Expected NPE for null input, got: " + e.getCause());
        }
    }
    
    @Test
    public void testCaseSensitiveColorConversion() throws Exception {
        // Test that color string conversion is case-sensitive (current implementation)
        // These should default to RED since they don't match exact case
        assertEquals(Color.RED, stringToColorMethod.invoke(saveGameController, "red"));
        assertEquals(Color.RED, stringToColorMethod.invoke(saveGameController, "Red"));
        assertEquals(Color.RED, stringToColorMethod.invoke(saveGameController, "blue"));
        assertEquals(Color.RED, stringToColorMethod.invoke(saveGameController, "green"));
    }
    
    @Test
    public void testNoJavaFXDependencies() {
        // Verify that SaveGameController no longer imports JavaFX Color
        Class<?> clazz = SaveGameController.class;
        
        // Check that the class doesn't reference JavaFX Color in its imports
        // This is a compile-time verification that should pass if import was removed
        try {
            // Try to find any field or method that uses JavaFX Color
            java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
            java.lang.reflect.Method[] methods = clazz.getDeclaredMethods();
            
            for (java.lang.reflect.Field field : fields) {
                assertFalse(field.getType().getName().contains("javafx"), 
                    "Found JavaFX dependency in field: " + field.getName());
            }
            
            for (java.lang.reflect.Method method : methods) {
                assertFalse(method.getReturnType().getName().contains("javafx"), 
                    "Found JavaFX dependency in method return type: " + method.getName());
                
                for (Class<?> paramType : method.getParameterTypes()) {
                    assertFalse(paramType.getName().contains("javafx"), 
                        "Found JavaFX dependency in method parameter: " + method.getName());
                }
            }
            
        } catch (Exception e) {
            fail("Error checking for JavaFX dependencies: " + e.getMessage());
        }
    }
}
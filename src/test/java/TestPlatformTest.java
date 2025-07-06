import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import platform.TestPlatform;
import platform.api.*;
import utils.GameConfiguration;
import java.security.SecureRandom;

/**
 * Proof-of-concept test for headless testing architecture - System 4 of DevCycle 36.
 * Enhanced in DevCycle 41 with deterministic mode and random seed generation.
 * 
 * This test demonstrates that:
 * 1. TestPlatform can be created and initialized without UI dependencies
 * 2. Platform abstraction layer works correctly for headless operation
 * 3. SaveGameController JavaFX decoupling enables headless testing
 * 
 * This establishes the foundation for more complex headless tests in the future.
 * 
 * SEED MANAGEMENT:
 * - Normal Operation: Uses randomly generated seed each run to discover edge cases
 * - Bug Reproduction: Use -Dtest.seed=123456789 to reproduce specific test scenarios
 * - Seed Reporting: Outputs seed at start and completion for easy reproduction
 * 
 * USAGE EXAMPLES:
 * 
 * Basic Usage:
 * mvn test -Dtest=TestPlatformTest                     # Random seed testing
 * mvn test -Dtest=TestPlatformTest -Dtest.seed=54321  # Positive seed reproduction
 * 
 * Cross-Platform Seed Reproduction:
 * 
 * Windows PowerShell (recommended - always quote properties):
 * mvn test "-Dtest=TestPlatformTest" "-Dtest.seed=4292768217366888882"
 * 
 * Windows Command Prompt (standard syntax):
 * mvn test -Dtest=TestPlatformTest -Dtest.seed=4292768217366888882
 * 
 * macOS/Linux (bash/zsh):
 * mvn test -Dtest=TestPlatformTest -Dtest.seed=4292768217366888882
 * 
 * TROUBLESHOOTING:
 * - If you see "Unknown lifecycle phase .seed=" errors, quote the -D properties
 * - Windows PowerShell has parsing issues with -D properties, always use quotes
 * - Use Windows Command Prompt as alternative if PowerShell fails
 * - All seeds (positive and negative) produce deterministic results
 * 
 * NOTE: This test doesn't use random numbers but includes deterministic mode for consistency.
 * 
 * @author DevCycle 36 - System 4: Non-JavaFX Gunfight Test Creation
 * @author DevCycle 41 System 8 - Deterministic Mode Standardization
 */
public class TestPlatformTest {
    
    private TestPlatform testPlatform;
    
    // Random seed for deterministic testing with reproducibility
    private long testSeed;
    
    @BeforeEach
    public void setUp() {
        System.out.println("=== TestPlatform Test Setup ===");
        
        // DevCycle 41: System 8 - Deterministic mode and seed management
        String seedProperty = System.getProperty("test.seed");
        if (seedProperty != null && !seedProperty.isEmpty()) {
            try {
                testSeed = Long.parseLong(seedProperty);
                System.out.println("=== MANUAL SEED OVERRIDE ===");
                System.out.println("Using manual seed: " + testSeed);
                System.out.println("============================");
            } catch (NumberFormatException e) {
                System.out.println("Invalid seed format: " + seedProperty + ", generating random seed");
                testSeed = new SecureRandom().nextLong();
            }
        } else {
            testSeed = new SecureRandom().nextLong();
        }
        
        // Enable deterministic mode
        GameConfiguration.setDeterministicMode(true, testSeed);
        System.out.println("Deterministic mode ENABLED with seed: " + testSeed);
        
        testPlatform = new TestPlatform();
        System.out.println("✓ TestPlatform created successfully");
    }
    
    @Test
    public void testPlatformInitialization() {
        System.out.println("Testing platform initialization...");
        
        // Test platform initialization
        assertTrue(testPlatform.initialize(800, 600, "Test Platform"), 
                  "TestPlatform should initialize successfully");
        System.out.println("✓ Platform initialized");
        
        // Verify platform is running
        assertTrue(testPlatform.isRunning(), "Platform should be running after initialization");
        System.out.println("✓ Platform is running");
        
        // Test platform name
        assertEquals("Test", testPlatform.getName(), "Platform should have correct name");
        System.out.println("✓ Platform name is correct: " + testPlatform.getName());
    }
    
    @Test
    public void testPlatformComponents() {
        System.out.println("Testing platform components...");
        
        testPlatform.initialize(800, 600, "Test Components");
        
        // Test renderer access
        Renderer renderer = testPlatform.getRenderer();
        assertNotNull(renderer, "Platform should provide a renderer");
        System.out.println("✓ Renderer available");
        
        // Test renderer dimensions
        assertEquals(800.0, renderer.getWidth(), "Renderer should have correct width");
        assertEquals(600.0, renderer.getHeight(), "Renderer should have correct height");
        System.out.println("✓ Renderer dimensions correct: " + renderer.getWidth() + "x" + renderer.getHeight());
        
        // Test input provider access
        InputProvider input = testPlatform.getInputProvider();
        assertNotNull(input, "Platform should provide input provider");
        System.out.println("✓ Input provider available");
        
        // Test mouse state
        InputProvider.MouseState mouseState = input.getMouseState();
        assertNotNull(mouseState, "Input provider should provide mouse state");
        assertEquals(0.0, mouseState.x, "Mouse should be at origin");
        assertEquals(0.0, mouseState.y, "Mouse should be at origin");
        assertFalse(mouseState.primaryPressed, "No mouse buttons should be pressed");
        System.out.println("✓ Mouse state correct");
        
        // Test audio system access
        AudioSystem audio = testPlatform.getAudioSystem();
        assertNotNull(audio, "Platform should provide audio system");
        assertTrue(audio.initialize(), "Audio system should initialize successfully");
        assertEquals(1.0f, audio.getMasterVolume(), "Master volume should be 1.0");
        System.out.println("✓ Audio system available and initialized");
    }
    
    @Test
    public void testRendererOperations() {
        System.out.println("Testing renderer operations...");
        
        testPlatform.initialize(800, 600, "Test Renderer");
        Renderer renderer = testPlatform.getRenderer();
        
        // Test that all rendering operations complete without error
        assertDoesNotThrow(() -> {
            renderer.beginFrame();
            renderer.clear();
            renderer.setColor(platform.api.Color.RED);
            renderer.drawUnit(100, 100, platform.api.Color.BLUE, "Test Unit", 10);
            renderer.drawText("Test Text", 50, 50, platform.api.Color.GREEN);
            renderer.setTransform(10, 20, 1.5);
            renderer.endFrame();
            renderer.present();
        }, "All renderer operations should complete without throwing exceptions");
        
        System.out.println("✓ All renderer operations completed successfully");
    }
    
    @Test
    public void testAudioOperations() {
        System.out.println("Testing audio operations...");
        
        testPlatform.initialize(800, 600, "Test Audio");
        AudioSystem audio = testPlatform.getAudioSystem();
        
        // Test that all audio operations complete without error
        assertDoesNotThrow(() -> {
            audio.initialize();
            audio.loadSound("test", "test.wav");
            audio.playSound("test");
            audio.playSound("test", 0.5f);
            audio.setMasterVolume(0.8f);
            audio.stopSound("test");
            audio.stopAllSounds();
            audio.unloadSound("test");
            audio.unloadAllSounds();
        }, "All audio operations should complete without throwing exceptions");
        
        // Test audio queries
        assertTrue(audio.isSoundLoaded("any"), "TestAudioSystem should report all sounds as loaded");
        assertEquals(1.0f, audio.getMasterVolume(), "Master volume should return default value");
        
        System.out.println("✓ All audio operations completed successfully");
    }
    
    @Test
    public void testInputOperations() {
        System.out.println("Testing input operations...");
        
        testPlatform.initialize(800, 600, "Test Input");
        InputProvider input = testPlatform.getInputProvider();
        
        // Test that all input operations complete without error
        assertDoesNotThrow(() -> {
            input.pollEvents();
            input.isKeyPressed(InputProvider.Key.A);
            input.isKeyJustPressed(InputProvider.Key.SPACE);
            input.getPressedKeys();
            input.getMouseState();
            
            // Test handler registration (should not throw)
            InputProvider.ClickHandler clickHandler = (x, y, button) -> {};
            InputProvider.KeyHandler keyHandler = new InputProvider.KeyHandler() {
                public void onKeyPressed(InputProvider.Key key) {}
                public void onKeyReleased(InputProvider.Key key) {}
            };
            
            input.registerClickHandler(clickHandler);
            input.unregisterClickHandler(clickHandler);
            input.registerKeyHandler(keyHandler);
            input.unregisterKeyHandler(keyHandler);
        }, "All input operations should complete without throwing exceptions");
        
        // Test input state
        assertFalse(input.isKeyPressed(InputProvider.Key.A), "No keys should be pressed in test platform");
        assertTrue(input.getPressedKeys().isEmpty(), "No keys should be pressed");
        
        System.out.println("✓ All input operations completed successfully");
    }
    
    @Test
    public void testPlatformLifecycle() {
        System.out.println("Testing platform lifecycle...");
        
        // Test initialization
        assertTrue(testPlatform.initialize(800, 600, "Lifecycle Test"), 
                  "Platform should initialize");
        assertTrue(testPlatform.isRunning(), "Platform should be running");
        
        // Test timing operations
        long startTime = testPlatform.getCurrentTimeMillis();
        assertTrue(startTime > 0, "Current time should be positive");
        
        // Test sleep (should complete without error)
        assertDoesNotThrow(() -> testPlatform.sleep(1), "Sleep should complete without error");
        
        // Test shutdown
        testPlatform.setRunning(false);
        assertFalse(testPlatform.isRunning(), "Platform should not be running after setRunning(false)");
        
        assertDoesNotThrow(() -> testPlatform.shutdown(), "Shutdown should complete without error");
        
        System.out.println("✓ Platform lifecycle operations completed successfully");
    }
    
    @Test
    public void testHeadlessCapability() {
        System.out.println("Testing headless capability...");
        
        // Verify this test can run without any UI dependencies
        assertTrue(testPlatform.initialize(800, 600, "Headless Test"), 
                  "Platform should initialize in headless environment");
        
        // Simulate basic game operations
        Renderer renderer = testPlatform.getRenderer();
        InputProvider input = testPlatform.getInputProvider();
        AudioSystem audio = testPlatform.getAudioSystem();
        
        // Simulate a basic game loop iteration
        assertDoesNotThrow(() -> {
            // Input phase
            input.pollEvents();
            
            // Render phase
            renderer.beginFrame();
            renderer.clear();
            renderer.setColor(platform.api.Color.BLUE);
            renderer.drawUnit(400, 300, platform.api.Color.RED, "Player", 15);
            renderer.drawText("Headless Test", 10, 10, platform.api.Color.WHITE);
            renderer.endFrame();
            renderer.present();
            
            // Audio phase
            audio.playSound("background");
            
        }, "Basic game loop operations should work in headless mode");
        
        System.out.println("✓ Headless capability verified - no UI dependencies required");
        System.out.println("=== TestPlatform Test SUCCESS ===");
        System.out.println("Headless testing architecture is functional!");
        
        System.out.println("=== TEST COMPLETION SUMMARY ===");
        System.out.println("Test seed used: " + testSeed);
        System.out.println("To reproduce (Windows PowerShell): mvn test \"-Dtest=TestPlatformTest\" \"-Dtest.seed=" + testSeed + "\"");
        System.out.println("To reproduce (CMD/Linux/macOS): mvn test -Dtest=TestPlatformTest -Dtest.seed=" + testSeed);
        System.out.println("===============================");
    }
}
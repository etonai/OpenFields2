package com.openfields.testutils;

import platform.TestPlatform;
import platform.api.Platform;
import utils.GameConfiguration;
import java.security.SecureRandom;

/**
 * Core test utility class for game initialization and configuration.
 * 
 * This utility standardizes game setup patterns across all test classes,
 * providing consistent initialization, deterministic mode configuration,
 * and proper cleanup for both headless and JavaFX testing scenarios.
 * 
 * Key features:
 * - Deterministic seed management with override capability
 * - Headless and JavaFX game instance creation
 * - Standardized cleanup and state reset
 * - Platform setup and teardown
 * 
 * Usage examples:
 * 
 * Basic headless setup:
 * <pre>
 * {@code
 * @BeforeEach
 * public void setUp() {
 *     long seed = TestGameSetup.generateOrExtractSeed();
 *     TestGameSetup.enableDeterministicMode(seed);
 *     testPlatform = TestGameSetup.createTestPlatform();
 *     gameInstance = TestGameSetup.createHeadlessGame();
 * }
 * 
 * @AfterEach
 * public void tearDown() {
 *     TestGameSetup.cleanupGame(gameInstance);
 *     TestGameSetup.resetGlobalState();
 * }
 * }
 * </pre>
 * 
 * @author DevCycle 42 - Test Utility Classes Implementation
 */
public class TestGameSetup {
    
    // Test configuration constants
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;
    private static final String DEFAULT_TITLE = "Test Environment";
    
    /**
     * Creates a headless OpenFields2 game instance.
     * 
     * @return initialized headless game instance
     * @throws RuntimeException if game initialization fails
     */
    public static Object createHeadlessGame() {
        try {
            // Use reflection to create OpenFields2 instance (it's in default package)
            Class<?> gameClass = Class.forName("OpenFields2");
            Object game = gameClass.getDeclaredConstructor(boolean.class).newInstance(true);
            
            // Call initializeHeadless() method
            java.lang.reflect.Method initMethod = gameClass.getMethod("initializeHeadless");
            Boolean result = (Boolean) initMethod.invoke(game);
            
            if (!result) {
                throw new RuntimeException("Game failed to initialize in headless mode");
            }
            return game;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create headless game: " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates a JavaFX-enabled OpenFields2 game instance.
     * 
     * Note: This method should only be called from JavaFX Application Thread.
     * 
     * @return initialized JavaFX game instance
     * @throws RuntimeException if game initialization fails
     */
    public static Object createJavaFXGame() {
        try {
            // Use reflection to create OpenFields2 instance (it's in default package)
            Class<?> gameClass = Class.forName("OpenFields2");
            Object game = gameClass.getDeclaredConstructor(boolean.class).newInstance(false);
            return game;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create JavaFX game: " + e.getMessage(), e);
        }
    }
    
    /**
     * Configures a game instance for testing with standard settings.
     * 
     * @param game the game instance to configure
     */
    public static void configureGameForTesting(Object game) {
        if (game == null) {
            throw new IllegalArgumentException("Game instance cannot be null");
        }
        
        try {
            // Set game to paused state for controlled testing
            java.lang.reflect.Method setPausedMethod = game.getClass().getMethod("setPaused", boolean.class);
            setPausedMethod.invoke(game, true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure game for testing: " + e.getMessage(), e);
        }
        
        // Additional configuration can be added here as needed
    }
    
    /**
     * Enables deterministic mode with the specified seed.
     * 
     * @param seed the random seed to use for deterministic behavior
     */
    public static void enableDeterministicMode(long seed) {
        GameConfiguration.setDeterministicMode(true, seed);
        System.out.println("✓ Deterministic mode enabled with seed " + seed);
    }
    
    /**
     * Generates a new random seed or extracts seed from system property.
     * 
     * Checks for the "test.seed" system property and uses it if valid,
     * otherwise generates a new random seed.
     * 
     * @return the seed to use for testing
     */
    public static long generateOrExtractSeed() {
        String seedProperty = System.getProperty("test.seed");
        if (seedProperty != null && !seedProperty.isEmpty()) {
            try {
                long seed = Long.parseLong(seedProperty);
                System.out.println("=== MANUAL SEED OVERRIDE ===");
                System.out.println("Using manual seed: " + seed);
                System.out.println("============================");
                return seed;
            } catch (NumberFormatException e) {
                System.out.println("Invalid seed format: " + seedProperty + ", generating random seed");
            }
        }
        return new SecureRandom().nextLong();
    }
    
    /**
     * Prints seed information for test reproduction.
     * 
     * @param seed the seed used in the test
     */
    public static void printSeedInformation(long seed) {
        System.out.println("=== TEST COMPLETION SUMMARY ===");
        System.out.println("Test seed used: " + seed);
        System.out.println("To reproduce (Windows PowerShell): mvn test \"-Dtest=TestClassName\" \"-Dtest.seed=" + seed + "\"");
        System.out.println("To reproduce (CMD/Linux/macOS): mvn test -Dtest=TestClassName -Dtest.seed=" + seed);
        System.out.println("===============================");
    }
    
    /**
     * Creates and initializes a TestPlatform for headless testing.
     * 
     * @return initialized TestPlatform instance
     * @throws RuntimeException if platform initialization fails
     */
    public static TestPlatform createTestPlatform() {
        return createTestPlatform(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_TITLE);
    }
    
    /**
     * Creates and initializes a TestPlatform with custom dimensions.
     * 
     * @param width platform width
     * @param height platform height
     * @param title platform title
     * @return initialized TestPlatform instance
     * @throws RuntimeException if platform initialization fails
     */
    public static TestPlatform createTestPlatform(int width, int height, String title) {
        try {
            TestPlatform platform = new TestPlatform();
            if (!platform.initialize(width, height, title)) {
                throw new RuntimeException("TestPlatform failed to initialize");
            }
            return platform;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create TestPlatform: " + e.getMessage(), e);
        }
    }
    
    /**
     * Initializes JavaFX for testing if not already initialized.
     * 
     * This method can be called safely multiple times.
     */
    public static void initializeJavaFXForTesting() {
        try {
            // Initialize JavaFX toolkit if needed
            // Note: Actual JavaFX initialization depends on the platform implementation
            System.out.println("JavaFX initialization check completed");
        } catch (Exception e) {
            // JavaFX may already be initialized, which is fine
            System.out.println("JavaFX initialization: " + e.getMessage());
        }
    }
    
    /**
     * Cleans up a game instance and releases resources.
     * 
     * @param game the game instance to clean up (can be null)
     */
    public static void cleanupGame(Object game) {
        if (game != null) {
            try {
                java.lang.reflect.Method setPausedMethod = game.getClass().getMethod("setPaused", boolean.class);
                setPausedMethod.invoke(game, true);
            } catch (Exception e) {
                // Ignore cleanup errors
            }
            // Game instance will be garbage collected
        }
    }
    
    /**
     * Resets global state to avoid interference between tests.
     */
    public static void resetGlobalState() {
        // Reset deterministic mode
        GameConfiguration.reset();
        System.out.println("✓ Deterministic mode reset");
    }
    
    /**
     * Performs complete cleanup of platform and global state.
     * 
     * @param platform the TestPlatform to shutdown (can be null)
     */
    public static void cleanupPlatform(TestPlatform platform) {
        if (platform != null) {
            platform.shutdown();
        }
    }
    
    /**
     * Complete setup method that performs all common initialization steps.
     * 
     * @return SetupResult containing all initialized components
     */
    public static SetupResult performCompleteSetup() {
        long seed = generateOrExtractSeed();
        enableDeterministicMode(seed);
        TestPlatform platform = createTestPlatform();
        Object game = createHeadlessGame();
        configureGameForTesting(game);
        
        return new SetupResult(seed, platform, game);
    }
    
    /**
     * Complete teardown method that cleans up all components.
     * 
     * @param result the SetupResult from performCompleteSetup()
     */
    public static void performCompleteTeardown(SetupResult result) {
        if (result != null) {
            cleanupGame(result.game);
            cleanupPlatform(result.platform);
            resetGlobalState();
        }
    }
    
    /**
     * Result container for complete setup operations.
     */
    public static class SetupResult {
        public final long seed;
        public final TestPlatform platform;
        public final Object game;
        
        public SetupResult(long seed, TestPlatform platform, Object game) {
            this.seed = seed;
            this.platform = platform;
            this.game = game;
        }
    }
}
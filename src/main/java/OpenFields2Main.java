import platform.PlatformInitializer;
import platform.api.Platform;
import platform.api.PlatformFactory;
import core.GameEngine;
import core.InputAdapter;
import combat.Character;
import combat.Handedness;
import game.Unit;

/**
 * New main entry point that supports platform selection.
 * Can run in JavaFX mode (default) or console mode.
 */
public class OpenFields2Main {
    
    public static void main(String[] args) {
        // Initialize platforms
        PlatformInitializer.registerPlatforms();
        
        // Determine platform from arguments
        String platformName = PlatformInitializer.getPlatformFromArgs(args);
        boolean useNewEngine = hasFlag(args, "--use-new-engine");
        
        if (platformName == null) {
            platformName = PlatformFactory.getDefaultPlatform();
        }
        
        System.out.println("Starting OpenFields2 with platform: " + platformName);
        System.out.println("Using new engine: " + useNewEngine);
        
        if ("console".equals(platformName) || useNewEngine) {
            // Launch with new platform abstraction
            launchWithPlatform(platformName);
        } else {
            // Launch original JavaFX application using reflection to avoid dependency issues
            launchJavaFXApplication(args);
        }
    }
    
    private static void launchJavaFXApplication(String[] args) {
        try {
            // Use reflection to avoid direct JavaFX dependency when running console mode
            Class<?> applicationClass = Class.forName("javafx.application.Application");
            Class<?> openFields2Class = Class.forName("OpenFields2");
            
            // Call Application.launch(OpenFields2.class, args)
            java.lang.reflect.Method launchMethod = applicationClass.getMethod("launch", Class.class, String[].class);
            launchMethod.invoke(null, openFields2Class, args);
            
        } catch (Exception e) {
            System.err.println("Failed to launch JavaFX application: " + e.getMessage());
            System.err.println("Make sure JavaFX is available on the classpath for JavaFX mode.");
            System.exit(1);
        }
    }
    
    private static void launchWithPlatform(String platformName) {
        // Set system property to disable automatic weapon initialization in console mode
        if ("console".equals(platformName)) {
            System.setProperty("openfields2.skipDefaultWeapons", "true");
        }
        
        // Create platform
        Platform platform = PlatformFactory.createPlatform(platformName);
        if (platform == null) {
            System.err.println("Unknown platform: " + platformName);
            System.err.println("Available platforms: " + 
                             String.join(", ", PlatformFactory.getRegisteredPlatforms()));
            System.exit(1);
        }
        
        // Initialize platform
        if (!platform.initialize(800, 600, "OpenFields2")) {
            System.err.println("Failed to initialize platform: " + platformName);
            System.exit(1);
        }
        
        try {
            // Create game engine
            GameEngine engine = new GameEngine(platform);
            
            // Set up input adapter
            InputAdapter inputAdapter = new InputAdapter(engine, System.out::println);
            
            // TODO: Initialize game state (load units, weapons, etc.)
            initializeGameState(engine);
            
            // Run game loop
            engine.run();
            
        } finally {
            // Shutdown platform
            platform.shutdown();
        }
    }
    
    private static void initializeGameState(GameEngine engine) {
        System.out.println("Initializing game state...");
        
        try {
            // For console mode, we can't create Units with JavaFX dependencies
            // This would require creating platform-independent units
            System.out.println("Console mode game state initialization complete.");
            System.out.println("Note: Full game content requires platform-independent Unit class.");
            
            // TODO: When platform-independent units are available:
            // - Create test characters
            // - Create platform units using platform.api.Color
            // - Add units to game state
            
        } catch (Exception e) {
            System.err.println("Failed to initialize game state: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static boolean hasFlag(String[] args, String flag) {
        for (String arg : args) {
            if (flag.equals(arg)) {
                return true;
            }
        }
        return false;
    }
}
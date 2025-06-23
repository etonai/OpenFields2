import platform.PlatformInitializer;
import platform.api.Platform;
import platform.api.PlatformFactory;
import core.GameEngine;
import core.InputAdapter;
import javafx.application.Application;

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
        
        if (!useNewEngine || "javafx".equals(platformName)) {
            // Launch original JavaFX application
            Application.launch(OpenFields2.class, args);
        } else {
            // Launch with new platform abstraction
            launchWithPlatform(platformName);
        }
    }
    
    private static void launchWithPlatform(String platformName) {
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
        // TODO: This would load the initial game state
        // For now, create some test units
        
        System.out.println("Initializing game state...");
        
        // This is placeholder - would need to properly initialize
        // units, weapons, factions, etc. from the game data
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
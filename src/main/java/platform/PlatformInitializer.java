package platform;

import platform.api.PlatformFactory;
import platform.impl.javafx.JavaFXPlatform;
import platform.impl.console.ConsolePlatform;

/**
 * Initializes and registers all available platforms.
 */
public class PlatformInitializer {
    
    /**
     * Registers all platform implementations with the factory.
     */
    public static void registerPlatforms() {
        // Register JavaFX platform
        PlatformFactory.registerPlatform("javafx", JavaFXPlatform::new);
        
        // Register Console platform
        PlatformFactory.registerPlatform("console", ConsolePlatform::new);
        
        // Set default platform
        PlatformFactory.setDefaultPlatform("javafx");
    }
    
    /**
     * Determines platform from command line arguments.
     * @param args command line arguments
     * @return platform name or null to use default
     */
    public static String getPlatformFromArgs(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--platform=")) {
                return arg.substring("--platform=".length());
            }
            if (arg.equals("-Dplatform=console")) {
                return "console";
            }
        }
        
        // Check system property
        String sysProp = System.getProperty("platform");
        if (sysProp != null) {
            return sysProp;
        }
        
        return null;
    }
}
package platform.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Factory for creating platform instances.
 * Allows registration of different platform implementations.
 */
public class PlatformFactory {
    private static final Map<String, Supplier<Platform>> platforms = new HashMap<>();
    private static String defaultPlatform = "javafx";
    
    /**
     * Registers a platform implementation.
     * @param name platform name (e.g., "javafx", "console")
     * @param platformSupplier supplier that creates platform instances
     */
    public static void registerPlatform(String name, Supplier<Platform> platformSupplier) {
        platforms.put(name.toLowerCase(), platformSupplier);
    }
    
    /**
     * Creates a platform instance by name.
     * @param name platform name
     * @return platform instance, or null if not found
     */
    public static Platform createPlatform(String name) {
        Supplier<Platform> supplier = platforms.get(name.toLowerCase());
        return supplier != null ? supplier.get() : null;
    }
    
    /**
     * Creates the default platform instance.
     * @return default platform instance
     */
    public static Platform createDefaultPlatform() {
        return createPlatform(defaultPlatform);
    }
    
    /**
     * Sets the default platform name.
     * @param name default platform name
     */
    public static void setDefaultPlatform(String name) {
        defaultPlatform = name.toLowerCase();
    }
    
    /**
     * Gets the default platform name.
     * @return default platform name
     */
    public static String getDefaultPlatform() {
        return defaultPlatform;
    }
    
    /**
     * Checks if a platform is registered.
     * @param name platform name
     * @return true if the platform is registered
     */
    public static boolean isPlatformRegistered(String name) {
        return platforms.containsKey(name.toLowerCase());
    }
    
    /**
     * Gets all registered platform names.
     * @return array of platform names
     */
    public static String[] getRegisteredPlatforms() {
        return platforms.keySet().toArray(new String[0]);
    }
    
    /**
     * Clears all registered platforms.
     * Mainly useful for testing.
     */
    public static void clearRegistrations() {
        platforms.clear();
    }
}
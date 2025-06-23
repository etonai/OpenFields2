package platform.api;

/**
 * Composite interface that provides access to all platform services.
 * Each platform implementation provides its own version of these services.
 */
public interface Platform {
    /**
     * Gets the renderer for this platform.
     * @return the platform's renderer
     */
    Renderer getRenderer();
    
    /**
     * Gets the input provider for this platform.
     * @return the platform's input provider
     */
    InputProvider getInputProvider();
    
    /**
     * Gets the audio system for this platform.
     * @return the platform's audio system
     */
    AudioSystem getAudioSystem();
    
    /**
     * Gets the name of this platform.
     * @return platform name (e.g., "JavaFX", "Console")
     */
    String getName();
    
    /**
     * Initializes the platform.
     * Called once before the platform is used.
     * @param width initial width
     * @param height initial height
     * @param title window title (if applicable)
     * @return true if initialization was successful
     */
    boolean initialize(int width, int height, String title);
    
    /**
     * Shuts down the platform.
     * Called once when the application exits.
     */
    void shutdown();
    
    /**
     * Checks if the platform is still running.
     * @return true if the platform is running
     */
    boolean isRunning();
    
    /**
     * Sets whether the platform should continue running.
     * @param running true to keep running, false to stop
     */
    void setRunning(boolean running);
    
    /**
     * Gets the current time in milliseconds.
     * Used for timing and animation.
     * @return current time in milliseconds
     */
    long getCurrentTimeMillis();
    
    /**
     * Sleeps for the specified number of milliseconds.
     * Used for frame rate limiting.
     * @param millis milliseconds to sleep
     */
    void sleep(long millis);
}
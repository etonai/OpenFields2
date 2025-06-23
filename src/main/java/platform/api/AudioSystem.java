package platform.api;

/**
 * Platform-independent audio system interface.
 * Provides sound loading and playback functionality.
 */
public interface AudioSystem {
    /**
     * Loads a sound from a resource path.
     * @param soundId unique identifier for the sound
     * @param resourcePath path to the sound resource
     * @return true if the sound was loaded successfully
     */
    boolean loadSound(String soundId, String resourcePath);
    
    /**
     * Plays a previously loaded sound.
     * @param soundId identifier of the sound to play
     */
    void playSound(String soundId);
    
    /**
     * Plays a previously loaded sound with volume control.
     * @param soundId identifier of the sound to play
     * @param volume volume level (0.0 to 1.0)
     */
    void playSound(String soundId, float volume);
    
    /**
     * Stops a currently playing sound.
     * @param soundId identifier of the sound to stop
     */
    void stopSound(String soundId);
    
    /**
     * Stops all currently playing sounds.
     */
    void stopAllSounds();
    
    /**
     * Sets the master volume for all sounds.
     * @param volume volume level (0.0 to 1.0)
     */
    void setMasterVolume(float volume);
    
    /**
     * Gets the current master volume.
     * @return volume level (0.0 to 1.0)
     */
    float getMasterVolume();
    
    /**
     * Checks if a sound is currently loaded.
     * @param soundId identifier of the sound
     * @return true if the sound is loaded
     */
    boolean isSoundLoaded(String soundId);
    
    /**
     * Unloads a sound from memory.
     * @param soundId identifier of the sound to unload
     */
    void unloadSound(String soundId);
    
    /**
     * Unloads all sounds from memory.
     */
    void unloadAllSounds();
    
    /**
     * Initializes the audio system.
     * Called once at startup.
     * @return true if initialization was successful
     */
    boolean initialize();
    
    /**
     * Shuts down the audio system.
     * Called once at shutdown.
     */
    void shutdown();
}
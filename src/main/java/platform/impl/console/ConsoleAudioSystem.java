package platform.impl.console;

import platform.api.AudioSystem;
import config.DebugConfig;
import java.util.HashSet;
import java.util.Set;

/**
 * Console implementation of the AudioSystem interface.
 * Provides stub audio functionality for console mode.
 */
public class ConsoleAudioSystem implements AudioSystem {
    private final Set<String> loadedSounds;
    private float masterVolume;
    private boolean debugMode;
    
    public ConsoleAudioSystem() {
        this.loadedSounds = new HashSet<>();
        this.masterVolume = 1.0f;
        this.debugMode = false;
    }
    
    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
    }
    
    @Override
    public boolean loadSound(String soundId, String resourcePath) {
        loadedSounds.add(soundId);
        if (debugMode) {
            System.out.println("[Audio] Loaded sound: " + soundId + " from " + resourcePath);
        }
        return true;
    }
    
    @Override
    public void playSound(String soundId) {
        playSound(soundId, 1.0f);
    }
    
    @Override
    public void playSound(String soundId, float volume) {
        if (loadedSounds.contains(soundId)) {
            if (debugMode || DebugConfig.getInstance().isCombatDebugEnabled()) {
                System.out.println("[AUDIO-SYSTEM] Playing audio file: " + soundId + " (volume: " + (masterVolume * volume) + ")");
            }
            
            // Could use terminal bell for certain sounds
            if (soundId.contains("hit") || soundId.contains("impact")) {
                System.out.print("\007"); // Terminal bell
            }
        }
    }
    
    @Override
    public void stopSound(String soundId) {
        if (debugMode) {
            System.out.println("[Audio] Stopping: " + soundId);
        }
    }
    
    @Override
    public void stopAllSounds() {
        if (debugMode) {
            System.out.println("[Audio] Stopping all sounds");
        }
    }
    
    @Override
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0, Math.min(1, volume));
        if (debugMode) {
            System.out.println("[Audio] Master volume set to: " + masterVolume);
        }
    }
    
    @Override
    public float getMasterVolume() {
        return masterVolume;
    }
    
    @Override
    public boolean isSoundLoaded(String soundId) {
        return loadedSounds.contains(soundId);
    }
    
    @Override
    public void unloadSound(String soundId) {
        loadedSounds.remove(soundId);
        if (debugMode) {
            System.out.println("[Audio] Unloaded sound: " + soundId);
        }
    }
    
    @Override
    public void unloadAllSounds() {
        loadedSounds.clear();
        if (debugMode) {
            System.out.println("[Audio] Unloaded all sounds");
        }
    }
    
    @Override
    public boolean initialize() {
        if (debugMode) {
            System.out.println("[Audio] Console audio system initialized");
        }
        return true;
    }
    
    @Override
    public void shutdown() {
        unloadAllSounds();
        if (debugMode) {
            System.out.println("[Audio] Console audio system shut down");
        }
    }
}
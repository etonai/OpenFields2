package platform.impl.javafx;

import platform.api.AudioSystem;
import javafx.scene.media.AudioClip;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * JavaFX implementation of the AudioSystem interface.
 * Wraps JavaFX AudioClip functionality.
 */
public class JavaFXAudioSystem implements AudioSystem {
    private final Map<String, AudioClip> sounds;
    private float masterVolume;
    
    public JavaFXAudioSystem() {
        this.sounds = new HashMap<>();
        this.masterVolume = 1.0f;
    }
    
    @Override
    public boolean loadSound(String soundId, String resourcePath) {
        try {
            URL soundUrl = getClass().getResource(resourcePath);
            if (soundUrl == null) {
                System.err.println("Sound not found: " + resourcePath);
                return false;
            }
            
            AudioClip clip = new AudioClip(soundUrl.toString());
            sounds.put(soundId, clip);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to load sound: " + resourcePath);
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public void playSound(String soundId) {
        playSound(soundId, 1.0f);
    }
    
    @Override
    public void playSound(String soundId, float volume) {
        AudioClip clip = sounds.get(soundId);
        if (clip != null) {
            clip.play(masterVolume * Math.max(0, Math.min(1, volume)));
        }
    }
    
    @Override
    public void stopSound(String soundId) {
        AudioClip clip = sounds.get(soundId);
        if (clip != null) {
            clip.stop();
        }
    }
    
    @Override
    public void stopAllSounds() {
        for (AudioClip clip : sounds.values()) {
            clip.stop();
        }
    }
    
    @Override
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0, Math.min(1, volume));
    }
    
    @Override
    public float getMasterVolume() {
        return masterVolume;
    }
    
    @Override
    public boolean isSoundLoaded(String soundId) {
        return sounds.containsKey(soundId);
    }
    
    @Override
    public void unloadSound(String soundId) {
        AudioClip clip = sounds.remove(soundId);
        if (clip != null) {
            clip.stop();
        }
    }
    
    @Override
    public void unloadAllSounds() {
        stopAllSounds();
        sounds.clear();
    }
    
    @Override
    public boolean initialize() {
        // JavaFX audio is initialized automatically
        return true;
    }
    
    @Override
    public void shutdown() {
        unloadAllSounds();
    }
}
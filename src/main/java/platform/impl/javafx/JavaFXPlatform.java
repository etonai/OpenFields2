package platform.impl.javafx;

import platform.api.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the Platform interface.
 * Provides JavaFX-based implementations of all platform services.
 */
public class JavaFXPlatform implements platform.api.Platform {
    private JavaFXRenderer renderer;
    private JavaFXInputProvider inputProvider;
    private JavaFXAudioSystem audioSystem;
    private Stage stage;
    private Canvas canvas;
    private boolean running;
    
    // For use with existing JavaFX application
    public JavaFXPlatform(Stage stage, Canvas canvas, Scene scene) {
        this.stage = stage;
        this.canvas = canvas;
        this.renderer = new JavaFXRenderer(canvas);
        this.inputProvider = new JavaFXInputProvider(scene);
        this.audioSystem = new JavaFXAudioSystem();
        this.running = true;
    }
    
    // For standalone use
    public JavaFXPlatform() {
        this.audioSystem = new JavaFXAudioSystem();
        this.running = true;
    }
    
    @Override
    public Renderer getRenderer() {
        return renderer;
    }
    
    @Override
    public InputProvider getInputProvider() {
        return inputProvider;
    }
    
    @Override
    public AudioSystem getAudioSystem() {
        return audioSystem;
    }
    
    @Override
    public String getName() {
        return "JavaFX";
    }
    
    @Override
    public boolean initialize(int width, int height, String title) {
        if (stage != null) {
            // Already initialized with existing stage
            return true;
        }
        
        // Create new JavaFX window (would need to be on JavaFX thread)
        // For now, assume we're using existing JavaFX application
        return false;
    }
    
    @Override
    public void shutdown() {
        running = false;
        audioSystem.shutdown();
        
        if (stage != null) {
            Platform.runLater(() -> stage.close());
        }
    }
    
    @Override
    public boolean isRunning() {
        return running && (stage == null || stage.isShowing());
    }
    
    @Override
    public void setRunning(boolean running) {
        this.running = running;
    }
    
    @Override
    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }
    
    @Override
    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Sets up the platform with existing JavaFX components.
     * Called from the main JavaFX application.
     */
    public void setupWithExisting(Stage stage, Canvas canvas, Scene scene) {
        this.stage = stage;
        this.canvas = canvas;
        this.renderer = new JavaFXRenderer(canvas);
        this.inputProvider = new JavaFXInputProvider(scene);
    }
}
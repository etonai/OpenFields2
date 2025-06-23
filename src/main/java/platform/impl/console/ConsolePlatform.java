package platform.impl.console;

import platform.api.*;

/**
 * Console implementation of the Platform interface.
 * Provides a text-based interface for the game.
 */
public class ConsolePlatform implements Platform {
    private ConsoleRenderer renderer;
    private ConsoleInputProvider inputProvider;
    private ConsoleAudioSystem audioSystem;
    private boolean running;
    
    public ConsolePlatform() {
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
        return "Console";
    }
    
    @Override
    public boolean initialize(int width, int height, String title) {
        System.out.println("=== " + title + " - Console Mode ===");
        System.out.println("Initializing console interface...");
        
        // Create console components
        this.renderer = new ConsoleRenderer(System.out, 80, 24);
        this.inputProvider = new ConsoleInputProvider();
        this.audioSystem = new ConsoleAudioSystem();
        
        // Initialize audio
        audioSystem.initialize();
        
        System.out.println("Console interface ready!");
        System.out.println("Press 'h' for help, 'q' to quit\n");
        
        return true;
    }
    
    @Override
    public void shutdown() {
        running = false;
        
        if (inputProvider != null) {
            inputProvider.shutdown();
        }
        
        if (audioSystem != null) {
            audioSystem.shutdown();
        }
        
        System.out.println("\nShutting down console interface...");
    }
    
    @Override
    public boolean isRunning() {
        return running;
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
}
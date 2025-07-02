package platform;

import platform.api.*;

/**
 * Minimal platform implementation for headless testing.
 * Provides null implementations for rendering and audio while
 * supporting basic platform operations needed for game logic testing.
 */
public class TestPlatform implements Platform {
    private TestRenderer renderer;
    private TestInputProvider inputProvider;
    private TestAudioSystem audioSystem;
    private boolean running;
    
    public TestPlatform() {
        this.running = true;
        this.renderer = new TestRenderer();
        this.inputProvider = new TestInputProvider();
        this.audioSystem = new TestAudioSystem();
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
        return "Test";
    }
    
    @Override
    public boolean initialize(int width, int height, String title) {
        // No actual initialization needed for testing
        return true;
    }
    
    @Override
    public void shutdown() {
        running = false;
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
    
    /**
     * Null renderer for testing - all rendering operations are no-ops
     */
    private static class TestRenderer implements Renderer {
        @Override
        public void clear() {
            // No-op
        }
        
        @Override
        public void setColor(Color color) {
            // No-op
        }
        
        @Override
        public void drawUnit(double x, double y, Color color, String name, double radius) {
            // No-op
        }
        
        @Override
        public void drawLine(double x1, double y1, double x2, double y2, Color color, double width) {
            // No-op
        }
        
        @Override
        public void drawText(String text, double x, double y, Color color) {
            // No-op
        }
        
        @Override
        public void drawHealthBar(double x, double y, double width, double height, 
                                double percentage, Color borderColor, Color fillColor, Color backgroundColor) {
            // No-op
        }
        
        @Override
        public void fillRect(double x, double y, double width, double height, Color color) {
            // No-op
        }
        
        @Override
        public void drawRect(double x, double y, double width, double height, Color color, double lineWidth) {
            // No-op
        }
        
        @Override
        public void fillCircle(double centerX, double centerY, double radius, Color color) {
            // No-op
        }
        
        @Override
        public void drawCircle(double centerX, double centerY, double radius, Color color, double lineWidth) {
            // No-op
        }
        
        @Override
        public void setTransform(double offsetX, double offsetY, double zoom) {
            // No-op
        }
        
        @Override
        public void pushTransform() {
            // No-op
        }
        
        @Override
        public void popTransform() {
            // No-op
        }
        
        @Override
        public double getWidth() {
            return 800.0; // Default test width
        }
        
        @Override
        public double getHeight() {
            return 600.0; // Default test height
        }
        
        @Override
        public void present() {
            // No-op
        }
        
        @Override
        public void beginFrame() {
            // No-op
        }
        
        @Override
        public void endFrame() {
            // No-op
        }
    }
    
    /**
     * Null input provider for testing - no input events generated
     */
    private static class TestInputProvider implements InputProvider {
        @Override
        public void pollEvents() {
            // No input events in headless testing
        }
        
        @Override
        public boolean isKeyPressed(Key key) {
            return false; // No keys pressed in headless testing
        }
        
        @Override
        public boolean isKeyJustPressed(Key key) {
            return false; // No key events in headless testing
        }
        
        @Override
        public java.util.Set<Key> getPressedKeys() {
            return java.util.Collections.emptySet();
        }
        
        @Override
        public MouseState getMouseState() {
            return new MouseState(0, 0, false, false, false);
        }
        
        @Override
        public void registerClickHandler(ClickHandler handler) {
            // No-op - no click events in headless testing
        }
        
        @Override
        public void unregisterClickHandler(ClickHandler handler) {
            // No-op
        }
        
        @Override
        public void registerKeyHandler(KeyHandler handler) {
            // No-op - no key events in headless testing
        }
        
        @Override
        public void unregisterKeyHandler(KeyHandler handler) {
            // No-op
        }
    }
    
    /**
     * Null audio system for testing - all audio operations are no-ops
     */
    private static class TestAudioSystem implements AudioSystem {
        @Override
        public boolean loadSound(String soundId, String resourcePath) {
            return true; // Always successful for testing
        }
        
        @Override
        public void playSound(String soundId) {
            // No-op - silent testing
        }
        
        @Override
        public void playSound(String soundId, float volume) {
            // No-op - silent testing
        }
        
        @Override
        public void stopSound(String soundId) {
            // No-op
        }
        
        @Override
        public void stopAllSounds() {
            // No-op
        }
        
        @Override
        public void setMasterVolume(float volume) {
            // No-op
        }
        
        @Override
        public float getMasterVolume() {
            return 1.0f; // Default volume
        }
        
        @Override
        public boolean isSoundLoaded(String soundId) {
            return true; // Pretend all sounds are loaded
        }
        
        @Override
        public void unloadSound(String soundId) {
            // No-op
        }
        
        @Override
        public void unloadAllSounds() {
            // No-op
        }
        
        @Override
        public boolean initialize() {
            return true; // Always successful for testing
        }
        
        @Override
        public void shutdown() {
            // No-op
        }
    }
}
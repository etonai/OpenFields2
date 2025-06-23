import platform.PlatformInitializer;
import platform.api.Platform;
import platform.api.PlatformFactory;
import platform.api.Color;

/**
 * Simple test to verify console mode works.
 */
public class TestConsoleMode {
    public static void main(String[] args) {
        // Initialize platforms
        PlatformInitializer.registerPlatforms();
        
        // Create console platform
        Platform platform = PlatformFactory.createPlatform("console");
        
        if (platform == null) {
            System.err.println("Failed to create console platform");
            return;
        }
        
        // Initialize
        if (!platform.initialize(80, 24, "Console Test")) {
            System.err.println("Failed to initialize console platform");
            return;
        }
        
        try {
            // Test renderer
            platform.getRenderer().beginFrame();
            platform.getRenderer().clear();
            
            // Draw some test content
            platform.getRenderer().drawText("OpenFields2 Console Mode Test", 10, 2, Color.WHITE);
            platform.getRenderer().drawUnit(200, 100, Color.BLUE, "Unit 1", 10);
            platform.getRenderer().drawHealthBar(190, 90, 30, 5, 0.75, 
                                               Color.WHITE, Color.GREEN, Color.DARK_GRAY);
            
            platform.getRenderer().endFrame();
            platform.getRenderer().present();
            
            // Wait a bit
            Thread.sleep(3000);
            
            System.out.println("\nConsole mode test completed successfully!");
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            platform.shutdown();
        }
    }
}
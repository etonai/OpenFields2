import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.List;
import game.Unit;

/**
 * Handles all rendering operations for the OpenFields2 game.
 * Extracted from OpenFields2.java to separate rendering concerns.
 */
public class GameRenderer {
    // Canvas constants
    static final int WIDTH = 800;
    static final int HEIGHT = 600;
    
    // Canvas and camera state
    private final Canvas canvas;
    private double offsetX = 0;
    private double offsetY = 0;
    private double zoom = 1.0;
    
    // Debug rendering state
    private static boolean debugMode = false;
    
    // Game state references
    private List<Unit> units;
    private SelectionManager selectionManager;
    
    /**
     * Creates a new GameRenderer with the specified canvas.
     */
    public GameRenderer(Canvas canvas) {
        this.canvas = canvas;
    }
    
    /**
     * Sets the game state references needed for rendering.
     */
    public void setGameState(List<Unit> units, SelectionManager selectionManager) {
        this.units = units;
        this.selectionManager = selectionManager;
    }
    
    /**
     * Main rendering method that draws the entire game scene.
     */
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.save();
        gc.translate(offsetX, offsetY);
        gc.scale(zoom, zoom);
        
        // First pass: Draw all unit circles and basic elements
        for (Unit u : units) {
            boolean isSelected = selectionManager.isUnitSelected(u);
            u.render(gc, isSelected);
            
            // Draw cyan border for multi-selected units (when more than one unit selected)
            if (isSelected && selectionManager.getSelectionCount() > 1) {
                gc.setStroke(Color.CYAN);
                gc.setLineWidth(2);
                gc.strokeOval(u.x - 12, u.y - 12, 24, 24);
            }
        }
        
        // Draw selection-related graphics (rectangle and center marker)
        selectionManager.render(gc);
        
        // Second pass: Draw target overlays that need to appear on top
        if (selectionManager.getSelectionCount() == 1) {
            Unit selected = selectionManager.getSelected();
            if (selected.character.currentTarget != null) {
                Unit target = selected.character.currentTarget;
                
                if (selected.character.isPersistentAttack()) {
                    // Persistent attack: yellow X inside target
                    gc.setStroke(Color.YELLOW);
                    gc.setLineWidth(2);
                    gc.strokeLine(target.x - 5, target.y - 5, target.x + 5, target.y + 5);
                    gc.strokeLine(target.x - 5, target.y + 5, target.x + 5, target.y - 5);
                } else {
                    // Normal attack: small white circle inside target
                    gc.setStroke(Color.WHITE);
                    gc.setLineWidth(2);
                    gc.strokeOval(target.x - 3, target.y - 3, 6, 6);
                }
            }
        }
        
        gc.restore();
    }
    
    // Camera control methods
    public void setOffset(double offsetX, double offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }
    
    public void adjustOffset(double deltaX, double deltaY) {
        this.offsetX += deltaX;
        this.offsetY += deltaY;
    }
    
    public void setZoom(double zoom) {
        this.zoom = zoom;
    }
    
    public void adjustZoom(double factor) {
        this.zoom *= factor;
    }
    
    // Debug mode
    public static void setDebugMode(boolean enabled) {
        debugMode = enabled;
    }
    
    public static boolean isDebugMode() {
        return debugMode;
    }
    
    // Coordinate transformation utilities
    public double screenToWorldX(double screenX) {
        return (screenX - offsetX) / zoom;
    }
    
    public double screenToWorldY(double screenY) {
        return (screenY - offsetY) / zoom;
    }
    
    public double worldToScreenX(double worldX) {
        return worldX * zoom + offsetX;
    }
    
    public double worldToScreenY(double worldY) {
        return worldY * zoom + offsetY;
    }
    
    // Getters for camera state
    public double getOffsetX() {
        return offsetX;
    }
    
    public double getOffsetY() {
        return offsetY;
    }
    
    public double getZoom() {
        return zoom;
    }
    
    public Canvas getCanvas() {
        return canvas;
    }
}
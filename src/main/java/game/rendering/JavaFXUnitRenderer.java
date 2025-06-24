package game.rendering;

import game.interfaces.IUnit;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;

/**
 * JavaFX implementation of the unit renderer.
 * Handles rendering units using JavaFX GraphicsContext.
 */
public class JavaFXUnitRenderer implements IUnitRenderer {
    private final GraphicsContext gc;
    
    public JavaFXUnitRenderer(GraphicsContext gc) {
        this.gc = gc;
    }
    
    @Override
    public void renderUnit(IUnit unit, boolean isSelected, boolean debugMode) {
        // Draw unit circle
        gc.setFill(unit.getColor().toJavaFX());
        gc.fillOval(unit.getX() - 10.5, unit.getY() - 10.5, 21, 21);
        
        // Draw selection-related info
        if (isSelected) {
            // Draw movement target if present
            if (unit.hasTarget()) {
                renderMovementTarget(unit);
            }
            
            // Draw unit name and health
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font(12));
            String displayText = unit.getCharacter().getDisplayName() + 
                " (" + unit.getCharacter().currentHealth + "/" + unit.getCharacter().health + ")";
            gc.fillText(displayText, unit.getX() - 15, unit.getY() - 15);
            
            // Display movement type and aiming speed only in debug mode
            if (debugMode) {
                gc.setFont(Font.font(10));
                gc.fillText(unit.getCharacter().getCurrentMovementType().getDisplayName(), 
                    unit.getX() - 15, unit.getY() + 25);
                // Display aiming speed
                gc.fillText(unit.getCharacter().getCurrentAimingSpeed().getDisplayName(), 
                    unit.getX() - 15, unit.getY() + 35);
            }
        }
    }
    
    @Override
    public void renderMovementTarget(IUnit unit) {
        // Show small yellow X at movement target location
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(2);
        // Draw X by drawing two diagonal lines
        double targetX = unit.getTargetX();
        double targetY = unit.getTargetY();
        gc.strokeLine(targetX - 5, targetY - 5, targetX + 5, targetY + 5);
        gc.strokeLine(targetX - 5, targetY + 5, targetX + 5, targetY - 5);
    }
    
    @Override
    public void renderSelectionIndicator(IUnit unit, boolean isMultiSelect) {
        if (isMultiSelect) {
            // Draw cyan border for multi-selected units
            gc.setStroke(Color.CYAN);
            gc.setLineWidth(2);
            gc.strokeOval(unit.getX() - 12, unit.getY() - 12, 24, 24);
        }
    }
}
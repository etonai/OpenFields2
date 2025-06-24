package platform.impl.javafx;

import combat.Character;
import game.EntityFactory;
import game.Unit;
import game.interfaces.IUnit;
import platform.api.Color;

/**
 * JavaFX-specific implementation of entity factory.
 * Creates Unit instances that work with JavaFX rendering.
 */
public class JavaFXEntityFactory implements EntityFactory {
    
    private static final double ROTATION_SPEED = 6.0; // 6 degrees per tick
    private static final double ROTATION_THRESHOLD = 15.0; // Instant rotation under 15 degrees
    private static final Color INCAPACITATED_COLOR = Color.fromRGB(169, 169, 169); // Light gray
    
    @Override
    public IUnit createUnit(Character character, double x, double y, Color color, int id) {
        return new Unit(character, x, y, color, id);
    }
    
    @Override
    public IUnit createUnitFromData(Object unitData) {
        // TODO: Implement when save system is updated
        throw new UnsupportedOperationException("Unit deserialization not yet implemented");
    }
    
    @Override
    public Color getIncapacitatedColor() {
        return INCAPACITATED_COLOR;
    }
    
    @Override
    public double getRotationSpeed() {
        return ROTATION_SPEED;
    }
    
    @Override
    public double getRotationThreshold() {
        return ROTATION_THRESHOLD;
    }
}
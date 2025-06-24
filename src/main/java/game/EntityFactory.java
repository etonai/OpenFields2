package game;

import combat.Character;
import game.interfaces.IUnit;
import platform.api.Color;

/**
 * Factory interface for creating platform-specific entity implementations.
 */
public interface EntityFactory {
    
    /**
     * Creates a new unit with the specified parameters.
     * @param character the character for this unit
     * @param x initial x position
     * @param y initial y position
     * @param color unit color
     * @param id unique unit identifier
     * @return platform-specific IUnit implementation
     */
    IUnit createUnit(Character character, double x, double y, Color color, int id);
    
    /**
     * Creates a unit from existing unit data (for save/load).
     * @param unitData saved unit data
     * @return platform-specific IUnit implementation
     */
    IUnit createUnitFromData(Object unitData);
    
    /**
     * Gets the incapacitated color for units.
     * @return platform-specific incapacitated color
     */
    Color getIncapacitatedColor();
    
    /**
     * Gets the default rotation speed in degrees per tick.
     * @return rotation speed
     */
    double getRotationSpeed();
    
    /**
     * Gets the rotation threshold for instant rotation.
     * @return rotation threshold in degrees
     */
    double getRotationThreshold();
}
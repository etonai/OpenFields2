package game.interfaces;

import combat.Character;
import platform.api.Color;

/**
 * Platform-independent interface for game units.
 * Defines the core contract for units without any rendering dependencies.
 */
public interface IUnit {
    
    /**
     * Gets the unique identifier for this unit.
     * @return unit ID
     */
    int getId();
    
    /**
     * Gets the character associated with this unit.
     * @return the character
     */
    Character getCharacter();
    
    /**
     * Sets the character for this unit.
     * @param character the character to set
     */
    void setCharacter(Character character);
    
    /**
     * Gets the current X position of the unit.
     * @return x coordinate
     */
    double getX();
    
    /**
     * Sets the X position of the unit.
     * @param x new x coordinate
     */
    void setX(double x);
    
    /**
     * Gets the current Y position of the unit.
     * @return y coordinate
     */
    double getY();
    
    /**
     * Sets the Y position of the unit.
     * @param y new y coordinate
     */
    void setY(double y);
    
    /**
     * Gets the target X position for movement.
     * @return target x coordinate
     */
    double getTargetX();
    
    /**
     * Sets the target X position for movement.
     * @param targetX new target x coordinate
     */
    void setTargetX(double targetX);
    
    /**
     * Gets the target Y position for movement.
     * @return target y coordinate
     */
    double getTargetY();
    
    /**
     * Sets the target Y position for movement.
     * @param targetY new target y coordinate
     */
    void setTargetY(double targetY);
    
    /**
     * Checks if unit has a movement target.
     * @return true if unit has target
     */
    boolean hasTarget();
    
    /**
     * Sets whether unit has a movement target.
     * @param hasTarget target state
     */
    void setHasTarget(boolean hasTarget);
    
    /**
     * Checks if unit is stopped.
     * @return true if stopped
     */
    boolean isStopped();
    
    /**
     * Sets the stopped state of the unit.
     * @param stopped new stopped state
     */
    void setStopped(boolean stopped);
    
    /**
     * Gets the combat target of this unit.
     * @return combat target unit or null
     */
    IUnit getCombatTarget();
    
    /**
     * Sets the combat target for this unit.
     * @param target new combat target
     */
    void setCombatTarget(IUnit target);
    
    /**
     * Gets the current color of the unit.
     * @return current color
     */
    Color getColor();
    
    /**
     * Sets the color of the unit.
     * @param color new color
     */
    void setColor(Color color);
    
    /**
     * Gets the base color of the unit.
     * @return base color
     */
    Color getBaseColor();
    
    /**
     * Checks if unit is highlighted from being hit.
     * @return true if hit highlighted
     */
    boolean isHitHighlighted();
    
    /**
     * Sets the hit highlight state.
     * @param highlighted new highlight state
     */
    void setHitHighlighted(boolean highlighted);
    
    /**
     * Checks if unit is highlighted from firing.
     * @return true if firing highlighted
     */
    boolean isFiringHighlighted();
    
    /**
     * Sets the firing highlight state.
     * @param highlighted new highlight state
     */
    void setFiringHighlighted(boolean highlighted);
    
    /**
     * Gets the color before incapacitation.
     * @return pre-incapacitation color or null
     */
    Color getPreIncapacitationColor();
    
    /**
     * Sets the color before incapacitation.
     * @param color color to restore after incapacitation
     */
    void setPreIncapacitationColor(Color color);
    
    /**
     * Gets the last tick this unit was updated.
     * @return last update tick
     */
    long getLastTickUpdated();
    
    /**
     * Sets the last tick this unit was updated.
     * @param tick update tick
     */
    void setLastTickUpdated(long tick);
    
    // Rotation system
    
    /**
     * Gets the current facing direction in degrees (0-360, North = 0).
     * @return current facing
     */
    double getCurrentFacing();
    
    /**
     * Sets the current facing direction.
     * @param facing new facing in degrees
     */
    void setCurrentFacing(double facing);
    
    /**
     * Gets the target facing direction in degrees.
     * @return target facing
     */
    double getTargetFacing();
    
    /**
     * Sets the target facing direction.
     * @param facing new target facing in degrees
     */
    void setTargetFacing(double facing);
    
    /**
     * Checks if unit is currently rotating.
     * @return true if rotating
     */
    boolean isRotating();
    
    /**
     * Sets the rotating state.
     * @param rotating new rotating state
     */
    void setRotating(boolean rotating);
    
    // Movement and utility methods
    
    /**
     * Checks if the unit is currently moving.
     * @return true if unit has different position and target
     */
    boolean isMoving();
    
    /**
     * Sets the unit's position and target to the same location.
     * @param x new x coordinate
     * @param y new y coordinate
     */
    void setTarget(double x, double y);
    
    /**
     * Gets the current velocity in pixels per tick.
     * @return velocity magnitude
     */
    double getVelocity();
    
    /**
     * Gets the perpendicular velocity relative to another unit.
     * @param shooter the observing unit
     * @return perpendicular velocity component
     */
    double getPerpendicularVelocity(IUnit shooter);
    
    /**
     * Updates the unit's rotation toward target facing.
     * @param rotationSpeed degrees per tick
     * @param rotationThreshold instant rotation threshold
     */
    void updateRotation(double rotationSpeed, double rotationThreshold);
    
    /**
     * Makes the unit face toward a target position.
     * @param targetX target x coordinate
     * @param targetY target y coordinate
     */
    void faceToward(double targetX, double targetY);
}
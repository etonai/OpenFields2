package game.rendering;

import game.interfaces.IUnit;

/**
 * Platform-independent interface for rendering units.
 * Allows different rendering implementations (JavaFX, Console, etc.) to render units.
 */
public interface IUnitRenderer {
    /**
     * Renders a unit with the given selection and debug mode states.
     * 
     * @param unit the unit to render
     * @param isSelected whether the unit is currently selected
     * @param debugMode whether debug information should be displayed
     */
    void renderUnit(IUnit unit, boolean isSelected, boolean debugMode);
    
    /**
     * Renders movement target indicators for a selected unit.
     * 
     * @param unit the unit whose movement target to render
     */
    void renderMovementTarget(IUnit unit);
    
    /**
     * Renders a selection indicator around a unit.
     * 
     * @param unit the unit to highlight as selected
     * @param isMultiSelect whether this is part of a multi-unit selection
     */
    void renderSelectionIndicator(IUnit unit, boolean isMultiSelect);
}
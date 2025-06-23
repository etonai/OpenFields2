package core;

import platform.api.InputProvider;
import game.Unit;
import java.util.List;
import java.util.function.Consumer;

/**
 * Adapts platform input events to game input commands.
 * Bridges between the platform abstraction and game engine.
 */
public class InputAdapter {
    private final GameEngine engine;
    private final Consumer<String> consoleOutput;
    private int selectedUnitId = -1;
    
    public InputAdapter(GameEngine engine, Consumer<String> consoleOutput) {
        this.engine = engine;
        this.consoleOutput = consoleOutput != null ? consoleOutput : s -> {};
        setupInputHandlers();
    }
    
    private void setupInputHandlers() {
        InputProvider input = engine.getPlatform().getInputProvider();
        
        // Register click handler for unit selection and movement
        input.registerClickHandler((x, y, button) -> {
            handleClick(x, y, button);
        });
        
        // Register key handler for keyboard controls
        input.registerKeyHandler(new InputProvider.KeyHandler() {
            @Override
            public void onKeyPressed(InputProvider.Key key) {
                handleKeyPress(key);
            }
            
            @Override
            public void onKeyReleased(InputProvider.Key key) {
                // Not used currently
            }
        });
    }
    
    private void handleClick(double x, double y, InputProvider.MouseButton button) {
        GameState state = engine.getGameState();
        
        if (button == InputProvider.MouseButton.PRIMARY) {
            // Check if clicking on a unit
            Unit clickedUnit = findUnitAt(x, y);
            
            if (clickedUnit != null) {
                // Select unit
                engine.queueInputCommand(new InputCommand(
                    InputCommand.Type.SELECT_UNIT, clickedUnit.getId()
                ));
                selectedUnitId = clickedUnit.getId();
                consoleOutput.accept("Selected: " + clickedUnit.character.getDisplayName());
            } else {
                // Deselect if clicking empty space
                engine.queueInputCommand(new InputCommand(
                    InputCommand.Type.DESELECT_ALL
                ));
                selectedUnitId = -1;
            }
        } else if (button == InputProvider.MouseButton.SECONDARY) {
            // Right click for move or attack
            Unit clickedUnit = findUnitAt(x, y);
            
            if (clickedUnit != null && selectedUnitId >= 0) {
                // Attack target
                engine.queueInputCommand(new InputCommand(
                    InputCommand.Type.ATTACK_TARGET, selectedUnitId, clickedUnit.getId()
                ));
                consoleOutput.accept("Attacking: " + clickedUnit.character.getDisplayName());
            } else if (selectedUnitId >= 0) {
                // Move to location
                engine.queueInputCommand(new InputCommand(
                    InputCommand.Type.MOVE_UNIT, selectedUnitId, x, y
                ));
                consoleOutput.accept("Moving to: " + (int)x + ", " + (int)y);
            }
        }
    }
    
    private void handleKeyPress(InputProvider.Key key) {
        // Number keys for unit selection
        if (key.ordinal() >= InputProvider.Key.DIGIT_1.ordinal() && 
            key.ordinal() <= InputProvider.Key.DIGIT_9.ordinal()) {
            
            int unitNumber = key.ordinal() - InputProvider.Key.DIGIT_0.ordinal();
            selectUnitByNumber(unitNumber);
            return;
        }
        
        switch (key) {
            // Camera controls
            case UP:
                engine.queueInputCommand(new InputCommand(
                    InputCommand.Type.PAN_CAMERA, 0, -10
                ));
                break;
            case DOWN:
                engine.queueInputCommand(new InputCommand(
                    InputCommand.Type.PAN_CAMERA, 0, 10
                ));
                break;
            case LEFT:
                engine.queueInputCommand(new InputCommand(
                    InputCommand.Type.PAN_CAMERA, -10, 0
                ));
                break;
            case RIGHT:
                engine.queueInputCommand(new InputCommand(
                    InputCommand.Type.PAN_CAMERA, 10, 0
                ));
                break;
                
            // Zoom
            case PLUS:
            case EQUALS:
                engine.queueInputCommand(new InputCommand(
                    InputCommand.Type.ZOOM_IN
                ));
                break;
            case MINUS:
                engine.queueInputCommand(new InputCommand(
                    InputCommand.Type.ZOOM_OUT
                ));
                break;
                
            // Game control
            case SPACE:
                engine.queueInputCommand(new InputCommand(
                    InputCommand.Type.PAUSE_RESUME
                ));
                break;
                
            // Movement speed (if unit selected)
            case W:
                if (selectedUnitId >= 0) {
                    engine.queueInputCommand(new InputCommand(
                        InputCommand.Type.SET_MOVEMENT_SPEED, Integer.valueOf(1)
                    ));
                }
                break;
            case S:
                if (selectedUnitId >= 0) {
                    engine.queueInputCommand(new InputCommand(
                        InputCommand.Type.SET_MOVEMENT_SPEED, Integer.valueOf(-1)
                    ));
                }
                break;
                
            // Aiming speed
            case Q:
                if (selectedUnitId >= 0) {
                    engine.queueInputCommand(new InputCommand(
                        InputCommand.Type.SET_AIMING_SPEED, Integer.valueOf(1)
                    ));
                }
                break;
            case E:
                if (selectedUnitId >= 0) {
                    engine.queueInputCommand(new InputCommand(
                        InputCommand.Type.SET_AIMING_SPEED, Integer.valueOf(-1)
                    ));
                }
                break;
                
            // Help
            case H:
                engine.queueInputCommand(new InputCommand(
                    InputCommand.Type.SHOW_HELP
                ));
                break;
                
            // Stats
            case SLASH:
                if (engine.getPlatform().getInputProvider().isKeyPressed(InputProvider.Key.SHIFT)) {
                    engine.queueInputCommand(new InputCommand(
                        InputCommand.Type.SHOW_STATS
                    ));
                }
                break;
        }
    }
    
    private void selectUnitByNumber(int number) {
        List<Unit> units = engine.getGameState().getUnits();
        int index = 0;
        
        for (Unit unit : units) {
            if (!unit.character.isIncapacitated()) {
                index++;
                if (index == number) {
                    engine.queueInputCommand(new InputCommand(
                        InputCommand.Type.SELECT_UNIT, unit.getId()
                    ));
                    selectedUnitId = unit.getId();
                    consoleOutput.accept("Selected: " + unit.character.getDisplayName());
                    return;
                }
            }
        }
    }
    
    private Unit findUnitAt(double x, double y) {
        List<Unit> units = engine.getGameState().getUnitsInRange(x, y, 20);
        return units.isEmpty() ? null : units.get(0);
    }
}
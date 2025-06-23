package core;

/**
 * High-level semantic input commands that are platform-independent.
 * These represent game actions rather than raw input events.
 */
public class InputCommand {
    private final Type type;
    private final int unitId;
    private final int targetId;
    private final double x;
    private final double y;
    private final Object data;
    
    /**
     * Command types representing different game actions.
     */
    public enum Type {
        // Unit selection
        SELECT_UNIT,
        DESELECT_ALL,
        SELECT_NEXT_UNIT,
        SELECT_PREVIOUS_UNIT,
        
        // Movement
        MOVE_UNIT,
        STOP_UNIT,
        SET_MOVEMENT_SPEED,
        
        // Combat
        ATTACK_TARGET,
        STOP_ATTACK,
        SET_AIMING_SPEED,
        TOGGLE_WEAPON_MODE,
        
        // Camera
        PAN_CAMERA,
        ZOOM_IN,
        ZOOM_OUT,
        CENTER_ON_UNIT,
        
        // Game control
        PAUSE_RESUME,
        SAVE_GAME,
        LOAD_GAME,
        QUIT_GAME,
        
        // UI
        SHOW_HELP,
        SHOW_STATS,
        TOGGLE_DEBUG_INFO,
        
        // Console specific
        CONSOLE_COMMAND
    }
    
    /**
     * Creates a simple command with no parameters.
     */
    public InputCommand(Type type) {
        this(type, -1, -1, 0, 0, null);
    }
    
    /**
     * Creates a command targeting a specific unit.
     */
    public InputCommand(Type type, int unitId) {
        this(type, unitId, -1, 0, 0, null);
    }
    
    /**
     * Creates a command with unit and target.
     */
    public InputCommand(Type type, int unitId, int targetId) {
        this(type, unitId, targetId, 0, 0, null);
    }
    
    /**
     * Creates a command with coordinates.
     */
    public InputCommand(Type type, double x, double y) {
        this(type, -1, -1, x, y, null);
    }
    
    /**
     * Creates a command with unit and coordinates.
     */
    public InputCommand(Type type, int unitId, double x, double y) {
        this(type, unitId, -1, x, y, null);
    }
    
    /**
     * Creates a command with custom data.
     */
    public InputCommand(Type type, Object data) {
        this(type, -1, -1, 0, 0, data);
    }
    
    /**
     * Full constructor with all parameters.
     */
    public InputCommand(Type type, int unitId, int targetId, double x, double y, Object data) {
        this.type = type;
        this.unitId = unitId;
        this.targetId = targetId;
        this.x = x;
        this.y = y;
        this.data = data;
    }
    
    // Getters
    public Type getType() { return type; }
    public int getUnitId() { return unitId; }
    public int getTargetId() { return targetId; }
    public double getX() { return x; }
    public double getY() { return y; }
    public Object getData() { return data; }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("InputCommand{type=").append(type);
        if (unitId >= 0) sb.append(", unitId=").append(unitId);
        if (targetId >= 0) sb.append(", targetId=").append(targetId);
        if (x != 0 || y != 0) sb.append(", x=").append(x).append(", y=").append(y);
        if (data != null) sb.append(", data=").append(data);
        sb.append("}");
        return sb.toString();
    }
}
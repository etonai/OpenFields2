package core;

import platform.api.Color;

/**
 * High-level rendering commands that are platform-independent.
 * These describe what to render without specifying how.
 */
public class RenderCommand {
    private final Type type;
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final Color color;
    private final String text;
    private final Object data;
    
    /**
     * Command types for different rendering operations.
     */
    public enum Type {
        // High-level game objects
        DRAW_UNIT,
        DRAW_SELECTION_INDICATOR,
        DRAW_HEALTH_BAR,
        DRAW_WEAPON_STATE,
        DRAW_MOVEMENT_PATH,
        DRAW_ATTACK_LINE,
        DRAW_PROJECTILE,
        DRAW_MUZZLE_FLASH,
        DRAW_IMPACT_EFFECT,
        
        // UI elements
        DRAW_UI_PANEL,
        DRAW_UI_TEXT,
        DRAW_UI_BUTTON,
        DRAW_MINIMAP,
        
        // Primitives (for flexibility)
        DRAW_LINE,
        DRAW_RECT,
        DRAW_FILLED_RECT,
        DRAW_CIRCLE,
        DRAW_FILLED_CIRCLE,
        DRAW_TEXT,
        
        // Special
        SET_TRANSFORM,
        PUSH_TRANSFORM,
        POP_TRANSFORM,
        CLEAR_SCREEN
    }
    
    /**
     * Unit rendering data.
     */
    public static class UnitData {
        public final int unitId;
        public final String name;
        public final double radius;
        public final boolean isSelected;
        public final boolean isIncapacitated;
        public final double facingAngle;
        
        public UnitData(int unitId, String name, double radius, 
                       boolean isSelected, boolean isIncapacitated, double facingAngle) {
            this.unitId = unitId;
            this.name = name;
            this.radius = radius;
            this.isSelected = isSelected;
            this.isIncapacitated = isIncapacitated;
            this.facingAngle = facingAngle;
        }
    }
    
    /**
     * Health bar rendering data.
     */
    public static class HealthBarData {
        public final double currentHealth;
        public final double maxHealth;
        public final boolean showNumbers;
        
        public HealthBarData(double currentHealth, double maxHealth, boolean showNumbers) {
            this.currentHealth = currentHealth;
            this.maxHealth = maxHealth;
            this.showNumbers = showNumbers;
        }
    }
    
    /**
     * Transform data for camera operations.
     */
    public static class TransformData {
        public final double offsetX;
        public final double offsetY;
        public final double zoom;
        
        public TransformData(double offsetX, double offsetY, double zoom) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.zoom = zoom;
        }
    }
    
    // Factory methods for common commands
    
    public static RenderCommand clearScreen(Color backgroundColor) {
        return new RenderCommand(Type.CLEAR_SCREEN, 0, 0, 0, 0, backgroundColor, null, null);
    }
    
    public static RenderCommand drawUnit(double x, double y, Color color, UnitData unitData) {
        return new RenderCommand(Type.DRAW_UNIT, x, y, 0, 0, color, null, unitData);
    }
    
    public static RenderCommand drawHealthBar(double x, double y, double width, double height, 
                                            Color color, HealthBarData healthData) {
        return new RenderCommand(Type.DRAW_HEALTH_BAR, x, y, width, height, color, null, healthData);
    }
    
    public static RenderCommand drawLine(double x1, double y1, double x2, double y2, Color color, double width) {
        return new RenderCommand(Type.DRAW_LINE, x1, y1, x2 - x1, y2 - y1, color, null, width);
    }
    
    public static RenderCommand drawText(String text, double x, double y, Color color) {
        return new RenderCommand(Type.DRAW_TEXT, x, y, 0, 0, color, text, null);
    }
    
    public static RenderCommand setTransform(TransformData transform) {
        return new RenderCommand(Type.SET_TRANSFORM, 0, 0, 0, 0, null, null, transform);
    }
    
    // Constructor
    private RenderCommand(Type type, double x, double y, double width, double height,
                         Color color, String text, Object data) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.text = text;
        this.data = data;
    }
    
    // Getters
    public Type getType() { return type; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public Color getColor() { return color; }
    public String getText() { return text; }
    public Object getData() { return data; }
}
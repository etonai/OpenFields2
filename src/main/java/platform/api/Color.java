package platform.api;

/**
 * Platform-independent color representation.
 * Uses normalized RGBA values (0.0 to 1.0) for consistency across platforms.
 */
public class Color {
    public final float r;
    public final float g;
    public final float b;
    public final float a;
    
    // Common color constants
    public static final Color BLACK = new Color(0, 0, 0, 1);
    public static final Color WHITE = new Color(1, 1, 1, 1);
    public static final Color RED = new Color(1, 0, 0, 1);
    public static final Color GREEN = new Color(0, 1, 0, 1);
    public static final Color BLUE = new Color(0, 0, 1, 1);
    public static final Color YELLOW = new Color(1, 1, 0, 1);
    public static final Color CYAN = new Color(0, 1, 1, 1);
    public static final Color MAGENTA = new Color(1, 0, 1, 1);
    public static final Color GRAY = new Color(0.5f, 0.5f, 0.5f, 1);
    public static final Color DARK_GRAY = new Color(0.25f, 0.25f, 0.25f, 1);
    public static final Color LIGHT_GRAY = new Color(0.75f, 0.75f, 0.75f, 1);
    public static final Color ORANGE = new Color(1, 0.65f, 0, 1);
    public static final Color PURPLE = new Color(0.5f, 0, 0.5f, 1);
    public static final Color BROWN = new Color(0.65f, 0.16f, 0.16f, 1);
    
    /**
     * Creates a color with the specified RGBA values.
     * @param r red component (0.0 to 1.0)
     * @param g green component (0.0 to 1.0)
     * @param b blue component (0.0 to 1.0)
     * @param a alpha component (0.0 to 1.0)
     */
    public Color(float r, float g, float b, float a) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
        this.a = clamp(a);
    }
    
    /**
     * Creates an opaque color with the specified RGB values.
     * @param r red component (0.0 to 1.0)
     * @param g green component (0.0 to 1.0)
     * @param b blue component (0.0 to 1.0)
     */
    public Color(float r, float g, float b) {
        this(r, g, b, 1.0f);
    }
    
    /**
     * Creates a color from integer RGB values (0-255).
     * @param r red component (0-255)
     * @param g green component (0-255)
     * @param b blue component (0-255)
     * @param a alpha component (0-255)
     */
    public static Color fromRGB(int r, int g, int b, int a) {
        return new Color(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f);
    }
    
    /**
     * Creates an opaque color from integer RGB values (0-255).
     * @param r red component (0-255)
     * @param g green component (0-255)
     * @param b blue component (0-255)
     */
    public static Color fromRGB(int r, int g, int b) {
        return fromRGB(r, g, b, 255);
    }
    
    /**
     * Converts from JavaFX Color.
     * @param javafxColor the JavaFX color to convert
     * @return platform-independent Color
     */
    public static Color fromJavaFX(javafx.scene.paint.Color javafxColor) {
        return new Color(
            (float) javafxColor.getRed(),
            (float) javafxColor.getGreen(),
            (float) javafxColor.getBlue(),
            (float) javafxColor.getOpacity()
        );
    }
    
    /**
     * Converts to JavaFX Color.
     * @return JavaFX Color representation
     */
    public javafx.scene.paint.Color toJavaFX() {
        return new javafx.scene.paint.Color(r, g, b, a);
    }
    
    /**
     * Gets the red component as an integer (0-255).
     * @return red component
     */
    public int getRed() {
        return Math.round(r * 255);
    }
    
    /**
     * Gets the green component as an integer (0-255).
     * @return green component
     */
    public int getGreen() {
        return Math.round(g * 255);
    }
    
    /**
     * Gets the blue component as an integer (0-255).
     * @return blue component
     */
    public int getBlue() {
        return Math.round(b * 255);
    }
    
    /**
     * Gets the alpha component as an integer (0-255).
     * @return alpha component
     */
    public int getAlpha() {
        return Math.round(a * 255);
    }
    
    /**
     * Creates a new color with the specified alpha value.
     * @param alpha new alpha value (0.0 to 1.0)
     * @return new Color with modified alpha
     */
    public Color withAlpha(float alpha) {
        return new Color(r, g, b, alpha);
    }
    
    /**
     * Creates a darker version of this color.
     * @param factor darkening factor (0.0 to 1.0)
     * @return darker color
     */
    public Color darker(float factor) {
        factor = clamp(factor);
        return new Color(r * factor, g * factor, b * factor, a);
    }
    
    /**
     * Creates a brighter version of this color.
     * @param factor brightening factor (> 1.0)
     * @return brighter color
     */
    public Color brighter(float factor) {
        return new Color(r * factor, g * factor, b * factor, a);
    }
    
    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Color color = (Color) obj;
        return Float.compare(color.r, r) == 0 &&
               Float.compare(color.g, g) == 0 &&
               Float.compare(color.b, b) == 0 &&
               Float.compare(color.a, a) == 0;
    }
    
    @Override
    public int hashCode() {
        int result = Float.floatToIntBits(r);
        result = 31 * result + Float.floatToIntBits(g);
        result = 31 * result + Float.floatToIntBits(b);
        result = 31 * result + Float.floatToIntBits(a);
        return result;
    }
    
    @Override
    public String toString() {
        return String.format("Color[r=%.2f, g=%.2f, b=%.2f, a=%.2f]", r, g, b, a);
    }
}
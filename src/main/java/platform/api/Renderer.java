package platform.api;

/**
 * Platform-independent rendering interface.
 * Provides high-level drawing operations that can be implemented by different rendering backends.
 */
public interface Renderer {
    /**
     * Clears the entire rendering surface.
     */
    void clear();
    
    /**
     * Sets the current drawing color.
     * @param color the color to use for subsequent drawing operations
     */
    void setColor(Color color);
    
    /**
     * Draws a unit at the specified position.
     * @param x x-coordinate
     * @param y y-coordinate
     * @param color unit color
     * @param label unit label (e.g., name or ID)
     * @param radius unit radius in pixels
     */
    void drawUnit(double x, double y, Color color, String label, double radius);
    
    /**
     * Draws a line between two points.
     * @param x1 start x-coordinate
     * @param y1 start y-coordinate
     * @param x2 end x-coordinate
     * @param y2 end y-coordinate
     * @param color line color
     * @param width line width in pixels
     */
    void drawLine(double x1, double y1, double x2, double y2, Color color, double width);
    
    /**
     * Draws text at the specified position.
     * @param text the text to draw
     * @param x x-coordinate
     * @param y y-coordinate
     * @param color text color
     */
    void drawText(String text, double x, double y, Color color);
    
    /**
     * Draws a health bar.
     * @param x x-coordinate of the bar's top-left
     * @param y y-coordinate of the bar's top-left
     * @param width total width of the bar
     * @param height height of the bar
     * @param healthPercentage health percentage (0.0 to 1.0)
     * @param borderColor color of the bar border
     * @param fillColor color of the health portion
     * @param backgroundColor color of the empty portion
     */
    void drawHealthBar(double x, double y, double width, double height, 
                      double healthPercentage, Color borderColor, 
                      Color fillColor, Color backgroundColor);
    
    /**
     * Draws a filled rectangle.
     * @param x x-coordinate of top-left corner
     * @param y y-coordinate of top-left corner
     * @param width rectangle width
     * @param height rectangle height
     * @param color fill color
     */
    void fillRect(double x, double y, double width, double height, Color color);
    
    /**
     * Draws a rectangle outline.
     * @param x x-coordinate of top-left corner
     * @param y y-coordinate of top-left corner
     * @param width rectangle width
     * @param height rectangle height
     * @param color outline color
     * @param lineWidth width of the outline
     */
    void drawRect(double x, double y, double width, double height, Color color, double lineWidth);
    
    /**
     * Draws a filled circle.
     * @param centerX x-coordinate of center
     * @param centerY y-coordinate of center
     * @param radius circle radius
     * @param color fill color
     */
    void fillCircle(double centerX, double centerY, double radius, Color color);
    
    /**
     * Draws a circle outline.
     * @param centerX x-coordinate of center
     * @param centerY y-coordinate of center
     * @param radius circle radius
     * @param color outline color
     * @param lineWidth width of the outline
     */
    void drawCircle(double centerX, double centerY, double radius, Color color, double lineWidth);
    
    /**
     * Sets the viewport/camera transform.
     * @param offsetX x offset in pixels
     * @param offsetY y offset in pixels
     * @param zoom zoom factor (1.0 = normal, 2.0 = 2x zoom)
     */
    void setTransform(double offsetX, double offsetY, double zoom);
    
    /**
     * Saves the current transform state.
     */
    void pushTransform();
    
    /**
     * Restores the previously saved transform state.
     */
    void popTransform();
    
    /**
     * Gets the width of the rendering surface.
     * @return width in pixels
     */
    double getWidth();
    
    /**
     * Gets the height of the rendering surface.
     * @return height in pixels
     */
    double getHeight();
    
    /**
     * Presents the rendered frame to the display.
     * Some platforms may buffer rendering commands until this is called.
     */
    void present();
    
    /**
     * Begins a new frame.
     * Called before any drawing operations for a frame.
     */
    void beginFrame();
    
    /**
     * Ends the current frame.
     * Called after all drawing operations for a frame.
     */
    void endFrame();
}
package platform.impl.javafx;

import platform.api.Renderer;
import platform.api.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;
import java.util.Stack;

/**
 * JavaFX implementation of the Renderer interface.
 * Adapts existing JavaFX rendering code to the platform abstraction.
 */
public class JavaFXRenderer implements Renderer {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Stack<Affine> transformStack;
    private Color currentColor;
    
    public JavaFXRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.transformStack = new Stack<>();
        this.currentColor = Color.BLACK;
    }
    
    @Override
    public void clear() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        if (currentColor != null) {
            gc.setFill(currentColor.toJavaFX());
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }
    
    @Override
    public void setColor(Color color) {
        this.currentColor = color;
        if (color != null) {
            javafx.scene.paint.Color fxColor = color.toJavaFX();
            gc.setFill(fxColor);
            gc.setStroke(fxColor);
        }
    }
    
    @Override
    public void drawUnit(double x, double y, Color color, String label, double radius) {
        javafx.scene.paint.Color fxColor = color.toJavaFX();
        
        // Draw unit circle
        gc.setFill(fxColor);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        
        // Draw unit label
        if (label != null) {
            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.fillText(label, x - 15, y - radius - 5);
        }
    }
    
    @Override
    public void drawLine(double x1, double y1, double x2, double y2, Color color, double width) {
        gc.setStroke(color.toJavaFX());
        gc.setLineWidth(width);
        gc.strokeLine(x1, y1, x2, y2);
    }
    
    @Override
    public void drawText(String text, double x, double y, Color color) {
        gc.setFill(color.toJavaFX());
        gc.fillText(text, x, y);
    }
    
    @Override
    public void drawHealthBar(double x, double y, double width, double height,
                            double healthPercentage, Color borderColor,
                            Color fillColor, Color backgroundColor) {
        // Background
        gc.setFill(backgroundColor.toJavaFX());
        gc.fillRect(x, y, width, height);
        
        // Health fill
        double fillWidth = width * Math.max(0, Math.min(1, healthPercentage));
        gc.setFill(fillColor.toJavaFX());
        gc.fillRect(x, y, fillWidth, height);
        
        // Border
        gc.setStroke(borderColor.toJavaFX());
        gc.setLineWidth(1);
        gc.strokeRect(x, y, width, height);
    }
    
    @Override
    public void fillRect(double x, double y, double width, double height, Color color) {
        gc.setFill(color.toJavaFX());
        gc.fillRect(x, y, width, height);
    }
    
    @Override
    public void drawRect(double x, double y, double width, double height, Color color, double lineWidth) {
        gc.setStroke(color.toJavaFX());
        gc.setLineWidth(lineWidth);
        gc.strokeRect(x, y, width, height);
    }
    
    @Override
    public void fillCircle(double centerX, double centerY, double radius, Color color) {
        gc.setFill(color.toJavaFX());
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }
    
    @Override
    public void drawCircle(double centerX, double centerY, double radius, Color color, double lineWidth) {
        gc.setStroke(color.toJavaFX());
        gc.setLineWidth(lineWidth);
        gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }
    
    @Override
    public void setTransform(double offsetX, double offsetY, double zoom) {
        gc.setTransform(new Affine());
        gc.translate(offsetX, offsetY);
        gc.scale(zoom, zoom);
    }
    
    @Override
    public void pushTransform() {
        transformStack.push(gc.getTransform());
    }
    
    @Override
    public void popTransform() {
        if (!transformStack.isEmpty()) {
            gc.setTransform(transformStack.pop());
        }
    }
    
    @Override
    public double getWidth() {
        return canvas.getWidth();
    }
    
    @Override
    public double getHeight() {
        return canvas.getHeight();
    }
    
    @Override
    public void present() {
        // JavaFX handles presentation automatically
    }
    
    @Override
    public void beginFrame() {
        // Save current state if needed
    }
    
    @Override
    public void endFrame() {
        // Restore state if needed
    }
}
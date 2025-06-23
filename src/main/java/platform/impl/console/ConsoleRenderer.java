package platform.impl.console;

import platform.api.Renderer;
import platform.api.Color;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * Console/terminal implementation of the Renderer interface.
 * Renders game state as ASCII art to the console.
 */
public class ConsoleRenderer implements Renderer {
    private static final int DEFAULT_WIDTH = 80;
    private static final int DEFAULT_HEIGHT = 24;
    private static final int GAME_AREA_HEIGHT = 19; // Reserve lines for UI
    
    private final PrintStream out;
    private final char[][] buffer;
    private final Color[][] colorBuffer;
    private final int width;
    private final int height;
    
    // Camera transform
    private double offsetX = 0;
    private double offsetY = 0;
    private double zoom = 1.0;
    
    // ANSI color codes
    private static final String RESET = "\u001B[0m";
    private static final String BLACK = "\u001B[30m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";
    private static final String BRIGHT_BLACK = "\u001B[90m";
    private static final String BRIGHT_RED = "\u001B[91m";
    private static final String BRIGHT_GREEN = "\u001B[92m";
    private static final String BRIGHT_YELLOW = "\u001B[93m";
    private static final String BRIGHT_BLUE = "\u001B[94m";
    private static final String BRIGHT_MAGENTA = "\u001B[95m";
    private static final String BRIGHT_CYAN = "\u001B[96m";
    private static final String BRIGHT_WHITE = "\u001B[97m";
    
    private final boolean ansiSupported;
    
    public ConsoleRenderer() {
        this(System.out, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    
    public ConsoleRenderer(PrintStream out, int width, int height) {
        this.out = out;
        this.width = width;
        this.height = height;
        this.buffer = new char[height][width];
        this.colorBuffer = new Color[height][width];
        this.ansiSupported = checkAnsiSupport();
        clear();
    }
    
    private boolean checkAnsiSupport() {
        // Simple check - could be more sophisticated
        String os = System.getProperty("os.name").toLowerCase();
        return !os.contains("win") || System.getenv("ANSICON") != null;
    }
    
    @Override
    public void clear() {
        for (int y = 0; y < height; y++) {
            Arrays.fill(buffer[y], ' ');
            Arrays.fill(colorBuffer[y], null);
        }
    }
    
    @Override
    public void setColor(Color color) {
        // Current color is set per-operation in console mode
    }
    
    @Override
    public void drawUnit(double x, double y, Color color, String label, double radius) {
        // Convert world coordinates to screen coordinates
        int screenX = worldToScreenX(x);
        int screenY = worldToScreenY(y);
        
        if (screenX >= 0 && screenX < width && screenY >= 0 && screenY < GAME_AREA_HEIGHT) {
            // Draw unit as numbered box [1], [2], etc.
            if (label != null && label.length() > 0) {
                String unitStr = "[" + (Character.isDigit(label.charAt(0)) ? label.charAt(0) : 'U') + "]";
                for (int i = 0; i < unitStr.length() && screenX + i < width; i++) {
                    buffer[screenY][screenX + i] = unitStr.charAt(i);
                    colorBuffer[screenY][screenX + i] = color;
                }
            }
        }
    }
    
    @Override
    public void drawLine(double x1, double y1, double x2, double y2, Color color, double width) {
        // Simple line drawing using Bresenham's algorithm
        int sx1 = worldToScreenX(x1);
        int sy1 = worldToScreenY(y1);
        int sx2 = worldToScreenX(x2);
        int sy2 = worldToScreenY(y2);
        
        int dx = Math.abs(sx2 - sx1);
        int dy = Math.abs(sy2 - sy1);
        int sx = sx1 < sx2 ? 1 : -1;
        int sy = sy1 < sy2 ? 1 : -1;
        int err = dx - dy;
        
        while (true) {
            if (sx1 >= 0 && sx1 < width && sy1 >= 0 && sy1 < GAME_AREA_HEIGHT) {
                buffer[sy1][sx1] = width > 1 ? '=' : '-';
                colorBuffer[sy1][sx1] = color;
            }
            
            if (sx1 == sx2 && sy1 == sy2) break;
            
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                sx1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                sy1 += sy;
            }
        }
    }
    
    @Override
    public void drawText(String text, double x, double y, Color color) {
        int screenX = (int) x; // UI text uses screen coordinates directly
        int screenY = (int) y;
        
        if (text != null && screenY >= 0 && screenY < height) {
            for (int i = 0; i < text.length() && screenX + i < width; i++) {
                if (screenX + i >= 0) {
                    buffer[screenY][screenX + i] = text.charAt(i);
                    colorBuffer[screenY][screenX + i] = color;
                }
            }
        }
    }
    
    @Override
    public void drawHealthBar(double x, double y, double width, double height,
                            double healthPercentage, Color borderColor,
                            Color fillColor, Color backgroundColor) {
        int screenX = worldToScreenX(x);
        int screenY = worldToScreenY(y) - 1; // Above unit
        
        if (screenY >= 0 && screenY < GAME_AREA_HEIGHT && screenX >= 0) {
            // Simple health bar: [===--]
            int barWidth = 6;
            if (screenX + barWidth < width) {
                buffer[screenY][screenX] = '[';
                colorBuffer[screenY][screenX] = borderColor;
                
                int filled = (int) (4 * healthPercentage);
                for (int i = 1; i <= 4; i++) {
                    if (screenX + i < width) {
                        buffer[screenY][screenX + i] = i <= filled ? '=' : '-';
                        colorBuffer[screenY][screenX + i] = i <= filled ? fillColor : backgroundColor;
                    }
                }
                
                if (screenX + 5 < width) {
                    buffer[screenY][screenX + 5] = ']';
                    colorBuffer[screenY][screenX + 5] = borderColor;
                }
            }
        }
    }
    
    @Override
    public void fillRect(double x, double y, double width, double height, Color color) {
        int sx = worldToScreenX(x);
        int sy = worldToScreenY(y);
        int sw = (int) (width / (7.0 * zoom)); // Convert pixels to characters
        int sh = (int) (height / (7.0 * zoom));
        
        for (int dy = 0; dy < sh; dy++) {
            for (int dx = 0; dx < sw; dx++) {
                int px = sx + dx;
                int py = sy + dy;
                if (px >= 0 && px < this.width && py >= 0 && py < GAME_AREA_HEIGHT) {
                    buffer[py][px] = '#';
                    colorBuffer[py][px] = color;
                }
            }
        }
    }
    
    @Override
    public void drawRect(double x, double y, double width, double height, Color color, double lineWidth) {
        // Draw rectangle outline
        int sx = worldToScreenX(x);
        int sy = worldToScreenY(y);
        int sw = (int) (width / (7.0 * zoom));
        int sh = (int) (height / (7.0 * zoom));
        
        // Top and bottom
        for (int dx = 0; dx < sw; dx++) {
            setPixel(sx + dx, sy, '-', color);
            setPixel(sx + dx, sy + sh - 1, '-', color);
        }
        
        // Left and right
        for (int dy = 0; dy < sh; dy++) {
            setPixel(sx, sy + dy, '|', color);
            setPixel(sx + sw - 1, sy + dy, '|', color);
        }
        
        // Corners
        setPixel(sx, sy, '+', color);
        setPixel(sx + sw - 1, sy, '+', color);
        setPixel(sx, sy + sh - 1, '+', color);
        setPixel(sx + sw - 1, sy + sh - 1, '+', color);
    }
    
    @Override
    public void fillCircle(double centerX, double centerY, double radius, Color color) {
        // Approximate circle with characters
        int cx = worldToScreenX(centerX);
        int cy = worldToScreenY(centerY);
        int r = (int) (radius / (7.0 * zoom));
        
        if (r == 0) {
            setPixel(cx, cy, 'o', color);
        } else {
            setPixel(cx, cy, 'O', color);
        }
    }
    
    @Override
    public void drawCircle(double centerX, double centerY, double radius, Color color, double lineWidth) {
        // Same as fill for console
        fillCircle(centerX, centerY, radius, color);
    }
    
    @Override
    public void setTransform(double offsetX, double offsetY, double zoom) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.zoom = zoom;
    }
    
    @Override
    public void pushTransform() {
        // Not implemented for console
    }
    
    @Override
    public void popTransform() {
        // Not implemented for console
    }
    
    @Override
    public double getWidth() {
        return width * 7.0; // Approximate pixel width
    }
    
    @Override
    public double getHeight() {
        return height * 7.0; // Approximate pixel height
    }
    
    @Override
    public void present() {
        // Clear screen (ANSI escape code)
        if (ansiSupported) {
            out.print("\033[H\033[2J");
        } else {
            // Poor man's clear screen
            for (int i = 0; i < 50; i++) out.println();
        }
        
        // Draw buffer to console
        for (int y = 0; y < height; y++) {
            StringBuilder line = new StringBuilder();
            Color lastColor = null;
            
            for (int x = 0; x < width; x++) {
                Color currentColor = colorBuffer[y][x];
                
                if (ansiSupported && currentColor != lastColor) {
                    if (currentColor != null) {
                        line.append(getAnsiColor(currentColor));
                    } else {
                        line.append(RESET);
                    }
                    lastColor = currentColor;
                }
                
                line.append(buffer[y][x]);
            }
            
            if (ansiSupported) {
                line.append(RESET);
            }
            
            out.println(line.toString());
        }
        
        out.flush();
    }
    
    @Override
    public void beginFrame() {
        // Draw UI borders
        drawUIBorders();
    }
    
    @Override
    public void endFrame() {
        // Nothing special needed
    }
    
    private void drawUIBorders() {
        // Draw separator line between game area and status area
        for (int x = 0; x < width; x++) {
            buffer[GAME_AREA_HEIGHT][x] = '=';
            colorBuffer[GAME_AREA_HEIGHT][x] = Color.GRAY;
        }
    }
    
    private int worldToScreenX(double worldX) {
        return (int) ((worldX + offsetX) / (7.0 * zoom));
    }
    
    private int worldToScreenY(double worldY) {
        return (int) ((worldY + offsetY) / (7.0 * zoom));
    }
    
    private void setPixel(int x, int y, char c, Color color) {
        if (x >= 0 && x < width && y >= 0 && y < GAME_AREA_HEIGHT) {
            buffer[y][x] = c;
            colorBuffer[y][x] = color;
        }
    }
    
    private String getAnsiColor(Color color) {
        // Map platform colors to ANSI colors
        if (color.equals(Color.BLACK)) return BLACK;
        if (color.equals(Color.RED)) return BRIGHT_RED;
        if (color.equals(Color.GREEN)) return BRIGHT_GREEN;
        if (color.equals(Color.BLUE)) return BRIGHT_BLUE;
        if (color.equals(Color.YELLOW)) return BRIGHT_YELLOW;
        if (color.equals(Color.CYAN)) return BRIGHT_CYAN;
        if (color.equals(Color.MAGENTA)) return BRIGHT_MAGENTA;
        if (color.equals(Color.WHITE)) return BRIGHT_WHITE;
        if (color.equals(Color.GRAY)) return WHITE;
        if (color.equals(Color.DARK_GRAY)) return BRIGHT_BLACK;
        
        // Default based on brightness
        float brightness = (color.r + color.g + color.b) / 3.0f;
        if (brightness > 0.8f) return BRIGHT_WHITE;
        if (brightness > 0.6f) return WHITE;
        if (brightness > 0.3f) return BRIGHT_BLACK;
        return BLACK;
    }
}
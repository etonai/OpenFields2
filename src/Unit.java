import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Unit {
    public final int id;
    public Character character;
    public double x, y;
    public double targetX, targetY;
    public boolean hasTarget = false;
    public Color color;
    public final Color baseColor;
    public boolean isHitHighlighted = false;
    private long lastTickUpdated = -1;

    public Unit(Character character, double x, double y, Color color, int id) {
        this.id = id;
        this.character = character;
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.color = color != null ? color : Color.GRAY;
        this.baseColor = this.color;
    }

    public void setTarget(double x, double y) {
        this.targetX = x;
        this.targetY = y;
        this.hasTarget = true;
    }

    public void update(long currentTick) {
        if (currentTick == lastTickUpdated) return;
        lastTickUpdated = currentTick;

        if (!hasTarget) return;

        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 1) {
            hasTarget = false;
            return;
        }

        double speed = character.movementSpeed / 60.0;
        double moveX = speed * (dx / distance);
        double moveY = speed * (dy / distance);

        if (Math.abs(moveX) > Math.abs(dx)) x = targetX; else x += moveX;
        if (Math.abs(moveY) > Math.abs(dy)) y = targetY; else y += moveY;
    }

    public void render(GraphicsContext gc, boolean isSelected) {
        gc.setFill(color != null ? color : Color.GRAY);
        gc.fillOval(x - 10.5, y - 10.5, 21, 21);

        if (isSelected) {
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(2);
            gc.strokeOval(x - 12, y - 12, 24, 24);
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font(12));
            gc.fillText(character.name, x - 15, y - 15);
        }
    }

    public boolean contains(double px, double py) {
        return Math.hypot(px - x, py - y) <= 10.5;
    }
}

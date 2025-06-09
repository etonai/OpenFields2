package game;

import combat.Character;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Unit {
    public final int id;
    public Character character;
    public double x, y;
    public double targetX, targetY;
    public boolean hasTarget = false;
    public boolean isStopped = false;
    public Color color;
    public final Color baseColor;
    public boolean isHitHighlighted = false;
    private Color preIncapacitationColor = null;
    long lastTickUpdated = -1;

    public Unit(Character character, double x, double y, Color color, int id) {
        this.id = id;
        this.character = character;
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.color = color;
        this.baseColor = color;
    }

    public int getId() {
        return id;
    }

    public Character getCharacter() {
        return character;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
    public void setTarget(double x, double y) {
        this.targetX = x;
        this.targetY = y;
        this.hasTarget = true;
        this.isStopped = false;
    }

    public void stopMovement() {
        this.isStopped = true;
    }

    public void resumeMovement() {
        if (isStopped && hasTarget) {
            this.isStopped = false;
        }
    }

    public void update(long currentTick) {
        if (currentTick == lastTickUpdated) return;
        lastTickUpdated = currentTick;

        // Update color based on incapacitation status
        if (character.isIncapacitated()) {
            // Save current color before changing to gray (only if not already gray)
            if (color != Color.DARKGRAY) {
                preIncapacitationColor = color;
            }
            color = Color.DARKGRAY;
        } else {
            // Character is not incapacitated
            if (color == Color.DARKGRAY) {
                // Was incapacitated, now restore appropriate color
                if (preIncapacitationColor != null) {
                    color = preIncapacitationColor;
                    preIncapacitationColor = null;
                } else {
                    color = baseColor;
                }
            } else if (!isHitHighlighted && color != baseColor) {
                // Normal color restoration for non-incapacitated cases
                color = baseColor;
            }
        }

        if (!hasTarget) return;
        
        // Stopped characters don't move but keep their target
        if (isStopped) return;
        
        // Incapacitated characters cannot move
        if (character.isIncapacitated()) {
            hasTarget = false;
            return;
        }

        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= 1) {
            x = targetX;
            y = targetY;
            hasTarget = false;
            return;
        }

        double moveX = character.getEffectiveMovementSpeed() / 60.0 * (dx / distance);
        double moveY = character.getEffectiveMovementSpeed() / 60.0 * (dy / distance);

        if (Math.abs(moveX) > Math.abs(dx)) x = targetX; else x += moveX;
        if (Math.abs(moveY) > Math.abs(dy)) y = targetY; else y += moveY;
    }

    public void render(GraphicsContext gc, boolean isSelected) {
        gc.setFill(color);
        gc.fillOval(x - 10.5, y - 10.5, 21, 21);
        if (isSelected) {
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(2);
            gc.strokeOval(x - 12, y - 12, 24, 24);
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font(12));
            gc.fillText(character.name, x - 15, y - 15);
            // Display movement type
            gc.setFont(Font.font(10));
            gc.fillText(character.getCurrentMovementType().getDisplayName(), x - 15, y + 25);
            // Display aiming speed
            gc.fillText(character.getCurrentAimingSpeed().getDisplayName(), x - 15, y + 35);
        }
    }

    public boolean contains(double px, double py) {
        return Math.hypot(px - x, py - y) <= 10.5;
    }
    
    public boolean isMoving() {
        return hasTarget && !isStopped && !character.isIncapacitated();
    }
    
    /**
     * Gets the current velocity vector of this unit
     * @return double array [vx, vy] representing velocity in pixels per tick
     */
    public double[] getVelocityVector() {
        if (!isMoving()) {
            return new double[]{0.0, 0.0};
        }
        
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance <= 1) {
            return new double[]{0.0, 0.0};
        }
        
        double vx = character.getEffectiveMovementSpeed() / 60.0 * (dx / distance);
        double vy = character.getEffectiveMovementSpeed() / 60.0 * (dy / distance);
        
        return new double[]{vx, vy};
    }
    
    /**
     * Calculates the perpendicular velocity component relative to the line of sight to another unit
     * @param other The other unit (typically the shooter)
     * @return The magnitude of velocity perpendicular to the line of sight, in pixels per tick
     */
    public double getPerpendicularVelocity(Unit other) {
        if (!isMoving()) {
            return 0.0;
        }
        
        // Vector from other unit to this unit (line of sight)
        double losX = x - other.x;
        double losY = y - other.y;
        double losDistance = Math.sqrt(losX * losX + losY * losY);
        
        if (losDistance == 0) {
            return 0.0; // Same position, no meaningful line of sight
        }
        
        // Normalize line of sight vector
        double losUnitX = losX / losDistance;
        double losUnitY = losY / losDistance;
        
        // Get velocity vector
        double[] velocity = getVelocityVector();
        double vx = velocity[0];
        double vy = velocity[1];
        
        // Calculate component of velocity perpendicular to line of sight
        // This is done by subtracting the parallel component from the total velocity
        // Parallel component = (v · los_unit) * los_unit
        double parallelComponent = vx * losUnitX + vy * losUnitY;
        double parallelX = parallelComponent * losUnitX;
        double parallelY = parallelComponent * losUnitY;
        
        // Perpendicular component = v - parallel_component
        double perpX = vx - parallelX;
        double perpY = vy - parallelY;
        
        // Return magnitude of perpendicular component
        return Math.sqrt(perpX * perpX + perpY * perpY);
    }
}
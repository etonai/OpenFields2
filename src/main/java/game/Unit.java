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
    public boolean isFiringHighlighted = false;
    private Color preIncapacitationColor = null;
    long lastTickUpdated = -1;
    
    // Rotation system
    public double currentFacing = 0.0; // Current facing direction in degrees (0-360, North = 0)
    public double targetFacing = 0.0;  // Target facing direction
    public boolean isRotating = false; // Whether unit is currently rotating
    private static final double ROTATION_SPEED = 6.0; // 6 degrees per tick (360 degrees per second at 60fps)
    private static final double ROTATION_THRESHOLD = 15.0; // Rotations less than 15 degrees are instant

    public Unit(Character character, double x, double y, Color color, int id) {
        this.id = id;
        this.character = character;
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.color = color;
        this.baseColor = color;
        
        // Initialize with random facing direction
        this.currentFacing = Math.random() * 360.0;
        this.targetFacing = this.currentFacing;
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
            if (color != Color.GRAY) {
                preIncapacitationColor = color;
            }
            color = Color.GRAY;
        } else {
            // Character is not incapacitated
            if (color == Color.GRAY) {
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

        // Update rotation animation
        updateRotation();
        
        if (!hasTarget) return;
        
        // Stopped characters don't move but keep their target
        if (isStopped) return;
        
        // Incapacitated characters cannot move
        if (character.isIncapacitated()) {
            hasTarget = false;
            return;
        }
        
        // Face movement direction while moving, unless auto-targeting with a target
        if (!(character.usesAutomaticTargeting && character.currentTarget != null)) {
            setTargetFacing(targetX, targetY);
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

    public void render(GraphicsContext gc, boolean isSelected, boolean debugMode) {
        gc.setFill(color);
        gc.fillOval(x - 10.5, y - 10.5, 21, 21);
        
        // Show yellow circle around unit when firing
        // if (isFiringHighlighted) {
        //     gc.setStroke(Color.YELLOW);
        //     gc.setLineWidth(3);
        //     gc.strokeOval(x - 15, y - 15, 30, 30);
        // }
        
        if (isSelected) {
            // Show small yellow X at movement target location
            if (hasTarget) {
                gc.setStroke(Color.YELLOW);
                gc.setLineWidth(2);
                // Draw X by drawing two diagonal lines
                gc.strokeLine(targetX - 5, targetY - 5, targetX + 5, targetY + 5);
                gc.strokeLine(targetX - 5, targetY + 5, targetX + 5, targetY - 5);
            }
            
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font(12));
            gc.fillText(character.getDisplayName(), x - 15, y - 15);
            
            // Display movement type and aiming speed only in debug mode
            if (debugMode) {
                gc.setFont(Font.font(10));
                gc.fillText(character.getCurrentMovementType().getDisplayName(), x - 15, y + 25);
                // Display aiming speed
                gc.fillText(character.getCurrentAimingSpeed().getDisplayName(), x - 15, y + 35);
            }
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
     * Set the target facing direction based on a target position
     */
    public void setTargetFacing(double targetX, double targetY) {
        if (character.isIncapacitated()) {
            return; // Incapacitated characters don't rotate
        }
        
        double dx = targetX - this.x;
        double dy = targetY - this.y;
        
        // Calculate angle in degrees (0 = North, clockwise)
        double angleRadians = Math.atan2(dx, -dy); // Note: -dy because Y increases downward
        double angleDegrees = Math.toDegrees(angleRadians);
        if (angleDegrees < 0) angleDegrees += 360;
        
        this.targetFacing = angleDegrees;
        
        // Check if rotation is needed
        double rotationDiff = getShortestRotationDifference(currentFacing, targetFacing);
        if (Math.abs(rotationDiff) > ROTATION_THRESHOLD) {
            isRotating = true;
        } else {
            // Small rotation - make it instant
            currentFacing = targetFacing;
            isRotating = false;
        }
    }
    
    /**
     * Calculate the shortest rotation difference between two angles (in degrees)
     * Returns the signed difference (-180 to +180)
     */
    private double getShortestRotationDifference(double from, double to) {
        double diff = to - from;
        while (diff > 180) diff -= 360;
        while (diff < -180) diff += 360;
        return diff;
    }
    
    /**
     * Update rotation animation
     */
    private void updateRotation() {
        if (!isRotating) return;
        
        double rotationDiff = getShortestRotationDifference(currentFacing, targetFacing);
        
        if (Math.abs(rotationDiff) <= ROTATION_SPEED) {
            // Close enough - snap to target
            currentFacing = targetFacing;
            isRotating = false;
        } else {
            // Continue rotating
            double rotationStep = Math.signum(rotationDiff) * ROTATION_SPEED;
            currentFacing += rotationStep;
            
            // Normalize angle to 0-360 range
            while (currentFacing >= 360) currentFacing -= 360;
            while (currentFacing < 0) currentFacing += 360;
        }
    }
    
    /**
     * Get current facing direction in degrees (0-360, North = 0, clockwise)
     */
    public double getCurrentFacing() {
        return currentFacing;
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
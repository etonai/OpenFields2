package game;

import combat.Character;
import platform.api.Color;

/**
 * Platform-independent Unit class that uses platform.api.Color instead of JavaFX Color.
 * This allows units to work in both JavaFX and console modes.
 */
public class PlatformUnit {
    public final int id;
    public Character character;
    public double x, y;
    public double targetX, targetY;
    public boolean hasTarget = false;
    public boolean isStopped = false;
    
    // Combat target separation (DevCycle 15f)
    public PlatformUnit combatTarget = null;
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
    private static final Color INCAPACITATED_COLOR = Color.GRAY; // Light gray for incapacitated characters

    public PlatformUnit(Character character, double x, double y, Color color, int id) {
        this.id = id;
        this.character = character;
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.color = color;
        this.baseColor = color;
        
        // Initialize with random facing direction
        this.currentFacing = utils.RandomProvider.nextDouble() * 360.0;
        this.targetFacing = this.currentFacing;
    }

    public int getId() {
        return id;
    }

    public void setTarget(double targetX, double targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.hasTarget = true;
        this.isStopped = false;
        
        // Calculate target facing for rotation
        double dx = targetX - x;
        double dy = targetY - y;
        targetFacing = calculateFacing(dx, dy);
        
        // Check if rotation is needed
        double angleDiff = normalizeAngleDifference(targetFacing - currentFacing);
        isRotating = Math.abs(angleDiff) > ROTATION_THRESHOLD;
    }
    
    /**
     * Update unit position and rotation for the current tick
     */
    public void update(long currentTick) {
        // Prevent multiple updates in same tick
        if (lastTickUpdated == currentTick) {
            return;
        }
        lastTickUpdated = currentTick;
        
        // Handle rotation
        if (isRotating) {
            updateRotation();
        }
        
        // Handle movement only if not rotating or rotation is small
        if (hasTarget && !isStopped && !isRotating) {
            moveTowardTarget();
        }
        
        // Check for incapacitation color change
        updateIncapacitationColor();
    }
    
    private void updateRotation() {
        double angleDiff = normalizeAngleDifference(targetFacing - currentFacing);
        
        if (Math.abs(angleDiff) <= ROTATION_SPEED) {
            // Complete the rotation
            currentFacing = targetFacing;
            isRotating = false;
        } else {
            // Rotate by ROTATION_SPEED in the correct direction
            if (angleDiff > 0) {
                currentFacing = (currentFacing + ROTATION_SPEED) % 360;
            } else {
                currentFacing = (currentFacing - ROTATION_SPEED + 360) % 360;
            }
        }
    }
    
    private void moveTowardTarget() {
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        double speed = character.getEffectiveMovementSpeed();
        
        if (distance <= speed) {
            // Reached target
            x = targetX;
            y = targetY;
            hasTarget = false;
            onReachedTarget();
        } else {
            // Move toward target
            double moveX = (dx / distance) * speed;
            double moveY = (dy / distance) * speed;
            x += moveX;
            y += moveY;
        }
    }
    
    private void onReachedTarget() {
        // Reset movement-related states
        character.isMovingToMelee = false;
        character.meleeTarget = null;
    }
    
    private void updateIncapacitationColor() {
        if (character.isIncapacitated() && preIncapacitationColor == null) {
            // Store the current color and change to incapacitated color
            preIncapacitationColor = color;
            color = INCAPACITATED_COLOR;
        } else if (!character.isIncapacitated() && preIncapacitationColor != null) {
            // Restore original color when no longer incapacitated
            color = preIncapacitationColor;
            preIncapacitationColor = null;
        }
    }
    
    /**
     * Calculate facing angle in degrees (0-360) from a direction vector
     */
    private double calculateFacing(double dx, double dy) {
        // Convert to degrees, with 0 degrees = North (negative Y)
        double angle = Math.toDegrees(Math.atan2(dx, -dy));
        // Normalize to 0-360 range
        return (angle + 360) % 360;
    }
    
    /**
     * Normalize angle difference to -180 to 180 range
     */
    private double normalizeAngleDifference(double diff) {
        while (diff > 180) diff -= 360;
        while (diff < -180) diff += 360;
        return diff;
    }
    
    public void stop() {
        isStopped = true;
        hasTarget = false;
    }
    
    /**
     * Highlight unit when hit
     */
    public void applyHitHighlight() {
        isHitHighlighted = true;
        color = Color.YELLOW;
    }
    
    /**
     * Remove hit highlight
     */
    public void removeHitHighlight() {
        isHitHighlighted = false;
        if (!isFiringHighlighted) {
            // Restore appropriate color
            if (character.isIncapacitated() && preIncapacitationColor != null) {
                color = INCAPACITATED_COLOR;
            } else {
                color = baseColor;
            }
        }
    }
    
    /**
     * Highlight unit when firing
     */
    public void applyFiringHighlight() {
        isFiringHighlighted = true;
        color = Color.WHITE;
    }
    
    /**
     * Remove firing highlight
     */
    public void removeFiringHighlight() {
        isFiringHighlighted = false;
        if (!isHitHighlighted) {
            // Restore appropriate color
            if (character.isIncapacitated() && preIncapacitationColor != null) {
                color = INCAPACITATED_COLOR;
            } else {
                color = baseColor;
            }
        }
    }
}
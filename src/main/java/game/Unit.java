package game;

import combat.Character;
import game.interfaces.IUnit;
// JavaFX imports removed - rendering now handled by platform-specific renderers

public class Unit implements IUnit {
    public final int id;
    public Character character;
    public double x, y;
    public double targetX, targetY;
    public boolean hasTarget = false;
    public boolean isStopped = false;
    
    // Combat target separation (DevCycle 15f)
    public Unit combatTarget = null;
    public platform.api.Color color;
    public final platform.api.Color baseColor;
    public boolean isHitHighlighted = false;
    public boolean isFiringHighlighted = false;
    private platform.api.Color preIncapacitationColor = null;
    long lastTickUpdated = -1;
    
    // Rotation system
    public double currentFacing = 0.0; // Current facing direction in degrees (0-360, North = 0)
    public double targetFacing = 0.0;  // Target facing direction
    public boolean isRotating = false; // Whether unit is currently rotating
    private static final double ROTATION_SPEED = 6.0; // 6 degrees per tick (360 degrees per second at 60fps)
    private static final double ROTATION_THRESHOLD = 15.0; // Rotations less than 15 degrees are instant
    private static final platform.api.Color INCAPACITATED_COLOR = platform.api.Color.fromRGB(169, 169, 169); // Light gray for incapacitated characters

    public Unit(Character character, double x, double y, platform.api.Color color, int id) {
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

    // IUnit interface implementation
    
    @Override
    public Character getCharacter() {
        return character;
    }
    
    @Override
    public void setCharacter(Character character) {
        this.character = character;
    }

    @Override
    public double getX() {
        return x;
    }
    
    @Override
    public void setX(double x) {
        this.x = x;
    }

    @Override
    public double getY() {
        return y;
    }
    
    @Override
    public void setY(double y) {
        this.y = y;
    }
    
    @Override
    public double getTargetX() {
        return targetX;
    }
    
    @Override
    public void setTargetX(double targetX) {
        this.targetX = targetX;
    }
    
    @Override
    public double getTargetY() {
        return targetY;
    }
    
    @Override
    public void setTargetY(double targetY) {
        this.targetY = targetY;
    }
    
    @Override
    public boolean hasTarget() {
        return hasTarget;
    }
    
    @Override
    public void setHasTarget(boolean hasTarget) {
        this.hasTarget = hasTarget;
    }
    
    @Override
    public boolean isStopped() {
        return isStopped;
    }
    
    @Override
    public void setStopped(boolean stopped) {
        this.isStopped = stopped;
    }
    
    @Override
    public platform.api.Color getColor() {
        return color;
    }
    
    @Override
    public void setColor(platform.api.Color color) {
        this.color = color;
    }
    
    @Override
    public platform.api.Color getBaseColor() {
        return baseColor;
    }
    
    @Override
    public boolean isHitHighlighted() {
        return isHitHighlighted;
    }
    
    @Override
    public void setHitHighlighted(boolean highlighted) {
        this.isHitHighlighted = highlighted;
    }
    
    @Override
    public boolean isFiringHighlighted() {
        return isFiringHighlighted;
    }
    
    @Override
    public void setFiringHighlighted(boolean highlighted) {
        this.isFiringHighlighted = highlighted;
    }
    
    @Override
    public platform.api.Color getPreIncapacitationColor() {
        return preIncapacitationColor;
    }
    
    @Override
    public void setPreIncapacitationColor(platform.api.Color color) {
        this.preIncapacitationColor = color;
    }
    
    @Override
    public long getLastTickUpdated() {
        return lastTickUpdated;
    }
    
    @Override
    public void setLastTickUpdated(long tick) {
        this.lastTickUpdated = tick;
    }
    
    @Override
    public double getCurrentFacing() {
        return currentFacing;
    }
    
    @Override
    public void setCurrentFacing(double facing) {
        this.currentFacing = facing;
    }
    
    @Override
    public double getTargetFacing() {
        return targetFacing;
    }
    
    @Override
    public void setTargetFacing(double facing) {
        this.targetFacing = facing;
    }
    
    @Override
    public boolean isRotating() {
        return isRotating;
    }
    
    @Override
    public void setRotating(boolean rotating) {
        this.isRotating = rotating;
    }
    public void setTarget(double x, double y) {
        this.targetX = x;
        this.targetY = y;
        this.hasTarget = true;
        this.isStopped = false;
    }
    
    @Override
    public void setCombatTarget(IUnit target) {
        this.combatTarget = (Unit) target; // Safe cast - we control implementation
    }
    
    @Override
    public IUnit getCombatTarget() {
        return combatTarget;
    }
    
    public void clearCombatTarget() {
        this.combatTarget = null;
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
            // Save current color before changing to light gray (only if not already light gray)
            if (!color.equals(INCAPACITATED_COLOR)) {
                preIncapacitationColor = color;
            }
            color = INCAPACITATED_COLOR;
        } else {
            // Character is not incapacitated
            if (color.equals(INCAPACITATED_COLOR)) {
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
        } else {
            // Update facing toward auto-targeting target as character position changes
            setTargetFacing(character.currentTarget.getX(), character.currentTarget.getY());
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

    // Rendering removed - now handled by platform-specific renderers via IUnitRenderer interface

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
    
    @Override
    public void updateRotation(double rotationSpeed, double rotationThreshold) {
        if (!isRotating) return;
        
        double rotationDiff = getShortestRotationDifference(currentFacing, targetFacing);
        
        if (Math.abs(rotationDiff) <= rotationSpeed) {
            // Close enough - snap to target
            currentFacing = targetFacing;
            isRotating = false;
        } else {
            // Continue rotating
            double rotationStep = Math.signum(rotationDiff) * rotationSpeed;
            currentFacing += rotationStep;
            
            // Normalize angle to 0-360 range
            while (currentFacing >= 360) currentFacing -= 360;
            while (currentFacing < 0) currentFacing += 360;
        }
    }
    
    @Override
    public void faceToward(double targetX, double targetY) {
        setTargetFacing(targetX, targetY);
    }
    
    /**
     * Update rotation animation with default values
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
    
    // getCurrentFacing() method already defined above via IUnit interface
    
    /**
     * Calculates the perpendicular velocity component relative to the line of sight to another unit
     * @param other The other unit (typically the shooter)
     * @return The magnitude of velocity perpendicular to the line of sight, in pixels per tick
     */
    @Override
    public double getVelocity() {
        if (!isMoving()) {
            return 0.0;
        }
        double[] vel = getVelocityVector();
        return Math.sqrt(vel[0] * vel[0] + vel[1] * vel[1]);
    }
    
    @Override
    public double getPerpendicularVelocity(IUnit other) {
        if (!isMoving()) {
            return 0.0;
        }
        
        // Vector from other unit to this unit (line of sight)
        double losX = x - other.getX();
        double losY = y - other.getY();
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
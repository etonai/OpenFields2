package platform.impl.console;

import combat.Character;
import game.EntityFactory;
import game.interfaces.IUnit;
import platform.api.Color;

/**
 * Console-specific implementation of entity factory.
 * Creates platform-independent unit implementations for console mode.
 */
public class ConsoleEntityFactory implements EntityFactory {
    
    private static final double ROTATION_SPEED = 6.0; // 6 degrees per tick
    private static final double ROTATION_THRESHOLD = 15.0; // Instant rotation under 15 degrees
    private static final Color INCAPACITATED_COLOR = Color.fromRGB(169, 169, 169); // Light gray
    
    @Override
    public IUnit createUnit(Character character, double x, double y, Color color, int id) {
        return new ConsoleUnit(character, x, y, color, id);
    }
    
    @Override
    public IUnit createUnitFromData(Object unitData) {
        // TODO: Implement when save system is updated
        throw new UnsupportedOperationException("Unit deserialization not yet implemented");
    }
    
    @Override
    public Color getIncapacitatedColor() {
        return INCAPACITATED_COLOR;
    }
    
    @Override
    public double getRotationSpeed() {
        return ROTATION_SPEED;
    }
    
    @Override
    public double getRotationThreshold() {
        return ROTATION_THRESHOLD;
    }
    
    /**
     * Console-specific unit implementation without JavaFX dependencies.
     */
    private static class ConsoleUnit implements IUnit {
        private final int id;
        private Character character;
        private double x, y;
        private double targetX, targetY;
        private boolean hasTarget = false;
        private boolean isStopped = false;
        private IUnit combatTarget = null;
        private Color color;
        private final Color baseColor;
        private boolean isHitHighlighted = false;
        private boolean isFiringHighlighted = false;
        private Color preIncapacitationColor = null;
        private long lastTickUpdated = -1;
        private double currentFacing = 0.0;
        private double targetFacing = 0.0;
        private boolean isRotating = false;
        
        public ConsoleUnit(Character character, double x, double y, Color color, int id) {
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
        
        @Override
        public int getId() {
            return id;
        }
        
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
        public IUnit getCombatTarget() {
            return combatTarget;
        }
        
        @Override
        public void setCombatTarget(IUnit target) {
            this.combatTarget = target;
        }
        
        @Override
        public Color getColor() {
            return color;
        }
        
        @Override
        public void setColor(Color color) {
            this.color = color;
        }
        
        @Override
        public Color getBaseColor() {
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
        public Color getPreIncapacitationColor() {
            return preIncapacitationColor;
        }
        
        @Override
        public void setPreIncapacitationColor(Color color) {
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
        
        @Override
        public boolean isMoving() {
            return hasTarget && !isStopped && !character.isIncapacitated();
        }
        
        @Override
        public void setTarget(double x, double y) {
            this.targetX = x;
            this.targetY = y;
            this.hasTarget = true;
            this.isStopped = false;
        }
        
        @Override
        public double getVelocity() {
            if (!isMoving()) {
                return 0.0;
            }
            double dx = targetX - x;
            double dy = targetY - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance <= 1) {
                return 0.0;
            }
            return character.getEffectiveMovementSpeed() / 60.0;
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
            losX /= losDistance;
            losY /= losDistance;
            
            // Get velocity vector
            double dx = targetX - x;
            double dy = targetY - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance <= 1) {
                return 0.0;
            }
            
            double vx = character.getEffectiveMovementSpeed() / 60.0 * (dx / distance);
            double vy = character.getEffectiveMovementSpeed() / 60.0 * (dy / distance);
            
            // Calculate perpendicular component using cross product magnitude
            double perpendicularVelocity = Math.abs(vx * losY - vy * losX);
            
            return perpendicularVelocity;
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
        
        private double getShortestRotationDifference(double from, double to) {
            double diff = to - from;
            while (diff > 180) diff -= 360;
            while (diff < -180) diff += 360;
            return diff;
        }
    }
}
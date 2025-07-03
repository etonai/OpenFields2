/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import game.Unit;
import game.rendering.IUnitRenderer;
import game.rendering.JavaFXUnitRenderer;
import combat.WeaponType;
import combat.Handedness;
import combat.Weapon;
import combat.WeaponRenderState;

/**
 * Handles all rendering operations for the OpenFields2 game.
 * Extracted from OpenFields2.java to separate rendering concerns.
 */
public class GameRenderer implements BaseGameRenderer {
    // Canvas constants - removed hardcoded values, now use actual Canvas size
    
    // Canvas and camera state
    private final Canvas canvas;
    private double offsetX = 0;
    private double offsetY = 0;
    private double zoom = 1.0;
    
    // Debug rendering state
    private static boolean debugMode = false;
    
    // Game state references
    private List<Unit> units;
    private SelectionManager selectionManager;
    
    // Muzzle flash tracking
    private Map<Integer, Long> muzzleFlashes = new HashMap<>(); // Unit ID -> End tick
    private long currentTick = 0;
    
    /**
     * Creates a new GameRenderer with the specified canvas.
     */
    public GameRenderer(Canvas canvas) {
        this.canvas = canvas;
    }
    
    /**
     * Sets the game state references needed for rendering.
     */
    public void setGameState(List<Unit> units, SelectionManager selectionManager) {
        this.units = units;
        this.selectionManager = selectionManager;
    }
    
    /**
     * Main rendering method that draws the entire game scene.
     */
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.save();
        gc.translate(offsetX, offsetY);
        gc.scale(zoom, zoom);
        
        // Create unit renderer for this frame
        IUnitRenderer unitRenderer = new JavaFXUnitRenderer(gc);
        
        // First pass: Draw all unit circles and basic elements
        for (Unit u : units) {
            boolean isSelected = selectionManager.isUnitSelected(u);
            unitRenderer.renderUnit(u, isSelected, debugMode);
            
            // Draw cyan border for multi-selected units (when more than one unit selected)
            if (isSelected && selectionManager.getSelectionCount() > 1) {
                unitRenderer.renderSelectionIndicator(u, true);
            }
            
            // Draw weapon if character has a target and weapon
            renderWeapon(gc, u);
        }
        
        // Draw selection-related graphics (rectangle and center marker)
        selectionManager.render(gc);
        
        // Second pass: Draw target overlays that need to appear on top
        if (selectionManager.getSelectionCount() == 1) {
            Unit selected = selectionManager.getSelected();
            
            // Draw target zone if one exists for the selected character
            if (selected.character.targetZone != null) {
                gc.setStroke(Color.YELLOW);
                gc.setLineWidth(2);
                gc.strokeRect(selected.character.targetZone.x, selected.character.targetZone.y, 
                             selected.character.targetZone.width, selected.character.targetZone.height);
            }
            
            if (selected.character.currentTarget != null) {
                Unit target = (Unit)selected.character.currentTarget;
                
                if (selected.character.isPersistentAttack()) {
                    // Persistent attack: yellow X inside target
                    gc.setStroke(Color.YELLOW);
                    gc.setLineWidth(2);
                    gc.strokeLine(target.x - 5, target.y - 5, target.x + 5, target.y + 5);
                    gc.strokeLine(target.x - 5, target.y + 5, target.x + 5, target.y - 5);
                } else {
                    // Normal attack: small white circle inside target
                    gc.setStroke(Color.WHITE);
                    gc.setLineWidth(2);
                    gc.strokeOval(target.x - 3, target.y - 3, 6, 6);
                }
            }
        }
        
        gc.restore();
    }
    
    // Camera control methods
    public void setOffset(double offsetX, double offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }
    
    public void adjustOffset(double deltaX, double deltaY) {
        this.offsetX += deltaX;
        this.offsetY += deltaY;
    }
    
    public void setZoom(double zoom) {
        this.zoom = zoom;
    }
    
    public void adjustZoom(double factor) {
        this.zoom *= factor;
    }
    
    // Debug mode
    public static void setDebugMode(boolean enabled) {
        debugMode = enabled;
    }
    
    public static boolean isDebugMode() {
        return debugMode;
    }
    
    // Coordinate transformation utilities
    public double screenToWorldX(double screenX) {
        return (screenX - offsetX) / zoom;
    }
    
    public double screenToWorldY(double screenY) {
        return (screenY - offsetY) / zoom;
    }
    
    public double worldToScreenX(double worldX) {
        return worldX * zoom + offsetX;
    }
    
    public double worldToScreenY(double worldY) {
        return worldY * zoom + offsetY;
    }
    
    // Getters for camera state
    public double getOffsetX() {
        return offsetX;
    }
    
    public double getOffsetY() {
        return offsetY;
    }
    
    public double getZoom() {
        return zoom;
    }
    
    public Canvas getCanvas() {
        return canvas;
    }
    
    /**
     * Render weapon for a unit if they have a target and weapon
     */
    private void renderWeapon(GraphicsContext gc, Unit unit) {
        // Always render melee weapons when in melee combat mode, otherwise only render with target/facing
        if (!unit.character.isMeleeCombatMode && unit.character.currentTarget == null && unit.character.lastTargetFacing == null) {
            return;
        }
        
        // Determine which weapon to render based on combat mode
        Weapon weaponToRender;
        if (unit.character.isMeleeCombatMode && unit.character.meleeWeapon != null) {
            weaponToRender = unit.character.meleeWeapon;
        } else if (!unit.character.isMeleeCombatMode && unit.character.rangedWeapon != null) {
            weaponToRender = unit.character.rangedWeapon;
        } else {
            // Fallback to legacy weapon field if combat mode weapons not available
            weaponToRender = unit.character.weapon;
        }
        
        // Skip if no weapon to render
        if (weaponToRender == null) {
            return;
        }
        
        // Skip if weapon type is OTHER
        WeaponType weaponType = weaponToRender.getWeaponType();
        if (weaponType == WeaponType.OTHER) {
            return;
        }
        
        // Check weapon state - only render for visible states
        if (unit.character.currentWeaponState == null) {
            System.err.println("Warning: Null weapon state for " + unit.character.getDisplayName() + " - skipping weapon render");
            return;
        }
        String weaponState = unit.character.currentWeaponState.getState();
        
        // Task #7: Hide melee weapons when in initial state (sheathed)
        if (unit.character.isMeleeCombatMode && "sheathed".equals(weaponState)) {
            return; // Hide melee weapons in sheathed state
        }
        
        WeaponRenderState renderState = WeaponRenderState.fromWeaponState(weaponState);
        if (!renderState.isVisible()) {
            return; // Hide weapon during hidden states
        }
        
        // Calculate weapon properties
        double weaponLength = getWeaponLength(weaponToRender);
        boolean isLeftHanded = unit.character.handedness == Handedness.LEFT_HANDED;
        
        // Use unit's current facing direction (unit rotation system)
        double facingRadians = Math.toRadians(unit.getCurrentFacing());
        double dirX = Math.sin(facingRadians); // Note: sin for X because 0 degrees = North
        double dirY = -Math.cos(facingRadians); // Note: -cos for Y because Y increases downward
        
        // Calculate perpendicular vector for tangent line (perpendicular to target direction)
        double perpX = -dirY; // Perpendicular vector (90 degrees rotation)
        double perpY = dirX;
        
        // Select left or right tangent based on handedness
        double tangentMultiplier = isLeftHanded ? -1 : 1;
        
        // Calculate tangent point on circle (where weapon base will be for rifles)
        double circleRadius = 12; // Unit circle radius
        double tangentX = unit.x + perpX * circleRadius * tangentMultiplier;
        double tangentY = unit.y + perpY * circleRadius * tangentMultiplier;
        
        // Calculate weapon start and end positions based on weapon type and state
        double startX, startY, endX, endY;
        boolean isAimingState = renderState.isAimed();
        boolean isReadyState = !isAimingState;
        
        if (weaponType == WeaponType.RIFLE) {
            if (isReadyState) {
                // Rifle ready state: Move 14 pixels (2 feet) closer to target, then rotate 60 degrees towards center
                double readyStartX = tangentX + dirX * 14;
                double readyStartY = tangentY + dirY * 14;
                
                // Calculate 60-degree rotation towards unit center
                double angleToCenter = Math.atan2(-readyStartY + unit.y, -readyStartX + unit.x);
                double currentAngle = Math.atan2(dirY, dirX);
                double rotationDirection = isLeftHanded ? 1 : -1; // Left-handed: clockwise, Right-handed: counterclockwise
                double readyAngle = currentAngle + rotationDirection * Math.toRadians(60);
                
                startX = readyStartX;
                startY = readyStartY;
                endX = startX + Math.cos(readyAngle) * weaponLength;
                endY = startY + Math.sin(readyAngle) * weaponLength;
            } else {
                // Rifle aiming state: Start at tangent point, point toward target
                startX = tangentX;
                startY = tangentY;
                endX = startX + dirX * weaponLength;
                endY = startY + dirY * weaponLength;
            }
        } else if (weaponType == WeaponType.PISTOL) {
            if (isReadyState) {
                // Pistol ready state: Start at standard position, rotate 45 degrees towards unit center
                double pistolStartX = tangentX + dirX * 14;
                double pistolStartY = tangentY + dirY * 14;
                
                double currentAngle = Math.atan2(dirY, dirX);
                double rotationDirection = isLeftHanded ? 1 : -1; // Left-handed: clockwise, Right-handed: counterclockwise
                double readyAngle = currentAngle + rotationDirection * Math.toRadians(45);
                
                startX = pistolStartX;
                startY = pistolStartY;
                endX = startX + Math.cos(readyAngle) * weaponLength;
                endY = startY + Math.sin(readyAngle) * weaponLength;
            } else {
                // Pistol aiming state: Start at tangent point, then move 14 pixels closer to target
                startX = tangentX + dirX * 14;
                startY = tangentY + dirY * 14;
                endX = startX + dirX * weaponLength;
                endY = startY + dirY * weaponLength;
            }
        } else if (weaponType == WeaponType.SUBMACHINE_GUN) {
            // Submachine guns use rifle-style positioning (tangent point start)
            if (isReadyState) {
                // Submachine gun ready state: Move 14 pixels (2 feet) closer to target, then rotate 60 degrees towards center
                double readyStartX = tangentX + dirX * 14;
                double readyStartY = tangentY + dirY * 14;
                
                // Calculate 60-degree rotation towards unit center
                double angleToCenter = Math.atan2(-readyStartY + unit.y, -readyStartX + unit.x);
                double currentAngle = Math.atan2(dirY, dirX);
                double rotationDirection = isLeftHanded ? 1 : -1; // Left-handed: clockwise, Right-handed: counterclockwise
                double readyAngle = currentAngle + rotationDirection * Math.toRadians(60);
                
                startX = readyStartX;
                startY = readyStartY;
                endX = startX + Math.cos(readyAngle) * weaponLength;
                endY = startY + Math.sin(readyAngle) * weaponLength;
            } else {
                // Submachine gun aiming state: Start at tangent point, point toward target (like rifles)
                startX = tangentX;
                startY = tangentY;
                endX = startX + dirX * weaponLength;
                endY = startY + dirY * weaponLength;
            }
        } else if (weaponType == WeaponType.MELEE_SHORT || weaponType == WeaponType.MELEE_MEDIUM || 
                   weaponType == WeaponType.MELEE_LONG || weaponType == WeaponType.MELEE_UNARMED) {
            // Task #8: State-based melee weapon rendering
            if (isReadyState) {
                // Melee ready state: Move 14 pixels (2 feet) closer to target, then rotate 60 degrees towards center
                double meleeReadyStartX = tangentX + dirX * 14;
                double meleeReadyStartY = tangentY + dirY * 14;
                
                // Task #11: Calculate 60-degree rotation towards unit center (similar to rifle ready state)
                double currentAngle = Math.atan2(dirY, dirX);
                double rotationDirection = isLeftHanded ? 1 : -1; // Left-handed: clockwise, Right-handed: counterclockwise
                double readyAngle = currentAngle + rotationDirection * Math.toRadians(60);
                
                startX = meleeReadyStartX;
                startY = meleeReadyStartY;
                endX = startX + Math.cos(readyAngle) * weaponLength;
                endY = startY + Math.sin(readyAngle) * weaponLength;
            } else {
                // Task #10: Melee attacking state - use consistent base position and point toward target center
                double meleeAttackStartX = tangentX + dirX * 14;
                double meleeAttackStartY = tangentY + dirY * 14;
                
                // Calculate direction toward target center
                if (unit.character.currentTarget != null) {
                    Unit target = (Unit)unit.character.currentTarget;
                    double targetDirX = target.x - meleeAttackStartX;
                    double targetDirY = target.y - meleeAttackStartY;
                    double targetDistance = Math.sqrt(targetDirX * targetDirX + targetDirY * targetDirY);
                    
                    // Avoid division by zero and normalize direction
                    if (targetDistance > 0.001) { // Small threshold to avoid zero-distance issues
                        targetDirX /= targetDistance;
                        targetDirY /= targetDistance;
                    } else {
                        // Fallback to unit facing direction if target is too close
                        targetDirX = dirX;
                        targetDirY = dirY;
                    }
                    
                    startX = meleeAttackStartX;
                    startY = meleeAttackStartY;
                    endX = startX + targetDirX * weaponLength;
                    endY = startY + targetDirY * weaponLength;
                } else {
                    // Fallback: No target available, use unit facing direction
                    startX = meleeAttackStartX;
                    startY = meleeAttackStartY;
                    endX = startX + dirX * weaponLength;
                    endY = startY + dirY * weaponLength;
                }
            }
        } else { // OTHER weapons
            // OTHER weapons use default positioning
            startX = tangentX;
            startY = tangentY;
            endX = startX + dirX * weaponLength;
            endY = startY + dirY * weaponLength;
        }
        
        // Draw weapon line in black
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(startX, startY, endX, endY);
        
        // Draw muzzle flash if active
        renderMuzzleFlash(gc, unit.getId(), endX, endY);
    }
    
    /**
     * Get weapon length in pixels based on weapon instance
     */
    private double getWeaponLength(Weapon weapon) {
        if (weapon == null) {
            return 0;
        }
        
        // Convert feet to pixels (7 pixels = 1 foot)
        return weapon.getWeaponLength() * 7.0;
    }
    
    /**
     * Render muzzle flash if active for the given unit
     */
    private void renderMuzzleFlash(GraphicsContext gc, int unitId, double muzzleX, double muzzleY) {
        Long flashEndTick = muzzleFlashes.get(unitId);
        if (flashEndTick != null && currentTick <= flashEndTick) {
            // Muzzle flash is active - draw yellow circle
            gc.setFill(Color.YELLOW);
            double flashRadius = 2.5; // 5 pixel diameter = 2.5 pixel radius
            gc.fillOval(muzzleX - flashRadius, muzzleY - flashRadius, flashRadius * 2, flashRadius * 2);
        }
    }
    
    /**
     * Add a muzzle flash for the specified unit
     */
    public void addMuzzleFlash(int unitId, long fireTick) {
        // Muzzle flash lasts for 0.5 seconds (30 ticks at 60 FPS)
        long flashEndTick = fireTick + 30;
        
        // Extend duration if overlapping with existing flash
        Long existingEndTick = muzzleFlashes.get(unitId);
        if (existingEndTick != null && existingEndTick > fireTick) {
            flashEndTick = Math.max(flashEndTick, existingEndTick);
        }
        
        muzzleFlashes.put(unitId, flashEndTick);
    }
    
    /**
     * Update current tick for muzzle flash timing
     */
    public void setCurrentTick(long tick) {
        this.currentTick = tick;
        
        // Clean up expired muzzle flashes
        muzzleFlashes.entrySet().removeIf(entry -> entry.getValue() < tick);
    }
}
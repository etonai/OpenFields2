package core;

import platform.api.*;
import platform.api.Color;
import game.*;
import combat.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Platform-independent game engine that manages game state and logic.
 * Processes input commands and generates render commands.
 */
public class GameEngine {
    private final GameState gameState;
    private final Platform platform;
    private final Queue<InputCommand> inputQueue;
    private final List<RenderCommand> renderCommands;
    private final GameCallbacks gameCallbacks;
    
    // Frame timing
    private static final long FRAME_TIME_NANOS = 16_666_667; // ~60 FPS
    private long lastFrameTime;
    
    public GameEngine(Platform platform) {
        this.platform = platform;
        this.gameState = new GameState();
        this.inputQueue = new ConcurrentLinkedQueue<>();
        this.renderCommands = new ArrayList<>();
        this.gameCallbacks = createGameCallbacks();
        this.lastFrameTime = System.nanoTime();
    }
    
    /**
     * Main game loop that runs independently of platform.
     */
    public void run() {
        while (platform.isRunning()) {
            long currentTime = System.nanoTime();
            long deltaTime = currentTime - lastFrameTime;
            
            // Fixed timestep with frame skip
            if (deltaTime >= FRAME_TIME_NANOS) {
                update();
                render();
                lastFrameTime = currentTime;
                
                // Sleep if we have extra time
                long sleepTime = FRAME_TIME_NANOS - (System.nanoTime() - currentTime);
                if (sleepTime > 0) {
                    platform.sleep(sleepTime / 1_000_000);
                }
            }
        }
    }
    
    /**
     * Updates game state for one frame.
     */
    private void update() {
        // Poll platform input
        platform.getInputProvider().pollEvents();
        
        // Process input commands
        processInputCommands();
        
        // Update game state
        if (!gameState.isPaused()) {
            gameState.advanceTick();
            gameState.processEvents();
            
            // Update all units
            long currentTick = gameState.getGameClock().getCurrentTick();
            for (Unit unit : gameState.getUnits()) {
                unit.update(currentTick);
            }
        }
    }
    
    /**
     * Renders the current frame.
     */
    private void render() {
        renderCommands.clear();
        
        // Generate render commands
        generateRenderCommands();
        
        // Execute render commands on platform
        Renderer renderer = platform.getRenderer();
        renderer.beginFrame();
        
        for (RenderCommand cmd : renderCommands) {
            executeRenderCommand(renderer, cmd);
        }
        
        renderer.endFrame();
        renderer.present();
    }
    
    /**
     * Processes all queued input commands.
     */
    private void processInputCommands() {
        InputCommand cmd;
        while ((cmd = inputQueue.poll()) != null) {
            processInputCommand(cmd);
        }
    }
    
    /**
     * Processes a single input command.
     */
    private void processInputCommand(InputCommand cmd) {
        switch (cmd.getType()) {
            case SELECT_UNIT:
                handleSelectUnit(cmd.getUnitId());
                break;
                
            case DESELECT_ALL:
                gameState.setSelectedUnitId(-1);
                break;
                
            case MOVE_UNIT:
                handleMoveUnit(cmd.getUnitId(), cmd.getX(), cmd.getY());
                break;
                
            case ATTACK_TARGET:
                handleAttackTarget(cmd.getUnitId(), cmd.getTargetId());
                break;
                
            case PAN_CAMERA:
                gameState.panCamera(cmd.getX(), cmd.getY());
                break;
                
            case ZOOM_IN:
                gameState.zoomCamera(1.1);
                break;
                
            case ZOOM_OUT:
                gameState.zoomCamera(0.9);
                break;
                
            case PAUSE_RESUME:
                gameState.togglePause();
                break;
                
            // Add more command handlers as needed
        }
    }
    
    /**
     * Handles unit selection.
     */
    private void handleSelectUnit(int unitId) {
        Unit unit = gameState.getUnitById(unitId);
        if (unit != null) {
            gameState.setSelectedUnitId(unitId);
        }
    }
    
    /**
     * Handles unit movement.
     */
    private void handleMoveUnit(int unitId, double x, double y) {
        Unit unit = gameState.getUnitById(unitId);
        if (unit != null && !unit.character.isIncapacitated()) {
            unit.setTarget(x, y);
        }
    }
    
    /**
     * Handles attack command.
     */
    private void handleAttackTarget(int attackerId, int targetId) {
        Unit attacker = gameState.getUnitById(attackerId);
        Unit target = gameState.getUnitById(targetId);
        
        if (attacker != null && target != null && 
            !attacker.character.isIncapacitated() && 
            !target.character.isIncapacitated()) {
            
            // Start attack sequence
            attacker.character.startAttackSequence(
                attacker, target,
                gameState.getGameClock().getCurrentTick(),
                gameState.getEventQueue(),
                attackerId,
                gameCallbacks
            );
        }
    }
    
    /**
     * Generates render commands for the current frame.
     */
    private void generateRenderCommands() {
        // Clear screen
        renderCommands.add(RenderCommand.clearScreen(Color.BLACK));
        
        // Set camera transform
        RenderCommand.TransformData transform = new RenderCommand.TransformData(
            gameState.getCameraX(),
            gameState.getCameraY(),
            gameState.getCameraZoom()
        );
        renderCommands.add(RenderCommand.setTransform(transform));
        
        // Render units
        for (Unit unit : gameState.getUnits()) {
            renderUnit(unit);
        }
        
        // Render UI elements
        renderUI();
    }
    
    /**
     * Generates render commands for a unit.
     */
    private void renderUnit(Unit unit) {
        // Unit color is already a platform color
        Color color = unit.color;
        
        // Create unit data
        RenderCommand.UnitData unitData = new RenderCommand.UnitData(
            unit.getId(),
            unit.character.getDisplayName(),
            10.0, // radius
            unit.getId() == gameState.getSelectedUnitId(),
            unit.character.isIncapacitated(),
            0.0 // facing angle
        );
        
        // Draw unit
        renderCommands.add(RenderCommand.drawUnit(unit.getX(), unit.getY(), color, unitData));
        
        // Draw health bar
        RenderCommand.HealthBarData healthData = new RenderCommand.HealthBarData(
            unit.character.currentHealth,
            unit.character.health,
            true
        );
        renderCommands.add(RenderCommand.drawHealthBar(
            unit.getX() - 15, unit.getY() - 25, 30, 5,
            Color.GREEN, healthData
        ));
    }
    
    /**
     * Generates render commands for UI elements.
     */
    private void renderUI() {
        // Draw pause indicator if paused
        if (gameState.isPaused()) {
            renderCommands.add(RenderCommand.drawText(
                "PAUSED", 
                platform.getRenderer().getWidth() / 2 - 30,
                30,
                Color.WHITE
            ));
        }
        
        // Draw selected unit info
        Unit selected = gameState.getSelectedUnit();
        if (selected != null) {
            String info = String.format("%s - Health: %d/%d",
                selected.character.getDisplayName(),
                selected.character.currentHealth,
                selected.character.health
            );
            renderCommands.add(RenderCommand.drawText(
                info, 10, platform.getRenderer().getHeight() - 20,
                Color.WHITE
            ));
        }
    }
    
    /**
     * Executes a render command on the platform renderer.
     */
    private void executeRenderCommand(Renderer renderer, RenderCommand cmd) {
        switch (cmd.getType()) {
            case CLEAR_SCREEN:
                renderer.setColor(cmd.getColor());
                renderer.clear();
                break;
                
            case SET_TRANSFORM:
                RenderCommand.TransformData transform = 
                    (RenderCommand.TransformData) cmd.getData();
                renderer.setTransform(transform.offsetX, transform.offsetY, transform.zoom);
                break;
                
            case DRAW_UNIT:
                RenderCommand.UnitData unitData = (RenderCommand.UnitData) cmd.getData();
                renderer.drawUnit(cmd.getX(), cmd.getY(), cmd.getColor(), 
                                unitData.name, unitData.radius);
                break;
                
            case DRAW_HEALTH_BAR:
                RenderCommand.HealthBarData healthData = 
                    (RenderCommand.HealthBarData) cmd.getData();
                double percentage = healthData.currentHealth / healthData.maxHealth;
                renderer.drawHealthBar(
                    cmd.getX(), cmd.getY(), cmd.getWidth(), cmd.getHeight(),
                    percentage, Color.WHITE,
                    cmd.getColor(), Color.DARK_GRAY
                );
                break;
                
            case DRAW_TEXT:
                renderer.drawText(cmd.getText(), cmd.getX(), cmd.getY(), cmd.getColor());
                break;
                
            // Add more render command handlers as needed
        }
    }
    
    /**
     * Creates game callbacks for the combat system.
     */
    private GameCallbacks createGameCallbacks() {
        return new GameCallbacks() {
            @Override
            public void playWeaponSound(Weapon weapon) {
                if (weapon.soundFile != null) {
                    platform.getAudioSystem().playSound(weapon.soundFile);
                }
            }
            
            @Override
            public void scheduleProjectileImpact(Unit shooter, Unit target, 
                                               Weapon weapon, long fireTick, 
                                               double distanceFeet) {
                // Schedule projectile impact event
                long travelTime = 0;
                if (weapon instanceof RangedWeapon) {
                    RangedWeapon ranged = (RangedWeapon) weapon;
                    travelTime = Math.round(distanceFeet / ranged.velocityFeetPerSecond * 60);
                }
                long impactTick = fireTick + travelTime;
                
                ScheduledEvent impactEvent = new ScheduledEvent(impactTick, () -> {
                    // Handle projectile impact
                    if (weapon instanceof RangedWeapon) {
                        // Simple hit resolution for now
                        Wound wound = new Wound(BodyPart.CHEST, WoundSeverity.LIGHT);
                        wound.damage = weapon.damage;
                        wound.projectileName = weapon.getWoundDescription();
                        wound.weaponId = weapon.getWeaponId();
                        target.character.addWound(wound);
                    }
                }, ScheduledEvent.WORLD_OWNER);
                
                gameState.scheduleEvent(impactEvent);
            }
            
            @Override
            public void scheduleMeleeImpact(Unit attacker, Unit target, 
                                          MeleeWeapon weapon, long executionTick) {
                ScheduledEvent meleeEvent = new ScheduledEvent(executionTick, () -> {
                    // Simple melee hit resolution
                    Wound wound = new Wound(BodyPart.CHEST, WoundSeverity.LIGHT);
                    wound.damage = weapon.damage;
                    wound.projectileName = weapon.getWoundDescription();
                    wound.weaponId = weapon.getWeaponId();
                    target.character.addWound(wound);
                    playWeaponSound(weapon);
                }, attacker.getId());
                
                gameState.scheduleEvent(meleeEvent);
            }
            
            @Override
            public void applyFiringHighlight(Unit shooter, long fireTick) {
                // Visual feedback for firing
            }
            
            @Override
            public void addMuzzleFlash(Unit shooter, long fireTick) {
                // Visual effect for muzzle flash
            }
            
            @Override
            public void removeAllEventsForOwner(int ownerId) {
                gameState.getEventQueue().removeIf(e -> e.getOwnerId() == ownerId);
            }
            
            @Override
            public List<Unit> getUnits() {
                return gameState.getUnits();
            }
        };
    }
    
    // Public API for external control
    
    public void queueInputCommand(InputCommand command) {
        inputQueue.offer(command);
    }
    
    public GameState getGameState() {
        return gameState;
    }
    
    public Platform getPlatform() {
        return platform;
    }
    
    public void stop() {
        platform.setRunning(false);
    }
}
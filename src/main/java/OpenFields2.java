/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import combat.*;
import game.*;
import data.WeaponFactory;
import data.WeaponData;
import data.SkillsManager;
import utils.GameConstants;
import data.SaveGameManager;
import data.SaveData;
import data.SaveMetadata;
import data.GameStateData;
import data.CharacterData;
import data.UnitData;
import data.ThemeManager;
import data.UniversalCharacterRegistry;
import data.CharacterFactory;
import data.FactionRegistry;
import input.interfaces.InputManagerCallbacks;
import config.GameConfig;

public class OpenFields2 extends Application implements GameCallbacks, InputManagerCallbacks {
    
    // Headless mode flag for testing
    private boolean headless = false;

    public static double pixelsToFeet(double pixels) {
        return pixels / 7.0;
    }
    
    private static Date createDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day); // Calendar months are 0-based
        return cal.getTime();
    }
    

    // Window dimensions will be loaded from configuration at startup
    private static final GameConfig gameConfig = GameConfig.getInstance();
    static final double MOVE_SPEED = 42.0;

    private Canvas canvas;
    private final List<Unit> units = new ArrayList<>();
    private final SelectionManager selectionManager = new SelectionManager();
    private BaseGameRenderer gameRenderer;
    private InputManager inputManager;
    
    private boolean paused = true;
    private static int stressModifier = -40;
    private final GameClock gameClock = new GameClock();
    private final java.util.PriorityQueue<ScheduledEvent> eventQueue = new java.util.PriorityQueue<>();
    private AudioClip gunshotSound;
    private final SaveGameManager saveGameManager = SaveGameManager.getInstance();
    private final UniversalCharacterRegistry characterRegistry = UniversalCharacterRegistry.getInstance();
    private int nextUnitId = 1;
    private static boolean editMode = false;
    private SaveGameController saveGameController;
    private EditModeController editModeController;
    private Stage primaryStage;

    public static void main(String[] args) {
        System.out.println("Debug: I have a breakpoint here. We should have stopped!!!");
        launch(args);
    }
    
    /**
     * Constructor for headless mode - used for testing without JavaFX UI
     * @param headless true to run in headless mode
     */
    public OpenFields2(boolean headless) {
        this.headless = headless;
    }
    
    /**
     * Default constructor for normal JavaFX application mode
     */
    public OpenFields2() {
        this(false);
    }
    
    /**
     * Initialize Canvas and GameRenderer for JavaFX mode
     */
    private void initializeCanvas() {
        canvas = new Canvas() {
            @Override
            public boolean isResizable() {
                return true;
            }
            
            @Override
            public double prefWidth(double height) {
                return getWidth();
            }
            
            @Override
            public double prefHeight(double width) {
                return getHeight();
            }
            
            @Override
            public void resize(double width, double height) {
                setWidth(width);
                setHeight(height);
            }
        };
        gameRenderer = new GameRenderer(canvas);
    }
    
    /**
     * Initialize the game in headless mode for testing
     * @return true if initialization was successful
     */
    public boolean initializeHeadless() {
        if (!headless) {
            throw new IllegalStateException("initializeHeadless() called in non-headless mode");
        }
        
        // Display game title and theme information
        displayStartupTitle();
        
        // Load and apply debug configuration from config file
        config.DebugConfig.getInstance().applyConfiguration();
        
        // Initialize faction system
        initializeFactionSystem();
        
        createUnits();
        
        // Initialize EventSchedulingService for managers
        game.EventSchedulingService.getInstance().initialize(eventQueue, gameClock);
        
        // Initialize game renderer for headless mode
        gameRenderer = new HeadlessGameRenderer();
        gameRenderer.setGameState(units, selectionManager);
        
        // Skip audio loading in headless mode
        
        // Get window dimensions from configuration
        int windowWidth = gameConfig.getDisplay().getWindow().getWidth();
        int windowHeight = gameConfig.getDisplay().getWindow().getHeight();
        
        // Skip EditModeController in headless mode - it requires JavaFX GameRenderer
        // editModeController = new EditModeController(units, selectionManager, gameRenderer, 
        //                                            windowWidth, windowHeight, new EditModeCallbacksImpl());
        
        // Skip InputManager in headless mode - not needed for basic testing
        // inputManager = ...
        
        // Create a simple GameRenderer wrapper for SaveGameController in headless mode
        GameRenderer gameRendererForSave = new HeadlessGameRendererWrapper();
        
        // Initialize SaveGameController for headless mode
        saveGameController = new SaveGameController(units, selectionManager, gameRendererForSave, gameClock,
                                                   eventQueue, null, new GameStateAccessorImpl());
        
        return true;
    }
    
    /**
     * Runs a single tick of the game loop - for headless mode testing
     */
    public void runSingleTick() {
        run();
    }
    
    /**
     * Gets the current game clock - for headless mode testing
     */
    public GameClock getGameClock() {
        return gameClock;
    }
    
    /**
     * Gets the selection manager - for headless mode testing
     */
    public SelectionManager getSelectionManager() {
        return selectionManager;
    }
    
    /**
     * Gets the save game controller - for headless mode testing
     */
    public SaveGameController getSaveGameController() {
        return saveGameController;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Initialize Canvas for JavaFX mode
        initializeCanvas();
        
        // Display game title and theme information
        displayStartupTitle();
        
        // Load and apply debug configuration from config file
        config.DebugConfig.getInstance().applyConfiguration();
        
        // Initialize faction system
        initializeFactionSystem();
        
        createUnits();
        
        // Initialize EventSchedulingService for managers
        game.EventSchedulingService.getInstance().initialize(eventQueue, gameClock);
        
        // Initialize game renderer with game state
        gameRenderer.setGameState(units, selectionManager);
        
        try {
            gunshotSound = new AudioClip(getClass().getResource("/Slap0003.wav").toExternalForm());
        } catch (Exception e) {
            System.out.println("Could not load gunshot sound: " + e.getMessage());
        }
        
        // Get window dimensions from configuration
        int windowWidth = gameConfig.getDisplay().getWindow().getWidth();
        int windowHeight = gameConfig.getDisplay().getWindow().getHeight();
        
        // Set initial Canvas size to match configured window dimensions
        canvas.setWidth(windowWidth);
        canvas.setHeight(windowHeight);
        
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, windowWidth, windowHeight);
        
        // Add dynamic Canvas resizing listeners to handle window resize
        scene.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            canvas.setWidth(newWidth.doubleValue());
        });
        
        scene.heightProperty().addListener((obs, oldHeight, newHeight) -> {
            canvas.setHeight(newHeight.doubleValue());
        });

        // Initialize EditModeController
        editModeController = new EditModeController(units, selectionManager, (GameRenderer) gameRenderer, 
                                                   windowWidth, windowHeight, new EditModeCallbacksImpl());
        
        // Initialize InputManager
        inputManager = new InputManager(units, selectionManager, (GameRenderer) gameRenderer, gameClock, 
                                      eventQueue, canvas, this);
        inputManager.initializeInputHandlers(scene);
        
        // Initialize SaveGameController
        saveGameController = new SaveGameController(units, selectionManager, (GameRenderer) gameRenderer, gameClock,
                                                   eventQueue, inputManager, new GameStateAccessorImpl());
        
        // Validate system integrity
        inputManager.validateSystemIntegrity();

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.0 / 60), e -> run()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        primaryStage.setScene(scene);
        primaryStage.setTitle(gameConfig.getDisplay().getWindow().getTitle());
        primaryStage.setWidth(gameConfig.getDisplay().getWindow().getWidth());
        primaryStage.setHeight(gameConfig.getDisplay().getWindow().getHeight());
        primaryStage.setResizable(gameConfig.getDisplay().getWindow().isResizable());
        primaryStage.setFullScreen(gameConfig.getDisplay().getWindow().isFullscreen());
        primaryStage.show();
        
        System.out.println("***********************");
        System.out.println("*** Game is paused");
        System.out.println("***********************");
    }

    private void run() {
        if (!paused) {
            gameClock.advanceTick();
            while (!eventQueue.isEmpty() && eventQueue.peek().tick <= gameClock.getCurrentTick()) {
                eventQueue.poll().action.run();
            }
            for (Unit u : units) {
                u.update(gameClock.getCurrentTick());
                // Update automatic targeting for characters that have it enabled
                u.character.updateAutomaticTargeting(u, gameClock.getCurrentTick(), eventQueue, this);
                // Update melee movement progress and trigger attacks when in range
                u.character.updateMeleeMovement(u, gameClock.getCurrentTick(), eventQueue, this);
                // Defense state updates are now handled internally by DefenseManager (DevCycle 23)
                // Update melee recovery state (Bug #1 fix)
                u.character.updateMeleeRecovery(gameClock.getCurrentTick());
                // Update reaction monitoring (DevCycle 28)
                u.character.updateReactionMonitoring(u, gameClock.getCurrentTick(), eventQueue, this);
            }
            
            // Update selection center as selected units move
            if (selectionManager.hasSelection()) {
                // Selection center now managed by SelectionManager
            }
        }
        
        // Update GameRenderer with current tick for muzzle flash timing
        gameRenderer.setCurrentTick(gameClock.getCurrentTick());
        gameRenderer.render();
    }
    void createUnits() {
        // Load characters from universal registry and assign them weapons for this theme
        combat.Character c1 = characterRegistry.getCharacter(1000);
        if (c1 != null) {
            c1.weapon = WeaponFactory.createWeapon("wpn_mp5");
            c1.meleeWeapon = combat.MeleeWeaponFactory.createWeapon("mel_dagger");
            c1.currentWeaponState = c1.weapon.getInitialState();
            c1.setFaction(1);
            units.add(new Unit(c1, 100, 100, platform.api.Color.fromJavaFX(Color.RED), nextUnitId++));
        }
        
        combat.Character c2 = characterRegistry.getCharacter(1001);
        if (c2 != null) {
            c2.weapon = WeaponFactory.createWeapon("wpn_colt_peacemaker");
            c2.meleeWeapon = combat.MeleeWeaponFactory.createWeapon("mel_officers_sword");
            c2.currentWeaponState = c2.weapon.getInitialState();
            c2.setFaction(2);
            units.add(new Unit(c2, 400, 400, platform.api.Color.fromJavaFX(Color.BLUE), nextUnitId++));
        }
        
        combat.Character c3 = characterRegistry.getCharacter(1002);
        if (c3 != null) {
            c3.weapon = WeaponFactory.createWeapon("wpn_colt_peacemaker");
            c3.meleeWeapon = combat.MeleeWeaponFactory.createWeapon("mel_bowie_knife");
            c3.currentWeaponState = c3.weapon.getInitialState();
            c3.setFaction(1);
            units.add(new Unit(c3, 400, 100, platform.api.Color.fromJavaFX(Color.GREEN), nextUnitId++));
        }
        
        combat.Character c4 = characterRegistry.getCharacter(1003);
        if (c4 != null) {
            c4.weapon = WeaponFactory.createWeapon("wpn_plasma_pistol");
            c4.meleeWeapon = combat.MeleeWeaponFactory.createWeapon("mel_cavalry_sabre");
            c4.currentWeaponState = c4.weapon.getInitialState();
            c4.setFaction(2);
            units.add(new Unit(c4, 100, 400, platform.api.Color.fromJavaFX(Color.PURPLE), nextUnitId++));
        }
        
        combat.Character c5 = characterRegistry.getCharacter(1004);
        if (c5 != null) {
            c5.weapon = WeaponFactory.createWeapon("wpn_colt_peacemaker");
            c5.meleeWeapon = combat.MeleeWeaponFactory.createWeapon("mel_bowie_knife");
            c5.currentWeaponState = c5.weapon.getInitialState();
            c5.setFaction(1);
            units.add(new Unit(c5, 600, 100, platform.api.Color.fromJavaFX(Color.ORANGE), nextUnitId++));
        }
        
        combat.Character c6 = characterRegistry.getCharacter(1005);
        if (c6 != null) {
            c6.weapon = WeaponFactory.createWeapon("wpn_colt_peacemaker");
            c6.meleeWeapon = combat.MeleeWeaponFactory.createWeapon("mel_tomahawk");
            c6.currentWeaponState = c6.weapon.getInitialState();
            c6.setFaction(2);
            units.add(new Unit(c6, 600, 400, platform.api.Color.fromJavaFX(Color.MAGENTA), nextUnitId++));
        }
    }
    
    
    
    private static double calculateMovementModifier(Unit shooter) {
        if (!shooter.isMoving()) {
            return 0.0; // Stationary = no penalty
        }
        
        switch (shooter.character.getCurrentMovementType()) {
            case WALK: return -5.0;
            case CRAWL: return -10.0;
            case JOG: return -15.0;
            case RUN: return -25.0;
            default: return 0.0;
        }
    }
    
    private static double calculateTargetMovementModifier(Unit shooter, Unit target) {
        if (!target.isMoving()) {
            return 0.0; // Stationary target = no modifier
        }
        
        // Get perpendicular velocity component in pixels per tick
        double perpendicularVelocity = target.getPerpendicularVelocity(shooter);
        
        // Convert from pixels per tick to feet per second for easier calculation
        // 7 pixels = 1 foot, 60 ticks = 1 second
        double perpendicularVelocityFeetPerSecond = (perpendicularVelocity * 60.0) / 7.0;
        
        // Simple formula: -2 * perpendicular speed in feet/second
        // At walking speed (~6 feet/second perpendicular), this gives about -12 modifier
        // At running speed (~12 feet/second perpendicular), this gives about -24 modifier
        return -perpendicularVelocityFeetPerSecond * 2.0;
    }
    
    private static double calculateSkillModifier(Unit shooter) {
        if (shooter.character.weapon == null) {
            return 0.0;
        }
        
        combat.WeaponType weaponType = shooter.character.weapon.getWeaponType();
        String skillName;
        
        switch (weaponType) {
            case PISTOL:
                skillName = SkillsManager.PISTOL;
                break;
            case RIFLE:
                skillName = SkillsManager.RIFLE;
                break;
            case OTHER:
            default:
                return 0.0; // No skill bonus for OTHER weapon types
        }
        
        int skillLevel = shooter.character.getSkillLevel(skillName);
        return skillLevel * 5.0;
    }
    
    private static String getSkillDebugInfo(Unit shooter) {
        if (shooter.character.weapon == null) {
            return "(no weapon)";
        }
        
        combat.WeaponType weaponType = shooter.character.weapon.getWeaponType();
        String skillName;
        
        switch (weaponType) {
            case PISTOL:
                skillName = SkillsManager.PISTOL;
                break;
            case RIFLE:
                skillName = SkillsManager.RIFLE;
                break;
            case OTHER:
            default:
                return "(weapon type: " + weaponType.getDisplayName() + ", no skill bonus)";
        }
        
        int skillLevel = shooter.character.getSkillLevel(skillName);
        return "(" + skillName + ": " + skillLevel + ")";
    }

    private static double calculateWoundModifier(Unit shooter) {
        double modifier = 0.0;
        
        for (combat.Wound wound : shooter.character.getWounds()) {
            combat.BodyPart bodyPart = wound.getBodyPart();
            combat.WoundSeverity severity = wound.getSeverity();
            
            // Check for head wounds - every point of damage is -1
            if (bodyPart == combat.BodyPart.HEAD) {
                modifier -= getDamageFromSeverity(severity);
            }
            // Check for dominant arm wounds - every point of damage is -1
            else if (isShootingArm(bodyPart, shooter.character.getHandedness())) {
                modifier -= getDamageFromSeverity(severity);
            }
            // Check for other body parts based on severity
            else {
                switch (severity) {
                    case LIGHT:
                        modifier -= 1.0;
                        break;
                    case SERIOUS:
                        modifier -= 2.0;
                        break;
                    case CRITICAL:
                        // Every point of damage from critical wound in other parts is -1
                        modifier -= getDamageFromSeverity(severity);
                        break;
                    case SCRATCH:
                        // No modifier for scratches in other parts
                        break;
                }
            }
        }
        
        return modifier;
    }
    
    private static boolean isShootingArm(combat.BodyPart bodyPart, combat.Handedness handedness) {
        switch (handedness) {
            case LEFT_HANDED:
                return bodyPart == combat.BodyPart.LEFT_ARM;
            case RIGHT_HANDED:
                return bodyPart == combat.BodyPart.RIGHT_ARM;
            case AMBIDEXTROUS:
                // Right arm if ambidextrous
                return bodyPart == combat.BodyPart.RIGHT_ARM;
            default:
                return false;
        }
    }
    
    private static int getDamageFromSeverity(combat.WoundSeverity severity) {
        // Since we don't store actual damage with wounds, we use estimated damage values
        // based on typical weapon damage (around 7-8 damage)
        switch (severity) {
            case SCRATCH:
                return 1; // Always 1 damage
            case LIGHT:
                return 3; // Math.max(1, Math.round(7 * 0.4f)) = 3
            case SERIOUS:
                return 8; // Full weapon damage estimate
            case CRITICAL:
                return 8; // Full weapon damage estimate
            default:
                return 0;
        }
    }

    private static String getWoundModifierDebugInfo(Unit shooter) {
        List<combat.Wound> wounds = shooter.character.getWounds();
        if (wounds.isEmpty()) {
            return "(no wounds)";
        }
        
        StringBuilder debug = new StringBuilder("(");
        double totalModifier = 0.0;
        boolean first = true;
        
        for (combat.Wound wound : wounds) {
            if (!first) debug.append(", ");
            first = false;
            
            combat.BodyPart bodyPart = wound.getBodyPart();
            combat.WoundSeverity severity = wound.getSeverity();
            double woundModifier = 0.0;
            
            if (bodyPart == combat.BodyPart.HEAD) {
                woundModifier = -getDamageFromSeverity(severity);
                debug.append("HEAD ").append(severity.name()).append(": ").append(woundModifier);
            } else if (isShootingArm(bodyPart, shooter.character.getHandedness())) {
                woundModifier = -getDamageFromSeverity(severity);
                String armSide = (bodyPart == combat.BodyPart.LEFT_ARM) ? "LEFT" : "RIGHT";
                debug.append(armSide).append("_ARM(dominant) ").append(severity.name()).append(": ").append(woundModifier);
            } else {
                switch (severity) {
                    case LIGHT:
                        woundModifier = -1.0;
                        break;
                    case SERIOUS:
                        woundModifier = -2.0;
                        break;
                    case CRITICAL:
                        woundModifier = -getDamageFromSeverity(severity);
                        break;
                    case SCRATCH:
                        woundModifier = 0.0;
                        break;
                }
                debug.append(bodyPart.name()).append(" ").append(severity.name()).append(": ").append(woundModifier);
            }
            
            totalModifier += woundModifier;
        }
        
        debug.append(" | total: ").append(totalModifier).append(")");
        return debug.toString();
    }

    
    private static combat.BodyPart determineHitLocation(double randomRoll, double chanceToHit) {
        double excellentThreshold = chanceToHit * 0.2;
        double goodThreshold = chanceToHit * 0.7;
        
        if (randomRoll < excellentThreshold) {
            // Excellent shots have a small chance for headshots
            double headshotRoll = utils.RandomProvider.nextDouble() * 100;
            if (headshotRoll < 15) { // 15% chance for headshot on excellent shots
                return combat.BodyPart.HEAD;
            } else {
                return combat.BodyPart.CHEST;
            }
        } else if (randomRoll < goodThreshold) {
            // Good shots rarely hit the head (2% chance)
            double headshotRoll = utils.RandomProvider.nextDouble() * 100;
            if (headshotRoll < 2) {
                return combat.BodyPart.HEAD;
            } else {
                return utils.RandomProvider.nextDouble() < 0.5 ? combat.BodyPart.CHEST : combat.BodyPart.ABDOMEN;
            }
        } else {
            return getRandomBodyPart();
        }
    }
    
    private static combat.BodyPart getRandomBodyPart() {
        double roll = utils.RandomProvider.nextDouble() * 100;
        
        if (roll < 12) return combat.BodyPart.LEFT_ARM;
        else if (roll < 24) return combat.BodyPart.RIGHT_ARM;
        else if (roll < 32) return combat.BodyPart.LEFT_SHOULDER;
        else if (roll < 40) return combat.BodyPart.RIGHT_SHOULDER;
        else if (roll < 50) return combat.BodyPart.HEAD;
        else if (roll < 55) return combat.BodyPart.LEFT_LEG;
        else return combat.BodyPart.RIGHT_LEG;
    }
    
    private static double calculateRangeModifier(double distanceFeet, double maximumRange) {
        double optimalRange = maximumRange * 0.3;
        double rangeModifier;
        
        if (distanceFeet <= optimalRange) {
            rangeModifier = 10.0 * (1.0 - distanceFeet / optimalRange);
        } else {
            double remainingRange = maximumRange - optimalRange;
            double excessDistance = distanceFeet - optimalRange;
            rangeModifier = -(excessDistance / remainingRange) * 20.0;
        }
        
        return rangeModifier;
    }
    
    private static combat.WoundSeverity determineWoundSeverity(double randomRoll, double chanceToHit, combat.BodyPart hitLocation) {
        double excellentThreshold = chanceToHit * 0.2;
        
        // Excellent shots are always critical
        if (randomRoll < excellentThreshold) {
            return combat.WoundSeverity.CRITICAL;
        }
        
        // Determine wound severity based on hit location
        double severityRoll = utils.RandomProvider.nextDouble() * 100;
        
        if (isVitalArea(hitLocation)) {
            // HEAD/CHEST/ABDOMEN: 30% Critical, 40% Serious, 25% Light, 5% Scratch
            if (severityRoll < 30) return combat.WoundSeverity.CRITICAL;
            else if (severityRoll < 70) return combat.WoundSeverity.SERIOUS;
            else if (severityRoll < 95) return combat.WoundSeverity.LIGHT;
            else return combat.WoundSeverity.SCRATCH;
        } else {
            // ARMS/SHOULDERS/LEGS: 10% Critical, 25% Serious, 45% Light, 20% Scratch
            if (severityRoll < 10) return combat.WoundSeverity.CRITICAL;
            else if (severityRoll < 35) return combat.WoundSeverity.SERIOUS;
            else if (severityRoll < 80) return combat.WoundSeverity.LIGHT;
            else return combat.WoundSeverity.SCRATCH;
        }
    }
    
    private static boolean isVitalArea(combat.BodyPart bodyPart) {
        return bodyPart == combat.BodyPart.HEAD || bodyPart == combat.BodyPart.CHEST || bodyPart == combat.BodyPart.ABDOMEN;
    }
    
    // Backwards compatible method for tests
    private static int calculateActualDamage(int weaponDamage, combat.WoundSeverity woundSeverity) {
        return calculateActualDamage(weaponDamage, woundSeverity, null);
    }
    
    private static int calculateActualDamage(int weaponDamage, combat.WoundSeverity woundSeverity, combat.BodyPart hitLocation) {
        if (GameRenderer.isDebugMode()) {
            System.out.println("=== DAMAGE CALCULATION DEBUG ===");
            System.out.println("Weapon damage: " + weaponDamage);
            System.out.println("Wound severity: " + woundSeverity);
            System.out.println("Hit location: " + hitLocation);
        }
        
        int baseDamage;
        switch (woundSeverity) {
            case CRITICAL:
            case SERIOUS:
                baseDamage = weaponDamage;
                if (GameRenderer.isDebugMode()) {
                    System.out.println("Critical/Serious wound - using full weapon damage: " + baseDamage);
                }
                break;
            case LIGHT:
                baseDamage = Math.max(1, Math.round(weaponDamage * 0.4f));
                if (GameRenderer.isDebugMode()) {
                    System.out.println("Light wound - 40% of weapon damage: " + baseDamage);
                }
                break;
            case SCRATCH:
                baseDamage = 1;
                if (GameRenderer.isDebugMode()) {
                    System.out.println("Scratch wound - fixed 1 damage: " + baseDamage);
                }
                break;
            default:
                baseDamage = 0;
                if (GameRenderer.isDebugMode()) {
                    System.out.println("Unknown wound severity - 0 damage: " + baseDamage);
                }
        }
        
        // Apply headshot damage multiplier
        if (hitLocation == combat.BodyPart.HEAD) {
            int originalDamage = baseDamage;
            baseDamage = Math.round(baseDamage * 1.5f);
            if (GameRenderer.isDebugMode()) {
                System.out.println("Headshot multiplier applied: " + originalDamage + " -> " + baseDamage);
            }
        }
        
        if (GameRenderer.isDebugMode()) {
            System.out.println("Final calculated damage: " + baseDamage);
            System.out.println("==================================");
        }
        
        return baseDamage;
    }
    
    public void playWeaponSound(Weapon weapon) {
        try {
            if (GameRenderer.isDebugMode()) {
                System.out.println("*** Attempting to play sound: " + weapon.soundFile);
            }
            AudioClip sound = new AudioClip(getClass().getResource(weapon.soundFile).toExternalForm());
            if (GameRenderer.isDebugMode()) {
                System.out.println("*** Sound loaded successfully, playing...");
            }
            sound.play();
        } catch (Exception ex) {
            System.out.println("*** ERROR playing sound: " + ex.getMessage());
        }
    }
    
    public void scheduleProjectileImpact(Unit shooter, Unit target, Weapon weapon, long fireTick, double distanceFeet) {
        long impactTick = fireTick + Math.round(distanceFeet / ((RangedWeapon)weapon).getVelocityFeetPerSecond() * 60);
        HitResult hitResult = CombatCalculator.determineHit(shooter, target, distanceFeet, ((RangedWeapon)weapon).getMaximumRange(), weapon.weaponAccuracy, weapon.damage, GameRenderer.isDebugMode(), OpenFields2.stressModifier, fireTick);
        if (GameRenderer.isDebugMode()) {
            System.out.println("--- Ranged attack impact scheduled at tick " + impactTick + (hitResult.isHit() ? " (will hit)" : " (will miss)"));
        }
        
        // Track attack attempt (both legacy and separate tracking)
        shooter.character.attacksAttempted++;
        shooter.character.rangedAttacksAttempted++;
        
        eventQueue.add(new ScheduledEvent(impactTick, () -> {
            // Use CombatResolver for consistent tracking (like melee attacks)
            CombatResolver combatResolver = new CombatResolver(units, eventQueue, GameRenderer.isDebugMode());
            combatResolver.resolveCombatImpact(shooter, target, weapon, impactTick, hitResult);
        }, ScheduledEvent.WORLD_OWNER));
    }
    
    public void scheduleMeleeImpact(Unit attacker, Unit target, MeleeWeapon weapon, long attackTick) {
        // For melee attacks, impact is immediate (no travel time)
        // Calculate hit chance using melee combat resolver
        CombatResolver combatResolver = new CombatResolver(units, eventQueue, GameRenderer.isDebugMode());
        
        if (GameRenderer.isDebugMode()) {
            System.out.println("[MELEE-EVENT] Melee attack impact scheduled at tick " + attackTick);
            System.out.println("[MELEE-EVENT] Attacker: " + attacker.character.getDisplayName() + " -> Target: " + target.character.getDisplayName());
            System.out.println("[MELEE-EVENT] Weapon: " + weapon.getName());
        }
        
        eventQueue.add(new ScheduledEvent(attackTick, () -> {
            if (GameRenderer.isDebugMode()) {
                System.out.println("[MELEE-EVENT] Executing melee impact resolution at tick " + attackTick);
            }
            
            // DevCycle 33: System 13 - Play audio for all melee attacks (recovery timing issue fixed)
            // Recovery check removed because recovery starts before this audio check runs
            playWeaponSound(weapon);
            
            combatResolver.resolveMeleeAttack(attacker, target, weapon, attackTick);
        }, ScheduledEvent.WORLD_OWNER));
    }
    
    public void applyFiringHighlight(Unit shooter, long fireTick) {
        if (!shooter.isFiringHighlighted) {
            shooter.isFiringHighlighted = true;
            eventQueue.add(new ScheduledEvent(fireTick + 10, () -> {
                shooter.isFiringHighlighted = false;
            }, ScheduledEvent.WORLD_OWNER));
        }
    }
    
    public void addMuzzleFlash(Unit shooter, long fireTick) {
        gameRenderer.addMuzzleFlash(shooter.getId(), fireTick);
    }
    
    private void resolveCombatImpact(Unit shooter, Unit target, Weapon weapon, long impactTick, HitResult hitResult) {
        if (hitResult.isHit()) {
            combat.BodyPart hitLocation = hitResult.getHitLocation();
            combat.WoundSeverity woundSeverity = hitResult.getWoundSeverity();
            int actualDamage = hitResult.getActualDamage();
            
            System.out.println(">>> " + weapon.getProjectileName() + " hit " + target.character.getDisplayName() + " in the " + hitLocation.name().toLowerCase() + " causing a " + woundSeverity.name().toLowerCase() + " wound at tick " + impactTick);
            target.character.health -= actualDamage;
            System.out.println(">>> " + target.character.getDisplayName() + " takes " + actualDamage + " damage. Health now: " + target.character.health);
            
            // Track successful attack
            shooter.character.attacksSuccessful++;
            
            // Track wound infliction by type
            switch (woundSeverity) {
                case SCRATCH:
                    shooter.character.woundsInflictedScratch++;
                    break;
                case LIGHT:
                    shooter.character.woundsInflictedLight++;
                    break;
                case SERIOUS:
                    shooter.character.woundsInflictedSerious++;
                    break;
                case CRITICAL:
                    shooter.character.woundsInflictedCritical++;
                    break;
            }
            
            // Track headshot statistics
            if (hitLocation == combat.BodyPart.HEAD) {
                shooter.character.headshotsSuccessful++;
                System.out.println(">>> HEADSHOT! " + shooter.character.getDisplayName() + " scored a headshot on " + target.character.getDisplayName());
            }
            
            // Add wound to character's wound list with hesitation mechanics
            String weaponId = weapon.getWeaponId(); // Direct access to weapon ID (DevCycle 17)
            target.character.addWound(new combat.Wound(hitLocation, woundSeverity, weapon.getProjectileName(), weaponId, actualDamage), impactTick, eventQueue, target.getId());
            
            // Check for incapacitation
            boolean wasIncapacitated = target.character.isIncapacitated();
            if (wasIncapacitated) {
                // Track incapacitation caused by this shooter
                shooter.character.targetsIncapacitated++;
                
                // Track headshot incapacitation
                if (hitLocation == combat.BodyPart.HEAD) {
                    shooter.character.headshotIncapacitations++;
                    System.out.println(">>> " + target.character.getDisplayName() + " is incapacitated by headshot!");
                }
                
                if (woundSeverity == combat.WoundSeverity.CRITICAL) {
                    System.out.println(">>> " + target.character.getDisplayName() + " is incapacitated by critical wound!");
                } else {
                    System.out.println(">>> " + target.character.getDisplayName() + " is incapacitated!");
                }
                target.character.baseMovementSpeed = 0;
                eventQueue.removeIf(e -> e.getOwnerId() == target.getId());
                if (GameRenderer.isDebugMode()) {
                    System.out.println(">>> Removed all scheduled actions for " + target.character.getDisplayName());
                }
            }
            
            applyHitHighlight(target, impactTick);
        } else {
            System.out.println(">>> " + weapon.getProjectileName() + " missed " + target.character.getDisplayName() + " at tick " + impactTick);
            
            // Handle stray shot mechanics
            handleStrayShot(shooter, target, weapon, impactTick);
        }
    }
    
    private void handleStrayShot(Unit shooter, Unit target, Weapon weapon, long impactTick) {
        // Calculate the original trajectory from shooter to target
        double dx = target.x - shooter.x;
        double dy = target.y - shooter.y;
        double distance = Math.hypot(dx, dy);
        
        // Normalize the direction vector
        double directionX = dx / distance;
        double directionY = dy / distance;
        
        // Calculate how far the missed shot travels (extend beyond target)
        double missDistance = distance + (utils.RandomProvider.nextDouble() * 140 + 70); // 10-30 feet beyond target
        
        // Calculate the actual impact point of the missed shot
        double missX = shooter.x + directionX * missDistance;
        double missY = shooter.y + directionY * missDistance;
        
        // Find potential stray targets within a cone area
        List<Unit> potentialTargets = findPotentialStrayTargets(shooter, target, missX, missY, ((RangedWeapon)weapon).getMaximumRange());
        
        // Check each potential target for stray hits
        for (Unit strayTarget : potentialTargets) {
            if (strayTarget != shooter && strayTarget != target) {
                checkStrayHit(shooter, strayTarget, weapon, impactTick, missX, missY);
            }
        }
    }
    
    private List<Unit> findPotentialStrayTargets(Unit shooter, Unit originalTarget, double missX, double missY, double weaponRange) {
        List<Unit> potentialTargets = new ArrayList<>();
        
        // Define stray shot area - cone extending from original trajectory
        double strayRadius = 105; // 15 feet radius around miss point
        
        for (Unit unit : units) {
            if (unit == shooter || unit == originalTarget) continue;
            
            // Check if unit is within stray shot radius of miss point
            double distanceToMiss = Math.hypot(unit.x - missX, unit.y - missY);
            
            // Also check if unit is within weapon range from shooter
            double distanceFromShooter = Math.hypot(unit.x - shooter.x, unit.y - shooter.y);
            
            if (distanceToMiss <= strayRadius && distanceFromShooter <= weaponRange * 7) { // Convert feet to pixels
                potentialTargets.add(unit);
            }
        }
        
        return potentialTargets;
    }
    
    private void checkStrayHit(Unit shooter, Unit strayTarget, Weapon weapon, long impactTick, double missX, double missY) {
        // Calculate distance from stray target to the miss point
        double distanceToMiss = Math.hypot(strayTarget.x - missX, strayTarget.y - missY);
        double distanceFeet = distanceToMiss / 7.0;
        
        // Stray shots have significantly reduced accuracy
        // Base chance is much lower and decreases with distance from miss point
        double baseChance = 15.0; // Base 15% chance for stray hits
        double distancePenalty = distanceFeet * 2.0; // -2% per foot from miss point
        double finalChance = Math.max(1.0, baseChance - distancePenalty);
        
        double roll = utils.RandomProvider.nextDouble() * 100;
        
        if (roll < finalChance) {
            // Stray hit! Calculate reduced damage
            double distanceFromShooter = Math.hypot(strayTarget.x - shooter.x, strayTarget.y - shooter.y) / 7.0;
            
            // Determine hit location (more random for stray shots)
            combat.BodyPart hitLocation = getRandomBodyPart();
            
            // Determine wound severity (reduced for stray shots)
            combat.WoundSeverity woundSeverity = determineStrayWoundSeverity();
            
            // Calculate reduced damage
            int baseDamage = CombatCalculator.calculateActualDamage(weapon.damage, woundSeverity, hitLocation);
            int strayDamage = Math.max(1, Math.round(baseDamage * 0.7f)); // 30% damage reduction for stray shots
            
            // Create hit result for stray shot
            HitResult strayHitResult = new HitResult(true, hitLocation, woundSeverity, strayDamage);
            
            System.out.println(">>> STRAY SHOT! " + weapon.getProjectileName() + " ricocheted and hit " + strayTarget.character.getDisplayName() + " in the " + hitLocation.name().toLowerCase());
            
            // Apply the stray damage
            strayTarget.character.health -= strayDamage;
            System.out.println(">>> " + strayTarget.character.getDisplayName() + " takes " + strayDamage + " stray damage. Health now: " + strayTarget.character.health);
            
            // Track as successful attack for shooter (stray hits still count)
            shooter.character.attacksSuccessful++;
            
            // Track wound infliction
            switch (woundSeverity) {
                case SCRATCH:
                    shooter.character.woundsInflictedScratch++;
                    break;
                case LIGHT:
                    shooter.character.woundsInflictedLight++;
                    break;
                case SERIOUS:
                    shooter.character.woundsInflictedSerious++;
                    break;
                case CRITICAL:
                    shooter.character.woundsInflictedCritical++;
                    break;
            }
            
            // Add wound to target with hesitation mechanics
            String weaponId = weapon.getWeaponId(); // Direct access to weapon ID (DevCycle 17)
            strayTarget.character.addWound(new combat.Wound(hitLocation, woundSeverity, weapon.getProjectileName() + " (stray)", weaponId, strayDamage), impactTick, eventQueue, strayTarget.getId());
            
            // Check for incapacitation from stray shot
            if (strayTarget.character.isIncapacitated()) {
                shooter.character.targetsIncapacitated++;
                System.out.println(">>> " + strayTarget.character.getDisplayName() + " is incapacitated by stray shot!");
                strayTarget.character.baseMovementSpeed = 0;
                eventQueue.removeIf(e -> e.getOwnerId() == strayTarget.getId());
            }
            
            // Apply hit highlight to stray target
            applyHitHighlight(strayTarget, impactTick);
        }
    }
    
    private combat.WoundSeverity determineStrayWoundSeverity() {
        // Stray shots tend to be less severe
        double roll = utils.RandomProvider.nextDouble() * 100;
        
        if (roll < 5) return combat.WoundSeverity.CRITICAL;      // 5% critical
        else if (roll < 20) return combat.WoundSeverity.SERIOUS; // 15% serious  
        else if (roll < 60) return combat.WoundSeverity.LIGHT;   // 40% light
        else return combat.WoundSeverity.SCRATCH;                // 40% scratch
    }
    
    private void applyHitHighlight(Unit target, long impactTick) {
        if (!target.isHitHighlighted) {
            target.isHitHighlighted = true;
            target.color = platform.api.Color.fromJavaFX(Color.YELLOW);
            eventQueue.add(new ScheduledEvent(impactTick + 15, () -> {
                target.color = target.baseColor;
                target.isHitHighlighted = false;
            }, ScheduledEvent.WORLD_OWNER));
        }
    }
    


    public List<Unit> getUnits() {
        return units;
    }

    public java.util.PriorityQueue<ScheduledEvent> getEventQueue() {
        return eventQueue;
    }

    public void removeAllEventsForOwner(int ownerId) {
        eventQueue.removeIf(e -> e.getOwnerId() == ownerId);
    }

    // InputManagerCallbacks implementation
    @Override
    public boolean isPaused() {
        return paused;
    }
    
    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }
    
    @Override
    public boolean isEditMode() {
        return editMode;
    }
    
    @Override
    public void setEditMode(boolean editMode) {
        OpenFields2.editMode = editMode;
    }
    
    @Override
    public boolean isWaitingForCharacterDeployment() {
        return inputManager.getStateTracker().isWaitingForCharacterDeployment();
    }
    
    @Override
    public void setWaitingForCharacterDeployment(boolean waiting) {
        inputManager.getStateTracker().setWaitingForCharacterDeployment(waiting);
    }
    
    @Override
    public boolean isWaitingForVictoryOutcome() {
        return inputManager.getStateTracker().isWaitingForVictoryOutcome();
    }
    
    @Override
    public void setWaitingForVictoryOutcome(boolean waiting) {
        inputManager.getStateTracker().setWaitingForVictoryOutcome(waiting);
    }
    
    @Override
    public int getNextUnitId() {
        return nextUnitId;
    }
    
    @Override
    public void setNextUnitId(int nextUnitId) {
        this.nextUnitId = nextUnitId;
    }
    
    @Override
    public double convertPixelsToFeet(double pixels) {
        return OpenFields2.pixelsToFeet(pixels);
    }
    
    @Override
    public int convertStatToModifier(int stat) {
        return GameConstants.statToModifier(stat);
    }
    
    @Override
    public void promptForSaveSlot() {
        saveGameController.promptForSaveSlot();
    }
    
    @Override
    public void promptForLoadSlot() {
        saveGameController.promptForLoadSlot();
    }
    
    @Override
    public void promptForCharacterCreation() {
        inputManager.setWaitingForCharacterCreation(true);
        editModeController.promptForCharacterCreation();
    }
    
    @Override
    public void promptForWeaponSelection() {
        inputManager.setWaitingForWeaponSelection(true);
        editModeController.promptForWeaponSelection();
    }
    
    @Override
    public void promptForFactionSelection() {
        inputManager.setWaitingForFactionSelection(true);
        editModeController.promptForFactionSelection();
    }
    
    @Override
    public void saveGameToSlot(int slot) {
        saveGameController.saveGameToSlot(slot);
    }
    
    @Override
    public void loadGameFromSlot(int slot) {
        saveGameController.loadGameFromSlot(slot);
    }
    
    @Override
    public void loadGameFromTestSlot(char testSlot) {
        saveGameController.loadGameFromTestSlot(testSlot);
    }
    
    @Override
    public void createCharacterFromArchetype(int archetypeIndex) {
        editModeController.createCharacterFromArchetype(archetypeIndex);
        inputManager.setWaitingForCharacterCreation(false);
    }
    
    @Override
    public void assignWeaponToSelectedUnits(int weaponIndex) {
        editModeController.assignWeaponToSelectedUnits(weaponIndex);
        inputManager.setWaitingForWeaponSelection(false);
    }
    
    @Override
    public void assignFactionToSelectedUnits(int factionNumber) {
        editModeController.assignFactionToSelectedUnits(factionNumber);
        inputManager.setWaitingForFactionSelection(false);
    }
    
    @Override
    public void setWindowTitle(String title) {
        if (primaryStage != null) {
            primaryStage.setTitle(title);
        }
    }
    
    @Override
    public String[] getAvailableThemes() {
        return ThemeManager.getInstance().getAllThemeIds();
    }
    
    @Override
    public void setCurrentTheme(String themeId) {
        ThemeManager.getInstance().setCurrentTheme(themeId);
    }

    // GameStateAccessor implementation for SaveGameController
    private class GameStateAccessorImpl implements SaveGameController.GameStateAccessor {
        @Override
        public boolean isPaused() {
            return paused;
        }
        
        @Override
        public void setPaused(boolean paused) {
            OpenFields2.this.paused = paused;
        }
        
        @Override
        public int getNextUnitId() {
            return nextUnitId;
        }
        
        @Override
        public void setNextUnitId(int nextUnitId) {
            OpenFields2.this.nextUnitId = nextUnitId;
        }
    }
    
    // EditModeCallbacks implementation for EditModeController
    private class EditModeCallbacksImpl implements EditModeController.EditModeCallbacks {
        @Override
        public int getNextUnitId() {
            return nextUnitId;
        }
        
        @Override
        public void setNextUnitId(int nextUnitId) {
            OpenFields2.this.nextUnitId = nextUnitId;
        }
        
        @Override
        public boolean isEditMode() {
            return editMode;
        }
        
        @Override
        public void setEditMode(boolean editMode) {
            OpenFields2.this.editMode = editMode;
        }
    }
    
    /**
     * Display game title and theme information at startup
     */
    private void displayStartupTitle() {
        System.out.println();
        System.out.println("*********************************");
        System.out.println("***       OPEN FIELDS 2      ***");
        System.out.println("*********************************");
        
        // Display current theme information
        try {
            data.ThemeManager themeManager = data.ThemeManager.getInstance();
            data.ThemeData currentTheme = themeManager.getCurrentTheme();
            String themeName = (currentTheme != null && currentTheme.name != null) ? 
                              currentTheme.name : "null";
            System.out.println("Theme: " + themeName);
        } catch (Exception e) {
            System.out.println("Theme: null");
        }
        
        System.out.println();
    }
    
    /**
     * Initialize faction system and load faction registry
     */
    private void initializeFactionSystem() {
        System.out.println("*** Initializing Faction System ***");
        FactionRegistry factionRegistry = FactionRegistry.getInstance();
        factionRegistry.printFactionInfo();
        System.out.println("*** Faction System Ready ***");
    }

}












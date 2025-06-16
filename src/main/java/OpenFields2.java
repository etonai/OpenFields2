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

public class OpenFields2 extends Application implements GameCallbacks, InputManager.InputManagerCallbacks {

    public static double pixelsToFeet(double pixels) {
        return pixels / 7.0;
    }
    
    private static Date createDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day); // Calendar months are 0-based
        return cal.getTime();
    }
    
    public static int statToModifier(int stat) {
        // Clamp stat to valid range
        stat = Math.max(1, Math.min(100, stat));
        
        // Balanced requirements for symmetric distribution:
        // 1. Perfect symmetry around 50-51: statToModifier(50-i) = -statToModifier(51+i)
        // 2. Monotonic: each stat >= previous, increase by at most 1
        // 3. Extremes: 1→-20, 100→+20
        // 4. Center: 50→0, 51→0
        // 5. Close approximation to 1-6: -20 to -15 and 95-100: 15-20
        // 6. Single digits for 21-80 range
        // 7. All integers -20 to +20 possible
        
        // Use a lookup table for perfect control over the distribution
        // This ensures both symmetry and the specific boundary approximations
        int[] modifiers = new int[101]; // index 0 unused, 1-100 are valid stats
        
        // Define the negative half (1-50), then mirror for positive half (51-100)
        modifiers[1] = -20;   // Boundary requirement: 1 → -20
        modifiers[2] = -19;   // Boundary requirement: 2 → -19
        modifiers[3] = -18;   // Boundary requirement: 3 → -18
        modifiers[4] = -17;   // Boundary requirement: 4 → -17
        modifiers[5] = -16;   // Boundary requirement: 5 → -16
        modifiers[6] = -15;   // Boundary requirement: 6 → -15
        modifiers[7] = -14;
        modifiers[8] = -14;
        modifiers[9] = -13;
        modifiers[10] = -13;
        modifiers[11] = -12;
        modifiers[12] = -12;
        modifiers[13] = -11;
        modifiers[14] = -11;
        modifiers[15] = -10;
        modifiers[16] = -10;
        modifiers[17] = -9;
        modifiers[18] = -9;
        modifiers[19] = -8;
        modifiers[20] = -8;
        modifiers[21] = -7;   // Single digit starts here
        modifiers[22] = -7;
        modifiers[23] = -6;
        modifiers[24] = -6;
        modifiers[25] = -5;
        modifiers[26] = -5;
        modifiers[27] = -5;
        modifiers[28] = -4;
        modifiers[29] = -4;
        modifiers[30] = -4;
        modifiers[31] = -3;
        modifiers[32] = -3;
        modifiers[33] = -3;
        modifiers[34] = -3;
        modifiers[35] = -2;
        modifiers[36] = -2;
        modifiers[37] = -2;
        modifiers[38] = -2;
        modifiers[39] = -2;
        modifiers[40] = -1;
        modifiers[41] = -1;
        modifiers[42] = -1;
        modifiers[43] = -1;
        modifiers[44] = -1;
        modifiers[45] = -1;
        modifiers[46] = 0;
        modifiers[47] = 0;
        modifiers[48] = 0;
        modifiers[49] = 0;
        modifiers[50] = 0;    // Center point
        modifiers[51] = 0;    // Center point
        
        // Mirror for the positive half (perfect symmetry)
        for (int i = 1; i <= 49; i++) {
            modifiers[51 + i] = -modifiers[50 - i];
        }
        
        return modifiers[stat];
    }

    static final int WIDTH = 800;
    static final int HEIGHT = 600;
    static final double MOVE_SPEED = 42.0;

    private final Canvas canvas = new Canvas(WIDTH, HEIGHT);
    private final List<Unit> units = new ArrayList<>();
    private final SelectionManager selectionManager = new SelectionManager();
    private final GameRenderer gameRenderer = new GameRenderer(canvas);
    private InputManager inputManager;
    
    private boolean paused = true;
    private static int stressModifier = -20;
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

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Display game title and theme information
        displayStartupTitle();
        
        // Initialize faction system
        initializeFactionSystem();
        
        createUnits();
        
        // Initialize game renderer with game state
        gameRenderer.setGameState(units, selectionManager);
        
        try {
            gunshotSound = new AudioClip(getClass().getResource("/Slap0003.wav").toExternalForm());
        } catch (Exception e) {
            System.out.println("Could not load gunshot sound: " + e.getMessage());
        }
        
        Pane root = new Pane(canvas);
        Scene scene = new Scene(root);

        // Initialize EditModeController
        editModeController = new EditModeController(units, selectionManager, gameRenderer, 
                                                   WIDTH, HEIGHT, new EditModeCallbacksImpl());
        
        // Initialize InputManager
        inputManager = new InputManager(units, selectionManager, gameRenderer, gameClock, 
                                      eventQueue, canvas, this);
        inputManager.initializeInputHandlers(scene);
        
        // Initialize SaveGameController
        saveGameController = new SaveGameController(units, selectionManager, gameRenderer, gameClock,
                                                   eventQueue, inputManager, new GameStateAccessorImpl());
        
        // Validate system integrity
        inputManager.validateSystemIntegrity();

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.0 / 60), e -> run()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        primaryStage.setScene(scene);
        primaryStage.setTitle("Unit Movement Game");
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
            c1.weapon = WeaponFactory.createWeapon("wpn_uzi");
            c1.currentWeaponState = c1.weapon.getInitialState();
            c1.setFaction(1);
            units.add(new Unit(c1, 100, 100, Color.RED, nextUnitId++));
        }
        
        combat.Character c2 = characterRegistry.getCharacter(1001);
        if (c2 != null) {
            c2.weapon = WeaponFactory.createWeapon("wpn_colt_peacemaker");
            c2.currentWeaponState = c2.weapon.getInitialState();
            c2.setFaction(2);
            units.add(new Unit(c2, 400, 400, Color.BLUE, nextUnitId++));
        }
        
        combat.Character c3 = characterRegistry.getCharacter(1002);
        if (c3 != null) {
            c3.weapon = WeaponFactory.createWeapon("wpn_colt_peacemaker");
            c3.currentWeaponState = c3.weapon.getInitialState();
            c3.setFaction(1);
            units.add(new Unit(c3, 400, 100, Color.GREEN, nextUnitId++));
        }
        
        combat.Character c4 = characterRegistry.getCharacter(1003);
        if (c4 != null) {
            c4.weapon = WeaponFactory.createWeapon("wpn_plasma_pistol");
            c4.currentWeaponState = c4.weapon.getInitialState();
            c4.setFaction(2);
            units.add(new Unit(c4, 100, 400, Color.PURPLE, nextUnitId++));
        }
        
        combat.Character c5 = characterRegistry.getCharacter(1004);
        if (c5 != null) {
            c5.weapon = WeaponFactory.createWeapon("wpn_colt_peacemaker");
            c5.currentWeaponState = c5.weapon.getInitialState();
            c5.setFaction(1);
            units.add(new Unit(c5, 600, 100, Color.ORANGE, nextUnitId++));
        }
        
        combat.Character c6 = characterRegistry.getCharacter(1005);
        if (c6 != null) {
            c6.weapon = WeaponFactory.createWeapon("wpn_colt_peacemaker");
            c6.currentWeaponState = c6.weapon.getInitialState();
            c6.setFaction(2);
            units.add(new Unit(c6, 600, 400, Color.MAGENTA, nextUnitId++));
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

    private static HitResult determineHit(Unit shooter, Unit target, double distanceFeet, double maximumRange, int weaponAccuracy, int weaponDamage) {
        double weaponModifier = weaponAccuracy;
        double rangeModifier = calculateRangeModifier(distanceFeet, maximumRange);
        double movementModifier = calculateMovementModifier(shooter);
        double aimingSpeedModifier = shooter.character.getCurrentAimingSpeed().getAccuracyModifier();
        double targetMovementModifier = calculateTargetMovementModifier(shooter, target);
        double woundModifier = calculateWoundModifier(shooter);
        double stressModifier = Math.min(0, OpenFields2.stressModifier + statToModifier(shooter.character.coolness));
        double skillModifier = calculateSkillModifier(shooter);
        double sizeModifier = 0.0;
        double coverModifier = 0.0;
        double chanceToHit = 50.0 + statToModifier(shooter.character.dexterity) + stressModifier + rangeModifier + weaponModifier + movementModifier + aimingSpeedModifier + targetMovementModifier + woundModifier + skillModifier + sizeModifier + coverModifier;
        
        if (distanceFeet <= maximumRange) {
            chanceToHit = Math.max(chanceToHit, 0.01);
        }
        
        double randomRoll = Math.random() * 100;
        
        if (GameRenderer.isDebugMode()) {
            System.out.println("=== HIT CALCULATION DEBUG ===");
            System.out.println("Shooter: " + shooter.character.getDisplayName() + " -> Target: " + target.character.getDisplayName());
            System.out.println("Base chance: 50.0");
            System.out.println("Dexterity modifier: " + statToModifier(shooter.character.dexterity) + " (dex: " + shooter.character.dexterity + ")");
            System.out.println("Stress modifier: " + stressModifier + " (coolness: " + shooter.character.coolness + ":" + statToModifier(shooter.character.coolness) + ")");
            System.out.println("Range modifier: " + String.format("%.2f", rangeModifier) + " (distance: " + String.format("%.2f", distanceFeet) + " feet, max: " + String.format("%.2f", maximumRange) + " feet)");
            System.out.println("Weapon modifier: " + weaponModifier + " (accuracy: " + weaponAccuracy + ")");
            System.out.println("Movement modifier: " + movementModifier);
            System.out.println("Aiming speed modifier: " + aimingSpeedModifier + " (" + shooter.character.getCurrentAimingSpeed().getDisplayName() + ")");
            
            // Enhanced target movement debug info
            if (target.isMoving()) {
                double perpendicularVelocity = target.getPerpendicularVelocity(shooter);
                double perpendicularVelocityFeetPerSecond = (perpendicularVelocity * 60.0) / 7.0;
                System.out.println("Target movement modifier: " + String.format("%.2f", targetMovementModifier) + 
                                 " (perpendicular velocity: " + String.format("%.2f", perpendicularVelocityFeetPerSecond) + " ft/s, " +
                                 String.format("%.2f", perpendicularVelocity) + " px/tick)");
            } else {
                System.out.println("Target movement modifier: " + targetMovementModifier + " (target stationary)");
            }
            
            System.out.println("Wound modifier: " + String.format("%.1f", woundModifier) + " " + getWoundModifierDebugInfo(shooter));
            System.out.println("Skill modifier: " + String.format("%.1f", skillModifier) + " " + getSkillDebugInfo(shooter));
            System.out.println("Size modifier: " + sizeModifier);
            System.out.println("Cover modifier: " + coverModifier);
            System.out.println("Final chance to hit: " + String.format("%.2f", chanceToHit) + "%");
            System.out.println("Random roll: " + String.format("%.2f", randomRoll));
            System.out.println("Result: " + (randomRoll < chanceToHit ? "HIT" : "MISS"));
            System.out.println("=============================");
        }
        
        boolean hit = randomRoll < chanceToHit;
        combat.BodyPart hitLocation = null;
        combat.WoundSeverity woundSeverity = null;
        int actualDamage = 0;
        
        if (hit) {
            hitLocation = determineHitLocation(randomRoll, chanceToHit);
            woundSeverity = determineWoundSeverity(randomRoll, chanceToHit, hitLocation);
            actualDamage = calculateActualDamage(weaponDamage, woundSeverity, hitLocation);
        }
        
        return new HitResult(hit, hitLocation, woundSeverity, actualDamage);
    }
    
    private static combat.BodyPart determineHitLocation(double randomRoll, double chanceToHit) {
        double excellentThreshold = chanceToHit * 0.2;
        double goodThreshold = chanceToHit * 0.7;
        
        if (randomRoll < excellentThreshold) {
            // Excellent shots have a small chance for headshots
            double headshotRoll = Math.random() * 100;
            if (headshotRoll < 15) { // 15% chance for headshot on excellent shots
                return combat.BodyPart.HEAD;
            } else {
                return combat.BodyPart.CHEST;
            }
        } else if (randomRoll < goodThreshold) {
            // Good shots rarely hit the head (2% chance)
            double headshotRoll = Math.random() * 100;
            if (headshotRoll < 2) {
                return combat.BodyPart.HEAD;
            } else {
                return Math.random() < 0.5 ? combat.BodyPart.CHEST : combat.BodyPart.ABDOMEN;
            }
        } else {
            return getRandomBodyPart();
        }
    }
    
    private static combat.BodyPart getRandomBodyPart() {
        double roll = Math.random() * 100;
        
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
        double severityRoll = Math.random() * 100;
        
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
        long impactTick = fireTick + Math.round(distanceFeet / weapon.velocityFeetPerSecond * 60);
        HitResult hitResult = determineHit(shooter, target, distanceFeet, weapon.maximumRange, weapon.weaponAccuracy, weapon.damage);
        if (GameRenderer.isDebugMode()) {
            System.out.println("--- Ranged attack impact scheduled at tick " + impactTick + (hitResult.isHit() ? " (will hit)" : " (will miss)"));
        }
        
        // Track attack attempt
        shooter.character.attacksAttempted++;
        
        eventQueue.add(new ScheduledEvent(impactTick, () -> {
            resolveCombatImpact(shooter, target, weapon, impactTick, hitResult);
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
            String weaponId = SaveGameController.findWeaponId(weapon);
            target.character.addWound(new combat.Wound(hitLocation, woundSeverity, weapon.getProjectileName(), weaponId, actualDamage), impactTick, eventQueue, target.getId());
            
            // Check for incapacitation
            boolean wasIncapacitated = target.character.isIncapacitated();
            if (wasIncapacitated) {
                // Track incapacitation caused by this shooter
                shooter.character.targetsIncapacitated++;
                
                // Track headshot kill
                if (hitLocation == combat.BodyPart.HEAD) {
                    shooter.character.headshotsKills++;
                    System.out.println(">>> HEADSHOT KILL! " + target.character.getDisplayName() + " was killed by a headshot!");
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
        double missDistance = distance + (Math.random() * 140 + 70); // 10-30 feet beyond target
        
        // Calculate the actual impact point of the missed shot
        double missX = shooter.x + directionX * missDistance;
        double missY = shooter.y + directionY * missDistance;
        
        // Find potential stray targets within a cone area
        List<Unit> potentialTargets = findPotentialStrayTargets(shooter, target, missX, missY, weapon.maximumRange);
        
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
        
        double roll = Math.random() * 100;
        
        if (roll < finalChance) {
            // Stray hit! Calculate reduced damage
            double distanceFromShooter = Math.hypot(strayTarget.x - shooter.x, strayTarget.y - shooter.y) / 7.0;
            
            // Determine hit location (more random for stray shots)
            combat.BodyPart hitLocation = getRandomBodyPart();
            
            // Determine wound severity (reduced for stray shots)
            combat.WoundSeverity woundSeverity = determineStrayWoundSeverity();
            
            // Calculate reduced damage
            int baseDamage = calculateActualDamage(weapon.damage, woundSeverity, hitLocation);
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
            String weaponId = SaveGameController.findWeaponId(weapon);
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
        double roll = Math.random() * 100;
        
        if (roll < 5) return combat.WoundSeverity.CRITICAL;      // 5% critical
        else if (roll < 20) return combat.WoundSeverity.SERIOUS; // 15% serious  
        else if (roll < 60) return combat.WoundSeverity.LIGHT;   // 40% light
        else return combat.WoundSeverity.SCRATCH;                // 40% scratch
    }
    
    private void applyHitHighlight(Unit target, long impactTick) {
        if (!target.isHitHighlighted) {
            target.isHitHighlighted = true;
            target.color = Color.YELLOW;
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
        return OpenFields2.statToModifier(stat);
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












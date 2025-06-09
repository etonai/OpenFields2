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

import combat.*;
import game.*;

public class OpenFields2 extends Application implements GameCallbacks {

    public static double pixelsToFeet(double pixels) {
        return pixels / 7.0;
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
    private Unit selected = null;
    private double offsetX = 0;
    private double offsetY = 0;
    private double zoom = 1.0;
    private boolean paused = true;
    private static boolean debugMode = false;
    private static int stressModifier = -20;
    private final GameClock gameClock = new GameClock();
    private final java.util.PriorityQueue<ScheduledEvent> eventQueue = new java.util.PriorityQueue<>();
    private AudioClip gunshotSound;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        createUnits();
        
        try {
            gunshotSound = new AudioClip(getClass().getResource("/Slap0003.wav").toExternalForm());
        } catch (Exception e) {
            System.out.println("Could not load gunshot sound: " + e.getMessage());
        }
        
        Pane root = new Pane(canvas);
        Scene scene = new Scene(root);

        canvas.setOnMouseClicked(e -> {
            double x = (e.getX() - offsetX) / zoom;
            double y = (e.getY() - offsetY) / zoom;
            boolean clickedOnUnit = false;
            for (Unit u : units) {
                if (u.contains(x, y)) {
                    clickedOnUnit = true;
                    if (e.getButton() == MouseButton.PRIMARY) {
                        selected = u;
                        System.out.println("Selected: " + u.character.name + " (ID: " + u.id + ")");
                    } else if (e.getButton() == MouseButton.SECONDARY && selected != null && u == selected) {
                        if (selected.character.isIncapacitated()) {
                            System.out.println(">>> " + selected.character.name + " is incapacitated and cannot ready weapon.");
                            return;
                        }
                        
                        selected.character.startReadyWeaponSequence(selected, gameClock.getCurrentTick(), eventQueue, selected.getId());
                        System.out.println("READY WEAPON " + selected.character.name + " (ID: " + selected.id + ") - current state: " + selected.character.currentWeaponState.getState());
                    } else if (e.getButton() == MouseButton.SECONDARY && selected != null && u != selected) {
                        if (selected.character.isIncapacitated()) {
                            System.out.println(">>> " + selected.character.name + " is incapacitated and cannot attack.");
                            return;
                        }
                        
                        selected.character.startAttackSequence(selected, u, gameClock.getCurrentTick(), eventQueue, selected.getId(), this);
                        System.out.println("ATTACK " + selected.character.name + " (ID: " + selected.id + ") targets " + u.character.name + " (ID: " + u.id + ") - current state: " + selected.character.currentWeaponState.getState());
                    }
                }
            }
            if (!clickedOnUnit && selected != null && e.getButton() == MouseButton.SECONDARY) {
                if (selected.character.isIncapacitated()) {
                    System.out.println(">>> " + selected.character.name + " is incapacitated and cannot move.");
                    return;
                }
                selected.setTarget(x, y);
                System.out.println("MOVE " + selected.character.name + " to (" + x + ", " + y + ")");
            }
        });

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.UP) offsetY += 20;
            if (e.getCode() == KeyCode.DOWN) offsetY -= 20;
            if (e.getCode() == KeyCode.LEFT) offsetX += 20;
            if (e.getCode() == KeyCode.RIGHT) offsetX -= 20;
            if (e.getCode() == KeyCode.EQUALS || e.getCode() == KeyCode.PLUS) zoom *= 1.1;
            if (e.getCode() == KeyCode.MINUS) zoom /= 1.1;
            if (e.getCode() == KeyCode.SPACE) {
                paused = !paused;
                if (paused) {
                    System.out.println("***********************");
                    System.out.println("*** Game paused at tick " + gameClock.getCurrentTick());
                    System.out.println("***********************");
                } else {
                    System.out.println("***********************");
                    System.out.println("*** Game resumed");
                    System.out.println("***********************");
                }
            }
            if (e.getCode() == KeyCode.D && e.isControlDown()) {
                debugMode = !debugMode;
                System.out.println("***********************");
                System.out.println("*** Debug mode " + (debugMode ? "ENABLED" : "DISABLED"));
                System.out.println("***********************");
            }
            if (e.getCode() == KeyCode.SLASH && e.isShiftDown()) {
                if (selected != null) {
                    System.out.println("***********************");
                    System.out.println("*** CHARACTER STATS ***");
                    System.out.println("***********************");
                    System.out.println("Name: " + selected.character.name);
                    System.out.println("Dexterity: " + selected.character.dexterity + " (modifier: " + statToModifier(selected.character.dexterity) + ")");
                    System.out.println("Strength: " + selected.character.strength + " (modifier: " + statToModifier(selected.character.strength) + ")");
                    System.out.println("Reflexes: " + selected.character.reflexes + " (modifier: " + statToModifier(selected.character.reflexes) + ")");
                    System.out.println("Health: " + selected.character.health);
                    System.out.println("Coolness: " + selected.character.coolness + " (modifier: " + statToModifier(selected.character.coolness) + ")");
                    System.out.println("Handedness: " + selected.character.handedness.getDisplayName());
                    System.out.println("Base Movement Speed: " + selected.character.baseMovementSpeed + " pixels/second");
                    System.out.println("Current Movement: " + selected.character.getCurrentMovementType().getDisplayName() + 
                                     " (" + String.format("%.1f", selected.character.getEffectiveMovementSpeed()) + " pixels/sec)");
                    System.out.println("Current Aiming Speed: " + selected.character.getCurrentAimingSpeed().getDisplayName() + 
                                     " (timing: " + String.format("%.2fx", selected.character.getCurrentAimingSpeed().getTimingMultiplier()) + 
                                     ", accuracy: " + String.format("%+.0f", selected.character.getCurrentAimingSpeed().getAccuracyModifier()) + ")");
                    
                    // Show weapon ready speed
                    double readySpeedMultiplier = selected.character.getWeaponReadySpeedMultiplier();
                    int quickdrawLevel = selected.character.getSkillLevel(combat.Skills.QUICKDRAW);
                    String quickdrawInfo = quickdrawLevel > 0 ? " (Quickdraw " + quickdrawLevel + ")" : "";
                    System.out.println("Weapon Ready Speed: " + String.format("%.2fx", readySpeedMultiplier) + quickdrawInfo + 
                                     " (reflexes: " + String.format("%+d", statToModifier(selected.character.reflexes)) + ")");
                    
                    System.out.println("Incapacitated: " + (selected.character.isIncapacitated() ? "YES" : "NO"));
                    
                    if (selected.character.weapon != null) {
                        System.out.println("--- WEAPON ---");
                        System.out.println("Name: " + selected.character.weapon.name);
                        System.out.println("Type: " + selected.character.weapon.weaponType.getDisplayName());
                        System.out.println("Damage: " + selected.character.weapon.damage);
                        System.out.println("Accuracy: " + selected.character.weapon.weaponAccuracy);
                        System.out.println("Max Range: " + selected.character.weapon.maximumRange + " feet");
                        System.out.println("Velocity: " + selected.character.weapon.velocityFeetPerSecond + " feet/second");
                        System.out.println("Ammunition: " + selected.character.weapon.ammunition);
                        System.out.println("Current State: " + (selected.character.currentWeaponState != null ? selected.character.currentWeaponState.getState() : "None"));
                    } else {
                        System.out.println("--- WEAPON ---");
                        System.out.println("No weapon equipped");
                    }
                    
                    if (!selected.character.getSkills().isEmpty()) {
                        System.out.println("--- SKILLS ---");
                        for (combat.Skill skill : selected.character.getSkills()) {
                            System.out.println(skill.getSkillName() + ": " + skill.getLevel());
                        }
                    } else {
                        System.out.println("--- SKILLS ---");
                        System.out.println("No skills");
                    }
                    
                    if (!selected.character.wounds.isEmpty()) {
                        System.out.println("--- WOUNDS ---");
                        for (combat.Wound wound : selected.character.wounds) {
                            System.out.println(wound.getBodyPart().name().toLowerCase() + ": " + wound.getSeverity().name().toLowerCase());
                        }
                    } else {
                        System.out.println("--- WOUNDS ---");
                        System.out.println("No wounds");
                    }
                    System.out.println("***********************");
                } else {
                    System.out.println("*** No character selected - select a character first ***");
                }
            }
            // Movement type controls - W to increase, S to decrease
            if (e.getCode() == KeyCode.W && selected != null) {
                combat.MovementType previousType = selected.character.getCurrentMovementType();
                selected.character.increaseMovementType();
                combat.MovementType newType = selected.character.getCurrentMovementType();
                
                // Resume movement if stopped and speed was increased
                if (selected.isStopped) {
                    selected.resumeMovement();
                    if (previousType != newType) {
                        System.out.println("*** " + selected.character.getName() + " resumes movement at " + newType.getDisplayName() + 
                                         " (speed: " + String.format("%.1f", selected.character.getEffectiveMovementSpeed()) + " pixels/sec)");
                    } else {
                        System.out.println("*** " + selected.character.getName() + " resumes movement at " + newType.getDisplayName());
                    }
                } else if (previousType != newType) {
                    System.out.println("*** " + selected.character.getName() + " movement increased to " + newType.getDisplayName() + 
                                     " (speed: " + String.format("%.1f", selected.character.getEffectiveMovementSpeed()) + " pixels/sec)");
                } else {
                    System.out.println("*** " + selected.character.getName() + " is already at maximum movement type: " + newType.getDisplayName());
                }
            }
            if (e.getCode() == KeyCode.S && selected != null) {
                combat.MovementType previousType = selected.character.getCurrentMovementType();
                
                // If already at crawling speed and currently moving, stop movement
                if (previousType == combat.MovementType.CRAWL && selected.isMoving()) {
                    selected.stopMovement();
                    System.out.println("*** " + selected.character.getName() + " stops moving");
                } else {
                    // Otherwise, decrease movement type normally
                    selected.character.decreaseMovementType();
                    combat.MovementType newType = selected.character.getCurrentMovementType();
                    if (previousType != newType) {
                        System.out.println("*** " + selected.character.getName() + " movement decreased to " + newType.getDisplayName() + 
                                         " (speed: " + String.format("%.1f", selected.character.getEffectiveMovementSpeed()) + " pixels/sec)");
                    } else {
                        System.out.println("*** " + selected.character.getName() + " is already at minimum movement type: " + newType.getDisplayName());
                    }
                }
            }
            // Aiming speed controls - Q to increase, E to decrease
            if (e.getCode() == KeyCode.Q && selected != null) {
                combat.AimingSpeed previousSpeed = selected.character.getCurrentAimingSpeed();
                selected.character.increaseAimingSpeed();
                combat.AimingSpeed newSpeed = selected.character.getCurrentAimingSpeed();
                if (previousSpeed != newSpeed) {
                    System.out.println("*** " + selected.character.getName() + " aiming speed increased to " + newSpeed.getDisplayName() + 
                                     " (timing: " + String.format("%.2fx", newSpeed.getTimingMultiplier()) + ", accuracy: " + String.format("%+.0f", newSpeed.getAccuracyModifier()) + ")");
                } else {
                    System.out.println("*** " + selected.character.getName() + " is already at maximum aiming speed: " + newSpeed.getDisplayName());
                }
            }
            if (e.getCode() == KeyCode.E && selected != null) {
                combat.AimingSpeed previousSpeed = selected.character.getCurrentAimingSpeed();
                selected.character.decreaseAimingSpeed();
                combat.AimingSpeed newSpeed = selected.character.getCurrentAimingSpeed();
                if (previousSpeed != newSpeed) {
                    System.out.println("*** " + selected.character.getName() + " aiming speed decreased to " + newSpeed.getDisplayName() + 
                                     " (timing: " + String.format("%.2fx", newSpeed.getTimingMultiplier()) + ", accuracy: " + String.format("%+.0f", newSpeed.getAccuracyModifier()) + ")");
                } else {
                    System.out.println("*** " + selected.character.getName() + " is already at minimum aiming speed: " + newSpeed.getDisplayName());
                }
            }
        });

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
            }
        }
        render();
    }
    void createUnits() {

        int nextId = 1;
        combat.Character c1 = new combat.Character("Alice", 100, 11, 75, 65, 90, combat.Handedness.RIGHT_HANDED);
        c1.weapon = createPistol("Colt Peacemaker", 600.0, 7, 6, "/Slap0003.wav", 150.0, 0);
        c1.currentWeaponState = c1.weapon.getInitialState();
        c1.addSkill(new combat.Skill(combat.Skills.PISTOL, 4));
        combat.Character c2 = new combat.Character("Bobby", 75, 20, 60, 45, 70, combat.Handedness.LEFT_HANDED);
        c2.weapon = createSheathedWeapon("Wand of Magic Bolts", 30.0, 8, 20, "/magic.wav", 100.0, 20);
        c2.currentWeaponState = c2.weapon.getInitialState();
        combat.Character c3 = new combat.Character("Chris", 25, 8, 30, 40, 35, combat.Handedness.RIGHT_HANDED);
        c3.weapon = createPistol("Derringer", 600.0, 4, 1, "/Slap0003.wav", 50.0, -10);
        c3.currentWeaponState = c3.weapon.getInitialState();
        combat.Character c4 = new combat.Character("Drake", 50, 14, 85, 55, 80, combat.Handedness.AMBIDEXTROUS);
        c4.weapon = createPistol("Plasma Pistol", 3000.0, 6, 20, "/placeholder_laser.wav", 500.0, 20);
        c4.currentWeaponState = c4.weapon.getInitialState();
        combat.Character c5 = new combat.Character("Ethan", 100, 11, 75, 65, 90, combat.Handedness.LEFT_HANDED);
        c5.weapon = createPistol("Colt Peacemaker", 600.0, 7, 6, "/Slap0003.wav", 150.0, 0);
        c5.currentWeaponState = c5.weapon.getInitialState();
        c5.addSkill(new combat.Skill(Skills.QUICKDRAW, 4));
        units.add(new Unit(c1, 100, 100, Color.RED, nextId++));
        units.add(new Unit(c2, 400, 400, Color.BLUE, nextId++));
        units.add(new Unit(c3, 400, 100, Color.GREEN, nextId++));
        units.add(new Unit(c4, 100, 400, Color.PURPLE, nextId++));
        units.add(new Unit(c5, 600, 100, Color.ORANGE, nextId++));
    }
    
    private Weapon createPistol(String name, double velocity, int damage, int ammunition, String soundFile, double maximumRange, int weaponAccuracy) {
        Weapon weapon = new Weapon(name, velocity, damage, ammunition, soundFile, maximumRange, weaponAccuracy, combat.WeaponType.PISTOL);
        weapon.states = new ArrayList<>();
        weapon.states.add(new WeaponState("holstered", "drawing", 0));
        weapon.states.add(new WeaponState("drawing", "ready", 30));
        weapon.states.add(new WeaponState("ready", "aiming", 15));
        weapon.states.add(new WeaponState("aiming", "firing", 30));
        weapon.states.add(new WeaponState("firing", "recovering", 5));
        weapon.states.add(new WeaponState("recovering", "aiming", 30));
        weapon.initialStateName = "holstered";
        return weapon;
    }
    
    private Weapon createRifle(String name, double velocity, int damage, int ammunition, String soundFile, double maximumRange, int weaponAccuracy) {
        Weapon weapon = new Weapon(name, velocity, damage, ammunition, soundFile, maximumRange, weaponAccuracy, combat.WeaponType.RIFLE);
        weapon.states = new ArrayList<>();
        weapon.states.add(new WeaponState("slung", "unsling", 0));
        weapon.states.add(new WeaponState("unsling", "ready", 90));
        weapon.states.add(new WeaponState("ready", "aiming", 15));
        weapon.states.add(new WeaponState("aiming", "firing", 30));
        weapon.states.add(new WeaponState("firing", "recovering", 5));
        weapon.states.add(new WeaponState("recovering", "aiming", 20));
        weapon.initialStateName = "slung";
        return weapon;
    }
    
    private Weapon createSheathedWeapon(String name, double velocity, int damage, int ammunition, String soundFile, double maximumRange, int weaponAccuracy) {
        Weapon weapon = new Weapon(name, velocity, damage, ammunition, soundFile, maximumRange, weaponAccuracy, combat.WeaponType.OTHER);
        weapon.states = new ArrayList<>();
        weapon.states.add(new WeaponState("sheathed", "unsheathing", 0));
        weapon.states.add(new WeaponState("unsheathing", "ready", 25));
        weapon.states.add(new WeaponState("ready", "aiming", 10));
        weapon.states.add(new WeaponState("aiming", "firing", 25));
        weapon.states.add(new WeaponState("firing", "recovering", 8));
        weapon.states.add(new WeaponState("recovering", "aiming", 20));
        weapon.initialStateName = "sheathed";
        return weapon;
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
                skillName = combat.Skills.PISTOL;
                break;
            case RIFLE:
                skillName = combat.Skills.RIFLE;
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
                skillName = combat.Skills.PISTOL;
                break;
            case RIFLE:
                skillName = combat.Skills.RIFLE;
                break;
            case OTHER:
            default:
                return "(weapon type: " + weaponType.getDisplayName() + ", no skill bonus)";
        }
        
        int skillLevel = shooter.character.getSkillLevel(skillName);
        return "(" + skillName + ": " + skillLevel + ")";
    }

    private static HitResult determineHit(Unit shooter, Unit target, double distanceFeet, double maximumRange, int weaponAccuracy, int weaponDamage) {
        double weaponModifier = weaponAccuracy;
        double rangeModifier = calculateRangeModifier(distanceFeet, maximumRange);
        double movementModifier = calculateMovementModifier(shooter);
        double aimingSpeedModifier = shooter.character.getCurrentAimingSpeed().getAccuracyModifier();
        double targetMovementModifier = calculateTargetMovementModifier(shooter, target);
        double woundModifier = 0.0;
        double stressModifier = Math.min(0, OpenFields2.stressModifier + statToModifier(shooter.character.coolness));
        double skillModifier = calculateSkillModifier(shooter);
        double sizeModifier = 0.0;
        double coverModifier = 0.0;
        double chanceToHit = 50.0 + statToModifier(shooter.character.dexterity) + stressModifier + rangeModifier + weaponModifier + movementModifier + aimingSpeedModifier + targetMovementModifier + woundModifier + skillModifier + sizeModifier + coverModifier;
        
        if (distanceFeet <= maximumRange) {
            chanceToHit = Math.max(chanceToHit, 0.01);
        }
        
        double randomRoll = Math.random() * 100;
        
        if (debugMode) {
            System.out.println("=== HIT CALCULATION DEBUG ===");
            System.out.println("Shooter: " + shooter.character.name + " -> Target: " + target.character.name);
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
            
            System.out.println("Wound modifier: " + woundModifier);
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
            actualDamage = calculateActualDamage(weaponDamage, woundSeverity);
        }
        
        return new HitResult(hit, hitLocation, woundSeverity, actualDamage);
    }
    
    private static combat.BodyPart determineHitLocation(double randomRoll, double chanceToHit) {
        double excellentThreshold = chanceToHit * 0.2;
        double goodThreshold = chanceToHit * 0.7;
        
        if (randomRoll < excellentThreshold) {
            return combat.BodyPart.CHEST;
        } else if (randomRoll < goodThreshold) {
            return Math.random() < 0.5 ? combat.BodyPart.CHEST : combat.BodyPart.ABDOMEN;
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
    
    private static int calculateActualDamage(int weaponDamage, combat.WoundSeverity woundSeverity) {
        switch (woundSeverity) {
            case CRITICAL:
            case SERIOUS:
                return weaponDamage;
            case LIGHT:
                return Math.max(1, Math.round(weaponDamage * 0.4f));
            case SCRATCH:
                return 1;
            default:
                return 0;
        }
    }
    
    public void playWeaponSound(Weapon weapon) {
        try {
            System.out.println("*** Attempting to play sound: " + weapon.soundFile);
            AudioClip sound = new AudioClip(getClass().getResource(weapon.soundFile).toExternalForm());
            System.out.println("*** Sound loaded successfully, playing...");
            sound.play();
        } catch (Exception ex) {
            System.out.println("*** ERROR playing sound: " + ex.getMessage());
        }
    }
    
    public void scheduleProjectileImpact(Unit shooter, Unit target, Weapon weapon, long fireTick, double distanceFeet) {
        long impactTick = fireTick + Math.round(distanceFeet / weapon.velocityFeetPerSecond * 60);
        HitResult hitResult = determineHit(shooter, target, distanceFeet, weapon.maximumRange, weapon.weaponAccuracy, weapon.damage);
        System.out.println("--- Ranged attack impact scheduled at tick " + impactTick + (hitResult.isHit() ? " (will hit)" : " (will miss)"));
        
        eventQueue.add(new ScheduledEvent(impactTick, () -> {
            resolveCombatImpact(shooter, target, weapon, impactTick, hitResult);
        }, ScheduledEvent.WORLD_OWNER));
    }
    
    private void resolveCombatImpact(Unit shooter, Unit target, Weapon weapon, long impactTick, HitResult hitResult) {
        if (hitResult.isHit()) {
            combat.BodyPart hitLocation = hitResult.getHitLocation();
            combat.WoundSeverity woundSeverity = hitResult.getWoundSeverity();
            int actualDamage = hitResult.getActualDamage();
            
            System.out.println(">>> Projectile hit " + target.character.name + " in the " + hitLocation.name().toLowerCase() + " causing a " + woundSeverity.name().toLowerCase() + " wound at tick " + impactTick);
            target.character.health -= actualDamage;
            System.out.println(">>> " + target.character.name + " takes " + actualDamage + " damage. Health now: " + target.character.health);
            
            // Add wound to character's wound list
            target.character.addWound(new combat.Wound(hitLocation, woundSeverity));
            
            // Check for incapacitation
            if (target.character.isIncapacitated()) {
                if (woundSeverity == combat.WoundSeverity.CRITICAL) {
                    System.out.println(">>> " + target.character.name + " is incapacitated by critical wound!");
                } else {
                    System.out.println(">>> " + target.character.name + " is incapacitated!");
                }
                target.character.baseMovementSpeed = 0;
                eventQueue.removeIf(e -> e.getOwnerId() == target.getId());
                System.out.println(">>> Removed all scheduled actions for " + target.character.name);
            }
            
            applyHitHighlight(target, impactTick);
        } else {
            System.out.println(">>> Projectile missed " + target.character.name + " at tick " + impactTick);
        }
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
    

    private void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.save();
        gc.translate(offsetX, offsetY);
        gc.scale(zoom, zoom);
        for (Unit u : units) {
            u.render(gc, u == selected);
        }
        gc.restore();
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

}












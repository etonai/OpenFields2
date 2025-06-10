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

public class OpenFields2 extends Application implements GameCallbacks {

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
    private final SaveGameManager saveGameManager = SaveGameManager.getInstance();
    private final UniversalCharacterRegistry characterRegistry = UniversalCharacterRegistry.getInstance();
    private int nextUnitId = 1;
    private boolean waitingForSaveSlot = false;
    private boolean waitingForLoadSlot = false;
    private static boolean editMode = false;

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
                        System.out.println("Selected: " + u.character.getDisplayName() + " (Unit ID: " + u.id + ")");
                    } else if (e.getButton() == MouseButton.SECONDARY && selected != null && u == selected) {
                        if (editMode) {
                            System.out.println(">>> Combat actions disabled in edit mode");
                            return;
                        }
                        if (selected.character.isIncapacitated()) {
                            System.out.println(">>> " + selected.character.getDisplayName() + " is incapacitated and cannot ready weapon.");
                            return;
                        }
                        
                        selected.character.startReadyWeaponSequence(selected, gameClock.getCurrentTick(), eventQueue, selected.getId());
                        System.out.println("READY WEAPON " + selected.character.getDisplayName() + " (Unit ID: " + selected.id + ") - current state: " + selected.character.currentWeaponState.getState());
                    } else if (e.getButton() == MouseButton.SECONDARY && e.isShiftDown() && selected != null && u != selected) {
                        // Shift+right-click on different unit - toggle persistent attack
                        if (editMode) {
                            System.out.println(">>> Combat actions disabled in edit mode");
                            return;
                        }
                        if (selected.character.isIncapacitated()) {
                            System.out.println(">>> " + selected.character.getDisplayName() + " is incapacitated.");
                            return;
                        }
                        
                        // Toggle persistent attack and set target
                        selected.character.setPersistentAttack(!selected.character.isPersistentAttack());
                        selected.character.currentTarget = u;
                        
                        if (selected.character.isPersistentAttack()) {
                            System.out.println(selected.character.getDisplayName() + " enables persistent attack on " + u.character.getDisplayName());
                            // Start initial attack
                            selected.character.startAttackSequence(selected, u, gameClock.getCurrentTick(), eventQueue, selected.getId(), this);
                        } else {
                            System.out.println(selected.character.getDisplayName() + " disables persistent attack");
                            selected.character.currentTarget = null;
                        }
                        return; // Prevent normal right-click from executing
                    } else if (e.getButton() == MouseButton.SECONDARY && selected != null && u != selected) {
                        if (editMode) {
                            // Show range information in edit mode
                            double dx = u.x - selected.x;
                            double dy = u.y - selected.y;
                            double distancePixels = Math.hypot(dx, dy);
                            double distanceFeet = pixelsToFeet(distancePixels);
                            
                            System.out.println("*** RANGE CHECK ***");
                            System.out.println("Distance from " + selected.character.getDisplayName() + " to " + u.character.getDisplayName() + ": " + 
                                             String.format("%.2f", distanceFeet) + " feet");
                            
                            if (selected.character.weapon != null) {
                                double maxRange = selected.character.weapon.maximumRange;
                                System.out.println("Weapon: " + selected.character.weapon.name + " (max range: " + 
                                                 String.format("%.2f", maxRange) + " feet)");
                                
                                if (distanceFeet <= maxRange) {
                                    System.out.println("Target is WITHIN range");
                                } else {
                                    System.out.println("Target is OUT OF RANGE (exceeds by " + 
                                                     String.format("%.2f", distanceFeet - maxRange) + " feet)");
                                }
                            } else {
                                System.out.println("No weapon equipped");
                            }
                            System.out.println("******************");
                            return;
                        }
                        if (selected.character.isIncapacitated()) {
                            System.out.println(">>> " + selected.character.getDisplayName() + " is incapacitated and cannot attack.");
                            return;
                        }
                        
                        selected.character.startAttackSequence(selected, u, gameClock.getCurrentTick(), eventQueue, selected.getId(), this);
                        System.out.println("ATTACK " + selected.character.getDisplayName() + " (Unit ID: " + selected.id + ") targets " + u.character.getDisplayName() + " (Unit ID: " + u.id + ") - current state: " + selected.character.currentWeaponState.getState());
                    }
                }
            }
            if (!clickedOnUnit && selected != null && e.getButton() == MouseButton.SECONDARY) {
                if (!editMode && selected.character.isIncapacitated()) {
                    System.out.println(">>> " + selected.character.getDisplayName() + " is incapacitated and cannot move.");
                    return;
                }
                
                if (editMode) {
                    // Instant teleport in edit mode
                    selected.x = x;
                    selected.y = y;
                    selected.targetX = x;
                    selected.targetY = y;
                    selected.hasTarget = false;
                    selected.isStopped = false;
                    System.out.println("TELEPORT " + selected.character.getDisplayName() + " to (" + String.format("%.0f", x) + ", " + String.format("%.0f", y) + ")");
                } else {
                    // Normal movement with movement rules
                    selected.setTarget(x, y);
                    System.out.println("MOVE " + selected.character.getDisplayName() + " to (" + String.format("%.0f", x) + ", " + String.format("%.0f", y) + ")");
                }
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
            if (e.getCode() == KeyCode.E && e.isControlDown()) {
                editMode = !editMode;
                System.out.println("***********************");
                System.out.println("*** Edit mode " + (editMode ? "ENABLED" : "DISABLED"));
                if (editMode) {
                    System.out.println("*** Combat disabled, instant movement enabled");
                } else {
                    System.out.println("*** Combat enabled, normal movement rules apply");
                }
                System.out.println("***********************");
            }
            if (e.getCode() == KeyCode.SLASH && e.isShiftDown()) {
                if (selected != null) {
                    System.out.println("***********************");
                    System.out.println("*** CHARACTER STATS ***");
                    System.out.println("***********************");
                    System.out.println("ID: " + selected.character.id);
                    System.out.println("Nickname: " + selected.character.nickname);
                    System.out.println("Faction: " + selected.character.faction);
                    System.out.println("Full Name: " + selected.character.getFullName());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");
                    System.out.println("Birthdate: " + dateFormat.format(selected.character.birthdate));
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
                    int quickdrawLevel = selected.character.getSkillLevel(SkillsManager.QUICKDRAW);
                    String quickdrawInfo = quickdrawLevel > 0 ? " (Quickdraw " + quickdrawLevel + ")" : "";
                    System.out.println("Weapon Ready Speed: " + String.format("%.2fx", readySpeedMultiplier) + quickdrawInfo + 
                                     " (reflexes: " + String.format("%+d", statToModifier(selected.character.reflexes)) + ")");
                    
                    System.out.println("Incapacitated: " + (selected.character.isIncapacitated() ? "YES" : "NO"));
                    System.out.println("Automatic Targeting: " + (selected.character.isUsesAutomaticTargeting() ? "ON" : "OFF"));
                    
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
                            System.out.println(wound.getBodyPart().name().toLowerCase() + ": " + wound.getSeverity().name().toLowerCase() + 
                                             " (from " + wound.getProjectileName() + ", weapon: " + wound.getWeaponId() + ")");
                        }
                    } else {
                        System.out.println("--- WOUNDS ---");
                        System.out.println("No wounds");
                    }
                    
                    // Combat Experience Display
                    System.out.println("--- COMBAT EXPERIENCE ---");
                    System.out.println("Combat Engagements: " + selected.character.getCombatEngagements());
                    System.out.println("Wounds Received: " + selected.character.getWoundsReceived());
                    System.out.println("Wounds Inflicted: " + selected.character.getTotalWoundsInflicted() + " total (" + 
                                     selected.character.getWoundsInflictedByType(combat.WoundSeverity.SCRATCH) + " scratch, " +
                                     selected.character.getWoundsInflictedByType(combat.WoundSeverity.LIGHT) + " light, " +
                                     selected.character.getWoundsInflictedByType(combat.WoundSeverity.SERIOUS) + " serious, " +
                                     selected.character.getWoundsInflictedByType(combat.WoundSeverity.CRITICAL) + " critical)");
                    System.out.println("Attacks: " + selected.character.getAttacksAttempted() + " attempted, " + 
                                     selected.character.getAttacksSuccessful() + " successful (" + 
                                     String.format("%.1f", selected.character.getAccuracyPercentage()) + "% accuracy)");
                    System.out.println("Targets Incapacitated: " + selected.character.getTargetsIncapacitated());
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
                        System.out.println("*** " + selected.character.getDisplayName() + " resumes movement at " + newType.getDisplayName() + 
                                         " (speed: " + String.format("%.1f", selected.character.getEffectiveMovementSpeed()) + " pixels/sec)");
                    } else {
                        System.out.println("*** " + selected.character.getDisplayName() + " resumes movement at " + newType.getDisplayName());
                    }
                } else if (previousType != newType) {
                    System.out.println("*** " + selected.character.getDisplayName() + " movement increased to " + newType.getDisplayName() + 
                                     " (speed: " + String.format("%.1f", selected.character.getEffectiveMovementSpeed()) + " pixels/sec)");
                } else {
                    System.out.println("*** " + selected.character.getDisplayName() + " is already at maximum movement type: " + newType.getDisplayName());
                }
            }
            if (e.getCode() == KeyCode.S && selected != null) {
                combat.MovementType previousType = selected.character.getCurrentMovementType();
                
                // If already at crawling speed and currently moving, stop movement
                if (previousType == combat.MovementType.CRAWL && selected.isMoving()) {
                    selected.stopMovement();
                    System.out.println("*** " + selected.character.getDisplayName() + " stops moving");
                } else {
                    // Otherwise, decrease movement type normally
                    selected.character.decreaseMovementType();
                    combat.MovementType newType = selected.character.getCurrentMovementType();
                    if (previousType != newType) {
                        System.out.println("*** " + selected.character.getDisplayName() + " movement decreased to " + newType.getDisplayName() + 
                                         " (speed: " + String.format("%.1f", selected.character.getEffectiveMovementSpeed()) + " pixels/sec)");
                    } else {
                        System.out.println("*** " + selected.character.getDisplayName() + " is already at minimum movement type: " + newType.getDisplayName());
                    }
                }
            }
            // Aiming speed controls - Q to increase, E to decrease
            if (e.getCode() == KeyCode.Q && selected != null) {
                combat.AimingSpeed previousSpeed = selected.character.getCurrentAimingSpeed();
                selected.character.increaseAimingSpeed();
                combat.AimingSpeed newSpeed = selected.character.getCurrentAimingSpeed();
                if (previousSpeed != newSpeed) {
                    System.out.println("*** " + selected.character.getDisplayName() + " aiming speed increased to " + newSpeed.getDisplayName() + 
                                     " (timing: " + String.format("%.2fx", newSpeed.getTimingMultiplier()) + ", accuracy: " + String.format("%+.0f", newSpeed.getAccuracyModifier()) + ")");
                } else {
                    System.out.println("*** " + selected.character.getDisplayName() + " is already at maximum aiming speed: " + newSpeed.getDisplayName());
                }
            }
            if (e.getCode() == KeyCode.E && !e.isControlDown() && selected != null) {
                combat.AimingSpeed previousSpeed = selected.character.getCurrentAimingSpeed();
                selected.character.decreaseAimingSpeed();
                combat.AimingSpeed newSpeed = selected.character.getCurrentAimingSpeed();
                if (previousSpeed != newSpeed) {
                    System.out.println("*** " + selected.character.getDisplayName() + " aiming speed decreased to " + newSpeed.getDisplayName() + 
                                     " (timing: " + String.format("%.2fx", newSpeed.getTimingMultiplier()) + ", accuracy: " + String.format("%+.0f", newSpeed.getAccuracyModifier()) + ")");
                } else {
                    System.out.println("*** " + selected.character.getDisplayName() + " is already at minimum aiming speed: " + newSpeed.getDisplayName());
                }
            }
            
            // Automatic targeting control - Shift+T
            if (e.getCode() == KeyCode.T && e.isShiftDown()) {
                if (selected != null) {
                    boolean newState = !selected.character.isUsesAutomaticTargeting();
                    selected.character.setUsesAutomaticTargeting(newState);
                    System.out.println("*** " + selected.character.getDisplayName() + " automatic targeting " + 
                                     (newState ? "ENABLED" : "DISABLED"));
                } else {
                    System.out.println("*** No character selected - select a character first ***");
                }
            }
            
            // Save/Load controls
            if (e.getCode() == KeyCode.S && e.isControlDown()) {
                if (!waitingForSaveSlot && !waitingForLoadSlot) {
                    promptForSaveSlot();
                }
            }
            if (e.getCode() == KeyCode.L && e.isControlDown()) {
                if (!waitingForSaveSlot && !waitingForLoadSlot) {
                    promptForLoadSlot();
                }
            }
            
            // Handle number key input for save/load slot selection
            if (waitingForSaveSlot || waitingForLoadSlot) {
                int slotNumber = -1;
                if (e.getCode() == KeyCode.DIGIT1) slotNumber = 1;
                else if (e.getCode() == KeyCode.DIGIT2) slotNumber = 2;
                else if (e.getCode() == KeyCode.DIGIT3) slotNumber = 3;
                else if (e.getCode() == KeyCode.DIGIT4) slotNumber = 4;
                else if (e.getCode() == KeyCode.DIGIT5) slotNumber = 5;
                else if (e.getCode() == KeyCode.DIGIT6) slotNumber = 6;
                else if (e.getCode() == KeyCode.DIGIT7) slotNumber = 7;
                else if (e.getCode() == KeyCode.DIGIT8) slotNumber = 8;
                else if (e.getCode() == KeyCode.DIGIT9) slotNumber = 9;
                else if (e.getCode() == KeyCode.DIGIT0) slotNumber = 0;
                else if (e.getCode() == KeyCode.ESCAPE) {
                    System.out.println("*** Save/Load cancelled ***");
                    waitingForSaveSlot = false;
                    waitingForLoadSlot = false;
                }
                
                if (slotNumber >= 0 && slotNumber <= 9) {
                    if (waitingForSaveSlot) {
                        if (slotNumber >= 1 && slotNumber <= 9) {
                            saveGameToSlot(slotNumber);
                        } else {
                            System.out.println("*** Invalid save slot. Use 1-9 ***");
                        }
                    } else if (waitingForLoadSlot) {
                        if (slotNumber == 0) {
                            System.out.println("*** Load cancelled ***");
                            waitingForLoadSlot = false;
                        } else if (slotNumber >= 1 && slotNumber <= 9) {
                            loadGameFromSlot(slotNumber);
                        } else {
                            System.out.println("*** Invalid load slot. Use 1-9 or 0 to cancel ***");
                        }
                    }
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
                // Update automatic targeting for characters that have it enabled
                u.character.updateAutomaticTargeting(u, gameClock.getCurrentTick(), eventQueue, this);
            }
        }
        render();
    }
    void createUnits() {
        // Load characters from universal registry and assign them weapons for this theme
        combat.Character c1 = characterRegistry.getCharacter(1000);
        if (c1 != null) {
            c1.weapon = WeaponFactory.createWeapon("wpn_colt_peacemaker");
            c1.currentWeaponState = c1.weapon.getInitialState();
            c1.setFaction(1);
            units.add(new Unit(c1, 100, 100, Color.RED, nextUnitId++));
        }
        
        combat.Character c2 = characterRegistry.getCharacter(1001);
        if (c2 != null) {
            c2.weapon = WeaponFactory.createWeapon("wpn_hunting_rifle");
            c2.currentWeaponState = c2.weapon.getInitialState();
            c2.setFaction(2);
            units.add(new Unit(c2, 400, 400, Color.BLUE, nextUnitId++));
        }
        
        combat.Character c3 = characterRegistry.getCharacter(1002);
        if (c3 != null) {
            c3.weapon = WeaponFactory.createWeapon("wpn_derringer");
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
        
        if (debugMode) {
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
            
            // Add wound to character's wound list
            String weaponId = findWeaponId(weapon);
            target.character.addWound(new combat.Wound(hitLocation, woundSeverity, weapon.getProjectileName(), weaponId));
            
            // Check for incapacitation
            boolean wasIncapacitated = target.character.isIncapacitated();
            if (wasIncapacitated) {
                // Track incapacitation caused by this shooter
                shooter.character.targetsIncapacitated++;
                
                if (woundSeverity == combat.WoundSeverity.CRITICAL) {
                    System.out.println(">>> " + target.character.getDisplayName() + " is incapacitated by critical wound!");
                } else {
                    System.out.println(">>> " + target.character.getDisplayName() + " is incapacitated!");
                }
                target.character.baseMovementSpeed = 0;
                eventQueue.removeIf(e -> e.getOwnerId() == target.getId());
                System.out.println(">>> Removed all scheduled actions for " + target.character.getDisplayName());
            }
            
            applyHitHighlight(target, impactTick);
        } else {
            System.out.println(">>> " + weapon.getProjectileName() + " missed " + target.character.getDisplayName() + " at tick " + impactTick);
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
        
        // First pass: Draw all unit circles and basic elements
        for (Unit u : units) {
            u.render(gc, u == selected);
        }
        
        // Second pass: Draw target overlays that need to appear on top
        if (selected != null && selected.character.currentTarget != null) {
            Unit target = selected.character.currentTarget;
            
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

    // Save/Load functionality
    private void promptForSaveSlot() {
        System.out.println("*** SAVE GAME ***");
        List<SaveGameManager.SaveSlotInfo> availableSlots = saveGameManager.listAvailableSlots();
        
        if (!availableSlots.isEmpty()) {
            System.out.println("Existing saves:");
            for (SaveGameManager.SaveSlotInfo slot : availableSlots) {
                System.out.println(slot.slot + ". slot_" + slot.slot + ".json (" + 
                                 slot.getFormattedTimestamp() + ") - " + 
                                 slot.themeId + ", tick " + slot.currentTick);
            }
        } else {
            System.out.println("No existing saves found.");
        }
        
        System.out.println("Enter save slot (1-9): ");
        waitingForSaveSlot = true;
        waitingForLoadSlot = false;
    }
    
    private void promptForLoadSlot() {
        System.out.println("*** LOAD GAME ***");
        List<SaveGameManager.SaveSlotInfo> availableSlots = saveGameManager.listAvailableSlots();
        
        if (availableSlots.isEmpty()) {
            System.out.println("No save files found.");
            return;
        }
        
        System.out.println("Available saves:");
        for (SaveGameManager.SaveSlotInfo slot : availableSlots) {
            System.out.println(slot.slot + ". slot_" + slot.slot + ".json (" + 
                             slot.getFormattedTimestamp() + ") - " + 
                             slot.themeId + ", tick " + slot.currentTick);
        }
        System.out.println("Enter slot number (1-9) or 0 to cancel: ");
        waitingForSaveSlot = false;
        waitingForLoadSlot = true;
    }
    
    private void saveGameToSlot(int slot) {
        try {
            // Pause game during save
            boolean wasPaused = paused;
            paused = true;
            
            SaveData saveData = captureSaveData(slot);
            boolean success = saveGameManager.saveToSlot(slot, saveData);
            
            if (success) {
                System.out.println("*** Game saved successfully to slot " + slot + " ***");
            } else {
                System.out.println("*** Failed to save game to slot " + slot + " ***");
            }
            
            // Restore pause state
            paused = wasPaused;
            
        } catch (Exception e) {
            System.err.println("Error during save: " + e.getMessage());
            e.printStackTrace();
        }
        
        waitingForSaveSlot = false;
    }
    
    private void loadGameFromSlot(int slot) {
        try {
            SaveData saveData = saveGameManager.loadFromSlot(slot);
            if (saveData != null) {
                applySaveData(saveData);
                System.out.println("*** Game loaded successfully from slot " + slot + " ***");
                System.out.println("*** Loaded at tick " + gameClock.getCurrentTick() + " ***");
            } else {
                System.out.println("*** Failed to load game from slot " + slot + " ***");
            }
        } catch (Exception e) {
            System.err.println("Error during load: " + e.getMessage());
            e.printStackTrace();
        }
        
        waitingForLoadSlot = false;
    }
    
    private SaveData captureSaveData(int slot) {
        // Create metadata
        String currentThemeId = ThemeManager.getInstance().getCurrentThemeId();
        SaveMetadata metadata = new SaveMetadata("", "1.0", currentThemeId, slot);
        
        // Create game state
        GameStateData gameState = new GameStateData(
            gameClock.getCurrentTick(),
            paused,
            offsetX,
            offsetY,
            zoom,
            0, // nextCharacterId is managed by universal registry
            nextUnitId
        );
        
        // Serialize units with character ID references and scenario-specific data
        List<UnitData> unitDataList = new ArrayList<>();
        for (Unit unit : units) {
            UnitData unitData = serializeUnitWithCharacterRef(unit, currentThemeId);
            unitDataList.add(unitData);
        }
        
        return new SaveData(metadata, gameState, unitDataList);
    }
    
    private CharacterData serializeCharacter(combat.Character character) {
        CharacterData data = new CharacterData();
        data.id = character.id;
        data.name = character.nickname; // Legacy field for backward compatibility
        data.nickname = character.nickname;
        data.firstName = character.firstName;
        data.lastName = character.lastName;
        data.birthdate = character.birthdate;
        data.themeId = character.themeId;
        data.dexterity = character.dexterity;
        data.currentDexterity = character.currentDexterity;
        data.health = character.health;
        data.currentHealth = character.currentHealth;
        data.coolness = character.coolness;
        data.strength = character.strength;
        data.reflexes = character.reflexes;
        data.handedness = character.handedness;
        data.baseMovementSpeed = character.baseMovementSpeed;
        data.currentMovementType = character.currentMovementType;
        data.currentAimingSpeed = character.currentAimingSpeed;
        
        // Serialize weapon
        if (character.weapon != null) {
            // Find weapon ID from weapon name (we'll need to enhance this)
            data.weaponId = findWeaponId(character.weapon);
        }
        
        // Serialize weapon state
        if (character.currentWeaponState != null) {
            data.currentWeaponState = character.currentWeaponState.getState();
        }
        
        // Serialize skills
        data.skills = new ArrayList<>();
        for (combat.Skill skill : character.skills) {
            data.skills.add(new CharacterData.SkillData(skill.getSkillName(), skill.getLevel()));
        }
        
        // Serialize wounds
        data.wounds = new ArrayList<>();
        for (combat.Wound wound : character.wounds) {
            data.wounds.add(new CharacterData.WoundData(wound.getBodyPart().name(), wound.getSeverity().name()));
        }
        
        return data;
    }
    
    private String findWeaponId(combat.Weapon weapon) {
        // This is a simple approach - in a more complex system, 
        // we might want to store the weapon ID in the weapon object
        if (weapon.name.equals("Colt Peacemaker")) return "wpn_colt_peacemaker";
        if (weapon.name.equals("Hunting Rifle")) return "wpn_hunting_rifle";
        if (weapon.name.equals("Derringer")) return "wpn_derringer";
        if (weapon.name.equals("Plasma Pistol")) return "wpn_plasma_pistol";
        if (weapon.name.equals("Magic Wand")) return "wpn_magic_wand";
        if (weapon.name.equals("Sheathed Sword")) return "wpn_sheathed_sword";
        return "wpn_colt_peacemaker"; // default fallback
    }
    
    private UnitData serializeUnitWithCharacterRef(Unit unit, String themeId) {
        // Find weapon ID from current weapon
        String weaponId = null;
        String currentWeaponState = null;
        if (unit.character.weapon != null) {
            weaponId = findWeaponId(unit.character.weapon);
            if (unit.character.currentWeaponState != null) {
                currentWeaponState = unit.character.currentWeaponState.getState();
            }
        }
        
        return new UnitData(
            unit.id,
            unit.character.id,
            unit.x,
            unit.y,
            unit.targetX,
            unit.targetY,
            unit.hasTarget,
            unit.isStopped,
            colorToString(unit.color),
            colorToString(unit.baseColor),
            unit.isHitHighlighted,
            unit.isFiringHighlighted,
            weaponId,
            currentWeaponState,
            themeId
        );
    }
    
    private UnitData serializeUnit(Unit unit) {
        return new UnitData(
            unit.id,
            unit.character.id,
            unit.x,
            unit.y,
            unit.targetX,
            unit.targetY,
            unit.hasTarget,
            unit.isStopped,
            colorToString(unit.color),
            colorToString(unit.baseColor),
            unit.isHitHighlighted
        );
    }
    
    private String colorToString(Color color) {
        if (color.equals(Color.RED)) return "RED";
        if (color.equals(Color.BLUE)) return "BLUE";
        if (color.equals(Color.GREEN)) return "GREEN";
        if (color.equals(Color.PURPLE)) return "PURPLE";
        if (color.equals(Color.ORANGE)) return "ORANGE";
        if (color.equals(Color.YELLOW)) return "YELLOW";
        return "RED"; // default fallback
    }
    
    private void applySaveData(SaveData saveData) {
        // Clear current game state
        units.clear();
        eventQueue.clear();
        selected = null;
        
        // Restore game state
        gameClock.reset();
        for (long i = 0; i < saveData.gameState.currentTick; i++) {
            gameClock.advanceTick();
        }
        
        paused = saveData.gameState.paused;
        offsetX = saveData.gameState.offsetX;
        offsetY = saveData.gameState.offsetY;
        zoom = saveData.gameState.zoom;
        nextUnitId = saveData.gameState.nextUnitId;
        
        // Handle both new and legacy save formats
        if (saveData.characters != null && !saveData.characters.isEmpty()) {
            // Legacy format - deserialize characters from save data
            for (int i = 0; i < saveData.characters.size() && i < saveData.units.size(); i++) {
                CharacterData charData = saveData.characters.get(i);
                UnitData unitData = saveData.units.get(i);
                
                combat.Character character = deserializeCharacter(charData);
                Unit unit = deserializeUnit(unitData, character);
                units.add(unit);
            }
        } else {
            // New format - load characters from universal registry and apply unit data
            for (UnitData unitData : saveData.units) {
                combat.Character character = characterRegistry.getCharacter(unitData.characterId);
                if (character != null) {
                    // Apply scenario-specific weapon and state
                    if (unitData.weaponId != null && !unitData.weaponId.isEmpty()) {
                        character.weapon = WeaponFactory.createWeapon(unitData.weaponId);
                        if (character.weapon != null && unitData.currentWeaponState != null) {
                            character.currentWeaponState = character.weapon.getStateByName(unitData.currentWeaponState);
                            if (character.currentWeaponState == null) {
                                character.currentWeaponState = character.weapon.getInitialState();
                            }
                        }
                    }
                    
                    Unit unit = deserializeUnitFromCharacterRef(unitData, character);
                    units.add(unit);
                } else {
                    System.err.println("Warning: Character " + unitData.characterId + " not found in universal registry");
                }
            }
        }
        
        System.out.println("*** Restored " + units.size() + " units ***");
    }
    
    private combat.Character deserializeCharacter(CharacterData data) {
        // Handle both old and new save formats
        String nickname = data.nickname != null ? data.nickname : data.name;
        String firstName = data.firstName != null ? data.firstName : "";
        String lastName = data.lastName != null ? data.lastName : "";
        Date birthdate = data.birthdate != null ? data.birthdate : new Date(0); // Default to epoch if null
        
        combat.Character character = new combat.Character(
            data.id, nickname, firstName, lastName, birthdate, data.themeId, data.dexterity, data.health,
            data.coolness, data.strength, data.reflexes, data.handedness
        );
        
        character.currentDexterity = data.currentDexterity;
        character.currentHealth = data.currentHealth;
        character.baseMovementSpeed = data.baseMovementSpeed;
        character.currentMovementType = data.currentMovementType;
        character.currentAimingSpeed = data.currentAimingSpeed;
        
        // Restore weapon
        if (data.weaponId != null && !data.weaponId.isEmpty()) {
            character.weapon = WeaponFactory.createWeapon(data.weaponId);
            if (character.weapon != null && data.currentWeaponState != null) {
                character.currentWeaponState = character.weapon.getStateByName(data.currentWeaponState);
                if (character.currentWeaponState == null) {
                    character.currentWeaponState = character.weapon.getInitialState();
                }
            }
        }
        
        // Restore skills
        character.skills.clear();
        for (CharacterData.SkillData skillData : data.skills) {
            character.addSkill(new combat.Skill(skillData.skillName, skillData.level));
        }
        
        // Restore wounds
        character.wounds.clear();
        for (CharacterData.WoundData woundData : data.wounds) {
            try {
                combat.BodyPart bodyPart = combat.BodyPart.valueOf(woundData.bodyPart);
                combat.WoundSeverity severity = combat.WoundSeverity.valueOf(woundData.severity);
                character.addWound(new combat.Wound(bodyPart, severity));
            } catch (IllegalArgumentException e) {
                System.err.println("Warning: Invalid wound data: " + woundData.bodyPart + "/" + woundData.severity);
            }
        }
        
        return character;
    }
    
    private Unit deserializeUnitFromCharacterRef(UnitData data, combat.Character character) {
        Color color = stringToColor(data.color);
        Color baseColor = stringToColor(data.baseColor);
        
        // Create unit with the base color initially, then set current color
        Unit unit = new Unit(character, data.x, data.y, baseColor, data.id);
        unit.color = color; // Set the current color (which might be different due to highlighting)
        unit.targetX = data.targetX;
        unit.targetY = data.targetY;
        unit.hasTarget = data.hasTarget;
        unit.isStopped = data.isStopped;
        unit.isHitHighlighted = data.isHitHighlighted;
        unit.isFiringHighlighted = data.isFiringHighlighted;
        
        return unit;
    }
    
    private Unit deserializeUnit(UnitData data, combat.Character character) {
        Color color = stringToColor(data.color);
        Color baseColor = stringToColor(data.baseColor);
        
        // Create unit with the base color initially, then set current color
        Unit unit = new Unit(character, data.x, data.y, baseColor, data.id);
        unit.color = color; // Set the current color (which might be different due to highlighting)
        unit.targetX = data.targetX;
        unit.targetY = data.targetY;
        unit.hasTarget = data.hasTarget;
        unit.isStopped = data.isStopped;
        unit.isHitHighlighted = data.isHitHighlighted;
        // Handle backward compatibility for isFiringHighlighted (defaults to false if not present)
        unit.isFiringHighlighted = data.isFiringHighlighted;
        
        return unit;
    }
    
    private Color stringToColor(String colorString) {
        switch (colorString) {
            case "RED": return Color.RED;
            case "BLUE": return Color.BLUE;
            case "GREEN": return Color.GREEN;
            case "PURPLE": return Color.PURPLE;
            case "ORANGE": return Color.ORANGE;
            case "YELLOW": return Color.YELLOW;
            default: return Color.RED;
        }
    }

}












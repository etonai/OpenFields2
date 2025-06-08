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
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class OpenFields2 extends Application {

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
                        if (selected.character.health <= 0) {
                            System.out.println(">>> " + selected.character.name + " is incapacitated and cannot ready weapon.");
                            return;
                        }
                        
                        selected.character.startReadyWeaponSequence(selected, gameClock.getCurrentTick(), eventQueue, selected.getId());
                        System.out.println("READY WEAPON " + selected.character.name + " (ID: " + selected.id + ") - current state: " + selected.character.currentWeaponState.getState());
                    } else if (e.getButton() == MouseButton.SECONDARY && selected != null && u != selected) {
                        if (selected.character.health <= 0) {
                            System.out.println(">>> " + selected.character.name + " is incapacitated and cannot attack.");
                            return;
                        }
                        
                        selected.character.startAttackSequence(selected, u, gameClock.getCurrentTick(), eventQueue, selected.getId(), this);
                        System.out.println("ATTACK " + selected.character.name + " (ID: " + selected.id + ") targets " + u.character.name + " (ID: " + u.id + ") - current state: " + selected.character.currentWeaponState.getState());
                    }
                }
            }
            if (!clickedOnUnit && selected != null && e.getButton() == MouseButton.SECONDARY) {
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
        });

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.0 / 60), e -> run()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        primaryStage.setScene(scene);
        primaryStage.setTitle("Unit Movement Game");
        primaryStage.show();
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
        Character c1 = new Character("Alice", 100, 50, 75);
        c1.weapon = createPistol("Colt Peacemaker", 600.0, 50, 6, "/Slap0003.wav", 150.0, 0);
        c1.currentWeaponState = c1.weapon.getInitialState();
        Character c2 = new Character("Bobby", 75, 50, 60);
        c2.weapon = createRifle("Emek Paintball Gun", 300.0, 1, 160, "/Slap0003.wav", 300.0, -10);
        c2.currentWeaponState = c2.weapon.getInitialState();
        Character c3 = new Character("Chris", 25, 50, 30);
        c3.weapon = createPistol("Nerf Pistol", 30.0, 0, 10, "/Slap0003.wav", 50.0, -20);
        c3.currentWeaponState = c3.weapon.getInitialState();
        Character c4 = new Character("Drake", 50, 50, 85);
        c4.weapon = createPistol("Lasertag Gun", 30000.0, 0, 20, "/placeholder_laser.wav", 500.0, 20);
        c4.currentWeaponState = c4.weapon.getInitialState();
        units.add(new Unit(c1, 100, 100, Color.RED, nextId++));
        units.add(new Unit(c2, 400, 400, Color.BLUE, nextId++));
        units.add(new Unit(c3, 400, 100, Color.GREEN, nextId++));
        units.add(new Unit(c4, 100, 400, Color.PURPLE, nextId++));
    }
    
    private Weapon createPistol(String name, double velocity, int damage, int ammunition, String soundFile, double maximumRange, int weaponAccuracy) {
        Weapon weapon = new Weapon(name, velocity, damage, ammunition, soundFile, maximumRange, weaponAccuracy);
        weapon.states = new ArrayList<>();
        weapon.states.add(new WeaponState("holstered", "drawing", 0));
        weapon.states.add(new WeaponState("drawing", "ready", 30));
        weapon.states.add(new WeaponState("ready", "aiming", 15));
        weapon.states.add(new WeaponState("aiming", "firing", 60));
        weapon.states.add(new WeaponState("firing", "recovering", 5));
        weapon.states.add(new WeaponState("recovering", "aiming", 30));
        weapon.initialStateName = "holstered";
        return weapon;
    }
    
    private Weapon createRifle(String name, double velocity, int damage, int ammunition, String soundFile, double maximumRange, int weaponAccuracy) {
        Weapon weapon = new Weapon(name, velocity, damage, ammunition, soundFile, maximumRange, weaponAccuracy);
        weapon.states = new ArrayList<>();
        weapon.states.add(new WeaponState("slung", "unsling", 0));
        weapon.states.add(new WeaponState("unsling", "ready", 90));
        weapon.states.add(new WeaponState("ready", "aiming", 15));
        weapon.states.add(new WeaponState("aiming", "firing", 60));
        weapon.states.add(new WeaponState("firing", "recovering", 5));
        weapon.states.add(new WeaponState("recovering", "aiming", 20));
        weapon.initialStateName = "slung";
        return weapon;
    }
    
    private Weapon createSheathedWeapon(String name, double velocity, int damage, int ammunition, String soundFile, double maximumRange, int weaponAccuracy) {
        Weapon weapon = new Weapon(name, velocity, damage, ammunition, soundFile, maximumRange, weaponAccuracy);
        weapon.states = new ArrayList<>();
        weapon.states.add(new WeaponState("sheathed", "unsheathing", 0));
        weapon.states.add(new WeaponState("unsheathing", "ready", 25));
        weapon.states.add(new WeaponState("ready", "aiming", 10));
        weapon.states.add(new WeaponState("aiming", "firing", 45));
        weapon.states.add(new WeaponState("firing", "recovering", 8));
        weapon.states.add(new WeaponState("recovering", "aiming", 20));
        weapon.initialStateName = "sheathed";
        return weapon;
    }

    private static HitResult determineHit(Character shooter, Unit target, double distanceFeet, double maximumRange, int weaponAccuracy) {
        double weaponModifier = weaponAccuracy;
        double rangeModifier = calculateRangeModifier(distanceFeet, maximumRange);
        double movementModifier = 0.0;
        double targetMovementModifier = 0.0;
        double woundModifier = 0.0;
        double stressModifier = Math.min(0, OpenFields2.stressModifier + statToModifier(shooter.bravery));
        double skillModifier = 0.0;
        double sizeModifier = 0.0;
        double coverModifier = 0.0;
        double chanceToHit = 50.0 + statToModifier(shooter.dexterity) + stressModifier + rangeModifier + weaponModifier + movementModifier + targetMovementModifier + woundModifier + skillModifier + sizeModifier + coverModifier;
        
        if (distanceFeet <= maximumRange) {
            chanceToHit = Math.max(chanceToHit, 0.01);
        }
        
        double randomRoll = Math.random() * 100;
        
        if (debugMode) {
            System.out.println("=== HIT CALCULATION DEBUG ===");
            System.out.println("Shooter: " + shooter.name + " -> Target: " + target.character.name);
            System.out.println("Base chance: 50.0");
            System.out.println("Dexterity modifier: " + statToModifier(shooter.dexterity) + " (dex: " + shooter.dexterity + ")");
            System.out.println("Stress modifier: " + stressModifier + " (bravery: " + shooter.bravery + ":" + statToModifier(shooter.bravery) + ")");
            System.out.println("Range modifier: " + String.format("%.2f", rangeModifier) + " (distance: " + String.format("%.2f", distanceFeet) + " feet, max: " + String.format("%.2f", maximumRange) + " feet)");
            System.out.println("Weapon modifier: " + weaponModifier + " (accuracy: " + weaponAccuracy + ")");
            System.out.println("Movement modifier: " + movementModifier);
            System.out.println("Target movement modifier: " + targetMovementModifier);
            System.out.println("Wound modifier: " + woundModifier);
            System.out.println("Skill modifier: " + skillModifier);
            System.out.println("Size modifier: " + sizeModifier);
            System.out.println("Cover modifier: " + coverModifier);
            System.out.println("Final chance to hit: " + String.format("%.2f", chanceToHit) + "%");
            System.out.println("Random roll: " + String.format("%.2f", randomRoll));
            System.out.println("Result: " + (randomRoll < chanceToHit ? "HIT" : "MISS"));
            System.out.println("=============================");
        }
        
        boolean hit = randomRoll < chanceToHit;
        BodyPart hitLocation = null;
        
        if (hit) {
            hitLocation = determineHitLocation(randomRoll, chanceToHit);
        }
        
        return new HitResult(hit, hitLocation);
    }
    
    private static BodyPart determineHitLocation(double randomRoll, double chanceToHit) {
        double excellentThreshold = chanceToHit * 0.2;
        double goodThreshold = chanceToHit * 0.7;
        
        if (randomRoll < excellentThreshold) {
            return BodyPart.CHEST;
        } else if (randomRoll < goodThreshold) {
            return Math.random() < 0.5 ? BodyPart.CHEST : BodyPart.ABDOMEN;
        } else {
            return getRandomBodyPart();
        }
    }
    
    private static BodyPart getRandomBodyPart() {
        double roll = Math.random() * 100;
        
        if (roll < 12) return BodyPart.LEFT_ARM;
        else if (roll < 24) return BodyPart.RIGHT_ARM;
        else if (roll < 32) return BodyPart.LEFT_SHOULDER;
        else if (roll < 40) return BodyPart.RIGHT_SHOULDER;
        else if (roll < 50) return BodyPart.HEAD;
        else if (roll < 55) return BodyPart.LEFT_LEG;
        else return BodyPart.RIGHT_LEG;
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
    
    void playWeaponSound(Weapon weapon) {
        try {
            System.out.println("*** Attempting to play sound: " + weapon.soundFile);
            AudioClip sound = new AudioClip(getClass().getResource(weapon.soundFile).toExternalForm());
            System.out.println("*** Sound loaded successfully, playing...");
            sound.play();
        } catch (Exception ex) {
            System.out.println("*** ERROR playing sound: " + ex.getMessage());
        }
    }
    
    void scheduleProjectileImpact(Unit shooter, Unit target, Weapon weapon, long fireTick, double distanceFeet) {
        long impactTick = fireTick + Math.round(distanceFeet / weapon.velocityFeetPerSecond * 60);
        HitResult hitResult = determineHit(shooter.character, target, distanceFeet, weapon.maximumRange, weapon.weaponAccuracy);
        System.out.println("--- Ranged attack impact scheduled at tick " + impactTick + (hitResult.isHit() ? " (will hit)" : " (will miss)"));
        
        eventQueue.add(new ScheduledEvent(impactTick, () -> {
            resolveCombatImpact(shooter, target, weapon, impactTick, hitResult);
        }, ScheduledEvent.WORLD_OWNER));
    }
    
    private void resolveCombatImpact(Unit shooter, Unit target, Weapon weapon, long impactTick, HitResult hitResult) {
        if (hitResult.isHit()) {
            BodyPart hitLocation = hitResult.getHitLocation();
            System.out.println(">>> Projectile hit " + target.character.name + " in the " + hitLocation.name().toLowerCase() + " at tick " + impactTick);
            target.character.health -= weapon.damage;
            System.out.println(">>> " + target.character.name + " takes " + weapon.damage + " damage. Health now: " + target.character.health);
            
            if (target.character.health <= 0) {
                System.out.println(">>> " + target.character.name + " is incapacitated!");
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

class Character {
    String name;
    int dexterity;
    int currentDexterity;
    int health;
    int currentHealth;
    int bravery;
    double baseMovementSpeed;
    Weapon weapon;
    WeaponState currentWeaponState;
    Unit currentTarget;
    int queuedShots = 0;
    List<Skill> skills;
    List<Wound> wounds;

    public Character(String name, int dexterity, int health, int bravery) {
        this.name = name;
        this.dexterity = dexterity;
        this.health = health;
        this.bravery = bravery;
        this.baseMovementSpeed = 42.0;
        this.skills = new ArrayList<>();
        this.wounds = new ArrayList<>();
    }

    public Character(String name, int dexterity, int health, int bravery, Weapon weapon) {
        this.name = name;
        this.dexterity = dexterity;
        this.health = health;
        this.bravery = bravery;
        this.weapon = weapon;
        this.baseMovementSpeed = 42.0;
        this.skills = new ArrayList<>();
        this.wounds = new ArrayList<>();
    }
    
    public Character(String name, int dexterity, int health, int bravery, List<Skill> skills) {
        this.name = name;
        this.dexterity = dexterity;
        this.health = health;
        this.bravery = bravery;
        this.baseMovementSpeed = 42.0;
        this.skills = skills != null ? skills : new ArrayList<>();
        this.wounds = new ArrayList<>();
    }
    
    public Character(String name, int dexterity, int health, int bravery, Weapon weapon, List<Skill> skills) {
        this.name = name;
        this.dexterity = dexterity;
        this.health = health;
        this.bravery = bravery;
        this.weapon = weapon;
        this.baseMovementSpeed = 42.0;
        this.skills = skills != null ? skills : new ArrayList<>();
        this.wounds = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDexterity() {
        return dexterity;
    }

    public void setDexterity(int dexterity) {
        this.dexterity = dexterity;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public double getBaseMovementSpeed() {
        return baseMovementSpeed;
    }

    public void setBaseMovementSpeed(double baseMovementSpeed) {
        this.baseMovementSpeed = baseMovementSpeed;
    }

    public int getBravery() {
        return bravery;
    }

    public void setBravery(int bravery) {
        this.bravery = bravery;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
    }

    public WeaponState getCurrentWeaponState() {
        return currentWeaponState;
    }

    public void setCurrentWeaponState(WeaponState state) {
        this.currentWeaponState = state;
    }
    
    public List<Skill> getSkills() {
        return skills;
    }
    
    public void setSkills(List<Skill> skills) {
        this.skills = skills != null ? skills : new ArrayList<>();
    }
    
    public Skill getSkill(String skillName) {
        for (Skill skill : skills) {
            if (skill.getSkillName().equals(skillName)) {
                return skill;
            }
        }
        return null;
    }
    
    public int getSkillLevel(String skillName) {
        Skill skill = getSkill(skillName);
        return skill != null ? skill.getLevel() : 0;
    }
    
    public void addSkill(Skill skill) {
        skills.add(skill);
    }
    
    public List<Wound> getWounds() {
        return wounds;
    }
    
    public void setWounds(List<Wound> wounds) {
        this.wounds = wounds != null ? wounds : new ArrayList<>();
    }
    
    public void addWound(Wound wound) {
        wounds.add(wound);
    }
    
    public boolean removeWound(Wound wound) {
        return wounds.remove(wound);
    }
    
    public boolean canFire() {
        return currentWeaponState != null && "aiming".equals(currentWeaponState.getState());
    }
    
    public void startAttackSequence(Unit shooter, Unit target, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, OpenFields2 game) {
        if (weapon == null || currentWeaponState == null) return;
        
        if ("aiming".equals(currentWeaponState.getState()) && currentTarget != target) {
            currentWeaponState = weapon.getStateByName("ready");
            System.out.println(name + " weapon state: ready (target changed) at tick " + currentTick);
        }
        
        currentTarget = target;
        
        if (queuedShots > 0) {
            queuedShots++;
            System.out.println(name + " queued shot " + queuedShots + " at " + target.character.name);
            return;
        }
        
        queuedShots = 1;
        scheduleAttackFromCurrentState(shooter, target, currentTick, eventQueue, ownerId, game);
    }
    
    public void startReadyWeaponSequence(Unit unit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        if (weapon == null || currentWeaponState == null) return;
        
        scheduleReadyFromCurrentState(unit, currentTick, eventQueue, ownerId);
    }
    
    private void scheduleAttackFromCurrentState(Unit shooter, Unit target, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, OpenFields2 game) {
        if (weapon == null || currentWeaponState == null) return;
        
        String currentState = currentWeaponState.getState();
        long totalTimeToFire = calculateTimeToFire();
        
        if ("holstered".equals(currentState)) {
            scheduleStateTransition("drawing", currentTick, currentWeaponState.ticks, shooter, target, eventQueue, ownerId, game);
        } else if ("drawing".equals(currentState)) {
            scheduleStateTransition("ready", currentTick, currentWeaponState.ticks, shooter, target, eventQueue, ownerId, game);
        } else if ("slung".equals(currentState)) {
            scheduleStateTransition("unsling", currentTick, currentWeaponState.ticks, shooter, target, eventQueue, ownerId, game);
        } else if ("unsling".equals(currentState)) {
            scheduleStateTransition("ready", currentTick, currentWeaponState.ticks, shooter, target, eventQueue, ownerId, game);
        } else if ("ready".equals(currentState)) {
            scheduleStateTransition("aiming", currentTick, currentWeaponState.ticks, shooter, target, eventQueue, ownerId, game);
        } else if ("aiming".equals(currentState)) {
            scheduleFiring(shooter, target, currentTick + currentWeaponState.ticks, eventQueue, ownerId, game);
        }
    }
    
    private void scheduleStateTransition(String newStateName, long currentTick, long transitionTickLength, Unit shooter, Unit target, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, OpenFields2 game) {
        long transitionTick = currentTick + transitionTickLength;
        eventQueue.add(new ScheduledEvent(transitionTick, () -> {
            currentWeaponState = weapon.getStateByName(newStateName);
            System.out.println(name + " weapon state: " + newStateName + " at tick " + transitionTick);
            scheduleAttackFromCurrentState(shooter, target, transitionTick, eventQueue, ownerId, game);
        }, ownerId));
    }
    
    private void scheduleFiring(Unit shooter, Unit target, long fireTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, OpenFields2 game) {
        eventQueue.add(new ScheduledEvent(fireTick, () -> {
            currentWeaponState = weapon.getStateByName("firing");
            System.out.println(name + " weapon state: firing at tick " + fireTick);
            
            if (weapon.ammunition <= 0) {
                System.out.println("*** " + name + " tries to fire " + weapon.name + " but it's out of ammunition!");
            } else {
                weapon.ammunition--;
                System.out.println("*** " + name + " fires " + weapon.name + " (ammo remaining: " + weapon.ammunition + ")");
                
                game.playWeaponSound(weapon);
                
                double dx = target.x - shooter.x;
                double dy = target.y - shooter.y;
                double distancePixels = Math.hypot(dx, dy);
                double distanceFeet = OpenFields2.pixelsToFeet(distancePixels);
                System.out.println("*** " + name + " shoots at " + target.character.name + " at distance " + String.format("%.2f", distanceFeet) + " feet using " + weapon.name + " at tick " + fireTick);
                
                game.scheduleProjectileImpact(shooter, target, weapon, fireTick, distanceFeet);
            }
            
            WeaponState firingState = weapon.getStateByName("firing");
            eventQueue.add(new ScheduledEvent(fireTick + firingState.ticks, () -> {
                currentWeaponState = weapon.getStateByName("recovering");
                System.out.println(name + " weapon state: recovering at tick " + (fireTick + firingState.ticks));
                
                WeaponState recoveringState = weapon.getStateByName("recovering");
                eventQueue.add(new ScheduledEvent(fireTick + firingState.ticks + recoveringState.ticks, () -> {
                    currentWeaponState = weapon.getStateByName("aiming");
                    System.out.println(name + " weapon state: aiming at tick " + (fireTick + firingState.ticks + recoveringState.ticks));
                    
                    queuedShots--;
                    if (queuedShots > 0 && currentTarget != null) {
                        System.out.println(name + " starting queued shot " + (queuedShots + 1) + " at " + currentTarget.character.name);
                        scheduleFiring(shooter, currentTarget, fireTick + firingState.ticks + recoveringState.ticks + currentWeaponState.ticks, eventQueue, ownerId, game);
                    }
                }, ownerId));
            }, ownerId));
            
        }, ownerId));
    }
    
    private long calculateTimeToFire() {
        String currentState = currentWeaponState.getState();
        long timeToFire = 0;
        
        WeaponState state = currentWeaponState;
        while (state != null && !"aiming".equals(state.getState())) {
            timeToFire += state.ticks;
            state = weapon.getStateByName(state.getAction());
        }
        
        return timeToFire;
    }
    
    
    private void scheduleReadyFromCurrentState(Unit unit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        if (weapon == null || currentWeaponState == null) return;
        
        String currentState = currentWeaponState.getState();
        
        if ("ready".equals(currentState)) {
            System.out.println(name + " weapon is already ready");
            return;
        }
        
        if ("holstered".equals(currentState)) {
            scheduleReadyStateTransition("drawing", currentTick, currentWeaponState.ticks, unit, eventQueue, ownerId);
        } else if ("drawing".equals(currentState)) {
            scheduleReadyStateTransition("ready", currentTick, currentWeaponState.ticks, unit, eventQueue, ownerId);
        } else if ("slung".equals(currentState)) {
            scheduleReadyStateTransition("unsling", currentTick, currentWeaponState.ticks, unit, eventQueue, ownerId);
        } else if ("unsling".equals(currentState)) {
            scheduleReadyStateTransition("ready", currentTick, currentWeaponState.ticks, unit, eventQueue, ownerId);
        } else if ("aiming".equals(currentState) || "firing".equals(currentState) || "recovering".equals(currentState)) {
            WeaponState readyState = weapon.getStateByName("ready");
            eventQueue.add(new ScheduledEvent(currentTick + currentWeaponState.ticks, () -> {
                currentWeaponState = readyState;
                System.out.println(name + " weapon state: ready at tick " + (currentTick + currentWeaponState.ticks));
            }, ownerId));
        }
    }
    
    private void scheduleReadyStateTransition(String newStateName, long currentTick, long transitionTickLength, Unit unit, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        long transitionTick = currentTick + transitionTickLength;
        eventQueue.add(new ScheduledEvent(transitionTick, () -> {
            currentWeaponState = weapon.getStateByName(newStateName);
            System.out.println(name + " weapon state: " + newStateName + " at tick " + transitionTick);
            scheduleReadyFromCurrentState(unit, transitionTick, eventQueue, ownerId);
        }, ownerId));
    }
}

class Weapon {
    String name;
    double velocityFeetPerSecond;
    int damage;
    List<WeaponState> states;
    String initialStateName;
    int ammunition;
    String soundFile;
    double maximumRange;
    int weaponAccuracy;

    public Weapon(String name, double velocityFeetPerSecond, int damage, int ammunition, String soundFile, double maximumRange, int weaponAccuracy) {
        this.name = name;
        this.velocityFeetPerSecond = velocityFeetPerSecond;
        this.damage = damage;
        this.ammunition = ammunition;
        this.soundFile = soundFile;
        this.maximumRange = maximumRange;
        this.weaponAccuracy = weaponAccuracy;
    }

    public String getName() {
        return name;
    }

    public double getVelocityFeetPerSecond() {
        return velocityFeetPerSecond;
    }

    public int getDamage() {
        return damage;
    }

    public WeaponState getStateByName(String name) {
        for (WeaponState s : states) {
            if (s.getState().equals(name)) return s;
        }
        return null;
    }

    public WeaponState getNextState(WeaponState current) {
        return getStateByName(current.getAction());
    }

    public WeaponState getInitialState() {
        return getStateByName(initialStateName);
    }

}

class WeaponState {
    String state;
    String action;
    int ticks;

    public WeaponState(String state, String action, int ticks) {
        this.state = state;
        this.action = action;
        this.ticks = ticks;
    }

    public String getState() {
        return state;
    }
    public String getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "WeaponState{" +
                "state='" + state + '\'' +
                ", action='" + action + '\'' +
                ", ticks=" + ticks +
                '}';
    }
}

class Unit {
    public final int id;
    Character character;
    double x, y;
    double targetX, targetY;
    boolean hasTarget = false;
    Color color;
    final Color baseColor;
    boolean isHitHighlighted = false;
    long lastTickUpdated = -1;

    public Unit(Character character, double x, double y, Color color, int id) {
        this.id = id;
        this.character = character;
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.color = color;
        this.baseColor = color;
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
    }

    public void update(long currentTick) {
        if (currentTick == lastTickUpdated) return;
        lastTickUpdated = currentTick;

        if (!hasTarget) return;

        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 1) {
            hasTarget = false;
            return;
        }

        double moveX = character.baseMovementSpeed / 60.0 * (dx / distance);
        double moveY = character.baseMovementSpeed / 60.0 * (dy / distance);

        if (Math.abs(moveX) > Math.abs(dx)) x = targetX; else x += moveX;
        if (Math.abs(moveY) > Math.abs(dy)) y = targetY; else y += moveY;
    }

    public void render(GraphicsContext gc, boolean isSelected) {
        gc.setFill(color);
        gc.fillOval(x - 10.5, y - 10.5, 21, 21);
        if (isSelected) {
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(2);
            gc.strokeOval(x - 12, y - 12, 24, 24);
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font(12));
            gc.fillText(character.name, x - 15, y - 15);
        }
    }

    public boolean contains(double px, double py) {
        return Math.hypot(px - x, py - y) <= 10.5;
    }
}

class ScheduledEvent implements Comparable<ScheduledEvent> {
    final long tick;
    final Runnable action;

    private final int ownerId; // -1 for world-owned events

    public static final int WORLD_OWNER = -1;

    /*
    public ScheduledEvent(long tick, Runnable action) {
        this.tick = tick;
        this.action = action;
        this.ownerId = WORLD_OWNER;
    }

     */
    public ScheduledEvent(long tick, Runnable action, int ownerId) {
        this.tick = tick;
        this.action = action;
        this.ownerId = ownerId;
    }

    public long getTick() {
        return tick;
    }

    public Runnable getAction() {
        return action;
    }

    public int getOwnerId() {
        return ownerId;
    }

    @Override
    public int compareTo(ScheduledEvent other) {
        return Long.compare(this.tick, other.tick);
    }
}

class GameClock {
    private long currentTick = 0;

    public void advanceTick() {
        currentTick++;
    }

    public long getCurrentTick() {
        return currentTick;
    }

    public void reset() {
        currentTick = 0;
    }
}

class HitResult {
    boolean hit;
    BodyPart hitLocation;
    
    public HitResult(boolean hit, BodyPart hitLocation) {
        this.hit = hit;
        this.hitLocation = hitLocation;
    }
    
    public boolean isHit() {
        return hit;
    }
    
    public BodyPart getHitLocation() {
        return hitLocation;
    }
}

enum BodyPart {
    HEAD,
    CHEST,
    ABDOMEN,
    LEFT_SHOULDER,
    RIGHT_SHOULDER,
    LEFT_ARM,
    RIGHT_ARM,
    LEFT_LEG,
    RIGHT_LEG
}

class Wound {
    BodyPart bodyPart;
    int severity;
    
    public Wound(BodyPart bodyPart, int severity) {
        this.bodyPart = bodyPart;
        this.severity = severity;
    }
    
    public BodyPart getBodyPart() {
        return bodyPart;
    }
    
    public void setBodyPart(BodyPart bodyPart) {
        this.bodyPart = bodyPart;
    }
    
    public int getSeverity() {
        return severity;
    }
    
    public void setSeverity(int severity) {
        this.severity = severity;
    }
}

class Skill {
    String skillName;
    int level;
    
    public Skill(String skillName, int level) {
        this.skillName = skillName;
        this.level = level;
    }
    
    public String getSkillName() {
        return skillName;
    }
    
    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
}

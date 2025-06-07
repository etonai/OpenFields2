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
                        
                        selected.character.startAttackSequence(selected, u, gameClock.getCurrentTick(), eventQueue, selected.getId());
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
        Character c1 = new Character("Alice", 100, 50);
        c1.weapon = createStandardWeapon("Colt Peacemaker", 600.0, 50, 6, "/Slap0003.wav");
        c1.currentWeaponState = c1.weapon.getInitialState();
        Character c2 = new Character("Bobby", 50, 50);
        c2.weapon = createStandardWeapon("Paintball Gun", 300.0, 1, 7, "/Slap0003.wav");
        c2.currentWeaponState = c2.weapon.getInitialState();
        Character c3 = new Character("Chris", 50, 50);
        c3.weapon = createStandardWeapon("Nerf Gun", 30.0, 0, 10, "/Slap0003.wav");
        c3.currentWeaponState = c3.weapon.getInitialState();
        Character c4 = new Character("Drake", 50, 50);
        c4.weapon = createStandardWeapon("Lasertag Gun", 30000.0, 0, 20, "/placeholder_laser.wav");
        c4.currentWeaponState = c4.weapon.getInitialState();
        units.add(new Unit(c1, 100, 100, Color.RED, nextId++));
        units.add(new Unit(c2, 400, 400, Color.BLUE, nextId++));
        units.add(new Unit(c3, 400, 100, Color.GREEN, nextId++));
        units.add(new Unit(c4, 100, 400, Color.PURPLE, nextId++));
    }
    
    private Weapon createStandardWeapon(String name, double velocity, int damage, int ammunition, String soundFile) {
        Weapon weapon = new Weapon(name, velocity, damage, ammunition, soundFile);
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

    private void resolveRangedAttack(Unit shooter, Unit target, long impactTick, long fireTick, boolean hit) {
        System.out.println("--- Ranged attack fired at tick " + fireTick + ", resolved at tick " + impactTick + " (" + (hit ? "hit" : "miss") + ")");
        if (hit) {
            System.out.println(">>> Projectile hit " + target.character.name);
            target.character.health -= shooter.character.weapon.damage;
            System.out.println(">>> " + target.character.name + " takes " + shooter.character.weapon.damage + " damage. Health now: " + target.character.health);
            if (target.character.health <= 0) {
                System.out.println(">>> " + target.character.name + " is incapacitated!");
                target.character.movementSpeed = 0;
                eventQueue.removeIf(event -> event.action.toString().contains("ID: " + target.id));
            }
            if (!target.isHitHighlighted) {
                target.isHitHighlighted = true;
                target.color = Color.YELLOW;
                eventQueue.add(new ScheduledEvent(impactTick + 15,
                        () -> {
                            target.color = target.baseColor;
                            target.isHitHighlighted = false;
                        },
                        ScheduledEvent.WORLD_OWNER
                        ));
            }
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
    int health;
    double movementSpeed;
    Weapon weapon;
    WeaponState currentWeaponState;
    Unit currentTarget;
    int queuedShots = 0;

    public Character(String name, int dexterity, int health) {
        this.name = name;
        this.dexterity = dexterity;
        this.health = health;
        this.movementSpeed = 42.0;
    }

    public Character(String name, int dexterity, int health, Weapon weapon) {
        this.name = name;
        this.dexterity = dexterity;
        this.health = health;
        this.weapon = weapon;
        this.movementSpeed = 42.0;
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

    public double getMovementSpeed() {
        return movementSpeed;
    }

    public void setMovementSpeed(double movementSpeed) {
        this.movementSpeed = movementSpeed;
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
    
    public boolean canFire() {
        return currentWeaponState != null && "aiming".equals(currentWeaponState.getState());
    }
    
    public void startAttackSequence(Unit shooter, Unit target, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
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
        scheduleAttackFromCurrentState(shooter, target, currentTick, eventQueue, ownerId);
    }
    
    public void startReadyWeaponSequence(Unit unit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        if (weapon == null || currentWeaponState == null) return;
        
        scheduleReadyFromCurrentState(unit, currentTick, eventQueue, ownerId);
    }
    
    private void scheduleAttackFromCurrentState(Unit shooter, Unit target, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        if (weapon == null || currentWeaponState == null) return;
        
        String currentState = currentWeaponState.getState();
        long totalTimeToFire = calculateTimeToFire();
        
        if ("holstered".equals(currentState)) {
            scheduleStateTransition("drawing", currentTick, currentWeaponState.ticks, shooter, target, eventQueue, ownerId);
        } else if ("drawing".equals(currentState)) {
            scheduleStateTransition("ready", currentTick, currentWeaponState.ticks, shooter, target, eventQueue, ownerId);
        } else if ("ready".equals(currentState)) {
            scheduleStateTransition("aiming", currentTick, currentWeaponState.ticks, shooter, target, eventQueue, ownerId);
        } else if ("aiming".equals(currentState)) {
            scheduleFiring(shooter, target, currentTick + currentWeaponState.ticks, eventQueue, ownerId);
        }
    }
    
    private void scheduleStateTransition(String newStateName, long currentTick, long transitionTickLength, Unit shooter, Unit target, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        long transitionTick = currentTick + transitionTickLength;
        eventQueue.add(new ScheduledEvent(transitionTick, () -> {
            currentWeaponState = weapon.getStateByName(newStateName);
            System.out.println(name + " weapon state: " + newStateName + " at tick " + transitionTick);
            scheduleAttackFromCurrentState(shooter, target, transitionTick, eventQueue, ownerId);
        }, ownerId));
    }
    
    private void scheduleFiring(Unit shooter, Unit target, long fireTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        eventQueue.add(new ScheduledEvent(fireTick, () -> {
            currentWeaponState = weapon.getStateByName("firing");
            System.out.println(name + " weapon state: firing at tick " + fireTick);
            
            if (weapon.ammunition <= 0) {
                System.out.println("*** " + name + " tries to fire " + weapon.name + " but it's out of ammunition!");
            } else {
                weapon.ammunition--;
                System.out.println("*** " + name + " fires " + weapon.name + " (ammo remaining: " + weapon.ammunition + ")");
                
                // Play weapon sound
                try {
                    AudioClip sound = new AudioClip(getClass().getResource(weapon.soundFile).toExternalForm());
                    sound.play();
                } catch (Exception ex) {
                    // Sound file not found or couldn't play - continue silently
                }
                
                double dx = target.x - shooter.x;
                double dy = target.y - shooter.y;
                double distancePixels = Math.hypot(dx, dy);
                double distanceFeet = OpenFields2.pixelsToFeet(distancePixels);
                System.out.println("*** " + name + " shoots at " + target.character.name + " at distance " + String.format("%.2f", distanceFeet) + " feet using " + weapon.name + " at tick " + fireTick);
                
                long impactTick = fireTick + Math.round(distanceFeet / weapon.velocityFeetPerSecond * 60);
                boolean willHit = Math.random() * 100 < dexterity;
                System.out.println("--- Ranged attack impact scheduled at tick " + impactTick + (willHit ? " (will hit)" : " (will miss)"));
                
                eventQueue.add(new ScheduledEvent(impactTick, () -> {
                if (willHit) {
                    System.out.println(">>> Projectile hit " + target.character.name + " at tick " + impactTick);
                    target.character.health -= weapon.damage;
                    System.out.println(">>> " + target.character.name + " takes " + weapon.damage + " damage. Health now: " + target.character.health);
                    if (target.character.health <= 0) {
                        System.out.println(">>> " + target.character.name + " is incapacitated!");
                        target.character.movementSpeed = 0;
                        eventQueue.removeIf(e -> e.getOwnerId() == target.getId());
                        System.out.println(">>> Removed all scheduled actions for " + target.character.name);
                    }
                    if (!target.isHitHighlighted) {
                        target.isHitHighlighted = true;
                        target.color = Color.YELLOW;
                        eventQueue.add(new ScheduledEvent(impactTick + 15, () -> {
                            target.color = target.baseColor;
                            target.isHitHighlighted = false;
                        }, ScheduledEvent.WORLD_OWNER));
                    }
                } else {
                    System.out.println(">>> Projectile missed " + target.character.name + " at tick " + impactTick);
                }
            }, ScheduledEvent.WORLD_OWNER));
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
                        scheduleFiring(shooter, currentTarget, fireTick + firingState.ticks + recoveringState.ticks + currentWeaponState.ticks, eventQueue, ownerId);
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

    public Weapon(String name, double velocityFeetPerSecond, int damage, int ammunition, String soundFile) {
        this.name = name;
        this.velocityFeetPerSecond = velocityFeetPerSecond;
        this.damage = damage;
        this.ammunition = ammunition;
        this.soundFile = soundFile;
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

        double moveX = character.movementSpeed / 60.0 * (dx / distance);
        double moveY = character.movementSpeed / 60.0 * (dy / distance);

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

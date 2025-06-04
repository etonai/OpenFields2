import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class UnitMovementGame extends Application {

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
    private final PriorityQueue<ScheduledEvent> eventQueue = new PriorityQueue<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        createUnits();
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
                    } else if (e.getButton() == MouseButton.SECONDARY && selected != null && u != selected) {
                        if (selected.character.health <= 0) {
                            System.out.println(">>> " + selected.character.name + " is incapacitated and cannot attack.");
                            return;
                        }
                        long executeAt = gameClock.getCurrentTick() + 60;
                        final Unit shooter = selected;
                        final Unit target = u;
                        eventQueue.add(new ScheduledEvent(executeAt, () -> {
                            double dx = target.x - shooter.x;
                            double dy = target.y - shooter.y;
                            double distancePixels = Math.hypot(dx, dy);
                            double distanceFeet = UnitMovementGame.pixelsToFeet(distancePixels);
                            System.out.println("*** " + shooter.character.name + " (ID: " + shooter.id + ") shoots at " + target.character.name + " (ID: " + target.id + ") at distance " + String.format("%.2f", distanceFeet) + " feet (executed at tick " + executeAt + ") using " + shooter.character.weapon.name);

                            long impactTick = executeAt + Math.round(distanceFeet / shooter.character.weapon.velocityFeetPerSecond * 60);
                            boolean willHit = Math.random() * 100 < shooter.character.dexterity;
                            System.out.println("--- Ranged attack impact scheduled at tick " + impactTick + (willHit ? " (will hit)" : " (will miss)"));
                            final boolean finalWillHit = willHit;
                            final long fireTick = gameClock.getCurrentTick();
                            eventQueue.add(new ScheduledEvent(impactTick, () -> {
                                resolveRangedAttack(shooter, target, impactTick, fireTick, finalWillHit);
                            }));
                        }));
                        System.out.println("DIRECT " + selected.character.name + " (ID: " + selected.id + ") to shoot at " + u.character.name + " (ID: " + u.id + ") (executes at tick " + executeAt + ")");
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

    private void createUnits() {
        int nextId = 1;

        Character c1 = new Character("Alice", 99, 50);
        List<WeaponState> derringerStates = new ArrayList<>();
        derringerStates.add(new WeaponState("Holstered", "Draw", 60));
        derringerStates.add(new WeaponState("Ready", "Aim", 60));
        derringerStates.add(new WeaponState("Aimed", "Fire", 5));
        Weapon derringer = new Weapon("Derringer", 600.0, 50, derringerStates, "Ready");
        c1.weapon = derringer;

        Character c2 = new Character("Bobby", 25, 50);
        List<WeaponState> paintballStates = new ArrayList<>();
        paintballStates.add(new WeaponState("Holstered", "Draw", 60));
        paintballStates.add(new WeaponState("Ready", "Aim", 60));
        paintballStates.add(new WeaponState("Aimed", "Fire", 5));
        Weapon paintballGun = new Weapon("Paintball Gun", 30.0, 0, paintballStates);
        c2.weapon = paintballGun;

        units.add(new Unit(c1, 100, 100, Color.RED, nextId++));
        units.add(new Unit(c2, 300, 300, Color.BLUE, nextId++));
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
                eventQueue.add(new ScheduledEvent(impactTick + 15, () -> {
                    target.color = target.baseColor;
                    target.isHitHighlighted = false;
                    Platform.runLater(this::render);
                }));
            }
            Platform.runLater(() -> {
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.save();
                gc.translate(offsetX, offsetY);
                gc.scale(zoom, zoom);
                gc.setFill(Color.BLACK);
                gc.fillOval(target.x - 14, target.y - 14, 28, 28);
                gc.restore();
            });
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
}

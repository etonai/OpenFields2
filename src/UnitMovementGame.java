import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class UnitMovementGame extends Application {
    Canvas canvas = new Canvas(800, 600);
    boolean debugMode = false;
    GraphicsContext gc = canvas.getGraphicsContext2D();
    List<Unit> units = new ArrayList<>();
    Unit selected = null;
    double zoom = 1.0;
    double offsetX = 0;
    double offsetY = 0;
    GameClock gameClock = new GameClock();
    PriorityQueue<ScheduledEvent> eventQueue = new PriorityQueue<>();

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
                            System.out.println("DIRECT " + shooter.character.name + " (ID: " + shooter.id + ") to shoot at " + target.character.name + " (ID: " + target.id + ") (executes at tick " + executeAt + ")");
                            shooter.character.weapon.resolveRangedAttack(shooter, target, gameClock, eventQueue);
                        }, shooter.id));
                    }
                }
            }
            if (!clickedOnUnit && selected != null && e.getButton() == MouseButton.SECONDARY) {
                selected.setTarget(x, y);
                System.out.println("MOVE " + selected.character.name + " to (" + x + ", " + y + ")");
            }
        });

        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case D:
                    debugMode = !debugMode;
                    System.out.println("Debug mode: " + (debugMode ? "ON" : "OFF"));
                    break;
                case EQUALS:
                    zoom *= 1.1;
                    break;
                case MINUS:
                    zoom /= 1.1;
                    break;
                case UP:
                    offsetY += 10;
                    break;
                case DOWN:
                    offsetY -= 10;
                    break;
                case LEFT:
                    offsetX += 10;
                    break;
                case RIGHT:
                    offsetX -= 10;
                    break;
                case SPACE:
                    gameClock.togglePause();
                    System.out.println("Game " + (gameClock.isPaused() ? "paused" : "resumed"));
                    break;
            }
            render();
        });

        primaryStage.setTitle("Unit Movement Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        new Thread(() -> {
            while (true) {
                if (!gameClock.isPaused()) {
                    gameClock.tick();
                    while (!eventQueue.isEmpty() && eventQueue.peek().getTick() <= gameClock.getCurrentTick()) {
                        ScheduledEvent event = eventQueue.poll();
                        event.getAction().run();
                    }
                    for (Unit u : units) {
                        u.update(gameClock.getCurrentTick());
                    }
                    render();
                }
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    private void createUnits() {
        List<WeaponState> gunStates = List.of(
                new WeaponState("Holstered", "Draw", 60),
                new WeaponState("Ready", "Aim", 60),
                new WeaponState("Aimed", "Fire", 5)
        );

        Weapon gun1 = new Weapon("Revolver", 900, 15, gunStates, "Holstered");
        Weapon gun2 = new Weapon("Pistol", 800, 10, gunStates, "Holstered");

        Character alice = new Character("Alice", 100, 25, gun1);
        Character bobby = new Character("Bobby", 100, 20, gun2);

        Unit unit1 = new Unit(alice, 100, 100, Color.RED, 1);
        Unit unit2 = new Unit(bobby, 300, 300, Color.BLUE, 2);

        units.add(unit1);
        units.add(unit2);
    }

    private void render() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.save();
        gc.translate(offsetX, offsetY);
        gc.scale(zoom, zoom);
        for (Unit u : units) {
            u.render(gc, u == selected);
        }
        if (debugMode) {
            long currentTick = gameClock.getCurrentTick();
            gc.setFill(Color.BLACK);
            gc.fillText("Tick: " + currentTick, 10, 20);
            for (Unit u : units) {
                gc.fillText("ID: " + u.id, u.x, u.y - 10);
            }
        }
        gc.restore();
    }

    public static double pixelsToFeet(double pixels) {
        return pixels / 10.0;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

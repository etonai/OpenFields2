import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class UnitMovementGame extends Application {

    static final int WIDTH = 800;
    static final int HEIGHT = 600;
    static final double MOVE_SPEED = 100.0; // pixels per second

    private final Canvas canvas = new Canvas(WIDTH, HEIGHT);
    private final List<Unit> units = new ArrayList<>();
    private Unit selected = null;
    private double offsetX = 0;
    private double offsetY = 0;
    private double zoom = 1.0;
    private boolean paused = true;
    private final GameClock gameClock = new GameClock();
    private final java.util.PriorityQueue<ScheduledAction> actionQueue = new java.util.PriorityQueue<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Unit u1 = new Unit("Alice", 100, 100, Color.RED);
        Unit u2 = new Unit("Bobby", 300, 300, Color.BLUE);
        units.add(u1);
        units.add(u2);

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
                        System.out.println("Selected: " + u.name);
                    } else if (e.getButton() == MouseButton.SECONDARY && selected != null && u != selected) {
                        long executeAt = gameClock.getCurrentTick() + 60;
                        actionQueue.add(new ScheduledAction(executeAt, () -> {
                            System.out.println("*** " + selected.name + " shoots at " + u.name + " (executed at tick " + executeAt + ")");
                        }));
                        System.out.println("DIRECT " + selected.name + " to shoot at " + u.name + " (executes at tick " + executeAt + ")");
                    }
                }
            }
            if (!clickedOnUnit && selected != null && e.getButton() == MouseButton.SECONDARY) {
                selected.setTarget(x, y);
                System.out.println("MOVE " + selected.name + " to (" + x + ", " + y + ")");
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
            while (!actionQueue.isEmpty() && actionQueue.peek().tick <= gameClock.getCurrentTick()) {
                actionQueue.poll().action.run();
            }
            for (Unit u : units) {
                u.update(gameClock.getCurrentTick());
            }
        }
        render();
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

class Unit {
    String name;
    double x, y;
    double targetX, targetY;
    boolean hasTarget = false;
    Color color;
    long lastTickUpdated = -1;

    public Unit(String name, double x, double y, Color color) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.color = color;
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

        double moveX = UnitMovementGame.MOVE_SPEED / 60.0 * (dx / distance);
        double moveY = UnitMovementGame.MOVE_SPEED / 60.0 * (dy / distance);

        if (Math.abs(moveX) > Math.abs(dx)) x = targetX; else x += moveX;
        if (Math.abs(moveY) > Math.abs(dy)) y = targetY; else y += moveY;
    }

    public void render(GraphicsContext gc, boolean isSelected) {
        gc.setFill(color);
        gc.fillOval(x - 10, y - 10, 20, 20);
        if (isSelected) {
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(2);
            gc.strokeOval(x - 12, y - 12, 24, 24);
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font(12));
            gc.fillText(name, x - 15, y - 15);
        }
    }

    public boolean contains(double px, double py) {
        return Math.hypot(px - x, py - y) <= 10;
    }
}

class ScheduledAction implements Comparable<ScheduledAction> {
    final long tick;
    final Runnable action;

    public ScheduledAction(long tick, Runnable action) {
        this.tick = tick;
        this.action = action;
    }

    @Override
    public int compareTo(ScheduledAction other) {
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

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class UnitMovementGame extends Application {

    public static class Unit {
        private String name;
        private double x, y;
        private double targetX, targetY;
        private final double speed = 2.0;

        public Unit(String name, double x, double y) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.targetX = x;
            this.targetY = y;
        }

        public void setTarget(double x, double y) {
            this.targetX = x;
            this.targetY = y;
        }

        public void update() {
            double dx = targetX - x;
            double dy = targetY - y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist > speed) {
                x += dx / dist * speed;
                y += dy / dist * speed;
            } else {
                x = targetX;
                y = targetY;
            }
        }

        public boolean contains(double px, double py) {
            return Math.hypot(px - x, py - y) <= 10;
        }

        public double getX() { return x; }
        public double getY() { return y; }
        public String getName() { return name; }
    }

    public static class World {
        private final List<Unit> units = new ArrayList<>();

        public void addUnit(Unit unit) {
            units.add(unit);
        }

        public List<Unit> getUnits() {
            return units;
        }

        public void update() {
            for (Unit unit : units) {
                unit.update();
            }
        }
    }

    public static class Renderer {
        public void render(GraphicsContext gc, World world, double zoom, double offsetX, double offsetY) {
            // Reset and clear canvas before scaling
            gc.setTransform(1, 0, 0, 1, 0, 0);
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

            gc.save();
            gc.scale(zoom, zoom);
            // apply camera offset after zoom
            gc.translate(offsetX, offsetY);

            gc.setFill(Color.BLUE);
            for (Unit unit : world.getUnits()) {
                gc.fillOval(unit.getX() - 10, unit.getY() - 10, 20, 20);
            }

            if (UnitMovementGame.selectedUnit != null) {
                Unit unit = UnitMovementGame.selectedUnit;
                gc.setStroke(Color.YELLOW);
                gc.strokeOval(unit.getX() - 12, unit.getY() - 12, 24, 24);

                gc.setFill(Color.WHITE);
                gc.setFont(Font.font(14));
                gc.fillText(unit.getName(), unit.getX() - 20, unit.getY() - 15);
            }

            gc.restore();
        }
    }

    private final World world = new World();
    private final Renderer renderer = new Renderer();
    private double zoom = 1.0;
    private static Unit selectedUnit = null;
    private boolean paused = false;
    private double offsetX = 0;
    private double offsetY = 0;

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Unit unit1 = new Unit("Alice", 100, 100);
        Unit unit2 = new Unit("Bob", 300, 200);
        world.addUnit(unit1);
        world.addUnit(unit2);

        Scene scene = new Scene(new StackPane(canvas));
        scene.setOnMousePressed(e -> {
            double mx = e.getX() / zoom - offsetX;
            double my = e.getY() / zoom - offsetY;

            if (e.getButton() == MouseButton.PRIMARY) {
                selectedUnit = null;
                for (Unit unit : world.getUnits()) {
                    if (unit.contains(mx, my)) {
                        selectedUnit = unit;
                        System.out.println("Selected unit: " + unit.getName());
                        break;
                    }
                }
            } else if (e.getButton() == MouseButton.SECONDARY) {
                System.out.println("Right-click at: (" + mx + ", " + my + ")");
                if (selectedUnit != null) {
                    for (Unit target : world.getUnits()) {
                        if (target != selectedUnit && target.contains(mx, my)) {
                            System.out.println(selectedUnit.getName() + " shoots at " + target.getName());
                            return;
                        }
                    }
                    System.out.println(selectedUnit.getName() + " is moving to: (" + mx + ", " + my + ")");
                    selectedUnit.setTarget(mx, my);
                }
            }
        });

        new AnimationTimer() {
            public void handle(long now) {
                if (!paused) {
                    world.update();
                }
                renderer.render(gc, world, zoom, offsetX, offsetY);
            }
        }.start();

        scene.setOnKeyPressed(e -> {
            double panStep = 20 / zoom;
            switch (e.getCode()) {
                case PLUS, EQUALS -> {
                    zoom *= 1.1;
                    System.out.println("Zoom in: " + zoom);
                }
                case MINUS -> {
                    zoom /= 1.1;
                    System.out.println("Zoom out: " + zoom);
                }
                case UP -> offsetY += panStep;
                case DOWN -> offsetY -= panStep;
                case LEFT -> offsetX += panStep;
                case RIGHT -> offsetX -= panStep;
                case SPACE -> {
                    paused = !paused;
                    System.out.println(paused ? "Game paused" : "Game resumed");
                }
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Unit Movement Game");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

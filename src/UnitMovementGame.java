import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class UnitMovementGame extends Application {

    public static class Unit {
        private double x, y;
        private double targetX, targetY;
        private final double speed = 2.0;

        public Unit(double x, double y) {
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
        public void render(GraphicsContext gc, World world) {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

            gc.setFill(Color.BLUE);
            for (Unit unit : world.getUnits()) {
                gc.fillOval(unit.getX() - 10, unit.getY() - 10, 20, 20);
            }

            if (UnitMovementGame.selectedUnit != null) {
                gc.setStroke(Color.YELLOW);
                gc.strokeOval(UnitMovementGame.selectedUnit.getX() - 12, UnitMovementGame.selectedUnit.getY() - 12, 24, 24);
            }
        }
    }

    private final World world = new World();
    private final Renderer renderer = new Renderer();
    private static Unit selectedUnit = null;

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Unit unit1 = new Unit(100, 100);
        Unit unit2 = new Unit(300, 200);
        world.addUnit(unit1);
        world.addUnit(unit2);

        Scene scene = new Scene(new StackPane(canvas));
        scene.setOnMousePressed(e -> {
            double mx = e.getX();
            double my = e.getY();

            if (e.getButton() == MouseButton.PRIMARY) {
                selectedUnit = null;
                for (Unit unit : world.getUnits()) {
                    if (unit.contains(mx, my)) {
                        selectedUnit = unit;
                        break;
                    }
                }
            } else if (e.getButton() == MouseButton.SECONDARY) {
                if (selectedUnit != null) {
                    selectedUnit.setTarget(mx, my);
                }
            }
        });

        new AnimationTimer() {
            public void handle(long now) {
                world.update();
                renderer.render(gc, world);
            }
        }.start();

        primaryStage.setScene(scene);
        primaryStage.setTitle("Unit Movement Game");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

import javafx.scene.input.MouseEvent;
import java.util.function.BiConsumer;

public class MouseController {
    private double lastX, lastY;

    public void handleClick(MouseEvent event, BiConsumer<Double, Double> onClick) {
        lastX = event.getX();
        lastY = event.getY();
        onClick.accept(lastX, lastY);
    }

    public double getLastX() { return lastX; }
    public double getLastY() { return lastY; }
}

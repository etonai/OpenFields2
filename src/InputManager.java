import java.util.HashSet;
import java.util.Set;
import javafx.scene.input.KeyCode;

public class InputManager {
    private final Set<KeyCode> activeKeys = new HashSet<>();

    public void keyPressed(KeyCode key) {
        activeKeys.add(key);
    }

    public void keyReleased(KeyCode key) {
        activeKeys.remove(key);
    }

    public boolean isPressed(KeyCode key) {
        return activeKeys.contains(key);
    }

    public void clear() {
        activeKeys.clear();
    }
}

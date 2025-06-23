package platform.impl.javafx;

import platform.api.InputProvider;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import java.util.*;

/**
 * JavaFX implementation of the InputProvider interface.
 * Wraps JavaFX input handling for the platform abstraction.
 */
public class JavaFXInputProvider implements InputProvider {
    private final Scene scene;
    private final Set<Key> pressedKeys;
    private final Set<Key> justPressedKeys;
    private final List<ClickHandler> clickHandlers;
    private final List<KeyHandler> keyHandlers;
    private MouseState currentMouseState;
    
    // Key mapping from JavaFX to platform keys
    private static final Map<KeyCode, Key> KEY_MAP = createKeyMap();
    
    public JavaFXInputProvider(Scene scene) {
        this.scene = scene;
        this.pressedKeys = new HashSet<>();
        this.justPressedKeys = new HashSet<>();
        this.clickHandlers = new ArrayList<>();
        this.keyHandlers = new ArrayList<>();
        this.currentMouseState = new MouseState(0, 0, false, false, false);
        
        setupEventHandlers();
    }
    
    private void setupEventHandlers() {
        // Mouse events
        scene.setOnMousePressed(this::handleMousePressed);
        scene.setOnMouseReleased(this::handleMouseReleased);
        scene.setOnMouseMoved(this::handleMouseMoved);
        scene.setOnMouseDragged(this::handleMouseDragged);
        
        // Key events
        scene.setOnKeyPressed(this::handleKeyPressed);
        scene.setOnKeyReleased(this::handleKeyReleased);
    }
    
    @Override
    public void pollEvents() {
        // Clear just pressed keys from previous frame
        justPressedKeys.clear();
    }
    
    @Override
    public boolean isKeyPressed(Key key) {
        return pressedKeys.contains(key);
    }
    
    @Override
    public boolean isKeyJustPressed(Key key) {
        return justPressedKeys.contains(key);
    }
    
    @Override
    public Set<Key> getPressedKeys() {
        return new HashSet<>(pressedKeys);
    }
    
    @Override
    public MouseState getMouseState() {
        return currentMouseState;
    }
    
    @Override
    public void registerClickHandler(ClickHandler handler) {
        clickHandlers.add(handler);
    }
    
    @Override
    public void unregisterClickHandler(ClickHandler handler) {
        clickHandlers.remove(handler);
    }
    
    @Override
    public void registerKeyHandler(KeyHandler handler) {
        keyHandlers.add(handler);
    }
    
    @Override
    public void unregisterKeyHandler(KeyHandler handler) {
        keyHandlers.remove(handler);
    }
    
    private void handleMousePressed(MouseEvent event) {
        updateMouseState(event);
        
        MouseButton button = mapMouseButton(event.getButton());
        for (ClickHandler handler : new ArrayList<>(clickHandlers)) {
            handler.onClick(event.getX(), event.getY(), button);
        }
    }
    
    private void handleMouseReleased(MouseEvent event) {
        updateMouseState(event);
    }
    
    private void handleMouseMoved(MouseEvent event) {
        updateMouseState(event);
    }
    
    private void handleMouseDragged(MouseEvent event) {
        updateMouseState(event);
    }
    
    private void updateMouseState(MouseEvent event) {
        currentMouseState = new MouseState(
            event.getX(),
            event.getY(),
            event.isPrimaryButtonDown(),
            event.isSecondaryButtonDown(),
            event.isMiddleButtonDown()
        );
    }
    
    private void handleKeyPressed(KeyEvent event) {
        Key key = mapKey(event.getCode());
        if (key != Key.UNKNOWN && !pressedKeys.contains(key)) {
            pressedKeys.add(key);
            justPressedKeys.add(key);
            
            for (KeyHandler handler : new ArrayList<>(keyHandlers)) {
                handler.onKeyPressed(key);
            }
        }
    }
    
    private void handleKeyReleased(KeyEvent event) {
        Key key = mapKey(event.getCode());
        if (key != Key.UNKNOWN) {
            pressedKeys.remove(key);
            
            for (KeyHandler handler : new ArrayList<>(keyHandlers)) {
                handler.onKeyReleased(key);
            }
        }
    }
    
    private static Key mapKey(KeyCode keyCode) {
        return KEY_MAP.getOrDefault(keyCode, Key.UNKNOWN);
    }
    
    private static MouseButton mapMouseButton(javafx.scene.input.MouseButton button) {
        switch (button) {
            case PRIMARY: return MouseButton.PRIMARY;
            case SECONDARY: return MouseButton.SECONDARY;
            case MIDDLE: return MouseButton.MIDDLE;
            default: return MouseButton.PRIMARY;
        }
    }
    
    private static Map<KeyCode, Key> createKeyMap() {
        Map<KeyCode, Key> map = new HashMap<>();
        
        // Letters
        map.put(KeyCode.A, Key.A);
        map.put(KeyCode.B, Key.B);
        map.put(KeyCode.C, Key.C);
        map.put(KeyCode.D, Key.D);
        map.put(KeyCode.E, Key.E);
        map.put(KeyCode.F, Key.F);
        map.put(KeyCode.G, Key.G);
        map.put(KeyCode.H, Key.H);
        map.put(KeyCode.I, Key.I);
        map.put(KeyCode.J, Key.J);
        map.put(KeyCode.K, Key.K);
        map.put(KeyCode.L, Key.L);
        map.put(KeyCode.M, Key.M);
        map.put(KeyCode.N, Key.N);
        map.put(KeyCode.O, Key.O);
        map.put(KeyCode.P, Key.P);
        map.put(KeyCode.Q, Key.Q);
        map.put(KeyCode.R, Key.R);
        map.put(KeyCode.S, Key.S);
        map.put(KeyCode.T, Key.T);
        map.put(KeyCode.U, Key.U);
        map.put(KeyCode.V, Key.V);
        map.put(KeyCode.W, Key.W);
        map.put(KeyCode.X, Key.X);
        map.put(KeyCode.Y, Key.Y);
        map.put(KeyCode.Z, Key.Z);
        
        // Numbers
        map.put(KeyCode.DIGIT0, Key.DIGIT_0);
        map.put(KeyCode.DIGIT1, Key.DIGIT_1);
        map.put(KeyCode.DIGIT2, Key.DIGIT_2);
        map.put(KeyCode.DIGIT3, Key.DIGIT_3);
        map.put(KeyCode.DIGIT4, Key.DIGIT_4);
        map.put(KeyCode.DIGIT5, Key.DIGIT_5);
        map.put(KeyCode.DIGIT6, Key.DIGIT_6);
        map.put(KeyCode.DIGIT7, Key.DIGIT_7);
        map.put(KeyCode.DIGIT8, Key.DIGIT_8);
        map.put(KeyCode.DIGIT9, Key.DIGIT_9);
        
        // Arrow keys
        map.put(KeyCode.UP, Key.UP);
        map.put(KeyCode.DOWN, Key.DOWN);
        map.put(KeyCode.LEFT, Key.LEFT);
        map.put(KeyCode.RIGHT, Key.RIGHT);
        
        // Modifiers
        map.put(KeyCode.SHIFT, Key.SHIFT);
        map.put(KeyCode.CONTROL, Key.CONTROL);
        map.put(KeyCode.ALT, Key.ALT);
        map.put(KeyCode.META, Key.META);
        
        // Special keys
        map.put(KeyCode.SPACE, Key.SPACE);
        map.put(KeyCode.ENTER, Key.ENTER);
        map.put(KeyCode.ESCAPE, Key.ESCAPE);
        map.put(KeyCode.TAB, Key.TAB);
        map.put(KeyCode.BACK_SPACE, Key.BACKSPACE);
        map.put(KeyCode.DELETE, Key.DELETE);
        
        // Function keys
        map.put(KeyCode.F1, Key.F1);
        map.put(KeyCode.F2, Key.F2);
        map.put(KeyCode.F3, Key.F3);
        map.put(KeyCode.F4, Key.F4);
        map.put(KeyCode.F5, Key.F5);
        map.put(KeyCode.F6, Key.F6);
        map.put(KeyCode.F7, Key.F7);
        map.put(KeyCode.F8, Key.F8);
        map.put(KeyCode.F9, Key.F9);
        map.put(KeyCode.F10, Key.F10);
        map.put(KeyCode.F11, Key.F11);
        map.put(KeyCode.F12, Key.F12);
        
        // Punctuation
        map.put(KeyCode.PLUS, Key.PLUS);
        map.put(KeyCode.MINUS, Key.MINUS);
        map.put(KeyCode.EQUALS, Key.EQUALS);
        map.put(KeyCode.SLASH, Key.SLASH);
        map.put(KeyCode.PERIOD, Key.PERIOD);
        map.put(KeyCode.COMMA, Key.COMMA);
        
        return map;
    }
}
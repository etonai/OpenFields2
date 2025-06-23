package platform.api;

import java.util.Set;

/**
 * Platform-independent input provider interface.
 * Abstracts keyboard and mouse input across different platforms.
 */
public interface InputProvider {
    /**
     * Polls for new input events.
     * Should be called once per frame to update input state.
     */
    void pollEvents();
    
    /**
     * Checks if a specific key is currently pressed.
     * @param key the key to check
     * @return true if the key is pressed
     */
    boolean isKeyPressed(Key key);
    
    /**
     * Checks if a specific key was just pressed this frame.
     * @param key the key to check
     * @return true if the key was pressed this frame
     */
    boolean isKeyJustPressed(Key key);
    
    /**
     * Gets all currently pressed keys.
     * @return set of pressed keys
     */
    Set<Key> getPressedKeys();
    
    /**
     * Gets the current mouse state.
     * @return current mouse state
     */
    MouseState getMouseState();
    
    /**
     * Registers a click handler for mouse clicks.
     * @param handler the handler to register
     */
    void registerClickHandler(ClickHandler handler);
    
    /**
     * Unregisters a click handler.
     * @param handler the handler to unregister
     */
    void unregisterClickHandler(ClickHandler handler);
    
    /**
     * Registers a key handler for key events.
     * @param handler the handler to register
     */
    void registerKeyHandler(KeyHandler handler);
    
    /**
     * Unregisters a key handler.
     * @param handler the handler to unregister
     */
    void unregisterKeyHandler(KeyHandler handler);
    
    /**
     * Platform-independent key enumeration.
     */
    enum Key {
        // Letters
        A, B, C, D, E, F, G, H, I, J, K, L, M,
        N, O, P, Q, R, S, T, U, V, W, X, Y, Z,
        
        // Numbers
        DIGIT_0, DIGIT_1, DIGIT_2, DIGIT_3, DIGIT_4,
        DIGIT_5, DIGIT_6, DIGIT_7, DIGIT_8, DIGIT_9,
        
        // Arrow keys
        UP, DOWN, LEFT, RIGHT,
        
        // Modifiers
        SHIFT, CONTROL, ALT, META,
        
        // Special keys
        SPACE, ENTER, ESCAPE, TAB, BACKSPACE, DELETE,
        
        // Function keys
        F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12,
        
        // Punctuation
        PLUS, MINUS, EQUALS, SLASH, PERIOD, COMMA,
        
        // Unknown key
        UNKNOWN
    }
    
    /**
     * Mouse button enumeration.
     */
    enum MouseButton {
        PRIMARY,    // Usually left button
        SECONDARY,  // Usually right button
        MIDDLE      // Middle button/wheel click
    }
    
    /**
     * Represents the current state of the mouse.
     */
    class MouseState {
        public final double x;
        public final double y;
        public final boolean primaryPressed;
        public final boolean secondaryPressed;
        public final boolean middlePressed;
        
        public MouseState(double x, double y, boolean primaryPressed, 
                         boolean secondaryPressed, boolean middlePressed) {
            this.x = x;
            this.y = y;
            this.primaryPressed = primaryPressed;
            this.secondaryPressed = secondaryPressed;
            this.middlePressed = middlePressed;
        }
    }
    
    /**
     * Handler interface for mouse click events.
     */
    interface ClickHandler {
        /**
         * Called when a mouse button is clicked.
         * @param x x-coordinate of the click
         * @param y y-coordinate of the click
         * @param button the button that was clicked
         */
        void onClick(double x, double y, MouseButton button);
    }
    
    /**
     * Handler interface for keyboard events.
     */
    interface KeyHandler {
        /**
         * Called when a key is pressed.
         * @param key the key that was pressed
         */
        void onKeyPressed(Key key);
        
        /**
         * Called when a key is released.
         * @param key the key that was released
         */
        void onKeyReleased(Key key);
    }
}
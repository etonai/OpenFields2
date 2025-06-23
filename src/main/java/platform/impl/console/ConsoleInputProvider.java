package platform.impl.console;

import platform.api.InputProvider;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Console implementation of the InputProvider interface.
 * Provides keyboard-based input for the console platform.
 */
public class ConsoleInputProvider implements InputProvider {
    private final Set<Key> pressedKeys;
    private final Set<Key> justPressedKeys;
    private final List<ClickHandler> clickHandlers;
    private final List<KeyHandler> keyHandlers;
    private final Queue<Character> inputQueue;
    private final Thread inputThread;
    private MouseState mouseState;
    private volatile boolean running;
    
    // Input mode
    private InputMode mode = InputMode.NORMAL;
    private StringBuilder commandBuffer = new StringBuilder();
    
    private enum InputMode {
        NORMAL,
        MOVE,
        ATTACK,
        COMMAND
    }
    
    public ConsoleInputProvider() {
        this.pressedKeys = new HashSet<>();
        this.justPressedKeys = new HashSet<>();
        this.clickHandlers = new ArrayList<>();
        this.keyHandlers = new ArrayList<>();
        this.inputQueue = new ConcurrentLinkedQueue<>();
        this.mouseState = new MouseState(0, 0, false, false, false);
        this.running = true;
        
        // Start input thread for non-blocking input
        this.inputThread = new Thread(this::readInput);
        this.inputThread.setDaemon(true);
        this.inputThread.start();
    }
    
    private void readInput() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (running) {
                if (reader.ready()) {
                    int ch = reader.read();
                    if (ch != -1) {
                        inputQueue.offer((char) ch);
                    }
                }
                Thread.sleep(10); // Small delay to prevent busy waiting
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void pollEvents() {
        // Clear just pressed keys
        justPressedKeys.clear();
        
        // Process input queue
        Character ch;
        while ((ch = inputQueue.poll()) != null) {
            processChar(ch);
        }
    }
    
    private void processChar(char ch) {
        Key key = charToKey(ch);
        
        if (mode == InputMode.COMMAND) {
            if (ch == '\n' || ch == '\r') {
                // Execute command
                processCommand(commandBuffer.toString());
                commandBuffer.setLength(0);
                mode = InputMode.NORMAL;
            } else if (ch == 27) { // ESC
                // Cancel command
                commandBuffer.setLength(0);
                mode = InputMode.NORMAL;
            } else {
                commandBuffer.append(ch);
            }
            return;
        }
        
        // Normal key processing
        if (key != Key.UNKNOWN) {
            if (!pressedKeys.contains(key)) {
                pressedKeys.add(key);
                justPressedKeys.add(key);
                
                for (KeyHandler handler : new ArrayList<>(keyHandlers)) {
                    handler.onKeyPressed(key);
                }
            }
            
            // Immediate key release for console
            pressedKeys.remove(key);
            for (KeyHandler handler : new ArrayList<>(keyHandlers)) {
                handler.onKeyReleased(key);
            }
        }
        
        // Mode changes
        switch (ch) {
            case 'm':
            case 'M':
                mode = InputMode.MOVE;
                System.out.println("\n[MOVE MODE] Enter coordinates (x y) or use arrow keys:");
                break;
            case 'a':
            case 'A':
                mode = InputMode.ATTACK;
                System.out.println("\n[ATTACK MODE] Enter target unit number:");
                break;
            case ':':
                mode = InputMode.COMMAND;
                System.out.print("\n:");
                break;
            case 27: // ESC
                mode = InputMode.NORMAL;
                System.out.println("\n[NORMAL MODE]");
                break;
        }
    }
    
    private void processCommand(String command) {
        // Handle console commands
        String[] parts = command.trim().split("\\s+");
        if (parts.length == 0) return;
        
        switch (parts[0].toLowerCase()) {
            case "help":
            case "h":
                showHelp();
                break;
            case "quit":
            case "q":
                running = false;
                break;
            case "move":
                if (parts.length >= 3) {
                    try {
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        simulateClick(x * 7, y * 7, MouseButton.PRIMARY);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid coordinates");
                    }
                }
                break;
            case "select":
                if (parts.length >= 2) {
                    try {
                        int unit = Integer.parseInt(parts[1]);
                        // Simulate unit selection
                        Key unitKey = numberToKey(unit);
                        if (unitKey != Key.UNKNOWN) {
                            simulateKeyPress(unitKey);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid unit number");
                    }
                }
                break;
        }
    }
    
    private void showHelp() {
        System.out.println("\n=== Console Controls ===");
        System.out.println("1-9: Select unit");
        System.out.println("m: Move mode");
        System.out.println("a: Attack mode");
        System.out.println("Arrow keys: Pan camera");
        System.out.println("+/-: Zoom in/out");
        System.out.println("Space: Pause/Resume");
        System.out.println(":: Enter command mode");
        System.out.println("ESC: Cancel/Normal mode");
        System.out.println("h: Show this help");
        System.out.println("q: Quit game");
        System.out.println("====================\n");
    }
    
    private Key charToKey(char ch) {
        // Number keys
        if (ch >= '0' && ch <= '9') {
            return Key.values()[Key.DIGIT_0.ordinal() + (ch - '0')];
        }
        
        // Letter keys
        if (ch >= 'a' && ch <= 'z') {
            return Key.values()[Key.A.ordinal() + (ch - 'a')];
        }
        if (ch >= 'A' && ch <= 'Z') {
            return Key.values()[Key.A.ordinal() + (ch - 'A')];
        }
        
        // Special characters
        switch (ch) {
            case ' ': return Key.SPACE;
            case '\n':
            case '\r': return Key.ENTER;
            case 27: return Key.ESCAPE;
            case '\t': return Key.TAB;
            case '+':
            case '=': return Key.PLUS;
            case '-':
            case '_': return Key.MINUS;
            case '/': return Key.SLASH;
            case '.': return Key.PERIOD;
            case ',': return Key.COMMA;
        }
        
        return Key.UNKNOWN;
    }
    
    private Key numberToKey(int number) {
        if (number >= 0 && number <= 9) {
            return Key.values()[Key.DIGIT_0.ordinal() + number];
        }
        return Key.UNKNOWN;
    }
    
    private void simulateKeyPress(Key key) {
        if (!pressedKeys.contains(key)) {
            pressedKeys.add(key);
            justPressedKeys.add(key);
            
            for (KeyHandler handler : new ArrayList<>(keyHandlers)) {
                handler.onKeyPressed(key);
            }
            
            pressedKeys.remove(key);
            for (KeyHandler handler : new ArrayList<>(keyHandlers)) {
                handler.onKeyReleased(key);
            }
        }
    }
    
    private void simulateClick(double x, double y, MouseButton button) {
        for (ClickHandler handler : new ArrayList<>(clickHandlers)) {
            handler.onClick(x, y, button);
        }
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
        return mouseState;
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
    
    public void shutdown() {
        running = false;
        if (inputThread != null) {
            inputThread.interrupt();
        }
    }
}
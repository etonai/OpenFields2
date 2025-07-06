# DevCycle Future Plan 004: Complete JavaFX Decoupling

## Overview

This plan outlines the comprehensive strategy to complete the decoupling of JavaFX from the OpenFields2 core game logic. While significant progress has been made (Unit class, color abstraction, platform API), critical dependencies remain in the input and rendering systems that block true platform independence.

## Current State Analysis

### ✅ Already Decoupled
- **Unit class**: Now uses platform.api.Color instead of JavaFX Color
- **Combat system**: Completely JavaFX-free (combat/*, managers/*)
- **SaveGameController**: Fully abstracted
- **Data layer**: No JavaFX dependencies
- **Platform API**: Basic abstraction layer established

### ❌ Remaining JavaFX Dependencies

#### 1. Input System (Critical)
- **InputManager**: Deeply coupled to JavaFX Scene, Canvas, events
- **Event handlers**: Direct use of KeyEvent, MouseEvent, KeyCode
- **All game controllers**: Depend on JavaFX input events

#### 2. Rendering System
- **GameRenderer**: Direct Canvas/GraphicsContext usage
- **Coordinate transforms**: Tied to JavaFX canvas operations
- **Visual effects**: Muzzle flashes, highlights use JavaFX

#### 3. Application Layer
- **OpenFields2**: Extends JavaFX Application
- **Main game loop**: Timeline-based (JavaFX)
- **Window management**: Stage/Scene coupling

#### 4. Lingering Issues
- **platform.api.Color**: Still has fromJavaFX/toJavaFX methods
- **Audio system**: Partial abstraction only
- **Resource loading**: Classpath assumptions

## Strategic Approach

### Phase 1: Input System Abstraction (High Priority)

#### 1.1 Create Platform-Independent Input Events
```java
// platform.api.input package
interface InputEvent {
    long getTimestamp();
    boolean isConsumed();
    void consume();
}

interface KeyEvent extends InputEvent {
    KeyCode getKeyCode();
    boolean isShiftDown();
    boolean isControlDown();
    boolean isAltDown();
}

interface MouseEvent extends InputEvent {
    double getX();
    double getY();
    MouseButton getButton();
    int getClickCount();
}

enum KeyCode {
    A, B, C, ... Z,
    DIGIT0, DIGIT1, ... DIGIT9,
    SPACE, ENTER, ESCAPE,
    UP, DOWN, LEFT, RIGHT,
    // etc.
}

enum MouseButton {
    PRIMARY, SECONDARY, MIDDLE
}
```

#### 1.2 Abstract Input Provider
```java
interface InputProvider {
    void addKeyPressedHandler(Consumer<KeyEvent> handler);
    void addKeyReleasedHandler(Consumer<KeyEvent> handler);
    void addMousePressedHandler(Consumer<MouseEvent> handler);
    void addMouseReleasedHandler(Consumer<MouseEvent> handler);
    void addMouseMovedHandler(Consumer<MouseEvent> handler);
}
```

#### 1.3 Refactor InputManager
- Remove all JavaFX event dependencies
- Use platform-independent events
- Inject InputProvider instead of Scene/Canvas

### Phase 2: Rendering System Abstraction

#### 2.1 Complete Renderer Interface
```java
interface Renderer {
    void clear();
    void setTransform(double translateX, double translateY, double scale);
    void drawCircle(double x, double y, double radius, Color color);
    void drawRectangle(double x, double y, double width, double height, Color color);
    void drawLine(double x1, double y1, double x2, double y2, Color color, double width);
    void drawText(String text, double x, double y, Font font, Color color);
    void drawPolygon(double[] xPoints, double[] yPoints, Color color);
    void fillCircle(double x, double y, double radius, Color color);
    void fillRectangle(double x, double y, double width, double height, Color color);
    void present(); // Commit rendering
}
```

#### 2.2 Abstract GameRenderer
- Extract interface for GameRenderer
- Move JavaFX-specific code to JavaFXGameRenderer
- Create ConsoleGameRenderer for testing

### Phase 3: Application Layer Decoupling

#### 3.1 Game Engine Core
```java
class GameEngine {
    private final GameLoop gameLoop;
    private final InputProvider inputProvider;
    private final Renderer renderer;
    private final AudioSystem audioSystem;
    
    public void start() {
        gameLoop.start(this::update, this::render);
    }
    
    private void update(long tick) {
        // Game update logic
    }
    
    private void render() {
        // Rendering logic
    }
}
```

#### 3.2 Platform Launchers
```java
// JavaFX launcher
class JavaFXLauncher extends Application {
    @Override
    public void start(Stage stage) {
        Platform platform = new JavaFXPlatform(stage);
        GameEngine engine = new GameEngine(platform);
        engine.start();
    }
}

// Console launcher
class ConsoleLauncher {
    public static void main(String[] args) {
        Platform platform = new ConsolePlatform();
        GameEngine engine = new GameEngine(platform);
        engine.start();
    }
}
```

### Phase 4: Clean Up Remaining Dependencies

#### 4.1 Remove JavaFX from platform.api.Color
- Remove fromJavaFX/toJavaFX methods
- Move conversions to JavaFX platform implementation

#### 4.2 Complete Audio Abstraction
- Finish AudioSystem interface
- Move JavaFX AudioClip to implementation

#### 4.3 Resource Management
- Create ResourceProvider interface
- Abstract resource loading from classpath

## Implementation Plan

### System 1: Input Event Abstraction
**Priority**: Critical  
**Effort**: High (3-4 days)  
**Risk**: Medium (extensive refactoring)

1. Create platform.api.input package with event interfaces
2. Implement JavaFX event adapters
3. Create InputProvider interface and implementations
4. Refactor InputManager to use abstractions
5. Update all event handlers in game controllers

### System 2: Input Manager Refactoring
**Priority**: Critical  
**Effort**: High (2-3 days)  
**Risk**: High (core system change)

1. Extract InputManager interface
2. Create JavaFXInputManager implementation
3. Create HeadlessInputManager for testing
4. Update all InputManager dependencies
5. Comprehensive testing of input handling

### System 3: Renderer Abstraction
**Priority**: High  
**Effort**: Medium (2-3 days)  
**Risk**: Medium

1. Complete Renderer interface
2. Extract rendering logic from GameRenderer
3. Create JavaFXRenderer implementation
4. Create ConsoleRenderer for testing
5. Update all rendering code

### System 4: Game Engine Core
**Priority**: High  
**Effort**: Medium (2 days)  
**Risk**: Low

1. Create GameEngine class
2. Extract game loop logic
3. Create platform launchers
4. Refactor OpenFields2 to use GameEngine
5. Test both JavaFX and console modes

### System 5: Cleanup and Polish
**Priority**: Medium  
**Effort**: Low (1-2 days)  
**Risk**: Low

1. Remove JavaFX from Color API
2. Complete audio abstraction
3. Create resource provider
4. Update documentation
5. Final testing and validation

## Testing Strategy

### Unit Tests
- Test all input event conversions
- Verify renderer abstraction
- Test game engine in isolation

### Integration Tests
- Full game loop in headless mode
- Input handling without JavaFX
- Save/load in console mode

### Platform Tests
- JavaFX platform functionality
- Console platform functionality
- Platform switching

## Success Criteria

1. **No JavaFX imports** in core game packages (combat, game, data)
2. **Successful headless execution** of full game scenarios
3. **Console mode** runs without JavaFX on classpath
4. **All tests pass** in both JavaFX and headless modes
5. **Performance parity** between platforms

## Risk Mitigation

### High Risk: Input System Refactoring
- Maintain backward compatibility during transition
- Extensive testing at each step
- Feature flags for gradual rollout

### Medium Risk: Breaking Existing Functionality
- Comprehensive test coverage before changes
- Incremental refactoring approach
- Maintain parallel implementations during transition

### Low Risk: Performance Degradation
- Profile before and after changes
- Optimize hot paths
- Cache frequently used conversions

## Timeline Estimate

**Total Duration**: 10-15 days

- Phase 1 (Input): 5-7 days
- Phase 2 (Rendering): 2-3 days
- Phase 3 (Application): 2 days
- Phase 4 (Cleanup): 1-2 days
- Testing & Bug Fixes: 2-3 days

## Benefits

1. **True Platform Independence**: Run on any Java platform
2. **Improved Testability**: Full headless testing capability
3. **Better Architecture**: Clean separation of concerns
4. **Future Flexibility**: Easy to add new platforms (mobile, web)
5. **Performance**: Potential optimizations per platform

## Conclusion

Completing the JavaFX decoupling is critical for achieving the project's goals of platform independence and comprehensive testing. While the input system refactoring presents the highest risk and effort, the benefits far outweigh the costs. The phased approach allows for incremental progress while maintaining system stability.
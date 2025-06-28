# OpenFields2 Keyboard Shortcuts

This document tracks all keyboard shortcuts to prevent conflicts during development.

## Combat Controls

| Key Combination | Function | Implementation | Status |
|-----------------|----------|----------------|---------|
| CTRL-1 | Multiple Shot Count Cycling | CombatCommandProcessor.java | ✅ Active (DevCycle 28) |
| CTRL-SHIFT-Right-Click | Reaction Action Setup | MouseInputHandler.java | ✅ Active (DevCycle 28) |
| Right-Click | Combat Targeting | MouseInputHandler.java | ✅ Active |
| CTRL-Right-Click | Hold State Targeting | MouseInputHandler.java | ✅ Active (DevCycle 25) |
| F | Firing Mode Toggle | CombatCommandProcessor.java | ✅ Active |
| R | Ready Weapon | CombatCommandProcessor.java | ✅ Active |
| SHIFT-T | Automatic Targeting Toggle | CombatCommandProcessor.java | ✅ Active |
| Z | Clear Target Zone | CombatCommandProcessor.java | ✅ Active |
| H | Hold State Cycling | KeyboardInputHandler.java | ✅ Active (DevCycle 25) |
| SHIFT-F | Firing Preference Toggle | KeyboardInputHandler.java | ✅ Active (DevCycle 26) |

## Movement Controls

| Key Combination | Function | Implementation | Status |
|-----------------|----------|----------------|---------|
| W | Increase Movement Speed | KeyboardInputHandler.java | ✅ Active |
| S | Decrease Movement Speed | KeyboardInputHandler.java | ✅ Active |
| Q | Increase Aiming Speed | KeyboardInputHandler.java | ✅ Active |
| E | Decrease Aiming Speed | KeyboardInputHandler.java | ✅ Active |

## Camera Controls

| Key Combination | Function | Implementation | Status |
|-----------------|----------|----------------|---------|
| Arrow Keys | Pan Camera | CameraController.java | ✅ Active |
| +/- | Zoom Camera | CameraController.java | ✅ Active |

## Game State Controls

| Key Combination | Function | Implementation | Status |
|-----------------|----------|----------------|---------|
| Space | Pause/Resume | KeyboardInputHandler.java | ✅ Active |
| CTRL-E | Edit Mode Toggle | KeyboardInputHandler.java | ✅ Active |

## Edit Mode Controls

| Key Combination | Function | Implementation | Status |
|-----------------|----------|----------------|---------|
| CTRL-A | Direct Character Addition | EditModeManager.java | ✅ Active (DevCycle 14) |

## Disabled/Deprecated Controls

| Key Combination | Function | Implementation | Status | Reason |
|-----------------|----------|----------------|---------|---------|
| CTRL-1 | Individual Character Creation | EditModeManager.java | ❌ Disabled | Conflicts with Multiple Shot Control (DevCycle 28) |

## Debug Controls

| Key Combination | Function | Implementation | Status |
|-----------------|----------|----------------|---------|
| SHIFT-/ | Character Stats Display | KeyboardInputHandler.java | ✅ Active |
| Various | Debug dumps and system checks | KeyboardInputHandler.java | ✅ Active |

## Selection Controls

| Key Combination | Function | Implementation | Status |
|-----------------|----------|----------------|---------|
| Left-Click | Unit Selection | MouseInputHandler.java | ✅ Active |
| Drag | Rectangle Selection | MouseInputHandler.java | ✅ Active |

## Notes

- **DevCycle 28**: CTRL-1 reassigned from Individual Character Creation to Multiple Shot Control
- **Key Conflict Resolution**: Individual character creation disabled to prevent dual functionality
- **Future Development**: Check this document before implementing new keyboard shortcuts

## Key Binding Guidelines

1. **Check Existing**: Always check this document before implementing new key bindings
2. **Document Changes**: Update this document when adding/removing/changing key bindings
3. **Avoid Conflicts**: Choose unused key combinations or coordinate with existing features
4. **User Communication**: Document key changes in DevCycle planning documents

## Reserved Key Combinations

The following combinations should be avoided for future features:
- Function keys (F1-F12) - reserved for system functions
- ALT combinations - may conflict with system menus
- Single letters without modifiers - interfere with text input in some contexts

Last Updated: June 28, 2025 (DevCycle 28)
# OpenFields2 - Player Instructions

This document provides a comprehensive guide to all player commands and controls available in OpenFields2.

## Mouse Controls

### Left Click
- **Click on unit**: Select single unit (clears previous selection)
- **Click on empty space**: Start rectangle selection mode
- **Drag**: Extend rectangle selection area
- **Release**: Complete rectangle selection of multiple units

### Right Click
- **Right-click on enemy unit**: Attack target with selected units
- **Right-click on friendly unit**: Ready weapon (if single unit selected)
- **Shift + Right-click on unit**: Toggle persistent attack mode
- **Right-click on empty space**: Move selected units to location
  - In **Edit Mode**: Instantly teleport units
  - In **Normal Mode**: Move units maintaining formation

## Keyboard Controls

### Camera Navigation
- **Arrow Keys**: Pan camera view
  - `↑` Up
  - `↓` Down  
  - `←` Left
  - `→` Right
- **+/=**: Zoom in
- **-**: Zoom out

### Game State Controls
- **Space**: Toggle pause/resume game
- **Ctrl+D**: Toggle debug mode display
- **Ctrl+E**: Toggle edit mode (disables combat, enables instant movement)

### Unit Movement Controls
*Applies to all selected units*
- **W**: Increase movement speed (Crawl → Walk → Jog → Run)
- **S**: Decrease movement speed (Run → Jog → Walk → Crawl → Stop)

### Aiming Speed Controls
*Applies to all selected units*
- **Q**: Increase aiming speed (Careful → Normal → Quick)
- **E**: Decrease aiming speed (Quick → Normal → Careful)
  - **Careful**: Slower aiming, +15 accuracy bonus
  - **Normal**: Standard aiming speed, no modifier
  - **Quick**: Faster aiming, -20 accuracy penalty

### Combat Controls
- **R**: Ready weapon for all selected units
- **Shift+T**: Toggle automatic targeting for selected units

### Information Display
- **Shift+/**: Display detailed character stats (single unit only)

### Save/Load System
- **Ctrl+S**: Save game (prompts for slot 1-9)
- **Ctrl+L**: Load game (prompts for slot 1-9, or 0 to cancel)

## Edit Mode Controls
*Only available when edit mode is enabled (Ctrl+E)*

- **Ctrl+C**: Create new character (prompts for archetype selection)
- **Ctrl+W**: Change weapon for selected units
- **Ctrl+F**: Change faction for selected units

## Number Key Input
When prompted by the game, use number keys for selections:

### Save/Load Slots
- **1-9**: Select save/load slot
- **0**: Cancel operation (load only)

### Character Creation
- **1-9**: Select character archetype from available list
- **0**: Cancel character creation

### Weapon/Faction Selection
- **1-9**: Select from available options
- **0**: Cancel selection

### Cancel Operations
- **Esc**: Cancel any current prompt operation

## Movement Types
Units have different movement speeds that affect combat accuracy:

- **Crawl**: 0.25x speed, -10 shooting accuracy
- **Walk**: 1.0x speed, -5 shooting accuracy (default)
- **Jog**: 1.5x speed, -15 shooting accuracy  
- **Run**: 2.0x speed, -25 shooting accuracy
- **Stationary**: No movement penalty

## Combat System

### Attack Mechanics
- Right-click enemy units to attack with selected units
- Attacks are scheduled based on weapon ready time and aiming speed
- Accuracy affected by movement speed, aiming speed, character stats, and skills
- Units become incapacitated at 0 health and stop moving

### Unit Selection
- Single unit selection: Left-click on unit
- Multiple unit selection: Click and drag rectangle around units
- Most commands apply to all selected units simultaneously

### Game Modes
- **Normal Mode**: Full combat simulation with realistic timing
- **Edit Mode**: Instant movement, combat disabled, character editing enabled

## Tips
- Use **Shift+/** to view detailed character statistics
- Edit mode allows quick setup and testing of scenarios
- Combine movement and aiming speed controls for tactical advantage
- Rectangle selection enables efficient multi-unit management
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
- **Shift + Right-click and drag on empty space**: Create target zone (single unit selected)

## Keyboard Controls

### Camera Navigation
- **Arrow Keys**: Pan camera view
  - `↑` Up
  - `↓` Down  
  - `←` Left
  - `→` Right
- **+/=**: Zoom in/
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
- **Q**: Increase aiming speed (Very Careful → Careful → Normal → Quick)
- **E**: Decrease aiming speed (Quick → Normal → Careful → Very Careful)
  - **Very Careful**: Slowest aiming, double skill bonus, requires weapon skill level 1+
  - **Careful**: Slower aiming, +15 accuracy bonus
  - **Normal**: Standard aiming speed, no modifier
  - **Quick**: Faster aiming, -20 accuracy penalty

### Position Controls
*Applies to all selected units*
- **C**: Crouch down (Standing → Kneeling → Prone)
- **V**: Stand up (Prone → Kneeling → Standing)
  - **Standing**: Normal targeting difficulty
  - **Kneeling**: Reduced targeting profile
  - **Prone**: Hardest to hit (-15 accuracy for attackers), crawl movement only

### Combat Controls
- **R**: Ready weapon for all selected units
- **F**: Cycle weapon firing mode (Single Shot → Burst → Full Auto)
- **Shift+T**: Toggle automatic targeting for selected units
- **Z**: Clear target zone for selected unit (single unit only)

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

### Movement Restrictions
- **Prone position**: Forces crawl movement only
- **Leg wounds**: Single leg wound disables RUN, both legs wounded forces CRAWL only and prone position
- **Incapacitated**: Cannot move or change position

## Combat System

### Attack Mechanics
- Right-click enemy units to attack with selected units
- Attacks are scheduled based on weapon ready time and aiming speed
- Accuracy affected by movement speed, aiming speed, character stats, skills, position, wounds, and bravery
- Units become incapacitated at 0 health and stop moving

### Enhanced Combat Features

#### Stray Shots
- Missed shots may hit nearby characters (friendly fire possible)
- Probability based on character positions: Standing 0.5%, Kneeling 0.25%, Prone 0.125%
- Target selection weighted by position visibility

#### Bravery System
- Characters make bravery checks when wounded or when allies are hit nearby
- Failed checks apply -10 accuracy penalty per failure for 3 seconds
- Based on coolness stat: 50 + coolness modifier = target number

#### Wound Effects
- **Head/Arm wounds**: Accuracy penalty equal to damage dealt
- **Leg wounds**: Movement restrictions (see Movement Restrictions above)  
- **All wounds**: Tracked with actual damage values for realistic effects

#### Target Zones
- **Create**: Shift+right-click and drag to define rectangular targeting area
- **Purpose**: Characters with automatic targeting prioritize targets within their zone
- **Display**: Yellow rectangle outline when single character selected
- **Clear**: Press Z key to remove target zone from selected character
- **Behavior**: If no targets in zone, character selects closest global target

#### Weapon Firing Modes
- **Single Shot**: Standard semi-automatic firing (default)
- **Burst**: Fires 3-round bursts at cyclic rate
  - First shot uses current aiming speed
  - Subsequent shots use quick aiming (faster but less accurate)
- **Full Auto**: Continuous automatic fire
  - First shot uses current aiming speed  
  - All following shots use quick aiming
- **Cycle**: Press F key to cycle between available modes
- **Availability**: Only weapons with multiple firing modes can be switched

### Unit Selection
- Single unit selection: Left-click on unit
- Multiple unit selection: Click and drag rectangle around units
- Most commands apply to all selected units simultaneously

### Game Modes
- **Normal Mode**: Full combat simulation with realistic timing
- **Edit Mode**: Instant movement, combat disabled, character editing enabled

## Tips
- Use **Shift+/** to view detailed character statistics including wounds, restrictions, and skills
- Edit mode allows quick setup and testing of scenarios
- Combine movement, aiming speed, and position controls for tactical advantage
- Rectangle selection enables efficient multi-unit management
- **Very Careful aiming** requires weapon skill level 1+ but provides double accuracy bonus
- **Prone position** makes you harder to hit but restricts movement to crawling
- **Leg wounds** can severely limit mobility - protect your legs in combat!
- **Bravery failures** stack penalties - keep units with high coolness in stressful situations
- **Stray shots** can hit anyone nearby - consider positioning when engaging enemies
- **Target zones** help focus automatic targeting on specific battlefield areas
- **Burst and full auto** provide rapid fire at the cost of accuracy after the first shot
- **Shift+T automatic targeting** combined with target zones creates powerful tactical control
- **F key** quickly switches firing modes for weapons that support multiple modes
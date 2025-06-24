# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

OpenFields2 is a Java-based tactical combat simulation game built with JavaFX. It implements a real-time strategy game where units can move, attack, and be damaged in a turn-based system with scheduled events.

## Key Architecture

- **Single-file structure**: All core classes are in `src/main/java/OpenFields2.java`
- **Event-driven system**: Uses `ScheduledEvent` with `PriorityQueue` for delayed actions (attacks, effects)
- **Game loop**: 60 FPS JavaFX Timeline driving tick-based simulation
- **Core entities**: `Character`, `Unit`, `Weapon`, `WeaponState` classes
- **Coordinate system**: 7 pixels = 1 foot conversion for distance calculations

## Development Commands

### Build and Test
```bash
mvn compile                    # Compile the project
mvn test                      # Run all tests
mvn test -Dtest=ClassName     # Run specific test class
mvn clean                     # Clean build artifacts
```

### Run Application
```bash
mvn javafx:run                # Run the JavaFX application
```

## Game Mechanics

- **Movement**: Right-click empty space to move selected unit
- **Movement Types**: W/S keys to increase/decrease movement speed (Crawl, Walk, Jog, Run)
- **Aiming Speed**: Q/E keys to increase/decrease aiming speed (Careful, Normal, Quick)
- **Combat**: Right-click enemy unit to schedule ranged attack
- **Selection**: Left-click unit to select
- **Controls**: Arrow keys pan, +/- zoom, Space pauses/resumes
- **Health system**: Units become incapacitated at 0 health, movement stops
- **Attack timing**: Projectile travel time based on weapon velocity and distance

### Character Stats System
- **Dexterity** (1-100): Affects shooting accuracy and manual dexterity
- **Strength** (1-100): Physical power and carrying capacity - *not yet implemented*
- **Reflexes** (1-100): Reaction time and quick responses - affects weapon readying speed
- **Health**: Current hit points, incapacitated at 0
- **Coolness** (1-100): Mental composure under stress, affects stress modifier
- **Stat Modifiers**: Each stat uses a balanced curve from -20 to +20 modifiers

### Character Generation System (DevCycle 12)
- **Archetype-Based Health**: Characters now generate varied health based on archetype background
  - **Gunslinger**: 70-100 health (hardy frontier background)
  - **Soldier**: 80-100 health (very hardy, military training)
  - **Medic**: 60-90 health (average physical condition)
  - **Scout**: 50-90 health (varied backgrounds)
  - **Marksman**: 40-80 health (focused training, less physical)
  - **Brawler**: 85-100 health (very hardy, physical background)
  - **Civil War Soldiers**: 75-100 health (military experience)
- **Health Variation**: Replaces uniform 100 health assignment with realistic character diversity

### Movement System
- **Crawl**: 0.25x base speed (10.5 pixels/sec)
- **Walk**: 1.0x base speed (42 pixels/sec)
- **Jog**: 1.5x base speed (63 pixels/sec)
- **Run**: 2.0x base speed (84 pixels/sec) - default
- **Controls**: W to increase, S to decrease movement type
- **Display**: Selected unit shows current movement type below name

### Aiming Speed System
- **Careful**: 2.0x slower aiming, +15 accuracy bonus - deliberate aimed shots
- **Normal**: 1.0x baseline aiming timing, no accuracy modifier - standard aiming
- **Quick**: 0.5x faster aiming, -20 accuracy penalty - rapid aiming
- **Controls**: Q to increase, E to decrease aiming speed
- **Display**: Selected unit shows current aiming speed below movement type
- **Timing**: Only affects aiming phase duration, not other weapon states

### Burst and Full Auto Firing (DevCycle 20)
- **Burst Mode**: Fires exactly `burstSize` bullets (e.g., UZI fires 3)
- **Timing**: Uses `firingDelay` between shots (e.g., UZI: +0, +6, +12 ticks)
- **Accuracy**: First bullet uses character's current aiming speed, bullets 2+ get -20 penalty
- **Full Auto**: Continues firing until trigger release, same timing and accuracy rules
- **Interruptions**: Mode switch, new attack, or hesitation stops burst; target death does NOT
- **Partial Bursts**: Fires available ammo even if less than full burst size

### Combat Modifiers
- **Movement Penalties**: Shooting accuracy decreases while moving
  - **Walking**: -5 modifier
  - **Crawling**: -10 modifier
  - **Jogging**: -15 modifier
  - **Running**: -25 modifier
  - **Stationary**: No penalty (0 modifier)
- **Aiming Speed Modifiers**: Accuracy affected by aiming technique
  - **Careful**: +15 accuracy modifier (deliberate aiming)
  - **Normal**: 0 accuracy modifier (standard aiming)
  - **Quick**: -20 accuracy modifier (rapid aiming)
- **Incapacitated units**: Considered stationary (no movement penalty), cannot change aiming speed

### Default Skills System
- **Pistol**: Handgun proficiency for accuracy and handling - provides +5 accuracy per skill level when using pistol weapons
- **Rifle**: Long gun proficiency for rifles and similar weapons - provides +5 accuracy per skill level when using rifle weapons
- **Quickdraw**: Speed of weapon readying and drawing - provides 5% speed increase per skill level for weapon preparation
- **Medicine**: Healing and wound treatment capabilities - *not yet implemented*
- **Implementation**: Available via `Character.createDefaultSkills()` but not auto-assigned to characters
- **Baseline Level**: All default skills start at level 50
- **Combat Integration**: Pistol and Rifle skills automatically apply to hit calculations; Quickdraw affects weapon readying timing

### Weapon Ready Speed System
- **Reflexes Impact**: Uses stat modifier (-20 to +20) converted to speed multiplier (1.2x slower to 0.8x faster)
- **Quickdraw Skill**: Each skill level provides 5% speed improvement (level 4 = 20% faster)
- **Combined Effect**: Reflexes and Quickdraw stack multiplicatively for total speed bonus
- **Affected States**: Only weapon preparation states (drawing, unsheathing, unsling, ready transitions)
- **Example**: Reflexes 90 (+12 mod) + Quickdraw 4 = 0.88 × 0.8 = 0.704x (≈30% faster weapon readying)
- **Display**: Weapon ready speed shown in character stats (Shift+/) with contributing factors

### Weapon Types System
- **Pistol Type**: Created by `createPistol()` method - handguns with holstered/drawing states
- **Rifle Type**: Created by `createRifle()` method - long guns with slung/unsling states
- **Other Type**: Created by `createSheathedWeapon()` method or default constructor - magical items, tools, etc.
- **Implementation**: `WeaponType` enum with PISTOL, RIFLE, and OTHER values
- **Display**: Weapon type shown in character stats display (Shift+/)

### Enhanced Combat Tracking (DevCycle 12)
- **Separate Statistics**: Combat tracking now differentiates between ranged and melee attacks
  - **Ranged**: `rangedAttacksAttempted`, `rangedAttacksSuccessful`, `rangedWoundsInflicted`
  - **Melee**: `meleeAttacksAttempted`, `meleeAttacksSuccessful`, `meleeWoundsInflicted`
- **Legacy Compatibility**: Original total statistics (`attacksAttempted`, `attacksSuccessful`) still maintained
- **Backward Compatibility**: New methods `getCombinedAttacksAttempted()`, `getCombinedAttacksSuccessful()`, `getCombinedWoundsInflicted()`

### Enhanced Character Stats Display (DevCycle 12)
- **Dual Weapon Display**: Shows both ranged and melee weapons with active indication
  - **Format**: `Ranged: Weapon Name (damage, accuracy) [ACTIVE]`
  - **Format**: `Melee: Weapon Name (damage, accuracy, reach) [ACTIVE]`
- **Combat Mode Indication**: [ACTIVE] marker shows which weapon type is currently in use
- **Separate Combat Stats**: Displays ranged and melee combat statistics independently
- **Enhanced Information**: Shows weapon-specific details (range, velocity, reach) for active weapon

### Melee Combat Audio (DevCycle 12)
- **Sound Integration**: Melee attacks now play weapon sound effects at moment of execution
- **Pattern Consistency**: Follows same audio pattern as ranged weapon sounds
- **Error Handling**: Graceful fallback if sound files missing or audio system unavailable

### CTRL-A Character Addition System (DevCycle 14)
- **Direct Character Deployment**: CTRL-A directly adds existing characters from faction files without creation step
- **Console-Based Interface**: Faction selection, quantity (1-20), and spacing (1-9 feet) via console menus
- **Faction Character Loading**: Characters sourced from existing faction JSON files using proper CharacterData deserialization
- **Character Availability**: Excludes already deployed and incapacitated characters from selection pool
- **Horizontal Lineup**: Characters placed in line formation with configurable spacing in feet
- **Error Handling**: Graceful handling of missing faction files and JSON parsing issues

### Enhanced Melee Combat System (DevCycle 14)
- **Target Incapacitation Response**: Melee characters immediately stop movement when targets become incapacitated
- **Unified Weapon Readiness**: Both ranged and melee weapons use the same proven state management system
- **Movement Integration**: Weapon readiness begins immediately when moving to targets, ready upon arrival
- **State Persistence**: Weapon states properly maintained through movement phases without resets
- **Combat Mode Awareness**: System automatically detects ranged vs. melee mode for appropriate behavior
- **Enhanced Movement Logic**: `cancelMeleeMovement()` properly stops units using `Unit.setTarget(currentX, currentY)`

## Technical Details

- **Java 21** with JavaFX 21.0.2
- **JUnit 5** for testing
- **Maven** build system
- **Tick-based timing**: 60 ticks per second, events scheduled by tick number
- **Canvas rendering**: Custom 2D graphics with zoom/pan support

## Development Workflow

- **Workflow Reference**: See `plans/DevCycle_workflow_plan.md` for complete development cycle process
- **Key Workflow Principles**:
  - **No Auto-Commits**: Never commit implementation or bug fixes without explicit approval
  - **Q&A Process**: Use iterative question/answer cycles for planning clarity
  - **Review Gates**: All code changes must be reviewed via diff before commit
  - **Phase Structure**: Brainstorm → Plan → Implement → Debug → Close
- **Document Naming**: `DevCycle_2025_00##_brainstorm.md`, `DevCycle_2025_00##.md`, `DevCycle_2025_00##_bugs_##.md`
- **Branch Pattern**: Work in `DC_##` branches, merge to main when complete

## Git and Version Control Rules

### Absolute Prohibitions
- **CRITICAL**: Claude is ABSOLUTELY FORBIDDEN from executing ANY git commands that modify repository state without explicit user permission
- **FORBIDDEN COMMANDS**: 
  - `git commit` (any variant)
  - `git checkout -b` (branch creation)
  - `git branch` (branch creation)
  - `git merge`
  - `git rebase`
  - `git push`
  - `git pull` (unless explicitly requested)
  - `git tag`
  - Any other git command that modifies repository state

### Required Process for ANY Git Operations
1. **ASK PERMISSION FIRST**: Always ask explicit permission before executing any git command
2. **Implement all requested changes** (using non-git tools)
3. **Show user the implementation results and/or diffs**
4. **Wait for explicit approval** before executing git commands
5. **Only execute git commands when user specifically requests them**

### Enforcement Rules
- **NO EXCEPTIONS**: These rules apply to ALL git operations including branch creation, commits, merges, pushes, and any repository modifications
- **NO ASSUMPTIONS**: Never assume user wants git commands executed, even if they seem logical or part of a workflow
- **ASK EXPLICITLY**: Always ask "Should I create a branch?" or "Should I commit these changes?" before executing
- **VIOLATION CONSEQUENCES**: Unauthorized git operations violate the project workflow and must be undone immediately

### Permitted Git Operations (Read-Only)
Claude MAY execute these git commands without permission (read-only operations):
- `git status`
- `git diff`
- `git log`
- `git show`
- `git ls-files`
- Other read-only git inspection commands

## Documentation Conventions

### Document Review Process

#### Definition
When asked to "review a document", "look at a document and think about it", or similar instructions, Claude must perform a comprehensive analysis following this mandatory checklist.

#### Document Review Checklist
1. **Read the entire document** thoroughly from beginning to end
2. **Analyze completeness** - identify missing information, incomplete sections, or unclear requirements
3. **Check consistency** - verify information aligns across sections and with existing project standards
4. **Identify potential issues** - technical risks, implementation challenges, design conflicts, or ambiguities
5. **Formulate clarifying questions** - prepare specific, actionable questions about unclear or incomplete areas
6. **Add questions to document** - **MANDATORY**: Append all questions to the end of the document being reviewed (never provide questions separately in response)
7. **Follow specific instructions** - if user provides additional review instructions, execute them precisely

#### Question Format Requirements
- **Organization**: Group questions by category (Technical, Requirements, Implementation, Scope, etc.)
- **Numbering**: Use sequential numbering for easy reference
- **Specificity**: Each question must be actionable and specific
- **Placement**: **ALWAYS** add questions as a new section at the end of the document
- **Section Title**: Use "Planning Questions for User Review" or similar clear heading

#### Critical Rule
**NEVER provide review questions separately from the document**. Questions must be added directly to the document being reviewed. This ensures all review information stays with the document for future reference.

### EDNOTE Convention
- **EDNOTE**: Prefix used for notes from the project owner (Edward T. Tonai) in planning and brainstorming documents
- **Usage**: `EDNOTE: [note content]` - provides clarification, preferences, or additional context for development decisions
- **Purpose**: Distinguishes owner notes from analysis or generated content, helping maintain clear communication in collaborative planning
- **Location**: Typically found in planning documents (`plans/` directory) and brainstorming files

## Legal Framework

- **License**: MIT License - see LICENSE file for full text
- **Copyright**: Copyright (c) 2025 Edward T. Tonai
- **Usage**: Open source project with permissive licensing for modification and distribution
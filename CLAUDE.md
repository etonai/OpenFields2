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

### Combat Management Architecture (DevCycle 29)

- **CombatCoordinator**: Central orchestrator for all combat operations
  - Located in `combat/CombatCoordinator.java`
  - Singleton pattern for global access
  - Coordinates all combat manager interactions
  - Entry point for all combat operations

- **Manager Pattern**: Combat behavior extracted into specialized managers
  - **BurstFireManager**: Handles burst and automatic fire mechanics
  - **AimingSystem**: Manages aiming calculations and bonuses
  - **DefenseManager**: Controls defensive actions and counter-attacks
  - **WeaponStateManager**: Manages weapon state transitions
  - **ReloadManager**: Handles reload operations
  - All managers in `combat/managers/` package

- **Architecture Benefits**:
  - Character class focused on data only
  - Each manager handles one specific aspect of combat
  - Testable, maintainable, and extensible design
  - Clear separation of concerns

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
- **Combat**: Right-click enemy unit to schedule ranged attack, Ctrl+Right-click to target with hold state
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

### Weapon Hold State System (DevCycle 25)
- **Hold State Control**: Characters can target opponents and hold at specific weapon states instead of automatically firing
- **H Key Cycling**: Cycles through weapon-specific available states (single character selection only)
  - **Available States**: All weapon states except "firing", "recovering", "reloading"
  - **State Examples**: holstered/slung/sheathed, grippinginholster, drawing/unsling, ready, pointedfromhip, aiming
- **Dual Targeting Modes**:
  - **Right-click**: Full attack sequence (existing behavior)
  - **Ctrl+Right-click**: Target and hold at current hold state
- **State Management**:
  - **Default Hold State**: "aiming" (for new characters and weapon switches)
  - **Weapon Switch Reset**: Hold state automatically resets to "aiming" when changing weapons or combat modes
  - **Display Integration**: Current hold state shown in character stats (Shift+/)
- **Tactical Applications**:
  - **Threat Escalation**: Progress from holstered warning to full aim without committing to fire
  - **Response Positioning**: Pre-ready weapons for fast response to changing situations
  - **Intimidation Tactics**: Hold at "pointedfromhip" for visible but non-lethal threat
  - **Defensive Stances**: Maintain aiming position without automatic firing

### Firing Preference System (DevCycle 26)
- **SHIFT-F Toggle**: Each character can toggle between two firing modes for ranged weapons
  - **Aiming Mode** (default): Fires from "aiming" state with normal accuracy
  - **Point-from-Hip Mode**: Fires from "pointedfromhip" state with -20 hit penalty
- **Smart Context-Aware Switching**: Preference changes adapt to current weapon state
  - **Immediate Adjustment**: When at pointedfromhip/aiming, immediately switches to preferred state
  - **Non-Disruptive**: During firing/recovery, preference change queued until action completes
  - **Intuitive Progression**: For other states, preference affects next attack sequence targeting
- **State Progression Impact**: Weapon progression respects firing preference
  - **Aiming Mode**: Full progression through all states (slung → unsling → ready → pointedfromhip → aiming → firing)
  - **Point-from-Hip Mode**: Stops at pointedfromhip (slung → unsling → ready → pointedfromhip → firing)
- **Recovery Behavior**: After firing, recovery transitions back to preferred firing state
  - **Aiming Mode**: firing → recovering → aiming (existing behavior)
  - **Point-from-Hip Mode**: firing → recovering → pointedfromhip (new behavior)
- **Weapon Compatibility**: Applies to all ranged weapon types (pistols, rifles, submachine guns)
  - **Universal Support**: Works with holstered, slung, and sheathed weapon initial states
  - **Melee Exclusion**: Melee weapons unaffected, use existing combat system
- **User Interface Integration**: 
  - **Character Stats**: Firing preference displayed in Shift+/ character information
  - **Console Output**: Firing messages show "shootingfromaiming" or "shootingfromhip"
  - **Tactical Feedback**: Immediate visual confirmation of preference changes
- **Tactical Applications**:
  - **Quick Engagement**: Point-from-hip for faster weapon readiness at accuracy cost
  - **Precision Shooting**: Aiming mode for maximum accuracy in critical situations
  - **Situational Adaptation**: Switch preferences mid-combat based on tactical needs
  - **Character Specialization**: Set preferred firing styles for different character archetypes

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
- **Document Naming**: `DevCycle_2025_####_brainstorm.md`, `DevCycle_2025_####.md`, `DevCycle_2025_####_bugs_##.md`
- **Branch Pattern**: Work in `DC_##` branches, merge to main when complete

### DevCycle Closure Checklist (MANDATORY)
When closing ANY DevCycle, Claude MUST complete ALL of these steps:

1. ✅ **Verify all critical tests pass** - Run and verify all required tests pass:
   - `mvn test -Dtest=GunfightTestAutomated`
   - `mvn test -Dtest=BasicMissTestAutomated`
   - `mvn test -Dtest=BasicMissTestSimple`
2. ✅ **Update DevCycle document with final status and close-out summary**
3. ✅ **Commit final documentation updates**
4. ✅ **Switch to main branch** (`git checkout main`)
5. ✅ **Update CLAUDE.md with cycle completion** - Update "Last Completed Cycle" in Current Development Status section
6. ✅ **Archive plan documents to completed directory** - Move DevCycle documents from `plans/` to `plans/completed/`
7. ✅ **Merge development branch** (`git merge DC_##`)
8. ✅ **Delete development branch** (`git branch -d DC_##`)
9. ✅ **Verify clean status** (`git status`)

**🚨 CRITICAL**: The git merge step (#7) is MANDATORY and must NEVER be skipped. DevCycle is NOT closed until the branch is merged to main.

### Critical Test Requirements (MANDATORY)
**Critical Test Requirement**: The following tests must ALWAYS pass before ANY system or DevCycle can be considered complete.

**Required Tests:**
- ✅ **GunfightTestAutomated** - Core combat functionality and regression detection
- ✅ **BasicMissTestAutomated** - Miss calculation and basic combat mechanics
- ✅ **BasicMissTestSimple** - Simple miss test scenarios and validation

**Enforcement Rules:**
- ✅ **System Completion**: No system can be marked ✅ **COMPLETE** until ALL required tests pass
- ✅ **Cycle Closure**: No DevCycle can be closed until ALL required tests pass  
- ✅ **Verification Required**: Must run each test individually and verify success:
  - `mvn test -Dtest=GunfightTestAutomated`
  - `mvn test -Dtest=BasicMissTestAutomated`
  - `mvn test -Dtest=BasicMissTestSimple`
- ✅ **No Exceptions**: This rule applies to ALL systems and cycles, no exceptions allowed

**Rationale**: These tests represent core game functionality, combat mechanics, and regression detection. If any of these tests fail, it indicates fundamental issues that must be resolved before any completion.

### DevCycle Closure Enforcement
To prevent missing the branch merge step in future cycles:

1. **Use Closure Template**: Copy `/templates/DevCycle_Closure_Template.md` to end of DevCycle document before closure
2. **Add Mandatory Todos**: When user requests cycle closure, immediately add all git workflow tasks to todo list
3. **Double-Check Before Declaration**: Never declare a DevCycle "closed" until branch merge is verified complete
4. **Status Verification**: Always run `git status` and `git branch` to confirm main branch and clean state

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

### Development Cycle Closure Authorization
- **USER VERIFICATION REQUIRED**: Before executing any development cycle closure procedures, Claude must confirm the requesting user is the project owner (Edward T. Tonai)
- **CLOSURE COMMANDS RESTRICTED**: Development cycle closure includes git operations that modify repository state and must follow strict authorization
- **VERIFICATION PROCESS**: 
  1. **User Identity Confirmation**: Ask user to confirm they are Edward T. Tonai, the project owner
  2. **Explicit Closure Authorization**: Request explicit authorization to proceed with cycle closure procedures
  3. **Step-by-Step Confirmation**: Confirm each major step of the closure process before execution
- **SECURITY PRINCIPLE**: Only the project owner should have authority to close development cycles and merge branches to main

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

## Development Cycle Tracking

### Current Development Status
- **Last Completed Cycle**: DevCycle 37 - Iterative Development Cycle (Completed: July 3, 2025)
  - Enhanced development workflow with comprehensive CLAUDE.md cycle tracking and document location mapping
  - Improved combat realism with simplified reaiming system (15-tick delay on target incapacitation)  
  - Professional combat messaging with eliminated inappropriate "headshot kill" terminology
  - 3 systems completed in 5 hours across 2 days with all 24 tests passing

- **Current Active Cycle**: None
  - Status: Ready for next development cycle planning
  - All systems operational with clean codebase and comprehensive documentation

### Development Cycle Numbering System
- **Format**: DevCycle YYYY_NNNN (e.g., DevCycle 2025_0037)
- **Numbering**: Sequential numbering starting from 0001, zero-padded to 4 digits
- **Year Prefix**: Cycles are prefixed with the current year for organization
- **Brainstorm Documents**: `DevCycle_YYYY_NNNN_brainstorm.md` for planning phases
- **Bug Fix Documents**: `DevCycle_YYYY_NNNN_bugs_NN.md` for issue resolution during cycles

### DevCycle Number Determination (MANDATORY)
When creating a new DevCycle, Claude MUST follow this procedure:

1. ✅ **Read CLAUDE.md first** - Check "Current Development Status" section
2. ✅ **Use "Last Completed Cycle" number + 1** for new cycle number
3. ✅ **NEVER calculate cycle numbers from file directories** - CLAUDE.md is the authoritative source
4. ✅ **Add TodoWrite tasks** to enforce correct sequence before creating DevCycle document

**Example Process:**
- CLAUDE.md shows "Last Completed Cycle: DevCycle 37"
- Next cycle should be "DevCycle 38"
- Create todos to verify this process before proceeding

### System Creation vs. Implementation (MANDATORY)
When working with DevCycle systems, Claude MUST distinguish between planning and implementation:

**CREATING a system** = Planning only, no code changes whatsoever
**IMPLEMENTING a system** = Actual coding after plan approval

#### Mandatory Process for System Creation:
When user says "create a new system":
1. ✅ **Add TodoWrite tasks** that enforce correct sequence:
   - "Plan System X (do not implement)"
   - "Wait for user approval of plan" 
   - "Only implement when explicitly requested"
2. ✅ **Add planning section only** to DevCycle document
3. ✅ **Mark as ⭕ PLANNING status**
4. ✅ **STOP** - wait for explicit implementation request
5. ✅ **NEVER code without explicit "implement" instruction**

#### Implementation Authorization:
- ✅ **Only implement when user explicitly says**: "implement system X" or "please implement system X"
- ✅ **NEVER implement** when user says: "create system", "add system", "new system"
- ✅ **When in doubt, ask**: "Do you want me to (1) plan the system, or (2) plan AND implement it?"

#### Enforcement Rules:
- ✅ **NO EXCEPTIONS**: This rule applies to ALL system work, no matter how simple or obvious
- ✅ **VIOLATION CONSEQUENCES**: Unauthorized implementation violates project workflow
- ✅ **TODO VERIFICATION**: Check todos before any code changes to ensure proper authorization

### Cycle Completion Dates and Major Achievements
- **DevCycle 36** (July 2, 2025): Headless Testing Architecture - Complete JavaFX decoupling for automated testing
- **DevCycle 35** (June 2025): [Previous cycle details - to be populated from completed documents]
- **DevCycle 34** (June 2025): [Previous cycle details - to be populated from completed documents]

*Note: This tracking section should be updated whenever a development cycle is completed. The workflow document contains detailed procedures for maintaining this information.*

## Planning Document Structure

### Document Organization Overview
The project uses a comprehensive documentation system organized in the `plans/` directory with specific subdirectories and naming conventions for different types of planning documents.

### Document Location Reference

#### Current/Active Plan Documents
**Location**: `/mnt/c/dev/TTCombat/OF2Prototype01/plans/`

**Active Development Cycles**:
- `DevCycle_2025_0037.md` - Current iterative cycle for CLAUDE.md enhancements and workflow improvements

**Future Planning Documents**:
- `DevCycle_2025_future_005.md` - Future cycle planning for medium-term improvements
- `DevCycle_2025_future_010.md` - Future cycle planning for long-term enhancements
- `DevCycle_2025_test_001.md` - Test cycle template for experimental features

**Planning Support Documents**:
- `FuturePlans.md` - High-level future feature planning and roadmap
- `FutureTasks.md` - Task backlog and improvement opportunities
- `DevCycle_workflow_plan.md` - Development cycle process documentation and procedures

#### Completed Plan Documents
**Location**: `/mnt/c/dev/TTCombat/OF2Prototype01/plans/completed/`

**Recently Completed Cycles** (most recent first):
- `DevCycle_2025_0036.md` - Headless Testing Architecture (Complete)
- `DevCycle_2025_0035.md` - [Previous cycle - details in completed document]
- `DevCycle_2025_0034.md` - [Previous cycle - details in completed document]

**Document Types in Completed Directory**:
- **Main Cycle Documents**: `DevCycle_2025_NNNN.md` - Primary planning and implementation documentation
- **Brainstorm Documents**: `DevCycle_2025_NNNN_brainstorm.md` - Initial planning and ideation phase documentation
- **Bug Fix Documents**: `DevCycle_2025_NNNN_bugs_NN.md` - Issue resolution and debugging documentation
- **Analysis Documents**: `javafx_decoupling_analysis.md`, `Technical_Debt_Analysis.md` - Technical analysis and research

#### Plan Templates
**Location**: `/mnt/c/dev/TTCombat/OF2Prototype01/plans/`

**Available Templates**:
- `DevCycle_template.md` - Standard development cycle template for comprehensive feature implementation
- `DevCycle_iterative_template.md` - Iterative development cycle template for multiple independent systems
- `DevCycle_Closure_Template.md` - Template for proper cycle closure documentation and procedures

**Template Usage Guidelines**:
- **Standard Template**: Use for single large features or comprehensive system overhauls
- **Iterative Template**: Use for cycles with multiple independent improvements and bug fixes
- **Closure Template**: Use to ensure proper cycle completion with all required steps

### Document Naming Conventions
- **DevCycle Documents**: `DevCycle_YYYY_NNNN.md` (main implementation document)
- **Brainstorm Documents**: `DevCycle_YYYY_NNNN_brainstorm.md` (planning phase)
- **Bug Documents**: `DevCycle_YYYY_NNNN_bugs_NN.md` (issue resolution)
- **Future Planning**: `DevCycle_YYYY_future_NNN.md` (future cycle preparation)
- **Test Cycles**: `DevCycle_YYYY_test_NNN.md` (experimental or test implementations)

### Maintenance Guidelines
**Keeping Cycle Tracking Current**:
1. **Cycle Completion**: Update "Current Development Status" section when cycles are completed
2. **Document Archiving**: Move completed cycle documents to `plans/completed/` directory
3. **Status Updates**: Maintain accurate status information for current active cycle
4. **Achievement Recording**: Document major achievements and completion dates for historical reference

**Document Lifecycle Management**:
1. **Active Phase**: Documents remain in main `plans/` directory during development
2. **Completion Phase**: Move to `plans/completed/` directory when cycle is closed
3. **Template Updates**: Keep templates current with evolving workflow processes
4. **Reference Maintenance**: Ensure all documented paths and references remain valid

*This planning document structure enables efficient development cycle management, clear documentation organization, and comprehensive tracking of project evolution over time.*

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
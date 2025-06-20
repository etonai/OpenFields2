# DevCycle 2025-14 Brainstorm: Fix CTRL-A Character Addition in Edit Mode

## Issue Description

In Edit Mode, CTRL-A should trigger just adding a character by selecting a faction and number of characters to add. Somewhere along the way, the code changed such that CTRL-A begins by creating a character, which is then added. This is wrong. It also appears we lost the ability to add multiple characters and have them line up and select how closely they are lined up.

## Current Problem Analysis

### CTRL-A Behavior Issues
- **Current (Incorrect)**: CTRL-A creates a character first, then adds it
- **Expected (Correct)**: CTRL-A should directly add characters by faction selection
- **Lost Functionality**: Multiple character addition with lineup spacing control

### Missing Features
- Ability to add multiple characters at once
- Character lineup spacing control
- Proper faction-based character selection

## Additional Issue: Auto-Targeting Melee Behavior

When auto targeting in melee mode, a character will move towards its target if the target is out of range. Please modify this that the character will stop if its target becomes incapacitated.

### Melee Auto-Targeting Problems
- **Issue 1 - Incapacitated Targets**: Characters continue moving toward incapacitated targets
  - **Expected**: Characters should stop movement when target becomes incapacitated
  - **Impact**: Inefficient movement and unrealistic combat behavior

- **Issue 2 - Weapon Readiness**: When a melee character attacks a target out of range, the melee character moves to the target and then attacks
  - **Expected**: When the melee character begins moving towards its target, it should put its weapon into a ready state
  - **Impact**: Inefficient combat timing - weapon should be ready upon arrival

## Scope for DevCycle 14

### Primary Goals
1. Fix CTRL-A to directly add characters without creating them first
2. Restore multiple character addition capability
3. Restore character lineup and spacing controls
4. Fix melee auto-targeting to stop movement when target is incapacitated
5. Fix melee weapon readiness during movement to target

### Success Criteria
- CTRL-A triggers console-based faction selection for adding characters
- Users can specify number of characters to add (1-20 maximum)
- Characters line up with configurable spacing measured in feet
- No intermediate character creation step
- Melee characters stop pursuing incapacitated targets (both manual and auto-targeting)
- Melee characters immediately ready weapons when moving to targets

## Implementation Requirements

Based on user answers, the following requirements have been established:

### CTRL-A Character Addition Requirements
- **UI Method**: Console-based menu options (not JavaFX dialogs)
- **Character Limit**: Maximum 20 characters per addition
- **Spacing**: Distance measured in feet, configurable per addition
- **Faction Selection**: All factions of current theme available
- **Coordinate System**: Use existing coordinate system for positioning
- **Legacy Code**: Check for existing character spacing code that may still be present

### Melee Combat Behavior Requirements
- **Target Status Checking**: Continuous checking during movement (leveraging existing target location tracking)
- **Multiple Attackers**: All melee characters targeting incapacitated enemy should stop
- **Weapon Readiness**: Begin immediately when movement starts
- **Weapon State**: Target "Ready" state for melee weapons
- **Weapon Type Differences**: Use existing weapon type differences, do not add new ones
- **Scope**: Apply to both manual melee attacks and auto-targeting

### Research Tasks
- Find existing target status checking methods
- Locate original character spacing/lineup code
- Identify current CTRL-A handler implementation
- Analyze existing melee targeting and movement logic

### Testing Strategy
- **CTRL-A**: Test adding single character and multiple characters
- **Melee Combat**: Manual testing only
- **Edge Cases**: No special edge cases identified for melee targeting
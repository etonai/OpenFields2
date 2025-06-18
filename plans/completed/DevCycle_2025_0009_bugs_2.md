# Development Cycle 9 - Bug Fixes (Part 2)

**Document Version**: 1.0  
**Date Created**: 2025-01-17  
**Status**: Planning  

## Overview

This document tracks additional bugs discovered during Development Cycle 9 implementation and testing. This is a continuation of the original DevCycle_2025_0009_bugs.md document, focusing on newly identified issues that require attention.

## Bug Tracking

### BUG-9-002: Melee Weapon Range Calculation Issue

**Priority**: High  
**Status**: Identified  
**Category**: Combat System  

**Description**:
Melee weapon ranges appear to be calculated center-to-center between characters, which results in incorrect distance measurements. Characters that are visually touching are reported as being 3 feet away from each other, which puts them out of range for most melee weapons that have ranges of 1-3 feet.

**Impact**:
- Characters cannot perform melee attacks even when they appear to be in close contact
- Melee combat system is effectively non-functional due to range calculation errors
- Visual representation doesn't match the actual combat mechanics

**Expected Behavior**:
- Melee range should be calculated edge-to-edge (not center-to-center)
- Characters that are touching should be within range for short melee weapons (1-2 feet)
- Visual proximity should match functional combat range

**Technical Details**:
- Current range calculation appears to use center-to-center distance
- Solution: Implement edge-to-edge distance calculation for melee combat
- Need to investigate how character positioning and collision detection affects range calculation
- Must account for character radius/size when calculating melee weapon reach

**Reproduction Steps**:
1. Create two characters in edit mode
2. Position them so they are visually touching
3. Check the distance calculation used for melee combat
4. Observe that they are reported as 3+ feet apart

**Files Likely Involved**:
- Combat system range calculation methods
- Character positioning and collision detection
- Melee weapon attack validation logic

---

### BUG-9-003: Missing Melee Attack Movement System

**Priority**: High  
**Status**: Identified  
**Category**: Combat System  

**Description**:
When a character attempts a melee attack on a target that is out of range, the character should automatically move towards the target to get within melee range before attacking. Currently, characters do not move towards melee targets, making melee combat ineffective when characters are not already positioned optimally.

**Impact**:
- Melee attacks fail silently when targets are out of range
- Players must manually position characters before melee attacks
- Melee combat lacks the expected tactical movement behavior
- Combat feels disconnected from standard tactical game expectations

**Expected Behavior**:
- When a melee attack is commanded on an out-of-range target, the attacker should move towards the target
- Movement should stop when the attacker is within melee weapon range
- Attack should execute automatically once in range
- Movement should respect pathing and obstacles
- Should work similarly to how ranged attacks might require positioning

**Technical Details**:
- Need to implement melee attack movement logic in combat system
- May require integration with existing movement system
- Should consider weapon reach when determining "in range" status
- May need to queue attack action after movement completion

**Reproduction Steps**:
1. Create two characters with melee weapons
2. Position them more than weapon reach apart
3. Command one character to attack the other with melee weapon
4. Observe that no movement towards target occurs
5. Attack either fails or does nothing

**Files Likely Involved**:
- Melee combat attack initiation logic
- Character movement system
- Attack command processing
- Weapon range validation

---

### BUG-9-004: Missing Melee Weapon Selection in Character Creation (CTRL-A)

**Priority**: Medium  
**Status**: Identified  
**Category**: Character Creation System  

**Description**:
When a character is added to the game using CTRL-A (character creation), the system requires the player to select a ranged weapon but does not provide an option to select a melee weapon. Since characters now support both ranged and melee weapons, the character creation process should include melee weapon selection to match the ranged weapon selection workflow.

**Impact**:
- New characters created via CTRL-A only get default/no melee weapons
- Inconsistent weapon assignment process between ranged and melee weapons
- Players must use separate edit mode weapon assignment after character creation
- Character creation workflow is incomplete for the dual weapon system

**Expected Behavior**:
- Character creation (CTRL-A) should prompt for both ranged and melee weapon selection
- Melee weapon selection should follow the same pattern as ranged weapon selection
- Players should be able to choose melee weapons from available options during creation
- Character creation should result in fully equipped characters with both weapon types

**Technical Details**:
- Character creation workflow currently handles ranged weapon selection
- Need to integrate melee weapon selection into the same workflow
- Should reuse existing melee weapon selection UI components
- May need to extend character creation state management

**Reproduction Steps**:
1. Press CTRL-A to start character creation
2. Go through archetype selection
3. Observe ranged weapon selection prompt
4. Notice no melee weapon selection prompt
5. Complete character creation and verify character has no melee weapon assigned

**Files Likely Involved**:
- Character creation workflow (InputManager character creation handling)
- Edit mode controller character creation methods
- Character archetype assignment logic
- Weapon selection UI components

---

### BUG-9-005: Ranged Weapons Start with No Ammunition and Characters Not Reloading

**Priority**: High  
**Status**: Identified  
**Category**: Combat System  

**Description**:
Ranged weapons appear to be starting with no ammunition loaded, and characters are not automatically reloading their weapons when they run out of ammunition. This makes ranged combat non-functional as characters cannot fire their weapons.

**Impact**:
- Ranged combat is completely non-functional
- Characters cannot engage in ranged attacks
- Weapons may appear ready but cannot fire due to lack of ammunition
- No automatic reload behavior when ammunition is depleted

**Expected Behavior**:
- Ranged weapons should start with ammunition loaded (full magazine/chamber)
- Characters should automatically reload when ammunition runs out
- Reload process should follow weapon-specific timing and behavior
- Players should see clear indication of ammunition status and reload actions

**Technical Details**:
- Need to investigate weapon initialization and ammunition assignment
- Check if ammunition system is properly integrated with weapon creation
- Verify reload mechanics are implemented and triggered correctly
- May need to review weapon state management during reload cycles

**Reproduction Steps**:
1. Create a character with a ranged weapon
2. Attempt to fire the weapon at a target
3. Observe that weapon cannot fire (likely due to no ammunition)
4. Check if character attempts to reload automatically
5. Verify ammunition status and reload behavior

**Files Likely Involved**:
- Weapon initialization and creation logic
- Ammunition system integration
- Weapon state management during combat
- Reload mechanics and timing
- WeaponFactory weapon setup

---

### ENH-9-001: Add Civil War Melee Weapons to Test Theme

**Priority**: Low  
**Status**: Identified  
**Category**: Enhancement - Content  

**Description**:
The test theme's melee-weapons.json file should include all melee weapons that are available in the civil war theme. This would provide a more comprehensive testing environment and ensure all weapon types are available for development and testing purposes.

**Impact**:
- Test theme has limited melee weapon variety compared to civil war theme
- Developers may miss testing certain weapon types or combinations
- Inconsistent weapon availability between themes for testing
- Enhanced testing capabilities with broader weapon selection

**Expected Behavior**:
- Test theme melee-weapons.json should contain all weapons from civil war theme
- Both theme-specific and universal weapons should be available in test theme
- Test theme should serve as comprehensive development environment
- Weapon IDs should remain consistent between themes where applicable

**Technical Details**:
- Copy melee weapons from civil war theme melee-weapons.json to test theme
- Ensure weapon IDs don't conflict with existing test theme weapons
- May need to adjust weapon names or descriptions for test theme context
- Verify weapon balance and stats are appropriate for testing

**Files Involved**:
- `/src/main/resources/data/themes/test_theme/melee-weapons.json`
- `/src/main/resources/data/themes/civil_war/melee-weapons.json` (source)

**Implementation Steps**:
1. Review current test theme melee weapons
2. Review civil war theme melee weapons
3. Merge weapon lists, avoiding ID conflicts
4. Test weapon loading and selection in test theme
5. Verify all weapons function correctly

---

## Implementation Notes

### Next Steps
1. Investigate current range calculation implementation (BUG-9-002)
2. Identify where center-to-center vs edge-to-edge calculation occurs (BUG-9-002)
3. Determine appropriate solution (adjust calculation vs adjust weapon ranges) (BUG-9-002)
4. Research current melee attack command processing (BUG-9-003)
5. Design melee attack movement system integration (BUG-9-003)
6. Investigate character creation workflow and weapon selection (BUG-9-004)
7. Design melee weapon selection integration for CTRL-A workflow (BUG-9-004)
8. Investigate weapon initialization and ammunition system (BUG-9-005)
9. Fix ammunition loading and reload mechanics (BUG-9-005)
10. Merge civil war melee weapons into test theme (ENH-9-001)
11. Test solutions with various character positions and weapon types

### Testing Requirements
- Verify melee attacks work at appropriate visual distances (BUG-9-002)
- Test with different melee weapon types (short, medium, long) (BUG-9-002, BUG-9-003)
- Ensure fixes don't break ranged weapon calculations (BUG-9-002)
- Validate character collision detection still works properly (BUG-9-002)
- Test automatic movement to melee targets at various distances (BUG-9-003)
- Verify attack execution after movement completion (BUG-9-003)
- Test movement pathing with obstacles between attacker and target (BUG-9-003)
- Test character creation (CTRL-A) with both ranged and melee weapon selection (BUG-9-004)
- Verify created characters have both weapon types properly assigned (BUG-9-004)
- Test ranged weapon ammunition initialization and firing (BUG-9-005)
- Verify automatic reload behavior when ammunition is depleted (BUG-9-005)
- Test expanded melee weapon selection in test theme (ENH-9-001)
- Verify all civil war melee weapons function correctly in test theme (ENH-9-001)

---

## Document History

- **v1.0** (2025-01-17): Initial document creation with BUG-9-002 melee range calculation issue
- **v1.1** (2025-01-17): Added BUG-9-003 missing melee attack movement system
- **v1.2** (2025-01-17): Added BUG-9-004 missing melee weapon selection in character creation (CTRL-A)
- **v1.3** (2025-01-17): Added ENH-9-001 enhancement to add civil war melee weapons to test theme
- **v1.4** (2025-01-17): Added BUG-9-005 ranged weapons start with no ammunition and characters not reloading

---

## Implementation Questions and Considerations

After deep analysis of the documented bugs and enhancement, the following questions and considerations have been identified:

### BUG-9-002 (Melee Range Calculation) Questions:
1. **Character Size/Radius**: What is the actual character radius/size used in the game for collision detection? This directly affects edge-to-edge calculations.
- We don't have collision detection. The current character radius is 1.5 feet, translated to pixels.
2. **Character Variation**: Should different character types or archetypes have different sizes/radii that affect melee range?
- Don't worry about this right now.
3. **Backwards Compatibility**: Does the edge-to-edge calculation fix need to maintain compatibility with existing ranged weapon calculations?
- Technically this is true, but we can ignore it for ranged weapons for now.
4. **Visual Feedback**: Are there any visual indicators (range circles, highlighting) that show weapon range to players? These would need updating too.
- No
5. **Collision Integration**: How does the new range calculation integrate with existing collision detection without breaking movement?
- We don't do collision detection.

### BUG-9-003 (Melee Attack Movement) Questions:
1. **Pathfinding System**: Is there an existing pathfinding system in the game, or does one need to be implemented?
- There is no pathfinding. Just move in the direction of the target.
2. **Moving Targets**: What happens if the target moves while the attacker is moving towards them? Should the attacker continue tracking?
- Yes, the attacker should continue tracking and change direction to match the target's new position.
3. **Multiple Attackers**: How should the system handle multiple characters trying to move to attack the same target simultaneously?
- Do not worry about any concerns with multiple attackers.
4. **Movement Limits**: Should there be a maximum movement distance for melee attacks to prevent characters from crossing the entire map?
- Not yet.
5. **Turn vs Real-time**: How does automatic movement integrate with the game's timing system (tick-based, turn-based, real-time)?
- Automatic movement should be handled like normal movement. This can probably be done by just adjusting the attacker's movement target location to the defender's location.
6. **Path Blocking**: What happens if the path to the target is blocked by other units or obstacles?
- Do not worry about this right now.
7. **Interruption**: Should the movement be interruptible by player commands or enemy actions?
- THe player will be able to change the action if the player desires.
8. **Animation/Feedback**: How should the automatic movement be visually communicated to players?
- No.

### BUG-9-004 (Character Creation Melee Weapons) Questions:
1. **Selection Order**: Should melee weapon selection occur before or after ranged weapon selection in the workflow?
- After ranged weapon
2. **Optional Selection**: Should players be able to skip melee weapon selection, or is it mandatory?
- It is mandatory. PLayers can select unarmed, though.
3. **Archetype Integration**: Should certain archetypes have recommended or restricted melee weapon options?
- Not at this time.
4. **UI Flow**: How does adding melee weapon selection affect the timing and user experience of character creation?
- Don't worry about it.
5. **Default Behavior**: What happens if a player cancels out of melee weapon selection?
- Then cancel out of adding a charcter.

### BUG-9-005 (Ammunition System) Questions:
1. **Regression Check**: Is this a new issue or a regression from a previous working version?
- Regression
2. **Ammunition Types**: Are there different ammunition types for different weapons that need separate handling?
- No
3. **Spare Ammunition**: Should characters carry spare ammunition for multiple reloads?
- Characters have unlimited spare ammunition
4. **Reload Timing**: How should reload timing integrate with the game's 60-tick-per-second system?
- Reload timing is already handled.
5. **Reload Control**: Should reloading be automatic, manual, or player-configurable?
- Reloading is already automatic
6. **User Feedback**: Are there visual/audio cues for low ammunition warnings and reload actions?
- no
7. **Weapon States**: How does ammunition tracking integrate with existing weapon state management?
- Ammunition tracking already exists.

### ENH-9-001 (Civil War Weapons in Test Theme) Questions:
1. **Contextual Naming**: Should weapon names be modified to fit the test theme context, or kept as-is?
- Kept as-is.
2. **Balance Considerations**: Are the civil war weapon stats appropriate for test theme balance?
- Yes. 
3. **ID Conflicts**: How should weapon ID conflicts be resolved when merging weapon lists?
- If there is an id conflict, change one of the IDs. It doesn't matter which one.

### Cross-Cutting Implementation Questions:
1. **Implementation Order**: Are there dependencies between these bugs that require a specific implementation order?
- Handle them in the order listed.
2. **Performance Impact**: What are the performance implications of the movement system (BUG-9-003) on the game loop?
- Don't worry about it.
3. **Testing Strategy**: How should the fixes be tested in isolation vs. integration testing?
- Don't worry about it.
4. **Error Handling**: How should edge cases and error conditions be handled for each fix?
- Don't worry about it.
5. **User Experience**: How do these changes affect the overall game flow and user experience?
- Don't worry about it.
6. **Save Game Compatibility**: Do any of these changes affect save game compatibility?
- IT shouldn't.
7. **Theme Consistency**: How do the fixes ensure consistency across different game themes?
- DOn't worry about it.

### Priority and Risk Assessment Questions:
1. **Critical Path**: Which bugs are blocking other functionality and should be prioritized?
- Fix in order
2. **Risk Mitigation**: What are the risks of each implementation, and how can they be mitigated?
- Don't worry about it.
3. **Rollback Strategy**: If an implementation causes issues, what is the rollback strategy?
- I will roll back myself.
4. **Testing Resources**: What testing resources are needed for each bug fix?
- Don't worry about it.
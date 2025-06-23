# DevCycle 2025_0015f - Ranged Attack Movement Bug Fix

*Created: June 21, 2025 | Implementation Status: Planning*

## Overview

Fix critical bugs in the ranged combat system identified during testing. Two major issues prevent proper ranged combat functionality: characters incorrectly moving towards targets during manual ranged attacks (should remain stationary) and null pointer exceptions during weapon firing that break audio feedback.

**EDNOTE**: Combat movement is not always broken. Auto targeting ranged attacks work correctly - characters don't move and sound outputs correctly. The specific problem occurs when a character manually targets a ranged attack.

**Development Cycle Goals:**
- **Priority 1**: Fix ranged attack movement bug for manual targeting - characters should stay stationary during ranged attacks
- **Priority 2**: Resolve null pointer exception in weapon sound callback system  
- Maintain all existing melee combat functionality (characters should still move for melee)
- Maintain working auto targeting functionality
- Ensure zero regressions in combat system functionality

**Prerequisites:**
- DevCycle 15e completed (InputManager refactoring operational)
- Combat system baseline established
- Audio system baseline established

**Estimated Complexity:** Medium - Critical bug fixes affecting core combat mechanics

## Bug Analysis Summary

### Critical Bug #1: Manual Ranged Attack Movement Issue  
**Problem**: Characters move towards targets during manual ranged attacks instead of staying stationary
**Expected**: Character remains in position during ranged weapon sequence (like auto targeting)
**Actual**: Character moves towards target as if initiating melee combat
**Impact**: Breaks fundamental ranged vs melee combat distinction for manual targeting
**Working Case**: Auto targeting ranged attacks work correctly - no movement, sound works properly
**Broken Case**: Manual targeting ranged attacks cause unwanted movement

### Critical Bug #2: Null Pointer Exception in Audio
**Problem**: Game throws exception when weapon firing attempts to play sound
```
Exception: Cannot invoke "game.GameCallbacks.playWeaponSound(combat.Weapon)" 
because "gameCallbacks" is null at combat.Character.lambda$scheduleFiring$4(Character.java:1063)
```
**Impact**: Audio feedback lost, exception spam in console

### Evidence from Testing
```
Selected: 1:Alice
*** 1000:Alice targeting 1003:Drake for ranged attack ***
1000:Alice weapon state: unsling at tick 0
1000:Alice weapon state: ready at tick 86
1000:Alice weapon state: aiming at tick 101
1000:Alice weapon state: firing at tick 131
*** 1000:Alice fires a 9mm round from Uzi Submachine Gun (ammo remaining: 31)
[NULL POINTER EXCEPTION OCCURS]
```

## Implementation Plan

**Implementation Priority**: Movement bug fix first (Priority 1), then audio callback fix (Priority 2)
**Scope**: Single DevCycle, minimal fixes focused on specific issues
**Key Insight**: Manual vs auto targeting behaves differently - investigate manual targeting code path

### Phase 1: Manual Ranged Attack Movement Bug Investigation and Fix (6 hours)

#### Step 1.1: Manual vs Auto Targeting Analysis (2 hours)
**Objective**: Understand why manual targeting causes movement but auto targeting doesn't

**Investigation Tasks:**
- Compare code paths: manual targeting vs auto targeting ranged attacks
- Examine CombatCommandProcessor changes related to manual targeting
- Trace manual target assignment from user input to character movement
- Review differences in `combatTarget` vs `movementTarget` assignment

**Expected Findings:**
- Manual targeting incorrectly sets movement target in addition to combat target
- Auto targeting bypasses movement target assignment
- Recent CombatCommandProcessor changes affected manual targeting logic

#### Step 1.2: Combat Target Separation Fix (2 hours)
**Objective**: Separate combat targeting from movement targeting in manual ranged attacks

**Implementation Tasks:**
- Implement `combatTarget` and `movementTarget` separation in Unit class
- Fix manual targeting to only set combat target, not movement target
- Ensure ranged attacks don't trigger movement commands
- Maintain melee combat movement functionality

**Success Criteria:**
- Manual ranged attacks: character stays stationary (like auto targeting)
- Melee attacks: movement works as expected
- Combat target assignment doesn't affect movement target

#### Step 1.3: Manual Targeting Code Path Fix (2 hours)
**Objective**: Fix the specific manual targeting logic that causes unwanted movement

**Implementation Tasks:**
- Locate and fix the manual targeting code in CombatCommandProcessor
- Ensure proper combat mode setting for manual ranged attacks
- Add validation to prevent movement during ranged weapon states
- Test manual targeting behavior matches auto targeting behavior

**Success Criteria:**
- Manual targeting works identically to auto targeting (no movement)
- `isMeleeCombatMode` correctly set for manual ranged attacks
- No regressions in auto targeting functionality

### Phase 2: Null Pointer Exception Investigation and Fix (4 hours)

#### Step 2.1: Audio Callback Root Cause Analysis (1 hour)
**Objective**: Identify why `gameCallbacks` is null in Character instances during manual targeting

**Investigation Tasks:**
- Examine `Character.java:1063` and surrounding `scheduleFiring` lambda
- Trace `gameCallbacks` assignment in Character creation/initialization
- Check if manual targeting creates characters differently than auto targeting
- Review DevCycle 15e changes that might have affected callback chains

**Expected Findings:**
- Missing callback assignment during character creation
- Broken callback chain in refactored components
- Manual targeting code path missing callback setup

#### Step 2.2: Callback Assignment Fix (2 hours)
**Objective**: Ensure all Character instances receive proper gameCallbacks reference

**Implementation Tasks:**
- Add/fix gameCallbacks assignment in Character creation
- Verify callback assignment in CharacterFactory
- Add defensive null checks before `gameCallbacks.playWeaponSound()` calls
- Test callback chain for both manual and auto targeting

**Success Criteria:**
- All Character instances have non-null gameCallbacks
- Weapon firing plays sounds without exceptions
- Both manual and auto targeting have working audio

#### Step 2.3: Critical Error Handling (1 hour)
**Objective**: Treat missing callbacks as critical errors with proper logging

**Implementation Tasks:**
- Add validation to detect null callbacks at runtime
- Log critical errors when callbacks are missing
- Fail fast if callbacks are not properly initialized
- Focus only on weapon sound callback (not expanding to other callbacks)

**Success Criteria:**
- Clear error messages when callback issues occur
- No silent failures in audio system
- Missing callbacks treated as critical errors

### Phase 3: Integration Testing and Validation (4 hours)

#### Step 3.1: Functional Testing (2 hours)
**Objective**: Verify both bugs are fixed and no regressions introduced

**Test Cases:**
- **Manual Ranged Attack Test**: Character manually targets ranged attack, remains stationary (like auto targeting)
- **Auto Ranged Attack Test**: Verify auto targeting still works correctly (regression test)
- **Melee Attack Test**: Character initiates melee attack, moves to target as expected
- **Audio Test**: Weapon sounds play correctly for both manual and auto targeting
- **Pistol and Rifle Test**: Test both weapon types as specified

**Success Criteria:**
- Manual targeting behaves identically to auto targeting (no movement)
- Audio feedback works correctly for both targeting modes
- No regressions in existing auto targeting functionality

#### Step 3.2: Regression Testing (2 hours)
**Objective**: Ensure no existing functionality is broken by the fixes

**Test Procedures:**
- Run DevCycle 15b validation procedures
- Test pistol and rifle weapons (as specified)
- Focus on single unit testing (no multi-unit scenarios needed)
- Test combat during different game states (paused, edit mode, etc.)
- No performance benchmarking required

**Success Criteria:**
- DevCycle 15b tests pass completely
- All combat functionality preserved
- Auto targeting continues to work correctly
- Manual targeting now works like auto targeting

## Technical Implementation Details

### Callback System Fix

**Target Files:**
- `Character.java` - Fix null pointer at line 1063
- `CharacterFactory.java` - Ensure callback assignment during creation
- `OpenFields2.java` - Verify callback chain initialization

**Key Changes:**
- Add `setGameCallbacks(GameCallbacks callbacks)` method to Character if missing
- Call `character.setGameCallbacks(this)` during character creation
- Add null checks in firing lambda: `if (gameCallbacks != null) gameCallbacks.playWeaponSound(weapon)`

### Manual Targeting Movement Fix

**Target Files:**
- `CombatCommandProcessor.java` - Fix manual targeting code path  
- `Unit.java` - Implement combatTarget/movementTarget separation
- Related input processing files for manual targeting

**Key Changes:**
- Implement `combatTarget` and `movementTarget` separation in Unit class
- Fix manual targeting to only set combat target, not movement target  
- Ensure manual targeting behaves like auto targeting (no movement)
- Set `character.isMeleeCombatMode = false` correctly for manual ranged attacks
- Maintain melee combat movement functionality

### Integration Points

**Component Coordination:**
- CombatCommandProcessor manual targeting matches auto targeting behavior
- Character combat mode reflects actual attack type for both targeting modes
- Unit movement system respects combatTarget vs movementTarget separation
- Audio system receives proper callbacks for both targeting modes

**State Management:**
- Combat state properly tracked through InputStateTracker
- Movement state separated from combat state via target separation
- Manual targeting doesn't trigger unwanted movement (like auto targeting)

## Success Metrics

### Functional Requirements
- **Manual ranged attacks work correctly**: Character stays stationary during manual ranged attacks (like auto targeting)
- **Auto targeting preserved**: Auto targeting continues to work correctly (no regressions)
- **No exceptions**: Weapon firing completes without null pointer exceptions for both targeting modes
- **Audio works**: Weapon sounds play correctly during all firing events
- **Melee unchanged**: Melee attacks continue to work as expected (character moves to target)

### Quality Requirements
- **Zero regressions**: All existing functionality preserved from DevCycle 15e
- **Performance maintained**: 60 FPS performance requirements met during combat
- **Error handling**: Graceful handling of edge cases and error conditions
- **Code quality**: Clean, maintainable fixes that don't introduce technical debt

### Validation Requirements
- **DevCycle 15b tests pass**: Existing validation procedures confirm no regressions
- **Combat system tests pass**: All combat functionality works correctly
- **Audio system tests pass**: All audio feedback functions properly
- **Integration tests pass**: Component interactions work correctly

## Risk Assessment

### Critical Risks
- **Combat system regressions**: Fix might break other combat functionality
- **Audio system impact**: Callback fixes might affect other audio operations
- **Performance degradation**: Additional checks might impact 60 FPS requirements

### Medium Risks
- **Complex debugging**: Root cause might be deeper than initial analysis suggests
- **Multiple component changes**: Fix might require changes across several files
- **Testing complexity**: Comprehensive testing needed to ensure no side effects

### Low Risks
- **Development time**: Bugs appear to be isolated and fixable
- **Architecture impact**: Fixes should not require major architectural changes
- **User impact**: Issues are clearly defined with reproducible test cases

### Mitigation Strategies
- **Incremental fixes**: Fix one bug at a time with testing between fixes
- **Comprehensive testing**: Apply DevCycle 15b framework after each fix
- **Performance monitoring**: Continuous validation of performance requirements
- **Rollback capability**: Git branch strategy allows safe experimentation

## Testing Strategy

### Unit Testing
- **Character callback testing**: Verify callback assignment and null handling
- **Combat mode testing**: Test combat type determination logic
- **Movement restriction testing**: Verify movement blocked during ranged attacks
- **Audio system testing**: Test weapon sound callback functionality

### Integration Testing
- **Ranged combat workflow**: End-to-end ranged attack without movement
- **Melee combat workflow**: End-to-end melee attack with movement
- **Combat transition testing**: Switching between attack types
- **Multi-unit testing**: Multiple characters in different combat modes

### Regression Testing
- **DevCycle 15b procedures**: Complete validation of existing functionality
- **Combat system testing**: All weapon types and combat scenarios
- **Input system testing**: Verify input processing still works correctly
- **Performance testing**: Maintain 60 FPS during all combat operations

## Long-term Impact

### Immediate Benefits
- **Ranged combat functional**: Players can effectively use ranged weapons
- **Stable audio**: No more exceptions disrupting gameplay
- **Clear combat distinction**: Ranged vs melee behavior is consistent and predictable

### Architecture Improvements
- **Robust callback system**: Better error handling for callback chains
- **Clear combat types**: Improved separation between ranged and melee combat logic
- **Movement coordination**: Better integration between combat and movement systems

### Future Development
- **Combat system foundation**: Solid base for future combat enhancements
- **Audio system reliability**: Stable foundation for additional audio features
- **Testing framework**: Enhanced validation procedures for combat functionality

## Conclusion

DevCycle 15f addresses critical bugs that prevent proper ranged combat functionality. The fixes target specific issues in callback handling and combat type determination while preserving all existing functionality. The implementation plan provides a systematic approach to resolving both issues with comprehensive testing to ensure no regressions.

The successful completion of this cycle will restore full combat functionality and provide a stable foundation for future combat system development.

---

*DevCycle 15f represents essential bug fixes to maintain core gameplay functionality following the major InputManager refactoring completed in DevCycle 15e.*

## Implementation Decisions (Based on User Input)

### Priority and Scope
- **Priority 1**: Movement bug fix (manual targeting causing unwanted movement)
- **Priority 2**: Audio callback null pointer exception
- **Scope**: Single DevCycle with minimal fixes focused on specific issues
- **Testing**: Pistol and rifle weapons, single unit testing, no performance benchmarking

### Technical Approach
- **Target Separation**: Implement `combatTarget` and `movementTarget` separation in Unit class
- **Movement Restrictions**: Do NOT restrict movement during combat states
- **Callback Handling**: Treat missing callbacks as critical errors, use defensive programming (null checks)
- **Focus**: Only weapon sound callback, not expanding to other callbacks

### Investigation Focus
- **Root Cause**: Manual vs auto targeting code path differences
- **Suspected Location**: Recent CombatCommandProcessor changes
- **Key Insight**: Auto targeting works correctly, manual targeting is broken
- **No Investigation Needed**: Character creation methods, edge cases, multi-unit scenarios

### Architecture Decisions
- **Minimal Fixes**: Focus on specific bugs, not broader architecture improvements
- **No Debug Logging**: Not adding debug logging at this time
- **No Combat/Movement Separation**: Not establishing broader separation of combat and movement logic
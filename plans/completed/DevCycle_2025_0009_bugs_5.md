# Development Cycle 9 - Bug Fixes (Part 5)

**Document Version**: 1.0  
**Date Created**: 2025-01-17  
**Status**: Planning  

## Overview

This document tracks a critical gameplay issue with melee combat where characters fail to automatically move toward targets when they are out of melee range. This breaks the fundamental melee combat flow and prevents effective close-combat gameplay. The issue was identified during testing where a character would switch to melee mode but remain stationary when the target was beyond weapon reach.

## Bug Tracking

### BUG-9-013: Melee Attackers Do Not Move Toward Out-of-Range Targets

**Priority**: Critical  
**Status**: Identified  
**Category**: Gameplay - Melee Combat  

**Description**:
When a character attempts a melee attack on a target that is beyond their weapon's reach, the system correctly identifies the range limitation but fails to initiate automatic movement toward the target. This results in the character becoming stuck in melee mode without taking any action, breaking the combat flow.

**Reproduction Steps**:
1. Select a character and switch to melee combat mode
2. Right-click on an enemy target that is beyond melee weapon reach
3. Observe that the character identifies the target but does not move toward it

**Evidence from Test Output**:
```
Selected: 1:Alice
Health: 11/11, Faction: Red, Weapon: Uzi Submachine Gun, Position: Standing
Movement: Walk, Aiming: Normal, Hesitation: 0.0s (Wound: 0.0s, Bravery: 0.0s)
*** 1000:Alice switched to Melee Combat mode
*** 1000:Alice cannot reach 1003:Drake
*** Target distance: 42.86 feet, weapon reach: 2.00 feet
ATTACK 1 units target 1003:Drake (Unit ID: 4)
```

**Technical Analysis**:

**Root Cause Location**: `InputManager.java:2747`
```java
// TODO: Add automatic movement toward target when out of range
return;
```

**Current Flow**:
1. User commands melee attack on target
2. System switches character to melee combat mode ✓
3. System calculates distance to target ✓
4. System identifies target is out of range ✓
5. System displays "cannot reach" message ✓
6. **System terminates attack sequence** ❌
7. **No movement toward target is initiated** ❌

**Expected Flow**:
1. User commands melee attack on target
2. System switches character to melee combat mode ✓
3. System calculates distance to target ✓
4. System identifies target is out of range ✓
5. **System initiates movement toward target** ❌ (Missing)
6. **System monitors approach progress** ❌ (Missing)
7. **System initiates attack when in range** ❌ (Missing)

**Impact Analysis**:

**Gameplay Impact**:
- **Broken Melee Combat**: Players cannot effectively use melee weapons
- **Poor User Experience**: Characters appear "stuck" or unresponsive
- **Tactical Limitation**: Melee units become ineffective beyond starting positions
- **Combat Flow Disruption**: Mixed combat scenarios break down

**Technical Impact**:
- **Incomplete Feature**: Melee combat system is partially implemented
- **User Frustration**: System appears to ignore valid commands
- **Testing Complications**: Melee combat scenarios cannot be properly tested

**Affected Components**:

**Primary Affected File**:
- `src/main/java/InputManager.java:2730-2755` - `startMeleeAttackSequence()` method

**Related Systems**:
- Movement system (`Unit.setTarget()`) - Available but not utilized
- Melee combat system (`Character.startMeleeAttackSequence()`) - Ready but not reached
- Range calculation system (`CombatResolver.isInMeleeRange()`) - Working correctly

**Detailed Technical Analysis**:

**Current Method Analysis** (`InputManager.java:2730-2755`):
```java
private void startMeleeAttackSequence(Unit attacker, Unit target) {
    MeleeWeapon meleeWeapon = attacker.character.meleeWeapon;
    if (meleeWeapon == null) {
        System.out.println("*** " + attacker.character.getDisplayName() + " has no melee weapon equipped");
        return; // ✓ Appropriate handling
    }
    
    // Check if target is within melee range
    CombatResolver combatResolver = new CombatResolver(units, eventQueue, false);
    if (!combatResolver.isInMeleeRange(attacker, target, meleeWeapon)) {
        double distance = Math.hypot(target.x - attacker.x, target.y - attacker.y);
        double distanceFeet = distance / 7.0; // Convert pixels to feet
        double maxReach = meleeWeapon.getTotalReach();
        
        System.out.println("*** " + attacker.character.getDisplayName() + " cannot reach " + target.character.getDisplayName());
        System.out.println("*** Target distance: " + String.format("%.2f", distanceFeet) + " feet, weapon reach: " + String.format("%.2f", maxReach) + " feet");
        
        // TODO: Add automatic movement toward target when out of range
        return; // ❌ PROBLEM: Terminates without movement
    }
    
    // Schedule melee attack based on weapon state
    attacker.character.startMeleeAttackSequence(attacker, target, gameClock.getCurrentTick(), eventQueue, attacker.getId(), (GameCallbacks) callbacks);
    
    System.out.println("*** " + attacker.character.getDisplayName() + " begins melee attack on " + target.character.getDisplayName() + " with " + meleeWeapon.getName());
}
```

**Available Movement System Analysis**:
The game already has a working movement system that can be utilized:

**Unit Movement Methods** (`game/Unit.java`):
```java
public void setTarget(double x, double y) {
    this.targetX = x;
    this.targetY = y;
    this.hasTarget = true;
    this.isStopped = false;
}
```

**Existing Movement Logic** (`InputManager.java:453`):
```java
unit.setTarget(newTargetX, newTargetY); // Already used for normal movement
```

**Range Calculation Available** (Current implementation):
```java
double distance = Math.hypot(target.x - attacker.x, target.y - attacker.y);
double distanceFeet = distance / 7.0;
double maxReach = meleeWeapon.getTotalReach();
```

## Implementation Requirements

### Core Requirements:

1. **Automatic Movement Initiation**:
   - When target is out of melee range, calculate movement path toward target
   - Initiate movement using existing `Unit.setTarget()` method
   - Maintain melee attack intent during movement

2. **Movement Targeting**:
   - Calculate optimal position within melee range of target
   - Account for weapon reach and character radius
   - Position attacker at edge of weapon range for maximum flexibility

3. **Progress Monitoring**:
   - Monitor movement progress toward target
   - Check range periodically during movement
   - Initiate attack when target comes within range

4. **State Management**:
   - Maintain "moving to melee" state during approach
   - Handle interruptions (target moves, gets incapacitated, etc.)
   - Clean up state when movement completes or is cancelled

### Advanced Requirements:

5. **Target Movement Handling**:
   - Continuously update movement target if enemy moves
   - Recalculate approach path when target relocates
   - Handle scenarios where target moves out of pursuit range

6. **Collision Avoidance**:
   - Ensure movement path doesn't collide with other units
   - Handle blocked paths to target
   - Implement fallback positioning strategies

7. **Animation Integration**:
   - Ensure proper facing direction during movement
   - Smooth transition from movement to attack animation
   - Visual feedback for "approaching for melee" state

## Implementation Strategy

### Phase 1: Basic Movement Implementation (2-3 hours)
**Goal**: Implement automatic movement toward out-of-range melee targets

**Tasks**:
1. Replace `return;` statement in `startMeleeAttackSequence()` with movement logic
2. Calculate optimal approach position within melee range
3. Use `Unit.setTarget()` to initiate movement toward target
4. Add debug output for movement initiation

**Success Criteria**:
- Characters automatically move toward out-of-range melee targets
- Movement stops when character reaches weapon range
- Basic functionality works for stationary targets

### Phase 2: State Management Enhancement (2-3 hours)
**Goal**: Properly manage melee attack state during movement

**Tasks**:
1. Add "moving to melee" state tracking to Character class
2. Implement periodic range checking during movement
3. Trigger attack when target comes within range
4. Handle movement completion and state cleanup

**Success Criteria**:
- Characters maintain melee intent during movement
- Attack automatically triggers when range is achieved
- State is properly cleaned up after attack or cancellation

### Phase 3: Dynamic Target Tracking (2-3 hours)
**Goal**: Handle moving targets and changing conditions

**Tasks**:
1. Implement target position monitoring during movement
2. Update movement target when enemy relocates
3. Handle target incapacitation or loss during approach
4. Add maximum pursuit distance limits

**Success Criteria**:
- System adapts to moving targets
- Movement cancels appropriately when targets become invalid
- Pursuit behavior feels natural and responsive

### Phase 4: Polish and Integration (1-2 hours)
**Goal**: Refine behavior and integrate with existing systems

**Tasks**:
1. Optimize movement calculations for performance
2. Add appropriate visual and audio feedback
3. Ensure compatibility with existing combat systems
4. Add comprehensive debug logging

**Success Criteria**:
- Movement behavior feels smooth and responsive
- Integration with existing systems is seamless
- Debug information is comprehensive and useful

**Total Estimated Effort**: 7-11 hours

## Risk Assessment

### High Risk Areas:
- **State Management Complexity**: Coordinating movement and attack states
- **Performance Impact**: Continuous range checking during movement
- **Interaction Conflicts**: Ensuring compatibility with existing combat systems

### Medium Risk Areas:
- **Target Prediction**: Handling fast-moving or erratically moving targets
- **Edge Cases**: Unusual scenarios like target death during approach
- **Animation Timing**: Synchronizing movement and attack animations

### Low Risk Areas:
- **Basic Movement**: Core movement system already exists and works
- **Range Calculation**: Existing range checking is accurate and reliable
- **Debug Output**: Logging system is already established

### Mitigation Strategies:
- **Incremental Implementation**: Build functionality in phases
- **Extensive Testing**: Test each phase thoroughly before proceeding
- **Fallback Logic**: Implement safe fallbacks for edge cases
- **Performance Monitoring**: Monitor performance impact of continuous checking

## Success Criteria

### Functional Metrics:
- **100% Melee Attack Initiation**: All valid melee attacks result in movement or attack
- **Accurate Range Detection**: Movement stops within weapon range consistently
- **State Consistency**: No orphaned states or stuck characters
- **Target Tracking**: System adapts to 90%+ of target movement scenarios

### Performance Metrics:
- **Responsive Movement**: Movement initiation within 1 game tick of command
- **Efficient Checking**: Range checking overhead < 5% of frame time
- **Smooth Animation**: No visible stuttering or jumping during movement

### User Experience Metrics:
- **Intuitive Behavior**: Melee commands result in expected character actions
- **Visual Feedback**: Clear indication of character intent and state
- **Combat Flow**: Seamless transition from ranged to melee combat

## Dependencies

### Required Before Implementation:
- Current movement system must remain stable
- Range calculation accuracy must be maintained
- Melee weapon system must continue functioning

### Blocks Future Work:
- Comprehensive melee combat testing
- Mixed combat scenario development
- Advanced AI melee behavior implementation

## Implementation Notes

### Key Design Decisions:

1. **Movement Target Calculation**:
   ```java
   // Calculate position just within weapon range
   double approachDistance = meleeWeapon.getTotalReach() - 0.5; // Leave 0.5 feet buffer
   double angle = Math.atan2(target.y - attacker.y, target.x - attacker.x);
   double approachX = target.x - Math.cos(angle) * approachDistance * 7.0; // Convert to pixels
   double approachY = target.y - Math.sin(angle) * approachDistance * 7.0;
   ```

2. **State Management Approach**:
   - Add `isMovingToMelee` boolean to Character class
   - Add `meleeTarget` reference to track intended target
   - Use existing movement system without modification

3. **Range Checking Strategy**:
   - Check range every 10 ticks during movement (6 times per second)
   - Use existing `CombatResolver.isInMeleeRange()` method
   - Trigger attack immediately when range is achieved

### Testing Strategy:
1. **Unit Tests**: Test range calculations and movement target positioning
2. **Integration Tests**: Test movement and attack sequence coordination
3. **Scenario Tests**: Test various target movement patterns
4. **Performance Tests**: Monitor continuous range checking impact

### Documentation Updates Required:
- Update CLAUDE.md melee combat documentation
- Add movement behavior descriptions
- Document new character state fields
- Update debugging guide for melee movement

---

## Document History

- **v1.0** (2025-01-17): Initial document creation with melee movement analysis and implementation plan
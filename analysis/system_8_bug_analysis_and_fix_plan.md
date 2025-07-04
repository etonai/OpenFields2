# System 8 Bug Analysis and Fix Plan
*Analysis performed using Opus model (claude-opus-4-20250514)*
*Date: July 4, 2025*

## Executive Summary
The bug occurs because the `lastAttackScheduledTick` field is not being properly managed after melee attack recovery completes. When auto-targeting resumes immediately after recovery (tick 368), it violates the 5-tick minimum interval check because `lastAttackScheduledTick` still holds the value from when the attack was scheduled (likely tick 367 from recovery completion event scheduling).

## Bug Analysis

### Root Cause
The core issue is a timing coordination problem between:
1. **Recovery Completion**: Sets `isAttacking = false` at tick 367, allowing auto-targeting to resume
2. **Attack Scheduling Tracking**: `lastAttackScheduledTick` is set when attacks are scheduled but not properly reset/managed after recovery
3. **Auto-Targeting Resumption**: Immediately tries to schedule new attack at tick 368 without considering the interval constraint

### Why Melee-Only?
This bug only affects melee combat (not ranged) due to differences in recovery handling:

1. **Melee Recovery**: 
   - Recovery completion event scheduled at attack time + visual delay + recovery time
   - The event scheduling itself may update `lastAttackScheduledTick` to the recovery completion tick (367)
   - When recovery completes, `isAttacking` is cleared but `lastAttackScheduledTick` remains at 367

2. **Ranged Recovery**: 
   - Likely has different timing or doesn't update `lastAttackScheduledTick` during recovery scheduling
   - May have additional delays or different state management that prevents immediate re-scheduling

### Evidence from output.txt
```
Line 164: [COMBAT-RECOVERY] -1003:SoldierAlpha starts melee recovery for 120 ticks (until tick 367)
Line 450-453: IllegalStateException at tick 368 - "Last scheduled: tick 367, Current: tick 368, Interval: 1"
Line 496-497: Second violation at tick 369 - interval=2
```

The `lastAttackScheduledTick = 367` strongly suggests it was set when the recovery completion event was scheduled.

## Technical Deep Dive

### Current Flow (Broken)
1. **Tick 247**: Melee attack executes, schedules recovery completion for tick 367
2. **During scheduling**: `lastAttackScheduledTick` may be set to 367 (the scheduled event time)
3. **Tick 367**: Recovery completes, `isAttacking = false`, calls `checkContinuousAttack()`
4. **Tick 368**: Auto-targeting runs, tries to schedule attack, but 368 - 367 = 1 < 5 minimum
5. **Result**: IllegalStateException thrown

### Expected Flow
1. Recovery completes and clears attack state
2. Sufficient time passes before next attack can be scheduled
3. Auto-targeting respects the minimum interval between attack attempts

## Proposed Solutions

### Solution 1: Reset lastAttackScheduledTick on Recovery Completion (RECOMMENDED)
**Location**: `MeleeCombatSequenceManager.java` line 185 (after `isAttacking = false`)

```java
// Clear attacking flag AFTER recovery completes
character.isAttacking = false;
// Reset attack scheduling tracker to allow new attacks
character.lastAttackScheduledTick = -1;  // ADD THIS LINE
```

**Pros**: 
- Simple, targeted fix
- Maintains interval protection during active attacks
- Allows clean restart after recovery
- Matches expected behavior

**Cons**: 
- None significant

### Solution 2: Add Grace Period Check in AutoTargetingSystem
**Location**: `AutoTargetingSystem.java` before line 186

```java
// Skip interval check if enough time has passed since recovery
long RECOVERY_GRACE_PERIOD = 120; // Typical melee recovery time
if (currentTick - character.lastAttackScheduledTick > RECOVERY_GRACE_PERIOD) {
    // Enough time has passed, allow new attack without interval check
} else if (character.lastAttackScheduledTick >= 0 && 
           currentTick - character.lastAttackScheduledTick < MIN_ATTACK_SCHEDULE_INTERVAL) {
    throw new IllegalStateException(...);
}
```

**Pros**: 
- Doesn't require modifying recovery logic
- Could handle edge cases

**Cons**: 
- More complex
- Hardcoded grace period
- Doesn't address root cause

### Solution 3: Track Recovery Completion Time Separately
**Add new field**: `lastRecoveryCompletedTick`

**Pros**: 
- More precise tracking
- Could enable advanced mechanics

**Cons**: 
- Adds complexity
- Requires more changes

## Recommended Implementation Plan

### Phase 1: Implement Solution 1 (Primary Fix)
1. **File**: `/src/main/java/combat/managers/MeleeCombatSequenceManager.java`
2. **Location**: Line 185, after `character.isAttacking = false;`
3. **Change**: Add `character.lastAttackScheduledTick = -1;`
4. **Rationale**: This ensures the attack scheduling tracker is reset when recovery completes

### Phase 2: Verify Ranged Combat Consistency
1. **Check**: Does ranged combat recovery also reset `lastAttackScheduledTick`?
2. **If not**: Add similar reset for consistency
3. **File**: Look for ranged recovery completion handling

### Phase 3: Add Debug Logging (Optional)
```java
if (config.DebugConfig.getInstance().isCombatDebugEnabled()) {
    System.out.println("[ATTACK-SEQUENCE] " + character.getDisplayName() + 
                       " recovery complete, lastAttackScheduledTick reset from " + 
                       character.lastAttackScheduledTick + " to -1");
}
```

### Phase 4: Testing
1. Run `MeleeCombatTestAutomated` - should pass without exceptions
2. Verify attacks still respect 5-tick interval during active combat
3. Confirm no regression in ranged combat
4. Test with multiple characters in melee combat

## Additional Considerations

### Why This Approach?
- **Root Cause Fix**: Addresses the actual problem (stale `lastAttackScheduledTick`)
- **Minimal Changes**: Single line addition in the right place
- **Consistent Behavior**: Makes melee match expected attack scheduling behavior
- **Preserves Protection**: Interval check still prevents rapid scheduling during attacks

### Alternative Investigation
If the simple fix doesn't work, investigate:
1. Where exactly is `lastAttackScheduledTick` being set to 367?
2. Is it set during event scheduling or recovery handling?
3. Does `checkContinuousAttack()` modify this field?

## Conclusion
The bug is a simple state management issue where `lastAttackScheduledTick` isn't reset after melee recovery completes. The recommended fix is to reset this field when clearing `isAttacking`, allowing auto-targeting to schedule new attacks without triggering the interval protection that's meant for preventing duplicate scheduling within the same attack sequence.
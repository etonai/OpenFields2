# Test Plan: Immediate Hold State Firing (DevCycle 27: System 5)

## Expected Behavior
When a character is already in their preferred firing state (aiming or pointedfromhip) and receives a fire command, they should fire immediately (within 1-2 ticks) without any weapon progression delays.

## Test Scenario
1. **Setup**: Alice with MP5, already in "aiming" hold state with aiming preference
2. **Action**: Command Alice to fire at Drake
3. **Expected Result**: Alice fires within 1-2 ticks instead of the previous 297-tick delay

## Evidence from output.txt
**Before Fix**:
- Line 66: `*** 1000:Alice reached hold state: aiming ***`
- Line 71: `*** 1000:Alice scheduleAttackFromCurrentState: weapon=MP5 Submachine Gun, currentWeaponState=aiming ***`
- Line 76: Alice fires at tick 907 (delay of 297 ticks from command at tick 610)

**After Fix**:
Should see:
- `*** Alice firing immediately - already in correct state: aiming ***`
- Firing within 1-2 ticks of the command

## Implementation Summary
- Added `isAlreadyInCorrectFiringState()` method to detect when immediate firing should occur
- Modified `scheduleAttackFromCurrentState()` to use 1-tick delay when immediate firing is appropriate
- Requires minimum 5-tick duration in current state to prevent firing during rapid state transitions
- Preserves all existing combat accuracy calculations and timing for non-immediate scenarios

## Key Code Changes
1. **Character.java**: Added immediate firing detection logic
2. **System Integration**: Maintains compatibility with accumulated aiming bonus system
3. **Safety Check**: 5-tick minimum prevents abuse during rapid state changes
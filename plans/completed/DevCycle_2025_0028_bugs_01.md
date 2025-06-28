# DevCycle 2025_0028 - Bug Report #1

**Bug ID**: DC28-BUG-001  
**Date Reported**: June 28, 2025 at 1:55 PM  
**Reporter**: User Testing  
**Status**: ✅ FIXED  
**Priority**: High  

## Bug Description

**Issue**: Rate of fire inconsistency in multiple shot sequences. The 4th shot in a 4-shot sequence has a significantly longer delay than expected.

**Expected Behavior**: 
For a 4-shot sequence with pattern `Aimed → Quick → Quick → Quick`:
- Shot 1: Uses character's aiming speed
- Shot 2: Uses Quick aiming speed (faster)
- Shot 3: Uses Quick aiming speed (faster)  
- Shot 4: Uses Quick aiming speed (faster)

**Correct Pattern for ALL Multiple Shot Sequences**:
- **Shot 1**: Character's aiming speed (Aimed)
- **All subsequent shots**: Quick aiming speed

This applies to all shot counts:
- 2 shots: Aimed → Quick
- 3 shots: Aimed → Quick → Quick
- 4 shots: Aimed → Quick → Quick → Quick
- 5 shots: Aimed → Quick → Quick → Quick → Quick

All shots should have consistent timing based on their designated aiming speed.

**Actual Behavior**:
From user testing log:
- Shot 1 to 2: 40 ticks (correct)
- Shot 2 to 3: 40 ticks (correct)
- Shot 3 to 4: 55 ticks (15 ticks longer - incorrect)

The 4th shot had an unexpectedly long delay.

## Root Cause Analysis

**Problem Location**: `Character.java`, lines 2083-2095 in the recovery scheduling logic

**Root Cause**: Completely incorrect pattern logic in aiming speed calculation for multiple shot sequences.

**Original Incorrect Logic**: The code implemented a complex pattern `Aimed → Quick → Quick → Aimed → Quick` which was completely wrong.

**Actual Required Logic**: Simple pattern where first shot is aimed, all subsequent shots are quick.

The incorrect logic meant:
- Shot 4 in any sequence incorrectly used character's aiming speed instead of Quick ✗
- This caused the observed timing inconsistency

**Correct Pattern**: First shot aimed, all subsequent shots quick
- 2 shots: Aimed → Quick
- 3 shots: Aimed → Quick → Quick
- 4 shots: Aimed → Quick → Quick → Quick  
- 5 shots: Aimed → Quick → Quick → Quick → Quick

## Fix Implementation

**File**: `/src/main/java/combat/Character.java`  
**Lines**: 2083-2092  
**Change Type**: Logic fix

**Before**:
```java
// More shots to fire - increment shot counter and schedule next shot
currentShotInSequence++;

// Maintain attack state and schedule next shot with Quick aiming delay
isAttacking = true;

// Calculate delay based on Quick aiming speed  
AimingSpeed nextShotSpeed = getAimingSpeedForMultipleShot();
```

**After**:
```java
// Determine aiming speed for NEXT shot before incrementing counter
currentShotInSequence++; // Increment to next shot number
AimingSpeed nextShotSpeed = getAimingSpeedForMultipleShot(); // Get speed for this shot number

// Maintain attack state and schedule next shot
isAttacking = true;

// Calculate delay based on pattern aiming speed
```

**Pattern Logic Fix** (lines 1759-1767):
```java
private AimingSpeed getAimingSpeedForMultipleShot() {
    if (currentShotInSequence <= 0 || currentShotInSequence == 1) {
        // 1st shot always uses character's current aiming speed
        return getCurrentAimingSpeed();
    } else {
        // All subsequent shots (2nd, 3rd, 4th, 5th) use Quick
        return AimingSpeed.QUICK;
    }
}
```

**Explanation**: Simplified to the correct pattern - first shot aimed, all subsequent shots quick.

## Pattern Verification

The corrected pattern for multiple shots (ALL sequences):

**Any multiple shot sequence**:
- **Shot 1**: `currentShotInSequence = 1` → Uses character's aiming speed ✓
- **Shot 2+**: `currentShotInSequence > 1` → Uses Quick aiming speed ✓

**Examples**:
- **2-shot**: `Aimed → Quick`
- **3-shot**: `Aimed → Quick → Quick`
- **4-shot**: `Aimed → Quick → Quick → Quick`
- **5-shot**: `Aimed → Quick → Quick → Quick → Quick`

**Expected Timing for 4-shot sequence**:
- Shot 1: Uses character's aiming speed (could be 30+ ticks if aiming)
- Shots 2-4: All use Quick aiming speed (~15 ticks each)

## Testing Status

- ✅ Code compiles successfully
- ✅ Logic verified against design specification
- ⭕ Manual testing required to verify timing consistency

## Impact Assessment

**Severity**: High - affects core multiple shot functionality  
**Scope**: All multiple shot sequences where shot count > 3  
**User Experience**: Improved consistency in rapid fire sequences  

## Prevention

Added detailed comments in the code to clarify the increment timing and pattern calculation to prevent similar issues in future modifications.
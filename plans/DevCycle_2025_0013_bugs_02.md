# DevCycle 2025-0013 Bug Report #2
*Created: 2025-06-20 at 11:00 PM PDT*

## Bug Description

**Manual-To-Auto targeting transition is still not working after Bug Report #1 fixes**

Despite implementing comprehensive fixes in Bug Report #1, the manual-to-auto targeting transition is still failing. Alice performs a manual attack, the attack completes normally, auto-targeting is enabled via SHIFT+T, but no automatic attacks occur afterward.

## Sample Output

```
***********************
*** Game is paused
***********************
Selected: 1:Alice
Health: 60/60, Faction: Red, Weapon: Uzi Submachine Gun, Position: Standing
Movement: Walk, Aiming: Normal, Hesitation: 0.0s (Wound: 0.0s, Bravery: 0.0s)
[ATTACK-DECISION] 1000:Alice attack decision:
[ATTACK-DECISION] isMeleeCombatMode: false
[ATTACK-DECISION] meleeWeapon: Steel Dagger
[ATTACK-DECISION] rangedWeapon: null
[ATTACK-DECISION] meleeWeapon reach: 5.50 feet
ATTACK 1 units target 1003:Drake (Unit ID: 4)
***********************
*** Game resumed
***********************
1000:Alice weapon state: unsling at tick 0
1000:Alice weapon state: ready at tick 86
1000:Alice weapon state: aiming at tick 101
1000:Alice weapon state: firing at tick 131
*** 1000:Alice fires a 9mm round from Uzi Submachine Gun (ammo remaining: 31)
*** 1000:Alice shoots a 9mm round at 1003:Drake at distance 42.86 feet using Uzi Submachine Gun at tick 131
>>> 9mm round missed 1003:Drake at tick 133
1000:Alice weapon state: recovering at tick 136
1000:Alice weapon state: aiming at tick 156
*** 1000:Alice automatic targeting ENABLED
***********************
*** Game paused at tick 712
***********************
***********************
*** Game resumed
***********************
***********************
*** Game paused at tick 770
***********************
```

## Critical Analysis

### What We Can Observe:

1. **Manual Attack Sequence Completes Normally**: 
   - unsling → ready → aiming → firing → recovering → aiming
   - Attack completes at tick 156 with weapon in "aiming" state

2. **Auto-Targeting Successfully Enabled**: 
   - "*** 1000:Alice automatic targeting ENABLED" message appears
   - This confirms the SHIFT+T toggle is working

3. **No Debug Output**: 
   - **CRITICAL**: No `[AUTO-TARGET-ENTRY]`, `[CONTINUOUS-ATTACK]`, or other debug messages appear
   - This suggests our enhanced debug logging isn't being triggered

4. **Extended Time Gap**: 
   - Auto-targeting enabled at ~tick 156
   - Game runs until tick 712-770 with no action
   - Sufficient time for auto-targeting evaluation to occur

5. **Target Still Valid**: 
   - Drake is still alive (not mentioned as incapacitated)
   - Should be a valid target for auto-targeting

## Technical Investigation

### Missing Debug Output Analysis

The absence of our enhanced debug logging suggests one of these critical issues:

1. **`updateAutomaticTargeting()` Not Being Called**: 
   - The main game loop may not be calling `updateAutomaticTargeting()` for Alice
   - This would be a fundamental integration issue

2. **`checkContinuousAttack()` Not Being Called**:
   - If the manual attack didn't properly call `checkContinuousAttack()`, our fix wouldn't trigger
   - This could be a timing or state management issue

3. **Debug Logging Disabled**:
   - The `debugPrint()` method may be disabled or not outputting
   - This would mask the actual execution flow

### Key Questions Requiring Investigation

1. **Game Loop Integration**: Is `updateAutomaticTargeting()` being called every tick for characters with `usesAutomaticTargeting = true`?

2. **Attack Completion Flow**: Did the manual attack properly call `checkContinuousAttack()` after completion?

3. **State Management**: What are Alice's actual state values after auto-targeting is enabled?
   - `usesAutomaticTargeting = ?`
   - `isAttacking = ?`
   - `currentTarget = ?`
   - `persistentAttack = ?`
   - `currentWeaponState = ?`

4. **Target Preservation**: Is Drake still a valid target reference, or was `currentTarget` cleared during manual attack?

## Hypothesis

**Primary Hypothesis**: The main game loop is not calling `updateAutomaticTargeting()` for Alice, despite auto-targeting being enabled. This would explain the complete absence of debug output.

**Secondary Hypothesis**: Alice's `currentTarget` is null after the manual attack, and our fix in `checkContinuousAttack()` isn't being triggered because the manual attack didn't call it.

## Required Debugging Steps

### 1. Verify Game Loop Integration
- Add debug logging to the main game loop to confirm `updateAutomaticTargeting()` is being called
- Check if Alice is being skipped or filtered out of auto-targeting evaluation

### 2. Verify Attack Completion Flow
- Add debug logging to `scheduleFiring()` to confirm `checkContinuousAttack()` is called
- Verify the timing and state when `checkContinuousAttack()` executes

### 3. State Validation
- Add immediate state dump when auto-targeting is enabled via SHIFT+T
- Log Alice's complete state immediately after manual attack completion

### 4. Target Reference Investigation
- Verify if `currentTarget` is preserved or cleared during manual attacks
- Check if Drake's Unit reference remains valid

## Priority

**Critical** - This indicates a fundamental integration issue that prevents the auto-targeting system from working at all after manual attacks, despite our fixes being implemented.

---

## Bug #2: Melee Auto-Targeting Stops After Non-Fatal Attack

### Description

While in melee auto-targeting mode, if an attack incapacitates the target, the attacker correctly targets another unit. However, if the attack doesn't incapacitate the target (leaving them alive), the attacker stops attacking entirely, even though the target is still valid and alive.

### Sample Output

```
[AUTO-TARGET] 1000:Alice acquired target 1001:Bobby at distance 43.1 feet
1000:Alice melee weapon state: melee_ready at tick 565
[MELEE-ATTACK] 1000:Alice startMeleeAttackSequence called
[MELEE-ATTACK] Current tick: 898, Target: 1001:Bobby
[MELEE-ATTACK] Weapon: Steel Dagger (reach: 5.50 feet)
[MELEE-ATTACK] Current weapon state: melee_ready
[MELEE-ATTACK] Is attacking: false
[MELEE-ATTACK] Is incapacitated: false
[MELEE-ATTACK] Distance check: 5.15 feet
[MELEE-ATTACK] Target in range: 5.15 feet - proceeding with attack
[MELEE-ATTACK] Facing target at 96.7 degrees
[MELEE-ATTACK] Calling scheduleMeleeAttackFromCurrentState
[MELEE-STATE] 1000:Alice scheduleMeleeAttackFromCurrentState called
[MELEE-STATE] Active weapon: Steel Dagger
[MELEE-STATE] Current weapon state: melee_ready
[MELEE-STATE] 1000:Alice current weapon state: melee_ready
[MELEE-STATE] 1000:Alice ready to attack - scheduling attack in 57 ticks
[MELEE-ATTACK] scheduleMeleeAttackFromCurrentState call completed
1000:Alice executes melee attack with Steel Dagger at tick 955
>>> Steel Dagger strikes 1001:Bobby in the head causing a serious wound at tick 955
>>> 1001:Bobby takes 50 damage
>>> HEADSHOT! 1000:Alice scored a headshot on 1001:Bobby
>>> HESITATION STARTED: 1001:Bobby begins hesitating for 60 ticks due to serious wound
>>> 1001:Bobby current health: 45/95
>>> BRAVERY CHECK: 1001:Bobby rolls 87.5 vs 51 (wounded by Steel Dagger)
>>> BRAVERY FAILED: 1001:Bobby fails bravery check! Total failures: 1 (penalty: -10 accuracy)
>>> BRAVERY CHECK: 1005:Frank rolls 24.7 vs 57 (ally 1001:Bobby hit by Steel Dagger)
>>> BRAVERY PASSED: 1005:Frank passes bravery check
1000:Alice melee weapon state: melee_ready at tick 1012
>>> HESITATION ENDED: 1001:Bobby recovers from hesitation at tick 955
>>> BRAVERY RECOVERY: 1001:Bobby recovers from bravery failure. Remaining failures: 0
***********************
*** Game paused at tick 1656
***********************
***********************
*** Game resumed
***********************
***********************
*** Game paused at tick 1782
***********************
```

### Analysis

From the output, we can observe:

1. **Successful Auto-Targeting**: Alice correctly acquires Bobby as a target and initiates melee attack
2. **Successful Attack**: The Steel Dagger attack hits Bobby, causing 50 damage and reducing his health to 45/95
3. **Target Still Valid**: Bobby is wounded but still alive (45/95 health) and should remain a valid target
4. **Weapon Recovery**: Alice's weapon properly returns to "melee_ready" state at tick 1012
5. **No Follow-up**: Despite Bobby being alive and in range, Alice does not continue attacking

### Critical Missing Elements

**Expected but Missing Debug Output:**
- No `[MELEE-RECOVERY]` debug messages showing `isAttacking=false` being set
- No `[AUTO-TARGET-ENTRY]` messages indicating `updateAutomaticTargeting()` is being called
- No evidence of auto-targeting re-evaluation after the attack completes

### Technical Investigation

**Potential Root Causes:**

1. **Melee Recovery Event Issue**: The melee attack recovery event may not be scheduling/executing properly, leaving `isAttacking = true`

2. **Auto-Targeting Integration**: The melee combat system may not be properly integrated with the auto-targeting evaluation loop

3. **State Management**: The `currentTarget` may be cleared or invalidated after the melee attack, preventing continued targeting

4. **Timing Issues**: There may be timing conflicts between melee attack completion and auto-targeting re-evaluation

### Expected Behavior

After the melee attack hits Bobby (non-fatal):
1. Alice should complete the attack recovery cycle
2. `isAttacking` should be set to `false`
3. Auto-targeting should re-evaluate and detect Bobby as still valid (alive, in range)
4. Alice should automatically initiate another melee attack
5. This cycle should continue until Bobby is incapacitated

### Comparison with Incapacitation Scenario

**When target is incapacitated**: Auto-targeting correctly switches to a new target
**When target survives**: Auto-targeting stops entirely

This suggests the issue is specifically with continued targeting of the same alive target, rather than the target switching logic.

### Priority

**High** - This bug makes melee auto-targeting unreliable and limits its tactical effectiveness, as characters stop attacking after one hit instead of continuing until target elimination.

---

## Implementation Status

The fixes from Bug Report #1 appear to be syntactically correct but are not being executed, suggesting the issue is at a higher level in the system integration rather than the specific methods we modified.

Both bugs indicate fundamental issues with auto-targeting integration and execution flow that require deeper investigation into the game loop and event scheduling systems.
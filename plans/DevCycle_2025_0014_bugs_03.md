# DevCycle 2025-0014 Bug Report #3

## Remaining Critical Bugs from DevCycle 14
After implementing Bug 2B fixes, two critical bugs remain unresolved and prevent DevCycle 14 functionality from working properly.

## Bug 3A: JSON Character Loading Still Fails (Critical)

### Problem Description
Despite implementing Jackson ObjectMapper configuration and mixin classes, CTRL-A direct character addition still fails to load characters from faction JSON files. The system continues to show "0 available characters" for all factions even when faction files contain character data.

### Current Status
- Jackson ObjectMapper has been configured with deserialization settings
- CharacterMixin class added to ignore problematic fields
- Compilation succeeds but character loading still fails at runtime

### Expected Behavior
- Characters should load successfully from faction JSON files
- Available character counts should display correctly (e.g., "1. Union (1 available characters)")
- CTRL-A workflow should proceed normally to quantity and spacing selection

### Actual Behavior
- All factions continue to show "0 available characters"
- Character addition workflow cannot proceed past faction selection
- CTRL-A functionality remains completely blocked

### Root Cause Analysis
The Jackson configuration may not be sufficient to handle all deserialization issues, or there may be additional problematic fields not covered by the mixin class. The character loading process may still be failing silently during JSON parsing.

### Technical Context
- **File**: `InputManager.java:3395` - `objectMapper.treeToValue(charNode, combat.Character.class)`
- **Method**: `getFactionCharacterInfo()` 
- **Mixin Applied**: CharacterMixin class ignoring problematic fields
- **Updated Faction Data**: Alexander character updated in faction 1

### Specific Debug Investigation Required
**Problem Location**: The issue occurs in `InputManager.java` in the `getFactionCharacterInfo()` function on line 3426 (originally line 3395) when `objectMapper.treeToValue(charNode, combat.Character.class)` attempts to convert the JsonNode `charNode` into a `combat.Character` object.

**Debugging Approach**: Add debug output to log the actual `charNode` content so it can be compared with what the Character class expects. This will allow for field-by-field comparison to identify specific deserialization conflicts between the JSON structure and the Character class fields.

**Recommended Debug Code**:
```java
// Add before the objectMapper.treeToValue() call
System.out.println("[DEBUG] Attempting to deserialize character node:");
System.out.println("[DEBUG] charNode content: " + charNode.toString());
```

This should make it relatively easy to compare the JSON structure with the Character class field requirements and identify exactly which fields are causing the deserialization failure.

### Impact Assessment
- **Severity**: Critical - Completely blocks CTRL-A character addition
- **User Impact**: Feature is unusable despite implementation
- **Workaround**: None available

## Bug 3C: Weapon Readiness Still Not Working During Melee Movement (High)

### Problem Description
Despite implementing dedicated melee weapon readiness methods, weapon readiness is still not working properly during melee movement. Characters may still experience weapon state resets or delays when transitioning from movement to attack.

### Current Implementation
- Created `startMeleeWeaponReadySequence()` method for melee weapons
- Added `scheduleMeleeWeaponReadyFromCurrentState()` for state progression
- Added `scheduleMeleeWeaponStateTransition()` for melee-specific transitions
- Updated InputManager to use melee-specific weapon readiness

### Expected Behavior
- Weapon readiness should begin during movement and continue through to target arrival
- Weapon state should persist and progress during movement
- Character should be ready to attack immediately upon reaching target
- No re-initialization of weapon state when starting attack sequence

### Actual Behavior
- Weapon readiness may still not persist properly through movement
- Characters may still experience delays when starting melee attacks
- Weapon state management between movement and attack needs verification

### Comparative Analysis: Ranged vs Melee Weapon Readiness
**Key Finding**: Readying a weapon works correctly for ranged weapons but fails for melee weapons.

**Test Case Evidence**:
- **Alice (Ranged - Uzi)**: `READY WEAPON` → `unsling at tick 0` → `ready at tick 86` → `weapon is already ready`
- **Chris (Melee - Battle Axe)**: `READY WEAPON` → `unsheathing at tick 0` → **No completion message, weapon never becomes ready**

**Observed Differences**:
1. **Ranged weapons** complete their readiness sequence and show "weapon is already ready"
2. **Melee weapons** start the unsheathing process but never complete it
3. **State progression** works for ranged but fails for melee

**Code Analysis Required**:
The implementation difference between ranged and melee weapon readiness systems needs investigation. The ranged weapon system (original `startReadyWeaponSequence()`) successfully completes state transitions, while the new melee-specific system (`startMeleeWeaponReadySequence()`) appears to start but not complete the readiness process.

**Specific Investigation Points**:
1. Compare event scheduling between `scheduleReadyFromCurrentState()` (ranged) and `scheduleMeleeWeaponReadyFromCurrentState()` (melee)
2. Verify that `scheduleMeleeWeaponStateTransition()` properly schedules and executes transition events
3. Check if melee weapon states have the correct progression path from "sheathed" → "unsheathing" → "melee_ready"
4. Ensure melee weapon readiness events are being added to the event queue and processed correctly

### Sample Issue Pattern (From Previous Bug Report)
```
[MELEE-TRIGGER] *** Character readying melee weapon for combat
*** Character moving to melee range of target
Character weapon state: unsheathing at tick 0

[MELEE-ATTACK] Character startMeleeAttackSequence called
[MELEE-ATTACK] Current tick: 780, Target: Target
[MELEE-STATE] Character current weapon state: null
[MELEE-STATE] Character initializing weapon state to: sheathed
```

### Technical Investigation Needed
- Verify that weapon state persists correctly during movement
- Ensure melee weapon readiness events continue independently when movement begins
- Check if the new melee-specific system properly maintains state
- Test timing between movement completion and attack initiation

### Impact Assessment
- **Severity**: High - Reduces combat efficiency and responsiveness
- **User Impact**: Delayed combat, unrealistic weapon handling
- **Workaround**: None available for optimal combat timing

## Reproduction Steps

### Bug 3A - JSON Loading Issue
1. Start the game in edit mode
2. Press CTRL-A to start direct character addition
3. Observe that all factions still show "0 available characters"
4. Character addition workflow remains unusable

### Bug 3C - Weapon Readiness
1. Set up melee character with distant target
2. Initiate melee attack
3. Observe weapon readiness behavior during movement
4. Check if character is ready to attack immediately upon reaching target

## Overall Priority
**Critical** - These remaining bugs continue to make DevCycle 14 implementation largely non-functional.

## Next Steps Required

### Bug 3A - Advanced JSON Loading Debugging
1. **Enhanced Error Handling**: Add more detailed error logging to identify specific deserialization failures
2. **Field-by-Field Analysis**: Systematically test which Character fields cause deserialization issues
3. **Alternative Approaches**: Consider custom deserializer or simplified character loading
4. **Runtime Testing**: Test the ObjectMapper configuration at runtime rather than just compilation

### Bug 3C - Weapon State Verification
1. **State Persistence Testing**: Verify weapon state maintenance during movement phases
2. **Event Timing Analysis**: Check timing between readiness events and movement completion
3. **Integration Testing**: Test complete workflow from movement initiation to attack execution
4. **Debug Logging**: Add comprehensive logging to track weapon state transitions

## Files Affected
- `src/main/java/InputManager.java` - Character loading and JSON deserialization
- `src/main/java/combat/Character.java` - Melee weapon readiness and state management
- `factions/*.json` - Character data storage format (updated with Alexander character)

## Success Criteria
- **Bug 3A**: CTRL-A shows correct available character counts and successfully loads characters from faction files
- **Bug 3C**: Melee characters are ready to attack immediately upon reaching targets without weapon state resets

## Implementation Plan Based on User Answers

### Bug 3A Implementation Strategy (Priority 1)
Based on the user's responses, the approach for Bug 3A will be:

1. **Start with debug logging** - Add debug output to see the actual `charNode` content vs Character class expectations
2. **Examine CTRL-C character creation functionality** - Study how characters are successfully created and placed in faction files as a reference for proper serialization/deserialization
3. **Continue with full Character deserialization** - Maintain the approach of deserializing the entire Character object rather than manual field parsing
4. **Investigate both potential causes** - Check for issues in both the Character class fields and the JSON structure in faction files

### Bug 3C Implementation Strategy (Priority 2)
Based on the user's responses, the approach for Bug 3C will be:

1. **Study the working ranged weapon system** - Analyze `scheduleReadyFromCurrentState()` to understand why it succeeds
2. **Add comprehensive debug logging** - Track exactly where melee weapon state transitions fail
3. **Investigate WeaponState.getStateByName() calls** - Verify that melee weapons have the expected state names
4. **Unify weapon systems** - Both ranged and melee weapons should use the same state management approach since "they are weapons"
5. **Fix the existing melee system** - Improve the current implementation rather than reverting

### Execution Order
- **Focus**: Complete Bug 3A entirely before moving to Bug 3C
- **Reference Point**: Use CTRL-C character creation as the model for successful Character serialization
- **Technical Approach**: Unified weapon state system for both ranged and melee weapons

### Key Insights from User Feedback
- Character creation (CTRL-C) provides a working example of Character serialization
- Weapon readiness should work identically for both weapon types
- Debug logging is essential for identifying the specific failure points
- Full object deserialization is preferred over manual field parsing
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
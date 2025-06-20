# DevCycle 2025-14 Bug Report 01: Character Generation and Melee Combat Issues

**Created:** 2025-06-20  
**Status:** Open  
**Priority:** High  
**Affected System:** CTRL-A Character Addition, Melee Combat Auto-Targeting

## Bug 1: Character Addition Must Use Existing Faction Characters

### Issue Description
When you add a new character, you are not allowed to create a new character to add the character. The character must come from already-created characters in the factions files, for that specific faction. When the user picks a faction to add, ask the user if they want to include incapacitated characters, then tell the user how many characters are available in the faction file. If there aren't enough characters available, allow the user to exit adding characters.

### Current Behavior
- CTRL-A character addition generates new random characters with random stats and names
- Does not use existing faction character data from faction files
- No validation of available characters in faction

### Expected Behavior
- CTRL-A should select characters from existing faction files
- Ask user if incapacitated characters should be included in selection
- Display count of available characters for selected faction
- Allow user to exit if insufficient characters available
- Only deploy characters that exist in the faction data

### Technical Requirements
- Read characters from JSON faction files in `factions/` directory
- Parse character list from faction file structure
- Exclude already deployed characters from selection pool
- Calculate incapacitation status using Character class logic (health > 0, no critical wounds)
- Add incapacitation filter option to character selection
- Map faction numbers (1-3) to corresponding faction file names/IDs
- Validate character availability before allowing quantity selection
- Provide graceful exit option when insufficient characters available

## Bug 2: Melee Characters Don't Stop When Target Incapacitated During Movement

### Issue Description
If a character is attacking a target and moving towards it, and the target is incapacitated, make the attacking character stop moving. This will require changing the character's movement target location (which should be near the unit the character is trying to attack) to the character's current location.

### Current Behavior
- Characters continue moving toward incapacitated targets during auto-targeting
- Movement target remains at original approach position even after target incapacitation

### Expected Behavior
- Characters should immediately stop movement when target becomes incapacitated during approach
- Movement target should be changed to character's current location to stop movement
- Should affect both manual and auto-targeting scenarios

### Technical Implementation
- Use `Unit.setTarget()` method to set movement target to current position when target incapacitated
- Modify `Character.updateMeleeMovement()` to call `Unit.setTarget(currentX, currentY)` upon target incapacitation
- Apply to all characters simultaneously when multiple attackers target same incapacitated enemy
- No animation handling required for stopping
- Maintain existing incapacitation checking logic

## Bug 3: Weapon Readiness Not Working During Melee Movement

### Issue Description
Readying weapons is still not working. When a melee character is given a distant target, there is a message about unsheathing the weapon, but the weapon is not ready by the time the attacker reaches the target.

### Sample Log Analysis
```
[MELEE-TRIGGER] *** 1000:Alice readying melee weapon Steel Dagger for combat
*** 1000:Alice moving to melee range of 1005:Frank
1000:Alice weapon state: unsheathing at tick 0

[MELEE-ATTACK] 1000:Alice startMeleeAttackSequence called
[MELEE-ATTACK] Current tick: 780, Target: 1005:Frank
[MELEE-STATE] 1000:Alice current weapon state: null
[MELEE-STATE] 1000:Alice initializing weapon state to: sheathed
[MELEE-STATE] 1000:Alice unsheathing weapon (0 ticks)
[MELEE-STATE] Scheduled state transition event for tick 780
[MELEE-STATE] 1000:Alice becoming melee ready (60 ticks)
[MELEE-STATE] Scheduled state transition event for tick 840
```

### Root Cause Analysis
- Alice began unsheathing at tick 0 during movement initiation
- When attack initiated at tick 780, weapon state was `null` instead of continuing from previous state
- System re-initialized weapon to `sheathed` and started unsheathing process again
- Required additional 60 ticks to become melee ready instead of being ready upon arrival

### Expected Behavior
- Weapon readiness should begin during movement and continue through to target arrival
- Weapon state should persist and progress during movement
- Character should be ready to attack immediately upon reaching target
- No re-initialization of weapon state when starting attack sequence

### Technical Investigation Needed
- Verify weapon state persistence during all movement types (not just melee movement)
- Check if `startReadyWeaponSequence()` is properly maintaining state
- Ensure weapon readiness events continue independently when movement begins
- Compare with ranged weapon readiness implementation as reference
- Investigate why weapon state becomes `null` before attack initiation
- Determine if issue affects all melee weapon types or specific ones

## Reproduction Steps

### Bug 1 - Character Generation
1. Press CTRL-E to enter Edit Mode
2. Press CTRL-A to add characters
3. Select any faction
4. Select any quantity
5. Observe that new random characters are generated instead of using faction file characters

### Bug 2 - Movement During Target Incapacitation
1. Set up melee character with distant target
2. Initiate melee attack (character begins moving toward target)
3. Incapacitate target during movement (via external means)
4. Observe character continues moving instead of stopping

### Bug 3 - Weapon Readiness
1. Set up melee character with distant target
2. Initiate melee attack
3. Observe weapon unsheathing message during movement initiation
4. When character reaches target and begins attack, observe weapon state reset and additional readiness time required

## Impact Assessment

**Severity:** High
- Bug 1: Breaks intended character management system
- Bug 2: Unrealistic combat behavior, affects game balance
- Bug 3: Reduces combat efficiency and responsiveness

**Affected Users:** All users using CTRL-A character addition and melee combat

## Implementation Strategy

Based on user answers, the following implementation approach has been established:

**Resolution Order:** All bugs can be addressed in parallel
**Testing Method:** Manual testing by user
**Backward Compatibility:** No existing features depend on current buggy behavior

## Notes

All three bugs are related to the DevCycle 14 implementation and should be addressed before considering the cycle complete. Bug 3 may require investigation into the weapon state management system to understand why state persistence is failing during movement.

## Implementation Requirements Summary

Based on user answers, the following specific requirements have been established:

### Bug 1 - Character Selection from Faction Files
- **Data Source**: JSON files in `factions/` directory containing character lists
- **Character Structure**: Examine existing faction files to understand data format
- **Availability Logic**: Exclude already deployed characters from selection pool
- **Incapacitation Calculation**: Use Character class logic (health > 0, no critical wounds)
- **Faction Mapping**: Map faction numbers (1-3) to corresponding faction file names/IDs

### Bug 2 - Movement Stopping Implementation
- **Stop Method**: Use `Unit.setTarget(currentX, currentY)` to stop movement
- **Target Position**: Set movement target to exact current position
- **Multiple Attackers**: All attackers targeting same enemy stop simultaneously
- **Animation**: No special stop animations required

### Bug 3 - Weapon State Persistence
- **Scope**: Weapon state should persist during all movement types
- **Event Handling**: Weapon readiness events should continue independently during movement
- **Reference Implementation**: Use ranged weapon readiness as implementation reference
- **Investigation Focus**: Determine why weapon state becomes `null` before attack initiation
- **Weapon Types**: Issue may affect all melee weapon types (requires investigation)
- **Debug Logging**: No additional debug information needed at this time

All bugs can be addressed in parallel with manual testing validation.
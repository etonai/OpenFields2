# DevCycle 2025-0014 Bug Report #2

## Multiple Bug Summary
Several critical bugs prevent DevCycle 14 functionality from working properly, including JSON deserialization issues, movement behavior, and weapon readiness problems.

## Bug 2A: JSON Character Loading Fails (Critical)

### Problem Description
When attempting to use CTRL-A direct character addition, the system fails to load characters from faction JSON files due to a Jackson deserialization error with JavaFX Color objects. This results in all factions showing "0 available characters" even when faction files contain character data.

### Error Message
```
Error loading character from faction 1: Invalid type definition for type `javafx.scene.paint.Color`: Failed to call `setAccess()` on Field 'red' (of class `javafx.scene.paint.Color`) due to `java.lang.reflect.InaccessibleObjectException`, problem: Unable to make field private final float javafx.scene.paint.Color.red accessible: module javafx.graphics does not "opens javafx.scene.paint" to unnamed module @6ef4caa9
```

### Root Cause Analysis
The issue occurs because Jackson ObjectMapper cannot access private fields in JavaFX Color objects due to Java module system restrictions. The Character class likely contains JavaFX Color objects that are being serialized/deserialized in the faction JSON files.

### Technical Context
- **File**: `InputManager.java:3395` - `objectMapper.treeToValue(charNode, combat.Character.class)`
- **Method**: `getFactionCharacterInfo()` 
- **Jackson Version**: 2.16.1
- **Java Version**: 21 with module system restrictions
- **JavaFX Version**: 21.0.2

### Impact Assessment
- **Severity**: Critical - Completely blocks CTRL-A character addition
- **User Impact**: Feature is unusable
- **Workaround**: None available

## Bug 2B: Melee Characters Don't Stop When Target Incapacitated During Movement

### Issue Description
If a character is attacking a target and moving towards it, and the target is incapacitated, the attacking character continues moving instead of stopping. This requires changing the character's movement target location to the character's current location.

### Current Behavior
- Characters continue moving toward incapacitated targets during auto-targeting
- Movement target remains at original approach position even after target incapacitation

### Expected Behavior
- Characters should immediately stop movement when target becomes incapacitated during approach
- Movement target should be changed to character's current location to stop movement
- Should affect both manual and auto-targeting scenarios

### Technical Implementation Required
- Use `Unit.setTarget()` method to set movement target to current position when target incapacitated
- Modify `Character.updateMeleeMovement()` to call `Unit.setTarget(currentX, currentY)` upon target incapacitation
- Apply to all characters simultaneously when multiple attackers target same incapacitated enemy

### Impact Assessment
- **Severity**: High - Unrealistic combat behavior, affects game balance
- **User Impact**: Poor combat experience, units waste time moving to dead targets

## Bug 2C: Weapon Readiness Not Working During Melee Movement

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
- Investigate why weapon state becomes `null` before attack initiation

### Impact Assessment
- **Severity**: High - Reduces combat efficiency and responsiveness
- **User Impact**: Delayed combat, unrealistic weapon handling

## Reproduction Steps

### Bug 2A - JSON Loading Issue
1. Start the game in edit mode
2. Press CTRL-A to start direct character addition
3. Observe error messages and "0 available characters" for all factions
4. Character addition workflow becomes unusable

### Bug 2B - Movement During Target Incapacitation
1. Set up melee character with distant target
2. Initiate melee attack (character begins moving toward target)
3. Incapacitate target during movement (via external means)
4. Observe character continues moving instead of stopping

### Bug 2C - Weapon Readiness
1. Set up melee character with distant target
2. Initiate melee attack
3. Observe weapon unsheathing message during movement initiation
4. When character reaches target and begins attack, observe weapon state reset and additional readiness time required

## Overall Priority
**Critical** - These bugs make the DevCycle 14 implementation largely non-functional.

## Proposed Solutions

### Bug 2A - JSON Loading
1. **Option A**: Configure Jackson to handle JavaFX Color objects properly
   - Add custom deserializer for JavaFX Color
   - Configure ObjectMapper with appropriate visibility settings

2. **Option B**: Modify Character class JSON serialization
   - Exclude JavaFX Color fields from JSON serialization
   - Store color information as hex strings or RGB values
   - Convert during runtime as needed

3. **Option C**: Use alternative JSON structure
   - Store character data without JavaFX-specific types
   - Map color information separately during character loading

### Bug 2B - Movement Stopping
- Implement target incapacitation checking in movement update loops
- Call `Unit.setTarget(currentX, currentY)` when target becomes incapacitated
- Apply to all movement types that involve target tracking

### Bug 2C - Weapon State Persistence
- Investigate weapon state lifecycle during movement
- Ensure weapon readiness events persist through movement phases
- Fix weapon state re-initialization issue
- Use ranged weapon readiness as reference implementation

## Files Affected
- `src/main/java/InputManager.java` - Character loading and JSON deserialization
- `factions/*.json` - Character data storage format
- `combat/Character.java` - Character class serialization, movement, and weapon state management
- Movement and weapon state management systems
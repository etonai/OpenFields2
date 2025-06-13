# Future Tasks and Design Decisions
*Created: June 11, 2025 at 3:30 PM PDT*

## Items for Future Consideration

### Prone Weapon Handling Enhancement
**Source**: Tasks20250611_02.md - System 2 (Prone Combat)  
**Date**: June 11, 2025  
**Description**: Revisit prone weapon restrictions - consider whether some weapons (like rifles) should be easier to use when prone, providing accuracy bonuses or other advantages.

### Bravery Check Trigger Refinement
**Source**: Tasks20250611_02.md - System 3 (Bravery Check)  
**Date**: June 11, 2025  
**Description**: Revisit bravery check trigger conditions - currently triggers on all misses, but could be refined to only trigger for near misses within a certain distance or only when directly targeted.

### Bravery Recovery Mechanics
**Source**: Tasks20250611_02.md - System 3 (Bravery Check)  
**Date**: June 11, 2025  
**Description**: Implement bravery recovery system - consider how bravery penalties could decay over time, reset after combat, or be restored through rest/medical attention.

### Wound Healing System
**Source**: Tasks20250611_02.md - System 6 (Movement Wound Penalties)  
**Date**: June 11, 2025  
**Description**: Implement comprehensive wound healing mechanics that would gradually restore movement capabilities and remove movement restrictions over time.

### Kneeling Position Combat Modifiers
**Source**: Tasks20250611_02.md - System 1 (Enhanced Stray Shot System)  
**Date**: June 11, 2025  
**Description**: Handle kneeling character combat modifiers - accuracy penalties for shooting at kneeling targets, prone/kneeling transition timing, and other combat effects.

### Prone Character Rotation Ability
**Source**: Tasks20250611_02.md - System 2 (Prone Combat System)  
**Date**: June 11, 2025  
**Description**: Determine whether prone characters can turn/rotate freely or are movement-restricted in terms of directional facing.

### Bravery Check Formula Refinement
**Source**: Tasks20250611_02.md - System 3 (Bravery Check System)  
**Date**: June 11, 2025  
**Description**: Revisit the bravery check formula currently set as 50% + (2×coolness modifier) to ensure proper balance and gameplay feel.

### Critical Wound Incapacitation System
**Source**: Tasks20250611_02.md - System 5 (Movement Wound Penalties)  
**Date**: June 11, 2025  
**Description**: Allow some critically wounded characters to not be incapacitated, creating a middle ground between serious wounds and full incapacitation.

### Character Status UI Enhancement
**Source**: Tasks20250611_02.md - System Interaction Specifications  
**Date**: June 11, 2025  
**Description**: Show a shortened version of the '?' character stats when a character is selected, displaying key status effects and conditions clearly to the player.

### Hesitation Status Display
**Source**: Tasks20250611_02.md - System Integration Questions  
**Date**: June 11, 2025  
**Description**: Add hesitation information to character status display. Currently wounds and position states are shown in the '?' stat information, but hesitation states are not visible to the player.

### Area Fire Systems Implementation
**Source**: DevCycle_2025_0003.md (moved from development cycle)  
**Date**: June 12, 2025  
**Description**: Implement comprehensive area fire mechanics including burst area fire and full auto area fire systems with area selection UI, target enumeration, and sequencing.

**Detailed Requirements:**
- **Burst Area Fire Implementation**
  - Create area selection UI (draw box mechanism)
  - Implement target enumeration within selected area
  - Add burst fire sequencing (minimal aim → fire → next target)
  - Apply "minimal aiming" accuracy penalties
  - Calculate burst fire timing and ammunition consumption
  - Add burst area fire to firing mode system

- **Full Auto Area Fire Design**
  - Research and design game advantages for full auto area fire
  - Consider: suppression effects, area denial, multiple simultaneous hits
  - Implement similar area selection to burst fire
  - Design full auto timing, accuracy, and ammo consumption
  - Add full auto area fire controls and UI

- **Area Fire UI and Controls**
  - Add area selection tool (click and drag box)
  - Visual feedback for selected area and targets within
  - Key bindings for burst/full auto area fire modes
  - Target priority ordering within selected area

**Design Considerations:**
1. **Minimal Aiming Penalty**: First shot has normal penalty, successive shots use quick aiming speed accuracy
2. **Full Auto Advantages**: Integrate with hesitation system for suppression effects
3. **Target Priority**: To be determined (closest first, left-to-right, user-defined order)
4. **Area Size Limits**: To be determined (minimum/maximum area sizes)
5. **Ammo Consumption**: Real-time based on weapon rate of fire, every bullet calculated

**Technical Implementation:**
- **Key Files**: `InputManager.java`, `GameRenderer.java`, `CombatResolver.java`, firing mode system files
- **New Systems**: Area selection tool, target enumeration, area fire execution sequencing
- **Balance**: Area fire effectiveness vs ammunition consumption, accuracy penalties for rapid engagement

### Leg Wound and Weapon Rendering Integration
**Source**: DevCycle_2025_0003.md - Complex System Interaction Questions  
**Date**: June 12, 2025  
**Description**: Implement interaction between leg wounds forcing prone position and weapon visual rendering/target zone selection.

**Requirements:**
- Determine weapon rendering behavior when character is forced prone by leg wounds
- Consider target zone selection limitations for characters with movement restrictions
- Handle visual weapon positioning changes when transitioning between positions due to wounds
- Address user interface considerations for wounded characters' targeting capabilities

**Technical Considerations:**
- Integration with existing wound system and position state management
- Weapon line positioning calculations for prone characters
- Target zone UI accessibility for movement-restricted characters
- Visual feedback for wound-based targeting limitations

### Weapon-Dependent Burst Size Configuration
**Source**: DevCycle_2025_0003.md - System 3 (Enhanced Firing Mode Mechanics)  
**Date**: June 12, 2025  
**Description**: Add configurable burst size property to burst-capable weapons instead of using fixed burst count.

**Requirements:**
- Add burst size field to Weapon class for burst-capable weapons
- Implement weapon-specific burst counts (e.g., 3-round burst vs 5-round burst)
- Update burst firing logic to use weapon's configured burst size
- Consider balance implications of different burst sizes on accuracy and ammunition consumption
- Ensure burst size configuration persists through save/load operations

**Technical Considerations:**
- Weapon class modification to include burst size property
- Firing mode system updates to respect weapon-specific burst counts
- UI considerations for displaying burst size information
- Balance testing for different burst sizes across weapon types

### Weapon Rendering Constants Extraction
**Source**: DevCycle_2025_0004.md - System 5 (Submachine Gun)  
**Date**: June 13, 2025  
**Description**: Extract hardcoded weapon rendering constants for weapon states and lengths to improve maintainability and support new weapon types.

**Requirements:**
- Extract weapon length constants from hardcoded values to configurable properties
- Create weapon state rendering configuration system
- Consolidate weapon positioning and rendering logic into centralized system
- Support dynamic weapon rendering parameters for different weapon types

**Technical Considerations:**
- Locate current hardcoded weapon length values (pistol, rifle, submachine gun)
- Create weapon rendering configuration class or constants
- Update weapon rendering system to use configurable values
- Ensure backward compatibility with existing weapon types

### Save Game Data Completeness Enhancement
**Source**: DevCycle_2025_0006.md - System 2 (postponed from development cycle)  
**Date**: January 13, 2025  
**Description**: Implement complete save/load functionality including event queue, character firing modes, and automatic targeting status to preserve complete game state.

**Requirements:**
- **Event Queue Persistence**
  - Add event queue serialization to save data
  - Implement event queue deserialization on load
  - Ensure scheduled events resume correctly after load
  - Test event timing continuity across save/load cycles
  - Handle edge cases with events scheduled far in the future

- **Character State Persistence**
  - Save character firing mode settings (per-weapon property)
  - Save automatic targeting status for each character (usesAutomaticTargeting)
  - Verify all character combat states are preserved
  - Test state restoration accuracy after load
  - Handle version compatibility for new save data fields

**Technical Implementation:**
- **Key Files to Modify**: 
  - `GameStateData.java` - Add eventQueueData field
  - `CharacterData.java` - Add usesAutomaticTargeting field  
  - `WeaponData.java` - Add currentFiringMode field
  - `SaveGameController.java` - Add serialization/deserialization logic
- **Event Queue Serialization**: Need to serialize tick, action type, ownerId (not Runnable directly)
- **Database/Save Changes**: Save file format expanded, maintains backwards compatibility
- **Backwards Compatibility**: Use JsonProperty with default values for new fields

**Design Decisions Made:**
- **Event Timing**: Use absolute ticks when loading events
- **Character vs Weapon Firing Mode**: Per-weapon property (weapon.currentFiringMode)
- **Event References**: Throw error if saved events reference units/characters that no longer exist
- **Muzzle Flash Priority**: Do not save muzzle flash state (0.5-second visual effect not critical)
- **Load Strategy**: Pause game during event queue reconstruction to prevent timing conflicts
- **Save File Size**: No concerns about file size increase
- **30+ Events Overhead**: Acceptable during active combat
- **Event Retention**: Do not discard old events

**Remaining Questions for Implementation:**
- **Event Queue Complexity**: How to serialize/deserialize Runnable actions - need action type enum and reconstruction logic
- **Event Queue Scope**: Which event types should be saved vs temporary visual effects
- **Event Validation**: How to validate loaded events execute correctly without causing game imbalance
- **Event Type Filtering**: Should certain event types be excluded to prevent save file bloat

**Technical Challenges:**
- **Event Action Serialization**: ScheduledEvent contains Runnable actions that can't be serialized directly
- **Event Reconstruction**: Need factory/registry system to recreate events from serialized data
- **Error Handling**: Graceful handling of corrupted or incomplete save data
- **Testing Strategy**: Need comprehensive backwards compatibility testing with existing save files

**Estimated Complexity**: Medium-High - Requires significant event system architecture work and thorough testing


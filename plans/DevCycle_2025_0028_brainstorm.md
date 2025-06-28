# DevCycle 2025-0028 Brainstorm
*Created: June 28, 2025*

## Overview

DevCycle 28 focuses on advanced tactical combat systems that enhance player control and tactical decision-making. The cycle introduces two sophisticated combat mechanics: Multiple Shot Control System and Reaction Action System.

## System 1: Multiple Shot Control System

### Core Concept
Players can configure characters to fire multiple shots automatically with specific timing and accuracy patterns, simulating rapid engagement sequences while maintaining tactical control over shot accuracy.

### Key Features

#### Shot Configuration Control
- **CTRL-1 Cycling**: Pressing CTRL-1 cycles through multiple shot options (1-5 shots)
- **Per-Character Setting**: Each character maintains their own multiple shot preference
- **Persistence**: Setting persists through target changes and save/load operations
- **Default Value**: 1 shot (standard single-shot behavior)

#### Shot Timing and Accuracy Pattern
- **First Shot**: Uses character's current aiming speed (Careful/Normal/Quick)
- **Follow-up Shots (2nd, 3rd)**: Automatically use Quick aiming speed
- **Fourth Shot**: Returns to character's aiming speed
- **Fifth Shot**: Uses Quick aiming speed
- **Pattern**: Aimed → Quick → Quick → Aimed → Quick (repeating for higher counts)
- **Inter-Shot Timing**: Uses same delay as Quick aiming mode between shots
- **Accumulated Aiming Bonuses**: Follow-up shots reset accumulated bonuses (first shot retains bonuses)
- **First Shot Penalty**: Follow-up shots not affected by first-shot-on-target penalty (if such mechanic exists)
- **Burst Weapon Integration**: Multiple shots work as multiple trigger pulls (burst = multiple shots per trigger pull)
- **Sequence Cancellation**: Can be cancelled by changing shot count to lower number or changing target
- **Reaction Integration**: Characters in reaction mode follow normal multiple shot pattern

#### Ammunition and Reloading Integration
- **Ammunition Depletion**: Shooting sequence stops when weapon runs out of ammunition
- **Automatic Reload**: Character automatically reloads when ammunition exhausted
- **Post-Reload Behavior**: Multiple shot sequence stops after reload (reloading ends the cycle)
- **Reload Timing**: Uses character's current aiming speed for reload timing
- **Normal Modifiers**: All shots apply standard combat modifiers (movement, stress, etc.)
- **Weapon Restrictions**: Single-shot and melee weapons do not support multiple shots

#### User Interface Integration
- **Selection Requirement**: CTRL-1 cycles all selected characters to same shot count value
- **Visual Feedback**: Character stats display shows current multiple shot setting
- **Console Output**: No changes to existing firing messages for multiple shot sequences

### Technical Implementation Considerations
- **Character Property**: Add `multipleShootCount` field to Character class
- **State Tracking**: Track current shot number in sequence during firing
- **Aiming Speed Override**: Temporarily override aiming speed for follow-up shots
- **Save Integration**: Include multipleShootCount in character serialization (reaction state not saved - players re-establish after loading)

## System 2: Reaction Action System

### Core Concept
Characters can be set to automatically respond to target weapon state changes, simulating anticipatory combat situations where one character reacts to another's hostile actions.

### Key Features

#### Reaction Setup Control
- **CTRL-SHIFT-Right-Click**: Sets target and establishes reaction monitoring
- **Hold State**: Character moves to their preferred hold state (based on firing preference)
- **Initial State Recording**: System records target's current weapon state as baseline
- **Visual Indication**: No visual changes for reaction mode
- **Reaction Cancellation**: Right-clicking on the reacting character cancels reaction setup

#### Trigger Conditions
- **State Change Detection**: Monitors target's weapon state continuously (every tick)
- **Trigger Event**: Any change from initial weapon state (including intermediate states like grippinginholster)
- **Immediate Response**: Character begins attack sequence when trigger occurs
- **State Specificity**: Only responds to weapon state changes, not movement or other actions
- **No Range Limit**: Characters can react to weapon state changes at any distance
- **Chain Reactions**: Character reactions can trigger other character reactions (with normal 30-tick + Reflex delay) when the reacting character changes weapon state

#### Reaction Timing and Modifiers
- **Base Delay**: 30-tick delay from trigger detection to attack initiation
- **Reflex Modifier**: Character's Reflexes stat modifies reaction delay
- **Calculation**: Higher Reflexes = faster reaction time
- **Range**: Approximately 20-40 ticks depending on Reflexes stat (using existing modifier system)

#### Tactical Applications
- **Overwatch**: Cover areas with reaction fire capability
- **Standoff Situations**: Mexican standoff scenarios with mutual reaction threats
- **Defensive Positioning**: React to hostile weapon draws in tense situations
- **Intimidation**: Visual deterrent showing readiness to respond

### Technical Implementation Considerations
- **Target Monitoring**: Continuous weapon state monitoring for reaction targets
- **State Tracking**: Record initial weapon state and detect changes
- **Reaction Queue**: Manage multiple characters with active reactions (one target per character)
- **Integration**: Work with existing combat and weapon state systems

## Implementation Priority

### High Priority Systems
1. **Multiple Shot Control System** - Core functionality with CTRL-1 cycling
2. **Reaction Action System** - Basic CTRL-right-click reaction setup

### Implementation Approach
- **Character Class Extensions**: Add new fields and methods for both systems
- **Input Handling**: Integrate CTRL-1 and CTRL-SHIFT-right-click controls (separate from existing CTRL-right-click)
- **Combat Integration**: Modify existing attack systems to support new behaviors
- **Testing**: Comprehensive testing for edge cases and system interactions

## Success Criteria

### Multiple Shot System
- [ ] CTRL-1 cycles through 1-5 shot configurations
- [ ] Shot patterns follow specified timing (Aimed → Quick → Quick → Aimed → Quick)
- [ ] Ammunition handling stops sequence and triggers reload
- [ ] Settings persist through targets and save/load
- [ ] Works with all weapon types and combat situations

### Reaction System
- [ ] CTRL-SHIFT-right-click establishes reaction monitoring
- [ ] Character holds in preferred weapon state while monitoring
- [ ] Detects target weapon state changes accurately (every tick monitoring)
- [ ] Reaction delay uses 30-tick base with Reflex modifiers
- [ ] Integrates with existing combat accuracy and timing systems
- [ ] Respects firing preference system and accumulated aiming bonuses
- [ ] Single target monitoring per character
- [ ] Multiple shot integration (fires all shots in sequence when triggered)

## Technical Risks and Considerations

### Multiple Shot System Risks
- **State Management**: Tracking shot sequences through complex combat scenarios
- **Ammunition Edge Cases**: Handling partial bursts when ammunition runs low
- **Aiming Speed Override**: Ensuring temporary speed changes don't break other systems
- **Performance**: Managing multiple rapid-fire sequences simultaneously

### Reaction System Risks
- **State Monitoring**: Continuous monitoring without performance impact
- **Trigger Accuracy**: Ensuring reliable detection of weapon state changes
- **Multiple Reactions**: Handling scenarios with multiple characters in reaction mode
- **Combat Integration**: Preserving existing combat flow while adding reaction triggers

## Future Enhancement Opportunities

### Multiple Shot Extensions
- **Weapon-Specific Patterns**: Different shot patterns for different weapon types
- **Skill-Based Improvements**: Higher skills reduce Quick shot penalties
- **Ammunition Type Integration**: Different patterns for different ammunition types

### Reaction Extensions
- **Multiple Trigger Types**: React to movement, health changes, or other actions
- **Conditional Reactions**: React only under specific circumstances
- **Chain Reactions**: Multiple characters reacting to single trigger event

## Development Notes

### Implementation Order
1. **Multiple Shot System**: Simpler system with clear input/output requirements
2. **Reaction System**: More complex monitoring and trigger system

### Testing Strategy
- **Unit Tests**: Core functionality for both systems
- **Integration Tests**: Interaction with existing combat systems
- **Scenario Tests**: Complex tactical situations using both systems
- **Performance Tests**: Multiple characters using systems simultaneously

### Documentation Requirements
- **User Controls**: CTRL-1 and CTRL-right-click behavior documentation
- **Tactical Guide**: Strategic applications of both systems
- **Technical Documentation**: Implementation details for future maintenance

---

**Brainstorm Status**: Ready for planning phase
**Estimated Complexity**: Medium-High (two interconnected advanced combat systems)
**Dependencies**: Existing weapon state system, combat timing infrastructure, input handling system

## Planning Questions for User Review

### Control Scheme and Input Handling
1. **CTRL-Right-Click Conflict**: DevCycle 25 already implemented CTRL-right-click for "target and hold at current hold state". How should the Reaction Action System differentiate from this existing behavior? Should it use a different key combination, or modify the existing CTRL-right-click to detect intent?
- Change this system to use CTRL-SHIFT-Right click

2. **Multiple Character Selection**: For Multiple Shot System, what should happen if multiple characters are selected when CTRL-1 is pressed? Should it cycle all selected characters' settings, show an error, or only affect the first selected character?
Cycle all character settings

3. **Control Feedback**: What specific visual indicators should show that a character is in reaction mode? Should it be similar to the hold state indicators, or something distinct?
- Let it be similar to hold state indicators

### Multiple Shot System Timing and Mechanics
4. **Inter-Shot Timing**: What is the exact timing delay between shots in a multiple shot sequence? Should it use the weapon's firing delay, a fixed delay, or some other timing mechanism?
- It uses the same delay as if the character was in Quick aiming mode.

5. **Aiming Speed Transition**: When switching from the character's aiming speed to Quick speed for follow-up shots, does this affect accumulated aiming bonuses from DevCycle 27? Should accumulated bonuses be preserved, reset, or applied differently?
- Follow up shots always reset bonuses, except for the specific first shot penalty

6. **Burst vs Multiple Shot Interaction**: How does the Multiple Shot System interact with existing burst fire weapons (like the UZI from DevCycle 20)? Can characters set a multiple shot count on burst weapons, and if so, how do they interact?
Yes. Think of it like multiple trigger pulls. A burst gives you multiple shots per trigger pull. Multiple shots is multiple trigger pulls.

7. **Shot Sequence Cancellation**: Can players cancel a multiple shot sequence mid-firing? If so, what inputs cancel it, and does the character return to their normal firing behavior immediately?
- Yes, a multiple shot sequence can be cancelled. One way would be to change the shot selection to a lower number. One way would be to change target. This multiple shot mechanic is an automated way to take a first shot in whatever aimed mode the player wants, then switch to quick for a number of shots, and back to the original aimed mode.

### Reaction System Mechanics and Edge Cases
8. **Reaction Cancellation**: How can players cancel a reaction setup? Should it be automatic after firing, manual with a specific input, or persist until explicitly changed?
- Right clicking on the shooting character cancels reaction setup.

9. **Multiple Simultaneous Reactions**: If Alice is in reaction mode watching Bob, and Charlie is in reaction mode watching Alice, what happens when Bob draws his weapon? Does Alice's reaction trigger Charlie's reaction? How should chain reactions be handled?
- Yes, Alice's reaction will trigger Charlie's reaction, after Alice changes weapon state.

10. **Reaction Range Limitations**: Should reaction monitoring have a maximum range limit? Can characters react to weapon state changes at any distance, or should there be line-of-sight and distance restrictions?
- No range limit

11. **State Change Specificity**: The document mentions reacting to "any change from initial weapon state." Should this include ALL state changes (e.g., holstered → grippinginholster → drawing), or only changes that represent an escalation toward combat readiness?
- all state changes

### System Integration and Conflicts
12. **Multiple Shot + Reaction Interaction**: What happens if a character has multiple shots set to 3, and they're in reaction mode when triggered? Do they fire all 3 shots in the reaction, or just 1 shot with normal reaction timing?
- Character takes 3 shots

13. **Hold State Integration**: How does the Reaction System integrate with the existing hold state system from DevCycle 25? When a character moves to their "preferred hold state," should this respect their current hold state setting (H key cycling), or override it?
- Override it. Reactions cause attacks.

14. **Firing Preference Integration**: How do these systems interact with the firing preference system from DevCycle 26? Should reaction shots respect the character's firing preference (aiming vs point-from-hip), or always use a specific state?
- The system obeys the firing preference system.

15. **Accumulated Aiming Bonus Interaction**: For reaction shots, should characters benefit from accumulated aiming bonuses if they've been holding in an aiming state? Or should reaction shots use a separate accuracy calculation?
- Characters do benefit from accumulated aim bonuses

### Ammunition and Weapon Compatibility
16. **Weapon Type Restrictions**: Are there any weapon types that should NOT support multiple shots or reaction firing? Should melee weapons, single-shot weapons, or other weapon types be excluded?
- Single shot and melee weapons do not support multiple shots

17. **Ammunition Management**: For multiple shot sequences, if a character has 2 rounds left and sets multiple shots to 4, what should happen? Fire 2 shots and reload, then continue with remaining 2 shots? Or fire 2 shots, reload, and reset to single shot mode?
- Fire 2 shots, reload, then fire 4 shots.

18. **Reload Timing Integration**: When a character reloads during a multiple shot sequence, should the reload timing use their current aiming speed, or always use a standard timing regardless of the shot pattern?
- Use current aiming speed

### Performance and Technical Implementation
19. **Monitoring Efficiency**: For the Reaction System, how frequently should the system check for weapon state changes? Every tick, every few ticks, or only when significant events occur?
- Check every tick

20. **Maximum Reaction Targets**: Should there be a limit on how many characters can be simultaneously monitoring the same target? Or how many targets a single character can monitor?
- A character can only monitor one target

21. **Save File Impact**: What specific new character properties need to be saved? Beyond `multipleShootCount`, are there reaction target IDs, reaction states, or other persistent data that needs serialization?
- Reaction does not need to be saved

### User Experience and Feedback
22. **Console Output Details**: For multiple shot sequences, should each shot show "Shot 2 of 4" type messaging, or should it be more subtle? How detailed should the reaction system console output be?
- Do not change the messaging for multiple shot sequences

23. **Error Handling**: What should happen if a player tries to set up a reaction on a target that doesn't have weapon states (like an unarmed character)? Should it show an error, automatically cancel, or allow setup but never trigger?
- Allow setup but never trigger

24. **Training/Tutorial Integration**: Should there be any in-game guidance or tooltips to help players understand these complex new systems, or is external documentation sufficient?
- External documentation

## Additional Planning Questions Based on User Responses

### Multiple Shot System Clarifications
25. **Post-Reload Shot Count Clarification**: When you said "Fire 2 shots, reload, then fire 4 shots" - does this mean the character fires the FULL multiple shot count (4) after reloading, regardless of how many shots were fired before reload? Or should it be "Fire 2 shots, reload, then fire remaining 2 shots"?
- Sorry, I was thinking about auto-targeting situations. If this is a manual firing situation, fire 2 shots, then reload, then stop. Reloading should stop the cycle of multiple shots.

26. **Multiple Character CTRL-1 Behavior**: When CTRL-1 is pressed with multiple characters selected, should they all cycle to the same shot count value, or should each character cycle independently through their own sequence (1→2→3→4→5→1)?
- They all cycle to the same shot count value

27. **First Shot Penalty Exception**: You mentioned "Follow up shots always reset bonuses, except for the specific first shot penalty" - what is this "specific first shot penalty"? Is this referring to burst fire's first shot vs subsequent shots penalty from DevCycle 20?
- I might need a followup question to this. Isn't there a mechanic int he game where the first shot on a target has a penalty? Follow up shots would not be affected by this penalty.

### Reaction System Integration
28. **Chain Reaction Timing**: When Alice reacts to Bob and triggers Charlie's reaction, does Charlie get the normal 30-tick (+ Reflex modifier) delay, or does the chain reaction happen immediately?
- Charlie gets the normal 30 tick delay, becuase Charlie is reacting to Alice's action.

29. **Reaction State Persistence**: Since reaction state isn't saved, does this mean all reaction setups are lost when the game is saved/loaded? Should players need to re-establish all reactions after loading?
- Yes, for now. 

30. **Multiple Shot + Reaction Shot Pattern**: When a character in reaction mode fires multiple shots, do they follow the normal pattern (Aimed→Quick→Quick→Aimed→Quick) or does the reaction override this to use a different pattern?
- They follow the normal pattern

### Technical Implementation Details
31. **Weapon State Change Granularity**: For "all state changes" triggering reactions, should intermediate states like "grippinginholster" trigger reactions, or only states that represent drawing/readying weapons for combat?
- grippinginholster triggers a reaction.

32. **Reaction Visual Feedback**: How should the visual indication for reaction mode differ from hold state indicators? Same icon but different color? Different icon entirely?
- There are no icon changes.

33. **CTRL-SHIFT-Right-Click Implementation**: Should this be a completely separate input handler, or should it modify the existing CTRL-right-click to check for SHIFT modifier and branch accordingly?
- This is a separate implementation. Do not cancel the existing CTRL-rightclick implementation for holding. CTRL-SHIFT-rightclick is a separate command.

## Final Clarification Questions

### First Shot Penalty Mechanic
34. **First Shot Penalty Clarification**: You mentioned follow-up shots should not be affected by "first shot penalty" but asked for a follow-up question. Does the current game have a mechanic where the first shot fired at a new target has an accuracy penalty? If so, what is this penalty and how should multiple shots interact with it?
- Please look up the answer to this question. I believe there is a mechanic for first shot penalty.

### Error Handling and Edge Cases
35. **Unarmed Target Reactions**: For reaction setup on targets without weapon states, you said "Allow setup but never trigger." Should there be any console feedback to inform the player that the reaction was set up but won't trigger, or should it be silent?
- No console feedback.

36. **Multiple Shot with Insufficient Ammunition**: If a character has multiple shots set to 4 but only has 1 round in the weapon, what should happen? Fire 1 shot and reload (ending sequence), or should there be some indication to the player beforehand?
No indication to the character. Fire 1 shot and reload (ending equence) is correct.

### Technical Implementation Confirmation
37. **Manual vs Auto-Targeting Distinction**: You mentioned thinking about "auto-targeting situations" when discussing reload behavior. Should the multiple shot system behave differently in auto-targeting scenarios versus manual player commands, or should both follow the same "reload ends sequence" rule?
- the multiple shot system should behave the same way in auto-targeting scenarios.

# DevCycle 2025-0013 Brainstorm

## Known Issues

### Bug 1: Auto-Targeting Melee Weapon Issues
**Description**: AutoTargeting is not quite working for melee weapons. After the first attack, all characters stop attacking. Even if nobody is incapacitated.

**Impact**: Auto-targeting functionality broken for melee combat, significantly limiting automated combat capabilities.

### Bug 2: Character Facing Direction During Auto-Targeting Movement
**Description**: While Auto Targeting a ranged attack, characters are facing the direction they are moving. They should face their target if they are moving while shooting at a target.

**Impact**: Visual inconsistency and tactical realism - characters should orient toward their target while attacking, not their movement direction.

### Bug 3: Auto-Targeting Activation After Manual Shot
**Description**: If a character who is NOT Auto Targeting takes a shot at an opponent (and doesn't incapacitate that opponent) and then turns on Auto Targeting, the character does not shoot. I think the...

**Impact**: Auto-targeting state management issue prevents proper activation after manual attacks.

## Initial Analysis

This development cycle appears focused on fixing auto-targeting system bugs that affect both ranged and melee combat mechanics. The issues suggest problems with:

1. State management in auto-targeting system
2. Character orientation logic during combat
3. Transition between manual and automated combat modes

## Next Steps

- Investigate auto-targeting implementation in OpenFields2.java
- Analyze melee combat state transitions
- Review character facing/orientation code
- Test manual-to-auto targeting workflows
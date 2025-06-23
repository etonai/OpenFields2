# DevCycle 2025-0016 Brainstorm: Double-Firing Bug in Auto-Target Combat

## Problem Statement

In auto-target ranged combat, characters sometimes fire multiple bullets in the same tick, causing:
- Ammunition waste (multiple bullets consumed simultaneously) 
- Unrealistic combat behavior (impossible firing rates)
- Potential game balance issues

## Evidence from Log Analysis

From the provided log, Alice fires twice at tick 308:
```
1000:Alice weapon state: firing at tick 308
*** 1000:Alice fires a 9mm round from Uzi Submachine Gun (ammo remaining: 23)
*** 1000:Alice shoots a 9mm round at 1003:Drake at distance 42.86 feet using Uzi Submachine Gun at tick 308
1000:Alice weapon state: firing at tick 308
*** 1000:Alice fires a 9mm round from Uzi Submachine Gun (ammo remaining: 22)  
*** 1000:Alice shoots a 9mm round at 1003:Drake at distance 42.86 feet using Uzi Submachine Gun at tick 308
```

## Root Cause Hypotheses

1. **Event Queue Duplication**: Multiple firing events scheduled for the same tick
2. **Auto-Target Logic Issue**: Auto-targeting system scheduling attacks while weapon is already firing
3. **State Transition Race Condition**: Weapon transitioning to firing state multiple times in same tick
4. **Recovery/Firing Overlap**: Recovery state ending and immediately triggering another fire in same tick

## Investigation Areas

### Primary Focus
- Auto-targeting event scheduling logic
- Weapon state transition validation  
- Event queue management for duplicate prevention

### Secondary Areas
- Attack continuation logic after recovery states
- Tick-based event collision detection
- Single-shot vs. burst fire mode handling

## Success Criteria

- Characters fire exactly one bullet per intended shot
- No multiple bullets consumed in same tick
- Auto-targeting maintains proper firing cadence
- Weapon state transitions remain atomic

## Technical Scope

This appears to be a timing/scheduling bug in the auto-target system rather than a fundamental weapon mechanics issue, since manual targeting likely doesn't exhibit this behavior.

EDNOTE: This bug affects game balance and realism. The Uzi should have a high rate of fire, but each bullet should be fired in sequence according to the weapon's timing, not multiple bullets simultaneously.
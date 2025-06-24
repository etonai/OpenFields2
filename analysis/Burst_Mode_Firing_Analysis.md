# Burst Mode Firing Analysis - OpenFields2

*Created: 2025-06-23*  
*Updated: 2025-06-24 - DevCycle 20 fixes*

## Overview

This document provides a comprehensive analysis of how burst mode firing works in the OpenFields2 tactical combat game. Burst mode is one of three firing modes available for automatic weapons, firing a predetermined number of rounds (typically 3) per trigger pull with quick succession timing.

## Key Components

### 1. Firing Mode Configuration

#### FiringMode Enum
Located in `combat/FiringMode.java`:
```java
public enum FiringMode {
    SINGLE_SHOT,    // Fire one round per trigger pull
    BURST,          // Fire 3 rounds per trigger pull
    FULL_AUTO       // Fire continuously until trigger released or ammo exhausted
}
```

#### Weapon Configuration Properties
Weapons define burst capability in JSON configuration files:
```json
"burstSize": 3,
"cyclicRate": 6,
"availableFiringModes": ["SINGLE_SHOT", "BURST", "FULL_AUTO"]
```

### 2. Example: UZI Submachine Gun Configuration

From `/src/main/resources/data/themes/test_theme/ranged-weapons.json`:
```json
"wpn_uzi": {
    "name": "Uzi Submachine Gun",
    "type": "SUBMACHINE_GUN",
    "damage": 30,
    "ammunition": 32,
    "firingDelay": 6,
    "cyclicRate": 6,
    "burstSize": 3,
    "availableFiringModes": [
        "SINGLE_SHOT",
        "BURST", 
        "FULL_AUTO"
    ]
}
```

**Key Properties:**
- **Burst Size**: 3 rounds per burst
- **Cyclic Rate**: 6 ticks between shots (0.1 seconds at 60 FPS)
- **Firing Delay**: 6 ticks base delay
- **Available Modes**: Single, Burst, Full Auto

## 3. Burst Firing Mechanism

### State Management Variables
Located in `combat/Character.java`:
```java
public boolean isAutomaticFiring = false;   // Currently in automatic firing mode
public int burstShotsFired = 0;             // Number of shots fired in current burst
public AimingSpeed savedAimingSpeed = null; // Saved aiming speed for first shot in burst/auto
```

### Burst Initiation Process

1. **First Shot**: Character goes through normal aiming sequence using current aiming speed
2. **Burst Activation**: After first shot fires, `isAutomaticFiring = true`
3. **Rapid Succession**: Remaining shots (typically 2 more) scheduled immediately with reduced timing

### Timing Sequence Example
```java
// First shot: Full aiming time (e.g., 30 ticks for normal aiming)
// Shot 2: fireTick + firingDelay * 1 = +6 ticks
// Shot 3: fireTick + firingDelay * 2 = +12 ticks
```

**UPDATE (DevCycle 20)**: Burst timing now correctly uses `firingDelay` instead of `cyclicRate`.

## 4. Implementation Details

### Burst Scheduling Code
Located in `Character.java:1118-1162`:
```java
// Handle burst firing - schedule additional shots immediately after first shot
if (weapon instanceof RangedWeapon && 
    ((RangedWeapon)weapon).getCurrentFiringMode() == FiringMode.BURST && 
    !isAutomaticFiring) {
    
    isAutomaticFiring = true;
    burstShotsFired = 1; // First shot just fired
    lastAutomaticShot = fireTick;
    
    // Schedule remaining shots in the burst
    for (int shot = 2; shot <= ((RangedWeapon)weapon).getBurstSize(); shot++) {
        long nextShotTick = fireTick + (((RangedWeapon)weapon).getFiringDelay() * (shot - 1));
        final int shotNumber = shot;
        eventQueue.add(new ScheduledEvent(nextShotTick, () -> {
            // Fire additional burst shot with validation
            if (currentTarget != null && !currentTarget.character.isIncapacitated() && 
                !this.isIncapacitated() && weapon instanceof RangedWeapon && 
                ((RangedWeapon)weapon).getAmmunition() > 0) {
                
                // Execute shot, decrement ammo, play sound, schedule projectile
                burstShotsFired++;
                // ... shot execution logic
            }
        }, ownerId));
    }
}
```

### Burst Completion Logic
```java
// Reset burst state after final shot
if (burstShotsFired >= ((RangedWeapon)weapon).getBurstSize()) {
    isAutomaticFiring = false;
    burstShotsFired = 0;
    savedAimingSpeed = null;
}
```

## 5. Aiming Speed Logic

### First vs Subsequent Shot Accuracy

**UPDATE (DevCycle 20)**: The aiming speed system has been simplified. Characters no longer change their aiming speed state during burst/auto firing. Instead:

- **First Shot**: Uses character's current aiming speed modifier
- **Subsequent Shots (2+)**: Apply a fixed -20 accuracy penalty (Quick aiming modifier)
- **State Management**: Character's aiming speed remains unchanged throughout burst

The new implementation in `Character.java`:
```java
public boolean shouldApplyBurstAutoPenalty() {
    if (weapon == null || !(weapon instanceof RangedWeapon)) {
        return false;
    }
    
    FiringMode mode = ((RangedWeapon)weapon).getCurrentFiringMode();
    if (mode == FiringMode.BURST || mode == FiringMode.FULL_AUTO) {
        // Apply penalty to bullets 2+ in burst/auto
        return isAutomaticFiring && burstShotsFired > 1;
    }
    
    return false;
}
```

This penalty is applied in `CombatCalculator.java` during hit determination:
```java
double burstAutoPenalty = shooter.character.shouldApplyBurstAutoPenalty() ? -20.0 : 0.0;
```

## 6. Ammunition and Resource Management

### Ammunition Consumption
- Each shot in burst consumes 1 round of ammunition
- Ammunition check performed before each shot in sequence
- Burst terminates early if ammunition runs out

### Console Logging Example
```
"Soldier1 burst fires shot 2/3 (9mm round, ammo remaining: 30)"
```

## 7. Interruption Conditions

**UPDATE (DevCycle 20)**: Interruption behavior has been refined:

Burst firing can be interrupted by:

1. **Firing Mode Switch**: Changing firing mode immediately cancels burst/auto
2. **Shooter Incapacitation**: Shooter takes critical damage during burst
3. **Ammunition Depletion**: No rounds remaining mid-burst (fires available rounds)
4. **Wounds/Hesitation**: Combat stress effects trigger hesitation state
5. **New Attack Command**: Starting a new attack cancels ongoing burst/auto

**Important**: Target incapacitation does NOT interrupt burst - remaining bullets fire at corpse location

### Interruption Handling
```java
} else {
    // Burst interrupted
    isAutomaticFiring = false;
    burstShotsFired = 0;
    savedAimingSpeed = null;
    System.out.println(getDisplayName() + " burst firing interrupted (target lost or no ammo)");
}
```

## 8. Mode Switching Mechanism

### Cycling Through Firing Modes
Located in `RangedWeapon.java:152-158`:
```java
public void cycleFiringMode() {
    if (availableFiringModes.size() <= 1) return; // No modes to cycle
    
    int currentIndex = availableFiringModes.indexOf(currentFiringMode);
    int nextIndex = (currentIndex + 1) % availableFiringModes.size();
    currentFiringMode = availableFiringModes.get(nextIndex);
}
```

**Mode Progression**: Single Shot → Burst → Full Auto → Single Shot (cycles)

### Display Names
```java
public String getFiringModeDisplayName() {
    switch (currentFiringMode) {
        case SINGLE_SHOT: return "Single";
        case BURST: return "Burst";
        case FULL_AUTO: return "Auto";
        default: return "Unknown";
    }
}
```

## 9. Combat Effectiveness Analysis

### Tactical Advantages
- **Controlled Firepower**: More shots than single, less wasteful than full-auto
- **Hit Probability**: Multiple projectiles increase chance of target engagement
- **Suppression**: Creates area denial effect
- **Ammunition Economy**: Uses exactly 3 rounds per engagement

### Tactical Disadvantages
- **Reduced Accuracy**: Follow-up shots use Quick aiming (-20 accuracy)
- **Predictable Pattern**: Always fires exactly 3 rounds
- **Commitment**: Cannot stop mid-burst once initiated
- **Ammunition Consumption**: 3x usage compared to single shot

### Timing Analysis
For UZI example:
- **Total Burst Duration**: ~0.2 seconds (12 ticks)
- **Shot Intervals**: 0.1 seconds between shots
- **Recovery Time**: Additional firing delay after burst completion

## 10. Integration with Combat System

### Event Scheduling
- Uses game's `ScheduledEvent` system for precise timing
- Each burst shot is individual scheduled event
- Maintains proper sequence even under combat stress

### Audio/Visual Effects
- Each shot triggers weapon sound effect
- Muzzle flash and firing highlights applied per shot
- Projectile impact calculations performed individually

### Wound System Integration
- Each projectile can cause separate wounds
- Damage accumulation from multiple hits
- Individual hit/miss calculations per shot

## 11. Future Enhancement Opportunities

### Potential Improvements
1. **Variable Burst Sizes**: Allow weapons to have different burst sizes (2, 4, 5 rounds)
2. **Burst Spread Simulation**: Implement accuracy degradation over burst duration
3. **Advanced Interruption**: More nuanced interruption conditions
4. **Burst Mode Variants**: Different burst patterns (controlled pairs, etc.)

### Configuration Flexibility
- Per-weapon burst size customization
- Adjustable cyclic rates for different weapon types
- Mode availability based on weapon condition/upgrades

## Summary

Burst mode in OpenFields2 provides a tactical middle ground between single shot precision and full-auto suppression. The system uses event scheduling to create realistic timing between shots while managing ammunition, targeting, and combat state. The first shot uses the character's intended aiming speed, while follow-up shots are fired rapidly with reduced accuracy, creating an authentic representation of burst fire mechanics.

The implementation successfully balances realism with gameplay considerations, providing players with meaningful tactical choices while maintaining the game's turn-based combat flow through precise event timing.

## 12. DevCycle 20 Changes Summary

The following key changes were implemented in DevCycle 20:

1. **Timing Fix**: Burst and full auto modes now use `firingDelay` instead of `cyclicRate`
2. **Accuracy System**: Simplified to apply fixed -20 penalty to bullets 2+ without changing character state
3. **State Management**: Removed `savedAimingSpeed` - character's aiming speed never changes
4. **Interruption Logic**: 
   - Added immediate cancellation on mode switch and new attack commands
   - Burst continues firing at corpse if target dies mid-burst
   - Partial bursts allowed with insufficient ammo
5. **Full Auto Consistency**: Full auto mode follows same rules as burst (firingDelay timing, -20 penalty)

These changes ensure burst firing works as originally intended, with predictable timing and accuracy modifiers.

---

*This analysis is based on OpenFields2 codebase as of DevCycle 20 (2025-06-24)*
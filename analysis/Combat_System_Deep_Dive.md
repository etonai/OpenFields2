# Combat System Deep Dive - OpenFields2
*Generated: 2025-06-24*

## Overview

The OpenFields2 combat system is a sophisticated simulation that models both ranged and melee combat with realistic ballistics, comprehensive modifiers, and detailed wound tracking. This document provides an in-depth technical analysis of the combat implementation.

## Combat Flow Architecture

### 1. Attack Initiation Flow

```
User Input (right-click target)
    ↓
InputManager.handleRightClick()
    ↓
Character.startAttackSequence()
    ↓
Weapon State Transitions
    ├─→ Ready State
    ├─→ Aiming State (timing based on aiming speed)
    └─→ Firing State
         ↓
    Projectile Creation (ranged) or
    Immediate Resolution (melee)
         ↓
    Hit Calculation
         ↓
    Damage & Wound Application
         ↓
    Post-Combat Effects
```

### 2. Weapon State System

Each weapon maintains state through WeaponState objects:

```java
WeaponState {
    String state;        // Current state name
    String nextState;    // Transition target
    int tickLength;      // Duration in ticks
}
```

**State Transitions**:
- **Holstered → Drawing** (15 ticks × speed multiplier)
- **Drawing → Ready** (automatic)
- **Ready → Aiming** (based on aiming speed: 15-60 ticks)
- **Aiming → Firing** (automatic)
- **Firing → Recovering** (5 ticks)
- **Recovering → Ready** (automatic)

## Hit Determination System

### 3. Probability Calculation

The hit chance formula combines 16+ modifiers:

```
Base Chance: 50%
+ Dexterity Modifier: -20 to +20 (based on stat 1-100)
+ Stress Modifier: -40 to 0 (modified by coolness)
+ Range Modifier: varies by distance/max range ratio
+ Weapon Accuracy: weapon-specific modifier
+ Movement Modifier: 0 to -25 (based on shooter movement)
+ Aiming Speed Modifier: -20 to +15
+ Burst/Auto Penalty: -20 (bullets 2+ in burst)
+ Target Movement: based on perpendicular velocity
+ Wound Modifier: -2 per wound level
+ Skill Modifier: +5 per skill level
+ Position Modifier: based on target stance
+ Bravery Modifier: -30 max from failures
+ First Attack Penalty: -15 (unless very careful)
+ Size Modifier: 0 (future implementation)
+ Cover Modifier: 0 (future implementation)
= Final Hit Chance (minimum 0.01% if in range)
```

### 4. Range Calculations

**Range Modifier Formula**:
```java
if (distance <= maxRange) {
    modifier = ((maxRange - distance) / maxRange) * 20.0;
} else {
    modifier = -50.0; // Out of range penalty
}
```

**Distance Calculations**:
- Ranged: Center-to-center distance
- Melee: Edge-to-edge distance
- Conversion: 7 pixels = 1 foot

### 5. Movement Impact

**Shooter Movement Penalties**:
- Stationary: 0
- Walking: -5
- Crawling: -10
- Jogging: -15  
- Running: -25

**Target Movement Calculation**:
```java
perpendicularVelocity = target.getPerpendicularVelocity(shooter);
if (velocity < 2.1) modifier = 0;        // Slow
else if (velocity < 6.3) modifier = -5;  // Medium
else modifier = -10;                     // Fast
```

## Damage and Wound System

### 6. Damage Calculation

**Base Damage Flow**:
1. Weapon base damage value
2. Apply location multiplier (head shots deal more)
3. Add strength bonus (melee only)
4. Reduce by armor (future implementation)

**Wound Severity Determination**:
```java
if (totalDamage >= 35) return "CRITICAL";
else if (totalDamage >= 20) return "Serious";
else if (totalDamage >= 10) return "Light";
else return "Scratch";
```

### 7. Body Part Selection

**Ranged Combat** (stray shots):
- Torso: 40% + position modifiers
- Head: 10% + position modifiers  
- Arms: 20% (10% each)
- Legs: 30% (15% each)

**Melee Combat**:
- Always targets torso (future: selective targeting)

### 8. Wound Effects

**Immediate Effects**:
- Health reduction
- Potential hesitation (based on willpower check)
- Movement penalty for leg wounds
- Dexterity penalty for arm wounds

**Ongoing Effects**:
- Cumulative accuracy penalties (-2 per wound level)
- Bravery checks for serious/critical wounds
- Incapacitation at 0 health

## Special Combat Mechanics

### 9. Burst and Automatic Fire

**Burst Mode** (DevCycle 20):
- Fires exactly `burstSize` bullets
- Timing: Uses `firingDelay` between shots
- First bullet: Normal accuracy
- Bullets 2+: -20 accuracy penalty
- Can be interrupted by mode switch or new attack

**Full Auto Mode**:
- Continuous firing until trigger release
- Same timing as burst (firingDelay)
- Same accuracy penalties as burst
- Consumes ammo rapidly

### 10. First Attack Penalty

**Conditions**:
- Applied when engaging new target: -15 accuracy
- Not applied if using very careful aiming
- Resets when switching targets
- Affects first bullet only in burst/auto

### 11. Skill Integration

**Weapon Skills**:
- Pistol/Rifle/SMG: +5 accuracy per level
- Skill determined by weapon type
- Very careful aiming doubles skill bonus

**Speed Skills**:
- Quickdraw: 5% faster weapon ready per level
- Stacks with reflexes modifier
- Affects all preparation states

### 12. Projectile Physics

**Travel Time Calculation**:
```java
travelTime = (distance / weaponVelocity) * TICKS_PER_SECOND;
impactTick = currentTick + travelTime;
```

**Projectile Properties**:
- Velocity in feet/second
- No gravity or wind (future features)
- Instant trajectory calculation
- Visual tracer rendering

## Combat Statistics Tracking

### 13. Comprehensive Metrics

**Per Character Tracking**:
- Attacks attempted (separated by ranged/melee)
- Successful hits (separated by ranged/melee)
- Wounds inflicted by severity
- Headshots (attempted/successful/kills)
- Targets incapacitated
- Combat engagements

**Statistical Analysis**:
- Hit percentage calculation
- Wound severity distribution
- Preferred weapon usage
- Combat effectiveness rating

## AI Combat Behavior

### 14. Automatic Targeting

**Target Selection Priority**:
1. Hostile faction check
2. Range verification
3. Line of sight (future)
4. Threat assessment (future)

**Engagement Rules**:
- Persistent attack until target eliminated
- Automatic target switching on kill
- Zone-based prioritization
- Configurable aggression levels

### 15. Combat AI State Machine

```
Idle → Target Search → Target Acquired → 
Weapon Ready → Aiming → Firing → 
Result Evaluation → (Continue or New Target)
```

## Performance Considerations

### 16. Optimization Strategies

**Current Optimizations**:
- Event-based system (no polling)
- Lazy distance calculations
- Cached modifier values
- Efficient projectile updates

**Scaling Challenges**:
- O(n²) for all-pairs distance checks
- Per-tick combat calculations
- Event queue growth with unit count
- Projectile tracking overhead

## Future Enhancements

### 17. Planned Features

**Near Term**:
- Cover system implementation
- Suppression mechanics
- Armor and penetration
- Selective body part targeting

**Long Term**:
- Environmental effects (wind, weather)
- Explosive weapons and area effects
- Squad-based tactics
- Morale system integration

## Code Quality Analysis

### 18. Strengths

1. **Modular Design**: Clear separation between calculation and resolution
2. **Data-Driven**: Weapon stats from JSON configuration
3. **Comprehensive**: Models many real-world factors
4. **Extensible**: Easy to add new modifiers or weapons

### 19. Improvement Areas

1. **Method Complexity**: Some calculation methods exceed 50 lines
2. **Magic Numbers**: Some constants still hardcoded
3. **Test Coverage**: Critical combat paths lack unit tests
4. **Performance**: Not optimized for large battles

## Conclusion

The OpenFields2 combat system represents a mature, realistic tactical combat simulation. With 16+ factors affecting hit probability, detailed wound modeling, and sophisticated weapon handling, it provides deep tactical gameplay. Recent improvements to burst firing and first attack penalties show continued refinement.

The system is well-architected for its current scale but will need optimization for the planned 40+ character battles. The modular design supports future enhancements while maintaining code clarity.

---
*This analysis covers combat system implementation through DevCycle 20*
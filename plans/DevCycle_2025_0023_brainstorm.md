# DevCycle_2025_0023_brainstorm.md
*Created: 2025-06-25 | Status: BRAINSTORMING*

## Overview
This document contains the brainstorming for DevCycle 23, focusing on the Defense System and Skill System Integration phases transferred from the future development roadmap.

## Phase 4: Defense System

### 4. Defense System ⏳ **PLANNED** (Phase 4)
- [ ] **Defensive Mechanics Implementation**
  - [ ] Create automatic defend attempt system when targeted
  - [ ] Implement defend success calculation using Reflexes and weapon defend score
  - [ ] Design defend cooldown to prevent continuous defending
  - [ ] Implement separate defense state machine (independent of weapon states)

- [ ] **Counter-Attack System**
  - [ ] Implement counter-attack opportunities after successful defenses
  - [ ] Create faster counter-attack timing than normal attacks
  - [ ] Design counter-attack damage bonuses
  - [ ] Add skill-based counter-attack improvement

- [ ] **Unified Defense System**
  - [ ] Combine blocking, dodging, and parrying under single "defending" action
  - [ ] Design defensive action priority system
  - [ ] Add defensive skill progression

**Defensive System Features:**
- **Automatic Defense**: Characters automatically attempt to defend when attacked
- **Defend Success**: Base 50% + Dexterity Modifier + (Weapon Skill Level × 5), capped at 99%
- **Counter-Attack Window**: 0.5-1 second window after successful defense for enhanced attack
- **Defense Cooldown**: 1 second between defensive actions (allows multiple defends per enemy attack)
- **Separate State Machine**: Defense state independent of weapon states for clean logic separation

## Phase 5: Skill System Integration

### 5. Skill System Integration ⏳ **PLANNED** (Phase 5)
- [ ] **Melee Weapon Skills**
  - [ ] Create melee weapon skills for knife, tomahawk, rifle (melee)
  - [ ] Track meleeAttacksAttempted, meleeAttacksSuccessful, meleeWoundsInflicted by severity
  - [ ] Apply +5 accuracy per skill level (same as ranged weapons)
  - [ ] Implement skill-based defensive improvements

- [ ] **Character Stat Integration**
  - [ ] Link Dexterity to melee attack accuracy and defense success
  - [ ] Connect Strength to melee damage output
  - [ ] Tie Reflexes to defend success and counter-attacks
  - [ ] Use existing Coolness for combat stress effects

- [ ] **Skill Tracking System**
  - [ ] Track melee attacks attempted and successful
  - [ ] Track melee wounds inflicted by severity
  - [ ] Track defensive actions and success rates
  - [ ] Implement skill experience gain from combat participation

**Skill Integration:**
- **Base Skills**: Weapon skills range from 0-9 (same as ranged weapons)
- **Weapon Skills**: Knife, Tomahawk, Rifle (melee) skills available
- **Skill Effects**: Apply melee weapon bonuses same way as ranged weapon bonuses
- **Defense Skills**: Weapon skills also improve defensive capabilities

## Technical Implementation Plan

### Phase 4: Defense System (Estimated: 4-5 days)
**Priority:** High - Essential for balanced combat

**Implementation Steps:**
1. **Separate Defense State Machine** (Day 1-2)
   - Create DefenseState enum (ready, defending, cooldown)
   - Implement defense state manager independent of weapon states
   - Design defense availability logic (cannot defend while attacking/switching)

2. **Automatic Defense System** (Day 2-3)
   - Implement automatic defend attempt when targeted by melee attack
   - Create defend success calculation (Base 50% + Dex + Weapon Skill × 5, max 99%)
   - Process defensive attempts immediately (not as scheduled events)

3. **Counter-Attack Implementation** (Day 3-5)
   - Implement counter-attack window after successful defense
   - Create enhanced counter-attack timing and damage calculations
   - Add counter-attack skill-based improvements
   - Integrate counter-attacks with existing event queue

### Phase 5: Skill System Integration (Estimated: 3-4 days)
**Priority:** Medium-High - Enhances character progression

**Implementation Steps:**
1. **Melee Skills Creation** (Day 1-2)
   - Create melee weapon skill types (Knife, Tomahawk, Rifle melee)
   - Implement skill effect calculations for accuracy and defense
   - Design skill progression rates and thresholds

2. **Combat Statistics Tracking** (Day 2-3)
   - Track meleeAttacksAttempted, meleeAttacksSuccessful
   - Track meleeWoundsInflicted by severity
   - Track defensiveAttempts, defensiveSuccesses
   - Implement skill experience gain from combat participation

3. **Character Stat Integration** (Day 3-4)
   - Link Dexterity to attack accuracy and defense success
   - Connect Strength to damage output and counter-attack power
   - Tie Reflexes to defense timing and counter-attack availability
   - Integrate Coolness for combat stress effects

## Testing Strategy

### Unit Testing Requirements
- [ ] **Defense System Tests**
  - [ ] Defend success calculation accuracy with various stat combinations
  - [ ] Counter-attack timing and damage verification
  - [ ] Defense cooldown and limitation enforcement
  - [ ] Defense state machine transitions and conflicts

- [ ] **Skill System Tests**
  - [ ] Skill progression calculations and experience gain
  - [ ] Stat modifier applications to combat effectiveness
  - [ ] Combat statistics tracking accuracy
  - [ ] Skill-based improvement verification

## Files to Modify

### Core Implementation Files
- **`src/main/java/combat/Character.java`** - Add defense state and skill progression
- **`src/main/java/combat/MeleeCombatResolver.java`** - Enhance with defense and counter-attacks
- **`src/main/java/CombatResolver.java`** - Integrate defensive mechanics
- **`src/main/java/OpenFields2.java`** - Add advanced combat features to game loop

### New Implementation Files
- **`src/main/java/combat/DefenseSystem.java`** - Defensive mechanics and counter-attacks
- **`src/main/java/combat/DefenseState.java`** - Defense state machine enum
- **`src/main/java/combat/CombatStatistics.java`** - Skill tracking and progression

### Enhancement Files
- **`src/main/java/data/CharacterData.java`** - Add defense and skill progression data
- **`src/main/java/data/SkillsManager.java`** - Enhance with melee skill management

## Risk Assessment

### Technical Risks
- **Defense System Complexity**: Automatic defense with counter-attacks might create complex interaction chains
- **Performance Impact**: Additional defensive calculations could affect performance
- **Balance Challenges**: Defensive mechanics and skill progression require extensive balancing

### Integration Risks
- **Combat Flow Disruption**: Advanced features might complicate existing melee combat implementation
- **Save Compatibility**: Additional combat data might affect save file structure

## Success Criteria

### Functional Requirements
- [ ] Automatic defense system works reliably with proper cooldowns
- [ ] Counter-attacks provide tactical depth without breaking balance
- [ ] Skill system rewards melee combat participation and improvement

### Balance Requirements
- [ ] Defensive options remain viable without being overpowered
- [ ] Skill progression feels rewarding but not game-breaking
- [ ] Character stats meaningfully impact all aspects of melee combat
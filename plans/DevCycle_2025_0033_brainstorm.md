# DevCycle 2025-0033 Brainstorm: Melee Combat Enhancement

## Overview
This DevCycle focuses on improving the melee combat system to address audio feedback issues and implement proper counter-attacks.

## System 1: Melee Combat System Enhancement

### Current Issues Identified
- **Audio Overload**: Excessive audio output during melee combat suggests too many actions firing simultaneously
- **Defense/Counterattack Frequency**: Suspected over-triggering of defensive actions, each generating audio
- **Missing Counter-attacks**: Previously disabled due to action overflow, but desired functionality for tactical depth

### Melee Combat Problems Analysis
1. **Audio Feedback Loop**: Each defensive or counterattack action may be triggering weapon sound effects
2. **Action Cascade**: Multiple characters engaging in melee may create cascading defensive responses
3. **Event Queue Overflow**: Too many simultaneous melee events overwhelming the combat system
4. **State Management**: Possible issues with weapon state transitions during rapid melee exchanges

### Enhancement Goals
- **Controlled Counter-attacks**: Restore counter-attack functionality with proper rate limiting
- **Audio Management**: Implement intelligent audio throttling for melee combat
- **Action Prioritization**: Establish clear hierarchy for simultaneous melee actions
- **Tactical Depth**: Add meaningful defensive options without overwhelming complexity

### Potential Solutions to Explore
1. **Audio Throttling**: Limit melee audio to one sound per character per time window
2. **Counter-attack Cooldowns**: Implement cooldown periods between counter-attack opportunities
3. **Action Queue Management**: Priority-based resolution of simultaneous melee actions
4. **Defensive Stances**: Allow characters to choose defensive vs. aggressive melee postures

## Technical Architecture Considerations

### Melee Combat Enhancement
- **Combat Manager Integration**: Leverage existing CombatCoordinator and manager pattern
- **Audio System**: Extend current audio system with throttling capabilities
- **State Machine**: Enhance weapon state management for melee counter-attacks
- **Event Priority**: Integrate with existing ScheduledEvent system for action ordering

## Development Priority
1. **Phase 1**: Investigate and fix melee combat audio issues
2. **Phase 2**: Implement controlled counter-attack system

## Detailed Problem Analysis (Based on output.txt Investigation)

### Root Cause Identified: Melee Attack Spam Loop

**The Problem**: Analysis of the output.txt file reveals that the excessive audio is NOT caused by defensive actions or counter-attacks, but by a **melee attack spam loop** where the attacker continuously schedules new attacks every tick while in recovery.

**Timeline of Events**:
1. **Tick 312**: Chris successfully hits Bobby with Bowie Knife
2. **Tick 312**: Chris enters 120-tick recovery period (until tick 432)
3. **Ticks 313-421**: Every single tick, the system:
   - Schedules a new melee attack impact event
   - Plays weapon sound (`/Slap0003.wav`)
   - Executes the attack but correctly blocks it due to recovery
   - **Result**: ~108 redundant audio plays in ~2 seconds!**

**Audio Frequency**: 1 sound per tick = 60 sounds per second during the spam period.

### Technical Analysis

**Attack Scheduling Chain**:
1. `checkContinuousAttack()` → `handleAttackContinuation()`
2. `AutoTargetingSystem` → `startMeleeAttackSequence()`
3. `AttackSequenceManager.startMeleeAttackSequence()` → `scheduleMeleeAttackFromCurrentState()`
4. `CombatCoordinator.startMeleeAttackSequenceInternal()` → `MeleeCombatSequenceManager.scheduleMeleeAttack()`
5. **Audio plays BEFORE recovery check**

**Key Finding**: The recovery check works correctly (shows "ATTACK BLOCKED"), but audio is triggered during event scheduling, not during attack execution validation.

## Fix Plan: Melee Attack Spam Prevention

### Priority 1: Stop Attack Scheduling During Recovery

**Location**: `CombatCoordinator.handleAttackContinuation()` and related methods

**Changes Needed**:

1. **Add Recovery Check in Attack Continuation**:
   - Before calling `character.updateAutomaticTargeting()` or starting new attacks
   - Check if character is in recovery period for melee weapons
   - Block new attack scheduling if already in recovery

2. **Move Audio Trigger After Validation**:
   - **Current**: Audio plays in `MeleeCombatSequenceManager.scheduleMeleeAttack()` line 168
   - **Fix**: Move audio to AFTER the recovery check passes in the scheduled event execution
   - Only play audio when attack actually executes, not when it's scheduled

3. **Add Recovery State Tracking**:
   - Track recovery end time in Character class
   - Add `isInMeleeRecovery(currentTick)` method
   - Use this in attack continuation logic

### Priority 2: Enhanced Recovery Management

**Implementation Approach**:

1. **Character Class Enhancement**:
   ```java
   // Add to Character.java
   public long meleeRecoveryEndTick = -1;
   
   public boolean isInMeleeRecovery(long currentTick) {
       return meleeRecoveryEndTick > currentTick;
   }
   ```

2. **CombatCoordinator.handleAttackContinuation() Fix**:
   ```java
   // Add before auto-targeting check
   if (character.isInMeleeRecovery(currentTick)) {
       return; // Don't schedule new attacks during recovery
   }
   ```

3. **MeleeCombatSequenceManager.scheduleMeleeAttack() Fix**:
   ```java
   // Move audio from line 168 to inside the scheduled event, after validation
   // Only play audio when attack actually executes
   ```

### Priority 3: Auto-Targeting Refinement

**Problem**: Auto-targeting may be triggering too frequently during melee combat.

**Solution**: Add melee-specific delays to auto-targeting re-evaluation to prevent rapid-fire attack attempts.

### Testing Strategy

1. **Before Fix**: Count audio events during 10-second melee combat
2. **After Fix**: Verify only successful attacks trigger audio
3. **Recovery Verification**: Ensure no new attacks scheduled during recovery period
4. **Auto-targeting**: Test that legitimate attack continuation works after recovery ends

### Files to Modify

1. `src/main/java/combat/Character.java` - Add recovery tracking
2. `src/main/java/combat/CombatCoordinator.java` - Fix handleAttackContinuation()
3. `src/main/java/combat/managers/MeleeCombatSequenceManager.java` - Move audio trigger
4. `src/main/java/combat/AutoTargetingSystem.java` - Add melee-specific delays

## Research Questions
- How does the current melee audio system trigger sounds? **✅ ANSWERED: Audio triggers during event scheduling**
- What specific conditions cause counter-attack cascades? **✅ ANSWERED: Not counter-attacks, but attack spam loop**
- How can we implement counter-attacks without overwhelming the action queue?
- What audio throttling mechanisms would work best for melee combat?
# DevCycle 15b Test Procedures and Validation Framework
*Created: June 21, 2025*

## Overview
This document provides comprehensive test procedures for validating all critical InputManager functionality. These tests serve as regression prevention and establish behavior baselines for future development.

## Test Setup Requirements

### Prerequisites
1. **Application Build**: Ensure `mvn compile` completes successfully
2. **Application Launch**: Start via `mvn javafx:run`
3. **Game State**: Unpause game with Space key
4. **Test Characters**: Create characters using character creation system
5. **Test Environment**: Clear battlefield for controlled testing

### Character Setup for Testing
```
Recommended Test Setup:
- Create 3-4 characters from different factions
- Assign mix of ranged and melee weapons
- Position characters at various distances
- Save test scenario for repeatability
```

## Phase 1: Critical Functionality Testing

### 1. Rectangle Selection Validation

#### Test 1.1: Multi-Unit Selection with Mouse Drag
**Objective**: Verify mouse drag creates selection rectangle and selects multiple units

**Test Steps**:
1. Ensure multiple characters are visible on screen
2. Click and hold left mouse button on empty area
3. Drag to create rectangle encompassing multiple units
4. Release mouse button
5. Verify selection feedback and unit highlighting

**Expected Results**:
- Selection rectangle appears during drag
- Rectangle updates in real-time with mouse movement
- Units within rectangle become selected when drag completes
- Selected units show visual highlighting
- Selection count displays correctly

**Pass Criteria**:
- ✅ Selection rectangle renders properly
- ✅ Multiple units selected simultaneously  
- ✅ Visual feedback clear and accurate
- ✅ No crashes or errors during selection

#### Test 1.2: Selection Rectangle Rendering
**Objective**: Verify selection rectangle visual appearance and behavior

**Test Steps**:
1. Start mouse drag selection
2. Observe rectangle appearance, color, and opacity
3. Test rectangle at different zoom levels
4. Test rectangle with camera pan operations
5. Verify rectangle clears after selection

**Expected Results**:
- Rectangle has clear, visible outline
- Rectangle color contrasts with background
- Rectangle scales properly with zoom
- Rectangle moves correctly with camera pan
- Rectangle disappears after selection completes

#### Test 1.3: Selection with Shift Modifier
**Objective**: Verify additive selection works with Shift key

**Test Steps**:
1. Select one or more units normally
2. Hold Shift key
3. Drag select additional units
4. Release Shift and mouse
5. Verify both original and new units selected

**Expected Results**:
- Original selection maintained
- New units added to selection
- Total selection count increases appropriately
- Visual feedback shows all selected units

#### Test 1.4: Selection State Management
**Objective**: Verify selection state persistence and clearing

**Test Steps**:
1. Select multiple units
2. Test selection persistence during camera operations
3. Click empty area to clear selection
4. Verify selection state resets properly
5. Test selection with incapacitated units

**Expected Results**:
- Selection persists during zoom/pan
- Empty area click clears all selections
- Selection state tracking accurate
- Incapacitated units handle selection correctly

### 2. Melee Combat Testing

#### Test 2.1: Movement to Melee Range
**Objective**: Verify units move to melee range when ordered to attack

**Test Steps**:
1. Select unit with melee weapon
2. Right-click enemy unit outside melee range
3. Observe unit movement toward target
4. Verify unit stops at correct melee range
5. Test with different melee weapon types

**Expected Results**:
- Unit begins movement toward target
- Movement stops at appropriate melee range
- Range calculation accounts for weapon reach
- Unit faces target when in range

#### Test 2.2: Attack Execution When in Range
**Objective**: Verify melee attack executes when units reach range

**Test Steps**:
1. Position units at melee range
2. Order melee attack
3. Verify attack animation/feedback
4. Check damage application
5. Test attack timing and weapon states

**Expected Results**:
- Attack executes automatically when in range
- Visual/audio feedback for attack
- Damage applied to target
- Weapon state transitions correctly

#### Test 2.3: Melee Weapon Range Calculations
**Objective**: Verify different melee weapons have correct ranges

**Test Steps**:
1. Test attacks with short melee weapons (daggers)
2. Test attacks with medium melee weapons (swords)
3. Test attacks with long melee weapons (polearms)
4. Verify range differences work correctly
5. Test edge cases at maximum range

**Expected Results**:
- Short weapons require closest proximity
- Medium weapons allow moderate distance
- Long weapons work at extended range
- Range boundaries enforced accurately

#### Test 2.4: Melee State Transitions
**Objective**: Verify weapon state management during melee combat

**Test Steps**:
1. Observe weapon states during combat sequence
2. Check weapon readying/unsheathing
3. Verify attack states and timing
4. Test state transitions with interruptions
5. Verify state recovery after combat

**Expected Results**:
- Weapon states transition logically
- Timing matches weapon specifications
- Interruptions handled gracefully
- States reset properly after combat

### 3. Auto-Targeting Verification

#### Test 3.1: Auto-Targeting Toggle Functionality
**Objective**: Verify auto-targeting can be enabled/disabled

**Test Steps**:
1. Locate auto-targeting toggle control
2. Test enabling auto-targeting
3. Test disabling auto-targeting
4. Verify toggle state persistence
5. Test toggle during combat

**Expected Results**:
- Toggle responds to user input
- Auto-targeting behavior changes appropriately
- State persists across game operations
- Toggle works during active combat

#### Test 3.2: Target Acquisition Logic
**Objective**: Verify auto-targeting selects appropriate targets

**Test Steps**:
1. Enable auto-targeting
2. Position unit near multiple potential targets
3. Observe target selection behavior
4. Test with different weapon ranges
5. Verify target priority logic

**Expected Results**:
- Closest valid target selected
- Target within weapon range
- Enemy faction targets prioritized
- Invalid targets ignored

#### Test 3.3: Target Tracking Behavior
**Objective**: Verify auto-targeting tracks moving targets

**Test Steps**:
1. Enable auto-targeting
2. Acquire target
3. Move target unit
4. Observe tracking behavior
5. Test tracking limits and range

**Expected Results**:
- Target tracked during movement
- Tracking lost if out of range
- New target acquired if original lost
- Tracking respects weapon limitations

#### Test 3.4: Targeting State Persistence
**Objective**: Verify targeting state survives game operations

**Test Steps**:
1. Set up auto-targeting scenario
2. Test persistence during camera operations
3. Test persistence during save/load
4. Verify state after other game actions
5. Test state with multiple units

**Expected Results**:
- Targeting state survives zoom/pan
- State persists through save/load
- Other actions don't corrupt targeting
- Multiple unit targeting works independently

### 4. Character Stats Display Testing

#### Test 4.1: Character Information Display
**Objective**: Verify complete character information displays correctly

**Test Steps**:
1. Select character
2. Observe basic character information display
3. Check health, stats, faction information
4. Verify information accuracy against data
5. Test with different character types

**Expected Results**:
- All character data visible
- Information matches internal data
- Display formatting consistent
- Different character types handled correctly

#### Test 4.2: Stats Hotkey Functionality (Shift+/)
**Objective**: Verify detailed stats hotkey works properly

**Test Steps**:
1. Select character
2. Press Shift+/ to display detailed stats
3. Verify comprehensive information appears
4. Test hotkey toggle (show/hide)
5. Test with multiple characters selected

**Expected Results**:
- Hotkey triggers detailed display
- Comprehensive stats shown
- Toggle works properly
- Multiple character selection handled

#### Test 4.3: Weapon Information Display
**Objective**: Verify weapon stats display completely and accurately

**Test Steps**:
1. Select character with weapons
2. Verify ranged weapon information
3. Verify melee weapon information
4. Check weapon state information
5. Test with different weapon configurations

**Expected Results**:
- Ranged weapon stats complete (damage, accuracy, range, velocity)
- Melee weapon stats complete (damage, accuracy, reach)
- Weapon states shown correctly
- Active weapon indicated clearly

#### Test 4.4: Extended Stats Display Completeness
**Objective**: Verify all extended character information displays

**Test Steps**:
1. Activate extended stats display
2. Verify skill information
3. Check combat statistics
4. Verify archetype information
5. Test formatting and layout

**Expected Results**:
- All skills displayed with levels
- Combat stats accurate (attacks, successes, wounds)
- Archetype information shown
- Layout clear and readable

## Test Documentation Standards

### Result Recording Format
For each test:
```
Test ID: [Test Number]
Date: [Test Date]
Result: [PASS/FAIL/PARTIAL]
Notes: [Detailed observations]
Issues: [Any problems found]
Regression Risk: [High/Medium/Low]
```

### Baseline Behavior Documentation
Record exact behavior for:
- UI feedback and visual elements
- Timing and responsiveness
- State transitions and persistence
- Error handling and edge cases

## Phase 2: Regression Prevention Framework

### Behavior Baseline Documentation
Create detailed documentation of:
1. **Input Response Times**: Measure and document typical response latencies
2. **State Transition Maps**: Document all valid state changes
3. **Visual Feedback Specifications**: Exact appearance of UI elements
4. **Error Conditions**: Catalog error scenarios and responses

### Test Case Formalization
Convert manual test procedures into:
1. **Reproducible Test Scripts**: Step-by-step procedures
2. **Pass/Fail Criteria**: Objective success measures
3. **Test Data Sets**: Consistent scenarios for testing
4. **Regression Checklists**: Quick validation procedures

## Test Execution Guidelines

### Testing Session Management
1. **Pre-Test Setup**: Verify clean application state
2. **Test Isolation**: Each test starts from known state
3. **Result Documentation**: Record all findings immediately
4. **Issue Tracking**: Log any anomalies or concerns

### Quality Validation
1. **Functional Correctness**: Does it work as designed?
2. **Performance Acceptability**: Is response time reasonable?
3. **Visual Quality**: Are graphics and feedback clear?
4. **Stability**: Any crashes, errors, or instability?

---

*This test framework provides comprehensive validation of InputManager functionality while establishing regression prevention baselines for future development.*
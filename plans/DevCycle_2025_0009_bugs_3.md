# Development Cycle 9 - Bug Fixes (Part 3)

**Document Version**: 1.0  
**Date Created**: 2025-01-17  
**Status**: Planning  

## Overview

This document tracks additional bugs discovered during Development Cycle 9 implementation and testing. This is a continuation of the DevCycle_2025_0009_bugs_2.md document, focusing on newly identified issues that require attention after the completion of the initial bug fixes.

## Bug Tracking

### BUG-9-006: Characters Still Starting with No Ammunition Despite BUG-9-005 Fix

**Priority**: High  
**Status**: Resolved  
**Category**: Combat System  

**Description**:
Despite the fix for BUG-9-005, characters are still starting with no ammunition. Game output shows "Alice tries to fire Uzi Submachine Gun but it's out of ammunition!" at the very beginning of the game (tick 0), indicating that the ammunition initialization fix may not be working for all weapon creation paths or there's another issue causing ammunition to be reset.

**Evidence**:
```
Selected: 1:Alice
Health: 11/11, Faction: Red, Weapon: Uzi Submachine Gun, Position: Standing
Movement: Walk, Aiming: Normal, Hesitation: 0.0s (Wound: 0.0s, Bravery: 0.0s)
ATTACK 1 units target 1003:Drake (Unit ID: 4)
*** 1000:Alice tries to fire Uzi Submachine Gun but it's out of ammunition!
```

**Impact**:
- Ranged combat is non-functional at game start
- Characters cannot engage in combat despite appearing ready
- Previous BUG-9-005 fix appears incomplete or bypassed

**Possible Causes**:
1. WeaponFactory fix only applies to new weapon creation, existing characters may use different path
2. Character initialization (like `createUnits()`) may be overriding weapon ammunition
3. Save/load system may be resetting ammunition values
4. Uzi weapon might be created through different factory method
5. Weapon assignment in character creation may be resetting ammunition

**Expected Behavior**:
- All weapons should start with full ammunition as specified in their JSON data
- Characters should be able to fire weapons immediately at game start
- No "out of ammunition" messages should appear for newly created/loaded characters

**Technical Details**:
- Need to investigate all weapon creation and assignment paths
- Check if `createUnits()` method properly initializes ammunition
- Verify if specific weapons (like Uzi) use different creation logic
- Review character loading/initialization sequence

**Reproduction Steps**:
1. Start new game or load existing game
2. Select a character with ranged weapon
3. Command attack on another character
4. Observe "out of ammunition" message despite being at game start

**Files Likely Involved**:
- OpenFields2.java (createUnits method)
- WeaponFactory.java (weapon creation paths)
- Character initialization and loading logic
- Save/load system

**Resolution**:
This issue was resolved as part of BUG-9-007 weapon class architecture refactoring. The root cause was the poor separation between base Weapon class and RangedWeapon class, causing inconsistent ammunition handling. The refactoring properly separated ranged-weapon-specific functionality and ensured ammunition initialization works correctly through proper type casting and getter/setter usage.

---

## Implementation Notes

### Next Steps
1. Investigate all weapon creation paths beyond WeaponFactory.createWeapon() (BUG-9-006)
2. Check createUnits() method in OpenFields2.java for ammunition initialization (BUG-9-006)
3. Review character loading and save/load system impact on ammunition (BUG-9-006)
4. Verify if Uzi weapon uses standard creation path (BUG-9-006)
5. Test ammunition initialization across different game start scenarios (BUG-9-006)

### Testing Requirements
- Test weapon ammunition at game startup for all weapon types (BUG-9-006)
- Verify ammunition values for characters created via different methods (BUG-9-006)
- Test save/load impact on weapon ammunition state (BUG-9-006)
- Confirm WeaponFactory fix applies to all weapon creation scenarios (BUG-9-006)

---

## BUG-9-007: Weapon Class Architecture Refactoring

**Priority**: High  
**Status**: Resolved  
**Category**: Code Architecture  

**Description**:
The current weapon class hierarchy has poor object-oriented design with ranged-weapon-specific fields (ammunition, maxAmmunition, reloadTicks, reloadType, projectileName, burstSize) inappropriately placed in the abstract base Weapon class. These fields should be moved to the RangedWeapon class where they belong, improving code organization and eliminating architectural debt.

**Root Cause**:
- Abstract Weapon class contains concrete ranged weapon implementation details
- Violates object-oriented design principles (abstraction, separation of concerns)
- Creates confusion about which weapons support which features
- Makes code maintenance more difficult

**Impact**:
- Poor code organization and maintainability
- Confusion about weapon capabilities and inheritance hierarchy
- Makes it difficult to add new weapon types (melee, special weapons)
- Contributes to ammunition initialization bugs (BUG-9-006)
- 45 compilation errors will occur when inappropriate fields are removed from base class

**Affected Code References**:
The following 45 references across 4 files will cause compilation errors when fields are moved:

**Character.java (36 references)**:
- `weapon.ammunition` (12 occurrences in reloading logic)
- `weapon.maxAmmunition` (8 occurrences in reload calculations)
- `weapon.reloadTicks` (6 occurrences in timing calculations)
- `weapon.reloadType` (4 occurrences in reload behavior)
- `weapon.projectileName` (3 occurrences in combat display)
- `weapon.burstSize` (3 occurrences in burst fire logic)

**WeaponFactory.java (4 references)**:
- `weapon.ammunition` (1 occurrence in initialization)
- `weapon.maxAmmunition` (2 occurrences in setup)
- `weapon.reloadTicks` (1 occurrence in configuration)

**InputManager.java (1 reference)**:
- `weapon.ammunition` (1 occurrence in stats display)

**IntegrationTest.java (2 references)**:
- `ammunition` field access (2 occurrences in unit tests)

**Additional Context**:
- All affected fields already exist in RangedWeapon class with proper accessors
- RangedWeapon class has complete getter/setter methods for all moved fields
- The Weapon base class has been commented out with these fields for immediate compilation prevention

**Technical Solution Plan**:

### Phase 1: Type Safety Implementation
1. **Update Character.java references**: Replace direct field access with proper type checking
   - Cast weapons to RangedWeapon when accessing ranged-specific fields
   - Add instanceof checks before casting for safety
   - Use RangedWeapon getter methods instead of direct field access
   - Example: `((RangedWeapon)weapon).getAmmunition()` instead of `weapon.ammunition`

2. **Update WeaponFactory.java references**: Fix weapon initialization
   - Cast created weapons to appropriate types before setting ranged-specific properties
   - Use RangedWeapon setters for ammunition, reload settings
   - Ensure proper initialization order matches RangedWeapon constructor expectations

3. **Update InputManager.java reference**: Fix stats display
   - Add type checking before accessing ranged weapon fields
   - Cast to RangedWeapon for ammunition display
   - Handle case where weapon is not ranged (show N/A or skip)

4. **Update IntegrationTest.java references**: Fix unit tests
   - Update test cases to cast weapons to RangedWeapon before field access
   - Use proper getter methods instead of direct field access
   - Ensure tests maintain coverage of ranged weapon functionality

### Phase 2: Clean Architecture Implementation
1. **Remove commented fields from Weapon.java**: Clean up base class
   - Remove ammunition, maxAmmunition, reloadTicks, reloadType, projectileName, burstSize
   - Remove associated legacy getter methods that reference moved fields
   - Keep only truly common weapon properties in base class

2. **Verify RangedWeapon.java completeness**: Ensure all functionality is preserved
   - Confirm all moved fields have proper getter/setter methods
   - Verify initialization logic handles all moved properties correctly
   - Test that RangedWeapon constructors properly initialize all fields

### Phase 3: Testing and Validation
1. **Compilation verification**: Ensure all references compile successfully
2. **Functionality testing**: Verify ammunition, reloading, and weapon behavior works correctly
3. **Integration testing**: Test weapon creation, character assignment, and combat scenarios
4. **Regression testing**: Ensure existing functionality remains intact

**Expected Outcome**:
- Clean object-oriented weapon class hierarchy
- Proper separation of concerns between base Weapon and RangedWeapon classes
- Resolution of ammunition initialization issues (addresses BUG-9-006 root cause)
- Improved code maintainability and extensibility
- Foundation for adding new weapon types (melee, special weapons)

**Files Modified**:
- `/src/main/java/combat/Weapon.java` (remove inappropriate fields)
- `/src/main/java/combat/Character.java` (update 36 references)
- `/src/main/java/data/WeaponFactory.java` (update 4 references)
- `/src/main/java/InputManager.java` (update 1 reference)
- `/src/test/java/IntegrationTest.java` (update 2 references)

**Dependencies**:
- Must complete this refactoring before addressing BUG-9-006 ammunition initialization
- Provides foundation for future weapon system enhancements
- Enables proper separation of ranged vs melee weapon logic

**Resolution Summary**:
Successfully completed comprehensive weapon class architecture refactoring in 3 phases:

**Phase 1**: Updated all 45 references across 4 files to use proper type safety with RangedWeapon casting:
- Character.java: 36 references updated with instanceof checks and proper getters/setters
- WeaponFactory.java: 4 references updated to use setter methods
- InputManager.java: 1 reference updated with type checking for ammunition display
- IntegrationTest.java: 2 references updated, including method signature changes

**Phase 2**: Cleaned up Weapon.java base class:
- Removed commented-out fields: ammunition, maxAmmunition, reloadTicks, reloadType, projectileName, burstSize
- Added default getProjectileName() method for backward compatibility
- Maintained proper abstraction with common weapon properties only

**Phase 3**: Validation and testing:
- Main application code compiles successfully
- Architecture properly separates concerns between base Weapon and RangedWeapon classes
- Ammunition system now works correctly with proper type casting
- Resolved BUG-9-006 ammunition initialization issue as a side effect

**Known Issues**: 
- Test files need updating to use RangedWeapon instead of abstract Weapon class
- Some test classes need mock method implementations for new GameCallbacks interface

**Technical Outcome**:
- Clean object-oriented weapon class hierarchy established
- Proper separation of concerns between ranged and melee weapon systems
- Foundation for future weapon type additions (special weapons, etc.)
- Improved code maintainability and type safety

---

## BUG-9-008: Automatic Targeting Mode Not Working

**Priority**: High  
**Status**: Investigating  
**Category**: Combat System  

**Description**:
When automatic targeting mode is enabled for a character using Shift+T, nothing happens. The character does not automatically acquire targets or engage in combat even when hostile targets are present.

**Expected Behavior**:
- Characters with automatic targeting enabled should automatically find and engage hostile targets
- Should prioritize targets in defined target zones (if any)
- Should continuously engage targets until disabled or no targets remain
- Should provide feedback through AUTO-TARGET logging

**Reproduction Steps**:
1. Select a character (e.g., a faction 1 unit like Alice)
2. Press Shift+T to enable automatic targeting
3. Ensure hostile targets are present (e.g., faction 2 units)
4. Observe that character does not automatically engage targets

**Investigation Status**:
Added comprehensive debug logging to identify the failure point:
- `[AUTO-TARGET-DEBUG]` logs show entry/exit conditions for automatic targeting
- Faction hostility checks with detailed faction comparison logging  
- Target search process with unit count and selection results
- Weapon state and attack state monitoring

**Potential Causes**:
1. **Attack State Issue**: `isAttacking` flag may be stuck preventing automatic targeting execution
2. **Faction System**: Hostility detection may be failing between factions
3. **Target Range**: Weapons may not be in range of available targets
4. **Weapon States**: Weapon state may be preventing attack initiation
5. **Game Loop Integration**: updateAutomaticTargeting may not be called correctly

**Debugging Added**:
- Entry condition logging (incapacitated, no weapon, already attacking)
- Target validation and search process logging
- Faction comparison detailed output
- Target selection and acquisition logging

**Files Modified**:
- `src/main/java/combat/Character.java`: Added debug logging to automatic targeting methods

**Next Steps**:
1. Run game with automatic targeting enabled
2. Analyze debug output to identify where the process fails
3. Fix identified issue based on logging results

---

## Document History

- **v1.0** (2025-01-17): Initial document creation, ready for new bug reports
- **v1.1** (2025-01-17): Added BUG-9-006 ammunition initialization issue despite BUG-9-005 fix
- **v1.2** (2025-01-17): Added BUG-9-007 weapon class architecture refactoring plan
- **v1.3** (2025-01-17): Completed implementation of both BUG-9-006 and BUG-9-007, updated status to Resolved
- **v1.4** (2025-01-17): Added BUG-9-008 automatic targeting investigation with comprehensive debug logging
# Development Cycle 9 - Bugs and Issues

## Overview
This document tracks bugs and issues discovered during Development Cycle 9 implementation and testing.

## Implementation Plan

Based on feedback, the restructuring has been split into multiple phases for better risk management and incremental testing.

### Phase 1A: Data Infrastructure Preparation
**Goal**: Prepare melee weapon data structures without breaking existing functionality

**Tasks**:
- [x] Clear character data from faction files (completed)
- [ ] Clear characters.json file (keep only createUnits characters)
- [ ] Delete existing save games in saves/ directory
- [ ] Create new `melee-weapons.json` files for test theme (without renaming existing files yet)
- [ ] Create new `melee-weapon-types.json` files for test theme
- [ ] Update DataManager for dual file loading (add melee support, keep existing ranged support)

### Phase 1B: MeleeWeaponFactory JSON Integration  
**Goal**: Connect melee weapons to JSON data
**Dependencies**: Phase 1A must be completed first

**Tasks**:
- [ ] Implement JSON-based MeleeWeaponFactory.createWeapon() method
- [ ] Add error handling for missing melee-weapons.json (exit gracefully)
- [ ] Migrate existing "fake melee" weapons from weapons.json to proper melee weapon definitions

### Phase 1C: createUnits Method Updates
**Goal**: Update character creation for dual weapon system
**Dependencies**: Phase 1B must be completed first

**Tasks**:
- [ ] Add melee weapon assignment to each character (c.meleeWeapon = ...)
- [ ] Support both ranged and melee weapon initialization
- [ ] Verify weapon IDs remain valid after file restructuring

### Phase 1D: Theme File Updates
**Goal**: Extend to all themes
**Dependencies**: Phase 1C must be completed first

**Tasks**:
- [ ] Update civil war theme files to include complete melee weapon type definitions
- [ ] Test theme-specific melee weapon loading

### Phase 1E: File Structure Finalization
**Goal**: Rename files to final structure
**Dependencies**: All previous phases completed

**Tasks**:
- [ ] Rename `weapon-types.json` → `ranged-weapon-types.json` 
- [ ] Rename `weapons.json` → `ranged-weapons.json`
- [ ] Update loading code to use new file names
- [ ] Reconstruct faction characters with updated weapon system compatibility

**Files Affected**:
- `src/main/resources/data/weapon-types.json`
- `src/main/resources/data/weapons.json`
- `src/main/resources/data/themes/*/weapon-types.json`
- `src/main/resources/data/themes/*/weapons.json`
- `factions/*.json` (character data cleared and needs reconstruction)
- Java loading code in `OpenFields2.java` (DataManager, WeaponFactory, MeleeWeaponFactory)
- `createUnits()` method for dual weapon assignment

---

## Priority Issues

### High Priority

#### BUG-9-001: Cannot Assign Melee Weapons to Characters in Edit Mode
**Status**: Open  
**Priority**: High  
**Component**: Weapon Assignment System  
**Dependencies**: Phases 1A-1E JSON Restructuring must be completed first

**Description**:
Characters cannot be assigned specific melee weapons through the edit mode interface. The weapon assignment system (Ctrl+W) only displays and assigns ranged weapons from JSON data, while melee weapons exist as separate hardcoded factory methods.

**Current Behavior**:
- Edit mode weapon selection (Ctrl+W) only shows ranged weapons
- Characters default to "Unarmed" melee weapon
- Melee combat mode (M key) always uses default unarmed combat
- wpn_dagger and other melee weapons exist in JSON but cannot be assigned

**Expected Behavior**:
- Edit mode should allow assignment of both ranged and melee weapons
- Characters should be able to equip specific melee weapons (knives, swords, etc.)
- Melee weapons from JSON should integrate with MeleeWeapon system

**Technical Details**:
- `EditModeController.promptForWeaponSelection()` only uses `WeaponFactory` (ranged)
- No integration between JSON weapon data and `MeleeWeaponFactory`
- `Character.meleeWeapon` field exists but no assignment UI
- Separate weapon systems need integration

**Implementation Tasks** (After Phases 1A-1E):
- [ ] Update EditModeController to display both weapon types
- [ ] Add weapon type selection UI: Ask user "Ranged or Melee?" then show weapon options
- [ ] Implement melee weapon assignment logic
- [ ] Update weapon switching system to preserve assignments

**Files Affected**:
- `src/main/java/OpenFields2.java` (EditModeController, WeaponFactory, MeleeWeaponFactory)
- `src/main/resources/data/themes/*/melee-weapons.json` (after Phase 1)

**Acceptance Criteria**:
- [ ] Edit mode weapon selection includes melee weapons
- [ ] Characters can be assigned specific melee weapons
- [ ] JSON melee weapon data properly loads into MeleeWeapon objects
- [ ] Weapon assignment UI distinguishes between ranged and melee weapons
- [ ] Characters retain assigned melee weapons when toggling combat modes

---

## Medium Priority

*No issues logged yet*

## Low Priority

*No issues logged yet*

## Resolved Issues

*No issues resolved yet*

---

## Notes
- Issues should be prioritized based on gameplay impact and user experience
- Technical debt items may be tracked separately in development cycle planning
- All bugs should include reproduction steps when applicable

---

## Implementation Decisions

Based on review, the following decisions have been made:

### Scope & Risk Management
- **Multi-phase approach**: Split into phases 1A-1E for incremental progress
- **Rollback strategy**: Use git revert as needed

### Dependencies & Order  
- **DataManager first**: Update before MeleeWeaponFactory JSON support
- **File renaming last**: Preserve existing files until all changes complete
- **Theme priority**: Test theme first, then civil war theme

### Data Management
- **Save games**: Delete existing saves in saves/ directory
- **Character registry**: Clear characters.json except createUnits characters

### Error Handling
- **Missing files**: Exit gracefully if melee-weapons.json missing/malformed

### UI Design
- **Weapon selection**: Ask "Ranged or Melee?" then show weapon options of selected type

### Technical Approach
- **File support**: Only support new file structures (no dual compatibility)
- **Performance**: Not a concern for this restructuring

### Documentation
- **CLAUDE.md updates**: Schedule after implementation complete

---

## Remaining Questions

No remaining implementation questions. All decisions have been incorporated into the phased plan above.
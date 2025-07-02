# Iterative Development Cycle - DevCycle YYYY_NNNN
*Created: [Date] at [Time] | Last Design Update: [Date] at [Time] | Last Implementation Update: [Date] at [Time] | Implementation Status: [Status]*

## Overview
This is an iterative development cycle focused on implementing multiple varied tasks to improve system stability, fix critical bugs, and enhance game mechanics. The cycle will address several independent issues and improvements identified through testing and analysis.

**IMPORTANT ITERATIVE CYCLE PRINCIPLES:**
- **One System at a Time**: Focus completely on implementing one system before considering the next
- **No Future Planning**: Do NOT plan future systems while working on the current system
- **No Premature Implementation**: Do NOT implement systems before they are fully planned
- **Sequential Implementation**: Complete each system fully (including testing) before moving to the next
- **Flexible Scope**: Systems 2+ are defined only after System 1 is complete
- **Empty Placeholders**: Future system sections must contain no hints about what those systems should cover

**Development Cycle Goals:**
- [Primary Goal for System 1 - be specific and focused]
- Implement additional system improvements and bug fixes as needed
- Enhance test coverage and validation for affected components
- Address any additional issues discovered during iterative development

**Prerequisites:** 
- [Any dependencies on previous cycles or external requirements]

**Estimated Complexity:** [Low/Medium/High] - Multiple independent fixes with varying complexity levels

## System Implementations

### 1. [System Name] ⭕ **[STATUS]**
- [ ] **[Major Component 1]**
  - [ ] [Specific implementation task 1]
  - [ ] [Specific implementation task 2]
  - [ ] [Specific implementation task 3]
  - [ ] [Integration testing task]

- [ ] **[Major Component 2]**
  - [ ] [Specific implementation task 1]
  - [ ] [Specific implementation task 2]
  - [ ] [Performance testing task]
  - [ ] [Edge case handling]

**Design Specifications:**
- **[Key Specification 1]**: [Detailed requirement with measurable criteria]
- **[Key Specification 2]**: [Detailed requirement with measurable criteria]
- **[Integration Points]**: [How this system interacts with existing code]
- **[User Interface]**: [UI/UX requirements and controls]
- **[Performance Requirements]**: [Any performance constraints or optimization needs]

**Technical Implementation Notes:**
- **Key Files to Modify**: [List of files that need changes]
- **New Classes/Enums**: [New code artifacts to create]
- **Database/Save Changes**: [Any save format or data persistence updates]
- **Backwards Compatibility**: [Compatibility requirements with existing saves/features]

### 2. [Next System] ⭕ **TBD**
*To be determined after System 1 is complete and tested. DO NOT plan this section until System 1 is finished.*

**⚠️ CRITICAL WARNING ⚠️**
**DO NOT ADD ANY INFORMATION TO THIS SECTION UNTIL SYSTEM 1 IS COMPLETE**
- No system names or descriptions
- No implementation tasks or components
- No design specifications
- No technical notes
- This section exists only as a placeholder for future planning

## System Interaction Specifications
**Cross-system integration requirements and conflict resolution:**

*Note: This section will be updated as each system is completed and interactions are discovered.*

- **System 1 + [Existing Systems]**: [How System 1 integrates with current codebase]
- **Event Queue Management**: [Timing and priority for System 1 events]

**System Integration Priorities:**
1. **System 1**: [Rationale for priority level] (highest priority)
2. **Future Systems**: Priority determined after System 1 completion

## Technical Architecture

### Code Organization
**Files requiring modification:**
- **`[Filename.java]`** - [Specific changes needed for System 1]

**New Components Required:**
- **[Component Name]**: [Purpose and integration points for System 1]

### Data Flow
**Information flow for System 1:**
1. **[Input/Trigger]** → **[Processing System]** → **[Output/Effect]**

### Performance Considerations
- **Memory Impact**: [Expected memory usage changes for System 1]
- **CPU Usage**: [Computational complexity additions for System 1]
- **Rendering Impact**: [Graphics/UI performance effects for System 1]
- **Save File Size**: [Changes to save data size/complexity for System 1]

## Testing & Validation

### Unit Testing
- [ ] **System 1 Core Logic**
  - [ ] [Test case 1: Expected behavior]
  - [ ] [Test case 2: Edge case]
  - [ ] [Test case 3: Error condition]

### System Integration Testing
- [ ] **System 1 Integration**
  - [ ] [Test integration with existing systems]
  - [ ] [Test performance impact]
  - [ ] [Test save/load compatibility]

### User Experience Testing
- [ ] **System 1 User Experience**
  - [ ] [Test user interaction]
  - [ ] [Test visual feedback]
  - [ ] [Test error handling from user perspective]

### Technical Validation
- [ ] **Compilation and Build**
  - [ ] `mvn compile` passes without errors
  - [ ] `mvn test` passes all existing tests
  - [ ] [Any specific tests for System 1]

## Implementation Timeline

### Phase 1: System 1 Implementation (Estimated: [X hours])
- [ ] Analyze current implementation and requirements
- [ ] Implement core functionality
- [ ] Add debugging and validation

### Phase 2: System 1 Testing and Validation (Estimated: [X hours])
- [ ] Unit testing and edge cases
- [ ] Integration testing with existing systems
- [ ] Performance and compatibility validation

### Phase 3: System 2+ Planning (Estimated: TBD)
- [ ] Assess results from System 1
- [ ] Identify next highest priority issue
- [ ] Plan System 2 implementation

## Quality Assurance

### Code Quality
- [ ] **Code Review Checklist**
  - [ ] System 1 follows existing code patterns and conventions
  - [ ] Proper error handling for edge cases
  - [ ] Clear debug output for troubleshooting
  - [ ] Minimal impact on existing functionality

### Documentation Requirements
- [ ] **Code Documentation**
  - [ ] Document System 1 logic and integration points
  - [ ] Update method comments as needed
  - [ ] Add inline comments for complex logic

## Risk Assessment

### Technical Risks
- **System 1 Complexity**: [Impact level] - [Mitigation strategy]
- **Integration Risk**: [Impact level] - [Testing strategy]
- **Performance Risk**: [Impact level] - [Monitoring approach]

### Quality Risks
- **Regression Risk**: [Impact level] - [Testing coverage to prevent regressions]
- **System Balance**: [Impact level] - [Validation that changes don't break game balance]

## Success Criteria

### Functional Requirements
- [ ] System 1 implemented and functional as specified
- [ ] No regression in existing functionality
- [ ] Integration testing passes without critical issues
- [ ] Performance impact is within acceptable limits

### Quality Requirements
- [ ] Code compiles without errors or warnings
- [ ] All existing tests continue to pass
- [ ] System 1 provides clear indication of operation (debug output, user feedback, etc.)

## Post-Implementation Review

### Implementation Summary
*[To be completed after each system implementation]*

**Actual Implementation Time**: [X hours] (System 1 completed [Date])

**Systems Completed**:
- **✅ System 1**: [Brief implementation summary]
- **⭕ System 2+**: [Status after System 1 completion]

### Key Achievements
*[To be completed after each system implementation]*

### Files Modified
*[To be completed during implementation of each system]*

### Lessons Learned
*[To be completed after each system implementation]*

### Future Enhancements
*[To be identified during implementation of each system]*

---

## Development Cycle Workflow Reference

### Git Branch Management
```bash
# Create development branch
git checkout main
git pull origin main
git checkout -b DC_NN

# Development workflow
git add [files]
git commit -m "DC-NN: [Description]"

# Completion workflow (only when entire cycle is complete)
git checkout main
git merge DC_NN
git branch -d DC_NN
```

### Commit Message Format
- **Format**: `DC-NN: [Brief description]`
- **Examples**: 
  - `DC-36: Fix hesitation recovery bug in HesitationManager`
  - `DC-36: Add weapon state recovery logic to endHesitation method`
  - `DC-36: Validate combat resumption after hesitation ends`

### Testing Commands
```bash
mvn compile                    # Verify compilation
mvn test                      # Run existing tests  
mvn test -Dtest=[TestName]     # Run specific test
```

---

## ⚠️ ITERATIVE DEVELOPMENT REMINDERS ⚠️

### For Template Users:
1. **NEVER plan System 2+ while working on System 1**
2. **NEVER implement before planning is complete**
3. **NEVER add hints about future systems to placeholder sections**
4. **ALWAYS complete current system fully before considering next**
5. **ALWAYS test thoroughly before moving to next system**

### For System Planning:
- Plan only the current system in detail
- Leave future system sections as empty placeholders
- Add systems iteratively as they are identified
- Focus on one problem at a time

### For Implementation:
- Implement only planned systems
- Complete all testing before next system
- Update documentation as you go
- Mark tasks as complete immediately after finishing

---

*This iterative development cycle focuses on implementing one system at a time while maintaining flexibility for additional improvements discovered during implementation. Each system is completed fully before considering the next, ensuring focused development and thorough validation.*
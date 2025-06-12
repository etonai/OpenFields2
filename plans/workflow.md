# Development Cycle Workflow

This document outlines the systematic development workflow for OpenFields2 using numbered development cycles.

## Overview

Development cycles provide a structured approach to planning, implementing, and documenting feature development. Each cycle is tracked through git branches, detailed planning documents, and comprehensive implementation summaries.

## Naming Conventions

### Development Cycle Files
- **Format**: `DevCycle_YYYY_NNNN.md`
- **Example**: `DevCycle_2025_0003.md`
- **References**: "devcycle 3", "devcycle 2025 3", "DevCycle 3", "dev cycle 3"

### Git Branches
- **Format**: `devcycle-YYYY-NNNN`
- **Alternative**: `dc-YYYY-NNNN` or `dc-N`
- **Example**: `devcycle-2025-0003` or `dc-2025-0003` or `dc-3`

### Commit Messages
- **Format**: `"DC-N: Brief description of changes"`
- **Example**: `"DC-3: Implement area fire UI components"`

### Pull Request Titles
- **Format**: `"DevCycle YYYY-NNNN: Brief cycle description"`
- **Example**: `"DevCycle 2025-0003: Area Fire Systems Implementation"`

### Git Tags
- **Format**: `devcycle-YYYY-NNNN-complete`
- **Example**: `devcycle-2025-0003-complete`

## Development Process

### 1. Planning Phase
1. **Create DevCycle File**: Create new `DevCycle_YYYY_NNNN.md` in `/plans/` directory
2. **Define Scope**: Populate TODO items with detailed specifications
3. **Design Review**: Refine design questions and technical approaches
4. **Dependencies**: Identify any dependencies on previous cycles or external factors

### 2. Implementation Phase
1. **Branch Creation**: 
   ```bash
   git checkout main
   git pull origin main
   git checkout -b devcycle-2025-NNNN
   ```

2. **Systematic Development**: 
   - Work through TODO items in logical order
   - Mark items as `in_progress` then `completed`
   - Commit regularly with descriptive messages
   - Update DevCycle file with progress

3. **Testing Integration**:
   - Run `mvn compile` after major changes
   - Run `mvn test` to ensure existing functionality preserved
   - Test new features thoroughly before marking complete

### 3. Documentation Phase
1. **Implementation Summary**: Add comprehensive implementation details to DevCycle file
   - Mark all completed tasks with [x]
   - Document technical details for each system
   - List all files modified with key changes
   - Include timestamps and completion status
   - Note any deviations from original plan

2. **CLAUDE.md Updates**: Update project documentation with new features/commands

3. **Cross-References**: Link related cycles and dependencies

### 4. Completion Phase
1. **Final Testing**: Comprehensive testing of all cycle features
2. **Code Review**: Review all changes for quality and consistency
3. **Merge to Main**:
   ```bash
   git checkout main
   git merge devcycle-2025-NNNN
   git push origin main
   ```

4. **Tagging**:
   ```bash
   git tag devcycle-2025-NNNN-complete
   git push origin devcycle-2025-NNNN-complete
   ```

5. **Archive**: Move completed DevCycle file to `/plans/completed/` (optional)

## File Organization

### Directory Structure
```
/plans/
├── workflow.md                    # This file
├── DevCycle_2025_0003.md         # Active cycles
├── DevCycle_2025_0004.md
└── completed/                     # Optional archive
    ├── Tasks20250611_02.md       # Historical format
    └── DevCycle_2025_0001.md     # Completed cycles
```

### Documentation Standards
- **Detailed Specifications**: Each TODO item should have clear, actionable specifications
- **Design Questions**: Document open questions and decisions made
- **Implementation Details**: Comprehensive technical summary upon completion
- **File-by-File Changes**: Document specific changes made to each file
- **Integration Notes**: How new systems interact with existing code

## Tracking and References

### Cycle Cross-References
- Reference previous cycles that established foundations
- Note dependencies between cycles
- Link related design decisions

### Milestone Tracking
- Use cycle numbers for major feature milestones
- Track cumulative progress across multiple cycles
- Maintain feature roadmap aligned with cycle planning

### Audit Trail
- Git history provides implementation timeline
- DevCycle files provide planning and design rationale
- Tags mark significant completion points
- CLAUDE.md updates track user-facing changes

## Best Practices

### Planning
- Start with clear, measurable objectives
- Break large features into manageable cycle-sized chunks
- Document design decisions and alternatives considered
- Include testing and documentation tasks in cycle scope

### Implementation
- Commit early and often with descriptive messages
- Update cycle documentation as work progresses
- Test incrementally to catch issues early
- Maintain backwards compatibility where possible

### Documentation
- Follow the comprehensive format established in Tasks20250611_02.md
- Include both technical details and user-facing impact
- Document any deviations from original plan with rationale
- Ensure future developers can understand design decisions

### Quality Assurance
- All cycles must compile successfully (`mvn compile`)
- All existing tests must pass (`mvn test`)
- New features should include appropriate testing
- Performance impact should be monitored and documented

This workflow ensures systematic, well-documented development that maintains code quality while providing clear progress tracking and historical context for future development efforts.
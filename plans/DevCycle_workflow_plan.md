# Development Cycle Workflow Plan
*Created: June 18, 2025*

This document defines the standardized workflow for implementing development cycles in the OpenFields2 project.

## **Overview**

Each development cycle follows a structured approach from initial brainstorming through final implementation and bug fixes. This workflow ensures thorough planning, clear communication, and high-quality implementation while maintaining comprehensive documentation.

## **Workflow Phases**

### **Phase 1: Branch Creation and Initial Setup**

**Branch Creation:**
- Project owner creates new branch: `DC_##` (where ## is the dev cycle number)
- Branch from main/master branch
- Ensure clean starting state

### **Phase 2: Brainstorming**

**Document Creation:**
- **File Name**: `DevCycle_2025_####_brainstorm.md` (Where ## is the dev cycle number, 0 padded to 4 digits)
- **Purpose**: Explore and document potential systems for the dev cycle

**Process:**
1. **Initial Ideas**: Project owner provides initial system ideas and requirements
2. **Future Integration**: Review existing future brainstorm documents for potential additions
3. **System Analysis**: Analyze each proposed system for:
   - Implementation complexity
   - Impact on existing systems
   - Priority level
   - Dependencies
4. **Iterative Refinement**: Collaborate to flesh out ideas and refine scope
5. **Scope Decision**: Determine which systems will be included in current cycle vs future cycles

**Future Brainstorm Documents:**
- **Format**: `DevCycle_2025_future_###_brainstorm.md`
- **Numbering**: Lower numbers indicate sooner implementation priority
- **Usage**: Repository for ideas not ready for current cycle

### **Phase 3: Planning and Design**

**Document Creation:**
- **File Name**: `DevCycle_2025_####.md`
- **Purpose**: Detailed planning and implementation document

**Process:**
1. **Plan Creation**: Convert brainstorm decisions into detailed implementation plan
2. **Task Breakdown**: Define specific tasks, dependencies, and implementation order
3. **Question Phase**: Claude adds questions to document end requiring clarification
4. **Answer Integration**: Project owner answers questions within document
5. **Plan Refinement**: Claude incorporates answers into the plan
6. **Iteration**: Repeat Q&A until plan is complete and understood
7. **Finalization**: Confirm readiness to proceed with implementation

### **Phase 4: Implementation**

**Process:**
1. **Commit Planning Documents**: Commit finalized planning documents before implementation
2. **Task Implementation**: Implement planned systems according to document specifications
3. **Progress Tracking**: Update completed tasks in planning document as work progresses
4. **No Auto-Commit**: Do NOT commit implementation changes automatically

**Review Process:**
1. **Implementation Complete**: All planned tasks implemented
2. **Diff Review**: Project owner reviews all changes via git diff
3. **Modification Requests**: Make any requested changes before commit
4. **Final Approval**: Commit only after project owner approval

### **Phase 5: Bug Fixing**

**Document Creation:**
- **File Name**: `DevCycle_2025_####_bugs_01.md`
- **Purpose**: Document and plan bug fixes before cycle closure

**Process:**
1. **Bug Documentation**: Document bugs discovered during implementation or testing
2. **Fix Planning**: Plan bug fixes with same Q&A process as main implementation
3. **Implementation**: Implement bug fixes without auto-commit
4. **Review**: Project owner reviews bug fixes before commit approval
5. **Multiple Iterations**: Create additional documents (`bugs_02.md`, `bugs_03.md`, etc.) as needed

**Bug Triage:**
- **Immediate Bugs**: Fix in current cycle bug documents
- **Future Bugs**: Move to future development cycles if not critical

### **Phase 6: Cycle Closure**

**Two Closure Approaches:**

#### **A. Claude-Managed Closeout (Standard)**
For DevCycles managed by Claude, follows automated closeout process:

**Process:**
1. **Final Implementation Validation**
   - Ensure all tasks marked as completed in todo list
   - Verify compilation successful (`mvn compile`)
   - Confirm all features working as specified
   - Test implemented functionality

2. **Update DevCycle Document Status**
   - Change implementation status to "✅ COMPLETED"
   - Update "Last Implementation Update" timestamp
   - Add final implementation summary and metrics
   - Document lessons learned and technical insights

3. **Create Comprehensive Final Summary**
   - Document all files modified and lines changed
   - Summarize key achievements and technical innovations
   - Include compilation and testing results
   - Add performance impact assessment
   - Follow pattern from previous completed cycles

4. **Move Document to Completed Folder**
   - Move `plans/DevCycle_2025_####.md` to `plans/completed/DevCycle_2025_####.md`
   - Update any cross-references to new location

5. **Update CLAUDE.md Documentation**
   - Add new features to game mechanics section
   - Document new controls and key bindings
   - Update tactical applications and usage instructions
   - Ensure user-facing documentation is current

6. **Git Operations (Automatic)**
   - Create final commit for documentation updates
   - Merge development branch to main branch
   - Tag completion: `git tag DC-##-complete`
   - Clean up development branch
   
   **Exception Cases** (ask permission first):
   - Merge conflicts detected
   - Compilation failures
   - Test failures
   - User explicitly requests manual control

7. **Cleanup and Validation**
   - Remove temporary debug output added for testing
   - Ensure code is clean and production-ready
   - Verify all success criteria met

8. **Next Cycle Preparation**
   - Development environment ready for next cycle
   - Main branch updated with completed work

#### **B. Manual Script-Based Closeout (Alternative)**
For manual management or complex scenarios, use automated scripts:

**Process:**
1. **Final Satisfaction Check**: Confirm all systems working and bugs addressed
2. **Document Archival**: Use the `archive_cycle.sh` script to move all cycle documents to `completed/` directory
   ```bash
   # Preview what will be archived (recommended first step)
   bash archive_cycle.sh <cycle_number> --dry-run
   
   # Archive the documents
   bash archive_cycle.sh <cycle_number>
   
   # Example for DevCycle 15:
   bash archive_cycle.sh 15
   ```
   - Script automatically finds all documents matching `DevCycle_2025_####*.md` pattern
   - Handles main cycle documents, subcycles (15a, 15b, etc.), and brainstorm documents
   - Creates `completed/` directory if it doesn't exist
   - Provides confirmation prompt and detailed output
   - Skips files already in `completed/` directory
3. **Final Commit**: Commit document moves and any final changes
   ```bash
   git add completed/ plans/
   git commit -m "Archive DevCycle ## documents"
   ```
4. **Branch Merge and Cleanup**: Use the `close_cycle.sh` script to merge and clean up the development branch
   ```bash
   # Preview the closure process (recommended first step)
   bash close_cycle.sh <branch_name> --dry-run
   
   # Execute the full closure process
   bash close_cycle.sh <branch_name>
   
   # Example for DevCycle 15:
   bash close_cycle.sh DC_15
   ```
   - Script automatically switches to main branch and pulls latest changes
   - Merges development branch into main with conflict detection
   - Deletes local and remote development branches
   - Pushes merged changes to origin/main
   - Verifies compilation and provides summary
   - Creates backup branches for safety
   - Includes comprehensive error handling and rollback options
5. **Next Cycle Prep**: Ready for next development cycle

## **File Naming Conventions**

### **Active Development Documents**
- **Brainstorm**: `DevCycle_2025_####_brainstorm.md`
- **Planning**: `DevCycle_2025_####.md`
- **Bug Fixes**: `DevCycle_2025_####_bugs_##.md`

### **Future Planning Documents**
- **Format**: `DevCycle_2025_future_###_brainstorm.md`
- **Numbering**: Lower numbers = higher priority for future implementation

### **Completed Documents**
- **Location**: `completed/` directory
- **Naming**: Maintain original names after moving

## **Key Workflow Principles**

### **No Auto-Commit Rule**
- **Implementation Phase**: Never commit code changes without explicit approval
- **Bug Fix Phase**: Never commit bug fixes without review
- **Review Required**: All code changes must be reviewed via diff before commit

### **Iterative Refinement**
- **Q&A Process**: Use question/answer cycles to ensure clarity
- **Document Evolution**: Allow documents to evolve through iteration
- **Scope Flexibility**: Adjust scope based on discoveries during planning

### **Comprehensive Documentation**
- **Decision Trail**: Maintain clear record of decisions and rationale
- **Progress Tracking**: Update documents to reflect implementation progress
- **Future Reference**: Create documentation that serves future development

### **Quality Gates**
- **Planning Complete**: Don't implement until plan is fully understood
- **Implementation Review**: Don't commit until implementation is reviewed
- **Bug Resolution**: Don't close cycle until critical bugs are resolved

## **Branch Management**

### **Branch Lifecycle**
```
main branch
    ↓
DC_## branch created
    ↓
Brainstorm → Plan → Implement → Debug
    ↓
Review and approval
    ↓
Commit to DC_## branch
    ↓
Merge DC_## → main
    ↓
Delete DC_## branch
```

### **Commit Strategy**
- **Planning Commits**: Commit planning documents when finalized
- **Implementation Hold**: Hold implementation commits until reviewed
- **Bug Fix Hold**: Hold bug fix commits until reviewed
- **Archive Commit**: Commit document archival at cycle end

## **Communication Patterns**

### **Question Format**
- **Location**: End of planning documents
- **Style**: Clear, specific questions requiring definitive answers
- **Purpose**: Resolve ambiguities before implementation

### **Answer Integration**
- **Location**: Within document text where relevant
- **Process**: Replace questions with incorporated answers
- **Result**: Self-contained document requiring no external context

## **Success Metrics**

### **Workflow Effectiveness**
- [ ] Clear understanding before implementation begins
- [ ] No surprises during implementation
- [ ] High-quality code that meets requirements
- [ ] Comprehensive documentation for future reference

### **Quality Indicators**
- [ ] Planning documents accurately predict implementation
- [ ] Few unexpected bugs during implementation
- [ ] Smooth review and approval process
- [ ] Clean, maintainable code changes

## **Workflow Benefits**

### **Quality Assurance**
- **Thorough Planning**: Reduces implementation surprises
- **Review Gates**: Prevents low-quality code from being committed
- **Iterative Refinement**: Improves understanding before implementation

### **Documentation**
- **Decision Record**: Clear trail of why decisions were made
- **Future Reference**: Easy to understand past development choices
- **Knowledge Transfer**: New team members can understand project evolution

### **Risk Management**
- **Scope Control**: Prevents feature creep during implementation
- **Quality Control**: Review process catches issues before they're locked in
- **Bug Management**: Systematic approach to bug resolution

## **Workflow Automation Tools**

### **Document Archival Script**

The project includes `archive_cycle.sh` to automate the document archival process in Phase 6.

**Script Features:**
- **Smart Pattern Matching**: Automatically finds all documents for a given cycle (main, subcycles, brainstorms)
- **Flexible Numbering**: Handles cycle numbers 1-9999 with proper 4-digit formatting
- **Safety Features**: Dry-run mode, user confirmation, prevents overwrites
- **Error Handling**: Validates inputs, provides helpful feedback
- **Next Steps Guidance**: Shows git commands needed after archival

**Usage Examples:**
```bash
# Preview what would be archived
bash archive_cycle.sh 15 --dry-run

# Archive DevCycle 15 documents
bash archive_cycle.sh 15

# View help and options
bash archive_cycle.sh --help
```

**File Pattern Recognition:**
- `DevCycle_2025_####.md` - Main planning documents
- `DevCycle_2025_####[a-z].md` - Subcycle documents (15a, 15b, etc.)
- `DevCycle_2025_####*_brainstorm.md` - Brainstorm documents
- `DevCycle_2025_####*_bugs_*.md` - Bug fix documents

The script ensures consistent and reliable archival while reducing manual effort and potential errors.

### **Cycle Closure Script**

The project includes `close_cycle.sh` to automate the branch merge and cleanup process in Phase 6.

**Script Features:**
- **Automated Branch Merge**: Switches to main, pulls latest, merges development branch
- **Branch Cleanup**: Deletes local and remote development branches safely
- **Safety Features**: Clean working directory checks, backup branch creation, user confirmation
- **Error Handling**: Conflict detection, rollback options, clear error messages
- **Verification**: Compilation testing after merge completion
- **Comprehensive Reporting**: Step-by-step progress and final summary

**Usage Examples:**
```bash
# Preview what would be done
bash close_cycle.sh DC_15 --dry-run

# Execute full cycle closure
bash close_cycle.sh DC_15

# View help and options
bash close_cycle.sh --help
```

**Safety Measures:**
- Creates backup branches before destructive operations
- Requires clean working directory before proceeding
- User confirmation for branch deletion
- Comprehensive error handling with rollback guidance
- Compilation verification to ensure merge integrity

The script automates the error-prone manual merge process while maintaining safety and providing clear feedback throughout the closure workflow.

## **CLAUDE.md Maintenance Procedures**

### **Cycle Completion Updates**
When closing out any development cycle, CLAUDE.md must be updated to maintain accurate development tracking information.

**Required Updates During Cycle Closure:**
1. **Update Last Completed Cycle**: Modify "Current Development Status" section
   - Change "Last Completed Cycle" to reflect the cycle being closed
   - Add completion date and brief achievement summary
   - Update cycle completion dates list with new entry

2. **Update Current Active Cycle**: Modify for next development cycle
   - Update "Current Active Cycle" information if moving to new cycle
   - Mark cycle as complete if no immediate follow-on cycle planned

3. **Document Archival**: Ensure document location references remain accurate
   - Verify completed cycle documents are properly moved to `plans/completed/`
   - Update any specific document references if naming conventions change

**Example CLAUDE.md Update Process:**
```markdown
# Before cycle closure:
- **Last Completed Cycle**: DevCycle 36 - Headless Testing Architecture (Completed: July 2, 2025)
- **Current Active Cycle**: DevCycle 37 - CLAUDE.md Enhancement (Started: July 2, 2025)

# After DevCycle 37 closure:
- **Last Completed Cycle**: DevCycle 37 - CLAUDE.md Enhancement (Completed: July 2, 2025)
- **Current Active Cycle**: [Next cycle or "No active cycle"]
```

### **System Completion Procedures Within Cycles**

For iterative cycles with multiple systems, specific procedures apply when completing individual systems before the entire cycle closes.

**System Completion Steps:**
1. **Mark System Complete**: Update cycle document to show system as ✅ **COMPLETE**
2. **Document Implementation Summary**: Add brief summary of what was completed
3. **Commit System Completion**: Create commit specifically for system completion
   - Use format: `DC-NN: Complete System N - [Brief description]`
   - Example: `DC-37: Complete System 1 - CLAUDE.md cycle tracking and workflow enhancements`

4. **Update System Status**: If multiple systems in cycle, update cycle document status
   - Mark completed systems with ✅ **COMPLETE**
   - Keep cycle open for additional systems unless explicitly ordered to close

**System Completion Commit Process:**
```bash
# Update cycle document with system completion
git add plans/DevCycle_2025_NNNN.md

# Commit implementation files and documentation
git add [implementation files]
git commit -m "DC-NN: Complete System N - [Description]

- [Brief list of what was completed]
- [Any key implementation details]
- [Integration or testing notes]

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>"
```

### **Document Archiving Integration**

**Plan Document Movement Process:**
When closing a complete development cycle, all related documents must be moved to the completed directory.

**Documents to Archive:**
- Main cycle document: `DevCycle_YYYY_NNNN.md`
- Brainstorm documents: `DevCycle_YYYY_NNNN_brainstorm.md`
- Bug fix documents: `DevCycle_YYYY_NNNN_bugs_NN.md`
- Any analysis documents created during the cycle

**Archival Steps:**
1. **Move Documents**: Transfer all cycle-related documents from `plans/` to `plans/completed/`
2. **Update CLAUDE.md**: Ensure document location references reflect the archival
3. **Verify Links**: Check that any cross-references to archived documents remain valid
4. **Commit Archival**: Create specific commit for document archival

**Archival Commit Format:**
```bash
git add plans/completed/
git rm plans/DevCycle_YYYY_NNNN*
git commit -m "DC-NN: Archive completed cycle documents

- Move DevCycle_YYYY_NNNN.md to completed directory
- Archive all related brainstorm and bug documents
- Update CLAUDE.md with completion status

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>"
```

### **Workflow Integration Summary**

**Enhanced Cycle Closure Checklist:**
1. ✅ **Complete all planned systems**
2. ✅ **Update cycle document with final status**
3. ✅ **Update CLAUDE.md with cycle completion**
4. ✅ **Archive plan documents to completed directory**
5. ✅ **Commit CLAUDE.md and archival changes**
6. ✅ **Switch to main branch**
7. ✅ **Merge development branch**
8. ✅ **Delete development branch**
9. ✅ **Verify clean repository status**

**System Completion Integration:**
- **Individual Systems**: Complete systems with documentation updates and targeted commits
- **Cycle Management**: Keep cycles open for additional systems until explicitly ordered to close
- **Status Tracking**: Maintain clear visibility into which systems are complete vs. in progress

This enhanced workflow ensures CLAUDE.md remains current and development cycle documentation follows consistent lifecycle management procedures.

## **Adaptation Notes**

This workflow can be adapted as needed based on:
- **Cycle Complexity**: Larger cycles may need additional planning phases
- **System Dependencies**: Complex integrations may require additional review points
- **Team Size**: Workflow scales for different team configurations
- **Project Maturity**: Process can evolve as project and team mature

---

**Key Success Factor**: The workflow's strength comes from its emphasis on clarity and review at each stage, ensuring high-quality outcomes through systematic planning and implementation.
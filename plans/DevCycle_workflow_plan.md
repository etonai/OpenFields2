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
- **File Name**: `DevCycle_2025_00##_brainstorm.md`
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
- **File Name**: `DevCycle_2025_00##.md`
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
- **File Name**: `DevCycle_2025_00##_bugs_01.md`
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

**Process:**
1. **Final Satisfaction Check**: Confirm all systems working and bugs addressed
2. **Document Archival**: Move all cycle documents to `completed/` directory
3. **Final Commit**: Commit document moves and any final changes
4. **Branch Merge**: Merge development branch to main branch
5. **Branch Cleanup**: Delete development branch
6. **Next Cycle Prep**: Ready for next development cycle

## **File Naming Conventions**

### **Active Development Documents**
- **Brainstorm**: `DevCycle_2025_00##_brainstorm.md`
- **Planning**: `DevCycle_2025_00##.md`
- **Bug Fixes**: `DevCycle_2025_00##_bugs_##.md`

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

## **Adaptation Notes**

This workflow can be adapted as needed based on:
- **Cycle Complexity**: Larger cycles may need additional planning phases
- **System Dependencies**: Complex integrations may require additional review points
- **Team Size**: Workflow scales for different team configurations
- **Project Maturity**: Process can evolve as project and team mature

---

**Key Success Factor**: The workflow's strength comes from its emphasis on clarity and review at each stage, ensuring high-quality outcomes through systematic planning and implementation.
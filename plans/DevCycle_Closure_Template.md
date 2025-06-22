# Development Cycle Closure Template

*This template provides a standardized checklist for properly closing out development cycles in the OpenFields2 project.*

## Pre-Closure Verification

### Implementation Status Check
- [ ] **All planned features implemented** - Verify all system implementations are complete
- [ ] **Documentation updated** - DevCycle plan document reflects actual implementation
- [ ] **Success criteria met** - All functional, quality, and user experience requirements satisfied
- [ ] **Testing completed** - Unit tests, integration tests, and manual validation performed
- [ ] **No critical bugs** - Implementation is stable and ready for integration

### Branch Status Check
- [ ] **Working directory clean** - All changes committed or properly staged
- [ ] **Implementation commits made** - Core feature changes committed with proper DC-X messages
- [ ] **Documentation commits made** - Plan updates and completion notes committed
- [ ] **Branch is current** - Development branch has all necessary changes

## Streamlined Closure Workflow

### 1. Update Cycle Documentation
- [ ] **Update main cycle document header** - Add CLOSED timestamp
- [ ] **Mark implementation items complete** - Change all `[ ]` to `[x]` for completed items
- [ ] **Update success criteria** - Mark all requirements as `[x]` completed
- [ ] **Add post-implementation review** - Include summary of actual implementation and any lessons learned
- [ ] **Update CLAUDE.md if needed** - Add any new features, commands, or mechanics

### 2. Move Files to Completed Directory
```bash
# Move all DevCycle files to completed folder (replace 13 with actual cycle number)
cd plans
./move_cycle_to_completed.sh.old 13
```

### 3. Commit Closure Changes
```bash
# Add and commit all documentation updates
git add -A
git commit -m "DC-13: Close DevCycle 13 - [brief summary of cycle]"
```

### 4. Merge Branch and Push
```bash
# Merge development branch into main and push (replace DC_13 with actual branch name)
./merge_and_push.sh.old DC_13
```

### 5. Final Verification
```bash
# Verify compilation
mvn compile

# Verify application starts
mvn javafx:run
```

## Post-Closure Activities

### Verification Checklist
- [ ] **Main branch updated** - All changes merged successfully
- [ ] **Tag created** - Completion tag exists and is pushed to remote
- [ ] **Plan archived** - DevCycle plan moved to completed directory
- [ ] **Application functional** - Code compiles and runs without crashes
- [ ] **Documentation current** - All relevant docs reflect the changes

### Communication and Planning
- [ ] **Stakeholder notification** - Inform relevant parties of cycle completion
- [ ] **Next cycle planning** - Review FutureTasks.md for next development priorities
- [ ] **Lessons learned capture** - Document any process improvements in the plan

## Troubleshooting Common Issues

### Merge Conflicts
1. **Identify conflicting files**: `git status` will show conflicted files
2. **Resolve conflicts manually**: Edit files to resolve `<<<<<<<`, `=======`, `>>>>>>>` markers
3. **Mark as resolved**: `git add [resolved-file]`
4. **Complete merge**: `git commit`

### Failed Tests
1. **Determine if failures are new**: Compare with previous test runs
2. **If pre-existing failures**: Document and proceed (note in plan)
3. **If new failures**: Fix issues before completing closure
4. **Update tests if needed**: Modify tests that need updating for new features

### Build Failures
1. **Check compilation errors**: Review `mvn compile` output
2. **Fix syntax/import issues**: Resolve any code problems
3. **Verify dependencies**: Ensure all required dependencies are available
4. **Test incrementally**: Verify each major change compiles

## Template Usage Notes

### Customization
- Replace `YYYY_NNNN` with actual cycle numbers (e.g., `2025_0006`)
- Replace `DC-X` with actual cycle number (e.g., `DC-6`)
- Modify git commands based on actual branch names used

### Timing
- **Estimated closure time**: 10-20 minutes for simple cycles
- **Complex cycles**: May require 30-60 minutes including testing
- **Plan for merge conflicts**: Allow extra time if multiple developers involved

### Documentation
- This template should be updated as closure processes evolve
- Add cycle-specific notes to individual DevCycle plans
- Maintain this template in the main plans directory for easy access

---

## Quick Reference Commands

```bash
# Streamlined DevCycle Closure Workflow
cd plans                                      # Navigate to plans directory
./move_cycle_to_completed.sh.old 13              # Move all cycle files to completed
git add -A                                    # Stage all changes
git commit -m "DC-13: Close DevCycle 13 - [summary]"  # Commit closure
./merge_and_push.sh.old DC_13                     # Merge branch and push to main
mvn compile                                   # Verify build
mvn javafx:run                                # Verify application starts
```

---

*This template ensures consistent and thorough closure of development cycles while maintaining proper git hygiene and documentation standards.*
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

## Git Workflow Closure

### 1. Clean Up Working Directory
```bash
# Check for uncommitted changes
git status

# Add and commit any remaining changes
git add [remaining-files]
git commit -m "DC-X: Final cleanup and documentation updates"
```

### 2. Switch to Main Branch
```bash
# Switch to main branch
git checkout main

# Pull latest changes from remote
git pull origin main
```

### 3. Merge Development Branch
```bash
# Merge development branch (replace X with cycle number)
git merge devcycle-YYYY-NNNN

# If merge conflicts occur, resolve them and commit
# git add [resolved-files]
# git commit -m "DC-X: Resolve merge conflicts"
```

### 4. Create Completion Tag
```bash
# Create annotated tag for cycle completion
git tag devcycle-YYYY-NNNN-complete -m "DevCycle YYYY_NNNN: [Brief description] - Complete"

# Push main branch and tags to remote
git push origin main --tags
```

### 5. Final Verification
```bash
# Verify compilation
mvn compile

# Run tests (optional - may have pre-existing failures)
mvn test

# Verify application starts
mvn javafx:run
```

## Documentation Management

### Move Completed Plan
- [ ] **Move plan to completed directory**
  ```bash
  mv plans/DevCycle_YYYY_NNNN.md plans/completed/
  ```
- [ ] **Update plan status** - Change status to "Complete - Archived"
- [ ] **Add completion timestamp** - Record actual completion date/time
- [ ] **Commit documentation changes**
  ```bash
  git add plans/completed/DevCycle_YYYY_NNNN.md
  git commit -m "Archive completed DevCycle YYYY_NNNN plan"
  git push origin main
  ```

### Update Project Documentation
- [ ] **Update CLAUDE.md** - Add any new features, commands, or mechanics if applicable
- [ ] **Update README** - Modify if significant user-facing changes were made
- [ ] **Update FutureTasks.md** - Remove any completed items that were moved from future tasks

## Branch Cleanup (Optional)

### Local Branch Cleanup
```bash
# Delete local development branch (only after successful merge)
git branch -d devcycle-YYYY-NNNN
```

### Remote Branch Cleanup
```bash
# Delete remote development branch if it was pushed
git push origin --delete devcycle-YYYY-NNNN
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
# Standard DevCycle Closure Workflow
git status                                    # Check working directory
git add [files] && git commit -m "DC-X: ..." # Commit remaining changes
git checkout main                             # Switch to main
git pull origin main                          # Get latest main
git merge devcycle-YYYY-NNNN                  # Merge development branch
git tag devcycle-YYYY-NNNN-complete          # Tag completion
git push origin main --tags                   # Push to remote
mvn compile                                   # Verify build
mv plans/DevCycle_YYYY_NNNN.md plans/completed/ # Archive plan
git add plans/completed/ && git commit -m "Archive DevCycle YYYY_NNNN"
git push origin main                          # Push archive commit
```

---

*This template ensures consistent and thorough closure of development cycles while maintaining proper git hygiene and documentation standards.*
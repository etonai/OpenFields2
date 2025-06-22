#!/bin/bash

# DevCycle Closure Script
# Handles branch merge, cleanup, and final verification for completed development cycles
# Usage: ./close_cycle.sh <branch_name> [--dry-run]
# Example: ./close_cycle.sh DC_15

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_step() {
    echo -e "${PURPLE}[STEP]${NC} $1"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 <branch_name> [--dry-run]"
    echo ""
    echo "Complete development cycle closure by merging branch and cleanup"
    echo ""
    echo "Arguments:"
    echo "  branch_name     The development branch to merge and close (e.g., DC_15)"
    echo "  --dry-run       Preview what would be done without making changes"
    echo ""
    echo "Examples:"
    echo "  $0 DC_15           # Close DevCycle 15"
    echo "  $0 DC_15 --dry-run # Preview DC_15 closure"
    echo ""
    echo "This script will:"
    echo "  1. Verify clean working state"
    echo "  2. Switch to main branch and pull latest"
    echo "  3. Merge development branch into main"
    echo "  4. Delete development branch"
    echo "  5. Push merged changes to origin"
    echo "  6. Verify compilation and provide summary"
}

# Function to check if we're in a git repository
check_git_repo() {
    if git rev-parse --git-dir > /dev/null 2>&1; then
        # We are in a git repository
        true
    else
        print_error "Not in a git repository"
        exit 1
    fi
}

# Function to check if branch exists
check_branch_exists() {
    local branch="$1"
    if git show-ref --verify --quiet refs/heads/"$branch"; then
        # Branch exists
        true
    else
        print_error "Branch '$branch' does not exist"
        print_info "Available branches:"
        git branch -a
        exit 1
    fi
}

# Function to check working directory is clean
check_clean_working_dir() {
    if git diff-index --quiet HEAD --; then
        # Working directory is clean
        true
    else
        print_error "Working directory is not clean"
        print_info "Please commit or stash your changes before proceeding"
        git status --porcelain
        exit 1
    fi
}

# Function to backup current state
create_backup_branch() {
    local branch="$1"
    local backup_branch="${branch}_backup_$(date +%Y%m%d_%H%M%S)"
    
    if [[ "$DRY_RUN" == true ]]; then
        print_info "Would create backup branch: $backup_branch"
    else
        print_info "Creating backup branch: $backup_branch"
        git branch "$backup_branch" "$branch"
        print_success "Backup created: $backup_branch"
    fi
    
    echo "$backup_branch"
}

# Function to switch to main branch and pull latest
prepare_main_branch() {
    print_step "Preparing main branch"
    
    if [[ "$DRY_RUN" == true ]]; then
        print_info "Would switch to main branch"
        print_info "Would pull latest changes from origin/main"
        return 0
    fi
    
    # Switch to main branch
    print_info "Switching to main branch"
    if git checkout main; then
        print_success "Switched to main branch"
    else
        print_error "Failed to switch to main branch"
        exit 1
    fi
    
    # Pull latest changes
    print_info "Pulling latest changes from origin/main"
    if git pull origin main; then
        print_success "Pulled latest changes"
    else
        print_warning "Failed to pull from origin/main (remote may not exist or be accessible)"
        print_info "Continuing with local main branch..."
    fi
    
    print_success "Main branch prepared"
}

# Function to merge development branch
merge_development_branch() {
    local branch="$1"
    print_step "Merging development branch: $branch"
    
    if [[ "$DRY_RUN" == true ]]; then
        print_info "Would merge branch '$branch' into main"
        return 0
    fi
    
    # Check if merge will be fast-forward
    local merge_base=$(git merge-base main "$branch")
    local main_commit=$(git rev-parse main)
    
    if [[ "$merge_base" == "$main_commit" ]]; then
        print_info "Fast-forward merge available"
    else
        print_info "Three-way merge required"
    fi
    
    # Perform the merge
    print_info "Merging '$branch' into main"
    if git merge "$branch" --no-edit; then
        print_success "Successfully merged '$branch' into main"
    else
        print_error "Merge failed - you may need to resolve conflicts"
        print_info "To abort the merge: git merge --abort"
        print_info "To continue after resolving conflicts: git commit"
        exit 1
    fi
}

# Function to delete development branch
cleanup_development_branch() {
    local branch="$1"
    print_step "Cleaning up development branch: $branch"
    
    if [[ "$DRY_RUN" == true ]]; then
        print_info "Would delete local branch '$branch'"
        print_info "Would delete remote branch 'origin/$branch' (if it exists)"
        return 0
    fi
    
    # Delete local branch
    print_info "Deleting local branch '$branch'"
    if git branch -d "$branch"; then
        print_success "Deleted local branch '$branch'"
    else
        print_warning "Failed to delete branch '$branch' - it may have unmerged changes"
        print_info "Use 'git branch -D $branch' to force delete if you're sure"
        read -p "Force delete the branch? (y/N): " -n 1 -r
        echo ""
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            git branch -D "$branch"
            print_success "Force deleted local branch '$branch'"
        else
            print_warning "Local branch '$branch' was not deleted"
        fi
    fi
    
    # Check if remote branch exists and delete it
    if git show-ref --verify --quiet refs/remotes/origin/"$branch"; then
        print_info "Deleting remote branch 'origin/$branch'"
        if git push origin --delete "$branch"; then
            print_success "Deleted remote branch 'origin/$branch'"
        else
            print_warning "Failed to delete remote branch (remote may not be accessible)"
        fi
    else
        print_info "No remote branch 'origin/$branch' to delete"
    fi
}

# Function to push changes to remote
push_to_remote() {
    print_step "Pushing merged changes to remote"
    
    if [[ "$DRY_RUN" == true ]]; then
        print_info "Would push main branch to origin/main"
        return 0
    fi
    
    print_info "Pushing main branch to origin"
    if git push origin main; then
        print_success "Successfully pushed changes to origin/main"
    else
        print_warning "Failed to push to origin/main (remote may not be accessible)"
        print_info "You can push manually later with: git push origin main"
    fi
}

# Function to verify compilation
verify_compilation() {
    print_step "Verifying compilation"
    
    if [[ "$DRY_RUN" == true ]]; then
        print_info "Would run: mvn compile"
        return 0
    fi
    
    if command -v mvn > /dev/null 2>&1; then
        print_info "Running Maven compilation test"
        if mvn compile -q; then
            print_success "Compilation successful"
        else
            print_error "Compilation failed"
            print_warning "The merge was successful, but compilation is failing"
            print_info "You may need to investigate and fix compilation issues"
            return 1
        fi
    else
        print_warning "Maven not found - skipping compilation verification"
    fi
}

# Function to provide final summary
show_summary() {
    local branch="$1"
    local backup_branch="$2"
    
    echo ""
    print_success "=== DevCycle Closure Complete ==="
    echo ""
    
    if [[ "$DRY_RUN" == true ]]; then
        print_info "DRY RUN SUMMARY:"
        print_info "  • Would have merged '$branch' into main"
        print_info "  • Would have deleted local and remote '$branch'"
        print_info "  • Would have pushed changes to origin/main"
        print_info "  • Would have verified compilation"
        print_info ""
        print_info "Run without --dry-run to perform actual closure"
        return 0
    fi
    
    print_info "CLOSURE SUMMARY:"
    print_info "  ✓ Merged '$branch' into main"
    print_info "  ✓ Deleted development branch '$branch'"
    print_info "  ✓ Pushed changes to origin/main"
    print_info "  ✓ Verified compilation"
    if [[ -n "$backup_branch" ]]; then
        print_info "  ✓ Created backup: $backup_branch"
    fi
    
    echo ""
    print_info "NEXT STEPS:"
    print_info "  • DevCycle is now closed and merged"
    print_info "  • Ready to begin next development cycle"
    print_info "  • All cycle documents are in completed/ directory"
    
    echo ""
    print_info "GIT STATUS:"
    git log --oneline -3
    
    echo ""
    print_success "DevCycle closure successful! 🎉"
}

# Main script execution
main() {
    # Check if help is requested
    if [[ "$1" == "-h" || "$1" == "--help" ]]; then
        show_usage
        exit 0
    fi
    
    # Validate arguments
    if [[ $# -lt 1 || $# -gt 2 ]]; then
        print_error "Invalid number of arguments"
        show_usage
        exit 1
    fi
    
    # Parse arguments
    BRANCH_NAME="$1"
    DRY_RUN=false
    
    if [[ $# -eq 2 ]]; then
        if [[ "$2" == "--dry-run" ]]; then
            DRY_RUN=true
        else
            print_error "Invalid argument: $2"
            show_usage
            exit 1
        fi
    fi
    
    # Validate branch name format (optional - could be more flexible)
    if [[ ! "$BRANCH_NAME" =~ ^DC_[0-9]+$ ]]; then
        print_warning "Branch name '$BRANCH_NAME' doesn't follow DC_## pattern"
        read -p "Continue anyway? (y/N): " -n 1 -r
        echo ""
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 0
        fi
    fi
    
    # Pre-flight checks
    print_info "Starting DevCycle closure for branch: $BRANCH_NAME"
    if [[ "$DRY_RUN" == true ]]; then
        print_warning "DRY RUN MODE - No changes will be made"
    fi
    
    check_git_repo
    check_branch_exists "$BRANCH_NAME"
    check_clean_working_dir
    
    # Create backup
    backup_branch=$(create_backup_branch "$BRANCH_NAME")
    
    # Confirmation
    if [[ "$DRY_RUN" == false ]]; then
        echo ""
        print_warning "This will merge '$BRANCH_NAME' into main and delete the branch"
        read -p "Continue with cycle closure? (y/N): " -n 1 -r
        echo ""
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_info "Cycle closure cancelled by user"
            exit 0
        fi
    fi
    
    # Execute closure steps
    echo ""
    prepare_main_branch
    merge_development_branch "$BRANCH_NAME"
    cleanup_development_branch "$BRANCH_NAME"
    push_to_remote
    verify_compilation
    show_summary "$BRANCH_NAME" "$backup_branch"
}

# Run main function with all arguments
main "$@"
#!/bin/bash
# merge_and_push.sh
# 
# Merges a branch into main, deletes the branch, and pushes changes to main
# Usage: ./merge_and_push.sh <branch_name>
#
# Example: ./merge_and_push.sh DC_14
#
# This script will:
# 1. Switch to main branch
# 2. Pull latest changes from origin/main
# 3. Merge the specified branch into main
# 4. Delete the local branch
# 5. Push the merged changes to origin/main

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_step() {
    echo -e "${BLUE}==> $1${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Check if branch name is provided
if [ $# -ne 1 ]; then
    print_error "Usage: $0 <branch_name>"
    print_error "Example: $0 DC_14"
    exit 1
fi

BRANCH_NAME="$1"

# Validate branch name is not main
if [ "$BRANCH_NAME" = "main" ]; then
    print_error "Cannot merge main branch into itself"
    exit 1
fi

# Check if we're in a git repository
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    print_error "Not in a git repository"
    exit 1
fi

echo "======================================"
echo "Git Branch Merge and Push Script"
echo "Branch: $BRANCH_NAME → main"
echo "======================================"

# Step 1: Check if branch exists
print_step "Checking if branch '$BRANCH_NAME' exists..."
if ! git show-ref --verify --quiet "refs/heads/$BRANCH_NAME"; then
    print_error "Branch '$BRANCH_NAME' does not exist"
    exit 1
fi
print_success "Branch '$BRANCH_NAME' found"

# Step 2: Check working directory is clean
print_step "Checking working directory status..."
if ! git diff-index --quiet HEAD --; then
    print_error "Working directory has uncommitted changes"
    print_error "Please commit or stash your changes before merging"
    exit 1
fi
print_success "Working directory is clean"

# Step 3: Switch to main branch
print_step "Switching to main branch..."
git checkout main
print_success "Switched to main branch"

# Step 4: Pull latest changes from origin/main
print_step "Pulling latest changes from origin/main..."
if git pull origin main; then
    print_success "Successfully pulled latest changes"
else
    print_warning "Pull failed or no remote configured - continuing with local merge"
fi

# Step 5: Merge the branch into main
print_step "Merging '$BRANCH_NAME' into main..."
if git merge "$BRANCH_NAME"; then
    print_success "Successfully merged '$BRANCH_NAME' into main"
else
    print_error "Merge failed - please resolve conflicts manually"
    exit 1
fi

# Step 6: Delete the local branch
print_step "Deleting local branch '$BRANCH_NAME'..."
if git branch -d "$BRANCH_NAME" 2>/dev/null; then
    print_success "Successfully deleted branch '$BRANCH_NAME'"
elif git branch -D "$BRANCH_NAME" 2>/dev/null; then
    print_warning "Force deleted branch '$BRANCH_NAME' (was not fully merged to remote)"
else
    print_error "Failed to delete branch '$BRANCH_NAME'"
    exit 1
fi

# Step 7: Push changes to origin/main
print_step "Pushing merged changes to origin/main..."
if git push origin main; then
    print_success "Successfully pushed changes to origin/main"
else
    print_error "Failed to push to origin/main"
    print_error "You may need to push manually: git push origin main"
    exit 1
fi

echo ""
echo "======================================"
print_success "Merge and push completed successfully!"
echo "Summary:"
echo "  • Merged '$BRANCH_NAME' → main"
echo "  • Deleted local branch '$BRANCH_NAME'"
echo "  • Pushed changes to origin/main"
echo "======================================"
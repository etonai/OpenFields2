#!/bin/bash
# move_cycle_to_completed.sh
#
# Moves all files matching the DevCycle_2025_NNNN* pattern to the completed folder.
# Usage: ./move_cycle_to_completed.sh <cycle_number>
#
# Example: ./move_cycle_to_completed.sh 13
# This will move:
# - DevCycle_2025_0013.md
# - DevCycle_2025_0013_brainstorm.md  
# - DevCycle_2025_0013_bugs_01.md
# - etc.
#
# All files matching DevCycle_2025_0013* will be moved to plans/completed/

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

# Check if cycle number is provided
if [ $# -ne 1 ]; then
    print_error "Usage: $0 <cycle_number>"
    print_error "Example: $0 13"
    exit 1
fi

CYCLE_NUMBER="$1"

# Validate cycle number is a positive integer
if ! [[ "$CYCLE_NUMBER" =~ ^[0-9]+$ ]]; then
    print_error "Cycle number must be a positive integer"
    exit 1
fi

# Validate cycle number is within range (1-9999)
if [ "$CYCLE_NUMBER" -lt 1 ] || [ "$CYCLE_NUMBER" -gt 9999 ]; then
    print_error "Cycle number must be between 1 and 9999"
    exit 1
fi

# Get the directory where this script is located (plans folder)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPLETED_DIR="$SCRIPT_DIR/completed"

# Format cycle number with leading zeros to 4 digits
CYCLE_PADDED=$(printf "%04d" "$CYCLE_NUMBER")

# Pattern to match all files for this cycle
PATTERN="DevCycle_2025_${CYCLE_PADDED}*"

echo "======================================"
echo "DevCycle File Mover Script"
echo "Moving DevCycle 2025-${CYCLE_PADDED} files to completed folder"
echo "======================================"

# Ensure completed directory exists
print_step "Ensuring completed directory exists..."
mkdir -p "$COMPLETED_DIR"
print_success "Completed directory ready: $COMPLETED_DIR"

# Find all matching files in the plans directory
print_step "Looking for files matching pattern: $PATTERN"
cd "$SCRIPT_DIR"

# Use find to get matching files (more reliable than glob in some shells)
MATCHING_FILES=()
while IFS= read -r -d '' file; do
    MATCHING_FILES+=("$file")
done < <(find . -maxdepth 1 -name "$PATTERN" -type f -print0 2>/dev/null)

if [ ${#MATCHING_FILES[@]} -eq 0 ]; then
    print_warning "No files found matching pattern: $PATTERN"
    echo "Files in current directory:"
    ls -la DevCycle_2025_* 2>/dev/null || echo "  No DevCycle files found"
    exit 0
fi

print_success "Found ${#MATCHING_FILES[@]} files to move:"

# Move each file
MOVED_COUNT=0
for file_path in "${MATCHING_FILES[@]}"; do
    # Remove the ./ prefix from find output
    filename="${file_path#./}"
    destination="$COMPLETED_DIR/$filename"
    
    # Check if destination already exists
    if [ -f "$destination" ]; then
        print_warning "  $filename already exists in completed folder, skipping"
        continue
    fi
    
    # Move the file
    if mv "$filename" "$destination" 2>/dev/null; then
        print_success "  Moved: $filename"
        ((MOVED_COUNT++))
    else
        print_error "  Error moving $filename"
    fi
done

echo ""
echo "======================================"
if [ $MOVED_COUNT -gt 0 ]; then
    print_success "Completed: $MOVED_COUNT files moved to completed folder"
else
    print_warning "No files were moved (all may have already existed in completed folder)"
fi
echo "======================================"
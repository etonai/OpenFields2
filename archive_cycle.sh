#!/bin/bash

# DevCycle Document Archival Script
# Archives all documents for a completed development cycle to the completed/ directory
# Usage: ./archive_cycle.sh <cycle_number> [--dry-run]
# Example: ./archive_cycle.sh 15

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

# Function to show usage
show_usage() {
    echo "Usage: $0 <cycle_number> [--dry-run]"
    echo ""
    echo "Archive all documents for a completed development cycle"
    echo ""
    echo "Arguments:"
    echo "  cycle_number    The cycle number to archive (e.g., 15, 100, 1000)"
    echo "  --dry-run       Preview what would be moved without actually moving files"
    echo ""
    echo "Examples:"
    echo "  $0 15           # Archive all DevCycle 15 documents"
    echo "  $0 100          # Archive all DevCycle 100 documents"
    echo "  $0 15 --dry-run # Preview what would be archived for cycle 15"
}

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
CYCLE_NUMBER="$1"
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

# Validate cycle number is numeric
if ! [[ "$CYCLE_NUMBER" =~ ^[0-9]+$ ]]; then
    print_error "Cycle number must be a positive integer"
    exit 1
fi

# Validate cycle number range (1-9999 to fit 4-digit format)
if [[ $CYCLE_NUMBER -lt 1 || $CYCLE_NUMBER -gt 9999 ]]; then
    print_error "Cycle number must be between 1 and 9999"
    exit 1
fi

# Format cycle number with leading zeros (4 digits)
FORMATTED_CYCLE=$(printf "%04d" "$CYCLE_NUMBER")
print_info "Archiving documents for DevCycle $CYCLE_NUMBER (formatted as $FORMATTED_CYCLE)"

# Define source and destination directories
PLANS_DIR="plans"
COMPLETED_DIR="completed"

# Check if plans directory exists
if [[ ! -d "$PLANS_DIR" ]]; then
    print_error "Plans directory '$PLANS_DIR' not found"
    exit 1
fi

# Create completed directory if it doesn't exist
if [[ ! -d "$COMPLETED_DIR" ]]; then
    if [[ "$DRY_RUN" == true ]]; then
        print_info "Would create directory: $COMPLETED_DIR"
    else
        print_info "Creating completed directory: $COMPLETED_DIR"
        mkdir -p "$COMPLETED_DIR"
    fi
fi

# Find all matching documents
PATTERN="DevCycle_2025_${FORMATTED_CYCLE}*.md"
print_info "Searching for files matching pattern: $PATTERN"

# Find files in plans directory
FILES_FOUND=()
while IFS= read -r -d '' file; do
    FILES_FOUND+=("$file")
done < <(find "$PLANS_DIR" -name "$PATTERN" -type f -print0 2>/dev/null)

# Check if any files were found
if [[ ${#FILES_FOUND[@]} -eq 0 ]]; then
    print_warning "No files found matching pattern: $PATTERN"
    print_info "Looking for files in $PLANS_DIR directory..."
    
    # Show what files are actually in the plans directory for debugging
    if [[ -d "$PLANS_DIR" ]]; then
        print_info "Files in $PLANS_DIR directory:"
        ls -la "$PLANS_DIR" | grep "DevCycle.*\.md" || print_warning "No DevCycle .md files found in $PLANS_DIR"
    fi
    exit 0
fi

print_success "Found ${#FILES_FOUND[@]} file(s) to archive"

# List files to be moved
echo ""
print_info "Files to be archived:"
for file in "${FILES_FOUND[@]}"; do
    filename=$(basename "$file")
    dest_file="$COMPLETED_DIR/$filename"
    
    if [[ -f "$dest_file" ]]; then
        print_warning "  $filename (already exists in completed/, will skip)"
    else
        echo "  $filename"
    fi
done

# If dry run, just show what would happen
if [[ "$DRY_RUN" == true ]]; then
    echo ""
    print_info "DRY RUN: No files were actually moved"
    print_info "Run without --dry-run to perform the actual archival"
    exit 0
fi

# Confirm with user before proceeding
echo ""
read -p "Proceed with archiving these files? (y/N): " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    print_info "Archival cancelled by user"
    exit 0
fi

# Move files
echo ""
print_info "Archiving files..."

MOVED_COUNT=0
SKIPPED_COUNT=0

for file in "${FILES_FOUND[@]}"; do
    filename=$(basename "$file")
    dest_file="$COMPLETED_DIR/$filename"
    
    if [[ -f "$dest_file" ]]; then
        print_warning "Skipping $filename (already exists in completed/)"
        ((SKIPPED_COUNT++))
    else
        print_info "Moving $filename to completed/"
        if mv "$file" "$dest_file"; then
            print_success "  ✓ Moved $filename"
            ((MOVED_COUNT++))
        else
            print_error "  ✗ Failed to move $filename"
        fi
    fi
done

# Summary
echo ""
print_success "Archival complete!"
print_info "Summary:"
print_info "  Files moved: $MOVED_COUNT"
print_info "  Files skipped: $SKIPPED_COUNT"
print_info "  Total files processed: ${#FILES_FOUND[@]}"

if [[ $MOVED_COUNT -gt 0 ]]; then
    echo ""
    print_info "Next steps for cycle closure:"
    print_info "1. Commit the document moves: git add completed/ plans/ && git commit -m 'Archive DevCycle $CYCLE_NUMBER documents'"
    print_info "2. Merge DC_$CYCLE_NUMBER branch to main"
    print_info "3. Delete DC_$CYCLE_NUMBER branch"
fi
#!/usr/bin/env python3
"""
DevCycle File Mover Script

Moves all files matching the DevCycle_2025_NNNN* pattern to the completed folder.
Usage: python move_cycle_to_completed.py <cycle_number>

Example: python move_cycle_to_completed.py 13
This will move:
- DevCycle_2025_0013.md
- DevCycle_2025_0013_brainstorm.md  
- DevCycle_2025_0013_bugs_01.md
- etc.

All files matching DevCycle_2025_0013* will be moved to plans/completed/
"""

import os
import sys
import glob
import shutil
from pathlib import Path


def move_cycle_files(cycle_number):
    """
    Move all DevCycle files for the given cycle number to completed folder.
    
    Args:
        cycle_number (int): The cycle number (e.g., 13)
    """
    # Get the directory where this script is located (plans folder)
    script_dir = Path(__file__).parent
    completed_dir = script_dir / "completed"
    
    # Ensure completed directory exists
    completed_dir.mkdir(exist_ok=True)
    
    # Format cycle number with leading zeros to 4 digits
    cycle_padded = f"{cycle_number:04d}"
    
    # Pattern to match all files for this cycle
    pattern = f"DevCycle_2025_{cycle_padded}*"
    
    # Find all matching files in the plans directory
    matching_files = list(script_dir.glob(pattern))
    
    if not matching_files:
        print(f"No files found matching pattern: {pattern}")
        return
    
    print(f"Found {len(matching_files)} files to move:")
    
    moved_count = 0
    for file_path in matching_files:
        try:
            destination = completed_dir / file_path.name
            
            # Check if destination already exists
            if destination.exists():
                print(f"  WARNING: {file_path.name} already exists in completed folder, skipping")
                continue
            
            # Move the file
            shutil.move(str(file_path), str(destination))
            print(f"  ✓ Moved: {file_path.name}")
            moved_count += 1
            
        except Exception as e:
            print(f"  ✗ Error moving {file_path.name}: {e}")
    
    print(f"\nCompleted: {moved_count} files moved to completed folder")


def main():
    """Main function to handle command line arguments and execute the move."""
    if len(sys.argv) != 2:
        print("Usage: python move_cycle_to_completed.py <cycle_number>")
        print("Example: python move_cycle_to_completed.py 13")
        sys.exit(1)
    
    try:
        cycle_number = int(sys.argv[1])
        
        if cycle_number < 1 or cycle_number > 9999:
            print("Error: Cycle number must be between 1 and 9999")
            sys.exit(1)
        
        print(f"Moving DevCycle 2025-{cycle_number:04d} files to completed folder...")
        move_cycle_files(cycle_number)
        
    except ValueError:
        print("Error: Cycle number must be a valid integer")
        sys.exit(1)
    except Exception as e:
        print(f"Error: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
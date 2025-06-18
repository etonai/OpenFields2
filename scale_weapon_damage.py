#!/usr/bin/env python3
"""
DC-11 Weapon Damage Scaling Script
Scales all weapon damage values by 5x in JSON files to match new 100 base health system.
"""
import json
import os
import sys

def scale_weapon_damage(file_path, scale_factor=5):
    """Scale damage values in a weapon JSON file by the given factor."""
    print(f"Processing {file_path}...")
    
    # Read the file
    with open(file_path, 'r') as f:
        data = json.load(f)
    
    changes_made = False
    
    # Handle ranged weapons
    if 'weapons' in data:
        for weapon_id, weapon in data['weapons'].items():
            if 'damage' in weapon:
                old_damage = weapon['damage']
                new_damage = old_damage * scale_factor
                weapon['damage'] = new_damage
                print(f"  {weapon_id}: {old_damage} -> {new_damage}")
                changes_made = True
    
    # Handle melee weapons
    if 'meleeWeapons' in data:
        for weapon_id, weapon in data['meleeWeapons'].items():
            if 'damage' in weapon:
                old_damage = weapon['damage']
                new_damage = old_damage * scale_factor
                weapon['damage'] = new_damage
                print(f"  {weapon_id}: {old_damage} -> {new_damage}")
                changes_made = True
    
    # Write back the file if changes were made
    if changes_made:
        with open(file_path, 'w') as f:
            json.dump(data, f, indent=2)
        print(f"  Updated {file_path}")
    else:
        print(f"  No damage values found in {file_path}")
    
    return changes_made

def main():
    """Main function to scale weapon damage in all theme files."""
    base_dir = "src/main/resources/data/themes"
    themes = ["test_theme", "civil_war"]
    weapon_files = ["ranged-weapons.json", "melee-weapons.json"]
    
    total_files_changed = 0
    
    print("DC-11 Weapon Damage Scaling Script")
    print("Scaling all weapon damage values by 5x for new 100 base health system")
    print("=" * 60)
    
    for theme in themes:
        print(f"\nProcessing theme: {theme}")
        theme_dir = os.path.join(base_dir, theme)
        
        if not os.path.exists(theme_dir):
            print(f"  Warning: Theme directory {theme_dir} not found")
            continue
        
        for weapon_file in weapon_files:
            file_path = os.path.join(theme_dir, weapon_file)
            
            if os.path.exists(file_path):
                if scale_weapon_damage(file_path):
                    total_files_changed += 1
            else:
                print(f"  Warning: File {file_path} not found")
    
    print(f"\n" + "=" * 60)
    print(f"Weapon damage scaling complete!")
    print(f"Files updated: {total_files_changed}")
    print(f"All weapon damage values have been scaled by 5x to match the new 100 base health system.")

if __name__ == "__main__":
    main()
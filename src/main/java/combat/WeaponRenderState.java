package combat;

/**
 * WeaponRenderState enum defines the visual rendering states for weapons.
 * This enum simplifies weapon rendering logic by categorizing weapon states
 * into clear visual categories independent of the underlying weapon state machine.
 * 
 * The enum provides a clean separation between weapon state management
 * and weapon visual representation, making rendering logic more maintainable.
 */
public enum WeaponRenderState {
    /**
     * HIDDEN - Weapon is not visible (sheathed, slung, holstered)
     * Used when weapon is in storage states and should not be rendered
     */
    HIDDEN,
    
    /**
     * READY - Weapon is visible in default/ready position
     * Used for ready, reloading, and other non-combat states
     * Weapon points in character's default facing direction
     */
    READY,
    
    /**
     * ATTACKING - Weapon is visible in combat position
     * Used for aiming, firing, and recovering states
     * Weapon points toward target or last target position
     */
    ATTACKING;
    
    /**
     * Determine the render state from a weapon state string.
     * This method maps weapon states to their corresponding visual rendering states.
     * 
     * @param weaponState The weapon state string (e.g., "aiming", "firing", "ready")
     * @return The corresponding WeaponRenderState enum value
     */
    public static WeaponRenderState fromWeaponState(String weaponState) {
        if (weaponState == null) {
            return READY; // Safe default
        }
        
        switch (weaponState.toLowerCase()) {
            // Hidden states - weapon not visible
            case "holstered":
            case "slung":
            case "sheathed":
                return HIDDEN;
                
            // Attacking states - weapon visible and aimed
            case "aiming":
            case "reaiming":
            case "firing":
            case "recovering":
            case "melee_attacking":
                return ATTACKING;
                
            // Ready states - weapon visible but not aimed
            case "ready":
            case "reloading":
            case "drawing":
            case "unsheathing":
            case "unsling":
            case "melee_ready":
            case "switching_to_melee":
            case "switching_to_ranged":
            default:
                return READY;
        }
    }
    
    /**
     * Check if the weapon should be visible for this render state.
     * 
     * @return true if weapon should be rendered, false if hidden
     */
    public boolean isVisible() {
        return this != HIDDEN;
    }
    
    /**
     * Check if the weapon should be aimed at a target for this render state.
     * 
     * @return true if weapon should point at target, false for default position
     */
    public boolean isAimed() {
        return this == ATTACKING;
    }
}
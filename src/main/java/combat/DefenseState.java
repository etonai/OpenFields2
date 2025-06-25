package combat;

/**
 * Enum representing the defensive state of a character in melee combat.
 * This state machine operates independently of weapon states to allow
 * clean separation of offensive and defensive mechanics.
 */
public enum DefenseState {
    /**
     * Character is ready to defend against incoming attacks.
     * Can transition to DEFENDING when attacked.
     */
    READY,
    
    /**
     * Character is actively defending against an attack.
     * This state is brief and transitions to COOLDOWN after defense attempt.
     */
    DEFENDING,
    
    /**
     * Character is in defensive cooldown and cannot defend.
     * Duration determined by weapon's defenseCooldown attribute.
     * Also applies during counter-attacks to create tactical vulnerability.
     */
    COOLDOWN
}
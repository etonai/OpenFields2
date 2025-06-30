package combat;

import game.ScheduledEvent;
import game.Unit;
import game.interfaces.IUnit;
import game.GameCallbacks;

/**
 * Melee combat system manager for Character.
 * Extracted from Character class as part of DevCycle 24 refactoring.
 * 
 * Handles melee attack sequences, movement to targets, range checking,
 * path updates, and melee-specific state management.
 */
public class MeleeCombatManager {
    
    // ========================================
    // MELEE ATTACK SEQUENCING
    // ========================================
    
    /**
     * Starts a melee attack sequence against a target
     * @param character Character performing the attack
     * @param attacker Attacking unit
     * @param target Target unit
     * @param currentTick Current game tick
     * @param eventQueue Game event queue
     * @param ownerId Owner ID for event scheduling
     * @param gameCallbacks Game callbacks interface
     */
    public static void startMeleeAttackSequence(Character character, IUnit attacker, IUnit target, 
            long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        System.out.println("[MELEE-ATTACK] " + character.getDisplayName() + " startMeleeAttackSequence called at tick " + currentTick);
        
        // Set attacking flag and mark as melee attack
        character.isAttacking = true;
        
        // Check for melee weapon
        MeleeWeapon meleeWeapon = character.meleeWeapon;
        if (meleeWeapon == null) {
            System.err.println("ERROR: " + character.getDisplayName() + " attempted melee attack without melee weapon!");
            character.isAttacking = false;
            return;
        }
        
        // Check range before starting attack
        if (!isInMeleeRange(attacker, target, meleeWeapon)) {
            System.out.println("[MELEE-ATTACK] " + character.getDisplayName() + " target out of melee range - cancelling attack at tick " + currentTick);
            character.isAttacking = false;
            return;
        }
        
        // Check if weapon is ready for melee attack
        // DevCycle 33: System 5 - Fix weapon state validation to check for "melee_ready" instead of "READY"
        if (character.currentWeaponState != null && !"melee_ready".equals(character.currentWeaponState.getState())) {
            System.out.println("[MELEE-ATTACK] " + character.getDisplayName() + " weapon not ready for melee attack (state: " + character.currentWeaponState.getState() + ") at tick " + currentTick);
            character.isAttacking = false;
            return;
        }
        
        // Calculate melee attack delay (based on weapon speed and character reflexes)
        long attackDelay = calculateMeleeAttackDelay(character, meleeWeapon);
        long attackTick = currentTick + attackDelay;
        
        System.out.println("[MELEE-ATTACK] " + character.getDisplayName() + " scheduling melee attack on " + 
                          target.getCharacter().getDisplayName() + " in " + attackDelay + " ticks at tick " + currentTick);
        
        // Schedule the actual melee attack
        eventQueue.add(new ScheduledEvent(attackTick, () -> {
            executeMeleeAttack(character, attacker, target, attackTick, eventQueue, ownerId, gameCallbacks);
        }, ownerId));
    }
    
    /**
     * Calculates the delay for a melee attack based on weapon and character stats
     * @param character Character performing attack
     * @param weapon Melee weapon being used
     * @return Attack delay in ticks
     */
    private static long calculateMeleeAttackDelay(Character character, MeleeWeapon weapon) {
        // Base attack delay from weapon speed
        long baseDelay = weapon.getAttackSpeed();
        
        // Apply character reflexes modifier (faster reflexes = faster attacks)
        // Reflexes modifier ranges from -20 to +20, convert to speed multiplier
        int reflexesModifier = utils.GameConstants.statToModifier(character.reflexes);
        double speedMultiplier = 1.0 - (reflexesModifier * 0.02); // Each point = 2% speed change
        
        // Ensure reasonable bounds (0.6x to 1.4x speed)
        speedMultiplier = Math.max(0.6, Math.min(1.4, speedMultiplier));
        
        return Math.round(baseDelay * speedMultiplier);
    }
    
    /**
     * Executes the actual melee attack
     * @param character Character performing attack
     * @param attacker Attacking unit
     * @param target Target unit
     * @param currentTick Current game tick
     * @param eventQueue Game event queue
     * @param ownerId Owner ID for event scheduling
     * @param gameCallbacks Game callbacks interface
     */
    private static void executeMeleeAttack(Character character, IUnit attacker, IUnit target, 
            long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        
        // Final range check before executing attack
        if (!isInMeleeRange(attacker, target, character.meleeWeapon)) {
            System.out.println("[MELEE-ATTACK] " + character.getDisplayName() + " target moved out of range before attack execution at tick " + currentTick);
            character.isAttacking = false;
            return;
        }
        
        // Check if target is still valid
        if (target.getCharacter().isIncapacitated()) {
            System.out.println("[MELEE-ATTACK] " + character.getDisplayName() + " target incapacitated before attack execution at tick " + currentTick);
            character.isAttacking = false;
            return;
        }
        
        System.out.println("[MELEE-ATTACK] " + character.getDisplayName() + " executes melee attack on " + target.getCharacter().getDisplayName() + " at tick " + currentTick);
        
        // DevCycle 33: System 6 - Fix melee attack to use direct impact scheduling instead of ranged attack sequence
        // Schedule immediate melee impact (no travel time for melee attacks)
        gameCallbacks.scheduleMeleeImpact((game.Unit)attacker, (game.Unit)target, character.meleeWeapon, currentTick);
        
        // Start recovery period
        long recoveryTime = Math.round(character.meleeWeapon.getStateBasedAttackCooldown() * character.calculateAttackSpeedMultiplier());
        character.startMeleeRecovery((int)recoveryTime, currentTick);
        
        // Clear attacking flag
        character.isAttacking = false;
    }
    
    // ========================================
    // MELEE MOVEMENT AND RANGE MANAGEMENT
    // ========================================
    
    /**
     * Updates melee movement toward target
     * @param character Character moving to melee
     * @param selfUnit Character's unit
     * @param currentTick Current game tick
     * @param eventQueue Game event queue
     * @param ownerId Owner ID for event scheduling
     * @param gameCallbacks Game callbacks interface
     */
    public static void updateMeleeMovement(Character character, IUnit selfUnit, long currentTick, 
            java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        
        // Check if we should be doing melee movement
        if (!character.isMovingToMelee || character.meleeTarget == null) {
            return;
        }
        
        // Check if target is still valid
        if (character.meleeTarget.getCharacter().isIncapacitated()) {
            CharacterDebugUtils.debugPrint("[MELEE-MOVEMENT] " + character.getDisplayName() + " target " + character.meleeTarget.getCharacter().getDisplayName() + " incapacitated during approach - cancelling movement");
            cancelMeleeMovement(character, selfUnit);
            return;
        }
        
        MeleeWeapon meleeWeapon = character.meleeWeapon;
        if (meleeWeapon == null) {
            CharacterDebugUtils.debugPrint("[MELEE-MOVEMENT] " + character.getDisplayName() + " lost melee weapon during movement - cancelling");
            cancelMeleeMovement(character, selfUnit);
            return;
        }
        
        // Check current distance to target
        double currentDistance = Math.hypot(character.meleeTarget.getX() - selfUnit.getX(), character.meleeTarget.getY() - selfUnit.getY());
        double distanceFeet = currentDistance / 7.0;
        double weaponReach = meleeWeapon.getTotalReach();
        
        // If we're already in range, start attack immediately
        if (distanceFeet <= weaponReach) {
            CharacterDebugUtils.debugPrint("[MELEE-MOVEMENT] " + character.getDisplayName() + " reached melee range of " + character.meleeTarget.getCharacter().getDisplayName() + " (" + String.format("%.2f", distanceFeet) + " feet)");
            CharacterDebugUtils.debugPrint("[MELEE-MOVEMENT] Range satisfied: " + String.format("%.2f", distanceFeet) + " <= " + String.format("%.2f", weaponReach) + " feet");
            CharacterDebugUtils.debugPrint("[MELEE-MOVEMENT] Cancelling movement and triggering melee attack");
            
            IUnit targetUnit = character.meleeTarget; // Save reference before clearing state
            cancelMeleeMovement(character, selfUnit);
            
            // Start the actual melee attack sequence
            CharacterDebugUtils.debugPrint("[MELEE-MOVEMENT] Calling startMeleeAttackSequence from movement completion");
            startMeleeAttackSequence(character, selfUnit, targetUnit, currentTick, eventQueue, selfUnit.getId(), gameCallbacks);
            return;
        }
        
        // Check if we're still moving (hasTarget indicates movement in progress)
        if (selfUnit.hasTarget()) {
            // Still moving - check if target has moved significantly and update path if needed
            double distanceToCurrentTarget = Math.hypot(selfUnit.getTargetX() - character.meleeTarget.getX(), selfUnit.getTargetY() - character.meleeTarget.getY());
            double targetMovementFeet = distanceToCurrentTarget / 7.0;
            
            // If target moved more than 3 feet, recalculate approach path
            if (targetMovementFeet > 3.0) {
                CharacterDebugUtils.debugPrint("[MELEE-MOVEMENT] " + character.getDisplayName() + " target " + character.meleeTarget.getCharacter().getDisplayName() + " moved " + String.format("%.2f", targetMovementFeet) + " feet - updating approach path");
                updateApproachPath(selfUnit, character.meleeTarget, meleeWeapon);
            }
        } else {
            // Movement completed, but we're not in range yet
            // Check if we should pursue further or give up
            double maxPursuitRange = 300.0; // Maximum pursuit range: 100 yards (300 feet)
            
            if (distanceFeet <= maxPursuitRange) {
                // Target is within pursuit range - start new movement
                CharacterDebugUtils.debugPrint("[MELEE-MOVEMENT] " + character.getDisplayName() + " movement completed but still out of range (" + String.format("%.2f", distanceFeet) + "/" + String.format("%.2f", weaponReach) + " feet) - continuing pursuit");
                updateApproachPath(selfUnit, character.meleeTarget, meleeWeapon);
            } else {
                // Target too far away - give up pursuit
                CharacterDebugUtils.debugPrint("[MELEE-MOVEMENT] " + character.getDisplayName() + " target " + character.meleeTarget.getCharacter().getDisplayName() + " too far away (" + String.format("%.2f", distanceFeet) + " feet) - cancelling pursuit (max: " + maxPursuitRange + " feet)");
                cancelMeleeMovement(character, selfUnit);
            }
        }
    }
    
    /**
     * Updates the approach path to the melee target (used when target moves during pursuit)
     * @param selfUnit Unit moving to target
     * @param target Target unit
     * @param meleeWeapon Melee weapon being used
     */
    private static void updateApproachPath(IUnit selfUnit, IUnit target, MeleeWeapon meleeWeapon) {
        // Calculate optimal approach position within melee range
        double weaponReach = meleeWeapon.getTotalReach();
        double approachDistance = weaponReach - 0.5; // Leave 0.5 feet buffer
        
        // Calculate direction from target to attacker
        double dx = selfUnit.getX() - target.getX();
        double dy = selfUnit.getY() - target.getY();
        double currentDistance = Math.hypot(dx, dy);
        
        // Normalize direction vector
        if (currentDistance > 0) {
            dx = dx / currentDistance;
            dy = dy / currentDistance;
        }
        
        // Calculate new approach position
        double approachPixelDistance = approachDistance * 7.0; // Convert feet to pixels
        double approachX = target.getX() + (dx * approachPixelDistance);
        double approachY = target.getY() + (dy * approachPixelDistance);
        
        // Update movement target
        selfUnit.setTarget(approachX, approachY);
    }
    
    /**
     * Cancels melee movement and clears related state
     * @param character Character to cancel movement for
     * @param selfUnit Character's unit
     */
    private static void cancelMeleeMovement(Character character, IUnit selfUnit) {
        character.isMovingToMelee = false;
        character.meleeTarget = null;
        
        // Stop movement by setting target to current position
        if (selfUnit != null && selfUnit.hasTarget()) {
            selfUnit.setTarget(selfUnit.getX(), selfUnit.getY());
            CharacterDebugUtils.debugPrint("[MELEE-MOVEMENT] " + character.getDisplayName() + " movement stopped at current position");
        }
    }
    
    // ========================================
    // RANGE AND VALIDATION UTILITIES
    // ========================================
    
    /**
     * Checks if target is within melee range of attacker using edge-to-edge distance
     * @param attacker Attacking unit
     * @param target Target unit
     * @param weapon Melee weapon being used
     * @return True if target is within melee range
     */
    public static boolean isInMeleeRange(IUnit attacker, IUnit target, MeleeWeapon weapon) {
        double centerToCenter = Math.hypot(target.getX() - attacker.getX(), target.getY() - attacker.getY());
        // Convert to edge-to-edge by subtracting target radius (1.5 feet = 10.5 pixels)
        double edgeToEdge = centerToCenter - (1.5 * 7.0);
        double pixelRange = weapon.getTotalReach() * 7.0; // Convert feet to pixels (7 pixels = 1 foot)
        
        return edgeToEdge <= pixelRange;
    }
}
package combat;

import game.interfaces.IUnit;
import game.GameCallbacks;
import game.ScheduledEvent;
import game.Unit;
import java.awt.Rectangle;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Handles automatic target acquisition and attack initiation for characters.
 * Extracted from Character.java to improve code organization and reduce file size.
 */
public class AutoTargetingSystem {
    
    /**
     * Updates automatic targeting for a character, finding and engaging valid targets
     */
    public static void updateAutomaticTargeting(Character character, IUnit selfUnit, long currentTick, 
                                               PriorityQueue<ScheduledEvent> eventQueue, GameCallbacks gameCallbacks) {
        
        // Only execute if automatic targeting is enabled
        if (!character.usesAutomaticTargeting) {
            return;
        }
        
        // Skip if character is incapacitated
        if (character.isIncapacitated()) {
            return;
        }
        
        // Skip if character has no weapon
        if (character.weapon == null) {
            return;
        }
        
        // Skip if character is already attacking (let existing attack complete)
        if (character.isAttacking) {
            return;
        }
        
        // Skip if character is reloading (let reload complete)
        if (character.isReloading) {
            return;
        }
        
        // DevCycle 33: System 2 - Skip if character is in melee recovery to prevent excessive attack continuation calls
        if (character.isMeleeCombatMode && character.isInMeleeRecovery(currentTick)) {
            if (config.DebugConfig.getInstance().isCombatDebugEnabled()) {
                System.out.println("[AUTO-TARGETING] " + character.getDisplayName() + 
                                 " auto-targeting skipped - in melee recovery until tick " + character.meleeRecoveryEndTick);
            }
            return;
        }
        
        // DevCycle 37: System 2 - Create reaiming state when target becomes incapacitated
        boolean targetJustIncapacitated = character.currentTarget != null 
            && character.currentTarget.getCharacter().isIncapacitated() 
            && character.isHostileTo(character.currentTarget.getCharacter())
            && !character.hasProcessedTargetIncapacitation;
        
        if (targetJustIncapacitated) {
            // Target just became incapacitated - create reaiming state
            character.hasProcessedTargetIncapacitation = true;
            
            // Create temporary "reaiming" state to represent target acquisition time
            if (character.weapon != null && character.currentWeaponState != null) {
                String previousState = character.currentWeaponState.getState();
                
                // Determine the target state to return to after reaiming
                String targetStateAfterReaiming;
                if ("aiming".equals(previousState)) {
                    targetStateAfterReaiming = "aiming";
                } else if ("pointedfromhip".equals(previousState)) {
                    targetStateAfterReaiming = "pointedfromhip";
                } else {
                    // For other states, use firing preference to determine target state
                    targetStateAfterReaiming = character.getFiresFromAimingState() ? "aiming" : "pointedfromhip";
                }
                
                // Create temporary reaiming state (15 ticks)
                WeaponState reamingState = new WeaponState("reaiming", targetStateAfterReaiming, 15);
                character.currentWeaponState = reamingState;
                
                // Clear any ongoing aiming timing in the AimingSystem
                combat.managers.AimingSystem.getInstance().resetAimingTiming(character.id);
                combat.managers.AimingSystem.getInstance().resetPointingFromHipTiming(character.id);
                
                if (config.DebugConfig.getInstance().isCombatDebugEnabled()) {
                    System.out.println("[REAIMING] " + character.getDisplayName() + 
                                     " weapon state reset from " + previousState + " to reaiming (will return to " + targetStateAfterReaiming + ")");
                }
            }
        }
        
        // Check if current target is still valid
        boolean currentTargetValid = character.currentTarget != null 
            && !character.currentTarget.getCharacter().isIncapacitated() 
            && character.isHostileTo(character.currentTarget.getCharacter());
        
        if (!currentTargetValid) {
            // Find a new target with target zone priority
            IUnit newTarget = findNearestHostileTargetWithZonePriority(character, selfUnit, gameCallbacks);
            
            if (newTarget != null) {
                // Target found - start attacking
                character.persistentAttack = true;
                character.currentTarget = newTarget; // DevCycle 22: Fix auto targeting infinite loop by setting currentTarget
                character.hasProcessedTargetIncapacitation = false; // Reset for new target
                
                // Calculate distance for logging
                double dx = newTarget.getX() - selfUnit.getX();
                double dy = newTarget.getY() - selfUnit.getY();
                double distanceFeet = Math.hypot(dx, dy) / 7.0; // Convert pixels to feet
                
                String zoneStatus = (character.targetZone != null && character.targetZone.contains((int)newTarget.getX(), (int)newTarget.getY())) ? " (in target zone)" : "";
                
                // Start attack sequence - check combat mode to determine attack type
                if (character.isMeleeCombatMode() && character.meleeWeapon != null) {
                    // Check if already in melee range
                    double distance = Math.hypot(newTarget.getX() - selfUnit.getX(), newTarget.getY() - selfUnit.getY());
                    double meleeRangePixels = character.meleeWeapon.getTotalReach() * 7.0; // Convert feet to pixels
                    
                    if (distance <= meleeRangePixels) {
                        // Already in range, attack immediately
                        character.startMeleeAttackSequence(selfUnit, newTarget, currentTick, eventQueue, selfUnit.getId(), gameCallbacks);
                    } else {
                        // Move to melee range first
                        // Set melee movement target - the updateMeleeMovement method will handle the attack when in range
                        character.isMovingToMelee = true;
                        character.meleeTarget = newTarget;
                        character.lastMeleeMovementUpdate = currentTick;
                        
                        // Ready melee weapon during movement (like manual attacks)
                        if (character.meleeWeapon != null) {
                            character.startReadyWeaponSequence(selfUnit, currentTick, eventQueue, selfUnit.getId());
                        }
                    }
                } else {
                    CombatCoordinator.getInstance().startAttackSequence(selfUnit, newTarget, currentTick, gameCallbacks);
                }
            } else {
                // No targets found - disable persistent attack but maintain weapon direction
                if (character.persistentAttack) {
                    character.persistentAttack = false;
                    character.currentTarget = null;
                    
                    // Preserve the current facing direction so weapon continues to aim at last target location
                    if (character.lastTargetFacing != null && selfUnit != null) {
                        selfUnit.setTargetFacing(character.lastTargetFacing);
                    }
                }
            }
        } else {
            // Handle case where we have a valid target but need to initiate/continue attack
            
            // Set persistent attack for auto-targeting continuation if not already set
            if (!character.persistentAttack) {
                character.persistentAttack = true;
            }
            
            // Only initiate attack sequence if not already in progress
            // DevCycle 33: System 2 - Prevent attacks during melee recovery to eliminate excessive attack continuation calls
            if (character.isMovingToMelee || character.isAttacking || 
                (character.isMeleeCombatMode && character.isInMeleeRecovery(currentTick))) {
                
                // Debug logging for recovery blocking
                if (character.isMeleeCombatMode && character.isInMeleeRecovery(currentTick) && 
                    config.DebugConfig.getInstance().isCombatDebugEnabled()) {
                    System.out.println("[AUTO-TARGETING] " + character.getDisplayName() + 
                                     " attack blocked - in melee recovery until tick " + character.meleeRecoveryEndTick);
                }
                return;
            }
            
            // Calculate distance for logging
            double dx = character.currentTarget.getX() - selfUnit.getX();
            double dy = character.currentTarget.getY() - selfUnit.getY();
            double distanceFeet = Math.hypot(dx, dy) / 7.0; // Convert pixels to feet
            
            String zoneStatus = (character.targetZone != null && character.targetZone.contains((int)character.currentTarget.getX(), (int)character.currentTarget.getY())) ? " (in target zone)" : "";
            
            // Start attack sequence - check combat mode to determine attack type
            if (character.isMeleeCombatMode() && character.meleeWeapon != null) {
                // Check if already in melee range
                double distance = Math.hypot(character.currentTarget.getX() - selfUnit.getX(), character.currentTarget.getY() - selfUnit.getY());
                double meleeRangePixels = character.meleeWeapon.getTotalReach() * 7.0; // Convert feet to pixels
                
                if (distance <= meleeRangePixels) {
                    // Already in range, attack immediately
                    character.startMeleeAttackSequence(selfUnit, character.currentTarget, currentTick, eventQueue, selfUnit.getId(), gameCallbacks);
                } else {
                    // Move to melee range first
                    // Set melee movement target - the updateMeleeMovement method will handle the attack when in range
                    character.isMovingToMelee = true;
                    character.meleeTarget = character.currentTarget;
                    character.lastMeleeMovementUpdate = currentTick;
                    
                    // Ready melee weapon during movement (like manual attacks)
                    if (character.meleeWeapon != null) {
                        character.startReadyWeaponSequence(selfUnit, currentTick, eventQueue, selfUnit.getId());
                    }
                }
            } else {
                CombatCoordinator.getInstance().startAttackSequence(selfUnit, character.currentTarget, currentTick, gameCallbacks);
            }
        }
    }
    
    /**
     * Finds the nearest hostile target, prioritizing targets within the character's target zone
     */
    public static IUnit findNearestHostileTargetWithZonePriority(Character character, IUnit selfUnit, GameCallbacks gameCallbacks) {
        List<Unit> allUnits = gameCallbacks.getUnits();
        IUnit nearestZoneTarget = null;
        IUnit nearestGlobalTarget = null;
        double nearestZoneDistance = Double.MAX_VALUE;
        double nearestGlobalDistance = Double.MAX_VALUE;
        int hostilesFound = 0;
        Random random = new Random();
        
        for (IUnit unit : allUnits) {
            // Skip self
            if (unit == selfUnit) continue;
            
            // Skip if not hostile (same faction)
            if (!character.isHostileTo(unit.getCharacter())) {
                continue;
            }
            
            hostilesFound++;
            
            // Skip if incapacitated
            if (unit.getCharacter().isIncapacitated()) continue;
            
            // Calculate distance
            double dx = unit.getX() - selfUnit.getX();
            double dy = unit.getY() - selfUnit.getY();
            double distance = Math.hypot(dx, dy);
            
            // Check weapon range limitations
            if (character.weapon != null && distance / 7.0 > ((RangedWeapon)character.weapon).getMaximumRange()) {
                continue; // Skip targets beyond weapon range
            }
            
            // Check if target is within target zone (if zone exists)
            boolean inTargetZone = false;
            if (character.targetZone != null) {
                inTargetZone = character.targetZone.contains((int)unit.getX(), (int)unit.getY());
            }
            
            if (inTargetZone) {
                // Target is in zone - prioritize zone targets
                if (distance < nearestZoneDistance) {
                    nearestZoneDistance = distance;
                    nearestZoneTarget = unit;
                } else if (distance == nearestZoneDistance && nearestZoneTarget != null) {
                    // Random selection for equidistant targets
                    if (random.nextBoolean()) {
                        nearestZoneTarget = unit;
                    }
                }
            } else {
                // Target is not in zone - track as global fallback
                if (distance < nearestGlobalDistance) {
                    nearestGlobalDistance = distance;
                    nearestGlobalTarget = unit;
                } else if (distance == nearestGlobalDistance && nearestGlobalTarget != null) {
                    // Random selection for equidistant targets
                    if (random.nextBoolean()) {
                        nearestGlobalTarget = unit;
                    }
                }
            }
        }
        
        // Return zone target if available, otherwise global target
        IUnit result = nearestZoneTarget != null ? nearestZoneTarget : nearestGlobalTarget;
        return result;
    }
    
    /**
     * Performs automatic target change for persistent attack scenarios
     */
    public static void performAutomaticTargetChange(Character character, IUnit shooter, long currentTick, 
                                                  PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Only proceed if still in persistent attack mode and not incapacitated
        if (!character.persistentAttack || character.isIncapacitated() || character.weapon == null) {
            System.out.println(character.getDisplayName() + " automatic retargeting cancelled - conditions no longer met");
            character.persistentAttack = false;
            character.currentTarget = null;
            character.isAttacking = false;
            
            // Preserve the current facing direction so weapon continues to aim at last target location
            if (character.lastTargetFacing != null && shooter != null) {
                shooter.setTargetFacing(character.lastTargetFacing);
            }
            return;
        }
        
        // Find new target with target zone priority
        IUnit newTarget = findNearestHostileTargetWithZonePriority(character, shooter, gameCallbacks);
        
        if (newTarget != null) {
            // New target found - start attacking
            character.currentTarget = newTarget;
            character.hasProcessedTargetIncapacitation = false; // Reset for new target
            
            // Calculate distance for logging
            double dx = newTarget.getX() - shooter.getX();
            double dy = newTarget.getY() - shooter.getY();
            double distanceFeet = Math.hypot(dx, dy) / 7.0;
            
            String zoneStatus = (character.targetZone != null && character.targetZone.contains((int)newTarget.getX(), (int)newTarget.getY())) ? " (in target zone)" : "";
            System.out.println(character.getDisplayName() + " automatically retargets to " + newTarget.getCharacter().getDisplayName() + 
                             " at " + String.format("%.1f", distanceFeet) + " feet" + zoneStatus);
            
            // Start new attack sequence using CombatCoordinator
            CombatCoordinator.getInstance().startAttackSequence(shooter, newTarget, currentTick, gameCallbacks);
        } else {
            // No targets found - end persistent attack
            System.out.println(character.getDisplayName() + " found no more targets for automatic retargeting");
            character.persistentAttack = false;
            character.currentTarget = null;
            character.isAttacking = false;
            
            // Preserve the current facing direction so weapon continues to aim at last target location
            if (character.lastTargetFacing != null && shooter != null) {
                shooter.setTargetFacing(character.lastTargetFacing);
            }
        }
    }
}
package combat;

import combat.managers.BurstFireManager;
import game.ScheduledEvent;
import utils.GameConstants;
import java.util.ArrayList;
import java.util.List;

/**
 * Hesitation and bravery system manager for Character.
 * Extracted from Character class as part of DevCycle 24 refactoring.
 * 
 * Handles wound-based hesitation, bravery checks, action pausing/resuming,
 * and bravery penalty management.
 */
public class HesitationManager {
    
    // ========================================
    // HESITATION TRIGGERING
    // ========================================
    
    /**
     * Triggers hesitation based on wound severity
     * @param character Character experiencing hesitation
     * @param woundSeverity Severity of wound causing hesitation
     * @param currentTick Current game tick
     * @param eventQueue Game event queue for scheduling
     * @param ownerId Owner ID for event scheduling
     */
    public static void triggerHesitation(Character character, WoundSeverity woundSeverity, 
            long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        // Calculate hesitation duration based on wound severity
        long hesitationDuration = calculateHesitationDuration(woundSeverity);
        
        if (hesitationDuration <= 0) {
            return; // No hesitation for scratch wounds
        }
        
        // Track hesitation statistics
        character.totalWoundHesitationTicks += hesitationDuration;
        
        if (character.isHesitating) {
            character.hesitationEndTick = Math.max(character.hesitationEndTick, currentTick + hesitationDuration);
            System.out.println(">>> HESITATION EXTENDED: " + character.getDisplayName() + " hesitation extended due to additional " + woundSeverity.name().toLowerCase() + " wound");
        } else {
            // Start new hesitation
            character.isHesitating = true;
            character.hesitationEndTick = currentTick + hesitationDuration;
            character.hesitationEndedAtTick = -1; // Reset the hesitation ended tick when new hesitation begins
            System.out.println(">>> HESITATION STARTED: " + character.getDisplayName() + " begins hesitating for " + hesitationDuration + " ticks due to " + woundSeverity.name().toLowerCase() + " wound");
            
            // Pause current actions by removing character's events and storing them
            pauseCurrentActions(character, eventQueue, ownerId);
            
            // Stop automatic firing if in progress
            if (BurstFireManager.getInstance().isAutomaticFiring(character.id)) {
                BurstFireManager.getInstance().setAutomaticFiring(character.id, false);
                BurstFireManager.getInstance().setBurstShotsFired(character.id, 0);
                System.out.println(">>> " + character.getDisplayName() + " automatic firing interrupted by wound");
            }
        }
        
        // Schedule hesitation end event
        eventQueue.add(new ScheduledEvent(character.hesitationEndTick, () -> {
            endHesitation(character, character.hesitationEndTick, eventQueue, ownerId);
        }, ownerId));
    }
    
    /**
     * Calculates hesitation duration based on wound severity
     * @param woundSeverity Severity of the wound
     * @return Duration in ticks (0 for no hesitation)
     */
    private static long calculateHesitationDuration(WoundSeverity woundSeverity) {
        switch (woundSeverity) {
            case LIGHT:
                return 15; // 1/4 second (15 ticks)
            case SERIOUS:
            case CRITICAL:
                return 60; // 1 second (60 ticks)
            case SCRATCH:
            default:
                return 0; // No hesitation for scratches
        }
    }
    
    // ========================================
    // ACTION PAUSING AND RESUMING
    // ========================================
    
    /**
     * Pauses current actions by removing scheduled events
     * @param character Character whose actions to pause
     * @param eventQueue Game event queue
     * @param ownerId Owner ID for event filtering
     */
    private static void pauseCurrentActions(Character character, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        // Find and remove all events belonging to this character
        List<ScheduledEvent> toRemove = new ArrayList<>();
        for (ScheduledEvent event : eventQueue) {
            if (event.getOwnerId() == ownerId) {
                toRemove.add(event);
            }
        }
        
        // Store paused events for later restoration
        character.pausedEvents.addAll(toRemove);
        
        // Remove from event queue
        eventQueue.removeAll(toRemove);
        
        if (!toRemove.isEmpty()) {
            System.out.println(">>> " + character.getDisplayName() + " paused " + toRemove.size() + " scheduled actions due to hesitation");
        }
    }
    
    /**
     * Ends hesitation and resets attack state
     * @param character Character recovering from hesitation
     * @param currentTick Current game tick
     * @param eventQueue Game event queue
     * @param ownerId Owner ID for event scheduling
     */
    private static void endHesitation(Character character, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        if (!character.isHesitating) {
            return; // Already ended
        }
        
        character.isHesitating = false;
        character.hesitationEndedAtTick = currentTick; // Record the tick when hesitation ended
        System.out.println(">>> HESITATION ENDED: " + character.getDisplayName() + " recovers from hesitation at tick " + currentTick);
        
        // DevCycle 41: System 6 - Debug logging for hesitation/melee recovery coordination
        if (character.isMeleeCombatMode && character.isInMeleeRecovery(currentTick)) {
            System.out.println("[HESITATION-MELEE COORDINATION] " + character.getDisplayName() + 
                             " is still in melee recovery (until tick " + character.meleeRecoveryEndTick + 
                             ") - auto-targeting will be allowed for 5 ticks despite recovery");
        }
        
        // DevCycle 41: System 6 - Reschedule paused events instead of clearing them
        // This ensures recovery events execute after hesitation ends
        if (!character.pausedEvents.isEmpty()) {
            System.out.println("[HESITATION-RECOVERY] " + character.getDisplayName() + 
                             " resuming " + character.pausedEvents.size() + " paused events");
            for (ScheduledEvent pausedEvent : character.pausedEvents) {
                // Reschedule the event to execute immediately
                eventQueue.add(new ScheduledEvent(currentTick, pausedEvent.action, pausedEvent.getOwnerId()));
            }
            character.pausedEvents.clear();
        }
        
        // CRITICAL FIX: Always reset attack state to allow new commands, regardless of weapon state
        character.isAttacking = false;
        System.out.println(">>> HESITATION RECOVERY: " + character.getDisplayName() + " attack state reset to allow new combat actions");
        
        
        // DevCycle 36+38: Fix weapon state recovery after hesitation
        // Check if character is stuck in "recovering" or "firing" state and schedule proper transition
        if (character.currentWeaponState != null && 
            ("recovering".equals(character.currentWeaponState.getState()) || 
             "firing".equals(character.currentWeaponState.getState()))) {
            String currentState = character.currentWeaponState.getState();
            System.out.println(">>> HESITATION RECOVERY: " + character.getDisplayName() + " detected in " + currentState + " state, scheduling transition to preferred firing state");
            
            // Determine target state based on firing preference
            String targetState = character.getFiresFromAimingState() ? "aiming" : "pointedfromhip";
            
            // Get the transition timing from the current weapon state
            long transitionTicks = character.currentWeaponState.ticks;
            
            System.out.println(">>> HESITATION RECOVERY: " + character.getDisplayName() + " scheduling transition from recovering to " + targetState + " in " + transitionTicks + " ticks");
            
            // Schedule the state transition directly without requiring unit parameters
            long transitionTick = currentTick + transitionTicks;
            eventQueue.add(new ScheduledEvent(transitionTick, () -> {
                // Get the appropriate weapon for state lookup
                Weapon activeWeapon = character.isMeleeCombatMode ? character.meleeWeapon : character.weapon;
                String previousState = character.currentWeaponState != null ? character.currentWeaponState.getState() : "None";
                character.currentWeaponState = activeWeapon.getStateByName(targetState);
                
                // Start timing if entering aiming state
                if ("aiming".equals(targetState)) {
                    character.startAimingTiming(transitionTick);
                } else if ("pointedfromhip".equals(targetState)) {
                    character.startPointingFromHipTiming(transitionTick);
                }
                
                // Output weapon state change
                System.out.println(">>> HESITATION RECOVERY COMPLETE: " + character.getDisplayName() + " weapon state: " + previousState + " -> " + targetState + " ***");
                
                // Reset attacking flag to allow new combat actions
                character.isAttacking = false;
            }, ownerId));
        } else {
            // DevCycle 38: Handle characters stuck in "firing" state without proper recovery transition
            // This can happen when hesitation interrupts the normal firing→recovering→reload/ready flow
            if (character.currentWeaponState != null && "firing".equals(character.currentWeaponState.getState())) {
                System.out.println(">>> HESITATION RECOVERY: " + character.getDisplayName() + " stuck in firing state after hesitation, scheduling immediate recovery transition");
                
                // Immediately transition to the appropriate post-firing state
                if (character.weapon instanceof RangedWeapon && ((RangedWeapon)character.weapon).getAmmunition() <= 0 && character.canReload() && !character.isReloading) {
                    // Need to reload - start reload sequence immediately
                    character.currentWeaponState = character.weapon.getStateByName("reloading");
                    character.isAttacking = false;
                    
                    // Use CombatCoordinator to start reload properly
                    eventQueue.add(new ScheduledEvent(currentTick + 1, () -> {
                        // Start reload sequence using proper delegation
                        character.startReloadSequence(null, currentTick + 1, eventQueue, ownerId, null);
                        System.out.println(">>> HESITATION RECOVERY: " + character.getDisplayName() + " starting reload after hesitation recovery");
                    }, ownerId));
                } else {
                    // Still has ammo - return to ready state and then preferred firing state
                    String targetState = character.getFiresFromAimingState() ? "aiming" : "pointedfromhip";
                    character.currentWeaponState = character.weapon.getStateByName(targetState);
                    
                    // Start timing for the target state
                    if ("aiming".equals(targetState)) {
                        character.startAimingTiming(currentTick);
                    } else if ("pointedfromhip".equals(targetState)) {
                        character.startPointingFromHipTiming(currentTick);
                    }
                    
                    System.out.println(">>> HESITATION RECOVERY: " + character.getDisplayName() + " returning to " + targetState + " state after hesitation");
                }
            }
        }
    }
    
    /**
     * Checks if character is currently hesitating
     * @param character Character to check
     * @param currentTick Current game tick
     * @return True if character is actively hesitating
     */
    public static boolean isCurrentlyHesitating(Character character, long currentTick) {
        return character.isHesitating && currentTick < character.hesitationEndTick;
    }
    
    // ========================================
    // BRAVERY CHECK MECHANICS
    // ========================================
    
    /**
     * Performs a bravery check for the character
     * @param character Character performing bravery check
     * @param currentTick Current game tick
     * @param eventQueue Game event queue for scheduling recovery
     * @param ownerId Owner ID for event scheduling
     * @param reason Reason for the bravery check (for logging)
     */
    public static void performBraveryCheck(Character character, long currentTick, 
            java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, String reason) {
        // Skip bravery checks for incapacitated characters
        if (character.isIncapacitated()) {
            return;
        }
        
        // Calculate bravery check target number: 50 + coolness modifier
        int coolnessModifier = GameConstants.statToModifier(character.coolness);
        int targetNumber = 50 + coolnessModifier;
        
        // Roll d100 for bravery check
        double roll = utils.RandomProvider.nextDouble() * 100;
        
        System.out.println(">>> BRAVERY CHECK: " + character.getDisplayName() + " rolls " + String.format("%.1f", roll) + " vs " + targetNumber + " (" + reason + ")");
        
        if (roll >= targetNumber) {
            // Bravery check failed
            character.braveryCheckFailures++;
            character.braveryPenaltyEndTick = currentTick + 180; // 3 seconds (180 ticks)
            
            // Track bravery hesitation statistics
            character.totalBraveryHesitationTicks += 180;
            
            System.out.println(">>> BRAVERY FAILED: " + character.getDisplayName() + " fails bravery check! Total failures: " + character.braveryCheckFailures + " (penalty: -" + (character.braveryCheckFailures * 10) + " accuracy)");
            
            // Schedule bravery recovery event
            eventQueue.add(new ScheduledEvent(character.braveryPenaltyEndTick, () -> {
                recoverFromBraveryFailure(character, currentTick);
            }, ownerId));
        } else {
            System.out.println(">>> BRAVERY PASSED: " + character.getDisplayName() + " passes bravery check");
        }
    }
    
    /**
     * Handles recovery from bravery failure
     * @param character Character recovering
     * @param currentTick Current game tick
     */
    private static void recoverFromBraveryFailure(Character character, long currentTick) {
        if (character.braveryCheckFailures > 0) {
            character.braveryCheckFailures--;
            System.out.println(">>> BRAVERY RECOVERY: " + character.getDisplayName() + " recovers from bravery failure. Remaining failures: " + character.braveryCheckFailures);
            
            // If more failures remain, the penalty continues
            if (character.braveryCheckFailures > 0) {
                character.braveryPenaltyEndTick = currentTick + 180; // Reset duration for remaining penalties
            }
        }
    }
    
    /**
     * Gets current bravery penalty for accuracy calculations
     * @param character Character to check
     * @param currentTick Current game tick
     * @return Penalty amount (negative modifier)
     */
    public static int getBraveryPenalty(Character character, long currentTick) {
        if (currentTick < character.braveryPenaltyEndTick && character.braveryCheckFailures > 0) {
            return character.braveryCheckFailures * 10; // -10 per failure
        }
        return 0;
    }
}
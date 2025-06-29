package combat;

import combat.managers.*;
import game.IEventSchedulingService;
import game.interfaces.IUnit;
import game.GameCallbacks;
import game.ScheduledEvent;
import game.Unit;

/**
 * Central coordinator for all combat operations.
 * Manages interactions between combat managers and provides a unified interface
 * for combat mechanics, replacing the monolithic combat code in Character class.
 */
public class CombatCoordinator {
    
    // Singleton instance
    private static CombatCoordinator instance;
    
    // Manager references (eagerly initialized)
    private final IBurstFireManager burstFireManager;
    private final IAimingSystem aimingSystem;
    private final IDefenseManager defenseManager;
    private final IWeaponStateManager weaponStateManager;
    private final IReloadManager reloadManager;
    private final ICharacterSkillsManager skillsManager;
    private final ICharacterStatsManager statsManager;
    private final ITargetManager targetManager;
    
    // Service references
    private final IEventSchedulingService eventSchedulingService;
    
    // Other system references are static classes
    
    /**
     * Private constructor for singleton pattern.
     * Initializes all managers eagerly.
     */
    private CombatCoordinator() {
        // Initialize managers
        this.burstFireManager = BurstFireManager.getInstance();
        this.aimingSystem = AimingSystem.getInstance();
        this.defenseManager = DefenseManager.getInstance();
        this.weaponStateManager = WeaponStateManager.getInstance();
        this.reloadManager = ReloadManager.getInstance();
        this.skillsManager = CharacterSkillsManager.getInstance();
        this.statsManager = CharacterStatsManager.getInstance();
        this.targetManager = TargetManager.getInstance();
        
        // Get service references
        this.eventSchedulingService = game.EventSchedulingService.getInstance();
        
        // Existing systems use static methods
    }
    
    /**
     * Get the singleton instance of CombatCoordinator.
     * 
     * @return The coordinator instance
     */
    public static CombatCoordinator getInstance() {
        if (instance == null) {
            instance = new CombatCoordinator();
        }
        return instance;
    }
    
    // ===== Attack Coordination =====
    
    /**
     * Start an attack sequence for a character.
     * Coordinates all managers involved in the attack.
     * 
     * @param attacker The attacking unit
     * @param target The target unit
     * @param currentTick The current game tick
     * @param gameCallbacks Game callback interface
     * @return true if attack was started successfully
     */
    public boolean startAttackSequence(IUnit attacker, IUnit target, long currentTick, GameCallbacks gameCallbacks) {
        Character character = attacker.getCharacter();
        
        // Check if character can attack
        if (character.isIncapacitated() || character.isAttacking) {
            return false;
        }
        
        // Handle automatic targeting if needed
        if (target == null && character.usesAutomaticTargeting) {
            target = AutoTargetingSystem.findNearestHostileTargetWithZonePriority(character, attacker, gameCallbacks);
            if (target == null) {
                return false;
            }
        }
        
        // Call the full attack sequence implementation - get eventQueue from gameCallbacks
        java.util.PriorityQueue<game.ScheduledEvent> eventQueue = gameCallbacks != null ? gameCallbacks.getEventQueue() : null;
        return startAttackSequenceInternal(attacker, target, currentTick, eventQueue, attacker.getId(), gameCallbacks);
    }
    
    /**
     * Internal attack sequence implementation with full logic from Character class.
     * Extracted from Character.startAttackSequence to reduce Character.java size.
     */
    public boolean startAttackSequenceInternal(IUnit attacker, IUnit target, long currentTick, java.util.PriorityQueue<game.ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        Character character = attacker.getCharacter();
        
        if (character.weapon == null || character.currentWeaponState == null) {
            return false;
        }
        
        // Always interrupt burst/auto when starting a new attack
        if (burstFireManager.isAutomaticFiring(character.id)) {
            burstFireManager.setAutomaticFiring(character.id, false);
            burstFireManager.setBurstShotsFired(character.id, 0);
        }
        
        // Check if this is a target change and handle first attack penalty
        boolean targetChanged = (character.currentTarget != null && character.currentTarget != target);
        boolean newTarget = (character.currentTarget == null);
        
        // If targeting a different unit, cancel all pending attacks and reset
        if (character.currentTarget != null && character.currentTarget != target) {
            // Clear all pending events for this character
            if (gameCallbacks != null) {
                gameCallbacks.removeAllEventsForOwner(attacker.getId());
            } else {
                System.err.println("CRITICAL ERROR: gameCallbacks is null in " + character.getDisplayName() + " attack sequence - cannot cancel pending events");
            }
            // DevCycle 27: System 6 - Smart target switching that respects firing preference
            character.currentWeaponState = character.getOptimalStateForTargetSwitch();
            // DevCycle 27: Reset aiming timing when changing targets
            character.resetAimingTiming();
            // DevCycle 28: Reset multiple shot sequence on target change
            character.resetMultipleShotSequence();
            // Start timing for new state if applicable
            character.startTimingForTargetSwitchState(currentTick);
            // Interrupt burst/auto if in progress
            if (burstFireManager.isAutomaticFiring(character.id)) {
                burstFireManager.setAutomaticFiring(character.id, false);
                burstFireManager.setBurstShotsFired(character.id, 0);
            }
        } else if ("aiming".equals(character.currentWeaponState.getState()) && character.currentTarget != target) {
            // DevCycle 27: System 6 - Smart target switching for aiming state changes
            character.currentWeaponState = character.getOptimalStateForTargetSwitch();
            // Reset aiming timing when changing targets from aiming state
            character.resetAimingTiming();
            // Start timing for new state if applicable
            character.startTimingForTargetSwitchState(currentTick);
        } else if (character.currentTarget == target && character.isAttacking) {
            // Already attacking the same target, don't start duplicate attack
            return false;
        } else if (character.lastAttackScheduledTick == currentTick) {
            // Prevent multiple attack sequences from being scheduled in the same tick
            return false;
        }
        
        // Handle first attack penalty system
        if (targetChanged || newTarget) {
            // Target changed or new target - apply first attack penalty
            character.isFirstAttackOnTarget = true;
        } else if (character.currentTarget == target) {
            // Same target as before - no first attack penalty
            character.isFirstAttackOnTarget = false;
        } else {
            // This shouldn't happen but be safe - treat as new target
            character.isFirstAttackOnTarget = true;
        }
        
        character.previousTarget = character.currentTarget;
        character.currentTarget = target;
        character.isAttacking = true;
        character.lastAttackScheduledTick = currentTick;
        
        // DevCycle 28: Initialize multiple shot sequence
        character.currentShotInSequence = 1; // Starting first shot
        
        // Make unit face the target and save the direction for later use
        attacker.faceToward(target.getX(), target.getY());
        
        // Calculate and save the target facing direction for weapon visibility
        double dx = target.getX() - attacker.getX();
        double dy = target.getY() - attacker.getY();
        double angleRadians = Math.atan2(dx, -dy);
        double angleDegrees = Math.toDegrees(angleRadians);
        if (angleDegrees < 0) angleDegrees += 360;
        character.lastTargetFacing = angleDegrees;
        
        // Handle melee vs ranged combat
        if (character.isMeleeCombatMode) {
            // Delegate to character for melee attack
            character.startMeleeAttackSequence(attacker, target, currentTick, eventQueue, ownerId, gameCallbacks);
        } else {
            // Schedule ranged attack based on current weapon state
            character.scheduleAttackFromCurrentState(attacker, target, currentTick, eventQueue, ownerId, gameCallbacks);
        }
        
        return true;
    }
    
    /**
     * Internal melee attack sequence implementation with full logic from Character class.
     * Extracted from Character.scheduleMeleeAttackFromCurrentState to reduce Character.java size.
     * DevCycle 31: Extract melee attack scheduling logic to CombatCoordinator.
     */
    public void startMeleeAttackSequenceInternal(IUnit attacker, IUnit target, long currentTick, java.util.PriorityQueue<game.ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        Character character = attacker.getCharacter();
        
        if (character.meleeWeapon == null) {
            return;
        }
        
        // Get active weapon for state management (use melee weapon's states)
        Weapon activeWeapon = character.getActiveWeapon();
        WeaponState currentState = character.currentWeaponState;
        
        if (currentState == null) {
            // Initialize to weapon's initial state if no current state
            currentState = activeWeapon.getInitialState();
            character.currentWeaponState = currentState;
        }
        
        String stateName = currentState != null ? currentState.getState() : "null";
        
        // Handle melee weapon state transitions
        if ("sheathed".equals(stateName)) {
            character.scheduleMeleeStateTransition("unsheathing", currentTick, currentState.ticks, attacker, target, eventQueue, ownerId, gameCallbacks);
        } else if ("unsheathing".equals(stateName)) {
            character.scheduleMeleeStateTransition("melee_ready", currentTick, currentState.ticks, attacker, target, eventQueue, ownerId, gameCallbacks);
        } else if ("melee_ready".equals(stateName)) {
            // Ready to attack - schedule the melee attack
            long attackTime = Math.round(character.meleeWeapon.getStateBasedAttackSpeed() * character.calculateAttackSpeedMultiplier());
            character.scheduleMeleeAttack(attacker, target, currentTick + attackTime, eventQueue, ownerId, gameCallbacks);
        } else if ("switching_to_melee".equals(stateName)) {
            character.scheduleMeleeStateTransition("melee_ready", currentTick, currentState.ticks, attacker, target, eventQueue, ownerId, gameCallbacks);
        } else if ("melee_attacking".equals(stateName)) {
            // Already attacking - cannot start another attack until current one completes
            return;
        } else {
            // For any other state (like "slung"), go directly to sheathed state first
            
            WeaponState sheathedState = activeWeapon.getStateByName("sheathed");
            if (sheathedState != null) {
                character.currentWeaponState = sheathedState;
                startMeleeAttackSequenceInternal(attacker, target, currentTick, eventQueue, ownerId, gameCallbacks);
            } else {
                // Emergency fallback: use any available state or create a simple ready state
                if (activeWeapon.states != null && !activeWeapon.states.isEmpty()) {
                    WeaponState firstState = activeWeapon.states.get(0);
                    character.currentWeaponState = firstState;
                    startMeleeAttackSequenceInternal(attacker, target, currentTick, eventQueue, ownerId, gameCallbacks);
                } else {
                    WeaponState emergencyReady = new WeaponState("melee_ready", "melee_attacking", 15);
                    character.currentWeaponState = emergencyReady;
                    startMeleeAttackSequenceInternal(attacker, target, currentTick, eventQueue, ownerId, gameCallbacks);
                }
            }
        }
    }
    
    /**
     * Internal firing sequence implementation with full logic from Character class.
     * Extracted from Character.scheduleFiring to reduce Character.java size.
     * DevCycle 31: Extract firing sequence logic to CombatCoordinator.
     */
    public void scheduleFiringInternal(IUnit shooter, IUnit target, long fireTick, java.util.PriorityQueue<game.ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        Character character = shooter.getCharacter();
        
        // Prevent duplicate firing events for the same tick
        if (character.lastFiringScheduledTick == fireTick) {
            return;
        }
        character.lastFiringScheduledTick = fireTick;
        
        eventQueue.add(new ScheduledEvent(fireTick, () -> {
            // Add firing console output with aiming duration and earned bonus (DevCycle 27: System 3)
            String firingMode = character.getFiresFromAimingState() ? "shootingfromaiming" : "shootingfromhip";
            long aimingDuration = character.getCurrentAimingDuration(fireTick);
            String aimingText = character.getFiresFromAimingState() ? "aimed " + aimingDuration + " ticks" : "pointed " + aimingDuration + " ticks";
            
            // Check for earned bonus and format appropriately
            AccumulatedAimingBonus earnedBonus = character.calculateEarnedAimingBonus(fireTick);
            String bonusText;
            if (earnedBonus != AccumulatedAimingBonus.NONE) {
                bonusText = ", earned " + earnedBonus.getDisplayName() + " bonus";
            } else {
                bonusText = ", using " + character.getCurrentAimingSpeed().getDisplayName() + " aiming";
            }
            
            // Calculate ammunition display for after firing (DevCycle 27: System 7)
            String ammunitionText = "";
            if (character.weapon instanceof RangedWeapon) {
                RangedWeapon rangedWeapon = (RangedWeapon) character.weapon;
                int currentAmmo = rangedWeapon.getAmmunition();
                int maxAmmo = rangedWeapon.getMaxAmmunition();
                // Show ammunition after firing (subtract 1 if there's ammunition to fire)
                int ammoAfterFiring = currentAmmo > 0 ? currentAmmo - 1 : currentAmmo;
                ammunitionText = ", [ammo: " + ammoAfterFiring + "/" + maxAmmo + "]";
            }
            
            System.out.println(character.getDisplayName() + " fires a " + character.weapon.getName() + " at " + 
                             target.getCharacter().getDisplayName() + ", " + firingMode + " (" + aimingText + bonusText + ")" + ammunitionText + ", at tick " + fireTick);
            
            character.currentWeaponState = character.weapon.getStateByName("firing");
            // DevCycle 27: Reset aiming timing after firing (timing is now reported)
            character.resetAimingTiming();
            
            if (character.weapon instanceof RangedWeapon && ((RangedWeapon)character.weapon).getAmmunition() <= 0) {
            } else if (character.weapon instanceof RangedWeapon) {
                ((RangedWeapon)character.weapon).setAmmunition(((RangedWeapon)character.weapon).getAmmunition() - 1);
                
                if (gameCallbacks != null) {
                    gameCallbacks.playWeaponSound(character.weapon);
                    gameCallbacks.applyFiringHighlight((Unit)shooter, fireTick);
                    gameCallbacks.addMuzzleFlash((Unit)shooter, fireTick);
                } else {
                    System.err.println("CRITICAL ERROR: gameCallbacks is null in " + character.getDisplayName() + " firing sequence - audio and visual effects disabled");
                }
                
                double dx = target.getX() - shooter.getX();
                double dy = target.getY() - shooter.getY();
                double distancePixels = Math.hypot(dx, dy);
                double distanceFeet = distancePixels / 7.0; // pixelsToFeet conversion
                
                if (gameCallbacks != null) {
                    gameCallbacks.scheduleProjectileImpact((Unit)shooter, (Unit)target, character.weapon, fireTick, distanceFeet);
                } else {
                    System.err.println("CRITICAL ERROR: gameCallbacks is null in " + character.getDisplayName() + " projectile impact scheduling - hit detection disabled");
                }
                
                // Handle burst firing - delegate to BurstFireManager
                if (character.weapon instanceof RangedWeapon && ((RangedWeapon)character.weapon).getCurrentFiringMode() == FiringMode.BURST) {
                    BurstFireManager.getInstance().scheduleBurstShots(character, shooter, fireTick, gameCallbacks);
                }
            }
            
            WeaponState firingState = character.weapon.getStateByName("firing");
            eventQueue.add(new ScheduledEvent(fireTick + firingState.ticks, () -> {
                character.currentWeaponState = character.weapon.getStateByName("recovering");
                
                WeaponState recoveringState = character.weapon.getStateByName("recovering");
                eventQueue.add(new ScheduledEvent(fireTick + firingState.ticks + recoveringState.ticks, () -> {
                    if (character.weapon instanceof RangedWeapon && ((RangedWeapon)character.weapon).getAmmunition() <= 0 && character.canReload() && !character.isReloading) {
                        character.isAttacking = false; // Clear attacking flag during reload
                        // DevCycle 28: Reset multiple shot sequence when reloading
                        character.resetMultipleShotSequence();
                        character.startReloadSequence(shooter, fireTick + firingState.ticks + recoveringState.ticks, eventQueue, ownerId, gameCallbacks);
                    } else {
                        long completionTick = fireTick + firingState.ticks + recoveringState.ticks;
                        // Set recovery state based on firing preference (Task 2)
                        String recoveryTargetState = character.getFiresFromAimingState() ? "aiming" : "pointedfromhip";
                        character.currentWeaponState = character.weapon.getStateByName(recoveryTargetState);
                        
                        // DevCycle 27: Start timing when entering aiming or pointing states after recovery
                        if ("aiming".equals(recoveryTargetState)) {
                            character.startAimingTiming(completionTick);
                        } else if ("pointedfromhip".equals(recoveryTargetState)) {
                            character.startPointingFromHipTiming(completionTick);
                        }
                        
                        // DevCycle 28: Check if we need to fire more shots in the sequence
                        if (character.multipleShootCount > 1 && character.currentShotInSequence < character.multipleShootCount && character.currentTarget != null) {
                            // Determine aiming speed for NEXT shot before incrementing counter
                            character.currentShotInSequence++; // Increment to next shot number
                            AimingSpeed nextShotSpeed = AimingSystem.getInstance().getAimingSpeedForMultipleShot(character); // Get speed for this shot number
                            
                            // Maintain attack state and schedule next shot
                            character.isAttacking = true;
                            
                            // Calculate delay based on pattern aiming speed
                            long quickDelay = Math.round(character.currentWeaponState.ticks * nextShotSpeed.getTimingMultiplier() * AimingSystem.getInstance().calculateAimingSpeedMultiplier(character));
                            
                            // Schedule the next shot in the sequence
                            scheduleFiringInternal(shooter, character.currentTarget, completionTick + quickDelay, eventQueue, ownerId, gameCallbacks);
                        } else {
                            // Multiple shot sequence complete or single shot
                            character.currentShotInSequence = 0; // Reset shot counter
                            character.isAttacking = false; // Attack sequence complete
                            
                            // Only call checkContinuousAttack if NOT using persistent attack mode
                            // Persistent attack is handled entirely by continueStandardAttack scheduling
                            if (!character.persistentAttack) {
                                character.checkContinuousAttack(shooter, completionTick, eventQueue, ownerId, gameCallbacks);
                            } else {
                            }
                        }
                    }
                }, ownerId));
            }, ownerId));
            
        }, ownerId));
    }
    
    /**
     * Continue a persistent attack for a character.
     * Handles burst fire and automatic weapons.
     * 
     * @param attacker The attacking unit
     * @param currentTick The current game tick
     * @param gameCallbacks Game callback interface
     */
    public void continuePersistentAttack(IUnit attacker, long currentTick, GameCallbacks gameCallbacks) {
        Character character = attacker.getCharacter();
        
        if (!character.persistentAttack || character.currentTarget == null) {
            return;
        }
        
        // Check if target is still valid
        if (character.currentTarget.getCharacter().isIncapacitated()) {
            // Try to find new target if using automatic targeting
            if (character.usesAutomaticTargeting) {
                IUnit newTarget = AutoTargetingSystem.findNearestHostileTargetWithZonePriority(character, attacker, gameCallbacks);
                if (newTarget != null) {
                    character.currentTarget = newTarget;
                    startAttackSequence(attacker, newTarget, currentTick, gameCallbacks);
                }
            }
            return;
        }
        
        // Check for automatic/burst fire
        if (character.weapon instanceof combat.RangedWeapon) {
            combat.RangedWeapon rangedWeapon = (combat.RangedWeapon) character.weapon;
            if (burstFireManager.handleContinuousFiring(character, character.currentTarget, currentTick)) {
                // Burst/auto fire is handling continuation
                if (rangedWeapon.getCurrentFiringMode() == combat.FiringMode.BURST) {
                    burstFireManager.handleBurstFiring(character, character.currentTarget, currentTick);
                } else if (rangedWeapon.getCurrentFiringMode() == combat.FiringMode.FULL_AUTO) {
                    burstFireManager.handleFullAutoFiring(character, character.currentTarget, currentTick);
                }
                return;
            }
        }
        
        // Standard attack continuation
        burstFireManager.continueStandardAttack(character, character.currentTarget, currentTick);
    }
    
    // ===== Weapon State Management =====
    
    /**
     * Progress weapon to a specific hold state.
     * 
     * @param unit The unit whose weapon should progress
     * @param targetState The target hold state
     * @param currentTick The current game tick
     */
    public void progressWeaponToHoldState(IUnit unit, String targetState, long currentTick) {
        Character character = unit.getCharacter();
        
        if (character.weapon == null || character.currentWeaponState == null) {
            return;
        }
        
        weaponStateManager.setTargetHoldState(character.id, targetState);
        
        // Start progression if not already at target
        if (!character.currentWeaponState.getState().equals(targetState)) {
            scheduleWeaponStateProgression(unit, targetState, currentTick);
        }
    }
    
    /**
     * Cycle weapon hold state (H key functionality).
     * 
     * @param unit The unit to cycle hold state
     * @return The new hold state, or null if not applicable
     */
    public String cycleWeaponHoldState(IUnit unit) {
        Character character = unit.getCharacter();
        
        if (character.weapon == null || character.isMeleeCombatMode) {
            return null;
        }
        
        return weaponStateManager.cycleWeaponHoldState(character, character.weapon);
    }
    
    /**
     * Toggle firing preference (SHIFT-F functionality).
     * 
     * @param unit The unit to toggle preference
     * @return The new firing preference
     */
    public boolean toggleFiringPreference(IUnit unit) {
        Character character = unit.getCharacter();
        
        if (character.isMeleeCombatMode) {
            return character.getFiresFromAimingState(); // No change in melee mode
        }
        
        return weaponStateManager.toggleFiringPreference(character.id);
    }
    
    // ===== Reload Management =====
    
    /**
     * Start a reload sequence for a character.
     * 
     * @param unit The unit reloading
     * @param currentTick The current game tick
     * @return true if reload started successfully
     */
    public boolean startReload(IUnit unit, long currentTick) {
        Character character = unit.getCharacter();
        
        if (!(character.weapon instanceof RangedWeapon)) {
            return false;
        }
        
        RangedWeapon weapon = (RangedWeapon) character.weapon;
        return reloadManager.startReloadSequence(character, weapon, currentTick);
    }
    
    /**
     * Check if a character is currently reloading.
     * 
     * @param characterId The character ID to check
     * @return true if reloading
     */
    public boolean isReloading(int characterId) {
        return reloadManager.isReloading(characterId);
    }
    
    // ===== Defense Management =====
    
    /**
     * Attempt to defend against an attack.
     * 
     * @param defender The defending unit
     * @param attacker The attacking unit
     * @param currentTick The current game tick
     * @return true if defense was successful
     */
    public boolean attemptDefense(IUnit defender, IUnit attacker, long currentTick) {
        Character character = defender.getCharacter();
        
        if (!defenseManager.canDefend(character, currentTick)) {
            return false;
        }
        
        return defenseManager.attemptBlock(character, attacker, currentTick);
    }
    
    /**
     * Check for counter-attack opportunity.
     * 
     * @param defender The defending unit
     * @param attacker The attacking unit
     * @param currentTick The current game tick
     * @return true if counter-attack was executed
     */
    public boolean checkCounterAttack(IUnit defender, IUnit attacker, long currentTick) {
        Character character = defender.getCharacter();
        
        if (!defenseManager.hasCounterAttackOpportunity(character.id, currentTick)) {
            return false;
        }
        
        return defenseManager.executeCounterAttack(character, attacker, currentTick);
    }
    
    // ===== Aiming Management =====
    
    /**
     * Get the accumulated aiming bonus for a character.
     * 
     * @param character The character aiming
     * @param currentTick The current game tick
     * @return The aiming bonus
     */
    public int getAimingBonus(Character character, long currentTick) {
        long aimingDuration = aimingSystem.getAimingDuration(character.id, currentTick);
        
        if (aimingDuration <= 0) {
            return 0;
        }
        
        return aimingSystem.calculateEarnedAimingBonusModifier(character, aimingDuration);
    }
    
    /**
     * Start aiming timing for a character.
     * 
     * @param characterId The character ID
     * @param currentTick The current game tick
     */
    public void startAimingTiming(int characterId, long currentTick) {
        aimingSystem.startAimingTiming(characterId, currentTick);
    }
    
    /**
     * Start pointing-from-hip timing for a character.
     * 
     * @param characterId The character ID
     * @param currentTick The current game tick
     */
    public void startPointingFromHipTiming(int characterId, long currentTick) {
        aimingSystem.startPointingFromHipTiming(characterId, currentTick);
    }
    
    // ===== Cleanup =====
    
    /**
     * Clean up all manager state for a character being removed.
     * 
     * @param characterId The ID of the character to clean up
     */
    public void cleanupCharacter(int characterId) {
        // Clean up each manager
        burstFireManager.cleanupCharacter(characterId);
        aimingSystem.cleanupCharacter(characterId);
        defenseManager.cleanupCharacter(characterId);
        weaponStateManager.cleanupCharacter(characterId);
        reloadManager.cleanupCharacter(characterId);
        skillsManager.cleanupCharacter(characterId);
        statsManager.cleanupCharacter(characterId);
        targetManager.cleanupCharacter(characterId);
        
        // Cancel any scheduled events
        eventSchedulingService.cancelEventsForOwner(characterId);
    }
    
    // ===== Private Helper Methods =====
    
    private void scheduleAttackFromCurrentState(IUnit attacker, IUnit target, long currentTick, GameCallbacks gameCallbacks) {
        // Schedule ranged attack based on current weapon state
        Character character = attacker.getCharacter();
        character.scheduleAttackFromCurrentState(attacker, target, currentTick, null, attacker.getId(), gameCallbacks);
    }
    
    private void scheduleWeaponStateProgression(IUnit unit, String targetState, long currentTick) {
        // This will handle weapon state transitions
        // Implementation depends on extracting logic from Character class
        Character character = unit.getCharacter();
        
        // Use weapon state manager to handle the progression
        long transitionTick = weaponStateManager.scheduleStateTransition(
            character, 
            character.currentWeaponState, 
            character.weapon.getStateByName(targetState),
            currentTick
        );
        
        // Schedule the state change
        eventSchedulingService.scheduleEvent(transitionTick, () -> {
            character.currentWeaponState = character.weapon.getStateByName(targetState);
            
            // Check if we should continue to attack
            if (character.isAttacking && character.currentTarget != null) {
                scheduleAttackFromCurrentState(unit, character.currentTarget, transitionTick, null);
            }
        }, unit.getId());
    }
    
    // ===== Getters for Managers (for testing) =====
    
    public IBurstFireManager getBurstFireManager() {
        return burstFireManager;
    }
    
    public IAimingSystem getAimingSystem() {
        return aimingSystem;
    }
    
    public IDefenseManager getDefenseManager() {
        return defenseManager;
    }
    
    public IWeaponStateManager getWeaponStateManager() {
        return weaponStateManager;
    }
    
    public IReloadManager getReloadManager() {
        return reloadManager;
    }
}
package combat.managers;

import combat.Character;
import combat.RangedWeapon;
import combat.FiringMode;
import game.IEventSchedulingService;
import game.EventSchedulingService;
import game.interfaces.IUnit;
import game.GameCallbacks;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton manager for burst and automatic fire mechanics.
 * Handles burst sequences, full-auto fire, and related state tracking.
 */
public class BurstFireManager implements IBurstFireManager {
    
    private static BurstFireManager instance;
    
    // Per-character state tracking
    private final Map<Integer, Boolean> automaticFiringState = new HashMap<>();
    private final Map<Integer, Integer> burstShotsFired = new HashMap<>();
    private final Map<Integer, Long> lastAutomaticShot = new HashMap<>();
    private final Map<Integer, Long> lastContinueAttackTick = new HashMap<>();
    
    // Service references
    private final IEventSchedulingService eventSchedulingService;
    
    /**
     * Private constructor for singleton pattern.
     */
    private BurstFireManager() {
        this.eventSchedulingService = EventSchedulingService.getInstance();
    }
    
    /**
     * Get the singleton instance of BurstFireManager.
     * 
     * @return The manager instance
     */
    public static BurstFireManager getInstance() {
        if (instance == null) {
            instance = new BurstFireManager();
        }
        return instance;
    }
    
    @Override
    public boolean handleContinuousFiring(Character character, IUnit target, long currentTick) {
        if (character.weapon == null || !(character.weapon instanceof RangedWeapon)) {
            return false;
        }
        
        RangedWeapon weapon = (RangedWeapon) character.weapon;
        FiringMode firingMode = weapon.getCurrentFiringMode();
        
        if (firingMode == null) {
            // Default behavior for weapons without firing modes
            return false;
        }
        
        switch (firingMode) {
            case SINGLE_SHOT:
                // Single shot mode - let standard attack handle it
                return false;
                
            case BURST:
                // Burst mode - fire predetermined number of rounds quickly
                handleBurstFiring(character, target, currentTick);
                return true;
                
            case FULL_AUTO:
                // Full auto mode - continuous firing at cyclic rate
                handleFullAutoFiring(character, target, currentTick);
                return true;
                
            default:
                return false;
        }
    }
    
    @Override
    public void handleBurstFiring(Character character, IUnit target, long currentTick) {
        if (!(character.weapon instanceof RangedWeapon)) {
            return;
        }
        
        RangedWeapon weapon = (RangedWeapon) character.weapon;
        
        // Check if a burst is already in progress
        if (isAutomaticFiring(character.id)) {
            // Burst already in progress - wait for it to complete
            
            // Calculate when current burst will complete and schedule next burst
            int remainingShots = weapon.getBurstSize() - getBurstShotsFired(character.id);
            if (remainingShots > 0) {
                // Schedule next burst after current burst completes + firing delay
                long fullBurstDuration = (weapon.getBurstSize() - 1) * weapon.getCyclicRate();
                long lastShot = getLastAutomaticShot(character.id);
                long nextBurstTick = lastShot + fullBurstDuration + weapon.getFiringDelay();
                
                // Ensure we don't schedule in the past
                if (nextBurstTick <= currentTick) {
                    nextBurstTick = currentTick + weapon.getFiringDelay();
                }
                
                final long finalNextBurstTick = nextBurstTick;
                eventSchedulingService.scheduleEvent(finalNextBurstTick, () -> {
                    if (character.persistentAttack && character.currentTarget != null && 
                        !character.currentTarget.getCharacter().isIncapacitated() && 
                        !character.isIncapacitated()) {
                        character.isAttacking = true;
                        // Note: In full implementation, this would call back to CombatCoordinator
                        // to start a new attack sequence
                    }
                }, character.id);
            }
            return;
        }
        
        // No burst in progress - the attack sequence will trigger burst via scheduleFiring()
        if (target != null && !target.getCharacter().isIncapacitated() && !character.isIncapacitated()) {
            // Check if we can start new attack sequence without clearing isAttacking flag
            if (character.isAttacking) {
                // Already attacking - don't start duplicate sequence
                return;
            }
            // Note: In full implementation, this would call back to CombatCoordinator
            // to start a new attack sequence
        }
    }
    
    @Override
    public void handleFullAutoFiring(Character character, IUnit target, long currentTick) {
        if (!(character.weapon instanceof RangedWeapon)) {
            return;
        }
        
        RangedWeapon weapon = (RangedWeapon) character.weapon;
        
        if (!isAutomaticFiring(character.id)) {
            // Start new full auto sequence
            setAutomaticFiring(character.id, true);
            setBurstShotsFired(character.id, 1); // First shot already fired
            setLastAutomaticShot(character.id, currentTick);
        } else {
            // Continue full auto - increment shot count
            setBurstShotsFired(character.id, getBurstShotsFired(character.id) + 1);
        }
        
        // Schedule next shot at firing delay
        long nextShotTick = currentTick + weapon.getFiringDelay();
        eventSchedulingService.scheduleEvent(nextShotTick, () -> {
            // DC-24: Continue full-auto even if shooter incapacitated (but not if target incapacitated)
            if (character.persistentAttack && character.currentTarget != null && 
                !character.currentTarget.getCharacter().isIncapacitated()) {
                setLastAutomaticShot(character.id, nextShotTick);
                character.isAttacking = true;
                // Note: In full implementation, this would call back to CombatCoordinator
                // to schedule attack from current state
            } else {
                // Stop automatic firing if conditions not met
                setAutomaticFiring(character.id, false);
                setBurstShotsFired(character.id, 0);
            }
        }, character.id);
    }
    
    @Override
    public void continueStandardAttack(Character character, IUnit target, long currentTick) {
        // Prevent duplicate continue attack commands for the same tick
        Long lastTick = lastContinueAttackTick.get(character.id);
        if (lastTick != null && lastTick == currentTick) {
            return;
        }
        lastContinueAttackTick.put(character.id, currentTick);
        
        if (!(character.weapon instanceof RangedWeapon)) {
            return;
        }
        
        RangedWeapon weapon = (RangedWeapon) character.weapon;
        
        if (weapon.getFiringDelay() > 0) {
            long nextAttackTick = currentTick + weapon.getFiringDelay();
            eventSchedulingService.scheduleEvent(nextAttackTick, () -> {
                if (character.persistentAttack && character.currentTarget != null && 
                    !character.currentTarget.getCharacter().isIncapacitated() && 
                    !character.isIncapacitated() && weapon.getAmmunition() > 0) {
                    character.isAttacking = true;
                    // Note: In full implementation, this would call back to CombatCoordinator
                    // to schedule attack from current state
                } else if (character.persistentAttack && character.currentTarget != null && 
                          !character.currentTarget.getCharacter().isIncapacitated() && 
                          !character.isIncapacitated() && weapon.getAmmunition() <= 0 && 
                          character.canReload() && !character.isReloading) {
                    // Note: In full implementation, this would call back to CombatCoordinator
                    // to start reload sequence
                }
            }, character.id);
        } else {
            if (weapon.getAmmunition() > 0) {
                character.isAttacking = true;
                // Note: In full implementation, this would call back to CombatCoordinator
                // to schedule attack from current state
            } else if (weapon.getAmmunition() <= 0 && character.canReload() && !character.isReloading) {
                // Note: In full implementation, this would call back to CombatCoordinator
                // to start reload sequence
            }
        }
    }
    
    @Override
    public void continueAutomaticShooting(Character character, IUnit target, long currentTick) {
        // This method handles the continuation of automatic fire sequences
        // It's typically called from scheduled events for burst/auto fire
        
        if (!isAutomaticFiring(character.id)) {
            return;
        }
        
        if (!(character.weapon instanceof RangedWeapon)) {
            // Stop automatic firing if weapon changed
            setAutomaticFiring(character.id, false);
            setBurstShotsFired(character.id, 0);
            return;
        }
        
        RangedWeapon weapon = (RangedWeapon) character.weapon;
        
        // Check if we should continue based on firing mode
        switch (weapon.getCurrentFiringMode()) {
            case BURST:
                // Check if burst is complete
                if (getBurstShotsFired(character.id) >= weapon.getBurstSize()) {
                    // Burst complete
                    setAutomaticFiring(character.id, false);
                    setBurstShotsFired(character.id, 0);
                }
                break;
                
            case FULL_AUTO:
                // Full auto continues until trigger release or conditions not met
                // Conditions are checked in handleFullAutoFiring
                break;
                
            default:
                // Not in automatic mode
                setAutomaticFiring(character.id, false);
                setBurstShotsFired(character.id, 0);
                break;
        }
    }
    
    @Override
    public boolean isAutomaticFiring(int characterId) {
        return automaticFiringState.getOrDefault(characterId, false);
    }
    
    @Override
    public void setAutomaticFiring(int characterId, boolean isAutomaticFiring) {
        if (isAutomaticFiring) {
            automaticFiringState.put(characterId, true);
        } else {
            automaticFiringState.remove(characterId);
        }
    }
    
    @Override
    public int getBurstShotsFired(int characterId) {
        return burstShotsFired.getOrDefault(characterId, 0);
    }
    
    @Override
    public void setBurstShotsFired(int characterId, int shotsFired) {
        if (shotsFired > 0) {
            burstShotsFired.put(characterId, shotsFired);
        } else {
            burstShotsFired.remove(characterId);
        }
    }
    
    @Override
    public long getLastAutomaticShot(int characterId) {
        return lastAutomaticShot.getOrDefault(characterId, 0L);
    }
    
    @Override
    public void setLastAutomaticShot(int characterId, long tick) {
        if (tick > 0) {
            lastAutomaticShot.put(characterId, tick);
        } else {
            lastAutomaticShot.remove(characterId);
        }
    }
    
    @Override
    public void scheduleBurstShots(Character character, IUnit shooter, long fireTick, GameCallbacks gameCallbacks) {
        if (!(character.weapon instanceof RangedWeapon)) {
            return;
        }
        
        RangedWeapon weapon = (RangedWeapon) character.weapon;
        
        // Only handle burst mode
        if (weapon.getCurrentFiringMode() != FiringMode.BURST || isAutomaticFiring(character.id)) {
            return;
        }
        
        // Set burst firing state
        setAutomaticFiring(character.id, true);
        setBurstShotsFired(character.id, 1); // First shot just fired
        setLastAutomaticShot(character.id, fireTick);
        
        // Schedule remaining shots in the burst
        for (int shot = 2; shot <= weapon.getBurstSize(); shot++) {
            long nextShotTick = fireTick + (weapon.getFiringDelay() * (shot - 1));
            final int shotNumber = shot;
            
            eventSchedulingService.scheduleEvent(nextShotTick, () -> {
                // Continue burst even if target dies or shooter incapacitated, but stop if out of ammo
                if (character.currentTarget != null && weapon.getAmmunition() > 0) {
                    weapon.setAmmunition(weapon.getAmmunition() - 1);
                    setBurstShotsFired(character.id, shotNumber);
                    
                    // Play effects
                    if (gameCallbacks != null) {
                        gameCallbacks.playWeaponSound(character.weapon);
                        gameCallbacks.applyFiringHighlight((game.Unit)shooter, nextShotTick);
                        gameCallbacks.addMuzzleFlash((game.Unit)shooter, nextShotTick);
                        
                        // Calculate projectile impact
                        double dx = character.currentTarget.getX() - shooter.getX();
                        double dy = character.currentTarget.getY() - shooter.getY();
                        double distancePixels = Math.hypot(dx, dy);
                        double distanceFeet = distancePixels / 7.0;
                        
                        gameCallbacks.scheduleProjectileImpact((game.Unit)shooter, (game.Unit)character.currentTarget, 
                                                              character.weapon, nextShotTick, distanceFeet);
                    }
                    
                    // Reset burst state after final shot
                    if (shotNumber >= weapon.getBurstSize()) {
                        setAutomaticFiring(character.id, false);
                        setBurstShotsFired(character.id, 0);
                    }
                } else {
                    // Burst interrupted
                    setAutomaticFiring(character.id, false);
                    setBurstShotsFired(character.id, 0);
                }
            }, character.id);
        }
    }
    
    @Override
    public boolean shouldApplyBurstAutoPenalty(int characterId) {
        return getBurstShotsFired(characterId) > 1;
    }
    
    // ===== Continuous Firing Support =====
    
    /**
     * Continue standard (non-burst/auto) attack for persistent attack mode.
     * 
     * @param character The character continuing attack
     * @param shooter The unit shooting
     * @param currentTick Current game tick
     * @param gameCallbacks Game callback interface
     */
    public void continueStandardAttack(Character character, IUnit shooter, long currentTick, GameCallbacks gameCallbacks) {
        // Prevent duplicate continue attack commands for the same tick
        if (character.lastContinueAttackTick == currentTick) {
            return;
        }
        character.lastContinueAttackTick = currentTick;
        
        if (!(character.weapon instanceof RangedWeapon)) {
            return;
        }
        
        RangedWeapon weapon = (RangedWeapon) character.weapon;
        
        if (weapon.getFiringDelay() > 0) {
            long nextAttackTick = currentTick + weapon.getFiringDelay();
            eventSchedulingService.scheduleEvent(nextAttackTick, () -> {
                if (character.persistentAttack && character.currentTarget != null && 
                    !character.currentTarget.getCharacter().isIncapacitated() && !character.isIncapacitated() && 
                    weapon.getAmmunition() > 0) {
                    character.isAttacking = true;
                    // Use CombatCoordinator to schedule attack instead of calling private method
                    combat.CombatCoordinator.getInstance().startAttackSequence(shooter, character.currentTarget, nextAttackTick, gameCallbacks);
                } else if (character.persistentAttack && character.currentTarget != null && 
                          !character.currentTarget.getCharacter().isIncapacitated() && !character.isIncapacitated() && 
                          weapon.getAmmunition() <= 0 && character.canReload() && !character.isReloading) {
                    character.startReloadSequence(shooter, nextAttackTick, null, shooter.getId(), gameCallbacks);
                }
            }, shooter.getId());
        } else {
            if (weapon.getAmmunition() > 0) {
                character.isAttacking = true;
                // Use CombatCoordinator to schedule attack instead of calling private method
                combat.CombatCoordinator.getInstance().startAttackSequence(shooter, character.currentTarget, currentTick, gameCallbacks);
            } else if (weapon.getAmmunition() <= 0 && character.canReload() && !character.isReloading) {
                character.startReloadSequence(shooter, currentTick, null, shooter.getId(), gameCallbacks);
            }
        }
    }
    
    /**
     * Handle continuous firing coordination.
     * Determines whether to use burst/auto or standard attack continuation.
     * 
     * @param character The character firing
     * @param shooter The unit shooting
     * @param currentTick Current game tick
     * @param gameCallbacks Game callback interface
     */
    public void handleContinuousFiring(Character character, IUnit shooter, long currentTick, GameCallbacks gameCallbacks) {
        // Use BurstFireManager to handle continuous firing modes
        if (!handleContinuousFiring(character, character.currentTarget, currentTick)) {
            // Default behavior for single shot or weapons without firing modes
            continueStandardAttack(character, shooter, currentTick, gameCallbacks);
        }
    }
    
    @Override
    public void cleanupCharacter(int characterId) {
        // Remove all state for this character
        automaticFiringState.remove(characterId);
        burstShotsFired.remove(characterId);
        lastAutomaticShot.remove(characterId);
        lastContinueAttackTick.remove(characterId);
    }
}
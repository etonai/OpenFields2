package combat;

import combat.managers.*;
import game.IEventSchedulingService;
import game.interfaces.IUnit;
import game.GameCallbacks;

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
        
        // Interrupt any burst/auto firing
        burstFireManager.setAutomaticFiring(character.id, false);
        burstFireManager.setBurstShotsFired(character.id, 0);
        
        // Set attack state
        character.isAttacking = true;
        character.currentTarget = target;
        
        // Reset multiple shot sequence if target changed
        if (character.previousTarget != target) {
            character.currentShotInSequence = 0;
            character.previousTarget = target;
        }
        
        // Handle melee vs ranged combat
        if (character.isMeleeCombatMode) {
            // Delegate to character for melee attack
            // For now, we need to pass null for event queue as Character still needs it
            character.startMeleeAttackSequence(attacker, target, currentTick, null, attacker.getId(), gameCallbacks);
        } else {
            // Schedule ranged attack based on current weapon state
            scheduleAttackFromCurrentState(attacker, target, currentTick, gameCallbacks);
        }
        
        return true;
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
        
        // Cancel any scheduled events
        eventSchedulingService.cancelEventsForOwner(characterId);
    }
    
    // ===== Private Helper Methods =====
    
    private void scheduleAttackFromCurrentState(IUnit attacker, IUnit target, long currentTick, GameCallbacks gameCallbacks) {
        // For ranged attacks, delegate to Character's existing attack sequence
        // The Character class already has all the complex logic for weapon state progression
        Character character = attacker.getCharacter();
        character.startAttackSequence(attacker, target, currentTick, null, attacker.getId(), gameCallbacks);
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
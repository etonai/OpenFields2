package combat.managers;

import combat.Character;
import combat.RangedWeapon;
import combat.ReloadType;
import combat.WeaponState;
import game.IEventSchedulingService;
import game.EventSchedulingService;
import utils.GameConstants;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton manager for weapon reload mechanics.
 * Handles reload sequences, timing, and ammunition management.
 */
public class ReloadManager implements IReloadManager {
    
    private static ReloadManager instance;
    
    // Per-character reload state tracking
    private final Map<Integer, Long> reloadStartTicks = new HashMap<>();
    private final Map<Integer, Long> reloadCompletionTicks = new HashMap<>();
    
    // Service references
    private final IEventSchedulingService eventSchedulingService;
    
    /**
     * Private constructor for singleton pattern.
     */
    private ReloadManager() {
        this.eventSchedulingService = EventSchedulingService.getInstance();
    }
    
    /**
     * Get the singleton instance of ReloadManager.
     * 
     * @return The manager instance
     */
    public static ReloadManager getInstance() {
        if (instance == null) {
            instance = new ReloadManager();
        }
        return instance;
    }
    
    @Override
    public boolean startReloadSequence(Character character, RangedWeapon weapon, long currentTick) {
        // Check if character can reload
        if (!canReload(character, weapon)) {
            return false;
        }
        
        // Check if already reloading
        if (isReloading(character.id)) {
            return false;
        }
        
        // Set weapon state to reloading
        character.currentWeaponState = character.weapon.getStateByName("reloading");
        character.isReloading = true;
        
        // Calculate reload duration
        long reloadDuration = calculateReloadDuration(weapon, character);
        long completionTick = currentTick + reloadDuration;
        
        // Store reload state
        setReloadState(character.id, currentTick, completionTick);
        
        // Schedule reload completion
        eventSchedulingService.scheduleEvent(completionTick, () -> {
            performReload(character, weapon, weapon.getReloadType());
            
            // Check if we need to continue reloading (single-round weapons)
            if (weapon.getReloadType() == ReloadType.SINGLE_ROUND && 
                weapon.getAmmunition() < weapon.getMaxAmmunition()) {
                // Continue reloading
                continueReloading(character, completionTick);
            } else {
                // Reload complete
                completeReload(character);
            }
        }, character.id);
        
        return true;
    }
    
    @Override
    public boolean continueReloading(Character character, long currentTick) {
        if (!(character.weapon instanceof RangedWeapon)) {
            completeReload(character);
            return false;
        }
        
        RangedWeapon weapon = (RangedWeapon) character.weapon;
        
        // Check if weapon is full
        if (weapon.getAmmunition() >= weapon.getMaxAmmunition()) {
            completeReload(character);
            return false;
        }
        
        // Calculate next reload duration
        long reloadDuration = calculateReloadDuration(weapon, character);
        long completionTick = currentTick + reloadDuration;
        
        // Update reload state
        setReloadState(character.id, currentTick, completionTick);
        
        // Schedule next reload
        eventSchedulingService.scheduleEvent(completionTick, () -> {
            performReload(character, weapon, ReloadType.SINGLE_ROUND);
            
            // Check if we need to continue
            if (weapon.getAmmunition() < weapon.getMaxAmmunition()) {
                continueReloading(character, completionTick);
            } else {
                completeReload(character);
            }
        }, character.id);
        
        return true;
    }
    
    @Override
    public void performReload(Character character, RangedWeapon weapon, ReloadType reloadType) {
        if (reloadType == ReloadType.SINGLE_ROUND) {
            // Add one round
            weapon.setAmmunition(Math.min(weapon.getAmmunition() + 1, weapon.getMaxAmmunition()));
        } else {
            // Full magazine reload
            weapon.setAmmunition(weapon.getMaxAmmunition());
        }
    }
    
    @Override
    public double calculateReloadSpeed(Character character) {
        // Get reflexes modifier (-20 to +20)
        int reflexesModifier = GameConstants.statToModifier(character.reflexes);
        
        // Convert to speed multiplier (slower with negative modifier, faster with positive)
        // Each point of modifier = 1% speed change
        double reflexesSpeedMultiplier = 1.0 - (reflexesModifier * 0.01);
        
        return reflexesSpeedMultiplier;
    }
    
    @Override
    public boolean isReloading(int characterId) {
        return reloadCompletionTicks.containsKey(characterId) && 
               reloadCompletionTicks.get(characterId) > eventSchedulingService.getCurrentTick();
    }
    
    @Override
    public double getReloadProgress(int characterId, long currentTick) {
        Long startTick = reloadStartTicks.get(characterId);
        Long completionTick = reloadCompletionTicks.get(characterId);
        
        if (startTick == null || completionTick == null || currentTick >= completionTick) {
            return -1; // Not reloading
        }
        
        long totalDuration = completionTick - startTick;
        long elapsed = currentTick - startTick;
        
        return (double) elapsed / totalDuration;
    }
    
    @Override
    public long getReloadStartTick(int characterId) {
        return reloadStartTicks.getOrDefault(characterId, -1L);
    }
    
    @Override
    public long getReloadCompletionTick(int characterId) {
        return reloadCompletionTicks.getOrDefault(characterId, -1L);
    }
    
    @Override
    public boolean cancelReload(int characterId) {
        if (!isReloading(characterId)) {
            return false;
        }
        
        // Clear reload state
        reloadStartTicks.remove(characterId);
        reloadCompletionTicks.remove(characterId);
        
        // Cancel scheduled events
        eventSchedulingService.cancelEventsForOwner(characterId);
        
        return true;
    }
    
    @Override
    public void setReloadState(int characterId, long startTick, long completionTick) {
        reloadStartTicks.put(characterId, startTick);
        reloadCompletionTicks.put(characterId, completionTick);
    }
    
    @Override
    public long calculateReloadDuration(RangedWeapon weapon, Character character) {
        // Base reload time from weapon
        long baseReloadTime = weapon.getReloadTicks();
        
        // Apply character's reload speed multiplier
        double speedMultiplier = calculateReloadSpeed(character);
        
        return Math.round(baseReloadTime * speedMultiplier);
    }
    
    @Override
    public void cleanupCharacter(int characterId) {
        reloadStartTicks.remove(characterId);
        reloadCompletionTicks.remove(characterId);
    }
    
    // Private helper methods
    
    /**
     * Check if character can reload their weapon.
     */
    private boolean canReload(Character character, RangedWeapon weapon) {
        // Check if weapon needs reloading
        if (weapon.getAmmunition() >= weapon.getMaxAmmunition()) {
            return false;
        }
        
        // Check weapon state
        if (character.currentWeaponState == null) {
            return false;
        }
        
        String state = character.currentWeaponState.getState();
        return "ready".equals(state) || "aiming".equals(state) || "recovering".equals(state);
    }
    
    /**
     * Complete the reload sequence.
     */
    private void completeReload(Character character) {
        // Clear reload state
        character.isReloading = false;
        reloadStartTicks.remove(character.id);
        reloadCompletionTicks.remove(character.id);
        
        // Set weapon state back to ready
        if (character.weapon != null) {
            character.currentWeaponState = character.weapon.getStateByName("ready");
        }
        
        // Note: In full implementation, this would notify CombatCoordinator
        // to check if attack should continue
    }
}
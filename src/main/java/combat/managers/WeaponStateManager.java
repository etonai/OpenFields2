package combat.managers;

import combat.WeaponState;
import combat.Weapon;
import combat.Character;
import game.IEventSchedulingService;
import game.EventSchedulingService;
import combat.CombatCoordinator;
import data.SkillsManager;
import utils.GameConstants;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Singleton manager for weapon state transitions and hold states.
 * Handles weapon state progression, hold states, and firing preferences.
 */
public class WeaponStateManager implements IWeaponStateManager {
    
    private static WeaponStateManager instance;
    
    // Per-character state tracking
    private final Map<Integer, String> weaponHoldStates = new HashMap<>();
    private final Map<Integer, String> targetHoldStates = new HashMap<>();
    private final Map<Integer, Boolean> firesFromAimingState = new HashMap<>();
    
    // Service references
    private final IEventSchedulingService eventSchedulingService;
    
    /**
     * Private constructor for singleton pattern.
     */
    private WeaponStateManager() {
        this.eventSchedulingService = EventSchedulingService.getInstance();
    }
    
    /**
     * Get the singleton instance of WeaponStateManager.
     * 
     * @return The manager instance
     */
    public static WeaponStateManager getInstance() {
        if (instance == null) {
            instance = new WeaponStateManager();
        }
        return instance;
    }
    
    @Override
    public long scheduleStateTransition(Character character, WeaponState fromState, WeaponState toState, long currentTick) {
        if (fromState == null || toState == null) {
            throw new IllegalArgumentException("States cannot be null");
        }
        
        // Calculate transition duration
        long duration = calculateTransitionDuration(character, fromState, toState);
        long transitionTick = currentTick + duration;
        
        // Schedule the transition
        eventSchedulingService.scheduleEvent(transitionTick, () -> {
            character.currentWeaponState = toState;
            
            // Start timing if transitioning to aiming/pointing states
            if ("aiming".equals(toState.getState())) {
                // TODO: Call aiming system through coordinator
                // CombatCoordinator.getInstance().startAimingTiming(character.id, transitionTick);
            } else if ("pointedfromhip".equals(toState.getState())) {
                // TODO: Call aiming system through coordinator
                // CombatCoordinator.getInstance().startPointingFromHipTiming(character.id, transitionTick);
            }
        }, character.id);
        
        return transitionTick;
    }
    
    @Override
    public boolean isWeaponPreparationState(WeaponState state) {
        if (state == null) {
            return false;
        }
        
        String stateName = state.getState();
        return "drawing".equals(stateName) || 
               "unsheathing".equals(stateName) || 
               "unsling".equals(stateName) || 
               "ready".equals(stateName) || 
               "melee_ready".equals(stateName);
    }
    
    @Override
    public String getWeaponHoldState(int characterId) {
        return weaponHoldStates.getOrDefault(characterId, "aiming");
    }
    
    @Override
    public void setWeaponHoldState(int characterId, String holdState) {
        if (holdState != null) {
            weaponHoldStates.put(characterId, holdState);
        } else {
            weaponHoldStates.remove(characterId);
        }
    }
    
    @Override
    public String cycleWeaponHoldState(Character character, Weapon weapon) {
        if (weapon == null) {
            return null;
        }
        
        // Get available hold states (exclude firing, recovering, reloading)
        List<String> availableStates = getAvailableHoldStates(weapon);
        if (availableStates.isEmpty()) {
            return getWeaponHoldState(character.id);
        }
        
        String currentHoldState = getWeaponHoldState(character.id);
        
        // Find current hold state index
        int currentIndex = availableStates.indexOf(currentHoldState);
        if (currentIndex == -1) {
            // Current hold state not found, default to first available
            String newState = availableStates.get(0);
            setWeaponHoldState(character.id, newState);
            return newState;
        } else {
            // Cycle to next state (wrap around)
            int nextIndex = (currentIndex + 1) % availableStates.size();
            String newState = availableStates.get(nextIndex);
            setWeaponHoldState(character.id, newState);
            return newState;
        }
    }
    
    @Override
    public boolean getFiresFromAimingState(int characterId) {
        return firesFromAimingState.getOrDefault(characterId, true);
    }
    
    @Override
    public void setFiresFromAimingState(int characterId, boolean firesFromAiming) {
        firesFromAimingState.put(characterId, firesFromAiming);
    }
    
    @Override
    public boolean toggleFiringPreference(int characterId) {
        boolean currentPreference = getFiresFromAimingState(characterId);
        boolean newPreference = !currentPreference;
        setFiresFromAimingState(characterId, newPreference);
        return newPreference;
    }
    
    @Override
    public String getTargetHoldState(int characterId) {
        return targetHoldStates.get(characterId);
    }
    
    @Override
    public void setTargetHoldState(int characterId, String targetState) {
        if (targetState != null) {
            targetHoldStates.put(characterId, targetState);
        } else {
            targetHoldStates.remove(characterId);
        }
    }
    
    @Override
    public boolean shouldStopAtState(Character character, WeaponState currentState, String targetState) {
        if (currentState == null || targetState == null) {
            return false;
        }
        
        String currentStateName = currentState.getState();
        
        // Check if we've reached the target state
        if (currentStateName.equals(targetState)) {
            return true;
        }
        
        // Check if we should stop based on firing preference
        boolean prefersAiming = getFiresFromAimingState(character.id);
        
        // If target is "firing" but preference says stop at intermediate state
        if ("firing".equals(targetState)) {
            if (!prefersAiming && "pointedfromhip".equals(currentStateName)) {
                return true; // Stop at point-from-hip for hip shooters
            }
        }
        
        // Check hold state
        String holdState = getWeaponHoldState(character.id);
        if (holdState != null && holdState.equals(currentStateName)) {
            return true; // Stop at hold state
        }
        
        return false;
    }
    
    @Override
    public long calculateTransitionDuration(Character character, WeaponState fromState, WeaponState toState) {
        if (fromState == null || toState == null) {
            return 0;
        }
        
        // Base duration from state data
        long baseDuration = fromState.ticks;
        
        // Apply speed multiplier only to weapon preparation states
        if (isWeaponPreparationState(fromState)) {
            double speedMultiplier = calculateWeaponReadySpeedMultiplier(character);
            return Math.round(baseDuration * speedMultiplier);
        }
        
        return baseDuration;
    }
    
    @Override
    public void cleanupCharacter(int characterId) {
        weaponHoldStates.remove(characterId);
        targetHoldStates.remove(characterId);
        firesFromAimingState.remove(characterId);
    }
    
    // Private helper methods
    
    /**
     * Get available hold states for a weapon.
     */
    private List<String> getAvailableHoldStates(Weapon weapon) {
        List<String> availableStates = new ArrayList<>();
        if (weapon == null || weapon.getStates() == null) {
            return availableStates;
        }
        
        for (WeaponState state : weapon.getStates()) {
            String stateName = state.getState();
            // Exclude post-firing states
            if (!stateName.equals("firing") && 
                !stateName.equals("recovering") && 
                !stateName.equals("reloading")) {
                availableStates.add(stateName);
            }
        }
        
        return availableStates;
    }
    
    /**
     * Calculate weapon ready speed multiplier based on reflexes and quickdraw skill.
     */
    private double calculateWeaponReadySpeedMultiplier(Character character) {
        // Get reflex modifier (-20 to +20)
        int reflexesModifier = GameConstants.statToModifier(character.reflexes);
        // Convert to speed multiplier (1.3x slower to 0.7x faster)
        double reflexesSpeedMultiplier = 1.0 - (reflexesModifier * 0.015);
        
        // Get quickdraw skill level
        int quickdrawLevel = character.getSkillLevel(SkillsManager.QUICKDRAW);
        // Each level provides 8% speed improvement
        double quickdrawSpeedMultiplier = 1.0 - (quickdrawLevel * 0.08);
        
        // Combine multipliers
        return reflexesSpeedMultiplier * quickdrawSpeedMultiplier;
    }
}
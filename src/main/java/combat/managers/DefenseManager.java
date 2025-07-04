package combat.managers;

import combat.DefenseState;
import combat.Character;
import combat.MeleeWeapon;
import combat.Weapon;
import game.IEventSchedulingService;
import game.EventSchedulingService;
import game.interfaces.IUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Singleton manager for defensive mechanics including blocking and counter-attacks.
 * Handles defense states, cooldowns, and counter-attack opportunities.
 * 
 * DevCycle 40: Enhanced with new defense calculation system
 * - Defense eligibility based on nextDefenseTick
 * - Defense value calculations with stat and skill modifiers
 * - Integration with melee combat hit calculations
 */
public class DefenseManager implements IDefenseManager {
    
    private static DefenseManager instance;
    private final Random random = new Random();
    
    // Per-character state tracking
    private final Map<Integer, DefenseState> defenseStates = new HashMap<>();
    private final Map<Integer, Long> defenseCooldownEndTicks = new HashMap<>();
    private final Map<Integer, Long> counterAttackWindowEndTicks = new HashMap<>();
    private final Map<Integer, Boolean> hasCounterAttackOpportunity = new HashMap<>();
    
    // Service references
    private final IEventSchedulingService eventSchedulingService;
    
    /**
     * Private constructor for singleton pattern.
     */
    private DefenseManager() {
        this.eventSchedulingService = EventSchedulingService.getInstance();
    }
    
    /**
     * Get the singleton instance of DefenseManager.
     * 
     * @return The manager instance
     */
    public static DefenseManager getInstance() {
        if (instance == null) {
            instance = new DefenseManager();
        }
        return instance;
    }
    
    @Override
    public DefenseState getDefenseState(int characterId) {
        return defenseStates.getOrDefault(characterId, DefenseState.READY);
    }
    
    @Override
    public void setDefenseState(int characterId, DefenseState state) {
        if (state != null) {
            defenseStates.put(characterId, state);
        } else {
            defenseStates.remove(characterId);
        }
    }
    
    @Override
    public boolean canDefend(Character character, long currentTick) {
        int characterId = character.id;
        DefenseState state = getDefenseState(characterId);
        long cooldownEnd = getDefenseCooldownEndTick(characterId);
        
        // Update state if cooldown expired
        if (state == DefenseState.COOLDOWN && currentTick >= cooldownEnd) {
            setDefenseState(characterId, DefenseState.READY);
            state = DefenseState.READY;
        }
        
        return state == DefenseState.READY && currentTick >= cooldownEnd;
    }
    
    @Override
    public boolean attemptBlock(Character defender, IUnit attacker, long currentTick) {
        // DevCycle 33: System 10 - Check configuration for defensive blocking disable
        if (config.DebugConfig.getInstance().isDefensiveBlockingDisabled()) {
            return false;
        }
        
        // ORIGINAL BLOCKING LOGIC - RESTORED FOR CONFIGURATION CONTROL
        if (!canDefend(defender, currentTick)) {
            return false;
        }
        
        // Set to defending state
        setDefenseState(defender.id, DefenseState.DEFENDING);
        
        // TODO: Implement actual block mechanics
        // This would involve checking weapon defensive capabilities,
        // skill checks, and determining if the block was successful
        boolean blockSuccessful = performBlockCalculation(defender, attacker);
        
        // Start cooldown after defense attempt
        int cooldownDuration = getDefenseCooldownDuration(defender);
        setDefenseCooldown(defender.id, currentTick + cooldownDuration);
        
        // Update statistics
        updateDefenseStatistics(defender.id, blockSuccessful);
        
        // If block was successful, grant counter-attack opportunity
        if (blockSuccessful) {
            int counterWindowDuration = getCounterAttackWindowDuration(defender);
            setCounterAttackWindow(defender.id, currentTick + counterWindowDuration);
            setHasCounterAttackOpportunity(defender.id, true);
        }
        
        return blockSuccessful;
    }
    
    @Override
    public void setDefenseCooldown(int characterId, long cooldownEndTick) {
        defenseCooldownEndTicks.put(characterId, cooldownEndTick);
        setDefenseState(characterId, DefenseState.COOLDOWN);
    }
    
    @Override
    public long getDefenseCooldownEndTick(int characterId) {
        return defenseCooldownEndTicks.getOrDefault(characterId, -1L);
    }
    
    @Override
    public boolean isInDefenseCooldown(int characterId, long currentTick) {
        long cooldownEnd = getDefenseCooldownEndTick(characterId);
        return cooldownEnd > 0 && currentTick < cooldownEnd;
    }
    
    @Override
    public void setCounterAttackWindow(int characterId, long windowEndTick) {
        counterAttackWindowEndTicks.put(characterId, windowEndTick);
    }
    
    @Override
    public long getCounterAttackWindowEndTick(int characterId) {
        return counterAttackWindowEndTicks.getOrDefault(characterId, -1L);
    }
    
    @Override
    public boolean hasCounterAttackOpportunity(int characterId, long currentTick) {
        Boolean hasOpportunity = hasCounterAttackOpportunity.get(characterId);
        if (hasOpportunity == null || !hasOpportunity) {
            return false;
        }
        
        long windowEnd = getCounterAttackWindowEndTick(characterId);
        if (windowEnd > 0 && currentTick >= windowEnd) {
            // Window expired
            setHasCounterAttackOpportunity(characterId, false);
            return false;
        }
        
        return true;
    }
    
    @Override
    public void setHasCounterAttackOpportunity(int characterId, boolean hasOpportunity) {
        if (hasOpportunity) {
            hasCounterAttackOpportunity.put(characterId, true);
        } else {
            hasCounterAttackOpportunity.remove(characterId);
            counterAttackWindowEndTicks.remove(characterId);
        }
    }
    
    @Override
    public boolean executeCounterAttack(Character defender, IUnit attacker, long currentTick) {
        if (!hasCounterAttackOpportunity(defender.id, currentTick)) {
            return false;
        }
        
        // Clear counter-attack opportunity
        setHasCounterAttackOpportunity(defender.id, false);
        
        // TODO: Implement actual counter-attack execution
        // This would involve initiating a melee attack against the attacker
        boolean counterSuccessful = performCounterAttack(defender, attacker, currentTick);
        
        // Update statistics
        updateCounterAttackStatistics(defender.id, counterSuccessful);
        
        // Start defense cooldown after counter-attack
        int cooldownDuration = getDefenseCooldownDuration(defender);
        setDefenseCooldown(defender.id, currentTick + cooldownDuration);
        
        return counterSuccessful;
    }
    
    @Override
    public void updateDefenseStatistics(int characterId, boolean isSuccessful) {
        // This would typically delegate to CombatStatisticsManager
        // For now, this is a placeholder
        // TODO: Integrate with CombatStatisticsManager
    }
    
    @Override
    public void updateCounterAttackStatistics(int characterId, boolean isSuccessful) {
        // This would typically delegate to CombatStatisticsManager
        // For now, this is a placeholder
        // TODO: Integrate with CombatStatisticsManager
    }
    
    @Override
    public void cleanupCharacter(int characterId) {
        defenseStates.remove(characterId);
        defenseCooldownEndTicks.remove(characterId);
        counterAttackWindowEndTicks.remove(characterId);
        hasCounterAttackOpportunity.remove(characterId);
    }
    
    // Private helper methods
    
    /**
     * Perform block calculation to determine if defense was successful.
     * TODO: Implement actual block mechanics based on weapon, skills, etc.
     */
    private boolean performBlockCalculation(Character defender, IUnit attacker) {
        // Placeholder implementation
        // In full implementation, this would check:
        // - Weapon defensive capabilities
        // - Defender's defensive skills
        // - Attacker's offensive skills
        // - Random factors
        return Math.random() < 0.5; // 50% chance for now
    }
    
    /**
     * Perform counter-attack against the attacker.
     * TODO: Implement actual counter-attack mechanics.
     */
    private boolean performCounterAttack(Character defender, IUnit attacker, long currentTick) {
        // Placeholder implementation
        // In full implementation, this would:
        // - Initiate a melee attack
        // - Apply counter-attack modifiers
        // - Check for success
        return Math.random() < 0.7; // 70% chance for now
    }
    
    /**
     * Get defense cooldown duration for a character.
     * TODO: Get from weapon's defensive properties.
     */
    private int getDefenseCooldownDuration(Character character) {
        // Placeholder - would get from weapon's defenseCooldown property
        return 60; // 1 second default
    }
    
    /**
     * Get counter-attack window duration for a character.
     * TODO: Get from weapon or skill properties.
     */
    private int getCounterAttackWindowDuration(Character character) {
        // Placeholder - would be based on weapon/skill
        return 30; // 0.5 second default
    }
    
    // ========== DEVCYCLE 40: NEW DEFENSE SYSTEM ==========
    
    /**
     * Check if a character can defend against an attack
     * DevCycle 40: Uses nextDefenseTick for timing
     * 
     * @param defender The defending character
     * @param currentTick Current game tick
     * @return true if the character can defend
     */
    public boolean canDefendAgainstAttack(Character defender, long currentTick) {
        // Check if character is incapacitated
        if (defender.isIncapacitated()) {
            return false;
        }
        
        // Check if defense is on cooldown
        if (currentTick < defender.getNextDefenseTick()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Calculate defense value for a character
     * DevCycle 40: Implements the defense calculation formula
     * 
     * @param defender The defending character
     * @return The total defense value to apply as negative modifier
     */
    public int calculateDefenseValue(Character defender) {
        // Base roll: 1-50
        int baseRoll = random.nextInt(50) + 1;
        
        // Dexterity modifier
        int dexterityModifier = utils.GameConstants.statToModifier(defender.getDexterity());
        
        // Melee weapon skill bonus (+5 per skill level)
        int skillBonus = 0;
        Weapon currentWeapon = defender.isMeleeCombatMode ? defender.meleeWeapon : defender.weapon;
        if (currentWeapon != null) {
            // Get skill for current weapon type
            String skillName = getWeaponSkillName(currentWeapon);
            int skillLevel = CharacterSkillsManager.getInstance().getSkillLevel(defender.id, skillName);
            skillBonus = skillLevel * 5;
        }
        
        // Weapon defense bonus
        int weaponDefenseBonus = 0;
        if (currentWeapon != null) {
            weaponDefenseBonus = currentWeapon.getDefenseBonus();
        }
        
        // Calculate total
        int totalDefense = baseRoll + dexterityModifier + skillBonus + weaponDefenseBonus;
        
        // Debug output
        if (config.DebugConfig.getInstance().isCombatDebugEnabled()) {
            System.out.println(String.format("[DEFENSE] %s defends: roll(%d) + dex(%d) + skill(%d) + weapon(%d) = total(%d)",
                defender.getDisplayName(), baseRoll, dexterityModifier, skillBonus, weaponDefenseBonus, totalDefense));
        }
        
        // Update statistics
        defender.defensiveAttempts++;
        
        return totalDefense;
    }
    
    /**
     * Apply defense to an attack and update cooldown
     * DevCycle 40: Main entry point for defense system
     * 
     * @param defender The defending character
     * @param currentTick Current game tick
     * @return Defense value to apply as negative modifier to attack
     */
    public int performDefense(Character defender, long currentTick) {
        if (!canDefendAgainstAttack(defender, currentTick)) {
            return 0;
        }
        
        // Calculate defense value
        int defenseValue = calculateDefenseValue(defender);
        
        // Update next defense tick (current + 60)
        defender.setNextDefenseTick(currentTick + 60);
        
        // Track successful defense
        if (defenseValue > 0) {
            defender.defensiveSuccesses++;
        }
        
        return defenseValue;
    }
    
    /**
     * Get the appropriate skill name for a weapon
     * 
     * @param weapon The weapon to check
     * @return The skill name for that weapon type
     */
    private String getWeaponSkillName(Weapon weapon) {
        if (weapon instanceof MeleeWeapon) {
            MeleeWeapon meleeWeapon = (MeleeWeapon) weapon;
            switch (meleeWeapon.getMeleeType()) {
                case SHORT:
                    return "Knife"; // Short weapons use knife skill
                case MEDIUM:
                    return "Sword"; // Medium weapons use sword skill
                case LONG:
                    return "Spear"; // Long weapons use spear skill
                case UNARMED:
                    return ""; // No weapon skill for unarmed
                case TWO_WEAPON:
                    return "Sword"; // Two weapon fighting uses sword skill
                default:
                    return "Melee"; // Generic melee skill fallback
            }
        }
        
        // For ranged weapons being used defensively
        switch (weapon.getWeaponType()) {
            case PISTOL:
                return "Pistol";
            case RIFLE:
                return "Rifle";
            default:
                return ""; // No skill bonus for other weapon types
        }
    }
}
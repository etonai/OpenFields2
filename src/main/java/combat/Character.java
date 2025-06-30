package combat;

import combat.interfaces.ICharacter;
import combat.managers.BurstFireManager;
import combat.managers.AttackSequenceManager;
import combat.managers.WeaponStateTransitionManager;
import combat.managers.MeleeCombatSequenceManager;
import combat.managers.ReactionManager;
import combat.managers.WeaponReadinessManager;
import combat.managers.AttackContinuationManager;
import combat.managers.FiringSequenceManager;
import combat.managers.MultiShotManager;
import combat.managers.AimingSystem;
import combat.managers.DefenseManager;
import combat.managers.WeaponStateManager;
import combat.managers.ReloadManager;
import combat.managers.CharacterSkillsManager;
import combat.managers.CharacterStatsManager;
import combat.managers.TargetManager;
import game.ScheduledEvent;
import game.Unit;
import game.interfaces.IUnit;
import game.GameCallbacks;
import data.SkillsManager;
import utils.GameConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class Character implements ICharacter {
    
    // IDENTITY AND BASIC ATTRIBUTES
    
    /** Character unique identifier */
    public int id;
    
    /** Value objects for organized data access */
    public CharacterIdentity identity;
    public CharacterStats stats;
    
    /** Character display name and identity */
    public String nickname;
    public String firstName;
    public String lastName;
    public Date birthdate;
    public String themeId;
    
    /** Core character attributes and current state */
    public int dexterity;
    public int currentDexterity;
    public int health;
    public int currentHealth;
    public int coolness;
    public int strength;
    public int reflexes;
    
    /** Physical characteristics */
    public Handedness handedness;
    public double baseMovementSpeed;
    
    // MOVEMENT AND POSITIONING STATE
    
    /** Current movement configuration */
    public MovementType currentMovementType;
    public AimingSpeed currentAimingSpeed;
    public PositionState currentPosition;
    
    // WEAPON AND COMBAT STATE
    
    /** Weapon management */
    public Weapon weapon; // Legacy field - will become rangedWeapon
    public RangedWeapon rangedWeapon; // Primary ranged weapon
    public MeleeWeapon meleeWeapon; // Primary melee weapon
    public boolean isMeleeCombatMode = false; // True when in melee combat mode
    public WeaponState currentWeaponState;
    
    /** Combat targeting and state */
    public IUnit currentTarget;
    public boolean persistentAttack;
    public boolean isAttacking;
    public boolean isDefensiveAiming = false;
    
    /** Weapon hold state for targeting control - DEPRECATED: Now managed by WeaponStateManager */
    // public String weaponHoldState = "aiming"; // Default hold state
    // public String targetHoldState = null; // Target state for hold state progression
    
    /** Firing preference - DEPRECATED: Now managed by WeaponStateManager */
    // public boolean firesFromAimingState = true; // Default to aiming
    
    /** Multiple shot control (DevCycle 28) */
    public int multipleShootCount = 1; // Number of shots to fire in sequence (1-5)
    public int currentShotInSequence = 0; // Current shot number during multiple shot execution
    
    /** Reaction action system (DevCycle 28) */
    public IUnit reactionTarget = null; // Target being monitored for weapon state changes
    public WeaponState reactionBaselineState = null; // Initial weapon state when reaction was set
    public long reactionTriggerTick = -1; // Tick when reaction should execute (-1 = not triggered)
    
    /** Aiming duration tracking (DevCycle 27) - Now managed by AimingSystem */
    // aimingStartTick and pointingFromHipStartTick removed - managed by AimingSystem
    
    /** Character configuration */
    public int faction;
    public boolean usesAutomaticTargeting;
    public FiringMode preferredFiringMode;
    
    // SKILLS AND WOUNDS
    
    /** Character skills and abilities - DevCycle 30: Now managed by CharacterSkillsManager, but kept for compatibility */
    public List<Skill> skills;
    
    /** Current wounds and injuries */
    public List<Wound> wounds;
    
    // DEBUG AND SYSTEM STATE
    
    /** Auto-targeting debug throttling (moved to CharacterDebugUtils) */
    public long lastAutoTargetDebugTick = -1;
    
    // COMBAT STATISTICS TRACKING
    
    /** General combat experience tracking */
    public int combatEngagements = 0;           // Manual tracking (no auto-update yet)
    public int woundsReceived = 0;              // Auto-updated when addWound() called
    
    /** General attack statistics */
    public int attacksAttempted = 0;            // Auto-updated when attacks are attempted
    public int attacksSuccessful = 0;           // Auto-updated when attacks hit
    public int targetsIncapacitated = 0;        // Auto-updated when targets become incapacitated
    
    /** Wound infliction by severity */
    public int woundsInflictedScratch = 0;      // Auto-updated on successful hits
    public int woundsInflictedLight = 0;        // Auto-updated on successful hits  
    public int woundsInflictedSerious = 0;      // Auto-updated on successful hits
    public int woundsInflictedCritical = 0;     // Auto-updated on successful hits
    
    /** Ranged combat tracking (DevCycle 12) */
    public int rangedAttacksAttempted = 0;      // Auto-updated when ranged attacks are attempted
    public int rangedAttacksSuccessful = 0;     // Auto-updated when ranged attacks hit
    public int rangedWoundsInflicted = 0;       // Auto-updated when ranged attacks cause wounds
    
    /** Melee combat tracking (DevCycle 12) */
    public int meleeAttacksAttempted = 0;       // Auto-updated when melee attacks are attempted
    public int meleeAttacksSuccessful = 0;      // Auto-updated when melee attacks hit
    public int meleeWoundsInflicted = 0;        // Auto-updated when melee attacks cause wounds
    
    /** Headshot statistics */
    public int headshotsAttempted = 0;          // Auto-updated when attacks target the head
    public int headshotsSuccessful = 0;         // Auto-updated when headshots hit
    public int headshotsKills = 0;              // Auto-updated when headshots result in kills
    
    /** Battle outcome statistics */
    public int battlesParticipated = 0;         // Manual tracking - updated after battles
    public int victories = 0;                   // Manual tracking - updated after victories
    public int defeats = 0;                     // Manual tracking - updated after defeats
    
    /** Defensive statistics (DevCycle 23) */
    public int defensiveAttempts = 0;           // Auto-updated when defense is attempted
    public int defensiveSuccesses = 0;          // Auto-updated when defense succeeds
    public int counterAttacksExecuted = 0;      // Auto-updated when counter-attacks are performed
    public int counterAttacksSuccessful = 0;    // Auto-updated when counter-attacks hit
    
    // AUTOMATIC FIRING STATE
    
    // Burst and full-auto firing state - now managed by BurstFireManager
    
    /** Attack scheduling prevention (prevents duplicates) */
    public long lastAttackScheduledTick = -1;   // Tick when last attack sequence was scheduled
    public long lastFiringScheduledTick = -1;   // Tick when last firing event was scheduled
    public long lastContinueAttackTick = -1;    // Tick when last continue attack was scheduled
    
    /** Weapon state management */
    public boolean isReloading = false;         // Currently reloading weapon (prevents duplicate reloads)
    public AimingSpeed savedAimingSpeed = null; // Saved aiming speed for first shot in burst/auto
    
    // HESITATION AND RECOVERY SYSTEMS
    
    /** Melee attack recovery (Bug #1 fix) */
    public long lastMeleeAttackTick = -1;       // Tick when last melee attack was executed
    public long meleeRecoveryEndTick = -1;      // Tick when melee recovery period ends
    
    /** Hesitation state management */
    public boolean isHesitating = false;        // Currently hesitating due to wound
    public long hesitationEndTick = 0;          // When hesitation will end
    public List<ScheduledEvent> pausedEvents = new ArrayList<>(); // Events paused during hesitation
    
    /** Bravery check system */
    public int braveryCheckFailures = 0;        // Number of active bravery check failures
    public long braveryPenaltyEndTick = 0;      // When bravery penalty will end
    
    /** Hesitation tracking for display and statistics */
    public long totalWoundHesitationTicks = 0;   // Total hesitation time from wounds
    public long totalBraveryHesitationTicks = 0; // Total hesitation time from bravery failures
    
    // AI TARGETING AND MOVEMENT STATE
    
    /** Automatic targeting system */
    public java.awt.Rectangle targetZone = null; // Target zone rectangle in world coordinates
    public Double lastTargetFacing = null; // Last direction character was aiming (degrees)
    
    /** First attack penalty system - track target changes for accuracy penalty */
    public IUnit previousTarget = null; // Track previous target to detect target changes
    public boolean isFirstAttackOnTarget = true; // True if this is the first attack on current target
    
    /** Melee movement state tracking */
    public boolean isMovingToMelee = false; // Currently moving to engage target in melee combat
    public IUnit meleeTarget = null; // Target unit for melee attack (maintained during movement)
    public long lastMeleeMovementUpdate = 0; // Last tick when melee movement was updated (for throttling)
    
    // DEFENSE SYSTEM STATE (DevCycle 23)
    // Defense state is now managed by DefenseManager

    // Legacy constructors for backwards compatibility with tests
    public Character(String nickname, int dexterity, int health, int coolness, int strength, int reflexes, Handedness handedness) {
        this.id = 0;
        this.nickname = nickname;
        this.firstName = nickname;
        this.lastName = "";
        this.birthdate = new Date();
        this.themeId = "test_theme";
        this.dexterity = dexterity;
        this.currentDexterity = dexterity; // Initialize current to base value
        this.health = health;
        this.currentHealth = health; // Initialize current to base value
        this.coolness = coolness;
        this.strength = strength;
        this.reflexes = reflexes;
        this.handedness = handedness;
        this.baseMovementSpeed = 42.0;
        this.currentMovementType = MovementType.RUN;
        this.currentAimingSpeed = AimingSpeed.NORMAL;
        this.currentPosition = PositionState.STANDING;
        this.persistentAttack = false;
        this.isAttacking = false;
        this.faction = 1; // Default faction
        this.usesAutomaticTargeting = false; // Default to manual targeting
        this.preferredFiringMode = FiringMode.SINGLE_SHOT; // Default firing mode
        this.skills = new ArrayList<>(); // Kept for compatibility
        this.wounds = new ArrayList<>();
        initializeDefaultWeapons();
    }

    public Character(String nickname, int dexterity, int health, int coolness, int strength, int reflexes, Handedness handedness, Weapon weapon) {
        this(nickname, dexterity, health, coolness, strength, reflexes, handedness);
        this.weapon = weapon;
        initializeDefaultWeapons();
    }

    public Character(String nickname, int dexterity, int health, int coolness, int strength, int reflexes, Handedness handedness, List<Skill> skills) {
        this(nickname, dexterity, health, coolness, strength, reflexes, handedness);
        if (skills != null) {
            CharacterSkillsManager.getInstance().setSkills(this.id, skills);
        }
        initializeDefaultWeapons();
    }

    public Character(String nickname, int dexterity, int health, int coolness, int strength, int reflexes, Handedness handedness, Weapon weapon, List<Skill> skills) {
        this(nickname, dexterity, health, coolness, strength, reflexes, handedness);
        this.weapon = weapon;
        if (skills != null) {
            CharacterSkillsManager.getInstance().setSkills(this.id, skills);
        }
        initializeDefaultWeapons();
    }

    public Character(int id, String nickname, String firstName, String lastName, Date birthdate, String themeId, int dexterity, int health, int coolness, int strength, int reflexes, Handedness handedness) {
        this.id = id;
        this.nickname = nickname;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthdate = birthdate;
        this.themeId = themeId;
        this.dexterity = dexterity;
        this.currentDexterity = dexterity; // Initialize current to base value
        this.health = health;
        this.currentHealth = health; // Initialize current to base value
        this.coolness = coolness;
        this.strength = strength;
        this.reflexes = reflexes;
        this.handedness = handedness;
        this.baseMovementSpeed = 42.0;
        this.currentMovementType = MovementType.RUN;
        this.currentAimingSpeed = AimingSpeed.NORMAL;
        this.currentPosition = PositionState.STANDING;
        this.persistentAttack = false;
        this.isAttacking = false;
        this.faction = 1; // Default faction
        this.usesAutomaticTargeting = false; // Default to manual targeting
        this.preferredFiringMode = FiringMode.SINGLE_SHOT; // Default firing mode
        this.skills = new ArrayList<>(); // Kept for compatibility
        this.wounds = new ArrayList<>();
        initializeDefaultWeapons();
    }

    public Character(int id, String nickname, String firstName, String lastName, Date birthdate, String themeId, int dexterity, int health, int coolness, int strength, int reflexes, Handedness handedness, Weapon weapon) {
        this.id = id;
        this.nickname = nickname;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthdate = birthdate;
        this.themeId = themeId;
        this.dexterity = dexterity;
        this.health = health;
        this.coolness = coolness;
        this.strength = strength;
        this.reflexes = reflexes;
        this.handedness = handedness;
        this.weapon = weapon;
        this.baseMovementSpeed = 42.0;
        this.currentMovementType = MovementType.RUN;
        this.currentAimingSpeed = AimingSpeed.NORMAL;
        this.currentPosition = PositionState.STANDING;
        this.persistentAttack = false;
        this.isAttacking = false;
        this.faction = 1; // Default faction
        this.usesAutomaticTargeting = false; // Default to manual targeting
        this.preferredFiringMode = FiringMode.SINGLE_SHOT; // Default firing mode
        this.skills = new ArrayList<>(); // Kept for compatibility
        this.wounds = new ArrayList<>();
        initializeDefaultWeapons();
    }
    
    public Character(int id, String nickname, String firstName, String lastName, Date birthdate, String themeId, int dexterity, int health, int coolness, int strength, int reflexes, Handedness handedness, List<Skill> skills) {
        this.id = id;
        this.nickname = nickname;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthdate = birthdate;
        this.themeId = themeId;
        this.dexterity = dexterity;
        this.health = health;
        this.coolness = coolness;
        this.strength = strength;
        this.reflexes = reflexes;
        this.handedness = handedness;
        this.baseMovementSpeed = 42.0;
        this.currentMovementType = MovementType.RUN;
        this.currentAimingSpeed = AimingSpeed.NORMAL;
        this.currentPosition = PositionState.STANDING;
        this.persistentAttack = false;
        this.isAttacking = false;
        this.faction = 1; // Default faction
        this.usesAutomaticTargeting = false; // Default to manual targeting
        if (skills != null) {
            CharacterSkillsManager.getInstance().setSkills(this.id, skills);
        }
        this.wounds = new ArrayList<>();
        initializeDefaultWeapons();
    }
    
    public Character(int id, String nickname, String firstName, String lastName, Date birthdate, String themeId, int dexterity, int health, int coolness, int strength, int reflexes, Handedness handedness, Weapon weapon, List<Skill> skills) {
        this.id = id;
        this.nickname = nickname;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthdate = birthdate;
        this.themeId = themeId;
        this.dexterity = dexterity;
        this.health = health;
        this.coolness = coolness;
        this.strength = strength;
        this.reflexes = reflexes;
        this.handedness = handedness;
        this.weapon = weapon;
        this.baseMovementSpeed = 42.0;
        this.currentMovementType = MovementType.RUN;
        this.currentAimingSpeed = AimingSpeed.NORMAL;
        this.currentPosition = PositionState.STANDING;
        this.persistentAttack = false;
        this.isAttacking = false;
        this.faction = 1; // Default faction
        this.usesAutomaticTargeting = false; // Default to manual targeting
        if (skills != null) {
            CharacterSkillsManager.getInstance().setSkills(this.id, skills);
        }
        this.wounds = new ArrayList<>();
        initializeDefaultWeapons();
    }

    // ICharacter interface implementation - Basic Attributes
    
    @Override
    public int getId() {
        return id;
    }
    
    @Override
    public void setId(int id) {
        this.id = id;
    }
    
    @Override
    public String getNickname() {
        return nickname;
    }
    
    @Override
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    @Override
    public String getFirstName() {
        return firstName;
    }
    
    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    @Override
    public String getLastName() {
        return lastName;
    }
    
    @Override
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    @Override
    public Date getBirthdate() {
        return birthdate;
    }
    
    @Override
    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }
    
    @Override
    public String getThemeId() {
        return themeId;
    }
    
    @Override
    public void setThemeId(String themeId) {
        this.themeId = themeId;
    }
    
    // ICharacter interface implementation - Physical Attributes
    
    @Override
    public int getDexterity() {
        return dexterity;
    }
    
    @Override
    public void setDexterity(int dexterity) {
        this.dexterity = dexterity;
    }
    
    @Override
    public int getCurrentDexterity() {
        return currentDexterity;
    }
    
    @Override
    public void setCurrentDexterity(int currentDexterity) {
        this.currentDexterity = currentDexterity;
    }
    
    @Override
    public int getHealth() {
        return health;
    }
    
    @Override
    public void setHealth(int health) {
        this.health = health;
    }
    
    @Override
    public int getCurrentHealth() {
        return currentHealth;
    }
    
    @Override
    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }
    
    @Override
    public int getCoolness() {
        return coolness;
    }
    
    @Override
    public void setCoolness(int coolness) {
        this.coolness = coolness;
    }
    
    @Override
    public int getStrength() {
        return strength;
    }
    
    @Override
    public void setStrength(int strength) {
        this.strength = strength;
    }
    
    @Override
    public int getReflexes() {
        return reflexes;
    }
    
    @Override
    public void setReflexes(int reflexes) {
        this.reflexes = reflexes;
    }
    
    @Override
    public Handedness getHandedness() {
        return handedness;
    }
    
    @Override
    public void setHandedness(Handedness handedness) {
        this.handedness = handedness;
    }
    
    // ICharacter interface implementation - Movement and Positioning
    
    @Override
    public double getBaseMovementSpeed() {
        return baseMovementSpeed;
    }
    
    @Override
    public void setBaseMovementSpeed(double speed) {
        this.baseMovementSpeed = speed;
    }
    
    @Override
    public MovementType getCurrentMovementType() {
        return currentMovementType;
    }
    
    @Override
    public void setCurrentMovementType(MovementType type) {
        this.currentMovementType = type;
    }
    
    @Override
    public AimingSpeed getCurrentAimingSpeed() {
        return currentAimingSpeed;
    }
    
    @Override
    public void setCurrentAimingSpeed(AimingSpeed speed) {
        this.currentAimingSpeed = speed;
    }
    
    @Override
    public PositionState getCurrentPosition() {
        return currentPosition;
    }
    
    @Override
    public void setCurrentPosition(PositionState position) {
        this.currentPosition = position;
    }
    
    // ICharacter interface implementation - Weapons
    
    @Override
    public Weapon getWeapon() {
        return weapon;
    }
    
    @Override
    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
        resetWeaponHoldStateToDefault();
    }
    
    @Override
    public RangedWeapon getRangedWeapon() {
        return rangedWeapon;
    }
    
    @Override
    public void setRangedWeapon(RangedWeapon weapon) {
        this.rangedWeapon = weapon;
        resetWeaponHoldStateToDefault();
    }
    
    @Override
    public MeleeWeapon getMeleeWeapon() {
        return meleeWeapon;
    }
    
    @Override
    public void setMeleeWeapon(MeleeWeapon weapon) {
        this.meleeWeapon = weapon;
        resetWeaponHoldStateToDefault();
    }
    
    @Override
    public boolean isMeleeCombatMode() {
        return isMeleeCombatMode;
    }
    
    @Override
    public void setMeleeCombatMode(boolean melee) {
        this.isMeleeCombatMode = melee;
        resetWeaponHoldStateToDefault();
    }
    
    @Override
    public WeaponState getCurrentWeaponState() {
        return currentWeaponState;
    }
    
    @Override
    public void setCurrentWeaponState(WeaponState state) {
        this.currentWeaponState = state;
    }
    
    // Aiming duration tracking methods (DevCycle 27)
    
    /**
     * Start timing for aiming state.
     */
    public void startAimingTiming(long currentTick) {
        AimingSystem.getInstance().startAimingTiming(id, currentTick);
    }
    
    /**
     * Start timing for pointing from hip state.
     */
    public void startPointingFromHipTiming(long currentTick) {
        AimingSystem.getInstance().startPointingFromHipTiming(id, currentTick);
    }
    
    /**
     * Get duration spent in aiming state.
     */
    public long getAimingDuration(long currentTick) {
        return AimingSystem.getInstance().getAimingDuration(id, currentTick);
    }
    
    /**
     * Get duration spent in pointing from hip state.
     */
    public long getPointingFromHipDuration(long currentTick) {
        return AimingSystem.getInstance().getPointingFromHipDuration(id, currentTick);
    }
    
    /**
     * Reset all aiming timing (called when changing targets or exiting aiming states).
     */
    public void resetAimingTiming() {
        AimingSystem.getInstance().resetAimingTiming(id);
    }
    
    /**
     * Get current aiming duration based on firing preference.
     */
    public long getCurrentAimingDuration(long currentTick) {
        return AimingSystem.getInstance().getCurrentAimingDuration(this, currentTick);
    }
    
    /**
     * Get current pointing from hip duration (direct access, not preference-based).
     */
    public long getCurrentPointingFromHipDuration(long currentTick) {
        return getPointingFromHipDuration(currentTick);
    }
    
    /**
     * DevCycle 27: System 6 - Get optimal weapon state for target switching based on firing preference.
     * This prevents unnecessary regression to pointedfromhip when character prefers aiming state.
     * 
     * @return WeaponState that respects character's firing preference while allowing efficient progression
     */
    WeaponState getOptimalStateForTargetSwitch() {
        if (weapon == null) {
            return null;
        }
        
        // For characters who prefer aiming state, try to get them closer to aiming
        if (getFiresFromAimingState()) {
            // Check if weapon has aiming state available
            WeaponState aimingState = weapon.getStateByName("aiming");
            if (aimingState != null) {
                // Character prefers aiming - use aiming state directly for immediate targeting
                return aimingState;
            }
            
            // Fallback: try pointedfromhip if aiming not available
            WeaponState pointingState = weapon.getStateByName("pointedfromhip");
            if (pointingState != null) {
                return pointingState;
            }
        } else {
            // Character prefers pointedfromhip - use that state
            WeaponState pointingState = weapon.getStateByName("pointedfromhip");
            if (pointingState != null) {
                return pointingState;
            }
            
            // Fallback: try aiming if pointedfromhip not available
            WeaponState aimingState = weapon.getStateByName("aiming");
            if (aimingState != null) {
                return aimingState;
            }
        }
        
        // Final fallback: use ready state (original behavior)
        WeaponState readyState = weapon.getStateByName("ready");
        if (readyState != null) {
            return readyState;
        }
        
        // Emergency fallback: use current state if nothing else works
        return currentWeaponState;
    }
    
    /**
     * DevCycle 27: System 6 - Start timing for weapon state after target switch.
     * This ensures aiming counters begin immediately when switching to targeting states.
     * 
     * @param currentTick The current game tick
     */
    void startTimingForTargetSwitchState(long currentTick) {
        AimingSystem.getInstance().startTimingForTargetSwitchState(this, currentTick);
    }
    
    // Support methods for System 3: Accumulated Aiming Time Bonus System (DevCycle 27)
    // Note: Aiming calculation methods moved to AimingSystem manager
    
    /**
     * Find a weapon state by name from the current weapon.
     * @param stateName The name of the state to find
     * @return The WeaponState if found, null otherwise
     */
    private WeaponState findWeaponState(String stateName) {
        if (weapon != null && weapon.states != null) {
            for (WeaponState state : weapon.states) {
                if (stateName.equals(state.state)) {
                    return state;
                }
            }
        }
        return null;
    }
    
    /**
     * Get the current weapon state name.
     * @return Current weapon state name, or empty string if not available
     */
    private String getCurrentWeaponStateName() {
        return currentWeaponState != null ? currentWeaponState.state : "";
    }
    
    /**
     * Calculate accumulated aiming bonus based on time spent aiming at target.
     * DevCycle 27: System 3 - Accumulated Aiming Time Bonus System
     * 
     * @param currentTick Current game tick
     * @return The earned accumulated aiming bonus
     */
    public AccumulatedAimingBonus calculateEarnedAimingBonus(long currentTick) {
        return AimingSystem.getInstance().calculateEarnedAimingBonus(this, currentTick);
    }
    
    // Dual weapon system methods
    
    /**
     * Initialize default weapons (ranged weapon from legacy weapon field, unarmed for melee)
     */
    public void initializeDefaultWeapons() {
        // Check if we should skip weapon initialization (for platform independence)
        if (isWeaponInitializationDisabled()) {
            return;
        }
        
        // Initialize melee weapon to unarmed if not set
        if (meleeWeapon == null) {
            meleeWeapon = MeleeWeaponFactory.createUnarmed();
        }
        
        // If we have a legacy weapon, convert it to ranged weapon
        if (weapon != null && rangedWeapon == null) {
            if (weapon instanceof RangedWeapon) {
                rangedWeapon = (RangedWeapon) weapon;
            }
            // Note: weapon will remain for backward compatibility
        }
    }
    
    /**
     * Check if weapon initialization should be disabled (for platform independence)
     */
    private boolean isWeaponInitializationDisabled() {
        // Check system property for disabling weapon initialization
        String skipWeapons = System.getProperty("openfields2.skipDefaultWeapons");
        return "true".equals(skipWeapons);
    }
    
    /**
     * Get the currently active weapon based on combat mode
     */
    public Weapon getActiveWeapon() {
        if (isMeleeCombatMode && meleeWeapon != null) {
            return meleeWeapon;
        } else if (rangedWeapon != null) {
            return rangedWeapon;
        } else {
            return weapon; // Fallback to legacy weapon
        }
    }
    
    /**
     * Toggle between ranged and melee combat modes
     */
    public void toggleCombatMode() {
        // Cancel any ongoing melee movement when switching modes
        if (isMovingToMelee) {
            isMovingToMelee = false;
            meleeTarget = null;
        }
        
        // Cancel any ongoing attacks when switching modes
        if (isAttacking) {
            isAttacking = false;
        }
        
        boolean oldMode = isMeleeCombatMode;
        isMeleeCombatMode = !isMeleeCombatMode;
        
        // ALWAYS PRINT for diagnosis
        
        if (meleeWeapon != null) {
        }
        
        // Ensure character has a melee weapon when switching to melee mode
        if (isMeleeCombatMode && meleeWeapon == null) {
            meleeWeapon = MeleeWeaponFactory.createUnarmed();
        }
        
        // Initialize weapon state to melee weapon's initial state when switching to melee mode
        if (isMeleeCombatMode && meleeWeapon != null) {
            WeaponState meleeInitialState = meleeWeapon.getInitialState();
            if (meleeInitialState != null) {
                WeaponState oldState = currentWeaponState;
                currentWeaponState = meleeInitialState;
            } else {
            }
        }
        
        // Reset weapon hold state when switching combat modes
        resetWeaponHoldStateToDefault();
    }
    
    // Legacy methods for backwards compatibility with tests
    public String getName() {
        return nickname;
    }

    public void setName(String name) {
        this.nickname = name;
    }
    
    @Override
    public String getDisplayName() {
        return id + ":" + nickname;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    // ICharacter interface implementation - Combat State
    
    @Override
    public IUnit getCurrentTarget() {
        // DevCycle 30: Delegate to TargetManager and sync field for compatibility
        this.currentTarget = TargetManager.getInstance().getCurrentTarget(this.id);
        return this.currentTarget;
    }
    
    @Override
    public void setCurrentTarget(IUnit target) {
        // DevCycle 30: Delegate to TargetManager and sync field for compatibility
        TargetManager.getInstance().setCurrentTarget(this.id, target);
        this.currentTarget = target;
    }
    
    public boolean hasValidTarget() {
        return TargetManager.getInstance().hasValidTarget(this.id);
    }
    
    public boolean hasTargetChanged(IUnit newTarget) {
        return TargetManager.getInstance().hasTargetChanged(this.id, newTarget);
    }
    
    public IUnit getPreviousTarget() {
        return TargetManager.getInstance().getPreviousTarget(this.id);
    }
    
    public void setPreviousTarget(IUnit target) {
        TargetManager.getInstance().setPreviousTarget(this.id, target);
        this.previousTarget = target;
    }
    
    public IUnit getMeleeTarget() {
        return TargetManager.getInstance().getMeleeTarget(this.id);
    }
    
    public void setMeleeTarget(IUnit target) {
        TargetManager.getInstance().setMeleeTarget(this.id, target);
        this.meleeTarget = target;
    }
    
    public IUnit getReactionTarget() {
        return TargetManager.getInstance().getReactionTarget(this.id);
    }
    
    public void setReactionTarget(IUnit target) {
        TargetManager.getInstance().setReactionTarget(this.id, target);
        this.reactionTarget = target;
    }
    
    @Override
    public boolean isPersistentAttack() {
        return persistentAttack;
    }
    
    @Override
    public void setPersistentAttack(boolean persistent) {
        this.persistentAttack = persistent;
    }
    
    @Override
    public boolean isAttacking() {
        return isAttacking;
    }
    
    @Override
    public void setAttacking(boolean attacking) {
        this.isAttacking = attacking;
    }
    
    @Override
    public int getFaction() {
        return faction;
    }
    
    @Override
    public void setFaction(int faction) {
        this.faction = faction;
    }
    
    @Override
    public boolean isUsesAutomaticTargeting() {
        return usesAutomaticTargeting;
    }
    
    @Override
    public void setUsesAutomaticTargeting(boolean autoTarget) {
        this.usesAutomaticTargeting = autoTarget;
    }
    
    @Override
    public FiringMode getPreferredFiringMode() {
        return preferredFiringMode;
    }
    
    @Override
    public void setPreferredFiringMode(FiringMode mode) {
        this.preferredFiringMode = mode;
    }
    
    // ICharacter interface implementation - Skills and Wounds
    
    @Override
    public List<Skill> getSkills() {
        // DevCycle 30: Sync from manager to field for compatibility
        this.skills = CharacterSkillsManager.getInstance().getSkills(this.id);
        return this.skills;
    }
    
    @Override
    public void setSkills(List<Skill> skills) {
        // DevCycle 30: Sync both manager and field for compatibility
        CharacterSkillsManager.getInstance().setSkills(this.id, skills);
        this.skills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
    }
    
    @Override
    public List<Wound> getWounds() {
        // DevCycle 30: Sync from manager to field for compatibility
        this.wounds = CharacterStatsManager.getInstance().getWounds(this.id);
        return this.wounds;
    }
    
    @Override
    public void setWounds(List<Wound> wounds) {
        // DevCycle 30: Sync both manager and field for compatibility
        CharacterStatsManager.getInstance().setWounds(this.id, wounds);
        this.wounds = wounds != null ? new ArrayList<>(wounds) : new ArrayList<>();
    }
    
    public double getEffectiveMovementSpeed() {
        if (isIncapacitated()) {
            return 0.0;
        }
        return MovementController.getEffectiveMovementSpeed(this);
    }
    
    public void increaseMovementType() {
        if (!isIncapacitated()) {
            MovementController.increaseMovementType(this);
        }
    }
    
    public void decreaseMovementType() {
        if (!isIncapacitated()) {
            MovementController.decreaseMovementType(this);
        }
    }
    
    public void increaseAimingSpeed() {
        if (!isIncapacitated()) {
            AimingSystem.getInstance().increaseAimingSpeed(this);
        }
    }
    
    public void decreaseAimingSpeed() {
        if (!isIncapacitated()) {
            AimingSystem.getInstance().decreaseAimingSpeed(this);
        }
    }
    
    public boolean canUseVeryCarefulAiming() {
        return AimingSystem.getInstance().canUseVeryCarefulAiming(this);
    }
    
    public void cycleWeaponHoldState() {
        if (!isIncapacitated()) {
            Weapon activeWeapon = isMeleeCombatMode ? meleeWeapon : weapon;
            if (activeWeapon == null) {
                return;
            }
            
            // Delegate to WeaponStateManager
            WeaponStateManager.getInstance().cycleWeaponHoldState(this, activeWeapon);
        }
    }
    
    public String getCurrentWeaponHoldState() {
        return WeaponStateManager.getInstance().getWeaponHoldState(this.id);
    }
    
    public void toggleFiringPreference(long currentTick) {
        if (!isIncapacitated()) {
            AimingSystem.getInstance().toggleFiringPreference(this, currentTick);
            System.out.println("*** " + getDisplayName() + " firing preference: " + 
                             (getFiresFromAimingState() ? "aiming state" : "pointedfromhip state") + " ***");
        }
    }
    // Removed handleFiringPreferenceStateAdjustment - now handled by AimingSystem
    
    public boolean getFiringPreference() {
        return WeaponStateManager.getInstance().getFiresFromAimingState(this.id);
    }
    
    public void resetWeaponHoldStateToDefault() {
        WeaponStateManager.getInstance().setWeaponHoldState(this.id, "aiming");
    }
    
    /** Setter for weapon hold state - delegates to WeaponStateManager */
    public void setWeaponHoldState(String holdState) {
        WeaponStateManager.getInstance().setWeaponHoldState(this.id, holdState);
    }
    
    /** Getter for weapon hold state - delegates to WeaponStateManager */
    public String getWeaponHoldState() {
        return WeaponStateManager.getInstance().getWeaponHoldState(this.id);
    }
    
    /** Setter for firing preference - delegates to WeaponStateManager */
    public void setFiresFromAimingState(boolean firesFromAiming) {
        WeaponStateManager.getInstance().setFiresFromAimingState(this.id, firesFromAiming);
    }
    
    /** Getter for firing preference - delegates to WeaponStateManager */
    public boolean getFiresFromAimingState() {
        return WeaponStateManager.getInstance().getFiresFromAimingState(this.id);
    }
    
    private java.util.List<String> getAvailableHoldStates(Weapon weapon) {
        java.util.List<String> availableStates = new java.util.ArrayList<>();
        if (weapon == null || weapon.getStates() == null) {
            return availableStates;
        }
        
        for (WeaponState state : weapon.getStates()) {
            String stateName = state.getState();
            // Exclude post-firing states
            if (!stateName.equals("firing") && !stateName.equals("recovering") && !stateName.equals("reloading")) {
                availableStates.add(stateName);
            }
        }
        
        return availableStates;
    }
    
    // ICharacter interface implementation - Combat Statistics
    
    @Override
    public int getCombatEngagements() {
        return combatEngagements;
    }
    
    @Override
    public void setCombatEngagements(int engagements) {
        this.combatEngagements = engagements;
    }
    
    @Override
    public int getWoundsReceived() {
        return woundsReceived;
    }
    
    @Override
    public void setWoundsReceived(int wounds) {
        this.woundsReceived = wounds;
    }
    
    @Override
    public int getAttacksAttempted() {
        return attacksAttempted;
    }
    
    @Override
    public void setAttacksAttempted(int attacks) {
        this.attacksAttempted = attacks;
    }
    
    @Override
    public int getAttacksSuccessful() {
        return attacksSuccessful;
    }
    
    @Override
    public void setAttacksSuccessful(int attacks) {
        this.attacksSuccessful = attacks;
    }
    
    @Override
    public int getTargetsIncapacitated() {
        return targetsIncapacitated;
    }
    
    @Override
    public void setTargetsIncapacitated(int targets) {
        this.targetsIncapacitated = targets;
    }
    
    @Override
    public int getRangedAttacksAttempted() {
        return rangedAttacksAttempted;
    }
    
    @Override
    public void setRangedAttacksAttempted(int attacks) {
        this.rangedAttacksAttempted = attacks;
    }
    
    @Override
    public int getRangedAttacksSuccessful() {
        return rangedAttacksSuccessful;
    }
    
    @Override
    public void setRangedAttacksSuccessful(int attacks) {
        this.rangedAttacksSuccessful = attacks;
    }
    
    @Override
    public int getRangedWoundsInflicted() {
        return rangedWoundsInflicted;
    }
    
    @Override
    public void setRangedWoundsInflicted(int wounds) {
        this.rangedWoundsInflicted = wounds;
    }
    
    @Override
    public int getMeleeAttacksAttempted() {
        return meleeAttacksAttempted;
    }
    
    @Override
    public void setMeleeAttacksAttempted(int attacks) {
        this.meleeAttacksAttempted = attacks;
    }
    
    @Override
    public int getMeleeAttacksSuccessful() {
        return meleeAttacksSuccessful;
    }
    
    @Override
    public void setMeleeAttacksSuccessful(int attacks) {
        this.meleeAttacksSuccessful = attacks;
    }
    
    @Override
    public int getMeleeWoundsInflicted() {
        return meleeWoundsInflicted;
    }
    
    @Override
    public void setMeleeWoundsInflicted(int wounds) {
        this.meleeWoundsInflicted = wounds;
    }
    
    // ICharacter interface implementation - State Checks and Burst/Auto
    
    @Override
    public int getBurstShotsFired() {
        return BurstFireManager.getInstance().getBurstShotsFired(this.id);
    }
    
    @Override
    public void setBurstShotsFired(int shots) {
        BurstFireManager.getInstance().setBurstShotsFired(this.id, shots);
    }
    
    @Override
    public long getLastAutomaticShot() {
        return BurstFireManager.getInstance().getLastAutomaticShot(this.id);
    }
    
    @Override
    public void setLastAutomaticShot(long tick) {
        BurstFireManager.getInstance().setLastAutomaticShot(this.id, tick);
    }
    
    @Override
    public boolean isReloading() {
        // Check both local flag and ReloadManager state
        return isReloading || ReloadManager.getInstance().isReloading(this.id);
    }
    
    @Override
    public void setReloading(boolean reloading) {
        this.isReloading = reloading;
    }
    
    public void increasePosition() {
        if (!isIncapacitated()) {
            // Characters with both legs wounded cannot stand up from prone
            if (currentPosition == PositionState.PRONE && hasBothLegsWounded()) {
                return;
            }
            this.currentPosition = currentPosition.increase();
        }
    }
    
    public void decreasePosition() {
        if (!isIncapacitated()) {
            PositionState oldPosition = currentPosition;
            this.currentPosition = currentPosition.decrease();
            
            // Force crawl movement when going prone
            if (oldPosition != PositionState.PRONE && currentPosition == PositionState.PRONE) {
                this.currentMovementType = MovementType.CRAWL;
            }
        }
    }
    
    public Skill getSkill(String skillName) {
        return CharacterSkillsManager.getInstance().getSkill(this.id, skillName);
    }
    
    public int getSkillLevel(String skillName) {
        return CharacterSkillsManager.getInstance().getSkillLevel(this.id, skillName);
    }
    
    public void setSkillLevel(String skillName, int level) {
        CharacterSkillsManager.getInstance().setSkillLevel(this.id, skillName, level);
    }
    
    public void addSkill(Skill skill) {
        CharacterSkillsManager.getInstance().addSkill(this.id, skill);
    }
    
    public boolean hasSkill(String skillName) {
        return CharacterSkillsManager.getInstance().hasSkill(this.id, skillName);
    }
    
    public static List<Skill> createDefaultSkills() {
        List<Skill> defaultSkills = new ArrayList<>();
        defaultSkills.add(new Skill(SkillsManager.PISTOL, 50));
        defaultSkills.add(new Skill(SkillsManager.RIFLE, 50));
        defaultSkills.add(new Skill(SkillsManager.QUICKDRAW, 50));
        defaultSkills.add(new Skill(SkillsManager.MEDICINE, 50));
        return defaultSkills;
    }
    
    public void addDefaultSkills() {
        CharacterSkillsManager.getInstance().addDefaultSkills(this.id);
    }
    
    public void addWound(Wound wound, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        wounds.add(wound);
        woundsReceived++;
        
        // Apply damage to current health
        currentHealth -= wound.getDamage();
        
        // Enforce movement restrictions immediately after adding wound
        enforceMovementRestrictions();
        
        // Don't add hesitation for incapacitated characters
        if (!isIncapacitated()) {
            triggerHesitation(wound.severity, currentTick, eventQueue, ownerId);
        }
    }
    
    // Backwards compatibility for existing calls
    public void addWound(Wound wound) {
        wounds.add(wound);
        woundsReceived++;
        
        // Apply damage to current health
        currentHealth -= wound.getDamage();
        
        // Enforce movement restrictions immediately after adding wound
        enforceMovementRestrictions();
        
        // Note: Hesitation will not be triggered without event queue context
    }
    
    @Override
    public boolean isIncapacitated() {
        boolean incapacitated = false;
        
        if (currentHealth <= 0) {
            incapacitated = true;
        }
        // Check for any critical wounds
        for (Wound wound : wounds) {
            if (wound.getSeverity() == WoundSeverity.CRITICAL) {
                incapacitated = true;
                break;
            }
        }
        
        // Force prone position for incapacitated characters
        if (incapacitated && currentPosition != PositionState.PRONE) {
            currentPosition = PositionState.PRONE;
        }
        
        return incapacitated;
    }
    
    public boolean removeWound(Wound wound) {
        // DevCycle 30: Delegate to CharacterStatsManager and sync field
        boolean removed = CharacterStatsManager.getInstance().removeWound(this.id, wound);
        if (removed) {
            this.wounds = CharacterStatsManager.getInstance().getWounds(this.id);
        }
        return removed;
    }
    
    public boolean canFire() {
        return currentWeaponState != null && "aiming".equals(currentWeaponState.getState());
    }
    
    @Override
    public boolean isHesitating() {
        return isHesitating;
    }
    
    @Override
    public boolean isAutomaticFiring() {
        return BurstFireManager.getInstance().isAutomaticFiring(this.id);
    }
    
    @Override
    public int getWoundModifier() {
        int modifier = 0;
        for (Wound wound : wounds) {
            modifier += wound.getModifier();
        }
        return modifier;
    }
    
    @Override
    public void update(long currentTick) {
        // Character update logic if needed
        // Currently most updates are handled through Unit.update()
    }
    
    public void startAttackSequence(IUnit shooter, IUnit target, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // DevCycle 30: Delegate to CombatCoordinator for attack sequence management
        CombatCoordinator.getInstance().startAttackSequenceInternal(shooter, target, currentTick, eventQueue, ownerId, gameCallbacks);
    }
    
    public void startReadyWeaponSequence(IUnit unit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        // Handle both ranged and melee weapons in unified system
        if (isMeleeCombatMode) {
            // Melee mode - ready the melee weapon
            if (meleeWeapon == null) {
                return;
            }
            
            // Initialize weapon state if needed for melee weapon
            if (currentWeaponState == null) {
                currentWeaponState = meleeWeapon.getInitialState();
            }
        } else {
            // Ranged mode - ready the ranged weapon
            if (weapon == null) {
                return;
            }
            
            // Initialize weapon state if needed for ranged weapon
            if (currentWeaponState == null) {
                currentWeaponState = weapon.getInitialState();
            }
        }
        
        scheduleReadyFromCurrentState(unit, currentTick, eventQueue, ownerId);
    }
    
    /**
     * Start melee attack sequence from current weapon state
     */
    public void startMeleeAttackSequence(IUnit attacker, IUnit target, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Delegate to AttackSequenceManager following DevCycle 31 Option 4 refactoring pattern
        AttackSequenceManager.getInstance().startMeleeAttackSequence(this, attacker, target, currentTick, eventQueue, ownerId, gameCallbacks);
    }
    
    public void scheduleAttackFromCurrentState(IUnit shooter, IUnit target, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Delegate to AttackSequenceManager following DevCycle 31 Option 4 refactoring pattern
        AttackSequenceManager.getInstance().scheduleAttackFromCurrentState(this, shooter, target, currentTick, eventQueue, ownerId, gameCallbacks);
    }
    
    /**
     * DevCycle 28: Determine the aiming speed for the current shot in a multiple shot sequence.
     * Pattern: First shot uses character's aiming speed, ALL subsequent shots use Quick
     * - Shot 1: Character's aiming speed (Aimed)
     * - Shots 2, 3, 4, 5: Quick aiming speed
     * 
     * @return The aiming speed to use for the current shot
     */
    
    /**
     * DevCycle 28: Reset the multiple shot sequence when interrupted.
     * Called when target changes, character is hit, or sequence is otherwise interrupted.
     */
    public void resetMultipleShotSequence() {
        currentShotInSequence = 0;
    }
    
    /**
     * DevCycle 28: Update reaction monitoring each tick.
     * Checks if monitored target's weapon state has changed and schedules reaction.
     * 
     * @param selfUnit The unit performing the monitoring
     * @param currentTick Current game tick
     * @param eventQueue Event queue for scheduling reactions
     * @param gameCallbacks Game callbacks for attack scheduling
     */
    public void updateReactionMonitoring(IUnit selfUnit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, GameCallbacks gameCallbacks) {
        // Delegate to ReactionManager following DevCycle 31 Option 4 refactoring pattern
        ReactionManager.getInstance().updateReactionMonitoring(this, selfUnit, currentTick, eventQueue, gameCallbacks);
    }
    
    private void originalUpdateReactionMonitoring(IUnit selfUnit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, GameCallbacks gameCallbacks) {
        // Skip if no reaction target set
        if (reactionTarget == null || reactionBaselineState == null) {
            return;
        }
        
        // Skip if already triggered
        if (reactionTriggerTick > 0) {
            return;
        }
        
        // Skip if character is incapacitated or reloading
        if (isIncapacitated() || isReloading) {
            return;
        }
        
        // Check if target's weapon state has changed
        WeaponState currentTargetState = reactionTarget.getCharacter().currentWeaponState;
        if (currentTargetState != null && currentTargetState != reactionBaselineState) {
            // Weapon state changed - trigger reaction with delay
            int reflexModifier = GameConstants.statToModifier(this.reflexes);
            long reactionDelay = Math.max(1, 30 - reflexModifier); // 30 base minus reflex modifier, minimum 1 tick
            
            reactionTriggerTick = currentTick + reactionDelay;
            
            // Schedule the reaction attack
            eventQueue.add(new ScheduledEvent(reactionTriggerTick, () -> {
                // Check if still valid to react (not incapacitated, target still exists, etc)
                if (!isIncapacitated() && reactionTarget != null && !isAttacking) {
                    System.out.println("*** " + getDisplayName() + " reacting to " + 
                                     reactionTarget.getCharacter().getDisplayName() + 
                                     " weapon state change (delay: " + reactionDelay + " ticks) ***");
                    
                    // Start attack sequence - this will handle queueing if already attacking
                    startAttackSequence(selfUnit, reactionTarget, reactionTriggerTick, eventQueue, selfUnit.getId(), gameCallbacks);
                    
                    // Clear reaction after triggering
                    reactionTarget = null;
                    reactionBaselineState = null;
                    reactionTriggerTick = -1;
                } else if (isAttacking) {
                    // Queue the reaction for after current attack
                    System.out.println("*** " + getDisplayName() + " queuing reaction - already attacking ***");
                    // Re-schedule for later
                    eventQueue.add(new ScheduledEvent(reactionTriggerTick + 30, () -> {
                        if (!isIncapacitated() && reactionTarget != null && !isAttacking) {
                            startAttackSequence(selfUnit, reactionTarget, reactionTriggerTick + 30, eventQueue, selfUnit.getId(), gameCallbacks);
                            // Clear reaction after triggering
                            reactionTarget = null;
                            reactionBaselineState = null;
                            reactionTriggerTick = -1;
                        }
                    }, selfUnit.getId()));
                }
            }, selfUnit.getId()));
        }
    }
    
    /**
     * DevCycle 27: System 5 - Check if character is already in the correct firing state and should fire immediately.
     * This eliminates unnecessary weapon progression delays when the character is already holding at the target state.
     * 
     * @param currentState The current weapon state name
     * @param currentTick The current game tick
     * @return true if should fire immediately, false if normal progression delays should apply
     */
    public boolean isAlreadyInCorrectFiringState(String currentState, long currentTick) {
        // Criteria for immediate firing:
        // 1. Character is in "aiming" state AND firing preference is aiming
        // 2. Character is in "pointedfromhip" state AND firing preference is pointedfromhip  
        // 3. Character has been in current state for some minimum time (not just transitioned)
        
        if ("aiming".equals(currentState) && getFiresFromAimingState()) {
            // Character is aiming and prefers to fire from aiming state
            // Check if they've been aiming for at least a minimal amount of time (5+ ticks)
            long timingDuration = getCurrentAimingDuration(currentTick);
            return timingDuration >= 5;
        }
        
        if ("pointedfromhip".equals(currentState) && !getFiresFromAimingState()) {
            // Character is pointing from hip and prefers to fire from pointedfromhip state
            // Check if they've been pointing for at least a minimal amount of time (5+ ticks)
            long timingDuration = getCurrentPointingFromHipDuration(currentTick);
            return timingDuration >= 5;
        }
        
        // For all other cases, use normal firing progression with delays
        return false;
    }
    
    /**
     * Schedule melee attack from current weapon state
     * DevCycle 31: Delegate to CombatCoordinator to reduce Character.java size
     */
    public void scheduleMeleeAttackFromCurrentState(IUnit attacker, IUnit target, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // DevCycle 31: Delegate to CombatCoordinator for melee attack sequence management
        CombatCoordinator.getInstance().startMeleeAttackSequenceInternal(attacker, target, currentTick, eventQueue, ownerId, gameCallbacks);
    }
    
    public void scheduleStateTransition(String newStateName, long currentTick, long transitionTickLength, IUnit shooter, IUnit target, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Delegate to WeaponStateTransitionManager following DevCycle 31 Option 4 refactoring pattern
        WeaponStateTransitionManager.getInstance().scheduleStateTransition(this, newStateName, currentTick, transitionTickLength, shooter, target, eventQueue, ownerId, gameCallbacks);
    }
    
    public void scheduleFiring(IUnit shooter, IUnit target, long fireTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // DevCycle 31: Delegate to CombatCoordinator for firing sequence management
        CombatCoordinator.getInstance().scheduleFiringInternal(shooter, target, fireTick, eventQueue, ownerId, gameCallbacks);
    }
    
    public void scheduleReadyFromCurrentState(IUnit unit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        // Check weapon and state availability for both ranged and melee
        if (currentWeaponState == null) return;
        if (!isMeleeCombatMode && weapon == null) return;
        if (isMeleeCombatMode && meleeWeapon == null) return;
        
        String currentState = currentWeaponState.getState();
        
        // Determine target state: either hold state or default ready state
        String targetState;
        String targetHoldState = WeaponStateManager.getInstance().getTargetHoldState(this.id);
        if (targetHoldState != null) {
            targetState = targetHoldState;
        } else {
            targetState = isMeleeCombatMode ? "melee_ready" : "ready";
        }
        
        // Debug output for weapon state progression
        System.out.println("*** " + getDisplayName() + " weapon progression: current=" + currentState + 
                          ", target=" + targetState + ", tick=" + currentTick + " ***");
        
        // If we're already at the target state, stop progression
        if (targetState.equals(currentState)) {
            if (targetHoldState != null) {
                WeaponStateManager.getInstance().setTargetHoldState(this.id, null); // Clear target hold state after reaching it
                System.out.println("*** " + getDisplayName() + " reached hold state: " + currentState + " ***");
            }
            return;
        }
        
        // Get the appropriate weapon for state transitions
        Weapon activeWeapon = isMeleeCombatMode ? meleeWeapon : weapon;
        
        // Find the next state in the weapon's progression using the action field
        String nextState = currentWeaponState.getAction();
        if (nextState == null || nextState.isEmpty()) {
            // No next state defined, we're at the end of progression
            return;
        }
        
        // Check if the next state is available in the weapon
        WeaponState nextWeaponState = activeWeapon.getStateByName(nextState);
        if (nextWeaponState == null) {
            // Next state not found in weapon, can't progress
            return;
        }
        
        // Debug: Show what state we're transitioning to and when
        long transitionTime = currentTick + currentWeaponState.ticks;
        System.out.println("*** " + getDisplayName() + " scheduling transition from " + currentState + 
                          " to " + nextState + " in " + currentWeaponState.ticks + " ticks (at tick " + transitionTime + ") ***");
        
        // Schedule transition to the next state
        scheduleReadyStateTransition(nextState, currentTick, currentWeaponState.ticks, unit, eventQueue, ownerId);
    }
    
    public double calculateWeaponReadySpeedMultiplier() {
        int reflexesModifier = GameConstants.statToModifier(this.reflexes);
        double reflexesSpeedMultiplier = 1.0 - (reflexesModifier * 0.015);
        
        int quickdrawLevel = getSkillLevel(SkillsManager.QUICKDRAW);
        double quickdrawSpeedMultiplier = 1.0 - (quickdrawLevel * 0.08);
        
        return reflexesSpeedMultiplier * quickdrawSpeedMultiplier;
    }
    
    
    public void scheduleReadyStateTransition(String newStateName, long currentTick, long transitionTickLength, IUnit unit, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        // Delegate to WeaponStateTransitionManager following DevCycle 31 Option 4 refactoring pattern
        WeaponStateTransitionManager.getInstance().scheduleReadyStateTransition(this, newStateName, currentTick, transitionTickLength, unit, eventQueue, ownerId);
    }
    
    public boolean isWeaponPreparationState(String stateName) {
        // Create a temporary WeaponState to delegate to WeaponStateManager
        WeaponState tempState = new WeaponState(stateName, "", 0);
        return WeaponStateManager.getInstance().isWeaponPreparationState(tempState);
    }
    
    
    /**
     * Check if the current shot should have burst/auto quick penalty
     */
    public boolean shouldApplyBurstAutoPenalty() {
        // Delegate to BurstFireManager which tracks burst shot counts
        return BurstFireManager.getInstance().shouldApplyBurstAutoPenalty(this.id);
    }
    
    public double getWeaponReadySpeedMultiplier() {
        return calculateWeaponReadySpeedMultiplier();
    }
    
    /**
     * Schedule melee state transition
     */
    public void scheduleMeleeStateTransition(String newStateName, long currentTick, long transitionTickLength, IUnit attacker, IUnit target, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Delegate to MeleeCombatSequenceManager following DevCycle 31 Option 4 refactoring pattern
        MeleeCombatSequenceManager.getInstance().scheduleMeleeStateTransition(this, newStateName, currentTick, transitionTickLength, attacker, target, eventQueue, ownerId, gameCallbacks);
    }
    
    /**
     * Schedule range check for melee attack - continues tracking target until in range
     */
    public void scheduleRangeCheckForMeleeAttack(IUnit attacker, IUnit target, long checkTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Delegate to MeleeCombatSequenceManager following DevCycle 31 Option 4 refactoring pattern
        MeleeCombatSequenceManager.getInstance().scheduleRangeCheckForMeleeAttack(this, attacker, target, checkTick, eventQueue, ownerId, gameCallbacks);
    }

    /**
     * Schedule actual melee attack execution
     */
    public void scheduleMeleeAttack(IUnit attacker, IUnit target, long attackTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Delegate to MeleeCombatSequenceManager following DevCycle 31 Option 4 refactoring pattern
        MeleeCombatSequenceManager.getInstance().scheduleMeleeAttack(this, attacker, target, attackTick, eventQueue, ownerId, gameCallbacks);
    }
    
    /**
     * Calculate attack speed multiplier based on character stats and skills
     */
    public double calculateAttackSpeedMultiplier() {
        // Use same speed calculation as weapon ready speed for consistency
        return calculateWeaponReadySpeedMultiplier();
    }
    
    public boolean canReload() {
        if (weapon == null || !(weapon instanceof RangedWeapon)) return false;
        RangedWeapon rangedWeapon = (RangedWeapon)weapon;
        if (rangedWeapon.getAmmunition() >= rangedWeapon.getMaxAmmunition()) return false;
        String state = currentWeaponState.getState();
        return "ready".equals(state) || "aiming".equals(state) || "recovering".equals(state);
    }
    
    public void startReloadSequence(IUnit unit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        if (!canReload() || isReloading) return;
        
        // Delegate to ReloadManager
        boolean started = ReloadManager.getInstance().startReloadSequence(this, (RangedWeapon)weapon, currentTick);
        
        // If reload started successfully and we're not in persistent attack mode,
        // schedule a check for continuous attack after reload completes
        if (started && !persistentAttack) {
            long reloadCompleteTick = ReloadManager.getInstance().getReloadCompletionTick(this.id);
            eventQueue.add(new ScheduledEvent(reloadCompleteTick, () -> {
                checkContinuousAttack(unit, reloadCompleteTick, eventQueue, ownerId, gameCallbacks);
            }, ownerId));
        }
    }
    
    // Method removed - now handled internally by ReloadManager
    
    // Method removed - now handled by ReloadManager
    
    // Method removed - now handled by ReloadManager
    
    public boolean isHostileTo(Character other) {
        return this.faction != other.faction;
    }

    // Removed duplicate method - now using AutoTargetingSystem.findNearestHostileTargetWithZonePriority
    
    public void performAutomaticTargetChange(IUnit shooter, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Only proceed if still in persistent attack mode and not incapacitated
        if (!persistentAttack || this.isIncapacitated() || weapon == null) {
            persistentAttack = false;
            currentTarget = null;
            isAttacking = false;
            
            // Preserve the current facing direction so weapon continues to aim at last target location
            if (lastTargetFacing != null && shooter != null) {
                shooter.setTargetFacing(lastTargetFacing);
            }
            return;
        }
        
        // Find new target with target zone priority
        IUnit newTarget = AutoTargetingSystem.findNearestHostileTargetWithZonePriority(this, shooter, gameCallbacks);
        
        if (newTarget != null) {
            // New target found - start attacking
            currentTarget = newTarget;
            
            // Calculate distance for logging
            double dx = newTarget.getX() - shooter.getX();
            double dy = newTarget.getY() - shooter.getY();
            double distanceFeet = Math.hypot(dx, dy) / 7.0;
            
            String zoneStatus = (targetZone != null && targetZone.contains((int)newTarget.getX(), (int)newTarget.getY())) ? " (in target zone)" : "";
            
            // Start attack sequence from current state
            startAttackSequence(shooter, newTarget, currentTick, eventQueue, ownerId, gameCallbacks);
        } else {
            // No valid targets found - disable persistent attack but preserve weapon aiming direction
            persistentAttack = false;
            currentTarget = null;
            isAttacking = false;
            
            // Preserve the current facing direction so weapon continues to aim at last target location
            if (lastTargetFacing != null) {
                shooter.setTargetFacing(lastTargetFacing);
            }
            
        }
    }
    
    public void updateAutomaticTargeting(IUnit selfUnit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, GameCallbacks gameCallbacks) {
        // DevCycle 30: Delegate to AutoTargetingSystem to reduce Character.java size
        AutoTargetingSystem.updateAutomaticTargeting(this, selfUnit, currentTick, eventQueue, gameCallbacks);
    }
    
    /**
     * Update melee movement progress and trigger attack when target is reached
     */
    public void updateMeleeMovement(IUnit selfUnit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, GameCallbacks gameCallbacks) {
        MeleeCombatManager.updateMeleeMovement(this, selfUnit, currentTick, eventQueue, selfUnit.getId(), gameCallbacks);
    }
    
    public void checkContinuousAttack(IUnit shooter, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Delegate to CombatCoordinator following DevCycle 31 refactoring pattern
        CombatCoordinator.getInstance().handleAttackContinuation(this, shooter, currentTick, eventQueue, ownerId, gameCallbacks);
    }
    // handleBurstFiring and handleFullAutoFiring methods removed - now handled by BurstFireManager
    
    public int getTotalWoundsInflicted() {
        return CombatStatisticsManager.getTotalWoundsInflicted(this);
    }
    
    public int getWoundsInflictedByType(WoundSeverity severity) {
        return CombatStatisticsManager.getWoundsInflictedByType(this, severity);
    }
    
    // Firing mode management
    public void cycleFiringMode() {
        CombatStatisticsManager.cycleFiringMode(this);
    }
    
    public String getCurrentFiringMode() {
        return CombatStatisticsManager.getCurrentFiringMode(this);
    }
    
    public boolean hasMultipleFiringModes() {
        return weapon != null && weapon instanceof RangedWeapon && ((RangedWeapon)weapon).hasMultipleFiringModes();
    }
    
    // Hesitation mechanics delegated to HesitationManager (DevCycle 24)
    private void triggerHesitation(WoundSeverity woundSeverity, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        HesitationManager.triggerHesitation(this, woundSeverity, currentTick, eventQueue, ownerId);
    }
    
    // Bravery check mechanics delegated to HesitationManager (DevCycle 24)
    public void performBraveryCheck(long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, String reason) {
        HesitationManager.performBraveryCheck(this, currentTick, eventQueue, ownerId, reason);
    }
    
    public int getBraveryPenalty(long currentTick) {
        return HesitationManager.getBraveryPenalty(this, currentTick);
    }
    
    // Movement wound penalty methods
    public boolean hasLegWound(BodyPart leg) {
        for (Wound wound : wounds) {
            if (wound.getBodyPart() == leg && wound.getSeverity().ordinal() >= WoundSeverity.LIGHT.ordinal()) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasBothLegsWounded() {
        return hasLegWound(BodyPart.LEFT_LEG) && hasLegWound(BodyPart.RIGHT_LEG);
    }
    
    public boolean hasAnyLegWound() {
        return hasLegWound(BodyPart.LEFT_LEG) || hasLegWound(BodyPart.RIGHT_LEG);
    }
    
    public MovementType getMaxAllowedMovementType() {
        return MovementController.getMaxAllowedMovementType(this);
    }
    
    public void enforceMovementRestrictions() {
        MovementController.enforceMovementRestrictions(this);
    }
    
    /**
     * Check if target is within melee range of attacker using edge-to-edge distance
     */
    public boolean isInMeleeRange(IUnit attacker, IUnit target, MeleeWeapon weapon) {
        double centerToCenter = Math.hypot(target.getX() - attacker.getX(), target.getY() - attacker.getY());
        // Convert to edge-to-edge by subtracting target radius (1.5 feet = 10.5 pixels)
        double edgeToEdge = centerToCenter - (1.5 * 7.0);
        double pixelRange = weapon.getTotalReach() * 7.0; // Convert feet to pixels (7 pixels = 1 foot)
        
        return edgeToEdge <= pixelRange;
    }
    
    // Defense state methods (DevCycle 23)
    
    /**
     * Gets the current defense state
     * @return Current DefenseState
     */
    public DefenseState getDefenseState() {
        return DefenseManager.getInstance().getDefenseState(id);
    }
    
    /**
     * Sets the defense state
     * @param state New defense state
     */
    public void setDefenseState(DefenseState state) {
        DefenseManager.getInstance().setDefenseState(id, state);
    }
    
    /**
     * Checks if character can defend (not in cooldown or mid-counter-attack)
     * @return true if character can defend
     */
    public boolean canDefend(long currentTick) {
        return DefenseManager.getInstance().canDefend(this, currentTick);
    }
    
    /**
     * Starts defense cooldown
     * @param cooldownTicks Duration of cooldown in ticks
     * @param currentTick Current game tick
     */
    public void startDefenseCooldown(int cooldownTicks, long currentTick) {
        DefenseManager.getInstance().setDefenseCooldown(id, currentTick + cooldownTicks);
    }
    
    /**
     * Updates defense state based on current tick
     * @param currentTick Current game tick
     * @deprecated DefenseManager now handles state updates internally
     */
    @Deprecated
    public void updateDefenseState(long currentTick) {
        // DefenseManager handles state updates internally
    }
    
    /**
     * Grants a counter-attack opportunity
     * @param windowDurationTicks Duration of counter-attack window in ticks
     * @param currentTick Current game tick
     */
    public void grantCounterAttackOpportunity(int windowDurationTicks, long currentTick) {
        DefenseManager.getInstance().setCounterAttackWindow(id, currentTick + windowDurationTicks);
        DefenseManager.getInstance().setHasCounterAttackOpportunity(id, true);
    }
    
    /**
     * Checks if character can perform a melee attack (not in recovery from previous attack)
     * Fix for Bug #1: Rapid Consecutive Melee Attacks
     * @param currentTick Current game tick
     * @return true if character can attack (recovery period has ended)
     */
    public boolean canMeleeAttack(long currentTick) {
        return currentTick >= meleeRecoveryEndTick;
    }
    
    /**
     * Starts melee attack recovery period
     * Fix for Bug #1: Rapid Consecutive Melee Attacks
     * @param recoveryTicks Duration of recovery in ticks
     * @param currentTick Current game tick when attack was executed
     */
    public void startMeleeRecovery(int recoveryTicks, long currentTick) {
        lastMeleeAttackTick = currentTick;
        meleeRecoveryEndTick = currentTick + recoveryTicks;
    }
    
    /**
     * Updates melee recovery state based on current tick
     * Fix for Bug #1: Rapid Consecutive Melee Attacks
     * @param currentTick Current game tick
     */
    public void updateMeleeRecovery(long currentTick) {
        // Recovery tracking is passive - no active updates needed
        // Characters can attack again when currentTick >= meleeRecoveryEndTick
    }
}

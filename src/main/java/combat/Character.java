package combat;

import combat.interfaces.ICharacter;
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
    
    /** Character configuration */
    public int faction;
    public boolean usesAutomaticTargeting;
    public FiringMode preferredFiringMode;
    
    // SKILLS AND WOUNDS
    
    /** Character skills and abilities */
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
    
    /** Burst and full-auto firing state */
    public boolean isAutomaticFiring = false;   // Currently in automatic firing mode
    public int burstShotsFired = 0;             // Number of shots fired in current burst
    public long lastAutomaticShot = 0;          // Tick of last automatic shot
    
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
    
    /** Defense state management */
    private DefenseState currentDefenseState = DefenseState.READY; // Current defensive state
    private long defenseCooldownEndTick = 0; // When defense cooldown will end
    private long counterAttackWindowEndTick = 0; // When counter-attack window expires
    private boolean hasCounterAttackOpportunity = false; // True when character can counter-attack

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
        this.skills = new ArrayList<>();
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
        this.skills = skills != null ? skills : new ArrayList<>();
        initializeDefaultWeapons();
    }

    public Character(String nickname, int dexterity, int health, int coolness, int strength, int reflexes, Handedness handedness, Weapon weapon, List<Skill> skills) {
        this(nickname, dexterity, health, coolness, strength, reflexes, handedness);
        this.weapon = weapon;
        this.skills = skills != null ? skills : new ArrayList<>();
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
        this.skills = new ArrayList<>();
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
        this.skills = new ArrayList<>();
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
        this.skills = skills != null ? skills : new ArrayList<>();
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
        this.skills = skills != null ? skills : new ArrayList<>();
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
    }
    
    @Override
    public RangedWeapon getRangedWeapon() {
        return rangedWeapon;
    }
    
    @Override
    public void setRangedWeapon(RangedWeapon weapon) {
        this.rangedWeapon = weapon;
    }
    
    @Override
    public MeleeWeapon getMeleeWeapon() {
        return meleeWeapon;
    }
    
    @Override
    public void setMeleeWeapon(MeleeWeapon weapon) {
        this.meleeWeapon = weapon;
    }
    
    @Override
    public boolean isMeleeCombatMode() {
        return isMeleeCombatMode;
    }
    
    @Override
    public void setMeleeCombatMode(boolean melee) {
        this.isMeleeCombatMode = melee;
    }
    
    @Override
    public WeaponState getCurrentWeaponState() {
        return currentWeaponState;
    }
    
    @Override
    public void setCurrentWeaponState(WeaponState state) {
        this.currentWeaponState = state;
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
        return currentTarget;
    }
    
    @Override
    public void setCurrentTarget(IUnit target) {
        this.currentTarget = target;
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
        return skills;
    }
    
    @Override
    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }
    
    @Override
    public List<Wound> getWounds() {
        return wounds;
    }
    
    @Override
    public void setWounds(List<Wound> wounds) {
        this.wounds = wounds;
    }
    
    public double getEffectiveMovementSpeed() {
        if (isIncapacitated()) {
            return 0.0;
        }
        return baseMovementSpeed * currentMovementType.getSpeedMultiplier();
    }
    
    public void increaseMovementType() {
        if (!isIncapacitated()) {
            // Prone characters can only crawl
            if (currentPosition == PositionState.PRONE && currentMovementType == MovementType.CRAWL) {
                return; // Cannot increase from crawl when prone
            }
            
            MovementType newType = currentMovementType.increase();
            MovementType maxAllowed = getMaxAllowedMovementType();
            
            // Check if the new movement type is allowed given wound restrictions
            if (newType.ordinal() <= maxAllowed.ordinal()) {
                this.currentMovementType = newType;
            } else {
            }
        }
    }
    
    public void decreaseMovementType() {
        if (!isIncapacitated()) {
            this.currentMovementType = currentMovementType.decrease();
        }
    }
    
    public void increaseAimingSpeed() {
        if (!isIncapacitated()) {
            this.currentAimingSpeed = currentAimingSpeed.increase();
        }
    }
    
    public void decreaseAimingSpeed() {
        if (!isIncapacitated()) {
            AimingSpeed newSpeed = currentAimingSpeed.decrease();
            
            // Check if trying to go to Very Careful and verify requirements
            if (newSpeed == AimingSpeed.VERY_CAREFUL) {
                if (canUseVeryCarefulAiming()) {
                    this.currentAimingSpeed = newSpeed;
                }
                // If can't use very careful, stay at current speed (no change)
            } else {
                this.currentAimingSpeed = newSpeed;
            }
        }
    }
    
    public boolean canUseVeryCarefulAiming() {
        // Must have weapon skill level 1+ for pistol, rifle, or submachine gun weapons
        if (weapon == null) {
            return false;
        }
        
        WeaponType weaponType = weapon.getWeaponType();
        if (weaponType == WeaponType.OTHER) {
            return false; // OTHER weapons don't support very careful aiming
        }
        
        String skillName;
        switch (weaponType) {
            case PISTOL:
                skillName = SkillsManager.PISTOL;
                break;
            case RIFLE:
                skillName = SkillsManager.RIFLE;
                break;
            case SUBMACHINE_GUN:
                skillName = SkillsManager.SUBMACHINE_GUN;
                break;
            default:
                return false;
        }
        
        // Check if character has the required skill level (1+)
        return getSkillLevel(skillName) >= 1;
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
        return burstShotsFired;
    }
    
    @Override
    public void setBurstShotsFired(int shots) {
        this.burstShotsFired = shots;
    }
    
    @Override
    public long getLastAutomaticShot() {
        return lastAutomaticShot;
    }
    
    @Override
    public void setLastAutomaticShot(long tick) {
        this.lastAutomaticShot = tick;
    }
    
    @Override
    public boolean isReloading() {
        return isReloading;
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
        for (Skill skill : skills) {
            if (skill.getSkillName().equals(skillName)) {
                return skill;
            }
        }
        return null;
    }
    
    public int getSkillLevel(String skillName) {
        Skill skill = getSkill(skillName);
        return skill != null ? skill.getLevel() : 0;
    }
    
    public void setSkillLevel(String skillName, int level) {
        Skill skill = getSkill(skillName);
        if (skill != null) {
            skill.setLevel(level);
        } else {
            addSkill(new Skill(skillName, level));
        }
    }
    
    public void addSkill(Skill skill) {
        skills.add(skill);
    }
    
    public boolean hasSkill(String skillName) {
        return getSkill(skillName) != null;
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
        for (Skill defaultSkill : createDefaultSkills()) {
            if (!hasSkill(defaultSkill.getSkillName())) {
                addSkill(defaultSkill);
            }
        }
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
        return wounds.remove(wound);
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
        return isAutomaticFiring;
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
        if (weapon == null || currentWeaponState == null) return;
        
        // Always interrupt burst/auto when starting a new attack
        if (isAutomaticFiring) {
            isAutomaticFiring = false;
            burstShotsFired = 0;
        }
        
        // Check if this is a target change and handle first attack penalty
        boolean targetChanged = (currentTarget != null && currentTarget != target);
        boolean newTarget = (currentTarget == null);
        
        // If targeting a different unit, cancel all pending attacks and reset
        if (currentTarget != null && currentTarget != target) {
            // Clear all pending events for this character
            if (gameCallbacks != null) {
                gameCallbacks.removeAllEventsForOwner(ownerId);
            } else {
                System.err.println("CRITICAL ERROR: gameCallbacks is null in " + getDisplayName() + " attack sequence - cannot cancel pending events");
            }
            currentWeaponState = weapon.getStateByName("ready");
            // Interrupt burst/auto if in progress
            if (isAutomaticFiring) {
                isAutomaticFiring = false;
                burstShotsFired = 0;
                }
        } else if ("aiming".equals(currentWeaponState.getState()) && currentTarget != target) {
            currentWeaponState = weapon.getStateByName("ready");
        } else if (currentTarget == target && isAttacking) {
            // Already attacking the same target, don't start duplicate attack
            return;
        } else if (lastAttackScheduledTick == currentTick) {
            // Prevent multiple attack sequences from being scheduled in the same tick
            return;
        }
        
        // Handle first attack penalty system
        if (targetChanged || newTarget) {
            // Target changed or new target - apply first attack penalty
            isFirstAttackOnTarget = true;
        } else if (currentTarget == target) {
            // Same target as before - no first attack penalty
            isFirstAttackOnTarget = false;
        } else {
            // This shouldn't happen but be safe - treat as new target
            isFirstAttackOnTarget = true;
        }
        
        previousTarget = currentTarget;
        currentTarget = target;
        isAttacking = true;
        lastAttackScheduledTick = currentTick;
        
        // Make unit face the target and save the direction for later use
        shooter.faceToward(target.getX(), target.getY());
        
        // Calculate and save the target facing direction for weapon visibility
        double dx = target.getX() - shooter.getX();
        double dy = target.getY() - shooter.getY();
        double angleRadians = Math.atan2(dx, -dy);
        double angleDegrees = Math.toDegrees(angleRadians);
        if (angleDegrees < 0) angleDegrees += 360;
        lastTargetFacing = angleDegrees;
        scheduleAttackFromCurrentState(shooter, target, currentTick, eventQueue, ownerId, gameCallbacks);
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
        if (meleeWeapon == null) {
            return;
        }
        
        // Check if target is within melee range using edge-to-edge calculation
        double distance = Math.hypot(target.getX() - attacker.getX(), target.getY() - attacker.getY());
        double distanceFeet = distance / 7.0;
        
        if (!isInMeleeRange(attacker, target, meleeWeapon)) {
            // Target is out of range - move towards target
            attacker.setTarget(target.getX(), target.getY());
            
            // Schedule a follow-up check to attempt attack once in range
            scheduleRangeCheckForMeleeAttack(attacker, target, currentTick + 10, eventQueue, ownerId, gameCallbacks);
            return;
        }
        
        // Calculate facing direction to target
        double dx = target.getX() - attacker.getX();
        double dy = target.getY() - attacker.getY();
        double angleRadians = Math.atan2(dx, -dy);
        double angleDegrees = Math.toDegrees(angleRadians);
        if (angleDegrees < 0) angleDegrees += 360;
        lastTargetFacing = angleDegrees;
        
        // Schedule melee attack from current state
        scheduleMeleeAttackFromCurrentState(attacker, target, currentTick, eventQueue, ownerId, gameCallbacks);
        
    }
    
    private void scheduleAttackFromCurrentState(IUnit shooter, IUnit target, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        if (weapon == null || currentWeaponState == null) return;
        
        String currentState = currentWeaponState.getState();
        
        // Prevent scheduling attacks if weapon is still firing or recovering
        if ("firing".equals(currentState) || "recovering".equals(currentState)) {
            return;
        }

        // EDTODO: Verify this is no longer needed
        // long totalTimeToFire = calculateTimeToFire();
        
        if ("holstered".equals(currentState)) {
            scheduleStateTransition("drawing", currentTick, currentWeaponState.ticks, shooter, target, eventQueue, ownerId, gameCallbacks);
        } else if ("drawing".equals(currentState)) {
            scheduleStateTransition("ready", currentTick, currentWeaponState.ticks, shooter, target, eventQueue, ownerId, gameCallbacks);
        } else if ("slung".equals(currentState)) {
            scheduleStateTransition("unsling", currentTick, currentWeaponState.ticks, shooter, target, eventQueue, ownerId, gameCallbacks);
        } else if ("unsling".equals(currentState)) {
            scheduleStateTransition("ready", currentTick, currentWeaponState.ticks, shooter, target, eventQueue, ownerId, gameCallbacks);
        } else if ("sheathed".equals(currentState)) {
            scheduleStateTransition("unsheathing", currentTick, currentWeaponState.ticks, shooter, target, eventQueue, ownerId, gameCallbacks);
        } else if ("unsheathing".equals(currentState)) {
            scheduleStateTransition("ready", currentTick, currentWeaponState.ticks, shooter, target, eventQueue, ownerId, gameCallbacks);
        } else if ("ready".equals(currentState)) {
            scheduleStateTransition("aiming", currentTick, currentWeaponState.ticks, shooter, target, eventQueue, ownerId, gameCallbacks);
        } else if ("aiming".equals(currentState)) {
            // Determine which aiming speed to use based on firing mode and shot number
            AimingSpeed aimingSpeedToUse = determineAimingSpeedForShot();
            
            long adjustedAimingTime = Math.round(currentWeaponState.ticks * aimingSpeedToUse.getTimingMultiplier() * calculateAimingSpeedMultiplier());
            
            // Add random additional time for very careful aiming
            if (aimingSpeedToUse.isVeryCareful()) {
                long additionalTime = aimingSpeedToUse.getVeryCarefulAdditionalTime();
                adjustedAimingTime += additionalTime;
            }
            
            // Log aiming speed usage for burst/auto modes
            if (isAutomaticFiring && burstShotsFired > 1) {
            }
            
            scheduleFiring(shooter, target, currentTick + adjustedAimingTime, eventQueue, ownerId, gameCallbacks);
        }
    }
    
    /**
     * Schedule melee attack from current weapon state
     */
    private void scheduleMeleeAttackFromCurrentState(IUnit attacker, IUnit target, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        if (meleeWeapon == null) {
            return;
        }
        
        // Get active weapon for state management (use melee weapon's states)
        Weapon activeWeapon = getActiveWeapon();
        WeaponState currentState = currentWeaponState;
        
        if (currentState == null) {
            // Initialize to weapon's initial state if no current state
            currentState = activeWeapon.getInitialState();
            currentWeaponState = currentState;
        }
        
        String stateName = currentState != null ? currentState.getState() : "null";
        
        // Handle melee weapon state transitions
        if ("sheathed".equals(stateName)) {
            scheduleMeleeStateTransition("unsheathing", currentTick, currentState.ticks, attacker, target, eventQueue, ownerId, gameCallbacks);
        } else if ("unsheathing".equals(stateName)) {
            scheduleMeleeStateTransition("melee_ready", currentTick, currentState.ticks, attacker, target, eventQueue, ownerId, gameCallbacks);
        } else if ("melee_ready".equals(stateName)) {
            // Ready to attack - schedule the melee attack
            long attackTime = Math.round(meleeWeapon.getStateBasedAttackSpeed() * calculateAttackSpeedMultiplier());
            scheduleMeleeAttack(attacker, target, currentTick + attackTime, eventQueue, ownerId, gameCallbacks);
        } else if ("switching_to_melee".equals(stateName)) {
            scheduleMeleeStateTransition("melee_ready", currentTick, currentState.ticks, attacker, target, eventQueue, ownerId, gameCallbacks);
        } else if ("melee_attacking".equals(stateName)) {
            // Already attacking - cannot start another attack until current one completes
            return;
        } else {
            // For any other state (like "slung"), go directly to sheathed state first
            
            WeaponState sheathedState = activeWeapon.getStateByName("sheathed");
            if (sheathedState != null) {
                currentWeaponState = sheathedState;
                scheduleMeleeAttackFromCurrentState(attacker, target, currentTick, eventQueue, ownerId, gameCallbacks);
            } else {
                // Emergency fallback: use any available state or create a simple ready state
                if (activeWeapon.states != null && !activeWeapon.states.isEmpty()) {
                    WeaponState firstState = activeWeapon.states.get(0);
                    currentWeaponState = firstState;
                    scheduleMeleeAttackFromCurrentState(attacker, target, currentTick, eventQueue, ownerId, gameCallbacks);
                } else {
                    WeaponState emergencyReady = new WeaponState("melee_ready", "melee_attacking", 15);
                    currentWeaponState = emergencyReady;
                    scheduleMeleeAttackFromCurrentState(attacker, target, currentTick, eventQueue, ownerId, gameCallbacks);
                }
            }
        }
    }
    
    private void scheduleStateTransition(String newStateName, long currentTick, long transitionTickLength, IUnit shooter, IUnit target, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Apply speed multiplier only to weapon preparation states
        if (isWeaponPreparationState(newStateName)) {
            double speedMultiplier = calculateWeaponReadySpeedMultiplier();
            transitionTickLength = Math.round(transitionTickLength * speedMultiplier);
        }
        
        long transitionTick = currentTick + transitionTickLength;
        eventQueue.add(new ScheduledEvent(transitionTick, () -> {
            currentWeaponState = weapon.getStateByName(newStateName);
            scheduleAttackFromCurrentState(shooter, target, transitionTick, eventQueue, ownerId, gameCallbacks);
        }, ownerId));
    }
    
    private void scheduleFiring(IUnit shooter, IUnit target, long fireTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Prevent duplicate firing events for the same tick
        if (lastFiringScheduledTick == fireTick) {
            return;
        }
        lastFiringScheduledTick = fireTick;
        
        eventQueue.add(new ScheduledEvent(fireTick, () -> {
            currentWeaponState = weapon.getStateByName("firing");
            
            if (weapon instanceof RangedWeapon && ((RangedWeapon)weapon).getAmmunition() <= 0) {
            } else if (weapon instanceof RangedWeapon) {
                ((RangedWeapon)weapon).setAmmunition(((RangedWeapon)weapon).getAmmunition() - 1);
                
                if (gameCallbacks != null) {
                    gameCallbacks.playWeaponSound(weapon);
                    gameCallbacks.applyFiringHighlight((Unit)shooter, fireTick);
                    gameCallbacks.addMuzzleFlash((Unit)shooter, fireTick);
                } else {
                    System.err.println("CRITICAL ERROR: gameCallbacks is null in " + getDisplayName() + " firing sequence - audio and visual effects disabled");
                }
                
                double dx = target.getX() - shooter.getX();
                double dy = target.getY() - shooter.getY();
                double distancePixels = Math.hypot(dx, dy);
                double distanceFeet = distancePixels / 7.0; // pixelsToFeet conversion
                
                if (gameCallbacks != null) {
                    gameCallbacks.scheduleProjectileImpact((Unit)shooter, (Unit)target, weapon, fireTick, distanceFeet);
                } else {
                    System.err.println("CRITICAL ERROR: gameCallbacks is null in " + getDisplayName() + " projectile impact scheduling - hit detection disabled");
                }
                
                // Handle burst firing - schedule additional shots immediately after first shot
                if (weapon instanceof RangedWeapon && ((RangedWeapon)weapon).getCurrentFiringMode() == FiringMode.BURST && !isAutomaticFiring) {
                    isAutomaticFiring = true;
                    burstShotsFired = 1; // First shot just fired
                    lastAutomaticShot = fireTick;
                    
                    // Schedule remaining shots in the burst
                    for (int shot = 2; shot <= ((RangedWeapon)weapon).getBurstSize(); shot++) {
                        long nextShotTick = fireTick + (((RangedWeapon)weapon).getFiringDelay() * (shot - 1));
                        final int shotNumber = shot;
                        eventQueue.add(new ScheduledEvent(nextShotTick, () -> {
                            // DC-24: Continue burst even if target dies (fires at corpse) or shooter incapacitated, but stop if out of ammo
                            if (currentTarget != null && weapon instanceof RangedWeapon && ((RangedWeapon)weapon).getAmmunition() > 0) {
                                ((RangedWeapon)weapon).setAmmunition(((RangedWeapon)weapon).getAmmunition() - 1);
                                burstShotsFired++;
                                String targetStatus = currentTarget.getCharacter().isIncapacitated() ? " at incapacitated target" : "";
                                
                                gameCallbacks.playWeaponSound(weapon);
                                gameCallbacks.applyFiringHighlight((Unit)shooter, nextShotTick);
                                gameCallbacks.addMuzzleFlash((Unit)shooter, nextShotTick);
                                
                                double dx2 = currentTarget.getX() - shooter.getX();
                                double dy2 = currentTarget.getY() - shooter.getY();
                                double distancePixels2 = Math.hypot(dx2, dy2);
                                double distanceFeet2 = distancePixels2 / 7.0;
                                
                                gameCallbacks.scheduleProjectileImpact((Unit)shooter, (Unit)currentTarget, weapon, nextShotTick, distanceFeet2);
                                
                                // Reset burst state after final shot
                                if (burstShotsFired >= ((RangedWeapon)weapon).getBurstSize()) {
                                    isAutomaticFiring = false;
                                    burstShotsFired = 0;
                                }
                            } else {
                                // Burst interrupted
                                isAutomaticFiring = false;
                                burstShotsFired = 0;
                            }
                        }, ownerId));
                    }
                }
            }
            
            WeaponState firingState = weapon.getStateByName("firing");
            eventQueue.add(new ScheduledEvent(fireTick + firingState.ticks, () -> {
                currentWeaponState = weapon.getStateByName("recovering");
                
                WeaponState recoveringState = weapon.getStateByName("recovering");
                eventQueue.add(new ScheduledEvent(fireTick + firingState.ticks + recoveringState.ticks, () -> {
                    if (weapon instanceof RangedWeapon && ((RangedWeapon)weapon).getAmmunition() <= 0 && canReload() && !isReloading) {
                        isAttacking = false; // Clear attacking flag during reload
                        startReloadSequence(shooter, fireTick + firingState.ticks + recoveringState.ticks, eventQueue, ownerId, gameCallbacks);
                    } else {
                        long completionTick = fireTick + firingState.ticks + recoveringState.ticks;
                        currentWeaponState = weapon.getStateByName("aiming");
                        isAttacking = false; // Attack sequence complete
                        
                        // Only call checkContinuousAttack if NOT using persistent attack mode
                        // Persistent attack is handled entirely by continueStandardAttack scheduling
                        if (!persistentAttack) {
                            checkContinuousAttack(shooter, completionTick, eventQueue, ownerId, gameCallbacks);
                        } else {
                        }
                    }
                }, ownerId));
            }, ownerId));
            
        }, ownerId));
    }
    
    private void scheduleReadyFromCurrentState(IUnit unit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        // Check weapon and state availability for both ranged and melee
        if (currentWeaponState == null) return;
        if (!isMeleeCombatMode && weapon == null) return;
        if (isMeleeCombatMode && meleeWeapon == null) return;
        
        String currentState = currentWeaponState.getState();
        String targetReadyState = isMeleeCombatMode ? "melee_ready" : "ready";
        
        if (targetReadyState.equals(currentState)) {
            return;
        }
        
        // Handle weapon state transitions for both ranged and melee
        if ("holstered".equals(currentState)) {
            scheduleReadyStateTransition("drawing", currentTick, currentWeaponState.ticks, unit, eventQueue, ownerId);
        } else if ("drawing".equals(currentState)) {
            scheduleReadyStateTransition(targetReadyState, currentTick, currentWeaponState.ticks, unit, eventQueue, ownerId);
        } else if ("slung".equals(currentState)) {
            scheduleReadyStateTransition("unsling", currentTick, currentWeaponState.ticks, unit, eventQueue, ownerId);
        } else if ("unsling".equals(currentState)) {
            scheduleReadyStateTransition(targetReadyState, currentTick, currentWeaponState.ticks, unit, eventQueue, ownerId);
        } else if ("sheathed".equals(currentState)) {
            scheduleReadyStateTransition("unsheathing", currentTick, currentWeaponState.ticks, unit, eventQueue, ownerId);
        } else if ("unsheathing".equals(currentState)) {
            scheduleReadyStateTransition(targetReadyState, currentTick, currentWeaponState.ticks, unit, eventQueue, ownerId);
        } else if ("aiming".equals(currentState) || "firing".equals(currentState) || "recovering".equals(currentState)) {
            // Get the appropriate weapon for state lookup
            Weapon activeWeapon = isMeleeCombatMode ? meleeWeapon : weapon;
            WeaponState readyState = activeWeapon.getStateByName(targetReadyState);
            eventQueue.add(new ScheduledEvent(currentTick + currentWeaponState.ticks, () -> {
                currentWeaponState = readyState;
            }, ownerId));
        } else {
            // For unknown states, try direct transition to ready state
            Weapon activeWeapon = isMeleeCombatMode ? meleeWeapon : weapon;
            WeaponState readyState = activeWeapon.getStateByName(targetReadyState);
            if (readyState != null) {
                currentWeaponState = readyState;
            }
        }
    }
    
    public double calculateWeaponReadySpeedMultiplier() {
        int reflexesModifier = GameConstants.statToModifier(this.reflexes);
        double reflexesSpeedMultiplier = 1.0 - (reflexesModifier * 0.015);
        
        int quickdrawLevel = getSkillLevel(SkillsManager.QUICKDRAW);
        double quickdrawSpeedMultiplier = 1.0 - (quickdrawLevel * 0.08);
        
        return reflexesSpeedMultiplier * quickdrawSpeedMultiplier;
    }
    
    private double calculateAimingSpeedMultiplier() {
        // Apply 25% of the weapon ready speed bonus to aiming
        double weaponReadyMultiplier = calculateWeaponReadySpeedMultiplier();
        double speedBonus = 1.0 - weaponReadyMultiplier;
        double aimingSpeedBonus = speedBonus * 0.25;
        return 1.0 - aimingSpeedBonus;
    }
    
    private void scheduleReadyStateTransition(String newStateName, long currentTick, long transitionTickLength, IUnit unit, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        // Apply speed multiplier only to weapon preparation states
        if (isWeaponPreparationState(newStateName)) {
            double speedMultiplier = calculateWeaponReadySpeedMultiplier();
            transitionTickLength = Math.round(transitionTickLength * speedMultiplier);
        }
        
        long transitionTick = currentTick + transitionTickLength;
        
        eventQueue.add(new ScheduledEvent(transitionTick, () -> {
            // Get the appropriate weapon for state lookup
            Weapon activeWeapon = isMeleeCombatMode ? meleeWeapon : weapon;
            currentWeaponState = activeWeapon.getStateByName(newStateName);
            
            // Continue the ready sequence recursively
            scheduleReadyFromCurrentState(unit, transitionTick, eventQueue, ownerId);
        }, ownerId));
    }
    
    public boolean isWeaponPreparationState(String stateName) {
        return "drawing".equals(stateName) || "unsheathing".equals(stateName) || "unsling".equals(stateName) || 
               "ready".equals(stateName) || "melee_ready".equals(stateName);
    }
    
    private AimingSpeed determineAimingSpeedForShot() {
        // Always use current aiming speed - burst/auto penalty is applied separately
        return currentAimingSpeed;
    }
    
    /**
     * Check if the current shot should have burst/auto quick penalty
     */
    public boolean shouldApplyBurstAutoPenalty() {
        if (weapon == null || !(weapon instanceof RangedWeapon)) {
            return false;
        }
        
        FiringMode mode = ((RangedWeapon)weapon).getCurrentFiringMode();
        if (mode == FiringMode.BURST || mode == FiringMode.FULL_AUTO) {
            // Apply penalty to bullets 2+ in burst/auto
            return isAutomaticFiring && burstShotsFired > 1;
        }
        
        return false;
    }
    
    public double getWeaponReadySpeedMultiplier() {
        return calculateWeaponReadySpeedMultiplier();
    }
    
    /**
     * Schedule melee state transition
     */
    private void scheduleMeleeStateTransition(String newStateName, long currentTick, long transitionTickLength, IUnit attacker, IUnit target, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        
        // Apply speed multiplier to weapon preparation states
        if (isWeaponPreparationState(newStateName)) {
            double speedMultiplier = calculateWeaponReadySpeedMultiplier();
            transitionTickLength = Math.round(transitionTickLength * speedMultiplier);
        }
        
        Weapon activeWeapon = getActiveWeapon();
        
        WeaponState newState = activeWeapon != null ? activeWeapon.getStateByName(newStateName) : null;
        
        if (newState != null) {
            // Create final copies for lambda
            final String finalStateName = newStateName;
            final long finalTick = currentTick + transitionTickLength;
            
            eventQueue.add(new ScheduledEvent(finalTick, () -> {
                currentWeaponState = newState;
                
                // Continue the attack sequence
                scheduleMeleeAttackFromCurrentState(attacker, target, finalTick, eventQueue, ownerId, gameCallbacks);
            }, ownerId));
            
        } else {
            // Fallback: skip to melee_ready state immediately
            WeaponState readyState = activeWeapon != null ? activeWeapon.getStateByName("melee_ready") : null;
            if (readyState != null) {
                currentWeaponState = readyState;
                scheduleMeleeAttackFromCurrentState(attacker, target, currentTick, eventQueue, ownerId, gameCallbacks);
            } else {
            }
        }
    }
    
    /**
     * Schedule range check for melee attack - continues tracking target until in range
     */
    private void scheduleRangeCheckForMeleeAttack(IUnit attacker, IUnit target, long checkTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        eventQueue.add(new ScheduledEvent(checkTick, () -> {
            // Update movement target to track target's current position
            attacker.setTarget(target.getX(), target.getY());
            
            // Calculate current distance for debug
            double distance = Math.hypot(target.getX() - attacker.getX(), target.getY() - attacker.getY());
            double distanceFeet = distance / 7.0;
            double weaponReach = meleeWeapon.getTotalReach();
            
            // Check if now in range
            if (isInMeleeRange(attacker, target, meleeWeapon)) {
                // Now in range - proceed with attack
                
                // Calculate facing direction to target
                double dx = target.getX() - attacker.getX();
                double dy = target.getY() - attacker.getY();
                double angleRadians = Math.atan2(dx, -dy);
                double angleDegrees = Math.toDegrees(angleRadians);
                if (angleDegrees < 0) angleDegrees += 360;
                lastTargetFacing = angleDegrees;
                
                // Schedule melee attack from current state
                scheduleMeleeAttackFromCurrentState(attacker, target, checkTick, eventQueue, ownerId, gameCallbacks);
            } else {
                // Still not in range - schedule another check
                scheduleRangeCheckForMeleeAttack(attacker, target, checkTick + 10, eventQueue, ownerId, gameCallbacks);
            }
        }, ownerId));
    }

    /**
     * Schedule actual melee attack execution
     */
    private void scheduleMeleeAttack(IUnit attacker, IUnit target, long attackTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        
        eventQueue.add(new ScheduledEvent(attackTick, () -> {
            
            // Validate target is still valid
            if (target.getCharacter().isIncapacitated()) {
                return;
            }
            
            if (isIncapacitated()) {
                return;
            }
            
            // Update weapon state to attacking
            WeaponState attackingState = getActiveWeapon().getStateByName("melee_attacking");
            if (attackingState != null) {
                currentWeaponState = attackingState;
            } else {
            }
            
            
            // Play melee weapon sound effect (DevCycle 12)
            gameCallbacks.playWeaponSound(meleeWeapon);
            
            // Schedule immediate impact (no travel time for melee)
            gameCallbacks.scheduleMeleeImpact((Unit)attacker, (Unit)target, meleeWeapon, attackTick);
            
            // Schedule recovery back to ready state
            long recoveryTime = Math.round(meleeWeapon.getStateBasedAttackCooldown() * calculateAttackSpeedMultiplier());
            
            WeaponState readyState = getActiveWeapon().getStateByName("melee_ready");
            if (readyState != null) {
                eventQueue.add(new ScheduledEvent(attackTick + recoveryTime, () -> {
                    currentWeaponState = readyState;
                    isAttacking = false; // Clear attacking flag to allow auto-targeting to continue
                    
                    // Additional debug: check if auto-targeting should continue
                    if (usesAutomaticTargeting) {
                    }
                    
                    // Call checkContinuousAttack to trigger auto-targeting re-evaluation (similar to ranged weapon recovery)
                    checkContinuousAttack(attacker, attackTick + recoveryTime, eventQueue, ownerId, gameCallbacks);
                }, ownerId));
            }
        }, ownerId));
    }
    
    /**
     * Calculate attack speed multiplier based on character stats and skills
     */
    private double calculateAttackSpeedMultiplier() {
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
        
        isReloading = true; // Set reload flag to prevent duplicates
        
        currentWeaponState = weapon.getStateByName("reloading");
        
        long reloadTicks = calculateReloadSpeed();
        long reloadCompleteTick = currentTick + reloadTicks;
        
        eventQueue.add(new ScheduledEvent(reloadCompleteTick, () -> {
            performReload();
            
            // Continue reloading if needed for single-round weapons
            if (weapon instanceof RangedWeapon && ((RangedWeapon)weapon).getReloadType() == ReloadType.SINGLE_ROUND && ((RangedWeapon)weapon).getAmmunition() < ((RangedWeapon)weapon).getMaxAmmunition()) {
                continueReloading(unit, reloadCompleteTick, eventQueue, ownerId, gameCallbacks);
            } else {
                isReloading = false; // Clear reload flag when finished
                currentWeaponState = weapon.getStateByName("ready");
                
                // Only call checkContinuousAttack if NOT using persistent attack mode
                // Persistent attack resumes via continueStandardAttack scheduling
                if (!persistentAttack) {
                    checkContinuousAttack(unit, reloadCompleteTick, eventQueue, ownerId, gameCallbacks);
                } else {
                }
            }
        }, ownerId));
    }
    
    private void continueReloading(IUnit unit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        if (weapon == null || !(weapon instanceof RangedWeapon) || ((RangedWeapon)weapon).getAmmunition() >= ((RangedWeapon)weapon).getMaxAmmunition()) {
            isReloading = false; // Clear reload flag when stopping
            currentWeaponState = weapon.getStateByName("ready");
            return;
        }
        
        long reloadTicks = calculateReloadSpeed();
        long reloadCompleteTick = currentTick + reloadTicks;
        
        eventQueue.add(new ScheduledEvent(reloadCompleteTick, () -> {
            performReload();
            
            // Continue reloading if still not full
            if (weapon instanceof RangedWeapon && ((RangedWeapon)weapon).getAmmunition() < ((RangedWeapon)weapon).getMaxAmmunition()) {
                continueReloading(unit, reloadCompleteTick, eventQueue, ownerId, gameCallbacks);
            } else {
                isReloading = false; // Clear reload flag when finished
                currentWeaponState = weapon.getStateByName("ready");
                
                // Only call checkContinuousAttack if NOT using persistent attack mode
                // Persistent attack resumes via continueStandardAttack scheduling
                if (!persistentAttack) {
                    checkContinuousAttack(unit, reloadCompleteTick, eventQueue, ownerId, gameCallbacks);
                } else {
                }
            }
        }, ownerId));
    }
    
    private long calculateReloadSpeed() {
        int reflexesModifier = GameConstants.statToModifier(this.reflexes);
        double reflexesSpeedMultiplier = 1.0 - (reflexesModifier * 0.01);
        return weapon instanceof RangedWeapon ? Math.round(((RangedWeapon)weapon).getReloadTicks() * reflexesSpeedMultiplier) : 60;
    }
    
    private void performReload() {
        if (weapon instanceof RangedWeapon) {
            RangedWeapon rangedWeapon = (RangedWeapon)weapon;
            if (rangedWeapon.getReloadType() == ReloadType.SINGLE_ROUND) {
                rangedWeapon.setAmmunition(Math.min(rangedWeapon.getAmmunition() + 1, rangedWeapon.getMaxAmmunition()));
            } else {
                rangedWeapon.setAmmunition(rangedWeapon.getMaxAmmunition());
            }
        }
    }
    
    public boolean isHostileTo(Character other) {
        return this.faction != other.faction;
    }

    private IUnit findNearestHostileTargetWithZonePriority(IUnit selfUnit, GameCallbacks gameCallbacks) {
        List<Unit> allUnits = gameCallbacks.getUnits();
        IUnit nearestZoneTarget = null;
        IUnit nearestGlobalTarget = null;
        double nearestZoneDistance = Double.MAX_VALUE;
        double nearestGlobalDistance = Double.MAX_VALUE;
        int hostilesFound = 0;
        java.util.Random random = new java.util.Random();
        
        for (IUnit unit : allUnits) {
            // Skip self
            if (unit == selfUnit) continue;
            
            // Skip if not hostile (same faction)
            if (!this.isHostileTo(unit.getCharacter())) {
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
            if (weapon != null && distance / 7.0 > ((RangedWeapon)weapon).getMaximumRange()) {
                continue; // Skip targets beyond weapon range
            }
            
            // Check if target is within target zone (if zone exists)
            boolean inTargetZone = false;
            if (targetZone != null) {
                inTargetZone = targetZone.contains((int)unit.getX(), (int)unit.getY());
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
    
    private void performAutomaticTargetChange(IUnit shooter, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
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
        IUnit newTarget = findNearestHostileTargetWithZonePriority(shooter, gameCallbacks);
        
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
        
        // Only execute if automatic targeting is enabled
        if (!usesAutomaticTargeting) {
            return;
        }
        
        // Skip if character is incapacitated
        if (this.isIncapacitated()) {
            return;
        }
        
        // Skip if character has no weapon
        if (weapon == null) {
            return;
        }
        
        // Skip if character is already attacking (let existing attack complete)
        if (isAttacking) {
            return;
        }
        
        // Skip if character is reloading (let reload complete)
        if (isReloading) {
            return;
        }
        
        // Check if current target is still valid
        boolean currentTargetValid = currentTarget != null 
            && !currentTarget.getCharacter().isIncapacitated() 
            && this.isHostileTo(currentTarget.getCharacter());
        
        if (!currentTargetValid) {
            // Find a new target with target zone priority
            IUnit newTarget = findNearestHostileTargetWithZonePriority(selfUnit, gameCallbacks);
            
            if (newTarget != null) {
                // Target found - start attacking
                persistentAttack = true;
                currentTarget = newTarget; // DevCycle 22: Fix auto targeting infinite loop by setting currentTarget
                
                // Calculate distance for logging
                double dx = newTarget.getX() - selfUnit.getX();
                double dy = newTarget.getY() - selfUnit.getY();
                double distanceFeet = Math.hypot(dx, dy) / 7.0; // Convert pixels to feet
                
                String zoneStatus = (targetZone != null && targetZone.contains((int)newTarget.getX(), (int)newTarget.getY())) ? " (in target zone)" : "";
                
                // Start attack sequence - check combat mode to determine attack type
                if (isMeleeCombatMode() && meleeWeapon != null) {
                    // Check if already in melee range
                    double distance = Math.hypot(newTarget.getX() - selfUnit.getX(), newTarget.getY() - selfUnit.getY());
                    double meleeRangePixels = meleeWeapon.getTotalReach() * 7.0; // Convert feet to pixels
                    
                    if (distance <= meleeRangePixels) {
                        // Already in range, attack immediately
                        startMeleeAttackSequence(selfUnit, newTarget, currentTick, eventQueue, selfUnit.getId(), gameCallbacks);
                    } else {
                        // Move to melee range first
                        // Set melee movement target - the updateMeleeMovement method will handle the attack when in range
                        isMovingToMelee = true;
                        meleeTarget = newTarget;
                        lastMeleeMovementUpdate = currentTick;
                        
                        // Task #9: Ready melee weapon during movement (like manual attacks)
                        if (meleeWeapon != null) {
                            startReadyWeaponSequence(selfUnit, currentTick, eventQueue, selfUnit.getId());
                        }
                    }
                } else {
                    startAttackSequence(selfUnit, newTarget, currentTick, eventQueue, selfUnit.getId(), gameCallbacks);
                }
            } else {
                // No targets found - disable persistent attack but maintain weapon direction
                if (persistentAttack) {
                    persistentAttack = false;
                    currentTarget = null;
                    
                    // Preserve the current facing direction so weapon continues to aim at last target location
                    if (lastTargetFacing != null && selfUnit != null) {
                        selfUnit.setTargetFacing(lastTargetFacing);
                    }
                    
                }
            }
        } else {
            // Handle case where we have a valid target but need to initiate/continue attack
            
            // Set persistent attack for auto-targeting continuation if not already set
            if (!persistentAttack) {
                persistentAttack = true;
            }
            
            // Only initiate attack sequence if not already in progress
            if (isMovingToMelee || isAttacking) {
                return;
            }
            
            // Calculate distance for logging
            double dx = currentTarget.getX() - selfUnit.getX();
            double dy = currentTarget.getY() - selfUnit.getY();
            double distanceFeet = Math.hypot(dx, dy) / 7.0; // Convert pixels to feet
            
            String zoneStatus = (targetZone != null && targetZone.contains((int)currentTarget.getX(), (int)currentTarget.getY())) ? " (in target zone)" : "";
            
            // Start attack sequence - check combat mode to determine attack type
            if (isMeleeCombatMode() && meleeWeapon != null) {
                CharacterDebugUtils.autoTargetDebugPrintAlways(this, "[AUTO-TARGET] " + getDisplayName() + " starting MELEE attack sequence");
                // Check if already in melee range
                double distance = Math.hypot(currentTarget.getX() - selfUnit.getX(), currentTarget.getY() - selfUnit.getY());
                double meleeRangePixels = meleeWeapon.getTotalReach() * 7.0; // Convert feet to pixels
                
                if (distance <= meleeRangePixels) {
                    // Already in range, attack immediately
                    startMeleeAttackSequence(selfUnit, currentTarget, currentTick, eventQueue, selfUnit.getId(), gameCallbacks);
                } else {
                    // Move to melee range first
                    // Set melee movement target - the updateMeleeMovement method will handle the attack when in range
                    isMovingToMelee = true;
                    meleeTarget = currentTarget;
                    lastMeleeMovementUpdate = currentTick;
                    
                    // Task #9: Ready melee weapon during movement (like manual attacks)
                    if (meleeWeapon != null) {
                        startReadyWeaponSequence(selfUnit, currentTick, eventQueue, selfUnit.getId());
                    }
                }
            } else {
                CharacterDebugUtils.autoTargetDebugPrintAlways(this, "[AUTO-TARGET] " + getDisplayName() + " starting RANGED attack sequence");
                startAttackSequence(selfUnit, currentTarget, currentTick, eventQueue, selfUnit.getId(), gameCallbacks);
            }
        }
    }
    
    /**
     * Update melee movement progress and trigger attack when target is reached
     */
    public void updateMeleeMovement(IUnit selfUnit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, GameCallbacks gameCallbacks) {
        MeleeCombatManager.updateMeleeMovement(this, selfUnit, currentTick, eventQueue, selfUnit.getId(), gameCallbacks);
    }
    
    private void checkContinuousAttack(IUnit shooter, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Debug logging for checkContinuousAttack entry
        
        // Continue only if persistent attack is enabled OR auto-targeting is enabled
        if (!persistentAttack && !usesAutomaticTargeting) {
            return;
        }
        
        // Skip if character is reloading (let reload complete)
        if (isReloading) {
            return;
        }
        
        // Handle case where we have auto-targeting enabled but no current target
        if (currentTarget == null) {
            if (usesAutomaticTargeting) {
                // Delegate to the auto-targeting system to find a new target
                updateAutomaticTargeting(shooter, currentTick, eventQueue, gameCallbacks);
                return;
            } else {
                return;
            }
        }
        if (currentTarget.getCharacter().isIncapacitated()) {
            // Target incapacitated - schedule automatic target change after 1 second delay
            
            // Schedule target reassessment event 1 second later (60 ticks)
            long retargetTick = currentTick + 60;
            eventQueue.add(new ScheduledEvent(retargetTick, () -> {
                performAutomaticTargetChange(shooter, retargetTick, eventQueue, ownerId, gameCallbacks);
            }, ownerId));
            
            // Clear current target but maintain persistent attack mode and weapon direction
            currentTarget = null;
            isAttacking = false;
            
            // Preserve the current facing direction so weapon continues to aim at last target location
            if (lastTargetFacing != null) {
                shooter.setTargetFacing(lastTargetFacing);
            }
            return;
        }
        if (this.isIncapacitated()) {
            persistentAttack = false;
            currentTarget = null;
            isAttacking = false;
            
            // Preserve the current facing direction so weapon continues to aim at last target location
            if (lastTargetFacing != null && shooter != null) {
                shooter.setTargetFacing(lastTargetFacing);
            }
            return;
        }
        if (weapon == null) {
            persistentAttack = false;
            currentTarget = null;
            isAttacking = false;
            
            // Preserve the current facing direction so weapon continues to aim at last target location
            if (lastTargetFacing != null && shooter != null) {
                shooter.setTargetFacing(lastTargetFacing);
            }
            return;
        }
        
        // Handle different firing modes for continuous attacks
        handleContinuousFiring(shooter, currentTick, eventQueue, ownerId, gameCallbacks);
    }
    
    private void handleContinuousFiring(IUnit shooter, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        if (weapon == null || ((RangedWeapon)weapon).getCurrentFiringMode() == null) {
            // Default behavior for weapons without firing modes
            continueStandardAttack(shooter, currentTick, eventQueue, ownerId, gameCallbacks);
            return;
        }
        
        switch (((RangedWeapon)weapon).getCurrentFiringMode()) {
            case SINGLE_SHOT:
                // Single shot mode - use standard firing delay
                continueStandardAttack(shooter, currentTick, eventQueue, ownerId, gameCallbacks);
                break;
                
            case BURST:
                // Burst mode - fire predetermined number of rounds quickly
                handleBurstFiring(shooter, currentTick, eventQueue, ownerId, gameCallbacks);
                break;
                
            case FULL_AUTO:
                // Full auto mode - continuous firing at cyclic rate
                handleFullAutoFiring(shooter, currentTick, eventQueue, ownerId, gameCallbacks);
                break;
        }
    }
    
    private void continueStandardAttack(IUnit shooter, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Prevent duplicate continue attack commands for the same tick
        if (lastContinueAttackTick == currentTick) {
            return;
        }
        lastContinueAttackTick = currentTick;
        
        if (((RangedWeapon)weapon).getFiringDelay() > 0) {
            long nextAttackTick = currentTick + ((RangedWeapon)weapon).getFiringDelay();
            eventQueue.add(new ScheduledEvent(nextAttackTick, () -> {
                if (persistentAttack && currentTarget != null && !currentTarget.getCharacter().isIncapacitated() && !this.isIncapacitated() && 
                    weapon instanceof RangedWeapon && ((RangedWeapon)weapon).getAmmunition() > 0) {
                    isAttacking = true;
                    scheduleAttackFromCurrentState(shooter, currentTarget, nextAttackTick, eventQueue, ownerId, gameCallbacks);
                } else if (persistentAttack && currentTarget != null && !currentTarget.getCharacter().isIncapacitated() && !this.isIncapacitated() && 
                          weapon instanceof RangedWeapon && ((RangedWeapon)weapon).getAmmunition() <= 0 && canReload() && !isReloading) {
                    startReloadSequence(shooter, nextAttackTick, eventQueue, ownerId, gameCallbacks);
                }
            }, ownerId));
        } else {
            if (weapon instanceof RangedWeapon && ((RangedWeapon)weapon).getAmmunition() > 0) {
                isAttacking = true;
                scheduleAttackFromCurrentState(shooter, currentTarget, currentTick, eventQueue, ownerId, gameCallbacks);
            } else if (weapon instanceof RangedWeapon && ((RangedWeapon)weapon).getAmmunition() <= 0 && canReload() && !isReloading) {
                startReloadSequence(shooter, currentTick, eventQueue, ownerId, gameCallbacks);
            }
        }
    }
    
    private void handleBurstFiring(IUnit shooter, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Check if a burst is already in progress from the new burst implementation
        if (isAutomaticFiring) {
            // Burst already in progress from scheduleFiring() method - wait for it to complete
            
            // Calculate when current burst will complete and schedule next burst
            int remainingShots = ((RangedWeapon)weapon).getBurstSize() - burstShotsFired;
            if (remainingShots > 0) {
                // Schedule next burst after current burst completes + firing delay
                // Full burst duration = (burstSize - 1) * cyclicRate + firing delay
                long fullBurstDuration = (((RangedWeapon)weapon).getBurstSize() - 1) * ((RangedWeapon)weapon).getCyclicRate();
                long nextBurstTick = lastAutomaticShot + fullBurstDuration + ((RangedWeapon)weapon).getFiringDelay();
                
                // Ensure we don't schedule in the past
                if (nextBurstTick <= currentTick) {
                    nextBurstTick = currentTick + ((RangedWeapon)weapon).getFiringDelay();
                }
                
                final long finalNextBurstTick = nextBurstTick;
                eventQueue.add(new ScheduledEvent(finalNextBurstTick, () -> {
                    if (persistentAttack && currentTarget != null && !currentTarget.getCharacter().isIncapacitated() && !this.isIncapacitated()) {
                        isAttacking = true;
                        startAttackSequence(shooter, currentTarget, finalNextBurstTick, eventQueue, ownerId, gameCallbacks);
                    }
                }, ownerId));
            }
            return;
        }
        
        // No burst in progress - start new attack sequence which will trigger burst via scheduleFiring()
        if (currentTarget != null && !currentTarget.getCharacter().isIncapacitated() && !this.isIncapacitated()) {
            // Check if we can start new attack sequence without clearing isAttacking flag
            if (isAttacking) {
                // Already attacking - don't start duplicate sequence
                return;
            }
            startAttackSequence(shooter, currentTarget, currentTick, eventQueue, ownerId, gameCallbacks);
        } else {
        }
    }
    
    private void handleFullAutoFiring(IUnit shooter, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        if (!isAutomaticFiring) {
            // Start new full auto sequence
            isAutomaticFiring = true;
            burstShotsFired = 1; // First shot already fired
            lastAutomaticShot = currentTick;
        } else {
            // Continue full auto - increment shot count
            burstShotsFired++;
        }
        
        // Schedule next shot at firing delay
        long nextShotTick = currentTick + ((RangedWeapon)weapon).getFiringDelay();
        eventQueue.add(new ScheduledEvent(nextShotTick, () -> {
            // DC-24: Continue full-auto even if shooter incapacitated (but not if target incapacitated)
            if (persistentAttack && currentTarget != null && !currentTarget.getCharacter().isIncapacitated()) {
                lastAutomaticShot = nextShotTick;
                isAttacking = true;
                scheduleAttackFromCurrentState(shooter, currentTarget, nextShotTick, eventQueue, ownerId, gameCallbacks);
            } else {
                // Stop automatic firing if conditions not met
                isAutomaticFiring = false;
                burstShotsFired = 0;
            }
        }, ownerId));
    }
    
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
        // Both legs wounded: can only crawl
        if (hasBothLegsWounded()) {
            return MovementType.CRAWL;
        }
        
        // Single leg wound: cannot run
        if (hasAnyLegWound()) {
            return MovementType.JOG; // Can walk, jog, crawl but not run
        }
        
        // No leg wounds: no movement restrictions
        return MovementType.RUN;
    }
    
    public void enforceMovementRestrictions() {
        MovementType maxAllowed = getMaxAllowedMovementType();
        
        // If current movement type exceeds what's allowed, force it down
        if (currentMovementType.ordinal() > maxAllowed.ordinal()) {
            currentMovementType = maxAllowed;
        }
        
        // Force prone if both legs are wounded
        if (hasBothLegsWounded() && currentPosition != PositionState.PRONE) {
            currentPosition = PositionState.PRONE;
            currentMovementType = MovementType.CRAWL;
        }
    }
    
    /**
     * Check if target is within melee range of attacker using edge-to-edge distance
     */
    private boolean isInMeleeRange(IUnit attacker, IUnit target, MeleeWeapon weapon) {
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
        return currentDefenseState;
    }
    
    /**
     * Sets the defense state
     * @param state New defense state
     */
    public void setDefenseState(DefenseState state) {
        this.currentDefenseState = state;
    }
    
    /**
     * Checks if character can defend (not in cooldown or mid-counter-attack)
     * @return true if character can defend
     */
    public boolean canDefend(long currentTick) {
        return currentDefenseState == DefenseState.READY && currentTick >= defenseCooldownEndTick;
    }
    
    /**
     * Starts defense cooldown
     * @param cooldownTicks Duration of cooldown in ticks
     * @param currentTick Current game tick
     */
    public void startDefenseCooldown(int cooldownTicks, long currentTick) {
        currentDefenseState = DefenseState.COOLDOWN;
        defenseCooldownEndTick = currentTick + cooldownTicks;
    }
    
    /**
     * Updates defense state based on current tick
     * @param currentTick Current game tick
     */
    public void updateDefenseState(long currentTick) {
        if (currentDefenseState == DefenseState.COOLDOWN && currentTick >= defenseCooldownEndTick) {
            currentDefenseState = DefenseState.READY;
        }
        
        // Clear counter-attack opportunity if window expired
        if (hasCounterAttackOpportunity && currentTick >= counterAttackWindowEndTick) {
            hasCounterAttackOpportunity = false;
        }
    }
    
    /**
     * Grants a counter-attack opportunity
     * @param windowDurationTicks Duration of counter-attack window in ticks
     * @param currentTick Current game tick
     */
    public void grantCounterAttackOpportunity(int windowDurationTicks, long currentTick) {
        hasCounterAttackOpportunity = true;
        counterAttackWindowEndTick = currentTick + windowDurationTicks;
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

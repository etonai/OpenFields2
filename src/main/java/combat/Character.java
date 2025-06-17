package combat;

import game.ScheduledEvent;
import game.Unit;
import game.GameCallbacks;
import data.SkillsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class Character {
    public int id;
    public String nickname;
    public String firstName;
    public String lastName;
    public Date birthdate;
    public String themeId;
    public int dexterity;
    public int currentDexterity;
    public int health;
    public int currentHealth;
    public int coolness;
    public int strength;
    public int reflexes;
    public Handedness handedness;
    public double baseMovementSpeed;
    public MovementType currentMovementType;
    public AimingSpeed currentAimingSpeed;
    public PositionState currentPosition;
    public Weapon weapon; // Legacy field - will become rangedWeapon
    public RangedWeapon rangedWeapon; // Primary ranged weapon
    public MeleeWeapon meleeWeapon; // Primary melee weapon
    public boolean isMeleeCombatMode = false; // True when in melee combat mode
    public WeaponState currentWeaponState;
    public Unit currentTarget;
    public boolean persistentAttack;
    public boolean isAttacking;
    public int faction;
    public boolean usesAutomaticTargeting;
    public FiringMode preferredFiringMode;
    public List<Skill> skills;
    public List<Wound> wounds;
    
    // Combat Experience Tracking
    public int combatEngagements = 0;           // Manual tracking (no auto-update yet)
    public int woundsReceived = 0;              // Auto-updated when addWound() called
    public int woundsInflictedScratch = 0;      // Auto-updated on successful hits
    public int woundsInflictedLight = 0;        // Auto-updated on successful hits  
    public int woundsInflictedSerious = 0;      // Auto-updated on successful hits
    public int woundsInflictedCritical = 0;     // Auto-updated on successful hits
    public int attacksAttempted = 0;            // Auto-updated when attacks are attempted
    public int attacksSuccessful = 0;           // Auto-updated when attacks hit
    public int targetsIncapacitated = 0;        // Auto-updated when targets become incapacitated
    
    // Headshot Statistics
    public int headshotsAttempted = 0;          // Auto-updated when attacks target the head
    public int headshotsSuccessful = 0;         // Auto-updated when headshots hit
    public int headshotsKills = 0;              // Auto-updated when headshots result in kills
    
    // Battle outcome statistics
    public int battlesParticipated = 0;         // Manual tracking - updated after battles
    public int victories = 0;                   // Manual tracking - updated after victories
    public int defeats = 0;                     // Manual tracking - updated after defeats
    
    // Automatic firing state
    public boolean isAutomaticFiring = false;   // Currently in automatic firing mode
    public int burstShotsFired = 0;             // Number of shots fired in current burst
    public long lastAutomaticShot = 0;          // Tick of last automatic shot
    public AimingSpeed savedAimingSpeed = null; // Saved aiming speed for first shot in burst/auto
    
    // Hesitation state
    public boolean isHesitating = false;        // Currently hesitating due to wound
    public long hesitationEndTick = 0;          // When hesitation will end
    public List<ScheduledEvent> pausedEvents = new ArrayList<>(); // Events paused during hesitation
    
    // Bravery check state
    public int braveryCheckFailures = 0;        // Number of active bravery check failures
    public long braveryPenaltyEndTick = 0;      // When bravery penalty will end
    
    // Hesitation tracking for display
    public long totalWoundHesitationTicks = 0;   // Total hesitation time from wounds
    public long totalBraveryHesitationTicks = 0; // Total hesitation time from bravery failures
    
    // Target zone for automatic targeting
    public java.awt.Rectangle targetZone = null; // Target zone rectangle in world coordinates
    
    // Last target direction for weapon visibility when no current target
    public Double lastTargetFacing = null; // Last direction character was aiming (degrees)
    
    // Melee movement state tracking
    public boolean isMovingToMelee = false; // Currently moving to engage target in melee combat
    public Unit meleeTarget = null; // Target unit for melee attack (maintained during movement)
    private long lastMeleeMovementUpdate = 0; // Last tick when melee movement was updated (for throttling)

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
        this.currentMovementType = MovementType.WALK;
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
        this.currentMovementType = MovementType.WALK;
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
        this.currentMovementType = MovementType.WALK;
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
        this.currentMovementType = MovementType.WALK;
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
        this.currentMovementType = MovementType.WALK;
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

    public int getId() {
        return id;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    // Dual weapon system methods
    
    /**
     * Initialize default weapons (ranged weapon from legacy weapon field, unarmed for melee)
     */
    public void initializeDefaultWeapons() {
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
        isMeleeCombatMode = !isMeleeCombatMode;
    }
    
    /**
     * Set combat mode explicitly
     */
    public void setCombatMode(boolean meleeMode) {
        // Cancel any ongoing melee movement when switching modes
        if (isMovingToMelee) {
            isMovingToMelee = false;
            meleeTarget = null;
        }
        isMeleeCombatMode = meleeMode;
    }
    
    /**
     * Check if character is in melee combat mode
     */
    public boolean isMeleeCombatMode() {
        return isMeleeCombatMode;
    }
    
    /**
     * Set the ranged weapon
     */
    public void setRangedWeapon(RangedWeapon rangedWeapon) {
        this.rangedWeapon = rangedWeapon;
        this.weapon = rangedWeapon; // Maintain backward compatibility
    }
    
    /**
     * Set the melee weapon
     */
    public void setMeleeWeapon(MeleeWeapon meleeWeapon) {
        this.meleeWeapon = meleeWeapon;
    }
    
    /**
     * Get the ranged weapon
     */
    public RangedWeapon getRangedWeapon() {
        return rangedWeapon;
    }
    
    /**
     * Get the melee weapon
     */
    public MeleeWeapon getMeleeWeapon() {
        return meleeWeapon;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    // Legacy methods for backwards compatibility with tests
    public String getName() {
        return nickname;
    }

    public void setName(String name) {
        this.nickname = name;
    }
    
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }
    
    public String getDisplayName() {
        return id + ":" + nickname;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public String getThemeId() {
        return themeId;
    }

    public void setThemeId(String themeId) {
        this.themeId = themeId;
    }

    public int getDexterity() {
        return dexterity;
    }

    public void setDexterity(int dexterity) {
        this.dexterity = dexterity;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public double getBaseMovementSpeed() {
        return baseMovementSpeed;
    }

    public void setBaseMovementSpeed(double baseMovementSpeed) {
        this.baseMovementSpeed = baseMovementSpeed;
    }

    public MovementType getCurrentMovementType() {
        return currentMovementType;
    }

    public void setCurrentMovementType(MovementType movementType) {
        this.currentMovementType = movementType;
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
                System.out.println(">>> " + getDisplayName() + " cannot increase movement speed to " + newType.getDisplayName() + " due to leg wounds (max: " + maxAllowed.getDisplayName() + ")");
            }
        }
    }
    
    public void decreaseMovementType() {
        if (!isIncapacitated()) {
            this.currentMovementType = currentMovementType.decrease();
        }
    }

    public AimingSpeed getCurrentAimingSpeed() {
        return currentAimingSpeed;
    }

    public void setCurrentAimingSpeed(AimingSpeed aimingSpeed) {
        this.currentAimingSpeed = aimingSpeed;
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
        // Must have weapon skill level 1+ for pistol or rifle weapons
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
            default:
                return false;
        }
        
        // Check if character has the required skill level (1+)
        return getSkillLevel(skillName) >= 1;
    }

    public PositionState getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(PositionState position) {
        this.currentPosition = position;
    }
    
    public void increasePosition() {
        if (!isIncapacitated()) {
            // Characters with both legs wounded cannot stand up from prone
            if (currentPosition == PositionState.PRONE && hasBothLegsWounded()) {
                System.out.println(">>> " + getDisplayName() + " cannot stand up due to wounds to both legs");
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

    public int getCoolness() {
        return coolness;
    }

    public void setCoolness(int coolness) {
        this.coolness = coolness;
    }
    
    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }
    
    public int getReflexes() {
        return reflexes;
    }

    public void setReflexes(int reflexes) {
        this.reflexes = reflexes;
    }
    
    public Handedness getHandedness() {
        return handedness;
    }

    public void setHandedness(Handedness handedness) {
        this.handedness = handedness;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
    }

    public WeaponState getCurrentWeaponState() {
        return currentWeaponState;
    }

    public void setCurrentWeaponState(WeaponState state) {
        this.currentWeaponState = state;
    }
    
    public List<Skill> getSkills() {
        return skills;
    }
    
    public void setSkills(List<Skill> skills) {
        this.skills = skills != null ? skills : new ArrayList<>();
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
    
    public List<Wound> getWounds() {
        return wounds;
    }
    
    public void setWounds(List<Wound> wounds) {
        this.wounds = wounds != null ? wounds : new ArrayList<>();
    }
    
    public void addWound(Wound wound, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        wounds.add(wound);
        woundsReceived++;
        
        // Apply damage to current health
        currentHealth -= wound.getDamage();
        if (currentHealth < 0) {
            currentHealth = 0; // Don't allow negative health
        }
        
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
        if (currentHealth < 0) {
            currentHealth = 0; // Don't allow negative health
        }
        
        // Enforce movement restrictions immediately after adding wound
        enforceMovementRestrictions();
        
        // Note: Hesitation will not be triggered without event queue context
    }
    
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
    
    public void startAttackSequence(Unit shooter, Unit target, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        if (weapon == null || currentWeaponState == null) return;
        
        // If targeting a different unit, cancel all pending attacks and reset
        if (currentTarget != null && currentTarget != target) {
            // Clear all pending events for this character
            gameCallbacks.removeAllEventsForOwner(ownerId);
            currentWeaponState = weapon.getStateByName("ready");
            System.out.println(getDisplayName() + " cancels attack on " + currentTarget.character.getDisplayName() + " and retargets " + target.character.getDisplayName() + " at tick " + currentTick);
        } else if ("aiming".equals(currentWeaponState.getState()) && currentTarget != target) {
            currentWeaponState = weapon.getStateByName("ready");
            System.out.println(getDisplayName() + " weapon state: ready (target changed) at tick " + currentTick);
        } else if (currentTarget == target && isAttacking) {
            // Already attacking the same target, don't start duplicate attack
            System.out.println(getDisplayName() + " is already attacking " + target.character.getDisplayName() + " - ignoring duplicate attack command");
            return;
        }
        
        currentTarget = target;
        isAttacking = true;
        
        // Make unit face the target and save the direction for later use
        shooter.setTargetFacing(target.x, target.y);
        
        // Calculate and save the target facing direction for weapon visibility
        double dx = target.x - shooter.x;
        double dy = target.y - shooter.y;
        double angleRadians = Math.atan2(dx, -dy);
        double angleDegrees = Math.toDegrees(angleRadians);
        if (angleDegrees < 0) angleDegrees += 360;
        lastTargetFacing = angleDegrees;
        scheduleAttackFromCurrentState(shooter, target, currentTick, eventQueue, ownerId, gameCallbacks);
    }
    
    public void startReadyWeaponSequence(Unit unit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        if (weapon == null || currentWeaponState == null) return;
        
        scheduleReadyFromCurrentState(unit, currentTick, eventQueue, ownerId);
    }
    
    /**
     * Start melee attack sequence from current weapon state
     */
    public void startMeleeAttackSequence(Unit attacker, Unit target, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        if (meleeWeapon == null) return;
        
        // Check if target is within melee range using edge-to-edge calculation
        if (!isInMeleeRange(attacker, target, meleeWeapon)) {
            // Target is out of range - move towards target
            System.out.println(getDisplayName() + " moving towards " + target.character.getDisplayName() + " for melee attack");
            attacker.setTarget(target.x, target.y);
            
            // Schedule a follow-up check to attempt attack once in range
            scheduleRangeCheckForMeleeAttack(attacker, target, currentTick + 10, eventQueue, ownerId, gameCallbacks);
            return;
        }
        
        // Calculate facing direction to target
        double dx = target.x - attacker.x;
        double dy = target.y - attacker.y;
        double angleRadians = Math.atan2(dx, -dy);
        double angleDegrees = Math.toDegrees(angleRadians);
        if (angleDegrees < 0) angleDegrees += 360;
        lastTargetFacing = angleDegrees;
        
        // Schedule melee attack from current state
        scheduleMeleeAttackFromCurrentState(attacker, target, currentTick, eventQueue, ownerId, gameCallbacks);
    }
    
    private void scheduleAttackFromCurrentState(Unit shooter, Unit target, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        if (weapon == null || currentWeaponState == null) return;
        
        String currentState = currentWeaponState.getState();
        long totalTimeToFire = calculateTimeToFire();
        
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
                System.out.println(getDisplayName() + " uses very careful aiming, adding " + String.format("%.1f", additionalTime / 60.0) + " seconds extra aiming time");
            }
            
            // Log aiming speed usage for burst/auto modes
            if (isAutomaticFiring && burstShotsFired > 1) {
                System.out.println(getDisplayName() + " uses " + aimingSpeedToUse.getDisplayName() + " aiming for burst/auto shot " + burstShotsFired);
            }
            
            scheduleFiring(shooter, target, currentTick + adjustedAimingTime, eventQueue, ownerId, gameCallbacks);
        }
    }
    
    /**
     * Schedule melee attack from current weapon state
     */
    private void scheduleMeleeAttackFromCurrentState(Unit attacker, Unit target, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        if (meleeWeapon == null) return;
        
        // Get active weapon for state management (use melee weapon's states)
        Weapon activeWeapon = getActiveWeapon();
        WeaponState currentState = currentWeaponState;
        
        if (currentState == null) {
            // Initialize to weapon's initial state if no current state
            currentState = activeWeapon.getInitialState();
            currentWeaponState = currentState;
        }
        
        String stateName = currentState.getState();
        
        // Handle melee weapon state transitions
        if ("sheathed".equals(stateName)) {
            scheduleMeleeStateTransition("unsheathing", currentTick, currentState.ticks, attacker, target, eventQueue, ownerId, gameCallbacks);
        } else if ("unsheathing".equals(stateName)) {
            scheduleMeleeStateTransition("melee_ready", currentTick, currentState.ticks, attacker, target, eventQueue, ownerId, gameCallbacks);
        } else if ("melee_ready".equals(stateName)) {
            // Ready to attack - schedule the melee attack
            long attackTime = Math.round(meleeWeapon.getAttackSpeed() * calculateAttackSpeedMultiplier());
            scheduleMeleeAttack(attacker, target, currentTick + attackTime, eventQueue, ownerId, gameCallbacks);
        } else if ("switching_to_melee".equals(stateName)) {
            scheduleMeleeStateTransition("melee_ready", currentTick, currentState.ticks, attacker, target, eventQueue, ownerId, gameCallbacks);
        } else {
            // For any other state, transition to switching_to_melee first
            scheduleMeleeStateTransition("switching_to_melee", currentTick, 30, attacker, target, eventQueue, ownerId, gameCallbacks);
        }
    }
    
    private void scheduleStateTransition(String newStateName, long currentTick, long transitionTickLength, Unit shooter, Unit target, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Apply speed multiplier only to weapon preparation states
        if (isWeaponPreparationState(newStateName)) {
            double speedMultiplier = calculateWeaponReadySpeedMultiplier();
            transitionTickLength = Math.round(transitionTickLength * speedMultiplier);
        }
        
        long transitionTick = currentTick + transitionTickLength;
        eventQueue.add(new ScheduledEvent(transitionTick, () -> {
            currentWeaponState = weapon.getStateByName(newStateName);
            System.out.println(getDisplayName() + " weapon state: " + newStateName + " at tick " + transitionTick);
            scheduleAttackFromCurrentState(shooter, target, transitionTick, eventQueue, ownerId, gameCallbacks);
        }, ownerId));
    }
    
    private void scheduleFiring(Unit shooter, Unit target, long fireTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        eventQueue.add(new ScheduledEvent(fireTick, () -> {
            currentWeaponState = weapon.getStateByName("firing");
            System.out.println(getDisplayName() + " weapon state: firing at tick " + fireTick);
            
            if (weapon instanceof RangedWeapon && ((RangedWeapon)weapon).getAmmunition() <= 0) {
                System.out.println("*** " + getDisplayName() + " tries to fire " + weapon.name + " but it's out of ammunition!");
            } else if (weapon instanceof RangedWeapon) {
                ((RangedWeapon)weapon).setAmmunition(((RangedWeapon)weapon).getAmmunition() - 1);
                System.out.println("*** " + getDisplayName() + " fires a " + weapon.getProjectileName() + " from " + weapon.name + " (ammo remaining: " + ((RangedWeapon)weapon).getAmmunition() + ")");
                
                gameCallbacks.playWeaponSound(weapon);
                gameCallbacks.applyFiringHighlight(shooter, fireTick);
                gameCallbacks.addMuzzleFlash(shooter, fireTick);
                
                double dx = target.x - shooter.x;
                double dy = target.y - shooter.y;
                double distancePixels = Math.hypot(dx, dy);
                double distanceFeet = distancePixels / 7.0; // pixelsToFeet conversion
                System.out.println("*** " + getDisplayName() + " shoots a " + weapon.getProjectileName() + " at " + target.character.getDisplayName() + " at distance " + String.format("%.2f", distanceFeet) + " feet using " + weapon.name + " at tick " + fireTick);
                
                gameCallbacks.scheduleProjectileImpact(shooter, target, weapon, fireTick, distanceFeet);
                
                // Handle burst firing - schedule additional shots immediately after first shot
                if (weapon instanceof RangedWeapon && ((RangedWeapon)weapon).getCurrentFiringMode() == FiringMode.BURST && !isAutomaticFiring) {
                    isAutomaticFiring = true;
                    burstShotsFired = 1; // First shot just fired
                    lastAutomaticShot = fireTick;
                    System.out.println(getDisplayName() + " starts burst firing (" + burstShotsFired + "/" + ((RangedWeapon)weapon).getBurstSize() + ")");
                    
                    // Schedule remaining shots in the burst
                    for (int shot = 2; shot <= ((RangedWeapon)weapon).getBurstSize(); shot++) {
                        long nextShotTick = fireTick + (((RangedWeapon)weapon).getCyclicRate() * (shot - 1));
                        final int shotNumber = shot;
                        eventQueue.add(new ScheduledEvent(nextShotTick, () -> {
                            if (currentTarget != null && !currentTarget.character.isIncapacitated() && !this.isIncapacitated() && weapon instanceof RangedWeapon && ((RangedWeapon)weapon).getAmmunition() > 0) {
                                ((RangedWeapon)weapon).setAmmunition(((RangedWeapon)weapon).getAmmunition() - 1);
                                burstShotsFired++;
                                System.out.println(getDisplayName() + " burst fires shot " + burstShotsFired + "/" + ((RangedWeapon)weapon).getBurstSize() + " (9mm round, ammo remaining: " + ((RangedWeapon)weapon).getAmmunition() + ")");
                                
                                gameCallbacks.playWeaponSound(weapon);
                                gameCallbacks.applyFiringHighlight(shooter, nextShotTick);
                                gameCallbacks.addMuzzleFlash(shooter, nextShotTick);
                                
                                double dx2 = currentTarget.x - shooter.x;
                                double dy2 = currentTarget.y - shooter.y;
                                double distancePixels2 = Math.hypot(dx2, dy2);
                                double distanceFeet2 = distancePixels2 / 7.0;
                                System.out.println("*** " + getDisplayName() + " shoots a " + weapon.getProjectileName() + " at " + currentTarget.character.getDisplayName() + " at distance " + String.format("%.2f", distanceFeet2) + " feet using " + weapon.name + " at tick " + nextShotTick);
                                
                                gameCallbacks.scheduleProjectileImpact(shooter, currentTarget, weapon, nextShotTick, distanceFeet2);
                                
                                // Reset burst state after final shot
                                if (burstShotsFired >= ((RangedWeapon)weapon).getBurstSize()) {
                                    isAutomaticFiring = false;
                                    burstShotsFired = 0;
                                    savedAimingSpeed = null;
                                    System.out.println(getDisplayName() + " completes burst firing");
                                }
                            } else {
                                // Burst interrupted
                                isAutomaticFiring = false;
                                burstShotsFired = 0;
                                savedAimingSpeed = null;
                                System.out.println(getDisplayName() + " burst firing interrupted (target lost or no ammo)");
                            }
                        }, ownerId));
                    }
                }
            }
            
            WeaponState firingState = weapon.getStateByName("firing");
            eventQueue.add(new ScheduledEvent(fireTick + firingState.ticks, () -> {
                currentWeaponState = weapon.getStateByName("recovering");
                System.out.println(getDisplayName() + " weapon state: recovering at tick " + (fireTick + firingState.ticks));
                
                WeaponState recoveringState = weapon.getStateByName("recovering");
                eventQueue.add(new ScheduledEvent(fireTick + firingState.ticks + recoveringState.ticks, () -> {
                    if (weapon instanceof RangedWeapon && ((RangedWeapon)weapon).getAmmunition() <= 0 && canReload()) {
                        System.out.println(getDisplayName() + " is out of ammunition, starting automatic reload");
                        isAttacking = false; // Clear attacking flag during reload
                        startReloadSequence(shooter, fireTick + firingState.ticks + recoveringState.ticks, eventQueue, ownerId, gameCallbacks);
                    } else {
                        currentWeaponState = weapon.getStateByName("aiming");
                        System.out.println(getDisplayName() + " weapon state: aiming at tick " + (fireTick + firingState.ticks + recoveringState.ticks));
                        isAttacking = false; // Attack sequence complete
                        // Check for persistent attack
                        checkContinuousAttack(shooter, fireTick + firingState.ticks + recoveringState.ticks, eventQueue, ownerId, gameCallbacks);
                    }
                }, ownerId));
            }, ownerId));
            
        }, ownerId));
    }
    
    private long calculateTimeToFire() {
        String currentState = currentWeaponState.getState();
        long timeToFire = 0;
        
        WeaponState state = currentWeaponState;
        while (state != null && !"aiming".equals(state.getState())) {
            timeToFire += state.ticks;
            state = weapon.getStateByName(state.getAction());
        }
        
        return timeToFire;
    }
    
    
    private void scheduleReadyFromCurrentState(Unit unit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        if (weapon == null || currentWeaponState == null) return;
        
        String currentState = currentWeaponState.getState();
        
        if ("ready".equals(currentState)) {
            System.out.println(getDisplayName() + " weapon is already ready");
            return;
        }
        
        if ("holstered".equals(currentState)) {
            scheduleReadyStateTransition("drawing", currentTick, currentWeaponState.ticks, unit, eventQueue, ownerId);
        } else if ("drawing".equals(currentState)) {
            scheduleReadyStateTransition("ready", currentTick, currentWeaponState.ticks, unit, eventQueue, ownerId);
        } else if ("slung".equals(currentState)) {
            scheduleReadyStateTransition("unsling", currentTick, currentWeaponState.ticks, unit, eventQueue, ownerId);
        } else if ("unsling".equals(currentState)) {
            scheduleReadyStateTransition("ready", currentTick, currentWeaponState.ticks, unit, eventQueue, ownerId);
        } else if ("sheathed".equals(currentState)) {
            scheduleReadyStateTransition("unsheathing", currentTick, currentWeaponState.ticks, unit, eventQueue, ownerId);
        } else if ("unsheathing".equals(currentState)) {
            scheduleReadyStateTransition("ready", currentTick, currentWeaponState.ticks, unit, eventQueue, ownerId);
        } else if ("aiming".equals(currentState) || "firing".equals(currentState) || "recovering".equals(currentState)) {
            WeaponState readyState = weapon.getStateByName("ready");
            eventQueue.add(new ScheduledEvent(currentTick + currentWeaponState.ticks, () -> {
                currentWeaponState = readyState;
                System.out.println(getDisplayName() + " weapon state: ready at tick " + (currentTick + currentWeaponState.ticks));
            }, ownerId));
        }
    }
    
    private double calculateWeaponReadySpeedMultiplier() {
        int reflexesModifier = statToModifier(this.reflexes);
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
    
    private static int statToModifier(int stat) {
        // Clamp stat to valid range
        stat = Math.max(1, Math.min(100, stat));
        
        // Use a lookup table for perfect control over the distribution
        int[] modifiers = new int[101]; // index 0 unused, 1-100 are valid stats
        
        // Define the negative half (1-50), then mirror for positive half (51-100)
        modifiers[1] = -20;   modifiers[2] = -19;   modifiers[3] = -18;   modifiers[4] = -17;   modifiers[5] = -16;   modifiers[6] = -15;
        modifiers[7] = -14;   modifiers[8] = -14;   modifiers[9] = -13;   modifiers[10] = -13;  modifiers[11] = -12;  modifiers[12] = -12;
        modifiers[13] = -11;  modifiers[14] = -11;  modifiers[15] = -10;  modifiers[16] = -10;  modifiers[17] = -9;   modifiers[18] = -9;
        modifiers[19] = -8;   modifiers[20] = -8;   modifiers[21] = -7;   modifiers[22] = -7;   modifiers[23] = -6;   modifiers[24] = -6;
        modifiers[25] = -5;   modifiers[26] = -5;   modifiers[27] = -5;   modifiers[28] = -4;   modifiers[29] = -4;   modifiers[30] = -4;
        modifiers[31] = -3;   modifiers[32] = -3;   modifiers[33] = -3;   modifiers[34] = -3;   modifiers[35] = -2;   modifiers[36] = -2;
        modifiers[37] = -2;   modifiers[38] = -2;   modifiers[39] = -2;   modifiers[40] = -1;   modifiers[41] = -1;   modifiers[42] = -1;
        modifiers[43] = -1;   modifiers[44] = -1;   modifiers[45] = -1;   modifiers[46] = 0;    modifiers[47] = 0;    modifiers[48] = 0;
        modifiers[49] = 0;    modifiers[50] = 0;    modifiers[51] = 0;
        
        // Mirror for the positive half (perfect symmetry)
        for (int i = 1; i <= 49; i++) {
            modifiers[51 + i] = -modifiers[50 - i];
        }
        
        return modifiers[stat];
    }
    
    private void scheduleReadyStateTransition(String newStateName, long currentTick, long transitionTickLength, Unit unit, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        // Apply speed multiplier only to weapon preparation states
        if (isWeaponPreparationState(newStateName)) {
            double speedMultiplier = calculateWeaponReadySpeedMultiplier();
            transitionTickLength = Math.round(transitionTickLength * speedMultiplier);
        }
        
        long transitionTick = currentTick + transitionTickLength;
        eventQueue.add(new ScheduledEvent(transitionTick, () -> {
            currentWeaponState = weapon.getStateByName(newStateName);
            System.out.println(getDisplayName() + " weapon state: " + newStateName + " at tick " + transitionTick);
            scheduleReadyFromCurrentState(unit, transitionTick, eventQueue, ownerId);
        }, ownerId));
    }
    
    private boolean isWeaponPreparationState(String stateName) {
        return "drawing".equals(stateName) || "unsheathing".equals(stateName) || "unsling".equals(stateName) || "ready".equals(stateName);
    }
    
    private AimingSpeed determineAimingSpeedForShot() {
        // If no weapon or firing mode, use current aiming speed
        if (weapon == null || !(weapon instanceof RangedWeapon) || ((RangedWeapon)weapon).getCurrentFiringMode() == null) {
            return currentAimingSpeed;
        }
        
        switch (((RangedWeapon)weapon).getCurrentFiringMode()) {
            case SINGLE_SHOT:
                // Single shot always uses current aiming speed
                return currentAimingSpeed;
                
            case BURST:
                if (!isAutomaticFiring || burstShotsFired <= 1) {
                    // First shot of burst uses current aiming speed
                    savedAimingSpeed = currentAimingSpeed;
                    return currentAimingSpeed;
                } else {
                    // Subsequent shots use quick aiming
                    return AimingSpeed.QUICK;
                }
                
            case FULL_AUTO:
                if (!isAutomaticFiring || burstShotsFired <= 1) {
                    // First shot of auto uses current aiming speed
                    savedAimingSpeed = currentAimingSpeed;
                    return currentAimingSpeed;
                } else {
                    // Subsequent shots use quick aiming
                    return AimingSpeed.QUICK;
                }
                
            default:
                return currentAimingSpeed;
        }
    }
    
    public double getWeaponReadySpeedMultiplier() {
        return calculateWeaponReadySpeedMultiplier();
    }
    
    public double getAimingSpeedMultiplier() {
        return calculateAimingSpeedMultiplier();
    }
    
    /**
     * Schedule melee state transition
     */
    private void scheduleMeleeStateTransition(String newStateName, long currentTick, long transitionTickLength, Unit attacker, Unit target, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Apply speed multiplier to weapon preparation states
        if (isWeaponPreparationState(newStateName)) {
            double speedMultiplier = calculateWeaponReadySpeedMultiplier();
            transitionTickLength = Math.round(transitionTickLength * speedMultiplier);
        }
        
        WeaponState newState = getActiveWeapon().getStateByName(newStateName);
        if (newState != null) {
            // Create final copies for lambda
            final String finalStateName = newStateName;
            final long finalTick = currentTick + transitionTickLength;
            
            eventQueue.add(new ScheduledEvent(finalTick, () -> {
                currentWeaponState = newState;
                System.out.println(getDisplayName() + " melee weapon state: " + finalStateName + " at tick " + finalTick);
                
                // Continue the attack sequence
                scheduleMeleeAttackFromCurrentState(attacker, target, finalTick, eventQueue, ownerId, gameCallbacks);
            }, ownerId));
        }
    }
    
    /**
     * Schedule range check for melee attack - continues tracking target until in range
     */
    private void scheduleRangeCheckForMeleeAttack(Unit attacker, Unit target, long checkTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        eventQueue.add(new ScheduledEvent(checkTick, () -> {
            // Update movement target to track target's current position
            attacker.setTarget(target.x, target.y);
            
            // Check if now in range
            if (isInMeleeRange(attacker, target, meleeWeapon)) {
                // Now in range - proceed with attack
                System.out.println(getDisplayName() + " is now in range, proceeding with melee attack");
                
                // Calculate facing direction to target
                double dx = target.x - attacker.x;
                double dy = target.y - attacker.y;
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
    private void scheduleMeleeAttack(Unit attacker, Unit target, long attackTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        eventQueue.add(new ScheduledEvent(attackTick, () -> {
            // Update weapon state to attacking
            WeaponState attackingState = getActiveWeapon().getStateByName("melee_attacking");
            if (attackingState != null) {
                currentWeaponState = attackingState;
            }
            
            System.out.println(getDisplayName() + " executes melee attack with " + meleeWeapon.getName() + " at tick " + attackTick);
            
            // Schedule immediate impact (no travel time for melee)
            gameCallbacks.scheduleMeleeImpact(attacker, target, meleeWeapon, attackTick);
            
            // Schedule recovery back to ready state
            long recoveryTime = Math.round(meleeWeapon.getAttackCooldown() * calculateAttackSpeedMultiplier());
            WeaponState readyState = getActiveWeapon().getStateByName("melee_ready");
            if (readyState != null) {
                eventQueue.add(new ScheduledEvent(attackTick + recoveryTime, () -> {
                    currentWeaponState = readyState;
                    System.out.println(getDisplayName() + " melee weapon state: melee_ready at tick " + (attackTick + recoveryTime));
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
    
    public void startReloadSequence(Unit unit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        if (!canReload()) return;
        
        System.out.println(getDisplayName() + " starts reloading " + weapon.getName());
        
        currentWeaponState = weapon.getStateByName("reloading");
        
        long reloadTicks = calculateReloadSpeed();
        long reloadCompleteTick = currentTick + reloadTicks;
        
        eventQueue.add(new ScheduledEvent(reloadCompleteTick, () -> {
            performReload();
            System.out.println(getDisplayName() + " loads one round into " + weapon.getName() + 
                             " (" + ((RangedWeapon)weapon).getAmmunition() + "/" + ((RangedWeapon)weapon).getMaxAmmunition() + ") at tick " + reloadCompleteTick);
            
            // Continue reloading if needed for single-round weapons
            if (weapon instanceof RangedWeapon && ((RangedWeapon)weapon).getReloadType() == ReloadType.SINGLE_ROUND && ((RangedWeapon)weapon).getAmmunition() < ((RangedWeapon)weapon).getMaxAmmunition()) {
                continueReloading(unit, reloadCompleteTick, eventQueue, ownerId, gameCallbacks);
            } else {
                currentWeaponState = weapon.getStateByName("ready");
                System.out.println(getDisplayName() + " finished reloading " + weapon.getName() + 
                                 " (" + ((RangedWeapon)weapon).getAmmunition() + "/" + ((RangedWeapon)weapon).getMaxAmmunition() + ") at tick " + reloadCompleteTick);
                // Check for persistent attack after reload
                checkContinuousAttack(unit, reloadCompleteTick, eventQueue, ownerId, gameCallbacks);
            }
        }, ownerId));
    }
    
    private void continueReloading(Unit unit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        if (weapon == null || !(weapon instanceof RangedWeapon) || ((RangedWeapon)weapon).getAmmunition() >= ((RangedWeapon)weapon).getMaxAmmunition()) {
            currentWeaponState = weapon.getStateByName("ready");
            return;
        }
        
        long reloadTicks = calculateReloadSpeed();
        long reloadCompleteTick = currentTick + reloadTicks;
        
        eventQueue.add(new ScheduledEvent(reloadCompleteTick, () -> {
            performReload();
            System.out.println(getDisplayName() + " loads one round into " + weapon.getName() + 
                             " (" + ((RangedWeapon)weapon).getAmmunition() + "/" + ((RangedWeapon)weapon).getMaxAmmunition() + ") at tick " + reloadCompleteTick);
            
            // Continue reloading if still not full
            if (weapon instanceof RangedWeapon && ((RangedWeapon)weapon).getAmmunition() < ((RangedWeapon)weapon).getMaxAmmunition()) {
                continueReloading(unit, reloadCompleteTick, eventQueue, ownerId, gameCallbacks);
            } else {
                currentWeaponState = weapon.getStateByName("ready");
                System.out.println(getDisplayName() + " finished reloading " + weapon.getName() + 
                                 " (" + ((RangedWeapon)weapon).getAmmunition() + "/" + ((RangedWeapon)weapon).getMaxAmmunition() + ") at tick " + reloadCompleteTick);
                // Check for persistent attack after reload
                checkContinuousAttack(unit, reloadCompleteTick, eventQueue, ownerId, gameCallbacks);
            }
        }, ownerId));
    }
    
    private long calculateReloadSpeed() {
        int reflexesModifier = statToModifier(this.reflexes);
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
    
    public boolean isPersistentAttack() {
        return persistentAttack;
    }
    
    public void setPersistentAttack(boolean persistentAttack) {
        this.persistentAttack = persistentAttack;
    }
    
    public int getFaction() {
        return faction;
    }
    
    public void setFaction(int faction) {
        this.faction = faction;
    }
    
    public boolean isHostileTo(Character other) {
        return this.faction != other.faction;
    }
    
    private Unit findNearestHostileTarget(Unit selfUnit, GameCallbacks gameCallbacks) {
        List<Unit> allUnits = gameCallbacks.getUnits();
        Unit nearestTarget = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (Unit unit : allUnits) {
            // Skip self
            if (unit == selfUnit) continue;
            
            // Skip if not hostile (same faction)
            if (!this.isHostileTo(unit.character)) continue;
            
            // Skip if incapacitated
            if (unit.character.isIncapacitated()) continue;
            
            // Calculate distance
            double dx = unit.x - selfUnit.x;
            double dy = unit.y - selfUnit.y;
            double distance = Math.hypot(dx, dy);
            
            // Check weapon range limitations
            if (weapon != null && distance / 7.0 > ((RangedWeapon)weapon).getMaximumRange()) {
                continue; // Skip targets beyond weapon range
            }
            
            // Check if this is the nearest target
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestTarget = unit;
            }
        }
        
        return nearestTarget;
    }
    
    private Unit findNearestHostileTargetWithZonePriority(Unit selfUnit, GameCallbacks gameCallbacks) {
        List<Unit> allUnits = gameCallbacks.getUnits();
        debugPrint("[AUTO-TARGET-DEBUG] " + getDisplayName() + " searching " + allUnits.size() + " units for targets...");
        Unit nearestZoneTarget = null;
        Unit nearestGlobalTarget = null;
        double nearestZoneDistance = Double.MAX_VALUE;
        double nearestGlobalDistance = Double.MAX_VALUE;
        int hostilesFound = 0;
        
        for (Unit unit : allUnits) {
            // Skip self
            if (unit == selfUnit) continue;
            
            // Skip if not hostile (same faction)
            if (!this.isHostileTo(unit.character)) {
                debugPrint("[AUTO-TARGET-DEBUG]   " + unit.character.getDisplayName() + " - not hostile (faction " + unit.character.faction + " vs " + this.faction + ")");
                continue;
            }
            
            hostilesFound++;
            
            // Skip if incapacitated
            if (unit.character.isIncapacitated()) continue;
            
            // Calculate distance
            double dx = unit.x - selfUnit.x;
            double dy = unit.y - selfUnit.y;
            double distance = Math.hypot(dx, dy);
            
            // Check weapon range limitations
            if (weapon != null && distance / 7.0 > ((RangedWeapon)weapon).getMaximumRange()) {
                continue; // Skip targets beyond weapon range
            }
            
            // Check if target is within target zone (if zone exists)
            boolean inTargetZone = false;
            if (targetZone != null) {
                inTargetZone = targetZone.contains((int)unit.x, (int)unit.y);
            }
            
            if (inTargetZone) {
                // Target is in zone - prioritize zone targets
                if (distance < nearestZoneDistance) {
                    nearestZoneDistance = distance;
                    nearestZoneTarget = unit;
                }
            } else {
                // Target is not in zone - track as global fallback
                if (distance < nearestGlobalDistance) {
                    nearestGlobalDistance = distance;
                    nearestGlobalTarget = unit;
                }
            }
        }
        
        // Return zone target if available, otherwise global target
        Unit result = nearestZoneTarget != null ? nearestZoneTarget : nearestGlobalTarget;
        debugPrint("[AUTO-TARGET-DEBUG] " + getDisplayName() + " found " + hostilesFound + " hostile units, selected: " + 
                             (result != null ? result.character.getDisplayName() : "none"));
        return result;
    }
    
    private void performAutomaticTargetChange(Unit shooter, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Only proceed if still in persistent attack mode and not incapacitated
        if (!persistentAttack || this.isIncapacitated() || weapon == null) {
            System.out.println(getDisplayName() + " automatic retargeting cancelled - conditions no longer met");
            persistentAttack = false;
            currentTarget = null;
            isAttacking = false;
            
            // Preserve the current facing direction so weapon continues to aim at last target location
            if (lastTargetFacing != null && shooter != null) {
                shooter.targetFacing = lastTargetFacing;
            }
            return;
        }
        
        // Find new target with target zone priority
        Unit newTarget = findNearestHostileTargetWithZonePriority(shooter, gameCallbacks);
        
        if (newTarget != null) {
            // New target found - start attacking
            currentTarget = newTarget;
            
            // Calculate distance for logging
            double dx = newTarget.x - shooter.x;
            double dy = newTarget.y - shooter.y;
            double distanceFeet = Math.hypot(dx, dy) / 7.0;
            
            String zoneStatus = (targetZone != null && targetZone.contains((int)newTarget.x, (int)newTarget.y)) ? " (in target zone)" : "";
            System.out.println("[AUTO-RETARGET] " + getDisplayName() + " acquired new target " + 
                             newTarget.character.getDisplayName() + " at distance " + 
                             String.format("%.1f", distanceFeet) + " feet" + zoneStatus);
            
            // Start attack sequence from current state
            startAttackSequence(shooter, newTarget, currentTick, eventQueue, ownerId, gameCallbacks);
        } else {
            // No valid targets found - disable persistent attack but preserve weapon aiming direction
            persistentAttack = false;
            currentTarget = null;
            isAttacking = false;
            
            // Preserve the current facing direction so weapon continues to aim at last target location
            if (lastTargetFacing != null) {
                shooter.targetFacing = lastTargetFacing;
            }
            
            System.out.println("[AUTO-RETARGET] " + getDisplayName() + " found no valid targets within range, disabling automatic targeting but maintaining weapon direction");
        }
    }
    
    public void updateAutomaticTargeting(Unit selfUnit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, GameCallbacks gameCallbacks) {
        // Only execute if automatic targeting is enabled
        if (!usesAutomaticTargeting) return;
        
        // Skip if character is incapacitated
        if (this.isIncapacitated()) {
            debugPrint("[AUTO-TARGET-DEBUG] " + getDisplayName() + " skipped: incapacitated");
            return;
        }
        
        // Skip if character has no weapon
        if (weapon == null) {
            debugPrint("[AUTO-TARGET-DEBUG] " + getDisplayName() + " skipped: no weapon");
            return;
        }
        
        // Skip if character is already attacking (let existing attack complete)
        if (isAttacking) {
            debugPrint("[AUTO-TARGET-DEBUG] " + getDisplayName() + " skipped: already attacking (weapon state: " + 
                                 (currentWeaponState != null ? currentWeaponState.getState() : "null") + ")");
            return;
        }
        
        debugPrint("[AUTO-TARGET-DEBUG] " + getDisplayName() + " executing automatic targeting (current target: " + 
                             (currentTarget != null ? currentTarget.character.getDisplayName() : "none") + ")");
        
        // Check if current target is still valid
        boolean currentTargetValid = currentTarget != null 
            && !currentTarget.character.isIncapacitated() 
            && this.isHostileTo(currentTarget.character);
        
        if (!currentTargetValid) {
            debugPrint("[AUTO-TARGET-DEBUG] " + getDisplayName() + " current target invalid, searching for new target...");
            // Find a new target with target zone priority
            Unit newTarget = findNearestHostileTargetWithZonePriority(selfUnit, gameCallbacks);
            
            if (newTarget != null) {
                // Target found - start attacking
                currentTarget = newTarget;
                persistentAttack = true;
                
                // Calculate distance for logging
                double dx = newTarget.x - selfUnit.x;
                double dy = newTarget.y - selfUnit.y;
                double distanceFeet = Math.hypot(dx, dy) / 7.0; // Convert pixels to feet
                
                String zoneStatus = (targetZone != null && targetZone.contains((int)newTarget.x, (int)newTarget.y)) ? " (in target zone)" : "";
                System.out.println("[AUTO-TARGET] " + getDisplayName() + " acquired target " + 
                                 newTarget.character.getDisplayName() + " at distance " + 
                                 String.format("%.1f", distanceFeet) + " feet" + zoneStatus);
                
                // Start attack sequence
                startAttackSequence(selfUnit, newTarget, currentTick, eventQueue, selfUnit.getId(), gameCallbacks);
            } else {
                // No targets found - disable persistent attack but maintain weapon direction
                if (persistentAttack) {
                    persistentAttack = false;
                    currentTarget = null;
                    
                    // Preserve the current facing direction so weapon continues to aim at last target location
                    if (lastTargetFacing != null && selfUnit != null) {
                        selfUnit.targetFacing = lastTargetFacing;
                    }
                    
                    System.out.println("[AUTO-TARGET] " + getDisplayName() + " found no valid targets within range, disabling automatic targeting but maintaining weapon direction");
                }
            }
        }
    }
    
    /**
     * Update melee movement progress and trigger attack when target is reached
     */
    public void updateMeleeMovement(Unit selfUnit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, GameCallbacks gameCallbacks) {
        // Only execute if currently moving to melee target
        if (!isMovingToMelee || meleeTarget == null) return;
        
        // Throttle updates to every 10 ticks (6 times per second) for performance
        if (currentTick - lastMeleeMovementUpdate < 10) return;
        lastMeleeMovementUpdate = currentTick;
        
        // Check if target is still valid
        if (meleeTarget.character.isIncapacitated()) {
            debugPrint("[MELEE-MOVEMENT] " + getDisplayName() + " target " + meleeTarget.character.getDisplayName() + " incapacitated during approach - cancelling movement");
            cancelMeleeMovement();
            return;
        }
        
        MeleeWeapon meleeWeapon = this.meleeWeapon;
        if (meleeWeapon == null) {
            debugPrint("[MELEE-MOVEMENT] " + getDisplayName() + " lost melee weapon during movement - cancelling");
            cancelMeleeMovement();
            return;
        }
        
        // Check current distance to target
        double currentDistance = Math.hypot(meleeTarget.x - selfUnit.x, meleeTarget.y - selfUnit.y);
        double distanceFeet = currentDistance / 7.0;
        double weaponReach = meleeWeapon.getTotalReach();
        
        // If we're already in range, start attack immediately
        if (distanceFeet <= weaponReach) {
            debugPrint("[MELEE-MOVEMENT] " + getDisplayName() + " reached melee range of " + meleeTarget.character.getDisplayName() + " (" + String.format("%.2f", distanceFeet) + " feet)");
            Unit targetUnit = meleeTarget; // Save reference before clearing state
            cancelMeleeMovement();
            
            // Start the actual melee attack sequence
            startMeleeAttackSequence(selfUnit, targetUnit, currentTick, eventQueue, selfUnit.getId(), gameCallbacks);
            return;
        }
        
        // Check if we're still moving (hasTarget indicates movement in progress)
        if (selfUnit.hasTarget) {
            // Still moving - check if target has moved significantly and update path if needed
            double distanceToCurrentTarget = Math.hypot(selfUnit.targetX - meleeTarget.x, selfUnit.targetY - meleeTarget.y);
            double targetMovementFeet = distanceToCurrentTarget / 7.0;
            
            // If target moved more than 3 feet, recalculate approach path
            if (targetMovementFeet > 3.0) {
                debugPrint("[MELEE-MOVEMENT] " + getDisplayName() + " target " + meleeTarget.character.getDisplayName() + " moved " + String.format("%.2f", targetMovementFeet) + " feet - updating approach path");
                updateApproachPath(selfUnit, meleeTarget, meleeWeapon);
            }
        } else {
            // Movement completed, but we're not in range yet
            // Check if we should pursue further or give up
            double maxPursuitRange = 50.0; // Maximum pursuit range in feet
            
            if (distanceFeet <= maxPursuitRange) {
                // Target is within pursuit range - start new movement
                debugPrint("[MELEE-MOVEMENT] " + getDisplayName() + " movement completed but still out of range (" + String.format("%.2f", distanceFeet) + "/" + String.format("%.2f", weaponReach) + " feet) - continuing pursuit");
                updateApproachPath(selfUnit, meleeTarget, meleeWeapon);
            } else {
                // Target too far away - give up pursuit
                debugPrint("[MELEE-MOVEMENT] " + getDisplayName() + " target " + meleeTarget.character.getDisplayName() + " too far away (" + String.format("%.2f", distanceFeet) + " feet) - cancelling pursuit");
                cancelMeleeMovement();
            }
        }
    }
    
    /**
     * Update the approach path to the melee target (used when target moves during pursuit)
     */
    private void updateApproachPath(Unit selfUnit, Unit target, MeleeWeapon meleeWeapon) {
        // Calculate optimal approach position within melee range
        double weaponReach = meleeWeapon.getTotalReach();
        double approachDistance = weaponReach - 0.5; // Leave 0.5 feet buffer
        
        // Calculate direction from target to attacker
        double dx = selfUnit.x - target.x;
        double dy = selfUnit.y - target.y;
        double currentDistance = Math.hypot(dx, dy);
        
        // Normalize direction vector
        if (currentDistance > 0) {
            dx = dx / currentDistance;
            dy = dy / currentDistance;
        }
        
        // Calculate new approach position
        double approachPixelDistance = approachDistance * 7.0; // Convert feet to pixels
        double approachX = target.x + (dx * approachPixelDistance);
        double approachY = target.y + (dy * approachPixelDistance);
        
        // Update movement target
        selfUnit.setTarget(approachX, approachY);
    }
    
    /**
     * Cancel melee movement and clear related state
     */
    private void cancelMeleeMovement() {
        isMovingToMelee = false;
        meleeTarget = null;
    }
    
    private void checkContinuousAttack(Unit shooter, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        if (!persistentAttack) return;
        if (currentTarget == null) return;
        if (currentTarget.character.isIncapacitated()) {
            // Target incapacitated - schedule automatic target change after 1 second delay
            System.out.println(getDisplayName() + " target incapacitated - scheduling automatic retargeting in 1 second");
            
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
                shooter.targetFacing = lastTargetFacing;
            }
            return;
        }
        if (this.isIncapacitated()) {
            System.out.println(getDisplayName() + " stops persistent attack - incapacitated");
            persistentAttack = false;
            currentTarget = null;
            isAttacking = false;
            
            // Preserve the current facing direction so weapon continues to aim at last target location
            if (lastTargetFacing != null && shooter != null) {
                shooter.targetFacing = lastTargetFacing;
            }
            return;
        }
        if (weapon == null) {
            persistentAttack = false;
            currentTarget = null;
            isAttacking = false;
            
            // Preserve the current facing direction so weapon continues to aim at last target location
            if (lastTargetFacing != null && shooter != null) {
                shooter.targetFacing = lastTargetFacing;
            }
            return;
        }
        
        // Handle different firing modes for continuous attacks
        handleContinuousFiring(shooter, currentTick, eventQueue, ownerId, gameCallbacks);
    }
    
    private void handleContinuousFiring(Unit shooter, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
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
    
    private void continueStandardAttack(Unit shooter, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        if (((RangedWeapon)weapon).getFiringDelay() > 0) {
            long nextAttackTick = currentTick + ((RangedWeapon)weapon).getFiringDelay();
            eventQueue.add(new ScheduledEvent(nextAttackTick, () -> {
                if (persistentAttack && currentTarget != null && !currentTarget.character.isIncapacitated() && !this.isIncapacitated()) {
                    System.out.println(getDisplayName() + " continues attacking " + currentTarget.character.getDisplayName() + " (single shot) after " + ((RangedWeapon)weapon).getFiringDelay() + " tick delay");
                    isAttacking = true;
                    scheduleAttackFromCurrentState(shooter, currentTarget, nextAttackTick, eventQueue, ownerId, gameCallbacks);
                }
            }, ownerId));
        } else {
            System.out.println(getDisplayName() + " continues attacking " + currentTarget.character.getDisplayName() + " (single shot)");
            isAttacking = true;
            scheduleAttackFromCurrentState(shooter, currentTarget, currentTick, eventQueue, ownerId, gameCallbacks);
        }
    }
    
    private void handleBurstFiring(Unit shooter, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Check if a burst is already in progress from the new burst implementation
        if (isAutomaticFiring) {
            // Burst already in progress from scheduleFiring() method - wait for it to complete
            System.out.println(getDisplayName() + " burst already in progress (" + burstShotsFired + "/" + ((RangedWeapon)weapon).getBurstSize() + "), waiting for completion");
            
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
                    if (persistentAttack && currentTarget != null && !currentTarget.character.isIncapacitated() && !this.isIncapacitated()) {
                        System.out.println(getDisplayName() + " starting next burst for auto targeting after " + ((RangedWeapon)weapon).getFiringDelay() + " tick delay");
                        isAttacking = true;
                        startAttackSequence(shooter, currentTarget, finalNextBurstTick, eventQueue, ownerId, gameCallbacks);
                    }
                }, ownerId));
            }
            return;
        }
        
        // No burst in progress - start new attack sequence which will trigger burst via scheduleFiring()
        if (currentTarget != null && !currentTarget.character.isIncapacitated() && !this.isIncapacitated()) {
            System.out.println(getDisplayName() + " starting new burst attack sequence for auto targeting");
            // Temporarily clear isAttacking to avoid duplicate attack rejection in startAttackSequence
            boolean wasAttacking = isAttacking;
            isAttacking = false;
            startAttackSequence(shooter, currentTarget, currentTick, eventQueue, ownerId, gameCallbacks);
            // Note: startAttackSequence will set isAttacking = true, so we don't need to restore it
        } else {
            System.out.println(getDisplayName() + " burst firing cancelled - no valid target");
        }
    }
    
    private void handleFullAutoFiring(Unit shooter, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        if (!isAutomaticFiring) {
            // Start new full auto sequence
            isAutomaticFiring = true;
            burstShotsFired = 1; // First shot already fired
            lastAutomaticShot = currentTick;
            System.out.println(getDisplayName() + " starts full-auto firing");
        } else {
            // Continue full auto - increment shot count
            burstShotsFired++;
        }
        
        // Schedule next shot at cyclic rate
        long nextShotTick = currentTick + ((RangedWeapon)weapon).getCyclicRate();
        eventQueue.add(new ScheduledEvent(nextShotTick, () -> {
            if (persistentAttack && currentTarget != null && !currentTarget.character.isIncapacitated() && !this.isIncapacitated()) {
                System.out.println(getDisplayName() + " continues full-auto firing at " + currentTarget.character.getDisplayName() + " (shot " + (burstShotsFired + 1) + ")");
                lastAutomaticShot = nextShotTick;
                isAttacking = true;
                scheduleAttackFromCurrentState(shooter, currentTarget, nextShotTick, eventQueue, ownerId, gameCallbacks);
            } else {
                // Stop automatic firing if conditions not met
                isAutomaticFiring = false;
                burstShotsFired = 0;
                savedAimingSpeed = null;
                System.out.println(getDisplayName() + " stops full-auto firing");
            }
        }, ownerId));
    }
    
    public boolean isUsesAutomaticTargeting() {
        return usesAutomaticTargeting;
    }
    
    public void setUsesAutomaticTargeting(boolean usesAutomaticTargeting) {
        this.usesAutomaticTargeting = usesAutomaticTargeting;
    }
    
    // Combat Experience Getters
    public int getCombatEngagements() {
        return combatEngagements;
    }
    
    public int getWoundsReceived() {
        return woundsReceived;
    }
    
    public int getTotalWoundsInflicted() {
        return woundsInflictedScratch + woundsInflictedLight + woundsInflictedSerious + woundsInflictedCritical;
    }
    
    public int getWoundsInflictedByType(WoundSeverity severity) {
        switch (severity) {
            case SCRATCH: return woundsInflictedScratch;
            case LIGHT: return woundsInflictedLight;
            case SERIOUS: return woundsInflictedSerious;
            case CRITICAL: return woundsInflictedCritical;
            default: return 0;
        }
    }
    
    public int getAttacksAttempted() {
        return attacksAttempted;
    }
    
    public int getAttacksSuccessful() {
        return attacksSuccessful;
    }
    
    public double getAccuracyPercentage() {
        return attacksAttempted > 0 ? (attacksSuccessful * 100.0 / attacksAttempted) : 0.0;
    }
    
    public int getTargetsIncapacitated() {
        return targetsIncapacitated;
    }
    
    // Headshot Statistics Getters
    public int getHeadshotsAttempted() {
        return headshotsAttempted;
    }
    
    public int getHeadshotsSuccessful() {
        return headshotsSuccessful;
    }
    
    public double getHeadshotAccuracyPercentage() {
        return headshotsAttempted > 0 ? (headshotsSuccessful * 100.0 / headshotsAttempted) : 0.0;
    }
    
    public int getHeadshotsKills() {
        return headshotsKills;
    }
    
    // Firing mode management
    public void cycleFiringMode() {
        if (weapon != null && weapon instanceof RangedWeapon) {
            ((RangedWeapon)weapon).cycleFiringMode();
        }
    }
    
    public String getCurrentFiringMode() {
        if (weapon != null && weapon instanceof RangedWeapon) {
            return ((RangedWeapon)weapon).getFiringModeDisplayName();
        }
        return "N/A";
    }
    
    public boolean hasMultipleFiringModes() {
        return weapon != null && weapon instanceof RangedWeapon && ((RangedWeapon)weapon).hasMultipleFiringModes();
    }
    
    // Hesitation mechanics
    private void triggerHesitation(WoundSeverity woundSeverity, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        // Calculate hesitation duration based on wound severity
        long hesitationDuration = calculateHesitationDuration(woundSeverity);
        
        if (hesitationDuration <= 0) {
            return; // No hesitation for scratch wounds
        }
        
        // If already hesitating, extend the hesitation (stack)
        if (isHesitating) {
            hesitationEndTick = Math.max(hesitationEndTick, currentTick + hesitationDuration);
            System.out.println(">>> HESITATION EXTENDED: " + getDisplayName() + " hesitation extended due to additional " + woundSeverity.name().toLowerCase() + " wound");
        } else {
            // Start new hesitation
            isHesitating = true;
            hesitationEndTick = currentTick + hesitationDuration;
            System.out.println(">>> HESITATION STARTED: " + getDisplayName() + " begins hesitating for " + hesitationDuration + " ticks due to " + woundSeverity.name().toLowerCase() + " wound");
            
            // Pause current actions by removing character's events and storing them
            pauseCurrentActions(eventQueue, ownerId);
            
            // Stop automatic firing if in progress
            if (isAutomaticFiring) {
                isAutomaticFiring = false;
                burstShotsFired = 0;
                System.out.println(">>> " + getDisplayName() + " automatic firing interrupted by wound");
            }
        }
        
        // Schedule hesitation end event
        eventQueue.add(new ScheduledEvent(hesitationEndTick, () -> {
            endHesitation(currentTick, eventQueue, ownerId);
        }, ownerId));
    }
    
    private long calculateHesitationDuration(WoundSeverity woundSeverity) {
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
    
    private void pauseCurrentActions(java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        // Find and remove all events belonging to this character
        List<ScheduledEvent> toRemove = new ArrayList<>();
        for (ScheduledEvent event : eventQueue) {
            if (event.getOwnerId() == ownerId) {
                toRemove.add(event);
            }
        }
        
        // Store paused events for later restoration
        pausedEvents.addAll(toRemove);
        
        // Remove from event queue
        eventQueue.removeAll(toRemove);
        
        if (!toRemove.isEmpty()) {
            System.out.println(">>> " + getDisplayName() + " paused " + toRemove.size() + " scheduled actions due to hesitation");
        }
    }
    
    private void endHesitation(long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        if (!isHesitating) {
            return; // Already ended
        }
        
        isHesitating = false;
        System.out.println(">>> HESITATION ENDED: " + getDisplayName() + " recovers from hesitation at tick " + currentTick);
        
        // Note: We don't automatically resume paused actions since they may no longer be valid
        // The character will need to restart actions (aiming, movement, etc.) from their current state
        pausedEvents.clear();
        
        // Reset attack state to allow new commands
        isAttacking = false;
    }
    
    public boolean isCurrentlyHesitating(long currentTick) {
        return isHesitating && currentTick < hesitationEndTick;
    }
    
    // Bravery check mechanics
    public void performBraveryCheck(long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, String reason) {
        // Skip bravery checks for incapacitated characters
        if (isIncapacitated()) {
            return;
        }
        
        // Calculate bravery check target number: 50 + coolness modifier
        int coolnessModifier = statToModifier(this.coolness);
        int targetNumber = 50 + coolnessModifier;
        
        // Roll d100 for bravery check
        double roll = Math.random() * 100;
        
        System.out.println(">>> BRAVERY CHECK: " + getDisplayName() + " rolls " + String.format("%.1f", roll) + " vs " + targetNumber + " (" + reason + ")");
        
        if (roll >= targetNumber) {
            // Bravery check failed
            braveryCheckFailures++;
            braveryPenaltyEndTick = currentTick + 180; // 3 seconds (180 ticks)
            
            System.out.println(">>> BRAVERY FAILED: " + getDisplayName() + " fails bravery check! Total failures: " + braveryCheckFailures + " (penalty: -" + (braveryCheckFailures * 10) + " accuracy)");
            
            // Schedule bravery recovery event
            eventQueue.add(new ScheduledEvent(braveryPenaltyEndTick, () -> {
                recoverFromBraveryFailure(currentTick);
            }, ownerId));
        } else {
            System.out.println(">>> BRAVERY PASSED: " + getDisplayName() + " passes bravery check");
        }
    }
    
    private void recoverFromBraveryFailure(long currentTick) {
        if (braveryCheckFailures > 0) {
            braveryCheckFailures--;
            System.out.println(">>> BRAVERY RECOVERY: " + getDisplayName() + " recovers from bravery failure. Remaining failures: " + braveryCheckFailures);
            
            // If more failures remain, the penalty continues
            if (braveryCheckFailures > 0) {
                braveryPenaltyEndTick = currentTick + 180; // Reset duration for remaining penalties
            }
        }
    }
    
    public int getBraveryPenalty(long currentTick) {
        if (currentTick < braveryPenaltyEndTick && braveryCheckFailures > 0) {
            return braveryCheckFailures * 10; // -10 per failure
        }
        return 0;
    }
    
    public boolean isUnderBraveryPenalty(long currentTick) {
        return getBraveryPenalty(currentTick) > 0;
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
            System.out.println(">>> " + getDisplayName() + " movement restricted to " + maxAllowed.getDisplayName() + " due to leg wounds");
        }
        
        // Force prone if both legs are wounded
        if (hasBothLegsWounded() && currentPosition != PositionState.PRONE) {
            currentPosition = PositionState.PRONE;
            currentMovementType = MovementType.CRAWL;
            System.out.println(">>> " + getDisplayName() + " forced prone due to wounds to both legs");
        }
    }
    
    /**
     * Check if target is within melee range of attacker using edge-to-edge distance
     */
    private boolean isInMeleeRange(Unit attacker, Unit target, MeleeWeapon weapon) {
        double centerToCenter = Math.hypot(target.x - attacker.x, target.y - attacker.y);
        // Convert to edge-to-edge by subtracting target radius (1.5 feet = 10.5 pixels)
        double edgeToEdge = centerToCenter - (1.5 * 7.0);
        double pixelRange = weapon.getTotalReach() * 7.0; // Convert feet to pixels (7 pixels = 1 foot)
        
        return edgeToEdge <= pixelRange;
    }
    
    /**
     * Helper method to print debug messages only when debug mode is enabled.
     * Uses reflection to access GameRenderer.isDebugMode() from the default package.
     */
    private void debugPrint(String message) {
        try {
            Class<?> gameRendererClass = Class.forName("GameRenderer");
            java.lang.reflect.Method isDebugModeMethod = gameRendererClass.getMethod("isDebugMode");
            boolean isDebugMode = (Boolean) isDebugModeMethod.invoke(null);
            if (isDebugMode) {
                System.out.println(message);
            }
        } catch (Exception e) {
            // If reflection fails, silently skip debug output
            // This prevents crashes if GameRenderer class structure changes
        }
    }
}
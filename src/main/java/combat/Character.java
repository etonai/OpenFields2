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
    public Weapon weapon;
    public WeaponState currentWeaponState;
    public Unit currentTarget;
    public boolean persistentAttack;
    public boolean isAttacking;
    public int faction;
    public boolean usesAutomaticTargeting;
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

    // Legacy constructors for backwards compatibility with tests
    public Character(String nickname, int dexterity, int health, int coolness, int strength, int reflexes, Handedness handedness) {
        this.id = 0;
        this.nickname = nickname;
        this.firstName = nickname;
        this.lastName = "";
        this.birthdate = new Date();
        this.themeId = "test_theme";
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
        this.skills = new ArrayList<>();
        this.wounds = new ArrayList<>();
    }

    public Character(String nickname, int dexterity, int health, int coolness, int strength, int reflexes, Handedness handedness, Weapon weapon) {
        this(nickname, dexterity, health, coolness, strength, reflexes, handedness);
        this.weapon = weapon;
    }

    public Character(String nickname, int dexterity, int health, int coolness, int strength, int reflexes, Handedness handedness, List<Skill> skills) {
        this(nickname, dexterity, health, coolness, strength, reflexes, handedness);
        this.skills = skills != null ? skills : new ArrayList<>();
    }

    public Character(String nickname, int dexterity, int health, int coolness, int strength, int reflexes, Handedness handedness, Weapon weapon, List<Skill> skills) {
        this(nickname, dexterity, health, coolness, strength, reflexes, handedness);
        this.weapon = weapon;
        this.skills = skills != null ? skills : new ArrayList<>();
    }

    public Character(int id, String nickname, String firstName, String lastName, Date birthdate, String themeId, int dexterity, int health, int coolness, int strength, int reflexes, Handedness handedness) {
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
        this.skills = new ArrayList<>();
        this.wounds = new ArrayList<>();
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
        this.skills = new ArrayList<>();
        this.wounds = new ArrayList<>();
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
    }

    public int getId() {
        return id;
    }
    
    public String getNickname() {
        return nickname;
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
        
        // Enforce movement restrictions immediately after adding wound
        enforceMovementRestrictions();
        
        // Note: Hesitation will not be triggered without event queue context
    }
    
    public boolean isIncapacitated() {
        boolean incapacitated = false;
        
        if (health <= 0) {
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
        scheduleAttackFromCurrentState(shooter, target, currentTick, eventQueue, ownerId, gameCallbacks);
    }
    
    public void startReadyWeaponSequence(Unit unit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId) {
        if (weapon == null || currentWeaponState == null) return;
        
        scheduleReadyFromCurrentState(unit, currentTick, eventQueue, ownerId);
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
            
            if (weapon.ammunition <= 0) {
                System.out.println("*** " + getDisplayName() + " tries to fire " + weapon.name + " but it's out of ammunition!");
            } else {
                weapon.ammunition--;
                System.out.println("*** " + getDisplayName() + " fires a " + weapon.getProjectileName() + " from " + weapon.name + " (ammo remaining: " + weapon.ammunition + ")");
                
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
                if (weapon.currentFiringMode == FiringMode.BURST && !isAutomaticFiring) {
                    isAutomaticFiring = true;
                    burstShotsFired = 1; // First shot just fired
                    lastAutomaticShot = fireTick;
                    System.out.println(getDisplayName() + " starts burst firing (" + burstShotsFired + "/" + weapon.burstSize + ")");
                    
                    // Schedule remaining shots in the burst
                    for (int shot = 2; shot <= weapon.burstSize; shot++) {
                        long nextShotTick = fireTick + (weapon.cyclicRate * (shot - 1));
                        final int shotNumber = shot;
                        eventQueue.add(new ScheduledEvent(nextShotTick, () -> {
                            if (currentTarget != null && !currentTarget.character.isIncapacitated() && !this.isIncapacitated() && weapon.ammunition > 0) {
                                weapon.ammunition--;
                                burstShotsFired++;
                                System.out.println(getDisplayName() + " burst fires shot " + burstShotsFired + "/" + weapon.burstSize + " (9mm round, ammo remaining: " + weapon.ammunition + ")");
                                
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
                                if (burstShotsFired >= weapon.burstSize) {
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
                    if (weapon.ammunition <= 0 && canReload()) {
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
        if (weapon == null || weapon.currentFiringMode == null) {
            return currentAimingSpeed;
        }
        
        switch (weapon.currentFiringMode) {
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
    
    public boolean canReload() {
        if (weapon == null) return false;
        if (weapon.ammunition >= weapon.maxAmmunition) return false;
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
                             " (" + weapon.ammunition + "/" + weapon.maxAmmunition + ") at tick " + reloadCompleteTick);
            
            // Continue reloading if needed for single-round weapons
            if (weapon.reloadType == ReloadType.SINGLE_ROUND && weapon.ammunition < weapon.maxAmmunition) {
                continueReloading(unit, reloadCompleteTick, eventQueue, ownerId, gameCallbacks);
            } else {
                currentWeaponState = weapon.getStateByName("ready");
                System.out.println(getDisplayName() + " finished reloading " + weapon.getName() + 
                                 " (" + weapon.ammunition + "/" + weapon.maxAmmunition + ") at tick " + reloadCompleteTick);
                // Check for persistent attack after reload
                checkContinuousAttack(unit, reloadCompleteTick, eventQueue, ownerId, gameCallbacks);
            }
        }, ownerId));
    }
    
    private void continueReloading(Unit unit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        if (weapon == null || weapon.ammunition >= weapon.maxAmmunition) {
            currentWeaponState = weapon.getStateByName("ready");
            return;
        }
        
        long reloadTicks = calculateReloadSpeed();
        long reloadCompleteTick = currentTick + reloadTicks;
        
        eventQueue.add(new ScheduledEvent(reloadCompleteTick, () -> {
            performReload();
            System.out.println(getDisplayName() + " loads one round into " + weapon.getName() + 
                             " (" + weapon.ammunition + "/" + weapon.maxAmmunition + ") at tick " + reloadCompleteTick);
            
            // Continue reloading if still not full
            if (weapon.ammunition < weapon.maxAmmunition) {
                continueReloading(unit, reloadCompleteTick, eventQueue, ownerId, gameCallbacks);
            } else {
                currentWeaponState = weapon.getStateByName("ready");
                System.out.println(getDisplayName() + " finished reloading " + weapon.getName() + 
                                 " (" + weapon.ammunition + "/" + weapon.maxAmmunition + ") at tick " + reloadCompleteTick);
                // Check for persistent attack after reload
                checkContinuousAttack(unit, reloadCompleteTick, eventQueue, ownerId, gameCallbacks);
            }
        }, ownerId));
    }
    
    private long calculateReloadSpeed() {
        int reflexesModifier = statToModifier(this.reflexes);
        double reflexesSpeedMultiplier = 1.0 - (reflexesModifier * 0.01);
        return Math.round(weapon.reloadTicks * reflexesSpeedMultiplier);
    }
    
    private void performReload() {
        if (weapon.reloadType == ReloadType.SINGLE_ROUND) {
            weapon.ammunition = Math.min(weapon.ammunition + 1, weapon.maxAmmunition);
        } else {
            weapon.ammunition = weapon.maxAmmunition;
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
            if (weapon != null && distance / 7.0 > weapon.maximumRange) {
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
        Unit nearestZoneTarget = null;
        Unit nearestGlobalTarget = null;
        double nearestZoneDistance = Double.MAX_VALUE;
        double nearestGlobalDistance = Double.MAX_VALUE;
        
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
            if (weapon != null && distance / 7.0 > weapon.maximumRange) {
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
        return nearestZoneTarget != null ? nearestZoneTarget : nearestGlobalTarget;
    }
    
    private void performAutomaticTargetChange(Unit shooter, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        // Only proceed if still in persistent attack mode and not incapacitated
        if (!persistentAttack || this.isIncapacitated() || weapon == null) {
            System.out.println(getDisplayName() + " automatic retargeting cancelled - conditions no longer met");
            persistentAttack = false;
            currentTarget = null;
            isAttacking = false;
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
            // No valid targets found - disable persistent attack
            persistentAttack = false;
            currentTarget = null;
            isAttacking = false;
            System.out.println("[AUTO-RETARGET] " + getDisplayName() + " found no valid targets within range, disabling automatic targeting");
        }
    }
    
    public void updateAutomaticTargeting(Unit selfUnit, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, GameCallbacks gameCallbacks) {
        // Only execute if automatic targeting is enabled
        if (!usesAutomaticTargeting) return;
        
        // Skip if character is incapacitated
        if (this.isIncapacitated()) return;
        
        // Skip if character has no weapon
        if (weapon == null) return;
        
        // Skip if character is already attacking (let existing attack complete)
        if (isAttacking) return;
        
        // Check if current target is still valid
        boolean currentTargetValid = currentTarget != null 
            && !currentTarget.character.isIncapacitated() 
            && this.isHostileTo(currentTarget.character);
        
        if (!currentTargetValid) {
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
                // No targets found - disable persistent attack
                if (persistentAttack) {
                    persistentAttack = false;
                    currentTarget = null;
                    System.out.println("[AUTO-TARGET] " + getDisplayName() + " found no valid targets within range, disabling automatic targeting");
                }
            }
        }
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
            
            // Clear current target but maintain persistent attack mode
            currentTarget = null;
            isAttacking = false;
            return;
        }
        if (this.isIncapacitated()) {
            System.out.println(getDisplayName() + " stops persistent attack - incapacitated");
            persistentAttack = false;
            currentTarget = null;
            isAttacking = false;
            return;
        }
        if (weapon == null) {
            persistentAttack = false;
            currentTarget = null;
            isAttacking = false;
            return;
        }
        
        // Handle different firing modes for continuous attacks
        handleContinuousFiring(shooter, currentTick, eventQueue, ownerId, gameCallbacks);
    }
    
    private void handleContinuousFiring(Unit shooter, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        if (weapon == null || weapon.currentFiringMode == null) {
            // Default behavior for weapons without firing modes
            continueStandardAttack(shooter, currentTick, eventQueue, ownerId, gameCallbacks);
            return;
        }
        
        switch (weapon.currentFiringMode) {
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
        if (weapon.firingDelay > 0) {
            long nextAttackTick = currentTick + weapon.firingDelay;
            eventQueue.add(new ScheduledEvent(nextAttackTick, () -> {
                if (persistentAttack && currentTarget != null && !currentTarget.character.isIncapacitated() && !this.isIncapacitated()) {
                    System.out.println(getDisplayName() + " continues attacking " + currentTarget.character.getDisplayName() + " (single shot) after " + weapon.firingDelay + " tick delay");
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
            System.out.println(getDisplayName() + " burst already in progress (" + burstShotsFired + "/" + weapon.burstSize + "), waiting for completion");
            
            // Calculate when current burst will complete and schedule next burst
            int remainingShots = weapon.burstSize - burstShotsFired;
            if (remainingShots > 0) {
                // Schedule next burst after current burst completes + firing delay
                // Full burst duration = (burstSize - 1) * cyclicRate + firing delay
                long fullBurstDuration = (weapon.burstSize - 1) * weapon.cyclicRate;
                long nextBurstTick = lastAutomaticShot + fullBurstDuration + weapon.firingDelay;
                
                // Ensure we don't schedule in the past
                if (nextBurstTick <= currentTick) {
                    nextBurstTick = currentTick + weapon.firingDelay;
                }
                
                final long finalNextBurstTick = nextBurstTick;
                eventQueue.add(new ScheduledEvent(finalNextBurstTick, () -> {
                    if (persistentAttack && currentTarget != null && !currentTarget.character.isIncapacitated() && !this.isIncapacitated()) {
                        System.out.println(getDisplayName() + " starting next burst for auto targeting after " + weapon.firingDelay + " tick delay");
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
            isAttacking = true;
            startAttackSequence(shooter, currentTarget, currentTick, eventQueue, ownerId, gameCallbacks);
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
        long nextShotTick = currentTick + weapon.cyclicRate;
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
        if (weapon != null) {
            weapon.cycleFiringMode();
        }
    }
    
    public String getCurrentFiringMode() {
        if (weapon != null) {
            return weapon.getFiringModeDisplayName();
        }
        return "N/A";
    }
    
    public boolean hasMultipleFiringModes() {
        return weapon != null && weapon.hasMultipleFiringModes();
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
}
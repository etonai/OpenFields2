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
    public Weapon weapon;
    public WeaponState currentWeaponState;
    public Unit currentTarget;
    public boolean persistentAttack;
    public boolean isAttacking;
    public int faction;
    public boolean usesAutomaticTargeting;
    public List<Skill> skills;
    public List<Wound> wounds;

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
            this.currentMovementType = currentMovementType.increase();
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
            this.currentAimingSpeed = currentAimingSpeed.decrease();
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
    
    public void addWound(Wound wound) {
        wounds.add(wound);
    }
    
    public boolean isIncapacitated() {
        if (health <= 0) {
            return true;
        }
        // Check for any critical wounds
        for (Wound wound : wounds) {
            if (wound.getSeverity() == WoundSeverity.CRITICAL) {
                return true;
            }
        }
        return false;
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
            long adjustedAimingTime = Math.round(currentWeaponState.ticks * currentAimingSpeed.getTimingMultiplier() * calculateAimingSpeedMultiplier());
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
                
                double dx = target.x - shooter.x;
                double dy = target.y - shooter.y;
                double distancePixels = Math.hypot(dx, dy);
                double distanceFeet = distancePixels / 7.0; // pixelsToFeet conversion
                System.out.println("*** " + getDisplayName() + " shoots a " + weapon.getProjectileName() + " at " + target.character.getDisplayName() + " at distance " + String.format("%.2f", distanceFeet) + " feet using " + weapon.name + " at tick " + fireTick);
                
                gameCallbacks.scheduleProjectileImpact(shooter, target, weapon, fireTick, distanceFeet);
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
            
            // Check if this is the nearest target
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestTarget = unit;
            }
        }
        
        return nearestTarget;
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
            // Find a new target
            Unit newTarget = findNearestHostileTarget(selfUnit, gameCallbacks);
            
            if (newTarget != null) {
                // Target found - start attacking
                currentTarget = newTarget;
                persistentAttack = true;
                
                // Calculate distance for logging
                double dx = newTarget.x - selfUnit.x;
                double dy = newTarget.y - selfUnit.y;
                double distanceFeet = Math.hypot(dx, dy) / 7.0; // Convert pixels to feet
                
                System.out.println("[AUTO-TARGET] " + getDisplayName() + " acquired target " + 
                                 newTarget.character.getDisplayName() + " at distance " + 
                                 String.format("%.1f", distanceFeet) + " feet");
                
                // Start attack sequence
                startAttackSequence(selfUnit, newTarget, currentTick, eventQueue, selfUnit.getId(), gameCallbacks);
            } else {
                // No targets found - disable persistent attack
                if (persistentAttack) {
                    persistentAttack = false;
                    currentTarget = null;
                    System.out.println("[AUTO-TARGET] " + getDisplayName() + " found no hostile targets, disabling automatic targeting");
                }
            }
        }
    }
    
    private void checkContinuousAttack(Unit shooter, long currentTick, java.util.PriorityQueue<ScheduledEvent> eventQueue, int ownerId, GameCallbacks gameCallbacks) {
        if (!persistentAttack) return;
        if (currentTarget == null) return;
        if (currentTarget.character.isIncapacitated()) {
            System.out.println(getDisplayName() + " stops persistent attack - target incapacitated");
            persistentAttack = false;
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
        
        // Continue attacking
        System.out.println(getDisplayName() + " continues attacking " + currentTarget.character.getDisplayName() + " (persistent mode)");
        isAttacking = true;
        scheduleAttackFromCurrentState(shooter, currentTarget, currentTick, eventQueue, ownerId, gameCallbacks);
    }
    
    public boolean isUsesAutomaticTargeting() {
        return usesAutomaticTargeting;
    }
    
    public void setUsesAutomaticTargeting(boolean usesAutomaticTargeting) {
        this.usesAutomaticTargeting = usesAutomaticTargeting;
    }
}
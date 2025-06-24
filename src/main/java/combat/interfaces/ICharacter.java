package combat.interfaces;

import combat.*;
import game.interfaces.IUnit;
import java.util.List;
import java.util.Date;

/**
 * Platform-independent interface for game characters.
 * Defines the core contract for characters without any rendering dependencies.
 */
public interface ICharacter {
    
    // Basic Attributes
    
    int getId();
    void setId(int id);
    
    String getNickname();
    void setNickname(String nickname);
    
    String getFirstName();
    void setFirstName(String firstName);
    
    String getLastName();
    void setLastName(String lastName);
    
    Date getBirthdate();
    void setBirthdate(Date birthdate);
    
    String getThemeId();
    void setThemeId(String themeId);
    
    String getDisplayName();
    
    // Physical Attributes
    
    int getDexterity();
    void setDexterity(int dexterity);
    
    int getCurrentDexterity();
    void setCurrentDexterity(int currentDexterity);
    
    int getHealth();
    void setHealth(int health);
    
    int getCurrentHealth();
    void setCurrentHealth(int currentHealth);
    
    int getCoolness();
    void setCoolness(int coolness);
    
    int getStrength();
    void setStrength(int strength);
    
    int getReflexes();
    void setReflexes(int reflexes);
    
    Handedness getHandedness();
    void setHandedness(Handedness handedness);
    
    // Movement and Positioning
    
    double getBaseMovementSpeed();
    void setBaseMovementSpeed(double speed);
    
    MovementType getCurrentMovementType();
    void setCurrentMovementType(MovementType type);
    
    AimingSpeed getCurrentAimingSpeed();
    void setCurrentAimingSpeed(AimingSpeed speed);
    
    PositionState getCurrentPosition();
    void setCurrentPosition(PositionState position);
    
    // Weapons
    
    Weapon getWeapon();
    void setWeapon(Weapon weapon);
    
    RangedWeapon getRangedWeapon();
    void setRangedWeapon(RangedWeapon weapon);
    
    MeleeWeapon getMeleeWeapon();
    void setMeleeWeapon(MeleeWeapon weapon);
    
    boolean isMeleeCombatMode();
    void setMeleeCombatMode(boolean melee);
    
    WeaponState getCurrentWeaponState();
    void setCurrentWeaponState(WeaponState state);
    
    // Combat State
    
    IUnit getCurrentTarget();
    void setCurrentTarget(IUnit target);
    
    boolean isPersistentAttack();
    void setPersistentAttack(boolean persistent);
    
    boolean isAttacking();
    void setAttacking(boolean attacking);
    
    int getFaction();
    void setFaction(int faction);
    
    boolean isUsesAutomaticTargeting();
    void setUsesAutomaticTargeting(boolean autoTarget);
    
    FiringMode getPreferredFiringMode();
    void setPreferredFiringMode(FiringMode mode);
    
    // Skills and Wounds
    
    List<Skill> getSkills();
    void setSkills(List<Skill> skills);
    
    List<Wound> getWounds();
    void setWounds(List<Wound> wounds);
    void addWound(Wound wound);
    
    // Combat Statistics
    
    int getCombatEngagements();
    void setCombatEngagements(int engagements);
    
    int getWoundsReceived();
    void setWoundsReceived(int wounds);
    
    int getAttacksAttempted();
    void setAttacksAttempted(int attacks);
    
    int getAttacksSuccessful();
    void setAttacksSuccessful(int attacks);
    
    int getTargetsIncapacitated();
    void setTargetsIncapacitated(int targets);
    
    // Ranged Combat Statistics
    
    int getRangedAttacksAttempted();
    void setRangedAttacksAttempted(int attacks);
    
    int getRangedAttacksSuccessful();
    void setRangedAttacksSuccessful(int attacks);
    
    int getRangedWoundsInflicted();
    void setRangedWoundsInflicted(int wounds);
    
    // Melee Combat Statistics
    
    int getMeleeAttacksAttempted();
    void setMeleeAttacksAttempted(int attacks);
    
    int getMeleeAttacksSuccessful();
    void setMeleeAttacksSuccessful(int attacks);
    
    int getMeleeWoundsInflicted();
    void setMeleeWoundsInflicted(int wounds);
    
    // State Checks
    
    boolean isIncapacitated();
    boolean isHesitating();
    boolean isAutomaticFiring();
    
    // Burst/Auto Fire State
    
    int getBurstShotsFired();
    void setBurstShotsFired(int shots);
    
    long getLastAutomaticShot();
    void setLastAutomaticShot(long tick);
    
    boolean isReloading();
    void setReloading(boolean reloading);
    
    // Core Combat Methods
    
    /**
     * Checks if character should apply burst/auto accuracy penalty.
     * @return true if penalty should apply
     */
    boolean shouldApplyBurstAutoPenalty();
    
    /**
     * Gets the weapon ready speed multiplier based on reflexes and skills.
     * @return speed multiplier (lower is faster)
     */
    double getWeaponReadySpeedMultiplier();
    
    /**
     * Checks if the specified state is a weapon preparation state.
     * @param stateName the state to check
     * @return true if preparation state
     */
    boolean isWeaponPreparationState(String stateName);
    
    /**
     * Calculates weapon ready speed multiplier including skills.
     * @return combined speed multiplier
     */
    double calculateWeaponReadySpeedMultiplier();
    
    /**
     * Gets total modifier from character wounds.
     * @return wound modifier
     */
    int getWoundModifier();
    
    /**
     * Updates the character state for the current tick.
     * @param currentTick current game tick
     */
    void update(long currentTick);
}
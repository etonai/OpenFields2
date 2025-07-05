/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import combat.*;
import combat.managers.DefenseManager;
import data.SkillsManager;
import game.Unit;
import utils.GameConstants;
import utils.RandomProvider;
import java.util.List;
import java.util.Random;

public final class CombatCalculator {
    
    public static HitResult determineHit(Unit shooter, Unit target, double distanceFeet, double maximumRange, int weaponAccuracy, int weaponDamage, boolean debugMode, int stressModifier, long currentTick) {
        return determineHit(shooter, target, distanceFeet, maximumRange, weaponAccuracy, weaponDamage, debugMode, stressModifier, currentTick, false);
    }
    
    public static HitResult determineHit(Unit shooter, Unit target, double distanceFeet, double maximumRange, int weaponAccuracy, int weaponDamage, boolean debugMode, int stressModifier, long currentTick, boolean isMeleeAttack) {
        return determineHit(shooter, target, distanceFeet, maximumRange, weaponAccuracy, weaponDamage, debugMode, stressModifier, currentTick, isMeleeAttack, RandomProvider.getCurrentRandom());
    }
    
    /**
     * Test-specific overload for determineHit with custom Random instance.
     * Allows precise control over random number generation for testing.
     */
    public static HitResult determineHit(Unit shooter, Unit target, double distanceFeet, double maximumRange, int weaponAccuracy, int weaponDamage, boolean debugMode, int stressModifier, long currentTick, boolean isMeleeAttack, Random testRandom) {
        double weaponModifier = weaponAccuracy;
        double rangeModifier = calculateRangeModifier(distanceFeet, maximumRange);
        double movementModifier = calculateMovementModifier(shooter);
        
        // DevCycle 27: System 3 - Calculate accumulated aiming bonus
        AccumulatedAimingBonus earnedBonus = shooter.character.calculateEarnedAimingBonus(currentTick);
        double aimingSpeedModifier;
        boolean useVeryCarefulBenefits = false;
        
        if (earnedBonus != AccumulatedAimingBonus.NONE) {
            // Use only earned bonus when present
            aimingSpeedModifier = earnedBonus.getAccuracyModifier();
            useVeryCarefulBenefits = earnedBonus.isVeryCareful() && meetsVeryCarefulSkillRequirements(shooter.character);
        } else {
            // Fall back to selected aiming speed
            AimingSpeed selectedSpeed = shooter.character.getCurrentAimingSpeed();
            aimingSpeedModifier = selectedSpeed.getAccuracyModifier();
            useVeryCarefulBenefits = selectedSpeed.isVeryCareful() && meetsVeryCarefulSkillRequirements(shooter.character);
        }
        
        // For burst fire shots 2+, ignore all aiming modifiers
        if (shooter.character.getBurstShotsFired() > 0) {
            aimingSpeedModifier = 0; // Burst penalty applied elsewhere
        }
        
        double burstAutoPenalty = shooter.character.shouldApplyBurstAutoPenalty() ? -20.0 : 0.0;
        double targetMovementModifier = calculateTargetMovementModifier(shooter, target);
        double woundModifier = calculateWoundModifier(shooter);
        double stressMod = Math.min(0, stressModifier + GameConstants.statToModifier(shooter.character.coolness));
        double skillModifier = calculateSkillModifier(shooter);
        double positionModifier = calculatePositionModifier(target);
        double braveryModifier = calculateBraveryModifier(shooter, currentTick);
        double firstAttackPenalty = (shooter.character.isFirstAttackOnTarget && !useVeryCarefulBenefits) ? GameConstants.FIRST_ATTACK_PENALTY : 0;
        double firingStateModifier = shooter.character.getFiresFromAimingState() ? 0.0 : -20.0; // -20 penalty for pointedfromhip firing
        double sizeModifier = 0.0;
        double coverModifier = 0.0;
        
        // DevCycle 40: Defense system for melee attacks only
        double defenseModifier = 0.0;
        if (isMeleeAttack) {
            int defenseValue = DefenseManager.getInstance().performDefense(target.character, currentTick);
            defenseModifier = -defenseValue; // Defense applied as negative modifier to attack
        }
        
        double chanceToHit = 50.0 + GameConstants.statToModifier(shooter.character.dexterity) + stressMod + rangeModifier + weaponModifier + movementModifier + aimingSpeedModifier + burstAutoPenalty + targetMovementModifier + woundModifier + skillModifier + positionModifier + braveryModifier + firstAttackPenalty + firingStateModifier + sizeModifier + coverModifier + defenseModifier;
        
        if (distanceFeet <= maximumRange) {
            chanceToHit = Math.max(chanceToHit, 0.01);
        }
        
        double randomRoll = testRandom.nextDouble() * 100;
        
        if (debugMode) {
            System.out.println("=== HIT CALCULATION DEBUG ===");
            System.out.println("Shooter: " + shooter.character.getDisplayName() + " -> Target: " + target.character.getDisplayName());
            System.out.println("Base chance: 50.0");
            System.out.println("Dexterity modifier: " + GameConstants.statToModifier(shooter.character.dexterity) + " (dex: " + shooter.character.dexterity + ")");
            System.out.println("Stress modifier: " + stressMod + " (coolness: " + shooter.character.coolness + ":" + GameConstants.statToModifier(shooter.character.coolness) + ")");
            System.out.println("Range modifier: " + String.format("%.2f", rangeModifier) + " (distance: " + String.format("%.2f", distanceFeet) + " feet, max: " + String.format("%.2f", maximumRange) + " feet)");
            System.out.println("Weapon modifier: " + weaponModifier + " (accuracy: " + weaponAccuracy + ")");
            System.out.println("Movement modifier: " + movementModifier);
            // Show aiming modifier source (earned vs selected)
            if (earnedBonus != AccumulatedAimingBonus.NONE) {
                System.out.println("Aiming speed modifier: " + aimingSpeedModifier + " (earned " + earnedBonus.getDisplayName() + " bonus, accumulated " + shooter.character.getCurrentAimingDuration(currentTick) + " ticks)");
            } else {
                System.out.println("Aiming speed modifier: " + aimingSpeedModifier + " (selected " + shooter.character.getCurrentAimingSpeed().getDisplayName() + ")");
            }
            if (burstAutoPenalty != 0) {
                System.out.println("Burst/Auto penalty: " + burstAutoPenalty + " (bullet " + shooter.character.getBurstShotsFired() + ")");
            }
            
            // Enhanced target movement debug info
            if (target.isMoving()) {
                double perpendicularVelocity = target.getPerpendicularVelocity(shooter);
                double perpendicularVelocityFeetPerSecond = (perpendicularVelocity * 60.0) / 7.0;
                System.out.println("Target movement modifier: " + String.format("%.2f", targetMovementModifier) + 
                                 " (perpendicular velocity: " + String.format("%.2f", perpendicularVelocityFeetPerSecond) + " ft/s, " +
                                 String.format("%.2f", perpendicularVelocity) + " px/tick)");
            } else {
                System.out.println("Target movement modifier: " + targetMovementModifier + " (target stationary)");
            }
            
            System.out.println("Wound modifier: " + String.format("%.1f", woundModifier) + " " + getWoundModifierDebugInfo(shooter));
            System.out.println("Skill modifier: " + String.format("%.1f", skillModifier) + " " + getSkillDebugInfo(shooter));
            System.out.println("Position modifier: " + String.format("%.1f", positionModifier) + " (target: " + target.character.getCurrentPosition().getDisplayName() + ")");
            System.out.println("Bravery modifier: " + String.format("%.1f", braveryModifier) + " " + getBraveryModifierDebugInfo(shooter, currentTick));
            System.out.println("First attack penalty: " + firstAttackPenalty + " (first attack: " + shooter.character.isFirstAttackOnTarget + ", very careful benefits: " + useVeryCarefulBenefits + ")");
            System.out.println("Firing state modifier: " + firingStateModifier + " (firing from " + (shooter.character.getFiresFromAimingState() ? "aiming" : "pointedfromhip") + ")");
            System.out.println("Size modifier: " + sizeModifier);
            System.out.println("Cover modifier: " + coverModifier);
            if (isMeleeAttack && defenseModifier != 0.0) {
                System.out.println("Defense modifier: " + defenseModifier + " (defender successfully defended)");
            }
            System.out.println("Final chance to hit: " + String.format("%.2f", chanceToHit) + "%");
            System.out.println("Random roll: " + String.format("%.2f", randomRoll));
            System.out.println("Result: " + (randomRoll < chanceToHit ? "HIT" : "MISS"));
            System.out.println("=============================");
        }
        
        boolean hit = randomRoll < chanceToHit;
        BodyPart hitLocation = null;
        WoundSeverity woundSeverity = null;
        int actualDamage = 0;
        
        if (hit) {
            hitLocation = determineHitLocation(randomRoll, chanceToHit, testRandom);
            woundSeverity = determineWoundSeverity(randomRoll, chanceToHit, hitLocation, testRandom);
            actualDamage = calculateActualDamage(weaponDamage, woundSeverity, hitLocation);
        }
        
        return new HitResult(hit, hitLocation, woundSeverity, actualDamage);
    }
    
    public static double calculateMovementModifier(Unit shooter) {
        if (!shooter.isMoving()) {
            return 0.0; // Stationary = no penalty
        }
        
        switch (shooter.character.getCurrentMovementType()) {
            case WALK: return -5.0;
            case CRAWL: return -10.0;
            case JOG: return -15.0;
            case RUN: return -25.0;
            default: return 0.0;
        }
    }
    
    public static double calculateTargetMovementModifier(Unit shooter, Unit target) {
        if (!target.isMoving()) {
            return 0.0; // Stationary target = no modifier
        }
        
        // Get perpendicular velocity component in pixels per tick
        double perpendicularVelocity = target.getPerpendicularVelocity(shooter);
        
        // Convert from pixels per tick to feet per second for easier calculation
        // 7 pixels = 1 foot, 60 ticks = 1 second
        double perpendicularVelocityFeetPerSecond = (perpendicularVelocity * 60.0) / 7.0;
        
        // Simple formula: -2 * perpendicular speed in feet/second
        // At walking speed (~6 feet/second perpendicular), this gives about -12 modifier
        // At running speed (~12 feet/second perpendicular), this gives about -24 modifier
        return -perpendicularVelocityFeetPerSecond * 2.0;
    }
    
    public static double calculateSkillModifier(Unit shooter) {
        if (shooter.character.weapon == null) {
            return 0.0;
        }
        
        WeaponType weaponType = shooter.character.weapon.getWeaponType();
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
            case OTHER:
            default:
                return 0.0; // No skill bonus for OTHER weapon types
        }
        
        int skillLevel = shooter.character.getSkillLevel(skillName);
        double baseSkillBonus = skillLevel * 5.0;
        
        // Double the skill bonus for very careful aiming
        if (shooter.character.getCurrentAimingSpeed().isVeryCareful()) {
            return baseSkillBonus * 2.0;
        }
        
        return baseSkillBonus;
    }
    
    public static double calculatePositionModifier(Unit target) {
        return target.character.getCurrentPosition().getTargetingPenalty();
    }
    
    public static double calculateBraveryModifier(Unit shooter, long currentTick) {
        return -shooter.character.getBraveryPenalty(currentTick);
    }
    
    public static String getSkillDebugInfo(Unit shooter) {
        if (shooter.character.weapon == null) {
            return "(no weapon)";
        }
        
        WeaponType weaponType = shooter.character.weapon.getWeaponType();
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
            case OTHER:
            default:
                return "(weapon type: " + weaponType.getDisplayName() + ", no skill bonus)";
        }
        
        int skillLevel = shooter.character.getSkillLevel(skillName);
        String debugInfo = "(" + skillName + ": " + skillLevel;
        
        if (shooter.character.getCurrentAimingSpeed().isVeryCareful()) {
            debugInfo += ", very careful x2";
        }
        
        return debugInfo + ")";
    }
    
    public static String getBraveryModifierDebugInfo(Unit shooter, long currentTick) {
        int braveryPenalty = shooter.character.getBraveryPenalty(currentTick);
        if (braveryPenalty > 0) {
            return "(" + shooter.character.braveryCheckFailures + " bravery failures: -" + braveryPenalty + ")";
        } else {
            return "(no bravery penalty)";
        }
    }
    
    public static double calculateWoundModifier(Unit shooter) {
        double modifier = 0.0;
        
        for (Wound wound : shooter.character.getWounds()) {
            BodyPart bodyPart = wound.getBodyPart();
            WoundSeverity severity = wound.getSeverity();
            
            // Check for head wounds - every point of damage is -1
            if (bodyPart == BodyPart.HEAD) {
                modifier -= wound.getDamage();
            }
            // Check for dominant arm wounds - every point of damage is -1
            else if (isShootingArm(bodyPart, shooter.character.getHandedness())) {
                modifier -= wound.getDamage();
            }
            // Check for other body parts based on severity
            else {
                switch (severity) {
                    case LIGHT:
                        modifier -= 1.0;
                        break;
                    case SERIOUS:
                        modifier -= 2.0;
                        break;
                    case CRITICAL:
                        // Every point of damage from critical wound in other parts is -1
                        modifier -= wound.getDamage();
                        break;
                    case SCRATCH:
                        // No modifier for scratches in other parts
                        break;
                }
            }
        }
        
        return modifier;
    }
    
    public static boolean isShootingArm(BodyPart bodyPart, Handedness handedness) {
        switch (handedness) {
            case LEFT_HANDED:
                return bodyPart == BodyPart.LEFT_ARM;
            case RIGHT_HANDED:
                return bodyPart == BodyPart.RIGHT_ARM;
            case AMBIDEXTROUS:
                // Right arm if ambidextrous
                return bodyPart == BodyPart.RIGHT_ARM;
            default:
                return false;
        }
    }
    
    
    public static String getWoundModifierDebugInfo(Unit shooter) {
        List<Wound> wounds = shooter.character.getWounds();
        if (wounds.isEmpty()) {
            return "(no wounds)";
        }
        
        StringBuilder debug = new StringBuilder("(");
        double totalModifier = 0.0;
        boolean first = true;
        
        for (Wound wound : wounds) {
            if (!first) debug.append(", ");
            first = false;
            
            BodyPart bodyPart = wound.getBodyPart();
            WoundSeverity severity = wound.getSeverity();
            double woundModifier = 0.0;
            
            if (bodyPart == BodyPart.HEAD) {
                woundModifier = -wound.getDamage();
                debug.append("HEAD ").append(severity.name()).append(": ").append(woundModifier).append(" (").append(wound.getDamage()).append(" dmg)");
            } else if (isShootingArm(bodyPart, shooter.character.getHandedness())) {
                woundModifier = -wound.getDamage();
                String armSide = (bodyPart == BodyPart.LEFT_ARM) ? "LEFT" : "RIGHT";
                debug.append(armSide).append("_ARM(dominant) ").append(severity.name()).append(": ").append(woundModifier).append(" (").append(wound.getDamage()).append(" dmg)");
            } else {
                switch (severity) {
                    case LIGHT:
                        woundModifier = -1.0;
                        break;
                    case SERIOUS:
                        woundModifier = -2.0;
                        break;
                    case CRITICAL:
                        woundModifier = -wound.getDamage();
                        break;
                    case SCRATCH:
                        woundModifier = 0.0;
                        break;
                }
                debug.append(bodyPart.name()).append(" ").append(severity.name()).append(": ").append(woundModifier);
            }
            
            totalModifier += woundModifier;
        }
        
        debug.append(" | total: ").append(totalModifier).append(")");
        return debug.toString();
    }
    
    public static double calculateRangeModifier(double distanceFeet, double maximumRange) {
        double optimalRange = maximumRange * 0.3;
        double rangeModifier;
        
        if (distanceFeet <= optimalRange) {
            rangeModifier = 10.0 * (1.0 - distanceFeet / optimalRange);
        } else {
            double remainingRange = maximumRange - optimalRange;
            double excessDistance = distanceFeet - optimalRange;
            rangeModifier = -(excessDistance / remainingRange) * 20.0;
        }
        
        return rangeModifier;
    }
    
    public static BodyPart determineHitLocation(double randomRoll, double chanceToHit) {
        return determineHitLocation(randomRoll, chanceToHit, RandomProvider.getCurrentRandom());
    }
    
    /**
     * Test-specific overload for determineHitLocation with custom Random instance.
     */
    public static BodyPart determineHitLocation(double randomRoll, double chanceToHit, Random testRandom) {
        double excellentThreshold = chanceToHit * 0.2;
        double goodThreshold = chanceToHit * 0.7;
        
        if (randomRoll < excellentThreshold) {
            // Excellent shots have a small chance for headshots
            double headshotRoll = testRandom.nextDouble() * 100;
            if (headshotRoll < 15) { // 15% chance for headshot on excellent shots
                return BodyPart.HEAD;
            } else {
                return BodyPart.CHEST;
            }
        } else if (randomRoll < goodThreshold) {
            // Good shots rarely hit the head (2% chance)
            double headshotRoll = testRandom.nextDouble() * 100;
            if (headshotRoll < 2) {
                return BodyPart.HEAD;
            } else {
                return testRandom.nextDouble() < 0.5 ? BodyPart.CHEST : BodyPart.ABDOMEN;
            }
        } else {
            return getRandomBodyPart(testRandom);
        }
    }
    
    public static BodyPart getRandomBodyPart() {
        return getRandomBodyPart(RandomProvider.getCurrentRandom());
    }
    
    /**
     * Test-specific overload for getRandomBodyPart with custom Random instance.
     */
    public static BodyPart getRandomBodyPart(Random testRandom) {
        double roll = testRandom.nextDouble() * 100;
        
        if (roll < 12) return BodyPart.LEFT_ARM;
        else if (roll < 24) return BodyPart.RIGHT_ARM;
        else if (roll < 32) return BodyPart.LEFT_SHOULDER;
        else if (roll < 40) return BodyPart.RIGHT_SHOULDER;
        else if (roll < 50) return BodyPart.HEAD;
        else if (roll < 55) return BodyPart.LEFT_LEG;
        else return BodyPart.RIGHT_LEG;
    }
    
    public static WoundSeverity determineWoundSeverity(double randomRoll, double chanceToHit, BodyPart hitLocation) {
        return determineWoundSeverity(randomRoll, chanceToHit, hitLocation, RandomProvider.getCurrentRandom());
    }
    
    /**
     * Test-specific overload for determineWoundSeverity with custom Random instance.
     */
    public static WoundSeverity determineWoundSeverity(double randomRoll, double chanceToHit, BodyPart hitLocation, Random testRandom) {
        double excellentThreshold = chanceToHit * 0.2;
        
        // Excellent shots are always critical
        if (randomRoll < excellentThreshold) {
            return WoundSeverity.CRITICAL;
        }
        
        // Determine wound severity based on hit location
        double severityRoll = testRandom.nextDouble() * 100;
        
        if (isVitalArea(hitLocation)) {
            // HEAD/CHEST/ABDOMEN: 30% Critical, 40% Serious, 25% Light, 5% Scratch
            if (severityRoll < 30) return WoundSeverity.CRITICAL;
            else if (severityRoll < 70) return WoundSeverity.SERIOUS;
            else if (severityRoll < 95) return WoundSeverity.LIGHT;
            else return WoundSeverity.SCRATCH;
        } else {
            // ARMS/SHOULDERS/LEGS: 10% Critical, 25% Serious, 45% Light, 20% Scratch
            if (severityRoll < 10) return WoundSeverity.CRITICAL;
            else if (severityRoll < 35) return WoundSeverity.SERIOUS;
            else if (severityRoll < 80) return WoundSeverity.LIGHT;
            else return WoundSeverity.SCRATCH;
        }
    }
    
    public static boolean isVitalArea(BodyPart bodyPart) {
        return bodyPart == BodyPart.HEAD || bodyPart == BodyPart.CHEST || bodyPart == BodyPart.ABDOMEN;
    }
    
    // Backwards compatible method for tests
    public static int calculateActualDamage(int weaponDamage, WoundSeverity woundSeverity) {
        return calculateActualDamage(weaponDamage, woundSeverity, null);
    }
    
    public static int calculateActualDamage(int weaponDamage, WoundSeverity woundSeverity, BodyPart hitLocation) {
        // Add debug output to match ranged combat debugging
        if (GameRenderer.isDebugMode()) {
            System.out.println("=== RANGED DAMAGE CALCULATION DEBUG ===");
            System.out.println("Weapon damage: " + weaponDamage);
            System.out.println("Wound severity: " + woundSeverity);
            System.out.println("Hit location: " + (hitLocation != null ? hitLocation : "unknown"));
        }
        
        int baseDamage;
        switch (woundSeverity) {
            case CRITICAL:
            case SERIOUS:
                baseDamage = weaponDamage;
                break;
            case LIGHT:
                baseDamage = Math.max(1, Math.round(weaponDamage * 0.4f));
                break;
            case SCRATCH:
                baseDamage = 1;
                break;
            default:
                baseDamage = 0;
        }
        
        // Apply headshot damage multiplier
        if (hitLocation == BodyPart.HEAD) {
            // Headshots deal 1.5x damage (50% more damage)
            baseDamage = Math.round(baseDamage * 1.5f);
        }
        
        if (GameRenderer.isDebugMode()) {
            System.out.println("Scaled damage: " + baseDamage);
            System.out.println("=========================================");
        }
        
        return baseDamage;
    }
    
    /**
     * Check if character meets skill requirements for Very Careful benefits.
     * @param character The character to check
     * @return True if character can use Very Careful benefits, false otherwise
     */
    private static boolean meetsVeryCarefulSkillRequirements(combat.Character character) {
        return character.canUseVeryCarefulAiming();
    }
    
    // Private constructor to prevent instantiation
    private CombatCalculator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
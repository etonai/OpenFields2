/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

import combat.*;
import game.Unit;
import game.ScheduledEvent;
import utils.GameConstants;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class CombatResolver {
    
    private final List<Unit> units;
    private final PriorityQueue<ScheduledEvent> eventQueue;
    private final boolean debugMode;
    
    public CombatResolver(List<Unit> units, PriorityQueue<ScheduledEvent> eventQueue, boolean debugMode) {
        this.units = units;
        this.eventQueue = eventQueue;
        this.debugMode = debugMode;
    }
    
    public void resolveCombatImpact(Unit shooter, Unit target, Weapon weapon, long impactTick, HitResult hitResult) {
        if (hitResult.isHit()) {
            BodyPart hitLocation = hitResult.getHitLocation();
            WoundSeverity woundSeverity = hitResult.getWoundSeverity();
            int actualDamage = hitResult.getActualDamage();
            
            // Generate weapon-type aware combat message
            String combatMessage;
            if (weapon instanceof MeleeWeapon) {
                // Melee weapons use action verbs: "strikes", "slashes", etc.
                combatMessage = ">>> " + weapon.getName() + " strikes " + target.character.getDisplayName() + " in the " + hitLocation.name().toLowerCase() + " causing a " + woundSeverity.name().toLowerCase() + " wound at tick " + impactTick;
            } else {
                // Ranged weapons use projectile language: "projectile hit"
                combatMessage = ">>> " + weapon.getWoundDescription() + " hit " + target.character.getDisplayName() + " in the " + hitLocation.name().toLowerCase() + " causing a " + woundSeverity.name().toLowerCase() + " wound at tick " + impactTick;
            }
            System.out.println(combatMessage);
            System.out.println(">>> " + target.character.getDisplayName() + " takes " + actualDamage + " damage");
            
            // Track successful attack (legacy tracking)
            shooter.character.attacksSuccessful++;
            
            // Track by weapon type (DevCycle 12)
            if (weapon instanceof MeleeWeapon) {
                shooter.character.meleeAttacksSuccessful++;
                shooter.character.meleeWoundsInflicted++;
            } else {
                shooter.character.rangedAttacksSuccessful++;
                shooter.character.rangedWoundsInflicted++;
            }
            
            // Track wound infliction by type
            switch (woundSeverity) {
                case SCRATCH:
                    shooter.character.woundsInflictedScratch++;
                    break;
                case LIGHT:
                    shooter.character.woundsInflictedLight++;
                    break;
                case SERIOUS:
                    shooter.character.woundsInflictedSerious++;
                    break;
                case CRITICAL:
                    shooter.character.woundsInflictedCritical++;
                    break;
            }
            
            // Track headshot statistics
            if (hitLocation == BodyPart.HEAD) {
                shooter.character.headshotsAttempted++;
                shooter.character.headshotsSuccessful++;
                System.out.println(">>> HEADSHOT! " + shooter.character.getDisplayName() + " scored a headshot on " + target.character.getDisplayName());
            }
            
            // Add wound to character's wound list with hesitation mechanics
            String weaponId = weapon.getWeaponId(); // Direct access to weapon ID (DevCycle 17)
            target.character.addWound(new Wound(hitLocation, woundSeverity, weapon.getWoundDescription(), weaponId, actualDamage), impactTick, eventQueue, target.getId());
            System.out.println(">>> " + target.character.getDisplayName() + " current health: " + target.character.currentHealth + "/" + target.character.health);
            
            // Trigger bravery check for the target when wounded (weapon-type aware)
            String braveryReason;
            if (weapon instanceof MeleeWeapon) {
                braveryReason = "wounded by " + weapon.getName();
            } else {
                braveryReason = "wounded by " + weapon.getWoundDescription();
            }
            target.character.performBraveryCheck(impactTick, eventQueue, target.getId(), braveryReason);
            
            // Trigger bravery checks for allies within 30 feet (weapon-type aware)
            String allyBraveryContext;
            if (weapon instanceof MeleeWeapon) {
                allyBraveryContext = weapon.getName();
            } else {
                allyBraveryContext = weapon.getWoundDescription();
            }
            triggerAllyBraveryChecks(target, impactTick, allyBraveryContext);
            
            // Check for incapacitation
            boolean wasIncapacitated = target.character.isIncapacitated();
            if (wasIncapacitated) {
                // Track incapacitation caused by this shooter
                shooter.character.targetsIncapacitated++;
                
                // Track headshot kill
                if (hitLocation == BodyPart.HEAD) {
                    shooter.character.headshotsKills++;
                    System.out.println(">>> HEADSHOT KILL! " + target.character.getDisplayName() + " was killed by a headshot!");
                }
                
                if (woundSeverity == WoundSeverity.CRITICAL) {
                    System.out.println(">>> " + target.character.getDisplayName() + " is incapacitated by critical wound!");
                } else {
                    System.out.println(">>> " + target.character.getDisplayName() + " is incapacitated!");
                }
                target.character.baseMovementSpeed = 0;
                eventQueue.removeIf(e -> e.getOwnerId() == target.getId());
                if (debugMode) {
                    System.out.println(">>> Removed all scheduled actions for " + target.character.getDisplayName());
                }
            }
            
            applyHitHighlight(target, impactTick);
        } else {
            System.out.println(">>> " + weapon.getWoundDescription() + " missed " + target.character.getDisplayName() + " at tick " + impactTick);
            
            // Handle stray shot mechanics
            handleStrayShot(shooter, target, weapon, impactTick);
        }
    }
    
    public void handleStrayShot(Unit shooter, Unit target, Weapon weapon, long impactTick) {
        // Calculate the original trajectory from shooter to target
        double dx = target.x - shooter.x;
        double dy = target.y - shooter.y;
        double distance = Math.hypot(dx, dy);
        
        // Normalize the direction vector
        double directionX = dx / distance;
        double directionY = dy / distance;
        
        // Calculate how far the missed shot travels (extend beyond target)
        double missDistance = distance + (Math.random() * 140 + 70); // 10-30 feet beyond target
        
        // Calculate the actual impact point of the missed shot
        double missX = shooter.x + directionX * missDistance;
        double missY = shooter.y + directionY * missDistance;
        
        // Find potential stray targets within danger circle
        List<Unit> potentialTargets = findPotentialStrayTargets(shooter, target, missX, missY, ((RangedWeapon)weapon).getMaximumRange());
        
        // Calculate total stray shot probability based on position states
        double totalProbability = calculateStrayProbability(potentialTargets);
        
        // Cap at 50% maximum probability
        totalProbability = Math.min(totalProbability, 50.0);
        
        if (totalProbability > 0) {
            double strayRoll = Math.random() * 100;
            
            if (strayRoll < totalProbability) {
                // Stray shot occurs - select target based on position weights
                Unit strayTarget = selectStrayTarget(potentialTargets);
                if (strayTarget != null) {
                    performStrayHit(shooter, strayTarget, weapon, impactTick);
                }
            }
        }
    }
    
    public List<Unit> findPotentialStrayTargets(Unit shooter, Unit originalTarget, double missX, double missY, double weaponRange) {
        List<Unit> potentialTargets = new ArrayList<>();
        
        // Define stray shot area - cone extending from original trajectory
        double strayRadius = 105; // 15 feet radius around miss point
        
        for (Unit unit : units) {
            // Include all units (shooter can hit themselves, original target gets bravery check)
            
            // Check if unit is within stray shot radius of miss point
            double distanceToMiss = Math.hypot(unit.x - missX, unit.y - missY);
            
            // Also check if unit is within weapon range from shooter
            double distanceFromShooter = Math.hypot(unit.x - shooter.x, unit.y - shooter.y);
            
            if (distanceToMiss <= strayRadius && distanceFromShooter <= weaponRange * 7) { // Convert feet to pixels
                potentialTargets.add(unit);
            }
        }
        
        return potentialTargets;
    }
    
    
    public WoundSeverity determineStrayWoundSeverity() {
        // Stray shots tend to be less severe
        double roll = Math.random() * 100;
        
        if (roll < 5) return WoundSeverity.CRITICAL;      // 5% critical
        else if (roll < 20) return WoundSeverity.SERIOUS; // 15% serious  
        else if (roll < 60) return WoundSeverity.LIGHT;   // 40% light
        else return WoundSeverity.SCRATCH;                // 40% scratch
    }
    
    public double calculateStrayProbability(List<Unit> potentialTargets) {
        double totalProbability = 0.0;
        for (Unit unit : potentialTargets) {
            totalProbability += unit.character.getCurrentPosition().getStrayProbabilityContribution();
        }
        return totalProbability;
    }
    
    public Unit selectStrayTarget(List<Unit> potentialTargets) {
        if (potentialTargets.isEmpty()) {
            return null;
        }
        
        // Calculate total weight
        double totalWeight = 0.0;
        for (Unit unit : potentialTargets) {
            totalWeight += unit.character.getCurrentPosition().getHitSelectionWeight();
        }
        
        // Select random target based on weights
        double random = Math.random() * totalWeight;
        double currentWeight = 0.0;
        
        for (Unit unit : potentialTargets) {
            currentWeight += unit.character.getCurrentPosition().getHitSelectionWeight();
            if (random <= currentWeight) {
                return unit;
            }
        }
        
        // Fallback to last unit if rounding errors occur
        return potentialTargets.get(potentialTargets.size() - 1);
    }
    
    public void performStrayHit(Unit shooter, Unit strayTarget, Weapon weapon, long impactTick) {
        System.out.println(">>> STRAY SHOT! " + weapon.getWoundDescription() + " hits " + strayTarget.character.getDisplayName() + " (position: " + strayTarget.character.getCurrentPosition().getDisplayName() + ")");
        
        // Calculate stray shot accuracy - reduced chance to hit
        double baseChance = 15.0; // Base 15% chance for stray hits
        double positionModifier = strayTarget.character.getCurrentPosition().getTargetingPenalty();
        double finalChance = Math.max(1.0, baseChance + positionModifier);
        
        double roll = Math.random() * 100;
        
        if (roll < finalChance) {
            // Stray hit successful! Calculate hit details
            BodyPart hitLocation = CombatCalculator.getRandomBodyPart();
            WoundSeverity woundSeverity = determineStrayWoundSeverity();
            int baseDamage = CombatCalculator.calculateActualDamage(weapon.damage, woundSeverity, hitLocation);
            int strayDamage = Math.max(1, Math.round(baseDamage * 0.7f)); // 30% damage reduction for stray shots
            
            System.out.println(">>> " + strayTarget.character.getDisplayName() + " hit in the " + hitLocation.name().toLowerCase() + " causing a " + woundSeverity.name().toLowerCase() + " wound");
            
            // Apply damage (will be handled by addWound method)
            System.out.println(">>> " + strayTarget.character.getDisplayName() + " takes " + strayDamage + " stray damage");
            
            // Track successful attack for shooter (stray hits still count)
            shooter.character.attacksSuccessful++;
            shooter.character.rangedAttacksSuccessful++;
            shooter.character.rangedWoundsInflicted++;
            
            // Track wound infliction by type
            switch (woundSeverity) {
                case SCRATCH:
                    shooter.character.woundsInflictedScratch++;
                    break;
                case LIGHT:
                    shooter.character.woundsInflictedLight++;
                    break;
                case SERIOUS:
                    shooter.character.woundsInflictedSerious++;
                    break;
                case CRITICAL:
                    shooter.character.woundsInflictedCritical++;
                    break;
            }
            
            // Add wound to target with hesitation mechanics
            String weaponId = weapon.getWeaponId(); // Direct access to weapon ID (DevCycle 17)
            strayTarget.character.addWound(new Wound(hitLocation, woundSeverity, weapon.getWoundDescription() + " (stray)", weaponId, strayDamage), impactTick, eventQueue, strayTarget.getId());
            System.out.println(">>> " + strayTarget.character.getDisplayName() + " current health: " + strayTarget.character.currentHealth + "/" + strayTarget.character.health);
            
            // Trigger bravery check for stray shot victim
            strayTarget.character.performBraveryCheck(impactTick, eventQueue, strayTarget.getId(), "hit by stray " + weapon.getWoundDescription());
            
            // Trigger bravery checks for allies within 30 feet
            triggerAllyBraveryChecks(strayTarget, impactTick, weapon.getWoundDescription() + " (stray)");
            
            // Check for incapacitation from stray shot
            if (strayTarget.character.isIncapacitated()) {
                shooter.character.targetsIncapacitated++;
                System.out.println(">>> " + strayTarget.character.getDisplayName() + " is incapacitated by stray shot!");
                strayTarget.character.baseMovementSpeed = 0;
                eventQueue.removeIf(e -> e.getOwnerId() == strayTarget.getId());
            }
            
            // Apply hit highlight to stray target
            applyHitHighlight(strayTarget, impactTick);
        } else {
            System.out.println(">>> Stray shot missed " + strayTarget.character.getDisplayName() + " (roll: " + String.format("%.1f", roll) + " vs " + String.format("%.1f", finalChance) + ")");
        }
    }
    
    public void applyHitHighlight(Unit target, long impactTick) {
        if (!target.isHitHighlighted) {
            target.isHitHighlighted = true;
            target.color = platform.api.Color.fromJavaFX(Color.YELLOW);
            eventQueue.add(new ScheduledEvent(impactTick + 15, () -> {
                target.color = target.baseColor;
                target.isHitHighlighted = false;
            }, ScheduledEvent.WORLD_OWNER));
        }
    }
    
    private void triggerAllyBraveryChecks(Unit hitTarget, long impactTick, String projectileName) {
        double allyCheckRange = 210.0; // 30 feet in pixels (30 * 7)
        
        for (Unit unit : units) {
            // Skip the hit target itself
            if (unit == hitTarget) {
                continue;
            }
            
            // Skip incapacitated units
            if (unit.character.isIncapacitated()) {
                continue;
            }
            
            // Only check allies (same faction)
            if (unit.character.getFaction() != hitTarget.character.getFaction()) {
                continue;
            }
            
            // Calculate distance to hit target
            double dx = unit.x - hitTarget.x;
            double dy = unit.y - hitTarget.y;
            double distance = Math.hypot(dx, dy);
            
            // Check if within 30 feet
            if (distance <= allyCheckRange) {
                unit.character.performBraveryCheck(impactTick, eventQueue, unit.getId(), 
                    "ally " + hitTarget.character.getDisplayName() + " hit by " + projectileName);
            }
        }
    }
    
    // findWeaponId() method removed in DevCycle 17 - replaced with direct weapon.getWeaponId() access
    
    /**
     * Resolve melee combat attack between attacker and target
     */
    public void resolveMeleeAttack(Unit attacker, Unit target, MeleeWeapon weapon, long attackTick) {
        if (debugMode) {
            System.out.println(">>> Resolving melee attack: " + attacker.character.getDisplayName() + " attacks " + target.character.getDisplayName() + " with " + weapon.getName());
        }
        
        // Track attempted attack (both legacy and separate tracking)
        attacker.character.attacksAttempted++;
        attacker.character.meleeAttacksAttempted++;
        
        // Calculate hit probability
        boolean hits = calculateMeleeHit(attacker, target, weapon);
        
        if (hits) {
            if (debugMode) {
                System.out.println("=== MELEE DAMAGE CALCULATION DEBUG ===");
            }
            
            // Determine hit location first (needed for wound severity calculation)
            BodyPart hitLocation = determineHitLocation();
            
            // Determine wound severity (like ranged combat does)
            double hitQuality = Math.random() * 100; // Random roll for wound severity
            double chanceToHit = 100.0; // Assume 100% since we already hit
            WoundSeverity woundSeverity = CombatCalculator.determineWoundSeverity(hitQuality, chanceToHit, hitLocation);
            
            // Calculate base weapon damage with wound severity scaling (like ranged combat)
            int weaponDamage = weapon.getDamage();
            int scaledDamage = CombatCalculator.calculateActualDamage(weaponDamage, woundSeverity, hitLocation);
            
            // Add strength damage bonus to the scaled damage (applied after wound severity)
            int strengthBonus = GameConstants.getStrengthDamageBonus(attacker.character.strength);
            int actualDamage = Math.max(1, scaledDamage + strengthBonus);
            
            if (debugMode) {
                System.out.println("Base Weapon Damage: " + weaponDamage);
                System.out.println("Hit Location: " + hitLocation.name().toLowerCase());
                System.out.println("Wound Severity: " + woundSeverity.name().toLowerCase());
                System.out.println("Scaled Damage (after wound severity): " + scaledDamage);
                System.out.println("Attacker Strength: " + attacker.character.strength + " (bonus: " + strengthBonus + ")");
                System.out.println("Final Damage: " + scaledDamage + " + " + strengthBonus + " = " + actualDamage);
            }
            
            // Create hit result
            HitResult hitResult = new HitResult(true, hitLocation, woundSeverity, actualDamage);
            
            // Apply damage and wound
            resolveCombatImpact(attacker, target, weapon, attackTick, hitResult);
            
            if (debugMode) {
                System.out.println(">>> Melee hit! " + weapon.getName() + " deals " + actualDamage + " damage to " + hitLocation.name().toLowerCase());
                System.out.println("========================================");
            }
        } else {
            if (debugMode) {
                System.out.println(">>> Melee attack missed!");
            }
        }
    }
    
    /**
     * Calculate if melee attack hits based on attacker skill and target defense
     */
    private boolean calculateMeleeHit(Unit attacker, Unit target, MeleeWeapon weapon) {
        if (debugMode) {
            System.out.println("=== MELEE HIT CALCULATION DEBUG ===");
            System.out.println("Attacker: " + attacker.character.getDisplayName() + " -> Target: " + target.character.getDisplayName());
            System.out.println("Weapon: " + weapon.getName() + " (accuracy: " + weapon.getWeaponAccuracy() + ")");
        }
        
        // Base hit chance calculation
        int attackerDexterity = GameConstants.statToModifier(attacker.character.dexterity);
        int weaponAccuracy = weapon.getWeaponAccuracy();
        
        // Get weapon skill bonus for melee weapons (DevCycle 17)
        int skillBonus = calculateMeleeSkillBonus(attacker, weapon);
        
        // Movement penalty for attacker
        int movementPenalty = getMovementPenalty(attacker.character);
        
        // Apply first attack penalty if applicable (not when using very careful aiming)
        int firstAttackPenalty = (attacker.character.isFirstAttackOnTarget && !attacker.character.getCurrentAimingSpeed().isVeryCareful()) ? GameConstants.FIRST_ATTACK_PENALTY : 0;
        
        // Calculate total attack modifier
        int attackModifier = attackerDexterity + weaponAccuracy + skillBonus - movementPenalty + firstAttackPenalty;
        
        // Target defense (simplified - no active defense in basic implementation)
        int targetDefense = GameConstants.statToModifier(target.character.dexterity);
        
        // Base hit chance (60%) + modifiers
        int hitChance = 60 + attackModifier - targetDefense;
        
        if (debugMode) {
            System.out.println("Attacker Dexterity: " + attacker.character.dexterity + " (modifier: " + attackerDexterity + ")");
            System.out.println("Weapon Accuracy: " + weaponAccuracy);
            System.out.println("Skill Bonus: " + skillBonus + " " + getMeleeSkillDebugInfo(attacker, weapon));
            System.out.println("Movement Penalty: " + movementPenalty);
            System.out.println("First Attack Penalty: " + firstAttackPenalty + " (first attack: " + attacker.character.isFirstAttackOnTarget + ", very careful: " + attacker.character.getCurrentAimingSpeed().isVeryCareful() + ")");
            System.out.println("Attack Modifier: " + attackModifier);
            System.out.println("Target Defense (Dex): " + target.character.dexterity + " (modifier: " + targetDefense + ")");
            System.out.println("Base Hit Chance: 60% + " + attackModifier + " - " + targetDefense + " = " + hitChance + "%");
        }
        
        // Clamp to reasonable range (5-95%)
        hitChance = Math.max(5, Math.min(95, hitChance));
        
        // Roll for hit
        int roll = (int)(Math.random() * 100) + 1;
        boolean hits = roll <= hitChance;
        
        if (debugMode) {
            System.out.println("Final Hit Chance: " + hitChance + "% (clamped 5-95%)");
            System.out.println("[MELEE-COMBAT] Random roll: " + roll + " (need <= " + hitChance + ")");
            System.out.println("[MELEE-COMBAT] Result: " + (hits ? "HIT!" : "MISS"));
            System.out.println("===================================");
        }
        
        return hits;
    }
    
    
    /**
     * Calculate skill bonus for melee weapons (DevCycle 17)
     */
    private int calculateMeleeSkillBonus(Unit attacker, MeleeWeapon weapon) {
        if (weapon.getWeaponId() == null) {
            return 0; // No skill bonus if weapon has no ID
        }
        
        // Get combat skill from weapon data
        data.DataManager dataManager = data.DataManager.getInstance();
        data.MeleeWeaponData weaponData = dataManager.getMeleeWeapon(weapon.getWeaponId());
        
        if (weaponData == null || weaponData.combatSkill == null || weaponData.combatSkill.isEmpty()) {
            return 0; // No skill specified for this weapon
        }
        
        // Get character's skill level for this weapon's skill
        int skillLevel = attacker.character.getSkillLevel(weaponData.combatSkill);
        
        if (skillLevel <= 0) {
            return 0; // Character doesn't have this skill
        }
        
        // Calculate skill bonus - same pattern as ranged weapons (5 points per skill level)
        return skillLevel * 5;
    }
    
    /**
     * Get debug information for melee skill bonuses (DevCycle 17)
     */
    private String getMeleeSkillDebugInfo(Unit attacker, MeleeWeapon weapon) {
        if (weapon.getWeaponId() == null) {
            return "(no weapon ID)";
        }
        
        data.DataManager dataManager = data.DataManager.getInstance();
        data.MeleeWeaponData weaponData = dataManager.getMeleeWeapon(weapon.getWeaponId());
        
        if (weaponData == null) {
            return "(weapon data not found)";
        }
        
        if (weaponData.combatSkill == null || weaponData.combatSkill.isEmpty()) {
            return "(no combat skill specified)";
        }
        
        int skillLevel = attacker.character.getSkillLevel(weaponData.combatSkill);
        return "(" + weaponData.combatSkill + ": " + skillLevel + ")";
    }
    
    /**
     * Get movement penalty based on character's current movement
     */
    private int getMovementPenalty(combat.Character character) {
        if (character.isIncapacitated()) {
            return 0; // Incapacitated is considered stationary
        }
        
        switch (character.currentMovementType) {
            case CRAWL:
                return 10;
            case WALK:
                return 5;
            case JOG:
                return 15;
            case RUN:
                return 25;
            default:
                return 0; // Stationary
        }
    }
    
    /**
     * Determine hit location for melee attacks (simplified)
     */
    private BodyPart determineHitLocation() {
        int roll = (int)(Math.random() * 100) + 1;
        
        if (roll <= 10) return BodyPart.HEAD;
        if (roll <= 25) return BodyPart.LEFT_ARM;
        if (roll <= 40) return BodyPart.RIGHT_ARM;
        if (roll <= 70) return BodyPart.CHEST;
        if (roll <= 85) return BodyPart.LEFT_LEG;
        return BodyPart.RIGHT_LEG;
    }
    
    /**
     * Check if target is within melee range of attacker using edge-to-edge distance
     */
    public boolean isInMeleeRange(Unit attacker, Unit target, MeleeWeapon weapon) {
        double centerToCenter = Math.hypot(target.x - attacker.x, target.y - attacker.y);
        // Convert to edge-to-edge by subtracting target radius (1.5 feet = 10.5 pixels)
        double edgeToEdge = centerToCenter - (1.5 * 7.0);
        double pixelRange = weapon.getTotalReach() * 7.0; // Convert feet to pixels (7 pixels = 1 foot)
        
        if (debugMode) {
            System.out.println("[MELEE-RANGE] Center-to-center: " + String.format("%.1f", centerToCenter) + " pixels (" + String.format("%.2f", centerToCenter/7.0) + " feet)");
            System.out.println("[MELEE-RANGE] Edge-to-edge: " + String.format("%.1f", edgeToEdge) + " pixels (" + String.format("%.2f", edgeToEdge/7.0) + " feet)");
            System.out.println("[MELEE-RANGE] Weapon reach: " + String.format("%.2f", weapon.getTotalReach()) + " feet (" + String.format("%.1f", pixelRange) + " pixels)");
            System.out.println("[MELEE-RANGE] In range result: " + (edgeToEdge <= pixelRange) + " (edge " + String.format("%.1f", edgeToEdge) + " <= range " + String.format("%.1f", pixelRange) + ")");
        }
        
        return edgeToEdge <= pixelRange;
    }
}
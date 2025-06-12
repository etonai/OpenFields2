import combat.*;
import game.Unit;
import game.ScheduledEvent;
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
            
            System.out.println(">>> " + weapon.getProjectileName() + " hit " + target.character.getDisplayName() + " in the " + hitLocation.name().toLowerCase() + " causing a " + woundSeverity.name().toLowerCase() + " wound at tick " + impactTick);
            target.character.health -= actualDamage;
            System.out.println(">>> " + target.character.getDisplayName() + " takes " + actualDamage + " damage. Health now: " + target.character.health);
            
            // Track successful attack
            shooter.character.attacksAttempted++;
            shooter.character.attacksSuccessful++;
            
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
            String weaponId = findWeaponId(weapon);
            target.character.addWound(new Wound(hitLocation, woundSeverity, weapon.getProjectileName(), weaponId, actualDamage), impactTick, eventQueue, target.getId());
            
            // Trigger bravery check for the target when wounded
            target.character.performBraveryCheck(impactTick, eventQueue, target.getId(), "wounded by " + weapon.getProjectileName());
            
            // Trigger bravery checks for allies within 30 feet
            triggerAllyBraveryChecks(target, impactTick, weapon.getProjectileName());
            
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
            System.out.println(">>> " + weapon.getProjectileName() + " missed " + target.character.getDisplayName() + " at tick " + impactTick);
            
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
        List<Unit> potentialTargets = findPotentialStrayTargets(shooter, target, missX, missY, weapon.maximumRange);
        
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
        System.out.println(">>> STRAY SHOT! " + weapon.getProjectileName() + " hits " + strayTarget.character.getDisplayName() + " (position: " + strayTarget.character.getCurrentPosition().getDisplayName() + ")");
        
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
            
            // Apply damage
            strayTarget.character.health -= strayDamage;
            System.out.println(">>> " + strayTarget.character.getDisplayName() + " takes " + strayDamage + " stray damage. Health now: " + strayTarget.character.health);
            
            // Track successful attack for shooter (stray hits still count)
            shooter.character.attacksSuccessful++;
            
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
            String weaponId = findWeaponId(weapon);
            strayTarget.character.addWound(new Wound(hitLocation, woundSeverity, weapon.getProjectileName() + " (stray)", weaponId, strayDamage), impactTick, eventQueue, strayTarget.getId());
            
            // Trigger bravery check for stray shot victim
            strayTarget.character.performBraveryCheck(impactTick, eventQueue, strayTarget.getId(), "hit by stray " + weapon.getProjectileName());
            
            // Trigger bravery checks for allies within 30 feet
            triggerAllyBraveryChecks(strayTarget, impactTick, weapon.getProjectileName() + " (stray)");
            
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
            target.color = Color.YELLOW;
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
    
    private String findWeaponId(Weapon weapon) {
        // This is a simple approach - in a more complex system, 
        // we might want to store the weapon ID in the weapon object
        if (weapon.name.equals("Colt Peacemaker")) return "wpn_colt_peacemaker";
        if (weapon.name.equals("Hunting Rifle")) return "wpn_hunting_rifle";
        if (weapon.name.equals("Derringer")) return "wpn_derringer";
        if (weapon.name.equals("Plasma Pistol")) return "wpn_plasma_pistol";
        if (weapon.name.equals("Magic Wand")) return "wpn_magic_wand";
        if (weapon.name.equals("Sheathed Sword")) return "wpn_sheathed_sword";
        if (weapon.name.equals("Brown Bess")) return "wpn_brown_bess";
        if (weapon.name.equals("Lee Enfield")) return "wpn_lee_enfield";
        if (weapon.name.equals("M1 Garand")) return "wpn_m1_garand";
        if (weapon.name.equals("English Longbow")) return "wpn_english_longbow";
        if (weapon.name.equals("Heavy Crossbow")) return "wpn_heavy_crossbow";
        if (weapon.name.equals("Steel Dagger")) return "wpn_steel_dagger";
        if (weapon.name.equals("Longsword")) return "wpn_longsword";
        if (weapon.name.equals("Battle Axe")) return "wpn_battle_axe";
        if (weapon.name.equals("Uzi")) return "wpn_uzi";
        return "wpn_colt_peacemaker"; // default fallback
    }
}
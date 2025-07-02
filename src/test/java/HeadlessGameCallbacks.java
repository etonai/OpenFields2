import game.GameCallbacks;
import game.Unit;
import game.ScheduledEvent;
import combat.Weapon;
import combat.MeleeWeapon;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Headless implementation of GameCallbacks for testing - System 5 of DevCycle 36.
 * 
 * Provides null implementations for all UI-related callbacks while maintaining
 * the essential game logic functionality needed for combat simulation.
 * 
 * @author DevCycle 36 - System 5: Complete Headless GunfightTestAutomated
 */
public class HeadlessGameCallbacks implements GameCallbacks {
    
    private final HeadlessGameState gameState;
    
    public HeadlessGameCallbacks(HeadlessGameState gameState) {
        this.gameState = gameState;
    }
    
    @Override
    public void playWeaponSound(Weapon weapon) {
        // No audio in headless mode - silent operation
        System.out.println("  [Audio] Playing weapon sound: " + weapon.name);
    }
    
    @Override
    public void scheduleProjectileImpact(Unit shooter, Unit target, Weapon weapon, long fireTick, double distanceFeet) {
        // Schedule the projectile impact event in our headless game state
        System.out.println("  [Combat] Scheduling projectile impact from " + 
                          shooter.getCharacter().getName() + " to " + 
                          target.getCharacter().getName() + " at tick " + fireTick);
        
        // Calculate impact tick based on distance and weapon velocity
        long impactTick = fireTick;
        if (weapon instanceof combat.RangedWeapon) {
            combat.RangedWeapon rangedWeapon = (combat.RangedWeapon) weapon;
            double travelTime = distanceFeet / rangedWeapon.velocityFeetPerSecond * 60; // Convert to ticks
            impactTick = fireTick + Math.round(travelTime);
        }
        
        // Create and schedule the impact event
        ScheduledEvent impactEvent = new ScheduledEvent(impactTick, () -> {
            handleProjectileImpact(shooter, target, weapon);
        }, shooter.getId());
        
        gameState.scheduleEvent(impactEvent);
    }
    
    @Override
    public void scheduleMeleeImpact(Unit attacker, Unit target, MeleeWeapon weapon, long attackTick) {
        // Schedule melee impact event
        System.out.println("  [Combat] Scheduling melee impact from " + 
                          attacker.getCharacter().getName() + " to " + 
                          target.getCharacter().getName() + " at tick " + attackTick);
        
        ScheduledEvent meleeEvent = new ScheduledEvent(attackTick, () -> {
            handleMeleeImpact(attacker, target, weapon);
        }, attacker.getId());
        
        gameState.scheduleEvent(meleeEvent);
    }
    
    @Override
    public void applyFiringHighlight(Unit shooter, long fireTick) {
        // No visual effects in headless mode
        System.out.println("  [Visual] Firing highlight for " + shooter.getCharacter().getName());
    }
    
    @Override
    public void addMuzzleFlash(Unit shooter, long fireTick) {
        // No visual effects in headless mode
        System.out.println("  [Visual] Muzzle flash for " + shooter.getCharacter().getName());
    }
    
    @Override
    public void removeAllEventsForOwner(int ownerId) {
        // This would remove events from the event queue for a specific owner
        // For now, we'll just log it - this is mainly used for cleanup
        System.out.println("  [Cleanup] Removing events for owner " + ownerId);
    }
    
    @Override
    public List<Unit> getUnits() {
        return gameState.getUnits();
    }
    
    @Override
    public PriorityQueue<ScheduledEvent> getEventQueue() {
        // Return the actual event queue from our game state
        // We need to expose the internal queue for the CombatCoordinator
        return gameState.getInternalEventQueue();
    }
    
    /**
     * Handle projectile impact and damage calculation
     */
    private void handleProjectileImpact(Unit shooter, Unit target, Weapon weapon) {
        System.out.println("  [Impact] Projectile hit: " + shooter.getCharacter().getName() + 
                          " → " + target.getCharacter().getName());
        
        // Record the hit for statistics
        gameState.recordHit();
        
        // Apply damage to target
        int damage = weapon.damage;
        combat.Character targetCharacter = target.getCharacter();
        int oldHealth = targetCharacter.getHealth();
        
        targetCharacter.setHealth(Math.max(0, oldHealth - damage));
        int actualDamage = oldHealth - targetCharacter.getHealth();
        
        if (actualDamage > 0) {
            gameState.recordWound(actualDamage);
            System.out.println("    Damage dealt: " + actualDamage + " (Health: " + 
                             oldHealth + " → " + targetCharacter.getHealth() + ")");
            
            if (targetCharacter.getHealth() <= 0) {
                System.out.println("    " + targetCharacter.getName() + " incapacitated!");
                gameState.recordIncapacitation();
            }
        }
    }
    
    /**
     * Handle melee impact and damage calculation
     */
    private void handleMeleeImpact(Unit attacker, Unit target, MeleeWeapon weapon) {
        System.out.println("  [Impact] Melee hit: " + attacker.getCharacter().getName() + 
                          " → " + target.getCharacter().getName());
        
        // Similar to projectile impact but for melee
        gameState.recordHit();
        
        int damage = weapon.damage;
        combat.Character targetCharacter = target.getCharacter();
        int oldHealth = targetCharacter.getHealth();
        
        targetCharacter.setHealth(Math.max(0, oldHealth - damage));
        int actualDamage = oldHealth - targetCharacter.getHealth();
        
        if (actualDamage > 0) {
            gameState.recordWound(actualDamage);
            System.out.println("    Melee damage dealt: " + actualDamage + " (Health: " + 
                             oldHealth + " → " + targetCharacter.getHealth() + ")");
            
            if (targetCharacter.getHealth() <= 0) {
                System.out.println("    " + targetCharacter.getName() + " incapacitated!");
                gameState.recordIncapacitation();
            }
        }
    }
}
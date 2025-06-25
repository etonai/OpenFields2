import combat.*;
import game.*;
import platform.PlatformInitializer;
import platform.api.Platform;
import platform.api.PlatformFactory;
import platform.api.Color;
import utils.GameConstants;
import data.DataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Standalone simulation to test Alice vs Drake melee combat bugs
 * This reproduces the scenarios from testoutput.txt to investigate:
 * - Bug #1: Rapid consecutive attacks
 * - Bug #2: Defense system not triggering for Alice
 */
public class AliceDrakeCombatSimulation {
    
    public static void main(String[] args) {
        System.out.println("=== ALICE vs DRAKE MELEE COMBAT SIMULATION ===");
        
        try {
            // Initialize platform for console mode
            PlatformInitializer.registerPlatforms();
            Platform platform = PlatformFactory.createPlatform("console");
            if (platform == null) {
                System.err.println("Failed to create console platform");
                return;
            }
            
            if (!platform.initialize(80, 24, "Combat Simulation")) {
                System.err.println("Failed to initialize platform");
                return;
            }
            
            // Initialize data manager
            DataManager dataManager = DataManager.getInstance();
            // Note: loadAllData() is private, so we'll work with defaults
            
            // Set up simulation
            AliceDrakeCombatSimulation sim = new AliceDrakeCombatSimulation();
            sim.runSimulation();
            
            platform.shutdown();
            
        } catch (Exception e) {
            System.err.println("Simulation failed:");
            e.printStackTrace();
        }
    }
    
    public void runSimulation() {
        // Create game systems
        List<Unit> units = new ArrayList<>();
        PriorityQueue<ScheduledEvent> eventQueue = new PriorityQueue<>();
        CombatResolver combatResolver = new CombatResolver(units, eventQueue, true); // Debug mode ON
        GameClock gameClock = new GameClock();
        long currentTick = 200; // Start at tick 200
        
        // Create Alice (Union faction, low health like in test)
        combat.Character aliceChar = new combat.Character("Alice", 75, 60, 55, 65, 70, Handedness.RIGHT_HANDED);
        aliceChar.id = 1000;
        aliceChar.setFaction(1); // Union
        
        // Create Drake (Confederate faction)  
        combat.Character drakeChar = new combat.Character("Drake", 50, 80, 50, 55, 60, Handedness.RIGHT_HANDED);
        drakeChar.id = 1003;
        drakeChar.setFaction(2); // Confederacy
        
        // Create units at close positions (6 feet apart for melee range)
        Unit alice = new Unit(aliceChar, 100, 100, Color.BLUE, 1000);
        Unit drake = new Unit(drakeChar, 142, 100, Color.RED, 1003); // 42 pixels = 6 feet apart
        
        units.add(alice);
        units.add(drake);
        
        // Set up melee weapons similar to test output
        setupMeleeWeapons(alice, drake);
        
        System.out.println("\n=== INITIAL SETUP ===");
        System.out.println("Alice: Health=" + alice.character.currentHealth + "/" + alice.character.health + 
                          ", Dex=" + alice.character.dexterity + ", Faction=" + alice.character.getFaction());
        System.out.println("Drake: Health=" + drake.character.currentHealth + "/" + drake.character.health + 
                          ", Dex=" + drake.character.dexterity + ", Faction=" + drake.character.getFaction());
        System.out.println("Distance: " + String.format("%.2f", Math.hypot(drake.x - alice.x, drake.y - alice.y) / 7.0) + " feet");
        
        if (alice.character.meleeWeapon != null) {
            System.out.println("Alice weapon: " + alice.character.meleeWeapon.getName() + 
                             " (defend score: " + alice.character.meleeWeapon.getDefendScore() + ")");
        }
        if (drake.character.meleeWeapon != null) {
            System.out.println("Drake weapon: " + drake.character.meleeWeapon.getName() + 
                             " (damage: " + drake.character.meleeWeapon.getDamage() + ")");
        }
        
        // Test defense system setup
        System.out.println("\n=== DEFENSE SYSTEM CHECK ===");
        System.out.println("Alice defense state: " + alice.character.getDefenseState());
        System.out.println("Drake defense state: " + drake.character.getDefenseState());
        System.out.println("Alice can defend: " + alice.character.canDefend(currentTick));
        System.out.println("Drake can defend: " + drake.character.canDefend(currentTick));
        
        // Simulate the bug scenario: Drake attacks Alice rapidly
        System.out.println("\n=== BUG REPRODUCTION: RAPID ATTACKS ===");
        System.out.println("Simulating Drake attacking Alice at consecutive ticks (like in testoutput.txt)");
        
        // Attack 1 at tick 211 (like in original)
        long attackTick1 = currentTick + 11;
        System.out.println("\n--- Drake Attack #1 at tick " + attackTick1 + " ---");
        combatResolver.resolveMeleeAttack(drake, alice, drake.character.meleeWeapon, attackTick1);
        System.out.println("Alice health after attack 1: " + alice.character.currentHealth + "/" + alice.character.health);
        
        // Attack 2 at tick 212 (BUG: should not be possible so quickly)
        long attackTick2 = attackTick1 + 1;
        System.out.println("\n--- Drake Attack #2 at tick " + attackTick2 + " (BUG: too quick!) ---");
        combatResolver.resolveMeleeAttack(drake, alice, drake.character.meleeWeapon, attackTick2);
        System.out.println("Alice health after attack 2: " + alice.character.currentHealth + "/" + alice.character.health);
        System.out.println("Alice incapacitated: " + alice.character.isIncapacitated());
        
        // Test reverse scenario: Alice attacks Drake
        if (!alice.character.isIncapacitated()) {
            System.out.println("\n=== REVERSE TEST: ALICE ATTACKS DRAKE ===");
            long aliceAttackTick = attackTick2 + 50; // Give some time
            System.out.println("--- Alice attacks Drake at tick " + aliceAttackTick + " ---");
            combatResolver.resolveMeleeAttack(alice, drake, alice.character.meleeWeapon, aliceAttackTick);
            System.out.println("Drake health after Alice's attack: " + drake.character.currentHealth + "/" + drake.character.health);
        }
        
        // Show defense statistics
        System.out.println("\n=== FINAL DEFENSE STATISTICS ===");
        System.out.println("Alice defensive attempts: " + alice.character.defensiveAttempts);
        System.out.println("Alice defensive successes: " + alice.character.defensiveSuccesses);
        System.out.println("Drake defensive attempts: " + drake.character.defensiveAttempts);
        System.out.println("Drake defensive successes: " + drake.character.defensiveSuccesses);
        
        // Analysis
        System.out.println("\n=== BUG ANALYSIS ===");
        if (alice.character.defensiveAttempts == 0) {
            System.out.println("❌ BUG CONFIRMED: Alice never attempted to defend (Bug #2)");
        } else {
            System.out.println("✅ Defense system worked for Alice");
        }
        
        if (alice.character.isIncapacitated()) {
            System.out.println("❌ Alice was incapacitated by rapid attacks (possible Bug #1)");
        } else {
            System.out.println("✅ Alice survived the attack sequence");
        }
        
        System.out.println("\n=== SIMULATION COMPLETE ===");
    }
    
    private void setupMeleeWeapons(Unit alice, Unit drake) {
        // Create simple melee weapons for testing
        // Alice gets a Steel Dagger (like in combat logs)
        MeleeWeapon aliceDagger = createTestDagger();
        alice.character.meleeWeapon = aliceDagger;
        alice.character.currentWeaponState = aliceDagger.getInitialState();
        
        // Drake gets an Enchanted Sword (like in combat logs) 
        MeleeWeapon drakeEnchantedSword = createTestEnchantedSword();
        drake.character.meleeWeapon = drakeEnchantedSword;
        drake.character.currentWeaponState = drakeEnchantedSword.getInitialState();
    }
    
    private MeleeWeapon createTestDagger() {
        // Simplified weapon creation matching the test data
        return new MeleeWeapon("Steel Dagger", "A sharp steel blade", 25, "/dagger.wav", 
                              MeleeWeaponType.SHORT, 5, 45, 65, 5.5, 60, true, false, 60);
    }
    
    private MeleeWeapon createTestEnchantedSword() {
        // High damage weapon like in the test output
        return new MeleeWeapon("Enchanted Sword", "A magical sword", 75, "/magic.wav",
                              MeleeWeaponType.MEDIUM, 6, 25, 30, 7.5, 45, true, false, 60);
    }
}
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test simulation of Alice vs Drake melee combat to investigate defense system bugs
 */
public class AliceDrakeCombatTest {
    
    private Platform platform;
    private List<Unit> units;
    private PriorityQueue<ScheduledEvent> eventQueue;
    private CombatResolver combatResolver;
    private GameClock gameClock;
    private Unit alice;
    private Unit drake;
    
    @BeforeEach
    public void setUp() {
        // Initialize platform for console mode
        PlatformInitializer.registerPlatforms();
        platform = PlatformFactory.createPlatform("console");
        assertNotNull(platform, "Console platform should be available");
        assertTrue(platform.initialize(80, 24, "Combat Test"), "Platform should initialize");
        
        // Initialize data manager
        DataManager dataManager = DataManager.getInstance();
        dataManager.loadAllData();
        
        // Set up game systems
        units = new ArrayList<>();
        eventQueue = new PriorityQueue<>();
        combatResolver = new CombatResolver(units, eventQueue, true); // Enable debug mode
        gameClock = new GameClock();
        gameClock.setCurrentTick(100); // Start at tick 100
        
        // Create Alice (Union faction, low health like in test)
        Character aliceChar = new Character(1000, "Alice", "Alice", "Smith", 
                                          "1995-01-01", "test_theme", 75, 60, 55, 65, 70, Handedness.RIGHT_HANDED);
        aliceChar.setFaction(1); // Union
        
        // Create Drake (Confederate faction)  
        Character drakeChar = new Character(1003, "Drake", "Drake", "Johnson",
                                          "1995-01-01", "test_theme", 50, 80, 50, 55, 60, Handedness.RIGHT_HANDED);
        drakeChar.setFaction(2); // Confederacy
        
        // Create units at close positions (6 feet apart for melee range)
        alice = new Unit(aliceChar, 100, 100, Color.BLUE, 1000);
        drake = new Unit(drakeChar, 142, 100, Color.RED, 1003); // 42 pixels = 6 feet apart
        
        units.add(alice);
        units.add(drake);
        
        // Set both to melee mode with weapons
        setupMeleeWeapons();
        
        System.out.println("=== COMBAT TEST SETUP ===");
        System.out.println("Alice: Health=" + alice.character.health + "/" + alice.character.currentHealth + 
                          ", Dex=" + alice.character.dexterity + ", Faction=" + alice.character.getFaction());
        System.out.println("Drake: Health=" + drake.character.health + "/" + drake.character.currentHealth + 
                          ", Dex=" + drake.character.dexterity + ", Faction=" + drake.character.getFaction());
        System.out.println("Distance: " + Math.hypot(drake.x - alice.x, drake.y - alice.y) / 7.0 + " feet");
        System.out.println("========================");
    }
    
    private void setupMeleeWeapons() {
        // Give Alice a Steel Dagger
        MeleeWeapon aliceDagger = createTestDagger();
        alice.character.meleeWeapon = aliceDagger;
        alice.character.currentWeaponState = aliceDagger.getInitialState();
        alice.character.currentCombatMode = CombatMode.MELEE;
        
        // Give Drake an Enchanted Sword (like in the test output)
        MeleeWeapon drakeEnchantedSword = createTestEnchantedSword();
        drake.character.meleeWeapon = drakeEnchantedSword;
        drake.character.currentWeaponState = drakeEnchantedSword.getInitialState();
        drake.character.currentCombatMode = CombatMode.MELEE;
        
        System.out.println("Alice weapon: " + aliceDagger.getName() + " (defend score: " + aliceDagger.getDefendScore() + ")");
        System.out.println("Drake weapon: " + drakeEnchantedSword.getName() + " (damage: " + drakeEnchantedSword.getDamage() + ")");
    }
    
    private MeleeWeapon createTestDagger() {
        MeleeWeapon dagger = new MeleeWeapon("Steel Dagger", 25, 5.5, "/dagger.wav", 
                                           WeaponType.MELEE_SHORT, 1.0, 45, 65);
        dagger.states = createMeleeStates();
        dagger.initialStateName = "sheathed";
        return dagger;
    }
    
    private MeleeWeapon createTestEnchantedSword() {
        MeleeWeapon sword = new MeleeWeapon("Enchanted Sword", 75, 7.5, "/magic.wav",
                                          WeaponType.MELEE_MEDIUM, 6.5, 25, 30);
        sword.states = createMeleeStates();
        sword.initialStateName = "sheathed";
        return sword;
    }
    
    private List<WeaponState> createMeleeStates() {
        List<WeaponState> states = new ArrayList<>();
        states.add(new WeaponState("sheathed", "unsheathing", 0));
        states.add(new WeaponState("unsheathing", "melee_ready", 60));
        states.add(new WeaponState("melee_ready", "melee_attacking", 15));
        states.add(new WeaponState("melee_attacking", "melee_ready", 120));
        states.add(new WeaponState("switching_to_melee", "melee_ready", 30));
        states.add(new WeaponState("switching_to_ranged", "ready", 30));
        return states;
    }
    
    @Test
    public void testAliceDrakeMeleeCombat() {
        System.out.println("\n=== STARTING ALICE vs DRAKE MELEE COMBAT TEST ===");
        
        // Set both to auto-targeting mode
        alice.character.usesAutomaticTargeting = true;
        drake.character.usesAutomaticTargeting = true;
        
        // Manual targeting for controlled test
        alice.character.currentTarget = drake;
        drake.character.currentTarget = alice;
        
        // Verify initial defense states
        System.out.println("Alice defense state: " + alice.character.getDefenseState());
        System.out.println("Drake defense state: " + drake.character.getDefenseState());
        
        // Test Alice's ability to defend
        boolean aliceCanDefend = alice.character.canDefend(gameClock.getCurrentTick());
        System.out.println("Alice can defend: " + aliceCanDefend);
        
        // Test Drake's ability to defend  
        boolean drakeCanDefend = drake.character.canDefend(gameClock.getCurrentTick());
        System.out.println("Drake can defend: " + drakeCanDefend);
        
        // Simulate Drake attacking Alice (like in the bug report)
        System.out.println("\n=== SIMULATING DRAKE ATTACKS ALICE ===");
        long attackTick = gameClock.getCurrentTick() + 10;
        
        // First attack
        System.out.println("\n--- Attack 1 at tick " + attackTick + " ---");
        combatResolver.resolveMeleeAttack(drake, alice, drake.character.meleeWeapon, attackTick);
        
        // Check Alice's health after first attack
        System.out.println("Alice health after attack 1: " + alice.character.currentHealth + "/" + alice.character.health);
        
        // Second attack (should NOT happen immediately)
        System.out.println("\n--- Attack 2 at tick " + (attackTick + 1) + " ---");
        combatResolver.resolveMeleeAttack(drake, alice, drake.character.meleeWeapon, attackTick + 1);
        
        // Check Alice's health after second attack
        System.out.println("Alice health after attack 2: " + alice.character.currentHealth + "/" + alice.character.health);
        System.out.println("Alice incapacitated: " + alice.character.isIncapacitated());
        
        // Now test Alice attacking Drake to see if defense works properly
        System.out.println("\n=== SIMULATING ALICE ATTACKS DRAKE ===");
        long aliceAttackTick = attackTick + 100; // Give some time
        
        System.out.println("\n--- Alice attacks Drake at tick " + aliceAttackTick + " ---");
        combatResolver.resolveMeleeAttack(alice, drake, alice.character.meleeWeapon, aliceAttackTick);
        
        System.out.println("Drake health after Alice's attack: " + drake.character.currentHealth + "/" + drake.character.health);
        
        // Verify defense statistics were updated
        System.out.println("\n=== DEFENSE STATISTICS ===");
        System.out.println("Alice defensive attempts: " + alice.character.defensiveAttempts);
        System.out.println("Alice defensive successes: " + alice.character.defensiveSuccesses);
        System.out.println("Drake defensive attempts: " + drake.character.defensiveAttempts);
        System.out.println("Drake defensive successes: " + drake.character.defensiveSuccesses);
        
        System.out.println("\n=== COMBAT TEST COMPLETED ===");
    }
    
    @Test 
    public void testDefenseSystemActivation() {
        System.out.println("\n=== TESTING DEFENSE SYSTEM ACTIVATION ===");
        
        // Test that canDefend works properly for both characters
        assertTrue(alice.character.canDefend(100), "Alice should be able to defend initially");
        assertTrue(drake.character.canDefend(100), "Drake should be able to defend initially");
        
        // Test defense state management
        assertEquals(DefenseState.READY, alice.character.getDefenseState(), "Alice should start in READY defense state");
        assertEquals(DefenseState.READY, drake.character.getDefenseState(), "Drake should start in READY defense state");
        
        // Test that characters have weapons for defending
        assertNotNull(alice.character.meleeWeapon, "Alice should have a melee weapon");
        assertNotNull(drake.character.meleeWeapon, "Drake should have a melee weapon");
        
        assertTrue(alice.character.meleeWeapon.getDefendScore() > 0, "Alice's weapon should have a defend score");
        assertTrue(drake.character.meleeWeapon.getDefendScore() > 0, "Drake's weapon should have a defend score");
        
        System.out.println("Defense system activation tests passed");
    }
}
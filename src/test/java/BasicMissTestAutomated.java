import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

import javafx.application.Platform;
import javafx.stage.Stage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

import combat.*;
import data.*;
import game.*;
import utils.GameConfiguration;
import java.security.SecureRandom;

/**
 * Automated test for BasicMissTest scenario as specified in DevCycle 34.
 * Enhanced in DevCycle 41 with deterministic mode and random seed generation.
 * 
 * SEED MANAGEMENT:
 * - Normal Operation: Uses randomly generated seed each run to discover edge cases
 * - Bug Reproduction: Use -Dtest.seed=123456789 to reproduce specific test scenarios
 * - Seed Reporting: Outputs seed at start and completion for easy reproduction
 * 
 * USAGE EXAMPLES:
 * 
 * Basic Usage:
 * mvn test -Dtest=BasicMissTestAutomated                     # Random seed testing
 * mvn test -Dtest=BasicMissTestAutomated -Dtest.seed=54321  # Positive seed reproduction
 * 
 * Cross-Platform Seed Reproduction:
 * 
 * Windows PowerShell (recommended - always quote properties):
 * mvn test "-Dtest=BasicMissTestAutomated" "-Dtest.seed=4292768217366888882"
 * 
 * Windows Command Prompt (standard syntax):
 * mvn test -Dtest=BasicMissTestAutomated -Dtest.seed=4292768217366888882
 * 
 * macOS/Linux (bash/zsh):
 * mvn test -Dtest=BasicMissTestAutomated -Dtest.seed=4292768217366888882
 * 
 * TROUBLESHOOTING:
 * - If you see "Unknown lifecycle phase .seed=" errors, quote the -D properties
 * - Windows PowerShell has parsing issues with -D properties, always use quotes
 * - Use Windows Command Prompt as alternative if PowerShell fails
 * - All seeds (positive and negative) produce deterministic results
 * 
 * Test Sequence:
 * 1. Generate random seed or use manual override from -Dtest.seed property
 * 2. Enable deterministic mode with generated/specified seed
 * 3. Start game and activate debug mode
 * 4. Load test_a.json save file
 * 5. Select MissBot character programmatically
 * 6. Enable auto-targeting mode on MissBot
 * 7. Unpause game and monitor combat execution
 * 8. Verify 6 shots fired at TargetDummy
 * 9. Confirm reload sequence initiation
 * 10. Validate 2nd round loading completion
 * 
 * Success Criteria: All 6 shots fired, reload initiated, 2nd round successfully loaded
 * 
 * @author DevCycle 34 - Automated Testing Foundation
 * @author DevCycle 41 System 8 - Deterministic Mode Standardization
 */
public class BasicMissTestAutomated {
    
    private OpenFields2 gameInstance;
    private GameClock gameClock;
    private List<Unit> units;
    private SaveGameController saveGameController;
    private CountDownLatch gameReadyLatch;
    private CountDownLatch testCompleteLatch;
    
    // Test monitoring variables
    private AtomicInteger shotsFired = new AtomicInteger(0);
    private AtomicBoolean reloadInitiated = new AtomicBoolean(false);
    private AtomicBoolean secondRoundLoaded = new AtomicBoolean(false);
    private AtomicBoolean testFailed = new AtomicBoolean(false);
    private String failureReason = "";
    
    private combat.Character missBot;
    private combat.Character targetDummy;
    private Unit missBotUnit;
    
    // Random seed for deterministic testing with reproducibility
    private long testSeed;
    
    @BeforeEach
    public void setUp() throws Exception {
        System.out.println("=== BasicMissTest Automated Setup ===");
        
        // DevCycle 41: System 8 - Deterministic mode and seed management
        String seedProperty = System.getProperty("test.seed");
        if (seedProperty != null && !seedProperty.isEmpty()) {
            try {
                testSeed = Long.parseLong(seedProperty);
                System.out.println("=== MANUAL SEED OVERRIDE ===");
                System.out.println("Using manual seed: " + testSeed);
                System.out.println("============================");
            } catch (NumberFormatException e) {
                System.out.println("Invalid seed format: " + seedProperty + ", generating random seed");
                testSeed = new SecureRandom().nextLong();
            }
        } else {
            testSeed = new SecureRandom().nextLong();
        }
        
        // Enable deterministic mode
        GameConfiguration.setDeterministicMode(true, testSeed);
        System.out.println("Deterministic mode ENABLED with seed: " + testSeed);
        
        // Initialize JavaFX if not already initialized
        if (!Platform.isFxApplicationThread()) {
            CountDownLatch fxLatch = new CountDownLatch(1);
            Platform.startup(() -> fxLatch.countDown());
            fxLatch.await(5, TimeUnit.SECONDS);
        }
        
        // Reset test monitoring variables
        shotsFired.set(0);
        reloadInitiated.set(false);
        secondRoundLoaded.set(false);
        testFailed.set(false);
        failureReason = "";
        
        gameReadyLatch = new CountDownLatch(1);
        testCompleteLatch = new CountDownLatch(1);
        
        System.out.println("✓ Test setup complete");
    }
    
    @Test
    @Timeout(30)
    public void testBasicMissAutomated() throws Exception {
        System.out.println("Starting BasicMiss automated test...");
        
        // Step 1: Start game and activate debug mode
        startGameAndActivateDebugMode();
        
        // Wait for game to be ready
        assertTrue(gameReadyLatch.await(10, TimeUnit.SECONDS), "Game should start within 10 seconds");
        System.out.println("✓ Game started and debug mode activated");
        
        // Step 2: Load test_a.json save file
        loadTestSave();
        System.out.println("✓ Test save loaded");
        
        // Step 3: Select MissBot character programmatically
        selectMissBot();
        System.out.println("✓ MissBot selected");
        
        // Step 4: Enable auto-targeting mode on MissBot
        enableAutoTargeting();
        System.out.println("✓ Auto-targeting enabled");
        
        // Step 5: Unpause game and monitor combat execution
        unpauseAndMonitorCombat();
        System.out.println("✓ Game unpaused, monitoring combat");
        
        // Wait for test completion or timeout
        assertTrue(testCompleteLatch.await(25, TimeUnit.SECONDS), "Test should complete within 25 seconds");
        
        // Verify success criteria
        assertFalse(testFailed.get(), "Test failed: " + failureReason);
        assertEquals(6, shotsFired.get(), "MissBot should fire exactly 6 shots");
        assertTrue(reloadInitiated.get(), "Reload sequence should be initiated");
        assertTrue(secondRoundLoaded.get(), "Second round should be loaded during reload");
        
        System.out.println("=== BasicMissTest Automated SUCCESS ===");
        System.out.println("Final results: " + shotsFired.get() + " shots fired, reload initiated: " + 
                         reloadInitiated.get() + ", 2nd round loaded: " + secondRoundLoaded.get());
        
        System.out.println("=== TEST COMPLETION SUMMARY ===");
        System.out.println("Test seed used: " + testSeed);
        System.out.println("To reproduce (Windows PowerShell): mvn test \"-Dtest=BasicMissTestAutomated\" \"-Dtest.seed=" + testSeed + "\"");
        System.out.println("To reproduce (CMD/Linux/macOS): mvn test -Dtest=BasicMissTestAutomated -Dtest.seed=" + testSeed);
        System.out.println("===============================");
    }
    
    private void startGameAndActivateDebugMode() throws Exception {
        Platform.runLater(() -> {
            try {
                // Initialize game instance
                gameInstance = new OpenFields2();
                Stage testStage = new Stage();
                gameInstance.start(testStage);
                
                // Get game components through reflection since they're private
                gameClock = (GameClock) getPrivateField(gameInstance, "gameClock");
                units = (List<Unit>) getPrivateField(gameInstance, "units");
                saveGameController = (SaveGameController) getPrivateField(gameInstance, "saveGameController");
                
                // Activate debug mode
                GameRenderer.setDebugMode(true);
                
                gameReadyLatch.countDown();
            } catch (Exception e) {
                testFailed.set(true);
                failureReason = "Failed to start game: " + e.getMessage();
                gameReadyLatch.countDown();
            }
        });
    }
    
    private Object getPrivateField(Object obj, String fieldName) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
    
    private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
    
    private void loadTestSave() throws Exception {
        CountDownLatch loadLatch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Load test_a.json save
                saveGameController.loadGameFromTestSlot('a');
                
                // Wait a moment for load to complete
                Platform.runLater(() -> {
                    try {
                        // Verify characters are loaded
                        missBot = null;
                        targetDummy = null;
                        
                        for (Unit unit : units) {
                            combat.Character character = unit.character;
                            if (character.id == -1001) {
                                missBot = character;
                                missBotUnit = unit;
                            } else if (character.id == -2001) {
                                targetDummy = character;
                            }
                        }
                        
                        if (missBot == null || targetDummy == null) {
                            testFailed.set(true);
                            failureReason = "Failed to find MissBot or TargetDummy after save load";
                        }
                        
                        loadLatch.countDown();
                    } catch (Exception e) {
                        testFailed.set(true);
                        failureReason = "Error verifying loaded characters: " + e.getMessage();
                        loadLatch.countDown();
                    }
                });
            } catch (Exception e) {
                testFailed.set(true);
                failureReason = "Failed to load test save: " + e.getMessage();
                loadLatch.countDown();
            }
        });
        
        assertTrue(loadLatch.await(5, TimeUnit.SECONDS), "Save load should complete within 5 seconds");
    }
    
    private void selectMissBot() throws Exception {
        Platform.runLater(() -> {
            try {
                // Get the selection manager through reflection
                SelectionManager selectionManager = (SelectionManager) getPrivateField(gameInstance, "selectionManager");
                
                // Select MissBot unit
                selectionManager.clearSelection();
                selectionManager.selectUnit(missBotUnit);
                
                // Verify selection
                if (!selectionManager.getSelectedUnits().contains(missBotUnit)) {
                    testFailed.set(true);
                    failureReason = "Failed to select MissBot unit";
                }
            } catch (Exception e) {
                testFailed.set(true);
                failureReason = "Error selecting MissBot: " + e.getMessage();
            }
        });
    }
    
    private void enableAutoTargeting() throws Exception {
        Platform.runLater(() -> {
            try {
                // Set MissBot to target TargetDummy
                Unit targetUnit = null;
                for (Unit unit : units) {
                    if (unit.character.id == -2001) {
                        targetUnit = unit;
                        break;
                    }
                }
                
                if (targetUnit == null) {
                    testFailed.set(true);
                    failureReason = "Could not find TargetDummy unit for auto-targeting";
                    return;
                }
                
                // Enable auto-targeting on MissBot character
                missBot.usesAutomaticTargeting = true;
                missBotUnit.combatTarget = targetUnit;
                
                System.out.println("Auto-targeting enabled: MissBot -> TargetDummy");
            } catch (Exception e) {
                testFailed.set(true);
                failureReason = "Error enabling auto-targeting: " + e.getMessage();
            }
        });
    }
    
    private void unpauseAndMonitorCombat() throws Exception {
        Platform.runLater(() -> {
            try {
                // Set up combat monitoring
                setupCombatMonitoring();
                
                // Unpause the game by setting the paused field
                setPrivateField(gameInstance, "paused", false);
                
                System.out.println("Game unpaused, combat monitoring active");
            } catch (Exception e) {
                testFailed.set(true);
                failureReason = "Error unpausing game: " + e.getMessage();
                testCompleteLatch.countDown();
            }
        });
    }
    
    private void setupCombatMonitoring() {
        // Create a monitoring thread that checks game state periodically
        Thread monitorThread = new Thread(() -> {
            try {
                final AtomicInteger lastAmmoCount = new AtomicInteger(-1);
                final AtomicBoolean wasReloading = new AtomicBoolean(false);
                int consecutiveChecks = 0;
                
                while (!testFailed.get() && consecutiveChecks < 300) { // 30 seconds max (100ms intervals)
                    Thread.sleep(100);
                    consecutiveChecks++;
                    
                    Platform.runLater(() -> {
                        try {
                            // Check MissBot's weapon and ammunition
                            RangedWeapon weapon = (RangedWeapon) missBot.weapon;
                            int currentAmmo = weapon.getAmmunition();
                            String weaponState = missBot.currentWeaponState.getState();
                            
                            // Detect shots fired by ammunition decrease
                            if (lastAmmoCount.get() != -1 && currentAmmo < lastAmmoCount.get()) {
                                int newShots = lastAmmoCount.get() - currentAmmo;
                                shotsFired.addAndGet(newShots);
                                System.out.println("Shot fired! Total shots: " + shotsFired.get() + 
                                                 ", Ammo remaining: " + currentAmmo);
                            }
                            
                            // Detect reload initiation
                            if ("reloading".equals(weaponState) && !wasReloading.get()) {
                                reloadInitiated.set(true);
                                System.out.println("Reload sequence initiated");
                            }
                            
                            // Detect second round loading (during reload)
                            if ("reloading".equals(weaponState) && currentAmmo >= 2) {
                                secondRoundLoaded.set(true);
                                System.out.println("Second round loaded during reload");
                            }
                            
                            // Check for test completion
                            if (shotsFired.get() >= 6 && reloadInitiated.get() && secondRoundLoaded.get()) {
                                System.out.println("Test success criteria met!");
                                testCompleteLatch.countDown();
                                return;
                            }
                            
                            // Update tracking variables
                            lastAmmoCount.set(currentAmmo);
                            wasReloading.set("reloading".equals(weaponState));
                            
                        } catch (Exception e) {
                            testFailed.set(true);
                            failureReason = "Error during combat monitoring: " + e.getMessage();
                            testCompleteLatch.countDown();
                        }
                    });
                }
                
                // Timeout reached
                if (consecutiveChecks >= 300) {
                    testFailed.set(true);
                    failureReason = "Test timeout reached. Shots fired: " + shotsFired.get() + 
                                  ", Reload initiated: " + reloadInitiated.get() + 
                                  ", 2nd round loaded: " + secondRoundLoaded.get();
                    testCompleteLatch.countDown();
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                testFailed.set(true);
                failureReason = "Monitoring thread interrupted";
                testCompleteLatch.countDown();
            } catch (Exception e) {
                testFailed.set(true);
                failureReason = "Unexpected error in monitoring: " + e.getMessage();
                testCompleteLatch.countDown();
            }
        });
        
        monitorThread.setDaemon(true);
        monitorThread.start();
    }
}
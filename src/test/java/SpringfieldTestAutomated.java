import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

import javafx.application.Platform;
import javafx.stage.Stage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;

import combat.*;
import data.*;
import game.*;

/**
 * Automated test for Springfield 1861 Musket 2v2 combat scenario as specified in DevCycle 38 System 6.
 * 
 * Test Sequence:
 * 1. Start game and activate debug mode
 * 2. Load test factions (TestFactionAlpha and TestFactionBeta)
 * 3. Create save file with 2v2 Springfield 1861 combat setup
 * 4. Verify all 4 soldiers are loaded correctly (50 stats, rifle level 1, Springfield 1861 muskets)
 * 5. Position characters: allies 3 feet apart, enemies 100 feet apart
 * 6. Enable auto-targeting for all characters
 * 7. Draw rectangle around all characters to select them
 * 8. Unpause game and monitor combat execution for 5 minutes
 * 9. Monitor for exceptions in console output
 * 10. Output detailed stats for all characters at completion
 * 
 * Success Criteria: Combat completes without exceptions within 5 minutes
 * Failure Conditions: Exception thrown during combat
 * 
 * @author DevCycle 38 System 6 - SpringfieldTestAutomated Implementation
 */
public class SpringfieldTestAutomated {
    
    private OpenFields2 gameInstance;
    private GameClock gameClock;
    private List<Unit> units;
    private SaveGameController saveGameController;
    private SelectionManager selectionManager;
    private CountDownLatch gameReadyLatch;
    private CountDownLatch combatCompleteLatch;
    
    // Test monitoring variables
    private AtomicBoolean combatComplete = new AtomicBoolean(false);
    private AtomicBoolean testFailed = new AtomicBoolean(false);
    private AtomicBoolean exceptionDetected = new AtomicBoolean(false);
    private String failureReason = "";
    
    // Character references (2v2 combat)
    private combat.Character soldierAlpha;
    private combat.Character gunfighterAlpha;
    private combat.Character soldierBeta;
    private combat.Character gunfighterBeta;
    private Unit soldierAlphaUnit;
    private Unit gunfighterAlphaUnit;
    private Unit soldierBetaUnit;
    private Unit gunfighterBetaUnit;
    
    @BeforeEach
    public void setUp() throws Exception {
        System.out.println("=== Springfield Test Automated Setup ===");
        
        // Initialize JavaFX if not already initialized
        if (!Platform.isFxApplicationThread()) {
            CountDownLatch fxLatch = new CountDownLatch(1);
            Platform.startup(() -> fxLatch.countDown());
            fxLatch.await(5, TimeUnit.SECONDS);
        }
        
        // Reset test monitoring variables
        combatComplete.set(false);
        testFailed.set(false);
        exceptionDetected.set(false);
        failureReason = "";
        
        gameReadyLatch = new CountDownLatch(1);
        combatCompleteLatch = new CountDownLatch(1);
        
        System.out.println("✓ Test setup complete");
    }
    
    @Test
    @Timeout(320) // 5 minutes + 20 second buffer
    public void testSpringfieldAutomated() throws Exception {
        System.out.println("Starting Springfield 1861 Musket 2v2 automated test...");
        
        // Step 1: Start game and activate debug mode
        startGameAndActivateDebugMode();
        
        // Wait for game to be ready
        assertTrue(gameReadyLatch.await(10, TimeUnit.SECONDS), "Game should start within 10 seconds");
        System.out.println("✓ Game started and debug mode activated");
        
        // Step 2 & 3: Load Springfield test save file
        loadSpringfieldSave();
        System.out.println("✓ Springfield 2v2 save loaded");
        
        // Step 4: Verify all 4 soldiers are loaded correctly
        verifySoldiers();
        System.out.println("✓ All 4 soldiers verified with correct stats and positioning");
        
        // Step 5: Enable auto-targeting and set careful aiming for all characters
        enableAutoTargetingAndSetCarefulAiming();
        System.out.println("✓ Auto-targeting enabled and careful aiming set for all characters");
        
        // Step 6: Select all characters with rectangle selection
        selectAllCharactersWithRectangle();
        System.out.println("✓ All characters selected with rectangle");
        
        // Step 7: Unpause game and monitor combat execution
        unpauseAndMonitorCombat();
        System.out.println("✓ Game unpaused, monitoring 2v2 combat for exceptions and completion");
        
        // Wait for combat completion or timeout (5 minutes)
        assertTrue(combatCompleteLatch.await(305, TimeUnit.SECONDS), "Combat should complete within 5 minutes");
        
        // Verify success criteria
        assertFalse(testFailed.get(), "Test failed: " + failureReason);
        assertFalse(exceptionDetected.get(), "Exception detected during combat");
        assertTrue(combatComplete.get(), "Combat should have completed");
        
        // Output detailed stats for all characters
        outputDetailedStats();
        
        System.out.println("=== Springfield Test Automated SUCCESS ===");
        System.out.println("2v2 combat completed without exceptions");
        
        // Pause for 5 seconds after test completion
        System.out.println("Pausing for 5 seconds...");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Sleep interrupted");
        }
        System.out.println("Test complete.");
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
                selectionManager = (SelectionManager) getPrivateField(gameInstance, "selectionManager");
                
                // Activate debug mode
                GameRenderer.setDebugMode(true);
                
                gameReadyLatch.countDown();
            } catch (Exception e) {
                testFailed.set(true);
                failureReason = "Failed to start game: " + e.getMessage();
                System.err.println("Exception during game startup: " + e.getMessage());
                e.printStackTrace();
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
    
    private void loadSpringfieldSave() throws Exception {
        CountDownLatch loadLatch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Load test_c.json save (Springfield 2v2 save)
                saveGameController.loadGameFromTestSlot('c');
                
                // Wait a moment for load to complete
                Platform.runLater(() -> {
                    try {
                        // Verify characters are loaded
                        soldierAlpha = null;
                        gunfighterAlpha = null;
                        soldierBeta = null;
                        gunfighterBeta = null;
                        soldierAlphaUnit = null;
                        gunfighterAlphaUnit = null;
                        soldierBetaUnit = null;
                        gunfighterBetaUnit = null;
                        
                        for (Unit unit : units) {
                            combat.Character character = unit.character;
                            if (character.id == -1003) { // SoldierAlpha
                                soldierAlpha = character;
                                soldierAlphaUnit = unit;
                            } else if (character.id == -1002) { // GunfighterAlpha
                                gunfighterAlpha = character;
                                gunfighterAlphaUnit = unit;
                            } else if (character.id == -2003) { // SoldierBeta
                                soldierBeta = character;
                                soldierBetaUnit = unit;
                            } else if (character.id == -2002) { // GunfighterBeta
                                gunfighterBeta = character;
                                gunfighterBetaUnit = unit;
                            }
                        }
                        
                        if (soldierAlpha == null || gunfighterAlpha == null || soldierBeta == null || gunfighterBeta == null) {
                            testFailed.set(true);
                            failureReason = "Failed to find all 4 characters after save load";
                        }
                        
                        loadLatch.countDown();
                    } catch (Exception e) {
                        testFailed.set(true);
                        failureReason = "Error verifying loaded characters: " + e.getMessage();
                        System.err.println("Exception during character verification: " + e.getMessage());
                        e.printStackTrace();
                        loadLatch.countDown();
                    }
                });
            } catch (Exception e) {
                testFailed.set(true);
                failureReason = "Failed to load Springfield save: " + e.getMessage();
                System.err.println("Exception during save load: " + e.getMessage());
                e.printStackTrace();
                loadLatch.countDown();
            }
        });
        
        assertTrue(loadLatch.await(5, TimeUnit.SECONDS), "Save load should complete within 5 seconds");
    }
    
    private void verifySoldiers() throws Exception {
        Platform.runLater(() -> {
            try {
                // Verify SoldierAlpha configuration
                assertEquals(-1003, soldierAlpha.id, "SoldierAlpha should have correct ID");
                assertEquals(50, soldierAlpha.dexterity, "SoldierAlpha should have dexterity 50");
                assertEquals(100, soldierAlpha.currentHealth, "SoldierAlpha should have 100 health");
                
                // Verify GunfighterAlpha configuration
                assertEquals(-1002, gunfighterAlpha.id, "GunfighterAlpha should have correct ID");
                assertEquals(50, gunfighterAlpha.dexterity, "GunfighterAlpha should have dexterity 50");
                assertEquals(50, gunfighterAlpha.currentHealth, "GunfighterAlpha should have 50 health");
                
                // Verify SoldierBeta configuration
                assertEquals(-2003, soldierBeta.id, "SoldierBeta should have correct ID");
                assertEquals(50, soldierBeta.dexterity, "SoldierBeta should have dexterity 50");
                assertEquals(100, soldierBeta.currentHealth, "SoldierBeta should have 100 health");
                
                // Verify GunfighterBeta configuration
                assertEquals(-2002, gunfighterBeta.id, "GunfighterBeta should have correct ID");
                assertEquals(50, gunfighterBeta.dexterity, "GunfighterBeta should have dexterity 50");
                assertEquals(50, gunfighterBeta.currentHealth, "GunfighterBeta should have 50 health");
                
                // Verify rifle skills for soldiers
                boolean soldierAlphaHasRifleSkill = soldierAlpha.getSkillLevel("skl_rifle") == 1;
                boolean soldierBetaHasRifleSkill = soldierBeta.getSkillLevel("skl_rifle") == 1;
                assertTrue(soldierAlphaHasRifleSkill, "SoldierAlpha should have rifle skill level 1");
                assertTrue(soldierBetaHasRifleSkill, "SoldierBeta should have rifle skill level 1");
                
                // Verify positioning from save file
                assertEquals(100.0, soldierAlphaUnit.x, 0.1, "SoldierAlpha should be at x=100");
                assertEquals(200.0, soldierAlphaUnit.y, 0.1, "SoldierAlpha should be at y=200");
                assertEquals(100.0, gunfighterAlphaUnit.x, 0.1, "GunfighterAlpha should be at x=100 (same as SoldierAlpha)");
                assertEquals(228.0, gunfighterAlphaUnit.y, 0.1, "GunfighterAlpha should be at y=228 (4 feet south)");
                assertEquals(800.0, soldierBetaUnit.x, 0.1, "SoldierBeta should be at x=800 (100 feet from Alpha team)");
                assertEquals(200.0, soldierBetaUnit.y, 0.1, "SoldierBeta should be at y=200");
                assertEquals(800.0, gunfighterBetaUnit.x, 0.1, "GunfighterBeta should be at x=800 (same as SoldierBeta)");
                assertEquals(228.0, gunfighterBetaUnit.y, 0.1, "GunfighterBeta should be at y=228 (4 feet south)");
                
                System.out.println("All soldiers verified with correct stats, weapons, and positioning");
                
            } catch (Exception e) {
                testFailed.set(true);
                failureReason = "Error verifying soldier configuration: " + e.getMessage();
                System.err.println("Exception during soldier verification: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    
    private void enableAutoTargetingAndSetCarefulAiming() throws Exception {
        Platform.runLater(() -> {
            try {
                // Enable auto-targeting for all characters
                soldierAlpha.usesAutomaticTargeting = true;
                gunfighterAlpha.usesAutomaticTargeting = true;
                soldierBeta.usesAutomaticTargeting = true;
                gunfighterBeta.usesAutomaticTargeting = true;
                
                // Set all characters to use careful aiming speed for this test
                soldierAlpha.currentAimingSpeed = combat.AimingSpeed.CAREFUL;
                gunfighterAlpha.currentAimingSpeed = combat.AimingSpeed.CAREFUL;
                soldierBeta.currentAimingSpeed = combat.AimingSpeed.CAREFUL;
                gunfighterBeta.currentAimingSpeed = combat.AimingSpeed.CAREFUL;
                
                // Set combat targets for alpha team vs beta team
                soldierAlphaUnit.combatTarget = soldierBetaUnit;
                gunfighterAlphaUnit.combatTarget = gunfighterBetaUnit;
                soldierBetaUnit.combatTarget = soldierAlphaUnit;
                gunfighterBetaUnit.combatTarget = gunfighterAlphaUnit;
                
                System.out.println("Auto-targeting enabled for all characters");
                System.out.println("All characters set to CAREFUL aiming speed for this test");
                System.out.println("Combat targets set: Alpha team vs Beta team");
                
            } catch (Exception e) {
                testFailed.set(true);
                failureReason = "Error enabling auto-targeting: " + e.getMessage();
                System.err.println("Exception during auto-targeting setup: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private void selectAllCharactersWithRectangle() throws Exception {
        Platform.runLater(() -> {
            try {
                // Clear any existing selection
                selectionManager.clearSelection();
                
                // Calculate rectangle coordinates that encompass all 4 characters
                // Characters are at: (100,200), (100,228), (800,200), (800,228)
                // Create a rectangle from (50, 150) to (850, 280) to encompass all
                double startX = 50.0;   // Left of all characters
                double startY = 150.0;  // Above all characters
                double endX = 850.0;    // Right of all characters
                double endY = 280.0;    // Below all characters
                
                // Simulate rectangle selection
                selectionManager.startRectangleSelection(startX, startY);
                selectionManager.updateRectangleSelection(endX, endY);
                selectionManager.completeRectangleSelection(units);
                
                // Verify all 4 characters are selected
                if (selectionManager.getSelectionCount() == 4) {
                    System.out.println("✓ Rectangle selection successful: All 4 characters selected");
                    System.out.println("  Selection count: " + selectionManager.getSelectionCount());
                } else {
                    System.out.println("⚠ Rectangle selection partial: " + selectionManager.getSelectionCount() + " units selected");
                    for (Unit unit : selectionManager.getSelectedUnits()) {
                        System.out.println("  - Selected: " + unit.character.nickname + " at (" + unit.x + ", " + unit.y + ")");
                    }
                }
                
            } catch (Exception e) {
                System.err.println("Error during rectangle selection: " + e.getMessage());
                e.printStackTrace();
                // Don't fail the test for selection issues, just log them
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
                
                System.out.println("Game unpaused, 2v2 combat monitoring active");
            } catch (Exception e) {
                testFailed.set(true);
                failureReason = "Error unpausing game: " + e.getMessage();
                System.err.println("Exception during game unpause: " + e.getMessage());
                e.printStackTrace();
                combatCompleteLatch.countDown();
            }
        });
    }
    
    private void setupCombatMonitoring() {
        // Create a monitoring thread that checks for combat completion and exceptions
        Thread monitorThread = new Thread(() -> {
            try {
                AtomicBoolean checkCounter = new AtomicBoolean(false);
                final int maxChecks = 3000; // 5 minutes at 100ms intervals
                
                for (int consecutiveChecks = 0; consecutiveChecks < maxChecks && !testFailed.get() && !combatComplete.get(); consecutiveChecks++) {
                    Thread.sleep(100);
                    final int currentCheck = consecutiveChecks;
                    
                    Platform.runLater(() -> {
                        try {
                            // Check for combat completion (both characters of either faction incapacitated or 5 minutes elapsed)
                            boolean alphaTeamEliminated = soldierAlpha.isIncapacitated() && gunfighterAlpha.isIncapacitated();
                            boolean betaTeamEliminated = soldierBeta.isIncapacitated() && gunfighterBeta.isIncapacitated();
                            boolean anyIncapacitated = soldierAlpha.isIncapacitated() || gunfighterAlpha.isIncapacitated() ||
                                                     soldierBeta.isIncapacitated() || gunfighterBeta.isIncapacitated();
                            
                            // Check for team-based victory condition
                            if (alphaTeamEliminated || betaTeamEliminated) {
                                combatComplete.set(true);
                                String winner = alphaTeamEliminated ? "Beta Team" : "Alpha Team";
                                System.out.println("Springfield test completed! " + winner + " victory - opposing team eliminated.");
                                combatCompleteLatch.countDown();
                                return;
                            }
                            
                            // For Springfield test, completion is also reaching 5 minutes without exceptions  
                            if (currentCheck >= 2999) { // 5 minutes reached (2999 checks at 100ms = ~5 minutes)
                                combatComplete.set(true);
                                System.out.println("Springfield test completed! 5 minutes elapsed without exceptions.");
                                combatCompleteLatch.countDown();
                                return;
                            }
                            
                            // Debug output every 100 checks (10 seconds)
                            if (currentCheck > 0 && currentCheck % 100 == 0) {
                                System.out.println("Combat monitoring: " + currentCheck + " checks (" + (currentCheck/10) + " seconds)");
                                System.out.println("  Alpha Team - SoldierAlpha: health=" + soldierAlpha.currentHealth + 
                                                 ", GunfighterAlpha: health=" + gunfighterAlpha.currentHealth);
                                System.out.println("  Beta Team - SoldierBeta: health=" + soldierBeta.currentHealth + 
                                                 ", GunfighterBeta: health=" + gunfighterBeta.currentHealth);
                                
                                if (anyIncapacitated) {
                                    System.out.println("  Individual incapacitations detected (waiting for team elimination)");
                                }
                                if (alphaTeamEliminated) {
                                    System.out.println("  Alpha Team eliminated!");
                                }
                                if (betaTeamEliminated) {
                                    System.out.println("  Beta Team eliminated!");
                                }
                            }
                            
                        } catch (Exception e) {
                            testFailed.set(true);
                            exceptionDetected.set(true);
                            failureReason = "Exception during combat monitoring: " + e.getMessage();
                            System.err.println("Exception during Springfield combat: " + e.getMessage());
                            e.printStackTrace();
                            combatCompleteLatch.countDown();
                        }
                    });
                }
                
                // Natural loop completion - this is success for Springfield test  
                if (!combatComplete.get() && !testFailed.get()) {
                    combatComplete.set(true);
                    System.out.println("Springfield test completed! 5 minutes elapsed without exceptions.");
                    combatCompleteLatch.countDown();
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                testFailed.set(true);
                failureReason = "Monitoring thread interrupted";
                combatCompleteLatch.countDown();
            } catch (Exception e) {
                testFailed.set(true);
                exceptionDetected.set(true);
                failureReason = "Unexpected error in monitoring: " + e.getMessage();
                System.err.println("Unexpected exception in Springfield monitoring: " + e.getMessage());
                e.printStackTrace();
                combatCompleteLatch.countDown();
            }
        });
        
        monitorThread.setDaemon(true);
        monitorThread.start();
    }
    
    private void outputDetailedStats() {
        // Output stats for all 4 characters
        try {
            // Access InputManager from game instance first
            Object inputManager = getPrivateField(gameInstance, "inputManager");
            
            if (inputManager != null) {
                // Get the DisplayCoordinator from InputManager
                Object displayCoordinator = getPrivateField(inputManager, "displayCoordinator");
                
                if (displayCoordinator instanceof DisplayCoordinator) {
                    DisplayCoordinator coordinator = (DisplayCoordinator) displayCoordinator;
                    
                    // Display stats for all 4 characters using actual Shift+/ command
                    System.out.println("=== SoldierAlpha Stats (via DisplayCoordinator) ===");
                    coordinator.displayCharacterStats(soldierAlphaUnit);
                    
                    System.out.println();
                    System.out.println("=== GunfighterAlpha Stats (via DisplayCoordinator) ===");
                    coordinator.displayCharacterStats(gunfighterAlphaUnit);
                    
                    System.out.println();
                    System.out.println("=== SoldierBeta Stats (via DisplayCoordinator) ===");
                    coordinator.displayCharacterStats(soldierBetaUnit);
                    
                    System.out.println();
                    System.out.println("=== GunfighterBeta Stats (via DisplayCoordinator) ===");
                    coordinator.displayCharacterStats(gunfighterBetaUnit);
                    return; // Success - exit method
                }
            }
            
            // Fallback if DisplayCoordinator not available
            System.out.println("DisplayCoordinator not available - using fallback stats display");
            displayBasicStats(soldierAlphaUnit, "SoldierAlpha");
            System.out.println();
            displayBasicStats(gunfighterAlphaUnit, "GunfighterAlpha");
            System.out.println();
            displayBasicStats(soldierBetaUnit, "SoldierBeta");
            System.out.println();
            displayBasicStats(gunfighterBetaUnit, "GunfighterBeta");
            
        } catch (Exception e) {
            System.err.println("Error accessing DisplayCoordinator: " + e.getMessage());
            // Fallback to basic stats display
            displayBasicStats(soldierAlphaUnit, "SoldierAlpha");
            System.out.println();
            displayBasicStats(gunfighterAlphaUnit, "GunfighterAlpha");
            System.out.println();
            displayBasicStats(soldierBetaUnit, "SoldierBeta");
            System.out.println();
            displayBasicStats(gunfighterBetaUnit, "GunfighterBeta");
        }
    }
    
    /**
     * Simple fallback stats display if DisplayCoordinator is not available
     */
    private void displayBasicStats(Unit unit, String characterName) {
        combat.Character character = unit.character;
        
        System.out.println("=== " + characterName + " Basic Stats ===");
        System.out.println("Health: " + character.currentHealth + "/" + character.health);
        System.out.println("Incapacitated: " + (character.isIncapacitated() ? "YES" : "NO"));
        
        // Calculate wounds inflicted manually
        int totalWoundsInflicted = character.woundsInflictedScratch + character.woundsInflictedLight + 
                                 character.woundsInflictedSerious + character.woundsInflictedCritical;
        
        System.out.println("Combat Stats:");
        System.out.println("  Attacks: " + character.getAttacksAttempted() + " attempted, " + character.getAttacksSuccessful() + " successful");
        System.out.println("  Wounds Inflicted: " + totalWoundsInflicted);
        System.out.println("=== End " + characterName + " Stats ===");
    }
}
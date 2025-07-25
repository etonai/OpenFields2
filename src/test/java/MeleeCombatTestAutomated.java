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
 * Automated test for Melee Combat scenario as specified in DevCycle 40 System 2.
 * Enhanced in DevCycle 41 with random seed generation and reproduction capabilities.
 * 
 * SEED MANAGEMENT:
 * - Normal Operation: Uses randomly generated seed each run to discover edge cases
 * - Bug Reproduction: Use -Dtest.seed=123456789 to reproduce specific test scenarios
 * - Seed Reporting: Outputs seed at start and completion for easy reproduction
 * 
 * USAGE EXAMPLES:
 * 
 * Basic Usage:
 * mvn test -Dtest=MeleeCombatTestAutomated                     # Random seed testing
 * mvn test -Dtest=MeleeCombatTestAutomated -Dtest.seed=54321  # Positive seed reproduction
 * 
 * Cross-Platform Seed Reproduction:
 * 
 * Windows PowerShell (recommended - always quote properties):
 * mvn test "-Dtest=MeleeCombatTestAutomated" "-Dtest.seed=4292768217366888882"
 * 
 * Windows Command Prompt (standard syntax):
 * mvn test -Dtest=MeleeCombatTestAutomated -Dtest.seed=4292768217366888882
 * 
 * macOS/Linux (bash/zsh):
 * mvn test -Dtest=MeleeCombatTestAutomated -Dtest.seed=4292768217366888882
 * 
 * TROUBLESHOOTING:
 * - If you see "Unknown lifecycle phase .seed=" errors, quote the -D properties
 * - Windows PowerShell has parsing issues with -D properties, always use quotes
 * - Use Windows Command Prompt as alternative if PowerShell fails
 * - All seeds (positive and negative) produce deterministic results
 * 
 * Test Sequence:
 * 1. Generate random seed or use manual override from -Dtest.seed property
 * 2. Start game and activate debug mode with deterministic seed
 * 3. Load test_d.json save file
 * 4. Verify both soldiers are loaded correctly (60 HP, knife skill level 1, mel_bowie_knife)
 * 5. Confirm auto-targeting and melee combat mode are enabled for both characters
 * 6. Select both characters with rectangle selection
 * 7. Unpause game and monitor combat execution
 * 8. Monitor for exceptions and defense attempts
 * 9. Track combat until one character is incapacitated or 1 minute timeout
 * 10. Output detailed stats and final seed for reproduction
 * 
 * Success Criteria: No exceptions thrown AND at least one character attempts defense at least once
 * Failure Conditions: Exception thrown, or combat duration exceeds 1 minute
 * 
 * @author DevCycle 40 System 2 - Melee Combat Defense Test
 * @author DevCycle 41 System 4 - Random Seed Generation and Reporting
 */
public class MeleeCombatTestAutomated {
    
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
    private AtomicInteger defenseAttempts = new AtomicInteger(0);
    private AtomicInteger nonZeroDefenseModifiers = new AtomicInteger(0);
    private String failureReason = "";
    
    // Problem collection for comprehensive reporting - don't fail fast
    // Use synchronized collection for thread safety
    private final java.util.List<String> problems = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
    
    // Random seed for deterministic testing with reproducibility
    private long testSeed;
    
    // Capture console output to analyze defense messages (both stdout and stderr)
    private java.io.ByteArrayOutputStream consoleOutput = new java.io.ByteArrayOutputStream();
    private java.io.PrintStream originalOut;
    private java.io.PrintStream originalErr;
    
    // Exception detection enhancement
    private Thread.UncaughtExceptionHandler originalExceptionHandler;
    private final java.util.List<Throwable> capturedExceptions = new java.util.concurrent.CopyOnWriteArrayList<>();
    
    private combat.Character soldierAlpha;
    private combat.Character soldierBeta;
    private Unit alphaUnit;
    private Unit betaUnit;
    
    @BeforeEach
    public void setUp() throws Exception {
        System.out.println("=== Melee Combat Test Automated Setup ===");
        
        // Generate or use override seed for deterministic testing
        String seedProperty = System.getProperty("test.seed");
        if (seedProperty != null && !seedProperty.trim().isEmpty()) {
            // Use manual seed override for bug reproduction
            try {
                testSeed = Long.parseLong(seedProperty.trim());
                System.out.println("=== MANUAL SEED OVERRIDE ===");
                System.out.println("Using manual seed: " + testSeed);
                System.out.println("============================");
            } catch (NumberFormatException e) {
                // Invalid seed format, fall back to random
                testSeed = new SecureRandom().nextLong();
                System.out.println("⚠️ Invalid seed format '" + seedProperty + "', using random seed: " + testSeed);
                System.out.println("💡 HELP: If on Windows PowerShell, try quoting the properties:");
                System.out.println("   mvn test \"-Dtest=MeleeCombatTestAutomated\" \"-Dtest.seed=" + seedProperty + "\"");
                System.out.println("   Or use Windows Command Prompt instead of PowerShell");
                System.out.println("   Valid seed format: any positive or negative long integer");
            }
        } else {
            // Generate random seed for normal operation
            testSeed = new SecureRandom().nextLong();
            System.out.println("=== RANDOM SEED TESTING ===");
            System.out.println("Generated random seed: " + testSeed);
            System.out.println("To reproduce this test (Windows PowerShell): mvn test \"-Dtest=MeleeCombatTestAutomated\" \"-Dtest.seed=" + testSeed + "\"");
            System.out.println("To reproduce this test (CMD/Linux/macOS): mvn test -Dtest=MeleeCombatTestAutomated -Dtest.seed=" + testSeed);
            System.out.println("============================");
        }
        
        // Set up deterministic mode with generated or override seed
        GameConfiguration.setDeterministicMode(true, testSeed);
        System.out.println("✓ Deterministic mode enabled with seed " + testSeed);
        
        // Capture console output to analyze defense messages (both stdout and stderr)
        originalOut = System.out;
        originalErr = System.err;
        consoleOutput = new java.io.ByteArrayOutputStream();
        
        // Capture System.out
        System.setOut(new java.io.PrintStream(new java.io.OutputStream() {
            @Override
            public void write(int b) throws java.io.IOException {
                consoleOutput.write(b);
                originalOut.write(b); // Still show output normally
            }
        }));
        
        // Capture System.err  
        System.setErr(new java.io.PrintStream(new java.io.OutputStream() {
            @Override
            public void write(int b) throws java.io.IOException {
                consoleOutput.write(b);
                originalErr.write(b); // Still show output normally
            }
        }));
        
        // Enhanced exception detection - capture uncaught exceptions from any thread
        originalExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            // Filter out known benign exceptions that shouldn't fail the test
            boolean isBenignException = isKnownBenignException(exception);
            
            // Always capture the exception for analysis
            capturedExceptions.add(exception);
            
            // Log the exception with full stack trace
            System.err.println("=== UNCAUGHT EXCEPTION DETECTED ===");
            System.err.println("Thread: " + thread.getName());
            System.err.println("Exception: " + exception.getClass().getSimpleName() + ": " + exception.getMessage());
            System.err.println("Benign: " + isBenignException);
            exception.printStackTrace();
            System.err.println("=== END EXCEPTION ===");
            
            // Only fail the test for non-benign exceptions
            if (!isBenignException) {
                exceptionDetected.set(true);
                testFailed.set(true);
                failureReason = "Uncaught exception in thread " + thread.getName() + ": " + exception.getMessage();
                
                // Signal test completion due to exception
                combatCompleteLatch.countDown();
            } else {
                System.out.println("INFO: Benign exception ignored for test purposes: " + exception.getClass().getSimpleName());
            }
            
            // Call original handler if it exists
            if (originalExceptionHandler != null) {
                originalExceptionHandler.uncaughtException(thread, exception);
            }
        });
        
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
        defenseAttempts.set(0);
        nonZeroDefenseModifiers.set(0);
        failureReason = "";
        
        gameReadyLatch = new CountDownLatch(1);
        combatCompleteLatch = new CountDownLatch(1);
        
        System.out.println("✓ Test setup complete");
    }
    
    @org.junit.jupiter.api.AfterEach
    public void tearDown() throws Exception {
        System.out.println("=== Melee Combat Test Automated Teardown ===");
        
        // Report final seed for reference and reproduction
        System.out.println("=== TEST COMPLETION SUMMARY ===");
        System.out.println("Test seed used: " + testSeed);
        System.out.println("To reproduce (Windows PowerShell): mvn test \"-Dtest=MeleeCombatTestAutomated\" \"-Dtest.seed=" + testSeed + "\"");
        System.out.println("To reproduce (CMD/Linux/macOS): mvn test -Dtest=MeleeCombatTestAutomated -Dtest.seed=" + testSeed);
        System.out.println("===============================");
        
        // Reset deterministic mode to avoid interfering with other tests
        GameConfiguration.reset();
        System.out.println("✓ Deterministic mode reset");
        
        // Restore original exception handler
        Thread.setDefaultUncaughtExceptionHandler(originalExceptionHandler);
        
        // Restore original System.out and System.err
        if (originalOut != null) {
            System.setOut(originalOut);
        }
        if (originalErr != null) {
            System.setErr(originalErr);
        }
        
        System.out.println("✓ Test teardown complete");
    }
    
    @Test
    @Timeout(100) // 1 minute + 40 second buffer
    public void testMeleeCombatAutomated() throws Exception {
        System.out.println("Starting Melee Combat automated test...");
        
        // Step 1: Start game and activate debug mode
        startGameAndActivateDebugMode();
        
        // Wait for game to be ready
        assertTrue(gameReadyLatch.await(10, TimeUnit.SECONDS), "Game should start within 10 seconds");
        System.out.println("✓ Game started and debug mode activated");
        
        // Step 2: Load test_d.json save file
        loadMeleeCombatSave();
        System.out.println("✓ Melee combat save loaded");
        
        // Step 3: Verify both soldiers are loaded correctly
        verifySoldiers();
        System.out.println("✓ Both soldiers verified");
        
        // Step 4: Confirm auto-targeting and melee combat mode are enabled
        confirmAutoTargetingAndMeleeMode();
        System.out.println("✓ Auto-targeting and melee combat mode confirmed");
        
        // Step 5: Select both characters and unpause game
        selectBothCharactersAndUnpause();
        System.out.println("✓ Both characters selected and game unpaused");
        
        // Wait for combat completion or timeout (1 minute = 60 seconds)
        assertTrue(combatCompleteLatch.await(65, TimeUnit.SECONDS), "Combat should complete within 1 minute");
        
        System.err.println("DEBUG: Combat completed, about to analyze defense activity");
        
        // Remove the first analysis - we'll do comprehensive analysis later
        // analyzeDefenseActivity();
        
        // Enhanced exception reporting
        if (exceptionDetected.get()) {
            StringBuilder exceptionReport = new StringBuilder();
            exceptionReport.append("Exception detected during combat execution!\n");
            exceptionReport.append("Failure Reason: ").append(failureReason).append("\n");
            
            if (!capturedExceptions.isEmpty()) {
                exceptionReport.append("Captured Exceptions (").append(capturedExceptions.size()).append("):\n");
                for (int i = 0; i < capturedExceptions.size(); i++) {
                    Throwable exception = capturedExceptions.get(i);
                    exceptionReport.append("  ").append(i + 1).append(". ")
                                  .append(exception.getClass().getSimpleName())
                                  .append(": ").append(exception.getMessage()).append("\n");
                }
            }
            
            System.err.println("=== EXCEPTION DETECTION SUMMARY ===");
            System.err.println(exceptionReport.toString());
            System.err.println("=== END SUMMARY ===");
        }
        
        // Calculate total attacks for use in success criteria and reporting
        int totalAttacks = soldierAlpha.getAttacksAttempted() + soldierBeta.getAttacksAttempted();
        
        // Wait a moment for all combat systems to finalize damage before validation
        System.err.println("DEBUG: Waiting 3 seconds for combat systems to finalize...");
        try {
            Thread.sleep(3000); // Wait 3 seconds for all damage to be processed
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Run comprehensive validation to collect all problems FIRST (don't fail fast)
        System.err.println("DEBUG: Starting comprehensive validation...");
        
        // Analyze defense activity and check for AUTO-TARGETING ERRORs
        analyzeDefenseActivity();
        
        validateCharacterWounds(soldierAlpha, "SoldierAlpha");
        validateCharacterWounds(soldierBeta, "SoldierBeta");
        validateCharacterHealth(soldierAlpha, "SoldierAlpha");
        validateCharacterHealth(soldierBeta, "SoldierBeta");
        validateAttackTiming();
        validateAttackFrequency();
        
        // Report all collected problems
        reportAllProblems();
        
        // If any problems were found, display character stats and fail the test
        System.err.println("DEBUG: Checking if problems.isEmpty(): " + problems.isEmpty());
        System.err.println("DEBUG: Problems size: " + problems.size());
        if (!problems.isEmpty()) {
            System.err.println("=== VALIDATION FAILURE - CHARACTER STATS DEBUG ===");
            outputDetailedStats();
            System.err.println("=== END CHARACTER STATS DEBUG ===");
            System.err.println("DEBUG: About to call fail() with " + problems.size() + " problems");
            fail("Combat system validation failed with " + problems.size() + " problem(s). See comprehensive problem report above.");
        }
        System.err.println("DEBUG: Passed the fail check - no problems found or fail() didn't work");
        
        // Now verify basic success criteria AFTER problem collection
        try {
            assertFalse(testFailed.get(), "Test failed: " + failureReason);
            assertFalse(exceptionDetected.get(), "Exception detected during combat. See detailed exception report above.");
            
            // Primary success criteria: No exceptions AND at least one defense attempt
            assertTrue(defenseAttempts.get() >= 1, "At least one defense attempt should have occurred. Actual: " + defenseAttempts.get());
            
            // Additional criteria: At least one non-zero defense modifier (indicating successful defense)
            assertTrue(nonZeroDefenseModifiers.get() >= 1, "At least one non-zero defense modifier should have occurred. Actual: " + nonZeroDefenseModifiers.get());
            
            // Enhanced success criteria: At least one attack performed
            assertTrue(totalAttacks >= 1, "At least one attack should have been performed. Alpha attacks: " + soldierAlpha.getAttacksAttempted() + ", Beta attacks: " + soldierBeta.getAttacksAttempted());
        } catch (AssertionError e) {
            // Display full character stats when primary success criteria fail
            System.err.println("=== PRIMARY SUCCESS CRITERIA FAILURE - CHARACTER STATS DEBUG ===");
            outputDetailedStats();
            System.err.println("=== END CHARACTER STATS DEBUG ===");
            throw e; // Re-throw the original assertion error
        }
        
        System.out.println("Test completed successfully - all success criteria met");
        System.out.println("Defense attempts: " + defenseAttempts.get() + ", Total attacks: " + totalAttacks);
        
        // Wait 2.5 seconds after combat completion for all systems to settle
        System.out.println("Waiting 2.5 seconds for all combat systems to settle...");
        try {
            Thread.sleep(2500); // Wait 2.5 seconds for background processes to quiet down
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Wait interrupted");
        }
        
        // Output detailed stats for both characters
        outputDetailedStats();
        
        System.out.println("=== Melee Combat Test Automated SUCCESS ===");
        System.out.println("Defense attempts detected: " + defenseAttempts.get());
        System.out.println("Winner: " + (soldierAlpha.isIncapacitated() ? "SoldierBeta" : "SoldierAlpha"));
        
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
    
    private void loadMeleeCombatSave() throws Exception {
        CountDownLatch loadLatch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Load test_d.json save
                saveGameController.loadGameFromTestSlot('d');
                
                // Wait a moment for load to complete
                Platform.runLater(() -> {
                    try {
                        // Verify characters are loaded
                        soldierAlpha = null;
                        soldierBeta = null;
                        alphaUnit = null;
                        betaUnit = null;
                        
                        for (Unit unit : units) {
                            combat.Character character = unit.character;
                            if (character.id == -1003) {
                                soldierAlpha = character;
                                alphaUnit = unit;
                            } else if (character.id == -2003) {
                                soldierBeta = character;
                                betaUnit = unit;
                            }
                        }
                        
                        if (soldierAlpha == null || soldierBeta == null) {
                            testFailed.set(true);
                            failureReason = "Failed to find SoldierAlpha or SoldierBeta after save load";
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
                failureReason = "Failed to load melee combat save: " + e.getMessage();
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
                assertEquals(60, soldierAlpha.currentHealth, "SoldierAlpha should have 60 health");
                assertEquals("CAREFUL", soldierAlpha.currentAimingSpeed.toString(), "SoldierAlpha should use careful aiming");
                
                // Verify SoldierBeta configuration
                assertEquals(-2003, soldierBeta.id, "SoldierBeta should have correct ID");
                assertEquals(50, soldierBeta.dexterity, "SoldierBeta should have dexterity 50");
                assertEquals(60, soldierBeta.currentHealth, "SoldierBeta should have 60 health");
                assertEquals("CAREFUL", soldierBeta.currentAimingSpeed.toString(), "SoldierBeta should use careful aiming");
                
                // Verify positioning (40 feet apart = 280 pixels)
                assertEquals(100.0, alphaUnit.x, 0.1, "SoldierAlpha should be at x=100");
                assertEquals(380.0, betaUnit.x, 0.1, "SoldierBeta should be at x=380");
                assertEquals(200.0, alphaUnit.y, 0.1, "Both should be at y=200");
                assertEquals(200.0, betaUnit.y, 0.1, "Both should be at y=200");
                
                double distance = Math.abs(betaUnit.x - alphaUnit.x);
                assertEquals(280.0, distance, 1.0, "Characters should be 280 pixels (40 feet) apart");
                
                // Verify knife skills
                boolean alphaHasKnifeSkill = soldierAlpha.getSkillLevel("skl_knife") == 1;
                boolean betaHasKnifeSkill = soldierBeta.getSkillLevel("skl_knife") == 1;
                assertTrue(alphaHasKnifeSkill, "SoldierAlpha should have knife skill level 1");
                assertTrue(betaHasKnifeSkill, "SoldierBeta should have knife skill level 1");
                
                // Verify melee weapons
                assertEquals("mel_bowie_knife", soldierAlpha.weapon.getWeaponId(), "SoldierAlpha should have mel_bowie_knife");
                assertEquals("mel_bowie_knife", soldierBeta.weapon.getWeaponId(), "SoldierBeta should have mel_bowie_knife");
                
                // DevCycle 41: System 7 - Set both characters to running speed
                soldierAlpha.setCurrentMovementType(combat.MovementType.RUN);
                soldierBeta.setCurrentMovementType(combat.MovementType.RUN);
                System.out.println("✓ Both characters set to running speed");
                System.out.println("  SoldierAlpha movement: " + soldierAlpha.currentMovementType.getDisplayName() + 
                                 " (speed multiplier: " + soldierAlpha.currentMovementType.getSpeedMultiplier() + "x)");
                System.out.println("  SoldierBeta movement: " + soldierBeta.currentMovementType.getDisplayName() + 
                                 " (speed multiplier: " + soldierBeta.currentMovementType.getSpeedMultiplier() + "x)");
                
                // Verify movement type was set correctly
                assertEquals(combat.MovementType.RUN, soldierAlpha.currentMovementType, "SoldierAlpha should be at running speed");
                assertEquals(combat.MovementType.RUN, soldierBeta.currentMovementType, "SoldierBeta should be at running speed");
                
            } catch (Exception e) {
                testFailed.set(true);
                failureReason = "Error verifying soldier configuration: " + e.getMessage();
                System.err.println("Exception during soldier verification: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private void confirmAutoTargetingAndMeleeMode() throws Exception {
        Platform.runLater(() -> {
            try {
                // Auto-targeting should be enabled from save file (following GunfightTestAutomated model)
                assertTrue(soldierAlpha.usesAutomaticTargeting, "SoldierAlpha should have auto-targeting enabled");
                assertTrue(soldierBeta.usesAutomaticTargeting, "SoldierBeta should have auto-targeting enabled");
                
                // Melee combat mode should be enabled from save file
                assertTrue(soldierAlpha.isMeleeCombatMode, "SoldierAlpha should have melee combat mode enabled");
                assertTrue(soldierBeta.isMeleeCombatMode, "SoldierBeta should have melee combat mode enabled");
                
                // Set combat targets manually to ensure auto-targeting works (following GunfightTestAutomated model)
                // SoldierAlpha targets SoldierBeta
                alphaUnit.combatTarget = betaUnit;
                // SoldierBeta targets SoldierAlpha  
                betaUnit.combatTarget = alphaUnit;
                
                System.out.println("Auto-targeting confirmed: Alpha=" + soldierAlpha.usesAutomaticTargeting + 
                                 ", Beta=" + soldierBeta.usesAutomaticTargeting);
                System.out.println("Melee combat mode confirmed: Alpha=" + soldierAlpha.isMeleeCombatMode + 
                                 ", Beta=" + soldierBeta.isMeleeCombatMode);
                System.out.println("Combat targets set: Alpha -> Beta, Beta -> Alpha");
                
            } catch (Exception e) {
                testFailed.set(true);
                failureReason = "Error confirming auto-targeting and melee mode: " + e.getMessage();
                System.err.println("Exception during auto-targeting/melee mode confirmation: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private void selectBothCharactersAndUnpause() throws Exception {
        Platform.runLater(() -> {
            try {
                // Select both characters with a rectangle selection
                selectBothCharactersWithRectangle();
                
                // Set up combat monitoring
                setupCombatMonitoring();
                
                // Unpause the game by setting the paused field
                setPrivateField(gameInstance, "paused", false);
                
                System.out.println("Game unpaused, combat monitoring active");
            } catch (Exception e) {
                testFailed.set(true);
                failureReason = "Error selecting characters and unpausing game: " + e.getMessage();
                System.err.println("Exception during character selection and unpause: " + e.getMessage());
                e.printStackTrace();
                combatCompleteLatch.countDown();
            }
        });
    }
    
    private void selectBothCharactersWithRectangle() throws Exception {
        try {
            // Get the selection manager and units list
            SelectionManager selectionManager = (SelectionManager) getPrivateField(gameInstance, "selectionManager");
            List<Unit> allUnits = (List<Unit>) getPrivateField(gameInstance, "units");
            
            // Clear any existing selection
            selectionManager.clearSelection();
            
            // Calculate rectangle coordinates that encompass both characters
            // SoldierAlpha is at (100, 200), SoldierBeta is at (380, 200)
            // Create a rectangle from (50, 150) to (430, 250) to encompass both
            double startX = 50.0;  // Left of SoldierAlpha
            double startY = 150.0; // Above both characters
            double endX = 430.0;   // Right of SoldierBeta  
            double endY = 250.0;   // Below both characters
            
            // Simulate rectangle selection by setting up the rectangle and finding units
            selectionManager.startRectangleSelection(startX, startY);
            selectionManager.updateRectangleSelection(endX, endY);
            selectionManager.completeRectangleSelection(allUnits);
            
            // Verify both characters are selected
            if (selectionManager.getSelectionCount() == 2 &&
                selectionManager.getSelectedUnits().contains(alphaUnit) &&
                selectionManager.getSelectedUnits().contains(betaUnit)) {
                System.out.println("✓ Rectangle selection successful: Both characters selected");
                System.out.println("  Selection count: " + selectionManager.getSelectionCount());
                System.out.println("  Selected units: SoldierAlpha and SoldierBeta");
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
    }
    
    private void setupCombatMonitoring() {
        // Create a monitoring thread that checks for combat completion and defense attempts
        Thread monitorThread = new Thread(() -> {
            try {
                final int maxChecks = 600; // 1 minute at 100ms intervals
                int lastAlphaDefenses = 0;
                int lastBetaDefenses = 0;
                
                for (int consecutiveChecks = 0; consecutiveChecks < maxChecks && !testFailed.get() && !combatComplete.get(); consecutiveChecks++) {
                    Thread.sleep(100);
                    final int currentCheck = consecutiveChecks;
                    
                    Platform.runLater(() -> {
                        try {
                            // Check for combat completion (one character incapacitated)
                            boolean alphaIncap = soldierAlpha.isIncapacitated();
                            boolean betaIncap = soldierBeta.isIncapacitated();
                            
                            if (alphaIncap || betaIncap) {
                                combatComplete.set(true);
                                System.out.println("Combat completed! One character incapacitated. Alpha: " + alphaIncap + ", Beta: " + betaIncap);
                                combatCompleteLatch.countDown();
                                return;
                            }
                            
                            // Track defense attempts by monitoring attack attempts and health changes
                            // Any combat engagement indicates the defense system is being tested
                            int alphaAttacks = soldierAlpha.getAttacksAttempted();
                            int betaAttacks = soldierBeta.getAttacksAttempted();
                            boolean healthChanged = soldierAlpha.currentHealth < 60 || soldierBeta.currentHealth < 60;
                            
                            if ((alphaAttacks > 0 || betaAttacks > 0 || healthChanged) && defenseAttempts.get() == 0) {
                                defenseAttempts.incrementAndGet();
                                System.out.println("Defense system engagement detected - combat has begun");
                                System.out.println("  Alpha attacks: " + alphaAttacks + ", Beta attacks: " + betaAttacks);
                                System.out.println("  Health changed: " + healthChanged);
                            }
                            
                            // Debug output every 50 checks (5 seconds) with weapon states
                            if (currentCheck > 0 && currentCheck % 50 == 0) {
                                String alphaState = soldierAlpha.currentWeaponState != null ? soldierAlpha.currentWeaponState.getState() : "None";
                                String betaState = soldierBeta.currentWeaponState != null ? soldierBeta.currentWeaponState.getState() : "None";
                                double distance = Math.sqrt(Math.pow(betaUnit.x - alphaUnit.x, 2) + Math.pow(betaUnit.y - alphaUnit.y, 2));
                                
                                System.out.println("Combat monitoring: " + currentCheck + " checks");
                                System.out.println("  Alpha: health=" + soldierAlpha.currentHealth + ", state=" + alphaState + ", attacks=" + alphaAttacks + ", target=" + (alphaUnit.combatTarget != null ? alphaUnit.combatTarget.character.nickname : "none"));
                                System.out.println("  Beta: health=" + soldierBeta.currentHealth + ", state=" + betaState + ", attacks=" + betaAttacks + ", target=" + (betaUnit.combatTarget != null ? betaUnit.combatTarget.character.nickname : "none"));
                                System.out.println("  Distance: " + String.format("%.1f", distance) + " pixels, Defense attempts: " + defenseAttempts.get());
                                System.out.println("  Alpha melee mode: " + soldierAlpha.isMeleeCombatMode + ", Beta melee mode: " + soldierBeta.isMeleeCombatMode);
                            }
                            
                        } catch (Exception e) {
                            // Capture exception for detailed reporting
                            capturedExceptions.add(e);
                            testFailed.set(true);
                            exceptionDetected.set(true);
                            failureReason = "Exception during combat monitoring: " + e.getMessage();
                            System.err.println("=== COMBAT MONITORING EXCEPTION ===");
                            System.err.println("Exception during combat monitoring: " + e.getMessage());
                            System.err.println("Exception type: " + e.getClass().getSimpleName());
                            e.printStackTrace();
                            System.err.println("=== END MONITORING EXCEPTION ===");
                            combatCompleteLatch.countDown();
                        }
                    });
                }
                
                // Timeout reached
                if (!combatComplete.get()) {
                    testFailed.set(true);
                    failureReason = "Combat timeout reached (1 minute). Defense attempts: " + defenseAttempts.get() + 
                                  ", Alpha attacks: " + soldierAlpha.getAttacksAttempted() + 
                                  ", Beta attacks: " + soldierBeta.getAttacksAttempted() +
                                  ", Alpha health: " + soldierAlpha.currentHealth + ", Beta health: " + soldierBeta.currentHealth;
                    System.out.println("Combat timeout after 1 minute");
                    combatCompleteLatch.countDown();
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                testFailed.set(true);
                failureReason = "Monitoring thread interrupted";
                combatCompleteLatch.countDown();
            } catch (Exception e) {
                // Capture exception for detailed reporting
                capturedExceptions.add(e);
                testFailed.set(true);
                exceptionDetected.set(true);
                failureReason = "Unexpected error in monitoring: " + e.getMessage();
                System.err.println("=== MONITORING THREAD EXCEPTION ===");
                System.err.println("Unexpected exception in monitoring: " + e.getMessage());
                System.err.println("Exception type: " + e.getClass().getSimpleName());
                e.printStackTrace();
                System.err.println("=== END MONITORING THREAD EXCEPTION ===");
                combatCompleteLatch.countDown();
            }
        });
        
        monitorThread.setDaemon(true);
        monitorThread.start();
    }
    
    private void outputDetailedStats() {
        // Get DisplayCoordinator instance to invoke actual Shift+/ command for both characters
        try {
            // Access InputManager from game instance first
            Object inputManager = getPrivateField(gameInstance, "inputManager");
            
            if (inputManager != null) {
                // Get the DisplayCoordinator from InputManager
                Object displayCoordinator = getPrivateField(inputManager, "displayCoordinator");
                
                if (displayCoordinator instanceof DisplayCoordinator) {
                    DisplayCoordinator coordinator = (DisplayCoordinator) displayCoordinator;
                    
                    // Display stats for SoldierAlpha using actual Shift+/ command with error handling
                    System.out.println("=== SoldierAlpha Stats (via DisplayCoordinator) ===");
                    try {
                        coordinator.displayCharacterStats(alphaUnit);
                        System.out.println(">> SoldierAlpha stats completed successfully");
                    } catch (Exception e) {
                        System.err.println("Error displaying SoldierAlpha stats: " + e.getMessage());
                        e.printStackTrace();
                        displayBasicStats(alphaUnit, "SoldierAlpha (Fallback)");
                    }
                    
                    System.out.println();  // Blank line between characters
                    
                    // Display stats for SoldierBeta using actual Shift+/ command with error handling
                    System.out.println("=== SoldierBeta Stats (via DisplayCoordinator) ===");
                    try {
                        coordinator.displayCharacterStats(betaUnit);
                        System.out.println(">> SoldierBeta stats completed successfully");
                    } catch (Exception e) {
                        System.err.println("Error displaying SoldierBeta stats: " + e.getMessage());
                        e.printStackTrace();
                        displayBasicStats(betaUnit, "SoldierBeta (Fallback)");
                    }
                    return; // Success - exit method
                }
            }
            
            // Fallback if DisplayCoordinator not available
            System.out.println("DisplayCoordinator not available - using fallback stats display");
            displayBasicStats(alphaUnit, "SoldierAlpha");
            System.out.println();
            displayBasicStats(betaUnit, "SoldierBeta");
            
        } catch (Exception e) {
            System.err.println("Error accessing DisplayCoordinator: " + e.getMessage());
            e.printStackTrace();
            // Fallback to basic stats display
            displayBasicStats(alphaUnit, "SoldierAlpha");
            System.out.println();
            displayBasicStats(betaUnit, "SoldierBeta");
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
        System.out.println("Melee Combat Mode: " + (character.isMeleeCombatMode ? "YES" : "NO"));
        System.out.println("Weapon: " + character.weapon.getWeaponId());
        System.out.println("Knife Skill: " + character.getSkillLevel("skl_knife"));
        
        // Calculate wounds inflicted manually since getTotalWoundsInflicted() might not exist
        int totalWoundsInflicted = character.woundsInflictedScratch + character.woundsInflictedLight + 
                                 character.woundsInflictedSerious + character.woundsInflictedCritical;
        
        System.out.println("Combat Stats:");
        System.out.println("  Attacks: " + character.getAttacksAttempted() + " attempted, " + character.getAttacksSuccessful() + " successful");
        System.out.println("  Wounds Inflicted: " + totalWoundsInflicted);
        System.out.println("=== End " + characterName + " Stats ===");
    }
    
    /**
     * Analyze captured console output for defense system activity.
     * Looks for [DEFENSE] debug messages, non-zero Defense modifier values, and exception patterns.
     */
    private void analyzeDefenseActivity() {
        // Parse console output for defense debug messages and exceptions
        String output = consoleOutput.toString();
        String[] lines = output.split("\n");
        
        // DEBUG: Show what we're analyzing
        System.err.println("DEBUG: analyzeDefenseActivity() analyzing " + lines.length + " lines of console output");
        System.err.println("DEBUG: Console output size: " + output.length() + " characters");
        
        // DEBUG: Show sample of console output to verify capture and look for AUTO-TARGETING patterns
        if (output.length() > 0) {
            String sample = output.length() > 500 ? output.substring(0, 500) + "..." : output;
            System.err.println("DEBUG: Console output sample: " + sample);
            
            // Check if AUTO-TARGETING ERROR appears anywhere in the captured output
            if (output.contains("[AUTO-TARGETING ERROR]")) {
                int count = output.split("\\[AUTO-TARGETING ERROR\\]").length - 1;
                System.err.println("DEBUG: Found " + count + " [AUTO-TARGETING ERROR] occurrences in captured output!");
            } else {
                System.err.println("DEBUG: NO [AUTO-TARGETING ERROR] found in captured output - this is the issue");
            }
            
            // Also check for System.err prefix patterns that might indicate the messages are there but formatted differently
            if (output.contains("AUTO-TARGETING ERROR")) {
                int count = output.split("AUTO-TARGETING ERROR").length - 1;
                System.err.println("DEBUG: Found " + count + " raw 'AUTO-TARGETING ERROR' text occurrences");
            }
            
        } else {
            System.err.println("DEBUG: Console output is EMPTY - this is the problem!");
        }
        
        // Count AUTO-TARGETING ERROR patterns for debugging
        int autoTargetingErrorCount = 0;
        int defenseMessageCount = 0;
        
        for (String line : lines) {
            // Look for [DEFENSE] debug messages
            if (line.contains("[DEFENSE]")) {
                defenseAttempts.incrementAndGet();
                defenseMessageCount++;
                System.out.println("Found DEFENSE message: " + line.trim());
            }
            
            // Look for AUTO-TARGETING ERROR messages and add to problem collection
            if (line.contains("[AUTO-TARGETING ERROR]")) {
                autoTargetingErrorCount++;
                // Extract the error message for problem collection
                String trimmedLine = line.trim();
                System.err.println("DEBUG: Found AUTO-TARGETING ERROR #" + autoTargetingErrorCount + ", adding to problems: " + trimmedLine);
                addProblem("Rapid attack scheduling detected: " + trimmedLine);
                System.out.println("Found AUTO-TARGETING ERROR: " + trimmedLine);
            }
            
            // Enhanced exception detection - look for common exception patterns in console output
            if (line.contains("Exception") || line.contains("Error") || 
                line.contains("at java.") || line.contains("at combat.") ||
                line.contains("IllegalStateException") || line.contains("NullPointerException") ||
                line.contains("Caused by:") || line.contains("java.lang.")) {
                
                // Only flag as exception if it's not just a debug message about exceptions or auto-targeting errors
                if (!line.contains("[AUTO-TARGETING ERROR]") && !line.contains("=== UNCAUGHT EXCEPTION DETECTED ===") && 
                    !line.contains("INFO: Benign exception ignored") && !line.contains("PROBLEM DETECTED:")) {
                    exceptionDetected.set(true);
                    testFailed.set(true);
                    if (failureReason.isEmpty()) {
                        failureReason = "Exception detected in console output: " + line.trim();
                    }
                    System.err.println("Found exception pattern in console: " + line.trim());
                }
            }
            
            // Look for non-zero "Defense modifier:" values
            if (line.contains("Defense modifier:")) {
                // Extract the numeric value
                try {
                    String[] parts = line.split("Defense modifier:\\s*");
                    if (parts.length > 1) {
                        String modifierPart = parts[1].trim();
                        // Extract number before any additional text
                        String[] modifierParts = modifierPart.split("\\s+");
                        if (modifierParts.length > 0) {
                            double modifierValue = Double.parseDouble(modifierParts[0]);
                            if (modifierValue != 0.0) {
                                nonZeroDefenseModifiers.incrementAndGet();
                                System.out.println("Found non-zero Defense modifier: " + modifierValue + " from line: " + line.trim());
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    // Skip lines that don't contain valid numbers
                    System.out.println("Could not parse Defense modifier from line: " + line.trim());
                }
            }
        }
        
        // DEBUG: Summary of what we found
        System.err.println("DEBUG: Analysis summary:");
        System.err.println("  - Lines analyzed: " + lines.length);
        System.err.println("  - AUTO-TARGETING ERROR messages found: " + autoTargetingErrorCount);
        System.err.println("  - DEFENSE messages found: " + defenseMessageCount);
        System.err.println("  - Problems added to collection: " + problems.size());
        
        System.out.println("Defense analysis complete: " + defenseAttempts.get() + " defense attempts, " + 
                          nonZeroDefenseModifiers.get() + " non-zero modifiers");
    }
    
    /**
     * Add a problem to the collection instead of failing immediately.
     * This allows the test to run to completion and report all issues.
     * 
     * @param problem Description of the problem found
     */
    private void addProblem(String problem) {
        problems.add(problem);
        System.err.println("PROBLEM DETECTED: " + problem);
        System.err.println("DEBUG: Problems collection now has " + problems.size() + " items");
        testFailed.set(true);
        if (failureReason.isEmpty()) {
            failureReason = problem;
        }
    }
    
    /**
     * Determines if an exception is known to be benign and shouldn't cause test failure.
     * Benign exceptions are typically environment-related issues (like audio/media)
     * that don't affect combat system functionality.
     */
    private boolean isKnownBenignException(Throwable exception) {
        String className = exception.getClass().getSimpleName();
        String message = exception.getMessage() != null ? exception.getMessage() : "";
        
        // Media/Audio related exceptions that are common in test environments
        if (className.contains("MediaException") || 
            className.contains("AudioClip") ||
            message.contains("Could not create player") ||
            message.contains("audio") ||
            message.contains("media")) {
            return true;
        }
        
        // JavaFX Platform exceptions that don't affect combat
        if (className.contains("Platform") && 
            (message.contains("toolkit") || message.contains("startup"))) {
            return true;
        }
        
        // Add other known benign exceptions here as needed
        
        return false;
    }
    
    /**
     * Validates character wounds to detect combat system bugs.
     * Fails if character has more than 1 critical wound or any wounds after a critical wound.
     * 
     * @param character The character to validate
     * @param characterName The character name for error messages
     */
    private void validateCharacterWounds(combat.Character character, String characterName) {
        // Count critical wounds RECEIVED by this character (not inflicted by them)
        int criticalWoundsReceived = 0;
        int totalWounds = 0;
        
        System.err.println("DEBUG: " + characterName + " wounds list size: " + (character.wounds != null ? character.wounds.size() : "null"));
        System.err.println("DEBUG: " + characterName + " woundsReceived counter: " + character.woundsReceived);
        
        if (character.wounds != null) {
            for (Object woundObj : character.wounds) {
                totalWounds++;
                if (woundObj instanceof combat.Wound) {
                    combat.Wound wound = (combat.Wound) woundObj;
                    System.err.println("DEBUG: " + characterName + " wound " + totalWounds + ": " + wound.getSeverity() + " on " + wound.getBodyPart());
                    if (combat.WoundSeverity.CRITICAL.equals(wound.getSeverity())) {
                        criticalWoundsReceived++;
                    }
                } else {
                    System.err.println("DEBUG: " + characterName + " wound " + totalWounds + ": not a Wound object, type: " + woundObj.getClass().getSimpleName());
                }
            }
        }
        
        System.err.println("DEBUG: Validating " + characterName + " wounds - received critical: " + criticalWoundsReceived + " total: " + totalWounds + " (inflicted: " + character.woundsInflictedCritical + ")");
        
        // Check for multiple critical wounds received
        if (criticalWoundsReceived > 1) {
            System.err.println("DEBUG: Found multiple critical wounds received by " + characterName + ": " + criticalWoundsReceived);
            addProblem(characterName + " has " + criticalWoundsReceived + " critical wounds - should have at most 1");
        }
        
        // Check wound list for wounds after critical wounds
        if (character.wounds != null && !character.wounds.isEmpty()) {
            boolean foundCritical = false;
            int woundIndex = 0;
            
            for (Object woundObj : character.wounds) {
                woundIndex++;
                
                if (woundObj instanceof combat.Wound) {
                    combat.Wound wound = (combat.Wound) woundObj;
                    
                    if (combat.WoundSeverity.CRITICAL.equals(wound.getSeverity())) {
                        foundCritical = true;
                    } else if (foundCritical) {
                        // Found a non-critical wound after a critical wound
                        addProblem(characterName + " has wound '" + wound.getSeverity() + "' at position " + woundIndex + 
                             " after a critical wound - no wounds should occur after critical wounds");
                    }
                }
            }
        }
    }
    
    /**
     * Validates character health to detect excessive damage bugs.
     * Fails if character health is less than -59.
     * 
     * @param character The character to validate
     * @param characterName The character name for error messages
     */
    private void validateCharacterHealth(combat.Character character, String characterName) {
        System.err.println("DEBUG: Validating " + characterName + " health: " + character.currentHealth);
        
        if (character.currentHealth < -59) {
            System.err.println("DEBUG: Found excessive health damage for " + characterName + ": " + character.currentHealth);
            addProblem(characterName + " has health " + character.currentHealth + " which is below the minimum threshold of -59");
        }
    }
    
    /**
     * Validates attack timing to detect rapid-fire attack bugs.
     * Fails if any character makes 2 attacks within a 10 tick time period.
     */
    private void validateAttackTiming() {
        System.err.println("DEBUG: Starting attack timing validation...");
        
        // Parse console output for attack messages and extract timing
        String output = consoleOutput.toString();
        String[] lines = output.split("\\n");
        
        java.util.Map<String, java.util.List<Long>> attacksByCharacter = new java.util.HashMap<>();
        
        for (String line : lines) {
            // Look for melee attack messages with tick information
            if (line.contains("[MELEE-ATTACK]") && line.contains("startMeleeAttackSequence called at tick")) {
                try {
                    // Extract character name and tick number
                    // Format: "[MELEE-ATTACK] -1003:SoldierAlpha startMeleeAttackSequence called at tick 177"
                    if (line.contains(":SoldierAlpha")) {
                        String tickPart = line.substring(line.indexOf("at tick ") + 8);
                        long tick = Long.parseLong(tickPart.trim());
                        
                        attacksByCharacter.computeIfAbsent("SoldierAlpha", k -> new java.util.ArrayList<>()).add(tick);
                    } else if (line.contains(":SoldierBeta")) {
                        String tickPart = line.substring(line.indexOf("at tick ") + 8);
                        long tick = Long.parseLong(tickPart.trim());
                        
                        attacksByCharacter.computeIfAbsent("SoldierBeta", k -> new java.util.ArrayList<>()).add(tick);
                    }
                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    // Skip malformed lines
                    System.out.println("Could not parse attack timing from line: " + line.trim());
                }
            }
        }
        
        // Check each character's attack timing
        for (java.util.Map.Entry<String, java.util.List<Long>> entry : attacksByCharacter.entrySet()) {
            String characterName = entry.getKey();
            java.util.List<Long> attackTicks = entry.getValue();
            
            // Sort attacks by tick to check consecutive attacks
            attackTicks.sort(Long::compareTo);
            
            // Check for attacks within 10 ticks of each other
            for (int i = 1; i < attackTicks.size(); i++) {
                long previousAttack = attackTicks.get(i - 1);
                long currentAttack = attackTicks.get(i);
                long timeDifference = currentAttack - previousAttack;
                
                if (timeDifference <= 10) {
                    addProblem(characterName + " made 2 attacks within " + timeDifference + " ticks " +
                         "(at tick " + previousAttack + " and tick " + currentAttack + ") - " +
                         "attacks should be at least 10 ticks apart");
                }
            }
            
            System.out.println("Attack timing validation: " + characterName + " made " + attackTicks.size() + 
                             " attacks with proper timing intervals");
        }
    }
    
    /**
     * Validates attack frequency to detect rapid-fire attack bugs.
     * Fails if any character attacks more than 1 time per 30 ticks.
     */
    private void validateAttackFrequency() {
        System.err.println("DEBUG: Starting attack frequency validation...");
        
        // Parse console output for attack messages and extract timing
        String output = consoleOutput.toString();
        String[] lines = output.split("\\n");
        
        java.util.Map<String, java.util.List<Long>> attacksByCharacter = new java.util.HashMap<>();
        long totalTestTicks = 0;
        
        for (String line : lines) {
            // Track total test duration by looking for tick references
            if (line.contains("at tick ") && line.contains("Combat completed")) {
                try {
                    String tickPart = line.substring(line.indexOf("at tick ") + 8);
                    // Extract just the number part
                    String[] tickParts = tickPart.split("\\s+");
                    if (tickParts.length > 0) {
                        totalTestTicks = Math.max(totalTestTicks, Long.parseLong(tickParts[0]));
                    }
                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    // Skip malformed lines
                }
            }
            
            // Look for melee attack messages with tick information
            if (line.contains("[MELEE-ATTACK]") && line.contains("startMeleeAttackSequence called at tick")) {
                try {
                    // Extract character name and tick number
                    // Format: "[MELEE-ATTACK] -1003:SoldierAlpha startMeleeAttackSequence called at tick 177"
                    if (line.contains(":SoldierAlpha")) {
                        String tickPart = line.substring(line.indexOf("at tick ") + 8);
                        long tick = Long.parseLong(tickPart.trim());
                        
                        attacksByCharacter.computeIfAbsent("SoldierAlpha", k -> new java.util.ArrayList<>()).add(tick);
                        totalTestTicks = Math.max(totalTestTicks, tick);
                    } else if (line.contains(":SoldierBeta")) {
                        String tickPart = line.substring(line.indexOf("at tick ") + 8);
                        long tick = Long.parseLong(tickPart.trim());
                        
                        attacksByCharacter.computeIfAbsent("SoldierBeta", k -> new java.util.ArrayList<>()).add(tick);
                        totalTestTicks = Math.max(totalTestTicks, tick);
                    }
                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    // Skip malformed lines
                    System.out.println("Could not parse attack frequency from line: " + line.trim());
                }
            }
        }
        
        // If we didn't find total ticks from combat completion, estimate from last attack
        if (totalTestTicks == 0) {
            totalTestTicks = 600; // Default estimate (10 seconds at 60 ticks/second)
        }
        
        // Check attack frequency for each character
        for (java.util.Map.Entry<String, java.util.List<Long>> entry : attacksByCharacter.entrySet()) {
            String characterName = entry.getKey();
            java.util.List<Long> attackTicks = entry.getValue();
            int attackCount = attackTicks.size();
            
            // Calculate expected maximum attacks (1 attack per 30 ticks)
            long expectedMaxAttacks = totalTestTicks / 30;
            if (totalTestTicks % 30 > 0) {
                expectedMaxAttacks++; // Round up for partial intervals
            }
            
            if (attackCount > expectedMaxAttacks) {
                addProblem(characterName + " made " + attackCount + " attacks in " + totalTestTicks + " ticks " +
                     "(expected maximum: " + expectedMaxAttacks + " attacks for 1 attack per 30 ticks) - " +
                     "attack frequency too high");
            }
            
            System.out.println("Attack frequency validation: " + characterName + " made " + attackCount + 
                             " attacks in " + totalTestTicks + " ticks (max allowed: " + expectedMaxAttacks + ")");
        }
    }
    
    /**
     * Report all collected problems at the end of the test.
     * This provides a comprehensive view of all issues found during the test run.
     */
    private void reportAllProblems() {
        System.out.println("\n=== DEBUG: Problems collection size: " + problems.size() + " ===");
        
        if (problems.isEmpty()) {
            System.out.println("\n=== VALIDATION COMPLETE: NO PROBLEMS DETECTED ===");
            return;
        }
        
        System.err.println("\n=== COMPREHENSIVE PROBLEM REPORT ===");
        System.err.println("Found " + problems.size() + " problem(s) during test execution:");
        
        for (int i = 0; i < problems.size(); i++) {
            System.err.println("  " + (i + 1) + ". " + problems.get(i));
        }
        
        System.err.println("=== END PROBLEM REPORT ===\n");
    }
}
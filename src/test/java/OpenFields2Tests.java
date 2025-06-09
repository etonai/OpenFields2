import org.junit.jupiter.api.Test;
import java.util.List;
import game.Unit;
import combat.Character;
import javafx.scene.paint.Color;

import static org.junit.jupiter.api.Assertions.*;

public class OpenFields2Tests {

    @Test
    void testPixelsToFeet() {
        assertEquals(1.0, OpenFields2.pixelsToFeet(7.0), "Expected 1.0 Feet");
    }
    
    @Test
    void testStatToModifier() {
        // Test key boundary values
        assertEquals(0, OpenFields2.statToModifier(50), "Stat 50 should give +0 modifier");
        assertEquals(0, OpenFields2.statToModifier(51), "Stat 51 should give +0 modifier");
        assertEquals(-20, OpenFields2.statToModifier(1), "Stat 1 should give -20 modifier");
        assertEquals(20, OpenFields2.statToModifier(100), "Stat 100 should give +20 modifier");
        
        // Test some mid-range values
        assertTrue(OpenFields2.statToModifier(75) > 0, "Stat 75 should give positive modifier");
        assertTrue(OpenFields2.statToModifier(25) < 0, "Stat 25 should give negative modifier");
        
        // Test that higher stats give higher modifiers
        assertTrue(OpenFields2.statToModifier(80) > OpenFields2.statToModifier(70), 
                  "Higher stats should give higher modifiers");
        assertTrue(OpenFields2.statToModifier(30) > OpenFields2.statToModifier(20), 
                  "Higher stats should give higher modifiers, even in negative range");
        
        // Test edge cases - values outside range should be clamped
        assertEquals(-20, OpenFields2.statToModifier(0), "Stat 0 should be clamped to -20");
        assertEquals(20, OpenFields2.statToModifier(101), "Stat 101 should be clamped to +20");
    }
    
    @Test
    void testStatToModifierBellCurve() {
        // Test that the function produces a bell curve distribution
        // Values should be more extreme at the ends
        System.out.println("Testing stat-to-modifier distribution:");
        
        // Collect all possible modifier values and print each calculation
        java.util.Set<Integer> foundModifiers = new java.util.HashSet<>();
        
        for (int stat = 1; stat <= 100; stat++) {
            int modifier = OpenFields2.statToModifier(stat);
            foundModifiers.add(modifier);
            System.out.println("Stat " + stat + " -> Modifier " + modifier);
            
            // Ensure modifiers are within expected range
            assertTrue(modifier >= -20 && modifier <= 20, 
                      "Modifier should be between -20 and +20, got " + modifier + " for stat " + stat);
        }
        
        // Check that all integers from -20 to +20 are possible
        System.out.println("\nFound modifiers: " + foundModifiers);
        for (int i = -20; i <= 20; i++) {
            assertTrue(foundModifiers.contains(i), 
                      "Modifier " + i + " should be possible but was not found");
        }
        
        System.out.println("✅ All modifiers from -20 to +20 are represented!");
        
        // Verify monotonic property - each modifier should be >= previous
        System.out.println("\nVerifying monotonic property:");
        for (int stat = 2; stat <= 100; stat++) {
            int currentModifier = OpenFields2.statToModifier(stat);
            int previousModifier = OpenFields2.statToModifier(stat - 1);
            assertTrue(currentModifier >= previousModifier, 
                      "Monotonic property violated: stat " + (stat-1) + " gives " + previousModifier + 
                      " but stat " + stat + " gives " + currentModifier);
            assertTrue(currentModifier <= previousModifier + 1,
                      "Modifier increased by more than 1: stat " + (stat-1) + " gives " + previousModifier +
                      " but stat " + stat + " gives " + currentModifier);
        }
        System.out.println("✅ Monotonic property verified: each stat gives equal or +1 modifier vs previous!");
    }
    
    @Test
    void testStatToModifierSpecificRules() {
        System.out.println("Testing stat-to-modifier with specific rules:");
        
        // Test every stat from 1-100 and collect results
        java.util.Set<Integer> foundModifiers = new java.util.HashSet<>();
        
        for (int stat = 1; stat <= 100; stat++) {
            int modifier = OpenFields2.statToModifier(stat);
            foundModifiers.add(modifier);
            System.out.println("Stat " + stat + " -> Modifier " + modifier);
        }
        
        // Rule 1: Specific boundary values
        assertEquals(-20, OpenFields2.statToModifier(1), "Stat 1 should give -20");
        assertEquals(-19, OpenFields2.statToModifier(2), "Stat 2 should give -19");
        assertEquals(-18, OpenFields2.statToModifier(3), "Stat 3 should give -18");
        assertEquals(-17, OpenFields2.statToModifier(4), "Stat 4 should give -17");
        assertEquals(-16, OpenFields2.statToModifier(5), "Stat 5 should give -16");
        assertEquals(-15, OpenFields2.statToModifier(6), "Stat 6 should give -15");
        assertEquals(0, OpenFields2.statToModifier(50), "Stat 50 should give 0");
        assertEquals(0, OpenFields2.statToModifier(51), "Stat 51 should give 0");
        assertEquals(15, OpenFields2.statToModifier(95), "Stat 95 should give 15");
        assertEquals(16, OpenFields2.statToModifier(96), "Stat 96 should give 16");
        assertEquals(17, OpenFields2.statToModifier(97), "Stat 97 should give 17");
        assertEquals(18, OpenFields2.statToModifier(98), "Stat 98 should give 18");
        assertEquals(19, OpenFields2.statToModifier(99), "Stat 99 should give 19");
        assertEquals(20, OpenFields2.statToModifier(100), "Stat 100 should give 20");
        
        // Rule 2: Single digit modifiers for stats 21-80
        for (int stat = 21; stat <= 80; stat++) {
            int modifier = OpenFields2.statToModifier(stat);
            assertTrue(modifier >= -9 && modifier <= 9, 
                      "Stat " + stat + " should have single digit modifier, got " + modifier);
        }
        
        // Rule 3: Monotonic property (equal or +1)
        for (int stat = 2; stat <= 100; stat++) {
            int currentModifier = OpenFields2.statToModifier(stat);
            int previousModifier = OpenFields2.statToModifier(stat - 1);
            assertTrue(currentModifier >= previousModifier, 
                      "Monotonic violated: stat " + (stat-1) + " gives " + previousModifier + 
                      " but stat " + stat + " gives " + currentModifier);
            assertTrue(currentModifier <= previousModifier + 1,
                      "Increase > 1: stat " + (stat-1) + " gives " + previousModifier +
                      " but stat " + stat + " gives " + currentModifier);
        }
        
        // Rule 4: All modifiers from -20 to +20 should be possible
        for (int i = -20; i <= 20; i++) {
            assertTrue(foundModifiers.contains(i), 
                      "Modifier " + i + " should be possible but was not found");
        }
        
        System.out.println("✅ All specific rules validated!");
    }
    
    @Test
    void testStatToModifierSymmetry() {
        System.out.println("Testing stat-to-modifier symmetry around 50-51:");
        
        // Test symmetry: for every i, |statToModifier(50-i)| should equal |statToModifier(51+i)|
        // and statToModifier(50-i) should equal -statToModifier(51+i)
        
        for (int i = 0; i <= 49; i++) {
            int leftStat = 50 - i;
            int rightStat = 51 + i;
            
            // Only test if both stats are in valid range
            if (leftStat >= 1 && rightStat <= 100) {
                int leftModifier = OpenFields2.statToModifier(leftStat);
                int rightModifier = OpenFields2.statToModifier(rightStat);
                
                System.out.println("i=" + i + ": stat " + leftStat + " -> " + leftModifier + 
                                 ", stat " + rightStat + " -> " + rightModifier + 
                                 " (should be opposites)");
                
                // They should be opposites (symmetric around 0)
                assertEquals(-leftModifier, rightModifier, 
                           "Symmetry violated: stat " + leftStat + " gives " + leftModifier + 
                           " but stat " + rightStat + " gives " + rightModifier + 
                           " (should be " + (-leftModifier) + ")");
            }
        }
        
        System.out.println("✅ Symmetry property verified!");
    }
    
    @Test
    void testPerpendicularVelocityStationaryUnit() {
        System.out.println("Testing perpendicular velocity calculation for stationary unit:");
        
        // Create two units at different positions
        Character char1 = new Character("Shooter", 100, 50, 50, 50, 50, combat.Handedness.RIGHT_HANDED);
        Character char2 = new Character("Target", 100, 50, 50, 50, 50, combat.Handedness.LEFT_HANDED);
        
        Unit shooter = new Unit(char1, 0, 0, Color.RED, 1);
        Unit target = new Unit(char2, 100, 0, Color.BLUE, 2);
        
        // Target is stationary (no movement target set)
        double perpendicularVelocity = target.getPerpendicularVelocity(shooter);
        
        assertEquals(0.0, perpendicularVelocity, 0.001, 
                    "Stationary unit should have zero perpendicular velocity");
        
        System.out.println("✅ Stationary unit perpendicular velocity test passed!");
    }
    
    @Test
    void testPerpendicularVelocityDirectApproach() {
        System.out.println("Testing perpendicular velocity for direct approach:");
        
        // Create characters with known movement speed
        Character char1 = new Character("Shooter", 100, 50, 50, 50, 50, combat.Handedness.RIGHT_HANDED);
        Character char2 = new Character("Target", 100, 50, 50, 50, 50, combat.Handedness.LEFT_HANDED);
        char2.baseMovementSpeed = 60.0; // 60 pixels per second = 1 pixel per tick
        
        Unit shooter = new Unit(char1, 0, 0, Color.RED, 1);
        Unit target = new Unit(char2, 100, 0, Color.BLUE, 2);
        
        // Target moves directly toward shooter (parallel to line of sight)
        target.setTarget(0, 0);
        
        double perpendicularVelocity = target.getPerpendicularVelocity(shooter);
        
        assertEquals(0.0, perpendicularVelocity, 0.001, 
                    "Direct approach should have zero perpendicular velocity");
        
        System.out.println("✅ Direct approach perpendicular velocity test passed!");
    }
    
    @Test
    void testPerpendicularVelocityDirectRetreat() {
        System.out.println("Testing perpendicular velocity for direct retreat:");
        
        // Create characters with known movement speed
        Character char1 = new Character("Shooter", 100, 50, 50, 50, 50, combat.Handedness.RIGHT_HANDED);
        Character char2 = new Character("Target", 100, 50, 50, 50, 50, combat.Handedness.LEFT_HANDED);
        char2.baseMovementSpeed = 60.0; // 60 pixels per second = 1 pixel per tick
        
        Unit shooter = new Unit(char1, 0, 0, Color.RED, 1);
        Unit target = new Unit(char2, 100, 0, Color.BLUE, 2);
        
        // Target moves directly away from shooter (parallel to line of sight)
        target.setTarget(200, 0);
        
        double perpendicularVelocity = target.getPerpendicularVelocity(shooter);
        
        assertEquals(0.0, perpendicularVelocity, 0.001, 
                    "Direct retreat should have zero perpendicular velocity");
        
        System.out.println("✅ Direct retreat perpendicular velocity test passed!");
    }
    
    @Test
    void testPerpendicularVelocityPurePerpendicular() {
        System.out.println("Testing perpendicular velocity for pure perpendicular movement:");
        
        // Create characters with known movement speed
        Character char1 = new Character("Shooter", 100, 50, 50, 50, 50, combat.Handedness.RIGHT_HANDED);
        Character char2 = new Character("Target", 100, 50, 50, 50, 50, combat.Handedness.LEFT_HANDED);
        char2.baseMovementSpeed = 60.0; // 60 pixels per second = 1 pixel per tick
        
        Unit shooter = new Unit(char1, 0, 0, Color.RED, 1);
        Unit target = new Unit(char2, 100, 0, Color.BLUE, 2);
        
        // Target moves perpendicular to line of sight (up)
        target.setTarget(100, 100);
        
        double perpendicularVelocity = target.getPerpendicularVelocity(shooter);
        
        // Should be approximately 1.0 (the full movement speed)
        assertEquals(1.0, perpendicularVelocity, 0.001, 
                    "Pure perpendicular movement should equal movement speed");
        
        System.out.println("✅ Pure perpendicular velocity test passed!");
    }
    
    @Test
    void testPerpendicularVelocityDiagonalMovement() {
        System.out.println("Testing perpendicular velocity for diagonal movement:");
        
        // Create characters with known movement speed
        Character char1 = new Character("Shooter", 100, 50, 50, 50, 50, combat.Handedness.RIGHT_HANDED);
        Character char2 = new Character("Target", 100, 50, 50, 50, 50, combat.Handedness.LEFT_HANDED);
        char2.baseMovementSpeed = 60.0; // 60 pixels per second = 1 pixel per tick
        
        Unit shooter = new Unit(char1, 0, 0, Color.RED, 1);
        Unit target = new Unit(char2, 100, 0, Color.BLUE, 2);
        
        // Target moves diagonally (45 degrees from line of sight)
        // This creates a movement vector that is 45 degrees from horizontal
        target.setTarget(200, 100);
        
        double perpendicularVelocity = target.getPerpendicularVelocity(shooter);
        
        // For 45-degree movement, perpendicular component should be sin(45°) * speed
        // sin(45°) ≈ 0.707, so perpendicular velocity should be about 0.707
        assertEquals(0.707, perpendicularVelocity, 0.01, 
                    "45-degree movement should have perpendicular component of ~0.707");
        
        System.out.println("✅ Diagonal movement perpendicular velocity test passed!");
    }
    
    @Test
    void testPerpendicularVelocityVectorMath() {
        System.out.println("Testing perpendicular velocity vector mathematics:");
        
        // Create characters with known movement speed
        Character char1 = new Character("Shooter", 100, 50, 50, 50, 50, combat.Handedness.RIGHT_HANDED);
        Character char2 = new Character("Target", 100, 50, 50, 50, 50, combat.Handedness.LEFT_HANDED);
        char2.baseMovementSpeed = 60.0; // 60 pixels per second = 1 pixel per tick
        
        Unit shooter = new Unit(char1, 0, 0, Color.RED, 1);
        Unit target = new Unit(char2, 100, 0, Color.BLUE, 2);
        
        // Test that perpendicular^2 + parallel^2 = total^2 (Pythagorean theorem)
        target.setTarget(150, 50); // Some arbitrary movement
        
        double[] velocity = target.getVelocityVector();
        double totalVelocity = Math.sqrt(velocity[0] * velocity[0] + velocity[1] * velocity[1]);
        
        double perpendicularVelocity = target.getPerpendicularVelocity(shooter);
        
        // Calculate parallel component
        double losX = target.x - shooter.x;
        double losY = target.y - shooter.y;
        double losDistance = Math.sqrt(losX * losX + losY * losY);
        double losUnitX = losX / losDistance;
        double losUnitY = losY / losDistance;
        double parallelComponent = velocity[0] * losUnitX + velocity[1] * losUnitY;
        
        // Verify Pythagorean theorem
        double calculatedTotal = Math.sqrt(perpendicularVelocity * perpendicularVelocity + 
                                         parallelComponent * parallelComponent);
        
        assertEquals(totalVelocity, calculatedTotal, 0.001, 
                    "Vector decomposition should satisfy Pythagorean theorem");
        
        System.out.println("Total velocity: " + String.format("%.3f", totalVelocity));
        System.out.println("Perpendicular component: " + String.format("%.3f", perpendicularVelocity));
        System.out.println("Parallel component: " + String.format("%.3f", parallelComponent));
        System.out.println("Calculated total: " + String.format("%.3f", calculatedTotal));
        
        System.out.println("✅ Vector mathematics test passed!");
    }
    
    @Test
    void testVelocityVectorCalculation() {
        System.out.println("Testing velocity vector calculation:");
        
        // Create character with known movement speed
        Character character = new Character("Test", 100, 50, 50, 50, 50, combat.Handedness.RIGHT_HANDED);
        character.baseMovementSpeed = 60.0; // 60 pixels per second = 1 pixel per tick
        
        Unit unit = new Unit(character, 0, 0, Color.RED, 1);
        
        // Test stationary unit
        double[] stationaryVelocity = unit.getVelocityVector();
        assertEquals(0.0, stationaryVelocity[0], 0.001, "Stationary unit should have zero X velocity");
        assertEquals(0.0, stationaryVelocity[1], 0.001, "Stationary unit should have zero Y velocity");
        
        // Test unit moving right (positive X direction)
        unit.setTarget(100, 0);
        double[] rightVelocity = unit.getVelocityVector();
        assertEquals(1.0, rightVelocity[0], 0.001, "Moving right should have +1.0 X velocity");
        assertEquals(0.0, rightVelocity[1], 0.001, "Moving right should have zero Y velocity");
        
        // Test unit moving up (positive Y direction)
        unit.setTarget(0, 100);
        double[] upVelocity = unit.getVelocityVector();
        assertEquals(0.0, upVelocity[0], 0.001, "Moving up should have zero X velocity");
        assertEquals(1.0, upVelocity[1], 0.001, "Moving up should have +1.0 Y velocity");
        
        // Test diagonal movement (should maintain speed = 1.0)
        unit.setTarget(100, 100);
        double[] diagonalVelocity = unit.getVelocityVector();
        double diagonalSpeed = Math.sqrt(diagonalVelocity[0] * diagonalVelocity[0] + 
                                       diagonalVelocity[1] * diagonalVelocity[1]);
        assertEquals(1.0, diagonalSpeed, 0.001, "Diagonal movement should maintain unit speed");
        
        System.out.println("✅ Velocity vector calculation test passed!");
    }
    
    @Test
    void testTargetMovementModifierIntegration() {
        System.out.println("Testing target movement modifier integration:");
        
        // Create test characters
        Character shooter = new Character("Shooter", 100, 70, 50, 50, 50, combat.Handedness.RIGHT_HANDED); // Good dexterity
        Character target = new Character("Target", 100, 50, 50, 50, 50, combat.Handedness.LEFT_HANDED);
        target.baseMovementSpeed = 60.0; // Standard movement speed
        
        Unit shooterUnit = new Unit(shooter, 0, 0, Color.RED, 1);
        Unit targetUnit = new Unit(target, 100, 0, Color.BLUE, 2);
        
        // Test 1: Stationary target - should have no modifier
        double modifierStationary = OpenFields2Tests.calculateTargetMovementModifierPublic(shooterUnit, targetUnit);
        assertEquals(0.0, modifierStationary, 0.001, "Stationary target should have no movement modifier");
        System.out.println("Stationary target modifier: " + modifierStationary);
        
        // Test 2: Target moving directly toward shooter - should have no modifier
        targetUnit.setTarget(0, 0);
        double modifierDirectApproach = OpenFields2Tests.calculateTargetMovementModifierPublic(shooterUnit, targetUnit);
        assertEquals(0.0, modifierDirectApproach, 0.001, "Direct approach should have no movement modifier");
        System.out.println("Direct approach modifier: " + modifierDirectApproach);
        
        // Test 3: Target moving directly away from shooter - should have no modifier
        targetUnit.setTarget(200, 0);
        double modifierDirectRetreat = OpenFields2Tests.calculateTargetMovementModifierPublic(shooterUnit, targetUnit);
        assertEquals(0.0, modifierDirectRetreat, 0.001, "Direct retreat should have no movement modifier");
        System.out.println("Direct retreat modifier: " + modifierDirectRetreat);
        
        // Test 4: Target moving perpendicular to line of sight - should have negative modifier
        // With new formula: -2 * perpendicular speed (~8.6 ft/s) = ~-17 modifier
        targetUnit.setTarget(100, 100);
        double modifierPerpendicular = OpenFields2Tests.calculateTargetMovementModifierPublic(shooterUnit, targetUnit);
        assertTrue(modifierPerpendicular < -15.0, "Perpendicular movement should have significant negative modifier");
        System.out.println("Perpendicular movement modifier: " + modifierPerpendicular);
        
        // Test 5: Target moving at 45 degrees - should have moderate negative modifier
        targetUnit.setTarget(200, 100);
        double modifierDiagonal = OpenFields2Tests.calculateTargetMovementModifierPublic(shooterUnit, targetUnit);
        assertTrue(modifierDiagonal < 0.0, "Diagonal movement should have negative modifier");
        assertTrue(modifierDiagonal > modifierPerpendicular, "Diagonal should be less negative than pure perpendicular");
        System.out.println("Diagonal movement modifier: " + modifierDiagonal);
        
        System.out.println("✅ Target movement modifier integration test passed!");
    }
    
    @Test
    public void testWoundModifierCalculation() {
        System.out.println("Testing wound modifier calculation:");
        
        // Test character with right-handed handedness
        Character rightHanded = new Character("RightHanded", 100, 70, 50, 50, 50, combat.Handedness.RIGHT_HANDED);
        Unit rightHandedUnit = new Unit(rightHanded, 0, 0, Color.RED, 1);
        
        // Test no wounds
        double noWoundsModifier = calculateWoundModifierPublic(rightHandedUnit);
        assertEquals(0.0, noWoundsModifier, 0.001, "No wounds should give 0 modifier");
        System.out.println("No wounds modifier: " + noWoundsModifier);
        
        // Test head wound - every point of damage is -1
        rightHanded.addWound(new combat.Wound(combat.BodyPart.HEAD, combat.WoundSeverity.LIGHT));
        double headLightModifier = calculateWoundModifierPublic(rightHandedUnit);
        assertEquals(-3.0, headLightModifier, 0.001, "Head light wound should be -3 (per damage point)");
        System.out.println("Head light wound modifier: " + headLightModifier);
        
        // Clear wounds and test dominant arm (right arm for right-handed)
        rightHanded.getWounds().clear();
        rightHanded.addWound(new combat.Wound(combat.BodyPart.RIGHT_ARM, combat.WoundSeverity.SERIOUS));
        double dominantArmModifier = calculateWoundModifierPublic(rightHandedUnit);
        assertEquals(-8.0, dominantArmModifier, 0.001, "Dominant arm serious wound should be -8 (per damage point)");
        System.out.println("Dominant arm serious wound modifier: " + dominantArmModifier);
        
        // Test non-dominant arm (left arm for right-handed) - follows severity rules
        rightHanded.getWounds().clear();
        rightHanded.addWound(new combat.Wound(combat.BodyPart.LEFT_ARM, combat.WoundSeverity.SERIOUS));
        double nonDominantArmModifier = calculateWoundModifierPublic(rightHandedUnit);
        assertEquals(-2.0, nonDominantArmModifier, 0.001, "Non-dominant arm serious wound should be -2 (severity rule)");
        System.out.println("Non-dominant arm serious wound modifier: " + nonDominantArmModifier);
        
        // Test other body parts with different severities
        rightHanded.getWounds().clear();
        rightHanded.addWound(new combat.Wound(combat.BodyPart.CHEST, combat.WoundSeverity.LIGHT));
        double chestLightModifier = calculateWoundModifierPublic(rightHandedUnit);
        assertEquals(-1.0, chestLightModifier, 0.001, "Chest light wound should be -1");
        System.out.println("Chest light wound modifier: " + chestLightModifier);
        
        rightHanded.getWounds().clear();
        rightHanded.addWound(new combat.Wound(combat.BodyPart.CHEST, combat.WoundSeverity.CRITICAL));
        double chestCriticalModifier = calculateWoundModifierPublic(rightHandedUnit);
        assertEquals(-8.0, chestCriticalModifier, 0.001, "Chest critical wound should be -8 (per damage point)");
        System.out.println("Chest critical wound modifier: " + chestCriticalModifier);
        
        // Test scratch wounds in other parts (should be 0)
        rightHanded.getWounds().clear();
        rightHanded.addWound(new combat.Wound(combat.BodyPart.LEFT_LEG, combat.WoundSeverity.SCRATCH));
        double scratchModifier = calculateWoundModifierPublic(rightHandedUnit);
        assertEquals(0.0, scratchModifier, 0.001, "Scratch wounds in other parts should be 0");
        System.out.println("Leg scratch wound modifier: " + scratchModifier);
        
        System.out.println("✅ Wound modifier calculation test passed!");
    }
    
    @Test
    public void testWoundModifierWithLeftHandedness() {
        System.out.println("Testing wound modifier with left-handedness:");
        
        Character leftHanded = new Character("LeftHanded", 100, 70, 50, 50, 50, combat.Handedness.LEFT_HANDED);
        Unit leftHandedUnit = new Unit(leftHanded, 0, 0, Color.RED, 1);
        
        // Test left arm as dominant arm for left-handed character
        leftHanded.addWound(new combat.Wound(combat.BodyPart.LEFT_ARM, combat.WoundSeverity.LIGHT));
        double leftDominantModifier = calculateWoundModifierPublic(leftHandedUnit);
        assertEquals(-3.0, leftDominantModifier, 0.001, "Left arm should be dominant for left-handed character");
        System.out.println("Left-handed dominant arm light wound modifier: " + leftDominantModifier);
        
        // Test right arm as non-dominant for left-handed character
        leftHanded.getWounds().clear();
        leftHanded.addWound(new combat.Wound(combat.BodyPart.RIGHT_ARM, combat.WoundSeverity.LIGHT));
        double leftNonDominantModifier = calculateWoundModifierPublic(leftHandedUnit);
        assertEquals(-1.0, leftNonDominantModifier, 0.001, "Right arm should be non-dominant for left-handed character");
        System.out.println("Left-handed non-dominant arm light wound modifier: " + leftNonDominantModifier);
        
        System.out.println("✅ Left-handed wound modifier test passed!");
    }
    
    @Test 
    public void testWoundModifierWithAmbidextrous() {
        System.out.println("Testing wound modifier with ambidextrous handedness:");
        
        Character ambidextrous = new Character("Ambidextrous", 100, 70, 50, 50, 50, combat.Handedness.AMBIDEXTROUS);
        Unit ambidextrousUnit = new Unit(ambidextrous, 0, 0, Color.RED, 1);
        
        // Test right arm as dominant arm for ambidextrous character (per spec)
        ambidextrous.addWound(new combat.Wound(combat.BodyPart.RIGHT_ARM, combat.WoundSeverity.LIGHT));
        double rightDominantModifier = calculateWoundModifierPublic(ambidextrousUnit);
        assertEquals(-3.0, rightDominantModifier, 0.001, "Right arm should be dominant for ambidextrous character");
        System.out.println("Ambidextrous dominant (right) arm light wound modifier: " + rightDominantModifier);
        
        // Test left arm as non-dominant for ambidextrous character
        ambidextrous.getWounds().clear();
        ambidextrous.addWound(new combat.Wound(combat.BodyPart.LEFT_ARM, combat.WoundSeverity.LIGHT));
        double leftNonDominantModifier = calculateWoundModifierPublic(ambidextrousUnit);
        assertEquals(-1.0, leftNonDominantModifier, 0.001, "Left arm should be non-dominant for ambidextrous character");
        System.out.println("Ambidextrous non-dominant (left) arm light wound modifier: " + leftNonDominantModifier);
        
        System.out.println("✅ Ambidextrous wound modifier test passed!");
    }
    
    @Test
    public void testMultipleWoundsModifier() {
        System.out.println("Testing multiple wounds modifier calculation:");
        
        Character character = new Character("MultiWounded", 100, 70, 50, 50, 50, combat.Handedness.RIGHT_HANDED);
        Unit unit = new Unit(character, 0, 0, Color.RED, 1);
        
        // Add multiple wounds
        character.addWound(new combat.Wound(combat.BodyPart.HEAD, combat.WoundSeverity.LIGHT)); // -3
        character.addWound(new combat.Wound(combat.BodyPart.RIGHT_ARM, combat.WoundSeverity.SCRATCH)); // -1 (dominant arm)
        character.addWound(new combat.Wound(combat.BodyPart.LEFT_LEG, combat.WoundSeverity.SERIOUS)); // -2
        character.addWound(new combat.Wound(combat.BodyPart.CHEST, combat.WoundSeverity.SCRATCH)); // 0 (scratch in other part)
        
        double multipleWoundsModifier = calculateWoundModifierPublic(unit);
        double expected = -3.0 + -1.0 + -2.0 + 0.0; // -6.0
        assertEquals(expected, multipleWoundsModifier, 0.001, "Multiple wounds should accumulate");
        System.out.println("Multiple wounds modifier: " + multipleWoundsModifier + " (expected: " + expected + ")");
        
        System.out.println("✅ Multiple wounds modifier test passed!");
    }
    
    // Helper method to access the private calculateWoundModifier method for testing
    private static double calculateWoundModifierPublic(Unit shooter) {
        try {
            java.lang.reflect.Method method = OpenFields2.class.getDeclaredMethod("calculateWoundModifier", Unit.class);
            method.setAccessible(true);
            return (Double) method.invoke(null, shooter);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke calculateWoundModifier", e);
        }
    }
    
    // Helper method to access the private calculateTargetMovementModifier method for testing
    private static double calculateTargetMovementModifierPublic(Unit shooter, Unit target) {
        try {
            java.lang.reflect.Method method = OpenFields2.class.getDeclaredMethod("calculateTargetMovementModifier", Unit.class, Unit.class);
            method.setAccessible(true);
            return (Double) method.invoke(null, shooter, target);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke calculateTargetMovementModifier", e);
        }
    }
}

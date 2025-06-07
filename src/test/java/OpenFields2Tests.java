import org.junit.jupiter.api.Test;
import java.util.List;

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
}

/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */
package utils;

import java.util.Calendar;
import java.util.Date;

public final class GameConstants {
    
    // Window dimensions
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    
    // Movement constants
    public static final double MOVE_SPEED = 42.0;
    
    // Combat constants
    public static final int STRESS_MODIFIER = -20;
    
    // Conversion utilities
    public static double pixelsToFeet(double pixels) {
        return pixels / 7.0;
    }
    
    public static Date createDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day); // Calendar months are 0-based
        return cal.getTime();
    }
    
    public static int statToModifier(int stat) {
        // Clamp stat to valid range
        stat = Math.max(1, Math.min(100, stat));
        
        // Balanced requirements for symmetric distribution:
        // 1. Perfect symmetry around 50-51: statToModifier(50-i) = -statToModifier(51+i)
        // 2. Monotonic: each stat >= previous, increase by at most 1
        // 3. Extremes: 1→-20, 100→+20
        // 4. Center: 50→0, 51→0
        // 5. Close approximation to 1-6: -20 to -15 and 95-100: 15-20
        // 6. Single digits for 21-80 range
        // 7. All integers -20 to +20 possible
        
        // Use a lookup table for perfect control over the distribution
        // This ensures both symmetry and the specific boundary approximations
        int[] modifiers = new int[101]; // index 0 unused, 1-100 are valid stats
        
        // Define the negative half (1-50), then mirror for positive half (51-100)
        modifiers[1] = -20;   // Boundary requirement: 1 → -20
        modifiers[2] = -19;   // Boundary requirement: 2 → -19
        modifiers[3] = -18;   // Boundary requirement: 3 → -18
        modifiers[4] = -17;   // Boundary requirement: 4 → -17
        modifiers[5] = -16;   // Boundary requirement: 5 → -16
        modifiers[6] = -15;   // Boundary requirement: 6 → -15
        modifiers[7] = -14;
        modifiers[8] = -14;
        modifiers[9] = -13;
        modifiers[10] = -13;
        modifiers[11] = -12;
        modifiers[12] = -12;
        modifiers[13] = -11;
        modifiers[14] = -11;
        modifiers[15] = -10;
        modifiers[16] = -10;
        modifiers[17] = -9;
        modifiers[18] = -9;
        modifiers[19] = -8;
        modifiers[20] = -8;
        modifiers[21] = -7;   // Single digit starts here
        modifiers[22] = -7;
        modifiers[23] = -6;
        modifiers[24] = -6;
        modifiers[25] = -5;
        modifiers[26] = -5;
        modifiers[27] = -5;
        modifiers[28] = -4;
        modifiers[29] = -4;
        modifiers[30] = -4;
        modifiers[31] = -3;
        modifiers[32] = -3;
        modifiers[33] = -3;
        modifiers[34] = -3;
        modifiers[35] = -2;
        modifiers[36] = -2;
        modifiers[37] = -2;
        modifiers[38] = -2;
        modifiers[39] = -2;
        modifiers[40] = -1;
        modifiers[41] = -1;
        modifiers[42] = -1;
        modifiers[43] = -1;
        modifiers[44] = -1;
        modifiers[45] = -1;
        modifiers[46] = 0;
        modifiers[47] = 0;
        modifiers[48] = 0;
        modifiers[49] = 0;
        modifiers[50] = 0;    // Center point
        modifiers[51] = 0;    // Center point
        
        // Mirror for the positive half (perfect symmetry)
        for (int i = 1; i <= 49; i++) {
            modifiers[51 + i] = -modifiers[50 - i];
        }
        
        return modifiers[stat];
    }
    
    // Private constructor to prevent instantiation
    private GameConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
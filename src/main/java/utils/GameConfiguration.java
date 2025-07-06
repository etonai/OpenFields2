/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

package utils;

/**
 * Game configuration utility for runtime control of deterministic behavior.
 * 
 * This class provides centralized control over game behavior modes, particularly
 * for enabling deterministic random number generation during testing and debugging.
 * 
 * Usage:
 * - GameConfiguration.setDeterministicMode(true, 12345) - Enable with specific seed
 * - GameConfiguration.setDeterministicMode(false) - Disable deterministic mode
 * - GameConfiguration.isDeterministicMode() - Check current mode
 */
public final class GameConfiguration {
    
    private static boolean deterministicMode = false;
    private static long currentSeed = 0;
    
    /**
     * Enables or disables deterministic mode with a specific seed.
     * @param enabled Whether to enable deterministic mode
     * @param seed The seed to use for random number generation
     */
    public static void setDeterministicMode(boolean enabled, long seed) {
        deterministicMode = enabled;
        currentSeed = seed;
        
        if (enabled) {
            RandomProvider.setSeed(seed);
            System.out.println("Deterministic mode ENABLED with seed: " + seed);
        } else {
            // Reset to a time-based seed for non-deterministic behavior
            long timeSeed = System.currentTimeMillis();
            RandomProvider.setSeed(timeSeed);
            System.out.println("Deterministic mode DISABLED (using time-based seed)");
        }
    }
    
    /**
     * Disables deterministic mode and returns to normal random behavior.
     */
    public static void setDeterministicMode(boolean enabled) {
        setDeterministicMode(enabled, System.currentTimeMillis());
    }
    
    /**
     * Returns whether deterministic mode is currently enabled.
     * @return True if deterministic mode is enabled, false otherwise
     */
    public static boolean isDeterministicMode() {
        return deterministicMode;
    }
    
    /**
     * Gets the current seed value (only meaningful in deterministic mode).
     * @return The current seed value
     */
    public static long getCurrentSeed() {
        return currentSeed;
    }
    
    /**
     * Resets configuration to default state (deterministic mode off).
     */
    public static void reset() {
        setDeterministicMode(false);
    }
    
    // Private constructor to prevent instantiation
    private GameConfiguration() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
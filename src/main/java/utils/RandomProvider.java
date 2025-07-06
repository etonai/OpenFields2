/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

package utils;

import java.util.Random;

/**
 * Centralized random number generation utility with deterministic mode support.
 * 
 * This class provides a single point of control for all random number generation
 * in the game, enabling deterministic behavior for testing and debugging.
 * 
 * Usage:
 * - Normal mode: RandomProvider.nextDouble() behaves like Math.random()
 * - Deterministic mode: Set seed via GameConfiguration for reproducible results
 */
public final class RandomProvider {
    
    private static Random random = new Random();
    
    /**
     * Sets the seed for deterministic random number generation.
     * @param seed The seed value to use
     */
    public static void setSeed(long seed) {
        random = new Random(seed);
    }
    
    /**
     * Returns the next pseudorandom double value between 0.0 and 1.0.
     * Replacement for Math.random().
     * @return A double value between 0.0 (inclusive) and 1.0 (exclusive)
     */
    public static double nextDouble() {
        return random.nextDouble();
    }
    
    /**
     * Returns the next pseudorandom int value between 0 (inclusive) and bound (exclusive).
     * @param bound The upper bound (exclusive)
     * @return A random int value
     */
    public static int nextInt(int bound) {
        return random.nextInt(bound);
    }
    
    /**
     * Returns the next pseudorandom boolean value.
     * @return A random boolean value
     */
    public static boolean nextBoolean() {
        return random.nextBoolean();
    }
    
    /**
     * Returns the next pseudorandom int value.
     * @return A random int value
     */
    public static int nextInt() {
        return random.nextInt();
    }
    
    /**
     * Gets the current Random instance for use in method overloads.
     * @return The current Random instance
     */
    public static Random getCurrentRandom() {
        return random;
    }
    
    // Private constructor to prevent instantiation
    private RandomProvider() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
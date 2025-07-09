package com.openfields.testutils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import game.Unit;
import game.GameClock;

/**
 * Type-safe reflection utilities for accessing private fields and methods in tests.
 * 
 * This utility class centralizes all reflection operations used across test classes,
 * providing type safety, clear error messages, and convenience methods for common
 * game component access patterns.
 * 
 * Key features:
 * - Type-safe generic methods with proper casting
 * - Detailed error messages for debugging
 * - Convenience methods for common OpenFields2 components
 * - Centralized reflection error handling
 * 
 * Usage examples:
 * 
 * Basic field access:
 * <pre>
 * {@code
 * List<Unit> units = ReflectionTestUtils.getPrivateField(game, "units", List.class);
 * GameClock clock = ReflectionTestUtils.getGameClock(game);
 * 
 * ReflectionTestUtils.setPrivateField(character, "health", 50);
 * }
 * </pre>
 * 
 * Method invocation:
 * <pre>
 * {@code
 * Boolean result = ReflectionTestUtils.invokePrivateMethod(
 *     component, "validateState", Boolean.class);
 * 
 * ReflectionTestUtils.invokePrivateMethod(
 *     manager, "processEvent", event);
 * }
 * </pre>
 * 
 * @author DevCycle 42 - Test Utility Classes Implementation
 */
public class ReflectionTestUtils {
    
    /**
     * Gets a private field value with type safety.
     * 
     * @param <T> the expected return type
     * @param obj the object containing the field
     * @param fieldName the name of the field to access
     * @param expectedType the expected type of the field value
     * @return the field value cast to the expected type
     * @throws RuntimeException if field access fails or type mismatch occurs
     */
    @SuppressWarnings("unchecked")
    public static <T> T getPrivateField(Object obj, String fieldName, Class<T> expectedType) {
        if (obj == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }
        if (fieldName == null || fieldName.isEmpty()) {
            throw new IllegalArgumentException("Field name cannot be null or empty");
        }
        if (expectedType == null) {
            throw new IllegalArgumentException("Expected type cannot be null");
        }
        
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(obj);
            
            if (value == null) {
                return null;
            }
            
            if (!expectedType.isAssignableFrom(value.getClass())) {
                throw new RuntimeException(String.format(
                    "Field '%s' in class '%s' is of type '%s', expected '%s'",
                    fieldName, obj.getClass().getSimpleName(),
                    value.getClass().getSimpleName(), expectedType.getSimpleName()));
            }
            
            return (T) value;
            
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(String.format(
                "Field '%s' not found in class '%s'", 
                fieldName, obj.getClass().getSimpleName()), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(String.format(
                "Cannot access field '%s' in class '%s'", 
                fieldName, obj.getClass().getSimpleName()), e);
        }
    }
    
    /**
     * Gets a private field value without type checking.
     * 
     * @param obj the object containing the field
     * @param fieldName the name of the field to access
     * @return the field value as Object
     * @throws RuntimeException if field access fails
     */
    public static Object getPrivateField(Object obj, String fieldName) {
        return getPrivateField(obj, fieldName, Object.class);
    }
    
    /**
     * Sets a private field value.
     * 
     * @param obj the object containing the field
     * @param fieldName the name of the field to set
     * @param value the value to set
     * @throws RuntimeException if field access fails
     */
    public static void setPrivateField(Object obj, String fieldName, Object value) {
        if (obj == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }
        if (fieldName == null || fieldName.isEmpty()) {
            throw new IllegalArgumentException("Field name cannot be null or empty");
        }
        
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
            
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(String.format(
                "Field '%s' not found in class '%s'", 
                fieldName, obj.getClass().getSimpleName()), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(String.format(
                "Cannot access field '%s' in class '%s'", 
                fieldName, obj.getClass().getSimpleName()), e);
        }
    }
    
    /**
     * Invokes a private method with type-safe return value.
     * 
     * @param <T> the expected return type
     * @param obj the object containing the method
     * @param methodName the name of the method to invoke
     * @param returnType the expected return type
     * @param args the method arguments
     * @return the method result cast to the expected type
     * @throws RuntimeException if method invocation fails
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokePrivateMethod(Object obj, String methodName, Class<T> returnType, Object... args) {
        if (obj == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }
        if (methodName == null || methodName.isEmpty()) {
            throw new IllegalArgumentException("Method name cannot be null or empty");
        }
        if (returnType == null) {
            throw new IllegalArgumentException("Return type cannot be null");
        }
        
        try {
            // Find method by name and argument count
            Method[] methods = obj.getClass().getDeclaredMethods();
            Method targetMethod = null;
            
            for (Method method : methods) {
                if (method.getName().equals(methodName) && method.getParameterCount() == args.length) {
                    targetMethod = method;
                    break;
                }
            }
            
            if (targetMethod == null) {
                throw new RuntimeException(String.format(
                    "Method '%s' with %d parameters not found in class '%s'",
                    methodName, args.length, obj.getClass().getSimpleName()));
            }
            
            targetMethod.setAccessible(true);
            Object result = targetMethod.invoke(obj, args);
            
            if (result == null) {
                return null;
            }
            
            if (!returnType.isAssignableFrom(result.getClass())) {
                throw new RuntimeException(String.format(
                    "Method '%s' returned type '%s', expected '%s'",
                    methodName, result.getClass().getSimpleName(), returnType.getSimpleName()));
            }
            
            return (T) result;
            
        } catch (Exception e) {
            throw new RuntimeException(String.format(
                "Failed to invoke method '%s' in class '%s': %s",
                methodName, obj.getClass().getSimpleName(), e.getMessage()), e);
        }
    }
    
    /**
     * Invokes a private method without return value (void methods).
     * 
     * @param obj the object containing the method
     * @param methodName the name of the method to invoke
     * @param args the method arguments
     * @throws RuntimeException if method invocation fails
     */
    public static void invokePrivateMethod(Object obj, String methodName, Object... args) {
        invokePrivateMethod(obj, methodName, Object.class, args);
    }
    
    // Convenience methods for common OpenFields2 game components
    
    /**
     * Gets the GameClock from an OpenFields2 game instance.
     * 
     * @param game the OpenFields2 game instance
     * @return the GameClock instance
     * @throws RuntimeException if field access fails
     */
    public static GameClock getGameClock(Object game) {
        return getPrivateField(game, "gameClock", GameClock.class);
    }
    
    /**
     * Gets the units list from an OpenFields2 game instance.
     * 
     * @param game the OpenFields2 game instance
     * @return the units list
     * @throws RuntimeException if field access fails
     */
    @SuppressWarnings("unchecked")
    public static List<Unit> getUnits(Object game) {
        return getPrivateField(game, "units", List.class);
    }
    
    /**
     * Gets the SaveGameController from an OpenFields2 game instance.
     * 
     * @param game the OpenFields2 game instance
     * @return the SaveGameController instance (as Object due to package visibility)
     * @throws RuntimeException if field access fails
     */
    public static Object getSaveGameController(Object game) {
        return getPrivateField(game, "saveGameController", Object.class);
    }
    
    /**
     * Gets the SelectionManager from an OpenFields2 game instance.
     * 
     * @param game the OpenFields2 game instance
     * @return the SelectionManager instance (as Object due to package visibility)
     * @throws RuntimeException if field access fails
     */
    public static Object getSelectionManager(Object game) {
        return getPrivateField(game, "selectionManager", Object.class);
    }
    
    /**
     * Gets the InputManager from an OpenFields2 game instance.
     * 
     * @param game the OpenFields2 game instance
     * @return the InputManager instance (as Object due to package visibility)
     * @throws RuntimeException if field access fails
     */
    public static Object getInputManager(Object game) {
        return getPrivateField(game, "inputManager", Object.class);
    }
    
    /**
     * Sets the paused state of an OpenFields2 game instance.
     * 
     * @param game the OpenFields2 game instance
     * @param paused the paused state to set
     * @throws RuntimeException if field access fails
     */
    public static void setPaused(Object game, boolean paused) {
        try {
            java.lang.reflect.Method setPausedMethod = game.getClass().getMethod("setPaused", boolean.class);
            setPausedMethod.invoke(game, paused);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set paused state: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets the paused state of an OpenFields2 game instance.
     * 
     * @param game the OpenFields2 game instance
     * @return true if the game is paused
     * @throws RuntimeException if field access fails
     */
    public static boolean isPaused(Object game) {
        return getPrivateField(game, "paused", Boolean.class);
    }
}
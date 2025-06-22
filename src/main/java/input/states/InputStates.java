/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

package input.states;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import combat.Character;
import game.Unit;
import combat.WeaponState;
import game.ScheduledEvent;

/**
 * Central container for all input workflow state enums and data transfer objects.
 * 
 * This class consolidates all state management definitions used by the input system,
 * providing a single location for workflow state enums, data transfer objects, and
 * configuration constants.
 * 
 * @author DevCycle 15h - State Extraction
 */
public class InputStates {
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Workflow State Enums
    // ─────────────────────────────────────────────────────────────────────────────────
    // State machine enums for managing complex multi-step workflows
    
    /**
     * Steps in the batch character creation workflow.
     * This workflow allows creating multiple characters of the same archetype at once.
     */
    public enum BatchCreationStep {
        /** Prompting user to enter number of characters to create (1-20) */
        QUANTITY,
        
        /** Prompting user to select character archetype from available options */
        ARCHETYPE,
        
        /** Prompting user to select faction assignment for created characters */
        FACTION
    }
    
    /**
     * Steps in the character deployment workflow.
     * This workflow deploys pre-created characters from faction files to the battlefield.
     */
    public enum DeploymentStep {
        /** Prompting user to select faction for character deployment */
        FACTION,
        
        /** Prompting user to specify number of characters to deploy */
        QUANTITY,
        
        /** Prompting user to select weapon configuration for deployed characters */
        WEAPON,
        
        /** Prompting user to select formation type (line, column, etc.) */
        FORMATION,
        
        /** Prompting user to specify spacing between characters */
        SPACING,
        
        /** Waiting for mouse click to place characters on battlefield */
        PLACEMENT
    }
    
    /**
     * Steps in the direct character addition workflow (CTRL-A functionality).
     * This workflow adds existing characters directly from faction files.
     */
    public enum DirectAdditionStep {
        /** Prompting user to select faction for character addition */
        FACTION,
        
        /** Prompting user to specify number of characters to add (1-20) */
        QUANTITY,
        
        /** Prompting user to specify spacing between characters (1-9 feet) */
        SPACING,
        
        /** Waiting for mouse click to place characters in line formation */
        PLACEMENT
    }
    
    /**
     * Victory outcome options for factions in manual victory determination.
     * Used when manually ending scenarios to record faction performance.
     */
    public enum VictoryOutcome {
        /** Faction achieved complete victory in the scenario */
        VICTORY,
        
        /** Faction was defeated or destroyed */
        DEFEAT,
        
        /** Faction participated but neither won nor lost definitively */
        PARTICIPANT
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // Data Transfer Objects
    // ─────────────────────────────────────────────────────────────────────────────────
    // Helper classes for organizing and transferring complex data
    
    /**
     * Data transfer object holding faction character information for deployment operations.
     * Provides organized access to faction character data including availability counts.
     */
    public static class FactionCharacterInfo {
        /** Display name of the faction */
        public String factionName;
        
        /** Total number of characters defined for this faction */
        public int totalCharacters;
        
        /** Number of characters available for deployment (not already deployed/incapacitated) */
        public int availableCount;
        
        /** List of available characters ready for deployment */
        public List<Character> availableCharacters;
        
        /**
         * Constructor for faction character information.
         * 
         * @param factionName Display name of the faction
         * @param totalCharacters Total characters defined for faction
         * @param availableCount Characters available for deployment
         * @param availableCharacters List of deployable characters
         */
        public FactionCharacterInfo(String factionName, int totalCharacters, int availableCount, List<Character> availableCharacters) {
            this.factionName = factionName;
            this.totalCharacters = totalCharacters;
            this.availableCount = availableCount;
            this.availableCharacters = availableCharacters;
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────────
    // JSON Deserialization Support
    // ─────────────────────────────────────────────────────────────────────────────────
    // Static configuration for handling faction character file loading
    
    /** Jackson ObjectMapper configured for safe faction character deserialization */
    public static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        // Configure ObjectMapper to handle deserialization issues gracefully
        // These settings prevent crashes when loading faction files with missing or extra fields
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
        
        // Add mixin to ignore problematic runtime fields during deserialization
        objectMapper.addMixIn(Character.class, CharacterMixin.class);
    }
    
    /**
     * Jackson mixin class to control Character deserialization from faction JSON files.
     * This prevents deserialization failures on runtime-only fields that shouldn't be persisted.
     */
    public static abstract class CharacterMixin {
        /** Ignore target zone (runtime-only AWT Rectangle) */
        @com.fasterxml.jackson.annotation.JsonIgnore
        public java.awt.Rectangle targetZone;
        
        /** Ignore current target reference (runtime-only Unit reference) */
        @com.fasterxml.jackson.annotation.JsonIgnore
        public Unit currentTarget;
        
        /** Ignore melee target reference (runtime-only Unit reference) */
        @com.fasterxml.jackson.annotation.JsonIgnore
        public Unit meleeTarget;
        
        /** Ignore weapon state (runtime-only state machine reference) */
        @com.fasterxml.jackson.annotation.JsonIgnore
        public WeaponState currentWeaponState;
        
        /** Ignore paused events (runtime-only event queue) */
        @com.fasterxml.jackson.annotation.JsonIgnore
        public List<ScheduledEvent> pausedEvents;
    }
}
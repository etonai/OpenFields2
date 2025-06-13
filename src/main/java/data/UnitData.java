package data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import combat.FiringMode;

public class UnitData {
    @JsonProperty("id")
    public int id;
    
    @JsonProperty("characterId")
    public int characterId;
    
    @JsonProperty("x")
    public double x;
    
    @JsonProperty("y")
    public double y;
    
    @JsonProperty("targetX")
    public double targetX;
    
    @JsonProperty("targetY")
    public double targetY;
    
    @JsonProperty("hasTarget")
    public boolean hasTarget;
    
    @JsonProperty("isStopped")
    public boolean isStopped;
    
    @JsonProperty("color")
    public String color;
    
    @JsonProperty("baseColor")
    public String baseColor;
    
    @JsonProperty("isHitHighlighted")
    public boolean isHitHighlighted;
    
    @JsonProperty("isFiringHighlighted")
    public boolean isFiringHighlighted;
    
    // Scenario-specific equipment data
    @JsonProperty("weaponId")
    public String weaponId;
    
    @JsonProperty("currentWeaponState")
    public String currentWeaponState;
    
    // Theme-specific overrides for scenario
    @JsonProperty("themeId")
    public String themeId;
    
    // Combat state data
    @JsonProperty("currentTargetId")
    public Integer currentTargetId;
    
    @JsonProperty("currentFiringMode")
    public FiringMode currentFiringMode;
    
    @JsonProperty("usesAutomaticTargeting")
    public boolean usesAutomaticTargeting;
    
    public UnitData() {
        // Default constructor for Jackson
    }
    
    public UnitData(int id, int characterId, double x, double y, double targetX, double targetY,
                   boolean hasTarget, boolean isStopped, String color, String baseColor, boolean isHitHighlighted,
                   boolean isFiringHighlighted, String weaponId, String currentWeaponState, String themeId,
                   Integer currentTargetId, FiringMode currentFiringMode, boolean usesAutomaticTargeting) {
        this.id = id;
        this.characterId = characterId;
        this.x = x;
        this.y = y;
        this.targetX = targetX;
        this.targetY = targetY;
        this.hasTarget = hasTarget;
        this.isStopped = isStopped;
        this.color = color;
        this.baseColor = baseColor;
        this.isHitHighlighted = isHitHighlighted;
        this.isFiringHighlighted = isFiringHighlighted;
        this.weaponId = weaponId;
        this.currentWeaponState = currentWeaponState;
        this.themeId = themeId;
        this.currentTargetId = currentTargetId;
        this.currentFiringMode = currentFiringMode;
        this.usesAutomaticTargeting = usesAutomaticTargeting;
    }
    
    // Legacy constructor for backward compatibility
    public UnitData(int id, int characterId, double x, double y, double targetX, double targetY,
                   boolean hasTarget, boolean isStopped, String color, String baseColor, boolean isHitHighlighted) {
        this(id, characterId, x, y, targetX, targetY, hasTarget, isStopped, color, baseColor, isHitHighlighted,
             false, null, null, null, null, null, false);
    }
    
    // Intermediate constructor for backward compatibility
    public UnitData(int id, int characterId, double x, double y, double targetX, double targetY,
                   boolean hasTarget, boolean isStopped, String color, String baseColor, boolean isHitHighlighted,
                   boolean isFiringHighlighted, String weaponId, String currentWeaponState, String themeId) {
        this(id, characterId, x, y, targetX, targetY, hasTarget, isStopped, color, baseColor, isHitHighlighted,
             isFiringHighlighted, weaponId, currentWeaponState, themeId, null, null, false);
    }
}
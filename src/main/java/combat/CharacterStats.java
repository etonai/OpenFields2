package combat;

/**
 * Mutable value object representing a character's core statistics and attributes.
 * Extracted from Character.java to improve code organization and reduce file size.
 */
public class CharacterStats {
    // Core attributes
    private int dexterity;
    private int currentDexterity;
    private int health;
    private int currentHealth;
    private int coolness;
    private int strength;
    private int reflexes;
    
    // Physical characteristics
    private Handedness handedness;
    private double baseMovementSpeed;
    
    public CharacterStats(int dexterity, int health, int coolness, int strength, int reflexes, 
                         Handedness handedness, double baseMovementSpeed) {
        this.dexterity = dexterity;
        this.currentDexterity = dexterity; // Initially same as base
        this.health = health;
        this.currentHealth = health; // Initially at full health
        this.coolness = coolness;
        this.strength = strength;
        this.reflexes = reflexes;
        this.handedness = handedness;
        this.baseMovementSpeed = baseMovementSpeed;
    }
    
    // Getters and setters for core attributes
    public int getDexterity() {
        return dexterity;
    }
    
    public void setDexterity(int dexterity) {
        this.dexterity = dexterity;
    }
    
    public int getCurrentDexterity() {
        return currentDexterity;
    }
    
    public void setCurrentDexterity(int currentDexterity) {
        this.currentDexterity = currentDexterity;
    }
    
    public int getHealth() {
        return health;
    }
    
    public void setHealth(int health) {
        this.health = health;
    }
    
    public int getCurrentHealth() {
        return currentHealth;
    }
    
    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }
    
    public int getCoolness() {
        return coolness;
    }
    
    public void setCoolness(int coolness) {
        this.coolness = coolness;
    }
    
    public int getStrength() {
        return strength;
    }
    
    public void setStrength(int strength) {
        this.strength = strength;
    }
    
    public int getReflexes() {
        return reflexes;
    }
    
    public void setReflexes(int reflexes) {
        this.reflexes = reflexes;
    }
    
    public Handedness getHandedness() {
        return handedness;
    }
    
    public void setHandedness(Handedness handedness) {
        this.handedness = handedness;
    }
    
    public double getBaseMovementSpeed() {
        return baseMovementSpeed;
    }
    
    public void setBaseMovementSpeed(double baseMovementSpeed) {
        this.baseMovementSpeed = baseMovementSpeed;
    }
    
    // Utility methods for stat calculations
    
    /**
     * Calculates the stat modifier for a given attribute value.
     * Uses a balanced curve from -20 to +20 modifiers based on attribute range 1-100.
     */
    public static int getStatModifier(int attributeValue) {
        if (attributeValue <= 0) return -20;
        if (attributeValue >= 100) return 20;
        
        // Linear scaling: 1-100 maps to -20 to +20
        return (int) Math.round((attributeValue - 50) * 0.4);
    }
    
    public int getDexterityModifier() {
        return getStatModifier(currentDexterity);
    }
    
    public int getCoolnessModifier() {
        return getStatModifier(coolness);
    }
    
    public int getStrengthModifier() {
        return getStatModifier(strength);
    }
    
    public int getReflexesModifier() {
        return getStatModifier(reflexes);
    }
    
    /**
     * Checks if the character is incapacitated (0 or negative health)
     */
    public boolean isIncapacitated() {
        return currentHealth <= 0;
    }
    
    /**
     * Applies damage to current health
     */
    public void takeDamage(int damage) {
        currentHealth = Math.max(0, currentHealth - damage);
    }
    
    /**
     * Heals the character by the specified amount, not exceeding max health
     */
    public void heal(int amount) {
        currentHealth = Math.min(health, currentHealth + amount);
    }
    
    /**
     * Restores the character to full health
     */
    public void fullHeal() {
        currentHealth = health;
    }
    
    @Override
    public String toString() {
        return "CharacterStats{" +
               "dex=" + dexterity + "(" + currentDexterity + ")" +
               ", health=" + currentHealth + "/" + health +
               ", coolness=" + coolness +
               ", strength=" + strength +
               ", reflexes=" + reflexes +
               ", handedness=" + handedness +
               ", baseMovementSpeed=" + baseMovementSpeed +
               '}';
    }
}
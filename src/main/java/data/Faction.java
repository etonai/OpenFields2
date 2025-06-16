package data;

import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.scene.paint.Color;
import java.util.HashSet;
import java.util.Set;

public class Faction {
    @JsonProperty("id")
    private int id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("allies")
    private Set<Integer> allies;
    
    @JsonProperty("enemies")
    private Set<Integer> enemies;
    
    @JsonProperty("color")
    private String colorString; // Store as string for JSON serialization
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("victories")
    private int victories;
    
    @JsonProperty("defeats")
    private int defeats;
    
    @JsonProperty("participations")
    private int participations;
    
    // Default constructor for Jackson
    public Faction() {
        this.allies = new HashSet<>();
        this.enemies = new HashSet<>();
        this.victories = 0;
        this.defeats = 0;
        this.participations = 0;
    }
    
    public Faction(int id, String name, String description, Color color) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.allies = new HashSet<>();
        this.enemies = new HashSet<>();
        this.colorString = colorToString(color);
        this.victories = 0;
        this.defeats = 0;
        this.participations = 0;
    }
    
    // Relationship methods
    public boolean isAlly(int factionId) {
        return allies.contains(factionId);
    }
    
    public boolean isEnemy(int factionId) {
        return enemies.contains(factionId);
    }
    
    public boolean isNeutral(int factionId) {
        return !isAlly(factionId) && !isEnemy(factionId) && factionId != this.id;
    }
    
    public void addAlly(int factionId) {
        allies.add(factionId);
        enemies.remove(factionId); // Remove from enemies if present
    }
    
    public void addEnemy(int factionId) {
        enemies.add(factionId);
        allies.remove(factionId); // Remove from allies if present
    }
    
    public void removeAlly(int factionId) {
        allies.remove(factionId);
    }
    
    public void removeEnemy(int factionId) {
        enemies.remove(factionId);
    }
    
    // Battle statistics methods
    public void incrementVictories() {
        victories++;
    }
    
    public void incrementDefeats() {
        defeats++;
    }
    
    public void incrementParticipations() {
        participations++;
    }
    
    // Color conversion methods
    private String colorToString(Color color) {
        if (color == null) return "#808080"; // Default gray
        return String.format("#%02X%02X%02X", 
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
    }
    
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Color getColor() {
        if (colorString == null || colorString.isEmpty()) {
            return Color.GRAY;
        }
        try {
            return Color.web(colorString);
        } catch (IllegalArgumentException e) {
            return Color.GRAY;
        }
    }
    
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setColor(Color color) {
        this.colorString = colorToString(color);
    }
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Set<Integer> getAllies() {
        return new HashSet<>(allies);
    }
    
    public void setAllies(Set<Integer> allies) {
        this.allies = new HashSet<>(allies);
    }
    
    public Set<Integer> getEnemies() {
        return new HashSet<>(enemies);
    }
    
    public void setEnemies(Set<Integer> enemies) {
        this.enemies = new HashSet<>(enemies);
    }
    
    public String getColorString() {
        return colorString;
    }
    
    public void setColorString(String colorString) {
        this.colorString = colorString;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getVictories() {
        return victories;
    }
    
    public void setVictories(int victories) {
        this.victories = victories;
    }
    
    public int getDefeats() {
        return defeats;
    }
    
    public void setDefeats(int defeats) {
        this.defeats = defeats;
    }
    
    public int getParticipations() {
        return participations;
    }
    
    public void setParticipations(int participations) {
        this.participations = participations;
    }
    
    @Override
    public String toString() {
        return String.format("Faction{id=%d, name='%s', allies=%s, enemies=%s, victories=%d, defeats=%d, participations=%d}",
                id, name, allies, enemies, victories, defeats, participations);
    }
}
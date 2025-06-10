package data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import java.util.Map;
import java.util.Date;

public class ThemeData {
    @JsonProperty("id")
    public String id;
    
    @JsonProperty("name")
    public String name;
    
    @JsonProperty("description")
    public String description;
    
    @JsonProperty("version")
    public String version;
    
    @JsonProperty("author")
    public String author;
    
    @JsonProperty("createdDate")
    public String createdDate;
    
    @JsonProperty("currentDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMMM d, yyyy")
    public Date currentDate;
    
    @JsonProperty("compatibleGameVersion")
    public String compatibleGameVersion;
    
    @JsonProperty("tags")
    public List<String> tags;
    
    @JsonProperty("resources")
    public ThemeResources resources;
    
    public ThemeData() {
        // Default constructor for Jackson
    }
    
    public ThemeData(String id, String name, String description, String version, String author,
                    String createdDate, Date currentDate, String compatibleGameVersion, List<String> tags, ThemeResources resources) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.version = version;
        this.author = author;
        this.createdDate = createdDate;
        this.currentDate = currentDate;
        this.compatibleGameVersion = compatibleGameVersion;
        this.tags = tags;
        this.resources = resources;
    }
    
    public static class ThemeResources {
        @JsonProperty("weapons")
        public String weapons;
        
        @JsonProperty("weaponTypes")
        public String weaponTypes;
        
        @JsonProperty("skills")
        public String skills; // Optional - skills are now universal
        
        public ThemeResources() {
            // Default constructor for Jackson
        }
        
        public ThemeResources(String weapons, String weaponTypes, String skills) {
            this.weapons = weapons;
            this.weaponTypes = weaponTypes;
            this.skills = skills; // Optional parameter for backwards compatibility
        }
        
        public ThemeResources(String weapons, String weaponTypes) {
            this.weapons = weapons;
            this.weaponTypes = weaponTypes;
            this.skills = null; // Skills are now universal
        }
    }
}
package data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ThemeManager {
    private static ThemeManager instance;
    private final ObjectMapper objectMapper;
    
    private Map<String, ThemeData> themes;
    private String defaultThemeId;
    private String currentThemeId;
    
    private ThemeManager() {
        this.objectMapper = new ObjectMapper();
        loadThemes();
    }
    
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    private void loadThemes() {
        try {
            InputStream is = getClass().getResourceAsStream("/data/themes.json");
            if (is == null) {
                throw new IOException("Could not find themes.json file");
            }
            
            JsonNode rootNode = objectMapper.readTree(is);
            JsonNode themesNode = rootNode.get("themes");
            JsonNode defaultThemeNode = rootNode.get("defaultTheme");
            
            themes = new HashMap<>();
            themesNode.fields().forEachRemaining(entry -> {
                try {
                    // Load theme metadata from themes.json
                    JsonNode themeNode = entry.getValue();
                    String themeId = themeNode.get("id").asText();
                    String dataPath = themeNode.get("dataPath").asText();
                    
                    // Load detailed theme data from theme-specific theme.json
                    InputStream themeIs = getClass().getResourceAsStream("/data/" + dataPath + "/theme.json");
                    if (themeIs != null) {
                        JsonNode detailedThemeNode = objectMapper.readTree(themeIs);
                        ThemeData themeData = objectMapper.treeToValue(detailedThemeNode.get("theme"), ThemeData.class);
                        themes.put(themeId, themeData);
                    } else {
                        System.err.println("Warning: Could not find theme.json for theme: " + themeId);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading theme " + entry.getKey() + ": " + e.getMessage());
                }
            });
            
            defaultThemeId = defaultThemeNode.asText();
            currentThemeId = defaultThemeId;
            
            System.out.println("*** Themes loaded successfully: " + themes.size() + " themes available");
            System.out.println("*** Default theme: " + defaultThemeId);
            
        } catch (Exception e) {
            System.err.println("Error loading themes: " + e.getMessage());
            e.printStackTrace();
            
            // Initialize empty maps to prevent null pointer exceptions
            themes = new HashMap<>();
            defaultThemeId = "test_theme";
            currentThemeId = defaultThemeId;
        }
    }
    
    // Getters
    public ThemeData getTheme(String themeId) {
        return themes.get(themeId);
    }
    
    public ThemeData getCurrentTheme() {
        return themes.get(currentThemeId);
    }
    
    public String getCurrentThemeId() {
        return currentThemeId;
    }
    
    public String getDefaultThemeId() {
        return defaultThemeId;
    }
    
    public Map<String, ThemeData> getAllThemes() {
        return new HashMap<>(themes);
    }
    
    // Theme management
    public boolean setCurrentTheme(String themeId) {
        if (themes.containsKey(themeId)) {
            currentThemeId = themeId;
            System.out.println("*** Switched to theme: " + themeId);
            return true;
        } else {
            System.err.println("Warning: Theme not found: " + themeId);
            return false;
        }
    }
    
    public boolean hasTheme(String themeId) {
        return themes.containsKey(themeId);
    }
    
    public String[] getAllThemeIds() {
        return themes.keySet().toArray(new String[0]);
    }
    
    // Utility methods for theme data paths
    public String getThemeDataPath(String themeId) {
        ThemeData theme = themes.get(themeId);
        return theme != null ? "themes/" + themeId : null;
    }
    
    public String getCurrentThemeDataPath() {
        return getThemeDataPath(currentThemeId);
    }
}
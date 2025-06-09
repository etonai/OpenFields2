package data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import combat.WeaponType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DataManager {
    private static DataManager instance;
    private final ObjectMapper objectMapper;
    private final ThemeManager themeManager;
    
    private Map<String, WeaponData> weapons;
    private Map<WeaponType, WeaponTypeData> weaponTypes;
    private Map<String, SkillData> skills;
    
    private DataManager() {
        this.objectMapper = new ObjectMapper();
        this.themeManager = ThemeManager.getInstance();
        loadAllData();
    }
    
    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }
    
    private void loadAllData() {
        try {
            loadWeapons();
            loadWeaponTypes();
            loadSkills();
            System.out.println("*** Data loaded successfully: " + weapons.size() + " weapons, " + 
                             weaponTypes.size() + " weapon types, " + skills.size() + " skills");
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            e.printStackTrace();
            
            // Initialize empty maps to prevent null pointer exceptions
            weapons = new HashMap<>();
            weaponTypes = new HashMap<>();
            skills = new HashMap<>();
        }
    }
    
    private void loadWeapons() throws IOException {
        String themePath = themeManager.getCurrentThemeDataPath();
        InputStream is = getClass().getResourceAsStream("/data/" + themePath + "/weapons.json");
        if (is == null) {
            throw new IOException("Could not find weapons.json file for theme: " + themeManager.getCurrentThemeId());
        }
        
        JsonNode rootNode = objectMapper.readTree(is);
        JsonNode weaponsNode = rootNode.get("weapons");
        
        weapons = new HashMap<>();
        weaponsNode.fields().forEachRemaining(entry -> {
            try {
                WeaponData weaponData = objectMapper.treeToValue(entry.getValue(), WeaponData.class);
                // Use the ID from the weapon data as the key, not the JSON object key
                weapons.put(weaponData.id, weaponData);
            } catch (Exception e) {
                System.err.println("Error loading weapon " + entry.getKey() + ": " + e.getMessage());
            }
        });
    }
    
    private void loadWeaponTypes() throws IOException {
        String themePath = themeManager.getCurrentThemeDataPath();
        InputStream is = getClass().getResourceAsStream("/data/" + themePath + "/weapon-types.json");
        if (is == null) {
            throw new IOException("Could not find weapon-types.json file for theme: " + themeManager.getCurrentThemeId());
        }
        
        JsonNode rootNode = objectMapper.readTree(is);
        JsonNode weaponTypesNode = rootNode.get("weaponTypes");
        
        weaponTypes = new HashMap<>();
        weaponTypesNode.fields().forEachRemaining(entry -> {
            try {
                WeaponType weaponType = WeaponType.valueOf(entry.getKey());
                WeaponTypeData weaponTypeData = objectMapper.treeToValue(entry.getValue(), WeaponTypeData.class);
                weaponTypes.put(weaponType, weaponTypeData);
            } catch (Exception e) {
                System.err.println("Error loading weapon type " + entry.getKey() + ": " + e.getMessage());
            }
        });
    }
    
    private void loadSkills() throws IOException {
        String themePath = themeManager.getCurrentThemeDataPath();
        InputStream is = getClass().getResourceAsStream("/data/" + themePath + "/skills.json");
        if (is == null) {
            throw new IOException("Could not find skills.json file for theme: " + themeManager.getCurrentThemeId());
        }
        
        JsonNode rootNode = objectMapper.readTree(is);
        JsonNode skillsNode = rootNode.get("skills");
        
        skills = new HashMap<>();
        skillsNode.fields().forEachRemaining(entry -> {
            try {
                SkillData skillData = objectMapper.treeToValue(entry.getValue(), SkillData.class);
                // Use the ID from the skill data as the key, not the JSON object key
                skills.put(skillData.id, skillData);
            } catch (Exception e) {
                System.err.println("Error loading skill " + entry.getKey() + ": " + e.getMessage());
            }
        });
    }
    
    // Getters
    public WeaponData getWeapon(String weaponId) {
        return weapons.get(weaponId);
    }
    
    public WeaponTypeData getWeaponType(WeaponType weaponType) {
        return weaponTypes.get(weaponType);
    }
    
    public SkillData getSkill(String skillName) {
        return skills.get(skillName);
    }
    
    public Map<String, WeaponData> getAllWeapons() {
        return new HashMap<>(weapons);
    }
    
    public Map<WeaponType, WeaponTypeData> getAllWeaponTypes() {
        return new HashMap<>(weaponTypes);
    }
    
    public Map<String, SkillData> getAllSkills() {
        return new HashMap<>(skills);
    }
    
    // Utility methods
    public boolean hasWeapon(String weaponId) {
        return weapons.containsKey(weaponId);
    }
    
    public boolean hasSkill(String skillName) {
        return skills.containsKey(skillName);
    }
}
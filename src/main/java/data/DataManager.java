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
    private Map<String, MeleeWeaponData> meleeWeapons;
    
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
            loadMeleeWeaponTypes();
            loadSkills();
            loadMeleeWeapons();
            System.out.println("*** Data loaded successfully: " + weapons.size() + " weapons, " + 
                             weaponTypes.size() + " weapon types, " + skills.size() + " skills, " +
                             meleeWeapons.size() + " melee weapons");
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            e.printStackTrace();
            
            // Initialize empty maps to prevent null pointer exceptions
            weapons = new HashMap<>();
            weaponTypes = new HashMap<>();
            skills = new HashMap<>();
            meleeWeapons = new HashMap<>();
        }
    }
    
    private void loadWeapons() throws IOException {
        String themePath = themeManager.getCurrentThemeDataPath();
        InputStream is = getClass().getResourceAsStream("/data/" + themePath + "/ranged-weapons.json");
        if (is == null) {
            throw new IOException("Could not find ranged-weapons.json file for theme: " + themeManager.getCurrentThemeId());
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
        InputStream is = getClass().getResourceAsStream("/data/" + themePath + "/ranged-weapon-types.json");
        if (is == null) {
            throw new IOException("Could not find ranged-weapon-types.json file for theme: " + themeManager.getCurrentThemeId());
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
    
    private void loadMeleeWeaponTypes() throws IOException {
        String themePath = themeManager.getCurrentThemeDataPath();
        InputStream is = getClass().getResourceAsStream("/data/" + themePath + "/melee-weapon-types.json");
        if (is == null) {
            throw new IOException("Could not find melee-weapon-types.json file for theme: " + themeManager.getCurrentThemeId());
        }
        
        JsonNode rootNode = objectMapper.readTree(is);
        JsonNode weaponTypesNode = rootNode.get("weaponTypes");
        
        // Add melee weapon types to the existing weaponTypes map
        weaponTypesNode.fields().forEachRemaining(entry -> {
            try {
                WeaponType weaponType = WeaponType.valueOf(entry.getKey());
                WeaponTypeData weaponTypeData = objectMapper.treeToValue(entry.getValue(), WeaponTypeData.class);
                weaponTypes.put(weaponType, weaponTypeData);
                System.out.println("*** Loaded melee weapon type: " + weaponType + " with " + weaponTypeData.states.size() + " states");
            } catch (Exception e) {
                System.err.println("Error loading melee weapon type " + entry.getKey() + ": " + e.getMessage());
            }
        });
    }
    
    private void loadSkills() throws IOException {
        // Load skills from universal location (not theme-specific)
        InputStream is = getClass().getResourceAsStream("/data/skills.json");
        if (is == null) {
            throw new IOException("Could not find universal skills.json file");
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
    
    private void loadMeleeWeapons() throws IOException {
        String themePath = themeManager.getCurrentThemeDataPath();
        InputStream is = getClass().getResourceAsStream("/data/" + themePath + "/melee-weapons.json");
        if (is == null) {
            System.err.println("Could not find melee-weapons.json file for theme: " + themeManager.getCurrentThemeId() + ". Exiting gracefully.");
            System.exit(1);
            return;
        }
        
        JsonNode rootNode = objectMapper.readTree(is);
        JsonNode meleeWeaponsNode = rootNode.get("meleeWeapons");
        
        meleeWeapons = new HashMap<>();
        meleeWeaponsNode.fields().forEachRemaining(entry -> {
            try {
                MeleeWeaponData meleeWeaponData = objectMapper.treeToValue(entry.getValue(), MeleeWeaponData.class);
                // Use the ID from the melee weapon data as the key, not the JSON object key
                meleeWeapons.put(meleeWeaponData.id, meleeWeaponData);
            } catch (Exception e) {
                System.err.println("Error loading melee weapon " + entry.getKey() + ": " + e.getMessage());
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
    
    public MeleeWeaponData getMeleeWeapon(String meleeWeaponId) {
        return meleeWeapons.get(meleeWeaponId);
    }
    
    public Map<String, MeleeWeaponData> getAllMeleeWeapons() {
        return new HashMap<>(meleeWeapons);
    }
    
    // Utility methods
    public boolean hasWeapon(String weaponId) {
        return weapons.containsKey(weaponId);
    }
    
    public boolean hasSkill(String skillName) {
        return skills.containsKey(skillName);
    }
    
    public boolean hasMeleeWeapon(String meleeWeaponId) {
        return meleeWeapons.containsKey(meleeWeaponId);
    }
}
package data;

import java.util.Map;
import java.util.Set;

public class SkillsManager {
    private static final DataManager dataManager = DataManager.getInstance();
    
    // Static methods to replace the hardcoded Skills constants - now using proper IDs
    public static final String PISTOL = "skl_pistol";
    public static final String RIFLE = "skl_rifle";
    public static final String QUICKDRAW = "skl_quickdraw";
    public static final String MEDICINE = "skl_medicine";
    public static final String ATHLETICS = "skl_athletics";
    public static final String OBSERVATION = "skl_observation";
    public static final String STEALTH = "skl_stealth";
    public static final String INTIMIDATION = "skl_intimidation";
    
    private SkillsManager() {
        // Utility class - prevent instantiation
    }
    
    public static String[] getAllSkillNames() {
        Map<String, SkillData> skills = dataManager.getAllSkills();
        return skills.keySet().toArray(new String[0]);
    }
    
    public static SkillData getSkillData(String skillName) {
        return dataManager.getSkill(skillName);
    }
    
    public static boolean isValidSkill(String skillName) {
        return dataManager.hasSkill(skillName);
    }
    
    public static String getSkillDescription(String skillName) {
        SkillData skillData = dataManager.getSkill(skillName);
        return skillData != null ? skillData.description : "Unknown skill";
    }
    
    public static int getMaxLevel(String skillName) {
        SkillData skillData = dataManager.getSkill(skillName);
        return skillData != null ? skillData.maxLevel : 10;
    }
    
    public static double getEffectValue(String skillName) {
        SkillData skillData = dataManager.getSkill(skillName);
        return skillData != null ? skillData.effectValue : 0.0;
    }
    
    public static String getEffectType(String skillName) {
        SkillData skillData = dataManager.getSkill(skillName);
        return skillData != null ? skillData.effectType : "NONE";
    }
}
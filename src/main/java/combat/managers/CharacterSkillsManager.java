package combat.managers;

import combat.Character;
import combat.Skill;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton manager for character skills
 * DevCycle 30 - Character class size reduction
 * Follows DevCycle 29 singleton pattern with per-character state tracking
 */
public class CharacterSkillsManager implements ICharacterSkillsManager {
    private static CharacterSkillsManager instance;
    
    // Per-character skills storage
    private final Map<Integer, List<Skill>> characterSkills = new HashMap<>();
    
    private CharacterSkillsManager() {
        // Singleton pattern - private constructor
    }
    
    public static CharacterSkillsManager getInstance() {
        if (instance == null) {
            instance = new CharacterSkillsManager();
        }
        return instance;
    }
    
    @Override
    public int getSkillLevel(int characterId, String skillName) {
        List<Skill> skills = characterSkills.get(characterId);
        if (skills == null) {
            return 0;
        }
        
        for (Skill skill : skills) {
            if (skill.skillName.equals(skillName)) {
                return skill.level;
            }
        }
        return 0;
    }
    
    @Override
    public void setSkillLevel(int characterId, String skillName, int level) {
        List<Skill> skills = characterSkills.computeIfAbsent(characterId, k -> new ArrayList<>());
        
        // Find existing skill
        for (Skill skill : skills) {
            if (skill.skillName.equals(skillName)) {
                skill.level = level;
                return;
            }
        }
        
        // Add new skill if not found
        skills.add(new Skill(skillName, level));
    }
    
    @Override
    public void addSkill(int characterId, Skill skill) {
        List<Skill> skills = characterSkills.computeIfAbsent(characterId, k -> new ArrayList<>());
        skills.add(skill);
    }
    
    @Override
    public List<Skill> getSkills(int characterId) {
        return characterSkills.getOrDefault(characterId, new ArrayList<>());
    }
    
    @Override
    public void setSkills(int characterId, List<Skill> skills) {
        characterSkills.put(characterId, new ArrayList<>(skills));
    }
    
    @Override
    public Skill getSkill(int characterId, String skillName) {
        List<Skill> skills = characterSkills.get(characterId);
        if (skills == null) return null;
        for (Skill skill : skills) {
            if (skill.skillName.equals(skillName)) {
                return skill;
            }
        }
        return null;
    }
    
    @Override
    public boolean hasSkill(int characterId, String skillName) {
        return getSkill(characterId, skillName) != null;
    }
    
    @Override
    public void createDefaultSkills(int characterId) {
        List<Skill> defaultSkills = new ArrayList<>();
        
        // Create default skills as in Character.createDefaultSkills()
        defaultSkills.add(new Skill(data.SkillsManager.PISTOL, 50));
        defaultSkills.add(new Skill(data.SkillsManager.RIFLE, 50));
        defaultSkills.add(new Skill(data.SkillsManager.QUICKDRAW, 50));
        defaultSkills.add(new Skill(data.SkillsManager.MEDICINE, 50));
        
        setSkills(characterId, defaultSkills);
    }
    
    @Override
    public void addDefaultSkills(int characterId) {
        // Add default skills only if the character doesn't already have them
        if (!hasSkill(characterId, data.SkillsManager.PISTOL)) {
            addSkill(characterId, new Skill(data.SkillsManager.PISTOL, 50));
        }
        if (!hasSkill(characterId, data.SkillsManager.RIFLE)) {
            addSkill(characterId, new Skill(data.SkillsManager.RIFLE, 50));
        }
        if (!hasSkill(characterId, data.SkillsManager.QUICKDRAW)) {
            addSkill(characterId, new Skill(data.SkillsManager.QUICKDRAW, 50));
        }
        if (!hasSkill(characterId, data.SkillsManager.MEDICINE)) {
            addSkill(characterId, new Skill(data.SkillsManager.MEDICINE, 50));
        }
    }
    
    @Override
    public void cleanupCharacter(int characterId) {
        characterSkills.remove(characterId);
    }
}
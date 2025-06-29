package combat.managers;

import combat.Character;
import combat.Skill;
import java.util.List;

/**
 * Interface for character skills management
 * DevCycle 30 - Character class size reduction
 */
public interface ICharacterSkillsManager {
    /**
     * Get a character's skill level for a specific skill
     * @param characterId Character ID
     * @param skillName Name of the skill
     * @return Skill level (0 if skill not found)
     */
    int getSkillLevel(int characterId, String skillName);
    
    /**
     * Set a character's skill level for a specific skill
     * @param characterId Character ID
     * @param skillName Name of the skill
     * @param level New skill level
     */
    void setSkillLevel(int characterId, String skillName, int level);
    
    /**
     * Add a skill to a character
     * @param characterId Character ID
     * @param skill The skill to add
     */
    void addSkill(int characterId, Skill skill);
    
    /**
     * Get all skills for a character
     * @param characterId Character ID
     * @return List of skills
     */
    List<Skill> getSkills(int characterId);
    
    /**
     * Set all skills for a character
     * @param characterId Character ID
     * @param skills List of skills
     */
    void setSkills(int characterId, List<Skill> skills);
    
    /**
     * Create default skills for a character
     * @param characterId Character ID
     */
    void createDefaultSkills(int characterId);
    
    /**
     * Clean up skills data for a character when they are removed
     * @param characterId Character ID to clean up
     */
    void cleanupCharacter(int characterId);
}
package combat;

public class Skill {
    public String skillName;
    public int level;
    
    public Skill(String skillName, int level) {
        this.skillName = skillName;
        this.level = level;
    }
    
    public String getSkillName() {
        return skillName;
    }
    
    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
}
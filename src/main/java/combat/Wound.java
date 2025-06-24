package combat;

public class Wound {
    public BodyPart bodyPart;
    public WoundSeverity severity;
    public String projectileName;
    public String weaponId;
    public int damage;
    
    public Wound(BodyPart bodyPart, WoundSeverity severity) {
        this.bodyPart = bodyPart;
        this.severity = severity;
        this.projectileName = "unknown";
        this.weaponId = "unknown";
        this.damage = 1; // Default damage for backwards compatibility
    }
    
    public Wound(BodyPart bodyPart, WoundSeverity severity, String projectileName, String weaponId) {
        this.bodyPart = bodyPart;
        this.severity = severity;
        this.projectileName = projectileName;
        this.weaponId = weaponId;
        this.damage = 1; // Default damage for backwards compatibility
    }
    
    public Wound(BodyPart bodyPart, WoundSeverity severity, String projectileName, String weaponId, int damage) {
        this.bodyPart = bodyPart;
        this.severity = severity;
        this.projectileName = projectileName;
        this.weaponId = weaponId;
        this.damage = damage;
    }
    
    public BodyPart getBodyPart() {
        return bodyPart;
    }
    
    public void setBodyPart(BodyPart bodyPart) {
        this.bodyPart = bodyPart;
    }
    
    public WoundSeverity getSeverity() {
        return severity;
    }
    
    public void setSeverity(WoundSeverity severity) {
        this.severity = severity;
    }
    
    public String getProjectileName() {
        return projectileName;
    }
    
    public void setProjectileName(String projectileName) {
        this.projectileName = projectileName;
    }
    
    public String getWeaponId() {
        return weaponId;
    }
    
    public void setWeaponId(String weaponId) {
        this.weaponId = weaponId;
    }
    
    public int getDamage() {
        return damage;
    }
    
    public void setDamage(int damage) {
        this.damage = damage;
    }
    
    public int getModifier() {
        // Return modifier based on wound severity
        switch (severity) {
            case SCRATCH:
                return -1;
            case LIGHT:
                return -2;
            case SERIOUS:
                return -5;
            case CRITICAL:
                return -10;
            default:
                return 0;
        }
    }
}
package combat;

public class HitResult {
    public boolean hit;
    public BodyPart hitLocation;
    public WoundSeverity woundSeverity;
    public int actualDamage;
    
    public HitResult(boolean hit, BodyPart hitLocation, WoundSeverity woundSeverity, int actualDamage) {
        this.hit = hit;
        this.hitLocation = hitLocation;
        this.woundSeverity = woundSeverity;
        this.actualDamage = actualDamage;
    }
    
    public boolean isHit() {
        return hit;
    }
    
    public BodyPart getHitLocation() {
        return hitLocation;
    }
    
    public WoundSeverity getWoundSeverity() {
        return woundSeverity;
    }
    
    public int getActualDamage() {
        return actualDamage;
    }
}
package combat;

public class Wound {
    public BodyPart bodyPart;
    public WoundSeverity severity;
    
    public Wound(BodyPart bodyPart, WoundSeverity severity) {
        this.bodyPart = bodyPart;
        this.severity = severity;
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
}
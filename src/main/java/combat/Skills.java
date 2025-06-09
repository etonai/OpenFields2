package combat;

public final class Skills {
    public static final String PISTOL = "Pistol";
    public static final String RIFLE = "Rifle";
    public static final String QUICKDRAW = "Quickdraw";
    public static final String MEDICINE = "Medicine";
    
    private Skills() {
        // Utility class - prevent instantiation
    }
    
    public static String[] getAllSkillNames() {
        return new String[]{PISTOL, RIFLE, QUICKDRAW, MEDICINE};
    }
}
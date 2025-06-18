package combat;

public final class Skills {
    public static final String PISTOL = "Pistol";
    public static final String RIFLE = "Rifle";
    public static final String QUICKDRAW = "Quickdraw";
    public static final String MEDICINE = "Medicine";
    public static final String UNARMED = "Unarmed";
    public static final String KNIFE = "Knife";
    public static final String SABRE = "Sabre";
    public static final String TOMAHAWK = "Tomahawk";
    
    private Skills() {
        // Utility class - prevent instantiation
    }
    
    public static String[] getAllSkillNames() {
        return new String[]{PISTOL, RIFLE, QUICKDRAW, MEDICINE, UNARMED, KNIFE, SABRE, TOMAHAWK};
    }
}
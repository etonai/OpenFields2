public class GameScale {
    public static final double PIXELS_PER_FOOT = 7.0;

    public static double pixelsToFeet(double pixels) {
        return pixels / PIXELS_PER_FOOT;
    }

    public static double feetToPixels(double feet) {
        return feet * PIXELS_PER_FOOT;
    }
}

package combat;

public enum FiringMode {
    SINGLE_SHOT,    // Fire one round per trigger pull
    BURST,          // Fire 3 rounds per trigger pull
    FULL_AUTO       // Fire continuously until trigger released or ammo exhausted
}
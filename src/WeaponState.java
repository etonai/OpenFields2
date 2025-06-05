public class WeaponState {
    String state;
    String action;
    int ticks;

    public WeaponState(String state, String action, int ticks) {
        this.state = state;
        this.action = action;
        this.ticks = ticks;
    }

    @Override
    public String toString() {
        return state + " -> " + action + " (" + ticks + " ticks)";
    }
}
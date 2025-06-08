package combat;

public class WeaponState {
    public String state;
    public String action;
    public int ticks;

    public WeaponState(String state, String action, int ticks) {
        this.state = state;
        this.action = action;
        this.ticks = ticks;
    }

    public String getState() {
        return state;
    }
    public String getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "WeaponState{" +
                "state='" + state + '\'' +
                ", action='" + action + '\'' +
                ", ticks=" + ticks +
                '}';
    }
}
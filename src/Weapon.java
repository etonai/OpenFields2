import java.util.List;
import java.util.PriorityQueue;

class WeaponState {
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

class Weapon {
    String name;
    double velocityFeetPerSecond;
    int damage;
    List<WeaponState> states;
    WeaponState currentState;

    public Weapon(String name, double velocityFeetPerSecond, int damage, List<WeaponState> states) {
        this(name, velocityFeetPerSecond, damage, states, (states != null && !states.isEmpty() ? states.get(0) : null));
    }

    public Weapon(String name, double velocityFeetPerSecond, int damage, List<WeaponState> states, String initialStateName) {
        this(name, velocityFeetPerSecond, damage, states,
                states != null ? states.stream().filter(s -> s.state.equals(initialStateName)).findFirst().orElse(states.isEmpty() ? null : states.get(0)) : null);
    }

    public Weapon(String name, double velocityFeetPerSecond, int damage, List<WeaponState> states, WeaponState initialState) {
        this.name = name;
        this.velocityFeetPerSecond = velocityFeetPerSecond;
        this.damage = damage;
        this.states = states;
        this.currentState = (initialState != null) ? initialState : (states != null && !states.isEmpty() ? states.get(0) : null);
    }

    public List<WeaponState> getStates() {
        return states;
    }

    public void resolveRangedAttack(Unit attacker, Unit target, GameClock gameClock, PriorityQueue<ScheduledEvent> eventQueue) {
        if (attacker == null || attacker.character == null || attacker.character.weapon == null) return;

        Weapon weapon = attacker.character.weapon;
        List<WeaponState> states = weapon.getStates();
        WeaponState currentState = weapon.currentState;

        int startIndex = -1;
        for (int i = 0; i < states.size(); i++) {
            if (states.get(i) == currentState) {
                startIndex = i;
                break;
            }
        }

        if (startIndex == -1) return;

        long currentTick = gameClock.getCurrentTick();
        long scheduledTick = currentTick;

        for (int i = startIndex; i < states.size(); i++) {
            WeaponState ws = states.get(i);
            int tickDelay = ws.ticks;
            scheduledTick += tickDelay;

            int finalIndex = i;
            eventQueue.add(new ScheduledEvent(scheduledTick, () -> {
                weapon.currentState = ws;
                System.out.println("Weapon transitioned to: " + ws.state);

                if ("Fire".equals(ws.action)) {
                    System.out.println(attacker.character.name + " fires at " + target.character.name);

                    // Compute distance in feet
                    double dx = target.x - attacker.x;
                    double dy = target.y - attacker.y;
                    double distancePixels = Math.hypot(dx, dy);
                    double distanceFeet = distancePixels / 10.0;

                    // Determine hit or miss
                    boolean willHit = Math.random() * 100 < attacker.character.dexterity;
                    long impactTick = gameClock.getCurrentTick() + Math.round(distanceFeet / weapon.velocityFeetPerSecond * 60);
                    System.out.println("--- Impact scheduled at tick " + impactTick + (willHit ? " (will hit)" : " (will miss)"));

                    // Schedule the impact
                    eventQueue.add(new ScheduledEvent(impactTick, () -> {
                        if (willHit) {
                            target.character.health -= weapon.damage;
                            System.out.println("*** " + attacker.character.name + " hits " + target.character.name + " for " + weapon.damage + " damage");
                        } else {
                            System.out.println("*** " + attacker.character.name + " misses " + target.character.name);
                        }
                    }));

                    // Reset to Aimed
                    for (WeaponState s : states) {
                        if ("Aimed".equals(s.state)) {
                            weapon.currentState = s;
                            break;
                        }
                    }
                }
            }));
        }
    }

    @Override
    public String toString() {
        return "Weapon{name='" + name + "', currentState=" + currentState + ", states=" + states + "}";
    }
}

package game;

import combat.Weapon;

public interface GameCallbacks {
    void playWeaponSound(Weapon weapon);
    void scheduleProjectileImpact(Unit shooter, Unit target, Weapon weapon, long fireTick, double distanceFeet);
    void applyFiringHighlight(Unit shooter, long fireTick);
}
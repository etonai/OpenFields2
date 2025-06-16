package game;

import combat.Weapon;
import combat.MeleeWeapon;
import java.util.List;

public interface GameCallbacks {
    void playWeaponSound(Weapon weapon);
    void scheduleProjectileImpact(Unit shooter, Unit target, Weapon weapon, long fireTick, double distanceFeet);
    void scheduleMeleeImpact(Unit attacker, Unit target, MeleeWeapon weapon, long attackTick);
    void applyFiringHighlight(Unit shooter, long fireTick);
    void addMuzzleFlash(Unit shooter, long fireTick);
    void removeAllEventsForOwner(int ownerId);
    List<Unit> getUnits();
}
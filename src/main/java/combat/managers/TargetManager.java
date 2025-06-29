package combat.managers;

import combat.Character;
import game.interfaces.IUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton manager for character targeting
 * DevCycle 30 - Character class size reduction
 * Follows DevCycle 29 singleton pattern with per-character state tracking
 */
public class TargetManager implements ITargetManager {
    private static TargetManager instance;
    
    // Per-character targeting storage
    private final Map<Integer, IUnit> currentTargets = new HashMap<>();
    private final Map<Integer, IUnit> previousTargets = new HashMap<>();
    private final Map<Integer, IUnit> meleeTargets = new HashMap<>();
    private final Map<Integer, IUnit> reactionTargets = new HashMap<>();
    
    private TargetManager() {
        // Singleton pattern - private constructor
    }
    
    public static TargetManager getInstance() {
        if (instance == null) {
            instance = new TargetManager();
        }
        return instance;
    }
    
    @Override
    public IUnit getCurrentTarget(int characterId) {
        return currentTargets.get(characterId);
    }
    
    @Override
    public void setCurrentTarget(int characterId, IUnit target) {
        currentTargets.put(characterId, target);
    }
    
    @Override
    public IUnit getPreviousTarget(int characterId) {
        return previousTargets.get(characterId);
    }
    
    @Override
    public void setPreviousTarget(int characterId, IUnit target) {
        previousTargets.put(characterId, target);
    }
    
    @Override
    public boolean hasValidTarget(int characterId) {
        IUnit target = getCurrentTarget(characterId);
        return target != null && target.getCharacter() != null && !target.getCharacter().isIncapacitated();
    }
    
    @Override
    public IUnit getMeleeTarget(int characterId) {
        return meleeTargets.get(characterId);
    }
    
    @Override
    public void setMeleeTarget(int characterId, IUnit target) {
        meleeTargets.put(characterId, target);
    }
    
    @Override
    public boolean hasTargetChanged(int characterId, IUnit newTarget) {
        IUnit currentTarget = getCurrentTarget(characterId);
        
        if (currentTarget == null && newTarget == null) {
            return false;
        }
        
        if (currentTarget == null || newTarget == null) {
            return true;
        }
        
        return !currentTarget.equals(newTarget);
    }
    
    @Override
    public IUnit getReactionTarget(int characterId) {
        return reactionTargets.get(characterId);
    }
    
    @Override
    public void setReactionTarget(int characterId, IUnit target) {
        reactionTargets.put(characterId, target);
    }
    
    @Override
    public void cleanupCharacter(int characterId) {
        currentTargets.remove(characterId);
        previousTargets.remove(characterId);
        meleeTargets.remove(characterId);
        reactionTargets.remove(characterId);
    }
}
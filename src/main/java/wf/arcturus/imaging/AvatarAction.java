package wf.arcturus.imaging;

import java.util.*;
import org.restlet.data.*;

public enum AvatarAction
{
    CARRY("crr"), 
    DRINK("drk"), 
    STAND("std"), 
    SIT("sit"), 
    LAY("lay"), 
    WALK("wlk"), 
    WAVE("wav");
    
    public final String key;
    public final List<AvatarAction> illegalWith;
    
    private AvatarAction(final String key) {
        this.illegalWith = new ArrayList<AvatarAction>();
        this.key = key;
    }
    
    public static AvatarAction fromParameter(final Parameter parameter) {
        if (parameter == null) {
            return AvatarAction.STAND;
        }
        for (final AvatarAction action : values()) {
            if (action.key.equalsIgnoreCase(parameter.getValue())) {
                return action;
            }
        }
        return AvatarAction.STAND;
    }
    
    public static AvatarAction fromString(final String key) {
        for (final AvatarAction action : values()) {
            if (action.key.equals(key)) {
                return action;
            }
        }
        return null;
    }
    
    static {
        AvatarAction.STAND.illegalWith.add(AvatarAction.SIT);
        AvatarAction.STAND.illegalWith.add(AvatarAction.LAY);
        AvatarAction.STAND.illegalWith.add(AvatarAction.WALK);
        AvatarAction.SIT.illegalWith.add(AvatarAction.STAND);
        AvatarAction.SIT.illegalWith.add(AvatarAction.LAY);
        AvatarAction.SIT.illegalWith.add(AvatarAction.WALK);
        AvatarAction.LAY.illegalWith.add(AvatarAction.STAND);
        AvatarAction.LAY.illegalWith.add(AvatarAction.SIT);
        AvatarAction.LAY.illegalWith.add(AvatarAction.WALK);
        AvatarAction.LAY.illegalWith.add(AvatarAction.WAVE);
        AvatarAction.WALK.illegalWith.add(AvatarAction.STAND);
        AvatarAction.WALK.illegalWith.add(AvatarAction.SIT);
        AvatarAction.WALK.illegalWith.add(AvatarAction.LAY);
        AvatarAction.WAVE.illegalWith.add(AvatarAction.LAY);
    }
}

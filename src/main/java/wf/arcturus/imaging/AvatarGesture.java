package wf.arcturus.imaging;

import org.restlet.data.*;

public enum AvatarGesture
{
    NORMAL("std"), 
    SMILE("sml"), 
    SAD("sad"), 
    ANGRY("agr"), 
    SURPRISED("srp"), 
    EYEBLINK("eyb"), 
    SPEAK("spk");
    
    public final String key;
    
    private AvatarGesture(final String key) {
        this.key = key;
    }
    
    public static AvatarGesture fromParameter(final Parameter parameter) {
        if (parameter == null) {
            return AvatarGesture.NORMAL;
        }
        for (final AvatarGesture gesture : values()) {
            if (gesture.key.equalsIgnoreCase(parameter.getValue())) {
                return gesture;
            }
        }
        return AvatarGesture.NORMAL;
    }
}

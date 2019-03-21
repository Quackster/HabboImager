package wf.arcturus.imaging;

import java.awt.*;
import org.restlet.data.*;

public enum AvatarSize
{
    SMALL("s", "sh", 1.0, new Point(33, 56)), 
    NORMAL("m", "h", 1.0, new Point(64, 110)), 
    LARGE("l", "h", 2.0, new Point(64, 110)), 
    XLARGE("xl", "h", 3.0, new Point(64, 110));
    
    public final String key;
    public final String prefix;
    public final double resize;
    public final Point size;
    
    private AvatarSize(final String key, final String prefix, final double resize, final Point size) {
        this.key = key;
        this.prefix = prefix;
        this.resize = resize;
        this.size = size;
    }
    
    public static AvatarSize fromParameter(final Parameter parameter) {
        if (parameter == null) {
            return AvatarSize.NORMAL;
        }
        for (final AvatarSize size : values()) {
            if (size.key.equalsIgnoreCase(parameter.getValue())) {
                return size;
            }
        }
        return AvatarSize.NORMAL;
    }
}

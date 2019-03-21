package wf.arcturus.imaging.imagers;

import org.restlet.data.*;

public abstract class Imager
{
    protected Form parameters;
    
    public Imager(final Form parameters) {
        this.parameters = parameters;
    }
    
    public abstract byte[] output();
}

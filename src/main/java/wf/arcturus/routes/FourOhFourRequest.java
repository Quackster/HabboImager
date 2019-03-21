package wf.arcturus.routes;

import org.restlet.data.*;
import org.restlet.representation.*;
import org.restlet.resource.*;

public class FourOhFourRequest extends ServerResource
{
    @Get("text/html")
    public void onGet() {
        final Representation representation = new StringRepresentation("<h1>Habbo Imager</h1><br/>Created by The General. <br />Contact: <br/><ul><li>Skype: wesley.jabbo</li><li>Discord: TheGeneral#0063</li></ul><br />", MediaType.TEXT_HTML);
        this.getResponse().setEntity(representation);
    }
}

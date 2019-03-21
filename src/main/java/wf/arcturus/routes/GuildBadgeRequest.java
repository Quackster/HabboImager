package wf.arcturus.routes;

import wf.arcturus.imaging.imagers.*;
import org.restlet.data.*;
import org.restlet.representation.*;
import org.restlet.resource.*;

public class GuildBadgeRequest extends ServerResource
{
    @Get("image/png")
    public void onGet() {
        final ByteArrayRepresentation bar = new ByteArrayRepresentation(new GuildBadgeImager(this.getRequest().getAttributes().get("badge").toString()).output(), MediaType.IMAGE_PNG);
        this.getResponse().setEntity(bar);
    }
}

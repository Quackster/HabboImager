package wf.arcturus.routes;

import wf.arcturus.*;
import wf.arcturus.imaging.imagers.*;
import org.restlet.data.*;
import org.restlet.representation.*;
import java.nio.file.*;
import java.io.*;
import org.restlet.resource.*;

public class AvatarImagerRequest extends ServerResource
{
    @Get("image/png")
    public void onGet() {
        if (this.getReferrerRef() == null || Imaging.validDomain(this.getReferrerRef().getHostDomain())) {
            final ByteArrayRepresentation bar = new ByteArrayRepresentation(new AvatarImager(this.getQuery()).output(), MediaType.IMAGE_PNG);
            this.getResponse().setEntity(bar);
        }
        else {
            try {
                final ByteArrayRepresentation bar = new ByteArrayRepresentation(Files.readAllBytes(new File("resources/ngh.png").toPath()));
                this.getResponse().setEntity(bar);
            }
            catch (Exception e) {
                this.getResponse().redirectPermanent("/");
            }
            try {
                Files.write(Paths.get("log.txt", new String[0]), ("Hotlink host: " + this.getReferrerRef().getHostDomain() + "").getBytes(), StandardOpenOption.APPEND);
            }
            catch (IOException ex) {}
        }
    }
}

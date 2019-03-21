package wf.arcturus;

import wf.arcturus.database.*;
import wf.arcturus.imaging.*;
import java.io.*;
import wf.arcturus.imaging.imagers.*;
import java.nio.file.*;
import org.restlet.*;
import org.restlet.data.*;
import org.restlet.service.*;
import org.restlet.resource.*;
import wf.arcturus.routes.*;
import java.util.*;

public class Imaging
{
    public static ConfigurationManager configurationManager;
    public static Database database;
    public static GuildParts guildParts;
    public static AvatarParts avatarParts;
    public static List<String> whitelistedDomains;
    
    public static void main(final String[] args) throws Exception {
        System.out.println("Habbo Imaging!");
        final String[] folders = { "cache/avatar/", "cache/badge/", "resources/avatar/", "resources/badge/", "resources/xml/assets/", "resources/xml/fx/" };
        for (int i = 0; i < folders.length; ++i) {
            final File folder = new File(folders[i]);
            folder.mkdirs();
        }
        Imaging.configurationManager = new ConfigurationManager("config_imager.ini");
        Imaging.database = new Database(Imaging.configurationManager);
        Imaging.guildParts = new GuildParts();
        Imaging.avatarParts = new AvatarParts();
        AvatarImager.defaultBytes = Files.readAllBytes(new File("resources/default.png").toPath());
        final Component component = new Component();
        component.getServers().add(Protocol.HTTP, Imaging.configurationManager.value("host"), Imaging.configurationManager.integer("port"));
        component.setLogService(new LogService(false));
        component.getDefaultHost().attachDefault(FourOhFourRequest.class);
        component.getDefaultHost().attach("/habbo-imaging/avatar/", AvatarImagerRequest.class);
        component.getDefaultHost().attach("/habbo-imaging/badge/{badge}", GuildBadgeRequest.class);
        (Imaging.whitelistedDomains = new ArrayList<String>()).add("localhost");
        Imaging.whitelistedDomains.add("nextgenhabbo.com");
        Imaging.whitelistedDomains.add("imaging.nextgenhabbo.com");
        Imaging.whitelistedDomains.add("dev.nextgenhabbo.com");
        Imaging.whitelistedDomains.add("speedhotelli.pw");
        Runtime.getRuntime().gc();
        component.start();
    }
    
    public static boolean validDomain(final String domain) {
        return true;//Imaging.whitelistedDomains.contains(domain);
    }
}

package wf.arcturus.imaging;

import java.awt.image.*;
import wf.arcturus.*;
import java.io.*;
import javax.imageio.*;
import java.sql.*;
import java.util.*;

public class GuildParts
{
    public static final String OUTPUT_FOLDER = "cache/badge/";
    public static final String PARTS_FOLDER = "resources/badge/";
    private final HashMap<Type, HashMap<Integer, Part>> guildParts;
    public HashMap<String, BufferedImage> cachedImages;
    
    public GuildParts() {
        this.guildParts = new HashMap<Type, HashMap<Integer, Part>>();
        this.cachedImages = new HashMap<String, BufferedImage>();
        for (final Type t : Type.values()) {
            this.guildParts.put(t, new HashMap<Integer, Part>());
        }
        try (final Connection connection = Imaging.database.dataSource().getConnection();
             final Statement statement = connection.createStatement();
             final ResultSet set = statement.executeQuery("SELECT * FROM guilds_elements")) {
            while (set.next()) {
                this.guildParts.get(Type.valueOf(set.getString("type").toUpperCase())).put(set.getInt("id"), new Part(set));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        final File file = new File("cache/badge/");
        if (!file.exists()) {
            System.out.println("[BadgeImager] Output folder: cache/badge/ does not exist. Creating!");
            file.mkdirs();
        }
        try {
            for (final Map.Entry<Type, HashMap<Integer, Part>> set2 : this.guildParts.entrySet()) {
                if (set2.getKey() == Type.SYMBOL || set2.getKey() == Type.BASE) {
                    for (final Map.Entry<Integer, Part> map : set2.getValue().entrySet()) {
                        if (!map.getValue().valueA.isEmpty()) {
                            try {
                                this.cachedImages.put(map.getValue().valueA, ImageIO.read(new File("resources/badge/", "badgepart_" + map.getValue().valueA.replace(".gif", ".png"))));
                            }
                            catch (Exception e3) {
                                System.out.println("[Badge Imager] Missing Badge Part: resources/badge//badgepart_" + map.getValue().valueA.replace(".gif", ".png"));
                            }
                        }
                        if (!map.getValue().valueB.isEmpty()) {
                            try {
                                this.cachedImages.put(map.getValue().valueB, ImageIO.read(new File("resources/badge/", "badgepart_" + map.getValue().valueB.replace(".gif", ".png"))));
                            }
                            catch (Exception e3) {
                                System.out.println("[Badge Imager] Missing Badge Part: resources/badge//badgepart_" + map.getValue().valueB.replace(".gif", ".png"));
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e2) {
            e2.printStackTrace();
        }
    }
    
    public boolean symbolColor(final int colorId) {
        for (final Part part : this.getSymbolColors()) {
            if (part.id == colorId) {
                return true;
            }
        }
        return false;
    }
    
    public boolean backgroundColor(final int colorId) {
        for (final Part part : this.getBackgroundColors()) {
            if (part.id == colorId) {
                return true;
            }
        }
        return false;
    }
    
    public HashMap<Type, HashMap<Integer, Part>> getParts() {
        return this.guildParts;
    }
    
    public Collection<Part> getBases() {
        return this.guildParts.get(Type.BASE).values();
    }
    
    public Part getBase(final int id) {
        return this.guildParts.get(Type.BASE).get(id);
    }
    
    public Collection<Part> getSymbols() {
        return this.guildParts.get(Type.SYMBOL).values();
    }
    
    public Part getSymbol(final int id) {
        return this.guildParts.get(Type.SYMBOL).get(id);
    }
    
    public Collection<Part> getBaseColors() {
        return this.guildParts.get(Type.BASE_COLOR).values();
    }
    
    public Part getBaseColor(final int id) {
        return this.guildParts.get(Type.BASE_COLOR).get(id);
    }
    
    public Collection<Part> getSymbolColors() {
        return this.guildParts.get(Type.SYMBOL_COLOR).values();
    }
    
    public Part getSymbolColor(final int id) {
        return this.guildParts.get(Type.SYMBOL_COLOR).get(id);
    }
    
    public Collection<Part> getBackgroundColors() {
        return this.guildParts.get(Type.BACKGROUND_COLOR).values();
    }
    
    public Part getBackgroundColor(final int id) {
        return this.guildParts.get(Type.BACKGROUND_COLOR).get(id);
    }
    
    public Part getPart(final Type type, final int id) {
        return this.guildParts.get(type).get(id);
    }
    
    public static class Part
    {
        public final int id;
        public final String valueA;
        public final String valueB;
        
        public Part(final ResultSet set) throws SQLException {
            this.id = set.getInt("id");
            this.valueA = set.getString("firstvalue");
            this.valueB = set.getString("secondvalue");
        }
    }
    
    public enum Type
    {
        BASE, 
        SYMBOL, 
        BASE_COLOR, 
        SYMBOL_COLOR, 
        BACKGROUND_COLOR;
    }
}

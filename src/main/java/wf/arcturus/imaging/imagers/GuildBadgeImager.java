package wf.arcturus.imaging.imagers;

import org.restlet.data.*;
import java.nio.file.*;
import java.io.*;
import wf.arcturus.*;
import wf.arcturus.imaging.*;
import javax.imageio.*;
import java.awt.image.*;
import java.awt.*;

public class GuildBadgeImager extends Imager
{
    private String badge;
    
    public GuildBadgeImager(final String badge) {
        super(null);
        if (this.validBadge(badge)) {
            this.badge = badge;
        }
    }
    
    @Override
    public byte[] output() {
        try {
            return Files.readAllBytes(this.generate(this.badge).toPath());
        }
        catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
    
    public File generate(final String badge) {
        File outputFile;
        try {
            outputFile = new File("cache/badge/" + badge + ".png");
            if (outputFile.exists()) {
                return outputFile;
            }
        }
        catch (Exception e) {
            return null;
        }
        final String[] parts = { "", "", "", "", "" };
        int count = 0;
        try {
            int i = 0;
            while (i < badge.length()) {
                if (i > 0 && i % 7 == 0) {
                    ++count;
                }
                for (int j = 0; j < 7; ++j) {
                    final StringBuilder sb = new StringBuilder();
                    final String[] array = parts;
                    final int n = count;
                    array[n] = sb.append(array[n]).append(badge.charAt(i)).toString();
                    ++i;
                }
            }
        }
        catch (Exception e2) {
            return null;
        }
        final BufferedImage image = new BufferedImage(39, 39, 2);
        final Graphics graphics = image.getGraphics();
        for (final String s : parts) {
            if (!s.isEmpty()) {
                final String type = s.charAt(0) + "";
                final int id = Integer.valueOf(s.substring(1, 4));
                final int c = Integer.valueOf(s.substring(4, 6));
                final int position = Integer.valueOf(s.substring(6));
                final GuildParts.Part color = Imaging.guildParts.getPart(GuildParts.Type.BASE_COLOR, c);
                GuildParts.Part part;
                if (type.equalsIgnoreCase("b")) {
                    part = Imaging.guildParts.getPart(GuildParts.Type.BASE, id);
                }
                else {
                    part = Imaging.guildParts.getPart(GuildParts.Type.SYMBOL, id);
                }
                BufferedImage imagePart = ImagingUtils.deepCopy(Imaging.guildParts.cachedImages.get(part.valueA));
                if (imagePart != null) {
                    if (imagePart.getColorModel().getPixelSize() < 32) {
                        imagePart = ImagingUtils.convert32(imagePart);
                    }
                    final Point point = ImagingUtils.getPoint(image, imagePart, position);
                    ImagingUtils.recolor(imagePart, ImagingUtils.colorFromHexString(color.valueA));
                    graphics.drawImage(imagePart, point.x, point.y, null);
                }
                if (!part.valueB.isEmpty()) {
                    imagePart = ImagingUtils.deepCopy(Imaging.guildParts.cachedImages.get(part.valueB));
                    if (imagePart != null) {
                        if (imagePart.getColorModel().getPixelSize() < 32) {
                            imagePart = ImagingUtils.convert32(imagePart);
                        }
                        final Point point = ImagingUtils.getPoint(image, imagePart, position);
                        graphics.drawImage(imagePart, point.x, point.y, null);
                    }
                }
            }
        }
        try {
            ImageIO.write(image, "PNG", outputFile);
        }
        catch (Exception e3) {
            return null;
        }
        graphics.dispose();
        return outputFile;
    }
    
    private boolean validBadge(final String badge) {
        return true;
    }
}

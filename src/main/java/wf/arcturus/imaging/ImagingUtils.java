package wf.arcturus.imaging;

import java.util.*;
import java.awt.image.*;
import java.awt.*;

public class ImagingUtils
{
    public static BufferedImage deepCopy(final BufferedImage bi) {
        if (bi == null) {
            return null;
        }
        final ColorModel cm = bi.getColorModel();
        final boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        final WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
    
    public static void recolor(final BufferedImage image, final Color maskColor) {
        for (int x = 0; x < image.getWidth(); ++x) {
            for (int y = 0; y < image.getHeight(); ++y) {
                final int pixel = image.getRGB(x, y);
                if (pixel >> 24 != 0) {
                    Color color = new Color(pixel);
                    final float alpha = color.getAlpha() / 255.0f * (maskColor.getAlpha() / 255.0f);
                    final float red = color.getRed() / 255.0f * (maskColor.getRed() / 255.0f);
                    final float green = color.getGreen() / 255.0f * (maskColor.getGreen() / 255.0f);
                    final float blue = color.getBlue() / 255.0f * (maskColor.getBlue() / 255.0f);
                    color = new Color(red, green, blue, alpha);
                    final int rgb = color.getRGB();
                    image.setRGB(x, y, rgb);
                }
            }
        }
    }
    
    public static Color colorFromHexString(final String colorStr) {
        try {
            return new Color(Integer.valueOf(colorStr, 16));
        }
        catch (Exception e) {
            return new Color(16777215);
        }
    }
    
    public static Point getPoint(final BufferedImage image, final BufferedImage imagePart, final int position) {
        int x = 0;
        int y = 0;
        if (position == 1) {
            x = (image.getWidth() - imagePart.getWidth()) / 2;
            y = 0;
        }
        else if (position == 2) {
            x = image.getWidth() - imagePart.getWidth();
            y = 0;
        }
        else if (position == 3) {
            x = 0;
            y = image.getHeight() / 2 - imagePart.getHeight() / 2;
        }
        else if (position == 4) {
            x = image.getWidth() / 2 - imagePart.getWidth() / 2;
            y = image.getHeight() / 2 - imagePart.getHeight() / 2;
        }
        else if (position == 5) {
            x = image.getWidth() - imagePart.getWidth();
            y = image.getHeight() / 2 - imagePart.getHeight() / 2;
        }
        else if (position == 6) {
            x = 0;
            y = image.getHeight() - imagePart.getHeight();
        }
        else if (position == 7) {
            x = (image.getWidth() - imagePart.getWidth()) / 2;
            y = image.getHeight() - imagePart.getHeight();
        }
        else if (position == 8) {
            x = image.getWidth() - imagePart.getWidth();
            y = image.getHeight() - imagePart.getHeight();
        }
        return new Point(x, y);
    }
    
    public static BufferedImage convert32(final BufferedImage src) {
        final BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), 2);
        final ColorConvertOp cco = new ColorConvertOp(src.getColorModel().getColorSpace(), dest.getColorModel().getColorSpace(), null);
        return cco.filter(src, dest);
    }
}

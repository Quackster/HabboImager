package wf.arcturus.imaging.imagers;

import org.apache.commons.lang3.tuple.Pair;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import wf.arcturus.Imaging;
import wf.arcturus.imaging.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.*;

public class AvatarImager extends Imager
{
    public static String defaultLook;
    public static byte[] defaultBytes;
    private String avatar;
    private AvatarSize size;
    private int direction;
    private int headDirection;
    private boolean bodyMirrored;
    private boolean headMirrored;
    private boolean headOnly;
    private List<AvatarAction> actions;
    private AvatarGesture gesture;
    private int carryItem;
    private int frame;
    private AvatarParts.FX effect;
    
    public AvatarImager(final Form parameters) {
        super(parameters);
        this.avatar = AvatarImager.defaultLook;
        this.size = AvatarSize.NORMAL;
        this.direction = 2;
        this.headDirection = 2;
        this.bodyMirrored = false;
        this.headMirrored = false;
        this.headOnly = true;
        this.actions = new ArrayList<AvatarAction>();
        this.gesture = AvatarGesture.NORMAL;
        this.carryItem = 0;
        this.frame = 0;
        this.effect = null;
        if (this.validAvatar(parameters.getFirstValue("figure"))) {
            this.avatar = parameters.getFirstValue("figure");
        }
        this.size = AvatarSize.fromParameter(parameters.getFirst("size"));
        this.direction = this.directionFromParameter(parameters.getFirst("direction"), 2) % 8;
        this.headDirection = this.directionFromParameter(parameters.getFirst("head_direction"), this.direction) % 8;
        this.headOnly = this.booleanFromParameter(parameters.getFirst("headonly"));
        if ((this.direction >= 4 && this.direction <= 6) || (this.headOnly && this.headDirection >= 4 && this.headDirection <= 6)) {
            this.direction = 6 - this.direction;
            this.bodyMirrored = true;
            if (this.headOnly) {
                this.headDirection = 6 - this.headDirection;
                this.direction = this.headDirection;
            }
        }
        for (final String actionKey : parameters.getValuesArray("action", "std")) {
            final String[] data = actionKey.split("=");
            final AvatarAction action = AvatarAction.fromString(data[0]);
            if (action != null && !this.actions.contains(action)) {
                for (final AvatarAction illegalWith : action.illegalWith) {
                    if (this.actions.contains(illegalWith)) {
                        continue;
                    }
                }
                if (action == AvatarAction.CARRY && data.length >= 2) {
                    try {
                        this.carryItem = Integer.valueOf(data[1]);
                    }
                    catch (Exception ex) {}
                }
                this.actions.add(action);
            }
        }
        if (this.actions.isEmpty()) {
            this.actions.add(AvatarAction.STAND);
        }
        this.gesture = AvatarGesture.fromParameter(parameters.getFirst("gesture"));
        this.frame = Integer.valueOf(parameters.getFirstValue("frame", "0"));
        this.effect = Imaging.avatarParts.effectMap.get(Integer.valueOf(parameters.getFirstValue("effect", "0")));
    }
    
    @Override
    public byte[] output() {
        boolean success = true;
        try {
            return this.generate();
        }
        catch (Exception e) {
            success = false;
            e.printStackTrace();
            if (!success) {
                return AvatarImager.defaultBytes;
            }
        }
        finally {
            if (!success) {
                return AvatarImager.defaultBytes;
            }
        }
        return null;
    }
    
    public byte[] generate() throws Exception {
        if (this.avatar == null || this.avatar.isEmpty()) {
            return new byte[0];
        }
        final boolean debugging = true;
        final AvatarParts parts = Imaging.avatarParts;
        final File output = new File("cache/avatar/" + this.avatar + ".png");
        final Map<AvatarParts.Type, Integer> colors = new HashMap<AvatarParts.Type, Integer>();
        final Map<AvatarParts.Type, Integer> secondaryColor = new HashMap<AvatarParts.Type, Integer>();
        if (!output.exists() || debugging) {
            BufferedImage image;
            if (this.headOnly) {
                if (this.size == AvatarSize.SMALL) {
                    image = new BufferedImage(27, 30, 2);
                }
                else {
                    image = new BufferedImage(52, 62, 2);
                }
            }
            else {
                image = new BufferedImage(this.size.size.x, this.size.size.y, 2);
            }
            final Graphics graphics = image.getGraphics();
            final List<AvatarParts.SetPart> partList = new ArrayList<AvatarParts.SetPart>(5);
            final List<AvatarParts.Type> hiddenLayerList = new ArrayList<AvatarParts.Type>(0);
            if (this.effect != null) {
                hiddenLayerList.addAll(this.effect.remove);
            }
            for (final String part : this.avatar.split("\\.")) {
                final String[] definition = part.split("\\-");
                final AvatarParts.Type type = AvatarParts.Type.fromString(definition[0]);
                if (!this.headOnly || type.location == AvatarParts.BodyLocation.HEAD) {
                    if (definition.length >= 3) {
                        colors.put(type, Integer.valueOf(definition[2]));
                    }
                    else if (definition.length >= 4) {
                        secondaryColor.put(type, Integer.valueOf(definition[3]));
                    }
                    final AvatarParts.Set set = parts.figureData.get(type).sets.get(Integer.valueOf(definition[1]));
                    if (set != null) {
                        for (final Map.Entry<AvatarParts.Type, List<AvatarParts.SetPart>> entry : set.parts.entrySet()) {
                            for (final AvatarParts.SetPart p : entry.getValue()) {
                                if (p.type.rotationIndexOffset.containsKey((p.type.location == AvatarParts.BodyLocation.BODY) ? this.direction : this.headDirection)) {
                                    final AvatarParts.SetPart copy;
                                    final AvatarParts.SetPart clone = copy = p.copy();
                                    copy.order += p.type.rotationIndexOffset.get((p.type.location == AvatarParts.BodyLocation.BODY) ? this.direction : this.headDirection);
                                    partList.add(clone);
                                }
                                else {
                                    partList.add(p);
                                }
                            }
                            for (final AvatarParts.Type t : set.hiddenLayers) {
                                if (!hiddenLayerList.contains(t)) {
                                    hiddenLayerList.add(t);
                                }
                            }
                        }
                    }
                }
            }
            if (this.carryItem > 0 && (this.actions.contains(AvatarAction.CARRY) || this.actions.contains(AvatarAction.DRINK))) {
                partList.add(new AvatarParts.SetPart(this.carryItem, AvatarParts.Type.RIGHT_HAND_ITEM, false, 0, 0));
            }
            Collections.sort(partList);
            if (this.size != AvatarSize.SMALL && this.effect != null) {
                for (final Map.Entry<String, Boolean> entry2 : this.effect.sprites.entrySet()) {
                    try {
                        partList.add(new AvatarParts.SetPart(Integer.valueOf(entry2.getKey().replace("fx" + this.effect.id + "_", "")), AvatarParts.Type.EFFECT, entry2.getValue(), 0, 0));
                    }
                    catch (Exception ex) {}
                }
            }
            for (final AvatarParts.SetPart p2 : partList) {
                if (hiddenLayerList.contains(p2.type)) {
                    continue;
                }
                if (this.headOnly && p2.type.location != AvatarParts.BodyLocation.HEAD) {
                    continue;
                }
                final String name = this.size.prefix + "_%gesture%_" + p2.type.key + ((p2.type == AvatarParts.Type.EFFECT) ? (this.effect.id + "_" + p2.id + "_1_" + (p2.colorable ? Integer.valueOf(this.direction) : "0")) : ("_" + p2.id + "_" + (p2.type.location.equals(AvatarParts.BodyLocation.BODY) ? this.direction : this.headDirection))) + "_" + this.frame;
                Pair<BufferedImage, Point> data = null;
                if (this.gesture != AvatarGesture.NORMAL) {
                    data = parts.assetOffsetMap.get(name.replace("%gesture%", this.gesture.key));
                }
                if (p2.type == AvatarParts.Type.HAIR && this.headDirection - 0 <= 0) {
                    continue;
                }
                if (data == null) {
                    for (final AvatarAction action : this.actions) {
                        if ((p2.type == AvatarParts.Type.LEFT_ARM_LARGE || p2.type == AvatarParts.Type.LEFT_ARM_SMALL) && action == AvatarAction.CARRY) {
                            continue;
                        }
                        data = parts.assetOffsetMap.get(name.replace("%gesture%", action.key));
                        if (data != null) {
                            break;
                        }
                    }
                }
                if (data == null) {
                    data = parts.assetOffsetMap.get(name.replace("%gesture%", AvatarAction.STAND.key));
                }
                if (data == null) {
                    continue;
                }
                BufferedImage partImage = data.getKey();
                if (p2.type != AvatarParts.Type.EYE && (p2.type == AvatarParts.Type.FACIAL_CONTOURS || p2.colorable) && ((colors.containsKey(p2.type.colorFrom) && p2.colorIndex == 1) || (secondaryColor.containsKey(p2.type.colorFrom) && p2.colorIndex == 2))) {
                    final boolean contains = parts.colorMap.containsKey(colors.get(p2.type.colorFrom));
                    if (contains) {
                        partImage = ImagingUtils.deepCopy(partImage);
                        final Color color = parts.colorMap.get((p2.colorIndex == 1) ? colors.get(p2.type.colorFrom) : secondaryColor.get(p2.type.colorFrom));
                        ImagingUtils.recolor(partImage, color);
                    }
                }
                final boolean mirroredPart = false;
                if (this.bodyMirrored) {
                    graphics.drawImage(partImage, image.getWidth() - Math.abs(-data.getValue().x) - (this.headOnly ? ((this.size == AvatarSize.SMALL) ? 2 : 12) : 0), image.getHeight() - data.getValue().y - 10 - (this.headOnly ? ((this.size == AvatarSize.SMALL) ? 25 : 46) : 0), image.getWidth() - Math.abs(-data.getValue().x) - partImage.getWidth(), image.getHeight() - data.getValue().y - 10 + partImage.getHeight(), 0, 0, partImage.getWidth(), partImage.getHeight(), null);
                }
                else {
                    graphics.drawImage(partImage, Math.abs(-data.getValue().x) - (this.headOnly ? ((this.size == AvatarSize.SMALL) ? 2 : 12) : 0), image.getHeight() - data.getValue().y - 10 + (this.headOnly ? ((this.size == AvatarSize.SMALL) ? 25 : 44) : 0), null);
                }
            }
            if (this.size.resize != 1.0) {
                final BufferedImage realOutput = new BufferedImage((int)(image.getWidth() * this.size.resize), (int)(image.getHeight() * this.size.resize), 2);
                final Graphics2D graphics2D = realOutput.createGraphics();
                graphics2D.setComposite(AlphaComposite.Src);
                graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2D.drawImage(image, 0, 0, realOutput.getWidth(), realOutput.getHeight(), null);
                image = realOutput;
                graphics2D.dispose();
            }
            ImageIO.write(image, "PNG", output);
        }
        return Files.readAllBytes(output.toPath());
    }
    
    private int directionFromParameter(final Parameter parameter, final int defaultValue) {
        if (parameter == null) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(parameter.getValue());
        }
        catch (Exception ex) {
            return defaultValue;
        }
    }
    
    private int frameFromParameter(final Parameter parameter) {
        if (parameter == null) {
            return 0;
        }
        try {
            return Integer.valueOf(parameter.getValue());
        }
        catch (Exception ex) {
            return 0;
        }
    }
    
    private boolean validAvatar(final String look) {
        return true;
    }
    
    private boolean booleanFromParameter(final Parameter parameter) {
        return parameter != null && parameter.getValue() != null && parameter.getValue().equalsIgnoreCase("1");
    }
    
    static {
        AvatarImager.defaultLook = "hr-893-45.hd-180-2.ch-210-66.lg-270-82.sh-300-91.wa-2007";
        AvatarImager.defaultBytes = new byte[0];
    }
}

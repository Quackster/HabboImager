package wf.arcturus.imaging;

import java.awt.image.*;
import java.awt.*;
import java.io.*;
import javax.imageio.*;
import javax.xml.parsers.*;

import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.*;
import java.util.*;
import java.util.List;

public class AvatarParts
{
    public static final String XML_FOLDER = "resources/xml/";
    public static final String AVATAR_FOLDER = "resources/avatar/";
    public static final String FIGURE_DATA = "figuredata.xml";
    public static final String FIGURE_MAP = "figuremap.xml";
    public Map<Type, SetType> figureData;
    public Map<Integer, String> figureMap;
    public Map<String, Pair<BufferedImage, Point>> assetOffsetMap;
    public Map<Integer, Color> colorMap;
    public Map<Integer, FX> effectMap;
    
    public AvatarParts() throws Exception {
        this.figureData = new HashMap<Type, SetType>();
        this.figureMap = new HashMap<Integer, String>();
        this.assetOffsetMap = new HashMap<String, Pair<BufferedImage, Point>>();
        this.colorMap = new HashMap<Integer, Color>();
        this.effectMap = new HashMap<Integer, FX>();
        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new File("resources/xml/figuremap.xml"));
        doc.getDocumentElement().normalize();
        final NodeList clothingList = doc.getElementsByTagName("lib");
        for (int index = 0; index < clothingList.getLength(); ++index) {
            final Node node = clothingList.item(index);
            if (node.getNodeType() == 1) {
                if (!((Element)node).getAttribute("id").startsWith("hh_")) {
                    final NodeList libNodes = node.getChildNodes();
                    for (int childIndex = 0; childIndex < libNodes.getLength(); ++childIndex) {
                        final Node childNode = libNodes.item(childIndex);
                        if (childNode.getNodeType() == 1) {
                            this.figureMap.put(Integer.valueOf(((Element)childNode).getAttribute("id")), ((Element)node).getAttribute("id"));
                        }
                    }
                }
            }
        }
        doc = dBuilder.parse(new File("resources/xml/figuredata.xml"));
        doc.getDocumentElement().normalize();
        final NodeList paletteList = doc.getElementsByTagName("palette");
        for (int index2 = 0; index2 < paletteList.getLength(); ++index2) {
            final Node nNode = paletteList.item(index2);
            if (nNode.getNodeType() == 1) {
                final Element eElement = (Element)nNode;
                final NodeList childNodes = eElement.getChildNodes();
                for (int childNodeIndex = 0; childNodeIndex < childNodes.getLength(); ++childNodeIndex) {
                    final Node childNode2 = childNodes.item(childNodeIndex);
                    if (childNode2.getNodeType() == 1) {
                        this.colorMap.put(Integer.valueOf(((Element)childNode2).getAttribute("id")), Color.decode("#" + childNode2.getTextContent()));
                    }
                }
            }
        }
        final NodeList setTypesList = doc.getElementsByTagName("settype");
        for (int index3 = 0; index3 < setTypesList.getLength(); ++index3) {
            final Node setList = setTypesList.item(index3);
            if (setList.getNodeType() == 1) {
                final SetType setType = new SetType(Type.fromString(((Element)setList).getAttribute("type")), Integer.valueOf(((Element)setList).getAttribute("paletteid")));
                final NodeList setsList = setList.getChildNodes();
                for (int childNodeIndex2 = 0; childNodeIndex2 < setsList.getLength(); ++childNodeIndex2) {
                    final Node setNode = setsList.item(childNodeIndex2);
                    if (setNode.getNodeType() == 1) {
                        try {
                            final Set set = new Set(Integer.valueOf(((Element)setNode).getAttribute("id")), this.figureMap.get(Integer.valueOf(((Element)setNode).getAttribute("id"))), ((Element)setNode).getAttribute("gender"), Integer.valueOf(((Element)setNode).getAttribute("club")), ((Element)setNode).getAttribute("colorable").equals("1"), ((Element)setNode).getAttribute("selectable").equals("1"), ((Element)setNode).getAttribute("preselectable").equals("1"));
                            setType.sets.put(set.id, set);
                            final NodeList partsList = ((Element)setNode).getElementsByTagName("part");
                            for (int partsIndex = 0; partsIndex < partsList.getLength(); ++partsIndex) {
                                final Node partNode = partsList.item(partsIndex);
                                if (partNode.getNodeType() == 1) {
                                    final SetPart part = new SetPart(Integer.valueOf(((Element)partNode).getAttribute("id")), Type.fromString(((Element)partNode).getAttribute("type")), ((Element)partNode).getAttribute("colorable").equals("1"), Integer.valueOf(((Element)partNode).getAttribute("index")), Integer.valueOf(((Element)partNode).getAttribute("colorindex")));
                                    set.addPart(part);
                                }
                            }
                            final NodeList hiddenLayerList = ((Element)setNode).getElementsByTagName("layer");
                            for (int hiddenLayerIndex = 0; hiddenLayerIndex < hiddenLayerList.getLength(); ++hiddenLayerIndex) {
                                final Node hiddenLayer = hiddenLayerList.item(hiddenLayerIndex);
                                if (hiddenLayer.getNodeType() == 1) {
                                    set.hiddenLayers.add(Type.fromString(((Element)hiddenLayer).getAttribute("parttype")));
                                }
                            }
                        }
                        catch (Exception ex) {}
                    }
                }
                this.figureData.put(setType.type, setType);
            }
        }
        for (final File file : new File("resources/xml/assets/").listFiles()) {
            try {
                if (file.getName().contains(".bin")) {
                    doc = dBuilder.parse(file);
                    doc.getDocumentElement().normalize();
                    final NodeList assetNodes = doc.getElementsByTagName("asset");
                    for (int index4 = 0; index4 < assetNodes.getLength(); ++index4) {
                        final Node node2 = assetNodes.item(index4);
                        if (node2.getNodeType() == 1 && ((Element)node2).getAttribute("mimeType").equalsIgnoreCase("image/png")) {
                            final Element e = (Element)((Element)node2).getElementsByTagName("param").item(0);
                            if (e != null) {
                                final String offset = e.getAttribute("value");
                                if (!offset.isEmpty()) {
                                    final String[] data = offset.split(",");
                                    if (data.length == 2) {
                                        final File f = new File("resources/avatar/" + ((Element)node2).getAttribute("name") + ".png");
                                        if (f.exists()) {
                                            try {
                                                this.assetOffsetMap.put(((Element)node2).getAttribute("name"), Pair.of(ImageIO.read(f), new Point(Integer.valueOf(data[0]), Integer.valueOf(data[1]))));
                                            }
                                            catch (Exception ex2) {}
                                        }
                                    }
                                }
                            }
                        }
                        if (file.getName().equalsIgnoreCase("hh_human_item_manifest.bin")) {}
                    }
                }
            }
            catch (Exception ex3) {}
        }
        final File effectMap = new File("resources/xml/effectmap.xml");
        if (!effectMap.exists()) {
            throw new Exception("effectmap.xml is missing!");
        }
        doc = dBuilder.parse(effectMap);
        doc.getDocumentElement().normalize();
        final NodeList effectNodes = doc.getElementsByTagName("effect");
        for (int index5 = 0; index5 < effectNodes.getLength(); ++index5) {
            final Node node3 = effectNodes.item(index5);
            if (node3.getNodeType() == 1 && ((Element)node3).getAttribute("type").equalsIgnoreCase("fx")) {
                final int id = Integer.valueOf(((Element)node3).getAttribute("id"));
                final String name = ((Element)node3).getAttribute("lib");
                this.effectMap.put(id, new FX(id, name));
            }
        }
        for (final Map.Entry<Integer, FX> effectEntry : this.effectMap.entrySet()) {
            final File animationFile = new File("resources/xml/fx/" + effectEntry.getValue().name + "_animation.bin");
            try {
                doc = dBuilder.parse(animationFile);
                doc.getDocumentElement().normalize();
                NodeList assetNodes2 = doc.getElementsByTagName("sprite");
                final Map<String, Boolean> sprites = new HashMap<String, Boolean>();
                for (int index6 = 0; index6 < assetNodes2.getLength(); ++index6) {
                    final Node node4 = assetNodes2.item(index6);
                    if (node4.getNodeType() == 1) {
                        sprites.put(((Element)node4).getAttribute("id"), ((Element)node4).hasAttribute("directions") && ((Element)node4).getAttribute("directions").equals("1"));
                    }
                }
                final List<Type> remove = new ArrayList<Type>();
                assetNodes2 = doc.getElementsByTagName("remove");
                for (int index7 = 0; index7 < assetNodes2.getLength(); ++index7) {
                    final Node node5 = assetNodes2.item(index7);
                    if (node5.getNodeType() == 1) {
                        remove.add(Type.fromString(((Element)node5).getAttribute("id")));
                    }
                }
                effectEntry.getValue().remove = remove;
                effectEntry.getValue().sprites = sprites;
            }
            catch (Exception ex4) {}
            final File assetsFile = new File("resources/xml/fx/" + effectEntry.getValue().name + "_manifest.bin");
            try {
                if (!assetsFile.getName().contains(".bin")) {
                    continue;
                }
                doc = dBuilder.parse(assetsFile);
                doc.getDocumentElement().normalize();
                final NodeList assetNodes3 = doc.getElementsByTagName("asset");
                for (int index6 = 0; index6 < assetNodes3.getLength(); ++index6) {
                    final Node node4 = assetNodes3.item(index6);
                    if (node4.getNodeType() == 1 && ((Element)node4).getAttribute("mimeType").equalsIgnoreCase("image/png")) {
                        final Element e2 = (Element)((Element)node4).getElementsByTagName("param").item(0);
                        if (e2 != null) {
                            final String offset2 = e2.getAttribute("value");
                            if (!offset2.isEmpty()) {
                                final String[] data2 = offset2.split(",");
                                if (data2.length == 2) {
                                    final File f2 = new File("resources/avatar/" + ((Element)node4).getAttribute("name") + ".png");
                                    if (f2.exists()) {
                                        try {
                                            this.assetOffsetMap.put(((Element)node4).getAttribute("name"), Pair.of(ImageIO.read(f2), new Point(Integer.valueOf(data2[0]), Integer.valueOf(data2[1]))));
                                        }
                                        catch (Exception ex5) {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception ex6) {}
        }
        System.out.println("Loaded " + this.assetOffsetMap.size() + " assets!");
        System.out.println("Loaded " + this.effectMap.size() + " effects!");
    }
    
    public static class SetType
    {
        public final Type type;
        public final int paletteId;
        public final Map<Integer, Set> sets;
        
        public SetType(final Type type, final int paletteId) {
            this.sets = new HashMap<Integer, Set>(10);
            this.type = type;
            this.paletteId = paletteId;
        }
    }
    
    public class Set
    {
        public final int id;
        public final String name;
        public final String gender;
        public final int club;
        public final boolean colorable;
        public final boolean selectable;
        public final boolean preselectable;
        public final Map<Type, List<SetPart>> parts;
        public final List<Type> hiddenLayers;
        
        public Set(final int id, final String name, final String gender, final int club, final boolean colorable, final boolean selectable, final boolean preselectable) {
            this.parts = new HashMap<Type, List<SetPart>>(1);
            this.hiddenLayers = new ArrayList<Type>(0);
            this.id = id;
            this.name = name;
            this.gender = gender;
            this.club = club;
            this.colorable = colorable;
            this.selectable = selectable;
            this.preselectable = preselectable;
        }
        
        public void addPart(final SetPart part) {
            if (!this.parts.containsKey(part.type)) {
                this.parts.put(part.type, new ArrayList<SetPart>());
            }
            this.parts.get(part.type).add(part);
        }
    }
    
    public static class SetPart implements Comparable<SetPart>
    {
        public final int id;
        public final Type type;
        public final boolean colorable;
        public final int index;
        public final int colorIndex;
        public int order;
        
        public SetPart(final int id, final Type type, final boolean colorable, final int index, final int colorIndex) {
            this.id = id;
            this.type = type;
            this.colorable = colorable;
            this.index = index;
            this.colorIndex = colorIndex;
            this.order = this.type.order;
        }
        
        @Override
        public String toString() {
            return "SetPart (id: " + this.id + ", type: " + this.type + ")";
        }
        
        @Override
        public int compareTo(final SetPart o) {
            return this.order - o.order;
        }
        
        public SetPart copy() {
            return new SetPart(this.id, this.type, this.colorable, this.index, this.colorIndex);
        }
    }
    
    public enum BodyLocation
    {
        HEAD, 
        BODY;
    }
    
    public enum Type
    {
        SHOES("sh", BodyLocation.BODY, 5, false), 
        LEGS("lg", BodyLocation.BODY, 6, false), 
        CHEST("ch", BodyLocation.BODY, 7, false), 
        WAIST("wa", BodyLocation.BODY, 8, false), 
        CHEST_ACCESSORY("ca", BodyLocation.BODY, 9, false), 
        FACE_ACCESSORY("fa", BodyLocation.HEAD, 27, false), 
        EYE_ACCESSORY("ea", BodyLocation.HEAD, 28, false), 
        HEAD_ACCESSORY("ha", BodyLocation.HEAD, 29, false), 
        HEAD_EXTRA("he", BodyLocation.HEAD, 20, false), 
        CHEST_COVER("cc", BodyLocation.BODY, 21, false), 
        CHEST_PIECE("cp", BodyLocation.BODY, 6, false), 
        FACIAL_CONTOURS("fc", BodyLocation.HEAD, 23, false), 
        HEAD("hd", BodyLocation.HEAD, 22, true), 
        BODY("bd", BodyLocation.BODY, 1, true), 
        HAIR("hr", BodyLocation.HEAD, 24, false), 
        LEFT_ARM_LARGE("lh", BodyLocation.BODY, 5, true), 
        LEFT_ARM_SMALL("ls", BodyLocation.BODY, 6, false), 
        RIGHT_ARM_LARGE("rh", BodyLocation.BODY, 10, false), 
        RIGHT_ARM_SMALL("rs", BodyLocation.BODY, 11, false), 
        EYE("ey", BodyLocation.HEAD, 24, false), 
        LEFT_HAND_ITEM("li", BodyLocation.BODY, 0, false), 
        HAIR_BACK("hrb", BodyLocation.HEAD, 26, false), 
        RIGHT_HAND_ITEM("ri", BodyLocation.BODY, 26, true), 
        LEFT_ARM_CARRY("lc", BodyLocation.BODY, 23, true), 
        RIGHT_ARM_CARRY("rc", BodyLocation.BODY, 24, true), 
        EFFECT("fx", BodyLocation.BODY, 100, false);
        
        public final String key;
        public final int order;
        public final BodyLocation location;
        public Type colorFrom;
        public final Map<Integer, Integer> rotationIndexOffset;
        
        private Type(final String key, final BodyLocation location, final int order, final boolean unused) {
            this.rotationIndexOffset = new HashMap<Integer, Integer>();
            this.key = key;
            this.location = location;
            this.order = order;
            this.colorFrom = this;
        }
        
        public static Type fromString(final String key) {
            for (final Type type : values()) {
                if (type.key.equalsIgnoreCase(key)) {
                    return type;
                }
            }
            throw new RuntimeException("Failed to find Type of : " + key);
        }
        
        static {
            Type.FACIAL_CONTOURS.colorFrom = Type.HEAD;
            Type.BODY.colorFrom = Type.HEAD;
            Type.HAIR_BACK.colorFrom = Type.HAIR;
            Type.LEFT_ARM_CARRY.colorFrom = Type.HEAD;
            Type.RIGHT_ARM_CARRY.colorFrom = Type.HEAD;
            Type.HAIR_BACK.colorFrom = Type.HAIR;
            Type.LEFT_ARM_LARGE.colorFrom = Type.HEAD;
            Type.RIGHT_ARM_LARGE.colorFrom = Type.HEAD;
            Type.LEFT_ARM_SMALL.colorFrom = Type.CHEST;
            Type.RIGHT_ARM_SMALL.colorFrom = Type.CHEST;
            Type.LEFT_ARM_SMALL.rotationIndexOffset.put(3, 1);
        }
    }
    
    public static class Palette
    {
        public final int id;
        public Map<Integer, PaletteColor> colorMap;
        
        public Palette(final int id) {
            this.colorMap = new HashMap<Integer, PaletteColor>();
            this.id = id;
        }
    }
    
    public static class PaletteColor
    {
        public final int id;
        public final int index;
        public final int club;
        public final boolean selectable;
        public final Color color;
        
        public PaletteColor(final int id, final int index, final int club, final boolean selectable, final Color color) {
            this.id = id;
            this.index = index;
            this.club = club;
            this.selectable = selectable;
            this.color = color;
        }
    }
    
    public static class FX
    {
        public final int id;
        public final String name;
        public Map<String, Boolean> sprites;
        public List<Type> remove;
        
        public FX(final int id, final String name) {
            this.sprites = new HashMap<String, Boolean>();
            this.remove = new ArrayList<Type>();
            this.id = id;
            this.name = name;
        }
    }
}

/*  MUD Map (v2) - A tool to create and organize maps for text-based games
 *  Copyright (C) 2016  Neop (email: mneop@web.de)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package mudmap2.backend.WorldFileReader.current;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import mudmap2.backend.PlaceGroup;
import mudmap2.backend.Label;
import mudmap2.backend.Layer;
import mudmap2.backend.Path;
import mudmap2.backend.Place;
import mudmap2.backend.RiskLevel;
import mudmap2.backend.World;
import mudmap2.backend.WorldCoordinate;
import mudmap2.backend.WorldFileReader.WorldFile;
import mudmap2.backend.WorldFileReader.Exception.WorldFileInvalidTypeException;
import mudmap2.backend.WorldFileReader.Exception.WorldFileReadError;
import mudmap2.backend.WorldFileReader.WorldFileType;
import org.json.*;

/**
 *
 * @author neop
 */
public class WorldFileJSON extends WorldFile {

    static Integer versionMajor = 2;
    static Integer versionMinor = 0;

    JSONObject fileRoot = null;

    HashMap<Integer, Integer> layerIDs;

    JSONObject metaData;
    WorldMetaJSON metaWriter;

    /**
     * Constructor
     * @param filename world filename with path
     */
    public WorldFileJSON(String filename) {
        super(filename);
    }

    public void setMetaGetter(WorldMetaJSON meta) {
        this.metaWriter = meta;
    }

    public JSONObject getMetaData() {
        return metaData;
    }

    /**
     * Read file to JSONObject
     * @return JSONObject
     * @throws IOException
     */
    private JSONObject getJSONRoot() throws IOException{
        if(fileRoot == null && (new File(filename)).isFile()){
            byte[] bytes = Files.readAllBytes(Paths.get(filename));
            String lines = new String(bytes);
            fileRoot = new JSONObject(lines);
        }
        return fileRoot;
    }

    public Integer translateLayerID(Integer layer){
        if(layerIDs == null) return null;
        return layerIDs.get(layer);
    }

    /**
     * Read world file
     * @return new world object or null if file is invalid
     * @throws Exception
     * @throws WorldFileInvalidTypeException
     * @ throws WorldFileReadError
     */
    @Override
    public World readFile() throws Exception {
        World world = null;

        try{
            JSONObject root = getJSONRoot();

            // check file version
            if(root.has("fileVer")){
                String[] fileVer = root.getString("fileVer").split("\\.");
                if(versionMajor != Integer.parseInt(fileVer[0])){
                    // version major not equal: different file format
                    throw new WorldFileInvalidTypeException(filename,
                            "invalid world file version", null);
                }
                if(versionMinor < Integer.parseInt(fileVer[1])){
                    // file was created by a newer MUD Map: might have unsupported features
                    int ret = JOptionPane.showConfirmDialog(null,
                            "World file version is greater than the reader version. "
                            + "Please update MUD Map. Continuing might cause data loss.",
                            "Loading world", JOptionPane.OK_CANCEL_OPTION);
                    if(ret == JOptionPane.CANCEL_OPTION)
                        throw new WorldFileInvalidTypeException(filename, "Could not read world file", null);
                }
            } else {
                throw new WorldFileInvalidTypeException(filename,
                        "could not read world file version", null);
            }

            // getPlace world name
            String worldName = "";
            if(root.has("worldName")) worldName = root.getString("worldName");
            if(worldName.isEmpty()) {
                Integer begin = worldName.lastIndexOf('/');
                if(begin == -1) begin = worldName.lastIndexOf('\\');
                begin += 1;
                worldName = worldName.substring(begin);
            }

            // create world root
            world = new World(worldName);
            world.setWorldFile(this);

            // showPlaceID
            if(root.has("showPlaceID")){
                world.setShowPlaceID(World.ShowPlaceID.valueOf(root.getString("showPlaceID")));
            }

            // tileCenterCol
            if(root.has("tileCenterCol")){
                world.setTileCenterColor(hexToCol(root.getString("tileCenterCol")));
            }

            // pathCol
            if(root.has("pathCol")){
                world.setPathColor(hexToCol(root.getString("pathCol")));
            }

            // pathColNonCardinal
            if(root.has("pathColNonCardinal")){
                world.setPathColorNstd(hexToCol(root.getString("pathColNonCardinal")));
            }

            // pathColDefs
            if(root.has("pathColDefs")){
                JSONArray pathColDefs = root.getJSONArray("pathColDefs");
                Integer length = pathColDefs.length();
                for(Integer i = 0; i < length; ++i){
                    JSONObject pathColDef = pathColDefs.getJSONObject(i);
                    if(pathColDef.has("path")
                            && pathColDef.has("col")){
                        world.setPathColor(pathColDef.getString("path"),
                                hexToCol(pathColDef.getString("col")));
                    }
                }
            }

            // home
            if(root.has("home")){
                JSONObject home = root.getJSONObject("home");
                if(home.has("l") && home.has("x") && home.has("y")){
                    Integer l = home.getInt("l");
                    Double x = home.getDouble("x");
                    Double y = home.getDouble("y");
                    world.setHome(new WorldCoordinate(l, x, y));
                }
            }

            // riskLevels
            if(root.has("riskLevels")){
                // remove existing risk levels
                world.getRiskLevels().clear();

                JSONArray riskLevels = root.getJSONArray("riskLevels");
                Integer length = riskLevels.length();
                for(Integer i = 0; i < length; ++i){
                    JSONObject riskLevel = riskLevels.getJSONObject(i);
                    if(riskLevel.has("id")
                            && riskLevel.has("desc")
                            && riskLevel.has("col")){
                        Integer id = riskLevel.getInt("id");
                        String desc = riskLevel.getString("desc");
                        Color col = hexToCol(riskLevel.getString("col"));
                        world.setRiskLevel(new RiskLevel(id, desc, col));
                    }
                }
            }

            // areaArray
            HashMap<Integer, PlaceGroup> areas = new HashMap<>();

            if(root.has("areas")){
                JSONArray areaArray = root.getJSONArray("areas");
                Integer length = areaArray.length();
                for(Integer i = 0; i < length; ++i){
                    JSONObject area = areaArray.getJSONObject(i);
                    if(area.has("id")
                            && area.has("name")
                            && area.has("col")){
                        Integer id = area.getInt("id");
                        String name = area.getString("name");
                        Color col = hexToCol(area.getString("col"));

                        PlaceGroup a = new PlaceGroup(name, col);
                        areas.put(id, a);
                        world.addPlaceGroup(a);
                    }
                }
            }

            // layers
            if(root.has("layers")){
                JSONArray layers = root.getJSONArray("layers");
                Integer length = layers.length();
                for(Integer i = 0; i < length; ++i){
                    JSONObject layer = layers.getJSONObject(i);
                    if(layer.has("id")){
                        Integer id = layer.getInt("id");
                        // create layer
                        Layer l = new Layer(id, world);

                        if(layer.has("centerX") && layer.has("centerY")){
                            // set quadtree center
                            Integer centerX = layer.getInt("centerX");
                            Integer centerY = layer.getInt("centerY");
                            l.setQuadtree(centerX, centerY);
                        }
                        if(layer.has("name")){
                            // set layer name
                            l.setName(layer.getString("name"));
                        }
                        world.addLayer(l);
                    }
                }
            }

            // places
            HashMap<Place, HashSet<Integer>> childrenMapping = new HashMap<>();
            //HashMap<Place, HashSet<Integer>> parentMapping = new HashMap<>();

            if(root.has("places")){
                JSONArray places = root.getJSONArray("places");
                Integer length = places.length();
                for(Integer i = 0; i < length; ++i){
                    JSONObject place = places.getJSONObject(i);
                    if(place.has("id")
                            && place.has("n")
                            && place.has("l")
                            && place.has("x")
                            && place.has("y")){
                        Integer id = place.getInt("id");
                        String name = place.getString("n");
                        Integer layer = place.getInt("l");
                        Integer x = place.getInt("x");
                        Integer y = place.getInt("y");
                        // create place
                        Place p = new Place(id, name, x, y, world.getLayer(layer));

                        // area
                        if(place.has("a")){
                            Integer area = place.getInt("a");
                            p.setPlaceGroup(areas.get(area));
                        }
                        // risk level
                        if(place.has("r")){
                            Integer risk = place.getInt("r");
                            p.setRiskLevel(world.getRiskLevel(risk));
                        }
                        // rec level
                        if(place.has("lvlMin")){
                            Integer lvlMin = place.getInt("lvlMin");
                            p.setRecLevelMin(lvlMin);
                        }
                        if(place.has("lvlMax")){
                            Integer lvlMax = place.getInt("lvlMax");
                            p.setRecLevelMin(lvlMax);
                        }

                        // children
                        if(place.has("c")){
                            JSONArray children = place.getJSONArray("c");
                            HashSet<Integer> set = new HashSet<>();
                            childrenMapping.put(p, set);

                            Integer lc = children.length();
                            for(Integer c = 0; c < lc; ++c){
                                set.add(children.getInt(c));
                            }
                        }

                        // parents
                        /* don't need to read parents: implicitly defined by children relations
                        if(place.has("p")){
                            JSONArray parents = place.getJSONArray("p");
                            HashSet<Integer> set = new HashSet<>();
                            parentMapping.putPlace(p, set);

                            Integer lp = parents.length();
                            for(Integer pa = 0; pa < lp; ++pa){
                                set.add(parents.getInt(pa));
                            }
                        }*/

                        // flags
                        if(place.has("f")){
                            JSONArray flags = place.getJSONArray("f");
                            Integer lf = flags.length();
                            for(Integer f = 0; f < lf; ++f){
                                String flagname = flags.getString(f);
                                p.setFlag(flagname, true);
                            }
                        }

                        // comments
                        if(place.has("co")){
                            JSONArray comments = place.getJSONArray("co");
                            Integer lc = comments.length();
                            for(Integer c = 0; c < lc; ++c){
                                p.addComment(comments.getString(c));
                            }
                        }

                        world.putPlace(p);
                    }
                }
            }

            // connect children
            for(Entry<Place, HashSet<Integer>> entry: childrenMapping.entrySet()){
                Place place = entry.getKey();
                for(Integer id: entry.getValue()){
                    place.connectChild(world.getPlace(id));
                }
            }

            // paths
            if(root.has("paths")){
                JSONArray paths = root.getJSONArray("paths");
                Integer length = paths.length();
                for(Integer i = 0; i < length; ++i){
                    JSONArray path = paths.getJSONArray(i);
                    if(path.length() == 2){
                        JSONObject p0 = path.getJSONObject(0);
                        JSONObject p1 = path.getJSONObject(1);
                        if(p0.has("p") && p0.has("e")
                                && p1.has("p") && p1.has("e")){
                            Place pl0 = world.getPlace(p0.getInt("p"));
                            Place pl1 = world.getPlace(p1.getInt("p"));
                            if(pl0 != null && pl1 != null){
                                Path p = new Path(pl0, p0.getString("e"),
                                        pl1, p1.getString("e"));
                                pl0.connectPath(p);
                            }
                        }
                    }
                }
            }

            // labels
            if(root.has("labels")){
                JSONArray labels = root.getJSONArray("labels");
                Integer length = labels.length();
                for(Integer i = 0; i < length; ++i){
                    JSONObject label = labels.getJSONObject(i);
                    if(label.has("x")
                            && label.has("y")
                            && label.has("l")
                            && label.has("fontSize")
                            && label.has("t")){
                        Double x = label.getDouble("x");
                        Double y = label.getDouble("y");
                        Layer l = world.getLayer(label.getInt("l"));
                        Double fontSize = label.getDouble("fontSize");
                        String text = label.getString("t");
                        if(l != null){
                            world.addLabel(new Label(text, x, y, l, fontSize));
                        }
                    }
                }
            }

            // remember meta data for WorldTab
            if(root.has("meta")) metaData = root.getJSONObject("meta");

        } catch(JSONException ex) {
            System.out.println(ex.getLocalizedMessage());
            throw new WorldFileReadError(filename, ex.getLocalizedMessage(), ex);
        }

        return world;
    }

    /**
     * Convert color to rgb hex string
     * @param col
     * @return
     */
    private String colToHex(Color col){
        return "#" + Integer.toHexString(col.getRGB()).substring(2);
    }

    /**
     * Convert rgb hex string to color
     * @param hex
     * @return
     */
    private Color hexToCol(String hex){
        return new Color(Integer.decode(hex));
    }

    /**
     * Write world to file
     * @param world
     * @throws java.io.IOException
     */
    @Override
    public void writeFile(World world) throws IOException {
        JSONObject root = new JSONObject();

        // metaWriter data
        // mudmap version
        String mudmapVer = getClass().getPackage().getImplementationVersion();
        if(mudmapVer != null)
            root.put("mudmapVer", getClass().getPackage().getImplementationVersion());
        else
            root.put("mudmapVer", "dev");

        // file version
        root.put("fileVer", versionMajor + "." + versionMinor);

        // world name
        root.put("worldName", world.getName());

        root.put("showPlaceID", world.getShowPlaceId());

        // tile center color
        root.put("tileCenterCol", colToHex(world.getTileCenterColor()));
        // cardinal and non cardinal path color
        root.put("pathCol", colToHex(world.getPathColor()));
        root.put("pathColNonCardinal", colToHex(world.getPathColorNstd()));
        // other path colors
        JSONArray pathColorsArray = new JSONArray();
        root.put("pathColDefs", pathColorsArray);
        for(Map.Entry<String, Color> pathCol: world.getPathColors().entrySet()){
            JSONObject pathColObj = new JSONObject();
            pathColObj.put("path", pathCol.getKey());
            pathColObj.put("col", colToHex(pathCol.getValue()));
            pathColorsArray.put(pathColObj);
        }

        // risk level colors
        JSONArray riskLevelColors = new JSONArray();
        root.put("riskLevels", riskLevelColors);
        for(RiskLevel rlc: world.getRiskLevels()){
            JSONObject rlo = new JSONObject();
            rlo.put("id", rlc.getId());
            rlo.put("desc", rlc.getDescription());
            rlo.put("col", colToHex(rlc.getColor()));
            riskLevelColors.put(rlo);
        }

        // areaArray
        // create IDs for areaArray
        HashMap<PlaceGroup, Integer> areaIDs = new HashMap<>();
        Integer cnt = 0; // incremental id
        for(PlaceGroup a: world.getPlaceGroups()){
            Boolean inUse = false;
            // removePlace unused
            for(Place place: world.getPlaces()){
                if(place.getPlaceGroup() == a){
                    inUse = true;
                    break;
                }
            }
            if(inUse) areaIDs.put(a, ++cnt);
        }

        // add areaArray
        JSONArray areas = new JSONArray();
        root.put("areas", areas);
        for(PlaceGroup area: world.getPlaceGroups()){
            JSONObject areaObj = new JSONObject();
            areaObj.put("id", areaIDs.get(area));
            areaObj.put("name", area.getName());
            areaObj.put("col", colToHex(area.getColor()));
            areas.put(areaObj);
        }

        // helper to assign new layer ids
        Integer nextLayerID = 0;
        layerIDs = new HashMap<>();

        // layers (for quadtree optimization
        JSONArray layers = new JSONArray();
        root.put("layers", layers);
        for(Layer layer: world.getLayers()){
            if(!layer.getPlaces().isEmpty()){
                JSONObject layerObj = new JSONObject();

                // add layer to id map
                Integer layerID = nextLayerID++;
                layerIDs.put(layer.getId(), layerID);

                layerObj.put("id", layerID);
                layerObj.put("centerX", layer.getCenterX());
                layerObj.put("centerY", layer.getCenterY());
                if(layer.hasName()) layerObj.put("name", layer.getName());

                layers.put(layerObj);
            }
        }

        // places
        JSONArray places = new JSONArray();
        root.put("places", places);
        for(Place place: world.getPlaces()){
            JSONObject placeObj = new JSONObject();

            placeObj.put("id", place.getId());
            placeObj.put("n", place.getName());
            placeObj.put("l", translateLayerID(place.getLayer().getId()));
            placeObj.put("x", place.getX());
            placeObj.put("y", place.getY());

            if(place.getPlaceGroup() != null) placeObj.put("a", areaIDs.get(place.getPlaceGroup()));
            if(place.getRiskLevel() != null) placeObj.put("r", place.getRiskLevel().getId());
            if(place.getRecLevelMin() > -1) placeObj.put("lvlMin", place.getRecLevelMin());
            if(place.getRecLevelMax() > -1) placeObj.put("lvlMax", place.getRecLevelMax());

            // child places
            if(!place.getChildren().isEmpty()){
                JSONArray children = new JSONArray();
                placeObj.put("c", children);
                for(Place child: place.getChildren()){
                    children.put(child.getId());
                }
            }

            // parent places
            if(!place.getParents().isEmpty()){
                JSONArray parents = new JSONArray();
                placeObj.put("p", parents);
                for(Place parent: place.getParents()){
                    parents.put(parent.getId());
                }
            }

            // flags
            if(!place.getFlags().isEmpty()){
                JSONArray flags = new JSONArray();
                placeObj.put("f", flags);
                for(Map.Entry<String, Boolean> flag: place.getFlags().entrySet()){
                    if(flag.getValue()) flags.put(flag.getKey());
                }
            }

            // comments
            if(!place.getComments().isEmpty()){
                JSONArray comments = new JSONArray();
                placeObj.put("co", comments);
                int idx = 0;
                for(String comment: place.getComments()){
                    comments.put(idx++, comment);
                }
            }

            places.put(placeObj);
        }

        // paths
        JSONArray pathsArray = new JSONArray();
        root.put("paths", pathsArray);
        HashSet<Path> paths = new HashSet<>(); // paths that have already been added
        for(Place place: world.getPlaces()){
            for(Path path: place.getPaths()){
                if(!paths.contains(path)){
                    JSONArray pathObj = new JSONArray();

                    JSONObject p1 = new JSONObject();
                    p1.put("p", path.getPlaces()[0].getId());
                    p1.put("e", path.getExit(path.getPlaces()[0]));
                    pathObj.put(p1);

                    JSONObject p2 = new JSONObject();
                    p2.put("p", path.getPlaces()[1].getId());
                    p2.put("e", path.getExit(path.getPlaces()[1]));
                    pathObj.put(p2);

                    pathsArray.put(pathObj);
                    paths.add(path);
                }
            }
        }

        // home position
        WorldCoordinate home = world.getHome();
        JSONObject obj = new JSONObject();
        obj.put("l", translateLayerID(home.getLayer()));
        obj.put("x", home.getX());
        obj.put("y", home.getY());
        root.put("home", obj);

        // text laels
        Label[] labelsArray = world.getLabels();
        if(labelsArray != null && labelsArray.length > 0){
            JSONArray labels = new JSONArray();
            root.put("labels", labels);
            for(Label label: world.getLabels()){
                JSONObject labelObj = new JSONObject();
                labelObj.put("x", label.getX());
                labelObj.put("y", label.getY());
                labelObj.put("l", label.getLayer());
                labelObj.put("fontSize", label.getFontSize());
                labelObj.put("t", label.getText());
            }
        }

        // add metaWriter data from WorldTab
        if(metaWriter != null) root.put("meta", metaWriter.getMeta(layerIDs));

        try ( FileWriter writer = new FileWriter(filename)) {
            // indentation for better readability (for debugging), increases file size
            //root.write(writer, 4, 0);
            root.write(writer);
            fileRoot = root;
        } catch (Exception ex){
            System.out.println(ex.getLocalizedMessage());
        }
    }

    /**
     * Create a copy of the referenced file.
     * The new filename will be originalfilename + .bak
     * existing files will be overwritten
     * @throws FileNotFoundException
     */
    @Override
    public void backup() throws FileNotFoundException {
        try {
            File fileold = new File(filename);
            File filenew = new File(filename + ".bak");

            if(fileold.canRead()){
                if(filenew.exists()) filenew.delete();
                Files.copy(fileold.toPath(), filenew.toPath());
            }
        } catch (IOException ex) {
            Logger.getLogger(WorldFileJSON.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Could not create world backup file", "World backup", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Read world name from file
     * @return
     * @throws Exception
     */
    @Override
    public String readWorldName() throws Exception {
        try{
            JSONObject object = getJSONRoot();
            if(object != null && object.has("worldName")){
                return object.getString("worldName");
            } else return null;
        } catch(JSONException ex) {
            return "";
        }
    }

    /**
     * Check whether the file can be read by this reader class
     * @return
     */
    @Override
    public Boolean canRead() {
        // exact check
        try {
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            Boolean first = true;
            String line;
            while((line = bufferedReader.readLine()) != null){
                if(first){
                    line = line.trim();
                    if(!line.isEmpty()){
                        if(!line.startsWith("{")) return false;
                        first = false;
                    }
                }
                if(line.contains("\"worldName\"")) return true;
            }
        } catch (Exception ex) {
            return false;
        }

        // quick check
        /*try{
        JSONObject object = getJSONRoot();
        if(object == null) return false;
        return object.has("worldName");
        } catch(JSONException | IOException ex) {
        System.out.println(filename + " " + ex.getLocalizedMessage());
        }*/
        return false;
    }

    /**
     * Return this WorldFileType of reader
     * @return
     */
    @Override
    public WorldFileType getWorldFileType() {
        return WorldFileType.JSON;
    }

}

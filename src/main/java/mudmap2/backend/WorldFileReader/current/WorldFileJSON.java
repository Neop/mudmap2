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
import mudmap2.backend.Layer;
import mudmap2.backend.Path;
import mudmap2.backend.Place;
import mudmap2.backend.InformationColor;
import mudmap2.backend.Layer.LayerImage;
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
    static Integer versionMinor = 1;

    /**
     * Version history:
     * 2.0: initial version
     * 2.1: added pathColUnknown
     */
    
    JSONObject fileRoot = null;

    HashMap<Integer, Integer> layerIDs;

    JSONObject metaData;
    WorldMetaJSON metaWriter;

    final Color defaultColor = new Color(0x808080);

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
                world.setPathColorStd(hexToCol(root.getString("pathCol")));
            }

            // pathColNonCardinal
            if(root.has("pathColNonCardinal")){
                world.setPathColorNstd(hexToCol(root.getString("pathColNonCardinal")));
            }
            
            // pathColUnknown
            if(root.has("pathColUnknown")){
                world.setPathColorUnknown(hexToCol(root.getString("pathColUnknown")));
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

            // information colors (formerly named 'risk levels')
            if(root.has("riskLevels")){
                // remove existing information colors
                world.getInformationColors().clear();

                JSONArray informationColors = root.getJSONArray("riskLevels");
                Integer length = informationColors.length();
                for(Integer i = 0; i < length; ++i){
                    JSONObject informationColor = informationColors.getJSONObject(i);
                    if(informationColor.has("id")
                            && informationColor.has("desc")
                            && informationColor.has("col")){
                        Integer id = informationColor.getInt("id");
                        String desc = informationColor.getString("desc");
                        Color col = hexToCol(informationColor.getString("col"));
                        world.setInformationColor(new InformationColor(id, desc, col));
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
                        
                        // images
                        if(layer.has("images")){
                            JSONArray jsonImages = layer.getJSONArray("images");
                            for(Integer imgNum = 0; imgNum < jsonImages.length(); imgNum++){
                                try {
                                    JSONObject jsonImage = jsonImages.getJSONObject(imgNum);
                                    l.AddImage(jsonImage);
                                } catch(IOException | JSONException ex) {
                                    System.out.println(ex.getLocalizedMessage());
                                    JOptionPane.showMessageDialog(null, "Could not load layer image, removing image", "Loading world", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                        
                        world.addLayer(l);
                    }
                }
            }

            // places
            HashMap<Integer, Place> places = new HashMap<>();
            HashMap<Place, HashSet<Integer>> childrenMapping = new HashMap<>();

            if(root.has("places")){
                JSONArray jPlaces = root.getJSONArray("places");
                Integer length = jPlaces.length();
                for(Integer i = 0; i < length; ++i){
                    JSONObject jPlace = jPlaces.getJSONObject(i);
                    if(jPlace.has("id")
                            && jPlace.has("n")
                            && jPlace.has("l")
                            && jPlace.has("x")
                            && jPlace.has("y")){
                        Integer id = jPlace.getInt("id");
                        String name = jPlace.getString("n");
                        Integer layerId = jPlace.getInt("l");
                        Integer x = jPlace.getInt("x");
                        Integer y = jPlace.getInt("y");

                        // get layer
                        Layer layer = world.getLayer(layerId);
                        if(layer == null){
                            layer = new Layer(layerId, world);
                            world.addLayer(layer);
                        }

                        // create place
                        Place place = new Place(id, name, x, y, layer);

                        places.put(id, place);

                        // area
                        if(jPlace.has("a")){
                            place.setPlaceGroup(areas.get(jPlace.getInt("a")));
                        }
                        // information colors
                        if(jPlace.has("r")){
                            place.setInfoRing(world.getInformationColor(jPlace.getInt("r")));
                        }
                        // rec level
                        if(jPlace.has("lvlMin")){
                            place.setRecLevelMin(jPlace.getInt("lvlMin"));
                        }
                        if(jPlace.has("lvlMax")){
                            place.setRecLevelMin(jPlace.getInt("lvlMax"));
                        }

                        // children
                        if(jPlace.has("c")){
                            JSONArray children = jPlace.getJSONArray("c");
                            HashSet<Integer> set = new HashSet<>();
                            childrenMapping.put(place, set);

                            Integer lc = children.length();
                            for(Integer c = 0; c < lc; ++c){
                                set.add(children.getInt(c));
                            }
                        }

                        // flags
                        if(jPlace.has("f")){
                            JSONArray flags = jPlace.getJSONArray("f");
                            Integer lf = flags.length();
                            for(Integer f = 0; f < lf; ++f){
                                String flagname = flags.getString(f);
                                place.setFlag(flagname, true);
                            }
                        }

                        // comments
                        if(jPlace.has("co")){
                            JSONArray comments = jPlace.getJSONArray("co");
                            StringBuilder builder = new StringBuilder();

                            String separator = System.getProperty("separator");
                            if(separator == null || separator.isEmpty()){
                                separator = "\r\n";
                            }

                            for(int c = 0; c < comments.length(); ++c){
                                if(c > 0) {
                                    builder.append(separator);
                                }
                                builder.append(comments.getString(c));
                            }

                            place.setComments(builder.toString());
                        }

                        layer.put(place);
                    }
                }
            }

            // connect children
            for(Entry<Place, HashSet<Integer>> entry: childrenMapping.entrySet()){
                Place place = entry.getKey();
                for(Integer id: entry.getValue()){
                    place.connectChild(places.get(id));
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
                            Place pl0 = places.get(p0.getInt("p"));
                            Place pl1 = places.get(p1.getInt("p"));
                            if(pl0 != null && pl1 != null){
                                Path p = new Path(pl0, p0.getString("e"),
                                        pl1, p1.getString("e"));
                                pl0.connectPath(p);
                            }
                        }
                    }
                }
            }

            // world preferences (dialog settings etc.)
            if(root.has("preferences")){
                world.setPreferences(root.getJSONObject("preferences"));
            }

            // save meta data for WorldTab
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
        if(col != null){
            return "#" + Integer.toHexString(col.getRGB()).substring(2);
        } else {
            return "#" + Integer.toHexString(defaultColor.getRGB()).substring(2);
        }
    }

    /**
     * Convert rgb hex string to color
     * @param hex
     * @return
     */
    private Color hexToCol(String hex){
        Color ret = new Color(defaultColor.getRGB());

        if(hex != null && !hex.isEmpty()){
            Integer col = Integer.decode(hex);
            if(col >= 0 && col <= 0xffffff){
                ret = new Color(col);
            }
        }
        return ret;
    }

    /**
     * Write world to file
     * @param world
     * @throws java.io.IOException
     */
    @Override
    public void writeFile(World world) throws IOException {
        writeFile(world, null);
    }

    /**
     * Write layer to file
     * @param layer
     * @throws IOException
     */
    public void writeFile(Layer layer) throws IOException {
        writeFile(layer.getWorld(), layer);
    }

    /**
     * Write entire world or single layer to file
     * @param world world to write
     * @param exportLayer layer to export or null to export all
     * @throws IOException
     */
    private void writeFile(World world, Layer exportLayer) throws IOException {
        boolean exportSingleLayer = (exportLayer != null);

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

        if(!exportSingleLayer) {
            root.put("showPlaceID", world.getShowPlaceId());

            // tile center color
            if(world.getTileCenterColor() != null){
                root.put("tileCenterCol", colToHex(world.getTileCenterColor()));
            }
            // cardinal and non cardinal path color
            if(world.getPathColorStd() != null){
                root.put("pathCol", colToHex(world.getPathColorStd()));
            }
            if(world.getPathColorNstd() != null){
                root.put("pathColNonCardinal", colToHex(world.getPathColorNstd()));
            }
            if(world.getPathColorUnknown() != null){
                root.put("pathColUnknown", colToHex(world.getPathColorUnknown()));
            }
            // other path colors
            JSONArray pathColorsArray = new JSONArray();
            root.put("pathColDefs", pathColorsArray);
            for(Map.Entry<String, Color> pathCol: world.getPathColors().entrySet()){
                if(pathCol.getValue() != null){
                    JSONObject pathColObj = new JSONObject();
                    pathColObj.put("path", pathCol.getKey());
                    pathColObj.put("col", colToHex(pathCol.getValue()));
                    pathColorsArray.put(pathColObj);
                }
            }
        }

        // information colors
        // filter colors if single layer is exported
        HashSet<InformationColor> infoColsInUse = new HashSet<>();
        if(exportSingleLayer) {
            for(Place place: exportLayer.getPlaces()){
                infoColsInUse.add(place.getInfoRing());
            }
        }

        // write
        JSONArray informationColors = new JSONArray();
        root.put("riskLevels", informationColors);
        for(InformationColor infoCol: world.getInformationColors()){
            // if single layer export: check if infoCol is used on layer
            if(exportSingleLayer && !infoColsInUse.contains(infoCol)) continue;

            JSONObject rlo = new JSONObject();
            rlo.put("id", infoCol.getId());
            rlo.put("desc", infoCol.getDescription());
            rlo.put("col", colToHex(infoCol.getColor()));
            informationColors.put(rlo);
        }

        // place groups (aka areas)
        // filter place groups if single layer is exported
        HashSet<PlaceGroup> areasInUse = new HashSet<>();
        if(exportSingleLayer) {
            for(Place place: exportLayer.getPlaces()){
                areasInUse.add(place.getPlaceGroup());
            }
        }

        // create IDs for areaArray
        HashMap<PlaceGroup, Integer> areaIDs = new HashMap<>();
        Integer cnt = 0; // incremental id
        for(PlaceGroup a: world.getPlaceGroups()){
            Boolean inUse = false;
            // removePlace unused
            for(Layer layer: world.getLayers()){
                for(Place place: layer.getPlaces()){
                    if(place.getPlaceGroup() == a){
                        inUse = true;
                        break;
                    }
                }
            }
            if(inUse) areaIDs.put(a, ++cnt);
        }

        // add areaArray
        JSONArray areas = new JSONArray();
        root.put("areas", areas);
        for(PlaceGroup area: world.getPlaceGroups()){
            // if single layer export: check if area is used on layer
            if(exportSingleLayer && !areasInUse.contains(area)) continue;

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
            // skip other layers
            if(exportSingleLayer && layer != exportLayer) continue;

            if(!layer.getPlaces().isEmpty()){
                JSONObject layerObj = new JSONObject();

                // add layer to id map
                Integer layerID = nextLayerID++;
                layerIDs.put(layer.getId(), layerID);

                layerObj.put("id", layerID);
                layerObj.put("centerX", layer.getCenterX());
                layerObj.put("centerY", layer.getCenterY());
                if(layer.hasName()) layerObj.put("name", layer.getName());

                // images
                if(!layer.GetImages().isEmpty()) {
                    JSONArray jsonImages = new JSONArray();
                    layerObj.put("images", jsonImages);
                    
                    for(LayerImage image: layer.GetImages()){
                        jsonImages.put(image.ToJSON());
                    }
                }
                
                layers.put(layerObj);
            }
        }

        // places
        JSONArray places = new JSONArray();
        root.put("places", places);
        for(Layer layer: world.getLayers()){
            // skip other layers
            if(exportSingleLayer && layer != exportLayer) continue;

            for(Place place: layer.getPlaces()){
                JSONObject placeObj = new JSONObject();

                placeObj.put("id", place.getId());
                placeObj.put("n", place.getName());
                placeObj.put("l", translateLayerID(layer.getId()));
                placeObj.put("x", place.getX());
                placeObj.put("y", place.getY());

                if(place.getPlaceGroup() != null) placeObj.put("a", areaIDs.get(place.getPlaceGroup()));
                if(place.getInfoRing() != null) placeObj.put("r", place.getInfoRing().getId());
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
                if(place.getComments() != null && !place.getComments().isEmpty()){
                    JSONArray comments = new JSONArray();
                    comments.put(place.getComments());
                    placeObj.put("co", comments);
                }

                places.put(placeObj);
            }
        }

        // paths
        JSONArray pathsArray = new JSONArray();
        root.put("paths", pathsArray);
        HashSet<Path> paths = new HashSet<>(); // paths that have already been added
        for(Layer layer: world.getLayers()){
            // skip other layers
            if(exportSingleLayer && layer != exportLayer) continue;

            for(Place place: layer.getPlaces()){
                for(Path path: place.getPaths()){
                    if(!paths.contains(path)){
                        JSONArray pathObj = new JSONArray();

                        JSONObject p1 = new JSONObject();
                        p1.put("p", path.getPlaces()[0].getId());
                        p1.put("e", path.getExitDirections()[0]);
                        pathObj.put(p1);

                        JSONObject p2 = new JSONObject();
                        p2.put("p", path.getPlaces()[1].getId());
                        p2.put("e", path.getExitDirections()[1]);
                        pathObj.put(p2);

                        pathsArray.put(pathObj);
                        paths.add(path);
                    }
                }
            }
        }

        if(!exportSingleLayer){
            // home position
            WorldCoordinate home = world.getHome();
            JSONObject obj = new JSONObject();
            obj.put("l", translateLayerID(home.getLayer()));
            obj.put("x", home.getX());
            obj.put("y", home.getY());
            root.put("home", obj);

            // world preferences (dialog settings etc.)
            root.put("preferences", world.getPreferences());

            // add metaWriter data from WorldTab
            if(metaWriter != null) root.put("meta", metaWriter.getMeta(layerIDs));
        }

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
        } catch (IOException ex) {
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

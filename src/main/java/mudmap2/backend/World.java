/*  MUD Map (v2) - A tool to create and organize maps for text-based games
 *  Copyright (C) 2014  Neop (email: mneop@web.de)
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

/*  File description
 *
 *  This class contains all data of a world. Places, Layers, PlaceGroups,... can be
 *  accessed via World. It also reads and writes world files
 */

package mudmap2.backend;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import mudmap2.backend.Layer.PlaceNotInsertedException;
import mudmap2.backend.WorldFileReader.WorldFile;
import mudmap2.backend.sssp.BreadthSearchGraph;
import org.json.JSONObject;

/**
 *
 * @author neop
 */
public class World implements BreadthSearchGraph {

    // worldname and file of the world
    String worldname;
    WorldFile worldFile;

    // color of path lines and self-defined path colors
    Color pathColorCardinal = new Color(0, 255, 0);
    Color pathColorNonCardinal = new Color(0, 255, 0);
    Color tileCenterColor = new Color(207, 190, 134);
    final HashMap<String, Color> pathColors = new HashMap<>();

    // Coordinates of the home position
    WorldCoordinate home = new WorldCoordinate(0, 0, 0);

    // ID and object
    final TreeMap<Integer, InformationColor> informationColors = new TreeMap<>();
    final HashSet<PlaceGroup> placeGroups = new HashSet<>();
    final TreeMap<Integer, Layer> layers = new TreeMap<>();

    // For creating world-unique layer ids
    Integer nextLayerID = 1;

    // Preferences
    ShowPlaceID showPlaceID = ShowPlaceID.UNIQUE;

    // World-related preferences for dialogs etc.
    JSONObject preferences = new JSONObject();
    public final static String PREFERENCES_KEY_DIALOG = "dialog";

    // Listeners
    final LinkedList<WorldChangeListener> changeListeners = new LinkedList<>();

    /**
     * Creates an empty world
     */
    public World(){
        initialize();
    }

    /**
     * Creates an empty world
     * @param name worldname of the world
     */
    public World(String name){
        worldname = name;
        initialize();
    }

    /**
     * Initializes the world
     */
    private void initialize(){
        informationColors.put(0, new InformationColor(0, "not evaluated", new Color(188, 188, 188)));
        informationColors.put(1, new InformationColor(1, "safe", new Color(0, 255, 0)));
        informationColors.put(2, new InformationColor(2, "mobs don't attack", new Color(255, 255, 0)));
        informationColors.put(3, new InformationColor(3, "mobs might attack", new Color(255, 128, 0)));
        informationColors.put(4, new InformationColor(4, "mobs will attack", new Color(255, 0, 0)));
    }

    // --------- WorldFile -----------------------------------------------------
    /**
     * Get world file reader
     * @return WorldFileReader or null
     */
    public WorldFile getWorldFile() {
        return worldFile;
    }

    /**
     * Set world file reader
     * @param worldFile
     */
    public void setWorldFile(WorldFile worldFile) {
        this.worldFile = worldFile;
    }

    // --------- World name ----------------------------------------------------
    /**
     * Gets the world worldname
     * @return world worldname
     */
    public String getName(){
        if(worldname == null){
            return "unnamed";
        } else {
            return worldname;
        }
    }

    /**
     * Sets the world worldname
     * @param n new world worldname
     */
    public void setName(String n){
        worldname = n;
        callListeners(this);
    }

    // --------- home position -------------------------------------------------
    /**
     * Gets the home position
     * @return home coordinate
     */
    public WorldCoordinate getHome(){
        return home;
    }

    /**
     * Sets a new home position
     * @param home
     */
    public void setHome(WorldCoordinate home){
        this.home = home;
    }

    // --------- places --------------------------------------------------------

    /**
     * Creates a placeholder place
     * @param layerId layer
     * @param x x coordinate
     * @param y y coordinate
     */
    public void putPlaceholder(int layerId, int x, int y){
        try {
            Place place = new Place(Place.PLACEHOLDER_NAME, x, y, null);

            // find or create placeholder group
            PlaceGroup placeGroup = null;
            for(PlaceGroup a: placeGroups) if(a.getName().equals("placeholder")){
                placeGroup = a;
                break;
            }
            // create new placeholder group
            if(placeGroup == null) addPlaceGroup(placeGroup = new PlaceGroup("placeholder", Color.GREEN));

            place.setPlaceGroup(placeGroup);
            place.setInfoRing(getInformationColor(0));

            Layer layer = getLayer(layerId);
            if(layer == null){
                addLayer(new Layer(layerId, this));
                layer = getLayer(layerId);
            }
            layer.put(place, x, y);
        } catch(PlaceNotInsertedException ex){ // ignore
        } catch (Exception ex) {
            Logger.getLogger(World.class.getName()).log(Level.WARNING, "Couldn't put placeholder to map: " + ex, ex);
        }
    }

    // --------- layers --------------------------------------------------------
    /**
     * Gets a layer
     * @param id layer id
     * @return layer or null
     */
    public Layer getLayer(int id){
        return layers.get(id);
    }

    /**
     * Adds or replaces a layer
     * @param layer
     */
    public void addLayer(Layer layer){ // TODO: throw Exception if layer exists
        if(layer == null){
            throw new NullPointerException();
        }

        if(!layers.containsKey(layer.getId()))
            layers.put(layer.getId(), layer);

        addChangeListener(layer);
        callListeners(layer);
    }

    /**
     * Creates a new and empty layer
     * @param name layer name
     * @return new layer
     */
    public Layer getNewLayer(String name){
        Layer layer = getNewLayer();
        if(name != null && !name.isEmpty()){
            layer.setName(name);
        }
        callListeners(layer);
        return layer;
    }

    /**
     * Create a new anonymous, empty layer
     * @return new layer
     */
    public Layer getNewLayer(){
        Layer layer = new Layer(this);
        addLayer(layer);
        return layer;
    }

    /**
     * Gets the next layer id and increases the internal counter
     * @return
     */
    public Integer getNextLayerID(){
        return nextLayerID++;
    }

    /**
     * Sets the next layer id
     * @param id
     */
    public void setNextLayerID(Integer id){
        nextLayerID = id;
    }

    /**
     * Gets all layers
     * @return all layers
     */
    public Collection<Layer> getLayers(){
        return layers.values();
    }

    // --------- colors --------------------------------------------------------
    /**
     * Gets the standard path color
     * @return path color
     */
    public Color getPathColorStd(){
        return pathColorCardinal;
    }

    /**
     * Gets the color for paths that aren't predefined
     * @return path color
     */
    public Color getPathColorNstd(){
        return pathColorNonCardinal;
    }

    /**
     * Gets the color of an exit direction
     * @param dir exit direction
     * @return path color
     */
    public Color getPathColor(String dir){
        Color ret;

        if(dir == null){
            ret = getPathColorStd();
        } else if(!pathColors.containsKey(dir)){
            if(dir.equals("n") || dir.equals("s") || dir.equals("e") || dir.equals("q") ||
               dir.equals("ne") || dir.equals("nw") || dir.equals("se") || dir.equals("sw") ||
               dir.equals("w") || dir.equals("e"))
                ret = pathColorCardinal;
            else ret = pathColorNonCardinal;
        } else ret = pathColors.get(dir);

        return ret;
    }

    /**
     * Gets exit direction colors (without default colors)
     * @return
     */
    public HashMap<String, Color> getPathColors(){
        return pathColors;
    }

    /**
     * Sets the color of an exit direction
     * @param dir
     * @param color
     */
    public void setPathColor(String dir, Color color){
        if(dir == null){
            throw new NullPointerException();
        }
        if(color == null){
            throw new NullPointerException();
        }

        pathColors.put(dir, color);
        callListeners(this);
    }

    /**
     * Sets the path color
     * @param color new color
     */
    public void setPathColorStd(Color color){
        if(color == null){
            throw new NullPointerException();
        }

        pathColorCardinal = color;
        callListeners(this);
    }

    /**
     * Sets the color for paths that aren't predefined
     * @param color
     */
    public void setPathColorNstd(Color color){
        if(color == null){
            throw new NullPointerException();
        }

        pathColorNonCardinal = color;
        callListeners(this);
    }

    /**
     * Gets the tile center color
     * @return
     */
    public Color getTileCenterColor(){
        return tileCenterColor;
    }

    /**
     * Sets the tile center color
     * @param color
     */
    public void setTileCenterColor(Color color){
        if(color == null){
            throw new NullPointerException();
        }

        tileCenterColor = color;
        callListeners(this);
    }

    // --------- config --------------------------------------------------------
    public enum ShowPlaceID {
        NONE, // don't show place ID on map
        UNIQUE, // show place ID if worldname isn't unique
        ALL // always show place ID
    }

    /**
     * Sets whether the place ID is shown on the map
     * @param show
     */
    public void setShowPlaceID(ShowPlaceID show){
        showPlaceID = show;
        callListeners(this);
    }

    /**
     * Gets the in which case the place ID is shown on the map
     * @return
     */
    public ShowPlaceID getShowPlaceId(){
        return showPlaceID;
    }

    // --------- PlaceGroupss ---------------------------------------------------------
    /**
     * Gets all PlaceGroupss (eg. for lists)
     * @return all PlaceGroupss
     */
    public ArrayList<PlaceGroup> getPlaceGroups(){
        ArrayList<PlaceGroup> ret = new ArrayList<>(placeGroups);
        Collections.sort(ret);
        return ret;
    }

    /**
     * Adds a PlaceGroup
     * @param placeGroup new PlaceGroup
     */
    public void addPlaceGroup(PlaceGroup placeGroup) {
        if(placeGroup == null){
            throw new NullPointerException();
        }

        if(!placeGroups.contains(placeGroup)){
            placeGroups.add(placeGroup);
        }
        callListeners(placeGroup);
    }

    /**
     * Removes a PlaceGroup
     * @param placeGroup PlaceGroup to be removed
     */
    public void removePlaceGroup(PlaceGroup placeGroup){
        for(Layer layer: getLayers()){
            for(Place p: layer.getPlaces()){
                if(p.getPlaceGroup() == placeGroup) p.setPlaceGroup(null);
            }
        }
        placeGroups.remove(placeGroup);
        callListeners(placeGroup);
    }

    // --------- information colors --------------------------------------------
    /**
     * Gets all information colors (eg. for lists)
     * @return all information colors
     */
    public Collection<InformationColor> getInformationColors(){
        return informationColors.values();
    }

    /**
     * Gets an information color
     * @param id information color id
     * @return information color
     */
    public InformationColor getInformationColor(int id){
        return informationColors.get(id);
    }

    /**
     * Adds an information colors, assignes a unique id
     * @param ic new information color
     */
    public void addInformationColor(InformationColor ic){
        if(ic == null){
            throw new NullPointerException();
        }

        if(!informationColors.containsValue(ic)){
            // ID-collision?
            while(informationColors.containsKey(ic.getId())){
                ++ic.id;
            }
            informationColors.put(ic.getId(), ic);
        }

        callListeners(ic);
    }

    /**
     * Adds or replaces an information color by it's id
     * @param ic information color to add or replace
     */
    public void setInformationColor(InformationColor ic){
        informationColors.put(ic.getId(), ic);
    }

    /**
     * Removes an information color
     * @param ic
     * @throws java.lang.Exception
     */
    public void removeInformationColor(InformationColor ic) throws Exception {
        if(ic != null){
            if(!informationColors.containsValue(ic)) throw new Exception("Tried to remove information color that does not belong to this world");
            // remode from information color list
            informationColors.remove(ic.getId());
            // removePlace from places
            for(Layer layer: getLayers()){
                for(Place place: layer.getPlaces()){
                    if(place.getInfoRing() == ic) place.setInfoRing(null);
                }
            }

            callListeners(ic);
        }
    }

    // --------- preference ----------------------------------------------------
    /**
     * Get preferences object
     * @return JSON object
     */
    public JSONObject getPreferences() {
        return preferences;
    }

    /**
     * Replace preferences object
     * @param preferences JSON object
     */
    public void setPreferences(JSONObject preferences) {
        this.preferences = preferences;
    }

    // --------- path finding --------------------------------------------------
    /**
     * does a breadth search
     * @param start start place
     * @param end end place
     * @return end place or null. Following the predecessors of this path leads
     * to the start place
     */
    @Override
    public Place breadthSearch(Place start, Place end) {
        for(Layer layer: getLayers()){
            for(Place place: layer.getPlaces()){
                place.breadthSearchReset();
            }
        }
        start.getBreadthSearchData().marked = true;

        LinkedList<Place> queue = new LinkedList<>();
        queue.add(start);

        while(!queue.isEmpty()){
            Place v = queue.pollFirst();
            if(v == end) return v;

            for(Path pa: v.getPaths()){
                Place vi = pa.getOtherPlace(v);
                if(!vi.getBreadthSearchData().marked && vi != v){
                    vi.getBreadthSearchData().marked = true;
                    vi.getBreadthSearchData().predecessor = v;
                    queue.addLast(vi);
                }
            }
        }
        return null;
    }

    // --------- listeners -----------------------------------------------------
    /**
     * Add change listener
     * @param listener listener to add
     */
    public void addChangeListener(WorldChangeListener listener){
        if(!changeListeners.contains(listener)) changeListeners.add(listener);
    }

    /**
     * Remove change listener
     * @param listener listener to remove
     */
    public void removeChangeListener(WorldChangeListener listener){
        changeListeners.remove(listener);
    }

    /**
     * Call listeners
     * @param source changed object
     */
    public void callListeners(Object source){
        for(WorldChangeListener listener: changeListeners){
            listener.worldChanged(source);
        }
    }
}

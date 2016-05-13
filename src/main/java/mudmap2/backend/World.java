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
 *  This file contains all data of a world. Places, Layers, Areas,... can be
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
import mudmap2.backend.Layer.PlaceNotFoundException;
import mudmap2.backend.Layer.PlaceNotInsertedException;
import mudmap2.backend.WorldFileReader.WorldFile;
import mudmap2.backend.sssp.BreadthSearchGraph;

/**
 *
 * @author neop
 */
public class World implements BreadthSearchGraph {

    // worldname and file of the world
    String worldname;
    WorldFile worldFile;
    // color of path lines and self-defined path colors
    Color pathColorCardinal, pathColorNonCardinal;
    HashMap<String, Color> pathColors;
    Color tileCenterColor;
    // Coordinates of the home position
    WorldCoordinate home;

    // ID and object
    TreeMap<Integer, RiskLevel> riskLevels;
    HashSet<Area> areas;
    TreeMap<Integer, Place> places;
    TreeMap<String, Integer> placeNames;
    TreeMap<Integer, Layer> layers;

    ShowPlaceID showPlaceID;

    /**
     * Creates an empty world
     */
    public World(){
        worldFile = null;
        initialize();
    }

    /**
     * Creates an empty world
     * @param name worldname of the world
     */
    public World(String name){
        worldname = name;
        worldFile = null;
        initialize();
    }

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

    /**
     * Initializes the world
     */
    private void initialize(){
        home = new WorldCoordinate(0, 0, 0);
        // path line colors
        pathColorNonCardinal = pathColorCardinal = new Color(0, 255, 0);
        pathColors = new HashMap<>();
        tileCenterColor = new Color(207, 190, 134);

        // risk levels
        riskLevels = new TreeMap<>();
        riskLevels.put(0, new RiskLevel(0, "not evaluated", new Color(188, 188, 188)));
        riskLevels.put(1, new RiskLevel(1, "secure", new Color(0, 255, 0)));
        riskLevels.put(2, new RiskLevel(2, "mobs don't attack", new Color(255, 255, 0)));
        riskLevels.put(3, new RiskLevel(3, "mobs might attack", new Color(255, 128, 0)));
        riskLevels.put(4, new RiskLevel(4, "mobs will attack", new Color(255, 0, 0)));

        areas = new HashSet<>();
        layers = new TreeMap<>();
        places = new TreeMap<>();
        placeNames = new TreeMap<>();

        showPlaceID = ShowPlaceID.UNIQUE;
    }

    /**
     * does a breadth search
     * @param start start place
     * @param end end place
     * @return
     */
    @Override
    public Place breadthSearch(Place start, Place end) {
        for(Place pl: getPlaces()) pl.breadthSearchReset();
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

    /**
     * Gets a place
     * @param layer layer id
     * @param x x coordinate
     * @param y y coordinate
     * @return place or null if it doesn't exist
     */
    public Place get(int layer, int x, int y){
        Layer l = getLayer(layer);
        if(l == null) return null;
        else return l.get(x, y);
    }

    /**
     * Places a place in the world, the layer and coordinates described by the
     * place will be used
     * @param place new place
     * @throws java.lang.Exception if place couldn't be added to layer
     */
    public void put(Place place) throws Exception{
        // create layer, if it doesn't exist
        Layer layer = place.getLayer();
        if(layer == null){
            layer = new Layer(this);
            layers.put(home.getLayer(), layer);
            place.setLayer(layer);
        }
        put(place, place.getLayer().getId(), place.getX(), place.getY());
    }

    /**
     * Places a place in the world
     * @param place new place
     * @param layer layer for the place to be put on, will be created if it doesnt exist
     * @param x x coordinate
     * @param y y coordinate
     * @throws java.lang.Exception if place couldn't be added to layer
     */
    public void put(Place place, int layer, int x, int y) throws Exception{
        // get layer, create a new one, if necessary
        Layer l = getLayer(layer);
        if(l == null) layers.put(layer, l = new Layer(layer, this));

        // remove from old layer and world
        if(place.getLayer() != null){
            try{
                // if place belongs to a different world
                if(place.getLayer().getWorld() != this) place.getLayer().getWorld().remove(place);
                else {
                    try{
                        if(place.getLayer() != l) place.getLayer().remove(place);
                    } catch(RuntimeException | PlaceNotFoundException ex){}
                }
            } catch(RuntimeException | PlaceNotFoundException ex){
                Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // add to layer
        place.setLayer(l);
        l.put(place, x, y);

        // add to place list of the world
        if(!places.containsKey(place.getId())) places.put(place.getId(), place);
        if(!placeNames.containsKey(place.getName())) placeNames.put(place.getName(), 1);
        else placeNames.put(place.getName(), placeNames.get(place.getName()) + 1);
    }

    /**
     * Creates a placeholder place
     * @param layer layer
     * @param x x coordinate
     * @param y y coordinate
     */
    public void putPlaceholder(int layer, int x, int y){
        try {
            Place place = new Place(Place.placeholderName, x, y, null);

            // find or create placeholder area
            Area area = null;
            for(Area a: areas) if(a.getName().equals("placeholder")){
                area = a;
                break;
            }
            // create new placeholder area
            if(area == null) addArea(area = new Area("placeholder", Color.GREEN));

            place.setArea(area);
            place.setRiskLevel(getRiskLevel(0));
            put(place, layer, x, y);
        } catch(PlaceNotInsertedException ex){ // ignore
        } catch (Exception ex) {
            Logger.getLogger(World.class.getName()).log(Level.WARNING, "Couldn't put placeholder to map: " + ex, ex);
        }
    }

    /**
     * Removes a place from the world and removes it's connections to other places
     * @param place place to be removed
     * @throws RuntimeException
     * @throws mudmap2.backend.Layer.PlaceNotFoundException
     */
    public void remove(Place place) throws RuntimeException, PlaceNotFoundException {
        Layer layer = layers.get(place.getLayer().getId());
        if(layer == null || layer != place.getLayer()){
            // error, wrong layer? (shouldn't occur)
            throw new RuntimeException("Couldn't remove \"" + place + ": layer mismatch");
        } else {
            layer.remove(place);
            place.removeConnections();
            places.remove(place.getId());
            if(placeNames.containsKey(place.getName()))
                placeNames.put(place.getName(), Math.max(0, placeNames.get(place.getName()) - 1));
        }
    }

    /**
     * Gets the amount of places with the same worldname
     * @param name
     * @return
     */
    public int getPlaceNameCount(String name){
        if(placeNames.containsKey(name)) return placeNames.get(name);
        else return 0;
    }

    /**
     * Returns the text data of a configuration file entry
     *
     * Some config file lines consist of space-separated data followed by a text
     * This method returns the text part.
     *
     * @param non_text number of data entries to be removed
     * @param line raw config line
     * @return text part
     */
    private String configGetText(int non_text, String line){
        while(non_text-- != 0){
            int index = line.indexOf(" ");
            // if " " not found -> entry not found in line
            if(index < 0) return "";
            line = line.substring(index).trim();
        }
        return line;
    }

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
     * @param l
     */
    public void setLayer(Layer l){ // TODO: rename addLayer(), throw Exception if layer exists
        if(!layers.containsKey(l.getId()))
            layers.put(l.getId(), l);
    }

    /**
     * Creates a new and empty layer and returns it
     * @return new layer
     */
    public Layer getNewLayer(){
        Layer ret = new Layer(this);
        layers.put(ret.getId(), ret);
        return ret;
    }

    /**
     * Gets a place
     * @param id place id
     * @return place
     */
    public Place getPlace(int id){
        return places.get(id);
    }

    /**
     * Gets all places
     * @return
     */
    public Collection<Place> getPlaces(){
        return places.values();
    }

    /**
     * Gets a risk level
     * @param id risk level id
     * @return risk level
     */
    public RiskLevel getRiskLevel(int id){
        return riskLevels.get(id);
    }

    /**
     * Gets the world worldname
     * @return world worldname
     */
    public String getName(){
        return worldname;
    }

    /**
     * Sets the world worldname
     * @param n new world worldname
     */
    public void setName(String n){
        worldname = n;
    }

    /**
     * Gets the home position
     * @return home coordinate
     */
    public WorldCoordinate getHome(){
        return home;
    }

    /**
     * Sets a new home position
     * @param _home
     */
    public void setHome(WorldCoordinate _home){
        home = _home;
    }

    /**
     * Gets the path color
     * @return path color
     */
    public Color getPathColor(){
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
     * @param dir
     * @return
     */
    public Color getPathColor(String dir){
        if(!pathColors.containsKey(dir)){
            if(dir.equals("n") || dir.equals("s") || dir.equals("e") || dir.equals("q") ||
               dir.equals("ne") || dir.equals("nw") || dir.equals("se") || dir.equals("sw") ||
               dir.equals("w") || dir.equals("e"))
                return pathColorCardinal;
            else return pathColorNonCardinal;
        } else {
            return pathColors.get(dir);
        }
    }

    /**
     * Sets the color of an exit direction
     * @param dir
     * @param color
     */
    public void setPathColor(String dir, Color color){
        pathColors.put(dir, color);
    }

    /**
     * Gets exit direction colors (without default colors)
     * @return
     */
    public HashMap<String, Color> getPathColors(){
        return pathColors;
    }

    /**
     * Sets the path color
     * @param color new color
     */
    public void setPathColor(Color color){
        pathColorCardinal = color;
    }

    /**
     * Sets the color for paths that aren't predefined
     * @param color
     */
    public void setPathColorNstd(Color color){
        pathColorNonCardinal = color;
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
        tileCenterColor = color;
    }

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
    }

    /**
     * Gets the in which case the place ID is shown on the map
     * @return
     */
    public ShowPlaceID getShowPlaceId(){
        return showPlaceID;
    }

    /**
     * Gets all areas (eg. for lists)
     * @return all areas
     */
    public ArrayList<Area> getAreas(){
        ArrayList<Area> ret = new ArrayList<>(areas);
        Collections.sort(ret);
        return ret;
    }

    /**
     * Adds an area
     * @param area new area
     */
    public void addArea(Area area) {
        areas.add(area);
    }

    /**
     * Removes an area
     * @param area area to be removed
     */
    public void removeArea(Area area){
        // remove area from places
        for(Place p: places.values()){
            if(p.getArea() == area) p.setArea(null);
        }
        areas.remove(area);
    }

    /**
     * Gets all layers
     * @return all layers
     */
    public Collection<Layer> getLayers(){
        return layers.values();
    }

    /**
     * Gets all risk levels (eg. for lists)
     * @return all risk levels
     */
    public Collection<RiskLevel> getRiskLevels(){
        return riskLevels.values();
    }

    /**
     * Adds a risk level
     * @param rl new risk level
     */
    public void addRiskLevel(RiskLevel rl){
        if(!riskLevels.containsValue(rl)){
            // ID-collision?
            while(riskLevels.containsKey(rl.getId())) ++rl.id;
            riskLevels.put(rl.getId(), rl);
        }
    }

    /**
     * Removes a risk level
     * @param rl
     * @throws java.lang.Exception
     */
    public void removeRiskLevel(RiskLevel rl) throws Exception {
        if(!riskLevels.containsValue(rl)) throw new Exception("Tried to remove risk level that does not belong to this world");
        // remode from risk level list
        riskLevels.remove(rl.getId());
        // remove from places
        for(Place place: places.values())
            if(place.getRiskLevel() == rl) place.setRiskLevel(null);
    }

    /**
     * Returns true, if the worldname of the place is unique in its world
     * @param name
     * @return true if the place worldname is unique
     */
    public Boolean isPlaceNameUnique(String name){
        return placeNames.containsKey(name);
    }

    /**
     * Add new label
     * @param label
     */
    public void addLabel(Label label){

    }

    /**
     * Remove label
     * @param label
     */
    public void removeLabel(Label label){

    }

    /**
     * Get all labels
     * @return
     */
    public Label[] getLabels(){
        return null;
    }

    /**
     * get all labels of layer
     * @param layer
     * @return
     */
    public Label[] getLabels(Integer layer){
        return null;
    }

    /**
     * Don't create multiple instances of the same world
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

}

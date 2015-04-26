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
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import mudmap2.backend.Layer.PlaceNotFoundException;
import mudmap2.backend.Layer.PlaceNotInsertedException;
import mudmap2.backend.WorldFileReader.current.WorldFileMM1;
import mudmap2.backend.sssp.BreadthSearchGraph;

/**
 *
 * @author neop
 */
public class World implements BreadthSearchGraph {
    
    public static final int file_version_major = 1;
    public static final int file_version_minor = 6;
    
    public static boolean compatibility_mudmap_1 = true;
    
    // name and file of the world
    String name, file;
    // color of path lines and self-defined path colors
    Color path_color_cardinal, path_color_non_cardinal;
    HashMap<String, Color> path_colors;
    Color tile_center_color;
    // Coordinates of the home position
    WorldCoordinate home;
    
    // ID and object
    TreeMap<Integer, RiskLevel> risk_levels;
    TreeMap<Integer, Area> areas;
    TreeMap<Integer, Place> places;
    TreeMap<String, Integer> place_names;
    TreeMap<Integer, Layer> layers;
    
    ShowPlaceID_t show_place_id;
    
    /**
     * Loads a world from a file
     * @param _file world file
     * @throws java.lang.Exception
     */
    public World(String _file) throws Exception{
        file = _file;
        initialize();
        load_world();
    }
    
    /**
     * Creates an empty world
     * @param _file new world file
     * @param _name name of the world
     */
    public World(String _file, String _name){
        name = _name;
        file = _file;
        initialize();
    }
    
    /**
     * Initializes the world
     */
    private void initialize(){
        home = new WorldCoordinate(0, 0, 0);
        // path line colors
        path_color_non_cardinal = path_color_cardinal = new Color(0, 255, 0);
        path_colors = new HashMap<String, Color>();
        tile_center_color = new Color(207, 190, 134);
        
        // risk levels
        risk_levels = new TreeMap<Integer, RiskLevel>();
        risk_levels.put(0, new RiskLevel(0, "not evaluated", new Color(188, 188, 188)));
        risk_levels.put(1, new RiskLevel(1, "secure", new Color(0, 255, 0)));
        risk_levels.put(2, new RiskLevel(2, "mobs don't attack", new Color(255, 255, 0)));
        risk_levels.put(3, new RiskLevel(3, "mobs might attack", new Color(255, 128, 0)));
        risk_levels.put(4, new RiskLevel(4, "mobs will attack", new Color(255, 0, 0)));
        
        areas = new TreeMap<Integer, Area>();
        layers = new TreeMap<Integer, Layer>();
        places = new TreeMap<Integer, Place>();
        place_names = new TreeMap<String, Integer>();
        
        show_place_id = ShowPlaceID_t.UNIQUE;
    }
    
    /**
     * Loads the world
     */
    private void load_world() throws Exception{
        WorldFileMM1 worldfile = new WorldFileMM1(this);
        worldfile.readFile(file);
    }
    
    /**
     * Saves the world
     */
    public void write_world(){
        WorldFileMM1 worldfile = new WorldFileMM1(this);
        worldfile.writeFile(file);
    }

    /**
     * does a breadth search
     * @param start start place
     * @param end end place
     * @return 
     */
    @Override
    public Place breadth_search(Place start, Place end) {
        for(Place pl: get_places()) pl.breadth_search_reset();
        start.get_breadth_search_data().marked = true;
        
        LinkedList<Place> queue = new LinkedList<Place>();
        queue.add(start);
        
        while(!queue.isEmpty()){
            Place v = queue.pollFirst();
            if(v == end) return v;
            
            for(Path pa: v.get_paths()){
                Place vi = pa.get_other_place(v);
                if(!vi.get_breadth_search_data().marked && vi != v){
                    vi.get_breadth_search_data().marked = true;
                    vi.get_breadth_search_data().predecessor = v;
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
        Layer l = get_layer(layer);
        if(l == null) return null;
        else return (Place) l.get(x, y);
    }
    
    /**
     * Places a place in the world, the layer and coordinates described by the
     * place will be used
     * @param place new place
     * @throws java.lang.Exception if place couldn't be added to layer
     */
    public void put(Place place) throws Exception{
        // create layer, if it doesn't exist
        Layer layer = place.get_layer();
        if(layer == null){
            layer = new Layer(this);
            layers.put(home.get_layer(), layer);
            place.set_layer(layer);
        }
        put(place, place.get_layer().get_id(), place.get_x(), place.get_y());
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
        Layer l = get_layer(layer);
        if(l == null) layers.put(layer, l = new Layer(layer, this));
        
        // remove from old layer and world
        if(place.get_layer() != null){ 
            try{
                // if place belongs to a different world
                if(place.get_layer().get_world() != this) place.get_layer().get_world().remove(place);
                else place.get_layer().remove(place);
            } catch(Exception ex){
                Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        // add to layer
        place.set_layer(l);
        l.put(place, x, y);
        
        // add to place list of the world
        places.put(place.get_id(), place);
        if(!place_names.containsKey(place.get_name())) place_names.put(place.get_name(), 1);
        else place_names.put(place.get_name(), place_names.get(place.get_name()) + 1);
    }
    
    /**
     * Creates a placeholder place
     * @param layer layer
     * @param x x coordinate
     * @param y y coordinate
     */
    public void put_placeholder(int layer, int x, int y){
        try {
            Place place = new Place(Place.placeholder_name, x, y, null);
            
            // find or create placeholder area
            Area area = null;
            for(Area a: areas.values()) if(a.get_name().equals("placeholder")){
                area = a;
                break;
            }
            // create new placeholder area
            if(area == null) add_area(area = new Area("placeholder", Color.GREEN));
            
            place.set_area(area);
            place.set_risk_level(get_risk_level(0));
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
        Layer layer = layers.get(place.get_layer().get_id());
        if(layer == null || layer != place.get_layer()){
            // error, wrong layer? (shouldn't occur)
            throw new RuntimeException("Couldn't remove \"" + place + ": layer mismatch");
        } else {
            place.remove_connections();
            layer.remove(place);
            places.remove(place.get_id());
            if(place_names.containsKey(place.get_name()))
                place_names.put(place.get_name(), Math.max(0, place_names.get(place.get_name()) - 1));
        }
    }
    
    /**
     * Gets the amount of places with the same name
     * @param name
     * @return 
     */
    public int get_place_name_count(String name){
        if(place_names.containsKey(name)) return place_names.get(name);
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
    private String config_get_text(int non_text, String line){
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
    public Layer get_layer(int id){
        return layers.get(id);
    }
    
    /**
     * Adds or replaces a layer
     * @param l 
     */
    public void set_layer(Layer l){
        layers.put(l.get_id(), l);
    }
    
    /**
     * Creates a new and empty layer and returns it
     * @return new layer
     */
    public Layer get_new_layer(){
        Layer ret = new Layer(this);
        layers.put(ret.get_id(), ret);
        return ret;
    }
    
    /**
     * Gets a place
     * @param id place id
     * @return place 
     */
    public Place get_place(int id){
        return places.get(id);
    }
    
    /**
     * Gets all places
     * @return 
     */
    public Collection<Place> get_places(){
        return places.values();
    }
    
    /**
     * Gets a risk level
     * @param id risk level id
     * @return risk level
     */
    public RiskLevel get_risk_level(int id){
        return risk_levels.get(id);
    }
    
    /**
     * Gets the world name
     * @return world name
     */
    public String get_name(){
        return name;
    }
    
    /**
     * Sets the world name
     * @param n new world name
     */
    public void set_name(String n){
        name = n;
    }
    
    /**
     * Gets the world file
     * @return world file string
     */
    public String get_file(){
        return file;
    }
    
    /**
     * Gets the home position
     * @return home coordinate
     */
    public WorldCoordinate get_home(){
        return home;
    }
    
    /**
     * Sets a new home position
     * @param _home
     */
    public void set_home(WorldCoordinate _home){
        home = _home;
    }
    
    /**
     * Gets the path color
     * @return path color
     */
    public Color get_path_color(){
        return path_color_cardinal;
    }
    
    /**
     * Gets the color for paths that aren't predefined
     * @return path color
     */
    public Color get_path_color_nstd(){
        return path_color_non_cardinal;
    }
    
    /**
     * Gets the color of an exit direction
     * @param dir
     * @return 
     */
    public Color get_path_color(String dir){
        if(!path_colors.containsKey(dir)){
            if(dir.equals("n") || dir.equals("s") || dir.equals("e") || dir.equals("q") ||
               dir.equals("ne") || dir.equals("nw") || dir.equals("se") || dir.equals("sw") ||
               dir.equals("w") || dir.equals("e"))
                return path_color_cardinal;
            else return path_color_non_cardinal;
        } else {
            return path_colors.get(dir);
        }
    }
    
    /**
     * Sets the color of an exit direction
     * @param dir
     * @param color 
     */
    public void set_path_color(String dir, Color color){
        path_colors.put(dir, color);
    }
    
    /**
     * Gets all exit direction colors
     * @return 
     */
    public HashMap<String, Color> get_path_colors(){
        return path_colors;
    }
    
    /**
     * Sets the path color
     * @param color new color
     */
    public void set_path_color(Color color){
        path_color_cardinal = color;
    }
    
    /**
     * Sets the color for paths that aren't predefined
     * @param color 
     */
    public void set_path_color_nstd(Color color){
        path_color_non_cardinal = color;
    }
    
    /**
     * Gets the tile center color
     * @return 
     */
    public Color get_tile_center_color(){
        return tile_center_color;
    }
    
    /**
     * Sets the tile center color
     * @param color 
     */
    public void set_tile_center_color(Color color){
        tile_center_color = color;
    }
    
    public enum ShowPlaceID_t {
        NONE, // don't show place ID on map
        UNIQUE, // show place ID if name isn't unique
        ALL // always show place ID
    }
    
    /**
     * Sets whether the place ID is shown on the map
     * @param show 
     */
    public void set_show_place_id(ShowPlaceID_t show){
        show_place_id = show;
    }
    
    /**
     * Gets the in which case the place ID is shown on the map
     * @return 
     */
    public ShowPlaceID_t get_show_place_id(){
        return show_place_id;
    }
    
    /**
     * Gets all areas (eg. for lists)
     * @return all areas
     */
    public ArrayList<Area> get_areas(){
        ArrayList<Area> ret = new ArrayList<Area>(areas.values());
        Collections.sort(ret);
        return ret;
    }
    
    /**
     * Gets an area by it's id
     * @param id area id
     * @return area
     */
    public Area get_area(int id){
        return areas.get(id);
    }
    
    /**
     * Adds an area
     * @param area new area
     */
    public void add_area(Area area) {
        if(!areas.containsValue(area)) areas.put(area.get_id(), area);
    }
    
    /**
     * Removes an area
     * @param area area to be removed
     */
    public void remove_area(Area area){
        // remove area from places
        for(Place p: places.values()){
            if(p.get_area() == area) p.set_area(null);
        }
        areas.remove(area.get_id());
    }
    
    /**
     * Gets all layers
     * @return all layers
     */
    public Collection<Layer> get_layers(){
        return layers.values();
    }
    
    /**
     * Gets all risk levels (eg. for lists)
     * @return all risk levels
     */
    public Collection<RiskLevel> get_risk_levels(){
        return risk_levels.values();
    }
    
    /**
     * Adds a risk level
     * @param rl new risk level
     */
    public void add_risk_level(RiskLevel rl){
        if(!risk_levels.containsKey(rl.get_id())) risk_levels.put(rl.get_id(), rl);
    }
    
    /**
     * Removes a risk level
     * @param rl 
     */
    public void remove_risk_level(RiskLevel rl){
        risk_levels.remove(rl.get_id());
        // remove from places
        for(Place place: places.values())
            if(place.get_risk_level().get_id() == rl.get_id()) place.set_risk_level(null);
    }
    
    /**
     * Don't create multiple instances of the same world
     * @return
     * @throws CloneNotSupportedException 
     */
    @Override
    protected Object clone() throws CloneNotSupportedException{
        throw new CloneNotSupportedException();
    }

}

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
 *  This class describes a place in a world
 */

package mudmap2.backend;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;
import mudmap2.backend.Layer.PlaceNotFoundException;
import mudmap2.backend.sssp.BreadthSearch;

/**
 * A place in the world
 * @author neop
 */
public class Place extends LayerElement implements Comparable<Place>, BreadthSearch {
    
    public static final String placeholder_name = "?";
    
    // next id to be assigned
    static int next_id;
    
    int id;
    String name;
    Area area;
    int rec_level_min, rec_level_max;
    RiskLevel risk_level;
    
    HashSet<Place> children, parents;
    HashSet<Path> paths;
    TreeMap<String, Boolean> flags;
    LinkedList<String> comments;
    
    BreadthSearchData breadth_search_data;
    
    public Place(int id, String name, int pos_x, int pos_y, Layer l){
        super(pos_x, pos_y, l);
        this.name = name;
        this.id = id;
        if(id >= next_id) next_id = id + 1;
        
        initialize();
    }
    
    /**
     * Constructs new a place a certain position
     * @param name name
     * @param pos_x x coordinate
     * @param pos_y y coordinate
     * @param l
     */
    public Place(String name, int pos_x, int pos_y, Layer l){
        super(pos_x, pos_y, l);
        this.name = name;
        id = next_id++;
        
        initialize();
    }
    
    /**
     * Initializes the place
     */
    private void initialize(){
        area = null;
        risk_level = null;
        rec_level_min = rec_level_max = -1;
        
        children = new HashSet<Place>();
        parents = new HashSet<Place>();
        paths = new HashSet<Path>();
        flags = new TreeMap<String, Boolean>();
        comments = new LinkedList<String>();
        
        breadth_search_data = null;
    }
    
    /**
     * Gets the place id
     * @return place id
     */
    public int getId(){
        return id;
    }
    
    /**
     * Gets the name
     * @return name of the place
     */
    public String getName(){
        return name;
    }
    
    /**
     * Sets the name
     * @param name new name
     */
    public void setName(String name){
        this.name = name;
    }
    
    /**
     * Returns true, if the name of the place is unique in its world
     * @return true if the place name is unique
     */
    public boolean isNameUnique(){
        if(getLayer() != null && getLayer().getWorld() != null){
            return getLayer().getWorld().getPlaceNameCount(getName()) <= 1;
        }
        return true;
    }
    
    /**
     * Gets the position of a place as world coordinate
     * @return place coordinate
     */
    public WorldCoordinate getCoordinate(){
        return new WorldCoordinate(getLayer().getId(), getX(), getY());
    }
    
    /**
     * Gets the area
     * @return area
     */
    public Area getArea(){
        return area;
    }
    
    /**
     * Sets the area
     * @param area 
     */
    public void setArea(Area area) {
        this.area = area;
    }
    
    /**
     * Gets the minimal recommended level
     * @return minimal recommended level
     */
    public int getRecLevelMin(){
        return rec_level_min;
    }
    
    /**
     * Sets the minimal recommended level
     * @param rec_level_min 
     */
    public void setRecLevelMin(int rec_level_min){
        this.rec_level_min = rec_level_min;
    }
    
    /**
     * Gets the maximal recommended level
     * @return maximal recommended level
     */
    public int getRecLevelMax(){
        return rec_level_max;
    }
    
    /**
     * Sets the maximal recommended level
     * @param rec_level_max 
     */
    public void setRecLevelMax(int rec_level_max){
        this.rec_level_max = rec_level_max;
    }
    
    /**
     * Gets the risk level
     * @return risk level
     */
    public RiskLevel getRiskLevel(){
        return risk_level;
    }
    
    /**
     * sets the risk level
     * @param risk_level
     */
    public void setRiskLevel(RiskLevel risk_level){
        this.risk_level = risk_level;
    }
    
    /**
     * Adds a comment at the end of the list
     * @param comment
     */
    public void addComment(String comment){
        comments.add(comment);
    }
    
    /**
     * removes all comments
     */
    public void deleteComments(){
        comments.clear();
    }
    
    /**
     * Gets the comments list
     * @return comments list
     */
    public LinkedList<String> getComments(){
        return comments;
    }
    
    /**
     * Gets the comments as a single string
     * @param newlines insert \n at lineendings, if true
     * @return 
     */
    public String getCommentsString(boolean newlines){
        String ret = "";
        String lineending = newlines ? "\n" : " ";
        for(String c: comments) ret += (ret.length() == 0 ? "" : lineending) + c;
        return ret;
    }
    
    /**
     * Gets the path connected to an exit
     * @param dir exit direction
     * @return path connected to that exit or null
     */
    public Path getExit(String dir){
        for(Path path: paths){
            if(path.getExit(this).equals(dir)) return path;
        }
        return null;
    }
    
    /**
     * Gets the path to a place, if available
     * @param place a place that this place is connected to
     * @return paths to place or empty set
     */
    public HashSet<Path> getPaths(Place place){
        HashSet<Path> ret = new HashSet<Path>();
        for(Path path: paths)
            if(path.hasPlace(place)) ret.add(path);            
        return ret;
    }
    
    /**
     * Removes a path
     * @param dir1 exit direction on this place
     * @param other connected place
     * @param dir2 exit direction of the other place
     * @throws java.lang.Exception if path could not be removed
     */
    public void removePath(String dir1, Place other, String dir2) throws Exception{
        boolean ok = false;
        for(Path path: Place.this.getPaths(other)){
            if(path.getExit(this).equals(dir1) && path.getExit(other).equals(dir2)){
                paths.remove(path);
                other.paths.remove(path);
                ok = true;
            }
        }
        if(!ok) throw new RuntimeException("Couldn't remove path connection (" + this + " [" + dir1 + "] - " + other + " [" + dir2 + "]), path not found");
    }
    
    /**
     * Removes a path
     * @param path 
     */
    public void removePath(Path path){
        paths.remove(path);
        path.getOtherPlace(this).paths.remove(path);
    }
    
    /**
     * Connects a place to another one tht is specified in path
     * If 'this place' is not in path an exception will be thrown
     * @param path
     * @return true, if successfully connected
     */
    public boolean connectPath(Path path) throws RuntimeException{
        Place[] pp = path.getPlaces();
        Place other;
        
        if(pp[0] == this) other = pp[1];
        else if(pp[1] == this) other = pp[0];
        else throw new RuntimeException("This place is not specified in given path");
        
        boolean exit_occupied = false;
        String exit_this = path.getExit(this);
        
        // check if exit is already connected with path
        for(Path p: paths){
            if(p.getExit(this).equals(exit_this)){
                exit_occupied = true;
                break;
            }
        }
        if(!exit_occupied){
            exit_this = path.getExit(other);
            for(Path p: other.paths){
                if(p.getExit(other).equals(exit_this)){
                    exit_occupied = true;
                    break;
                }
            }
        
            if(!exit_occupied){
                paths.add(path);
                other.paths.add(path);
            }
        }
        return !exit_occupied;
    }
    
    /**
     * Gets all paths
     * @return all paths
     */
    public HashSet<Path> getPaths(){
        return paths;
    }
    
    /**
     * Gets tha path that is connected to dir or null
     * @param dir
     * @return 
     */
    public Path getPathTo(String dir){
        for(Path pa: paths){
            if(pa.getExit(this).equals(dir)) return pa;
        }
        return null;
    }
    
    /**
     * Gets a flag value
     * @param key flag name
     * @return flag value
     */
    public boolean getFlag(String key){
        if(flags.containsKey(key)) return flags.get(key);
        return false;
    }
    
    /**
     * Sets a string flag
     * @param key flag name
     * @param state value
     */
    public void setFlag(String key, boolean state){
        flags.put(key, state);
    }
    
    /**
     * Gets the flags of a place
     * @return 
     */
    public TreeMap<String, Boolean> getFlags(){
        return flags;
    }
    
    /**
     * Connects a place as child, this place will be added to it as parent
     * @param p 
     */
    public void connectChild(Place p){
        children.add(p);
        p.parents.add(this);
    }
    
    /**
     * Removes a parent - child connection (subarea)
     * @param child child to be removed
     */
    public void removeChild(Place child){
        children.remove(child);
        child.parents.remove(this);
    }
    
    /**
     * Gets the child places / subareas
     * @return child places
     */
    public HashSet<Place> getChildren(){
        return children;
    }
    
    /**
     * Gets the parent places (subarea parents)
     * @return parent places
     */
    public HashSet<Place> getParents(){
        return parents;
    }
    
    /**
     * Gets the name
     * @return name of he place
     */
    @Override
    public String toString(){
        return name + " (ID: " + getId() + ")";
    }
    
    /**
     * Compares two places by their name
     * @param arg0
     * @return 
     */
    @Override
    public int compareTo(Place arg0) {
        if(arg0 == null) throw new NullPointerException();
        return getName().compareTo(arg0.getName());
    }

    /**
     * Removes all connections to other places (paths, child-connections)
     */
    void removeConnections() {
        // remove paths (buffer, becaus connected_places will be modified
        HashSet<Path> cp_buffer = (HashSet<Path>) paths.clone();
        for(Path p: cp_buffer) p.remove();
        // remove connection to sub-areas (children / parents)
        for(Place pl: children) pl.parents.remove(this);
        for(Place pl: parents) pl.children.remove(this);
    }

    /**
     * Removes the place from its layer
     * @throws RuntimeException
     * @throws mudmap2.backend.Layer.PlaceNotFoundException 
     */
    public void remove() throws RuntimeException, PlaceNotFoundException {
        getLayer().getWorld().remove(this);
    }

    /**
     * Returns true, if a keyword is found in any of the places data
     * it searches in name, comments and flags, keywords hve to be lower-case
     * @param keywords in lower case
     * @return true, if a keyword is found
     */
    public boolean matchKeywords(String[] keywords) {
        for(String kw: keywords){
            // search in name
            if(name.toLowerCase().contains(kw)) return true;
            // search in comments
            for(String comment: comments)
                if(comment.toLowerCase().contains(kw)) return true;
            // search in flags
            for(Entry<String, Boolean> flag: flags.entrySet())
                if(flag.getValue() && flag.getKey().toLowerCase().contains(kw)) return true;
        }
        return false;
    }
    
    /**
     * Creates a new place from this place
     * a new id will be created and connections to other places not copied
     * @return
     */
    public Place duplicate(){
        Place place = new Place(name, getX(), getY(), null);
        
        place.area = area;
        place.rec_level_max = rec_level_max;
        place.rec_level_min = rec_level_min;
        place.risk_level = risk_level;
        place.flags = (TreeMap<String, Boolean>) flags.clone();
        place.comments = (LinkedList<String>) comments.clone();
        
        return place;
    }

    /**
     * Resets the breadth search data
     */
    @Override
    public void breadthSearchReset() {
        getBreadthSearchData().reset();
    }

    /**
     * Gets the breadth search data
     * @return 
     */
    @Override
    public BreadthSearchData getBreadthSearchData() {
        if(breadth_search_data == null) breadth_search_data = new BreadthSearchData();
        return breadth_search_data;
    }
    
}

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
import java.util.TreeMap;
import mudmap2.backend.Layer.PlaceNotFoundException;
import mudmap2.backend.sssp.BreadthSearch;

/**
 * A place in the world
 * @author neop
 */
public class Place extends LayerElement implements Comparable<Place>, BreadthSearch {

    public static final String PLACEHOLDER_NAME = "?";

    // next id to be assigned
    static int nextID;

    int id;
    String name;
    Area area;
    int recLevelMin, recLevelMax;
    RiskLevel riskLevel;

    HashSet<Place> children, parents;
    HashSet<Path> paths;
    TreeMap<String, Boolean> flags;
    LinkedList<String> comments;

    BreadthSearchData breadthSearchData;

    public Place(int id, String name, int posX, int posY, Layer l){
        super(posX, posY, l);
        this.name = name;
        this.id = id;
        if(id >= nextID) nextID = id + 1;

        initialize();
    }

    /**
     * Constructs new a place a certain position
     * @param name name
     * @param posX x coordinate
     * @param posY y coordinate
     * @param l
     */
    public Place(String name, int posX, int posY, Layer l){
        super(posX, posY, l);
        this.name = name;
        id = nextID++;

        initialize();
    }

    /**
     * Initializes the place
     */
    private void initialize(){
        area = null;
        riskLevel = null;
        recLevelMin = recLevelMax = -1;

        children = new HashSet<>();
        parents = new HashSet<>();
        paths = new HashSet<>();
        flags = new TreeMap<>();
        comments = new LinkedList<>();

        breadthSearchData = null;
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
        callWorldChangeListeners();
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
        if(getLayer() != null && getLayer().getWorld() != null){
            getLayer().getWorld().addArea(area);
        }
        callWorldChangeListeners();
    }

    /**
     * Gets the minimal recommended level
     * @return minimal recommended level
     */
    public int getRecLevelMin(){
        return recLevelMin;
    }

    /**
     * Sets the minimal recommended level
     * @param recLevelMin
     */
    public void setRecLevelMin(int recLevelMin){
        this.recLevelMin = recLevelMin;
        callWorldChangeListeners();
    }

    /**
     * Gets the maximal recommended level
     * @return maximal recommended level
     */
    public int getRecLevelMax(){
        return recLevelMax;
    }

    /**
     * Sets the maximal recommended level
     * @param recLevelMax
     */
    public void setRecLevelMax(int recLevelMax){
        this.recLevelMax = recLevelMax;
        callWorldChangeListeners();
    }

    /**
     * Gets the risk level
     * @return risk level
     */
    public RiskLevel getRiskLevel(){
        return riskLevel;
    }

    /**
     * sets the risk level
     * @param riskLevel
     */
    public void setRiskLevel(RiskLevel riskLevel){
        this.riskLevel = riskLevel;
        callWorldChangeListeners();
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
        callWorldChangeListeners();
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
        HashSet<Path> ret = new HashSet<>();
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
        callWorldChangeListeners();
    }

    /**
     * Removes a path
     * @param path
     */
    public void removePath(Path path){
        paths.remove(path);
        path.getOtherPlace(this).paths.remove(path);
        callWorldChangeListeners();
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
        callWorldChangeListeners();
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
        callWorldChangeListeners();
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
        callWorldChangeListeners();
    }

    /**
     * Removes a parent - child connection (subarea)
     * @param child child to be removed
     */
    public void removeChild(Place child){
        children.remove(child);
        child.parents.remove(this);
        callWorldChangeListeners();
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
        // removePlace paths (buffer, becaus connected_places will be modified
        HashSet<Path> cp_buffer = (HashSet<Path>) paths.clone();
        for(Path p: cp_buffer) p.remove();
        // removePlace connection to sub-areas (children / parents)
        for(Place pl: children) pl.parents.remove(this);
        children.clear();
        for(Place pl: parents) pl.children.remove(this);
        parents.clear();

        callWorldChangeListeners();
    }

    /**
     * Removes the place from its layer
     * @throws RuntimeException
     * @throws mudmap2.backend.Layer.PlaceNotFoundException
     */
    public void remove() throws RuntimeException, PlaceNotFoundException {
        getLayer().getWorld().removePlace(this);
    }

    /**
     * Returns true, if the keyword is found in any of the places data
     * it searches in name, comments and flags
     * @param keyword
     * @return true, if the keyword is found
     */
    public boolean matchKeyword(String keyword){
        keyword = keyword.toLowerCase();
        // search in name
        if(name.toLowerCase().contains(keyword)) return true;
        // search in comments
        for(String comment: comments)
            if(comment.toLowerCase().contains(keyword)) return true;
        return false;
    }

    /**
     * Returns true, if a keyword is found in any of the places data
     * it searches in name, comments and flags
     * @param keywords
     * @return true, if a keyword is found
     */
    public boolean matchKeywords(String[] keywords) {
        if(keywords == null) return false;

        for(String kw: keywords){
            if(!matchKeyword(kw)) return false;
        }
        return true;
    }

    /**
     * Creates a new place from this place
     * a new id will be created and connections to other places not copied
     * @return
     */
    public Place duplicate(){
        Place place = new Place(name, getX(), getY(), null);

        place.area = area;
        place.recLevelMax = recLevelMax;
        place.recLevelMin = recLevelMin;
        place.riskLevel = riskLevel;
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
        if(breadthSearchData == null) breadthSearchData = new BreadthSearchData();
        return breadthSearchData;
    }

    /**
     * Call world change listeners on place changes
     */
    private void callWorldChangeListeners(){
        if(getLayer() != null && getLayer().getWorld() != null)
            getLayer().getWorld().callListeners(this);
    }

}

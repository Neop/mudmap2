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
 *  This class descibes a layer of a world map. It controls in which data
 *  structure the places are stored
 */

package mudmap2.backend;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import mudmap2.backend.prquadtree.Quadtree;
import mudmap2.utils.Pair;

/**
 * A layer stores places relatively to each other by position on the map.
 * Each world can consist of multiple layers
 *
 * @author neop
 */
public class Layer implements WorldChangeListener {

    World world;
    Integer id;
    String name;
    Quadtree<Place> elements;

    // for quadtree optimization
    int maxX, minX, maxY, minY;

    // place name cache for unique check
    HashMap<String, Integer> placeNameCache;

    /**
     * Constructor, sets layer id
     * @param id layer id
     * @param world world
     */
    public Layer(int id, World world){
        if(world == null) throw new NullPointerException();

        this.id = id;
        this.world = world;

        if(id >= world.getNextLayerID()) world.setNextLayerID(id + 1);

        maxX = minX = maxY = minY = 0;
        elements = new Quadtree<>();
        placeNameCache = new HashMap<>();
    }

    /**
     * Constructor, generates unique layer id
     * @param world world
     */
    public Layer(World world){
        if(world == null) throw new NullPointerException();

        id = world.getNextLayerID();
        this.world = world;

        world.setNextLayerID(id+1);

        maxX = minX = maxY = minY = 0;
        elements = new Quadtree<>();
        placeNameCache = new HashMap<>();
    }

    /**
     * Get layer name or generated name if no name is set
     * @return layer name or name generated from layer id
     */
    public String getName() {
        if(name == null) return "Map " + getId();
        return name;
    }

    /**
     * Set layer name
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Check whether the layer has an explicitly set name
     * @return
     */
    public Boolean hasName(){
        return name != null;
    }

    /**
     * Use this only to set an optimized quadtree after construction
     * @param center_x
     * @param center_y
     */
    public void setQuadtree(int center_x, int center_y){
        elements = new Quadtree<>(center_x, center_y);
    }

    /**
     * Gets the center x coordinate (estimation)
     * @return
     */
    public int getCenterX(){
        return (maxX + minX) / 2;
    }

    /**
     * Gets the center y coordinate (estimation)
     * @return
     */
    public int getCenterY(){
        return (maxY + minY) / 2;
    }

    /**
     * Gets the exact center
     * @return
     */
    public Pair<Double, Double> getExactCenter(){
        Pair<Double, Double> center = new Pair<>(0.0, 0.0);

        int layerXMin, layerXMax, layerYMin, layerYMax;

        HashSet<Place> places = getPlaces();
        if(!places.isEmpty()){
            layerXMax = layerXMin = places.iterator().next().getX();
            layerYMax = layerYMin = places.iterator().next().getY();

            for(Place place: places){
                layerXMax = Math.max(layerXMax, place.getX());
                layerXMin = Math.min(layerXMin, place.getX());
                layerYMax = Math.max(layerYMax, place.getY());
                layerYMin = Math.min(layerYMin, place.getY());
            }

            int centerX = layerXMax - layerXMin + 1;
            int centerY = layerYMax - layerYMin + 1;

            center.first = 0.5 * (double) centerX + (double) layerXMin;
            center.second = 0.5 * (double) centerY + (double) layerYMin - 1;
        }

        return center;
    }

    /**
     * Gets the max x coordinate
     * @return
     */
    public int getXMin(){
        HashSet<Place> places = getPlaces();
        if(places.isEmpty()) return 0;
        int ret = places.iterator().next().getX();
        for(Place place: places)
            ret = Math.min(ret, place.getX());
        return ret;
    }

    /**
     * Gets the min x coordinate
     * @return
     */
    public int getXMax(){
        HashSet<Place> places = getPlaces();
        if(places.isEmpty()) return 0;
        int ret = places.iterator().next().getX();
        for(Place place: places)
            ret = Math.max(ret, place.getX());
        return ret;
    }

    /**
     * Gets the max y coordinate
     * @return
     */
    public int getYMin(){
        HashSet<Place> places = getPlaces();
        if(places.isEmpty()) return 0;
        int ret = places.iterator().next().getY();
        for(Place place: places)
            ret = Math.min(ret, place.getY());
        return ret;
    }

    /**
     * Gets the min y coordinate
     * @return
     */
    public int getYMax(){
        HashSet<Place> places = getPlaces();
        if(places.isEmpty()) return 0;
        int ret = places.iterator().next().getY();
        for(Place place: places)
            ret = Math.max(ret, place.getY());
        return ret;
    }

    /**
     * Puts the element at a position but doesn't add it to the world
     * @param x x coordinate
     * @param y y coordinate
     * @param element new element
     * @throws java.lang.Exception
     */
    public void put(Place element, int x, int y) throws Exception{
        minX = Math.min(minX, x);
        maxX = Math.max(maxX, x);
        minY = Math.min(minY, y);
        maxY = Math.max(maxY, y);

        // removePlace element from layer, if it's already on the layer
        if(elements.contains(element)) elements.remove(element.getX(), element.getY());
        //element.getLayer().removePlace(element);
        element.setPosition(x, y, this);
        put(element);
    }

    /**
     * Adds an element to the layer (but not to the world!), uses the position
     * of the element
     * @param element element to be added
     * @throws mudmap2.backend.Layer.PlaceNotInsertedException
     */
    public void put(Place element) throws PlaceNotInsertedException {
        try {
            minX = Math.min(minX, element.getX());
            maxX = Math.max(maxX, element.getX());
            minY = Math.min(minY, element.getY());
            maxY = Math.max(maxY, element.getY());

            elements.insert(element, element.getX(), element.getY());
            world.callListeners(element);
        } catch (Exception ex) {
            throw new PlaceNotInsertedException(element.getX(), element.getY());
        }
    }

    /**
     * Gets the element at a position
     * @param x x coordinate
     * @param y y coordinate
     * @return element at that position or null
     */
    public Place get(int x, int y){
        return elements.get(x, y);
    }

    /**
     * Gets the surrounding places, without the center place
     * @param _x center coordinate
     * @param _y center coordinate
     * @param distance maximum distance in each drection
     * @return
     */
    public LinkedList<Place> getNeighbors(int _x, int _y, int distance){
        LinkedList<Place> ret = new LinkedList<>();
        distance = Math.abs(distance);
        for(int x = -distance; x <= distance; ++x){
            for(int y = -distance; y <= distance; ++y){
                if(!(x == 0 && y == 0)){ // if not center place
                    Place el = get(_x + x, _y + y);
                    if(el != null) ret.add(el);
                }
            }
        }
        return ret;
    }

    /**
     * Gets the id of the layer
     * @return layer id
     */
    public Integer getId(){
        return id;
    }

    /**
     * Gets the world
     * @return world
     */
    public World getWorld(){
        return world;
    }

    /**
     * Removes an element from the layer but not from the world
     * @param element
     * @throws mudmap2.backend.Layer.PlaceNotFoundException
     */
    public void remove(LayerElement element) throws RuntimeException, PlaceNotFoundException {
        // check if position is available
        LayerElement el_bef = get(element.getX(), element.getY());
        if(el_bef != element){
            if(el_bef != null) throw new RuntimeException("Element location mismatch (" + element.getX() + ", " + element.getY() + ")");
            else throw new PlaceNotFoundException(element.getX(), element.getY());
        }
        elements.remove(element.getX(), element.getY());
        world.callListeners(this);
    }

    /**
     * Returns true, if an element at position x,y exists
     * @param x x position
     * @param y y position
     * @return true, if an element exists
     */
    public boolean exist(int x, int y){
        return elements.exist(x, y);
    }

    /**
     * Check whether the layer is empty
     * @return true if empty
     */
    public boolean isEmpty(){
        return elements.isEmpty();
    }

    /**
     * Gets a collection of all elements
     * @return set of all elements or empty set
     */
    public HashSet<Place> getPlaces(){
        return elements.values();
    }

    /**
     * Gets the id
     * @return layer id
     */
    @Override
    public String toString(){
        return getName();
    }

    /**
     * Check if place name is unique
     * @param name place name to check
     * @return true if name is unique on this layer
     */
    public boolean isPlaceNameUnique(String name){
        Integer num = placeNameCache.get(name);
        return num == null || num <= 1;
    }

    /**
     * Recreates place name chache
     */
    private void updatePlaceNameCache(){
        placeNameCache.clear();

        for(Place place: getPlaces()){
            Integer value = placeNameCache.get(place.getName());
            if(value == null){
                value = 1;
            } else {
                value += 1;
            }
            placeNameCache.put(place.getName(), value);
        }
    }

    @Override
    public void worldChanged(Object source) {
        // if source is a place on this layer
        if(source instanceof Place && elements.contains((Place) source)){
            updatePlaceNameCache();
        } else if(source instanceof Layer && source == this){
            updatePlaceNameCache();
        }
    }

    /**
     * This exception will be thrown, if a place doesn't exist at a certain position
     */
    public static class PlaceNotFoundException extends Exception {

        private static final long serialVersionUID = 1L;
        int x, y;

        /**
         * Constructs an exception
         * @param _x x coordinate of the place
         * @param _y y coordinate of the place
         */
        public PlaceNotFoundException(int _x, int _y) {
            x = _x; y = _y;
        }

        @Override
        public String toString(){
            return "Element at position " + x + ", " + y + " doesn't exist";
        }
    }

    public static class PlaceNotInsertedException extends Exception {

        private static final long serialVersionUID = 1L;
        int x, y;

        /**
         * Constructs an exception
         * @param _x x coordinate of the place
         * @param _y y coordinate of the place
         */
        public PlaceNotInsertedException(int _x, int _y) {
            x = _x; y = _y;
        }

        @Override
        public String toString(){
            return "Couldn't insert place at position " + x + ", " + y;
        }
    }

}

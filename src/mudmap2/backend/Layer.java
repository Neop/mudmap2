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

import java.util.HashSet;
import java.util.LinkedList;
import mudmap2.backend.prquadtree.Quadtree;

/**
 * A layer stores places relatively to each other by position on the map.
 * Each world can consist of multiple layers
 * 
 * @author neop
 */
public class Layer {
    
    World world;
    static int next_id;
    int id;
    Quadtree elements;
    
    // for quadtree optimization
    int max_x, min_x, max_y, min_y;
    
    public Layer(int _id, World _world){
        id = _id;
        if(id >= next_id) next_id = id + 1;
        world = _world;
        max_x = min_x = max_y = min_y = 0;
        elements = new Quadtree();
    }
    
    public Layer(World _world){
        id = next_id++;
        world = _world;
        max_x = min_x = max_y = min_y = 0;
        elements = new Quadtree();
    }
    
    /**
     * Use this only to set an optimized quadtree after construction
     * @param center_x
     * @param center_y 
     */
    public void set_quadtree(int center_x, int center_y){
        elements = new Quadtree<Place>(center_x, center_y);
    }
    
    /**
     * Gets the center x coordinate (estimation)
     * @return 
     */
    public int get_center_x(){
        return max_x - min_x;
    }
      
    /**
     * Gets the center y coordinate (estimation)
     * @return 
     */
    public int get_center_y(){
        return max_y - min_y;
    }
    
    /**
     * Puts the element at a position but doesn't add it to the world
     * @param x x coordinate
     * @param y y coordinate
     * @param element new element
     * @throws java.lang.Exception
     */
    public void put(LayerElement element, int x, int y) throws Exception{
        min_x = Math.min(min_x, x);
        max_x = Math.max(max_x, x);
        min_y = Math.min(min_y, y);
        max_y = Math.max(max_y, y);
        
        //element.get_layer().remove(element);
        element.set_position(x, y, this);
        put(element);
    }
    
    /**
     * Adds an element to the layer (but not to the world), uses the position of the element
     * @param element element to be added
     * @throws mudmap2.backend.Layer.PlaceNotInsertedException
     */
    public void put(LayerElement element) throws PlaceNotInsertedException {
        try {
            min_x = Math.min(min_x, element.get_x());
            max_x = Math.max(max_x, element.get_x());
            min_y = Math.min(min_y, element.get_y());
            max_y = Math.max(max_y, element.get_y());
            
            elements.insert(element, element.get_x(), element.get_y());
        } catch (Exception ex) {
            throw new PlaceNotInsertedException(element.get_x(), element.get_y());
        }
    }
    
    /**
     * Gets the element at a position
     * @param x x coordinate
     * @param y y coordinate
     * @return element at that position or null
     */
    public LayerElement get(int x, int y){
        return (LayerElement) elements.get(x, y);
    }
    
    /**
     * Gets the surrounding places, without the center place
     * @param _x center coordinate
     * @param _y center coordinate
     * @param distance maximum distance in each drection
     * @return
     */
    public LinkedList<Place> get_neighbors(int _x, int _y, int distance){
        LinkedList<Place> ret = new LinkedList<Place>();
        distance = Math.abs(distance);
        for(int x = -distance; x <= distance; ++x){
            for(int y = -distance; y <= distance; ++y){
                if(!(x == 0 && y == 0)){ // if not center place
                    LayerElement el = get(_x + x, _y + y);
                    if(el != null) ret.add((Place) el);
                }
            }
        }
        return ret;
    }
    
    /**
     * Gets the id of the layer
     * @return layer id
     */
    public int get_id(){
        return id;
    }
    
    /**
     * Gets the world
     * @return world
     */
    public World get_world(){
        return world;
    }
    
    /**
     * Gets the id
     * @return layer id
     */
    @Override
    public String toString(){
        return String.valueOf(get_id());
    }
    
    /**
     * Removes an element from the layer but not from the world
     * @param element 
     * @throws mudmap2.backend.Layer.PlaceNotFoundException 
     */
    public void remove(LayerElement element) throws RuntimeException, PlaceNotFoundException{
        if(element.get_layer() != this) throw new RuntimeException("Element not in this layer");
        // element on the layer before placing the new one
        LayerElement el_bef = get(element.get_x(), element.get_y());
        if(el_bef != element && el_bef != null) throw new RuntimeException("Element location mismatch (" + element.get_x() + ", " + element.get_y() + ")");
        elements.remove(element.get_x(), element.get_y());
    }
    
    /**
     * Returns true, if an element at position x,y exists
     * @param x x position
     * @param y < position
     * @return true, if an element exists
     */
    public boolean exist(int x, int y){
        return elements.exist(x, y);
    }
    
    /**
     * Gets a collection of all elements
     * @return set of all elements or empty set
     */
    public HashSet<Place> get_places(){
        return elements.values();
    }
    
    /**
     * This exception will be thrown, if a place doesn't exist at a certain position
     */
    public static class PlaceNotFoundException extends Exception {
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

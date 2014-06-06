package mudmap2.backend;

import java.util.TreeMap;

/**
 * A layer stores places relatively to each other by position on the map.
 * Each world can consist of multiple layers
 * 
 * @author neop
 */
public class Layer {
    
    TreeMap<Integer, TreeMap<Integer, LayerElement>> elements;
    
    public Layer() {
        elements = new TreeMap<Integer, TreeMap<Integer, LayerElement>>();
    }
    
    /**
     * Sets the element at a positin
     * @param x x coordinate
     * @param y y coordinate
     * @param element new element
     */
    public void put(LayerElement element, int x, int y) throws Exception{
        element.get_layer().remove(element);
        element.set_position(x, y, this);
        put(element);
    }
    
    /**
     * Adds an element to the layer, uses the position of the element
     * @param element element to be added
     */
    public void put(LayerElement element) throws Exception{
        if(exist(element.get_x(), element.get_y())) throw new Exception("Position " + element.get_x() + ", " + element.get_y() + " is already in use");
        // create map if it doesn't exist
        if(!elements.containsKey(element.get_x())) elements.put(element.get_x(), new TreeMap<Integer, LayerElement>());
        elements.get(element.get_x()).put(element.get_y(), element);
    }
    
    /**
     * Gets the element at a position
     * @param x x coordinate
     * @param y y coordinate
     * @return element at that position
     */
    public LayerElement get(int x, int y) throws RuntimeException{
        if(!exist(x, y)) throw new RuntimeException("Element at position " + x + ", " + y + " doesn't exist");
        return elements.get(x).get(y);
    }
    
    /**
     * Removes an element from the layer
     * @param element 
     */
    public void remove(LayerElement element) throws RuntimeException{
        if(element.get_layer() != this) throw new RuntimeException("Element not in this layer");
        if(get(element.get_x(), element.get_y()) != element) throw new RuntimeException("Element location mismatch (" + element.get_x() + ", " + element.get_y() + ")");
        elements.get(element.get_x()).remove(element.get_y());
    }
    
    /**
     * Returns true, if an element at position x,y exists
     * @param x x position
     * @param y < position
     * @return true, if an element exists
     */
    public boolean exist(int x, int y){
        if(!elements.containsKey(x) || !elements.get(x).containsKey(y)) return false;
        return true;
    }
    
}

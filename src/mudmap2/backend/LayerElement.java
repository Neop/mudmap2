package mudmap2.backend;

/**
 * An element of a layer
 * @author neop
 */
public class LayerElement {    
    
    private int x, y;
    private Layer layer;
    
    /**
     * constructs a layer element
     * @param _x x position
     * @param _y y position
     * @param l layer
     */
    public LayerElement(int _x, int _y, Layer l){
        x = _x;
        y = _y;
        layer = l;
    }
    
    /**
     * Gets the x position
     * @return x position
     */
    public int get_x(){
        return x;
    }
    
    /**
     * Gets the y position
     * @return y position
     */
    public int get_y(){
        return y;
    }
    
    /**
     * Gets the layer
     * @return layer
     */
    public Layer get_layer(){
        return layer;
    }
    
    /**
     * Sets the position
     * @param _x x position
     * @param _y y position
     * @param l layer
     */
    public void set_position(int _x, int _y, Layer l){
        x = _x;
        y = _y;
        layer = l;
    }
    
}
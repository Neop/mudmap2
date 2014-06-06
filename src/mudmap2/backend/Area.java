package mudmap2.backend;

/**
 * An area is a collection of places, marked by a color
 * 
 * @author neop
 */
public class Area {
    
    int id;
    String name;
    Color color;
    
    /**
     * Constructs a new Area
     * @param id area id
     * @param _name Name of the area
     * @param col 
     */
    public Area(int _id, String _name, Color col) {
        id = _id;
        name = _name;
        color = col;
    }

    /**
     * Constructs a new Area
     * @param id area id
     * @param _name Name of the area
     */
    Area(int _id, String _name) {
        id = _id;
        name = _name;
        color = new Color(0, 0, 0);
    }
    
    /**
     * Gets the are name
     * 
     * @return area name
     */
    public String get_name(){
        return name;
    }
    
    /**
     * Sets a new name
     * @param _name new area name
     */
    public void set_name(String _name){
        name = _name;
    }
    
    /**
     * Gets the area color
     * 
     * @return area color
     */
    public Color get_color(){
        return color;
    }
    
    /**
     * Sets a new area color
     * 
     * @param col new area color
     */
    public void set_color(Color col){
        color = col;
    }
}

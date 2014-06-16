package mudmap2.backend;

/**
 * Describes a risk level (which are shown as the colored border of each place)
 * @author neop
 */
public class RiskLevel {
    // next id to be assigned
    static int next_id = 0;
    
    int id;
    public String description;
    public Color color;
    
    /**
     * Constructs the risk level
     * @param _id identification number, unique in a world
     * @param desc description
     * @param _color color that represents the risk level
     */
    public RiskLevel(int _id, String desc, Color _color){
        id = _id;
        if(id >= next_id) next_id = id + 1;
        description = desc;
        color = _color;
    }

    /**
     * Constructs the risk level
     * @param _id identification number, unique in a world
     * @param desc description
     * @param _color color that represents the risk level
     */
    public RiskLevel(String desc, Color _color){
        id = next_id++;
        description = desc;
        color = _color;
    }
    
    /**
     * Gets the risk level id
     * @return id
     */
    public int get_id(){
        return id;
    }
    
    /**
     * Gets the color
     * @return color of the risk level
     */
    public Color get_color(){
        return color;
    }
    
    /**
     * Sets the color
     * @param c new color
     */
    public void set_color(Color c){
        color = c;
    }
    
    /**
     * Gets the description of the risk level
     * @return description
     */
    public String get_description(){
        return toString();
    }
    
    /**
     * Sets the description
     * @param desc new description
     */
    public void set_description(String desc){
        description = desc;
    }
    
    /**
     * Gets the description of the risk level
     * @return description
     */
    @Override
    public String toString(){
        return description;
    }
}

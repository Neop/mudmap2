package mudmap2.backend;

/**
 * Describes a risk level (which are shown as the colored border of each place)
 * @author neop
 */
public class RiskLevel {
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
}

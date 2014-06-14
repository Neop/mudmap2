package mudmap2.backend;

/**
 * Describes an exit direction of a place
 * 
 * @author neop
 */
public class ExitDirection {
    /**
     * Constructs a exit direction
     * 
     * @param _dir
     */
    public ExitDirection(String _dir){
        dir = _dir;
    }

    /**
     * Gets the way direcion String
     * @return Way direction String
     */
    public String get_dir(){
        return toString();
    }
    
    /**
     * Gets the way direcion String
     * @return Way direction String
     */
    @Override
    public String toString(){
        return dir;
    }
    
    private String dir;
}

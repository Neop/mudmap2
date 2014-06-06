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
     * @param _dir_abbreviation 
     */
    public ExitDirection(String _dir, String _dir_abbreviation){
        dir = _dir;
        dir_abbreviation = _dir_abbreviation;
    }

    /** 
     * Gets the way direction abbreviation
     * 
     * @return way direction abbreviation
     */
    public String get_abbreviation(){
        return dir_abbreviation;
    }
    
    /**
     * Gets the way direcion String
     * @return Way direction String
     */
    @Override
    public String toString(){
        return dir;
    }
    
    private String dir, dir_abbreviation;
}

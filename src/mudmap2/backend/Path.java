package mudmap2.backend;

/**
 * Describes a way direction between two places
 * 
 * @author neop
 */
public class Path {
    private Place[] places;
    private ExitDirection[] exitdirections;
    
    /**
     * Constructs a new path between two places
     * 
     * @param pl1 A place to connect
     * @param exitdir1 the exit of place 1 to be used
     * @param pl2 the other place
     * @param exitdir2 exit of place 2
     */
    public Path(Place pl1, ExitDirection exitdir1, Place pl2, ExitDirection exitdir2) {
        places = new Place[2];
        places[0] = pl1;
        places[1] = pl2;
        
        exitdirections = new ExitDirection[2];
        exitdirections[0] = exitdir1;
        exitdirections[1] = exitdir2;
    }
    
    /**
     * Gets the connected places
     * 
     * @return the two connected places
     */
    public Place[] get_places(){
        return places;
    }
    
    /**
     * Checks whether a certain place is connected with this path
     * @param place
     * @return true if place is in this path
     */
    public boolean has_place(Place place){
        if(places[0] == place || places[1] == place) return true;
        return false;
    }
    
    /**
     * Gets the exit directions
     * 
     * @return The two exit directions
     */
    public ExitDirection[] get_exit_directions(){
        return exitdirections;
    }
    
    /**
     * Gets the exit direction of a place p used in this path
     * @param p
     * @return the exit direction of p in the path
     * @throws RuntimeException if the place isn't a member of the path
     */
    public ExitDirection get_exit(Place p) throws RuntimeException{
        if(places[0] == p) return exitdirections[0];
        else if(places[1] == p) return exitdirections[1];
        else throw new RuntimeException("Place not found in path");
    }
}
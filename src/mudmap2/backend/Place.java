package mudmap2.backend;

import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A place in the world
 * @author neop
 */
public class Place extends LayerElement implements Comparable<Place> {
    // next id to be assigned
    static int next_id;
    
    int id;
    String name;
    Area area;
    int rec_lvl_min, rec_lvl_max, risk_lvl;
    boolean has_shop, has_teacher, has_food, has_beverages;
    
    TreeSet<Place> children, parents;
    TreeSet<Path> connected_places;
    TreeMap<String, Boolean> flags;
    LinkedList<String> comments;
    
    public Place(int _id, String _name, int pos_x, int pos_y, Layer l){
        super(pos_x, pos_y, l);
        name = _name;
        area = null;
        id = _id;
        if(_id >= next_id) next_id++;
        
        initialize();
    }
    
    /**
     * Constructs new a place a certain position
     * @param _name name
     * @param _pos_x x coordinate
     * @param _pos_y y coordinate
     */
    public Place(String _name, int pos_x, int pos_y, Layer l){
        super(pos_x, pos_y, l);
        name = _name;
        area = null;
        id = next_id++;
        
        initialize();
    }
    
    /**
     * Initializes the place
     */
    private void initialize(){
        children = new TreeSet<Place>();
        parents = new TreeSet<Place>();
        connected_places = new TreeSet<Path>();
        flags = new TreeMap<String, Boolean>();
        comments = new LinkedList<String>();
    }
    
    /**
     * Gets the place id
     * @return place id
     */
    public int get_id(){
        return id;
    }
    
    /**
     * Gets the name
     * @return name of the place
     */
    public String get_name(){
        return name;
    }
    
    /**
     * Sets the name
     * @param _name new name
     */
    public void set_name(String _name){
        name = _name;
    }
    
    /**
     * Gets the area
     * @return area
     */
    public Area get_area(){
        return area;
    }
    
    /**
     * Sets the area
     * @param _area 
     */
    public void set_area(Area _area) {
        area = _area;
    }
    
    /**
     * Gets the minimal recommended level
     * @return minimal recommended level
     */
    public int get_rec_lvl_min(){
        return rec_lvl_min;
    }
    
    /**
     * Sets the minimal recommended level
     * @param _rec_lvl_min 
     */
    public void set_rec_lvl_min(int _rec_lvl_min){
        rec_lvl_min = _rec_lvl_min;
    }
    
    /**
     * Gets the maximal recommended level
     * @return maximal recommended level
     */
    public int get_rec_lvl_max(){
        return rec_lvl_max;
    }
    
    /**
     * Sets the maximal recommended level
     * @param _rec_lvl_max 
     */
    public void set_rec_lvl_max(int _rec_lvl_max){
        rec_lvl_max = _rec_lvl_max;
    }
    
    /**
     * Gets the risk level
     * @return risk level
     */
    public int get_risk_lvl(){
        return risk_lvl;
    }
    
    /**
     * sets the risk level
     * @param _risk_lvl 
     */
    public void set_risk_lvl(int _risk_lvl){
        risk_lvl = _risk_lvl;
    }
    
    /**
     * Adds a comment at the end of the list
     */
    public void add_comment(String comment){
        comments.add(comment);
    }
    
    /**
     * removes all comments
     */
    public void delete_comments(){
        comments.clear();
    }
    
    /**
     * Gets the comments list
     * @return comments list
     */
    public LinkedList<String> get_comments(){
        return comments;
    }
    
    /**
     * Gets the path connected to an exit
     * @param dir exit direction
     * @return path connected to that exit
     */
    public Path get_exit(ExitDirection dir){
        for(Path path: connected_places){
            if(path.get_exit(this) == dir) return path;
        }
        throw new RuntimeException();
    }
    
    /**
     * Gets the path to a place, if available
     * @param place a place that this place is connected to
     * @return path to place, if available
     */
    public Path get_path(Place place){
        for(Path path: connected_places){
            if(path.has_place(place)) return path;
        }
        throw new RuntimeException();
    }
    
    /**
     * Connects a place to another one tht is specified in path
     * If 'this place' is not in path an exception will be thrown
     * @param p 
     */
    public void connect_path(Path path) throws RuntimeException{
        Place[] pp = path.get_places();
        Place other;
        
        if(pp[0] == this) other = pp[1];
        else if(pp[1] == this) other = pp[0];
        else throw new RuntimeException("This place is not specified in given path");
        
        connected_places.add(path);
        other.connected_places.add(path);
    }
    
    /**
     * Gets all paths
     * @return all paths
     */
    public TreeSet<Path> get_paths(){
        return connected_places;
    }
    
    /**
     * Gets a flag value
     * @param key flag name
     * @return flag value
     */
    public boolean get_flag(String key){
        if(flags.containsKey(key)) return flags.get(key);
        return false;
    }
    
    /**
     * Sets a string flag
     * @param key flag name
     * @param state value
     */
    public void set_flag(String key, boolean state){
        flags.put(key, state);
    }
    
    public void connect_child(Place p){
        children.add(p);
        p.parents.add(this);
    }
    
    @Override
    public int compareTo(Place arg0) {
        if(arg0 == null) throw new NullPointerException();
        return (this.id == arg0.id) ? 0 : 1;
    }
}

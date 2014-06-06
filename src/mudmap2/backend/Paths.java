/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mudmap2.backend;

/**
 * Helper class to get common paths and filenames
 * @author neop
 */
public class Paths {
    
    /**
     * Gets the user data path
     * @return user data path
     */
    public static String get_user_data_dir(){
        // read the user data path from environment variables
        // operating system Windows
        if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0)
            user_data_dir = System.getenv().get("APPDATA") + "/mudmap/";
        // other operating systems
        else user_data_dir = System.getProperty("user.home") + "/.mudmap/";
        
        return user_data_dir;
    }
    
    /**
     * Gets the directory that contains the world files
     * @return worlds directory
     */
    public static String get_worlds_dir(){
        return get_user_data_dir() + "worlds/";
    }
    
    /**
     * Gets the available worlds file (path + filename)
     * @return available worlds file
     */
    public static String get_available_worlds_file(){
        return get_worlds_dir() + "worlds";
    }
    
    static String user_data_dir;
}

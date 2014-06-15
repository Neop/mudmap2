/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mudmap2.backend;

import mudmap2.Paths;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author neop
 */
public class WorldManager {
    
    /// contains the available worlds <name, file>
    static HashMap<String, String> available_worlds = new HashMap<String, String>();
    /// contains the loaded worlds <file, world>
    static HashMap<String, World> loaded_worlds = new HashMap<String, World>();
    
    /**
     * Reads the available worlds list
     */
    public static void read_world_list(){
        // the available worlds will be read from the worlds file
        // files found in the world directory
        // files found in the world directory
        
        // read from available worlds file
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Paths.get_available_worlds_file()));
            
            String line, name = new String(), file = new String();
            try {
                while((line = reader.readLine()) != null){
                    line = line.trim();
                    
                    if(line.charAt(0) == 'n'){ // world name
                        // save world if found
                        // TODO: check if the file exists?
                        if(!name.isEmpty() && !file.isEmpty()){
                            if(!available_worlds.containsKey(name)) available_worlds.put(name, file);
                            name = new String();
                            file = new String();
                        }
                        name = line.substring(2).trim();
                    } else if(line.charAt(0) == 'f') // file name
                        file = line.substring(2).trim();
                }
            } catch (IOException ex) {
                Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            // save last found world
            if(!available_worlds.containsKey(name)) available_worlds.put(name, file);
            
        } catch (FileNotFoundException ex) {
            System.out.println("Couldn't open available worlds file \"" + Paths.get_available_worlds_file() + "\", file not found");
            Logger.getLogger(WorldManager.class.getName()).log(Level.INFO, null, ex);
        }
        
        // read from directory
        // get file list
        File dir = new File(Paths.get_worlds_dir());
        File[] fileList = dir.listFiles();
        
        // find world files in file list
        for(File file : fileList){
            // exclude meta and backup files
            if(!file.getName().endsWith("_meta") && !file.getName().endsWith(".backup")){
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    try {
                        String line;
                        boolean is_world_file = false;
                        while((line = reader.readLine()) != null && !is_world_file){
                            if(line.trim().startsWith("wname")){
                                // world name found in file, save it
                                String name = line.trim().substring(6).trim();
                                if(!available_worlds.containsKey(name))
                                    available_worlds.put(name, file.toString());
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(WorldManager.class.getName()).log(Level.INFO, null, ex);
                }
            }
        }
    }
    
    /**
     * Saves the available worlds list
     */
    public static void save_world_list(){
        // TODO: implement this
    }
    
    /**
     * Gets the filename of a world
     * @param name name of a world in the available worlds list
     * @return filename of the world
     */
    public static String get_world_file(String name){
        return available_worlds.get(name);
    }
    
    /**
     * Gets a world, loads it if necessary
     * @param file file of a world
     * @return a world
     */
    public static World get_world(String file){
        if(!loaded_worlds.containsKey(file)) try {
            loaded_worlds.put(file, new World(file));
        } catch (Exception ex) {
            Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return loaded_worlds.get(file);
    }
    
    /**
     * Gets the names of all found worlds
     * @return list of world names
     */
    public static Set<String> get_world_list(){
        return available_worlds.keySet();
    }
    
    /**
     * Creates a new world and adds it to the available worlds list
     * @param name name of the world
     * @param file file of the world
     */
    public static void create_world(String name, String file) throws Exception{
        // check if name already exists
        if(!available_worlds.containsKey(name)){
            // check if the file already exists
            File f = new File(file);
            if(!f.exists() && !f.isDirectory()){
                loaded_worlds.put(file, new World(file, name));
                available_worlds.put(name, file);
            } throw new Exception("File \"" + file + "\" already exists");
        } else throw new Exception("A world with name \"" + name + "\" already exists");
    }
    
}

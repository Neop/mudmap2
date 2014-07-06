/*  MUD Map (v2) - A tool to create and organize maps for text-based games
 *  Copyright (C) 2014  Neop (email: mneop@web.de)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, see <http://www.gnu.org/licenses/>.
 */

/*  File description
 *
 *  This class manages the available worlds: it searches for world files in the
 *  main data directory and looks for the worlds specified in the "worlds"
 *  file
 */

package mudmap2.backend;

import mudmap2.Paths;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author neop
 */
public class WorldManager {
    
    /// contains the available worlds <name, file>
    static HashMap<String, String> available_worlds = new HashMap<String, String>();
    /// contains the loaded worlds <file, world>
    static HashMap<String, World> loaded_worlds = new HashMap<String, World>();
    
    static final int meta_file_ver_major = 1;
    static final int meta_file_ver_minor = 1;
    
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
            
            boolean relative_path = false;
            String line, name = new String(), file = new String();
            try {
                while((line = reader.readLine()) != null){
                    line = line.trim();
                    
                    if(line.charAt(0) == 'n'){ // world name
                        // save world if found
                        if(!name.isEmpty() && !file.isEmpty()){
                            if(!available_worlds.containsKey(name)){
                                if(Paths.file_exists(file)) available_worlds.put(name, file);
                                else if(Paths.file_exists(Paths.get_worlds_dir() + file)) available_worlds.put(name, Paths.get_worlds_dir() + file);
                            }
                            relative_path = false;
                            name = new String();
                            file = new String();
                        }
                        name = line.substring(2).trim();
                    } else if(line.charAt(0) == 'f' && !relative_path) // file name
                        file = line.substring(2).trim();
                    else if(line.charAt(0) == 'g'){ // file name (new, relative format)
                        file = line.substring(2).trim();
                        relative_path = true;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            // save last found world
            if(!available_worlds.containsKey(name) && Paths.file_exists(file)) available_worlds.put(name, file);
            
        } catch (FileNotFoundException ex) {
            System.out.println("Couldn't open available worlds file \"" + Paths.get_available_worlds_file() + "\", file not found");
            //Logger.getLogger(WorldManager.class.getName()).log(Level.INFO, null, ex);
        }
        
        // read from directory
        // get file list
        File dir = new File(Paths.get_worlds_dir());
        File[] fileList = null;
        if(dir != null) fileList = dir.listFiles();

        if(fileList != null){
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
    }
    
    /**
     * Saves the available worlds list
     * do this after writing the world files or new places won't appear in list
     */
    public static void write_world_list(){
        final String file = Paths.get_worlds_dir() + "worlds";
        try {
            // open file
            if(!Paths.is_directory(Paths.get_worlds_dir())) Paths.create_directory(Paths.get_worlds_dir());
            PrintWriter outstream = new PrintWriter(new BufferedWriter( new FileWriter(file)));

            outstream.println("# MUD Map (v2) worlds file");
            outstream.println("ver " + meta_file_ver_major + "." + meta_file_ver_minor);
            
            for(Entry<String, String> w: available_worlds.entrySet()){
                if(Paths.file_exists(w.getValue())){
                    outstream.println("n " + w.getKey());
                    String w_file = w.getValue();
                    outstream.println("f " + w_file);
                    if(w_file.startsWith(Paths.get_worlds_dir())){
                        w_file = w_file.substring(Paths.get_worlds_dir().length());
                        outstream.println("g " + w_file);
                    }
                }
            }
            
            outstream.close();
        } catch (IOException ex) {
            System.out.printf("Couldn't write worlds file " + file);
            //Logger.getLogger(WorldManager.class.getName()).log(Level.INFO, null, ex);
        }
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
            if(!Paths.file_exists(file)){
                loaded_worlds.put(file, new World(file, name));
                available_worlds.put(name, file);
            } else throw new Exception("File \"" + file + "\" already exists");
        } else throw new Exception("A world with name \"" + name + "\" already exists");
        
        loaded_worlds.put(file, new World(file, name));
    }
    
    /**
     * Creates a new world and adds it to the available worlds list
     * @param name world name
     */
    public static void create_world(String name) throws Exception{
        String path = Paths.get_worlds_dir();
        String file = name;
        
        // create available file path, if necessary
        boolean file_ok = false;
        for(int num = 0; !file_ok; ++num){
            file_ok = true;
            file = (num > 0 ? "_" + num : "");
            for(String s: available_worlds.values()) if(s.toLowerCase().equals((path + name + file).toLowerCase())) file_ok = false;
        }
        
        create_world(name, path + name + file);
    }
    
    /**
     * Deletes a worl, this can not be undone
     * @param name 
     */
    public static void delete_world(String name){
        String file = get_world_file(name);
        
        if(file != null && !file.isEmpty()){
            if(loaded_worlds.containsKey(file))
                JOptionPane.showMessageDialog(null, "Can't delete world \"" + name + "\", it is currently loaded", "Delete world", JOptionPane.INFORMATION_MESSAGE);
            else {
                int ret = JOptionPane.showConfirmDialog(null, "Do you want to delete \"" + name + "\"", "Delete world", JOptionPane.YES_NO_OPTION);
                if(ret == JOptionPane.YES_OPTION){
                    // delete world file
                    try {
                        (new File(file)).delete();
                    } catch (Exception e) {}

                    // delete world meta file
                    try {
                        (new File(file + "_meta")).delete();
                    } catch (Exception e) {}    
                }
            }
        }
    }
    
    /**
     * Removes a world from loaded worlds list
     * @param file 
     */
    public static void close_world(String file){
        loaded_worlds.remove(file);
    }
    
}

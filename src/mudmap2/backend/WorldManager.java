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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import mudmap2.backend.WorldFileReader.WorldReadException;

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
    public static void readWorldList(){
        // the available worlds will be read from the worlds file
        // files found in the world directory
        // files found in the world directory
        
        // read from available worlds file
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Paths.getAvailableWorldsFile()));
            
            boolean relative_path = false;
            String line, name = new String(), file = new String();
            try {
                while((line = reader.readLine()) != null){
                    line = line.trim();
                    
                    if(line.charAt(0) == 'n'){ // world name
                        // save world if found
                        if(!name.isEmpty() && !file.isEmpty()){
                            if(name != null && file != null && !available_worlds.containsKey(file)){
                                if(Paths.fileExists(file)) available_worlds.put(file, name);
                                else if(Paths.fileExists(Paths.getWorldsDir() + file)) available_worlds.put(Paths.getWorldsDir() + file, name);
                            }
                            relative_path = false;
                            file = new String();
                        }
                        if(line.length() > 2)
                            name = line.substring(2).trim();
                        else name = null;
                    } else if(line.charAt(0) == 'f' && !relative_path && line.length() > 3){ // file name
                        file = line.substring(2).trim();
                    } else if(line.charAt(0) == 'g' && line.length() > 3){ // file name (new, relative format)
                        file = line.substring(2).trim();
                        relative_path = true;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            // save last found world
            if(name != null && file != null && !available_worlds.containsKey(file)){
                if(Paths.fileExists(file)) available_worlds.put(file, name);
                else if(Paths.fileExists(Paths.getWorldsDir() + file)) available_worlds.put(Paths.getWorldsDir() + file, name);
            }
            
        } catch (FileNotFoundException ex) {
            System.out.println("Couldn't open available worlds file \"" + Paths.getAvailableWorldsFile() + "\", file not found");
        }
    }
    
    public static void findWorlds(){
        // read from directory
        // get file list
        File dir = new File(Paths.getWorldsDir());
        File[] fileList = dir.listFiles();

        if(fileList != null){
            // find world files in file list
            for(File file : fileList){
                // exclude meta and backup files
                if(!file.getName().equals("worlds")
                        && !file.getName().endsWith("_meta") 
                        && !file.getName().endsWith(".backup") 
                        && !file.getName().endsWith(".bak")
                        && file.isFile() && file.canRead()){
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        try {
                            String line;
                            boolean is_world_file = false;
                            while((line = reader.readLine()) != null && !is_world_file){
                                if(line.trim().startsWith("wname")){
                                    // world name found in file, save it
                                    String name = line.trim().substring(6).trim();
                                    if(!available_worlds.containsKey(file.getPath()))
                                        available_worlds.put(file.getPath(), name);
                                }
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(WorldManager.class.getName()).log(Level.WARNING, null, ex);
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
    public static void writeWorldList(){
        final String file = Paths.getWorldsDir() + "worlds";
        try {
            // open file
            if(!Paths.isDirectory(Paths.getWorldsDir())) Paths.createDirectory(Paths.getWorldsDir());
            PrintWriter outstream = new PrintWriter(new BufferedWriter( new FileWriter(file)));

            outstream.println("# MUD Map (v2) worlds file");
            outstream.println("ver " + meta_file_ver_major + "." + meta_file_ver_minor);
            
            for(Entry<String, String> w: available_worlds.entrySet()){
                // check whether the file name in file equals the name in the list
                String fw = readWorldName(w.getKey());
                if(fw != null && fw.equals(w.getValue())){
                    outstream.println("n " + w.getValue());
                    String w_file = w.getKey();
                    outstream.println("f " + w_file);
                    if(w_file.startsWith(Paths.getWorldsDir())){
                        w_file = w_file.substring(Paths.getWorldsDir().length());
                        outstream.println("g " + w_file);
                    }
                }
            }
            
            outstream.close();
        } catch (IOException ex) {
            System.out.printf("Couldn't write worlds file " + file);
            Logger.getLogger(WorldManager.class.getName()).log(Level.INFO, null, ex);
            JOptionPane.showMessageDialog(null, "Could not write worlds list file " 
                    + file + ".\nYou might have to open your worlds manually from " 
                    + Paths.getWorldsDir(), "MUD Map WorldManager", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Gets the filename of a world
     * @param name name of a world in the available worlds list
     * @return filename of the world
     */
    public static String getWorldFile(String name){
        for(Entry<String, String> e: available_worlds.entrySet())
            if(e.getValue().equals(name)) return e.getKey();
        return null;
    }
    
    /**
     * Gets a world, loads it if necessary
     * @param file file of a world
     * @return a world
     */
    public static World getWorld(String file){
        if(!loaded_worlds.containsKey(file)){
            try {
                loaded_worlds.put(file, new World(file));
            } catch (WorldReadException ex) {
                Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Loading world", JOptionPane.ERROR_MESSAGE);
            }
        }
        return loaded_worlds.get(file);
    }
    
    /**
     * Gets the names of all found worlds
     * @return list of world names
     */
    public static HashMap<String, String> getWorlds(){
        return available_worlds;
    }
    
    /**
     * Creates a new world and adds it to the available worlds list
     * @param name name of the world
     * @param file file of the world
     * @return 
     * @throws java.lang.Exception
     */
    public static World createWorld(String name, String file) throws Exception{
        // check if name already exists
        if(!available_worlds.containsKey(file)){
            // check if the file already exists
            file = file.replaceAll("\\s", "_");
            if(!Paths.fileExists(file)){
                
                System.out.println("Name: " + name);
                System.out.println("File: " + file);
                
                loaded_worlds.put(file, new World(file, name));
                available_worlds.put(file, name);
            } else throw new Exception("File \"" + file + "\" already exists");
        } else throw new Exception("File \"" + file + "\" is already in list");
        
        World ret;
        loaded_worlds.put(file, ret = new World(file, name));
        
        return ret;
    }
    
    /**
     * Creates a new world and adds it to the available worlds list
     * @param name world name
     * @return 
     * @throws java.lang.Exception
     */
    public static World createWorld(String name) throws Exception{
        String path = Paths.getWorldsDir();
        String file = name;
        
        // create available file path, if necessary
        boolean file_ok = false;
        for(int num = 0; !file_ok; ++num){
            file_ok = true;
            file = (num > 0 ? "_" + num : "");
            for(String s: available_worlds.keySet()) if(s.toLowerCase().equals((path + name + file).toLowerCase())) file_ok = false;
        }
        
        return WorldManager.createWorld(name, path + name + file);
    }
    
    /**
     * Adds a world file to the list
     * @param file 
     * @return world name
     * @throws java.lang.Exception
     */
    public static String addWorld(String file) throws Exception{
        String name = readWorldName(file);
        if(name != null && !available_worlds.containsKey(file)){
            available_worlds.put(file, name);
        } else throw new Exception("Can't add world, name is already in list");
        return name;
    }
    
    /**
     * Deletes a world, this can not be undone
     * @param file
     */
    public static void deleteWorld(String file){
        if(file != null && !file.isEmpty()){
            if(loaded_worlds.containsKey(file))
                JOptionPane.showMessageDialog(null, "Can't delete world \"" + file + "\", it is currently loaded", "Delete world", JOptionPane.INFORMATION_MESSAGE);
            else {
                int ret = JOptionPane.showConfirmDialog(null, "Do you want to delete \"" + file + "\"", "Delete world", JOptionPane.YES_NO_OPTION);
                if(ret == JOptionPane.YES_OPTION){
                    // delete world file
                    try {
                        (new File(file)).delete();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "Could not delete world file: " + e.getLocalizedMessage(), "Deleting world", JOptionPane.ERROR_MESSAGE);
                    }

                    // delete world meta file
                    try {
                        (new File(file + "_meta")).delete();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "Could not delete world meta file: " + e.getLocalizedMessage(), "Deleting world", JOptionPane.ERROR_MESSAGE);
                    }    
                }
            }
        }
    }
    
    /**
     * Opens a world file and reads the world name specified in it
     * @param file world file
     * @return world name or ""
     */
    public static String readWorldName(String file){
        String ret = null;
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            
            String line;
            try {
                int line_cnt = 0;
                while((line = reader.readLine()) != null){
                    line = line.trim();
                    
                    if(line.startsWith("wname")){ // world name
                        ret = line.substring(6).trim();
                        break;
                    }
                    // the world name should be in one of the first lines
                    if(line_cnt++ > 15) break;
                }
            } catch (IOException ex) {
                Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {} // no output needed
        
        return ret;
    }
    
    /**
     * Removes a world from loaded worlds list
     * @param file 
     */
    public static void closeWorld(String file){
        loaded_worlds.remove(file);
    }
    
}

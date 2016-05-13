/*  MUD Map (v2) - A tool to create and organize maps for text-based games
 *  Copyright (C) 2016  Neop (email: mneop@web.de)
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
package mudmap2.backend;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import mudmap2.Paths;
import mudmap2.backend.WorldFileReader.current.WorldFileDefault;

/**
 *
 * @author neop
 */
public class WorldFileList {

    // availableWorlds: <file, name>
    private static final HashMap<String, String> availableWorlds = new HashMap<>();

    static final int metaFileVerMajor = 1;
    static final int metaFileVerMinor = 1;

    public static String getWorldName(String file){
        return availableWorlds.get(file);
    }

    public static void setWorldName(String file, String name){
        availableWorlds.put(file, name);
    }

    public static void removeWorldFileEntry(String file){
        availableWorlds.remove(file);
    }

    public static void clear(){
        availableWorlds.clear();
    }

    public static Map<String, String> getWorlds(){
        return availableWorlds;
    }

    /**
     * Try to find worlds by reading the worlds directory
     * and by reading the worlds file
     */
    public static void findWorlds(){
        readDirectory();
        readWorldList();
    }

    /**
     * Get available worlds from filesystem
     */
    public static void readDirectory(){
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

                    // check if file is world file
                    WorldFileDefault worldFile = new WorldFileDefault(file.getPath());
                    try {
                        if(worldFile.canRead()){ // is world file
                            String name = worldFile.readWorldName();
                            availableWorlds.put(file.getPath(), name);
                        }
                    } catch (FileNotFoundException ex) {
                    } catch (Exception ex) {
                        Logger.getLogger(WorldFileList.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    /**
     * Get available worlds from worlds file
     */
    public static void readWorldList(){
        try {
            // read from available worlds file
            BufferedReader reader = new BufferedReader(new FileReader(Paths.getAvailableWorldsFile()));

            String line;
            while((line = reader.readLine()) != null){
                line = line.trim();
                if(line.startsWith("f ")){ // world file entry
                    String file = line.substring(2).trim();

                    WorldFileDefault worldFile = new WorldFileDefault(file);
                    if(worldFile.canRead()){ // is world file
                        String name;
                        try {
                            // get world name
                            name = worldFile.readWorldName();
                        } catch (Exception ex) {
                            Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, null, ex);
                            // use file name if world name not found
                            name = file.substring(file.lastIndexOf('/') + 1);
                        }
                        // add found world to list
                        availableWorlds.put(file, name);
                    }
                }
            }
        } catch (FileNotFoundException ex) {} catch (IOException ex) {
            Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, null, ex);
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
            try (PrintWriter outstream = new PrintWriter(new BufferedWriter( new FileWriter(file)))) {
                outstream.println("# MUD Map (v2) worlds file");
                outstream.println("ver " + metaFileVerMajor + "." + metaFileVerMinor);

                for(Map.Entry<String, String> w: availableWorlds.entrySet()){
                    // check whether the file name in file equals the name in the list
                    WorldFileDefault worldFile = new WorldFileDefault(w.getKey());
                    String fw = null;
                    try {
                        fw = worldFile.readWorldName();
                    } catch (Exception ex) {
                        Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if(fw != null){
                        outstream.println("n " + fw);
                        String w_file = w.getKey();
                        outstream.println("f " + w_file);
                        if(w_file.startsWith(Paths.getWorldsDir())){
                            w_file = w_file.substring(Paths.getWorldsDir().length());
                            outstream.println("g " + w_file);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            System.out.printf("Couldn't write worlds file " + file);
            Logger.getLogger(WorldManager.class.getName()).log(Level.INFO, null, ex);
            JOptionPane.showMessageDialog(null, "Could not write worlds list file "
                    + file + ".\nYou might have to open your worlds manually from "
                    + Paths.getWorldsDir(), "WorldManager", JOptionPane.WARNING_MESSAGE);
        }
    }
}

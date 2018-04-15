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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mudmap2.Environment;
import mudmap2.backend.WorldFileReader.WorldFile;
import mudmap2.backend.WorldFileReader.current.WorldFileDefault;

/**
 *
 * @author neop
 */
public class WorldManager {

    // loadedWorlds: <world, file>
    private static final HashMap<String, World> loadedWorlds = new HashMap<>();

    /**
     * Get loaded world or load world from file
     * @param file world file
     * @return
     * @throws Exception if world could not be loaded
     */
    public static World getWorld(String file) throws Exception{
        World world = null;
        if(loadedWorlds.containsKey(file)){ // world in list
            world = loadedWorlds.get(file);
        } else { // world not loaded
            WorldFile worldFile = new WorldFileDefault(file);
            if(worldFile.canRead()){
                world = worldFile.readFile();
                worldFile.backup();
                putWorld(file, world);
            } else {
                throw new Exception("Could not read world file");
            }
        }
        return world;
    }

    /**
     * Get map of all loaded worlds
     * @return
     */
    public static Map<String, World> getWorlds(){
        return loadedWorlds;
    }

    /**
     * Add world to list
     * @param file
     * @param world
     */
    public static void putWorld(String file, World world){
        loadedWorlds.put(file, world);

        WorldFileList.WorldFileEntry entry = WorldFileList.get(new File(file));
        if(entry == null){
            entry = new WorldFileList.WorldFileEntry(world.getName(), new File(file));
        }
        //entry.setWorld(world);
        WorldFileList.push(entry);
    }

    /**
     * getPlace all filenames associated to a world
     * @param world
     * @return list of filenames or empty list
     */
    public static List<String> getFilenames(World world){
        ArrayList<String> ret = new ArrayList<>();

        for(Map.Entry<String, World> entries: loadedWorlds.entrySet()){
            if(world == entries.getValue()){
                ret.add(entries.getKey());
            }
        }
        return ret;
    }

    /**
     * Checks if file is a world file and deletes it if so
     * @param filename
     * @return false on error
     */
    public static Boolean deleteWorldFile(String filename){
        Boolean error = false;

        WorldFileDefault worldFile = new WorldFileDefault(filename);
        if(worldFile.canRead()){
            File file = new File(filename);
            if(!file.delete()) error = true;
            //WorldFileList.removeWorldFileEntry(filename);
        }
        return error;
    }

    /**
     * creates a new world and adds it to the lists
     * @param name world name
     * @return new world object
     */
    public static World createWorld(String name){
        World world = new World(name);

        // find unique file name
        String shortname = name.replaceAll("\\s", ""); // removePlace whitespaces
        String filename = shortname;
        Integer cnt = 1;
        while((new File(Environment.getWorldsDir() + filename).exists())){
            filename = shortname + cnt++;
        }

        putWorld(Environment.getWorldsDir() + filename, world);

        return world;
    }

    /**
     * Removes a world from open
     * @param file
     */
    public static void closeFile(String file){
        loadedWorlds.remove(file);
    }

}
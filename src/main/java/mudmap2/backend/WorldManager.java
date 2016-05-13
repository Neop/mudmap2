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
import mudmap2.Paths;
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
        if(loadedWorlds.containsKey(file)){
            return loadedWorlds.get(file);
        } else {
            WorldFile worldFile = new WorldFileDefault(file);
            World world = worldFile.readFile();
            return world;
        }
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
        WorldFileList.setWorldName(file, world.getName());
    }

    /**
     * get all filenames associated to a world
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
        String shortname = name.replaceAll("\\s", ""); // remove whitespaces
        String filename = shortname;
        Integer cnt = 1;
        while((new File(Paths.getWorldsDir() + filename).exists())){
            filename = shortname + cnt++;
        }

        putWorld(filename, world);

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
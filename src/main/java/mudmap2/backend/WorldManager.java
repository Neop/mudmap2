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

import java.util.HashSet;
import mudmap2.backend.WorldFileReader.WorldFile;
import mudmap2.backend.WorldFileReader.current.WorldFileDefault;

/**
 * The WorldManager is the central place to register and retrieve loaded worlds.
 * This way one world file won't be loaded twice by the same instance of MUD Map.
 * @author neop
 */
public class WorldManager {

    private static final HashSet<World> loadedWorlds = new HashSet<>();

    /**
     * Add world to loaded worlds list
     * @param world
     */
    public static void register(World world){
        if(!loadedWorlds.contains(world)){
            loadedWorlds.add(world);
        }
    }

    /**
     * Remove world from loaded worlds list
     * @param world
     */
    public static void close(World world){
        loadedWorlds.remove(world);
    }

    /**
     * Load world or get it from loaded worlds list if it is already loaded
     * @param filename world file name
     * @return world or null
     * @throws java.lang.Exception throws Exception if world could not be read
     */
    public static World getWorld(String filename) throws Exception{
        World ret = null;

        // find world in loaded worlds list
        for(World world: loadedWorlds){
            if(world.getWorldFile() != null &&
                    world.getWorldFile().getFilename().equals(filename)){
                ret = world;
                break;
            }
        }

        // if world not loaded already
        if(ret == null){
            /* TODO: find correct file format if a new format gets implemented
             * in future
            */
            WorldFile worldFile = new WorldFileDefault(filename);
            if(worldFile.canRead()){
                ret = worldFile.readFile();
                worldFile.backup();
                register(ret);
            } else {
                throw new Exception("Could not read world file: invalid format");
            }
        }

        return ret;
    }

    /**
     * Creates and registers a new world
     * @param name world name
     * @return world
     */
    public static World getNewWorld(String name){
        World world = new World(name);

        // workaround: create and move to default layer
        if(world.getLayers().isEmpty()){
            Layer layer = world.getNewLayer();
            world.setHome(new WorldCoordinate(layer.getId(), 0, 0));
        }

        register(world);
        return world;
    }

}
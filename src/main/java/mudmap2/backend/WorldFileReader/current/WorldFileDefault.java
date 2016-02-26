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
package mudmap2.backend.WorldFileReader.current;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mudmap2.Pair;
import mudmap2.backend.World;
import mudmap2.backend.WorldFileReader.WorldFile;

/**
 * This class uses the default file type for saving world files and selects the
 * right world file reader
 * @author neop
 */
public class WorldFileDefault extends WorldFile {

    WorldFile worldFile;

    public WorldFileDefault(String filename) {
        super(filename);
        // set default world file type here:
        worldFile = new WorldFileMM1(filename);
    }

    /**
     * read world
     * @return
     * @throws FileNotFoundException
     * @throws Exception
     */
    @Override
    public World readFile() throws FileNotFoundException, Exception {
        World world = null;

        WorldFile worldFileReader = getWorldFileReader();
        if(worldFileReader == null){
            throw new Exception("Could not read world from file '" + filename + "': invalid file type or version");
        }
        return worldFileReader.readFile();
    }

    /**
     * Checks whether the file is a world file
     * @return
     * @throws FileNotFoundException
     */
    public Boolean isWorldFile() throws FileNotFoundException{
        if(null != filename && !filename.isEmpty()){
            String line;
            BufferedReader reader = new BufferedReader(new FileReader(filename));

            try {
                while((line = reader.readLine()) != null){
                    line = line.trim();
                    if(line.startsWith("// MUD Map world file")
                            || line.startsWith("# MUD Map 2 world file")){
                        return true;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(WorldFileDefault.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    /**
     * Reads the file version number from world file.
     *
     * @return
     * @throws FileNotFoundException
     */
    public Pair<Integer,Integer> readFileVersion() throws FileNotFoundException {
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(filename));

        Pair<Integer, Integer> fileVersion = new Pair<>(0, 0);

        try {
            while((line = reader.readLine()) != null){
                line = line.trim();
                if(line.startsWith("ver") && line.contains(" ")){
                    String[] parts = line.substring(line.indexOf(' ') + 1).split("\\.", 3);
                    if(parts.length >= 1) fileVersion.first = Integer.parseInt(parts[0]);
                    if(parts.length >= 2) fileVersion.second = Integer.parseInt(parts[1]);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(WorldFileDefault.class.getName()).log(Level.SEVERE, null, ex);
        }

        return fileVersion;
    }

    /**
     * Get the right file reader for the file type
     * @return
     * @throws java.io.FileNotFoundException
     */
    public WorldFile getWorldFileReader() throws FileNotFoundException {
        WorldFile ret = null;

        if(isWorldFile()){
            switch(readFileVersion().first){
                case 1:
                    ret = new WorldFileMM1(filename);
                    break;
                case 2:
                    ret = new WorldFileYAML(filename);
                    break;
            }
        }
        return ret;
    }

    /**
     * Read world name from world file
     * @return
     * @throws java.io.FileNotFoundException
     */
    @Override
    public String readWorldName() throws Exception {

        String worldname = "";

        // find the right file type
        switch(readFileVersion().first){
            case 0: // not found
            case 1: // MUD Map v1/v2 file
                worldFile = new WorldFileMM1(filename);
                worldname = worldFile.readWorldName();
                break;
            case 2: // MUD Map YAML file
                worldFile = new WorldFileYAML(filename);
                worldname = worldFile.readWorldName();
                break;
            default: // invalid value
                throw new Exception("Could not read world name from file '" + filename + "': invalid file version");
        }

        return worldname;
    }

    /**
     * write world
     * @param world
     */
    @Override
    public void writeFile(World world) {
        worldFile.writeFile(world);
    }

    /**
     * Backup world file
     * @throws java.io.FileNotFoundException
     */
    @Override
    public void backup() throws FileNotFoundException {
        worldFile.backup();
    }

}

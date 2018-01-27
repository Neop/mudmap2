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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import mudmap2.backend.World;
import mudmap2.backend.WorldFileReader.WorldFile;
import mudmap2.backend.WorldFileReader.WorldFileType;
import static mudmap2.backend.WorldFileReader.WorldFileType.INVALID;
import static mudmap2.backend.WorldFileReader.WorldFileType.UNKNOWN;

/**
 * This class uses the default file type for saving world files and selects the
 * right world file reader
 * @author neop
 */
public class WorldFileDefault extends WorldFile {

    WorldFile worldFile;
    WorldFileType worldFileType;

    @SuppressWarnings("deprecation")
    public WorldFileDefault(String filename) {
        super(filename);

        if(filename != null){
            File file = new File(filename);
            if(file.exists()){
                WorldFileJSON wfj = new WorldFileJSON(filename);
                if(wfj.canRead()){
                    worldFileType = WorldFileType.JSON;
                } else {
                    worldFileType = WorldFileType.INVALID;
                }
            } else {
                worldFileType = WorldFileType.UNKNOWN;
            }
        } else {
            worldFileType = WorldFileType.INVALID;
        }

        switch(worldFileType){
            default:
            case INVALID:
            case UNKNOWN: // set default world file type here:
            case JSON:
                worldFile = new WorldFileJSON(filename);
                break;
        }
    }

    public void setWorldFile(WorldFile worldFile) {
        this.worldFile = worldFile;
    }

    public WorldFile getWorldFile() {
        return worldFile;
    }

    @Override
    public WorldFileType getWorldFileType() {
        return worldFileType;
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

        if(worldFile != null){
            world = worldFile.readFile();
            world.setWorldFile(this);
        }

        if(world == null){
            throw new Exception("Could not read world from file '" + filename);
        }

        return world;
    }

    /**
     * Read world name from world file
     * @return
     * @throws java.io.FileNotFoundException
     */
    @Override
    public String readWorldName() throws Exception {
        if(worldFile != null && worldFile.canRead()){
            return worldFile.readWorldName();
        }
        return "";
    }

    /**
     * write world
     * @param world
     * @throws java.io.IOException
     */
    @Override
    public void writeFile(World world) throws IOException {
        if(worldFile.getWorldFileType() == WorldFileType.MUDMAP1){
            worldFile = new WorldFileJSON(filename);
            world.setWorldFile(this);
        }

        worldFile.writeFile(world);
        worldFileType = worldFile.getWorldFileType();
    }

    /**
     * Backup world file
     * @throws java.io.FileNotFoundException
     */
    @Override
    public void backup() throws FileNotFoundException {
        worldFile.backup();
    }

    @Override
    public Boolean canRead() {
        return !worldFileType.equals(INVALID) && !worldFileType.equals(UNKNOWN);
    }

}

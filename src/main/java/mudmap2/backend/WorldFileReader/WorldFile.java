/*  MUD Map (v2) - A tool to create and organize maps for text-based games
 *  Copyright (C) 2015  Neop (email: mneop@web.de)
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
 *  An interface for world file readers / writers
 */
package mudmap2.backend.WorldFileReader;

import java.io.FileNotFoundException;
import mudmap2.backend.World;

/**
 * Abstract class to read and write world files
 * @author Neop
 */
public abstract class WorldFile {

    protected String filename;

    public WorldFile(String filename){
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    // Reads a world from file
    public abstract World readFile() throws Exception;
    // Writes the world to file
    public abstract void writeFile(World world);
    // saves a backup copy of the world
    public abstract void backup() throws FileNotFoundException;

    // reads the world name
    public abstract String readWorldName() throws Exception;
}

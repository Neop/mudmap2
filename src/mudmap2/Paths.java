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
 *  This class staticly provides file path information
 */

package mudmap2;

/**
 * Helper class to get common paths and filenames
 * @author neop
 */
public class Paths {
    
    /**
     * Gets the user data path
     * @return user data path
     */
    public static String get_user_data_dir(){
        // read the user data path from environment variables
        // operating system Windows
        if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0)
            user_data_dir = System.getenv().get("APPDATA") + "/mudmap/";
        // other operating systems
        else user_data_dir = System.getProperty("user.home") + "/.mudmap/";
        
        return user_data_dir;
    }
    
    /**
     * Gets the directory that contains the world files
     * @return worlds directory
     */
    public static String get_worlds_dir(){
        return get_user_data_dir() + "worlds/";
    }
    
    /**
     * Gets the available worlds file (path + filename)
     * @return available worlds file
     */
    public static String get_available_worlds_file(){
        return get_worlds_dir() + "worlds";
    }
    
    public static String get_config_file(){
        return get_user_data_dir() + "config";
    }
    
    static String user_data_dir;
}

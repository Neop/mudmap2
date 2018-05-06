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

import java.io.File;

/**
 * Helper class to get common paths and filenames
 * @author neop
 */
public class Environment {

    static String userDataDir;
    public final static String WEBSITE_URL = "http://mudmap.sf.net";
    public final static String MANUAL_URL = WEBSITE_URL;

    public static final String GITHUB_URL = "https://github.com/Neop/mudmap2";
    public static final String SOURCEFORGE_URL = "http://sf.net/p/mudmap";

    public static String getHome(){
        String ret;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            // operating system Windows
            ret = System.getenv().get("APPDATA");
        } else {
            // other operating systems
            ret = System.getProperty("user.home");
        }
        return ret;
    }

    /**
     * Gets the user data path
     * @return user data path
     */
    public static String getUserDataDir(){
        if(userDataDir == null || userDataDir.isEmpty()){
            // read the user data path from environment variables
            if(System.getProperty("os.name").toLowerCase().contains("win")){
                // operating system Windows
                userDataDir = System.getenv().get("APPDATA") + File.separator + "mudmap" + File.separator;
            } else {
                // other operating systems
                userDataDir = System.getProperty("user.home") + File.separator + ".mudmap" + File.separator;
            }
        }
        return userDataDir;
    }

    /**
     * Changes the user data dir for debugging purposes
     * @param userDataDir
     */
    public static void setUserDataDir(String userDataDir) {
        Environment.userDataDir = userDataDir;
    }

    /**
     * Gets the directory that contains the world files
     * @return worlds directory
     */
    @Deprecated
    public static String getWorldsDir(){
        return getUserDataDir() + "worlds" + File.separator;
    }

}

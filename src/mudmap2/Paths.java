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

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class to get common paths and filenames
 * @author neop
 */
public class Paths {
    
    static String user_data_dir;
    final static String website_url = "http://mudmap.sf.net";
    final static String manual_url = website_url;
    
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
    
    /**
     * Gets the config file path
     * @return 
     */
    public static String get_config_file(){
        return get_user_data_dir() + "config";
    }
    
    /**
     * Gets the website url
     * @return manual url
     */
    public static String get_website_url(){
        return website_url;
    }
    
    /**
     * Gets the online manual url
     * @return manual url
     */
    public static String get_manual_url(){
        return manual_url;
    }
    
    /**
     * Tries to open the url in a web-browser
     * @param url 
     */
    public static void open_website(String url){
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException ex) {
            Logger.getLogger(Paths.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Checks whether a file exists
     * @param file file to check
     * @return true, if file exists or is a directory
     */
    public static boolean file_exists(String file){
        File f = new File(file);
        return f.exists() || f.isDirectory();
    }
    
    /**
     * checks if path is a file
     * @param file
     * @return true, if path is a file
     */
    public static boolean is_file(String path){
        File f = new File(path);
        return f.exists() && !f.isDirectory();
    }
    
    /**
     * checks if path is a directory
     * @param file
     * @return true, if path is a directory
     */
    public static boolean is_directory(String path){
        File f = new File(path);
        return f.exists() && f.isDirectory();
    }
    
    /**
     * Creates a directory
     * @param path 
     */
    public static void create_directory(String path){
        File f = new File(path);
        if(!f.exists()) f.mkdir();
    }
}

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
 *  Main class, it creates the Mainwindow and provides version information
 */

package mudmap2;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import mudmap2.frontend.Mainwindow;

/**
 *
 * @author neop
 */
public class Mudmap2 {
    static int version_major = 2;
    static int version_minor = 0;
    static int version_build = 0;
    static String version_state = "alpha";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // TODO code application logic here
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Mudmap2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(Mudmap2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Mudmap2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Mudmap2.class.getName()).log(Level.SEVERE, null, ex);
        }
                
        Mainwindow win = new Mainwindow();
    }
    
    /**
     * Gets the version major number
     * @return major version
     */
    public static int get_version_major(){
        return version_major;
    }
    
    /**
     * Gets the version minor number
     * @return minor version
     */
    public static int get_version_minor(){
        return version_minor;
    }
    
    /**
     * Gets the version build number
     * @return build version
     */
    public static int get_version_build(){
        return version_build;
    }
    
    /**
     * Gets the version state (eg. "alpha" or "beta")
     * @return version state
     */
    public static String get_version_state(){
        return version_state;
    }
}

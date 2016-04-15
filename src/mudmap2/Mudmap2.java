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
public final class Mudmap2 {

    static Mainwindow mwin;

    static final int version_major = 2;
    static final int version_minor = 2;
    static final int version_build = 8;
    static final String version_state = "";

    static boolean portable_mode = false;

    private Mudmap2(){};

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // TODO code application logic here
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            mwin = new Mainwindow();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Mudmap2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(Mudmap2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Mudmap2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Mudmap2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // ========================== Version info =================================

    /**
     * Gets the version major number
     * @return major version
     */
    public static int getVersionMajor(){
        return version_major;
    }

    /**
     * Gets the version minor number
     * @return minor version
     */
    public static int getVersionMinor(){
        return version_minor;
    }

    /**
     * Gets the version build number
     * @return build version
     */
    public static int getVersionBuild(){
        return version_build;
    }

    /**
     * Gets the version state (eg. "alpha" or "beta")
     * @return version state
     */
    public static String getVersionState(){
        return version_state;
    }

    /**
     * Gets the version number as a string
     * @return major, minor and build version, separated b '.'
     */
    public static String getVersion(){
        return version_major + "." + version_minor + "." + version_build;
    }
}

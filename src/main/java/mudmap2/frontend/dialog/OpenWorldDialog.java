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
 *  The OpenWorldDialog lets the user choose a world file to be opened in a new
 *  WorldTab
 */
package mudmap2.frontend.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import mudmap2.backend.World;
import mudmap2.backend.WorldFileReader.current.WorldFileDefault;
import mudmap2.backend.WorldManager;
import mudmap2.frontend.Mainwindow;

/**
 *
 * @author neop
 */
public class OpenWorldDialog implements ActionListener{

    Mainwindow parent;

    JFileChooser filechooser;

    public OpenWorldDialog(Mainwindow parent){
        this.parent = parent;
    }

    private void create(){
        filechooser = new JFileChooser();

        filechooser.setAcceptAllFileFilterUsed(false);
        filechooser.setDialogTitle("Open world");
        filechooser.setMultiSelectionEnabled(false);
        filechooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if(file == null) return false;
                if(file.isDirectory()) return true;
                String worldname = null;

                try {
                    worldname = (new WorldFileDefault(file.getPath()).readWorldName());
                } catch (Exception ex) {
                    Logger.getLogger(OpenWorldDialog.class.getName()).log(Level.SEVERE, null, ex);
                }

                return worldname != null && !worldname.equals("");
            }

            @Override
            public String getDescription() {
                return "MUD Map world files";
            }
        });

        int ret = filechooser.showOpenDialog(parent);

        if(ret == JFileChooser.APPROVE_OPTION){
            String file = filechooser.getSelectedFile().toString();
            try {
                World world = WorldManager.getWorld(file);
                if(null != world){
                    // create world tab
                    parent.createTab(world, file);
                }
            } catch (Exception ex) {
                Logger.getLogger(OpenWorldDialog.class.getName()).log(Level.WARNING, null, ex);
                JOptionPane.showMessageDialog(parent, "Could not open world file '" + file + "'", "Open world", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Opens the dialog
     */
    public void setVisible(){
        create();
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        create();
    }
}

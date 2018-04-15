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
package mudmap2.frontend.dialog;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.filechooser.FileFilter;
import mudmap2.Environment;
import mudmap2.backend.WorldFileReader.WorldFile;
import mudmap2.backend.WorldFileReader.WorldFileFilterJSON;
import mudmap2.backend.WorldFileReader.WorldFileFilterM2W;
import mudmap2.backend.WorldFileReader.current.WorldFileJSON;
import mudmap2.frontend.WorldTab;

/**
 *
 * @author neop
 */
public class SaveWorldDialog extends JFileChooser {
    private static final long serialVersionUID = 1L;

    WorldTab wt;

    ButtonGroup fileTypeGroup;
    JRadioButton radioMM1;
    JRadioButton radioJSON;

    public SaveWorldDialog(JFrame parent, WorldTab wt){
        super(wt.getWorld().getWorldFile() != null ? wt.getWorld().getWorldFile().getFilename() : Environment.getHome());

        setFileHidingEnabled(false);

        FileFilter filter;
        addChoosableFileFilter(filter = new WorldFileFilterM2W());
        addChoosableFileFilter(new WorldFileFilterJSON());

        setFileFilter(filter);

        this.wt = wt;
    }

    public WorldFile getWorldFile(){
        String file = getSelectedFile().getAbsolutePath();

        if(getFileFilter() instanceof WorldFileFilterM2W){
            if(!file.endsWith(".m2w")){
                file = file + ".m2w";
            }
        }

        WorldFile worldFile = new WorldFileJSON(file);

        return worldFile;
    }

}

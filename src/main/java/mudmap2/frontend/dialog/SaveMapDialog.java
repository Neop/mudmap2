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

import java.io.File;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import mudmap2.backend.WorldFileReader.WorldFile;
import mudmap2.backend.WorldFileReader.WorldFileFilterJSON;
import mudmap2.backend.WorldFileReader.WorldFileFilterM2M;
import mudmap2.backend.WorldFileReader.current.WorldFileJSON;
import mudmap2.frontend.WorldTab;

/**
 *
 * @author neop
 */
public class SaveMapDialog extends JFileChooser {
    private static final long serialVersionUID = 1L;

    WorldTab wt;

    ButtonGroup fileTypeGroup;

    public SaveMapDialog(JFrame parent, WorldTab wt){
        super();
        if(wt.getWorld().getWorldFile() != null &&
                !wt.getWorld().getWorldFile().getFilename().isEmpty()){
            setCurrentDirectory(new File(wt.getWorld().getWorldFile().getFilename()));
        }

        setFileHidingEnabled(false);

        FileFilter filter;
        addChoosableFileFilter(filter = new WorldFileFilterM2M());
        addChoosableFileFilter(new WorldFileFilterJSON());

        setFileFilter(filter);

        this.wt = wt;
    }

    public WorldFile getWorldFile(){
        String file = getSelectedFile().getAbsolutePath();

        if(getFileFilter() instanceof WorldFileFilterM2M){
            if(!file.endsWith(".m2m")){
                file = file + ".m2m";
            }
        }

        WorldFile worldFile = new WorldFileJSON(file);

        return worldFile;
    }

}

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

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import mudmap2.backend.WorldFileReader.WorldFile;
import mudmap2.backend.WorldFileReader.WorldFileType;
import mudmap2.backend.WorldFileReader.current.WorldFileJSON;
import mudmap2.backend.WorldFileReader.current.WorldFileMM1;
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
        super(wt.getWorld().getWorldFile().getFilename());

        this.wt = wt;

        JPanel fileType = new JPanel();
        fileType.setLayout(new BoxLayout(fileType, BoxLayout.PAGE_AXIS));

        radioMM1 = new JRadioButton("v1 (deprecated)");
        radioJSON = new JRadioButton("v2 (recommended)");
        radioMM1.setToolTipText("Use this for compatibility to MUD Map versions prior to v2.3. Does not support all features of v2.3+!");
        radioJSON.setToolTipText("Use this for MUD Map v2.3+");

        fileTypeGroup = new ButtonGroup();
        fileTypeGroup.add(radioMM1);
        fileTypeGroup.add(radioJSON);
        radioJSON.setSelected(true);

        fileType.add(new JLabel("File version:"));
        fileType.add(radioMM1);
        fileType.add(radioJSON);

        this.setAccessory(fileType);
    }

    public WorldFileType getFileVersion(){
        if(radioMM1.isSelected()) return WorldFileType.MUDMAP1;
        return WorldFileType.JSON;
    }

    public WorldFile getWorldFile(){
        String file = getSelectedFile().getAbsolutePath();
        WorldFile worldFile;

        switch(getFileVersion()){
            case MUDMAP1:
                worldFile = new WorldFileMM1(file);
                break;
            default:
            case JSON:
                worldFile = new WorldFileJSON(file);
                break;
        }

        return worldFile;
    }

}

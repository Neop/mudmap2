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
 *  The Help->About dialog, it shows information about the program
 */

package mudmap2.frontend.dialog;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import mudmap2.Paths;
import mudmap2.frontend.GUIElement.LinkLabel;

/**
 * The Help->About dialog, it shows information about the program
 * @author neop
 */
public class AboutDialog extends ActionDialog {

    /**
     * Creates an about dialog
     * @param parent 
     */
    public AboutDialog(JFrame parent){
        super(parent, "bout MUD Map", true);
    }

    @Override
    void create() {
        setLayout(new GridLayout(0, 1));
        
        add(new JLabel("MUD Map v2"));
        add(new JLabel("Version " + mudmap2.Mudmap2.get_version() + " " + mudmap2.Mudmap2.get_version_state()));
        add(new JLabel("License: GLPv3"));
        add(new JLabel("Use it on your own risk!"));
        add(new LinkLabel("GitHub", Paths.github_url));
        add(new LinkLabel("Sourceforge", Paths.sourceforge_url));
        add(new JLabel("by Neop (mneop@web.de)"));
        
        JButton button_ok = new JButton("Ok");
        add(button_ok);
        getRootPane().setDefaultButton(button_ok);
        button_ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        pack();
        setLocation(getParent().getX() + (getParent().getWidth() - getWidth()) / 2, getParent().getY() + (getParent().getHeight() - getHeight()) / 2);
    }
}

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
 *  Abstract dialog class, the dialog will be created and shown on an
 *  ActionEvent or if setVisible(true) gets called
 */
package mudmap2.frontend.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

/**
 * Abstract dialog class, the dialog will be created and shown on an
 * ActionEvent or if setVisible(true) gets called
 *
 * put the dialog creation / initialisation code in create(){} if possible
 *
 * @author neop
 */
public abstract class ActionDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = 1L;

    boolean created = false;

    public ActionDialog(JFrame parent, String title, boolean modal){
        super(parent, title, modal);

        // listener for escape key
        getRootPane().registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    protected abstract void create();

    @Override
    public void setVisible(boolean b){
        if(!created){
            create();
            created = true;
        }
        super.setVisible(b);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        setVisible(true);
    }

}

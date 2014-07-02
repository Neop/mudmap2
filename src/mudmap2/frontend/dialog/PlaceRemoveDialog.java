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
 *  The place remove dialog removes a place from the map after asking the user
 */
package mudmap2.frontend.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import mudmap2.backend.Layer.PlaceNotFoundException;
import mudmap2.backend.Place;
import mudmap2.backend.World;

/**
 * The place remove dialog removes a place from the map after asking the user
 * @author neop
 */
public class PlaceRemoveDialog implements ActionListener {
    
    JFrame parent;
    World world;
    Place place;
    
    public PlaceRemoveDialog(JFrame _parent, World _world, Place _place){        
        parent = _parent;
        world = _world;
        place = _place;
    }
    
    public void show(){
        int ret = JOptionPane.showConfirmDialog(parent, "Do yo want to remove \"" + place.get_name() + "\" (ID: " + place.get_id() + ") from the map? This can not be undone!", "Remove place", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if(ret == 0){
            try {
                world.remove(place);
                parent.repaint();
            } catch (RuntimeException ex) {
                Logger.getLogger(PlaceRemoveDialog.class.getName()).log(Level.SEVERE, null, ex);
            } catch (PlaceNotFoundException ex) {
                Logger.getLogger(PlaceRemoveDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        show();
    }
    
}

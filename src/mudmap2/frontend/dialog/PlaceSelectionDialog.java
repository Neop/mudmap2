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
 *  The PlaceSelectionDialog can be used to select a place or a position on a map
 */
package mudmap2.frontend.dialog;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import mudmap2.backend.Place;
import mudmap2.backend.World;
import mudmap2.backend.WorldCoordinate;
import mudmap2.frontend.WorldTab;

/**
 * The PlaceSelectionDialog can be used to select a place or a position on a map
 * @author neop
 */
public class PlaceSelectionDialog extends ActionDialog {
    
    World world;
    boolean layer_change_allowed;
    
    // true, if ok the button was clicked
    boolean ok; 
    
    WorldCoordinate default_coordinate;
    
    JFrame parent;
    JOptionPane optionPane;
    
    WorldTab worldtab;
    
    public PlaceSelectionDialog(JFrame _parent, World _world, WorldCoordinate _default_coordinate, boolean _layer_change_allowed) {
        super(_parent, "Select a place - " + _world.getName(), true);
        parent = _parent;
        world = _world;
        
        default_coordinate = _default_coordinate;
        layer_change_allowed = _layer_change_allowed;
    }
    
    /**
     * Gets the selected place
     * @return 
     */
    public Place getSelection(){
        return worldtab.getPlace(worldtab.getCursorX(), worldtab.getCursorY());
    }
    
    /**
     * Gets the world coordinate of the selected place
     * @return 
     */
    public WorldCoordinate getCoordinate(){
        return new WorldCoordinate(worldtab.getCurPosition().getLayer(), worldtab.getCursorX(), worldtab.getCursorY());
    }
    
    /**
     * Is true, if the ok button was clicked
     * @return 
     */
    public boolean getSelected(){
        return ok;
    }

    @Override
    void create() {
        optionPane = new JOptionPane();
        optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
        
        setContentPane(optionPane);
        optionPane.setMessage(worldtab = new WorldTab(parent, world, true));
        worldtab.setCursorForced(true);
        worldtab.resetHistory(default_coordinate.clone());
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        optionPane.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent arg0) {
                if(isVisible() && arg0.getSource() == optionPane && arg0.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)){
                    int value = ((Integer) optionPane.getValue()).intValue();
                    ok = value == JOptionPane.OK_OPTION;
                    dispose();
                    parent.repaint();
                }
            }
        });
        
        setSize(500, 500);
        setLocation(parent.getX() + (parent.getWidth() - getWidth()) / 2, parent.getY() + (parent.getHeight() - getHeight()) / 2);
    }
}

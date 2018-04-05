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
import mudmap2.backend.Layer;
import mudmap2.backend.Place;
import mudmap2.backend.World;
import mudmap2.backend.WorldCoordinate;
import mudmap2.frontend.WorldTab;

/**
 * The PlaceSelectionDialog can be used to select a place or a position on a map
 * @author neop
 */
public class PlaceSelectionDialog extends ActionDialog {

    private static final long serialVersionUID = 1L;

    World world;
    boolean layer_change_allowed;

    // true, if ok the button was clicked
    boolean ok;

    WorldCoordinate default_coordinate;

    JFrame parentFrame;
    JOptionPane optionPane;

    WorldTab worldtab;

    public PlaceSelectionDialog(JFrame parent, World world, WorldCoordinate defaultCoordinate, boolean layerChangeAllowed) {
        super(parent, "Select a place - " + world.getName(), true);
        this.parentFrame = parent;
        this.world = world;
        ok = false;

        default_coordinate = defaultCoordinate;
        layer_change_allowed = layerChangeAllowed;
    }

    /**
     * Gets the selected place
     * @return
     */
    public Place getSelection(){
        Layer layer = worldtab.getWorldPanel().getWorld().getLayer(
                worldtab.getWorldPanel().getPosition().getLayer());
        return layer.get(worldtab.getWorldPanel().getCursorX(),
                worldtab.getWorldPanel().getCursorY());
    }

    /**
     * Gets the world coordinate of the selected place
     * @return
     */
    public WorldCoordinate getCoordinate(){
        return new WorldCoordinate(
                worldtab.getWorldPanel().getPosition().getLayer(),
                worldtab.getWorldPanel().getCursorX(),
                worldtab.getWorldPanel().getCursorY());
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
        optionPane.setMessage(worldtab = new WorldTab(parentFrame, world, true));
        worldtab.getWorldPanel().setCursorForced(true);
        worldtab.getWorldPanel().resetHistory(new WorldCoordinate(default_coordinate));

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        optionPane.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent arg0) {
                if(isVisible() && arg0.getSource() == optionPane && arg0.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)){
                    int value = ((Integer) optionPane.getValue());
                    ok = value == JOptionPane.OK_OPTION;
                    dispose();
                    if(parentFrame != null) parentFrame.repaint();
                }
            }
        });

        setSize(500, 500);
    }
}

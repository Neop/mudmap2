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
 *  The PathConnectDialog select a second place and lets the user enter the two
 *  exit directions
 */
package mudmap2.frontend.dialog;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import mudmap2.backend.Layer;
import mudmap2.backend.Path;
import mudmap2.backend.Place;
import mudmap2.frontend.WorldTab;

/**
 * The PathConnectDialog select a second place and lets the user enter the two
 * exit directions
 * @author neop
 */
public class PathConnectDialog extends ActionDialog{

    Place place, other;
    
    WorldTab worldtab, wt_parent;
    JLabel label_other_place;
    JComboBox<String> direction_combo_box1, direction_combo_box2;
    
    public PathConnectDialog(WorldTab _parent, Place _place) {
        super(_parent.get_parent(), "Connect path to " + _place, true);
        wt_parent = _parent;
        place = _place;
        other = null;
    }
    
    @Override
    void create(){
        setMinimumSize(new Dimension(600, 600));
        setLayout(new GridBagLayout());
        
        // world tab
        worldtab = (WorldTab) wt_parent.clone();
        worldtab.set_place_selection_forced(true);
        worldtab.reset_history(place.get_coordinate());
        worldtab.set_forced_focus_disabled(true);
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridwidth = 4;
        add(worldtab, constraints);
        
        // Place config
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.insets = new Insets(4, 4, 4, 4);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        add(new JLabel(place.toString()), constraints);
        
        LinkedList<String> directions1 = new LinkedList<String>();
        for(String s: Path.directions)
            if(place.get_exit(s) == null) directions1.add(s);
        
        constraints.gridx = 1;
        constraints.weightx = 0.0;
        direction_combo_box1 = new JComboBox(directions1.toArray());
        direction_combo_box1.setEditable(true);
        add(direction_combo_box1, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.weightx = 1.0;
        add(label_other_place = new JLabel(), constraints);
        
        constraints.gridx = 1;
        constraints.weightx = 0.0;
        direction_combo_box2 = new JComboBox();
        update_direction_combo_box2();
        direction_combo_box2.setEditable(true);
        add(direction_combo_box2, constraints);
        
        // Buttons
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.weightx = 1.0;
        JButton button_cancel = new JButton("Cancel");
        add(button_cancel, constraints);
        button_cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        constraints.gridx = 1;
        constraints.gridy = 3;
        JButton button_ok = new JButton("Ok");
        add(button_ok, constraints);
        getRootPane().setDefaultButton(button_ok);
        button_ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
                dispose();
            }
        });
        
        worldtab.add_place_selection_listener(new WorldTab.PlaceSelectionListener() {
            @Override
            public void placeSelected(Place p) {
                if(p != place){ 
                    other = p;
                    label_other_place.setText(other.toString());
                    update_direction_combo_box2();
                }
            }

            @Override
            public void placeDeselected(Layer layer, int x, int y) {}
        });
        
        pack();
        setLocation(getParent().getX() + (getParent().getWidth() - getWidth()) / 2, getParent().getY() + (getParent().getHeight() - getHeight()) / 2);
    }
    
    /**
     * Fills the combo box only with directions thar aren't occupied yet
     */
    private void update_direction_combo_box2() {
        direction_combo_box2.removeAllItems();
        if(other != null)
            for(String s: Path.directions)
                if(other.get_exit(s) == null) direction_combo_box2.addItem(s);
    }    
    
    /**
     * Saves the new connection
     */
    private void save(){
        if(other != null){ // if a place is selected
            String dir1 = (String) direction_combo_box1.getSelectedItem();
            String dir2 = (String) direction_combo_box2.getSelectedItem();
            
            boolean exit_available_1 = place.get_exit(dir1) == null;
            boolean exit_available_2 = other.get_exit(dir2) == null;
            
            // if both exits are available
            if(exit_available_1 && exit_available_2)
                place.connect_path(new Path(place, dir1, other, dir2));
            // else show message
            else JOptionPane.showMessageDialog(this, "Couldn't connect path, an exit of a place is occupied");
            
            wt_parent.repaint();
        }
    }
}

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
 *  The area dialog is used to create, modify and remove areas
 */
package mudmap2.frontend.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import mudmap2.backend.Area;
import mudmap2.backend.Place;
import mudmap2.backend.World;
import mudmap2.frontend.GUIElement.ColorChooserButton;

/**
 * The area dialog is used to create, modify and remove areas
 * @author neop
 */
public class AreaDialog extends ActionDialog {

    boolean new_area;
    
    JTextField textfield_name;
    ColorChooserButton colorchooserbutton;
    
    World world;
    Area area;
    Place place;
    HashSet<Place> place_group;
    
    /**
     * Constructs a modify / delete dialog for existing areas
     * @param _parent parent window
     * @param _world world
     * @param _area area to be modified
     */
    public AreaDialog(JFrame _parent, World _world, Area _area){
        super(_parent, "Edit area - " + _area, true);
        
        new_area = false;
        world = _world;
        area = _area;
        place = null;
    }
    
    /**
     * Constructs a dialog to create a new area
     * @param _parent parent window
     * @param _world world
     */
    public AreaDialog(JFrame _parent, World _world){
        super(_parent, "New area", true);
        
        new_area = true;
        world = _world;
        area = null;
        place = null;
    }
    
    /**
     * Edits the area of a place or creates a new one and assigns it
     * @param _parent
     * @param _world
     * @param _place 
     */
    public AreaDialog(JFrame _parent, World _world, Place _place){
        super(_parent, (_place.get_area() == null) ? "New area" : ("Edit area - " + _place.get_area()), true);
        
        place = _place;
        new_area = place.get_area() == null;
        world = _world;
        area = place.get_area();
    }
    
    /**
     * Edits the area of a group of places or creates a new one and assigns it
     * the default area will be taken from _place, if available
     * @param _parent
     * @param _world
     * @param _place_group
     * @param _place 
     */
    public AreaDialog(JFrame _parent, World _world, HashSet<Place> _place_group, Place _place){
        super(_parent, (_place.get_area() == null) ? "New area" : ("Edit area - " + _place.get_area()), true);
        
        place = _place;
        place_group = _place_group;
        new_area = place.get_area() == null;
        world = _world;
        area = place.get_area();
    }
    
    /**
     * Creates the GUI
     */
    @Override
    void create(){
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        
        add(new JLabel("Name"), constraints);
        constraints.gridx = 1;
        constraints.gridwidth = 2;
        if(area != null) textfield_name = new JTextField(area.toString());
        else textfield_name = new JTextField();
        add(textfield_name, constraints);
        textfield_name.setColumns(20);
        
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy++;
        
        add(new JLabel("Color"), constraints);
        
        constraints.weighty = 4.0;
        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridwidth = 2;
        
        if(area != null) colorchooserbutton = new ColorChooserButton(getParent(), area.get_color()); 
        else colorchooserbutton = new ColorChooserButton(getParent());
        add(colorchooserbutton, constraints);
        
        constraints.weighty = 1.0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy++;
        
        JButton button_cancel = new JButton("Cancel");
        add(button_cancel, constraints);
        button_cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                dispose();
            }
        });
        
        constraints.gridx++;
        JButton button_new = new JButton("New");
        button_new.setToolTipText("Creates a new area");
        add(button_new, constraints);
        getRootPane().setDefaultButton(button_new);
        button_new.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                new_area = true;
                save();
                dispose();
            }
        });
        
        if(!new_area){ // don't show edit button when creating a new place
            constraints.gridx++;
            JButton button_edit = new JButton("Edit");
            button_edit.setToolTipText("Edits the current area");
            add(button_edit, constraints);
            getRootPane().setDefaultButton(button_edit);
            button_edit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    save();
                    dispose();
                }
            });
        }
       
        pack();
        setResizable(false);
        setLocation(getParent().getX() + (getParent().getWidth() - getWidth()) / 2, getParent().getY() + (getParent().getHeight() - getHeight()) / 2);
    }
    
    private void save(){
        // add new area to world and place
        if(new_area){
            area = new Area(textfield_name.getText(), colorchooserbutton.get_color());
            world.add_area(area);
            if(place != null && place_group == null) place.set_area(area);
        } else {
            // modify area
            area.set_name(textfield_name.getText());
            area.set_color(colorchooserbutton.get_color());
        }
        // assign to all places
        if(place_group != null)
            for(Place pl: place_group)
                pl.set_area(area);
        getParent().repaint();
    }
    
}

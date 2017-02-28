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
 *  The PlaceGroup dialog is used to create, modify and remove place groups
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
import mudmap2.backend.PlaceGroup;
import mudmap2.backend.Place;
import mudmap2.backend.World;
import mudmap2.frontend.GUIElement.ColorChooserButton;

/**
 * The place group dialog is used to create, modify and remove place groups
 * @author neop
 */
public class PlaceGroupDialog extends ActionDialog {

    private static final long serialVersionUID = 1L;

    boolean new_group;

    JTextField textfield_name;
    ColorChooserButton colorchooserbutton;

    World world;
    PlaceGroup placeGroup;
    Place place;
    HashSet<Place> place_group;

    /**
     * Constructs a modify / delete dialog for existing palce groups
     * @param parent parent window
     * @param world world
     * @param placeGroup PlaceGroup to be modified
     */
    public PlaceGroupDialog(JFrame parent, World world, PlaceGroup placeGroup){
        super(parent, "Edit place group - " + placeGroup, true);

        new_group = false;
        this.world = world;
        this.placeGroup = placeGroup;
        place = null;
    }

    /**
     * Constructs a dialog to create a new place group
     * @param parent parent window
     * @param world world
     */
    public PlaceGroupDialog(JFrame parent, World world){
        super(parent, "New place group", true);

        new_group = true;
        this.world = world;
        placeGroup = null;
        place = null;
    }

    /**
     * Edits the PlaceGroup of a place or creates a new one and assigns it
     * @param parent
     * @param world
     * @param place
     */
    public PlaceGroupDialog(JFrame parent, World world, Place place){
        super(parent, (place.getPlaceGroup() == null) ? "New place group" : ("Edit place group - " + place.getPlaceGroup()), true);

        this.place = place;
        new_group = place.getPlaceGroup() == null;
        this.world = world;
        placeGroup = place.getPlaceGroup();
    }

    /**
     * Edits the PlaceGroup of a group of places or creates a new one and assigns it
     * the default PlaceGroup will be taken from _place, if available
     * @param parent
     * @param world
     * @param placeGroup
     * @param place
     */
    public PlaceGroupDialog(JFrame parent, World world, HashSet<Place> placeGroup, Place place){
        super(parent, (place.getPlaceGroup() == null) ? "New place group" : ("Edit place group - " + place.getPlaceGroup()), true);

        this.place = place;
        this.place_group = placeGroup;
        new_group = place.getPlaceGroup() == null;
        this.world = world;
        this.placeGroup = place.getPlaceGroup();
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
        if(placeGroup != null) textfield_name = new JTextField(placeGroup.toString());
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

        if(placeGroup != null) colorchooserbutton = new ColorChooserButton(getParent(), placeGroup.getColor());
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
        JButton button_new = new JButton("Add");
        button_new.setToolTipText("Create a new place group");
        add(button_new, constraints);
        getRootPane().setDefaultButton(button_new);
        button_new.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                new_group = true;
                save();
                dispose();
            }
        });

        if(!new_group){ // don't show edit button when creating a new place
            constraints.gridx++;
            JButton button_edit = new JButton("Edit");
            button_edit.setToolTipText("Edit the current place group");
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
        // add new PlaceGroup to world and place
        if(new_group){
            placeGroup = new PlaceGroup(textfield_name.getText(), colorchooserbutton.getColor());
            world.addPlaceGroup(placeGroup);
            if(place != null && place_group == null) place.setPlaceGroup(placeGroup);
        } else {
            // modify PlaceGroup
            placeGroup.setName(textfield_name.getText());
            placeGroup.setColor(colorchooserbutton.getColor());
        }
        // assign to all places
        if(place_group != null)
            for(Place pl: place_group)
                pl.setPlaceGroup(placeGroup);
        getParent().repaint();
    }

}

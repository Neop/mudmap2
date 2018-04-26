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
package mudmap2.frontend.dialog.placeGroup;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import mudmap2.backend.PlaceGroup;
import mudmap2.backend.Place;
import mudmap2.backend.World;
import mudmap2.frontend.GUIElement.ColorChooserButton;
import mudmap2.frontend.dialog.ActionDialog;

/**
 * The place group dialog is used to create, modify and remove place groups
 * @author neop
 */
public class PlaceGroupDialog extends ActionDialog {

    private static final long serialVersionUID = 1L;

    JTextField textfieldName;
    ColorChooserButton colorChooserButton;

    World world;
    Place place;
    Collection<PlaceGroup> placeGroups;

    /**
     * Constructs a dialog to create a new place group
     * @param parent parent window
     * @param world world
     */
    public PlaceGroupDialog(JFrame parent, World world){
        super(parent, "New place group", true);

        this.world = world;
        place = null;
        placeGroups = null;
    }

    /**
     * Constructs a modify / delete dialog for existing place groups
     * @param parent parent window
     * @param placeGroups place group(s) to be modified
     */
    public PlaceGroupDialog(JFrame parent, Collection<PlaceGroup> placeGroups) {
        super(parent, (placeGroups.size() == 1) ?
                "Edit place group - " + placeGroups.toArray(new PlaceGroup[1])[0].getName() :
                "Edit " + placeGroups.size() + " place groups", true);

        this.placeGroups = placeGroups;
        world = null;
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
        this.world = world;
        placeGroups = null;
    }

    /**
     * Creates the GUI
     */
    @Override
    protected void create(){
        setLayout(new BorderLayout());

        final JPanel contentPanel = new JPanel();
        add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new GridBagLayout());

        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        contentPanel.add(new JLabel("Name"), constraints);
        constraints.gridx = 1;
        constraints.gridwidth = 2;

        textfieldName = new JTextField();
        if(place != null && place.getPlaceGroup() != null){
            textfieldName.setText(place.getPlaceGroup().getName());
        } else if(placeGroups != null && !placeGroups.isEmpty()){
            String text = "";
            for(PlaceGroup group: placeGroups){
                if(text.isEmpty()){
                    text = group.getName();
                } else {
                    text = text + ", " + group.getName();
                }
            }
            textfieldName.setText(text);
            textfieldName.setEditable(false);
            textfieldName.setEnabled(false);
        }
        contentPanel.add(textfieldName, constraints);
        textfieldName.setColumns(20);

        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy++;

        contentPanel.add(new JLabel("Color"), constraints);

        constraints.weighty = 4.0;
        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridwidth = 2;

        colorChooserButton = new ColorChooserButton(getParent());
        if(place != null && place.getPlaceGroup() != null){
            colorChooserButton.setColor(place.getPlaceGroup().getColor());
        } else if(placeGroups != null && !placeGroups.isEmpty()){
            PlaceGroup[] array = placeGroups.toArray(new PlaceGroup[placeGroups.size()]);
            colorChooserButton.setColor(array[0].getColor());
        }
        contentPanel.add(colorChooserButton, constraints);

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));
        add(buttonPanel, BorderLayout.SOUTH);

        final JButton buttonCancel = new JButton("Cancel");
        buttonPanel.add(buttonCancel);
        getRootPane().setDefaultButton(buttonCancel);
        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                dispose();
            }
        });

        JButton buttonAccept;
        if(placeGroups == null && world != null && (place == null || place.getPlaceGroup() == null)){
            buttonAccept = new JButton("Add");
            buttonAccept.setToolTipText("Create a new place group");

            buttonAccept.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    createNew();
                    dispose();
                    getParent().repaint();
                }
            });
        } else {
            buttonAccept = new JButton("Update");
            buttonAccept.setToolTipText("Change place group(s)");

            buttonAccept.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    modifyExisting();
                    dispose();
                    getParent().repaint();
                }
            });
        }
        buttonPanel.add(buttonAccept);

        pack();
        setResizable(false);
        setLocation(getParent().getX() + (getParent().getWidth() - getWidth()) / 2, getParent().getY() + (getParent().getHeight() - getHeight()) / 2);
    }

    void createNew(){
        PlaceGroup placeGroup = new PlaceGroup(textfieldName.getText(), colorChooserButton.getColor());
        if(world != null) world.addPlaceGroup(placeGroup);
        if(place != null) place.setPlaceGroup(placeGroup);
    }

    void modifyExisting(){
        if(place != null){ // existing of place
            PlaceGroup group = place.getPlaceGroup();
            group.setName(textfieldName.getText());
            group.setColor(colorChooserButton.getColor());
        } else if(placeGroups != null){ // existing of group
            if(placeGroups.size() == 1){ // existing single entry of group
                PlaceGroup group = placeGroups.toArray(new PlaceGroup[1])[0];
                group.setName(textfieldName.getText());
                group.setColor(colorChooserButton.getColor());
            } else { // existing multiple entries of group
                for(PlaceGroup group: placeGroups){
                    group.setColor(colorChooserButton.getColor());
                }
            }
        }
    }

}

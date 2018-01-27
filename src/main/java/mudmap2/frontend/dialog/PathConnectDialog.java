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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import mudmap2.backend.Layer;
import mudmap2.backend.Path;
import mudmap2.backend.Place;
import mudmap2.backend.WorldCoordinate;
import mudmap2.frontend.GUIElement.WorldPanel.MapCursorListener;
import mudmap2.frontend.GUIElement.WorldPanel.MapPainterDefault;
import mudmap2.frontend.WorldTab;

/**
 * The PathConnectDialog select a second place and lets the user enter the two
 * exit directions
 * @author neop
 */
public class PathConnectDialog extends ActionDialog{

    private static final long serialVersionUID = 1L;

    JFrame parentFrame;

    Place place, other;

    WorldTab worldtab;
    JLabel labelOtherPlace;
    JComboBox<String> directionComboBox1, directionComboBox2;

    public PathConnectDialog(JFrame parent, Place place) {
        super(parent, "Connect path to " + place, true);
        parentFrame = parent;
        this.place = place;
        this.other = null;
    }

    @Override
    void create(){
        setMinimumSize(new Dimension(600, 600));
        setLayout(new GridBagLayout());

        worldtab = new WorldTab(parentFrame, place.getLayer().getWorld(), true);
        worldtab.getWorldPanel().resetHistory(new WorldCoordinate(place.getLayer().getId(), place.getX(), place.getY()));
        worldtab.getWorldPanel().setCursorForced(true);
        worldtab.getWorldPanel().resetHistory(place.getCoordinate());
        worldtab.getWorldPanel().setFocusForced(false);
        ((MapPainterDefault) worldtab.getWorldPanel().getMappainter()).setGridEnabled(false);

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

        LinkedList<String> directions1 = new LinkedList<>();
        for(String s: Path.directions)
            if(place.getExit(s) == null) directions1.add(s);

        constraints.gridx = 1;
        constraints.weightx = 0.0;
        directionComboBox1 = new JComboBox<>(directions1.toArray(new String[directions1.size()]));
        directionComboBox1.setEditable(true);
        add(directionComboBox1, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.weightx = 1.0;
        add(labelOtherPlace = new JLabel(), constraints);

        constraints.gridx = 1;
        constraints.weightx = 0.0;
        directionComboBox2 = new JComboBox<>();
        updateDirectionComboBox2();
        directionComboBox2.setEditable(true);
        add(directionComboBox2, constraints);

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

        worldtab.getWorldPanel().addCursorListener(new MapCursorListener() {
            @Override
            public void placeSelected(Place p) {
                if(p != place){
                    other = p;
                    labelOtherPlace.setText(other.toString());
                    updateDirectionComboBox2();
                }
            }

            @Override
            public void placeDeselected(Layer layer, int x, int y) {}
        });

        pack();
        setLocation(getParent().getX() + (getParent().getWidth() - getWidth()) / 2, getParent().getY() + (getParent().getHeight() - getHeight()) / 2);
    }

    private final static String ONE_WAY_PATH_STR =  "one way";

    /**
     * Fills the combo box only with directions thar aren't occupied yet
     */
    private void updateDirectionComboBox2() {
        directionComboBox2.removeAllItems();
        if(other != null)
            for(String s: Path.directions){
                Path pa = other.getExit(s);
                if(s.equals("-")) s = ONE_WAY_PATH_STR;
                if(pa == null) directionComboBox2.addItem(s);
            }
    }

    /**
     * Saves the new connection
     */
    private void save(){
        if(other != null){ // if a place is selected
            String dir1 = (String) directionComboBox1.getSelectedItem();
            String dir2 = (String) directionComboBox2.getSelectedItem();

            if(dir1.equals(ONE_WAY_PATH_STR)) dir1 = "-";
            if(dir2.equals(ONE_WAY_PATH_STR)) dir2 = "-";

            boolean exit_available_1 = place.getExit(dir1) == null;
            boolean exit_available_2 = other.getExit(dir2) == null;

            // if both exits are available
            if(exit_available_1 && exit_available_2)
                place.connectPath(new Path(place, dir1, other, dir2));
            // else show message
            else JOptionPane.showMessageDialog(this, "Couldn't connect path, an exit of a place is occupied");
        }
    }
}

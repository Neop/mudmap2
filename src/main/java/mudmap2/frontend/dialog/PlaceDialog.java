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
 *  The place dialog is used to create and modify places
 */
package mudmap2.frontend.dialog;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import mudmap2.backend.PlaceGroup;
import mudmap2.backend.Layer;
import mudmap2.backend.Place;
import mudmap2.backend.RiskLevel;
import mudmap2.backend.World;

/**
 * The file dialog is used to create and modify places
 * Derived from MouseListener, so it can be directly attached to buttons
 *
 * @author neop
 */
public class PlaceDialog extends ActionDialog {

    private static final long serialVersionUID = 1L;

    World world;
    Place place;
    Layer layer;
    int px, py;

    // if this place_group is choosen, it will be replaced with null
    PlaceGroup place_group_null;

    JTextField textfield_name;
    JCheckBox checkbox_lvl_min, checkbox_lvl_max;
    JComboBox<PlaceGroup> combobox_place_group;
    JComboBox<RiskLevel> combobox_risk;
    JSpinner spinner_rec_lvl_min, spinner_rec_lvl_max;

    /**
     * Creates a dialog to modify a place
     * @param parent
     * @param world
     * @param place existing place
     */
    public PlaceDialog(JFrame parent, World world, Place place){
        super(parent, "Edit place - " + place, true);

        this.world = world;
        this.place = place;
        layer = place.getLayer();
        px = place.getX();
        py = place.getY();
    }

    /**
     * Creates a dialog to create a new place
     * @param parent
     * @param layer layer or null to create a new one
     * @param world
     * @param px place coordinate x
     * @param py place coordinate y
     */
    public PlaceDialog(JFrame parent, World world, Layer layer, int px, int py){
        super(parent, "Add place", true);

        this.world = world;
        this.place = null;
        this.layer = layer;
        this.px = px;
        this.py = py;
    }

    /**
     * Creates the dialog
     */
    @Override
    void create(){
        setLayout(new GridLayout(0, 2, 4, 4));

        add(new JLabel("Name"));
        if(place != null) textfield_name = new JTextField(place.getName());
        else textfield_name = new JTextField();
        add(textfield_name);

        place_group_null = new PlaceGroup("none", null);

        add(new JLabel("Place group"));
        combobox_place_group = new JComboBox<>();
        combobox_place_group.addItem(place_group_null);
        for(PlaceGroup a : world.getPlaceGroups()) combobox_place_group.addItem(a);
        if(place != null && place.getPlaceGroup() != null) combobox_place_group.setSelectedItem(place.getPlaceGroup());
        add(combobox_place_group);

        add(new JLabel("Risk level"));
        combobox_risk = new JComboBox<>();
        for(RiskLevel rl : world.getRiskLevels()) combobox_risk.addItem(rl);
        if(place != null && place.getRiskLevel() != null) combobox_risk.setSelectedItem(place.getRiskLevel());
        add(combobox_risk);
        
        Integer min_lvl = 0, max_lvl = 0;
        if(place != null){
            min_lvl = place.getRecLevelMin();
            max_lvl = place.getRecLevelMax();
        }
        
        add(new JLabel("Minimal level"));
        spinner_rec_lvl_min = new JSpinner();
        spinner_rec_lvl_min.setModel(new SpinnerNumberModel(max(min_lvl, 0), 0, 1000, 1));
        add(spinner_rec_lvl_min);
        if(min_lvl < 0) spinner_rec_lvl_min.setEnabled(false);

        add(new JLabel());
        add(checkbox_lvl_min = new JCheckBox("Show min level"));
        checkbox_lvl_min.setSelected(min_lvl >= 0);
        checkbox_lvl_min.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                spinner_rec_lvl_min.setEnabled(((JCheckBox) ae.getSource()).isSelected());
            }
        });

        add(new JLabel("Maximum level"));
        spinner_rec_lvl_max = new JSpinner();
        spinner_rec_lvl_max.setModel(new SpinnerNumberModel(max(max_lvl, 0), 0, 1000, 1));
        add(spinner_rec_lvl_max);
        if(max_lvl < 0) spinner_rec_lvl_max.setEnabled(false);

        add(new JLabel());
        add(checkbox_lvl_max = new JCheckBox("Show max level"));
        checkbox_lvl_max.setSelected(max_lvl >= 0);
        checkbox_lvl_max.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                spinner_rec_lvl_max.setEnabled(((JCheckBox) ae.getSource()).isSelected());
            }
        });
        
        JButton button_cancel = new JButton("Cancel");
        add(button_cancel);
        button_cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                dispose();
            }
        });

        JButton button_ok = new JButton("Ok");
        add(button_ok);
        getRootPane().setDefaultButton(button_ok);
        button_ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                save();
                dispose();
            }
        });

        // listener for escape key
        getRootPane().registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setLocation(getParent().getX() + (getParent().getWidth() - getWidth()) / 2, getParent().getY() + (getParent().getHeight() - getHeight()) / 2);
    }

    // Integer.max() causes java.lang.NoSuchMethodError in some cases
    private int max(int a, int b){
        if(a > b) return a;
        else return b;
    }

    /**
     * Saves the place data
     */
    public void save(){
        //if(!textfield_name.getText().isEmpty()){ // name not empty
            try {
                if(layer == null) layer = world.getNewLayer();

                // create place if it doesn't exist else just set the name
                if(place == null) layer.put(place = new Place(textfield_name.getText(), px, py, layer));
                else place.setName(textfield_name.getText());

                PlaceGroup a = (PlaceGroup) combobox_place_group.getSelectedItem();
                place.setPlaceGroup(a != place_group_null ? a : null); // replace null group with null
                place.setRiskLevel((RiskLevel) combobox_risk.getSelectedItem());

                if(checkbox_lvl_min.isSelected()){
                    place.setRecLevelMin((Integer) spinner_rec_lvl_min.getValue());
                } else {
                    place.setRecLevelMin(-1);
                }
                if(checkbox_lvl_max.isSelected()){
                    place.setRecLevelMax((Integer) spinner_rec_lvl_max.getValue());
                } else {
                    place.setRecLevelMax(-1);
                }
            } catch (Exception ex) {
                Logger.getLogger(PlaceDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        //}
        getParent().repaint();
    }

    /**
     * Gets the place (eg. after creation
     * @return place
     */
    public Place getPlace(){
        return place;
    }

}

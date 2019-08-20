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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import mudmap2.backend.PlaceGroup;
import mudmap2.backend.Layer;
import mudmap2.backend.Place;
import mudmap2.backend.InformationColor;
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
    PlaceGroup placeGroupNull;

    JTextField textfieldName;
    JCheckBox checkboxLvlMin, checkboxLvlMax;
    JComboBox<PlaceGroup> comboboxPlaceGroup;
    JComboBox<InformationColor> comboboxInfoColor;
    JSpinner spinnerRecLvlMin, spinnerRecLvlMax;
    // comment box
    JTextArea commentArea;
    JScrollPane scrollPane;

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
    protected void create(){
        setLayout(new GridBagLayout());
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.insets = new Insets(4, 4, 4, 4);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;

        add(new JLabel("Name"), constraints);
        constraints.gridx++;
        if(place != null) textfieldName = new JTextField(place.getName());
        else textfieldName = new JTextField();
        add(textfieldName, constraints);

        placeGroupNull = new PlaceGroup("none", null);

        constraints.gridx = 0;
        constraints.gridy++;
        add(new JLabel("Place group"), constraints);
        comboboxPlaceGroup = new JComboBox<>();
        comboboxPlaceGroup.addItem(placeGroupNull);
        for(PlaceGroup a : world.getPlaceGroups()) comboboxPlaceGroup.addItem(a);
        if(place != null && place.getPlaceGroup() != null) comboboxPlaceGroup.setSelectedItem(place.getPlaceGroup());
        constraints.gridx++;
        add(comboboxPlaceGroup, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        add(new JLabel("Colored info ring"), constraints);
        comboboxInfoColor = new JComboBox<>();
        for(InformationColor rl : world.getInformationColors()) comboboxInfoColor.addItem(rl);
        if(place != null && place.getInfoRing() != null) comboboxInfoColor.setSelectedItem(place.getInfoRing());
        constraints.gridx++;
        add(comboboxInfoColor, constraints);
        
        Integer min_lvl = -1, max_lvl = -1;
        if(place != null){
            min_lvl = place.getRecLevelMin();
            max_lvl = place.getRecLevelMax();
        }
        
        constraints.gridx = 0;
        constraints.gridy++;
        add(checkboxLvlMin = new JCheckBox("Show minimum level:"), constraints);
        checkboxLvlMin.setSelected(min_lvl >= 0);
        checkboxLvlMin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                spinnerRecLvlMin.setEnabled(((JCheckBox) ae.getSource()).isSelected());
            }
        });
        spinnerRecLvlMin = new JSpinner();
        spinnerRecLvlMin.setModel(new SpinnerNumberModel(max(min_lvl, 0), 0, 1000, 1));
        constraints.gridx++;
        add(spinnerRecLvlMin, constraints);
        if(min_lvl < 0) spinnerRecLvlMin.setEnabled(false);

        constraints.gridx = 0;
        constraints.gridy++;
        add(checkboxLvlMax = new JCheckBox("Show maximum level:"), constraints);
        checkboxLvlMax.setSelected(max_lvl >= 0);
        checkboxLvlMax.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                spinnerRecLvlMax.setEnabled(((JCheckBox) ae.getSource()).isSelected());
            }
        });
        spinnerRecLvlMax = new JSpinner();
        spinnerRecLvlMax.setModel(new SpinnerNumberModel(max(max_lvl, 0), 0, 1000, 1));
        constraints.gridx++;
        add(spinnerRecLvlMax, constraints);
        if(max_lvl < 0) spinnerRecLvlMax.setEnabled(false);
        
        
        // comment box
        if(place != null) {
            commentArea = new JTextArea(place.getComments());
        } else {
            commentArea = new JTextArea();
        }
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);

        scrollPane = new JScrollPane(commentArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); 
        scrollPane.setPreferredSize(new Dimension(100, 200));
        
        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 2;
        add(new JLabel("Comments:"), constraints);
        
        constraints.gridy++;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        add(scrollPane, constraints);
        
        constraints.gridwidth = 1;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        
        JButton button_cancel = new JButton("Cancel");
        constraints.gridx = 0;
        constraints.gridy++;
        add(button_cancel, constraints);
        button_cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                dispose();
            }
        });

        JButton button_ok = new JButton("Ok");
        constraints.gridx++;
        add(button_ok, constraints);
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
                if(place == null) layer.put(place = new Place(textfieldName.getText(), px, py, layer));
                else place.setName(textfieldName.getText());

                // set place group
                PlaceGroup a = (PlaceGroup) comboboxPlaceGroup.getSelectedItem();
                place.setPlaceGroup(a != placeGroupNull ? a : null); // replace null group with null
                place.setInfoRing((InformationColor) comboboxInfoColor.getSelectedItem());

                // set minimum level
                if(checkboxLvlMin.isSelected()){
                    place.setRecLevelMin((Integer) spinnerRecLvlMin.getValue());
                } else {
                    place.setRecLevelMin(-1);
                }
                
                // set maximum level
                if(checkboxLvlMax.isSelected()){
                    place.setRecLevelMax((Integer) spinnerRecLvlMax.getValue());
                } else {
                    place.setRecLevelMax(-1);
                }
                
                // set comments
                place.setComments(commentArea.getText());
            } catch (Layer.PlaceNotInsertedException ex) {
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

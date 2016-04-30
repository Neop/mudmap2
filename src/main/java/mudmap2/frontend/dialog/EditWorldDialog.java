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
 *  The world edit dialog is used to modify the world name, path colors and
 *  risk levels
 */
package mudmap2.frontend.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import mudmap2.utils.Pair;
import mudmap2.backend.RiskLevel;
import mudmap2.backend.World;
import mudmap2.backend.WorldManager;
import mudmap2.frontend.GUIElement.ColorChooserButton;

/**
 * The world edit dialog is used to modify the world name, path colors and
 * risk levels
 * @author neop
 */
public class EditWorldDialog extends ActionDialog {

    World world;

    JTextField textfield_name;
    ColorChooserButton colorchooser_path;

    HashMap<RiskLevel, Pair<JTextField, ColorChooserButton>> risklevel_colors;
    JTextField risklevel_new_name; // entry to create a new risk level
    ColorChooserButton risklevel_new_color, tile_center_color;

    ButtonGroup buttongroup_place_id;
    JRadioButton radiobutton_place_id_none, radiobutton_place_id_unique, radiobutton_place_id_all;

    public EditWorldDialog(JFrame _parent, World _world) {
        super(_parent, "Edit world - " + _world.getName(), true);
        world = _world;
    }

    @Override
    void create() {
        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        GridBagConstraints constraints_l = new GridBagConstraints();
        GridBagConstraints constraints_r = new GridBagConstraints();

        constraints.insets = constraints_l.insets = constraints_r.insets = new Insets(2, 2, 2, 2);

        constraints_l.fill = GridBagConstraints.HORIZONTAL;
        constraints_r.fill = GridBagConstraints.BOTH;
        constraints_r.gridx = 1;
        constraints_l.weightx = constraints_r.weightx = 1.0;
        constraints_l.gridy = ++constraints_r.gridy;

        add(new JLabel("World name"), constraints_l);
        add(textfield_name = new JTextField(world.getName()), constraints_r);
        textfield_name.setColumns(20);

        constraints_l.gridy = ++constraints_r.gridy;
        constraints.gridy = constraints_l.gridy = ++constraints_r.gridy;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(new JSeparator(), constraints);

        constraints.gridy = constraints_l.gridy = ++constraints_r.gridy;
        add(new JLabel("Risk Levels"), constraints);

        risklevel_colors = new HashMap<>();
        for(RiskLevel rl: world.getRiskLevels()){
            constraints_l.gridy = ++constraints_r.gridy;
            JTextField tf_rl_name = new JTextField(rl.getDescription());
            add(tf_rl_name, constraints_l);
            ColorChooserButton colorchooser = new ColorChooserButton(getParent(), rl.getColor());
            add(colorchooser, constraints_r);
            risklevel_colors.put(rl, new Pair<>(tf_rl_name, colorchooser));
        }

        constraints_l.gridy = ++constraints_r.gridy;

        add(risklevel_new_name = new JTextField(), constraints_l);
        add(risklevel_new_color = new ColorChooserButton(getParent()), constraints_r);

        constraints_l.gridy = ++constraints_r.gridy;

        add(new JLabel("Tile center color"), constraints_l);
        add(tile_center_color = new ColorChooserButton(getParent(), world.getTileCenterColor()), constraints_r);

        constraints.gridy = constraints_l.gridy = ++constraints_r.gridy;
        add(new JSeparator(), constraints);

        buttongroup_place_id = new ButtonGroup();
        radiobutton_place_id_none = new JRadioButton("Don't show place ID on map");
        radiobutton_place_id_unique = new JRadioButton("Show place ID if name isn't unique");
        radiobutton_place_id_all = new JRadioButton("Always show place ID");
        buttongroup_place_id.add(radiobutton_place_id_none);
        buttongroup_place_id.add(radiobutton_place_id_unique);
        buttongroup_place_id.add(radiobutton_place_id_all);
        constraints_l.gridy = ++constraints_r.gridy;
        add(radiobutton_place_id_none, constraints_l);
        constraints_l.gridy = ++constraints_r.gridy;
        add(radiobutton_place_id_unique, constraints_l);
        constraints_l.gridy = ++constraints_r.gridy;
        add(radiobutton_place_id_all, constraints_l);
        switch(world.getShowPlaceId()){
            case NONE:
                buttongroup_place_id.setSelected(radiobutton_place_id_none.getModel(), true);
                break;
            default:
            case UNIQUE:
                buttongroup_place_id.setSelected(radiobutton_place_id_unique.getModel(), true);
                break;
            case ALL:
                buttongroup_place_id.setSelected(radiobutton_place_id_all.getModel(), true);
                break;
        }

        constraints_l.insets = constraints_r.insets = new Insets(0, 2, 0, 2);
        constraints_l.gridy = ++constraints_r.gridy;

        JButton button_cancel = new JButton("Cancel");
        add(button_cancel, constraints_l);
        button_cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                dispose();
            }
        });

        JButton button_ok = new JButton("Ok");
        add(button_ok, constraints_r);
        getRootPane().setDefaultButton(button_ok);
        button_ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    save();
                } catch (Exception ex) {
                    Logger.getLogger(EditWorldDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
                dispose();
            }
        });

        pack();
        setResizable(false);
        setLocation(getParent().getX() + (getParent().getWidth() - getWidth()) / 2, getParent().getY() + (getParent().getHeight() - getHeight()) / 2);
    }

    /**
     * Saves the changes
     */
    private void save() throws Exception{
        String name = textfield_name.getText();

        // if textfield is not empty
        if(!name.isEmpty()){
            world.setName(name);
        }

        // modify risk levels
        for(Map.Entry<RiskLevel,Pair<JTextField, ColorChooserButton>> rl_color: risklevel_colors.entrySet()){
            String description = rl_color.getValue().first.getText();
            if(description.isEmpty()) world.removeRiskLevel(rl_color.getKey());
            else {
                rl_color.getKey().setDescription(description);
                rl_color.getKey().setColor(rl_color.getValue().second.getColor());
            }
        }

        world.setTileCenterColor(tile_center_color.getColor());

        // add new risk level, if name not empty
        String name_new = risklevel_new_name.getText();
        if(!name_new.isEmpty()){
            world.addRiskLevel(new RiskLevel(name_new, risklevel_new_color.getColor()));
        }

        ButtonModel selection = buttongroup_place_id.getSelection();
        if(selection == radiobutton_place_id_none.getModel()) world.setShowPlaceID(World.ShowPlaceID.NONE);
        else if(selection == radiobutton_place_id_all.getModel()) world.setShowPlaceID(World.ShowPlaceID.ALL);
        else world.setShowPlaceID(World.ShowPlaceID.UNIQUE);

        getParent().repaint();
    }

}

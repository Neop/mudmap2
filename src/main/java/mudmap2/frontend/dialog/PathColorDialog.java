/*  MUD Map (v2) - A tool to create and organize maps for text-based games
 *  Copyright (C) 2015  Neop (email: mneop@web.de)
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
 *  This dialog is used to specify special path colors
 */
package mudmap2.frontend.dialog;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import mudmap2.backend.World;
import mudmap2.frontend.GUIElement.ColorChooserButton;

/**
 *  This dialog is used to specify special path colors
 * @author Neop
 */
public class PathColorDialog extends ActionDialog {

    private static final long serialVersionUID = 1L;

    World world;

    ColorChooserButton colchooser_cardinal;
    ColorChooserButton colchooser_non_cardinal;
    HashMap<JTextField, ColorChooserButton> colchooser_userdefined;
    ColorChooserButton colchooser_userdefined_new;
    JTextField textfield_userdefined_new;

    public PathColorDialog(JFrame parent, World world) {
        super(parent, "Path colors - " + world.getName(), true);
        this.world = world;
    }

    @Override
    protected void create() {
        setLayout(new GridBagLayout());

        GridBagConstraints constraints_l = new GridBagConstraints();
        GridBagConstraints constraints_r = new GridBagConstraints();

        constraints_l.insets = constraints_r.insets = new Insets(4, 2, 4, 2);

        constraints_l.fill = GridBagConstraints.HORIZONTAL;
        constraints_r.fill = GridBagConstraints.BOTH;
        constraints_r.gridx = 1;
        constraints_l.weightx = constraints_r.weightx = 1.0;

        constraints_l.gridy = ++constraints_r.gridy;
        add(new JLabel("Cardinal path color"), constraints_l);
        if(colchooser_cardinal == null) colchooser_cardinal = new ColorChooserButton(getParent(), world.getPathColorStd());
        add(colchooser_cardinal, constraints_r);

        constraints_l.gridy = ++constraints_r.gridy;
        add(new JLabel("Non-cardinal path color"), constraints_l);
        if(colchooser_non_cardinal == null) colchooser_non_cardinal = new ColorChooserButton(getParent(), world.getPathColorNstd());
        add(colchooser_non_cardinal, constraints_r);

        constraints_l.insets = constraints_r.insets = new Insets(2, 2, 2, 2);

        HashMap<Color, String> pcol = new HashMap<>();
        for(Map.Entry<String, Color> entry: world.getPathColors().entrySet()){
            if(pcol.containsKey(entry.getValue()) && !pcol.isEmpty()){ // if value is already in pcol
                pcol.put(entry.getValue(), pcol.get(entry.getValue()) + ";" + entry.getKey());
            } else {
                pcol.put(entry.getValue(), entry.getKey());
            }
        }
        colchooser_userdefined = new HashMap<>();
        for(Map.Entry<Color, String> entry: pcol.entrySet()){
            constraints_l.gridy = ++constraints_r.gridy;
            JTextField tf = new JTextField(entry.getValue());
            add(tf, constraints_l);
            ColorChooserButton ccb = new ColorChooserButton(this, new Color(entry.getKey().getRGB()));
            add(ccb, constraints_r);

            colchooser_userdefined.put(tf, ccb);
        }

        constraints_l.gridy = ++constraints_r.gridy;
        add(textfield_userdefined_new = new JTextField(), constraints_l);
        add(colchooser_userdefined_new = new ColorChooserButton(this), constraints_r);
        colchooser_userdefined.put(textfield_userdefined_new, colchooser_userdefined_new);

        constraints_l.gridy = ++constraints_r.gridy;
        GridBagConstraints constraint = (GridBagConstraints) constraints_l.clone();
        constraint.gridwidth = 2;
        add(new JLabel("<html>Hint: to specify path colors enter the exit directions into the empty<br>"
                            + "text field above. Allowed are the abbreviations of exit directions<br>"
                            + "(eg. n,se,nw,u,d), '-' for one-way paths and the names of self-defined<br>"
                            + "paths. You can enter multiple paths by separating them by a comma ','<br>"
                            + "or semicolon ';' like this: \"n,w,ne,d\"</html>"), constraint);

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
                save();
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
    private void save(){
        world.setPathColorStd(colchooser_cardinal.getColor());
        world.setPathColorNstd(colchooser_non_cardinal.getColor());

        world.getPathColors().clear();
        for(Entry<JTextField, ColorChooserButton> entry: colchooser_userdefined.entrySet()){
            String dirs = entry.getKey().getText();
            if(!dirs.isEmpty())
                for(String dir: dirs.split("[,;]"))
                    if(!dir.isEmpty())
                        world.getPathColors().put(dir, entry.getValue().getColor());
        }
    }
}

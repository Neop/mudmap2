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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import mudmap2.Pair;
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
    ColorChooserButton risklevel_new_color;
    
    public EditWorldDialog(JFrame _parent, World _world) {
        super(_parent, "Edit world - " + _world.get_name(), true);
        world = _world;
    }
    
    @Override
    void create() {
        setLayout(new GridLayout(0, 2));
        
        add(new JLabel("World name"));
        add(textfield_name = new JTextField(world.get_name()));
        
        add(new JLabel("Path color"));
        add(colorchooser_path = new ColorChooserButton(getParent(), world.get_path_color()));
        
        add(new JLabel("Risk Levels"));
        add(new JLabel());
        
        risklevel_colors = new HashMap<RiskLevel, Pair<JTextField, ColorChooserButton>>();
        for(RiskLevel rl: world.get_risk_levels()){
            JTextField tf_rl_name = new JTextField(rl.get_description());
            add(tf_rl_name);
            ColorChooserButton colorchooser = new ColorChooserButton(getParent(), rl.get_color());
            add(colorchooser);
            risklevel_colors.put(rl, new Pair<JTextField, ColorChooserButton>(tf_rl_name, colorchooser));
        }
        
        add(risklevel_new_name = new JTextField());
        add(risklevel_new_color = new ColorChooserButton(getParent()));
        
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
        button_ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                save();
                dispose();
            }
        });
        
        pack();
        setLocation(getParent().getX() + (getParent().getWidth() - getWidth()) / 2, getParent().getY() + (getParent().getHeight() - getHeight()) / 2);
    }
    
    /**
     * Saves the changes
     */
    private void save(){
        String name = textfield_name.getText();
       
        // if textfield is not empty and name is unique
        if(!name.isEmpty() && (name.equals(world.get_name()) || WorldManager.get_world_file(name) == null)){
            world.set_name(name);
            
            world.set_path_color(colorchooser_path.get_color());
            
            // modify risk levels
            for(Map.Entry<RiskLevel,Pair<JTextField, ColorChooserButton>> foo: risklevel_colors.entrySet()){
                String description = foo.getValue().first.getText();
                if(description.isEmpty()) world.remove_risk_level(foo.getKey());
                else {
                    foo.getKey().set_description(description);
                    foo.getKey().set_color(foo.getValue().second.get_color());
                }
            }
            
            // add new risk level, if name not empty
            String name_new = risklevel_new_name.getText();
            if(!name_new.isEmpty()){
                world.add_risk_level(new RiskLevel(name_new, risklevel_new_color.get_color()));
            }
        }
        getParent().repaint();
    }
    
}

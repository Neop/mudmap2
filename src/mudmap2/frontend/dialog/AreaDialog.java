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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import mudmap2.backend.Area;
import mudmap2.backend.Place;
import mudmap2.backend.World;
import mudmap2.frontend.GUIElement.ColorChooserButton;

/**
 * The area dialog is used to create, modify and remove areas
 * @author neop
 */
public class AreaDialog extends JDialog implements ActionListener {

    boolean new_area;
    
    JFrame parent;
    JTextField textfield_name;
    ColorChooserButton colorchooserbutton;
    
    World world;
    Area area;
    Place place;
    
    /**
     * Constructs a modify / delete dialog for existing areas
     * @param _parent parent window
     * @param _world world
     * @param _area area to be modified
     */
    public AreaDialog(JFrame _parent, World _world, Area _area){
        super(_parent, "Edit area - " + _area, true);
        
        new_area = false;
        parent = _parent;
        world = _world;
        area = _area;
        place = null;
        
        create();
    }
    
    /**
     * Constructs a dialog to create a new area
     * @param _parent parent window
     * @param _world world
     */
    public AreaDialog(JFrame _parent, World _world){
        super(_parent, "New area", true);
        
        new_area = true;
        parent = _parent;
        world = _world;
        area = null;
        place = null;
        
        create();
    }
    
    public AreaDialog(JFrame _parent, World _world, Place _place){
        super(_parent, (_place.get_area() == null) ? "New area" : ("Edit area - " + _place.get_area()), true);
        
        place = _place;
        new_area = place.get_area() == null;
        parent = _parent;
        world = _world;
        area = place.get_area();
        
        create();
    }
    
    /**
     * Creates the GUI
     */
    private void create(){
        setSize(300, 150);
        setLocation(parent.getX() + (parent.getWidth() - getWidth()) / 2, parent.getY() + (parent.getHeight() - getHeight()) / 2);
        setLayout(new GridLayout(3, 2));
        
        add(new JLabel("Name"));
        if(area != null) textfield_name = new JTextField(area.toString());
        else textfield_name = new JTextField();
        add(textfield_name);
        
        add(new JLabel("Color"));
        if(area != null) add(colorchooserbutton = new ColorChooserButton(parent, area.get_color())); 
        else add(colorchooserbutton = new ColorChooserButton(parent));
        
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
       
        // listener for escape key
        getRootPane().registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    private void save(){
        // add new area to world and place
        if(new_area){
            area = new Area(textfield_name.getText(), colorchooserbutton.get_color());
            world.add_area(area);
            if(place != null) place.set_area(area);
        } else {
            // modify area
            area.set_name(textfield_name.getText());
            area.set_color(colorchooserbutton.get_color());
        }
        parent.repaint();
    }
    
    @Override
    public void actionPerformed(ActionEvent arg0) {
        setVisible(true);
    }
    
}

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
 *  A color chooser button, which changes it's color according to a color
 *  chooser dialog it opens
 */
package mudmap2.frontend.GUIElement;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import javax.swing.JButton;
import javax.swing.JColorChooser;

/**
 * A color chooser button, which changes it's color according to a color
 * chooser dialog it opens
 * @author neop
 */
public class ColorChooserButton extends JButton {

    Component parent;
    Color color;

    LinkedList<ColorChangeListener> colorChangeListeners;

    public ColorChooserButton(Component _parent){
        super("A"); // set text to enlarge the button
        color = new Color(128, 128, 128);
        create();
    }

    public ColorChooserButton(Component _parent, Color _color){
        super("A"); // set text to enlarge the button
        color = _color;
        create();
    }

    private void create(){
        colorChangeListeners = new LinkedList();

        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                Color col = JColorChooser.showDialog(parent, "Choose color", color);
                if(col != null){
                    color = col;
                    callColorChangeListeners();
                }
            }
        });
    }

    @Override
    public void paintComponent(Graphics g){
        g.setColor(getBackground());
        g.fillRect(0, 0, (int) g.getClipBounds().getWidth() + 1, (int) g.getClipBounds().getHeight() + 1);

        g.setColor(color);
        g.fillRect(2, 2, getSize().width - 4, getSize().height - 4);
        g.setColor(Color.LIGHT_GRAY);
        if(isFocusOwner()) g.drawRect(4, 4, getSize().width - 9, getSize().height - 9);

        g.drawRect(1, 1, getSize().width - 3, getSize().height - 3);
    }

    /**
     * Gets the color
     * @return color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the color
     * @param color
     */
    public void setColor(Color color) {
        this.color = color;
        repaint();
    }

    private void callColorChangeListeners(){
        for(ColorChangeListener ccl: colorChangeListeners){
            ccl.colorChanged(this);
        }
    }

    public void addColorChangeListener(ColorChangeListener ccl){
        colorChangeListeners.add(ccl);
    }

    public void removeColorChangeListener(ColorChangeListener ccl){
        colorChangeListeners.remove(ccl);
    }

    public interface ColorChangeListener {
        void colorChanged(ColorChooserButton ccb);
    }

}

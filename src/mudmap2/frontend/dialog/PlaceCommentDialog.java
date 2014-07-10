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
 *  The PlaceCommentDialog modifies the comments of a place
 */
package mudmap2.frontend.dialog;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import mudmap2.backend.Place;

/**
 * The PlaceCommentDialog modifies the comments of a place
 * @author neop
 */
public class PlaceCommentDialog extends ActionDialog {

    Place place;
    
    JTextArea commentarea;
    JOptionPane optionPane;
    
    public PlaceCommentDialog(JFrame _parent, Place _place) {
        super(_parent, "Comments - " + _place, true);
        place = _place;
    }

    @Override
    void create() {
        optionPane = new JOptionPane();
        optionPane.setOptionType(JOptionPane.YES_NO_OPTION);
        
        setContentPane(optionPane);
        optionPane.setMessage(commentarea = new JTextArea(place.get_comments_string(true)));
        
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        
        optionPane.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent arg0) {
                if(isVisible() && arg0.getSource() == optionPane && arg0.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)){
                    int value = ((Integer) optionPane.getValue()).intValue();
                    if(value == JOptionPane.YES_OPTION){
                        place.delete_comments();
                        String comments = commentarea.getText();
                        if(comments.trim().length() > 0)
                            for(String line: comments.split("\n"))
                                place.add_comment(line);
                    }
                    dispose();
                    getParent().repaint();
                }
            }
        });
        
        pack();
        setLocation(getParent().getX() + (getParent().getWidth() - getWidth()) / 2, getParent().getY() + (getParent().getHeight() - getHeight()) / 2);
    }
}

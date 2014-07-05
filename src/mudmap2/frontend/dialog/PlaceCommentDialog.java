/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mudmap2.frontend.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import mudmap2.backend.Place;

/**
 *
 * @author neop
 */
public class PlaceCommentDialog extends JDialog implements ActionListener {

    Place place;
    
    JFrame parent;
    JTextArea commentarea;
    JButton button_ok, button_cancel;
    JOptionPane optionPane;
    
    public PlaceCommentDialog(JFrame _parent, Place _place) {
        super(_parent, "Comments - " + _place.get_name(), true);
        parent = _parent;
        place = _place;
        
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
                    setVisible(false);
                    parent.repaint();
                }
            }
        });
        
        pack();
        setLocation(parent.getX() + (parent.getWidth() - getWidth()) / 2, parent.getY() + (parent.getHeight() - getHeight()) / 2);
    }
    
    @Override
    public void actionPerformed(ActionEvent arg0) {
        setVisible(true);
    }
}

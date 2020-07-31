/*  MUD Map (v2) - A tool to create and organize maps for text-based games
 *  Copyright (C) 2020  Neop (email: mneop@web.de)
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
package mudmap2.frontend.dialog;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JTextField;
import mudmap2.backend.Path;
import mudmap2.backend.Place;

/**
 * The PathConnectUnknownDialog kets the user select exit directions to be shown
 * at the map without connection to other places (they will effectively connect
 * the place to itself)
 * @author neop
 */
public class PathConnectUnknownDialog extends ActionDialog {
    
    final Place place;
    
    JCheckBox cbN, cbNE, cbE, cbSE, cbS, cbSW, cbW, cbNW, cbU, cbD, cbCustom;
    JTextField textField;

    public PathConnectUnknownDialog(JFrame parent, Place place) {
        super(parent, "Connect exits to unknown places to " + place, true);
        this.place = place;
    }

    @Override
    protected void create() {
        setLayout((new GridBagLayout()));
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        cbNW = new JCheckBox("NW");
        cbNW.setEnabled(place.getExit("nw").isEmpty());
        add(cbNW, constraints);
        
        constraints.gridx = 1;
        constraints.gridy = 0;
        cbN = new JCheckBox("N");
        cbN.setEnabled(place.getExit("n").isEmpty());
        add(cbN, constraints);
        
        constraints.gridx = 2;
        constraints.gridy = 0;
        cbNE = new JCheckBox("NE");
        cbNE.setEnabled(place.getExit("ne").isEmpty());
        add(cbNE, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 1;
        cbW = new JCheckBox("W");
        cbW.setEnabled(place.getExit("w").isEmpty());
        add(cbW, constraints);
        
        constraints.gridx = 2;
        constraints.gridy = 1;
        cbE = new JCheckBox("E");
        cbE.setEnabled(place.getExit("e").isEmpty());
        add(cbE, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 2;
        cbSW = new JCheckBox("SW");
        cbSW.setEnabled(place.getExit("sw").isEmpty());
        add(cbSW, constraints);
        
        constraints.gridx = 1;
        constraints.gridy = 2;
        cbS = new JCheckBox("S");
        cbS.setEnabled(place.getExit("s").isEmpty());
        add(cbS, constraints);
        
        constraints.gridx = 2;
        constraints.gridy = 2;
        cbSE = new JCheckBox("SE");
        cbSE.setEnabled(place.getExit("se").isEmpty());
        add(cbSE, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 3;
        cbU = new JCheckBox("Up");
        cbU.setEnabled(place.getExit("u").isEmpty());
        add(cbU, constraints);
        
        constraints.gridx = 1;
        constraints.gridy = 3;
        cbD = new JCheckBox("Down");
        cbD.setEnabled(place.getExit("d").isEmpty());
        add(cbD, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 4;
        cbCustom = new JCheckBox("Custom:");
        add(cbCustom, constraints);
        
        constraints.gridx = 1;
        constraints.gridy = 4;
        constraints.gridwidth = 2;
        textField = new JTextField();
        add(textField, constraints);
        
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy++;
        JButton button_cancel = new JButton("Cancel");
        add(button_cancel, constraints);
        button_cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        constraints.gridx = 1;
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
        
        setMinimumSize(new Dimension(250, 250));
        pack();
        setLocation(getParent().getX() + (getParent().getWidth() - getWidth()) / 2, getParent().getY() + (getParent().getHeight() - getHeight()) / 2);
    }

    private void save(){
        if(cbNW.isSelected()) addPath("nw");
        if(cbN.isSelected()) addPath("n");
        if(cbNE.isSelected()) addPath("ne");
        if(cbW.isSelected()) addPath("w");
        if(cbE.isSelected()) addPath("e");
        if(cbSW.isSelected()) addPath("sw");
        if(cbS.isSelected()) addPath("s");
        if(cbSE.isSelected()) addPath("se");
        if(cbU.isSelected()) addPath("u");
        if(cbD.isSelected()) addPath("d");
        if(cbCustom.isSelected()) addPath(textField.getText());
        
        getParent().repaint();
    }
    
    private void addPath(String exit) {
        place.connectPath(new Path(place, exit, place, "unknown"));
    }
    
}

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
 *  The PathConnecNeighborsDialog lets the user select multiple places next to a
 *  place and connects them with paths
 */
package mudmap2.frontend.dialog;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import mudmap2.backend.Layer;
import mudmap2.backend.Path;
import mudmap2.backend.Place;

/**
 * The PathConnecNeighborsDialog lets the user select multiple places next to a
 * place and connects them with paths
 * @author neop
 */
public class PathConnectNeighborsDialog extends ActionDialog{

    private static final long serialVersionUID = 1L;

    Place place;

    HashMap<Place, JCheckBox> neighbor_checkboxes;

    public PathConnectNeighborsDialog(JFrame parent, Place place) {
        super(parent, "Connect neighbor paths to " + place, true);

        this.place = place;
    }

    @Override
    protected void create(){
        neighbor_checkboxes = new HashMap<>();

        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.gridwidth = 2;

        Layer layer = place.getLayer();
        for(int x = -1; x <= 1; ++x){
            for(int y = -1; y <= 1; ++y){
                if(x != 0 || y != 0){
                    // diretion of the neighbor place relative to _place
                    String dir = Path.getDir(x, y);

                    // if exit of _place available
                    if(place.getExit(dir).isEmpty()){
                        Place neighbor = layer.get(place.getX() + x, place.getY() + y);

                        // if exit of neighbor available
                        if(neighbor != null && neighbor.getExit(Path.getOppositeDir(dir)).isEmpty()){
                            JCheckBox checkbox = new JCheckBox("[" + dir + "] " + neighbor);
                            constraints.gridy++;
                            add(checkbox, constraints);
                            neighbor_checkboxes.put(neighbor, checkbox);
                        }
                    }
                }
            }
        }

        constraints.gridwidth = 1;
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

        setMinimumSize(new Dimension(250, 20));
        pack();
        setLocation(getParent().getX() + (getParent().getWidth() - getWidth()) / 2, getParent().getY() + (getParent().getHeight() - getHeight()) / 2);
    }

    private void save(){
        for(Entry<Place, JCheckBox> entry: neighbor_checkboxes.entrySet()){
            if(entry.getValue().isSelected()){
                int dx = entry.getKey().getX() - place.getX();
                int dy = entry.getKey().getY() - place.getY();
                // get direction
                String dir = Path.getDir(dx, dy);
                // connect path
                place.connectPath(new Path(place, dir, entry.getKey(), Path.getOppositeDir(dir)));
            }
        }
        getParent().repaint();
    }
}

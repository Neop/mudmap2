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
 *  This dialog shows a list of places of a world. If a list item is selected,
 *  the parent WorldTab will move to the place
 */
package mudmap2.frontend.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import mudmap2.backend.Place;
import mudmap2.frontend.WorldTab;

/**
 * This dialog shows a list of places of a world. If a list item is selected,
 * the parent WorldTab will move to the place
 * @author neop
 */
public class PlaceListDialog extends ActionDialog{
    
    WorldTab parent;
    ArrayList<Place> places;
    
    /**
     * constructs a dialog that lists all places of a world
     * @param parent 
     */
    public PlaceListDialog(WorldTab parent, boolean modal){
        super(parent.get_parent(), "Place list - " + parent.getWorld().getName(), modal);
        this.parent = parent;
        // get places later, in case something changes
        //places = new ArrayList<Place>(parent.getWorld().getPlaces());
    }
  
    /**
     * constructs a dialog that lists all places
     * @param parent
     * @param places places to be shown
     */
    public PlaceListDialog(WorldTab parent, ArrayList<Place> places, boolean modal){
        super(parent.get_parent(), "Place list - " + parent.getWorld().getName(), modal);
        this.parent = parent;
        this.places = places;
    }
    
    @Override
    void create() {
        if(places == null) places = new ArrayList<Place>(parent.getWorld().getPlaces());
        
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        
        // sort list
        Collections.sort(places);
        
        // show places list
        constraints.weightx = constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        final JList<Place> list = new JList<Place>(places.toArray(new Place[places.size()]));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(list), constraints);
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                Place selection = (Place) (((JList) arg0.getSource()).getSelectedValue());
                if(selection != null) parent.pushPosition(selection.getCoordinate());
            }
        });
        
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = 1;
        
        JTextField text_search = new JTextField("Search");
        text_search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JTextField tf = (JTextField) ae.getSource();
                String search_str = tf.getText().toLowerCase();
                
                LinkedList<Place> places_found = new LinkedList<Place>();
                for(Place pl: places){
                    if(pl.getName().toLowerCase().contains(search_str)) places_found.add(pl);
                }
                
                list.setListData((Place[]) places_found.toArray(new Place[places_found.size()]));
            }
        });
        add(text_search, constraints);
        constraints.gridy = 2;
        
        JButton button_close = new JButton("Close");
        add(button_close, constraints);
        getRootPane().setDefaultButton(button_close);
        button_close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                dispose();
            }
        });
        
        pack();
    }
}

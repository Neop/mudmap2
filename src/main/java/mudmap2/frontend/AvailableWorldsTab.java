/*  MUD Map (v2) - A tool to create and organize maps for text-based games
 *  Copyright (C) 2016  Neop (email: mneop@web.de)
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
package mudmap2.frontend;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JPanel;
import mudmap2.backend.WorldFileList;
import mudmap2.backend.WorldManager;

/**
 * The available worlds tab shows a list off all known worlds
 */
final class AvailableWorldsTab extends JPanel {

    // Reference to the main window
    final Mainwindow mwin;

    public AvailableWorldsTab(Mainwindow mwin) {
        this.mwin = mwin;
        WorldFileList.findWorlds();
        update();
    }

    /**
     * Updates the tab (call this after creating a new world)
     */
    public void update() {
        // get and sort world names (can't use String array here :C)
        /*Object[] worlds = WorldManager.get_world_list().toArray();
        Arrays.sort(worlds, Collator.getInstance());*/
        // reset previously created tab
        removeAll();
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(2, 2, 2, 2);
        //for(final Object world_name: worlds){
        for(final Entry<String, String> entry: WorldFileList.getWorlds().entrySet()) {
            JButton b = new JButton(entry.getValue() + " (" + entry.getKey() + ")");
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    try {
                        mwin.createTab(WorldManager.getWorld(entry.getKey()), entry.getKey());
                    } catch (Exception ex) {
                        Logger.getLogger(AvailableWorldsTab.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            constraints.gridx = 0;
            constraints.gridy++;
            constraints.weightx = 1.0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            add(b, constraints);
            JButton r = new JButton("Delete");
            r.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    WorldManager.deleteWorldFile(entry.getKey());
                }
            });
            constraints.gridx = 1;
            constraints.weightx = 0.0;
            add(r, constraints);
        }
        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 2;
        JButton buttonSearch = new JButton("Search for worlds");
        add(buttonSearch, constraints);
        buttonSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                WorldFileList.readDirectory();
                update();
            }
        });
        constraints.gridy++;
        JButton buttonNew = new JButton("New world");
        add(buttonNew, constraints);
        buttonNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                mwin.createNewWorld();
            }
        });
    }

}

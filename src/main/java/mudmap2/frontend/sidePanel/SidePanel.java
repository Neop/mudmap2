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
package mudmap2.frontend.sidePanel;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import mudmap2.backend.World;

/**
 *
 * @author neop
 */
public class SidePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    World world;

    LayerPanel layerPanel;
    PlacePanel placePanel;

    public SidePanel(World world){
        this.world = world;

        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        layerPanel = new LayerPanel(world);
        tabbedPane.addTab("Maps", layerPanel);

        placePanel = new PlacePanel(world);
        tabbedPane.addTab("Places", placePanel);

        world.addChangeListener(layerPanel);
        world.addChangeListener(placePanel);
    }

    public void update(){
        layerPanel.update();
        placePanel.update();
    }

    public LayerPanel getLayerPanel() {
        return layerPanel;
    }

    public PlacePanel getPlacePanel() {
        return placePanel;
    }
    
    /**
     * Add LayerPanelListener
     * @param listener
     */
    public void addLayerPanelListener(LayerPanelListener listener){
        layerPanel.addLayerPanelListener(listener);
        placePanel.addLayerPanelListener(listener);
    }

    /**
     * Remove LayerPanelListener
     * @param listener
     */
    public void removeLayerPanelListener(LayerPanelListener listener){
        layerPanel.removeLayerPanelListener(listener);
        placePanel.removeLayerPanelListener(listener);
    }

    /**
     * Add LayerPanelListener
     * @param listener
     */
    public void addPlacePanelListener(PlacePanelListener listener){
        placePanel.addPlacePanelListener(listener);
    }

    /**
     * Remove LayerPanelListener
     * @param listener
     */
    public void removePlacePanelListener(PlacePanelListener listener){
        placePanel.removePlacePanelListener(listener);
    }

}

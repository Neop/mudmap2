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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import mudmap2.backend.Layer;
import mudmap2.backend.World;
import mudmap2.backend.WorldChangeListener;
import mudmap2.frontend.GUIElement.LayerPreviewPanel;
import mudmap2.utils.AlphanumComparator;

/**
 *
 * @author neop
 */
public class LayerPanel extends JPanel implements ActionListener,MouseListener,WorldChangeListener {
    private static final long serialVersionUID = 1L;

    final static Integer PREVIEW_WIDTH_X = 100;
    final static Integer PREVIEW_WIDTH_Y = 100;

    World world;
    JScrollPane scrollPane;

    HashSet<LayerPanelListener> layerListeners;
    HashMap<JPanel, Layer> panels;

    public LayerPanel(World world){
        this.world = world;

        layerListeners = new HashSet<>();
        panels = new HashMap<>();

        setLayout(new BorderLayout());
        updateLayerPanels();
    }

    public final void updateLayerPanels(){
        removeAll();

        // add button
        JButton buttonAdd = new JButton("Add new map");
        add(buttonAdd, BorderLayout.SOUTH);
        buttonAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(LayerPanelListener listener: layerListeners){
                    listener.createLayer();
                }
            }
        });

        // create content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // create scrollPane
        scrollPane = new JScrollPane(contentPanel,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        add(scrollPane, BorderLayout.CENTER);

        ArrayList<Layer> layerList = new ArrayList<>(world.getLayers());
        Collections.sort(layerList, new AlphanumComparator<>());

        // add layers to content panel
        for(Layer layer: layerList){
            JPanel preview = createLayerPanel(layer);
            contentPanel.add(preview);

            panels.put(preview, layer);
            preview.addMouseListener(this);
        }
    }

    /**
     * Creates a layer preview panel
     *
     * @param layer
     * @return layer preview
     */
    private JPanel createLayerPanel(Layer layer){
        // create panel
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(layer.getName()));

        panel.setPreferredSize(new Dimension(PREVIEW_WIDTH_X, PREVIEW_WIDTH_Y));
        panel.setMinimumSize(new Dimension(PREVIEW_WIDTH_X, PREVIEW_WIDTH_Y));

        // create layer preview
        LayerPreviewPanel layerPreviewPanel = new LayerPreviewPanel(layer);
        panel.add(layerPreviewPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Add LayerPanelListener
     * @param listener
     */
    public void addLayerPanelListener(LayerPanelListener listener){
        if(!layerListeners.contains(listener)) layerListeners.add(listener);
    }

    /**
     * Remove LayerPanelListener
     * @param listener
     */
    public void removeLayerPanelListener(LayerPanelListener listener){
        layerListeners.remove(listener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch(e.getActionCommand()){
            case "create_layer":
                for(LayerPanelListener listener: layerListeners){
                    listener.createLayer();
                }
                break;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getSource() instanceof JPanel && panels.containsKey((JPanel) e.getSource())){
            // getPlace layer
            Layer layer = panels.get((JPanel) e.getSource());
            for(LayerPanelListener listener: layerListeners){
                listener.layerSelected(layer, e);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void WorldChanged(Object source) {
        // only recreate panel on layer change
        if(source instanceof Layer){
            updateLayerPanels();
        } else {
            revalidate();
            repaint();
        }
    }

}

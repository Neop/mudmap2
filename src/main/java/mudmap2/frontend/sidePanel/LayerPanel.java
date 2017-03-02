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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import mudmap2.backend.Layer;
import mudmap2.backend.World;
import mudmap2.backend.WorldChangeListener;
import mudmap2.frontend.GUIElement.LayerPreviewPanel;
import mudmap2.utils.AlphanumComparator;

/**
 *
 * @author neop
 */
public class LayerPanel extends JPanel implements ActionListener,WorldChangeListener {
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

        JPanel south = new JPanel(new GridLayout(2, 1));

        // search box
        JTextField textFieldSearch = new JTextField("Search maps");
        south.add(textFieldSearch);
        textFieldSearch.setToolTipText("Search for layers");
        textFieldSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update(((JTextField) e.getSource()).getText());
            }
        });

        // add button
        JButton buttonAdd = new JButton("Add new map");
        south.add(buttonAdd);
        buttonAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(LayerPanelListener listener: layerListeners){
                    listener.createLayer();
                }
            }
        });

        add(south, BorderLayout.SOUTH);


        update();
    }

    public void setActiveLayer(Layer l){
        for(Map.Entry<JPanel, Layer> entry : panels.entrySet()){
            if(entry.getValue() == l){
                ((LayerPreviewPanel) entry.getKey().getComponent(0)).setMarked(true);
            } else {
                ((LayerPreviewPanel) entry.getKey().getComponent(0)).setMarked(false);
            }
        }
    }
    
    public final void update(){
        update("");
    }

    public final void update(String keyword){
        panels.clear();
        if(scrollPane != null){
            BorderLayout layout = (BorderLayout) getLayout();
            remove(layout.getLayoutComponent(BorderLayout.CENTER));
        }

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

        Boolean hasKeywords = keyword != null && !keyword.isEmpty();
        String[] keywords = keyword.toLowerCase().split(" ");

        // add layers to content panel
        for(Layer layer: layerList){
            if(hasKeywords){
                Boolean found = false;
                for(String key: keywords){
                    if(layer.getName().toLowerCase().contains(key)){
                        found = true;
                        break;
                    };
                }
                if(!found) continue;
            }

            JPanel preview = createLayerPanel(layer);
            contentPanel.add(preview);

            panels.put(preview, layer);
            preview.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(e.getSource() instanceof JPanel && panels.containsKey((JPanel) e.getSource())){
                        Layer layer = panels.get((JPanel) e.getSource());

                        if(e.getButton() == MouseEvent.BUTTON1){
                            if(e.getClickCount() == 1){
                                for(LayerPanelListener listener: layerListeners){
                                    listener.layerSelected(layer);
                                }
                            } else {
                                editLayer(layer);
                            }
                        } else if(e.getButton() == MouseEvent.BUTTON3){
                            editLayer(layer);
                        }
                    }
                }
            });
        }

        revalidate();
        repaint();
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

        // layerPreviewPanel stops accepting mouseEvents if using tooltip
        //layerPreviewPanel.setToolTipText("Click to go to layer, double click or righ click to change layer name");

        return panel;
    }

    private void editLayer(Layer layer){
        String name = JOptionPane.showInputDialog(this, "Map name", layer.getName());
        if(name != null && !name.isEmpty()){
            layer.setName(name);
            update();
        }
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
    public void worldChanged(Object source) {
        // only recreate panel on layer change
        if(source instanceof Layer){
            update();
        } else {
            revalidate();
            repaint();
        }
    }

}

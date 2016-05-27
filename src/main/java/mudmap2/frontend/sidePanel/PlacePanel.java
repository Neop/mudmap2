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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import mudmap2.backend.Layer;
import mudmap2.backend.Place;
import mudmap2.backend.World;
import mudmap2.backend.WorldChangeListener;
import mudmap2.utils.AlphanumComparator;

/**
 *
 * @author neop
 */
public class PlacePanel extends JPanel implements TreeSelectionListener,WorldChangeListener {

    private static final long serialVersionUID = 1L;

    World world;

    JTree tree;

    HashMap<Layer, LayerTreeNode> layerNodes;
    HashMap<Place, PlaceTreeNode> placeNodes;

    HashSet<LayerPanelListener> layerListeners;
    HashSet<PlacePanelListener> placeListeners;

    public PlacePanel(World world){
        this.world = world;
        setLayout(new BorderLayout());

        layerNodes = new HashMap<>();
        placeNodes = new HashMap<>();

        layerListeners = new HashSet<>();
        placeListeners = new HashSet<>();

        update();
    }

    public final void update(){
        removeAll();

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(world.getName());
        tree = new JTree(root);
        tree.getSelectionModel().addTreeSelectionListener(this);

        JScrollPane scrollPane = new JScrollPane(tree,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane);

        ArrayList<Layer> layerList = new ArrayList<>(world.getLayers());
        Collections.sort(layerList, new AlphanumComparator<>());

        for(Layer layer: layerList){
            LayerTreeNode layerNode = new LayerTreeNode(layer);
            root.add(layerNode);
            layerNodes.put(layer, layerNode);


            HashSet<Place> places = layer.getPlaces();
            ArrayList<Place> placeList = new ArrayList<>(places);
            Collections.sort(placeList, new AlphanumComparator<>());

            for(Place place: placeList){
                PlaceTreeNode placeNode = new PlaceTreeNode(place);
                layerNode.add(placeNode);
                placeNodes.put(place, placeNode);
            }
        }

        tree.expandRow(0);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
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


    /**
     * Add PlacePanelListener
     * @param listener
     */
    public void addPlacePanelListener(PlacePanelListener listener){
        if(!placeListeners.contains(listener)) placeListeners.add(listener);
    }

    /**
     * Remove PlacePanelListener
     * @param listener
     */
    public void removePlacePanelListener(PlacePanelListener listener){
        placeListeners.remove(listener);
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        Object component = e.getPath().getLastPathComponent();
        if(component instanceof PlaceTreeNode){
            for(PlacePanelListener listener: placeListeners){
                listener.placeSelected(((PlaceTreeNode) component).getPlace());
            }
        }
    }

    @Override
    public void WorldChanged(Object source) {
        if(source instanceof Layer){
            if(layerNodes.containsKey((Layer) source)){
                layerNodes.get((Layer) source).update();
            } else { // new layer
                update();
            }
        } else if(source instanceof Place){
            Place place = (Place) source;
            if(placeNodes.containsKey(place)){
                placeNodes.get(place).update();
            } else if(layerNodes.containsKey(place.getLayer())) { // new place
                HashSet<Place> places = place.getLayer().getPlaces();
                ArrayList<Place> placeList = new ArrayList<>(places);
                Collections.sort(placeList, new AlphanumComparator<>());

                Integer pos = placeList.indexOf(place);

                PlaceTreeNode placeNode = new PlaceTreeNode(place);
                placeNodes.put(place, placeNode);
                layerNodes.get(place.getLayer()).insert(placeNode, pos);

                ((DefaultTreeModel) tree.getModel()).reload();
            }
        }
    }

    private class LayerTreeNode extends DefaultMutableTreeNode {
        private static final long serialVersionUID = 1L;

        Layer layer;

        public LayerTreeNode(Layer layer){
            super(layer.getName());
            this.layer = layer;
        }

        public Layer getLayer() {
            return layer;
        }

        public void update(){
            System.out.println("update layer " + layer.getName());
            setUserObject(layer.getName());
        }

    }

    private class PlaceTreeNode extends DefaultMutableTreeNode {
        private static final long serialVersionUID = 1L;

        Place place;

        public PlaceTreeNode(Place place){
            super(place.toString());
            this.place = place;
        }

        public Place getPlace() {
            return place;
        }

        public void update(){
            System.out.println("update place " + place.toString());
            setUserObject(place.toString());
        }

    }

}

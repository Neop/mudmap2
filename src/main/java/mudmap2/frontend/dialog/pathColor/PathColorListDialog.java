/*  MUD Map (v2) - A tool to create and organize maps for text-based games
 *  Copyright (C) 2018  Neop (email: mneop@web.de)
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
package mudmap2.frontend.dialog.pathColor;

import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import mudmap2.backend.World;
import mudmap2.frontend.dialog.ListDialog;
import mudmap2.utils.Pair;

/**
 * A dialog for creating, modifying and removing path colors
 * @author neop
 */
public class PathColorListDialog extends ListDialog {

    World world;

    LinkedList<Pair<Color, String>> entryList = new LinkedList<>();

    public PathColorListDialog(JFrame parent, World world) {
        super(parent, "Path Colors", false);
        this.world = world;
        setCellRenderer(new PathColorListCellRenderer());
    }

    @Override
    protected void create(){
        super.create();

        // Only select one entry at once
        getList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        setPreferredSize(new Dimension(250, 300));
        pack();
        setLocation(getParent().getX() + (getParent().getWidth() - getWidth()) / 2, getParent().getY() + (getParent().getHeight() - getHeight()) / 2);
    }

    @Override
    protected void updateList() {
        List selectedValuesList = getList().getSelectedValuesList();

        Set<Entry<Integer, String>> pathColorList = makeList();
        entryList.clear();

        entryList.add(new Pair<>(world.getPathColorStd(), "Cardinal color"));
        entryList.add(new Pair<>(world.getPathColorNstd(), "Non-cardinal color"));
        entryList.add(new Pair<>(world.getPathColorUnknown(), "Unknown target"));

        for(Entry<Integer, String> entry: pathColorList){
            entryList.add(new Pair<>(new Color(entry.getKey()), entry.getValue()));
        }

        getList().setListData(entryList.toArray());

        // select previously selected value(s)
        if(!selectedValuesList.isEmpty()){
            int[] indices = new int[selectedValuesList.size()];
            int indicesCnt = 0;

            ListModel model = getList().getModel();
            for(Integer i = 0; i < model.getSize(); ++i){
                if(selectedValuesList.contains(model.getElementAt(i))){
                    indices[indicesCnt++] = i;
                }
            }

            /* number of selected values does not match if places got removed
             * Remove extra entries from list
             */
            if(indicesCnt != selectedValuesList.size()){
                int[] indicesTemp = indices;
                indices = new int[indicesCnt];
                for(int i = 0; i < indicesCnt; ++i){
                    indices[i] = indicesTemp[i];
                }
            }

            getList().setSelectedIndices(indices);
        }
    }

    @Override
    protected void addEntry() {
        // TODO: remove old PathColorDialog

        PathColorDialog pathColorDialog = new PathColorDialog((JFrame) getParent());
        pathColorDialog.setVisible(true);

        if(pathColorDialog.isAccepted()){
            String description = pathColorDialog.getDescription();
            Color color = pathColorDialog.getColor();

            if(pathColorDialog.getDescription().isEmpty()){
                int result = JOptionPane.showConfirmDialog(getParent(), "No description for path color given, no entry will be added.",
                        "Path Color", JOptionPane.OK_OPTION);
            } else {
                world.setPathColor(description, color);
                updateList();
            }
            getList().repaint();
        }
    }

    @Override
    protected void removeEntry() {
        if(!getList().isSelectionEmpty()){
            switch (getList().getSelectedIndex()) {
                case 0:
                    JOptionPane.showMessageDialog(getParent(), "Can not remove default color for cardinal directions",
                            "Removing Path Color", JOptionPane.INFORMATION_MESSAGE);
                    break;
                case 1:
                    JOptionPane.showMessageDialog(getParent(), "Can not remove default color for non-cardinal directions",
                            "Removing Path Color", JOptionPane.INFORMATION_MESSAGE);
                    break;
                default:
                    Pair<Color, String> entry = (Pair<Color, String>) getList().getSelectedValue();
                    int result = JOptionPane.showConfirmDialog(this, "Remove selected entry (" + entry.second + ")? This can not be undone!", "Removing Path Color", JOptionPane.OK_CANCEL_OPTION);
                    if(result == JOptionPane.OK_OPTION){

                        entryList.remove(getList().getSelectedIndex());
                        saveList();
                        updateList();
                    }   break;
            }
            getList().repaint();
        }
    }

    @Override
    protected void modifyEntry() {
        if(!getList().isSelectionEmpty()){
            Pair<Color, String> entry = (Pair<Color, String>) getList().getSelectedValue();

            PathColorDialog pathColorDialog = new PathColorDialog((JFrame) getParent(), entry.second, entry.first);
            if(getList().getSelectedIndex() < 2){
                pathColorDialog.lockTextEntry();
            }
            pathColorDialog.setVisible(true);

            if(pathColorDialog.isAccepted()){
                String description = pathColorDialog.getDescription();
                Color color = pathColorDialog.getColor();

                if(getList().getSelectedIndex() < 2){
                    entry.first = color;
                    saveList();
                } else if(pathColorDialog.getDescription().isEmpty()) {
                    if(description.isEmpty()){
                        int result = JOptionPane.showConfirmDialog(getParent(), "No description for path color given, this entry will be removed",
                                "Path Color", JOptionPane.OK_CANCEL_OPTION);
                        if(result == JOptionPane.OK_OPTION){
                            entryList.remove(getList().getSelectedIndex());
                            saveList();
                            updateList();
                        }
                    }
                } else {
                    entry.second = description;
                    entry.first = color;
                    saveList();
                }
            }
            getList().repaint();
        }
    }

    protected void saveList(){
        // clear entries in world
        world.getPathColors().clear();

        world.setPathColorStd(entryList.get(0).first);
        world.setPathColorNstd(entryList.get(1).first);
        world.setPathColorUnknown(entryList.get(2).first);

        // start after the predefined entries
        for(int i = 3; i < entryList.size(); ++i){
            Pair<Color, String> element = entryList.get(i);
            String[] split = element.second.split("[,;]");
            for(String exit: split){
                // write new entries to world
                world.setPathColor(exit.trim(), element.first);
            }
        }
    }

    /**
     * World.getPathColors() returns a list of  exit -> color.
     * This method combines exits that share the same color by a comma separated list
     * @return
     */
    Set<Entry<Integer,String>> makeList(){
        HashMap<Integer, String> exitsByColor = new HashMap<>();

        // group exits by color
        for(Entry<String, Color> entry: world.getPathColors().entrySet()){
            Integer key = entry.getValue().getRGB();
            if(exitsByColor.containsKey(key)){
                String exits = exitsByColor.get(key);
                exitsByColor.put(key, exits + "," + entry.getKey());
            } else {
                exitsByColor.put(key, entry.getKey());
            }
        }

        return exitsByColor.entrySet();
    }

    protected class PathColorListCellRenderer extends ColoredListCellRenderer<Pair<Color, String>> {

        @Override
        protected String getText(Pair<Color, String> object) {
            return object.second;
        }

        @Override
        protected Color getColor(Pair<Color, String> object) {
            return object.first;
        }

    }

}

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
package mudmap2.frontend.dialog.placeGroup;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import mudmap2.backend.PlaceGroup;
import mudmap2.backend.World;
import mudmap2.frontend.dialog.ListDialog;
import mudmap2.utils.AlphanumComparator;

/**
 * A dialog for creating, removing and modifying PlaceGroups
 * @author neop
 */
public class PlaceGroupListDialog extends ListDialog {

    World world;

    public PlaceGroupListDialog(JFrame parent, World world) {
        super(parent, "Place Groups", false);
        this.world = world;
        setCellRenderer(new PlaceGroupListCellRenderer());
    }

    @Override
    protected void updateList(){
        List selectedValuesList = getList().getSelectedValuesList();

        ArrayList<PlaceGroup> placeGroups = world.getPlaceGroups();
        // sort by name
        Collections.sort(placeGroups, new AlphanumComparator<>());
        getList().setListData(placeGroups.toArray());

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
    protected void addEntry(){
        (new PlaceGroupDialog((JFrame) getParent(), world)).setVisible(true);
        updateList();
    }

    @Override
    protected void removeEntry(){
        int response = JOptionPane.showConfirmDialog(this, "Remove selected entries? This can not be undone!", "Place Groups", JOptionPane.WARNING_MESSAGE);

        if(response == JOptionPane.OK_OPTION){
            List selectedValuesList = getList().getSelectedValuesList();
            for(Object entry: selectedValuesList){
                PlaceGroup placeGroup = (PlaceGroup) entry;
                world.removePlaceGroup(placeGroup);
            }
            updateList();
        }
    }

    @Override
    protected void modifyEntry(){
        (new PlaceGroupDialog((JFrame) getParent(), getList().getSelectedValuesList())).setVisible(true);
        updateList();
    }

    private class PlaceGroupListCellRenderer extends ColoredListCellRenderer<PlaceGroup> {

        @Override
        protected String getText(PlaceGroup object) {
            return object.getName();
        }

        @Override
        protected Color getColor(PlaceGroup object) {
            return object.getColor();
        }

    }

}

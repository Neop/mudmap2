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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import mudmap2.backend.PlaceGroup;
import mudmap2.backend.World;
import mudmap2.frontend.dialog.ActionDialog;
import mudmap2.utils.AlphanumComparator;

/**
 * A dialog for creating, removing and modifying PlaceGroups
 * @author neop
 */
public class PlaceGroupListDialog extends ActionDialog {

    World world;

    JList list;

    public PlaceGroupListDialog(JFrame parent, World world) {
        super(parent, "Place Groups", false);
        this.world = world;
    }

    @Override
    protected void create() {
        setLayout(new BorderLayout());

        final JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        add(content, BorderLayout.CENTER);


        list = new JList();
        list.setCellRenderer(new ColoredListCellRenderer());

        final JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(250, 500));
        content.add(scrollPane, BorderLayout.CENTER);

        updateList();


        final JPanel listButtons = new JPanel();
        listButtons.setLayout(new GridLayout(1, 3));
        content.add(listButtons, BorderLayout.SOUTH);

        final JButton buttonAdd = new JButton("Add");
        final JButton buttonRemove = new JButton("Remove");
        final JButton buttonModify = new JButton("Modify");

        buttonRemove.setEnabled(false);
        buttonModify.setEnabled(false);

        listButtons.add(buttonAdd);
        listButtons.add(buttonRemove);
        listButtons.add(buttonModify);

        buttonAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addEntry();
            }
        });

        buttonRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!list.isSelectionEmpty()){
                    removeEntry();
                }
            }
        });

        buttonModify.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!list.isSelectionEmpty()){
                    modifyEntry();
                }
            }
        });


        final JButton button_ok = new JButton("Close");
        add(button_ok, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(button_ok);
        button_ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });


        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                buttonRemove.setEnabled(!list.isSelectionEmpty());
                buttonModify.setEnabled(!list.isSelectionEmpty());
            }
        });

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2 && !list.isSelectionEmpty()){
                    modifyEntry();
                }
            }
        });

        pack();
        setResizable(true);
        setLocation(getParent().getX() + (getParent().getWidth() - getWidth()) / 2, getParent().getY() + (getParent().getHeight() - getHeight()) / 2);
    }

    void updateList(){
        List selectedValuesList = list.getSelectedValuesList();

        ArrayList<PlaceGroup> placeGroups = world.getPlaceGroups();
        // sort by name
        Collections.sort(placeGroups, new AlphanumComparator<>());
        list.setListData(placeGroups.toArray());

        // select previously selected value(s)
        if(!selectedValuesList.isEmpty()){
            int[] indices = new int[selectedValuesList.size()];
            int indicesCnt = 0;

            ListModel model = list.getModel();
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

            list.setSelectedIndices(indices);
        }
    }

    void addEntry(){
        (new PlaceGroupDialog((JFrame) getParent(), world)).setVisible(true);
        updateList();
    }

    void removeEntry(){
        int response = JOptionPane.showConfirmDialog(this, "Remove selected entries? This can not be undone!", "Place Groups", JOptionPane.WARNING_MESSAGE);

        if(response == JOptionPane.OK_OPTION){
            List selectedValuesList = list.getSelectedValuesList();
            for(Object entry: selectedValuesList){
                PlaceGroup placeGroup = (PlaceGroup) entry;
                world.removePlaceGroup(placeGroup);
            }
            updateList();
        }
    }

    void modifyEntry(){
        (new PlaceGroupDialog((JFrame) getParent(), list.getSelectedValuesList())).setVisible(true);
        updateList();
    }

    private class ColoredListCellRenderer implements ListCellRenderer<PlaceGroup> {

        double luminance(Color col){
            return Math.sqrt(0.299 * col.getRed() * col.getRed()
                    + 0.587 * col.getGreen() * col.getGreen()
                    + 0.114 * col.getBlue() * col.getBlue()) / 255.0;
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends PlaceGroup> list, PlaceGroup value, int index, boolean isSelected, boolean cellHasFocus) {
            JPanel panel = new JPanel();
            JLabel label = new JLabel(value.getName());
            panel.add(label);

            Color background = value.getColor();
            Color foreground;

            // white text on dark backgrounds
            if(luminance(background) > 0.5){
                foreground = Color.BLACK;
            } else {
                foreground = Color.WHITE;
            }

            panel.setBackground(background);
            label.setForeground(foreground);

            if(isSelected){
                panel.setBorder(new Border() {
                    int borderWidth = 2;

                    @Override
                    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                        g.setColor(Color.BLACK);
                        g.fillRect(x, y, width, height);
                        g.setColor(c.getBackground());
                        g.fillRect(x + borderWidth, y + borderWidth,
                                width - 2*borderWidth, height - 2*borderWidth);
                    }

                    @Override
                    public Insets getBorderInsets(Component c) {
                        return new Insets(borderWidth, borderWidth, borderWidth, borderWidth);
                    }

                    @Override
                    public boolean isBorderOpaque() {
                        return false;
                    }
                });
            } else {
                panel.setBorder(new Border() {
                    int borderWidth = 2;

                    @Override
                    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                        g.clearRect(x, y, width, height);
                        g.setColor(c.getBackground());
                        g.fillRect(x + borderWidth, y + borderWidth,
                                width - 2*borderWidth, height - 2*borderWidth);
                    }

                    @Override
                    public Insets getBorderInsets(Component c) {
                        return new Insets(borderWidth, borderWidth, borderWidth, borderWidth);
                    }

                    @Override
                    public boolean isBorderOpaque() {
                        return true;
                    }
                });
            }

            return panel;
        }

    }

}

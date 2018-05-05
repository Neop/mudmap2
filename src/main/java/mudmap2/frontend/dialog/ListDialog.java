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
package mudmap2.frontend.dialog;

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
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A dialog for creating, removing and modifying objects
 * @author neop
 */
public abstract class ListDialog extends ActionDialog {

    JList list;
    ColoredListCellRenderer cellRenderer;

    JButton buttonAdd;
    JButton buttonRemove;
    JButton buttonModify;

    public ListDialog(JFrame parent, String title, boolean modal) {
        super(parent, title, modal);
    }

    @Override
    protected void create() {
        setLayout(new BorderLayout());

        final JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        add(content, BorderLayout.CENTER);


        list = new JList();
        list.setCellRenderer(cellRenderer);

        final JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(250, 500));
        content.add(scrollPane, BorderLayout.CENTER);

        updateList();


        final JPanel listButtons = new JPanel();
        listButtons.setLayout(new GridLayout(1, 3));
        content.add(listButtons, BorderLayout.SOUTH);

        buttonAdd = new JButton("Add");
        buttonRemove = new JButton("Remove");
        buttonModify = new JButton("Modify");

        buttonRemove.setEnabled(false);
        buttonModify.setEnabled(false);

        listButtons.add(buttonRemove);
        listButtons.add(buttonModify);
        listButtons.add(buttonAdd);

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

    public final void setCellRenderer(ColoredListCellRenderer cellRenderer) {
        this.cellRenderer = cellRenderer;
    }

    protected abstract void updateList();

    protected abstract void addEntry();
    protected abstract void removeEntry();
    protected abstract void modifyEntry();

    protected JList getList() {
        return list;
    }

    protected abstract class ColoredListCellRenderer<T> implements ListCellRenderer<T> {

        protected abstract String getText(T object);
        protected abstract Color getColor(T object);

        double luminance(Color col){
            return Math.sqrt(0.299 * col.getRed() * col.getRed()
                    + 0.587 * col.getGreen() * col.getGreen()
                    + 0.114 * col.getBlue() * col.getBlue()) / 255.0;
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected, boolean cellHasFocus) {
            JPanel panel = new JPanel();
            JLabel label = new JLabel(getText(value));
            panel.add(label);

            Color background = getColor(value);
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

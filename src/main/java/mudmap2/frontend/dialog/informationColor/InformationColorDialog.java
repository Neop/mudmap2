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
package mudmap2.frontend.dialog.informationColor;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import mudmap2.backend.InformationColor;
import mudmap2.backend.World;
import mudmap2.frontend.GUIElement.ColorChooserButton;
import mudmap2.frontend.dialog.ActionDialog;

/**
 * A dialog for creating and modifying information color
 * @author neop
 */
public class InformationColorDialog extends ActionDialog {

    World world;
    InformationColor informationColor;

    JTextField textFieldDescription;
    ColorChooserButton colorChooserButton;

    /**
     * Constructor to create a new information color
     * @param parent
     * @param world
     */
    public InformationColorDialog(JFrame parent, World world) {
        super(parent, "Info ring colors", true);
        this.world = world;
        this.informationColor = null;
    }

    /**
     * Constructor to modify an existing information color
     * @param parent
     * @param world
     * @param ic
     */
    public InformationColorDialog(JFrame parent, World world, InformationColor ic) {
        super(parent, "Colored information rings", true);
        this.world = world;
        this.informationColor = ic;
    }

    @Override
    protected void create() {
        setLayout(new BorderLayout());

        final JPanel contentPanel = new JPanel();
        add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new GridBagLayout());

        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        contentPanel.add(new JLabel("Name"), constraints);
        constraints.gridx = 1;
        constraints.gridwidth = 2;

        textFieldDescription = new JTextField();
        if(informationColor != null){
            textFieldDescription.setText(informationColor.getDescription());
        }
        contentPanel.add(textFieldDescription, constraints);
        textFieldDescription.setColumns(20);

        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy++;

        contentPanel.add(new JLabel("Color"), constraints);

        constraints.weighty = 4.0;
        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridwidth = 2;

        colorChooserButton = new ColorChooserButton(getParent());
        if(informationColor != null){
            colorChooserButton.setColor(informationColor.getColor());
        }
        contentPanel.add(colorChooserButton, constraints);


        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3));
        add(buttonPanel, BorderLayout.SOUTH);

        final JButton buttonAccept;
        if(informationColor == null){
            buttonAccept = new JButton("Add");
            buttonAccept.setToolTipText("Create a new colored information ring for places");

            buttonAccept.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    createNew();
                    dispose();
                    getParent().repaint();
                }
            });
        } else {
            buttonAccept = new JButton("Update");
            buttonAccept.setToolTipText("Change information");

            buttonAccept.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    modifyExisting();
                    dispose();
                    getParent().repaint();
                }
            });
        }

        final JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                dispose();
            }
        });

        buttonPanel.add(new JLabel());
        buttonPanel.add(buttonCancel);
        buttonPanel.add(buttonAccept);
        getRootPane().setDefaultButton(buttonAccept);


        textFieldDescription.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                buttonAccept.setEnabled(!textFieldDescription.getText().isEmpty());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                buttonAccept.setEnabled(!textFieldDescription.getText().isEmpty());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                buttonAccept.setEnabled(!textFieldDescription.getText().isEmpty());
            }
        });

        pack();
        setLocation(getParent().getX() + (getParent().getWidth() - getWidth()) / 2, getParent().getY() + (getParent().getHeight() - getHeight()) / 2);
    }

    void createNew(){
        String description = textFieldDescription.getText();
        if(!description.isEmpty()){
            informationColor = new InformationColor(description, colorChooserButton.getColor());
            world.setInformationColor(informationColor);
        }
    }

    void modifyExisting(){
        String description = textFieldDescription.getText();
        if(!description.isEmpty()){
            informationColor.setDescription(description);
            informationColor.setColor(colorChooserButton.getColor());
        }
    }

}

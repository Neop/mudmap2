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
package mudmap2.frontend.dialog.riskLevel;

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
import mudmap2.backend.RiskLevel;
import mudmap2.backend.World;
import mudmap2.frontend.GUIElement.ColorChooserButton;
import mudmap2.frontend.dialog.ActionDialog;

/**
 * A dialog for creating and modifying RiskLevels
 * @author neop
 */
public class RiskLevelDialog extends ActionDialog {

    World world;
    RiskLevel riskLevel;

    JTextField textFieldDescription;
    ColorChooserButton colorChooserButton;

    /**
     * Creates a new RiskLevel
     * @param parent
     * @param world
     */
    public RiskLevelDialog(JFrame parent, World world) {
        super(parent, "Risk Level", true);
        this.world = world;
        this.riskLevel = null;
    }

    /**
     * Modifies an existing RiskLevel
     * @param parent
     * @param world
     * @param rl
     */
    public RiskLevelDialog(JFrame parent, World world, RiskLevel rl) {
        super(parent, "Risk Level", true);
        this.world = world;
        this.riskLevel = rl;
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
        if(riskLevel != null){
            textFieldDescription.setText(riskLevel.getDescription());
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
        if(riskLevel != null){
            colorChooserButton.setColor(riskLevel.getColor());
        }
        contentPanel.add(colorChooserButton, constraints);


        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3));
        add(buttonPanel, BorderLayout.SOUTH);

        final JButton buttonAccept;
        if(riskLevel == null){
            buttonAccept = new JButton("Add");
            buttonAccept.setToolTipText("Create a new risk level");

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
            buttonAccept.setToolTipText("Change risk level");

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
            riskLevel = new RiskLevel(description, colorChooserButton.getColor());
            world.setRiskLevel(riskLevel);
        }
    }

    void modifyExisting(){
        String description = textFieldDescription.getText();
        if(!description.isEmpty()){
            riskLevel.setDescription(description);
            riskLevel.setColor(colorChooserButton.getColor());
        }
    }

}

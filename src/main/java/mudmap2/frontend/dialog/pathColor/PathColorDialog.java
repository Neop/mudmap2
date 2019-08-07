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

import java.awt.BorderLayout;
import java.awt.Color;
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
import mudmap2.frontend.GUIElement.ColorChooserButton;
import mudmap2.frontend.dialog.ActionDialog;

/**
 * A dialog for creating and modifying path colors.
 * It does not change the world object but provide result values to be saved
 * by the calling method
 * @author neop
 */
public class PathColorDialog extends ActionDialog {

    boolean lockTextEntry = false;
    boolean createNew = false;
    boolean accepted = false;

    String description;
    Color color;

    JTextField textFieldDescription;
    ColorChooserButton colorChooserButton;

    /**
     * Creates a new RiskLevel
     * @param parent
     */
    public PathColorDialog(JFrame parent){
        super(parent, "Path Color", true);
        this.description = "";
        this.color = Color.GRAY;
        createNew = true;
    }

    /**
     * Modifies an existing RiskLevel
     * @param parent
     * @param description
     * @param color
     */
    public PathColorDialog(JFrame parent, String description, Color color) {
        super(parent, "Path Color", true);
        this.description = description;
        this.color = new Color(color.getRGB());
        createNew = false;
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

        contentPanel.add(new JLabel("Exit directions"), constraints);
        constraints.gridx = 1;
        constraints.gridwidth = 2;

        textFieldDescription = new JTextField(description);
        contentPanel.add(textFieldDescription, constraints);
        textFieldDescription.setColumns(20);
        textFieldDescription.setEnabled(!lockTextEntry);

        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy++;

        contentPanel.add(new JLabel("Color"), constraints);

        constraints.weighty = 4.0;
        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridwidth = 2;

        colorChooserButton = new ColorChooserButton(getParent(), color);
        contentPanel.add(colorChooserButton, constraints);

        if(!lockTextEntry){
            constraints.gridy++;
            constraints.weightx = 2;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            contentPanel.add(new JLabel("<html>Specify all exits you like to associate with this color in the text field<br>"
                                        + "as a comma separated list. You may use '-' for one-way paths as well as<br>"
                                        + "self-defined exits.<br>"
                                        + "Example: n,se,nw,u,d"), constraints);
        }


        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3));
        add(buttonPanel, BorderLayout.SOUTH);

        final JButton buttonAccept;
        if(createNew){
            buttonAccept = new JButton("Add");
            buttonAccept.setToolTipText("Create a new path color");
        } else {
            buttonAccept = new JButton("Update");
            buttonAccept.setToolTipText("Change path color");
        }

        buttonAccept.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accept();
                dispose();
                getParent().repaint();
            }
        });

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

    public void lockTextEntry(){
        lockTextEntry = true;
        if(textFieldDescription != null){
            textFieldDescription.setEnabled(lockTextEntry);
        }
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String getDescription() {
        return description;
    }

    public Color getColor() {
        return color;
    }

    void accept(){
        accepted = true;
        description = textFieldDescription.getText();
        color = colorChooserButton.getColor();
    }

}

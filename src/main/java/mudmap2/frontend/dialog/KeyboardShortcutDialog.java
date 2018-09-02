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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * This dialog shows information about available keyboard shortcuts
 * @author neop
 */
public class KeyboardShortcutDialog extends ActionDialog {

    final String textError = "<html><body><h1>MUD Map Keyboard Shortcuts</h1><p>An error occured while reading this file.</p></body></html>";
    
    public KeyboardShortcutDialog(JFrame parent) {
        super(parent, "MUD Map Keyboard Shortcuts", false);
    }

    @Override
    protected final void create() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        setContentPane(panel);
        
        ClassLoader classLoader = getClass().getClassLoader();
        URL contentURL = classLoader.getResource("resources/keyboardShortcuts.html");
        
        JEditorPane editor;
        try {
            editor = new JEditorPane(contentURL);
        } catch (IOException ex) {
            Logger.getLogger(KeyboardShortcutDialog.class.getName()).log(Level.SEVERE, null, ex);
            
            editor = new JEditorPane();
            editor.setContentType("text/html");
            editor.setText(textError);
        }
        editor.setBackground(getParent().getBackground());
        editor.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(editor);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton button_ok = new JButton("Close");
        add(button_ok, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(button_ok);
        button_ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        setMinimumSize(new Dimension(600, 400));
        
        pack();
        setSize(new Dimension(600, 600));
        setLocation(getParent().getX() + (getParent().getWidth() - getWidth()) / 2, getParent().getY() + (getParent().getHeight() - getHeight()) / 2);
    }
    
}

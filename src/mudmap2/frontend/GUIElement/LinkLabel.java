/*  MUD Map (v2) - A tool to create and organize maps for text-based games
 *  Copyright (C) 2014  Neop (email: mneop@web.de)
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

/*  File description
 *
 *  A Label, that opens a link in a web browser, when clicked
 *
 *  inspired by http://www.java2s.com/Code/Java/Swing-Components/HyperlinkLabel.htm
 */
package mudmap2.frontend.GUIElement;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import mudmap2.Paths;

/**
 * A Label, that opens a link in a web browser, when clicked
 * @author neop
 */
public class LinkLabel extends JLabel {
    
    String text, url;
    
    /**
     * Creates a new link label with text and url
     * @param _text
     * @param _url 
     */
    public LinkLabel(String _text, String _url){
        super(_text);
        text = _text;
        url = _url;
        
        setToolTipText(url);
        
        setForeground(Color.BLUE);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Paths.openWebsite(url);
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        
        g.setColor(getForeground());
        
        Insets insets = getInsets();
        g.drawLine(insets.left, getHeight() - 1 - insets.bottom, (int) getPreferredSize().getWidth() - insets.right, getHeight() - 1 - insets.bottom);
    }
}

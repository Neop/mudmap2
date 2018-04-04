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
package mudmap2.backend.legend;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * This legend item creates a title text label
 * @author neop
 */
public class LegendEntryTitle implements LegendEntry {

    String title;

    public LegendEntryTitle(String title) {
        this.title = title;
    }

    @Override
    public int getHeight(Graphics g) {
        return g.getFontMetrics().getHeight();
    }

    @Override
    public int getWidth(Graphics g) {
        FontMetrics fm = g.getFontMetrics();
        return fm.stringWidth(title);
    }

    @Override
    public void renderGraphic(Graphics g, int sx, int sy, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Font font = graphics.getFont();
        graphics.setFont(font.deriveFont(Font.BOLD));

        FontMetrics fm = graphics.getFontMetrics();

        graphics.setColor(Color.black);
        graphics.drawString(title, 0, fm.getAscent());
        g.drawImage(image, sx, sy, null);

        graphics.dispose();
    }

}

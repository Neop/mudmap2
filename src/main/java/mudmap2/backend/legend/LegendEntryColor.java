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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * This legend entry consists of a text label and a color box
 * @author neop
 */
public class LegendEntryColor implements LegendEntry {

    final static int COLOR_WIDTH            = 30;
    final static int COLOR_HEIGHT           = 15;
    final static int COLOR_DISTANCE_MIN     = 5;

    String text;
    Color color;

    public LegendEntryColor(String text, Color color) {
        this.text = text;
        this.color = color;
    }

    @Override
    public int getHeight(Graphics g) {
        int fontHeight = g.getFontMetrics().getHeight();

        return Math.max(fontHeight, COLOR_HEIGHT);
    }

    @Override
    public int getWidth(Graphics g) {
        FontMetrics fm = g.getFontMetrics();
        int fontWidth = fm.stringWidth(text);

        return fontWidth + COLOR_DISTANCE_MIN + COLOR_WIDTH;
    }

    @Override
    public void renderGraphic(Graphics g, int sx, int sy, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        FontMetrics fm = graphics.getFontMetrics();

        graphics.setColor(Color.black);
        graphics.drawString(text, 0, fm.getAscent());

        graphics.setColor(color);
        graphics.fillRect(width - COLOR_WIDTH, height - COLOR_HEIGHT, COLOR_WIDTH, COLOR_HEIGHT);

        g.drawImage(image, sx, sy, null);

        graphics.dispose();
    }

}

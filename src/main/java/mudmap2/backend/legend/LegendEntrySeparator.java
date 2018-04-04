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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * This legend entry creates a separator line
 * @author neop
 */
public class LegendEntrySeparator implements LegendEntry {

    static final int STROKE_WIDTH        = 1;

    static final double STROKE_START     = 0.2;
    static final double STROKE_END       = 0.8;

    static final int HEIGHT_REQUEST      = 15;
    static final int WIDTH_REQUEST       = 50;

    public LegendEntrySeparator() {
    }

    @Override
    public int getHeight(Graphics g) {
        return HEIGHT_REQUEST;
    }

    @Override
    public int getWidth(Graphics g) {
        return WIDTH_REQUEST;
    }

    @Override
    public void renderGraphic(Graphics g, int sx, int sy, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();
        ((Graphics2D) graphics).setStroke(new BasicStroke(STROKE_WIDTH));
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setColor(Color.black);
        graphics.drawLine((int) (width * STROKE_START), height / 2,
                (int) (width * STROKE_END), height / 2);
        g.drawImage(image, sx, sy, null);

        graphics.dispose();
    }

}

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

import java.awt.Graphics;

/**
 * This provides interface methods for different types of legend entries
 * @author neop
 */
public interface LegendEntry {

    /**
     * This method queries the legend entries requested height
     * @param g graphics for height estimation
     * @return height
     */
    public int getHeight(Graphics g);

    /**
     * This method queries the legend entries requested width
     * @param g graphics for width estimation
     * @return width
     */
    public int getWidth(Graphics g);

    /**
     * This method renders the legend entry onto an existing graphic
     * @param g graphics to render the element onto
     * @param sx start x
     * @param sy start y
     * @param width maximum width
     * @param height maximum height
     */
    public void renderGraphic(Graphics g, int sx, int sy, int width, int height);
}

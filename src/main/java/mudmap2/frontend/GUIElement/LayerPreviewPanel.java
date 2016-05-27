/*  MUD Map (v2) - A tool to create and organize maps for text-based games
 *  Copyright (C) 2016  Neop (email: mneop@web.de)
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
package mudmap2.frontend.GUIElement;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;
import mudmap2.backend.Layer;
import mudmap2.backend.WorldCoordinate;

/**
 *
 * @author neop
 */
public class LayerPreviewPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    final static Integer MIN_TILE_SIZE = 20;

    Layer layer;
    MapPainter mappainter;

    public LayerPreviewPanel(Layer layer) {
        this.layer = layer;
        mappainter = new MapPainterDefault();
        ((MapPainterDefault) mappainter).setShowPaths(false);
    }

    private WorldCoordinate getCenter(){
        Integer tileCntX = layer.getXMax() - layer.getXMin() + 1;
        Integer tileCntY = layer.getYMax() - layer.getYMin() + 1;
        Double centerX = layer.getXMin() + (tileCntX / 2.0);
        Double centerY = layer.getYMin() + (tileCntY / 2.0) - 1;

        //return new WorldCoordinate(layer.getId(), layer.getCenterX(), layer.getCenterY());
        return new WorldCoordinate(layer.getId(), centerX, centerY);
    }

    private int getTileSize(){
        Integer tileCntX = layer.getXMax() - layer.getXMin() + 1;
        Integer tileCntY = layer.getYMax() - layer.getYMin() + 1;

        //return Math.max(MIN_TILE_SIZE, Math.min(width / tileCntX, height / tileCntY));
        return Math.min(getWidth() / tileCntX, getHeight() / tileCntY);
    }

    @Override
    public void paintComponent(Graphics g){
        //mappainter.paint(g, getTileSize(), width, height, layer, getCenter());
        mappainter.paint(g, getTileSize(), getWidth(), getHeight(), layer, getCenter());
    }

}

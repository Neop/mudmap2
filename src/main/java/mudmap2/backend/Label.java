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
package mudmap2.backend;

/**
 *
 * @author neop
 */
public class Label {

    String text;
    Double x, y;
    Layer layer;
    Double fontSize;

    public Label(String text, Double x, Double y, Layer layer, Double fontSize) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.layer = layer;
        this.fontSize = fontSize;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Layer getLayer() {
        return layer;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public Double getFontSize() {
        return fontSize;
    }

    public void setFontSize(Double fontSize) {
        this.fontSize = fontSize;
    }


}

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
 *  This class is is an subelement of the place data structure used in Layer,
 *  Places are derived from this class
 */

package mudmap2.backend;

/**
 * An element of a layer
 * @author neop
 */
public class LayerElement {    
    
    private int x, y;
    private Layer layer;
    
    /**
     * constructs a layer element
     * @param x x position
     * @param y y position
     * @param l layer
     */
    public LayerElement(int x, int y, Layer l){
        this.x = x;
        this.y = y;
        layer = l;
    }
    
    /**
     * Gets the x position
     * @return x position
     */
    public int getX(){
        return x;
    }
    
    /**
     * Gets the y position
     * @return y position
     */
    public int getY(){
        return y;
    }
    
    /**
     * Gets the layer
     * @return layer
     */
    public Layer getLayer(){
        return layer;
    }
    
    /**
     * Sets the layer
     * @param layer layer
     */
    public void setLayer(Layer layer){
        this.layer = layer;
    }
    
    /**
     * Sets the position
     * @param x x position
     * @param y y position
     * @param l layer
     */
    public void setPosition(int x, int y, Layer l){
        this.x = x;
        this.y = y;
        layer = l;
    }
    
}
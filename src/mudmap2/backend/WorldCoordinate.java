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
 *  This class describes a position in the world
 */
package mudmap2.backend;

/**
 * This class describes a position in the world
 * @author neop
 */
public class WorldCoordinate {
    int layer;
    double x, y;

    /**
     * describes a position in the world
     * @param _layer current layer
     * @param _x x coordinate
     * @param _y y coordinate
     */
    public WorldCoordinate(int _layer, double _x, double _y){
        layer = _layer;
        x = _x;
        y = _y;
    }

    /**
     * Gets the layer
     * @return layer
     */
    public int getLayer(){
        return layer;
    }

    /**
     * Sets the layer
     * @param l 
     */
    public void setLayer(int l){
        layer = l;
    }
    
    /**
     * Gets the x coordinate
     * @return x coordinate
     */
    public double getX(){
        return x;
    }

    /**
     * Gets the y coordinate
     * @return y coordinate
     */
    public double getY(){
        return y;
    }

    /**
     * Sets the x coordinate
     * @param x new x coordinate
     */
    public void setX(double x){
        this.x = x;
    }

    /**
     * Sets the y coordinate
     * @param y new y coordinate
     */
    public void setY(double y){
        this.y = y;
    }

    /**
     * Moves the map
     * @param dx x movement
     * @param dy y movement
     */
    public void move(double dx, double dy){
        x += dx;
        y += dy;
    }

    /**
     * Gets the position data in String format
     * @return 
     */
    @Override
    public String toString(){
        return layer + " " + x + " " + y;
    }

    /**
     * Gets the position data in String format for meta files
     * @return 
     */
    public String getMetaString(){
        return layer + " " + -x + " " + y;
    }
    
    /**
     * creates a new instance of this world coordinate
     * @return world coordinate
     */
    @Override
    public WorldCoordinate clone(){
        return new WorldCoordinate(layer, x, y);
    }
}
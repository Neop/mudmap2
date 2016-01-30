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
 *  Areas can be assigned to places to loosely group them by a common area name
 *  and color
 */

package mudmap2.backend;

import java.awt.Color;

/**
 * An area is a collection of places, marked by a color
 * 
 * @author neop
 */
public class Area implements Comparable<Area> {
    // next id to be assigned
    static int next_id = 0;
    
    int id;
    String name;
    Color color;
    
    /**
     * Constructs a new Area
     * @param name Name of the area
     * @param color
     */
    public Area(String name, Color color) {
        id = next_id++;
        this.name = name;
        this.color = color;
    }

    /**
     * Constructs a new Area
     * @param id area id
     * @param name Name of the area
     */
    public Area(int id, String name) {
        this.id = id;
        if(this.id >= next_id) next_id = this.id + 1;
        this.name = name;
        color = new Color(0, 0, 0);
    }
    
    public int getId(){
        return id;
    }
    
    /**
     * Gets the are name
     * 
     * @return area name
     */
    public String getName(){
        return name;
    }
    
    /**
     * Sets a new name
     * @param _name new area name
     */
    public void setName(String _name){
        name = _name;
    }
    
    /**
     * Gets the area color
     * 
     * @return area color
     */
    public Color getColor(){
        return color;
    }
    
    /**
     * Sets a new area color
     * @param color new area color
     */
    public void setColor(Color color){
        this.color = color;
    }
    
    /**
     * Gets the name of an area
     * @return name
     */
    @Override
    public String toString(){
        return name;
    }

    @Override
    public int compareTo(Area arg0){
        return name.compareTo(arg0.getName());
    }
}

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
     * @param id area id
     * @param _name Name of the area
     * @param col 
     */
    public Area(String _name, Color col) {
        id = next_id++;
        name = _name;
        color = col;
    }

    /**
     * Constructs a new Area
     * @param id area id
     * @param _name Name of the area
     */
    Area(int _id, String _name) {
        id = _id;
        if(id >= next_id) next_id = id + 1;
        name = _name;
        color = new Color(0, 0, 0);
    }
    
    public int get_id(){
        return id;
    }
    
    /**
     * Gets the are name
     * 
     * @return area name
     */
    public String get_name(){
        return name;
    }
    
    /**
     * Sets a new name
     * @param _name new area name
     */
    public void set_name(String _name){
        name = _name;
    }
    
    /**
     * Gets the area color
     * 
     * @return area color
     */
    public Color get_color(){
        return color;
    }
    
    /**
     * Sets a new area color
     * 
     * @param col new area color
     */
    public void set_color(Color col){
        color = col;
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
    public int compareTo(Area arg0) {
        return -arg0.get_name().compareTo(name);
    }
}

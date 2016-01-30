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
 *  This class describes a risk level which describes how dangerous a place is
 *  The risk level is shown on the map as a colored border around the place
 *  (not to be confused with the area, which also is drawn as a colored border)
 */

package mudmap2.backend;

import java.awt.Color;

/**
 * Describes a risk level (which are shown as the colored border of each place)
 * @author neop
 */
public class RiskLevel {
    // next id to be assigned
    static int next_id = 0;
    
    int id;
    public String description;
    public Color color;
    
    /**
     * Constructs the risk level
     * @param id identification number, unique in a world
     * @param desc description
     * @param color color that represents the risk level
     */
    public RiskLevel(int id, String desc, Color color){
        this.id = id;
        if(id >= next_id) next_id = id + 1;
        description = desc;
        this.color = color;
    }

    /**
     * Constructs the risk level
     * @param desc description
     * @param color color that represents the risk level
     */
    public RiskLevel(String desc, Color color){
        id = next_id++;
        description = desc;
        this.color = color;
    }
    
    /**
     * Gets the risk level id
     * @return id
     */
    public int getId(){
        return id;
    }
    
    /**
     * Gets the color
     * @return color of the risk level
     */
    public Color getColor(){
        return color;
    }
    
    /**
     * Sets the color
     * @param c new color
     */
    public void setColor(Color c){
        color = c;
    }
    
    /**
     * Gets the description of the risk level
     * @return description
     */
    public String getDescription(){
        return toString();
    }
    
    /**
     * Sets the description
     * @param desc new description
     */
    public void setDescription(String desc){
        description = desc;
    }
    
    /**
     * Gets the description of the risk level
     * @return description
     */
    @Override
    public String toString(){
        return description;
    }
}

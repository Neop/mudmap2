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
 *  This class describes an information that is represented as a colored ring
 *  around place tiles. This can be used to describe the danger of a place or
 *  other information.
 *  Not to be confused with the PlaceGroup, which also is drawn as a colored
 *  border/ring.
 */

package mudmap2.backend;

import java.awt.Color;

/**
 * Describes a information color / colored ring on place tiles
 * @author neop
 */
public class InformationColor {
    // next id to be assigned
    static int nextID = 0;

    int id;
    public String description;
    public Color color;

    /**
     * Constructor
     * @param id identification number, unique in a world
     * @param desc description
     * @param color color that represents the risk level
     */
    public InformationColor(int id, String desc, Color color){
        this.id = id;
        if(id >= nextID) nextID = id + 1;
        description = desc;
        this.color = color;
    }

    /**
     * Constructor
     * @param desc description
     * @param color color that represents the risk level
     */
    public InformationColor(String desc, Color color){
        id = nextID++;
        description = desc;
        this.color = color;
    }

    /**
     * Get id
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
     * Gets the description
     * @return description
     */
    public String getDescription(){
        return description;
    }

    /**
     * Sets the description
     * @param desc new description
     */
    public void setDescription(String desc){
        description = desc;
    }

    /**
     * Gets the description
     * @return description
     */
    @Override
    public String toString(){
        if(description == null){
            return "";
        } else {
            return description;
        }
    }
}

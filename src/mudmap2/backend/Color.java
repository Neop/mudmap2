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
 *  This class describes a color
 */

package mudmap2.backend;

/**
 * A color
 * 
 * @author neop
 */
public class Color extends java.awt.Color {
    
    public Color(){
        super(0);
    }
    
    public Color(java.awt.Color color){
        super(color.getRGB());
    }
    
    public Color(int r, int g, int b){
        super(Math.min(255, Math.max(0, r)), Math.min(255, Math.max(0, g)), Math.min(255, Math.max(0, b)));
    }
    
    @Override
    public String toString(){
        return "" + getRed() + " " + getGreen() + " " + getBlue();
    }
}

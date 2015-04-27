/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mudmap2.frontend;

import java.awt.Graphics;
import java.util.HashSet;
import mudmap2.backend.Layer;
import mudmap2.backend.Place;
import mudmap2.backend.WorldCoordinate;

/**
 *
 * @author Neop
 */
public interface MapPainter {
    
    /**
     * Sets a place group
     * @param group 
     * @param shift_start 
     * @param shift_end 
     */
    public void set_place_group(HashSet<Place> group, WorldCoordinate shift_start, WorldCoordinate shift_end);
    
    /**
     * Sets the coordinate of the seleted place
     * @param x
     * @param y 
     */
    public void set_place_selection(int x, int y);
    
    /**
     * Sets whether the place selection is enabled
     * @param b 
     */
    public void set_place_selection_enabled(boolean b);

    /**
     * Paints layer to the graphic g
     * @param g
     * @param layer
     * @param tile_size
     * @param graphics_width
     * @param graphics_height 
     * @param cur_pos 
     */
    public void paint(Graphics g, int tile_size, double graphics_width, double graphics_height, Layer layer, WorldCoordinate cur_pos);
}

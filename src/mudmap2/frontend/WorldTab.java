package mudmap2.frontend;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import mudmap2.backend.Layer;
import mudmap2.backend.Place;
import mudmap2.backend.World;
import mudmap2.backend.WorldManager;

/**
 *
 * @author neop
 */
class WorldTab extends JPanel {
    
    String world_name;
    World world;
    
    final float risk_level_stroke_width = 5;
    Color tile_center_color;
    
    WorldPanel worldpane;
    
    JToolBar toolbar;
    
    // currently shown position
    int layer_id;
    double pos_x, pos_y;
    
    /**
     * Describes the tile size / zoom
     */
    enum TileSize {
        SMALL, MEDIUM, BIG
    };
    TileSize tile_size;
    
    /**
     * Constructs the world tab, opens the world if necessary
     * @param _world_name name of the world
     */
    public WorldTab(String _world_name){
        layer_id = 0;
        pos_x = 0; pos_y = 0;
        tile_size = TileSize.MEDIUM;
        
        world_name = _world_name;
        
        // open / get the world
        world = WorldManager.get_world(WorldManager.get_world_file(world_name));
        
        // TODO: load world meta file
        // simulating:
        layer_id = 4705;
        pos_x = -34;
        pos_y = 14;
        
        tile_center_color = new Color(207, 190, 134);
        
        setLayout(new BorderLayout());
        
        toolbar = new JToolBar();
        add(toolbar, BorderLayout.NORTH);
        
        worldpane = new WorldPanel(this);
        add(worldpane, BorderLayout.CENTER);
    }

    /**
     * Gets the world name
     * @return world name
     */
    public String get_world_name() {
        return world_name;
    }
    
    private static class WorldPanel extends JPanel {
        
        final int tile_small_size = 60;
        final int tile_small_border_area = 5;
        final int tile_small_border_risk_level = 5;
        
        final int tile_medium_size = 120;
        final int tile_medium_border_area = 10;
        final int tile_medium_border_risk_level = 10;
        
        final int tile_big_size = 180;
        final int tile_big_border_area = 10;
        final int tile_big_border_risk_level = 10;
        
        WorldTab parent;

        /**
         * Constructs a world panel
         * @param _parent parent world tab
         */
        public WorldPanel(WorldTab _parent) {
            parent = _parent;
        }
        
        /**
         * Gets the current tile size
         * @return tile size
         */
        private int get_tile_size(){
            switch(parent.tile_size){
                case SMALL: return tile_small_size;
                case MEDIUM: return tile_medium_size;
                case BIG: return tile_big_size;
            }
            return tile_medium_size;
        }
        
        /**
         * Gets the current tile border area size
         * @return area border width
         */
        private int get_tile_border_area(){
            switch(parent.tile_size){
                case SMALL: return tile_small_border_area;
                case MEDIUM: return tile_medium_border_area;
                case BIG: return tile_big_border_area;
            }
            return tile_medium_border_area;
        }
        
        /**
         * Gets the current tile risk level border size
         * @return risk level border width
         */
        private int get_tile_border_risk_level(){
            switch(parent.tile_size){
                case SMALL: return tile_small_border_risk_level;
                case MEDIUM: return tile_medium_border_risk_level;
                case BIG: return tile_big_border_risk_level;
            }
            return tile_medium_size;
        }
        
        /**
         * Draws the map to the screen
         * @param g 
         */
        @Override
        public void paintComponent(Graphics g){
            /// TODO: check if layer exists
            Layer layer = parent.world.get_layer(parent.layer_id);
            
            // screen size
            double screen_width = g.getClipBounds().getWidth();
            double screen_height = g.getClipBounds().getHeight();
            
            // screen center in world coordinates
            double screen_center_x = ((double) screen_width / get_tile_size()) / 2; // note: wdtwd2
            double screen_center_y = ((double) screen_height / get_tile_size()) / 2;
            
            int place_x_offset = (int) (Math.round((float) parent.pos_x) - Math.floor(screen_center_x));
            int place_y_offset = (int) (Math.round((float) parent.pos_y) - Math.floor(screen_center_y));
            
            System.out.println("tco" + place_x_offset + " " + place_y_offset);
            
            // draw the tiles / places
            for(int tile_x = -1; tile_x < screen_width / get_tile_size(); ++tile_x){
                for(int tile_y = -1; tile_y < screen_height / get_tile_size(); ++tile_y){
                    
                    // place position on the map
                    int place_x = tile_x + place_x_offset;
                    int place_y = (int)(screen_height / get_tile_size()) - tile_y + place_y_offset;
                    
                    try { // layer.get throws an exception, if the place doesn't exist
                        
                        // TODO: warum werden  keine Orte gefunden?
                        // werden in layer eingespeichert...
                        
                        Place cur_place = (Place) layer.get(place_x, place_y);
                        
                        System.out.println(cur_place.get_name() + " - ");
                        
                        // place position in pixel on the screen
                        // TODO: extract constant calculation from for loop
                        int place_x_px = (int)(tile_x + screen_center_x + parent.pos_x - Math.floor(screen_center_x) - Math.floor(parent.pos_x)) * get_tile_size();
                        int place_y_px = (int)(tile_y + screen_center_y + parent.pos_y - Math.floor(screen_center_y) - Math.floor(parent.pos_y)) * get_tile_size();
                        
                        // TODO: draw path lines here
                        
                        // draw area color
                        g.setColor(cur_place.get_area().get_color().get_awt_color());
                        g.fillRect(place_x_px, place_y_px, get_tile_size(), get_tile_size());
                        
                        // draw tile center color
                        g.setColor(parent.tile_center_color);
                        g.fillRect(place_x_px + get_tile_border_area(), place_y_px + get_tile_border_area(), get_tile_size() - 2 * get_tile_border_area(), get_tile_size() - 2 * get_tile_border_area());
                        
                        // TODO: draw risk level border
                        g.setColor(parent.world.get_risk_level(cur_place.get_risk_lvl()).get_color().get_awt_color());
                        ((Graphics2D)g).setStroke(new BasicStroke(parent.risk_level_stroke_width));
                        g.drawRect(place_x_px + get_tile_border_area(), place_y_px + get_tile_border_area(), get_tile_size() - 2 * get_tile_border_area(), get_tile_size() - 2 * get_tile_border_area());
                        
                        // TODO: draw eits
                        
                        // TODO: if not small tiles, draw text
                        
                        // TODO: draw flags
                        
                    } catch (RuntimeException e) {
                        //System.out.println(e); // only for debug purposes, exceptions are normal
                    }
                    
                }
            }
            
            
        }
        
    }
}

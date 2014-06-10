package mudmap2.frontend;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import mudmap2.backend.ExitDirection;
import mudmap2.backend.Layer;
import mudmap2.backend.Layer.PlaceNotFoundException;
import mudmap2.backend.Path;
import mudmap2.backend.Place;
import mudmap2.backend.World;
import mudmap2.backend.WorldManager;

/**
 * A tab in the main window that displays a world
 * 
 * @author neop
 */
class WorldTab extends JPanel {
    
    String world_name;
    World world;
    
    Color tile_center_color;
    
    WorldPanel worldpanel;
    JToolBar toolbar;
    
    // currently shown position
    Deque<WorldCoordinate> positions;
    // max amount of elements in the list
    final int history_max_length = 25;
    
    // true, if the mouse is in the panel, for relative motion calculation
    boolean mouse_in_panel;
    // previous position of the mouse
    int mouse_x_previous, mouse_y_previous;
    
    // the position of the selected place (selected by mouse or keyboard)
    boolean show_place_selection;
    int place_selected_x, place_selected_y;
    
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
        positions = new LinkedList<WorldCoordinate>() {};
        tile_size = TileSize.MEDIUM;
        
        world_name = _world_name;
        
        tile_center_color = new Color(207, 190, 134);
        
        setLayout(new BorderLayout());
        
        toolbar = new JToolBar();
        add(toolbar, BorderLayout.NORTH);
        
        worldpanel = new WorldPanel(this);
        add(worldpanel, BorderLayout.CENTER);
        worldpanel.addMouseListener(worldpanel.new TabMouseListener());
        worldpanel.addMouseMotionListener(worldpanel.new TabMouseMotionListener());
        worldpanel.addKeyListener(worldpanel.new TabKeyListener());
        
        mouse_in_panel = false;
        mouse_x_previous = mouse_y_previous = 0;
        
        // open / get the world
        world = WorldManager.get_world(WorldManager.get_world_file(world_name));
        goto_home();
        load_meta();
        
        show_place_selection = true; // TODO: default false, read state from file
        place_selected_x = (int) Math.round(get_cur_position().get_x());
        place_selected_y = (int) Math.round(get_cur_position().get_y());
    }

    /**
     * Gets the world name
     * @return world name
     */
    public String get_world_name() {
        return world_name;
    }
    
    /**
     * Saves the changes in the world
     */
    public void save(){
        write_meta();
        world.write_world();
    }
    
    /**
     * Gets the currently shown position
     * @return current position
     */
    private WorldCoordinate get_cur_position(){
        return positions.getFirst();
    }
    
    /**
     * Pushes a new position on the position stack
     * @param pos new position
     */
    public void push_position(WorldCoordinate pos){
        positions.push(pos);
        while(positions.size() > history_max_length) positions.removeLast();
        redraw();
    }
    
    /**
     * Removes the first position from the position stack,
     * go to home position if the stack is empty
     */
    public void pop_position(){
        if(positions.size() > 0) positions.removeFirst();
        if(positions.size() == 0) goto_home();
        redraw();
    }
    
    /**
     * Go to the home position
     */
    private void goto_home(){
        push_position(new WorldCoordinate(world.get_home_layer(), world.get_home_x(), world.get_home_y()));
    }
    
    /**
     * Redraws the window / tab
     */
    public void redraw(){
        worldpanel.repaint();
        // TODO: implement this
    }
    
    /**
     * Loads the world meta data file
     * this file describes the coordinates of the last shown positions
     */
    private void load_meta(){
        String file = world.get_file() + "_meta";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            
            String line;
            int layer_id = -1;
            double pos_x = 0, pos_y = 0;
            
            try {
                while((line = reader.readLine()) != null){
                    line = line.trim();

                    if(line.startsWith("//") || line.startsWith("#")) continue;
                    else if(line.startsWith("lp")){ // last position
                        String[] tmp = line.split(" ");
                        layer_id = Integer.parseInt(tmp[1]);
                        // the x coordinate has to be negated for backward compatibility to mudmap 1.x
                        pos_x = -Double.parseDouble(tmp[2]);
                        pos_y = Double.parseDouble(tmp[3]);
                        
                        
                    } else if(line.startsWith("pcv")){ // previously shown places
                        String[] tmp = line.split(" ");
                        int tmp_layer_id = Integer.parseInt(tmp[1]);
                        // the x coordinate has to be negated for backward compatibility to mudmap 1.x
                        double tmp_pos_x = -Double.parseDouble(tmp[2]);
                        double tmp_pos_y = Double.parseDouble(tmp[3]);
                        push_position(new WorldCoordinate(tmp_layer_id, tmp_pos_x, tmp_pos_y));
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            push_position(new WorldCoordinate(layer_id, pos_x, pos_y));
            
        } catch (FileNotFoundException ex) {
            System.out.println("Couldn't open available worlds file \"" + file + "\", file not found");
            Logger.getLogger(WorldManager.class.getName()).log(Level.INFO, null, ex);
        }
    }
    
    /**
     * Saves the world meta file
     */
    private void write_meta(){
        // TODO: implement this
    }
    
    private static class WorldPanel extends JPanel {
        
        final float risk_level_stroke_width = 3;
        
        final float tile_selection_stroke_width = 3;
        final java.awt.Color tile_selection_color = new java.awt.Color(255, 0, 0);
        
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
         * Calculates the offset of the exit visualization (dot/circle) to the
         * upper left corner of a tile
         * @param dir eit direction
         * @param x_offset reference to the x offset
         * @param y_offset reference to the y offset
         * @return false if the dot/circle doesn't have to be drawn
         */
        private boolean get_exit_offset(ExitDirection dir, Integer x_offset, Integer y_offset){
            if(dir.get_abbreviation().equals("n")){ // north
                x_offset = get_tile_size() / 2;
                y_offset = get_tile_border_risk_level();
            } else if(dir.get_abbreviation().equals("ne")){ // north-east
                x_offset = get_tile_border_risk_level();
                y_offset = get_tile_size() - get_tile_border_risk_level();
            } else if(dir.get_abbreviation().equals("e")){ // east
                x_offset = get_tile_size() / 2;
                y_offset = get_tile_border_risk_level();
            } else if(dir.get_abbreviation().equals("se")){ // south-east
                x_offset = y_offset = get_tile_size() - get_tile_border_risk_level();
            } else if(dir.get_abbreviation().equals("s")){ // south
                x_offset = get_tile_size() / 2;
                y_offset = get_tile_size() - get_tile_border_risk_level();
            } else if(dir.get_abbreviation().equals("sw")){ // south-west
                x_offset = get_tile_size() - get_tile_border_risk_level();
                y_offset = get_tile_border_risk_level();
            } else if(dir.get_abbreviation().equals("w")){ // west
                x_offset = get_tile_size() / 2;
                y_offset = get_tile_border_risk_level();
            } else if(dir.get_abbreviation().equals("nw")){ // north-west
                x_offset = get_tile_border_risk_level();
                y_offset = get_tile_border_risk_level();
            } else return false;
            return true;
        }
        
        /**
         * fits the string to max_width in px, cuts it at whitespaces, if possible
         * @param str string to be fitted
         * @param fm fontmetrics
         * @param max_length maximum length of the string in pixel
         * * @param max_lines maximum number of lines
         * @return a list of strings
         */
        private Deque<String> fit_line_width(String str, FontMetrics fm, int max_length, int max_lines){
            Deque<String> ret;
            if(fm.stringWidth(str) <= max_length){ // string isn't too long, return it
                ret = new LinkedList<String>();
                ret.add(str);
            } else { // string is too long
                // roughly fit the string
                int strlen = Math.min(str.length(), max_length / fm.charWidth('.'));

                // find last ' ' before max_length, if there is no ' ' cut the
                // string at max_length 
                while(fm.stringWidth(str.substring(0, strlen)) > max_length){
                    int whitespace = str.substring(0, strlen).lastIndexOf(' ');
                    if(whitespace != -1){
                        strlen = whitespace;
                    } else {
                        int lenpx = fm.stringWidth(str.substring(0, strlen / 2));
                        while(lenpx > max_length){
                            strlen /= 2;
                            lenpx = fm.stringWidth(str.substring(0, strlen));
                            if(lenpx < max_length) strlen *= 1.5;
                        } 
                        break;
                    }
                }
                
                // cut the next part and return it, abbreviate the string if the max line number is reached
                if(max_lines > 0){
                    ret = fit_line_width(str.substring(strlen), fm, max_length, max_lines - 1);
                    ret.addFirst(str.substring(0, strlen));
                } else {
                    ret = new LinkedList<String>();
                    ret.add(str.substring(0, strlen - 3) + "...");
                }
            }
            return ret;
        }
        
        /**
         * Draws the map to the screen
         * @param g 
         */
        @Override
        public void paintComponent(Graphics g){
            /// TODO: check if layer exists
            WorldCoordinate cur_pos = parent.get_cur_position();
            Layer layer = parent.world.get_layer(cur_pos.get_layer());
            
            // screen size
            double screen_width = g.getClipBounds().getWidth();
            double screen_height = g.getClipBounds().getHeight();
            
            // screen center in world coordinates
            double screen_center_x = ((double) screen_width / get_tile_size()) / 2; // note: wdtwd2
            double screen_center_y = ((double) screen_height / get_tile_size()) / 2;
            
            int place_x_offset = (int) (Math.round((float) cur_pos.get_x()) - Math.floor(screen_center_x));
            int place_y_offset = (int) (Math.round((float) cur_pos.get_y()) - Math.floor(screen_center_y));
            
            // clear screen
            g.clearRect(0, 0, (int) screen_width + 1, (int) screen_height + 1);
            
            // draw the tiles / places
            for(int tile_x = -1; tile_x < screen_width / get_tile_size() + 1; ++tile_x){
                for(int tile_y = -1; tile_y < screen_height / get_tile_size() + 1; ++tile_y){
                    
                    // place position on the map
                    int place_x = tile_x + place_x_offset;
                    int place_y = (int)(screen_height / get_tile_size()) - tile_y + place_y_offset;
                    
                    try { // layer.get throws an exception, if the place doesn't exist
                        Place cur_place = (Place) layer.get(place_x, place_y);
                        
                        // place position in pixel on the screen
                        // TODO: extract constant calculation from for loop (eg get_tile_size())
                        int place_x_px = (int)((tile_x - screen_center_x - cur_pos.get_x() + Math.round(screen_center_x) + Math.round(cur_pos.get_x())) * get_tile_size());
                        int place_y_px = (int)((tile_y + screen_center_y + cur_pos.get_y() - Math.round(screen_center_y) - Math.round(cur_pos.get_y())) * get_tile_size());
                    
                        // TODO: draw path lines here
                        
                        // draw area color
                        g.setColor(cur_place.get_area().get_color().get_awt_color());
                        g.fillRect(place_x_px, place_y_px, get_tile_size(), get_tile_size());
                        
                        // draw tile center color
                        g.setColor(parent.tile_center_color);
                        g.fillRect(place_x_px + get_tile_border_area(), place_y_px + get_tile_border_area(), get_tile_size() - 2 * get_tile_border_area(), get_tile_size() - 2 * get_tile_border_area());
                        
                        // TODO: draw risk level border
                        g.setColor(parent.world.get_risk_level(cur_place.get_risk_lvl()).get_color().get_awt_color());
                        ((Graphics2D)g).setStroke(new BasicStroke(risk_level_stroke_width));
                        g.drawRect(place_x_px + get_tile_border_area(), place_y_px + get_tile_border_area(), get_tile_size() - 2 * get_tile_border_area(), get_tile_size() - 2 * get_tile_border_area());
                        
                        // TODO: draw exits
                        g.setColor(parent.world.get_path_color().get_awt_color());
                        Integer exit_x_offset = new Integer(0), exit_y_offset = new Integer(0);
                        //System.out.println(cur_place.get_paths().size() + " exits");
                        for(Path p: cur_place.get_paths()){
                            if(get_exit_offset(p.get_exit(cur_place), exit_x_offset, exit_y_offset)){
                                //System.out.println("Exit " + exit_x_offset + " " + exit_y_offset);
                            }
                        }
                        // TODO: implement paths in world loader and draw the exits
                        // funktioniert die Werterückgabe?
                        
                        // draw text, if not in small tiles mode
                        if(parent.tile_size != TileSize.SMALL){
                            g.setColor(Color.BLACK);
                            FontMetrics fm = g.getFontMetrics(); // TODO: move constant expression out of the loop (this and part of next line)
                            // fit the string into the tile
                            Deque<String> line = fit_line_width(cur_place.get_name(), fm, (int) (get_tile_size() - 2 * get_tile_border_risk_level() - risk_level_stroke_width), (int) Math.floor(get_tile_size() / fm.getHeight()) - 1);
                            int line_num = 0;
                            for(String str: line){
                                g.drawString(str, place_x_px + get_tile_border_risk_level() + (int) Math.ceil(risk_level_stroke_width), place_y_px + get_tile_border_risk_level() + fm.getHeight() * (1 + line_num));
                                line_num++;
                            }                            
                        }
                        
                        // TODO: draw flags
                        
                    } catch (RuntimeException e) {
                        System.out.println(e);
                    } catch (PlaceNotFoundException e){ // these exceptions are normal
                    }
                    
                    // draw cursor / place selection
                    if(parent.show_place_selection && place_x == parent.place_selected_x && place_y == parent.place_selected_y){
                        int place_x_px = (int)((tile_x - screen_center_x - cur_pos.get_x() + Math.round(screen_center_x) + Math.round(cur_pos.get_x())) * get_tile_size());
                        int place_y_px = (int)((tile_y + screen_center_y + cur_pos.get_y() - Math.round(screen_center_y) - Math.round(cur_pos.get_y())) * get_tile_size());
                        
                        g.setColor(tile_selection_color);
                        ((Graphics2D)g).setStroke(new BasicStroke(tile_selection_stroke_width));
                        
                        g.drawLine((int) (place_x_px + tile_selection_stroke_width), (int) (place_y_px + tile_selection_stroke_width), (int) (place_x_px + tile_selection_stroke_width), (int) (place_y_px + tile_selection_stroke_width + get_tile_size() / 4));
                        g.drawLine((int) (place_x_px + tile_selection_stroke_width), (int) (place_y_px + tile_selection_stroke_width), (int) (place_x_px + tile_selection_stroke_width + get_tile_size() / 4), (int) (place_y_px + tile_selection_stroke_width));
                        
                        g.drawLine((int) (place_x_px - tile_selection_stroke_width + get_tile_size()), (int) (place_y_px + tile_selection_stroke_width), (int) (place_x_px - tile_selection_stroke_width + get_tile_size()), (int) (place_y_px + tile_selection_stroke_width + get_tile_size() / 4));
                        g.drawLine((int) (place_x_px - tile_selection_stroke_width + get_tile_size()), (int) (place_y_px + tile_selection_stroke_width), (int) (place_x_px - tile_selection_stroke_width + get_tile_size() * 3 / 4), (int) (place_y_px + tile_selection_stroke_width));
                        
                        g.drawLine((int) (place_x_px + tile_selection_stroke_width), (int) (place_y_px - tile_selection_stroke_width + get_tile_size()), (int) (place_x_px + tile_selection_stroke_width), (int) (place_y_px - tile_selection_stroke_width + get_tile_size() * 3 / 4));
                        g.drawLine((int) (place_x_px + tile_selection_stroke_width), (int) (place_y_px - tile_selection_stroke_width + get_tile_size()), (int) (place_x_px + tile_selection_stroke_width + get_tile_size()  / 4), (int) (place_y_px - tile_selection_stroke_width + get_tile_size()));                         
                        
                        g.drawLine((int) (place_x_px - tile_selection_stroke_width + get_tile_size()), (int) (place_y_px - tile_selection_stroke_width + get_tile_size()), (int) (place_x_px - tile_selection_stroke_width + get_tile_size()), (int) (place_y_px - tile_selection_stroke_width + get_tile_size() * 3 / 4));
                        g.drawLine((int) (place_x_px - tile_selection_stroke_width + get_tile_size()), (int) (place_y_px - tile_selection_stroke_width + get_tile_size()), (int) (place_x_px - tile_selection_stroke_width + get_tile_size() * 3 / 4), (int) (place_y_px - tile_selection_stroke_width + get_tile_size()));
                    }
                    
                }
            }
            
        }
        
        public class TabMouseListener implements MouseListener {

            @Override
            public void mouseClicked(MouseEvent arg0) {
            }

            @Override
            public void mousePressed(MouseEvent arg0) {}

            @Override
            public void mouseReleased(MouseEvent arg0) {}

            @Override
            public void mouseEntered(MouseEvent arg0) {
                parent.mouse_in_panel = true;
                parent.mouse_x_previous = arg0.getX();
                parent.mouse_y_previous = arg0.getY();
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                parent.mouse_in_panel = false;
            }
            
        }
        
        public class TabMouseMotionListener implements MouseMotionListener {

            @Override
            public void mouseDragged(MouseEvent arg0) {
                if(parent.mouse_in_panel){
                    double dx = (double) (arg0.getX() - parent.mouse_x_previous) / get_tile_size();
                    double dy = (double) (arg0.getY() - parent.mouse_y_previous) / get_tile_size();
                    parent.get_cur_position().move(-dx , dy);
                }
                parent.mouse_x_previous = arg0.getX();
                parent.mouse_y_previous = arg0.getY();
            }

            @Override
            public void mouseMoved(MouseEvent arg0) {
                parent.mouse_x_previous = arg0.getX();
                parent.mouse_y_previous = arg0.getY();
            }
            
        }
        
        public class TabKeyListener implements KeyListener {

            @Override
            public void keyTyped(KeyEvent arg0) {
                System.out.println("Dcdvdded");
            }

            @Override
            public void keyPressed(KeyEvent arg0) {
                System.out.println("dsvdv");
            }

            @Override
            public void keyReleased(KeyEvent arg0) {}
            
        }
        
    }
    
    private class WorldCoordinate {
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
        public int get_layer(){
            return layer;
        }
        
        /**
         * Gets the x coordinate
         * @return x coordinate
         */
        public double get_x(){
            return x;
        }
        
        /**
         * Gets the y coordinate
         * @return y coordinate
         */
        public double get_y(){
            return y;
        }
        
        /**
         * Moves the map
         * @param dx x movement
         * @param dy y movement
         */
        public void move(double dx, double dy){
            x += dx;
            y += dy;
            redraw();
        }
    }
}

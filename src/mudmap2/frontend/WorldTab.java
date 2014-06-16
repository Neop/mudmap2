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
 *  This class displays a world and the GUI elements that belong to it. It also
 *  processes keyboard commands. The class is supposed to be a tab in Mainwindow.
 *  It reads and writes the world meta (*_meta) files
 */

package mudmap2.frontend;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mudmap2.Pair;
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
    
    static boolean show_paths = true;
    static boolean show_paths_curved = true;
    
    JFrame parent;
    WorldPanel worldpanel;
    JToolBar toolbar;
    JSlider slider_zoom;
    JPanel panel_south;
    JLabel label_infobar;
    
    // currently shown position
    Deque<WorldCoordinate> positions;
    // max amount of elements in the list
    static final int history_max_length = 25;
    
    // true, if the mouse is in the panel, for relative motion calculation
    boolean mouse_in_panel;
    // previous position of the mouse
    int mouse_x_previous, mouse_y_previous;
    
    // the position of the selected place (selected by mouse or keyboard)
    boolean place_selection_enabled;
    int place_selected_x, place_selected_y;
    
    static final int meta_file_ver_major = 1;
    static final int meta_file_ver_minor = 1;
    
    // tile size in pixel
    int tile_size;
    static final int tile_size_min = 10;
    static final int tile_size_max = 200;
    
    /**
     * Constructs the world tab, opens the world if necessary
     * @param _world_name name of the world
     */
    public WorldTab(JFrame _parent, String _world_name){
        parent = _parent;
        
        positions = new LinkedList<WorldCoordinate>();
        tile_size = 120;
        
        world_name = _world_name;
        
        mouse_in_panel = false;
        mouse_x_previous = mouse_y_previous = 0;
        
        place_selection_enabled = false;
        
        tile_center_color = new Color(207, 190, 134);
        
        setLayout(new BorderLayout());
        
        toolbar = new JToolBar();
        add(toolbar, BorderLayout.WEST);
        toolbar.add(new JButton("dsd"));
        
        worldpanel = new WorldPanel(this);
        add(worldpanel, BorderLayout.CENTER);
        
        // open / get the world
        world = WorldManager.get_world(WorldManager.get_world_file(world_name));
        load_meta();
                        
        add(panel_south = new JPanel(), BorderLayout.SOUTH);
        panel_south.setLayout(new BorderLayout());
        
        slider_zoom = new JSlider(0, 100, (int) (100.0 / tile_size_max * tile_size));
        panel_south.add(slider_zoom, BorderLayout.EAST);
        slider_zoom.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
                set_tile_size((int) ((double) tile_size_max * ((JSlider) arg0.getSource()).getValue() / 100.0));
            }
        });
        
        panel_south.add(label_infobar = new JLabel(), BorderLayout.CENTER);
        
        // set default selected place to hte center place
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
     * Returns true if the path lines are enabled
     * @return 
     */
    public static boolean get_show_paths(){
        return show_paths;
    }
    
    /**
     * Enables or disables the path lines
     * @param b 
     */
    public static void set_show_paths(boolean b){
        show_paths = b;
    }
    
    /**
     * Returns true if curved path lines are enabled
     * @return 
     */
    public static boolean get_show_paths_curved(){
        return show_paths_curved;
    }
    
    /**
     * Enables or disables curved path lines
     * @param b 
     */
    public static void set_show_paths_curved(boolean b){
        show_paths_curved = b;
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
     * Gets the x coordinate of the selected place
     * @return x coordinate
     */
    private int get_place_selection_x(){
        return place_selected_x;
    }
    
    /**
     * Gets the y coordinate of the selected place 
     * @return y coordinate
     */
    private int get_place_selection_y(){
        return place_selected_y;
    }
    
    /**
     * Sets the coordinates of the selected place
     * @param x x coordinate
     * @param y y coordinate
     */
    private void set_place_selection(int x, int y){
        place_selected_x = x;
        place_selected_y = y;
        update_infobar();
        move_screen_to_place_selection();
        redraw();
    }
    
    /**
     * Moves the place selection coordinates
     * @param dx x movement
     * @param dy y movement
     */
    private void move_place_selection(int dx, int dy){
        place_selected_x += dx;
        place_selected_y += dy;
        update_infobar();
        move_screen_to_place_selection();
        redraw();
    }
    
    /**
     * moves the shown places so the selection is on the screen
     */
    private void move_screen_to_place_selection(){           
        int screen_x = worldpanel.get_screen_pos_x(place_selected_x);
        int screen_y = worldpanel.get_screen_pos_y(place_selected_y);
        int tilesize = get_tile_size();
        
        double dx = 0, dy = 0;
        
        if(screen_x < 0) dx = (double) screen_x / tilesize;
        else if(screen_x > worldpanel.screen_width - tilesize) dx = (double) (screen_x - worldpanel.screen_width) / tilesize + 1;
        if(screen_y < 0) dy = (double) -screen_y / tilesize;
        else if(screen_y > worldpanel.screen_height - tilesize) dy = (double) -(screen_y - worldpanel.screen_height) / tilesize - 1;
        
        if(dx != 0 || dy != 0) get_cur_position().move(dx, dy);
    }
    
    /**
     * Updates the infobar
     */
    private void update_infobar(){
        if(get_place_selection_enabled()){
            Layer layer = world.get_layer(get_cur_position().get_layer());
            if(layer != null && layer.exist(get_place_selection_x(), get_place_selection_y())){
                Place pl;
                try {
                    pl = (Place) layer.get(get_place_selection_x(), get_place_selection_y());
                    
                    boolean has_area = pl.get_area() != null;
                    boolean has_comments = pl.get_comments().size() != 0;

                    String infotext = pl.get_name();
                    if(has_area || has_comments) infotext += " (";
                    if(has_area) infotext += pl.get_area().get_name();
                    if(has_comments) infotext += (has_area ? ", " : "") + pl.get_comments_string();
                    if(has_area || has_comments) infotext += ")";
                    
                    label_infobar.setText(infotext);
                } catch (PlaceNotFoundException ex) {
                    Logger.getLogger(WorldTab.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                label_infobar.setText("");
            }
        }
    }
    
    /**
     * Sets the place selection enabled state (if true, the selection will be shown)
     * @param b 
     */
    public void set_place_selection(boolean b){
        place_selection_enabled = b;
        update_infobar();
        redraw();
    }
    
    /**
     * Toggles the place selection enabled state
     */
    public void set_place_selection_toggle(){
        place_selection_enabled = !place_selection_enabled;
        update_infobar();
        redraw();
    }
    
    /**
     * Gets the place selection enabled state
     * @return 
     */
    public boolean get_place_selection_enabled(){
        return place_selection_enabled;
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
    }
    
    /**
     * Gets the current tile size
     * @return tile size
     */
    private int get_tile_size(){
        return tile_size;
    }
    
    /**
     * sets the tile size
     * @param ts new tile size
     */
    public void set_tile_size(int ts){
        tile_size = Math.min(Math.max(ts, tile_size_min), tile_size_max);
        slider_zoom.setValue((int) (100.0 / tile_size_max * tile_size));
        redraw();
    }
    
    /**
     * increases the tile size
     */
    public void tile_size_increment(){
        if(tile_size < tile_size_max) tile_size++;
        slider_zoom.setValue((int) (100.0 / tile_size_max * tile_size));
        redraw();
    }
    
    /**
     * decreases the tile size
     */
    public void tile_size_decrement(){
        if(tile_size > tile_size_min) tile_size--;
        slider_zoom.setValue((int) (100.0 / tile_size_max * tile_size));
        redraw();
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
                        
                        WorldCoordinate newcoord = new WorldCoordinate(tmp_layer_id, tmp_pos_x, tmp_pos_y);
                        if(positions.size() == 0 || !get_cur_position().equals(newcoord)) push_position(newcoord);
                    } else if(line.startsWith("tile_size")){
                        String[] tmp = line.split(" ");
                        tile_size = Integer.parseInt(tmp[1]);
                    } else if(line.startsWith("enable_place_selection")){
                        String[] tmp = line.split(" ");
                        place_selection_enabled = Boolean.parseBoolean(tmp[1]);
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
    public void write_meta(){
        try {
            // open file
            PrintWriter outstream = new PrintWriter(new BufferedWriter( new FileWriter(world.get_file() + "_meta")));

            outstream.println("# MUD Map (v2) world meta data file");
            outstream.println("ver " + meta_file_ver_major + "." + meta_file_ver_minor);
            
            // tile size
            outstream.println("tile_size " + tile_size);
            
            // write whether the place selection is shown
            outstream.println("enable_place_selection " + get_place_selection_enabled());
            
            // write current position and position history
            outstream.println("lp " + get_cur_position().get_meta_String());
            
            // shown place history
            for(Iterator<WorldCoordinate> wcit = positions.descendingIterator(); wcit.hasNext();){
                WorldCoordinate next = wcit.next();
                if(next != get_cur_position()) outstream.println("pcv " + next.get_meta_String());
            }
            
            outstream.close();
        } catch (IOException ex) {
            System.out.printf("Couldn't write world meta file " + world.get_file() + "_meta");
            Logger.getLogger(WorldTab.class.getName()).log(Level.WARNING, null, ex);
        }
    }
    
    private static class WorldPanel extends JPanel {
        
        static final float risk_level_stroke_width = 3;
        
        static final float tile_selection_stroke_width = 3;
        static final java.awt.Color tile_selection_color = new java.awt.Color(255, 0, 0);
        
        static final int tile_border_area = 10;
        static final int tile_border_risk_level = 10;
        
        static final int exit_circle_radius = 5;
        
        double screen_width, screen_height;
        
        WorldTab parent;

        /**
         * Constructs a world panel
         * @param _parent parent world tab
         */
        public WorldPanel(WorldTab _parent) {
            parent = _parent;
            setFocusable(true);
            addKeyListener(new TabKeyListener());
            addMouseListener(new TabMouseListener());
            addMouseMotionListener(new TabMouseMotionListener());
        }
        
        /**
         * Gets the current tile border area size
         * @return area border width
         */
        private int get_tile_border_area(){
            return (int) Math.round(tile_border_area * Math.min(1.0, Math.max(0.5, (double) (parent.get_tile_size() - 20) / 80)));
            //return get_tile_draw_text() ? tile_border_area : (tile_border_area / 2);
        }
        
        /**
         * Gets the current tile risk level border size
         * @return risk level border width
         */
        private int get_tile_border_risk_level(){
            return (int) Math.round(tile_border_risk_level * Math.min(1.0, Math.max(0.5, (double) (parent.get_tile_size() - 20) / 80)));
            //return get_tile_draw_text() ? tile_border_risk_level : (tile_border_risk_level / 2);
        }
                
        private int get_exit_circle_radius(){
            return (int) Math.round(exit_circle_radius * Math.min(1.0, Math.max(0.5, (double) (parent.get_tile_size() - 20) / 80)));
        }
        
        /**
         * Returns true if the tile is large enough to draw text
         * @return 
         */
        private boolean get_tile_draw_text(){
            return parent.get_tile_size() >= 60;
        }
        
        /**
         * Calculates the offset of the exit visualization (dot/circle) to the
         * upper left corner of a tile
         * @param dir eit direction
         * @param x_offset reference to the x offset
         * @param y_offset reference to the y offset
         * @return false if the dot/circle doesn't have to be drawn
         */
        private Pair<Integer, Integer> get_exit_offset(String dir){
            Pair<Integer, Integer> ret = new Pair<Integer, Integer>(0, 0);
            if(dir.equals("n")){ // north
                ret.first = parent.get_tile_size() / 2;
                ret.second = get_tile_border_risk_level();
            } else if(dir.equals("e")){ // east
                ret.first = parent.get_tile_size() - get_tile_border_risk_level();
                ret.second = parent.get_tile_size() / 2;
            } else if(dir.equals("s")){ // south
                ret.first = parent.get_tile_size() / 2;
                ret.second = parent.get_tile_size() - get_tile_border_risk_level();
            } else if(dir.equals("w")){ // west
                ret.first = get_tile_border_risk_level();
                ret.second = parent.get_tile_size() / 2;
            } else if(dir.equals("ne")){ // north-east
                ret.first = parent.get_tile_size() - get_tile_border_risk_level();
                ret.second = get_tile_border_risk_level();
            } else if(dir.equals("se")){ // south-east
                ret.first = ret.second = parent.get_tile_size() - get_tile_border_risk_level();
            } else if(dir.equals("nw")){ // north-west
                ret.first = ret.second = get_tile_border_risk_level();
            } else if(dir.equals("sw")){ // south-west
                ret.first = get_tile_border_risk_level();
                ret.second = parent.get_tile_size() - get_tile_border_risk_level();
            } else {
                ret.first = ret.second = parent.get_tile_size() / 2;
            }
            return ret;
        }
        
        /**
         * Gets the direction / normal angle of an exit
         * @param dir exit direction
         * @return normal angle
         */
        private int get_exit_normal_angle(String dir){
            int ret = 0;
            if(dir.equals("n")){ // north
                ret = 90;
            } else if(dir.equals("e")){ // east
                ret = 0;
            } else if(dir.equals("s")){ // south
                ret = 270;
            } else if(dir.equals("w")){ // west
                ret = 180;
            } else if(dir.equals("ne")){ // north-east
                ret = 45;
            } else if(dir.equals("se")){ // south-east
                ret = 315;
            } else if(dir.equals("nw")){ // north-west
                ret = 135;
            } else if(dir.equals("sw")){ // south-west
                ret = 225;
            }
            return ret;
        }
        
        /**
         * fits the string to max_width in px, cuts it at whitespaces, if possible
         * @param str string to be fitted
         * @param fm fontmetrics
         * @param max_length maximum length of the string in pixel
         * @param max_lines maximum number of lines
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
                    // if there is still a whitespace: cut the string
                    if(whitespace != -1) strlen = whitespace; 
                    else {
                        // if there is no whitespace fit the string length to the line pixel width
                        int lenpx = fm.stringWidth(str.substring(0, (int) Math.ceil(strlen / 1.5)));
                        while(lenpx > max_length){
                            strlen = (int) Math.ceil(strlen / 1.5);
                            lenpx = fm.stringWidth(str.substring(0, strlen));
                            //if(lenpx < max_length) strlen *= 1.5;
                        } 
                        break;
                    }
                }
                
                // cut the next part and return it, abbreviate the string if the max line number is reached
                if(max_lines > 0){
                    ret = fit_line_width(str.substring(strlen).trim(), fm, max_length, max_lines - 1);
                    ret.addFirst(str.substring(0, strlen));
                } else {
                    ret = new LinkedList<String>();
                    if(strlen > 3) ret.add(str.substring(0, strlen - 3) + "...");
                    else ret.add("...");
                }
            }
            return ret;
        }
        
        /**
         * Remove integer part, the part after the point remains
         * @param val
         * @return 
         */
        private double remint(double val){
            return val - Math.round(val);
        }
        
        /**
         * Converts screen coordinates to world coordinates
         * @param screen_x a screen coordinate (x-axis)
         * @return world coordinate x
         */
        private int get_place_pos_x(int screen_x){
            return (int) Math.ceil((double) (screen_x - screen_width / 2) / parent.get_tile_size() + parent.get_cur_position().get_x()) - 1;
        }
        
        /**
         * Converts screen coordinates to world coordinates
         * @param mouse_y a screen coordinate (y-axis)
         * @return world coordinate y
         */
        private int get_place_pos_y(int screen_y){
            return (int) -Math.ceil((double) (screen_y - screen_height / 2) / parent.get_tile_size() - parent.get_cur_position().get_y()) + 1;
        }
        
        /**
         * Converts world coordinates to screen coordinates
         * @param place_x a world (place) coordinate (x axis)
         * @return a screen coordinate x
         */
        private int get_screen_pos_x(int place_x){
            int tile_size = parent.get_tile_size();
            double screen_center_x = ((double) screen_width / tile_size) / 2; // note: wdtwd2
            int place_x_offset = (int) (Math.round((double) parent.get_cur_position().get_x()) - Math.round(screen_center_x));
            return (int)((place_x - place_x_offset + remint(screen_center_x) - remint(parent.get_cur_position().get_x())) * tile_size);
        }
        
        /**
         * Converts world coordinates to screen coordinates
         * @param place_y a world (place) coordinate (y axis)
         * @return a screen coordinate y
         */
        private int get_screen_pos_y(int place_y){
            int tile_size = parent.get_tile_size();
            double screen_center_y = ((double) screen_height / tile_size) / 2;
            int place_y_offset = (int) (Math.round(parent.get_cur_position().get_y()) - Math.round(screen_center_y));
            return (int)((-place_y + place_y_offset - remint(screen_center_y) + remint(parent.get_cur_position().get_y())) * tile_size + screen_height);
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
            
            int tile_size = parent.get_tile_size();
            int exit_radius = get_exit_circle_radius();
            
            // screen size
            screen_width = g.getClipBounds().getWidth();
            screen_height = g.getClipBounds().getHeight();
            
            // screen center in world coordinates
            double screen_center_x = ((double) screen_width / tile_size) / 2; // note: wdtwd2
            double screen_center_y = ((double) screen_height / tile_size) / 2;
            
            int place_x_offset = (int) (Math.round((float) cur_pos.get_x()) - Math.round(screen_center_x));
            int place_y_offset = (int) (Math.round((float) cur_pos.get_y()) - Math.floor(screen_center_y));
            
            // clear screen
            g.clearRect(0, 0, (int) screen_width + 1, (int) screen_height + 1);
            
            // Paths will be drawn on this graphic and later on copied to g
            //Graphics graphic_path = null;
            //if(show_path_lines) graphic_path = g.create();

            // draw the tiles / places
            for(int tile_x = -1; tile_x < screen_width / tile_size + 1; ++tile_x){
                for(int tile_y = -1; tile_y < screen_height / tile_size + 1; ++tile_y){
                    
                    // place position on the map
                    int place_x = tile_x + place_x_offset;
                    int place_y = (int)(screen_height / tile_size) - tile_y + place_y_offset;
                    
                    try { // layer.get throws an exception, if the place doesn't exist
                        Place cur_place = (Place) layer.get(place_x, place_y);
                        
                        // place position in pixel on the screen
                        // TODO: extract constant calculation from for loop
                        int place_x_px = (int)((tile_x + remint(screen_center_x) - remint(cur_pos.get_x())) * tile_size);
                        int place_y_px = (int)((tile_y + remint(screen_center_y) + remint(cur_pos.get_y())) * tile_size);
                    
                        // draw path lines here
                        if(get_show_paths()){
                            for(Path p: cur_place.get_paths()){
                                try {
                                    Place other_place = p.get_other_place(cur_place);
                                    // if both places of a path are on the same layer
                                    if(other_place.get_layer().get_id() == parent.get_cur_position().get_layer()){
                                        Pair<Integer, Integer> exit_offset = get_exit_offset(p.get_exit(cur_place));
                                        Pair<Integer, Integer> exit_offset_other = get_exit_offset(p.get_exit(other_place));
                                        
                                        g.setColor(parent.world.get_path_color());
                                        
                                        if(get_show_paths_curved()){
                                            /*int angle_a = get_exit_normal_angle(p.get_exit(cur_place));
                                            if(angle_a == 0){
                                                //angle_a = arcsin((cur_place.get_y() - other_place.get_y()) / sqrt(dx² + dy²))
                                                // TODO: Winkel für beide berechnen (Fall kein normaler ausgang)
                                            }
                                            
                                            int angle_b = get_exit_normal_angle(p.get_exit(other_place));*/
                                            
                                            
                                            
                                            //g.drawArc(WIDTH, WIDTH, WIDTH, WIDTH, WIDTH, WIDTH);
                                        } else {
                                            // TODO: draw to graphic_path instead of g
                                            /*graphic_path.drawLine(place_x_px + exit_offset.first, place_y_px + exit_offset.second, 
                                                                  place_x_px + (other_place.get_x() - cur_place.get_x()) * tile_size + exit_offset_other.first, 
                                                                  place_y_px - (other_place.get_y() - cur_place.get_y()) * tile_size + exit_offset_other.second);*/
                                            g.drawLine(place_x_px + exit_offset.first, place_y_px + exit_offset.second, 
                                                                  place_x_px + (other_place.get_x() - cur_place.get_x()) * tile_size + exit_offset_other.first, 
                                                                  place_y_px - (other_place.get_y() - cur_place.get_y()) * tile_size + exit_offset_other.second);
                                        }
                                    }
                                } catch(RuntimeException e){
                                    System.out.println(e);
                                }
                            }
                        }
                        
                        // draw area color
                        if(cur_place.get_area() != null){
                            g.setColor(cur_place.get_area().get_color());
                            g.fillRect(place_x_px, place_y_px, tile_size, tile_size);
                        }
                        
                        // draw tile center color
                        if(get_tile_draw_text()){
                            g.setColor(parent.tile_center_color);
                            g.fillRect(place_x_px + get_tile_border_area(), place_y_px + get_tile_border_area(), tile_size - 2 * get_tile_border_area(), tile_size - 2 * get_tile_border_area());
                        }
                        
                        // draw risk level border
                        if(cur_place.get_risk_level() != null){
                            g.setColor(cur_place.get_risk_level().get_color());
                            ((Graphics2D)g).setStroke(new BasicStroke(risk_level_stroke_width));
                            g.drawRect(place_x_px + get_tile_border_area(), place_y_px + get_tile_border_area(), tile_size - 2 * get_tile_border_area(), tile_size - 2 * get_tile_border_area());
                            // TODO: this has to be done after the path rendering
                            //if(show_path_lines) graphic_path.clearRect((int) (place_x_px + get_tile_border_area() - risk_level_stroke_width / 2), (int) (place_y_px + get_tile_border_area() - risk_level_stroke_width / 2), (int) (tile_size - 2 * get_tile_border_area() + risk_level_stroke_width / 2), (int) (tile_size - 2 * get_tile_border_area() + risk_level_stroke_width / 2));
                        } else System.out.println("Error: Can't draw risk level, reference is null");
                        
                        // draw exits
                        if(tile_size >= 20){
                            g.setColor(parent.world.get_path_color());
                            Integer exit_x_offset = new Integer(0), exit_y_offset = new Integer(0);
                            g.setColor(parent.world.get_path_color());
                            boolean up = false, down = false;
                            
                            for(Path p: cur_place.get_paths()){
                                String exit = p.get_exit(cur_place);
                                if(exit.equals("u")) up = true;
                                else if(exit.equals("d")) down = true;
                                else {
                                    Pair<Integer, Integer> exit_offset = get_exit_offset(exit);
                                    if(exit_offset.first != tile_size / 2 || exit_offset.second != tile_size / 2){
                                        g.fillOval(place_x_px + exit_offset.first - exit_radius, place_y_px + exit_offset.second - exit_radius, 2 * exit_radius, 2 * exit_radius);
                                    }
                                }
                            }
                            
                            if(up || down){
                                // TODO: find working arrows, calculate position
                                g.setColor(Color.BLACK);
                                // ⬆⬇ ￪￬ ↑↓
                                String updownstr = "" + (up ? "\342\254\206" : "") + (down ? "\342\254\207" : "");
                                g.drawString(updownstr, place_x_px, place_y_px);
                            }
                        }
                        
                        // draw text, if not in small tiles mode
                        if(get_tile_draw_text()){
                            g.setColor(Color.BLACK);
                            FontMetrics fm = g.getFontMetrics(); // TODO: move constant expression out of the loop (this and part of next line)
                            // fit the string into the tile
                           
                            Deque<String> line = fit_line_width(cur_place.get_name(), fm, (int) (tile_size - 2 * (get_tile_border_area() + tile_selection_stroke_width)), (int) Math.floor((tile_size - 2 * (get_tile_border_risk_level() + get_tile_border_area())) / fm.getHeight()) - 1);
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

                    // TODO: draw graphic_path to g
                    //if(show_path_lines)
                    //graphic_path.dispose();
                    
                    // draw cursor / place selection
                    if(parent.get_place_selection_enabled() && place_x == parent.place_selected_x && place_y == parent.place_selected_y){
                        int place_x_px = (int)((tile_x + remint(screen_center_x) - remint(cur_pos.get_x())) * tile_size); // alternative: get_screen_pos_x();
                        int place_y_px = (int)((tile_y + remint(screen_center_y) + remint(cur_pos.get_y())) * tile_size);
                        
                        g.setColor(tile_selection_color);
                        ((Graphics2D)g).setStroke(new BasicStroke(tile_selection_stroke_width));
                        
                        g.drawLine((int) (place_x_px + tile_selection_stroke_width), (int) (place_y_px + tile_selection_stroke_width), (int) (place_x_px + tile_selection_stroke_width), (int) (place_y_px + tile_selection_stroke_width + tile_size / 4));
                        g.drawLine((int) (place_x_px + tile_selection_stroke_width), (int) (place_y_px + tile_selection_stroke_width), (int) (place_x_px + tile_selection_stroke_width + tile_size / 4), (int) (place_y_px + tile_selection_stroke_width));
                        
                        g.drawLine((int) (place_x_px - tile_selection_stroke_width + tile_size), (int) (place_y_px + tile_selection_stroke_width), (int) (place_x_px - tile_selection_stroke_width + tile_size), (int) (place_y_px + tile_selection_stroke_width + tile_size / 4));
                        g.drawLine((int) (place_x_px - tile_selection_stroke_width + tile_size), (int) (place_y_px + tile_selection_stroke_width), (int) (place_x_px - tile_selection_stroke_width + tile_size * 3 / 4), (int) (place_y_px + tile_selection_stroke_width));
                        
                        g.drawLine((int) (place_x_px + tile_selection_stroke_width), (int) (place_y_px - tile_selection_stroke_width + tile_size), (int) (place_x_px + tile_selection_stroke_width), (int) (place_y_px - tile_selection_stroke_width + tile_size * 3 / 4));
                        g.drawLine((int) (place_x_px + tile_selection_stroke_width), (int) (place_y_px - tile_selection_stroke_width + tile_size), (int) (place_x_px + tile_selection_stroke_width + tile_size  / 4), (int) (place_y_px - tile_selection_stroke_width + tile_size));                         
                        
                        g.drawLine((int) (place_x_px - tile_selection_stroke_width + tile_size), (int) (place_y_px - tile_selection_stroke_width + tile_size), (int) (place_x_px - tile_selection_stroke_width + tile_size), (int) (place_y_px - tile_selection_stroke_width + tile_size * 3 / 4));
                        g.drawLine((int) (place_x_px - tile_selection_stroke_width + tile_size), (int) (place_y_px - tile_selection_stroke_width + tile_size), (int) (place_x_px - tile_selection_stroke_width + tile_size * 3 / 4), (int) (place_y_px - tile_selection_stroke_width + tile_size));
                    }       
                }
            }    
        }
        
        public class TabMouseListener implements MouseListener {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                // if doubleclick: set place selection to coordinates if keyboard selection is enabled
                if(arg0.getButton() == MouseEvent.BUTTON1 && parent.get_place_selection_enabled() && arg0.getClickCount() > 1){
                    parent.set_place_selection(get_place_pos_x(arg0.getX()), get_place_pos_y(arg0.getY()));
                }
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
                    double dx = (double) (arg0.getX() - parent.mouse_x_previous) / parent.get_tile_size();
                    double dy = (double) (arg0.getY() - parent.mouse_y_previous) / parent.get_tile_size();
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
                // TODO: warum funktionieren keine Keycodes?
                switch(Character.toLowerCase(arg0.getKeyChar())){
                    // enable / disable place selection
                    case 'p':
                        parent.set_place_selection_toggle();
                        break;
                    // shift place selection - wasd
                    case 'w':
                        if(parent.get_place_selection_enabled()) parent.move_place_selection(0, +1);
                        break;
                    case 'a':
                        if(parent.get_place_selection_enabled()) parent.move_place_selection(-1, 0);
                        break;
                    case 's':
                        if(parent.get_place_selection_enabled()) parent.move_place_selection(0, -1);
                        break;
                    case 'd':
                        if(parent.get_place_selection_enabled()) parent.move_place_selection(+1, 0);
                        break;
                    // zoom the map
                    case '+':
                        parent.tile_size_increment();
                        break;
                    case '-':
                        parent.tile_size_decrement();
                        break;
                } 
            }

            @Override
            public void keyPressed(KeyEvent arg0) {}

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
         * Sets the x coordinate
         * @param _x new x coordinate
         */
        public void set_x(double _x){
            x = _x;
        }
        
        /**
         * Sets the y coordinate
         * @param _y new y coordinate
         */
        public void set_y(double _y){
            y = _y;
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
        public String get_meta_String(){
            return layer + " " + -x + " " + y;
        }
        
        public boolean equals(WorldCoordinate c){
            return layer == c.layer && x == c.x && y == c.y;
        }
    }
}
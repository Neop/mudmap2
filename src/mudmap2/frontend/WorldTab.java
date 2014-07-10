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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.CubicCurve2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import mudmap2.Pair;
import mudmap2.Paths;
import mudmap2.backend.Layer;
import mudmap2.backend.Layer.PlaceNotFoundException;
import mudmap2.backend.Path;
import mudmap2.backend.Place;
import mudmap2.backend.World;
import mudmap2.backend.WorldCoordinate;
import mudmap2.backend.WorldManager;
import mudmap2.frontend.dialog.AreaDialog;
import mudmap2.frontend.dialog.PathConnectDialog;
import mudmap2.frontend.dialog.PathConnectNeighborsDialog;
import mudmap2.frontend.dialog.PlaceCommentDialog;
import mudmap2.frontend.dialog.PlaceDialog;
import mudmap2.frontend.dialog.PlaceRemoveDialog;
import mudmap2.frontend.dialog.PlaceSelectionDialog;

/**
 * A tab in the main window that displays a world
 * 
 * @author neop
 */
public class WorldTab extends JPanel {
    
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
    static boolean place_selection_enabled_default = true; // default value
    boolean place_selection_enabled;
    int place_selected_x, place_selected_y;
    boolean force_selection;
    
    static final int meta_file_ver_major = 1;
    static final int meta_file_ver_minor = 1;
    
    // tile size in pixel
    int tile_size;
    static final int tile_size_min = 10;
    static final int tile_size_max = 200;
    
    // true, if a context menu is shown (to disable forced focus)
    boolean is_context_menu_shown;
    boolean forced_focus_disabled;
    
    // passive worldtabs don't modify the world
    final boolean passive;
    
    LinkedList<PlaceSelectionListener> place_selection_listeners;

    /**
     * Constructs the world tab, opens the world if necessary
     * @param _parent parent frame
     * @param _world_name name of the world
     * @param _passive if true, everything, that modifies the world, is disabled
     */
    public WorldTab(JFrame _parent, String _world_name, boolean _passive){
        parent = _parent;
        world = WorldManager.get_world(WorldManager.get_world_file(_world_name));
        passive = _passive;
        create();
    }
    
    /**
     * Constructs the world tab, opens the world if necessary
     * @param _parent parent frame
     * @param _world world
     * @param _passive if true, everything, that modifies the world, is disabled
     */
    public WorldTab(JFrame _parent, World _world, boolean _passive){
        parent = _parent;
        world = _world;
        passive = _passive;
        create();
    }
    
    private void create(){
        positions = new LinkedList<WorldCoordinate>();
        tile_size = 120;
        
        is_context_menu_shown = false;
        forced_focus_disabled = true;

        mouse_in_panel = false;
        mouse_x_previous = mouse_y_previous = 0;
        
        force_selection = false;
        place_selection_enabled = place_selection_enabled_default;
        
        tile_center_color = new Color(207, 190, 134);
        
        setLayout(new BorderLayout());
        
        /// TODO: Toolbar
        /*toolbar = new JToolBar();
        add(toolbar, BorderLayout.WEST);
        toolbar.add(new JButton("dsd"));*/
        
        worldpanel = new WorldPanel(this, passive);
        add(worldpanel, BorderLayout.CENTER);
        
        load_meta(); // important: call this after creation of worldpanel!
                        
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
        
        place_selection_listeners = new LinkedList<PlaceSelectionListener>();
    }
    
    /**
     * Closes the tab
     */
    public void close(){
        if(parent instanceof Mainwindow){
            int ret = JOptionPane.showConfirmDialog(this, "Save world \"" + get_world().get_name() + "\"?", "Save world", JOptionPane.YES_NO_OPTION);
            if(ret == JOptionPane.YES_OPTION) save();
            WorldManager.close_world(world.get_file());
            ((Mainwindow) parent).remove_tab(this);
        }
    }
    
    /**
     * Gets the world
     * @return world
     */
    public World get_world(){
        return world;
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
        if(!passive){
            write_meta();
            world.write_world();
        }
    }
    
    /**
     * Gets the currently shown position
     * @return current position
     */
    public WorldCoordinate get_cur_position(){
        return positions.getFirst();
    }
    
    /**
     * Gets the x coordinate of the selected place
     * @return x coordinate
     */
    public int get_place_selection_x(){
        return place_selected_x;
    }
    
    /**
     * Gets the y coordinate of the selected place 
     * @return y coordinate
     */
    public int get_place_selection_y(){
        return place_selected_y;
    }
    
    /**
     * Sets the coordinates of the selected place
     * @param x x coordinate
     * @param y y coordinate
     */
    public void set_place_selection(int x, int y){
        place_selected_x = x;
        place_selected_y = y;
        update_infobar();
        move_screen_to_place_selection();
        redraw();
        call_place_selection_listeners();
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
        call_place_selection_listeners();
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
        redraw();
    }
    
    /**
     * Updates the infobar
     */
    private void update_infobar(){
        if(label_infobar != null ){ 
            if(get_place_selection_enabled()){
                Layer layer = world.get_layer(get_cur_position().get_layer());
                if(layer != null && layer.exist(get_place_selection_x(), get_place_selection_y())){
                    Place pl;
                        pl = (Place) layer.get(get_place_selection_x(), get_place_selection_y());

                        boolean has_area = pl.get_area() != null;
                        boolean has_comments = pl.get_comments().size() != 0;

                        String infotext = pl.get_name();
                        if(has_area || has_comments) infotext += " (";
                        if(has_area) infotext += pl.get_area().get_name();
                        if(has_comments) infotext += (has_area ? ", " : "") + pl.get_comments_string(false);
                        if(has_area || has_comments) infotext += ")";

                        label_infobar.setText(infotext);
                } else {
                    label_infobar.setText("");
                }
            } else label_infobar.setText("");
        }
    }
    
    /**
     * Sets the place selection enabled state (if true, the selection will be shown)
     * @param b 
     */
    public void set_place_selection_enabled(boolean b){
        place_selection_enabled = b || force_selection;
        update_infobar();
        redraw();
    }
    
    /**
     * Toggles the place selection enabled state
     */
    public void set_place_selection_toggle(){
        if(!force_selection){
            place_selection_enabled = !place_selection_enabled;
            update_infobar();
            redraw();
        }
    }
    
    /**
     * Gets the place selection enabled state
     * @return 
     */
    public boolean get_place_selection_enabled(){
        return place_selection_enabled || force_selection;
    }
    
    /**
     * Enables or disables the place selection
     * @param b new place selection state
     */
    private void set_place_selection(boolean b){
        place_selection_enabled = b || force_selection;
        redraw();
    }
    
    /**
     * Forces the place selection to be enabled, if true
     * @param b 
     */
    public void set_place_selection_forced(boolean b){
        if(force_selection = b) set_place_selection(true);
    }
    
    /**
     * Pushes a new position on the position stack ("goto")
     * @param _pos new position
     */
    public void push_position(WorldCoordinate _pos){
        WorldCoordinate pos = _pos.clone();
        positions.push(pos);
        // move place selection
        set_place_selection((int) pos.get_x(), (int) pos.get_y());
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
     * Removes all previously visited positions from history and sets pos
     * @param pos new position
     */
    public void reset_history(WorldCoordinate pos){
        positions.clear();
        positions.add(pos);
        place_selected_x = (int) Math.round(pos.get_x());
        place_selected_y = (int) Math.round(pos.get_y());
        update_infobar();
        redraw();
    }
    
    /**
     * Go to the home position
     */
    public void goto_home(){
        push_position(world.get_home().clone());
        set_place_selection((int) Math.round(get_cur_position().get_x()), (int) Math.round(get_cur_position().get_y()));
    }
    
    /**
     * Sets a new home position
     */
    public void set_home(){
        world.set_home(get_cur_position().clone());
    }
    
    /**
     * Gets a place on the current layer
     * @param x x coordinate
     * @param y y coordinate
     * @return place or null
     */
    public Place get_place(int x, int y){
        Place ret = null;
        Layer layer = world.get_layer(get_cur_position().get_layer());
        if(layer != null) ret = (Place) layer.get(x, y);
        return ret;
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
     * Sets whether a context menu is shown, to disable forced focus
     * @param b 
     */
    private void set_context_menu(boolean b) {
        is_context_menu_shown = b;
    }
    
    /**
     * Returns true, if a context menu is shown and forced focus is disabled
     * @return 
     */
    private boolean has_context_menu(){
        return is_context_menu_shown;
    }
    
    /**
     * Manually disables the forced focus
     * @param b 
     */
    public void set_forced_focus_disabled(boolean b){
        forced_focus_disabled = b;
    }
    
    /**
     * Returns true, if forced focus is disabled manually
     * @return 
     */
    public boolean get_forced_focus_disabled(){
        return forced_focus_disabled;
    }
    
    /**
     * Returs true, if forced focus can be enabled
     * @return 
     */
    private boolean get_forced_focus(){
        return !forced_focus_disabled && !is_context_menu_shown;
    }
    
    
    /**
     * Adds a place selection listener
     * @param listener 
     */
    public void add_place_selection_listener(PlaceSelectionListener listener){
        if(!place_selection_listeners.contains(listener))
            place_selection_listeners.add(listener);
    }
    
    /**
     * Removes a place selection listener
     * @param listener 
     */
    public void remove_place_selection_listener(PlaceSelectionListener listener){
        place_selection_listeners.remove(listener);
    }
    
    /**
     * calls all place selection listeners
     */
    private void call_place_selection_listeners(){
        Place place = get_place(get_place_selection_x(), get_place_selection_y());

        if(place_selection_listeners != null) {
            if(place != null) 
                for(PlaceSelectionListener listener: place_selection_listeners) 
                    listener.placeSelected(place);
            else{
                Layer layer = get_world().get_layer(get_cur_position().get_layer());
                for(PlaceSelectionListener listener: place_selection_listeners) 
                    listener.placeDeselected(layer, get_place_selection_x(), get_place_selection_y());
            }
        }
    }
    
    public interface PlaceSelectionListener{
        // gets called, when the place selection changes to another place
        public void placeSelected(Place p);
        // gets called, when the place selection changes to null
        public void placeDeselected(Layer layer, int x, int y);
    }
    
    /**
     * Loads the world meta data file
     * this file describes the coordinates of the last shown positions
     *
     * important: call this after creation of worldpanel!
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

                    if(line.isEmpty() || line.startsWith("//") || line.startsWith("#")) continue;
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
                        place_selection_enabled = Boolean.parseBoolean(tmp[1]) || force_selection;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            push_position(new WorldCoordinate(layer_id, pos_x, pos_y));
            
        } catch (FileNotFoundException ex) {
            System.out.println("Couldn't open world meta file \"" + file + "\", file not found");
            //Logger.getLogger(WorldManager.class.getName()).log(Level.INFO, null, ex);
            
            push_position(new WorldCoordinate(0, 0, 0));
        }
    }
    
    /**
     * Saves the world meta file
     */
    public void write_meta(){
        if(!passive){
            try {
                // open file
                if(!Paths.is_directory(Paths.get_worlds_dir())) Paths.create_directory(Paths.get_worlds_dir());
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
    }
    
    private static class WorldPanel extends JPanel {
        
        static final float tile_path_stroke_width = 3;
        
        static final float tile_selection_stroke_width = 3;
        static final java.awt.Color tile_selection_color = new java.awt.Color(255, 0, 0);
        
        static final float tile_risk_level_stroke_width = 3;
        static final int tile_border_width = 10;
        static final int exit_circle_radius = 5;
        
        double screen_width, screen_height;
        
        WorldTab parent;

        // passive worldpanels don't modify the world
        final boolean passive;
        
        /**
         * Constructs a world panel
         * @param _parent parent world tab
         */
        public WorldPanel(WorldTab _parent, boolean _passive) {
            parent = _parent;
            passive = _passive;
            setFocusable(true);
            requestFocusInWindow();
            addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent arg0) {}
                @Override
                public void focusLost(FocusEvent arg0) {
                    if(parent.get_forced_focus()) requestFocusInWindow();
                }
            });
               
            addKeyListener(new TabKeyPassiveListener(this));
            addMouseListener(new TabMousePassiveListener());
            if(!passive){
                addKeyListener(new TabKeyListener(this));
                addMouseListener(new TabMouseListener());
            }
            addMouseWheelListener(new MouseWheelListener() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    parent.set_tile_size(parent.get_tile_size() + e.getWheelRotation());
                }
            });
            addMouseMotionListener(new TabMouseMotionListener());
        }
        
        /**
         * Gets the screen width
         * @return screen width
         */
        public double get_screen_width(){
            return screen_width;
        }
        
        /**
         * Gets the screen height
         * @return screen height
         */
        public double get_screen_height(){
            return screen_height;
        }
        
        /**
         * Gets the current tile border area size
         * @return area border width
         */
        private int get_tile_border_width(){
            // with interpolation for smooth transition
            return (int) Math.round(tile_border_width * Math.min(1.0, Math.max(0.5, (double) (parent.get_tile_size() - 20) / 80)));
        }
                
        /**
         * Gets the radius of the exit circles / dots
         * @return 
         */
        private int get_exit_circle_radius(){
            return (int) Math.round(exit_circle_radius * Math.min(1.0, Math.max(0.5, (double) (parent.get_tile_size() - 20) / 80)));
        }
        
        /**
         * Gets the stroke width of the tile selection box
         * @return 
         */
        private float get_tile_selection_stroke_width(){
            return tile_selection_stroke_width;
        }
        
        /**
         * Gets the stroke width of the risk level border
         * @return 
         */
        private float get_risk_level_stroke_width(){
            return tile_risk_level_stroke_width;
        }
        
        /**
         * Gets the path stroke width
         * @return 
         */
        private float get_path_stroke_width(){
            return tile_path_stroke_width * (float) (1.0 + parent.tile_size / 200.0);
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
            int border_width = get_tile_border_width();
            if(dir.equals("n")){ // north
                ret.first = parent.get_tile_size() / 2;
                ret.second = border_width;
            } else if(dir.equals("e")){ // east
                ret.first = parent.get_tile_size() - border_width;
                ret.second = parent.get_tile_size() / 2;
            } else if(dir.equals("s")){ // south
                ret.first = parent.get_tile_size() / 2;
                ret.second = parent.get_tile_size() - border_width;
            } else if(dir.equals("w")){ // west
                ret.first = border_width;
                ret.second = parent.get_tile_size() / 2;
            } else if(dir.equals("ne")){ // north-east
                ret.first = parent.get_tile_size() - border_width;
                ret.second = border_width;
            } else if(dir.equals("se")){ // south-east
                ret.first = ret.second = parent.get_tile_size() - border_width;
            } else if(dir.equals("nw")){ // north-west
                ret.first = ret.second = border_width;
            } else if(dir.equals("sw")){ // south-west
                ret.first = border_width;
                ret.second = parent.get_tile_size() - border_width; 
            } else {
                ret.first = ret.second = parent.get_tile_size() / 2;
            }
            return ret;
        }
        
        /**
         * Gets the normal vector of an exit
         * @param dir exit direction
         * @return normal vector
         */
        private Pair<Double, Double> get_exit_normal(String dir){
            Pair<Double, Double> ret = new Pair(0, 0);
            if(dir.equals("n")){
                ret.first = 0.0;
                ret.second = 1.0;
            } else if(dir.equals("e")){
                ret.first = -1.0;
                ret.second = 0.0;
            } else if(dir.equals("s")){
                ret.first = 0.0;
                ret.second = -1.0;
            } else if(dir.equals("w")){
                ret.first = 1.0;
                ret.second = 0.0;
            } else if(dir.equals("ne")){
                ret.first = -1.0;
                ret.second = 1.0;
            } else if(dir.equals("se")){
                ret.first = -1.0;
                ret.second = -1.0;
            } else if(dir.equals("nw")){
                ret.first = 1.0;
                ret.second = 1.0;
            } else if(dir.equals("sw")){
                ret.first = 1.0;
                ret.second = -1.0;
            }
            // normalize it
            if(ret.first != 0.0 && ret.second != 0.0){
                double length = Math.sqrt(ret.first * ret.first + ret.second * ret.second);
                ret.first /= length;
                ret.second /= length;
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
            WorldCoordinate cur_pos = parent.get_cur_position();
            Layer layer = parent.world.get_layer(cur_pos.get_layer());
            
            FontMetrics fm = g.getFontMetrics();

            final int tile_size = parent.get_tile_size();
            final int exit_radius = get_exit_circle_radius();
            final float selection_stroke_width = get_tile_selection_stroke_width();
            final float risk_level_stroke_width = get_risk_level_stroke_width();
            final int border_width = get_tile_border_width();

            // max number of text lines tht fit in a tile
            final int max_lines = (int) Math.floor((double)(tile_size - 3 * (border_width + (int) Math.ceil(risk_level_stroke_width))) / fm.getHeight());

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
            BufferedImage image_path = null;
            Graphics graphic_path = null;
            ArrayList<Pair<Integer, Integer>> tile_positions = null; // to mask out the tile positions on graphic_path
            if(get_show_paths()){
                image_path = new BufferedImage((int) screen_width, (int) screen_height, BufferedImage.TYPE_INT_ARGB);
                graphic_path = image_path.getGraphics();
                graphic_path.setColor(parent.world.get_path_color());
                ((Graphics2D) graphic_path).setStroke(new BasicStroke(get_path_stroke_width()));
                tile_positions = new ArrayList<Pair<Integer, Integer>>();
            }

            // draw the tiles / places
            for(int tile_x = -1; tile_x < screen_width / tile_size + 1; ++tile_x){
                for(int tile_y = -1; tile_y < screen_height / tile_size + 1; ++tile_y){

                    // place position on the map
                    int place_x = tile_x + place_x_offset;
                    int place_y = (int)(screen_height / tile_size) - tile_y + place_y_offset;

                    if(layer != null && layer.exist(place_x, place_y)){                
                        Place cur_place = (Place) layer.get(place_x, place_y);

                        // place position in pixel on the screen
                        // TODO: extract constant calculation from for loop
                        int place_x_px = (int)((tile_x + remint(screen_center_x) - remint(cur_pos.get_x())) * tile_size);
                        int place_y_px = (int)((tile_y + remint(screen_center_y) + remint(cur_pos.get_y())) * tile_size);

                        if(tile_positions != null) tile_positions.add(new Pair(place_x_px, place_y_px));

                        // number of drawn text lines
                        int line_num = 0;

                        // draw path lines here
                        if(get_show_paths()){
                            for(Path p: cur_place.get_paths()){
                                try {
                                    Place other_place = p.get_other_place(cur_place);
                                    // if both places of a path are on the same layer
                                    if(other_place.get_layer().get_id() == parent.get_cur_position().get_layer()){
                                        Pair<Integer, Integer> exit_offset = get_exit_offset(p.get_exit(cur_place));
                                        Pair<Integer, Integer> exit_offset_other = get_exit_offset(p.get_exit(other_place));

                                        // TODO: implement curved lines, normals dont fit yet and lines are drawn twice
                                        boolean draw_curves = false;//get_show_paths_curved();

                                        if(draw_curves){
                                            Pair<Double, Double> normal1 = get_exit_normal(p.get_exit(cur_place));
                                            Pair<Double, Double> normal2 = get_exit_normal(p.get_exit(other_place));

                                            double exit_1_x = place_x_px + exit_offset.first;
                                            double exit_1_y = place_y_px + exit_offset.second;
                                            double exit_2_x = place_x_px + (other_place.get_x() - cur_place.get_x()) * tile_size + exit_offset_other.first;
                                            double exit_2_y = place_y_px - (other_place.get_y() - cur_place.get_y()) * tile_size + exit_offset_other.second;

                                            double dx = exit_2_x - exit_1_x;
                                            double dy = exit_2_y - exit_1_y;

                                            if(draw_curves = Math.sqrt(dx * dx + dy * dy) >= 1.5 * tile_size){
                                                CubicCurve2D c = new CubicCurve2D.Double();
                                                c.setCurve(exit_1_x, 
                                                        exit_1_y,
                                                        place_x_px + normal1.first * tile_size, place_y_px + normal1.second * tile_size,
                                                        place_x_px + (other_place.get_x() - cur_place.get_x() + normal2.first) * tile_size + exit_offset_other.first,
                                                        place_y_px - (other_place.get_y() - cur_place.get_y() + normal2.second) * tile_size + exit_offset_other.second,
                                                        exit_2_x, 
                                                        exit_2_y);
                                                ((Graphics2D) graphic_path).draw(c);
                                            }
                                        }

                                        if(!draw_curves){
                                            graphic_path.drawLine(place_x_px + exit_offset.first, place_y_px + exit_offset.second, 
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
                            g.fillRect(place_x_px + border_width, place_y_px + border_width, tile_size - 2 * border_width, tile_size - 2 * border_width);
                        }

                        // draw risk level border
                        if(cur_place.get_risk_level() != null){
                            g.setColor(cur_place.get_risk_level().get_color());
                            ((Graphics2D)g).setStroke(new BasicStroke(risk_level_stroke_width));
                            g.drawRect(place_x_px + border_width, place_y_px + border_width, tile_size - 2 * border_width, tile_size - 2 * border_width);
                            // TODO: this has to be done after the path rendering
                            //if(show_path_lines) graphic_path.clearRect((int) (place_x_px + get_tile_border_area() - risk_level_stroke_width / 2), (int) (place_y_px + get_tile_border_area() - risk_level_stroke_width / 2), (int) (tile_size - 2 * get_tile_border_area() + risk_level_stroke_width / 2), (int) (tile_size - 2 * get_tile_border_area() + risk_level_stroke_width / 2));
                        } else System.out.println("Error: Can't draw risk level, reference is null");

                        // draw text, if tiles are large enough
                        if(get_tile_draw_text()){
                            g.setColor(Color.BLACK);
                            //FontMetrics fm = g.getFontMetrics(); // TODO: move constant expression out of the loop (this and part of next line)
                            // fit the string into the tile

                            // place name
                            Deque<String> line = fit_line_width(cur_place.get_name(), fm, (int) (tile_size - 2 * (border_width + selection_stroke_width)), max_lines);
                            for(String str: line){
                                g.drawString(str, place_x_px + border_width + (int) tile_selection_stroke_width + (int) Math.ceil(risk_level_stroke_width), place_y_px + border_width + (int) tile_selection_stroke_width + fm.getHeight() * (1 + line_num));
                                line_num++;
                            }

                            if(line_num < max_lines){ // it't not unusual for some places to fill up all the lines
                                // recommended level
                                int reclvlmin = cur_place.get_rec_lvl_min(), reclvlmax = cur_place.get_rec_lvl_max();
                                if(reclvlmin > -1 || reclvlmax > -1){
                                    g.drawString("lvl " + (reclvlmin > -1 ? reclvlmin : "?") + " - " + (reclvlmax > -1 ? reclvlmax : "?"), place_x_px + border_width + (int) tile_selection_stroke_width + (int) Math.ceil(risk_level_stroke_width), place_y_px + border_width + (int) tile_selection_stroke_width + fm.getHeight() * (1 + line_num));
                                    line_num++;
                                }

                                // sub areas / children
                                if(line_num < max_lines && !cur_place.get_children().isEmpty()){
                                    int children_num = cur_place.get_children().size();
                                    String sa_str = "sa" + (children_num > 1 ? " (" + cur_place.get_children().size() + "): " : ": ");

                                    boolean first_child = true;
                                    for(Place child: cur_place.get_children()){
                                        sa_str += (first_child ? "" : ", ") + child.get_name();
                                        first_child = false;
                                    }
                                    line = fit_line_width(sa_str, fm, (int) (tile_size - 2 * (border_width + selection_stroke_width)), max_lines - line_num);
                                    for(String str: line){
                                        g.drawString(str, place_x_px + border_width + (int) tile_selection_stroke_width + (int) Math.ceil(risk_level_stroke_width), place_y_px + border_width + (int) tile_selection_stroke_width + fm.getHeight() * (1 + line_num));
                                        line_num++;
                                    }
                                }

                                // flags
                                if(line_num < max_lines){
                                    String flags = "";
                                    // place has comments
                                    if(cur_place.get_comments().size() > 0) flags += "C";

                                    // other flags
                                    for(Entry<String, Boolean> flag: cur_place.get_flags().entrySet())
                                        if(flag.getValue()) flags += flag.getKey();

                                    // draw flags
                                    g.drawString(flags, place_x_px + border_width + (int) Math.ceil(2 * selection_stroke_width), place_y_px + tile_size - border_width - (int) Math.ceil(2 * selection_stroke_width));
                                }
                            }
                        }

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

                            if((up || down) && get_tile_draw_text() && line_num < max_lines){
                                g.setColor(Color.BLACK);
                                // have some arrows: ⬆⬇ ￪￬ ↑↓
                                //String updownstr = "" + (up ? "u" : "") + (down ? "d" : "");
                                String updownstr = "" + (up ? "⬆" : "") + (down ? "⬇" : "");
                                g.drawString(updownstr, place_x_px + tile_size - border_width - fm.stringWidth(updownstr) - (int) Math.ceil(2 * selection_stroke_width), place_y_px + tile_size - border_width - (int) Math.ceil(2 * selection_stroke_width));
                            }
                        }

                        // TODO: draw flags
                    }

                    // draw cursor / place selection
                    if(parent.get_place_selection_enabled() && place_x == parent.place_selected_x && place_y == parent.place_selected_y){
                        int place_x_px = (int)((tile_x + remint(screen_center_x) - remint(cur_pos.get_x())) * tile_size); // alternative: get_screen_pos_x();
                        int place_y_px = (int)((tile_y + remint(screen_center_y) + remint(cur_pos.get_y())) * tile_size);

                        g.setColor(tile_selection_color);
                        ((Graphics2D)g).setStroke(new BasicStroke((selection_stroke_width)));

                        g.drawLine((int) (place_x_px + selection_stroke_width), (int) (place_y_px + selection_stroke_width), (int) (place_x_px + selection_stroke_width), (int) (place_y_px + selection_stroke_width + tile_size / 4));
                        g.drawLine((int) (place_x_px + selection_stroke_width), (int) (place_y_px + selection_stroke_width), (int) (place_x_px + selection_stroke_width + tile_size / 4), (int) (place_y_px + selection_stroke_width));

                        g.drawLine((int) (place_x_px - selection_stroke_width + tile_size), (int) (place_y_px + selection_stroke_width), (int) (place_x_px - selection_stroke_width + tile_size), (int) (place_y_px + selection_stroke_width + tile_size / 4));
                        g.drawLine((int) (place_x_px - selection_stroke_width + tile_size), (int) (place_y_px + selection_stroke_width), (int) (place_x_px - selection_stroke_width + tile_size * 3 / 4), (int) (place_y_px + selection_stroke_width));

                        g.drawLine((int) (place_x_px + selection_stroke_width), (int) (place_y_px - selection_stroke_width + tile_size), (int) (place_x_px + selection_stroke_width), (int) (place_y_px - selection_stroke_width + tile_size * 3 / 4));
                        g.drawLine((int) (place_x_px + selection_stroke_width), (int) (place_y_px - selection_stroke_width + tile_size), (int) (place_x_px + selection_stroke_width + tile_size  / 4), (int) (place_y_px - selection_stroke_width + tile_size));                         

                        g.drawLine((int) (place_x_px - selection_stroke_width + tile_size), (int) (place_y_px - selection_stroke_width + tile_size), (int) (place_x_px - selection_stroke_width + tile_size), (int) (place_y_px - selection_stroke_width + tile_size * 3 / 4));
                        g.drawLine((int) (place_x_px - selection_stroke_width + tile_size), (int) (place_y_px - selection_stroke_width + tile_size), (int) (place_x_px - selection_stroke_width + tile_size * 3 / 4), (int) (place_y_px - selection_stroke_width + tile_size));
                    }       
                }
            }

            if(get_show_paths()){
                // mask out tile positions on graphic_path
                ((Graphics2D) graphic_path).setBackground(new Color(0,0,0,0));
                int clear_tile_size = tile_size - 2 * border_width;
                for(Pair<Integer, Integer> p: tile_positions)
                    //graphic_path.clearRect(p.first, p.second, p.first + tile_size, p.second + tile_size);
                    graphic_path.clearRect(p.first + border_width, p.second + border_width, clear_tile_size, clear_tile_size);

                // draw graphic_path to g
                g.drawImage(image_path, 0, 0, null);
                graphic_path.dispose();
            }

        }
        
        /**
         * This listener only contains actions, that don't modify the world
         */
        private class TabMousePassiveListener extends TabMouseListener implements MouseListener {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                if(arg0.getButton() == MouseEvent.BUTTON1){ // left click
                    // set place selection to coordinates if keyboard selection is enabled
                    parent.set_place_selection(get_place_pos_x(arg0.getX()), get_place_pos_y(arg0.getY()));
                }
            }
        }
        
        /**
         * This listener contains actions, that modify the world
         */
        private class TabMouseListener implements MouseListener {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                if(arg0.getButton() == MouseEvent.BUTTON3){ // right click
                    // show context menu
                    TabContextMenu context_menu = new TabContextMenu(parent, get_place_pos_x(arg0.getX()), get_place_pos_y(arg0.getY()));
                    context_menu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
                requestFocusInWindow();
            }

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
                
        private class TabMouseMotionListener implements MouseMotionListener {

            @Override
            public void mouseDragged(MouseEvent arg0) {
                if(parent.mouse_in_panel){
                    double dx = (double) (arg0.getX() - parent.mouse_x_previous) / parent.get_tile_size();
                    double dy = (double) (arg0.getY() - parent.mouse_y_previous) / parent.get_tile_size();
                    parent.get_cur_position().move(-dx , dy);
                    parent.redraw();
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
        
        /**
         * This listener only contains actions, that don't modify the world
         */
        private class TabKeyPassiveListener extends TabKeyListener {
            public TabKeyPassiveListener(WorldPanel parent){
                super(parent);
            }
            
            @Override
            public void keyPressed(KeyEvent arg0) {
                switch(arg0.getKeyCode()){
                    // zoom the map
                    case KeyEvent.VK_PLUS:
                    case KeyEvent.VK_SUBTRACT:
                    case KeyEvent.VK_PAGE_UP:
                        parent.tile_size_increment();
                        break;
                    case KeyEvent.VK_ADD:
                    case KeyEvent.VK_MINUS:
                    case KeyEvent.VK_PAGE_DOWN:
                        parent.tile_size_decrement();
                        break;

                    // enable / disable place selection
                    case KeyEvent.VK_P:
                        parent.set_place_selection_toggle();
                        break;

                    // shift place selection - wasd
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        if(parent.get_place_selection_enabled()) parent.move_place_selection(0, +1);
                        break;
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        if(parent.get_place_selection_enabled()) parent.move_place_selection(-1, 0);
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        if(parent.get_place_selection_enabled()) parent.move_place_selection(0, -1);
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        if(parent.get_place_selection_enabled()) parent.move_place_selection(+1, 0);
                        break;
                        
                    // goto home
                    case KeyEvent.VK_H:
                    case KeyEvent.VK_HOME:
                        parent.goto_home();
                        break;
                }
            }
        }
        
        /**
         * This listener contains actions, that modify the world
         */
        private class TabKeyListener implements KeyListener {
            
            WorldPanel worldpanel;
            
            public TabKeyListener(WorldPanel parent){
                worldpanel = parent;
            }

            @Override
            public void keyTyped(KeyEvent arg0) {}

            @Override
            public void keyPressed(KeyEvent arg0) {
                switch(arg0.getKeyCode()){
                    // show context menu
                    case KeyEvent.VK_CONTEXT_MENU:
                        if(parent.get_place_selection_enabled()){
                            TabContextMenu context_menu = new TabContextMenu(parent, parent.get_place_selection_x(), parent.get_place_selection_y());
                            context_menu.show(arg0.getComponent(), get_screen_pos_x(parent.get_place_selection_x()) + worldpanel.parent.get_tile_size() / 2, get_screen_pos_y(parent.get_place_selection_y()) + worldpanel.parent.get_tile_size() / 2);
                        }
                        break;

                    // edit / add place
                    case KeyEvent.VK_INSERT:
                    case KeyEvent.VK_E:
                        if(parent.get_place_selection_enabled()){
                            Place place = parent.get_place(parent.get_place_selection_x(), parent.get_place_selection_y());
                            PlaceDialog dlg;
                            if(place != null) dlg = new PlaceDialog(parent.parent, parent.world, place);
                            else dlg = new PlaceDialog(parent.parent, parent.world, parent.world.get_layer(parent.get_cur_position().get_layer()), parent.get_place_selection_x(), parent.get_place_selection_y());
                            dlg.setVisible(true);
                        }
                        break;
                    // create placeholder
                    case KeyEvent.VK_F:
                        if(parent.get_place_selection_enabled()){
                            Place place = parent.world.get(parent.get_cur_position().get_layer(), parent.get_place_selection_x(), parent.get_place_selection_y());
                            // create placeholder or remove one
                            if(place == null){
                                parent.world.put_placeholder(parent.get_cur_position().get_layer(), parent.get_place_selection_x(), parent.get_place_selection_y());
                            } else if(place.get_name().equals(Place.placeholder_name)){
                                try {
                                    place.remove();
                                } catch (RuntimeException ex) {
                                    Logger.getLogger(WorldTab.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (PlaceNotFoundException ex) {
                                    Logger.getLogger(WorldTab.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                        parent.redraw();
                        break;
                    // remove place
                    case KeyEvent.VK_DELETE:
                    case KeyEvent.VK_R:
                        if(parent.get_place_selection_enabled()){
                            Place place = parent.get_place(parent.get_place_selection_x(), parent.get_place_selection_y());
                            if(place != null) (new PlaceRemoveDialog(parent.parent, parent.world, place)).show();
                        }
                        break;
                    // edit place comments
                    case KeyEvent.VK_C:
                        if(parent.get_place_selection_enabled()){
                            Place place = parent.get_place(parent.get_place_selection_x(), parent.get_place_selection_y());
                            if(place != null){
                                (new PlaceCommentDialog(parent.parent, place)).setVisible(true);
                                parent.update_infobar();
                            }
                        }
                        break;
                    // modify area
                    case KeyEvent.VK_Q:
                        Place place = parent.get_place(parent.get_place_selection_x(), parent.get_place_selection_y());
                        AreaDialog dlg = null;
                        // if no place is selected or selection disabled: create new area
                        if(place == null || !parent.get_place_selection_enabled()) dlg = new AreaDialog(parent.parent, parent.world);
                        // else modify area of selected place
                        else if(parent.get_place_selection_enabled()) dlg = new AreaDialog(parent.parent, parent.world, place);
                        // show dialog
                        if(dlg != null) dlg.setVisible(true);
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent arg0) {}
            
        }
        
        // constructs the context menu (on right click)
        private static class TabContextMenu extends JPopupMenu {
            
            WorldTab parent;
            
            /**
             * Constructs a context menu at position (x,y)
             * @param x screen / panel coordinate x
             * @param y screen / panel coordinate y
             */
            public TabContextMenu(WorldTab _parent, final int px, final int py) {
                addPopupMenuListener(new TabContextPopMenuListener());
                
                parent = _parent;
                Layer layer = parent.world.get_layer(parent.get_cur_position().get_layer());
                
                final Place place = (layer != null ? (Place) layer.get(px, py) : null);
                if(layer != null && place != null){ // if place exists
                    JMenuItem mi_edit = new JMenuItem("Edit place");
                    mi_edit.addActionListener(new PlaceDialog(parent.parent, parent.world, place));
                    add(mi_edit);
                    
                    JMenuItem mi_remove = new JMenuItem("Remove place");
                    mi_remove.addActionListener(new PlaceRemoveDialog(parent.parent, parent.world, place));
                    add(mi_remove);
                    
                    JMenuItem mi_comments = new JMenuItem("Edit comments");
                    mi_comments.addActionListener(new PlaceCommentDialog(parent.parent, place));
                    add(mi_comments);
                    
                    JMenuItem mi_area = new JMenuItem("Edit area");
                    if(place.get_area() != null) mi_area.addActionListener(new AreaDialog(parent.parent, parent.world, place.get_area()));
                    else mi_area.addActionListener(new AreaDialog(parent.parent, parent.world, place));
                    add(mi_area);
                    
                    // ------------- Paths ------------------
                    JMenu m_paths = new JMenu("Paths / Exits");
                    add(m_paths);
                    
                    JMenu m_path_connect = new JMenu("Connect");
                    m_paths.add(m_path_connect);
                    m_path_connect.setToolTipText("Connect a path from this place to another one");
                    
                    JMenuItem mi_path_connect_select = new JMenuItem("Select");
                    m_path_connect.add(mi_path_connect_select);
                    mi_path_connect_select.setToolTipText("Select any place from the map");
                    mi_path_connect_select.addActionListener(new PathConnectDialog(parent.parent, place));
                    
                    JMenuItem mi_path_connect_neighbors = new JMenuItem("Neighbors");
                    m_path_connect.add(mi_path_connect_neighbors);
                    mi_path_connect_neighbors.setToolTipText("Choose from surrounding places");
                    mi_path_connect_neighbors.addActionListener(new PathConnectNeighborsDialog(parent.parent, place));

                    LinkedList<Place> places = layer.get_neighbors(px, py, 1);
                    if(!places.isEmpty()){
                        m_path_connect.add(new JSeparator());

                        for(Place neighbor: places){
                            // only show, if no connection exists, yet
                            if(place.get_paths(neighbor).isEmpty()){
                                String dir1 = "", dir2 = "";

                                if(neighbor.get_y() > place.get_y())
                                    {dir1 = "n"; dir2 = "s";}
                                else if(neighbor.get_y() < place.get_y())
                                    {dir1 = "s"; dir2 = "n";}
                                if(neighbor.get_x() > place.get_x())
                                    {dir1 = dir1 + "e"; dir2 = dir2 + "w";}
                                else if(neighbor.get_x() < place.get_x())
                                    {dir1 = dir1 + "w"; dir2 = dir2 + "e";}

                                JMenuItem mi_path_connect = new JMenuItem("[" + dir1 + "] " + neighbor.get_name());
                                m_path_connect.add(mi_path_connect);
                                mi_path_connect.addActionListener(new ConnectPathActionListener(place, neighbor, dir1, dir2));
                            }
                        }
                    }
                    
                    // get all connected places
                    HashSet<Path> paths = place.get_paths();
                    
                    if(!paths.isEmpty()){
                        JMenu m_path_remove = new JMenu("Remove");
                        m_paths.add(m_path_remove);
                        m_path_remove.setToolTipText("Remove a path");
                        
                        m_paths.add(new JSeparator());

                        for(Path path: paths){
                            Place other_place = path.get_other_place(place);
                            JMenuItem mi_path_goto = new JMenuItem("Go to [" + path.get_exit(place) + "] " + other_place);
                            m_paths.add(mi_path_goto);
                            mi_path_goto.addActionListener(new GotoPlaceActionListener(parent, other_place));
                            
                            JMenuItem mi_path_remove = new JMenuItem("Remove [" + path.get_exit(place) + "] " + other_place);
                            m_path_remove.add(mi_path_remove);
                            mi_path_remove.addActionListener(new RemovePathActionListener(path));
                        }
                    }
                    
                    // ------------- sub-areas ------------------
                    JMenu m_subareas = new JMenu("Sub-areas");
                    m_subareas.setToolTipText("Not to be confused with areas, sub-areas usually connect a place to another layer of the map, eg. a building <-> rooms inside it");
                    add(m_subareas);
                    
                    JMenuItem mi_sa_connect = new JMenuItem("Connect with place");
                    m_subareas.add(mi_sa_connect);
                    mi_sa_connect.setToolTipText("Connects another place to this place as sub-area");
                    mi_sa_connect.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            PlaceSelectionDialog dlg = new PlaceSelectionDialog((JFrame) parent.parent, parent.world, parent.get_cur_position(), true);
                            dlg.setVisible(true);
                            Place child = dlg.get_selection();
                            if(child != null && child != place){
                                int ret = JOptionPane.showConfirmDialog(parent, "Connect \"" + child.get_name() + "\" to \"" + place.get_name() + "\"?", "Connect sub-area", JOptionPane.YES_NO_OPTION);
                                if(ret == JOptionPane.YES_OPTION){
                                    place.connect_child(child);
                                    parent.redraw();
                                }
                            }
                        }
                    });
                    
                    JMenuItem mi_sa_new_layer = new JMenuItem("Add on new layer");
                    mi_sa_new_layer.setToolTipText("Creates a new place on a new layer and connects it with \"" + place.get_name() + "\" as a sub-area");
                    m_subareas.add(mi_sa_new_layer);
                    
                    HashSet<Place> children = place.get_children();
                    if(!children.isEmpty()){
                        JMenu m_sa_remove = new JMenu("Remove");
                        m_subareas.add(m_sa_remove);
                    
                        for(Place child: children){
                            JMenuItem mi_sa_remove = new JMenuItem("Remove " + child.get_name());
                            m_sa_remove.add(mi_sa_remove);
                            mi_sa_remove.addActionListener(new RemoveSubAreaActionListener(place, child));
                        }
                        
                        m_subareas.add(new JSeparator());
                        
                        for(Place child: children){
                            JMenuItem mi_sa_goto = new JMenuItem("Go to " + child.get_name());
                            m_subareas.add(mi_sa_goto);
                            mi_sa_goto.addActionListener(new GotoPlaceActionListener(parent, child));
                        }
                    }
                    
                }  else { // if layer doesn't exist or no place exists at position x,y
                    JMenuItem mi_new = new JMenuItem("New place");
                    mi_new.addActionListener(new PlaceDialog(parent.parent, parent.world, layer, px, py));
                    add(mi_new);
                    JMenuItem mi_placeholder = new JMenuItem("New placeholder");
                    add(mi_placeholder);
                    mi_placeholder.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent arg0) {
                            // creates a placeholder place
                            parent.world.put_placeholder(parent.get_cur_position().get_layer(), px, py);
                            parent.redraw();
                        }
                    });
                }
            }
            
            /**
             * redraws the world tab after the popup is closed
             */
            private class TabContextPopMenuListener implements PopupMenuListener {

                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
                    parent.set_context_menu(true);
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
                    parent.set_context_menu(false);
                    parent.redraw();
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent arg0) {
                    parent.set_context_menu(false);
                    parent.redraw();
                }
            }
            
            /**
             * Moves the map to the place, if action is performed
             */
            private class GotoPlaceActionListener implements ActionListener{
                WorldTab worldtab;
                Place place;
                
                public GotoPlaceActionListener(WorldTab _worldtab, Place _place){
                    worldtab = _worldtab;
                    place = _place;
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    if(place != null) worldtab.push_position(place.get_coordinate());
                }
            }
            
            /**
             * Removes a subarea child from a place, if action performed
             */
            private class RemoveSubAreaActionListener implements ActionListener{
                Place place, child;
                
                public RemoveSubAreaActionListener(Place _place, Place _child) {
                    place = _place;
                    child = _child;
                }
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(place != null && child != null) place.remove_child(child);
                }
            }
            
            /**
             * Connects a new path, if called
             */
            private class ConnectPathActionListener implements ActionListener{
                
                Place pl1, pl2;
                String dir1, dir2;

                public ConnectPathActionListener(Place _pl1, Place _pl2, String _dir1, String _dir2) {
                    pl1 = _pl1;
                    pl2 = _pl2;
                    dir1 = _dir1;
                    dir2 = _dir2;
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    pl1.connect_path(new Path(pl1, dir1, pl2, dir2));
                }
            }
            
            /**
             * removes a path, if called
             */
            private class RemovePathActionListener implements ActionListener{
                Path path;

                private RemovePathActionListener(Path _path) {
                    path = _path;
                }
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    path.remove();
                }   
            }
            
        }
    }
}
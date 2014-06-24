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
 *  This class constructs the main window and the available worlds tab. It also
 *  reads and writes the main config file
 */

package mudmap2.frontend;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import mudmap2.backend.World;
import mudmap2.backend.WorldManager;

/**
 * Main class for the mudmap window
 * @author neop
 */
public final class Mainwindow extends JFrame {
    
    static int config_file_version_major = 2;
    static int config_file_version_minor = 0;

    // Contains all opened maps <name, worldtab>
    HashMap<String, WorldTab> world_tabs;
    
    // GUI elements
    JMenuBar menu_bar;
    JMenu menu_file, menu_edit, menu_help;
    JMenuItem menu_file_new, menu_file_open, menu_file_save, menu_file_save_as_image, menu_file_quit;
    JMenuItem menu_edit_add_area, menu_edit_set_home_position, menu_edit_edit_world;
    JMenuItem menu_help_help, menu_help_info;
    
    JTabbedPane tabbed_pane;
    
    public Mainwindow(){
        super("MUD Map 2 " + "(" + mudmap2.Mudmap2.get_version_major() + "." + mudmap2.Mudmap2.get_version_minor() + "." + mudmap2.Mudmap2.get_version_build() + " " + mudmap2.Mudmap2.get_version_state() + ")");
        
        // set program icon
        try {
            URL url_icon = ClassLoader.getSystemResource("mudmap2/resources/mudmap.svg");
            Image icon = Toolkit.getDefaultToolkit().createImage(url_icon);
            setIconImage(icon);
        } catch(Exception e){
            System.out.println(e);
        }
        
        // create GUI
        world_tabs = new HashMap<String, WorldTab>();
        
        setSize(750, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new MainWindowListener());
        
        // Add GUI components
        menu_bar = new JMenuBar();
        add(menu_bar, BorderLayout.NORTH);
        
        menu_file = new JMenu("File");
        menu_bar.add(menu_file);
        menu_edit = new JMenu("Edit");
        menu_bar.add(menu_edit);
        menu_help = new JMenu("Help");
        menu_bar.add(menu_help);
        
        menu_file_new = new JMenuItem("New");
        menu_file.add(menu_file_new);
        menu_file_open = new JMenuItem("Open");
        menu_file.add(menu_file_open);
        menu_file.addSeparator();
        menu_file_save = new JMenuItem("Save");
        menu_file.add(menu_file_save);
        menu_file_save_as_image = new JMenuItem("Save as image");
        menu_file.add(menu_file_save_as_image);
        menu_file.addSeparator();
        menu_file_quit = new JMenuItem("Quit");
        menu_file.add(menu_file_quit);
        
        menu_edit_add_area = new JMenuItem("Add area");
        menu_edit.add(menu_edit_add_area);
        menu_edit_set_home_position = new JMenuItem("Set home position");
        menu_edit.add(menu_edit_set_home_position);
        menu_edit_edit_world = new JMenuItem("Edit world");
        menu_edit.add(menu_edit_edit_world);
        
        menu_help_help = new JMenuItem("Help");
        menu_help.add(menu_help_help);
        menu_help_info = new JMenuItem("Info");
        menu_help.add(menu_help_info);
        
        // ---
        read_config();
        
        // ---
        tabbed_pane = new JTabbedPane();
        add(tabbed_pane);
        tabbed_pane.addTab("Available worlds", new AvailableWorldsTab(this));
        
        setVisible(true);
    }
    
    /**
     * shows the tab of the world, opens the world if necessary
     * @param world_name world name
     */
    public void open_world(String world_name){
        if(!world_tabs.containsKey(world_name)){ 
            // open new tab
            WorldTab tab = new WorldTab(this, world_name);
            world_tabs.put(world_name, tab);
            tabbed_pane.addTab(tab.get_world_name(), tab);
        }
        // change current tab
        tabbed_pane.setSelectedComponent(world_tabs.get(world_name));
    }
    
    public void close_tabs(){
        for(WorldTab tab: world_tabs.values()){
            // TODO: implement dialog which asks the user if the world should be saved
            /*if(save_world) tab.save();
            else*/ 
            //tab.write_meta();
            tab.save();
            tabbed_pane.remove(tab);
        }
    }
    
    public void quit(){
        write_config();
        close_tabs();        
    }
    
    public void read_config(){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(mudmap2.Paths.get_config_file()));
            
            String line;
            int file_major = 0, file_minor = 0;
            
            try {    
                while((line = reader.readLine()) != null){
                    line = line.trim();
                    
                    if(line.startsWith("ver")){
                        String[] tmp = line.substring(4).split("\\.");
                        file_major = Integer.parseInt(tmp[0]);
                        file_minor = Integer.parseInt(tmp[1]);
                    } else if(line.startsWith("show_paths ")){ // show path lines
                        String[] tmp = line.split(" ");
                        WorldTab.set_show_paths(Boolean.parseBoolean(tmp[1]));
                    } else if(line.startsWith("show_paths_curved")){ // show curved path lines - if path lines are enabled
                        String[] tmp = line.split(" ");
                        WorldTab.set_show_paths_curved(Boolean.parseBoolean(tmp[1]));
                    } else if(line.startsWith("compat_mudmap_1")){ // save world files compatible to mudmap 1
                        String[] tmp = line.split(" ");
                        World.compatibility_mudmap_1 = Boolean.parseBoolean(tmp[1]);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Couldn't open config file \"" + mudmap2.Paths.get_config_file() + "\", file not found");
            Logger.getLogger(WorldManager.class.getName()).log(Level.INFO, null, ex);
        }
    }
    
    public void write_config(){
        try {
            // open file
            PrintWriter outstream = new PrintWriter(new BufferedWriter( new FileWriter(mudmap2.Paths.get_config_file())));

            outstream.println("# MUD Map 2 config file");
            outstream.println("ver " + config_file_version_major + "." + config_file_version_minor);
            outstream.println("show_paths " + WorldTab.get_show_paths());
            outstream.println("show_paths_curved " + WorldTab.get_show_paths_curved());
            outstream.println("compat_mudmap_1 " + World.compatibility_mudmap_1);
            
            outstream.close();
        } catch (IOException ex) {
            System.out.printf("Couldn't write config file " + mudmap2.Paths.get_config_file());
            Logger.getLogger(Mainwindow.class.getName()).log(Level.WARNING, null, ex);
        }
    }
    
    public class MainWindowListener implements WindowListener {

        @Override
        public void windowOpened(WindowEvent arg0) {}

        @Override
        public void windowClosing(WindowEvent arg0) {
            quit();
        }

        @Override
        public void windowClosed(WindowEvent arg0) {}

        @Override
        public void windowIconified(WindowEvent arg0) {}

        @Override
        public void windowDeiconified(WindowEvent arg0) {}

        @Override
        public void windowActivated(WindowEvent arg0) {}

        @Override
        public void windowDeactivated(WindowEvent arg0) {}
   
    }
    
    /**
     * The available worlds tab
     */
    private static class AvailableWorldsTab extends JPanel {

        // Reference to the main window
        Mainwindow mwin;
        
        public AvailableWorldsTab(Mainwindow _mwin) {
            mwin = _mwin;
            
            WorldManager.read_world_list();
            Set<String> worlds = WorldManager.get_world_list();
            
            setLayout(new GridLayout(worlds.size(), 2));
            
            for(String world_name: worlds){
                JButton b = new JButton(world_name);
                b.addActionListener(new ListenerButtonOpenWorld(mwin, world_name));
                add(b);
            }
        }
        
        /**
         * Opens a world tab (existing or creates it) when the corresponding
         * button is pressed
         */
        public class ListenerButtonOpenWorld implements ActionListener {

            Mainwindow mwin;
            String world_name;
            
            /**
             * Constructor
             * @param _mwin reference to the main window
             * @param _world_name name of the world to open
             */
            public ListenerButtonOpenWorld(Mainwindow _mwin, String _world_name){
                mwin = _mwin;
                world_name = _world_name;
            }
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                mwin.open_world(world_name);
            }   
        }   
    }
    
}

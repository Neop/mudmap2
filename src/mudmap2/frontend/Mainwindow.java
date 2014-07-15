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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.Collator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import mudmap2.Paths;
import mudmap2.backend.World;
import mudmap2.backend.WorldManager;
import mudmap2.frontend.dialog.AboutDialog;
import mudmap2.frontend.dialog.AreaDialog;
import mudmap2.frontend.dialog.EditWorldDialog;
import mudmap2.frontend.dialog.OpenWorldDialog;
import mudmap2.frontend.dialog.PlaceListDialog;

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
    JMenuItem menu_edit_add_area, menu_edit_set_home_position, menu_edit_goto_home_position, menu_edit_edit_world, menu_edit_list_places;
    JMenuItem menu_help_help, menu_help_about;
    
    JTabbedPane tabbed_pane;
    AvailableWorldsTab available_worlds_tab;
    
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
        
        setMinimumSize(new Dimension(400, 300));
        
        // create GUI
        world_tabs = new HashMap<String, WorldTab>();
        
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                quit();
            }
        });
        
        // Add GUI components
        menu_bar = new JMenuBar();
        add(menu_bar, BorderLayout.NORTH);
        
        menu_file = new JMenu("File");
        menu_bar.add(menu_file);
        menu_edit = new JMenu("World"); // Edit renamed to World
        menu_bar.add(menu_edit);
        menu_help = new JMenu("Help");
        menu_bar.add(menu_help);
        
        menu_file_new = new JMenuItem("New");
        menu_file.add(menu_file_new);
        menu_file_new.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {                
                String ret = JOptionPane.showInputDialog((Component) arg0.getSource(), "Enter new world name", "New world", JOptionPane.PLAIN_MESSAGE);
                if(ret != null && !ret.isEmpty()){
                    // create a new world
                    if(WorldManager.get_world_file(ret) == null) // no world with that name yet
                        try {
                        WorldManager.create_world(ret);
                        available_worlds_tab.update();
                        open_world(ret);
                    } catch (Exception ex) {
                        Logger.getLogger(Mainwindow.class.getName()).log(Level.SEVERE, null, ex);
                        JOptionPane.showMessageDialog((Component) arg0.getSource(), "Couldn't create world \"" + ret + "\":\n" + ex.getMessage());
                    }
                    else JOptionPane.showMessageDialog((Component) arg0.getSource(), "Can't create world \"" + ret + "\", name already exists");
                }
            }
        });
        
        menu_file_open = new JMenuItem("Open");
        menu_file.add(menu_file_open);
        menu_file_open.addActionListener(new OpenWorldDialog(this));
        
        menu_file.addSeparator();
        menu_file_save = new JMenuItem("Save");
        menu_file.add(menu_file_save);
        menu_file_save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                WorldTab wt = get_selected_tab();
                if(wt != null) wt.save();
            }
        });
        
        // TODO: implement save as image
        /*menu_file_save_as_image = new JMenuItem("Save as image");
        menu_file.add(menu_file_save_as_image);*/
        menu_file.addSeparator();
        menu_file_quit = new JMenuItem("Quit");
        menu_file.add(menu_file_quit);
        menu_file_quit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                quit();
            }
        });
        
        menu_edit_edit_world = new JMenuItem("Edit world");
        menu_edit.add(menu_edit_edit_world);
        menu_edit_edit_world.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WorldTab tab = get_selected_tab();
                if(tab != null){
                    (new EditWorldDialog(tab.parent, tab.get_world())).setVisible(true);
                }
            }
        });
        
        menu_edit_add_area = new JMenuItem("Add area");
        menu_edit.add(menu_edit_add_area);
        menu_edit_add_area.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                WorldTab wt = get_selected_tab();
                if(wt != null) (new AreaDialog(wt.parent, wt.get_world())).setVisible(true);
            }
        });
        
        menu_edit.add(new JSeparator());
        
        menu_edit_set_home_position = new JMenuItem("Set home position");
        menu_edit.add(menu_edit_set_home_position);
        menu_edit_set_home_position.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                WorldTab wt = get_selected_tab();
                if(wt != null) wt.set_home();
            }
        });
        menu_edit_goto_home_position = new JMenuItem("Go to home position");
        menu_edit.add(menu_edit_goto_home_position);
        menu_edit_goto_home_position.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                WorldTab wt = get_selected_tab();
                if(wt != null) wt.goto_home();
            }
        });
        
        menu_edit.add(new JSeparator());
        
        menu_edit_list_places = new JMenuItem("List places");
        menu_edit.add(menu_edit_list_places);
        menu_edit_list_places.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                WorldTab wt = get_selected_tab();
                // show place list
                if(wt != null) (new PlaceListDialog(wt, false)).setVisible(true);
            }
            
        });
        
        menu_help_help = new JMenuItem("Help (online)");
        menu_help.add(menu_help_help);
        menu_help_help.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                Paths.open_website(Paths.manual_url);
            }
        });
        
        menu_help_about = new JMenuItem("About");
        menu_help.add(menu_help_about);
        menu_help_about.addActionListener((ActionListener) new AboutDialog(this));
        
        // ---
        read_config();
        
        // ---
        tabbed_pane = new JTabbedPane();
        add(tabbed_pane);
        tabbed_pane.addTab("Available worlds", available_worlds_tab = new AvailableWorldsTab(this));
        
        setVisible(true);
    }
    
    /**
     * shows the tab of the world, opens the world if necessary
     * @param world_name world name
     */
    public void open_world(String world_name){
        setMinimumSize(new Dimension(500, 400));
        
        if(!world_tabs.containsKey(world_name)){ 
            // open new tab
            WorldTab tab = new WorldTab(this, world_name, false);
            world_tabs.put(world_name, tab);
            tabbed_pane.addTab(tab.get_world().get_name(), tab);
        }
        // change current tab
        tabbed_pane.setSelectedComponent(world_tabs.get(world_name));
    }
    
    /**
     * Closes all tabs
     */
    public void close_tabs(){
        for(WorldTab tab: world_tabs.values()) tab.close();
    }
    
    /**
     * Removes a tab without saving and closing the world in WorldManager
     * @param tab
     */
    public void remove_tab(WorldTab tab){
        tabbed_pane.remove(tab);
    }
    
    /**
     * Gets the currently shown WorldTab
     * @return WorldTab or null
     */
    private WorldTab get_selected_tab(){
        Component ret = tabbed_pane.getSelectedComponent();
        if(ret instanceof WorldTab) return (WorldTab) ret;
        else return null;
    }
    
    /**
     * Saves all config
     */
    public void quit(){
        write_config();
        close_tabs();
        WorldManager.write_world_list(); // do this after writing the world files
        System.exit(0);
    }
    
    /**
     * Reads MUD Map config file
     */
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
            //Logger.getLogger(WorldManager.class.getName()).log(Level.INFO, null, ex);
        }
    }
    
    /**
     * Writes MUD Map config file
     */
    public void write_config(){
        try {
            // open file
            if(!Paths.is_directory(Paths.get_user_data_dir())) Paths.create_directory(Paths.get_user_data_dir());
            PrintWriter outstream = new PrintWriter(new BufferedWriter( new FileWriter(Paths.get_config_file())));

            outstream.println("# MUD Map 2 config file");
            outstream.println("ver " + config_file_version_major + "." + config_file_version_minor);
            outstream.println("show_paths " + WorldTab.get_show_paths());
            outstream.println("show_paths_curved " + WorldTab.get_show_paths_curved());
            outstream.println("compat_mudmap_1 " + World.compatibility_mudmap_1);
            
            outstream.close();
        } catch (IOException ex) {
            System.out.printf("Couldn't write config file " + Paths.get_config_file());
            Logger.getLogger(Mainwindow.class.getName()).log(Level.WARNING, null, ex);
        }
    }
    
    /**
     * The available worlds tab shows a list off all known worlds
     */
    private static final class AvailableWorldsTab extends JPanel {

        // Reference to the main window
        final Mainwindow mwin;
        
        public AvailableWorldsTab(Mainwindow _mwin) {
            mwin = _mwin;
            WorldManager.read_world_list();
            update();
        }
        
        /**
         * Updates the tab (call this after creating a new world)
         */
        public void update(){
            // get and sort world names (can't use String array here :C)
            Object[] worlds = WorldManager.get_world_list().toArray();
            Arrays.sort(worlds, Collator.getInstance());
            
            // reset previously created tab
            removeAll();
            
            setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.insets = new Insets(2, 2, 2, 2);
            
            for(final Object world_name: worlds){
                JButton b = new JButton((String) world_name);
                b.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        mwin.open_world((String) world_name);
                    }
                });
                constraints.gridx = 0;
                constraints.gridy++;
                constraints.weightx = 1.0;
                constraints.fill = GridBagConstraints.HORIZONTAL;
                add(b, constraints);
                
                JButton r = new JButton("Delete");
                r.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        WorldManager.delete_world((String) world_name);
                    }
                });
                constraints.gridx = 1;
                constraints.weightx = 0.0;
                add(r, constraints);
            }
        }
    }
    
}

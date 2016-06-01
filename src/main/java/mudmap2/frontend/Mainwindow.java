/*  MUD Map (v2) - A tool to create and organize maps for text-based games
 *  Copyright (C) 2016  Neop (email: mneop@web.de)
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
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mudmap2.backend.World;
import mudmap2.backend.WorldFileList;
import mudmap2.backend.WorldManager;
import mudmap2.backend.html.GaardianMap;
import mudmap2.frontend.GUIElement.WorldPanel.MapPainterDefault;
import mudmap2.frontend.dialog.AboutDialog;
import mudmap2.frontend.dialog.AreaDialog;
import mudmap2.frontend.dialog.EditWorldDialog;
import mudmap2.frontend.dialog.ExportImageDialog;
import mudmap2.frontend.dialog.OpenWorldDialog;
import mudmap2.frontend.dialog.PathColorDialog;
import mudmap2.frontend.dialog.SaveWorldDialog;

/**
 * Main class for the mudmap window
 * call setVisible(true) to show window
 * @author neop
 */
public final class Mainwindow extends JFrame implements KeyEventDispatcher,ActionListener,ChangeListener {

    static Integer config_file_version_major = 2;
    static Integer config_file_version_minor = 0;
    private static final long serialVersionUID = 1L;

    // Contains all opened maps <name, worldtab>
    HashMap<World, WorldTab> worldTabs;

    // GUI elements
    JCheckBoxMenuItem menuEditCurvedPaths, menuEditShowCursor;

    JTabbedPane tabbedPane;
    AvailableWorldsTab availableWorldsTab;

    // for experimental html export message
    Boolean firstHtmlExport;

    public Mainwindow(){
        super("MUD Map " + Mainwindow.class.getPackage().getImplementationVersion());

        firstHtmlExport = true;

        setMinimumSize(new Dimension(400, 300));
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);

        ClassLoader classLoader = Mainwindow.class.getClassLoader();
        URL iconurl = classLoader.getResource("resources/mudmap-128.png");
        ImageIcon iconimage = new ImageIcon(iconurl);
        setIconImage(iconimage.getImage());

        // create GUI
        worldTabs = new HashMap<>();

        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                quit();
            }
        });

        initGui();

        // ---
        tabbedPane = new JTabbedPane();
        add(tabbedPane);
        tabbedPane.addTab("Available worlds", availableWorldsTab = new AvailableWorldsTab(this));
        tabbedPane.addChangeListener(this);
    }

    private void initGui() {
        // Add GUI components
        JMenuBar menu_bar = new JMenuBar();
        add(menu_bar, BorderLayout.NORTH);

        JMenu menu_file = new JMenu("File");
        menu_bar.add(menu_file);
        JMenu menu_edit = new JMenu("World"); // Edit renamed to World
        menu_bar.add(menu_edit);
        JMenu menu_help = new JMenu("Help");
        menu_bar.add(menu_help);

        JMenuItem menu_file_new = new JMenuItem("New");
        menu_file.add(menu_file_new);
        menu_file_new.setActionCommand("new_world");
        menu_file_new.addActionListener(this);

        JMenuItem menu_file_open = new JMenuItem("Open");
        menu_file_open.addActionListener(new OpenWorldDialog(this));
        menu_file.add(menu_file_open);

        menu_file.addSeparator();
        JMenuItem menu_file_save = new JMenuItem("Save");
        menu_file_save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        menu_file_save.setActionCommand("save_world");
        menu_file_save.addActionListener(this);
        menu_file.add(menu_file_save);

        JMenuItem menu_file_save_as = new JMenuItem("Save as");
        menu_file_save_as.setActionCommand("save_world_as");
        menu_file_save_as.addActionListener(this);
        menu_file.add(menu_file_save_as);

        JMenuItem menu_file_save_as_image = new JMenuItem("Export as image");
        menu_file_save_as_image.setActionCommand("export_image");
        menu_file_save_as_image.addActionListener(this);
        menu_file.add(menu_file_save_as_image);

        JMenuItem menu_file_save_as_html = new JMenuItem("Export as html");
        menu_file_save_as_html.setActionCommand("export_html");
        menu_file_save_as_html.addActionListener(this);
        menu_file.add(menu_file_save_as_html);

        menu_file.addSeparator();
        JMenuItem menu_file_quit = new JMenuItem("Quit");
        menu_file_quit.setActionCommand("quit");
        menu_file_quit.addActionListener(this);
        menu_file.add(menu_file_quit);

        JMenuItem menu_edit_edit_world = new JMenuItem("Edit world");
        menu_edit_edit_world.setActionCommand("edit_world");
        menu_edit_edit_world.addActionListener(this);
        menu_edit.add(menu_edit_edit_world);

        JMenuItem menu_edit_path_colors = new JMenuItem("Path colors");
        menu_edit_path_colors.setActionCommand("path_colors");
        menu_edit_path_colors.addActionListener(this);
        menu_edit.add(menu_edit_path_colors);

        JMenuItem menu_edit_add_area = new JMenuItem("Add area");
        menu_edit_add_area.setActionCommand("add_area");
        menu_edit_add_area.addActionListener(this);
        menu_edit.add(menu_edit_add_area);

        menu_edit.add(new JSeparator());

        JMenuItem menu_edit_set_home_position = new JMenuItem("Set home position");
        menu_edit_set_home_position.setActionCommand("set_home");
        menu_edit_set_home_position.addActionListener(this);
        menu_edit.add(menu_edit_set_home_position);

        JMenuItem menu_edit_goto_home_position = new JMenuItem("Go to home position");
        menu_edit_goto_home_position.setActionCommand("goto_home");
        menu_edit_goto_home_position.addActionListener(this);
        menu_edit.add(menu_edit_goto_home_position);

        menu_edit.add(new JSeparator());

        JMenuItem menu_edit_list_places = new JMenuItem("List places");
        menu_edit_list_places.setActionCommand("list_places");
        menu_edit_list_places.addActionListener(this);
        menu_edit.add(menu_edit_list_places);

        menu_edit.add(new JSeparator());

        menuEditCurvedPaths = new JCheckBoxMenuItem("Curved paths");
        menu_edit.add(menuEditCurvedPaths);
        menuEditCurvedPaths.addChangeListener(this);

        menuEditShowCursor = new JCheckBoxMenuItem("Show place cursor");
        menu_edit.add(menuEditShowCursor);
        menuEditShowCursor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0));
        menuEditShowCursor.addChangeListener(this);

        JMenuItem menu_help_help = new JMenuItem("Help (online)");
        menu_help_help.setActionCommand("help");
        menu_help_help.addActionListener(this);
        menu_help.add(menu_help_help);

        JMenuItem menu_help_about = new JMenuItem("About");
        menu_help.add(menu_help_about);
        menu_help_about.addActionListener((ActionListener) new AboutDialog(this));
    }

    public void createNewWorld(){
        String name = JOptionPane.showInputDialog(this, "Enter new world name", "New world", JOptionPane.PLAIN_MESSAGE);
        if(name != null && !name.isEmpty()){
            // create a new world
            try {
                World world = WorldManager.createWorld(name);
                availableWorldsTab.update();
                createTab(world, null);
            } catch (Exception ex) {
                Logger.getLogger(Mainwindow.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this, "Couldn't create world \"" + name + "\":\n" + ex.getMessage());
            }
        }
    }

    /**
     * create world tab
     * @param world
     * @param file world file or empty string / null
     */
    public void createTab(World world, String file){
        setMinimumSize(new Dimension(500, 400));

        if(!worldTabs.containsKey(world)){
            // open new tab
            WorldTab tab = new WorldTab(world, file, false);
            worldTabs.put(world, tab);
            tabbedPane.addTab(tab.getWorld().getName(), tab);
        }
        // change current tab
        tabbedPane.setSelectedComponent(worldTabs.get(world));

        WorldTab curTab = getSelectedTab();
        if(curTab != null){
            availableWorldsTab.update();

            // update menu entry
            menuEditShowCursor.setState(curTab.getWorldPanel().isCursorEnabled());
        }
    }

    /**
     * Closes all tabs
     */
    public void closeTabs(){
        for(WorldTab tab: worldTabs.values()) tab.close();
    }

    /**
     * Removes a tab without saving and closing the world in WorldManager
     * @param tab
     */
    public void removeTab(WorldTab tab){
        tabbedPane.remove(tab);
    }

    /**
     * Gets the currently shown WorldTab
     * @return WorldTab or null
     */
    private WorldTab getSelectedTab(){
        if(tabbedPane != null){
            Component ret = tabbedPane.getSelectedComponent();
            if(ret instanceof WorldTab) return (WorldTab) ret;
        }
        return null;
    }

    public JCheckBoxMenuItem getMiShowPlaceSelection(){
        return menuEditShowCursor;
    }

    /**
     * Saves all config
     */
    public void quit(){
        closeTabs();
        WorldFileList.writeWorldList();
        System.exit(0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        WorldTab wt = getSelectedTab();
        switch(e.getActionCommand()){
            case "new_world":
                createNewWorld();
                break;
            case "save_world":
                if(wt != null) wt.save();
                break;
            case "save_world_as":
                if(wt != null){
                    SaveWorldDialog dlg = new SaveWorldDialog(Mainwindow.this, wt);
                    dlg.setVisible(true);
                }
                break;
            case "export_image":
                if(wt != null){
                    ExportImageDialog dlg = new ExportImageDialog(Mainwindow.this, wt);
                    dlg.setVisible(true);
                }
                break;
            case "export_html":
                if(wt != null){
                    if(firstHtmlExport){
                        JOptionPane.showMessageDialog(Mainwindow.this, "The html export is experimental, some paths might not show up on the exported map. Thanks to gaardian.com for the html/js code!");
                        firstHtmlExport = false;
                    }

                    JFileChooser fc = new JFileChooser();
                    int retVal = fc.showSaveDialog(Mainwindow.this);
                    if(retVal == JFileChooser.APPROVE_OPTION){
                        String filename = fc.getSelectedFile().getAbsolutePath();
                        if(!filename.endsWith(".html")) filename = filename + ".html";
                        System.out.println(">>>> " + filename);
                        GaardianMap.writeFile(filename,
                                wt.getWorld().getLayer(wt.getWorldPanel().getPosition().getLayer()));
                    }
                }
                break;
            case "quit":
                quit();
                break;
            case "edit_world":
                if(wt != null){
                    (new EditWorldDialog(Mainwindow.this, wt.getWorld())).setVisible(true);
                            availableWorldsTab.update();
                }
                break;
            case "path_colors":
                if(wt != null){
                    (new PathColorDialog(Mainwindow.this, wt.getWorld())).setVisible(true);
                    wt.repaint();
                }
                break;
            case "add_area":
                if(wt != null) (new AreaDialog(Mainwindow.this, wt.getWorld())).setVisible(true);
                break;
            case "set_home": // set home position
                if(wt != null) wt.getWorldPanel().setHome();
                break;
            case "goto_home": // go to home position
                if(wt != null) wt.getWorldPanel().gotoHome();
                break;
            default:
                String message = getClass().getName() + ": ActionCommand not recognized";
                Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, message);
                JOptionPane.showMessageDialog(this, message, "MUD Map error", JOptionPane.ERROR_MESSAGE);
                break;
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        WorldTab wt = getSelectedTab();
        if(e.getSource() == menuEditCurvedPaths){
            if(wt != null){
                MapPainterDefault mapPainter = (MapPainterDefault) wt.getWorldPanel().getMappainter();
                mapPainter.setPathsCurved(((JCheckBoxMenuItem) e.getSource()).isSelected());
                wt.repaint();
            }
        } else if(e.getSource() == menuEditShowCursor){
            if(wt != null){
                wt.getWorldPanel().setCursorEnabled(((JCheckBoxMenuItem) e.getSource()).isSelected());
                wt.repaint();
            }
        } else if(e.getSource() == tabbedPane){ // tab changed
            if(wt != null){
                wt.getWorldPanel().callStatusUpdateListeners();
                menuEditCurvedPaths.setState(((MapPainterDefault) wt.getWorldPanel().getMappainter()).getPathsCurved());
            }
        } else {
            String message = getClass().getName() + ": ChangeEvent not recognized";
            Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, message);
            JOptionPane.showMessageDialog(this, message, "MUD Map error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if(KeyEvent.KEY_PRESSED == e.getID() && e.isControlDown()){
            switch(e.getKeyCode()){
                case KeyEvent.VK_S:
                    WorldTab wt = getSelectedTab();
                    if(wt != null){
                        wt.save();
                    }
                    return true;
                case KeyEvent.VK_O:
                    OpenWorldDialog dlg = new OpenWorldDialog(this);
                    dlg.setVisible();
                    return true;
            }
        }
        return false;
    }

}

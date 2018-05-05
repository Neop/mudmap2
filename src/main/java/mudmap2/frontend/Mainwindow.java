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
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mudmap2.backend.World;
import mudmap2.backend.WorldFileList;
import mudmap2.backend.WorldManager;
import mudmap2.frontend.GUIElement.WorldPanel.MapPainterDefault;
import mudmap2.frontend.dialog.AboutDialog;
import mudmap2.frontend.dialog.EditWorldDialog;
import mudmap2.frontend.dialog.ExportImageDialog;
import mudmap2.frontend.dialog.KeyboardShortcutDialog;
import mudmap2.frontend.dialog.OpenWorldDialog;
import mudmap2.frontend.dialog.placeGroup.PlaceGroupListDialog;
import mudmap2.frontend.dialog.QuickHelpDialog;
import mudmap2.frontend.dialog.SaveWorldDialog;
import mudmap2.frontend.dialog.UpdateDialog;
import mudmap2.frontend.dialog.pathColor.PathColorListDialog;
import mudmap2.frontend.dialog.riskLevel.RiskLevelListDialog;

/**
 * Main class for the mudmap window
 * call setVisible(true) to show window
 * @author neop
 */
public final class Mainwindow extends JFrame implements KeyEventDispatcher,ActionListener,ChangeListener {

    private static final long serialVersionUID = 1L;

    // Contains all opened maps <name, worldtab>
    HashMap<World, WorldTab> worldTabs;

    // GUI elements
    JCheckBoxMenuItem menuEditCurvedPaths;
    JCheckBoxMenuItem menuEditShowCursor;
    JCheckBoxMenuItem menuEditShowGrid;

    JMenuItem menuFileSave;
    JMenuItem menuFileSaveAs;
    JMenuItem menuFileSaveAsImage;

    JMenuItem menuEditEditWorld;
    JMenuItem menuEditPathColors;
    JMenuItem menuEditPlaceGroups;
    JMenuItem menuEditRiskLevels;

    JMenuItem menuEditSetHomePosition;
    JMenuItem menuEditGotoHomePosition;

    JTabbedPane tabbedPane = null;
    JPanel infoPanel = null;

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
    }

    private void initGui() {
        // Add GUI components
        JMenuBar menuBar = new JMenuBar();
        add(menuBar, BorderLayout.NORTH);

        JMenu menuFile = new JMenu("File");
        menuBar.add(menuFile);
        JMenu menuEdit = new JMenu("World"); // Edit renamed to World
        menuBar.add(menuEdit);
        JMenu menuHelp = new JMenu("Help");
        menuBar.add(menuHelp);

        JMenuItem menuFileNew = new JMenuItem("New");
        menuFile.add(menuFileNew);
        menuFileNew.setActionCommand("new_world");
        menuFileNew.addActionListener(this);

        JMenuItem menuFileOpen = new JMenuItem("Open");
        menuFileOpen.addActionListener(new OpenWorldDialog(this));
        menuFile.add(menuFileOpen);

        // available worlds
        JMenu menuFileOpenRecent = new JMenu("Open recent world");
        menuFile.add(menuFileOpenRecent);

        WorldFileList.read();
        for(final WorldFileList.WorldFileEntry entry: WorldFileList.getEntries()){
            JMenuItem openWorldEntry = new JMenuItem(entry.getWorldName() + " (" + entry.getFile() + ")");
            menuFileOpenRecent.add(openWorldEntry);
            openWorldEntry.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    try {
                        createTab(WorldManager.getWorld(entry.getFile().getAbsolutePath()));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(getParent(), "Could not open world: " + ex.getMessage());
                        Logger.getLogger(Mainwindow.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        }

        menuFile.addSeparator();
        menuFileSave = new JMenuItem("Save");
        menuFileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        menuFileSave.setActionCommand("save_world");
        menuFileSave.addActionListener(this);
        menuFile.add(menuFileSave);

        menuFileSaveAs = new JMenuItem("Save as");
        menuFileSaveAs.setActionCommand("save_world_as");
        menuFileSaveAs.addActionListener(this);
        menuFile.add(menuFileSaveAs);

        menuFileSaveAsImage = new JMenuItem("Export as image");
        menuFileSaveAsImage.setActionCommand("export_image");
        menuFileSaveAsImage.addActionListener(this);
        menuFile.add(menuFileSaveAsImage);

        menuFile.addSeparator();
        JMenuItem menuFileQuit = new JMenuItem("Quit");
        menuFileQuit.setActionCommand("quit");
        menuFileQuit.addActionListener(this);
        menuFile.add(menuFileQuit);

        menuEditEditWorld = new JMenuItem("Edit world");
        menuEditEditWorld.setActionCommand("edit_world");
        menuEditEditWorld.addActionListener(this);
        menuEdit.add(menuEditEditWorld);

        menuEditPathColors = new JMenuItem("Path colors");
        menuEditPathColors.setActionCommand("path_colors");
        menuEditPathColors.addActionListener(this);
        menuEdit.add(menuEditPathColors);

        menuEditPlaceGroups = new JMenuItem("Place groups");
        menuEditPlaceGroups.setActionCommand("place_group_dialog");
        menuEditPlaceGroups.addActionListener(this);
        menuEdit.add(menuEditPlaceGroups);

        menuEditRiskLevels = new JMenuItem("Risk levels");
        menuEditRiskLevels.setActionCommand("risk_level_dialog");
        menuEditRiskLevels.addActionListener(this);
        menuEdit.add(menuEditRiskLevels);

        menuEdit.add(new JSeparator());

        menuEditSetHomePosition = new JMenuItem("Set home position");
        menuEditSetHomePosition.setActionCommand("set_home");
        menuEditSetHomePosition.addActionListener(this);
        menuEdit.add(menuEditSetHomePosition);

        menuEditGotoHomePosition = new JMenuItem("Go to home position");
        menuEditGotoHomePosition.setActionCommand("goto_home");
        menuEditGotoHomePosition.addActionListener(this);
        menuEdit.add(menuEditGotoHomePosition);

        menuEdit.add(new JSeparator());

        menuEditCurvedPaths = new JCheckBoxMenuItem("Curved paths");
        menuEdit.add(menuEditCurvedPaths);
        menuEditCurvedPaths.addChangeListener(this);

        menuEditShowCursor = new JCheckBoxMenuItem("Show place cursor");
        menuEdit.add(menuEditShowCursor);
        menuEditShowCursor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0));
        menuEditShowCursor.addChangeListener(this);

        menuEditShowGrid = new JCheckBoxMenuItem("Show grid");
        menuEdit.add(menuEditShowGrid);
        menuEditShowGrid.addChangeListener(this);

        JMenuItem menuHelpKeyboardShortcuts = new JMenuItem("Keyboard Shortcuts");
        menuHelp.add(menuHelpKeyboardShortcuts);
        menuHelpKeyboardShortcuts.addActionListener((ActionListener) new KeyboardShortcutDialog(this));

        JMenuItem menuHelpQuickstart = new JMenuItem("Quickstart");
        menuHelp.add(menuHelpQuickstart);
        menuHelpQuickstart.addActionListener((ActionListener) new QuickHelpDialog(this));

        menuHelp.add(new JSeparator());

        JMenuItem menuHelpCheckUpdates = new JMenuItem("Check for updates");
        menuHelp.add(menuHelpCheckUpdates);
        menuHelpCheckUpdates.addActionListener((ActionListener) new UpdateDialog(this));

        JMenuItem menuHelpAbout = new JMenuItem("About");
        menuHelp.add(menuHelpAbout);
        menuHelpAbout.addActionListener((ActionListener) new AboutDialog(this));

        BorderLayout infoPanelLayout = new BorderLayout();
        infoPanel = new JPanel(infoPanelLayout);
        add(infoPanel, BorderLayout.CENTER);
        infoPanel.add(new JLabel("Load or create a world in the File menu.", SwingConstants.CENTER));

        updateMenus();
    }

    public void createNewWorld(){
        String name = JOptionPane.showInputDialog(this, "Enter new world name", "New world", JOptionPane.PLAIN_MESSAGE);
        if(name != null && !name.isEmpty()){
            // create a new world
            try {
                World world = WorldManager.getNewWorld(name);
                createTab(world);
            } catch (Exception ex) {
                Logger.getLogger(Mainwindow.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this, "Couldn't create world \"" + name + "\":\n" + ex.getMessage());
            }
        }
    }

    /**
     * create world tab
     * @param world
     */
    public void createTab(World world){
        setMinimumSize(new Dimension(500, 400));

        if(tabbedPane == null){
            remove(infoPanel);
            tabbedPane = new JTabbedPane();
            add(tabbedPane);
            tabbedPane.addChangeListener(this);
        }

        if(!worldTabs.containsKey(world)){
            // open new tab
            WorldTab tab = new WorldTab(this, world, false);
            worldTabs.put(world, tab);
            tabbedPane.addTab(tab.getWorld().getName(), tab);
        }
        // change current tab
        tabbedPane.setSelectedComponent(worldTabs.get(world));

        WorldTab curTab = getSelectedTab();
        if(curTab != null){
            // update menu entry
            menuEditShowCursor.setState(curTab.getWorldPanel().isCursorEnabled());
        }

        if(world.getWorldFile() != null){
            WorldFileList.push(new WorldFileList.WorldFileEntry(world.getName(), new File(world.getWorldFile().getFilename())));
        }
    }

    /**
     * Closes all tabs
     */
    public void closeTabs(){
        for(WorldTab tab: worldTabs.values()){
            int ret = JOptionPane.showConfirmDialog(this, "Save world \"" + tab.getWorld().getName() + "\"?", "Save world", JOptionPane.YES_NO_OPTION);
            if(ret == JOptionPane.YES_OPTION) tab.save();
            WorldManager.close(tab.getWorld());
            removeTab(tab);
        }
    }

    /**
     * Removes a tab without saving and closing the world in WorldManager
     * @param tab
     */
    public void removeTab(WorldTab tab){
        if(tabbedPane != null) tabbedPane.remove(tab);
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
        WorldFileList.write();
        System.exit(0);
    }

    /**
     * Updates menu items when the tab changes
     */
    private void updateMenus(){
        boolean enabled = tabbedPane != null &&
                tabbedPane.getSelectedComponent() instanceof WorldTab;
        menuFileSave.setEnabled(enabled);
        menuFileSaveAs.setEnabled(enabled);
        menuFileSaveAsImage.setEnabled(enabled);

        //menuEditCurvedPaths.setEnabled(enabled);
        menuEditEditWorld.setEnabled(enabled);
        menuEditGotoHomePosition.setEnabled(enabled);
        menuEditPathColors.setEnabled(enabled);
        menuEditSetHomePosition.setEnabled(enabled);
        menuEditPlaceGroups.setEnabled(enabled);
        menuEditRiskLevels.setEnabled(enabled);
        //menuEditShowCursor.setEnabled(enabled);
        //menuEditShowGrid.setEnabled(enabled);
        /* TODO: deactivated for toggle buttons since they did not accept
        *  correct state
        */

        /*WorldTab selectedTab = getSelectedTab();
        if(selectedTab != null){
            MapPainterDefault mapPainter = ((MapPainterDefault) selectedTab.getWorldPanel().getMappainter());
            menuEditShowCursor.setState(selectedTab.getWorldPanel().isCursorEnabled());
            menuEditShowGrid.setState(mapPainter.isGridEnabled());
            menuEditCurvedPaths.setState(mapPainter.getPathsCurved());
        }*/
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
                    int ret = dlg.showSaveDialog(wt);
                    if(ret == JFileChooser.APPROVE_OPTION){
                        wt.getWorld().setWorldFile(dlg.getWorldFile());
                        wt.save();
                    }
                }
                break;
            case "export_image":
                if(wt != null){
                    ExportImageDialog dlg = new ExportImageDialog(Mainwindow.this, wt);
                    dlg.setVisible(true);
                }
                break;
            case "quit":
                quit();
                break;
            case "edit_world":
                if(wt != null){
                    (new EditWorldDialog(Mainwindow.this, wt.getWorld())).setVisible(true);
                }
                break;
            case "path_colors":
                if(wt != null){
                    (new PathColorListDialog(Mainwindow.this, wt.getWorld())).setVisible(true);
                }
                break;
            case "place_group_dialog":
                if(wt != null){
                    (new PlaceGroupListDialog(Mainwindow.this, wt.getWorld())).setVisible(true);
                }
                break;
            case "risk_level_dialog":
                if(wt != null){
                    (new RiskLevelListDialog(Mainwindow.this, wt.getWorld())).setVisible(true);
                }
                break;
            case "set_home": // set home position
                if(wt != null){
                    wt.getWorldPanel().setHome();
                }
                break;
            case "goto_home": // go to home position
                if(wt != null){
                    wt.getWorldPanel().gotoHome();
                }
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
        } else if(e.getSource() == menuEditShowGrid){
            if(wt != null){
                MapPainterDefault mapPainter = (MapPainterDefault) wt.getWorldPanel().getMappainter();
                mapPainter.setGridEnabled(((JCheckBoxMenuItem) e.getSource()).isSelected());
                wt.repaint();
            }
        } else if(tabbedPane != null && e.getSource() == tabbedPane){ // tab changed
            if(wt != null){
                wt.getWorldPanel().callStatusUpdateListeners();
                menuEditCurvedPaths.setState(((MapPainterDefault) wt.getWorldPanel().getMappainter()).getPathsCurved());
                menuEditShowGrid.setState(((MapPainterDefault) wt.getWorldPanel().getMappainter()).isGridEnabled());
                menuEditShowCursor.setState(wt.getWorldPanel().isCursorEnabled());
            }
        } else {
            String message = getClass().getName() + ": ChangeEvent not recognized";
            Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, message);
            JOptionPane.showMessageDialog(this, message, "MUD Map error", JOptionPane.ERROR_MESSAGE);
        }
        updateMenus();
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

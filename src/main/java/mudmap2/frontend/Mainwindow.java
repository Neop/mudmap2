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
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mudmap2.backend.World;
import mudmap2.backend.WorldFileList;
import mudmap2.backend.WorldFileList.WorldFileEntry;
import mudmap2.backend.WorldManager;
import mudmap2.frontend.GUIElement.WorldPanel.MapPainterDefault;
import mudmap2.frontend.dialog.AboutDialog;
import mudmap2.frontend.dialog.EditWorldDialog;
import mudmap2.frontend.dialog.ExportImageDialog;
import mudmap2.frontend.dialog.KeyboardShortcutDialog;
import mudmap2.frontend.dialog.OpenWorldDialog;
import mudmap2.frontend.dialog.QuickHelpDialog;
import mudmap2.frontend.dialog.SaveWorldDialog;
import mudmap2.frontend.dialog.UpdateDialog;
import mudmap2.frontend.dialog.pathColor.PathColorListDialog;
import mudmap2.frontend.dialog.placeGroup.PlaceGroupListDialog;
import mudmap2.frontend.dialog.informationColor.InformationColorListDialog;
import mudmap2.utils.KeystrokeHelper;
import mudmap2.utils.MenuHelper;
import mudmap2.utils.StringHelper;

/**
 * Main class for the mudmap window
 * call setVisible(true) to show window
 * @author neop
 */
public final class Mainwindow extends JFrame implements KeyEventDispatcher, ActionListener, ChangeListener {

    private static final long serialVersionUID = 1L;

    // Contains all opened maps <name, worldtab>
    HashMap<World, WorldTab> worldTabs;

    // GUI elements
    JCheckBoxMenuItem menuWorldCurvedPaths;
    JCheckBoxMenuItem menuWorldShowCursor;
    JCheckBoxMenuItem menuWorldShowGrid;

    JMenuItem menuFileSave;
    JMenuItem menuFileSaveAs;
    JMenuItem menuFileSaveAsImage;

    JMenuItem menuWorldEditWorld;
    JMenuItem menuWorldPathColors;
    JMenuItem menuWorldPlaceGroups;
    JMenuItem menuWorldRiskLevels;

    JMenuItem menuWorldSetHomePosition;
    JMenuItem menuWorldGotoHomePosition;

    JTabbedPane tabbedPane = null;
    JPanel infoPanel = null;

    public Mainwindow() {
        super(StringHelper.join("MUD Map ", Mainwindow.class.getPackage().getImplementationVersion()));

        setMinimumSize(new Dimension(400, 300));
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);

        final ClassLoader classLoader = Mainwindow.class.getClassLoader();
        final URL iconurl = classLoader.getResource("resources/mudmap-128.png");
        final ImageIcon iconimage = new ImageIcon(iconurl);
        setIconImage(iconimage.getImage());

        // create GUI
        worldTabs = new HashMap<>();

        setSize(900, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent arg0) {
                quit();
            }
        });

        initGui();
    }

    private ActionListener newWorldFileEntryActionListener(final WorldFileEntry entry) {
        return new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent ae) {
                try {
                    createTab(WorldManager.getWorld(entry.getFile().getAbsolutePath()));
                } catch (final Exception ex) {
                    JOptionPane.showMessageDialog(getParent(), StringHelper.join("Could not open world: ", ex.getMessage()));
                    Logger.getLogger(Mainwindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
    }

    private void initGui() {
        // Add GUI components
        final JMenuBar menuBar = new JMenuBar();
        add(menuBar, BorderLayout.NORTH);

        //menu bar
        final JMenu menuFile = MenuHelper.addMenu(menuBar, "File", KeyEvent.VK_F);
        final JMenu menuWorld = MenuHelper.addMenu(menuBar, "World", KeyEvent.VK_W);
        final JMenu menuHelp = MenuHelper.addMenu(menuBar, "Help", KeyEvent.VK_H);

        //menu entries: file
        MenuHelper.addMenuItem(menuFile, "New", "new_world", KeyEvent.VK_N, KeystrokeHelper.ctrl(KeyEvent.VK_N), this);
        MenuHelper.addMenuItem(menuFile, "Open", KeyEvent.VK_O, KeystrokeHelper.ctrl(KeyEvent.VK_O), new OpenWorldDialog(this));
        final JMenu menuFileOpenRecent = MenuHelper.addMenu(menuFile, "Open Recent World", KeyEvent.VK_R);
        menuFile.addSeparator();
        menuFileSave = MenuHelper.addMenuItem(menuFile, "Save", "save_world", KeyEvent.VK_S, KeystrokeHelper.ctrl(KeyEvent.VK_S), this);
        menuFileSaveAs = MenuHelper.addMenuItem(menuFile, "Save As...", "save_world_as", KeystrokeHelper.ctrlAlt(KeyEvent.VK_S), this);
        menuFileSaveAsImage = MenuHelper.addMenuItem(menuFile, "Export As Image", "export_image", KeyEvent.VK_E, KeystrokeHelper.ctrl(KeyEvent.VK_E), this);
        menuFile.addSeparator();
        MenuHelper.addMenuItem(menuFile, "Quit", "quit", KeyEvent.VK_Q, KeystrokeHelper.ctrl(KeyEvent.VK_Q), this);

        //menu entries: file/open recent
        WorldFileList.read();
        for (final WorldFileList.WorldFileEntry entry : WorldFileList.getEntries()) {
            final String label = StringHelper.join(entry.getWorldName(), " (", entry.getFile(), ")");
            final ActionListener actionListener = newWorldFileEntryActionListener(entry);
            MenuHelper.addMenuItem(menuFileOpenRecent, label, actionListener);
        }

        //menu entries: World
        menuWorldEditWorld = MenuHelper.addMenuItem(menuWorld, "Edit World", "edit_world", this);
        menuWorldPathColors = MenuHelper.addMenuItem(menuWorld, "Path colors", "path_colors", this);
        menuWorldPlaceGroups = MenuHelper.addMenuItem(menuWorld, "Place Groups", "place_group_dialog", this);
        menuWorldRiskLevels = MenuHelper.addMenuItem(menuWorld, "Risk Levels", "risk_level_dialog", this);
        menuWorld.addSeparator();
        menuWorldSetHomePosition = MenuHelper.addMenuItem(menuWorld, "Set Home Position", "set_home", this);
        menuWorldGotoHomePosition = MenuHelper.addMenuItem(menuWorld, "Go to Home Position", "goto_home", this);
        menuWorld.addSeparator();
        menuWorldCurvedPaths = MenuHelper.addCheckboxMenuItem(menuWorld, "Curved Paths", this);
        menuWorldShowCursor = MenuHelper.addCheckboxMenuItem(menuWorld, "Show Place Cursor", KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), this);
        menuWorldShowGrid = MenuHelper.addCheckboxMenuItem(menuWorld, "Show Grid", this);

        //menu entries: Help
        MenuHelper.addMenuItem(menuHelp, "Keyboard Shortcuts", KeyEvent.VK_K, new KeyboardShortcutDialog(this));
        MenuHelper.addMenuItem(menuHelp, "Quickstart", KeyEvent.VK_Q, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), new QuickHelpDialog(this));
        menuHelp.addSeparator();
        MenuHelper.addMenuItem(menuHelp, "Check for Updates", KeyEvent.VK_U, new UpdateDialog(this));
        MenuHelper.addMenuItem(menuHelp, "About", KeyEvent.VK_A, new AboutDialog(this));

        infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(new JLabel("Load or create a world in the File menu.", SwingConstants.CENTER));
        add(infoPanel, BorderLayout.CENTER);

        updateMenus();
    }

    public void createNewWorld() {
        final String name = JOptionPane.showInputDialog(this, "Enter new world name", "New world", JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.isEmpty()) {
            // create a new world
            try {
                final World world = WorldManager.getNewWorld(name);
                createTab(world);
            } catch (final Exception ex) {
                Logger.getLogger(Mainwindow.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this, StringHelper.join("Couldn't create world \"", name, "\":\n", ex.getMessage()));
            }
        }
    }

    /**
     * create world tab
     * @param world
     */
    public void createTab(final World world) {
        setMinimumSize(new Dimension(500, 400));

        if (tabbedPane == null) {
            remove(infoPanel);
            tabbedPane = new JTabbedPane();
            add(tabbedPane);
            tabbedPane.addChangeListener(this);
        }

        if (!worldTabs.containsKey(world)) {
            // open new tab
            final WorldTab tab = new WorldTab(this, world, false);
            worldTabs.put(world, tab);
            tabbedPane.addTab(tab.getWorld().getName(), tab);
        }
        // change current tab
        tabbedPane.setSelectedComponent(worldTabs.get(world));

        final WorldTab curTab = getSelectedTab();
        if (curTab != null) {
            // update menu entry
            menuWorldShowCursor.setState(curTab.getWorldPanel().isCursorEnabled());
        }

        if (world.getWorldFile() != null) {
            WorldFileList.push(new WorldFileList.WorldFileEntry(world.getName(), new File(world.getWorldFile().getFilename())));
        }
    }

    /**
     * Closes all tabs
     */
    public void closeTabs() {
        for (final WorldTab tab : worldTabs.values()) {
            final int ret = JOptionPane.showConfirmDialog(this, StringHelper.join("Save world \"", tab.getWorld().getName(), "\"?"), "Save world", JOptionPane.YES_NO_OPTION);
            if (ret == JOptionPane.YES_OPTION) {
                tab.save();
            }
            WorldManager.close(tab.getWorld());
            removeTab(tab);
        }
    }

    /**
     * Removes a tab without saving and closing the world in WorldManager
     * @param tab
     */
    public void removeTab(final WorldTab tab) {
        if (tabbedPane != null) {
            tabbedPane.remove(tab);
        }
    }

    /**
     * Gets the currently shown WorldTab
     * @return WorldTab or null
     */
    private WorldTab getSelectedTab() {
        if (tabbedPane != null) {
            final Component ret = tabbedPane.getSelectedComponent();
            if (ret instanceof WorldTab) {
                return (WorldTab) ret;
            }
        }
        return null;
    }

    public JCheckBoxMenuItem getMiShowPlaceSelection() {
        return menuWorldShowCursor;
    }

    /**
     * Saves all config
     */
    public void quit() {
        closeTabs();
        WorldFileList.write();
        System.exit(0);
    }

    /**
     * Updates menu items when the tab changes
     */
    private void updateMenus() {
        final boolean enabled = tabbedPane != null && tabbedPane.getSelectedComponent() instanceof WorldTab;
        menuFileSave.setEnabled(enabled);
        menuFileSaveAs.setEnabled(enabled);
        menuFileSaveAsImage.setEnabled(enabled);

        //menuWorldCurvedPaths.setEnabled(enabled);
        menuWorldEditWorld.setEnabled(enabled);
        menuWorldGotoHomePosition.setEnabled(enabled);
        menuWorldPathColors.setEnabled(enabled);
        menuWorldSetHomePosition.setEnabled(enabled);
        menuWorldPlaceGroups.setEnabled(enabled);
        menuWorldRiskLevels.setEnabled(enabled);
        //menuWorldShowCursor.setEnabled(enabled);
        //menuWorldShowGrid.setEnabled(enabled);
        /* TODO: deactivated for toggle buttons since they did not accept
        *  correct state
        */

        /*WorldTab selectedTab = getSelectedTab();
        if(selectedTab != null){
            MapPainterDefault mapPainter = ((MapPainterDefault) selectedTab.getWorldPanel().getMappainter());
            menuWorldShowCursor.setState(selectedTab.getWorldPanel().isCursorEnabled());
            menuWorldShowGrid.setState(mapPainter.isGridEnabled());
            menuWorldCurvedPaths.setState(mapPainter.getPathsCurved());
        }*/
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final WorldTab wt = getSelectedTab();
        switch (e.getActionCommand()) {
        case "new_world":
            createNewWorld();
            break;
        case "save_world":
            if (wt != null) {
                wt.save();
            }
            break;
        case "save_world_as":
            if (wt != null) {
                final SaveWorldDialog dlg = new SaveWorldDialog(Mainwindow.this, wt);
                final int ret = dlg.showSaveDialog(wt);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    wt.getWorld().setWorldFile(dlg.getWorldFile());
                    wt.save();
                }
            }
            break;
        case "export_image":
            if (wt != null) {
                final ExportImageDialog dlg = new ExportImageDialog(Mainwindow.this, wt);
                dlg.setVisible(true);
            }
            break;
        case "quit":
            quit();
            break;
        case "edit_world":
            if (wt != null) {
                new EditWorldDialog(Mainwindow.this, wt.getWorld()).setVisible(true);
            }
            break;
        case "path_colors":
            if (wt != null) {
                new PathColorListDialog(Mainwindow.this, wt.getWorld()).setVisible(true);
            }
            break;
        case "place_group_dialog":
            if (wt != null) {
                new PlaceGroupListDialog(Mainwindow.this, wt.getWorld()).setVisible(true);
            }
            break;
        case "risk_level_dialog":
            if (wt != null) {
                new InformationColorListDialog(Mainwindow.this, wt.getWorld()).setVisible(true);
            }
            break;
        case "set_home": // set home position
            if (wt != null) {
                wt.getWorldPanel().setHome();
            }
            break;
        case "goto_home": // go to home position
            if (wt != null) {
                wt.getWorldPanel().gotoHome();
            }
            break;
        default:
            final String message = StringHelper.join(getClass().getName(), ": ActionCommand not recognized");
            Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, message);
            JOptionPane.showMessageDialog(this, message, "MUD Map error", JOptionPane.ERROR_MESSAGE);
            break;
        }
    }

    @Override
    public void stateChanged(final ChangeEvent e) {
        final WorldTab wt = getSelectedTab();
        if (e.getSource() == menuWorldCurvedPaths) {
            if (wt != null) {
                final MapPainterDefault mapPainter = (MapPainterDefault) wt.getWorldPanel().getMappainter();
                mapPainter.setPathsCurved(((JCheckBoxMenuItem) e.getSource()).isSelected());
                wt.repaint();
            }
        } else if (e.getSource() == menuWorldShowCursor) {
            if (wt != null) {
                wt.getWorldPanel().setCursorEnabled(((JCheckBoxMenuItem) e.getSource()).isSelected());
                wt.repaint();
            }
        } else if (e.getSource() == menuWorldShowGrid) {
            if (wt != null) {
                final MapPainterDefault mapPainter = (MapPainterDefault) wt.getWorldPanel().getMappainter();
                mapPainter.setGridEnabled(((JCheckBoxMenuItem) e.getSource()).isSelected());
                wt.repaint();
            }
        } else if (tabbedPane != null && e.getSource() == tabbedPane) { // tab changed
            if (wt != null) {
                wt.getWorldPanel().callStatusUpdateListeners();
                menuWorldCurvedPaths.setState(((MapPainterDefault) wt.getWorldPanel().getMappainter()).getPathsCurved());
                menuWorldShowGrid.setState(((MapPainterDefault) wt.getWorldPanel().getMappainter()).isGridEnabled());
                menuWorldShowCursor.setState(wt.getWorldPanel().isCursorEnabled());
            }
        } else {
            final String message = StringHelper.join(getClass().getName(), ": ChangeEvent not recognized");
            Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, message);
            JOptionPane.showMessageDialog(this, message, "MUD Map error", JOptionPane.ERROR_MESSAGE);
        }
        updateMenus();
    }

    @Override
    public boolean dispatchKeyEvent(final KeyEvent e) {
        // ctrl modifier
        if (e.isControlDown()) {
            // go to search box (side bar of current world tab)
            if (e.getKeyCode() == KeyEvent.VK_F) {
                final WorldTab wt = getSelectedTab();
                if (wt != null) {
                    wt.focusSidePanelSearchBox();
                }
                return true;
            }
        }

        // no modifier
        if (KeyEvent.KEY_PRESSED == e.getID() && e.isControlDown()) {
            switch (e.getKeyCode()) {
            case KeyEvent.VK_S: // save world
            {
                final WorldTab wt = getSelectedTab();
                if (wt != null) {
                    wt.save();
                }
                return true;
            }
            case KeyEvent.VK_O: // open world
            {
                final OpenWorldDialog dlg = new OpenWorldDialog(this);
                dlg.setVisible();
                return true;
            }
            }
        }
        return false;
    }

}
